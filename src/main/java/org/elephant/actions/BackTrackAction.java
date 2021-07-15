/*******************************************************************************
 * Copyright (C) 2020, Ko Sugawara
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.elephant.actions;

import java.net.HttpURLConnection;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.elephant.actions.mixins.TimepointActionMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import mpicbg.spim.data.sequence.VoxelDimensions;

/**
 * Track the highlighted spot backward, creating a new spot based on a flow
 * estimation.
 * 
 * @author Ko Sugawara
 */
public class BackTrackAction extends AbstractElephantAction
		implements BdvDataMixin, ElephantGraphTagActionMixin, ElephantStateManagerMixin, GraphChangeActionMixin, TimepointActionMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] back track";

	private static final String[] MENU_KEYS = new String[] { "alt C" };

	@Override
	public String[] getMenuKeys()
	{
		return MENU_KEYS;
	}

	public BackTrackAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final int timepoint = getCurrentTimepoint( 0 );
		if ( timepoint < 1 )
			return;
		final VoxelDimensions voxelSize = getVoxelDimensions();
		final JsonArray scales = new JsonArray()
				.add( voxelSize.dimension( 0 ) )
				.add( voxelSize.dimension( 1 ) )
				.add( voxelSize.dimension( 2 ) );
		final JsonObject jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_FLOW_MODEL_NAME, getMainSettings().getFlowModelName() )
				.add( JSON_KEY_DEBUG, getMainSettings().getDebug() )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() );
		if ( getMainSettings().getPatch() )
		{
			jsonRootObject.add( JSON_KEY_PATCH, new JsonArray()
					.add( getMainSettings().getPatchSizeX() )
					.add( getMainSettings().getPatchSizeY() )
					.add( getMainSettings().getPatchSizeZ() ) );
		}
		final Spot spotRef = getGraph().vertexRef();
		try
		{
			final double[] pos = new double[ 3 ];
			final double[][] cov = new double[ 3 ][ 3 ];
			final double[] cov1d = new double[ 9 ];
			getGraph().getLock().readLock().lock();
			try
			{
				final Spot spot = getAppModel().getHighlightModel().getHighlightedVertex( spotRef );
				if ( spot == null )
					return;
				spot.localize( pos );
				spot.getCovariance( cov );
				for ( int i = 0; i < 3; i++ )
					for ( int j = 0; j < 3; j++ )
						cov1d[ i * 3 + j ] = cov[ i ][ j ];
				final int id = spot.getInternalPoolIndex();
				final JsonObject jsonSpot = Json.object()
						.add( "pos", Json.array( pos ) )
						.add( "covariance", Json.array( cov1d ) )
						.add( "id", id );
				final JsonArray jsonSpots = Json.array().add( jsonSpot );
				jsonRootObject.set( JSON_KEY_TIMEPOINT, timepoint );
				jsonRootObject.set( "spots", jsonSpots );
			}
			finally
			{
				getGraph().getLock().readLock().unlock();
			}
			Unirest.post( getEndpointURL( ENDPOINT_PREDICT_FLOW ) )
					.body( jsonRootObject.toString() )
					.asStringAsync( new Callback< String >()
					{

						@Override
						public void failed( final UnirestException e )
						{
							getLogger().severe( ExceptionUtils.getStackTrace( e ) );
							getLogger().severe( "The request has failed" );
						}

						@Override
						public void completed( final HttpResponse< String > response )
						{
							if ( response.getStatus() == HttpURLConnection.HTTP_OK )
							{
								final JsonObject rootObject = Json.parse( response.getBody() ).asObject();
								final JsonArray jsonSpots = rootObject.get( "spots" ).asArray();
								final JsonObject jsonSpot = jsonSpots.get( 0 ).asObject();
								final int spotId = jsonSpot.get( "id" ).asInt();
								final Spot spotRef = getGraph().vertexRef();
								final Spot newSpotRef = getGraph().vertexRef();
								final Link edgeRef = getGraph().edgeRef();
								getGraph().getLock().writeLock().lock();
								try
								{
									final Spot spot = getGraph().vertices().stream().filter( s -> s.getInternalPoolIndex() == spotId ).findFirst().orElse( null );
									if ( spot == null )
									{
										final String msg = "spot " + spot + " was not found";
										getLogger().info( msg );
										showTextOverlayAnimator( msg, 3000, TextPosition.CENTER );
									}
									else
									{
										spotRef.refTo( spot );
										final JsonArray jsonPositions = jsonSpot.get( "pos" ).asArray();
										for ( int j = 0; j < 3; j++ )
											pos[ j ] = jsonPositions.get( j ).asDouble();
										final Spot newSpot = getGraph().addVertex( newSpotRef ).init( timepoint - 1, pos, cov );
										final Tag detectionFNTag = getTag( getDetectionTagSet(), DETECTION_FN_TAG_NAME );
										final Tag trackingApprovedTag = getTag( getTrackingTagSet(), TRACKING_APPROVED_TAG_NAME );
										getVertexTagMap( getDetectionTagSet() ).set( newSpot, detectionFNTag );
										final ObjTagMap< Spot, Tag > tagMapTrackingSpot = getVertexTagMap( getTrackingTagSet() );
										tagMapTrackingSpot.set( newSpot, trackingApprovedTag );
										final Link edge = getGraph().addEdge( newSpot, spot, edgeRef ).init();
										final ObjTagMap< Link, Tag > tagMapTrackingLink = getEdgeTagMap( getTrackingTagSet() );
										tagMapTrackingLink.set( edge, trackingApprovedTag );
									}
								}
								finally
								{
									getModel().setUndoPoint();
									getGraph().getLock().writeLock().unlock();
									getGraph().getLock().readLock().lock();
									try
									{
										getGroupHandle().getModel( getAppModel().NAVIGATION ).notifyNavigateToVertex( newSpotRef );
									}
									finally
									{
										getGraph().getLock().readLock().unlock();
									}
									notifyGraphChanged();
									getGraph().releaseRef( spotRef );
									getGraph().releaseRef( newSpotRef );
									getGraph().releaseRef( edgeRef );
								}
							}
							else
							{
								final StringBuilder sb = new StringBuilder( response.getStatusText() );
								if ( response.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR )
								{
									sb.append( ": " );
									sb.append( Json.parse( response.getBody() ).asObject().get( "error" ).asString() );
								}
								showTextOverlayAnimator( sb.toString(), 3000, TextPosition.CENTER );
								getLogger().severe( sb.toString() );
							}
						}

						@Override
						public void cancelled()
						{
							getLogger().info( "The request has been cancelled" );
						}

					} );
		}
		finally
		{
			getGraph().releaseRef( spotRef );
		}
	}

}

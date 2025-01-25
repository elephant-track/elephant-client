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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.elephant.actions.PredictSpotsAction.SpotStruct;
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantGraphActionMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.SpatioTemporalIndexActionMinxin;
import org.mastodon.collection.util.HashBimap;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatialIndexImp;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.HttpResponse;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.RealPoint;
import net.imglib2.neighborsearch.NearestNeighborSearch;

/**
 * Track the highlighted spot backward, creating a new spot based on a flow
 * estimation.
 * 
 * @author Ko Sugawara
 */
public class BackTrackAction extends AbstractElephantDatasetAction
		implements ElephantGraphActionMixin, ElephantGraphTagActionMixin, SpatioTemporalIndexActionMinxin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] back track";

	private static final String[] MENU_KEYS = new String[] { "alt C" };

	private static final String DESCRIPTION = "Track the highlighted vertex backward in time.";

	private JsonObject jsonRootObject;

	private final double[] pos = new double[ 3 ];

	private final double[][] cov = new double[ 3 ][ 3 ];

	private final double[] cov1d = new double[ 9 ];

	private int spotPoolIndex;

	private Iterator< Integer > timepointIterator;

	/*
	 * Command description.
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add(
					NAME,
					MENU_KEYS,
					DESCRIPTION );
		}
	}

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
	boolean prepare()
	{
		final int timepointEnd = getCurrentTimepoint( 0 );
		final int timeRange = getActionStateManager().isLivemode() ? 1 : getMainSettings().getTimeRange();
		final int timepointStart = Math.max( 1, timepointEnd - timeRange + 1 );
		timepointIterator = IntStream.rangeClosed( timepointStart, timepointEnd )
				.boxed().sorted( Collections.reverseOrder() ).iterator();
		getActionStateManager().setAborted( false );
		if ( timepointEnd < 1 )
			return false;
		final VoxelDimensions voxelSize = getVoxelDimensions();
		final JsonArray scales = new JsonArray()
				.add( voxelSize.dimension( 0 ) )
				.add( voxelSize.dimension( 1 ) )
				.add( voxelSize.dimension( 2 ) );
		final Dimensions dimensions = getRescaledDimensions();
		final JsonArray inputSize = new JsonArray()
				.add( dimensions.dimension( 0 ) )
				.add( dimensions.dimension( 1 ) )
				.add( dimensions.dimension( 2 ) );
		jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_MODEL_NAME, getMainSettings().getFlowModelName() )
				.add( JSON_KEY_DEBUG, getMainSettings().getDebug() )
				.add( JSON_KEY_OUTPUT_PREDICTION, getMainSettings().getOutputPrediction() )
				.add( JSON_KEY_MAX_DISPLACEMENT, getMainSettings().getMaxDisplacement() )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
				.add( JSON_KEY_INPUT_SIZE, inputSize )
				.add( JSON_KEY_CACHE_MAXBYTES, getMainSettings().getCacheMaxbytes() )
				.add( JSON_KEY_IS_3D, !is2D() )
				.add( JSON_KEY_USE_MEMMAP, getMainSettings().getUseMemmap() )
				.add( JSON_KEY_PATCH, new JsonArray()
						.add( getMainSettings().getPatchSizeX() )
						.add( getMainSettings().getPatchSizeY() )
						.add( getMainSettings().getPatchSizeZ() ) );
		final long[] cropOrigin = new long[ 3 ];
		final long[] cropSize = new long[ 3 ];
		calculateCropBoxAround( pos, cropOrigin, cropSize );
		jsonRootObject.add( JSON_KEY_PREDICT_CROP_BOX, Json.array()
				.add( cropOrigin[ 0 ] ).add( cropOrigin[ 1 ] ).add( cropOrigin[ 2 ] )
				.add( cropSize[ 0 ] ).add( cropSize[ 1 ] ).add( cropSize[ 2 ] ) );
		final Spot spotRef = getGraph().vertexRef();
		getGraph().getLock().readLock().lock();
		try
		{
			final Spot spot = getAppModel().getHighlightModel().getHighlightedVertex( spotRef );
			if ( spot == null )
				return false;
			spot.localize( pos );
			spot.getCovariance( cov );
			spotPoolIndex = spot.getInternalPoolIndex();
		}
		finally
		{
			getGraph().getLock().readLock().unlock();
			getGraph().releaseRef( spotRef );
		}
		return true;
	}

	@Override
	public void processDataset()
	{
		processNext();
	}

	private void processNext()
	{
		if ( !timepointIterator.hasNext() )
			return;

		final int timepoint = timepointIterator.next();
		for ( int i = 0; i < 3; i++ )
			for ( int j = 0; j < 3; j++ )
				cov1d[ i * 3 + j ] = cov[ i ][ j ];
		final JsonObject jsonSpot = Json.object()
				.add( "pos", Json.array( pos ) )
				.add( "covariance", Json.array( cov1d ) )
				.add( "id", spotPoolIndex );
		final JsonArray jsonSpots = Json.array().add( jsonSpot );
		jsonRootObject.set( JSON_KEY_TIMEPOINT, timepoint );
		jsonRootObject.set( "spots", jsonSpots );

		if ( getMainSettings().getUseOpticalflow() )
		{
			try
			{
				postAsStringAsync( getEndpointURL( ENDPOINT_FLOW_PREDICT ), jsonRootObject.toString(),
						response -> {
							if ( response.getStatus() == HttpURLConnection.HTTP_OK )
							{
								final JsonObject rootObject = Json.parse( response.getBody() ).asObject();
								backTrackAt( rootObject.get( "spots" ).asArray().get( 0 ).asObject(), timepoint );
								if ( getActionStateManager().isAborted() )
									showTextOverlayAnimator( "Aborted", 3000, TextPosition.BOTTOM_RIGHT );
								else
									processNext();
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
								getClientLogger().severe( sb.toString() );
							}
						} );
			}
			catch ( final ElephantConnectException e )
			{
				// already handled by UnirestMixin
			}
		}
		else
		{
			backTrackAt( jsonSpot, timepoint );
			if ( getActionStateManager().isAborted() )
				showTextOverlayAnimator( "Aborted", 3000, TextPosition.BOTTOM_RIGHT );
			else
				processNext();
		}

	}

	private void backTrackAt( final JsonObject jsonSpot, final int timepoint )
	{
		final int spotId = jsonSpot.get( "id" ).asInt();
		final Spot orgSpotRef = getGraph().vertexRef();
		final Spot newSpotRef = getGraph().vertexRef();
		final Link edgeRef = getGraph().edgeRef();
		Spot targetSpot = null;
		getGraph().getLock().writeLock().lock();
		try
		{
			final Spot spot = getGraph().vertices().stream().filter( s -> s.getInternalPoolIndex() == spotId ).findFirst().orElse( null );
			if ( spot == null )
			{
				final String msg = "spot " + spot + " was not found";
				getClientLogger().info( msg );
				showTextOverlayAnimator( msg, 3000, TextPosition.CENTER );
			}
			else
			{
				orgSpotRef.refTo( spot );
				final JsonArray jsonPositions = jsonSpot.get( "pos" ).asArray();
				for ( int j = 0; j < 3; j++ )
					pos[ j ] = jsonPositions.get( j ).asDouble();
				final SpatialIndex< Spot > spatialIndex = getSpatioTemporalIndex().getSpatialIndex( timepoint - 1 );
				final NearestNeighborSearch< Spot > nns = spatialIndex.getNearestNeighborSearch();
				final RealEllipsoid predictedPosition = new RealEllipsoid( pos, cov );
				nns.search( predictedPosition );
				targetSpot = nns.getSampler().get();
				if ( targetSpot != null && targetSpot.outgoingEdges().size() < getMainSettings().getNNMaxEdges()
						&& nns.getDistance() < getMainSettings().getNNLinkingThreshold() )
				{
					final Tag trackingUnlabeledTag = getTag( getTrackingTagSet(), TRACKING_UNLABELED_TAG_NAME );
					final Link edge = getGraph().addEdge( targetSpot, spot, edgeRef ).init();
					final ObjTagMap< Link, Tag > tagMapTrackingLink = getEdgeTagMap( getTrackingTagSet() );
					tagMapTrackingLink.set( edge, trackingUnlabeledTag );
				}
				else
				{
					targetSpot = null;
					JsonObject jsonRootObjectDetection = Json.object()
							.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
							.add( JSON_KEY_MODEL_NAME, getMainSettings().getDetectionModelName() )
							.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
							.add( JSON_KEY_SCALES, jsonRootObject.get( JSON_KEY_SCALES ) )
							.add( JSON_KEY_C_RATIO, getMainSettings().getCenterRatio() )
							.add( JSON_KEY_P_THRESH, getMainSettings().getProbThreshold() )
							.add( JSON_KEY_R_MIN, getMainSettings().getMinRadius() )
							.add( JSON_KEY_R_MAX, getMainSettings().getMaxRadius() )
							.add( JSON_KEY_DEBUG, getMainSettings().getDebug() )
							.add( JSON_KEY_OUTPUT_PREDICTION, getMainSettings().getOutputPrediction() )
							.add( JSON_KEY_USE_MEDIAN, getMainSettings().getMedianCorrection() )
							.add( JSON_KEY_IS_PAD, getMainSettings().getPad() )
							.add( JSON_KEY_CACHE_MAXBYTES, getMainSettings().getCacheMaxbytes() )
							.add( JSON_KEY_IS_3D, !is2D() )
							.add( JSON_KEY_USE_2D_MODEL, getMainSettings().getUse2dModel() )
							.add( JSON_KEY_USE_MEMMAP, getMainSettings().getUseMemmap() )
							.add( JSON_KEY_BATCH_SIZE, getMainSettings().getBatchSize() )
							.add( JSON_KEY_INPUT_SIZE, jsonRootObject.get( JSON_KEY_INPUT_SIZE ) )
							.add( JSON_KEY_PATCH, new JsonArray()
									.add( getMainSettings().getPatchSizeX() )
									.add( getMainSettings().getPatchSizeY() )
									.add( getMainSettings().getPatchSizeZ() ) )
							.set( JSON_KEY_TIMEPOINT, timepoint - 1 );
					final long[] cropOrigin = new long[ 3 ];
					final long[] cropSize = new long[ 3 ];
					calculateCropBoxAround( pos, cropOrigin, cropSize );
					jsonRootObjectDetection.add( JSON_KEY_PREDICT_CROP_BOX, Json.array()
							.add( cropOrigin[ 0 ] ).add( cropOrigin[ 1 ] ).add( cropOrigin[ 2 ] )
							.add( cropSize[ 0 ] ).add( cropSize[ 1 ] ).add( cropSize[ 2 ] ) );
					HttpResponse< String > responseDetectioin =
							postAsString( getEndpointURL( ENDPOINT_DETECTION_PREDICT ), jsonRootObjectDetection.toString() );
					if ( responseDetectioin.getStatus() == HttpURLConnection.HTTP_OK )
					{
						final String body = responseDetectioin.getBody();
						final JsonObject jsonDetectionObject = Json.parse( body ).asObject();
						if ( Json.parse( body ).asObject().get( "completed" ).asBoolean() )
						{
							final Collection< RealEllipsoid > refSet = new HashSet< RealEllipsoid >();
							final JsonArray jsonSpotsDetection = jsonDetectionObject.get( "spots" ).asArray();
							final SpotStruct jsonRef = new SpotStruct( pos, cov );
							for ( final JsonValue jsonValue : jsonSpotsDetection )
							{
								PredictSpotsAction.getNextFromJson( jsonRef, jsonValue.asObject() );
								refSet.add( new RealEllipsoid( jsonRef.pos, jsonRef.covariance ) );
							}
							if ( !refSet.isEmpty() )
							{
								final HashBimap< RealEllipsoid > bimap = new HashBimap<>( RealEllipsoid.class );
								final SpatialIndexImp< RealEllipsoid > spatialIndexDetection = new SpatialIndexImp<>( refSet, bimap );
								final NearestNeighborSearch< RealEllipsoid > nnsDetection =
										spatialIndexDetection.getNearestNeighborSearch();
								nnsDetection.search( predictedPosition );
								if ( nnsDetection.getDistance() < getMainSettings().getNNLinkingThreshold() )
								{
									final RealEllipsoid nearestEllipsoid = nnsDetection.getSampler().get();
									targetSpot = getGraph().addVertex( newSpotRef ).init( timepoint - 1, nearestEllipsoid.getPosition(),
											nearestEllipsoid.getCovariance() );
								}
								else if ( getMainSettings().getUseInterpolation() )
								{
									targetSpot = getGraph().addVertex( newSpotRef ).init( timepoint - 1, predictedPosition.getPosition(),
											predictedPosition.getCovariance() );
								}
							}
							if ( targetSpot == null )
							{
								while ( timepointIterator.hasNext() )
									timepointIterator.next();
								showTextOverlayAnimator( "Target spot not found", 3000, TextPosition.CENTER );
								return;
							}
							final Tag detectionTag = getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME );
							final Tag trackingTag = getTag( getTrackingTagSet(), TRACKING_UNLABELED_TAG_NAME );
							getVertexTagMap( getDetectionTagSet() ).set( targetSpot, detectionTag );
							final ObjTagMap< Spot, Tag > tagMapTrackingSpot = getVertexTagMap( getTrackingTagSet() );
							tagMapTrackingSpot.set( targetSpot, trackingTag );
							final Link edge = getGraph().addEdge( targetSpot, spot, edgeRef ).init();
							final ObjTagMap< Link, Tag > tagMapTrackingLink = getEdgeTagMap( getTrackingTagSet() );
							tagMapTrackingLink.set( edge, trackingTag );
						}
					}
					else
					{
						final StringBuilder sb = new StringBuilder( responseDetectioin.getStatusText() );
						if ( responseDetectioin.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR )
						{
							sb.append( ": " );
							sb.append( Json.parse( responseDetectioin.getBody() ).asObject().get( "error" ).asString() );
						}
						showTextOverlayAnimator( sb.toString(), 3000, TextPosition.CENTER );
						getClientLogger().severe( sb.toString() );
					}
				}
				if ( targetSpot != null )
				{
					targetSpot.localize( pos );
					targetSpot.getCovariance( cov );
					spotPoolIndex = targetSpot.getInternalPoolIndex();
					newSpotRef.refTo( targetSpot );
					if ( 0 < targetSpot.incomingEdges().size() )
					{
						while ( timepointIterator.hasNext() )
							timepointIterator.next();
						showTextOverlayAnimator( "Target spot has an incoming edge", 3000, TextPosition.CENTER );
						return;
					}
				}
			}
		}
		catch ( ElephantConnectException e )
		{
			// already handled by UnirestMixin
		}
		finally
		{
			getModel().setUndoPoint();
			getGraph().getLock().writeLock().unlock();
			if ( targetSpot != null )
			{
				getGraph().getLock().readLock().lock();
				try
				{
					getGroupHandle().getModel( getAppModel().NAVIGATION ).notifyNavigateToVertex( newSpotRef );
				}
				finally
				{
					getGraph().getLock().readLock().unlock();
				}
			}
			notifyGraphChanged();
			getGraph().releaseRef( orgSpotRef );
			getGraph().releaseRef( newSpotRef );
			getGraph().releaseRef( edgeRef );
		}
	}

	class RealEllipsoid extends RealPoint
	{
		private final double[][] covariance;

		RealEllipsoid( final double[] position, final double[][] covariance )
		{
			super( position );
			this.covariance = new double[ covariance.length ][];
			for ( int i = 0; i < covariance.length; i++ )
				this.covariance[ i ] = covariance[ i ].clone();
		}

		double[] getPosition()
		{
			return position;
		}

		double[][] getCovariance()
		{
			return covariance;
		}

	}

}

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantGraphActionMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.ElephantUtils;
import org.elephant.actions.mixins.EllipsoidActionMixin;
import org.elephant.actions.mixins.SpatioTemporalIndexActionMinxin;
import org.elephant.actions.mixins.TimepointActionMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.collection.RefCollection;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.spatial.SpatialIndex;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RealPoint;
import net.imglib2.neighborsearch.NearestNeighborSearch;

/**
 * A detection workflow based on a voxel classification.
 * 
 * @author Ko Sugawara
 */
public class PredictSpotsAction extends AbstractElephantAction
		implements BdvDataMixin, EllipsoidActionMixin, ElephantConstantsMixin, ElephantGraphActionMixin, ElephantSettingsMixin, ElephantStateManagerMixin, ElephantGraphTagActionMixin,
		SpatioTemporalIndexActionMinxin, TimepointActionMixin, UIActionMixin, URLMixin, WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] predict spots%s";

	private static final String MENU_TEXT = "Predict Spots";

	public enum PredictSpotsActionMode
	{
		ENTIRE( String.format( NAME, "" ), new String[] { "alt F" } ),
		AROUND_MOUSE( String.format( NAME, " (around selection)" ), new String[] { "alt shift F" } );

		private String name;

		private String[] menuKeys;

		private PredictSpotsActionMode( final String name, final String[] menuKeys )
		{
			this.name = name;
			this.menuKeys = menuKeys;
		}

		public String getName()
		{
			return name;
		}

		public String[] getMenuKeys()
		{
			return menuKeys;
		}
	}

	private final PredictSpotsActionMode mode;

	private final BdvViewMouseMotionService mouseMotionService;

	private VoxelDimensions cropBoxOrigin;

	private VoxelDimensions cropBoxSize;

	private JsonObject jsonRootObject;

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	@Override
	public String[] getMenuKeys()
	{
		return mode.getMenuKeys();
	}

	public PredictSpotsAction( final PredictSpotsActionMode mode, final BdvViewMouseMotionService mouseMotionService )
	{
		super( mode.getName() );
		this.mode = mode;
		this.mouseMotionService = mouseMotionService;
	}

	@Override
	public void process()
	{
		final int timepointEnd = getCurrentTimepoint( 0 );
		final int timeRange = getStateManager().isLivemode() ? 1 : getMainSettings().getTimeRange();
		final int timepointStart = Math.max( 0, timepointEnd - ( timeRange - 1 ) );
		ElephantActionStateManager.INSTANCE.setAborted( false );
		final VoxelDimensions voxelSize = getVoxelDimensions();
		final JsonArray scales = new JsonArray()
				.add( voxelSize.dimension( 0 ) )
				.add( voxelSize.dimension( 1 ) )
				.add( voxelSize.dimension( 2 ) );
		jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_SEG_MODEL_NAME, getMainSettings().getSegModelName() )
				.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_C_RATIO, getMainSettings().getCenterRatio() )
				.add( JSON_KEY_P_THRESH, getMainSettings().getProbThreshold() )
				.add( JSON_KEY_R_MIN, getMainSettings().getMinRadius() )
				.add( JSON_KEY_R_MAX, getMainSettings().getMaxRadius() )
				.add( JSON_KEY_DEBUG, getMainSettings().getDebug() )
				.add( JSON_KEY_OUTPUT_PREDICTION, getMainSettings().getOutputPrediction() )
				.add( JSON_KEY_USE_MEDIAN, getMainSettings().getMedianCorrection() )
				.add( JSON_KEY_IS_PAD, getMainSettings().getPad() )
				.add( JSON_KEY_IS_3D, !is2D() )
				.add( JSON_KEY_USE_2D_MODEL, getMainSettings().getUse2dModel() );
		if ( getMainSettings().getPatch() )
		{
			jsonRootObject.add( JSON_KEY_PATCH, new JsonArray()
					.add( getMainSettings().getPatchSizeX() )
					.add( getMainSettings().getPatchSizeY() )
					.add( getMainSettings().getPatchSizeZ() ) );
		}

		if ( mode == PredictSpotsActionMode.AROUND_MOUSE )
		{
			final double[] pos = new double[ 3 ];
			mouseMotionService.getMousePositionGlobal( pos );
			final long[] cropOrigin = new long[ 3 ];
			final long[] cropSize = new long[ 3 ];
			calculateCropBoxAround( pos, cropOrigin, cropSize );
			jsonRootObject.add( JSON_KEY_PREDICT_CROP_BOX, Json.array()
					.add( cropOrigin[ 0 ] ).add( cropOrigin[ 1 ] ).add( cropOrigin[ 2 ] )
					.add( cropSize[ 0 ] ).add( cropSize[ 1 ] ).add( cropSize[ 2 ] ) );
			cropBoxOrigin = new FinalVoxelDimensions(
					getVoxelDimensions().unit(),
					cropOrigin[ 0 ] * voxelSize.dimension( 0 ),
					cropOrigin[ 1 ] * voxelSize.dimension( 1 ),
					cropOrigin[ 2 ] * voxelSize.dimension( 2 ) );
			cropBoxSize = new FinalVoxelDimensions(
					getVoxelDimensions().unit(),
					cropSize[ 0 ] * voxelSize.dimension( 0 ),
					cropSize[ 1 ] * voxelSize.dimension( 1 ),
					cropSize[ 2 ] * voxelSize.dimension( 2 ) );
		}
		predictSpotsAt( timepointStart, timepointEnd );
	}

	private void predictSpotsAt( final int timepoint, final int timepointEnd )
	{
		if ( timepointEnd < timepoint )
			return;
		jsonRootObject.set( JSON_KEY_TIMEPOINT, timepoint );
		Unirest.post( getEndpointURL( ENDPOINT_PREDICT_SEG ) )
				.body( jsonRootObject.toString() )
				.asStringAsync( new Callback< String >()
				{

					@Override
					public void failed( final UnirestException e )
					{
						getLogger().severe( ExceptionUtils.getStackTrace( e ) );
						getLogger().severe( "The request has failed" );
						showTextOverlayAnimator( e.getLocalizedMessage(), 3000, TextPosition.CENTER );
					}

					@Override
					public void completed( final HttpResponse< String > response )
					{
						if ( response.getStatus() == 200 )
						{
							final String body = response.getBody();
							final RefCollection< Spot > spots = getGraph().vertices();
							Predicate< Spot > predicate = spot -> spot.getTimepoint() == timepoint;
							if ( mode == PredictSpotsActionMode.AROUND_MOUSE )
								predicate = predicate.and( spot -> ElephantUtils.spotIsInside( spot, cropBoxOrigin, cropBoxSize ) );
							refreshLabels( spots, predicate );
							predicate = predicate.and( spot -> getVertexTagMap( getDetectionTagSet() ).get( spot ) == getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME ) );
							predicate = predicate.and( spot -> getVertexTagMap( getTrackingTagSet() ).get( spot ) != getTag( getTrackingTagSet(), TRACKING_APPROVED_TAG_NAME ) );
							removeSpots( spots, predicate );
							addSpotsFromJsonString( body );
							summary( timepoint );
							showTextOverlayAnimator( String.format( "Detected at frame %d", timepoint ), 1000, TextPosition.BOTTOM_RIGHT );
							if ( getStateManager().isAborted() )
								showTextOverlayAnimator( "Aborted", 3000, TextPosition.BOTTOM_RIGHT );
							else
								predictSpotsAt( timepoint + 1, timepointEnd );
						}
						else
						{
							final StringBuilder sb = new StringBuilder( response.getStatusText() );
							if ( response.getStatus() == 500 )
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

	private static enum SpotEditMode
	{
		SKIP,
		CREATE,
		REFIT
	}

	private static class SpotStruct
	{
		int t;

		final double[] pos;

		final double[][] covariance;

		private SpotStruct( final double[] pos, final double[][] covariance )
		{
			this.pos = pos;
			this.covariance = covariance;
		}
	}

	private void getNextFromJson( final SpotStruct jsonRef, final JsonObject jsonObject )
	{
		final JsonArray posArray = jsonObject.get( "pos" ).asArray();
		final JsonArray covArray = jsonObject.get( "covariance" ).asArray();
		for ( int i = 0; i < 3; i++ )
		{
			jsonRef.pos[ i ] = posArray.get( i ).asDouble();
			for ( int j = 0; j < 3; j++ )
			{
				jsonRef.covariance[ i ][ j ] = covArray.get( i * 3 + j ).asDouble();
			}
		}
		jsonRef.t = jsonObject.get( "t" ).asInt();
	}

	private void refreshLabels( final Collection< Spot > spots, final Predicate< Spot > filter )
	{
		final Map< Tag, Tag > map = new HashMap<>();
		map.put( getTag( getDetectionTagSet(), DETECTION_TP_TAG_NAME ), getTag( getDetectionTagSet(), DETECTION_FN_TAG_NAME ) );
		map.put( getTag( getDetectionTagSet(), DETECTION_FP_TAG_NAME ), getTag( getDetectionTagSet(), DETECTION_TN_TAG_NAME ) );
		map.put( getTag( getDetectionTagSet(), DETECTION_FB_TAG_NAME ), getTag( getDetectionTagSet(), DETECTION_TB_TAG_NAME ) );

		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			for ( final Spot spot : spots )
			{
				if ( filter == null || filter.test( spot ) )
				{
					final Tag currentTag = getVertexTagMap( getDetectionTagSet() ).get( spot );
					if ( map.containsKey( currentTag ) )
						getVertexTagMap( getDetectionTagSet() ).set( spot, map.get( currentTag ) );
				}
			}
		}
		finally
		{
			getStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
		}
	}

	private void addSpotsFromJsonString( final String jsonString )
	{
		getGraph().getLock().readLock().lock();
		try
		{
			final ObjTagMap< Spot, Tag > tagMapDetection = getVertexTagMap( getDetectionTagSet() );
			final ObjTagMap< Spot, Tag > tagMapTracking = getVertexTagMap( getTrackingTagSet() );

			final Spot ref = getGraph().vertexRef();

			final double[] pos = new double[ 3 ];
			final double[][] covariance = new double[ 3 ][ 3 ];
			final SpotStruct jsonRef = new SpotStruct( pos, covariance );

			final JsonObject jsonRootObject = Json.parse( jsonString ).asObject();
			final JsonArray jsonSpots = jsonRootObject.get( "spots" ).asArray();

			final Tag tpTag = getTag( getDetectionTagSet(), DETECTION_TP_TAG_NAME );
			final Tag fpTag = getTag( getDetectionTagSet(), DETECTION_FP_TAG_NAME );
			final Tag fbTag = getTag( getDetectionTagSet(), DETECTION_FB_TAG_NAME );
			final Tag unlabeledTag = getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME );
			final Tag trackingUnlabeledTag = getTag( getTrackingTagSet(), TRACKING_UNLABELED_TAG_NAME );
			for ( final JsonValue jsonValue : jsonSpots )
			{
				getNextFromJson( jsonRef, jsonValue.asObject() );
				final SpatialIndex< Spot > spatialIndex = getSpatioTemporalIndex().getSpatialIndex( jsonRef.t );
				final NearestNeighborSearch< Spot > nns = spatialIndex.getNearestNeighborSearch();
				nns.search( new RealPoint( jsonRef.pos ) );
				final Spot nearestSpot = nns.getSampler().get();
				SpotEditMode editMode = SpotEditMode.CREATE;
				if ( nns.getDistance() < getMainSettings().getSuppressionDistance() )
				{
					final Tag tag = tagMapDetection.get( nearestSpot );
					if ( tag != null )
					{
						editMode = SpotEditMode.SKIP;
						switch ( tag.label() )
						{
						case DETECTION_FN_TAG_NAME:
							tagMapDetection.set( nearestSpot, tpTag );
							break;
						case DETECTION_TN_TAG_NAME:
							tagMapDetection.set( nearestSpot, fpTag );
							break;
						case DETECTION_TB_TAG_NAME:
							tagMapDetection.set( nearestSpot, fbTag );
							break;
						case DETECTION_UNLABELED_TAG_NAME:
							final Tag tagTracking = tagMapTracking.get( nearestSpot );
							if ( tagTracking == null || !tagTracking.label().equals( TRACKING_APPROVED_TAG_NAME ) )
							{
								nearestSpot.getCovariance( covariance );
								// if the new spot has the greater volume than the nearest spot
								if ( 0 < compareVolume( jsonRef.covariance, covariance ) )
									editMode = SpotEditMode.REFIT;
							}
							break;
						default:
							break;
						}
					}
					else
					{
						getLogger().info( nearestSpot + " does not have a valid tag" );
						editMode = SpotEditMode.SKIP;
					}
				}
				switch ( editMode )
				{
				case CREATE:
					getGraph().getLock().readLock().unlock();
					getGraph().getLock().writeLock().lock();
					getStateManager().setWriting( true );
					try
					{
						final Spot spot = getGraph().addVertex( ref ).init( jsonRef.t, jsonRef.pos, jsonRef.covariance );
						tagMapDetection.set( spot, unlabeledTag );
						tagMapTracking.set( spot, trackingUnlabeledTag );
						getGraph().getLock().readLock().lock();
					}
					finally
					{
						getStateManager().setWriting( false );
						getGraph().getLock().writeLock().unlock();
					}
					break;
				case REFIT:
					getGraph().getLock().readLock().unlock();
					getGraph().getLock().writeLock().lock();
					getStateManager().setWriting( true );
					try
					{
						ref.refTo( nearestSpot );
						ref.setPosition( jsonRef.pos );
						ref.setCovariance( jsonRef.covariance );
						getGraph().getLock().readLock().lock();
					}
					finally
					{
						getStateManager().setWriting( false );
						getGraph().getLock().writeLock().unlock();
					}
					break;
				case SKIP:
					break;
				default:
					throw new RuntimeException( "editMode is invalid: " + editMode );
				}
			}

			getGraph().releaseRef( ref );
		}
		catch ( final Exception e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		finally
		{
			getModel().setUndoPoint();
			getGraph().getLock().readLock().unlock();
			notifyGraphChanged();
		}
	}

	private void summary( final int timepoint )
	{
		getGraph().getLock().readLock().lock();
		try
		{
			getLogger().info( String.format( "FRAME: %d, TP: %d, FP: %d, TN: %d, FN: %d, TB: %d, FB: %d, unlabeled: %d",
					timepoint,
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_TP_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count(),
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_FP_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count(),
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_TN_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count(),
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_FN_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count(),
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_TB_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count(),
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_FB_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count(),
					getVerticesTaggedWith( getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME ) ).stream().filter( s -> s.getTimepoint() == timepoint ).count() ) );
		}
		finally
		{
			getGraph().getLock().readLock().unlock();
		}
	}

	/**
	 * Compare volumes of two ellipsoids represented with covariances
	 * 
	 * @param cov1
	 * @param cov2
	 * @return a negative integer, zero, or a positive integer as the first element
	 *         is less than, equal to, or greater than the second.
	 * 
	 */
	private int compareVolume( final double[][] cov1, final double[][] cov2 )
	{
		getEig().decomposeSymmetric( cov1 );
		final double[] eigVals1 = eig.getRealEigenvalues();
		getEig().decomposeSymmetric( cov2 );
		final double[] eigVals2 = eig.getRealEigenvalues();
		return Double.compare( arrayProduct( eigVals1 ), arrayProduct( eigVals2 ) );
	}

	private static double arrayProduct( double[] array )
	{
		double result = 0;
		for ( final double i : array )
			result *= i;
		return result;
	}

}

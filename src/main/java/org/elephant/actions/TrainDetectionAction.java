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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.elephant.actions.mixins.BdvContextMixin;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.TimepointMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure.Tag;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;

/**
 * Send a request for training a voxel-classification model.
 * 
 * There are three modes in the {@link TrainDetectionAction}.
 * 
 * <ol>
 * <li>LIVE: start live training</li>
 * <li>SELECTED: train with the labels within a time range specified in the
 * settings</li>
 * <li>All: train with the all labels in the dataset</li>
 * </ol>
 * 
 * @author Ko Sugawara
 */
public class TrainDetectionAction extends AbstractElephantDatasetAction
		implements BdvContextMixin, BdvDataMixin, ElephantConstantsMixin, ElephantGraphTagActionMixin, ElephantSettingsMixin, ElephantStateManagerMixin,
		TimepointMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private final TrainingMode trainingMode;

	private final BdvContextService bdvContextService;

	private JsonObject jsonRootObject;

	public static enum TrainingMode
	{
		LIVE( "[elephant] start live training", "Start Live Training" ),
		SELECTED( "[elephant] train detection model (selected timepoints)", "Train Detection Model (Selected Timepoints)" ),
		ALL( "[elephant] train detection model (all timepoints)", "Train Detection Model (All Timepoints)" );

		private String name;

		private String menuText;

		private TrainingMode( final String name, final String menuText )
		{
			this.name = name;
			this.menuText = menuText;
		}

		public String getName()
		{
			return name;
		}

		public String getMenuText()
		{
			return menuText;
		}
	}

	@Override
	public String getMenuText()
	{
		return trainingMode.getMenuText();
	}

	public TrainDetectionAction( final TrainingMode trainingMode, final BdvContextService bdvContextService )
	{
		super( trainingMode.getName() );
		this.trainingMode = trainingMode;
		this.bdvContextService = bdvContextService;
	}

	@Override
	public BdvContextService getBdvContextService()
	{
		return bdvContextService;
	}

	@Override
	boolean prepare()
	{
		if ( getActionStateManager().isLivemode() )
			return false;
		final int currentTimepoint = getCurrentTimepoint( 0 );
		getClientLogger().info( String.format( "Timepoint is %d.", currentTimepoint ) );
		final List< Integer > timepoints = new ArrayList<>( 0 );
		final JsonArray jsonSpots = Json.array();
		final List< Tag > tagsToProcess = new ArrayList< Tag >();
		getGraph().getLock().readLock().lock();
		try
		{
			tagsToProcess.add( getTag( getDetectionTagSet(), DETECTION_TP_TAG_NAME ) );
			tagsToProcess.add( getTag( getDetectionTagSet(), DETECTION_FP_TAG_NAME ) );
			tagsToProcess.add( getTag( getDetectionTagSet(), DETECTION_TN_TAG_NAME ) );
			tagsToProcess.add( getTag( getDetectionTagSet(), DETECTION_FN_TAG_NAME ) );
			tagsToProcess.add( getTag( getDetectionTagSet(), DETECTION_TB_TAG_NAME ) );
			tagsToProcess.add( getTag( getDetectionTagSet(), DETECTION_FB_TAG_NAME ) );
			final Iterable< Spot > spots;
			Predicate< Spot > spotFilter = spot -> timepoints.contains( spot.getTimepoint() );
			spotFilter = spotFilter.and( spot -> tagsToProcess.contains( getVertexTagMap( getDetectionTagSet() ).get( spot ) ) );
			switch ( trainingMode )
			{
			case LIVE:
				timepoints.add( currentTimepoint );
				spots = getVisibleVertices( currentTimepoint );
				if ( spots != null )
				{
					addSpotsToJson( spots, jsonSpots, spotFilter );
				}
				break;
			case SELECTED:
				final int timepointEnd = getCurrentTimepoint( 0 );
				final int timeRange = getActionStateManager().isLivemode() ? 1 : getMainSettings().getTimeRange();
				final int timepointStart = Math.max( 0, timepointEnd - timeRange + 1 );
				timepoints.addAll( IntStream.rangeClosed( timepointStart, timepointEnd ).boxed().collect( Collectors.toList() ) );
				addSpotsToJson( getGraph().vertices(), jsonSpots, spotFilter );
				break;
			case ALL:
				for ( int i = 0; i <= getAppModel().getMaxTimepoint(); i++ )
					timepoints.add( i );
				addSpotsToJson( getGraph().vertices(), jsonSpots, spotFilter );
				break;
			default:
				break;
			}
		}
		finally
		{
			getGraph().getLock().readLock().unlock();
		}
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
		final JsonArray cropSize = new JsonArray()
				.add( getMainSettings().getTrainingCropSizeX() )
				.add( getMainSettings().getTrainingCropSizeY() )
				.add( getMainSettings().getTrainingCropSizeZ() );
		final JsonArray classWeights = new JsonArray()
				.add( getMainSettings().getClassWeightBG() )
				.add( getMainSettings().getClassWeightBorder() )
				.add( getMainSettings().getClassWeightCenter() );
		jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_TRAIN_CROP_SIZE, cropSize )
				.add( JSON_KEY_SPOTS, jsonSpots )
				.add( JSON_KEY_MODEL_NAME, getMainSettings().getDetectionModelName() )
				.add( JSON_KEY_DEBUG, getMainSettings().getDebug() )
				.add( JSON_KEY_LR, getMainSettings().getLearningRate() )
				.add( JSON_KEY_BATCH_SIZE, getMainSettings().getBatchSize() )
				.add( JSON_KEY_N_CROPS, getMainSettings().getNumCrops() )
				.add( JSON_KEY_N_EPOCHS, getMainSettings().getNumEpochs() )
				.add( JSON_KEY_IS_LIVEMODE, trainingMode == TrainingMode.LIVE )
				.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
				.add( JSON_KEY_AUG_SCALE_FACTOR_BASE, getMainSettings().getAugScaleFactorBase() )
				.add( JSON_KEY_AUG_ROTATION_ANGLE, getMainSettings().getAugRotationAngle() )
				.add( JSON_KEY_AUG_CONTRAST, getMainSettings().getAugContrast() )
				.add( JSON_KEY_TIMEPOINT, currentTimepoint )
				.add( JSON_KEY_CLASS_WEIGHTS, classWeights )
				.add( JSON_KEY_FALSE_WEIGHT, getMainSettings().getFalseWeight() )
				.add( JSON_KEY_AUTO_BG_THRESH, getMainSettings().getAutoBgThreshold() )
				.add( JSON_KEY_C_RATIO, getMainSettings().getCenterRatio() )
				.add( JSON_KEY_CACHE_MAXBYTES, getMainSettings().getCacheMaxbytes() )
				.add( JSON_KEY_LOG_INTERVAL, getMainSettings().getLogInterval() )
				.add( JSON_KEY_LOG_DIR, getMainSettings().getDetectionLogName() )
				.add( JSON_KEY_IS_3D, !is2D() )
				.add( JSON_KEY_USE_MEMMAP, getMainSettings().getUseMemmap() )
				.add( JSON_KEY_INPUT_SIZE, inputSize );
		return true;
	}

	@Override
	public void processDataset()
	{
		if ( trainingMode == TrainingMode.LIVE )
		{
			getActionStateManager().setLivemode( true );
		}

		try
		{
			postAsStringAsync( getEndpointURL( ENDPOINT_DETECTION_TRAIN ), jsonRootObject.toString(),
					response -> {
						try
						{
							if ( response.getStatus() == HttpURLConnection.HTTP_OK )
							{
								final JsonObject rootObject = Json.parse( response.getBody() ).asObject();
								final String message = rootObject.get( "completed" ).asBoolean() ? "Training completed" : "Training aborted";
								showTextOverlayAnimator( message, 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
							}
							else
							{
								final StringBuilder sb = new StringBuilder( response.getStatusText() );
								if ( response.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR ||
										response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST )
								{
									sb.append( ": " );
									sb.append( Json.parse( response.getBody() ).asObject().get( "error" ).asString() );
								}
								showTextOverlayAnimator( sb.toString(), 3000, TextPosition.CENTER );
								getClientLogger().severe( sb.toString() );
							}
						}
						finally
						{
							getActionStateManager().setLivemode( false );
						}
					},
					e -> {
						handleError( e );
						getClientLogger().severe( "The request has failed" );
						getActionStateManager().setLivemode( false );
						showTextOverlayAnimator( e.getLocalizedMessage(), 3000, TextPosition.CENTER );
					},
					() -> {
						getClientLogger().info( "The request has been cancelled" );
						getActionStateManager().setLivemode( false );
					} );
		}
		catch ( final ElephantConnectException e )
		{
			getActionStateManager().setLivemode( false );
		}
	}

}

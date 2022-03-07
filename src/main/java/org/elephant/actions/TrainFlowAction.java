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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.TimepointMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.mastodon.mamut.model.Link;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import mpicbg.spim.data.sequence.VoxelDimensions;

/**
 * Send a request for training a flow model.
 * 
 * @author Ko Sugawara
 */
public class TrainFlowAction extends AbstractElephantDatasetAction
		implements BdvDataMixin, ElephantConstantsMixin, ElephantGraphTagActionMixin, TimepointMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] train flow model (selected timepoints)";

	private static final String MENU_TEXT = "Train Flow Model (Selected Timepoints)";

	private JsonObject jsonRootObject;

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public TrainFlowAction()
	{
		super( NAME );
	}

	@Override
	boolean prepare()
	{
		final int currentTimepoint = getCurrentTimepoint( 0 );
		getClientLogger().info( String.format( "Timepoint is %d.", currentTimepoint ) );
		final int timepointEnd = getCurrentTimepoint( 0 );
		final int timeRange = getMainSettings().getTimeRange();
		final int timepointStart = Math.max( 1, timepointEnd - timeRange + 1 );
		final List< Integer > timepoints = IntStream.rangeClosed( timepointStart, timepointEnd ).boxed().collect( Collectors.toList() );

		final JsonArray jsonSpots = Json.array();
		getGraph().getLock().readLock().lock();
		try
		{
			final Collection< Link > edges = getEdgesTaggedWith( getTag( getTrackingTagSet(), TRACKING_APPROVED_TAG_NAME ) );
			addEdgesToJsonFlow( edges, jsonSpots, timepoints );
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
		final JsonArray cropSize = new JsonArray()
				.add( getMainSettings().getTrainingCropSizeX() )
				.add( getMainSettings().getTrainingCropSizeY() )
				.add( getMainSettings().getTrainingCropSizeZ() );
		final JsonArray dimWeights = new JsonArray()
				.add( getMainSettings().getFlowWeightX() )
				.add( getMainSettings().getFlowWeightY() )
				.add( getMainSettings().getFlowWeightZ() );
		jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_SPOTS, jsonSpots )
				.add( JSON_KEY_MODEL_NAME, getMainSettings().getFlowModelName() )
				.add( JSON_KEY_DEBUG, getMainSettings().getDebug() )
				.add( JSON_KEY_BATCH_SIZE, getMainSettings().getBatchSize() )
				.add( JSON_KEY_N_CROPS, getMainSettings().getNumCrops() )
				.add( JSON_KEY_N_EPOCHS, getMainSettings().getNumEpochs() )
				.add( JSON_KEY_LR, getMainSettings().getLearningRate() )
				.add( JSON_KEY_MAX_DISPLACEMENT, getMainSettings().getMaxDisplacement() )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_TRAIN_CROP_SIZE, cropSize )
				.add( JSON_KEY_FLOW_DIM_WEIGHTS, dimWeights )
				.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
				.add( JSON_KEY_AUG_SCALE_FACTOR_BASE, getMainSettings().getAugScaleFactorBase() )
				.add( JSON_KEY_AUG_ROTATION_ANGLE, getMainSettings().getAugRotationAngle() )
				.add( JSON_KEY_CACHE_MAXBYTES, getMainSettings().getCacheMaxbytes() )
				.add( JSON_KEY_LOG_INTERVAL, getMainSettings().getLogInterval() )
				.add( JSON_KEY_LOG_DIR, getMainSettings().getFlowLogName() )
				.add( JSON_KEY_IS_3D, !is2D() )
				.add( JSON_KEY_USE_MEMMAP, getMainSettings().getUseMemmap() );
		return true;
	}

	@Override
	public void processDataset()
	{
		try
		{
			postAsStringAsync( getEndpointURL( ENDPOINT_FLOW_TRAIN ), jsonRootObject.toString(),
					response -> {
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
					} );
		}
		catch ( final ElephantConnectException e )
		{
			// already handled by UnirestMixin
		}
	}

}

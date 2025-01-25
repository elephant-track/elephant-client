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
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import mpicbg.spim.data.sequence.VoxelDimensions;

/**
 * Update voxel-classification labels used for training of a
 * voxel-classification model.
 * 
 * @author Ko Sugawara
 */
public class UpdateDetectionLabelsAction extends AbstractElephantDatasetAction
		implements BdvContextMixin, ElephantGraphTagActionMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] update detection labels";

	private static final String MENU_TEXT = "Update Detection Labels";

	private static final String[] MENU_KEYS = new String[] { "U" };

	private static final String DESCRIPTION = "Update labels for detection.";

	private final BdvContextService bdvContextService;

	private JsonObject jsonRootObject;

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
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	@Override
	public String[] getMenuKeys()
	{
		return MENU_KEYS;
	}

	public UpdateDetectionLabelsAction( final BdvContextService bdvContextService )
	{
		super( NAME );
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
		final int timepointEnd = getCurrentTimepoint( 0 );
		final int timeRange = getActionStateManager().isLivemode() ? 1 : getMainSettings().getTimeRange();
		final int timepointStart = Math.max( 0, timepointEnd - timeRange + 1 );
		final List< Integer > timepoints = IntStream.rangeClosed( timepointStart, timepointEnd ).boxed().collect( Collectors.toList() );
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
			Iterable< Spot > spots;
			if ( getActionStateManager().isLivemode() )
			{
				spots = getVisibleVertices( timepointStart );
				if ( spots != null )
				{
					final Predicate< Spot > spotFilter =
							spot -> tagsToProcess.contains( getVertexTagMap( getDetectionTagSet() ).get( spot ) );
					addSpotsToJson( spots, jsonSpots, spotFilter );
				}
			}
			else
			{
				Predicate< Spot > spotFilter = spot -> timepoints.contains( spot.getTimepoint() );
				spotFilter = spotFilter.and( spot -> tagsToProcess.contains( getVertexTagMap( getDetectionTagSet() ).get( spot ) ) );
				addSpotsToJson( getGraph().vertices(), jsonSpots, spotFilter );
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
		jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_AUTO_BG_THRESH, getMainSettings().getAutoBgThreshold() )
				.add( JSON_KEY_C_RATIO, getMainSettings().getCenterRatio() )
				.add( JSON_KEY_RESET, false )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_SPOTS, jsonSpots )
				.add( JSON_KEY_IS_3D, !is2D() )
				.add( JSON_KEY_IS_LIVEMODE, getActionStateManager().isLivemode() )
				.add( JSON_KEY_USE_MEMMAP, getMainSettings().getUseMemmap() );
		return true;
	}

	@Override
	public void processDataset()
	{
		try
		{
			postAsStringAsync( getEndpointURL( ENDPOINT_DETECTION_UPDATE ), jsonRootObject.toString(),
					response -> {
						if ( response.getStatus() == HttpURLConnection.HTTP_OK )
						{
							final JsonObject rootObject = Json.parse( response.getBody() ).asObject();
							final String message =
									rootObject.get( "completed" ).asBoolean() ? "Detection labels are updated" : "Update aborted";
							showTextOverlayAnimator( message, 3000, TextOverlayAnimator.TextPosition.CENTER );
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

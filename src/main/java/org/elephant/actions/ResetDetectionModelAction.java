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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.HttpResponse;
import mpicbg.spim.data.sequence.VoxelDimensions;

/**
 * Send a request for reseting a voxel-classification model to the server.
 * 
 * @author Ko Sugawara
 */
public class ResetDetectionModelAction extends AbstractElephantDatasetAction
		implements BdvDataMixin, ElephantConstantsMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] reset detection model";

	private static final String MENU_TEXT = "Reset Detection Model";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ResetDetectionModelAction()
	{
		super( NAME );
	}

	@Override
	public void processDataset()
	{
		if ( getActionStateManager().isLivemode() )
		{
			showTextOverlayAnimator( "Model cannot be reset during live mode", 3000, TextOverlayAnimator.TextPosition.CENTER );
			return;
		}
		final AtomicBoolean isCanceled = new AtomicBoolean(); // false by default
		final AtomicReference< File > atomoicFile = new AtomicReference<>();
		final AtomicReference< String > atomoicUrl = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final ModelResetDialog dialog = new ModelResetDialog();
				dialog.setVisible( true );
				try
				{
					isCanceled.set( dialog.isCanceled() );
					atomoicFile.set( dialog.getFile() );
					atomoicUrl.set( dialog.getUrl() );
				}
				finally
				{
					dialog.dispose();
				}
			} );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		if ( !isCanceled.get() )
		{
			final VoxelDimensions voxelSize = getVoxelDimensions();
			final JsonArray scales = new JsonArray()
					.add( voxelSize.dimension( 0 ) )
					.add( voxelSize.dimension( 1 ) )
					.add( voxelSize.dimension( 2 ) );
			final JsonArray cropSize = new JsonArray()
					.add( getMainSettings().getTrainingCropSizeX() )
					.add( getMainSettings().getTrainingCropSizeY() )
					.add( getMainSettings().getTrainingCropSizeZ() );
			final JsonObject jsonRootObject = Json.object()
					.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
					.add( JSON_KEY_SCALES, scales )
					.add( JSON_KEY_N_CROPS, getMainSettings().getNumCrops() )
					.add( JSON_KEY_TRAIN_CROP_SIZE, cropSize )
					.add( JSON_KEY_MODEL_NAME, getMainSettings().getDetectionModelName() )
					.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
					.add( JSON_KEY_IS_3D, !is2D() )
					.add( JSON_KEY_CACHE_MAXBYTES, getMainSettings().getCacheMaxbytes() )
					.add( JSON_KEY_MODEL_URL, atomoicUrl.get() );
			try
			{
				final Consumer< HttpResponse< String > > completed = response -> {
					if ( response.getStatus() == HttpURLConnection.HTTP_OK )
					{
						showTextOverlayAnimator( "Detection model is reset", 3000, TextOverlayAnimator.TextPosition.CENTER );
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
				};
				if ( atomoicFile.get() == null )
				{
					postAsStringAsync( getEndpointURL( ENDPOINT_DETECTION_RESET_MODEL ), jsonRootObject.toString(), completed );
				}
				else
				{
					postMultipartFormDataAsStringAsync( getEndpointURL( ENDPOINT_DETECTION_RESET_MODEL ), atomoicFile.get(), jsonRootObject.toString(), completed );
				}
			}
			catch ( final ElephantConnectException e )
			{
				// already handled by UnirestMixin
			}
		}
	}

}

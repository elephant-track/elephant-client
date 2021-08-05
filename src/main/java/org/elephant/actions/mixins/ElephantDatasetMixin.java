/*******************************************************************************
 * Copyright (C) 2021, Ko Sugawara
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
package org.elephant.actions.mixins;

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.elephant.actions.UploadAction;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import kong.unirest.HttpResponse;
import net.imglib2.Dimensions;

/**
 * Check and generate ELEPHANT datasets on server.
 * 
 * @author Ko Sugawara
 */
public interface ElephantDatasetMixin extends ActionMixin, BdvDataMixin, LoggerMixin, ElephantConstantsMixin, ElephantSettingsMixin, TimepointMixin, UnirestMixin, URLMixin
{
	default boolean ensureDataset()
	{
		final AtomicBoolean isDatasetReadyAtomic = new AtomicBoolean();
		final Dimensions dimensions = getDimensions();
		final JsonArray shape = new JsonArray()
				.add( getMaxTimepoint() + 1 )
				.add( dimensions.dimension( 2 ) )
				.add( dimensions.dimension( 1 ) )
				.add( dimensions.dimension( 0 ) );
		final JsonObject jsonDatasetCheck = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_SHAPE, shape );
		final HttpResponse< String > resCheck = postAsString( getEndpointURL( ENDPOINT_DATASET_CHECK ), jsonDatasetCheck.toString() );
		if ( resCheck.getStatus() == HttpURLConnection.HTTP_OK )
		{
			final String body = resCheck.getBody();
			final String message = Json.parse( body ).asObject().get( "message" ).asString();
			if ( message.equals( "ready" ) )
			{
				isDatasetReadyAtomic.set( true );
			}
			else
			{
				try
				{
					final AtomicInteger reply = new AtomicInteger( -1 );
					SwingUtilities.invokeAndWait( () -> {
						reply.set( JOptionPane.showConfirmDialog( null, message + "\n Initialize the dataset?", "Confirm", JOptionPane.YES_NO_OPTION ) );
					} );
					if ( reply.get() == JOptionPane.YES_OPTION )
					{
						final JsonObject jsonDatasetGenerate = Json.object()
								.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
								.add( JSON_KEY_IS_2D, is2D() );
						final HttpResponse< String > resGenerate = postAsString( getEndpointURL( ENDPOINT_DATASET_GENERATE ), jsonDatasetGenerate.toString() );
						if ( resGenerate.getStatus() == HttpURLConnection.HTTP_OK )
						{
							isDatasetReadyAtomic.set( true );
						}
						else if ( resGenerate.getStatus() == HttpURLConnection.HTTP_NO_CONTENT )
						{
							runPluginAction( UploadAction.NAME );
							final HttpResponse< String > resGenerate2 = postAsString( getEndpointURL( ENDPOINT_DATASET_GENERATE ), jsonDatasetGenerate.toString() );
							if ( resGenerate2.getStatus() == HttpURLConnection.HTTP_OK )
							{
								isDatasetReadyAtomic.set( true );
							}
						}
					}
				}
				catch ( InvocationTargetException | InterruptedException e )
				{
					handleError( e );
				}
			}
		}
		else
		{
			final StringBuilder sb = new StringBuilder( resCheck.getStatusText() );
			if ( resCheck.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR ||
					resCheck.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST )
			{
				sb.append( ": " );
				sb.append( Json.parse( resCheck.getBody() ).asObject().get( "error" ).asString() );
			}
			getLogger().severe( sb.toString() );
		}
		return isDatasetReadyAtomic.get();
	}
}

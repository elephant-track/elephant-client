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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.setting.main.ElephantMainSettingsListener;
import org.mastodon.mamut.plugin.MamutPluginAppModel;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

/**
 * Send a request for updating the training parameters.
 * 
 * @author Ko Sugawara
 */
public class UpdateTrainingParametersService extends AbstractElephantService
		implements ElephantConstantsMixin, ElephantMainSettingsListener, ElephantSettingsMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	public UpdateTrainingParametersService( final MamutPluginAppModel pluginAppModel )
	{
		super();
		super.init( pluginAppModel, null );
	}

	@Override
	public void mainSettingsUpdated()
	{
		final JsonObject jsonRootObject = Json.object()
				.add( JSON_KEY_LR, getMainSettings().getLearningRate() )
				.add( JSON_KEY_N_CROPS, getMainSettings().getNumCrops() );
		Unirest.post( getEndpointURL( ENDPOINT_PARAMS ) ).body( jsonRootObject.toString() ).asStringAsync( new Callback< String >()
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
				showTextOverlayAnimator( "Params updated", 3000, TextOverlayAnimator.TextPosition.CENTER );
			}

			@Override
			public void cancelled()
			{
				getLogger().info( "The request has been cancelled" );
			}

		} );
	}

}

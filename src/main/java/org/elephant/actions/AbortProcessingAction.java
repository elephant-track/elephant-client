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
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

/**
 * Abort running actions and let send an abort signal to the server.
 * 
 * @author Ko Sugawara
 */
public class AbortProcessingAction extends AbstractElephantAction
		implements ElephantConstantsMixin, ElephantStateManagerMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] abort processing";

	private static final String MENU_TEXT = "Abort Processing";

	private static final String[] MENU_KEYS = new String[] { "ctrl C" };

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

	public AbortProcessingAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		getStateManager().setAborted( true );
		// Send an abort signal to the server
		final JsonObject jsonRootObject = Json.object().add( JSON_KEY_STATE, 0 );
		Unirest.post( getEndpointURL( ENDPOINT_STATE ) ).body( jsonRootObject.toString() ).asStringAsync( new Callback< String >()
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
				showTextOverlayAnimator( "Sent abort signal", 3000, TextOverlayAnimator.TextPosition.CENTER );
				getStateManager().setLivemode( false );
			}

			@Override
			public void cancelled()
			{
				getLogger().info( "The request has been cancelled" );
			}

		} );
	}

}

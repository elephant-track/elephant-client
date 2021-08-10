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

import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.actions.mixins.UnirestMixin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;

/**
 * Send a request for reseting a flow model to the server.
 * 
 * @author Ko Sugawara
 */
public class ResetFlowModelAction extends AbstractElephantAction
		implements BdvDataMixin, ElephantConstantsMixin, ElephantSettingsMixin, UIActionMixin, UnirestMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] reset a flow model";

	private static final String MENU_TEXT = "Reset a Flow Model";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ResetFlowModelAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final AtomicInteger option = new AtomicInteger();
		try
		{
			SwingUtilities.invokeAndWait( () -> option.set( JOptionPane.showConfirmDialog( null, "Flow model will be reset", "Select an option", JOptionPane.OK_CANCEL_OPTION ) ) );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		if ( option.get() == JOptionPane.OK_OPTION )
		{
			final JsonObject jsonRootObject = Json.object()
					.add( JSON_KEY_FLOW_MODEL_NAME, getMainSettings().getFlowModelName() )
					.add( JSON_KEY_N_KEEP_AXIALS, getNKeepAxials() )
					.add( JSON_KEY_IS_3D, !is2D() );
			postAsStringAsync( getEndpointURL( ENDPOINT_RESET_FLOW_MODEL ), jsonRootObject.toString(),
					response -> {
						if ( response.getStatus() == HttpURLConnection.HTTP_OK )
						{
							showTextOverlayAnimator( "Flow model is reset", 3000, TextOverlayAnimator.TextPosition.CENTER );
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
	}

}

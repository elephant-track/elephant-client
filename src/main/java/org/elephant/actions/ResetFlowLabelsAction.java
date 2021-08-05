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
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;

/**
 * Send a request for reseting flow labels to the server.
 * 
 * @author Ko Sugawara
 */
public class ResetFlowLabelsAction extends AbstractElephantDatasetAction
		implements ElephantConstantsMixin, ElephantSettingsMixin, UIActionMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] reset flow labels";

	private static final String MENU_TEXT = "Reset Flow Labels";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ResetFlowLabelsAction()
	{
		super( NAME );
	}

	@Override
	public void processDataset()
	{
		final AtomicInteger option = new AtomicInteger();
		try
		{
			SwingUtilities.invokeAndWait( () -> option.set( JOptionPane.showConfirmDialog( null, "Flow labels will be reset", "Select an option", JOptionPane.OK_CANCEL_OPTION ) ) );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		if ( option.get() == JOptionPane.OK_OPTION )
		{
			final JsonObject jsonRootObject = Json.object()
					.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
					.add( JSON_KEY_RESET, true );
			postAsStringAsync( getEndpointURL( ENDPOINT_UPDATE_FLOW ), jsonRootObject.toString(),
					response -> {
						if ( response.getStatus() == HttpURLConnection.HTTP_OK )
						{
							showTextOverlayAnimator( "Flow labels are reset", 3000, TextOverlayAnimator.TextPosition.CENTER );
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
							getLogger().severe( sb.toString() );
						}
					} );
		}
	}

}

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

import java.awt.Font;
import java.awt.Graphics;

import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.mamut.MamutViewBdv;
import org.mastodon.mamut.WindowManager.BdvViewCreatedListener;
import org.mastodon.mamut.plugin.MamutPluginAppModel;

import bdv.viewer.OverlayRenderer;

/**
 * Provide a text overlay used in ELEPHANT.
 *
 * @author Ko Sugawara
 */
public class ElephantOverlayService extends AbstractElephantService implements ElephantStateManagerMixin, WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	public ElephantOverlayService( final MamutPluginAppModel pluginAppModel )
	{
		super.init( pluginAppModel, null );
		addBdvCreatedListener( new BdvViewCreatedListener()
		{

			@Override
			public void bdvViewCreated( MamutViewBdv view )
			{
				view.getViewerPanelMamut().getDisplay().overlays().add( new ElephantOverlay() );
			}
		} );
	}

	/**
	 * An overlay that can be shown a label to show the status of ELEPHANT.
	 *
	 * @author Ko Sugawara
	 */
	private class ElephantOverlay implements OverlayRenderer
	{

		@Override
		public void drawOverlays( final Graphics g )
		{
			if ( getActionStateManager().isLivemode() )
			{
				g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
				g.drawString( "live mode", ( int ) g.getClipBounds().getWidth() / 2, 38 );
			}
			if ( getActionStateManager().isAutoFocus() )
			{
				g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
				g.drawString( "autofocus", ( int ) g.getClipBounds().getWidth() / 2, 50 );
			}
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}

}

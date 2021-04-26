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

import java.awt.event.MouseEvent;

import org.elephant.actions.mixins.BdvViewMouseMotionMixin;
import org.mastodon.mamut.MamutViewBdv;
import org.mastodon.mamut.WindowManager.BdvViewCreatedListener;
import org.mastodon.mamut.plugin.MamutPluginAppModel;

import net.imglib2.realtransform.AffineTransform3D;

/**
 * Set up a backend for {@link BdvViewMouseMotionMixin}.
 * 
 * @author Ko Sugawara
 */
public class BdvViewMouseMotionService extends AbstractElephantService
		implements BdvViewMouseMotionMixin
{

	private static final long serialVersionUID = 1L;

	private int mouseX;

	private int mouseY;

	private final AffineTransform3D renderTransform;

	@Override
	public int getMouseX()
	{
		return mouseX;
	}

	@Override
	public int getMouseY()
	{
		return mouseY;
	}

	@Override
	public AffineTransform3D getRenderTransform()
	{
		return renderTransform;
	}

	public BdvViewMouseMotionService()
	{
		super();
		renderTransform = new AffineTransform3D();
	}

	public void init( final MamutPluginAppModel pluginAppModel )
	{
		super.init( pluginAppModel, null );
		addBdvCreatedListener( new BdvViewCreatedListener()
		{

			@Override
			public void bdvViewCreated( MamutViewBdv view )
			{
				view.getViewerPanelMamut().getDisplay().addHandler( BdvViewMouseMotionService.this );
				view.getViewerPanelMamut().renderTransformListeners().add( BdvViewMouseMotionService.this );
			}
		} );
	}

	@Override
	public void mouseDragged( MouseEvent e )
	{
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved( MouseEvent e )
	{
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void transformChanged( AffineTransform3D transform )
	{
		synchronized ( renderTransform )
		{
			renderTransform.set( transform );
		}
	}

}

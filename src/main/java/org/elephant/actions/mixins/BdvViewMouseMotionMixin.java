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
package org.elephant.actions.mixins;

import java.awt.event.MouseMotionListener;

import bdv.viewer.TransformListener;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Get access to the mouse position and its global coordinates.
 * 
 * @author Ko Sugawara
 */
public interface BdvViewMouseMotionMixin extends MouseMotionListener, TransformListener< AffineTransform3D >, WindowManagerMixin
{

	int getMouseX();

	int getMouseY();

	AffineTransform3D getRenderTransform();

	/**
	 * Copied from {@link org.mastodon.revised.bdv.overlay.OverlayGraphRenderer}.
	 * 
	 * Transform viewer coordinates to global (world) coordinates.
	 *
	 * @param x
	 *            viewer X coordinate
	 * @param y
	 *            viewer Y coordinate
	 * @param gPos
	 *            receives global coordinates corresponding to viewer coordinates
	 *            <em>(x, y, 0)</em>.
	 */
	default void getGlobalPosition( final int x, final int y, final double[] gPos )
	{
		synchronized ( getRenderTransform() )
		{
			getRenderTransform().applyInverse( gPos, new double[] { x, y, 0 } );
		}
	}

	default void getMousePositionGlobal( final double[] gPos )
	{
		getGlobalPosition( getMouseX(), getMouseY(), gPos );
	}

}

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

import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.EllipsoidActionMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.mastodon.mamut.model.Spot;

/**
 * Reset the rotation of the highlighted spot.
 * 
 * @author Ko Sugawara
 */
public class ResetEllipsoidRotation extends AbstractElephantAction
		implements EllipsoidActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] reset ellipsoid rotation";

	private static final String[] MENU_KEYS = new String[] { "J" };

	@Override
	public String[] getMenuKeys()
	{
		return MENU_KEYS;
	}

	public ResetEllipsoidRotation()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final Spot ref = getGraph().vertexRef();
		Spot spot;
		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			spot = getAppModel().getHighlightModel().getHighlightedVertex( ref );
			if ( spot == null )
			{
				getGraph().releaseRef( ref );
				return;
			}
			final double[][] cov = new double[ 3 ][ 3 ];
			spot.getCovariance( cov );
			getEig().decomposeSymmetric( cov );
			final double[][] V = new double[ 3 ][ 3 ];
			for ( int i = 0; i < V.length; i++ )
			{
				V[ i ][ i ] = 1.0;
			}
			final double[] d = getEig().getRealEigenvalues();
			LinAlgHelpersExt.compose( V, d, cov );
			spot.setCovariance( cov );
			getGraph().releaseRef( ref );
		}
		finally
		{
			getStateManager().setWriting( false );
			getModel().setUndoPoint();
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

}

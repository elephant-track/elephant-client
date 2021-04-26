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

import net.imglib2.util.LinAlgHelpers;

/**
 * Rotate the highlighted ellipsoid along the control axis set by
 * {@link SetControlAxisAction}}.
 * 
 * @author Ko Sugawara
 */
public class RotateEllipsoidAction extends AbstractElephantAction
		implements EllipsoidActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String WORD_TO_REPLACE = "CLOCKWISE/COUNTERCLOCKWISE";

	private static final String NAME = "[elephant] rotate ellipsoid " + WORD_TO_REPLACE;

	public static final double ROTATION_CHANGE_FACTOR = Math.toRadians( 1.0 );

	public enum RotateEllipsoidActionMode
	{
		CLOCKWISE( -ROTATION_CHANGE_FACTOR, new String[] { "alt RIGHT" } ),
		COUNTERCLOCKWISE( ROTATION_CHANGE_FACTOR, new String[] { "alt LEFT" } );

		private double factor;

		private final String[] menuKeys;

		private RotateEllipsoidActionMode( final double factor, final String[] menuKeys )
		{
			this.factor = factor;
			this.menuKeys = menuKeys;
		}

		public double getFactor()
		{
			return factor;
		}

		public String[] getMenuKeys()
		{
			return menuKeys;
		}
	}

	private final RotateEllipsoidActionMode mode;

	@Override
	public String[] getMenuKeys()
	{
		return mode.getMenuKeys();
	}

	public RotateEllipsoidAction( final RotateEllipsoidActionMode mode )
	{
		super( NAME.replace( WORD_TO_REPLACE, mode.name().toLowerCase() ) );
		this.mode = mode;
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
			final double[][] V = getEig().getV();
			final double[] d = getEig().getRealEigenvalues();
			final double[][] R = new double[ 3 ][ 3 ];
			switch ( ElephantActionStateManager.INSTANCE.getAxis() )
			{
			case X:
				R[ 0 ][ 0 ] = 1;
				R[ 0 ][ 1 ] = 0;
				R[ 0 ][ 2 ] = 0;
				R[ 1 ][ 0 ] = 0;
				R[ 1 ][ 1 ] = Math.cos( mode.getFactor() );
				R[ 1 ][ 2 ] = -Math.sin( mode.getFactor() );
				R[ 2 ][ 0 ] = 0;
				R[ 2 ][ 1 ] = Math.sin( mode.getFactor() );
				R[ 2 ][ 2 ] = Math.cos( mode.getFactor() );
				break;
			case Y:
				R[ 0 ][ 0 ] = Math.cos( mode.getFactor() );
				R[ 0 ][ 1 ] = 0;
				R[ 0 ][ 2 ] = Math.sin( mode.getFactor() );
				R[ 1 ][ 0 ] = 0;
				R[ 1 ][ 1 ] = 1;
				R[ 1 ][ 2 ] = 0;
				R[ 2 ][ 0 ] = -Math.sin( mode.getFactor() );
				R[ 2 ][ 1 ] = 0;
				R[ 2 ][ 2 ] = Math.cos( mode.getFactor() );
				break;
			case Z:
				R[ 0 ][ 0 ] = Math.cos( mode.getFactor() );
				R[ 0 ][ 1 ] = -Math.sin( mode.getFactor() );
				R[ 0 ][ 2 ] = 0;
				R[ 1 ][ 0 ] = Math.sin( mode.getFactor() );
				R[ 1 ][ 1 ] = Math.cos( mode.getFactor() );
				R[ 1 ][ 2 ] = 0;
				R[ 2 ][ 0 ] = 0;
				R[ 2 ][ 1 ] = 0;
				R[ 2 ][ 2 ] = 1;
				break;
			default:
				break;
			}
			final double[][] VR = new double[ 3 ][ 3 ];
			LinAlgHelpers.mult( R, V, VR );
			LinAlgHelpersExt.compose( VR, d, cov );
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

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

import org.elephant.actions.SetControlAxisAction.ControlAxis;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.EllipsoidActionMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.mastodon.mamut.model.Spot;

import bdv.viewer.animate.TextOverlayAnimator;
import net.imglib2.util.LinAlgHelpers;

/**
 * Change the size of the highlighted ellipsoid along the control axis set by
 * {@link SetControlAxisAction}}.
 * 
 * @author Ko Sugawara
 */
public class ChangeEllipsoidSizeAction extends AbstractElephantAction
		implements BdvDataMixin, EllipsoidActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin, UIActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String WORD_TO_REPLACE = "INCREASE/DECREASE";

	private static final String NAME = "[elephant] " + WORD_TO_REPLACE + " ellipsoid size";

	private static final double SIZE_CHANGE_FACTOR_BASE = 0.1;

	public enum ChangeEllipsoidSizeActionMode
	{
		INCREASE( 1 + SIZE_CHANGE_FACTOR_BASE, new String[] { "alt E" } ),
		DECREASE( 1 - SIZE_CHANGE_FACTOR_BASE, new String[] { "alt Q" } );

		private double factor;

		private String[] menuKeys;

		private ChangeEllipsoidSizeActionMode( final double factor, final String[] menuKeys )
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

	private final ChangeEllipsoidSizeActionMode mode;

	@Override
	public String[] getMenuKeys()
	{
		return mode.getMenuKeys();
	}

	public ChangeEllipsoidSizeAction( final ChangeEllipsoidSizeActionMode mode )
	{
		super( NAME.replace( WORD_TO_REPLACE, mode.name().toLowerCase() ) );
		this.mode = mode;
	}

	@Override
	public void process()
	{
		// Validation for 2D data
		if ( is2D() && getActionStateManager().getAxis() == ControlAxis.Z )
		{
			showTextOverlayAnimator( "Invalid control axis " + getActionStateManager().getAxis().name() + " for 2D data", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
			return;
		}
		final Spot ref = getGraph().vertexRef();
		Spot spot;
		getGraph().getLock().writeLock().lock();
		getActionStateManager().setWriting( true );
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
			final double[][] S = new double[ 3 ][ 3 ];
			final int axis = getActionStateManager().getAxis().getIndex();
			for ( int i = 0; i < 3; i++ )
			{
				for ( int j = 0; j < 3; j++ )
				{
					if ( i == j )
						S[ i ][ j ] = axis == i ? mode.getFactor() : 1;
					else
						S[ i ][ j ] = 0;
				}
			}
			final double[][] SR = new double[ 3 ][ 3 ];
			LinAlgHelpers.mult( S, V, SR );
			LinAlgHelpersExt.compose( SR, d, cov );
			spot.setCovariance( cov );
			getGraph().releaseRef( ref );
		}
		finally
		{
			getActionStateManager().setWriting( false );
			getModel().setUndoPoint();
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

}

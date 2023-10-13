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
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.model.Spot;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

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

	private static final String NAME_BASE = "[elephant] %s ellipsoid size";

	private static final String NAME_INCREASE = String.format( NAME_BASE, "increase" );

	private static final String NAME_DECREASE = String.format( NAME_BASE, "decrease" );

	private static final String[] MENU_KEYS_INCREASE = new String[] { "alt E" };

	private static final String[] MENU_KEYS_DECREASE = new String[] { "alt Q" };

	private static final String DESCRIPTION_BASE = "%s the size of the highlighted vertex in the specified axis.";

	private static final String DESCRIPTION_INCREASE = String.format( DESCRIPTION_BASE, "Increase" );

	private static final String DESCRIPTION_DECREASE = String.format( DESCRIPTION_BASE, "Decrease" );

	private static final double SIZE_CHANGE_FACTOR_BASE = 0.1;

	public enum ChangeEllipsoidSizeActionMode
	{
		INCREASE( 1 + SIZE_CHANGE_FACTOR_BASE, NAME_INCREASE, MENU_KEYS_INCREASE ),
		DECREASE( 1 - SIZE_CHANGE_FACTOR_BASE, NAME_DECREASE, MENU_KEYS_DECREASE );

		private double factor;

		private String name;

		private String[] menuKeys;

		private ChangeEllipsoidSizeActionMode( final double factor, final String name, final String[] menuKeys )
		{
			this.factor = factor;
			this.name = name;
			this.menuKeys = menuKeys;
		}

		public String getName()
		{
			return name;
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

	/*
	 * Command description.
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add(
					NAME_INCREASE,
					MENU_KEYS_INCREASE,
					DESCRIPTION_INCREASE );
			descriptions.add(
					NAME_DECREASE,
					MENU_KEYS_DECREASE,
					DESCRIPTION_DECREASE );
		}
	}

	@Override
	public String[] getMenuKeys()
	{
		return mode.getMenuKeys();
	}

	public ChangeEllipsoidSizeAction( final ChangeEllipsoidSizeActionMode mode )
	{
		super( mode.getName() );
		this.mode = mode;
	}

	@Override
	public void process()
	{
		// Validation for 2D data
		if ( is2D() && getActionStateManager().getAxis() == ControlAxis.Z )
		{
			showTextOverlayAnimator( "Invalid control axis " + getActionStateManager().getAxis().name() + " for 2D data", 3000,
					TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
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

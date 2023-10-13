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

import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.EllipsoidActionMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.model.Spot;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

import net.imglib2.util.LinAlgHelpers;

/**
 * Rotate the highlighted ellipsoid along the control axis set by
 * {@link SetControlAxisAction}}.
 * 
 * @author Ko Sugawara
 */
public class RotateEllipsoidAction extends AbstractElephantAction
		implements BdvDataMixin, EllipsoidActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] rotate ellipsoid %s";

	private static final String NAME_CLOCKWISE = String.format( NAME_BASE, "clockwise" );

	private static final String NAME_COUNTERCLOCKWISE = String.format( NAME_BASE, "counterclockwise" );

	private static final String[] MENU_KEYS_CLOCKWISE = new String[] { "alt RIGHT" };

	private static final String[] MENU_KEYS_COUNTERCLOCKWISE = new String[] { "alt LEFT" };

	private static final String DESCRIPTION_BASE = "Rotate the highlighted ellipsoid %s.";

	private static final String DESCRIPTION_CLOCKWISE = String.format( DESCRIPTION_BASE, "clockwise" );

	private static final String DESCRIPTION_COUNTERCLOCKWISE = String.format( DESCRIPTION_BASE, "counterclockwise" );

	public static final double ROTATION_CHANGE_FACTOR = Math.toRadians( 1.0 );

	public enum RotateEllipsoidActionMode
	{
		CLOCKWISE( -ROTATION_CHANGE_FACTOR, NAME_CLOCKWISE, MENU_KEYS_CLOCKWISE ),
		COUNTERCLOCKWISE( ROTATION_CHANGE_FACTOR, NAME_COUNTERCLOCKWISE, MENU_KEYS_COUNTERCLOCKWISE );

		private final double factor;

		private final String name;

		private final String[] menuKeys;

		private RotateEllipsoidActionMode( final double factor, final String name, final String[] menuKeys )
		{
			this.factor = factor;
			this.name = name;
			this.menuKeys = menuKeys;
		}

		public double getFactor()
		{
			return factor;
		}

		public String getName()
		{
			return name;
		}

		public String[] getMenuKeys()
		{
			return menuKeys;
		}
	}

	private final RotateEllipsoidActionMode mode;

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
					NAME_CLOCKWISE,
					MENU_KEYS_CLOCKWISE,
					DESCRIPTION_CLOCKWISE );
			descriptions.add(
					NAME_COUNTERCLOCKWISE,
					MENU_KEYS_COUNTERCLOCKWISE,
					DESCRIPTION_COUNTERCLOCKWISE );
		}
	}

	@Override
	public String[] getMenuKeys()
	{
		return mode.getMenuKeys();
	}

	public RotateEllipsoidAction( final RotateEllipsoidActionMode mode )
	{
		super( mode.getName() );
		this.mode = mode;
	}

	@Override
	public void process()
	{
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
			final double[][] R = new double[ 3 ][ 3 ];
			if ( is2D() )
			{
				R[ 0 ][ 0 ] = Math.cos( mode.getFactor() );
				R[ 0 ][ 1 ] = -Math.sin( mode.getFactor() );
				R[ 0 ][ 2 ] = 0;
				R[ 1 ][ 0 ] = Math.sin( mode.getFactor() );
				R[ 1 ][ 1 ] = Math.cos( mode.getFactor() );
				R[ 1 ][ 2 ] = 0;
				R[ 2 ][ 0 ] = 0;
				R[ 2 ][ 1 ] = 0;
				R[ 2 ][ 2 ] = 1;
			}
			else
			{
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
			}
			final double[][] VR = new double[ 3 ][ 3 ];
			LinAlgHelpers.mult( R, V, VR );
			LinAlgHelpersExt.compose( VR, d, cov );
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

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
import org.elephant.actions.mixins.UIActionMixin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

import bdv.viewer.animate.TextOverlayAnimator;

/**
 * Set the control axis for the highlighted ellipsoid.
 * 
 * @author Ko Sugawara
 */
public class SetControlAxisAction extends AbstractElephantAction
		implements BdvDataMixin, UIActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] set control axis %s";

	private static final String NAME_X = String.format( NAME_BASE, "x" );

	private static final String NAME_Y = String.format( NAME_BASE, "y" );

	private static final String NAME_Z = String.format( NAME_BASE, "z" );

	private static final String[] MENU_KEYS_X = new String[] { "alt X" };

	private static final String[] MENU_KEYS_Y = new String[] { "alt Y" };

	private static final String[] MENU_KEYS_Z = new String[] { "alt Z" };

	private static final String DESCRIPTION_BASE = "Set control axis to %s.";

	private static final String DESCRIPTION_X = String.format( DESCRIPTION_BASE, "x" );

	private static final String DESCRIPTION_Y = String.format( DESCRIPTION_BASE, "y" );

	private static final String DESCRIPTION_Z = String.format( DESCRIPTION_BASE, "z" );

	public enum ControlAxis
	{
		X( 0, NAME_X, MENU_KEYS_X ),
		Y( 1, NAME_Y, MENU_KEYS_Y ),
		Z( 2, NAME_Z, MENU_KEYS_Z );

		private final int index;

		private final String name;

		private final String[] menuKeys;

		ControlAxis( final int index, final String name, final String[] menuKeys )
		{
			this.index = index;
			this.name = name;
			this.menuKeys = menuKeys;
		}

		public int getIndex()
		{
			return index;
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

	private final ControlAxis axis;

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
					NAME_X,
					MENU_KEYS_X,
					DESCRIPTION_X );
			descriptions.add(
					NAME_Y,
					MENU_KEYS_Y,
					DESCRIPTION_Y );
			descriptions.add(
					NAME_Z,
					MENU_KEYS_Z,
					DESCRIPTION_Z );
		}
	}

	@Override
	public String[] getMenuKeys()
	{
		return axis.getMenuKeys();
	}

	public SetControlAxisAction( final ControlAxis axis )
	{
		super( axis.getName() );
		this.axis = axis;
	}

	@Override
	public void process()
	{
		if ( is2D() && axis == ControlAxis.Z )
		{
			showTextOverlayAnimator( "Invalid control axis " + axis.name() + " for 2D data", 3000,
					TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
		}
		else
		{
			getActionStateManager().setAxis( axis );
			showTextOverlayAnimator( "Set Control Axis: " + axis.name(), 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
		}
	}

}

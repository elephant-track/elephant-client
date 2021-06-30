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

	private static final String WORD_TO_REPLACE = "AXIS";

	private static final String NAME = "[elephant] set control axis " + WORD_TO_REPLACE;

	public enum ControlAxis
	{
		X( 0, new String[] { "alt X" } ),
		Y( 1, new String[] { "alt Y" } ),
		Z( 2, new String[] { "alt Z" } );

		private int index;

		private String[] menuKeys;

		ControlAxis( final int index, final String[] menuKeys )
		{
			this.index = index;
			this.menuKeys = menuKeys;
		}

		public int getIndex()
		{
			return index;
		}

		public String[] getMenuKeys()
		{
			return menuKeys;
		}
	}

	private final ControlAxis axis;

	@Override
	public String[] getMenuKeys()
	{
		return axis.getMenuKeys();
	}

	public SetControlAxisAction( final ControlAxis axis )
	{
		super( NAME.replace( WORD_TO_REPLACE, axis.name().toLowerCase() ) );
		this.axis = axis;
	}

	@Override
	public void process()
	{
		if ( is2D() && axis == ControlAxis.Z )
		{
			showTextOverlayAnimator( "Invalid control axis " + axis.name() + " for 2D data", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
		}
		else
		{
			getStateManager().setAxis( axis );
			showTextOverlayAnimator( "Set Control Axis: " + axis.name(), 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
		}
	}

}

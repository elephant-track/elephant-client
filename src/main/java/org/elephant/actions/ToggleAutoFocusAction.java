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

import org.elephant.actions.mixins.UIActionMixin;

import bdv.viewer.animate.TextOverlayAnimator;

/**
 * Toggle autofocus.
 * 
 * <p>
 * While autofocus is on, the BDV view is navigated to the highlighted spot.
 * 
 * @author Ko Sugawara
 */
public class ToggleAutoFocusAction extends AbstractElephantAction
		implements UIActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] toggle auto focus";

	private static final String MENU_TEXT = "Toggle Auto Focus";

	private static final String[] MENU_KEYS = new String[] { "alt R" };

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	@Override
	public String[] getMenuKeys()
	{
		return MENU_KEYS;
	}

	public ToggleAutoFocusAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		ElephantActionStateManager.INSTANCE.setAutoFocus( !ElephantActionStateManager.INSTANCE.isAutoFocus() );
		final String autoFocusStatus = ElephantActionStateManager.INSTANCE.isAutoFocus() ? "On" : "Off";
		showTextOverlayAnimator( "Auto Focus: " + autoFocusStatus, 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
	}

}

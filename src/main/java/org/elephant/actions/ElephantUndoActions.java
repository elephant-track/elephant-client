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

import org.mastodon.mamut.UndoActions;
import org.mastodon.mamut.model.Model;
import org.scijava.ui.behaviour.util.Actions;

/**
 * Overwrite the original Undo/Redo actions to avoid conflicts with modification
 * by a program.
 *
 * @author Ko Sugawara
 */
public class ElephantUndoActions extends UndoActions
{

	static final String[] UNDO_KEYS = new String[] { "meta Z", "ctrl Z" };

	static final String[] REDO_KEYS = new String[] { "meta shift Z", "ctrl shift Z" };

	/**
	 * Overwrite the original Undo/Redo actions.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param model
	 *            Actions are targeted at this {@link Model}s {@code undo()} and
	 *            {@code redo()} methods.
	 */
	public static void installOverwrite(
			final Actions actions,
			final Model model )
	{
		actions.runnableAction( () -> {
			ElephantActionStateManager.INSTANCE.setWriting( true );
			try
			{
				model.undo();
			}
			finally
			{
				ElephantActionStateManager.INSTANCE.setWriting( false );
			}
		}, UNDO, UNDO_KEYS );
		actions.runnableAction( () -> {
			ElephantActionStateManager.INSTANCE.setWriting( true );
			try
			{
				model.redo();
			}
			finally
			{
				ElephantActionStateManager.INSTANCE.setWriting( false );
			}
		}, REDO, REDO_KEYS );
	}

}

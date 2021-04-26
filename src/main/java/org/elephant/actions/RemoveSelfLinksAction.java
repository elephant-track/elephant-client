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

import java.util.function.Predicate;

import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;

/**
 * Remove accidentally generated self links (i.e. the source and target spots
 * are the same).
 * 
 * @author Ko Sugawara
 */
public class RemoveSelfLinksAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] remove self links";

	private static final String MENU_TEXT = "Remove Self Links";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RemoveSelfLinksAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final Predicate< Link > edgeFilter = edge -> {
			final Spot ref = getGraph().vertexRef();
			try
			{
				return edge.getSource( ref ).getInternalPoolIndex() == edge.getTarget( ref ).getInternalPoolIndex();

			}
			finally
			{
				getGraph().releaseRef( ref );
			}
		};
		removeEdges( getGraph().edges(), edgeFilter );
		getModel().setUndoPoint();
	}

}

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

import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;

/**
 * Tag the dividing and divided spots in the tracks.
 * 
 * @author Ko Sugawara
 */
public class TagDividingCellAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] tag dividing cells";

	private static final String MENU_TEXT = "Tag Dividing Cells";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public TagDividingCellAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		getGraph().getLock().writeLock().lock();
		getActionStateManager().setWriting( true );
		try
		{
			final ObjTagMap< Spot, Tag > spotTagMap = getVertexTagMap( getDivisionTagSet() );
			final Tag tagDividing = getTag( getDivisionTagSet(), DIVISION_DIVIDING_TAG_NAME );
			final Tag tagDivided = getTag( getDivisionTagSet(), DIVISION_DIVIDED_TAG_NAME );
			final Tag tagNondividing = getTag( getDivisionTagSet(), DIVISION_NONDIVIDING_TAG_NAME );
			for ( final Spot spot : getGraph().vertices() )
				spotTagMap.set( spot, tagNondividing );
			final Spot ref = getGraph().vertexRef();
			for ( final Spot spot : getGraph().vertices() )
			{
				if ( 1 < spot.outgoingEdges().size() )
				{
					spotTagMap.set( spot, tagDividing );
					for ( final Link edge : spot.outgoingEdges() )
						spotTagMap.set( edge.getTarget( ref ), tagDivided );
				}
			}
			getGraph().releaseRef( ref );
		}
		finally
		{
			getModel().setUndoPoint();
			getActionStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

}

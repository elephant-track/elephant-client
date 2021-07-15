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
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;

/**
 * Tag the highlighted spot with the specified {@code Detection} tag.
 * 
 * @author Ko Sugawara
 */
public class TagHighlightedVertexAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] tag with ";

	public enum TagMode
	{
		TP( "tp", new String[] { "4" } ),
		FP( "fp", new String[] { "5" } ),
		TN( "tn", new String[] { "6" } ),
		FN( "fn", new String[] { "7" } ),
		TB( "tb", new String[] { "8" } ),
		FB( "fb", new String[] { "9" } ),
		UNLABELED( "unlabeled", new String[] { "0" } );

		private final String name;

		private final String[] menuKeys;

		private TagMode( final String name, final String[] menuKeys )
		{
			this.name = name;
			this.menuKeys = menuKeys;
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

	private final TagMode tagMode;

	@Override
	public String[] getMenuKeys()
	{
		return tagMode.getMenuKeys();
	}

	public TagHighlightedVertexAction( final TagMode tagMode )
	{
		super( NAME_BASE + tagMode.getName() );
		this.tagMode = tagMode;
	}

	@Override
	public void process()
	{
		getGraph().getLock().writeLock().lock();
		getActionStateManager().setWriting( true );
		try
		{
			final ObjTagMap< Spot, Tag > tagMapDetection = getVertexTagMap( getDetectionTagSet() );
			final Spot ref = getGraph().vertexRef();
			final Spot spot = getAppModel().getHighlightModel().getHighlightedVertex( ref );

			if ( spot != null )
			{
				tagMapDetection.set( spot, getTag( getDetectionTagSet(), tagMode.getName() ) );
				getLogger().info( spot + " was tagged with " + tagMode.getName() );
			}
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

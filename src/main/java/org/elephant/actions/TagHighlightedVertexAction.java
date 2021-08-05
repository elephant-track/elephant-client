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
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

/**
 * Tag the highlighted spot with the specified {@code Detection} tag.
 * 
 * @author Ko Sugawara
 */
public class TagHighlightedVertexAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] tag with %s";

	private static final String NAME_TP = String.format( NAME_BASE, "tp" );

	private static final String NAME_FP = String.format( NAME_BASE, "fp" );

	private static final String NAME_TN = String.format( NAME_BASE, "tn" );

	private static final String NAME_FN = String.format( NAME_BASE, "fn" );

	private static final String NAME_TB = String.format( NAME_BASE, "tb" );

	private static final String NAME_FB = String.format( NAME_BASE, "fb" );

	private static final String NAME_UNLABELED = String.format( NAME_BASE, "unlabeled" );

	private static final String[] MENU_KEYS_TP = new String[] { "4" };

	private static final String[] MENU_KEYS_FP = new String[] { "5" };

	private static final String[] MENU_KEYS_TN = new String[] { "6" };

	private static final String[] MENU_KEYS_FN = new String[] { "7" };

	private static final String[] MENU_KEYS_TB = new String[] { "8" };

	private static final String[] MENU_KEYS_FB = new String[] { "9" };

	private static final String[] MENU_KEYS_UNLABELED = new String[] { "0" };

	private static final String DESCRIPTION_BASE = "Tag the highlighted spot with %s.";

	private static final String DESCRIPTION_TP = String.format( DESCRIPTION_BASE, "tp" );

	private static final String DESCRIPTION_FP = String.format( DESCRIPTION_BASE, "fp" );

	private static final String DESCRIPTION_TN = String.format( DESCRIPTION_BASE, "tn" );

	private static final String DESCRIPTION_FN = String.format( DESCRIPTION_BASE, "fn" );

	private static final String DESCRIPTION_TB = String.format( DESCRIPTION_BASE, "tb" );

	private static final String DESCRIPTION_FB = String.format( DESCRIPTION_BASE, "fb" );

	private static final String DESCRIPTION_UNLABELED = String.format( DESCRIPTION_BASE, "unlabeled" );

	public enum TagMode
	{
		TP( NAME_TP, MENU_KEYS_TP ),
		FP( NAME_FP, MENU_KEYS_FP ),
		TN( NAME_TN, MENU_KEYS_TN ),
		FN( NAME_FN, MENU_KEYS_FN ),
		TB( NAME_TB, MENU_KEYS_TB ),
		FB( NAME_FB, MENU_KEYS_FB ),
		UNLABELED( NAME_UNLABELED, MENU_KEYS_UNLABELED );

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

	/*
	 * Command description.
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add(
					NAME_TP,
					MENU_KEYS_TP,
					DESCRIPTION_TP );
			descriptions.add(
					NAME_FP,
					MENU_KEYS_FP,
					DESCRIPTION_FP );
			descriptions.add(
					NAME_TN,
					MENU_KEYS_TN,
					DESCRIPTION_TN );
			descriptions.add(
					NAME_FN,
					MENU_KEYS_FN,
					DESCRIPTION_FN );
			descriptions.add(
					NAME_TB,
					MENU_KEYS_TB,
					DESCRIPTION_TB );
			descriptions.add(
					NAME_FB,
					MENU_KEYS_FB,
					DESCRIPTION_FB );
			descriptions.add(
					NAME_UNLABELED,
					MENU_KEYS_UNLABELED,
					DESCRIPTION_UNLABELED );
		}
	}

	@Override
	public String[] getMenuKeys()
	{
		return tagMode.getMenuKeys();
	}

	public TagHighlightedVertexAction( final TagMode tagMode )
	{
		super( tagMode.getName() );
		this.tagMode = tagMode;
	}

	@Override
	public void process()
	{
		getGraph().getLock().writeLock().lock();
		getActionStateManager().setWriting( true );
		Spot spot = null;
		try
		{
			final ObjTagMap< Spot, Tag > tagMapDetection = getVertexTagMap( getDetectionTagSet() );
			final Spot ref = getGraph().vertexRef();
			spot = getAppModel().getHighlightModel().getHighlightedVertex( ref );

			if ( spot != null )
			{
				tagMapDetection.set( spot, getTag( getDetectionTagSet(), tagMode.name().toLowerCase() ) );
			}
			getGraph().releaseRef( ref );
		}
		finally
		{
			getActionStateManager().setWriting( false );
			getModel().setUndoPoint();
			getGraph().getLock().writeLock().unlock();
			getLogger().info( spot + " was tagged with " + tagMode.name() );
			notifyGraphChanged();
		}
	}

}

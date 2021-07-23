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

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Change a tag for the spots or links.
 * 
 * @author Ko Sugawara
 */
public class MapTagAction extends AbstractElephantAction
		implements ElephantConstantsMixin, ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] map %s tag";

	private static final String NAME_SPOT = String.format( NAME_BASE, "spot" );

	private static final String NAME_LINK = String.format( NAME_BASE, "link" );

	private static final String MENU_TEXT_BASE = "Map %s Tag";

	private static final String MENU_TEXT_SPOT = String.format( MENU_TEXT_BASE, "Spot" );

	private static final String MENU_TEXT_LINK = String.format( MENU_TEXT_BASE, "Link" );

	public enum ChangeTagActionMode
	{
		SPOT( NAME_SPOT, MENU_TEXT_SPOT ),
		LINK( NAME_LINK, MENU_TEXT_LINK );

		private final String name;

		private final String menuText;

		private ChangeTagActionMode( final String name, final String menuText )
		{
			this.name = name;
			this.menuText = menuText;
		}

		public String getName()
		{
			return name;
		}

		public String getMenuText()
		{
			return menuText;
		}
	}

	private final ChangeTagActionMode mode;

	@Override
	public String getMenuText()
	{
		return mode.getMenuText();
	}

	public MapTagAction( final ChangeTagActionMode mode )
	{
		super( mode.name() );
		this.mode = mode;
	}

	@Override
	public void process()
	{
		final String[] tagSetNames = getTagSetModel().getTagSetStructure().getTagSets().stream().map( TagSet::getName ).toArray( String[]::new );
		final String[] tagNamesDetection = ArrayUtils.insert( 0,
				getDetectionTagSet().getTags().stream().map( Tag::label ).toArray( String[]::new ),
				NO_TAG );
		final String[] tagNamesTracking = ArrayUtils.insert( 0,
				getTrackingTagSet().getTags().stream().map( Tag::label ).toArray( String[]::new ),
				NO_TAG );
		final String[] tagNamesProgenitor = ArrayUtils.insert( 0,
				getProgenitorTagSet().getTags().stream().map( Tag::label ).toArray( String[]::new ),
				NO_TAG );
		final String[] tagNamesStatus = ArrayUtils.insert( 0,
				getStatusTagSet().getTags().stream().map( Tag::label ).toArray( String[]::new ),
				NO_TAG );
		final String[] tagNamesProliferator = ArrayUtils.insert( 0,
				getProliferatorTagSet().getTags().stream().map( Tag::label ).toArray( String[]::new ),
				NO_TAG );
		final String[] tagNamesDivision = ArrayUtils.insert( 0,
				getDivisionTagSet().getTags().stream().map( Tag::label ).toArray( String[]::new ),
				NO_TAG );
		final AtomicReference< String > tagSetNameFrom = new AtomicReference<>();
		final AtomicReference< String > tagSetNameTo = new AtomicReference<>();
		final AtomicReference< String > tagNameFrom = new AtomicReference<>();
		final AtomicReference< String > tagNameTo = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final ChangeTagDialog dialog = new ChangeTagDialog(
						tagSetNames, tagNamesDetection, tagNamesTracking, tagNamesProgenitor, tagNamesStatus, tagNamesProliferator, tagNamesDivision );
				dialog.setVisible( true );
				try
				{
					if ( !dialog.isCanceled() )
					{
						tagSetNameFrom.set( dialog.getTagSetNameFrom() );
						tagSetNameTo.set( dialog.getTagSetNameTo() );
						tagNameFrom.set( dialog.getTagNameFrom() );
						tagNameTo.set( dialog.getTagNameTo() );
					}
				}
				finally
				{
					dialog.dispose();
				}
			} );
		}
		catch ( InvocationTargetException | InterruptedException e1 )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e1 ) );;
		}
		if ( tagSetNameFrom.get() != null && tagSetNameTo.get() != null &&
				tagNameFrom.get() != null && tagNameTo.get() != null )
		{
			try
			{
				final TagSet tagSetFrom = getTagSetByName( tagSetNameFrom.get() );
				final TagSet tagSetTo = getTagSetByName( tagSetNameTo.get() );
				final Tag tagFrom = getTag( tagSetFrom, tagNameFrom.get() );
				final Tag tagTo = getTag( tagSetTo, tagNameTo.get() );
				switch ( mode )
				{
				case SPOT:
					final ObjTagMap< Spot, Tag > tagMapFromSpot = getTagSetModel().getVertexTags().tags( tagSetFrom );
					final ObjTagMap< Spot, Tag > tagMapToSpot = getTagSetModel().getVertexTags().tags( tagSetTo );

					getGraph().getLock().writeLock().lock();
					getActionStateManager().setWriting( true );
					try
					{
						final Spot ref = getGraph().vertexRef();
						for ( final Spot spot : getGraph().vertices() )
						{
							ref.refTo( spot );
							final Tag tag = tagMapFromSpot.get( ref );
							if ( tag == tagFrom )
								tagMapToSpot.set( ref, tagTo );
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
					break;
				case LINK:
					final ObjTagMap< Link, Tag > tagMapFromLink = getTagSetModel().getEdgeTags().tags( tagSetFrom );
					final ObjTagMap< Link, Tag > tagMapToLink = getTagSetModel().getEdgeTags().tags( tagSetTo );

					getGraph().getLock().writeLock().lock();
					getActionStateManager().setWriting( true );
					try
					{
						final Link ref = getGraph().edgeRef();
						for ( final Link link : getGraph().edges() )
						{
							ref.refTo( link );
							final Tag tag = tagMapFromLink.get( ref );
							if ( tag == tagFrom )
								tagMapToLink.set( ref, tagTo );
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
					break;
				default:
					throw new RuntimeException( "mode is invalid: " + mode );
				}
			}
			catch ( final NoSuchElementException e )
			{
				getLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}

		}
	}

}

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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Remove the spots with the specified tag.
 * 
 * @author Ko Sugawara
 */
public class RemoveSpotsByTagAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] remove spots by tag";

	private static final String MENU_TEXT = "Remove Spots by Tag";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RemoveSpotsByTagAction()
	{
		super( NAME );
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
		final AtomicReference< String > tagSetName = new AtomicReference<>();
		final AtomicReference< String > tagName = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final SelectSingleTagDialog dialog = new SelectSingleTagDialog(
						tagSetNames, tagNamesDetection, tagNamesTracking, tagNamesProgenitor, tagNamesStatus, tagNamesProliferator, tagNamesDivision );
				dialog.setVisible( true );
				try
				{
					if ( !dialog.isCanceled() )
					{
						tagSetName.set( dialog.getTagSetName() );
						tagName.set( dialog.getTagName() );
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
		if ( tagSetName.get() != null && tagName.get() != null )
		{
			final TagSet tagSet = getTagSetByName( tagSetName.get() );
			final Predicate< Spot > spotFilter = spot -> getVertexTagMap( tagSet ).get( spot ) == getTag( tagSet, tagName.get() );
			removeSpots( getGraph().vertices(), spotFilter );
			getModel().setUndoPoint();
		}
	}

}

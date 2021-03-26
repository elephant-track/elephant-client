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
import org.elephant.actions.mixins.UIActionMixin;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;

import bdv.viewer.animate.TextOverlayAnimator;

/**
 * Assign the {@code Progenitor} tags to the tracks tagged with {@code Complete}
 * automatically.
 * 
 * @author Ko Sugawara
 */
public class TagProgenitorAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin, UIActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] tag progenitors";

	private static final String MENU_TEXT = "Tag Progenitors";

	private static final int MAX_COUNT = 255;

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public TagProgenitorAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			final PoolCollectionWrapper< Spot > spots = getGraph().vertices();
			final RefSet< Spot > rootSpots = RefCollections.createRefSet( spots );
			final ObjTagMap< Spot, Tag > spotStatusTagMap = getVertexTagMap( getStatusTagSet() );
			final Tag tagStatusCompleted = getTag( getStatusTagSet(), STATUS_COMPLETED_TAG_NAME );
			final ObjTagMap< Spot, Tag > spotProgenitorTagMap = getVertexTagMap( getProgenitorTagSet() );
			final ObjTagMap< Link, Tag > edgeProgenitorTagMap = getEdgeTagMap( getProgenitorTagSet() );
			final Tag tagProgenitorUnlabeled = getTag( getProgenitorTagSet(), PROGENITOR_UNLABELED_TAG_NAME );
			for ( final Spot spot : spots )
			{
				spotProgenitorTagMap.set( spot, tagProgenitorUnlabeled );
				if ( spotStatusTagMap.get( spot ) == tagStatusCompleted && spot.incomingEdges().isEmpty() )
					rootSpots.add( spot );
			}
			for ( final Link edge : getGraph().edges() )
				edgeProgenitorTagMap.set( edge, tagProgenitorUnlabeled );
			int index = 1;
			for ( final Spot rootSpot : rootSpots )
			{
				if ( MAX_COUNT < index )
				{
					showTextOverlayAnimator(
							"Number of progenitors reached to the maximum limit",
							3000,
							TextOverlayAnimator.TextPosition.CENTER );
					break;
				}
				final Tag tagProgenitor = getTag( getProgenitorTagSet(), String.valueOf( index++ ) );
				final RefSet< Spot > spotsInTrack = RefCollections.createRefSet( spots );
				final RefSet< Link > edgesInTrack = RefCollections.createRefSet( getGraph().edges() );
				parseTree( rootSpot, spotsInTrack, edgesInTrack );
				for ( final Spot spot : spotsInTrack )
					spotProgenitorTagMap.set( spot, tagProgenitor );
				for ( final Link edge : edgesInTrack )
					edgeProgenitorTagMap.set( edge, tagProgenitor );
			}
		}
		finally
		{
			getModel().setUndoPoint();
			getStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

	private void parseTree(
			final Spot spot,
			final RefCollection< Spot > spotsInTrack,
			final RefCollection< Link > edgesInTrack )
	{
		spotsInTrack.add( spot );
		final RefList< Link > outgoingEdges = RefCollections.createRefList( getGraph().edges() );
		for ( final Link edge : spot.outgoingEdges() )
		{
			outgoingEdges.add( edge );
			edgesInTrack.add( edge );
		}
		for ( final Link edge : outgoingEdges )
			parseTree( edge.getTarget( spot ), spotsInTrack, edgesInTrack );
	}
}

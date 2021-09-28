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
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.pool.PoolCollectionWrapper;

/**
 * Label the tracks with the following rule.
 * 
 * <ol>
 * <li>PROLIFERATOR: the track contains at least one division</li>
 * <li>NONPROLIFERATOR: the track is completely tracked from
 * {@code TIMEPOINT_THRESHOLD_LOWER} to {@code TIMEPOINT_THRESHOLD_HIGHER} and
 * has no division</li>
 * <li>INVISIBLE (undetermined): neither of the above applies</li>
 * </ol>
 * 
 * 
 * @author Ko Sugawara
 */
public class TagProliferatorAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	// If the track starts between timepoint 100 (inclusive) and 300 (inclusive),
	// it is marked as invisible.
	private static final int TIMEPOINT_THRESHOLD_LOWER = 100;

	private static final int TIMEPOINT_THRESHOLD_HIGHER = 350;

	private static final String NAME = "[elephant] tag proliferators";

	private static final String MENU_TEXT = "Tag Proliferators";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public TagProliferatorAction()
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
			final PoolCollectionWrapper< Spot > spots = getGraph().vertices();
			final RefSet< Spot > rootSpots = RefCollections.createRefSet( spots );
			final ObjTagMap< Spot, Tag > spotTagMap = getVertexTagMap( getProliferatorTagSet() );
			final ObjTagMap< Link, Tag > edgeTagMap = getEdgeTagMap( getProliferatorTagSet() );
			final Tag tagProliferator = getTag( getProliferatorTagSet(), PROLIFERATOR_PROLIFERATOR_TAG_NAME );
			final Tag tagNonproliferator = getTag( getProliferatorTagSet(), PROLIFERATOR_NONPROLIFERATOR_TAG_NAME );
			final Tag tagInvisible = getTag( getProliferatorTagSet(), PROLIFERATOR_INVISIBLE_TAG_NAME );
			for ( final Spot spot : spots )
			{
				spotTagMap.set( spot, tagProliferator );
				if ( spot.incomingEdges().isEmpty() )
					rootSpots.add( spot );
			}
			for ( final Link edge : getGraph().edges() )
				edgeTagMap.set( edge, tagProliferator );
			for ( final Spot rootSpot : rootSpots )
			{
				final int rootSpotTimepoint = rootSpot.getTimepoint();
				final RefSet< Spot > spotsInTrack = RefCollections.createRefSet( spots );
				final RefSet< Link > edgesInTrack = RefCollections.createRefSet( getGraph().edges() );
				if ( !isProliferator( rootSpot, spotsInTrack, edgesInTrack ) )
				{
					if ( rootSpotTimepoint < TIMEPOINT_THRESHOLD_LOWER && TIMEPOINT_THRESHOLD_HIGHER < rootSpot.getTimepoint() )
					{
						for ( final Spot spot : spotsInTrack )
							spotTagMap.set( spot, tagNonproliferator );
						for ( final Link edge : edgesInTrack )
							edgeTagMap.set( edge, tagNonproliferator );
					}
					else
					{
						for ( final Spot spot : spotsInTrack )
							spotTagMap.set( spot, tagInvisible );
						for ( final Link edge : edgesInTrack )
							edgeTagMap.set( edge, tagInvisible );
					}
				}
			}
		}
		finally
		{
			getModel().setUndoPoint();
			getActionStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

	private boolean isProliferator(
			final Spot spot,
			final RefCollection< Spot > spotsInTrack,
			final RefCollection< Link > edgesInTrack )
	{
		if ( 1 < spot.outgoingEdges().size() )
			return true;
		spotsInTrack.add( spot );
		final RefList< Link > outgoingEdges = RefCollections.createRefList( getGraph().edges() );
		for ( final Link edge : spot.outgoingEdges() )
		{
			outgoingEdges.add( edge );
			edgesInTrack.add( edge );
		}
		for ( final Link edge : outgoingEdges )
		{
			if ( isProliferator( edge.getTarget( spot ), spotsInTrack, edgesInTrack ) )
				return true;
		}
		return false;
	}
}

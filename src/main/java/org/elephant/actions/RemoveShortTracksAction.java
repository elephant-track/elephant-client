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
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
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
 * Remove the tracks shorter than the specified length. If there are branches in
 * the track, the longest path is evaluated.
 * 
 * @author Ko Sugawara
 */
public class RemoveShortTracksAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] remove short tracks";

	private static final String MENU_TEXT = "Remove Short Tracks";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RemoveShortTracksAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final AtomicInteger minLinksAtomic = new AtomicInteger( -1 );
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final IntegerInputDialog dialog = new IntegerInputDialog( getAppModel().getMaxTimepoint() );
				dialog.setVisible( true );
				try
				{
					if ( !dialog.isCanceled() )
					{
						minLinksAtomic.set( dialog.getMinimumNumberOfLinks() );
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
			getClientLogger().severe( ExceptionUtils.getStackTrace( e1 ) );;
		}
		final int minLinks = minLinksAtomic.get();
		if ( minLinks == -1 )
			return;

		getGraph().getLock().writeLock().lock();
		getActionStateManager().setWriting( true );
		try
		{
			final PoolCollectionWrapper< Spot > spots = getGraph().vertices();
			final RefSet< Spot > rootSpots = RefCollections.createRefSet( spots );
			for ( final Spot spot : spots )
			{
				if ( spot.incomingEdges().isEmpty() )
					rootSpots.add( spot );
			}
			final RefSet< Spot > toRemove = RefCollections.createRefSet( spots );
			final ObjTagMap< Spot, Tag > tagMapDetection = getVertexTagMap( getDetectionTagSet() );
			final ObjTagMap< Spot, Tag > tagMapTracking = getVertexTagMap( getTrackingTagSet() );
			final Tag detectionUnlabeledTag = getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME );
			final Tag trackingApprovedTag = getTag( getTrackingTagSet(), TRACKING_APPROVED_TAG_NAME );
			final Tag trackingUnlabeledTag = getTag( getTrackingTagSet(), TRACKING_UNLABELED_TAG_NAME );
			for ( final Spot spot : rootSpots )
			{
				final RefSet< Spot > spotsInTrack = RefCollections.createRefSet( spots );
				final int startTimepoint = spot.getTimepoint();
				if ( !shouldKeep( spot, spotsInTrack, tagMapDetection, tagMapTracking,
						detectionUnlabeledTag, trackingApprovedTag, trackingUnlabeledTag, startTimepoint, minLinks ) )
					toRemove.addAll( spotsInTrack );
			}
			for ( final Spot spot : toRemove )
				getGraph().remove( spot );
		}
		finally
		{
			getModel().setUndoPoint();
			getActionStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

	private boolean shouldKeep(
			final Spot spot,
			final RefCollection< Spot > spotsInTrack,
			final ObjTagMap< Spot, Tag > tagMapDetection,
			final ObjTagMap< Spot, Tag > tagMapTracking,
			final Tag detectionUnlabeledTag,
			final Tag trackingApprovedTag,
			final Tag trackingUnlabeledTag,
			final int startTimepoint,
			final int minLinks )
	{
		if ( ( tagMapTracking.get( spot ) == trackingApprovedTag ) ||
				( minLinks <= spot.getTimepoint() - startTimepoint ) )
			return true;
		if ( ( tagMapDetection.get( spot ) == detectionUnlabeledTag ) &&
				( tagMapTracking.get( spot ) == trackingUnlabeledTag ) )
			spotsInTrack.add( spot );
		final RefList< Link > outgoingEdges = RefCollections.createRefList( getGraph().edges() );
		for ( final Link edge : spot.outgoingEdges() )
			outgoingEdges.add( edge );
		for ( final Link edge : outgoingEdges )
		{
			if ( shouldKeep( edge.getTarget( spot ), spotsInTrack, tagMapDetection, tagMapTracking,
					detectionUnlabeledTag, trackingApprovedTag, trackingUnlabeledTag, startTimepoint, minLinks ) )
				return true;
		}
		return false;
	}

}

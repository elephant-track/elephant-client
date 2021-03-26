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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.TimepointActionMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.revised.model.mamut.Spot;

/**
 * Remove the spots in the visible area in the target BDV window.
 * 
 * <p>
 * The operation is applied for the time range specified in the settings
 * backward in time.
 * 
 * @author Ko Sugawara
 */
public class RemoveVisibleSpotsAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin, ElephantStateManagerMixin, ElephantSettingsMixin, TimepointActionMixin, WindowManagerMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] remove visible spots";

	private static final String MENU_TEXT = "Remove Visible Spots";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RemoveVisibleSpotsAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final int timepointEnd = getCurrentTimepoint( 0 );
		final int timeRange = getStateManager().isLivemode() ? 1 : getMainSettings().getTimeRange();
		final int timepointStart = Math.max( 0, timepointEnd - timeRange + 1 );
		final List< Integer > timepoints = IntStream.rangeClosed( timepointStart, timepointEnd ).boxed().collect( Collectors.toList() );

		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			for ( final int t : timepoints )
			{
				final Iterable< Spot > spots = getVisibleVertices( t );
				if ( spots != null )
				{
					final RefCollection< Spot > refSet = RefCollections.createRefSet( getGraph().vertices() );
					for ( final Spot spot : spots )
						refSet.add( spot );
					Predicate< Spot > spotFilter = spot -> getVertexTagMap( getDetectionTagSet() ).get( spot ) == getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME );
					spotFilter = spotFilter.and( spot -> getVertexTagMap( getTrackingTagSet() ).get( spot ) == getTag( getTrackingTagSet(), TRACKING_UNLABELED_TAG_NAME ) );
					removeSpots( refSet, spotFilter );
				}
			}
		}
		finally
		{
			getStateManager().setWriting( false );
			getModel().setUndoPoint();
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

}

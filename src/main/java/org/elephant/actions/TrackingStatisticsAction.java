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

import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.elephant.actions.mixins.ElephantTagActionMixin;
import org.elephant.actions.mixins.GraphActionMixin;
import org.elephant.actions.mixins.TimepointActionMixin;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;

/**
 * Display the summary of the tracking in the following format.
 * 
 * <pre>
 * All timepoints:
 *     Approved: 102 unlabeled: 1967
 * Timepoint (0):
 *     Approved: 55  unlabeled: 193
 * </pre>
 * 
 * @author Ko Sugawara
 */
public class TrackingStatisticsAction extends AbstractElephantAction
		implements GraphActionMixin, ElephantTagActionMixin, TimepointActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] tracking statistics";

	private static final String MENU_TEXT = "Tracking Statistics";

	private static final String[] MENU_KEYS = new String[] { "alt M" };

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	@Override
	public String[] getMenuKeys()
	{
		return MENU_KEYS;
	}

	public TrackingStatisticsAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		getGraph().getLock().readLock().lock();
		try
		{
			final ObjTagMap< Spot, Tag > tagMap = getVertexTagMap( getTrackingTagSet() );
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append( "All timepoints:" );
			getGraph().vertices().stream()
					.collect( Collectors.groupingBy( spot -> tagMap.get( spot ).label(), Collectors.counting() ) )
					.forEach( ( k, v ) -> stringBuilder.append( String.format( "\n\t%s: %d", k, v ) ) );
			final int currentTimepoint = getCurrentTimepoint( 0 );
			stringBuilder.append( String.format( "\nTimepoint (%d): ", currentTimepoint ) );
			getGraph().vertices().stream()
					.filter( spot -> spot.getTimepoint() == currentTimepoint )
					.collect( Collectors.groupingBy( spot -> tagMap.get( spot ).label(), Collectors.counting() ) )
					.forEach( ( k, v ) -> stringBuilder.append( String.format( "\n\t%s: %d", k, v ) ) );
			SwingUtilities.invokeLater( () -> JOptionPane.showMessageDialog( null, new JTextArea( stringBuilder.toString() ) ) );
		}
		finally
		{
			getGraph().getLock().readLock().unlock();
		}
	}

}

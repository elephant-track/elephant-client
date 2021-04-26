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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.GraphActionMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.model.Spot;

/**
 * Randomly sample N spots specified by the user.
 * 
 * @author Ko Sugawara
 */
public class RandomSampleAction extends AbstractElephantAction
		implements GraphActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] random sample";

	private static final String MENU_TEXT = "Random Sample";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RandomSampleAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final int size = getGraph().vertices().size();
		final AtomicInteger nSamplesAtomic = new AtomicInteger( -1 );
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final IntegerInputDialog dialog = new IntegerInputDialog( size );
				dialog.setVisible( true );
				try
				{
					if ( !dialog.isCanceled() )
					{
						nSamplesAtomic.set( dialog.getMinimumNumberOfLinks() );
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
		final int nSamples = nSamplesAtomic.get();
		if ( nSamples == -1 )
			return;

		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			final List< Integer > indexSampler = IntStream.range( 0, size ).boxed().collect( Collectors.toList() );
			Collections.shuffle( indexSampler );
			final List< Integer > indexToKeep = new ArrayList<>( nSamples );
			for ( int i = 0; i < nSamples; i++ )
				indexToKeep.add( indexSampler.get( i ) );

			final RefSet< Spot > spotsToRemove = RefCollections.createRefSet( getGraph().vertices() );
			int i = 0;
			for ( final Spot spot : getGraph().vertices() )
			{
				if ( !indexToKeep.contains( i++ ) )
					spotsToRemove.add( spot );
			}
			for ( final Spot spot : spotsToRemove )
				getGraph().remove( spot );
		}
		finally
		{
			getModel().setUndoPoint();
			getStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}
}

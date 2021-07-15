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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.GraphChangeActionMixin;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;

/**
 * Remove all spots and links.
 * 
 * @author Ko Sugawara
 */
public class RemoveAllAction extends AbstractElephantAction
		implements GraphChangeActionMixin, ElephantStateManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] remove all spots and links";

	private static final String MENU_TEXT = "Remove All Spots and Links";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RemoveAllAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final AtomicInteger option = new AtomicInteger();
		try
		{
			SwingUtilities.invokeAndWait( () -> option.set( JOptionPane.showConfirmDialog( null, "All spots and links will be removed", "Select an option", JOptionPane.OK_CANCEL_OPTION ) ) );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		if ( option.get() == JOptionPane.OK_OPTION )
		{
			getGraph().getLock().writeLock().lock();
			getActionStateManager().setWriting( true );
			try
			{
				final RefList< Link > linksToRemove = RefCollections.createRefList( getGraph().edges() );
				final RefList< Spot > spotsToRemove = RefCollections.createRefList( getGraph().vertices() );

				for ( final Link edge : getGraph().edges() )
					linksToRemove.add( edge );
				for ( final Spot spot : getGraph().vertices() )
					spotsToRemove.add( spot );

				for ( final Link edge : linksToRemove )
					getGraph().remove( edge );
				for ( final Spot spot : spotsToRemove )
					getGraph().remove( spot );
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

}

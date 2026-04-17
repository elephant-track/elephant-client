/*-
 * #%L
 * elephant
 * %%
 * Copyright (C) 2019 - 2026 Ko Sugawara
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.elephant.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.mamut.MamutViewBdvWrapper;
import org.mastodon.mamut.WindowManager.ViewCreatedListener;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextListener;
import org.mastodon.views.context.ContextProvider;

public class BdvContextService extends AbstractElephantService implements ContextListener< Spot >, WindowManagerMixin
{
	private static final long serialVersionUID = 1L;

	private Context< Spot > context;

	private ContextChooser< Spot > contextChooser;

	private final ViewCreatedListener< MamutViewBdv > bdvViewCreatedListener;

	/**
	 * The {@link ContextProvider}s of all currently open BigDataViewer windows.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	public BdvContextService()
	{
		super();
		bdvViewCreatedListener = new ViewCreatedListener< MamutViewBdv >()
		{

			@Override
			public void viewCreated( MamutViewBdv bdv )
			{
				final MamutViewBdvWrapper bdvWrapper = new MamutViewBdvWrapper( bdv );
				bdvWrapper.getContextProvider().listeners().add( BdvContextService.this );
				contextProviders.add( bdvWrapper.getContextProvider() );
				contextChooser.updateContextProviders( contextProviders );
				contextChooser.getProviders().remove( 0 ); // remove the default "full graph" option
				bdv.onClose( () -> {
					contextProviders.remove( bdvWrapper.getContextProvider() );
					contextChooser.updateContextProviders( contextProviders );
					contextChooser.getProviders().remove( 0 ); // remove the default "full graph" option
				} );
			}
		};
	}

	@Override
	public void init( final ProjectModel pluginAppModel )
	{
		super.init( pluginAppModel, null );

		contextChooser = new ContextChooser<>( this );
		contextChooser.getProviders().remove( 0 ); // remove the default "full graph" option
		getWindowManager().viewCreatedListeners( MamutViewBdv.class ).add( bdvViewCreatedListener );
	}

	public Iterable< Spot > getVisibleVertices( final int timepoint )
	{
		if ( context == null )
		{
			return null;
		}
		return context.getInsideVertices( timepoint );
	}

	public boolean showContextChooserDialog()
	{
		if ( contextProviders.size() == 0 )
		{
			try
			{
				SwingUtilities.invokeAndWait( () -> JOptionPane.showMessageDialog( null, "There are no BDV windows open" ) );
			}
			catch ( InvocationTargetException | InterruptedException e )
			{
				getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}
			return false;
		}
		final AtomicBoolean isCanceled = new AtomicBoolean(); // false by default
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final ContextChooserDialog< Spot > dialog = new ContextChooserDialog<>( contextChooser );
				dialog.setVisible( true );
				try
				{
					isCanceled.set( dialog.isCanceled() );
				}
				finally
				{
					dialog.dispose();
				}
			} );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		return !isCanceled.get();
	}

	@Override
	public void contextChanged( final Context< Spot > context )
	{
		this.context = context;
	}

}

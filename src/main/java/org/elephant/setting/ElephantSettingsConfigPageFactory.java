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
package org.elephant.setting;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;

import org.elephant.setting.main.ElephantMainSettings;
import org.elephant.setting.main.ElephantMainSettingsPanel;
import org.elephant.setting.main.ElephantMainSettingsPanel.MainSettingsMode;
import org.elephant.setting.server.ElephantServerSettings;
import org.elephant.setting.server.ElephantServerSettingsPanel;
import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.ProfileEditPanel;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage.ProfileManager;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.util.Listeners;

public class ElephantSettingsConfigPageFactory< M extends AbstractElephantSettingsManager< M, S >, S extends AbstractElephantSettings< S > >
{
	final static String BASIC_SETTINGS_KEY = "Basic Settings";

	final static String ADVANCED_SETTINGS_KEY = "Advanced Settings";

	public enum TreePath
	{
		MAIN
		{
			@Override
			public String toString()
			{
				return "Main Settings";
			}
		},
		SERVER
		{
			@Override
			public String toString()
			{
				return "Server Settings";
			}
		}
	}

	final ProfileManager< StyleProfile< S > > profileManager;

	final S editedStyle;

	public ElephantSettingsConfigPageFactory( final M settingsManager )
	{
		profileManager = new StyleProfileManager< M, S >( settingsManager, settingsManager.getStaticInstanceNoBuiltInStyles() );
		editedStyle = settingsManager.getDefaultStyle().copy( "Edited" );
	}

	public ElephantSettingsConfigPage< S > create( final TreePath treePath ) throws TypeMismatchException
	{
		return create( treePath, "unit" );
	}

	public ElephantSettingsConfigPage< S > create( final TreePath treePath, final String unit ) throws TypeMismatchException
	{
		return create( treePath, unit, Collections.< ModificationListener >emptyList() );
	}

	public ElephantSettingsConfigPage< S > create( final TreePath treePath, final String unit, final Collection< ModificationListener > listeners ) throws TypeMismatchException
	{
		JPanel styleEditorPanel = null;
		switch ( treePath )
		{
		case MAIN:
			if ( editedStyle instanceof ElephantMainSettings )
			{
				styleEditorPanel = new JPanel( new BorderLayout() );
				final JPanel cards = new JPanel();
				cards.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
				styleEditorPanel.add( cards, BorderLayout.CENTER );
				cards.setLayout( new CardLayout( 0, 0 ) );

				final JPanel basicSettingsPanel = new ElephantMainSettingsPanel( ( ElephantMainSettings ) editedStyle, MainSettingsMode.BASIC, unit );
				cards.add( basicSettingsPanel, BASIC_SETTINGS_KEY );

				final JPanel advancedSettingsPanel = new ElephantMainSettingsPanel( ( ElephantMainSettings ) editedStyle, MainSettingsMode.ADVANCED );
				cards.add( advancedSettingsPanel, ADVANCED_SETTINGS_KEY );

				final JPanel container = new JPanel();
				styleEditorPanel.add( container, BorderLayout.PAGE_START );
				container.setLayout( new GridLayout() );

				final JButton basicSettingsButton = new JButton( BASIC_SETTINGS_KEY );
				basicSettingsButton.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
				basicSettingsButton.setUI( new UnderlineJButtonUI() );
				basicSettingsButton.setSelected( true );
				container.add( basicSettingsButton );
				final JButton advancedSettingsButton = new JButton( ADVANCED_SETTINGS_KEY );
				advancedSettingsButton.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
				advancedSettingsButton.setUI( new UnderlineJButtonUI() );
				advancedSettingsButton.setSelected( false );
				container.add( advancedSettingsButton );
				basicSettingsButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						basicSettingsButton.setSelected( true );
						advancedSettingsButton.setSelected( false );
						final CardLayout cl = ( CardLayout ) ( cards.getLayout() );
						cl.show( cards, BASIC_SETTINGS_KEY );
					}
				} );
				advancedSettingsButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						basicSettingsButton.setSelected( false );
						advancedSettingsButton.setSelected( true );
						final CardLayout cl = ( CardLayout ) ( cards.getLayout() );
						cl.show( cards, ADVANCED_SETTINGS_KEY );
					}
				} );
				break;
			}
		case SERVER:
			if ( editedStyle instanceof ElephantServerSettings )
			{
				styleEditorPanel = new ElephantServerSettingsPanel( ( ElephantServerSettings ) editedStyle );
				break;
			}
		default:
			throw new TypeMismatchException( "treePath " + treePath.toString() + " and class " + editedStyle.getClass() + " are not compatible" );
		}
		return new ElephantSettingsConfigPage< S >( treePath.toString(),
				profileManager,
				new ElephantSettingsProfileEditPanel< S >( editedStyle, styleEditorPanel, listeners ) );
	}

	static class ElephantSettingsProfileEditPanel< S extends AbstractElephantSettings< S > > implements SettingsUpdateListener, ProfileEditPanel< StyleProfile< S > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final S editedStyle;

		private final JPanel styleEditorPanel;

		public ElephantSettingsProfileEditPanel( final S editedStyle, final JPanel styleEditorPanel, final Collection< ModificationListener > listeners )
		{
			this.editedStyle = editedStyle;
			this.styleEditorPanel = styleEditorPanel;
			modificationListeners = new Listeners.SynchronizedList<>();
			modificationListeners.addAll( listeners );
			editedStyle.updateListeners().add( this );
		}

		private boolean trackModifications = true;

		@Override
		public void settingsUpdated()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final StyleProfile< S > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< S > profile )
		{
			trackModifications = false;
			editedStyle.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return styleEditorPanel;
		}
	}

	static class UnderlineJButtonUI extends BasicButtonUI
	{
		@Override
		public void paint( Graphics g, JComponent c )
		{
			super.paint( g, c );
			final AbstractButton b = ( AbstractButton ) c;
			g.setColor( b.isSelected() ? UIManager.getColor( "Tree.selectionBackground" ) : UIManager.getColor( "windowBorder" ) );
			g.drawLine( 0, b.getHeight() - 1, b.getWidth(), b.getHeight() - 1 );
		}
	}
}

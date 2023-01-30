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
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.elephant.setting.main.ElephantMainSettingsListener;
import org.mastodon.app.ui.SettingsPanel;

import bdv.ui.settings.SettingsPage;

/**
 * A base dialog for ELEPHHANT Preferences.
 * 
 * @author Ko Sugawara
 */
public class ElephantSettingsDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private final SettingsPanel settingsPanel;

	private final List< ElephantMainSettingsListener > listeners = new ArrayList<>();

	public ElephantSettingsDialog()
	{
		super( ( Frame ) null, "ELEPHANT Preferences", false );
		settingsPanel = new SettingsPanel();
		settingsPanel.onOk( () -> {
			for ( final ElephantMainSettingsListener listener : listeners )
				listener.mainSettingsUpdated();
			setVisible( false );
		} );
		settingsPanel.onCancel( () -> setVisible( false ) );

		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settingsPanel.cancel();
			}
		} );

		getContentPane().add( settingsPanel, BorderLayout.CENTER );
		pack();
	}

	public void addSettingsListener( final ElephantMainSettingsListener listener )
	{
		listeners.add( listener );
	}

	public void addPage( final SettingsPage page )
	{
		settingsPanel.addPage( page );
		pack();
	}

	public void removePage( final String path )
	{
		settingsPanel.removePage( path );
		pack();
	}

}

/*******************************************************************************
 * Copyright (C) 2021, Ko Sugawara
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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.validator.routines.UrlValidator;
import org.elephant.swing.TextFieldPopup;

public class ModelResetDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private boolean isCanceled = true;

	private final ButtonGroup buttonGroup = new ButtonGroup();

	private static final String KEY_VERSATILE = "Versatile";

	private static final String KEY_DEFAULT = "Default";

	private static final String KEY_FROM_FILE = "From file";

	private static final String KEY_FROM_URL = "From URL";

	private final UrlValidator urlValidator = new UrlValidator( new String[] { "http", "https" } );

	final AtomicReference< File > fileReference = new AtomicReference<>();

	private final JTextField textFieldUrl;

	private final JLabel labelFile = new JLabel();

	private enum Prefs
	{
		FileLocation;
		private static Preferences prefs = Preferences.userRoot().node( Prefs.class.getName() );

		String get()
		{
			return prefs.get( this.name(), System.getProperty( "user.home" ).replace( "\\", "/" ) );
		}

		void put( String value )
		{
			prefs.put( this.name(), value );
		}
	}

	public ModelResetDialog()
	{
		setModal( true );
		setTitle( "Reset model" );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		getContentPane().setLayout( gridBagLayout );

		final JLabel lblWarning = new JLabel( "*** WARNING ***\nmodel will be reset" );
		lblWarning.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbc_lblWarning = new GridBagConstraints();
		gbc_lblWarning.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblWarning.gridx = 0;
		gbc_lblWarning.gridy = 0;
		getContentPane().add( lblWarning, gbc_lblWarning );

		// Radio button panel
		final JPanel rdbtnPanel = new JPanel();
		rdbtnPanel.setLayout( new GridLayout( 6, 1, 0, 0 ) );

		// VERSATILE
		final JRadioButton rdbtnVersatile = new JRadioButton( KEY_VERSATILE );
		rdbtnVersatile.setActionCommand( KEY_VERSATILE );
		rdbtnVersatile.setSelected( true );
		buttonGroup.add( rdbtnVersatile );
		rdbtnPanel.add( rdbtnVersatile );

		// DEFAULT
		final JRadioButton rdbtnSelfSupervised = new JRadioButton( KEY_DEFAULT );
		rdbtnSelfSupervised.setActionCommand( KEY_DEFAULT );
		buttonGroup.add( rdbtnSelfSupervised );
		rdbtnPanel.add( rdbtnSelfSupervised );

		// FROM_FILE
		final JRadioButton rdbtnFromFile = new JRadioButton( KEY_FROM_FILE );
		rdbtnFromFile.setActionCommand( KEY_FROM_FILE );
		buttonGroup.add( rdbtnFromFile );
		rdbtnPanel.add( rdbtnFromFile );

		final JPanel panelFile = new JPanel();
		final GridBagConstraints gbc_panelFile = new GridBagConstraints();
		gbc_panelFile.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelFile.fill = GridBagConstraints.BOTH;
		gbc_panelFile.gridx = 0;
		gbc_panelFile.gridy = 2;
		rdbtnPanel.add( panelFile );
		final GridBagLayout gbl_panelFile = new GridBagLayout();
		gbl_panelFile.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelFile.rowHeights = new int[] { 0 };
		gbl_panelFile.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_panelFile.rowWeights = new double[] { 0.0 };
		panelFile.setLayout( gbl_panelFile );

		final JLabel lblFile = new JLabel( "File: " );
		final GridBagConstraints gbc_lblFile = new GridBagConstraints();
		gbc_lblFile.insets = new Insets( 0, 0, 0, 5 );
		gbc_lblFile.fill = GridBagConstraints.BOTH;
		gbc_lblFile.gridx = 0;
		gbc_lblFile.gridy = 0;
		panelFile.add( lblFile, gbc_lblFile );

		final JButton btnBrowse = new JButton( "Browse" );
		btnBrowse.setHorizontalAlignment( SwingConstants.LEADING );
		btnBrowse.addActionListener( event -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle( "Select a file" );
			fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			fileChooser.setFileFilter( new FileNameExtensionFilter( "*.pth", "pth" ) );
			fileChooser.setCurrentDirectory( new File( Prefs.FileLocation.get() ) );
			fileChooser.setAcceptAllFileFilterUsed( false );

			final int userSelection = fileChooser.showOpenDialog( null );

			if ( userSelection == JFileChooser.APPROVE_OPTION )
			{
				fileReference.set( fileChooser.getSelectedFile() );
				Prefs.FileLocation.put( fileChooser.getSelectedFile().getAbsolutePath() );
				labelFile.setText( fileChooser.getSelectedFile().getAbsolutePath() );
			}
		} );
		final GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.anchor = GridBagConstraints.WEST;
		gbc_btnBrowse.gridx = 1;
		gbc_btnBrowse.gridy = 0;
		panelFile.add( btnBrowse, gbc_btnBrowse );

		final GridBagConstraints gbc_editorPaneFile = new GridBagConstraints();
		gbc_editorPaneFile.insets = new Insets( 0, 0, 0, 5 );
		gbc_editorPaneFile.fill = GridBagConstraints.BOTH;
		gbc_editorPaneFile.gridx = 2;
		gbc_editorPaneFile.gridy = 0;
		panelFile.add( labelFile, gbc_editorPaneFile );

		// FROM_URL
		final JRadioButton rdbtnFromUrl = new JRadioButton( KEY_FROM_URL );
		rdbtnFromUrl.setActionCommand( KEY_FROM_URL );
		buttonGroup.add( rdbtnFromUrl );
		rdbtnPanel.add( rdbtnFromUrl );
		final GridBagConstraints gbc_rdbtnPanel = new GridBagConstraints();
		gbc_rdbtnPanel.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtnPanel.fill = GridBagConstraints.BOTH;
		gbc_rdbtnPanel.gridx = 0;
		gbc_rdbtnPanel.gridy = 1;

		final JPanel panelUrl = new JPanel();
		final GridBagConstraints gbc_panelUrl = new GridBagConstraints();
		gbc_panelUrl.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelUrl.fill = GridBagConstraints.BOTH;
		gbc_panelUrl.gridx = 0;
		gbc_panelUrl.gridy = 2;
		rdbtnPanel.add( panelUrl );
		final GridBagLayout gbl_panelUrl = new GridBagLayout();
		gbl_panelUrl.columnWidths = new int[] { 0, 0 };
		gbl_panelUrl.rowHeights = new int[] { 0 };
		gbl_panelUrl.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panelUrl.rowWeights = new double[] { 0.0 };
		panelUrl.setLayout( gbl_panelUrl );

		final JLabel lblUrl = new JLabel( "URL: " );
		final GridBagConstraints gbc_lblUrl = new GridBagConstraints();
		gbc_lblUrl.fill = GridBagConstraints.BOTH;
		gbc_lblUrl.gridx = 0;
		gbc_lblUrl.gridy = 0;
		panelUrl.add( lblUrl, gbc_lblUrl );

		textFieldUrl = new TextFieldPopup( 50 );
		final GridBagConstraints gbc_editorPaneUrl = new GridBagConstraints();
		gbc_editorPaneUrl.fill = GridBagConstraints.BOTH;
		gbc_editorPaneUrl.gridx = 1;
		gbc_editorPaneUrl.gridy = 0;
		panelUrl.add( textFieldUrl, gbc_editorPaneUrl );

		getContentPane().add( rdbtnPanel, gbc_rdbtnPanel );

		final JPanel panelButtons = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelButtons.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		final GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.fill = GridBagConstraints.BOTH;
		gbc_panelButtons.gridx = 0;
		gbc_panelButtons.gridy = 2;
		getContentPane().add( panelButtons, gbc_panelButtons );

		final JButton btnCancel = new JButton( "Cancel" );
		btnCancel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setVisible( false );
			}
		} );
		panelButtons.add( btnCancel );

		final JButton btnOk = new JButton( "OK" );
		btnOk.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent event )
			{
				final String key = buttonGroup.getSelection().getActionCommand();
				final StringBuilder sbWarning = new StringBuilder();
				if ( key.equals( KEY_FROM_FILE ) && fileReference.get() == null )
				{
					sbWarning.append( "File is not specified" );
				}
				else if ( key.equals( KEY_FROM_URL ) && !urlValidator.isValid( textFieldUrl.getText() ) )
				{
					sbWarning.append( "URL is invalid" );
				}
				if ( 0 < sbWarning.length() )
				{
					JOptionPane.showMessageDialog( null, sbWarning.toString(), "Warning", JOptionPane.WARNING_MESSAGE );
				}
				else
				{
					isCanceled = false;
					setVisible( false );
				}
			}
		} );
		panelButtons.add( btnOk );

		pack();
		setLocationRelativeTo( null );
	}

	public boolean isCanceled()
	{
		return isCanceled;
	}

	public String getUrl()
	{
		final String key = buttonGroup.getSelection().getActionCommand();
		if ( key.equals( KEY_FROM_URL ) )
			return textFieldUrl.getText();
		else if ( key.equals( KEY_VERSATILE ) )
			return KEY_VERSATILE;
		return null;
	}

	public File getFile()
	{
		final String key = buttonGroup.getSelection().getActionCommand();
		if ( key.equals( KEY_FROM_FILE ) ) { return fileReference.get(); }
		return null;
	}

}

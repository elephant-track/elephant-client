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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * A dialog used in {@link TakeSnapshotAction}.
 * 
 * @author Ko Sugawara
 */
public class SnapshotDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private final JComboBox< String > comboBoxWindowName;

	private boolean isCanceled = true;

	private JTextField textFieldSavePath;

	private enum Prefs
	{
		FileLocation;
		private static Preferences prefs = Preferences.userRoot().node( Prefs.class.getName() );

		String get()
		{
			return prefs.get( this.name(), System.getProperty( "user.home" ).replace("\\", "/") );
		}

		void put( String value )
		{
			prefs.put( this.name(), value );
		}
	}

	/**
	 * Create the dialog.
	 */
	public SnapshotDialog( final String[] windowNames )
	{
		setModal( true );
		setTitle( "Snapshot settings" );
		setBounds( 100, 100, 450, 300 );
		getContentPane().setLayout( new BorderLayout() );
		contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		getContentPane().add( contentPanel, BorderLayout.CENTER );
		final GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 1, 1, 1, 0 };
		gbl_contentPanel.rowHeights = new int[] { 1, 1, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout( gbl_contentPanel );
		{
			final JLabel lblWindow = new JLabel( "A window to save" );
			final GridBagConstraints gbc_lblWindow = new GridBagConstraints();
			gbc_lblWindow.insets = new Insets( 0, 10, 5, 10 );
			gbc_lblWindow.anchor = GridBagConstraints.EAST;
			gbc_lblWindow.weighty = 0.5;
			gbc_lblWindow.gridx = 0;
			gbc_lblWindow.gridy = 0;
			contentPanel.add( lblWindow, gbc_lblWindow );
		}
		{
			comboBoxWindowName = new JComboBox<>();
			comboBoxWindowName.setModel( new DefaultComboBoxModel<>( windowNames ) );
			final GridBagConstraints gbc_comboBoxWindowName = new GridBagConstraints();
			gbc_comboBoxWindowName.gridwidth = 2;
			gbc_comboBoxWindowName.weightx = 0.5;
			gbc_comboBoxWindowName.weighty = 0.5;
			gbc_comboBoxWindowName.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxWindowName.insets = new Insets( 0, 0, 5, 20 );
			gbc_comboBoxWindowName.gridx = 1;
			gbc_comboBoxWindowName.gridy = 0;
			contentPanel.add( comboBoxWindowName, gbc_comboBoxWindowName );
		}
		{
			final JLabel lblSavePath = new JLabel( "Save path" );
			final GridBagConstraints gbc_lblSavePath = new GridBagConstraints();
			gbc_lblSavePath.insets = new Insets( 0, 10, 5, 10 );
			gbc_lblSavePath.anchor = GridBagConstraints.EAST;
			gbc_lblSavePath.gridx = 0;
			gbc_lblSavePath.gridy = 1;
			contentPanel.add( lblSavePath, gbc_lblSavePath );
		}
		{
			textFieldSavePath = new JTextField();
			textFieldSavePath.setText( Prefs.FileLocation.get() );
			final GridBagConstraints gbc_textFieldSavePath = new GridBagConstraints();
			gbc_textFieldSavePath.weighty = 0.5;
			gbc_textFieldSavePath.weightx = 0.5;
			gbc_textFieldSavePath.insets = new Insets( 0, 0, 0, 5 );
			gbc_textFieldSavePath.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldSavePath.gridx = 1;
			gbc_textFieldSavePath.gridy = 1;
			contentPanel.add( textFieldSavePath, gbc_textFieldSavePath );
			textFieldSavePath.setColumns( 10 );
		}
		{
			final JButton btnNewButton = new JButton( "Browse" );
			btnNewButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					final JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle( "Specify a file to save" );
					fileChooser.setCurrentDirectory( new File( Prefs.FileLocation.get() ).getParentFile() );

					final int userSelection = fileChooser.showSaveDialog( SnapshotDialog.this );

					if ( userSelection == JFileChooser.APPROVE_OPTION )
					{
						String pathString = fileChooser.getSelectedFile().getAbsolutePath();
						if ( !pathString.endsWith( ".png" ) )
							pathString = pathString + ".png";
						textFieldSavePath.setText( pathString );
						Prefs.FileLocation.put( pathString );
					}
				}
			} );
			final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.weighty = 0.5;
			gbc_btnNewButton.gridx = 2;
			gbc_btnNewButton.gridy = 1;
			contentPanel.add( btnNewButton, gbc_btnNewButton );
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
			getContentPane().add( buttonPane, BorderLayout.SOUTH );
			{
				final JButton cancelButton = new JButton( "Cancel" );
				cancelButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						setVisible( false );
					}
				} );
				cancelButton.setActionCommand( "Cancel" );
				buttonPane.add( cancelButton );
			}
			{
				final JButton okButton = new JButton( "OK" );
				okButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						Prefs.FileLocation.put( textFieldSavePath.getText() );
						isCanceled = false;
						setVisible( false );
					}
				} );
				okButton.setActionCommand( "OK" );
				buttonPane.add( okButton );
				getRootPane().setDefaultButton( okButton );
			}
		}
	}

	public boolean isCanceled()
	{
		return isCanceled;
	}

	public String getSelectedWindowName()
	{
		return comboBoxWindowName.getSelectedItem().toString();
	}

	public String getSaveFilePath()
	{
		return textFieldSavePath.getText();
	}

}

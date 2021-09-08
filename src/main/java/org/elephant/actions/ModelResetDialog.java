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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.elephant.swing.TextFieldPopup;

public class ModelResetDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private boolean isCanceled = true;

	private final ButtonGroup buttonGroup = new ButtonGroup();

	private static final String KEY_VERSATILE = "Versatile";

	private static final String KEY_DEFAULT = "Default";

	private static final String KEY_FROM_URL = "From URL";

	private final JTextField textFieldUrl;

	public ModelResetDialog()
	{
		setModal( true );
		setTitle( "Reset model" );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		getContentPane().setLayout( gridBagLayout );

		final JLabel lblWarning = new JLabel( "*** WARNING ***\nmodel will be reset" );
		lblWarning.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbc_lblWarning = new GridBagConstraints();
		gbc_lblWarning.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblWarning.gridx = 0;
		gbc_lblWarning.gridy = 0;
		getContentPane().add( lblWarning, gbc_lblWarning );

		final JPanel rdbtnPanel = new JPanel();
		rdbtnPanel.setLayout( new GridLayout( 3, 1, 0, 0 ) );

		final JRadioButton rdbtnVersatile = new JRadioButton( KEY_VERSATILE );
		rdbtnVersatile.setActionCommand( KEY_VERSATILE );
		rdbtnVersatile.setSelected( true );
		buttonGroup.add( rdbtnVersatile );
		rdbtnPanel.add( rdbtnVersatile );

		final JRadioButton rdbtnSelfSupervised = new JRadioButton( KEY_DEFAULT );
		rdbtnSelfSupervised.setActionCommand( KEY_DEFAULT );
		buttonGroup.add( rdbtnSelfSupervised );
		rdbtnPanel.add( rdbtnSelfSupervised );

		final JRadioButton rdbtnFromUrl = new JRadioButton( KEY_FROM_URL );
		rdbtnFromUrl.setActionCommand( KEY_FROM_URL );
		buttonGroup.add( rdbtnFromUrl );
		rdbtnPanel.add( rdbtnFromUrl );
		final GridBagConstraints gbc_rdbtnPanel = new GridBagConstraints();
		gbc_rdbtnPanel.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtnPanel.fill = GridBagConstraints.BOTH;
		gbc_rdbtnPanel.gridx = 0;
		gbc_rdbtnPanel.gridy = 1;
		getContentPane().add( rdbtnPanel, gbc_rdbtnPanel );

		final JPanel panelUrl = new JPanel();
		final GridBagConstraints gbc_panelUrl = new GridBagConstraints();
		gbc_panelUrl.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelUrl.fill = GridBagConstraints.BOTH;
		gbc_panelUrl.gridx = 0;
		gbc_panelUrl.gridy = 2;
		getContentPane().add( panelUrl, gbc_panelUrl );
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

		final JPanel panelButtons = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelButtons.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		final GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.fill = GridBagConstraints.BOTH;
		gbc_panelButtons.gridx = 0;
		gbc_panelButtons.gridy = 3;
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
			public void actionPerformed( ActionEvent e )
			{
				isCanceled = false;
				setVisible( false );
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

}

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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * A simple dialog to input an int value.
 * 
 * @author Ko Sugawara
 */
public class IntegerInputDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	private boolean isCanceled = true;

	private final JSpinner spinner;

	private static int initialValue = 50;

	public IntegerInputDialog( final int maxLinks )
	{
		setTitle( "Select a value" );
		setModal( true );
		setBounds( 100, 100, 450, 300 );

		final JPanel contentPanel = new JPanel();
		getContentPane().add( contentPanel, BorderLayout.CENTER );
		final GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 1, 1, 0 };
		gbl_contentPanel.rowHeights = new int[] { 1, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		contentPanel.setLayout( gbl_contentPanel );

		final JLabel lblMinLinks = new JLabel( "Minimum number of links" );
		final GridBagConstraints gbc_lblMinLinks = new GridBagConstraints();
		gbc_lblMinLinks.weighty = 1.0;
		gbc_lblMinLinks.weightx = 0.5;
		gbc_lblMinLinks.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblMinLinks.gridx = 0;
		gbc_lblMinLinks.gridy = 0;
		contentPanel.add( lblMinLinks, gbc_lblMinLinks );

		spinner = new JSpinner();
		spinner.setModel( new SpinnerNumberModel( new Integer( initialValue ), new Integer( 0 ), new Integer( maxLinks ), new Integer( 1 ) ) );
		final GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets( 5, 5, 5, 5 );
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.weighty = 1.0;
		gbc_spinner.weightx = 0.5;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		contentPanel.add( spinner, gbc_spinner );

		final JPanel buttonPanel = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) buttonPanel.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		getContentPane().add( buttonPanel, BorderLayout.SOUTH );

		final JButton btnOk = new JButton( "OK" );
		btnOk.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				initialValue = getMinimumNumberOfLinks();
				isCanceled = false;
				setVisible( false );
			}
		} );
		btnOk.setActionCommand( "OK" );
		buttonPanel.add( btnOk );
		getRootPane().setDefaultButton( btnOk );

		final JButton btnCancel = new JButton( "Cancel" );
		btnCancel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setVisible( false );
			}
		} );
		btnCancel.setActionCommand( "Cancel" );
		buttonPanel.add( btnCancel );
	}

	public boolean isCanceled()
	{
		return isCanceled;
	}

	public int getMinimumNumberOfLinks()
	{
		return ( ( Number ) spinner.getValue() ).intValue();
	}

}

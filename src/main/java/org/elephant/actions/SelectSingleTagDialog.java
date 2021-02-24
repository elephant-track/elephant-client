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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.elephant.actions.mixins.ElephantConstantsMixin;

/**
 * A dialog to specify a tag for removing spots or links.
 * 
 * @author Ko Sugawara
 */
public class SelectSingleTagDialog extends JDialog implements ElephantConstantsMixin
{

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private final JComboBox< String > comboBoxTagSet;

	private final JComboBox< String > comboBoxTag;

	private boolean isCanceled = true;

	/**
	 * Create a dialog.
	 */
	public SelectSingleTagDialog( final String[] tagSetNames, final String[] tagNamesDetection, final String[] tagNamesTracking,
			final String[] tagNamesProgenitor, final String[] tagNamesStatus, final String[] tagNamesProliferator, final String[] tagNamesDivision )
	{
		setModal( true );
		setTitle( "Remove Spots/Links By Tag" );
		setBounds( 100, 100, 450, 300 );
		getContentPane().setLayout( new BorderLayout() );
		contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		getContentPane().add( contentPanel, BorderLayout.CENTER );
		final GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 1, 1, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 1, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		contentPanel.setLayout( gbl_contentPanel );
		{
			comboBoxTagSet = new JComboBox<>();
			comboBoxTagSet.setModel( new DefaultComboBoxModel<>( tagSetNames ) );
			comboBoxTagSet.addItemListener( new ItemListener()
			{

				@Override
				public void itemStateChanged( ItemEvent e )
				{
					if ( e.getStateChange() == ItemEvent.SELECTED )
					{
						final String tagSetName = e.getItem().toString();
						if ( tagSetName.equals( DETECTION_TAGSET_NAME ) )
							comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesDetection ) );
						else if ( tagSetName.equals( TRACKING_TAGSET_NAME ) )
							comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesTracking ) );
						else if ( tagSetName.equals( PROGENITOR_TAGSET_NAME ) )
							comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesProgenitor ) );
						else if ( tagSetName.equals( STATUS_TAGSET_NAME ) )
							comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesStatus ) );
						else if ( tagSetName.equals( PROLIFERATOR_TAGSET_NAME ) )
							comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesProliferator ) );
						else if ( tagSetName.equals( DIVISION_TAGSET_NAME ) )
							comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesDivision ) );
						comboBoxTag.setSelectedIndex( 0 );
					}

				}
			} );
			{
				final JLabel lblTagSet = new JLabel( "Tag Set" );
				final GridBagConstraints gbc_lblTagSet = new GridBagConstraints();
				gbc_lblTagSet.insets = new Insets( 0, 10, 0, 10 );
				gbc_lblTagSet.anchor = GridBagConstraints.EAST;
				gbc_lblTagSet.gridx = 0;
				gbc_lblTagSet.gridy = 0;
				contentPanel.add( lblTagSet, gbc_lblTagSet );
			}
			final GridBagConstraints gbc_comboBoxTagSetFrom = new GridBagConstraints();
			gbc_comboBoxTagSetFrom.weightx = 1.0;
			gbc_comboBoxTagSetFrom.weighty = 0.5;
			gbc_comboBoxTagSetFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTagSetFrom.insets = new Insets( 0, 0, 0, 20 );
			gbc_comboBoxTagSetFrom.gridx = 1;
			gbc_comboBoxTagSetFrom.gridy = 0;
			contentPanel.add( comboBoxTagSet, gbc_comboBoxTagSetFrom );
		}
		{
			final JLabel lblTag = new JLabel( "Tag" );
			final GridBagConstraints gbc_lblTag = new GridBagConstraints();
			gbc_lblTag.insets = new Insets( 0, 10, 0, 10 );
			gbc_lblTag.anchor = GridBagConstraints.EAST;
			gbc_lblTag.gridx = 2;
			gbc_lblTag.gridy = 0;
			contentPanel.add( lblTag, gbc_lblTag );
		}
		{
			comboBoxTag = new JComboBox<>();
			comboBoxTag.setModel( new DefaultComboBoxModel<>( tagNamesDetection ) );
			final GridBagConstraints gbc_comboBoxTagFrom = new GridBagConstraints();
			gbc_comboBoxTagFrom.weighty = 0.5;
			gbc_comboBoxTagFrom.weightx = 1.0;
			gbc_comboBoxTagFrom.insets = new Insets( 0, 0, 0, 20 );
			gbc_comboBoxTagFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTagFrom.gridx = 3;
			gbc_comboBoxTagFrom.gridy = 0;
			contentPanel.add( comboBoxTag, gbc_comboBoxTagFrom );
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
			getContentPane().add( buttonPane, BorderLayout.SOUTH );
			{
				final JButton okButton = new JButton( "OK" );
				okButton.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( ActionEvent e )
					{
						isCanceled = false;
						setVisible( false );
					}
				} );
				okButton.setActionCommand( "OK" );
				buttonPane.add( okButton );
				getRootPane().setDefaultButton( okButton );
			}
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
		}
	}

	public boolean isCanceled()
	{
		return isCanceled;
	}

	public String getTagSetName()
	{
		return comboBoxTagSet.getSelectedItem().toString();
	}

	public String getTagName()
	{
		return comboBoxTag.getSelectedItem().toString();
	}

}

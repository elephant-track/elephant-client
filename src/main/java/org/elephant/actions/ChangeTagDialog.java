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
 * A dialog for selecting source and target tags for {@link MapTagAction}.
 * 
 * @author Ko Sugawara
 */
public class ChangeTagDialog extends JDialog implements ElephantConstantsMixin
{

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private final JComboBox< String > comboBoxTagSetFrom;

	private final JComboBox< String > comboBoxTagFrom;

	private final JComboBox< String > comboBoxTagSetTo;

	private final JComboBox< String > comboBoxTagTo;

	private boolean isCanceled = true;

	/**
	 * Create a dialog.
	 */
	public ChangeTagDialog( final String[] tagSetNames, final String[] tagNamesDetection, final String[] tagNamesTracking,
			final String[] tagNamesProgenitor, final String[] tagNamesStatus, final String[] tagNamesProliferator, final String[] tagNamesDivision )
	{
		setModal( true );
		setTitle( "Change Spot/Link Tag" );
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
			final JLabel lblFrom = new JLabel( "From" );
			final GridBagConstraints gbc_lblFrom = new GridBagConstraints();
			gbc_lblFrom.weighty = 0.5;
			gbc_lblFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblFrom.insets = new Insets( 0, 20, 5, 20 );
			gbc_lblFrom.gridx = 0;
			gbc_lblFrom.gridy = 0;
			contentPanel.add( lblFrom, gbc_lblFrom );
		}
		{
			comboBoxTagSetFrom = new JComboBox<>();
			comboBoxTagSetFrom.setModel( new DefaultComboBoxModel<>( tagSetNames ) );
			comboBoxTagSetFrom.addItemListener( new ItemListener()
			{

				@Override
				public void itemStateChanged( ItemEvent e )
				{
					if ( e.getStateChange() == ItemEvent.SELECTED )
					{
						final String tagSetName = e.getItem().toString();
						if ( tagSetName.equals( DETECTION_TAGSET_NAME ) )
							comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesDetection ) );
						else if ( tagSetName.equals( TRACKING_TAGSET_NAME ) )
							comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesTracking ) );
						else if ( tagSetName.equals( PROGENITOR_TAGSET_NAME ) )
							comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesProgenitor ) );
						else if ( tagSetName.equals( STATUS_TAGSET_NAME ) )
							comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesStatus ) );
						else if ( tagSetName.equals( PROLIFERATOR_TAGSET_NAME ) )
							comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesProliferator ) );
						else if ( tagSetName.equals( DIVISION_TAGSET_NAME ) )
							comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesDivision ) );
						comboBoxTagFrom.setSelectedIndex( 0 );
					}

				}
			} );
			final GridBagConstraints gbc_comboBoxTagSetFrom = new GridBagConstraints();
			gbc_comboBoxTagSetFrom.weightx = 1.0;
			gbc_comboBoxTagSetFrom.weighty = 0.5;
			gbc_comboBoxTagSetFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTagSetFrom.insets = new Insets( 0, 0, 5, 20 );
			gbc_comboBoxTagSetFrom.gridx = 1;
			gbc_comboBoxTagSetFrom.gridy = 0;
			contentPanel.add( comboBoxTagSetFrom, gbc_comboBoxTagSetFrom );
		}
		{
			comboBoxTagFrom = new JComboBox<>();
			comboBoxTagFrom.setModel( new DefaultComboBoxModel<>( tagNamesDetection ) );
			final GridBagConstraints gbc_comboBoxTagFrom = new GridBagConstraints();
			gbc_comboBoxTagFrom.weightx = 1.0;
			gbc_comboBoxTagFrom.insets = new Insets( 0, 0, 0, 20 );
			gbc_comboBoxTagFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTagFrom.gridx = 2;
			gbc_comboBoxTagFrom.gridy = 0;
			contentPanel.add( comboBoxTagFrom, gbc_comboBoxTagFrom );
		}
		{
			final JLabel lblTo = new JLabel( "To" );
			final GridBagConstraints gbc_lblTo = new GridBagConstraints();
			gbc_lblTo.weighty = 0.5;
			gbc_lblTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblTo.insets = new Insets( 0, 20, 0, 20 );
			gbc_lblTo.gridx = 0;
			gbc_lblTo.gridy = 1;
			contentPanel.add( lblTo, gbc_lblTo );
		}
		{
			comboBoxTagSetTo = new JComboBox<>();
			comboBoxTagSetTo.setModel( new DefaultComboBoxModel<>( tagSetNames ) );
			comboBoxTagSetTo.addItemListener( new ItemListener()
			{

				@Override
				public void itemStateChanged( ItemEvent e )
				{
					if ( e.getStateChange() == ItemEvent.SELECTED )
					{
						final String tagSetName = e.getItem().toString();
						if ( tagSetName.equals( DETECTION_TAGSET_NAME ) )
							comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesDetection ) );
						else if ( tagSetName.equals( TRACKING_TAGSET_NAME ) )
							comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesTracking ) );
						else if ( tagSetName.equals( PROGENITOR_TAGSET_NAME ) )
							comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesProgenitor ) );
						else if ( tagSetName.equals( STATUS_TAGSET_NAME ) )
							comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesStatus ) );
						else if ( tagSetName.equals( PROLIFERATOR_TAGSET_NAME ) )
							comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesProliferator ) );
						else if ( tagSetName.equals( DIVISION_TAGSET_NAME ) )
							comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesDivision ) );
						comboBoxTagTo.setSelectedIndex( 0 );
					}

				}
			} );
			final GridBagConstraints gbc_comboBoxTagSetTo = new GridBagConstraints();
			gbc_comboBoxTagSetTo.weightx = 1.0;
			gbc_comboBoxTagSetTo.weighty = 0.5;
			gbc_comboBoxTagSetTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTagSetTo.insets = new Insets( 0, 0, 0, 20 );
			gbc_comboBoxTagSetTo.gridx = 1;
			gbc_comboBoxTagSetTo.gridy = 1;
			contentPanel.add( comboBoxTagSetTo, gbc_comboBoxTagSetTo );
		}
		{
			comboBoxTagTo = new JComboBox<>();
			comboBoxTagTo.setModel( new DefaultComboBoxModel<>( tagNamesDetection ) );
			final GridBagConstraints gbc_comboBoxTagTo = new GridBagConstraints();
			gbc_comboBoxTagTo.insets = new Insets( 0, 0, 0, 20 );
			gbc_comboBoxTagTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTagTo.gridx = 2;
			gbc_comboBoxTagTo.gridy = 1;
			contentPanel.add( comboBoxTagTo, gbc_comboBoxTagTo );
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

	public String getTagSetNameFrom()
	{
		return comboBoxTagSetFrom.getSelectedItem().toString();
	}

	public String getTagSetNameTo()
	{
		return comboBoxTagSetTo.getSelectedItem().toString();
	}

	public String getTagNameFrom()
	{
		return comboBoxTagFrom.getSelectedItem().toString();
	}

	public String getTagNameTo()
	{
		return comboBoxTagTo.getSelectedItem().toString();
	}

}

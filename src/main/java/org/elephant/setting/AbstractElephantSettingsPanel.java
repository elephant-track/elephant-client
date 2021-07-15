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

import static org.mastodon.app.ui.settings.StyleElements.linkedCheckBox;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.elephant.setting.StyleElementsEx.DoubleElementEx;
import org.elephant.setting.StyleElementsEx.IntElementEx;
import org.elephant.setting.StyleElementsEx.PasswordElement;
import org.elephant.setting.StyleElementsEx.StringElement;
import org.elephant.setting.StyleElementsEx.StyleElementVisitorEx;
import org.mastodon.app.ui.settings.StyleElements.BooleanElement;
import org.mastodon.app.ui.settings.StyleElements.IntElement;
import org.mastodon.app.ui.settings.StyleElements.LabelElement;
import org.mastodon.app.ui.settings.StyleElements.Separator;
import org.mastodon.app.ui.settings.StyleElements.StyleElement;

import bdv.util.BoundedValue;
import bdv.util.BoundedValueDouble;

public abstract class AbstractElephantSettingsPanel< S extends UpdatableStyle< S > > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final int tfCols = 6;

	public AbstractElephantSettingsPanel()
	{
		super( new GridBagLayout() );
	}

	public void build( final S style )
	{
		final List< StyleElement > styleElements = styleElements( style );

		style.updateListeners().add( () -> {
			styleElements.forEach( StyleElement::update );
			repaint();
		} );

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;

		styleElements.forEach( element -> element.accept(
				new StyleElementVisitorEx()
				{
					@Override
					public void visit( final Separator element )
					{
						add( Box.createVerticalStrut( 10 ), c );
						++c.gridy;
					}

					@Override
					public void visit( final LabelElement element )
					{
						c.anchor = GridBagConstraints.LINE_END;
						c.gridwidth = 2;
						add( new JLabel( element.getLabel(), JLabel.TRAILING ), c );
						++c.gridy;
						c.gridwidth = 1;
					}

					@Override
					public void visit( final StringElement element )
					{
						final JTextField textField = linkedTextField( element );
						addToLayout(
								new JLabel( element.getLabel(), JLabel.TRAILING ),
								textField );
					}

					@Override
					public void visit( final PasswordElement element )
					{
						final JPasswordField passwwordField = linkedPasswordField( element );
						passwwordField.setEchoChar( '*' );
						final JCheckBox checkBox = new JCheckBox( "show password" );
						checkBox.addItemListener( new ItemListener()
						{

							@Override
							public void itemStateChanged( ItemEvent e )
							{
								if ( checkBox.isSelected() )
								{
									passwwordField.setEchoChar( ( char ) 0 );
								}
								else
								{
									passwwordField.setEchoChar( '*' );
								}

							}
						} );
						final JPanel container = new JPanel( new GridBagLayout() );
						final GridBagConstraints constraints = new GridBagConstraints();
						constraints.insets = new Insets( 0, 0, 0, 0 );
						constraints.fill = GridBagConstraints.HORIZONTAL;
						constraints.weightx = 1.0;
						constraints.gridwidth = 1;
						constraints.gridx = 0;
						constraints.gridy = 0;
						container.add( passwwordField, constraints );
						constraints.gridx = 1;
						constraints.weightx = 0.0;
						container.add( checkBox, constraints );
						addToLayout(
								new JLabel( element.getLabel(), JLabel.TRAILING ),
								container );
					};

					@Override
					public void visit( final BooleanElement element )
					{
						final JCheckBox checkbox = linkedCheckBox( element, "" );
						checkbox.setHorizontalAlignment( SwingConstants.LEADING );
						addToLayout(
								new JLabel( element.getLabel(), JLabel.TRAILING ),
								checkbox );
					}

					@Override
					public void visit( final DoubleElementEx element )
					{
						addToLayout(
								new JLabel( element.getLabel(), JLabel.TRAILING ),
								new DoubleSpinner( element.getValue(), element.getStepSize() ) );
					}

					@Override
					public void visit( final IntElement element )
					{
						addToLayout(
								new JLabel( element.getLabel(), JLabel.TRAILING ),
								new IntSpinner( element.getValue() ) );
					}

					@Override
					public void visit( final IntElementEx element )
					{
						addToLayout(
								new JLabel( element.getLabel(), JLabel.TRAILING ),
								new IntSpinner( element.getValue(), element.getDecimalFormatPatterne() ) );
					}

					class IntSpinner extends JSpinner implements BoundedValue.UpdateListener
					{
						private static final long serialVersionUID = 2389419890620020612L;

						private final BoundedValue model;

						public IntSpinner( final BoundedValue model )
						{
							this( model, null );
						}

						public IntSpinner( final BoundedValue model, final String decimalFormatPattern )
						{
							super();
							setModel( new SpinnerNumberModel( model.getCurrentValue(), model.getRangeMin(), model.getRangeMax(), Math.max( 1, model.getRangeMin() ) ) );
							addChangeListener( new ChangeListener()
							{
								@Override
								public void stateChanged( final ChangeEvent e )
								{
									final int value = ( ( Integer ) getValue() ).intValue();
									model.setCurrentValue( value );
								}
							} );

							this.model = model;
							if ( decimalFormatPattern != null )
							{
								setEditor( new JSpinner.NumberEditor( this, decimalFormatPattern ) );
							}
							model.setUpdateListener( this );
						}

						@Override
						public void update()
						{
							final int value = model.getCurrentValue();
							setValue( value );
						}
					}

					class DoubleSpinner extends JSpinner implements BoundedValueDouble.UpdateListener
					{
						private static final long serialVersionUID = -2622218113093499557L;

						private final BoundedValueDouble model;

						public DoubleSpinner( final BoundedValueDouble model, final double stepSize )
						{
							super();
							setModel( new SpinnerNumberModel( model.getCurrentValue(), model.getRangeMin(), model.getRangeMax(), stepSize ) );
							addChangeListener( new ChangeListener()
							{
								@Override
								public void stateChanged( final ChangeEvent e )
								{
									final double value = ( ( Double ) getValue() ).doubleValue();
									model.setCurrentValue( value );
								}
							} );

							( ( JSpinner.NumberEditor ) getEditor() ).getFormat().applyPattern( "0.#######" );
							( ( JSpinner.NumberEditor ) getEditor() ).getTextField().setColumns( tfCols );
							// Workaround for avoiding displaying "0" at
							// initialization of the panel, which only happens
							// the value is small (e.g. 0.00001)
							( ( JSpinner.NumberEditor ) getEditor() ).getTextField().setValue( getValue() );

							this.model = model;
							model.setUpdateListener( this );
						}

						@Override
						public void update()
						{
							final double value = model.getCurrentValue();
							setValue( value );
						}
					}

					private void addToLayout( final JComponent comp1, final JComponent comp2 )
					{
						c.anchor = GridBagConstraints.LINE_END;
						add( comp1, c );
						c.gridx++;
						c.weightx = 1.0;
						c.anchor = GridBagConstraints.LINE_START;
						add( comp2, c );
						c.gridx = 0;
						c.weightx = 0.0;
						c.gridy++;
					}
				} ) );
	}

	private static JPasswordField linkedPasswordField( final StringElement element )
	{
		return ( JPasswordField ) linkedTextField( element, true );
	}

	private static JTextField linkedTextField( final StringElement element )
	{
		return linkedTextField( element, false );
	}

	private static JTextField linkedTextField( final StringElement element, final boolean isPassword )
	{
		final JTextField textField = isPassword ? new JPasswordField( element.get() ) : new JTextField( element.get() );
		final DocumentListener documentListener = new DocumentListener()
		{

			@Override
			public void removeUpdate( DocumentEvent e )
			{
				element.set( textField.getText() );
			}

			@Override
			public void insertUpdate( DocumentEvent e )
			{
				element.set( textField.getText() );
			}

			@Override
			public void changedUpdate( DocumentEvent e )
			{
				element.set( textField.getText() );
			}
		};
		textField.getDocument().addDocumentListener( documentListener );
		element.onSet( s -> {
			if ( s != null && !s.equals( textField.getText() ) )
			{
				textField.getDocument().removeDocumentListener( documentListener );
				try
				{
					textField.setText( s );
				}
				finally
				{
					textField.getDocument().addDocumentListener( documentListener );
				}
			}

		} );
		return textField;
	}

	protected abstract List< StyleElement > styleElements( final S style );
}

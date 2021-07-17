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
package org.elephant.swing;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.util.BoundedValueDouble;

/**
 * Extra modules for {@link JSpinner}.
 * 
 * @author Ko Sugawara
 */
public class DoubleSpinner extends JSpinner implements BoundedValueDouble.UpdateListener
{
	private static final long serialVersionUID = -2622218113093499557L;

	public static final int DEFAULT_TFCOLS = 6;

	private final BoundedValueDouble model;

	public DoubleSpinner( final BoundedValueDouble model, final double stepSize )
	{
		this( model, stepSize, DEFAULT_TFCOLS );
	}

	public DoubleSpinner( final BoundedValueDouble model, final double stepSize, final int tfCols )
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

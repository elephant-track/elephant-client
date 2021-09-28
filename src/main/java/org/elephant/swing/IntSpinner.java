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

import bdv.util.BoundedValue;

/**
 * Extra modules for {@link JSpinner}.
 * 
 * @author Ko Sugawara
 */
public class IntSpinner extends JSpinner implements BoundedValue.UpdateListener
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

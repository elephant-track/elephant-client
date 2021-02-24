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

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.mastodon.app.ui.settings.StyleElements.StyleElement;
import org.mastodon.app.ui.settings.StyleElements.StyleElementVisitor;

import bdv.util.BoundedValueDouble;

/**
 * Extra modules for {@link StringElement}.
 * 
 * @author Ko Sugawara
 */
public class StyleElementsEx
{

	public static StringElement stringElement( final String label, final Supplier< String > get, final Consumer< String > set )
	{
		return new StringElement( label )
		{
			@Override
			public String get()
			{
				return get.get();
			}

			@Override
			public void set( final String file )
			{
				set.accept( file );
			}
		};
	}

	public static abstract class StringElement implements StyleElement
	{
		private final String label;

		private final ArrayList< Consumer< String > > onSet = new ArrayList<>();

		public StringElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			( ( StyleElementVisitorEx ) visitor ).visit( this );
		}

		public void onSet( final Consumer< String > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( get() ) );
		}

		public abstract String get();

		public abstract void set( String b );
	}

	public static PasswordElement passwordElement( final String label, final Supplier< String > get, final Consumer< String > set )
	{
		return new PasswordElement( label )
		{
			@Override
			public String get()
			{
				return get.get();
			}

			@Override
			public void set( final String file )
			{
				set.accept( file );
			}
		};
	}

	public static abstract class PasswordElement extends StringElement
	{

		public PasswordElement( String label )
		{
			super( label );
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			( ( StyleElementVisitorEx ) visitor ).visit( this );
		}
	}

	public static DoubleElementEx doubleElementEx( final String label, final double rangeMin, final double rangeMax, final double stepSize, final DoubleSupplier get, final Consumer< Double > set )
	{
		return new DoubleElementEx( label, rangeMin, rangeMax, stepSize )
		{
			@Override
			public double get()
			{
				return get.getAsDouble();
			}

			@Override
			public void set( final double v )
			{
				set.accept( v );
			}
		};
	}

	public static abstract class DoubleElementEx implements StyleElement
	{

		private final double stepSize;

		private final BoundedValueDouble value;

		private final String label;

		public DoubleElementEx( final String label, final double rangeMin, final double rangeMax, final double stepSize )
		{
			final double currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
			value = new BoundedValueDouble( rangeMin, rangeMax, currentValue )
			{
				@Override
				public void setCurrentValue( final double value )
				{
					super.setCurrentValue( value );
					if ( get() != getCurrentValue() )
						set( getCurrentValue() );
				}
			};
			this.label = label;
			this.stepSize = stepSize;
		}

		public BoundedValueDouble getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			( ( StyleElementVisitorEx ) visitor ).visit( this );
		}

		public abstract double get();

		public abstract void set( double v );

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}

		public double getStepSize()
		{
			return stepSize;
		}

	}

	public interface StyleElementEx
	{
		public default void update()
		{}

		public void accept( StyleElementVisitorEx visitor );
	}

	public interface StyleElementVisitorEx extends StyleElementVisitor
	{
		public default void visit( final StringElement element )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final PasswordElement element )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final DoubleElementEx element )
		{
			throw new UnsupportedOperationException();
		}
	}

}

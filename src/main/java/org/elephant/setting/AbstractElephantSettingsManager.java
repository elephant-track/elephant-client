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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import bdv.ui.settings.style.AbstractStyleManager;

public abstract class AbstractElephantSettingsManager< M extends AbstractElephantSettingsManager< M, S >, S extends AbstractElephantSettings< S > > extends AbstractStyleManager< M, S >
{

	private final S forwardDefaultStyle;

	private final SettingsUpdateListener updateForwardDefaultListener;

	protected AbstractElephantSettingsManager( final boolean loadStyles )
	{
		forwardDefaultStyle = getDefaultStyleCopy();
		updateForwardDefaultListener = () -> forwardDefaultStyle.set( selectedStyle );
		selectedStyle.updateListeners().add( updateForwardDefaultListener );
		if ( loadStyles )
			loadStyles();
	}

	public abstract M getStaticInstance();

	public abstract M getStaticInstanceNoBuiltInStyles();

	protected abstract Tag getTag();

	protected abstract String getStyleFile();

	protected abstract S getDefaultStyleCopy();

	protected abstract Collection< S > getDefaults();

	@Override
	protected List< S > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( getDefaults() ) );
	}

	@Override
	public synchronized void setSelectedStyle( final S settings )
	{
		selectedStyle.updateListeners().remove( updateForwardDefaultListener );
		selectedStyle = settings;
		forwardDefaultStyle.set( selectedStyle );
		selectedStyle.updateListeners().add( updateForwardDefaultListener );
	}

	public S getForwardDefaultStyle()
	{
		return forwardDefaultStyle;
	}

	public void loadStyles()
	{
		loadStyles( getStyleFile() );
	}

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( S::getName ).collect( Collectors.toSet() );
		try
		{
			final FileReader input = new FileReader( filename );
			final Yaml yaml = ElephantSettingsIO.createYaml( getTag() );
			final Iterable< Object > objs = yaml.loadAll( input );
			String defaultStyleName = null;
			for ( final Object obj : objs )
			{
				if ( obj instanceof String )
				{
					defaultStyleName = ( String ) obj;
					System.out.println( String.format( "%s.loadStyles", forwardDefaultStyle.getClass().getSimpleName() ) );
					System.out.println( defaultStyleName );
				}
				else if ( forwardDefaultStyle.getClass().isInstance( obj ) )
				{
					@SuppressWarnings( "unchecked" )
					final S ts = ( S ) obj;
					if ( null != ts )
					{
						// sanity check: style names must be unique
						if ( names.add( ts.getName() ) )
							userStyles.add( ts );
						else
							System.out.println( "Discarded style with duplicate name \"" + ts.getName() + "\"." );
					}
				}
			}
			setSelectedStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "ELEPHANT style file " + filename + " not found. Using builtin styles." );
		}
	}

	@Override
	public void saveStyles()
	{
		saveStyles( getStyleFile() );
	}

	public void saveStyles( final String filename )
	{
		try
		{
			mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = ElephantSettingsIO.createYaml( getTag() );
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( selectedStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/*
	 * STATIC UTILITIES
	 */

	private static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}
}

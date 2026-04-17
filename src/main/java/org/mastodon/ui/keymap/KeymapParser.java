/*-
 * #%L
 * elephant
 * %%
 * Copyright (C) 2019 - 2026 Ko Sugawara
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.ui.keymap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class KeymapParser
{
	private static final String KEYMAPS_PATH = System.getProperty( "user.home" ) + "/.mastodon/keymaps/keymaps.yaml";

	private static final String INITIAL_DEFAULT_KEYMAP_NAME = "Default";

	private KeymapsListIO keymapsList;

	public KeymapParser()
	{
		keymapsList = new KeymapsListIO( INITIAL_DEFAULT_KEYMAP_NAME, Collections.emptyList() );
		try ( final FileReader input = new FileReader( KEYMAPS_PATH ) )
		{
			keymapsList = createYaml().loadAs( input, KeymapsListIO.class );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "Keymap list file " + KEYMAPS_PATH + " not found. Using builtin styles." );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public KeymapParser addKeymap( final String keymapName )
	{
		if ( !keymapsList.keymapNameToFileName.containsKey( keymapName ) )
		{
			keymapsList.keymapNameToFileName.put( keymapName, keymapName + ".yaml" );
		}
		return this;
	}

	public void dump()
	{
		new File( KEYMAPS_PATH ).getParentFile().mkdirs();
		try ( final FileWriter output = new FileWriter( KEYMAPS_PATH ) )
		{
			createYaml().dump( keymapsList, output );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
		final Representer representer = new Representer( dumperOptions );
		representer.addClassTag( KeymapsListIO.class, new Tag( "!keymapslist" ) );
		final Constructor constructor = new Constructor( new org.yaml.snakeyaml.LoaderOptions() );
		constructor.addTypeDescription( new TypeDescription( KeymapsListIO.class, "!keymapslist" ) );
		return new Yaml( constructor, representer, dumperOptions );
	}

}

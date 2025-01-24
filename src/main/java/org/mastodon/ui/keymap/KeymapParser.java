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

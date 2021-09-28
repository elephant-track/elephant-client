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
package org.elephant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.mastodon.ui.keymap.KeymapParser;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Helper class to inject the Elephant keymap.
 * 
 * @author Ko Sugawara
 */
@Plugin( type = Service.class )
public class ElephantKeyMapService extends AbstractService
{
	@Parameter
	private PluginService pluginService;

	private static final String KEYMAPS_PATH = System.getProperty( "user.home" ) + "/.mastodon/keymaps/";

	private static final String ELEPHANT_KEYMAP_FILENAME = "Elephant.yaml";

	private static final String ELEPHANT_KEYMAP_NAME = "Elephant";

	public ElephantKeyMapService()
	{
		final File elephantKeymapFile = new File( KEYMAPS_PATH + ELEPHANT_KEYMAP_FILENAME );
		if ( !elephantKeymapFile.exists() )
		{
			try (final InputStream in = ElephantKeyMapService.class.getResourceAsStream( "/org/elephant/" + ELEPHANT_KEYMAP_FILENAME ))
			{
				Files.copy( in, elephantKeymapFile.toPath() );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}
		new KeymapParser().addKeymap( ELEPHANT_KEYMAP_NAME ).dump();
	}

}

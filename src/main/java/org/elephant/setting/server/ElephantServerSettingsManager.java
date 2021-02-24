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
package org.elephant.setting.server;

import java.util.Collection;

import org.elephant.setting.AbstractElephantSettingsManager;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Settings manager for server settings.
 * 
 * @author Ko Sugawara
 */
public class ElephantServerSettingsManager extends AbstractElephantSettingsManager< ElephantServerSettingsManager, ElephantServerSettings >
{

	public static ElephantServerSettingsManager getInstance()
	{
		return INSTANCE;
	}

	public static ElephantServerSettingsManager getInstanceNoBuiltInStyles()
	{
		return INSTANCE_NO_BUILT_IN_STYLES;
	}

	// STYLE_FILE and ELEPHANT_MAIN_SETTINGS_TAG should be declared before INSTANCE
	// and INSTANCE_NO_BUILT_IN_STYLES

	private static final Tag ELEPHANT_SERVER_SETTINGS_TAG = new Tag( "!elephantserversettings" );

	private static final String STYLE_FILE = System.getProperty( "user.home" ).replace("\\", "/") + "/.mastodon/Plugins/Elephant/elephant_server_settings.yaml";

	private static final ElephantServerSettingsManager INSTANCE = new ElephantServerSettingsManager();

	private static final ElephantServerSettingsManager INSTANCE_NO_BUILT_IN_STYLES = new ElephantServerSettingsManager( false );

	private ElephantServerSettingsManager()
	{
		this( true );
	}

	private ElephantServerSettingsManager( boolean loadStyles )
	{
		super( loadStyles );
	}

	@Override
	public ElephantServerSettingsManager getStaticInstance()
	{
		return INSTANCE;
	}

	@Override
	public ElephantServerSettingsManager getStaticInstanceNoBuiltInStyles()
	{
		return INSTANCE_NO_BUILT_IN_STYLES;
	}

	@Override
	protected Tag getTag()
	{
		return ELEPHANT_SERVER_SETTINGS_TAG;
	}

	@Override
	protected String getStyleFile()
	{
		return STYLE_FILE;
	}

	@Override
	protected ElephantServerSettings getDefaultStyleCopy()
	{
		return ElephantServerSettings.defaultStyle().copy();
	}

	@Override
	protected Collection< ElephantServerSettings > getDefaults()
	{
		return ElephantServerSettings.defaults;
	}
}

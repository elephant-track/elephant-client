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
package org.elephant.setting.main;

import java.util.Collection;

import org.elephant.setting.AbstractElephantSettingsManager;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Settings manager for main settings.
 * 
 * @author Ko Sugawara
 */
public class ElephantMainSettingsManager extends AbstractElephantSettingsManager< ElephantMainSettingsManager, ElephantMainSettings >
{

	public static ElephantMainSettingsManager getInstance()
	{
		return INSTANCE;
	}

	public static ElephantMainSettingsManager getInstanceNoBuiltInStyles()
	{
		return INSTANCE_NO_BUILT_IN_STYLES;
	}

	// STYLE_FILE and ELEPHANT_MAIN_SETTINGS_TAG should be declared before INSTANCE
	// and INSTANCE_NO_BUILT_IN_STYLES

	private static final String STYLE_FILE =
			System.getProperty( "user.home" ).replace( "\\", "/" ) + "/.mastodon/Plugins/Elephant/elephant_main_settings.yaml";

	private static final Tag ELEPHANT_MAIN_SETTINGS_TAG = new Tag( "!elephantmainsettings" );

	private static final ElephantMainSettingsManager INSTANCE = new ElephantMainSettingsManager();

	private static final ElephantMainSettingsManager INSTANCE_NO_BUILT_IN_STYLES = new ElephantMainSettingsManager( false );

	public ElephantMainSettingsManager()
	{
		this( true );
	}

	public ElephantMainSettingsManager( final boolean loadStyles )
	{
		super( loadStyles );
	}

	@Override
	public ElephantMainSettingsManager getStaticInstance()
	{
		return INSTANCE;
	}

	@Override
	public ElephantMainSettingsManager getStaticInstanceNoBuiltInStyles()
	{
		return INSTANCE_NO_BUILT_IN_STYLES;
	}

	@Override
	protected Tag getTag()
	{
		return ELEPHANT_MAIN_SETTINGS_TAG;
	}

	@Override
	protected TagInspector getTagInspector()
	{
		TagInspector taginspector = tag -> tag.getClassName().equals( ElephantMainSettings.class.getName() );
		return taginspector;
	}

	@Override
	protected String getStyleFile()
	{
		return STYLE_FILE;
	}

	@Override
	protected ElephantMainSettings getDefaultStyleCopy()
	{
		return ElephantMainSettings.defaultStyle().copy();
	}

	@Override
	protected Collection< ElephantMainSettings > getDefaults()
	{
		return ElephantMainSettings.defaults;
	}

}

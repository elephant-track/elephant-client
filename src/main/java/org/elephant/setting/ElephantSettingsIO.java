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

import java.util.LinkedHashMap;
import java.util.Map;

import org.elephant.setting.main.ElephantMainSettings;
import org.mastodon.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.io.yaml.WorkaroundConstructor;
import org.mastodon.io.yaml.WorkaroundRepresent;
import org.mastodon.io.yaml.WorkaroundRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class ElephantSettingsIO
{
	private static class RenderSettingsRepresenter extends WorkaroundRepresenter
	{
		public RenderSettingsRepresenter( final Tag tag )
		{
			putRepresent( new RepresentRenderSettings( this, tag ) );
		}
	}

	private static class RenderSettingsConstructor extends WorkaroundConstructor
	{
		public RenderSettingsConstructor( final Tag tag )
		{
			super( Object.class );
			putConstruct( new ConstructRenderSettings( this, tag ) );
		}
	}

	public static Yaml createYaml( final Tag tag )
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new RenderSettingsRepresenter( tag );
		final Constructor constructor = new RenderSettingsConstructor( tag );
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	private static class RepresentRenderSettings extends WorkaroundRepresent
	{
		public RepresentRenderSettings( final WorkaroundRepresenter r, final Tag tag )
		{
			super( r, tag, RepresentRenderSettings.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final ElephantMainSettings s = ( ElephantMainSettings ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			mapping.put( "name", s.getName() );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructRenderSettings extends AbstractWorkaroundConstruct
	{
		public ConstructRenderSettings( final WorkaroundConstructor c, final Tag tag )
		{
			super( c, tag );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = ( String ) mapping.get( "name" );
				final ElephantMainSettings s = ElephantMainSettings.defaultStyle().copy( name );

				s.setName( ( String ) mapping.get( "name" ) );

				return s;
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}

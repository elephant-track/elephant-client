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

import java.util.Objects;

import org.mastodon.util.Listeners;

public abstract class AbstractElephantSettings< S extends AbstractElephantSettings< S > > implements UpdatableStyle< S >
{

	private final Listeners.List< SettingsUpdateListener > updateListeners;

	protected AbstractElephantSettings()
	{
		updateListeners = new Listeners.SynchronizedList<>();
	}

	protected void notifyListeners()
	{
		for ( final SettingsUpdateListener l : updateListeners.list )
			l.settingsUpdated();
	}

	@Override
	public Listeners< SettingsUpdateListener > updateListeners()
	{
		return updateListeners;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public S copy( String name )
	{
		final S rs = getNewInstance();
		rs.set( ( S ) this );
		if ( name != null )
			rs.setName( name );
		return rs;
	}

	protected abstract S getNewInstance();

	public synchronized void set( final S settings )
	{
		name = settings.name;
	}

	protected String name;

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName( String name )
	{
		if ( !Objects.equals( this.name, name ) )
		{
			this.name = name;
			notifyListeners();
		}
	}
}

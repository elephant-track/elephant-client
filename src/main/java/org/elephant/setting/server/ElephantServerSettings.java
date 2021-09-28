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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.elephant.setting.AbstractElephantSettings;

/**
 * Server setting values.
 * 
 * @author Ko Sugawara
 */
public class ElephantServerSettings extends AbstractElephantSettings< ElephantServerSettings >
{

	public static final String DEFAULT_SERVER_URL = "http://localhost:8080";

	public static final String DEFAULT_RABBITMQ_HOST = "localhost";

	public static final int DEFAULT_RABBITMQ_PORT = 5672;

	public static final String DEFAULT_RABBITMQ_USERNAME = "user";

	public static final String DEFAULT_RABBITMQ_PASSWORD = "user";

	@Override
	public ElephantServerSettings copy( String name )
	{
		final ElephantServerSettings rs = new ElephantServerSettings();
		rs.set( this );
		if ( name != null )
			rs.setName( name );
		return rs;
	}

	@Override
	protected ElephantServerSettings getNewInstance()
	{
		return new ElephantServerSettings();
	}

	@Override
	public synchronized void set( final ElephantServerSettings settings )
	{
		name = settings.name;
		serverURL = settings.serverURL;
		rabbitMQHost = settings.rabbitMQHost;
		rabbitMQPort = settings.rabbitMQPort;
		rabbitMQUsername = settings.rabbitMQUsername;
		rabbitMQPassword = settings.rabbitMQPassword;
		notifyListeners();
	}

	private String serverURL;

	private String rabbitMQHost;

	private int rabbitMQPort;

	private String rabbitMQUsername;

	private String rabbitMQPassword;

	public String getServerURL()
	{
		return serverURL != null ? serverURL : DEFAULT_SERVER_URL;
	}

	public synchronized void setServerURL( final String serverURL )
	{
		if ( !Objects.equals( this.serverURL, serverURL ) )
		{
			this.serverURL = serverURL;
			notifyListeners();
		}
	}

	public String getRabbitMQHost()
	{
		return rabbitMQHost != null ? rabbitMQHost : DEFAULT_RABBITMQ_HOST;
	}

	public synchronized void setRabbitMQHost( final String rabbitMQHost )
	{
		if ( !Objects.equals( this.rabbitMQHost, rabbitMQHost ) )
		{
			this.rabbitMQHost = rabbitMQHost;
			notifyListeners();
		}
	}

	public int getRabbitMQPort()
	{
		return rabbitMQPort;
	}

	public synchronized void setRabbitMQPort( int rabbitMQPort )
	{
		if ( this.rabbitMQPort != rabbitMQPort )
		{
			this.rabbitMQPort = rabbitMQPort;
			notifyListeners();
		}
	}

	public String getRabbitMQUsername()
	{
		return rabbitMQUsername != null ? rabbitMQUsername : DEFAULT_RABBITMQ_USERNAME;
	}

	public synchronized void setRabbitMQUsername( final String rabbitMQUsername )
	{
		if ( !Objects.equals( this.rabbitMQUsername, rabbitMQUsername ) )
		{
			this.rabbitMQUsername = rabbitMQUsername;
			notifyListeners();
		}
	}

	public String getRabbitMQPassword()
	{
		return rabbitMQPassword != null ? rabbitMQPassword : DEFAULT_RABBITMQ_PASSWORD;
	}

	public synchronized void setRabbitMQPassword( final String rabbitMQPassword )
	{
		if ( !Objects.equals( this.rabbitMQPassword, rabbitMQPassword ) )
		{
			this.rabbitMQPassword = rabbitMQPassword;
			notifyListeners();
		}
	}

	private static final ElephantServerSettings df;
	static
	{
		df = new ElephantServerSettings();
		df.serverURL = DEFAULT_SERVER_URL;
		df.rabbitMQHost = DEFAULT_RABBITMQ_HOST;
		df.rabbitMQPort = DEFAULT_RABBITMQ_PORT;
		df.rabbitMQUsername = DEFAULT_RABBITMQ_USERNAME;
		df.rabbitMQPassword = DEFAULT_RABBITMQ_PASSWORD;
		df.name = "Default";
	}

	public static final Collection< ElephantServerSettings > defaults;
	static
	{
		defaults = new ArrayList<>( 2 );
		defaults.add( df );
	}

	public static ElephantServerSettings defaultStyle()
	{
		return df;
	}
}

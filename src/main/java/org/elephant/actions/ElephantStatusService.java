package org.elephant.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.URLMixin;
import org.scijava.listeners.Listeners;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class ElephantStatusService extends AbstractElephantService
		implements ElephantSettingsMixin, ElephantConstantsMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private final Listeners.List< ElephantServerStatusListener > elephantServerStatusListeners;

	private final Listeners.List< ElephantGpuStatusListener > elephantGpuStatusListeners;

	public ElephantStatusService()
	{
		elephantServerStatusListeners = new Listeners.SynchronizedList<>();
		elephantGpuStatusListeners = new Listeners.SynchronizedList<>();
	}

	public void start()
	{
		new Thread( () -> {
			while ( true )
			{
				try
				{
					final URL serverUrl = new URL( getServerSettings().getServerURL() );
					final boolean is_available = checkAvailability( serverUrl.getHost(), serverUrl.getPort() );
					final List< GPU > gpus = new ArrayList<>();
					if ( is_available )
					{
						try
						{

							final HttpResponse< String > response = Unirest.get( getEndpointURL( ENDPOINT_GPUS ) ).asString();
							if ( response.getStatus() == HttpURLConnection.HTTP_OK )
							{
								final String body = response.getBody();
								final JsonArray jsonGpus = Json.parse( body ).asArray();
								for ( final JsonValue jsonValue : jsonGpus )
								{
									final JsonObject jsonGpu = jsonValue.asObject();
									final String id = jsonGpu.get( "id" ).asString();
									final String name = jsonGpu.get( "name" ).asString();
									final float totalMemory = jsonGpu.get( "mem_total" ).asFloat();
									final float usedMemory = jsonGpu.get( "mem_used" ).asFloat();
									gpus.add( new GPU( id, name, totalMemory, usedMemory ) );
								}
							}
							else
							{
								// Ignore 502 Bad Gateway
								if ( response.getStatus() != HttpURLConnection.HTTP_BAD_GATEWAY )
								{
									final StringBuilder sb = new StringBuilder( String.valueOf( response.getStatus() ) );
									sb.append( " " );
									sb.append( response.getStatusText() );
									if ( response.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR )
									{
										sb.append( ": " );
										sb.append( Json.parse( response.getBody() ).asObject().get( "error" ).asString() );
									}
									getLogger().severe( sb.toString() );
								}
							}
						}
						catch ( final UnirestException e )
						{
							// Dismiss this error
						}
					}
					elephantServerStatusListeners.list.forEach( l -> l.statusUpdated(
							is_available ? ElephantStatus.AVAILABLE : ElephantStatus.UNAVAILABLE,
							serverUrl.toString() ) );
					elephantGpuStatusListeners.list.forEach( l -> l.statusUpdated( gpus ) );

					Thread.sleep( 1000 );
				}
				catch ( final MalformedURLException e )
				{
					getLogger().severe( ExceptionUtils.getStackTrace( e ) );
				}
				catch ( final InterruptedException e )
				{
					getLogger().severe( ExceptionUtils.getStackTrace( e ) );
				}
			}
		} ).start();
	}

	public static boolean checkAvailability( final String host, final int port )
	{
		try (Socket s = new Socket( host, port ))
		{
			return true;
		}
		catch ( final IOException ex )
		{
			/* ignore */
		}
		return false;
	}

	public enum ElephantStatus
	{
		UNAVAILABLE,
		AVAILABLE,
		PROCESSING
	}

	public Listeners< ElephantServerStatusListener > elephantServerStatusListeners()
	{
		return elephantServerStatusListeners;
	}

	public Listeners< ElephantGpuStatusListener > elephantGpuStatusListeners()
	{
		return elephantGpuStatusListeners;
	}

}

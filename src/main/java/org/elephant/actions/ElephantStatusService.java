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
import org.elephant.actions.mixins.ElephantStateManagerMixin;
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
		implements ElephantConstantsMixin, ElephantSettingsMixin, ElephantStateManagerMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private final Listeners.List< ElephantServerStatusListener > elephantServerStatusListeners;

	public ElephantStatusService()
	{
		elephantServerStatusListeners = new Listeners.SynchronizedList<>();
	}

	public void start()
	{
		new Thread( () -> {
			while ( true )
			{
				ElephantStatus serverStatus = ElephantStatus.UNAVAILABLE;
				final List< GPU > gpus = new ArrayList<>();
				try
				{
					if ( isAvailable( getServerSettings().getServerURL() ) )
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
								serverStatus = ElephantStatus.AVAILABLE;
								getServerStateManager().setElephantServerErrorMessage( ElephantServerStateManager.NO_ERROR_MESSAGE );
							}
							else
							{
								if ( response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND )
								{
									serverStatus = ElephantStatus.WARNING;
									getServerStateManager().setElephantServerErrorMessage( ElephantServerStateManager.OUTDATED_MESSAGE );
								}
								else
								{
									final StringBuilder sb = new StringBuilder( String.valueOf( response.getStatus() ) );
									sb.append( " " );
									sb.append( response.getStatusText() );
									if ( response.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR )
									{
										sb.append( ": " );
										sb.append( Json.parse( response.getBody() ).asObject().get( "error" ).asString() );
										getLogger().severe( sb.toString() ); // Do not log other errors
									}
									getServerStateManager().setElephantServerErrorMessage( sb.toString() );
								}
							}
						}
						catch ( final UnirestException e )
						{
							getServerStateManager().setElephantServerErrorMessage( e.getMessage() );
						}
					}
					else
					{
						getServerStateManager().setElephantServerErrorMessage( ElephantServerStateManager.SERVER_NOT_FOUND_MESSAGE );
					}
				}
				catch ( final MalformedURLException e )
				{
					getServerStateManager().setElephantServerErrorMessage( e.getMessage() );
				}
				getServerStateManager().setElephantServerStatus( serverStatus );
				getServerStateManager().setGpus( gpus );
				elephantServerStatusListeners.list.forEach( l -> l.serverStatusUpdated() );
				try
				{
					Thread.sleep( 1000 );
				}
				catch ( final InterruptedException e )
				{
					getLogger().severe( ExceptionUtils.getStackTrace( e ) );
				}
			}
		} ).start();
	}

	public static boolean isAvailable( final String serverUrlString ) throws MalformedURLException
	{
		final URL serverUrl = new URL( serverUrlString );
		try (final Socket s = new Socket( serverUrl.getHost(), serverUrl.getPort() ))
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
		WARNING
	}

	public Listeners< ElephantServerStatusListener > elephantServerStatusListeners()
	{
		return elephantServerStatusListeners;
	}

}

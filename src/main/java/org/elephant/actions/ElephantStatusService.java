package org.elephant.actions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.scijava.listeners.Listeners;

public class ElephantStatusService extends AbstractElephantService implements ElephantSettingsMixin
{

	private static final long serialVersionUID = 1L;

	private final Listeners.List< ElephantStatusListener > elephantServerStatusListeners;

	public ElephantStatusService()
	{
		elephantServerStatusListeners = new Listeners.SynchronizedList<>();
	}

	public void start()
	{
		new Thread( () -> {
			while ( true )
			{
				try
				{
					final URL serverUrl = new URL( getServerSettings().getServerURL() );
					elephantServerStatusListeners.list.forEach( l -> l.statusUpdated(
							checkAvailability( serverUrl.getHost(), serverUrl.getPort() ) ? ElephantStatus.AVAILABLE : ElephantStatus.UNAVAILABLE,
							serverUrl.toString() ) );
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

	public Listeners< ElephantStatusListener > elephantServerStatusListeners()
	{
		return elephantServerStatusListeners;
	}

}

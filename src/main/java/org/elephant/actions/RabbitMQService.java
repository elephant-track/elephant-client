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
package org.elephant.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.scijava.listeners.Listeners;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;

/**
 * Set up an environment for RabbitMQ.
 * 
 * @author Ko Sugawara
 */
public class RabbitMQService extends AbstractElephantService implements LivemodeListener, ElephantSettingsMixin
{

	private static final long serialVersionUID = 1L;

	private static final String RABBITMQ_QUEUE_NAME = "update";

	private Connection connection;

	private List< Runnable > callbackListSucceeded = Collections.emptyList();

	private List< Runnable > callbackListFailed = Collections.emptyList();

	private final Listeners.List< ElephantStatusListener > rabbitMQStatusListeners;

	public RabbitMQService( final MamutPluginAppModel pluginAppModel )
	{
		super();
		final List< Runnable > callbackListSucceeded = new ArrayList<>();
		callbackListSucceeded.add( () -> {
			final TextOverlayAnimator overlayAnimator = new TextOverlayAnimator( "Model updated", 3000, TextPosition.CENTER );
			pluginAppModel.getWindowManager().forEachBdvView( bdv -> ( ( ViewerFrameMamut ) bdv.getFrame() ).getViewerPanel().addOverlayAnimator( overlayAnimator ) );
		} );
		setCallbackListSucceeded( callbackListSucceeded );
		final List< Runnable > callbackListFailed = new ArrayList<>();
		callbackListFailed.add( () -> {
			final TextOverlayAnimator overlayAnimator = new TextOverlayAnimator( "The server is not responding", 3000, TextPosition.CENTER );
			pluginAppModel.getWindowManager().forEachBdvView( bdv -> ( ( ViewerFrameMamut ) bdv.getFrame() ).getViewerPanel().addOverlayAnimator( overlayAnimator ) );
		} );
		setCallbackListFailed( callbackListFailed );
		rabbitMQStatusListeners = new Listeners.SynchronizedList<>();
	}

	public void startStatusDaemon()
	{
		new Thread( () -> {
			while ( true )
			{
				final ConnectionFactory factory = new ConnectionFactory();
				factory.setUsername( getServerSettings().getRabbitMQUsername() );
				factory.setPassword( getServerSettings().getRabbitMQPassword() );
				factory.setHost( getServerSettings().getRabbitMQHost() );
				factory.setRequestedHeartbeat( 0 );
				boolean isAvailable = false;
				try (final Connection tempConnection = factory.newConnection())
				{
					isAvailable = true;
				}
				catch ( IOException | TimeoutException e )
				{
					// Do nothing
				}
				final ElephantStatus status = isAvailable ? ElephantStatus.AVAILABLE : ElephantStatus.UNAVAILABLE;
				final String url = "amqp://" + getServerSettings().getRabbitMQHost() + ":5672";
				rabbitMQStatusListeners.list.forEach( l -> l.statusUpdated( status, url ) );
			}
		} ).start();
	}

	private void setCallbackListSucceeded( final List< Runnable > callbackListSucceeded )
	{
		this.callbackListSucceeded = callbackListSucceeded;
	}

	private void setCallbackListFailed( final List< Runnable > callbackListFailed )
	{
		this.callbackListFailed = callbackListFailed;
	}

	private void openConnection()
	{
		final ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername( getServerSettings().getRabbitMQUsername() );
		factory.setPassword( getServerSettings().getRabbitMQPassword() );
		factory.setHost( getServerSettings().getRabbitMQHost() );
		factory.setRequestedHeartbeat( 0 );
		try
		{
			connection = factory.newConnection();
			final Channel channel = connection.createChannel();
			channel.queueDeclare( RABBITMQ_QUEUE_NAME, false, false, false, null );
			final DeliverCallback deliverCallback = ( consumerTag, delivery ) -> {
				callbackListSucceeded.forEach( Runnable::run );
			};
			channel.basicConsume( RABBITMQ_QUEUE_NAME, true, deliverCallback, consumerTag -> {} );
		}
		catch ( IOException | TimeoutException e )
		{
			callbackListFailed.forEach( Runnable::run );
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	public void closeConnection()
	{
		if ( connection != null )
		{
			try
			{
				connection.close();
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
				connection = null;
			}
		}
	}

	@Override
	public void livemodeCahnged( boolean isLivemode )
	{
		if ( isLivemode )
		{
			openConnection();
		}
		else
		{
			closeConnection();
		}

	}

	public Listeners< ElephantStatusListener > rabbitMQStatusListeners()
	{
		return rabbitMQStatusListeners;
	}

}

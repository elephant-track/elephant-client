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
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.scijava.listeners.Listeners;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.TopologyRecoveryException;

import bdv.viewer.animate.TextOverlayAnimator.TextPosition;

/**
 * Set up an environment for RabbitMQ.
 * 
 * @author Ko Sugawara
 */
public class RabbitMQService extends AbstractElephantService implements ElephantSettingsMixin, WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String RABBITMQ_QUEUE_NAME = "update";

	private Connection connection;

	private final Listeners.List< ElephantStatusListener > rabbitMQStatusListeners;

	private final ExceptionHandler emptyExceptionHandler = new EmptyExceptionHander();

	public RabbitMQService()
	{
		super();
		rabbitMQStatusListeners = new Listeners.SynchronizedList<>();
	}

	public void start()
	{
		new Thread( () -> {
			while ( true )
			{
				final ConnectionFactory factory = new ConnectionFactory();
				factory.setUsername( getServerSettings().getRabbitMQUsername() );
				factory.setPassword( getServerSettings().getRabbitMQPassword() );
				factory.setHost( getServerSettings().getRabbitMQHost() );
				factory.setExceptionHandler( emptyExceptionHandler );
				boolean isAvailable = false;
				try (final Connection tempConnection = factory.newConnection())
				{
					isAvailable = true;
					if ( connection == null )
					{
						openConnection();
					}
				}
				catch ( IOException | TimeoutException e )
				{
					closeConnection();
				}
				final ElephantStatus status = isAvailable ? ElephantStatus.AVAILABLE : ElephantStatus.UNAVAILABLE;
				final String url = "amqp://" + factory.getHost() + ":" + factory.getPort();
				rabbitMQStatusListeners.list.forEach( l -> l.statusUpdated( status, url ) );
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

	private synchronized void openConnection()
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
				final String message = new String( delivery.getBody(), "UTF-8" );
				addTextOverlayAnimator( message, 3000, TextPosition.CENTER );
			};
			channel.basicConsume( RABBITMQ_QUEUE_NAME, true, deliverCallback, consumerTag -> {} );
		}
		catch ( IOException | TimeoutException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	private synchronized void closeConnection()
	{
		if ( connection != null )
		{
			try
			{
				connection.close();
			}
			catch ( IOException | AlreadyClosedException e )
			{
				// Do nothing
			}
			finally
			{
				connection = null;
			}
		}
	}

	public Listeners< ElephantStatusListener > rabbitMQStatusListeners()
	{
		return rabbitMQStatusListeners;
	}

	private class EmptyExceptionHander implements ExceptionHandler
	{

		@Override
		public void handleUnexpectedConnectionDriverException( Connection conn, Throwable exception )
		{}

		@Override
		public void handleReturnListenerException( Channel channel, Throwable exception )
		{}

		@Override
		public void handleConfirmListenerException( Channel channel, Throwable exception )
		{}

		@Override
		public void handleBlockedListenerException( Connection connection, Throwable exception )
		{}

		@Override
		public void handleConsumerException( Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName )
		{}

		@Override
		public void handleConnectionRecoveryException( Connection conn, Throwable exception )
		{}

		@Override
		public void handleChannelRecoveryException( Channel ch, Throwable exception )
		{}

		@Override
		public void handleTopologyRecoveryException( Connection conn, Channel ch, TopologyRecoveryException exception )
		{}

	}

}

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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.ElephantStateManagerMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.scijava.listeners.Listeners;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
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
public class RabbitMQService extends AbstractElephantService
		implements ElephantStateManagerMixin, WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String RABBITMQ_QUEUE_UPDATE = "update";

	private static final String RABBITMQ_QUEUE_DATASET = "dataset";

	private static final String RABBITMQ_QUEUE_LOG = "log";

	private Connection connection;

	private Channel channel;

	private final Listeners.List< RabbitMQStatusListener > rabbitMQStatusListeners;

	private final Listeners.List< RabbitMQDatasetListener > rabbitMQDatasetListeners;

	private final ExceptionHandler emptyExceptionHandler = new EmptyExceptionHander();

	public RabbitMQService()
	{
		super();
		rabbitMQStatusListeners = new Listeners.SynchronizedList<>();
		rabbitMQDatasetListeners = new Listeners.SynchronizedList<>();
	}

	private ConnectionFactory getBaseConnectionFactory() throws KeyManagementException, NoSuchAlgorithmException
	{
		final ConnectionFactory factory = new ConnectionFactory();
		if ( getServerSettings().getUseSslProtocol() )
		{
			SSLContext sslContext = getServerSettings().getVerifySSL() ? SSLContext.getDefault()
					: SSLUtils.getSSLContextWithoutCertificateValidation();
			factory.useSslProtocol( sslContext );
		}
		factory.setHost( getServerSettings().getRabbitMQHost() );
		factory.setPort( getServerSettings().getRabbitMQPort() );
		factory.setVirtualHost( getServerSettings().getRabbitMQVirtualHost() );
		factory.setUsername( getServerSettings().getRabbitMQUsername() );
		factory.setPassword( getServerSettings().getRabbitMQPassword() );
		factory.setRequestedHeartbeat( 0 );
		return factory;
	}

	public void start()
	{
		new Thread( () -> {
			while ( true )
			{
				boolean isAvailable = false;
				try
				{
					ConnectionFactory factory = getBaseConnectionFactory();
					factory.setExceptionHandler( emptyExceptionHandler );

					try ( final Connection tempConnection = factory.newConnection() )
					{
						if ( connection == null )
						{
							openConnection();
						}
						isAvailable = true;
						getServerStateManager().setRabbitMQErrorMessage( ElephantServerStateManager.NO_ERROR_MESSAGE );
					}
					catch ( IOException | TimeoutException e )
					{
						getServerStateManager().setRabbitMQErrorMessage( e.getMessage() );
						closeConnection();
					}
				}
				catch ( KeyManagementException | NoSuchAlgorithmException e )
				{
					getServerStateManager().setRabbitMQErrorMessage( e.getMessage() );
				}
				finally
				{
					final ElephantStatus rabbitMQStatus = isAvailable ? ElephantStatus.AVAILABLE : ElephantStatus.UNAVAILABLE;
					getServerStateManager().setRabbitMQStatus( rabbitMQStatus );
					rabbitMQStatusListeners.list.forEach( l -> l.rabbitMQStatusUpdated() );
					try
					{
						Thread.sleep( 1000 );
					}
					catch ( final InterruptedException e )
					{
						getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
					}
				}
			}
		} ).start();
	}

	private synchronized void openConnection() throws IOException, TimeoutException, KeyManagementException, NoSuchAlgorithmException
	{
		final ConnectionFactory factory = getBaseConnectionFactory();
		connection = factory.newConnection();
		channel = connection.createChannel();
		// RABBITMQ_QUEUE_UPDATE
		channel.queueDeclare( RABBITMQ_QUEUE_UPDATE, false, false, false, null );
		final DeliverCallback callbackUpdate = ( consumerTag, delivery ) -> {
			final String message = new String( delivery.getBody(), "UTF-8" );
			addTextOverlayAnimator( message, 3000, TextPosition.CENTER );
		};
		channel.basicConsume( RABBITMQ_QUEUE_UPDATE, true, callbackUpdate, consumerTag -> {} );
		// RABBITMQ_QUEUE_DATASET
		channel.queueDeclare( RABBITMQ_QUEUE_DATASET, false, false, false, null );
		final DeliverCallback callbackDataset = ( consumerTag, delivery ) -> {
			rabbitMQDatasetListeners.list.forEach( l -> l.messageDelivered( consumerTag, delivery ) );
		};
		channel.basicConsume( RABBITMQ_QUEUE_DATASET, true, callbackDataset, consumerTag -> {} );
		// RABBITMQ_QUEUE_LOG
		channel.queueDeclare( RABBITMQ_QUEUE_LOG, false, false, false, null );
		final DeliverCallback callbackLog = ( consumerTag, delivery ) -> {
			final String body = new String( delivery.getBody(), "UTF-8" );
			final JsonObject jsonObject = Json.parse( body ).asObject();
			final String level = jsonObject.get( "level" ).asString();
			final String message = jsonObject.get( "message" ).asString();
			if ( level.equals( "DEBUG" ) )
			{
				getServerLogger().fine( message );
			}
			else if ( level.equals( "INFO" ) )
			{
				getServerLogger().info( message );
			}
			else if ( level.equals( "WARNING" ) )
			{
				getServerLogger().warning( message );
			}
			else if ( level.equals( "ERROR" ) )
			{
				getServerLogger().severe( message );
			}
			else if ( level.equals( "CRITICAL" ) )
			{
				getServerLogger().severe( message );
			}
		};
		channel.basicConsume( RABBITMQ_QUEUE_LOG, true, callbackLog, consumerTag -> {} );
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

	public Listeners< RabbitMQStatusListener > rabbitMQStatusListeners()
	{
		return rabbitMQStatusListeners;
	}

	public Listeners< RabbitMQDatasetListener > rabbitMQDatasetListeners()
	{
		return rabbitMQDatasetListeners;
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
		public void handleConsumerException( Channel channel, Throwable exception, Consumer consumer, String consumerTag,
				String methodName )
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

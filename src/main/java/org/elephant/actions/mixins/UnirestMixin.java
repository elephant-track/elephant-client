/*******************************************************************************
 * Copyright (C) 2021, Ko Sugawara
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
package org.elephant.actions.mixins;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.JOptionPane;

import org.elephant.actions.ElephantStatusService.ElephantStatus;

import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.MultipartBody;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

/**
 * Wrapper for Unirest.
 * 
 * @author Ko Sugawara
 */
public interface UnirestMixin extends ElephantStateManagerMixin, LoggerMixin, UIActionMixin
{

	default void validateServerAvailability() throws ElephantConnectException
	{
		if ( getServerStateManager().getElephantServerStatus() == ElephantStatus.UNAVAILABLE )
		{
			getClientLogger().severe( "The ELEPHANT server is unavailable" );
			JOptionPane.showMessageDialog( null, "The ELEPHANT server is unavailable. Please set it up first.",
					"ELEPHANT server is unavailable", JOptionPane.ERROR_MESSAGE );
			throw new ElephantConnectException( "ELEPHANT server is unavailable" );
		}
	}

	default RequestBodyEntity postBaseJson( final String endpointUrl, final String body ) throws ElephantConnectException
	{
		validateServerAvailability();
		return Unirest.post( endpointUrl )
				.header( "Content-Type", "application/json" )
				.body( body );
	}

	default MultipartBody postBaseMultipartFormData( final String endpointUrl, final File file, final String data )
			throws ElephantConnectException
	{
		validateServerAvailability();
		return Unirest.post( endpointUrl )
				.field( "file", file )
				.field( "data", data );
	}

	default CompletableFuture< HttpResponse< String > > postMultipartFormDataAsStringAsync( final String endpointUrl, final File file,
			final String data,
			final Consumer< HttpResponse< String > > completed ) throws ElephantConnectException
	{
		return postMultipartFormDataAsStringAsync( endpointUrl, file, data, completed,
				e -> {
					if ( !( e.getCause() instanceof SSLHandshakeException ) )
					{
						handleError( e );
						getClientLogger().severe( "The request has failed" );
						showTextOverlayAnimator( e.getLocalizedMessage(), 3000, TextPosition.CENTER );
					}
				},
				() -> getClientLogger().info( "The request has been cancelled" ) );
	}

	default CompletableFuture< HttpResponse< String > > postMultipartFormDataAsStringAsync( final String endpointUrl, final File file,
			final String data,
			final Consumer< HttpResponse< String > > completed, final Consumer< UnirestException > failed, final Runnable cancelled )
			throws ElephantConnectException
	{
		return postBaseMultipartFormData( endpointUrl, file, data ).asStringAsync( new Callback< String >()
		{

			@Override
			public void failed( UnirestException e )
			{
				failed.accept( e );
			};

			@Override
			public void completed( HttpResponse< String > response )
			{
				completed.accept( response );
			}

			@Override
			public void cancelled()
			{
				cancelled.run();
			}

		} );
	}

	default CompletableFuture< HttpResponse< String > > postAsStringAsync( final String endpointUrl, final String body,
			final Consumer< HttpResponse< String > > completed ) throws ElephantConnectException
	{
		return postAsStringAsync( endpointUrl, body, completed,
				e -> {
					if ( !( e.getCause() instanceof SSLHandshakeException ) )
					{
						handleError( e );
						getClientLogger().severe( "The request has failed" );
						showTextOverlayAnimator( e.getLocalizedMessage(), 3000, TextPosition.CENTER );
					}
				},
				() -> getClientLogger().info( "The request has been cancelled" ) );
	}

	default CompletableFuture< HttpResponse< String > > postAsStringAsync( final String endpointUrl, final String body,
			final Consumer< HttpResponse< String > > completed, final Consumer< UnirestException > failed, final Runnable cancelled )
			throws ElephantConnectException
	{
		return postBaseJson( endpointUrl, body ).asStringAsync( new Callback< String >()
		{

			@Override
			public void failed( UnirestException e )
			{
				failed.accept( e );
			};

			@Override
			public void completed( HttpResponse< String > response )
			{
				completed.accept( response );
			}

			@Override
			public void cancelled()
			{
				cancelled.run();
			}

		} );
	}

	default HttpResponse< String > postAsString( final String endpointUrl, final String body ) throws ElephantConnectException
	{
		return postBaseJson( endpointUrl, body ).asString();
	}

	default CompletableFuture< HttpResponse< File > > postAsFileAsync( final String endpointUrl, final String body,
			final String path, final Consumer< HttpResponse< File > > completed ) throws ElephantConnectException
	{
		return postAsFileAsync( endpointUrl, body, path, completed,
				e -> {
					if ( !( e.getCause() instanceof SSLHandshakeException ) )
					{
						handleError( e );
						getClientLogger().severe( "The request has failed" );
						showTextOverlayAnimator( e.getLocalizedMessage(), 3000, TextPosition.CENTER );
					}
				},
				() -> getClientLogger().info( "The request has been cancelled" ) );
	}

	default CompletableFuture< HttpResponse< File > > postAsFileAsync( final String endpointUrl, final String body, final String path,
			final Consumer< HttpResponse< File > > completed, final Consumer< UnirestException > failed, final Runnable cancelled )
			throws ElephantConnectException
	{
		return postBaseJson( endpointUrl, body ).asFileAsync( path, new Callback< File >()
		{

			@Override
			public void failed( UnirestException e )
			{
				failed.accept( e );
			};

			@Override
			public void completed( HttpResponse< File > response )
			{
				completed.accept( response );
			}

			@Override
			public void cancelled()
			{
				cancelled.run();
			}

		} );
	}

}

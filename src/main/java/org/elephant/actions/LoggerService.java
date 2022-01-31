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
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.setting.main.ElephantMainSettingsListener;
import org.scijava.log.LogService;

/**
 * Set up a logger. A static {@link Logger} instance is used across the process.
 * 
 * @author Ko Sugawara
 */
public class LoggerService extends AbstractElephantService implements ElephantMainSettingsListener
{

	private static final long serialVersionUID = 1L;

	private static Logger clientLogger; // to avoid GC

	private static Logger serverLogger; // to avoid GC

	public void setup()
	{
		clientLogger = getClientLogger();
		serverLogger = getServerLogger();
		try (final InputStream is = getClass().getClassLoader().getResourceAsStream( "logging.properties" ))
		{
			LogManager.getLogManager().readConfiguration( is );
		}
		catch ( SecurityException | IOException e )
		{
			clientLogger.severe( ExceptionUtils.getStackTrace( e ) );
		}
		Runtime.getRuntime().addShutdownHook( new Thread( () -> {
			for ( final Handler handler : clientLogger.getHandlers() )
			{
				if ( handler instanceof FileHandler )
					( ( FileHandler ) handler ).close();
			}
			for ( final Handler handler : serverLogger.getHandlers() )
			{
				if ( handler instanceof FileHandler )
					( ( FileHandler ) handler ).close();
			}
		} ) );
		setUpLogging();
	}

	public void setupSciJavaHandler( LogService sciJavaLogService )
	{
		setUpSciJavaLogger( sciJavaLogService );
	}

	@Override
	public void mainSettingsUpdated()
	{
		setUpLogging();
	}

}

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
package org.elephant.actions.mixins;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.scijava.log.LogService;

/**
 * Provide {@link Logger}.
 * 
 * @author Ko Sugawara
 */
public interface LoggerMixin
{

	Logger getClientLogger();

	Logger getServerLogger();

	String getClientLogFileName();

	String getServerLogFileName();

	default void setUpLogger( final Logger logger, final String logFileName )
	{
		try
		{
			final Handler[] handlers = logger.getHandlers();
			for ( final Handler handler : handlers )
			{
				if ( handler instanceof FileHandler )
				{
					( ( FileHandler ) handler ).close();
					logger.removeHandler( handler );
				}
			}
			final FileHandler fileHandler = new FileHandler( logFileName, true );
			fileHandler.setFormatter( new SimpleFormatter() );
			logger.addHandler( fileHandler );
			// Create a ConsoleHandler when it is not exists
			boolean hasConsoleHandler = false;
			for ( final Handler handler : handlers )
			{
				if ( handler instanceof ConsoleHandler )
					hasConsoleHandler = true;
			}
			if ( !hasConsoleHandler )
			{
				logger.addHandler( new ConsoleHandler() );
			}
		}
		catch ( SecurityException | IOException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	default void setUpLogging()
	{
		setUpLogger( getClientLogger(), getClientLogFileName() );
		setUpLogger( getServerLogger(), getServerLogFileName() );
	}

	default void setUpSciJavaLogger( final LogService sciJavaLogService )
	{
		boolean hasClientHandler = false;
		for ( final Handler handler : getClientLogger().getHandlers() )
		{
			if ( handler instanceof SciJavaHandler )
				hasClientHandler = true;
		}
		if ( !hasClientHandler )
		{
			getClientLogger().addHandler( new ClientHandler( sciJavaLogService ) );
		}

		boolean hasServerHandler = false;
		for ( final Handler handler : getServerLogger().getHandlers() )
		{
			if ( handler instanceof SciJavaHandler )
				hasServerHandler = true;
		}
		if ( !hasServerHandler )
		{
			getServerLogger().addHandler( new ServerHandler( sciJavaLogService ) );
		}
	}

	default void showClientLogWindow()
	{
//		ClientHandler.getInstance().showLogWindow();
	}

	default void showServerLogWindow()
	{
//		ServerHandler.getInstance().showLogWindow();
	}

	default void handleError( Exception e )
	{
		getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
	}

	/**
	 * Modified from the following original code:
	 * http://www.java2s.com/Tutorial/Java/0480__Log/WindowHandlerdisplaylogmessageinawindowJFrame.htm
	 * © Demo Source and Support. All rights reserved.
	 */
	class LogWindow extends JFrame
	{
		private static final long serialVersionUID = 1L;

		private final ColorPane colorPane = new ColorPane();

		public LogWindow( final String title )
		{
			super( title );
			setSize( 400, 600 );
			colorPane.setEditable( false );
			add( new JScrollPane( colorPane ) );
			setVisible( true );
		}

		public void showMessage( final String msg, final Color color )
		{
			SwingUtilities.invokeLater( () -> {
				colorPane.setEditable( true );
				try
				{
					colorPane.append( color, msg );
					this.validate();
				}
				finally
				{
					colorPane.setEditable( false );
				}
			} );
		}
	}

	class ClientHandler extends SciJavaHandler
	{
		private static SciJavaHandler handler = null;

		public ClientHandler( final LogService sciJavaLogService )
		{
			super( sciJavaLogService, "ELEPHANT client" );
		}

		public static synchronized SciJavaHandler getInstance( final LogService sciJavaLogService )
		{
			if ( handler == null )
			{
				handler = new ClientHandler( sciJavaLogService );
			}
			return handler;
		}
	}

	class ServerHandler extends SciJavaHandler
	{
		private static SciJavaHandler handler = null;

		public ServerHandler( final LogService sciJavaLogService )
		{
			super( sciJavaLogService, "ELEPHANT Server" );
		}

		public static synchronized SciJavaHandler getInstance( final LogService sciJavaLogService )
		{
			if ( handler == null )
			{
				handler = new ServerHandler( sciJavaLogService );
			}
			return handler;
		}
	}

	abstract class SciJavaHandler extends Handler
	{

		private org.scijava.log.Logger sciJavaLogger = null;

		private SciJavaHandler( final LogService sciJavaLogService, final String name )
		{
			sciJavaLogger = sciJavaLogService.subLogger( name );
			final LogManager manager = LogManager.getLogManager();
			final String className = this.getClass().getName();
			final String level = manager.getProperty( className + ".level" );
			setLevel( level != null ? Level.parse( level ) : Level.INFO );
			setFormatter( new SimpleFormatter() );
		}

		@Override
		public void publish( LogRecord record )
		{
			String msg = getFormatter().format( record );
			final Level level = record.getLevel();
			if ( level == Level.SEVERE )
				sciJavaLogger.error( msg );
			else if ( level == Level.WARNING )
				sciJavaLogger.warn( msg );
			else if ( level == Level.INFO )
				sciJavaLogger.info( msg );
			else if ( level == Level.CONFIG )
				sciJavaLogger.debug( msg );
			else
				sciJavaLogger.trace( msg );
		}

		@Override
		public void flush()
		{}

		@Override
		public void close() throws SecurityException
		{}

	}

}

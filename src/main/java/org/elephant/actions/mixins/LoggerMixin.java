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
import java.util.logging.ErrorManager;
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

	default void setUpLogger( final Logger logger, final String logFileName, final JFrameHandler jFrameHandler )
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
			// Create a JFrameHandler when it is not exists
			boolean hasJFrameHandler = false;
			for ( final Handler handler : handlers )
			{
				if ( handler instanceof JFrameHandler )
					hasJFrameHandler = true;
			}
			if ( !hasJFrameHandler )
			{
				logger.addHandler( jFrameHandler );
			}
		}
		catch ( SecurityException | IOException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	default void setUpLogging()
	{
		setUpLogger( getClientLogger(), getClientLogFileName(), ClientHandler.getInstance() );
		setUpLogger( getServerLogger(), getServerLogFileName(), ServerHandler.getInstance() );
	}

	default void showClientLogWindow()
	{
		ClientHandler.getInstance().showLogWindow();
	}

	default void showServerLogWindow()
	{
		ServerHandler.getInstance().showLogWindow();
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

	class ClientHandler extends JFrameHandler
	{
		private static JFrameHandler handler = null;

		public ClientHandler()
		{
			super( "ELEPHANT Client Log Window" );
		}

		public static synchronized JFrameHandler getInstance()
		{
			if ( handler == null )
			{
				handler = new ClientHandler();
			}
			return handler;
		}
	}

	class ServerHandler extends JFrameHandler
	{
		private static JFrameHandler handler = null;

		public ServerHandler()
		{
			super( "ELEPHANT Server Log Window" );
		}

		public static synchronized JFrameHandler getInstance()
		{
			if ( handler == null )
			{
				handler = new ServerHandler();
			}
			return handler;
		}
	}

	abstract class JFrameHandler extends Handler
	{

		private LogWindow window = null;

		private JFrameHandler( final String title )
		{
			final LogManager manager = LogManager.getLogManager();
			final String className = this.getClass().getName();
			final String level = manager.getProperty( className + ".level" );
			setLevel( level != null ? Level.parse( level ) : Level.INFO );
			setFormatter( new SimpleFormatter() );
			if ( window == null )
				window = new LogWindow( title );
		}

		public void showLogWindow()
		{
			SwingUtilities.invokeLater( () -> window.setVisible( true ) );
		}

		@Override
		public void publish( LogRecord record )
		{
			if ( !isLoggable( record ) ) { return; }
			String msg;
			Color color = Color.BLACK;
			try
			{
				final Level level = record.getLevel();
				if ( level == Level.WARNING || level == Level.SEVERE )
				{
					color = Color.RED;
				}
				msg = getFormatter().format( record );
			}
			catch ( final Exception ex )
			{
				// We don't want to throw an exception here, but we
				// report the exception to any registered ErrorManager.
				reportError( null, ex, ErrorManager.FORMAT_FAILURE );
				return;
			}
			window.showMessage( msg, color );
		}

		@Override
		public void flush()
		{}

		@Override
		public void close() throws SecurityException
		{}

	}

}

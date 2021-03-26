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

	Logger getLogger();

	String getLogFileName();

	default void setupLogging()
	{
		try
		{
			final Handler[] handlers = getLogger().getHandlers();
			for ( final Handler handler : handlers )
			{
				if ( handler instanceof FileHandler )
				{
					( ( FileHandler ) handler ).close();
					getLogger().removeHandler( handler );
				}
			}
			final FileHandler fileHandler = new FileHandler( getLogFileName(), true );
			fileHandler.setFormatter( new SimpleFormatter() );
			getLogger().addHandler( fileHandler );
			// Create a ConsoleHandler when it is not exists
			boolean hasConsoleHandler = false;
			for ( final Handler handler : handlers )
			{
				if ( handler instanceof ConsoleHandler )
					hasConsoleHandler = true;
			}
			if ( !hasConsoleHandler )
			{
				getLogger().addHandler( new ConsoleHandler() );
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
				getLogger().addHandler( JFrameHandler.getInstance() );
			}
		}
		catch ( SecurityException | IOException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	default void showLogWindow()
	{
		JFrameHandler.getInstance().showLogWindow();
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

		public LogWindow()
		{
			super( "ELEPHANT Log Window" );
			setSize( 400, 600 );
			add( new JScrollPane( colorPane ) );
			setVisible( true );
		}

		public void showMessage( final String msg, final Color color )
		{
			colorPane.append( color, msg );
			SwingUtilities.invokeLater( () -> this.validate() );
		}
	}

	class JFrameHandler extends Handler
	{
		private static JFrameHandler handler = null;

		private LogWindow window = null;

		private JFrameHandler()
		{
			final LogManager manager = LogManager.getLogManager();
			final String className = this.getClass().getName();
			final String level = manager.getProperty( className + ".level" );
			setLevel( level != null ? Level.parse( level ) : Level.INFO );
			setFormatter( new SimpleFormatter() );
			if ( window == null )
				window = new LogWindow();
		}

		public static synchronized JFrameHandler getInstance()
		{
			if ( handler == null )
			{
				handler = new JFrameHandler();
			}
			return handler;
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

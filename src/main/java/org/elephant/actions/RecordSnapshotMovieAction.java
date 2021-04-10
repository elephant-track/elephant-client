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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.revised.bdv.ViewerFrameMamut;
import org.mastodon.revised.bdv.ViewerPanelMamut;
import org.mastodon.revised.mamut.MamutViewBdv;

/**
 * Record a snapshot movie.
 * 
 * @author Ko Sugawara
 */
public class RecordSnapshotMovieAction extends AbstractElephantAction implements WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] take a snapshot movie";

	private static final String MENU_TEXT = "Take a Snapshot Movie";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public RecordSnapshotMovieAction()
	{
		super( NAME );
	}

	@Override
	public void process()
	{
		final List< MamutViewBdv > bdvWindows = getBdvWindows();
		if ( 0 < bdvWindows.size() )
		{
			final String[] bdvNames = new String[ bdvWindows.size() ];
			for ( int i = 0; i < bdvWindows.size(); i++ )
				bdvNames[ i ] = bdvWindows.get( i ).getContextProvider().getName();
			final AtomicReference< String > selectedWindowName = new AtomicReference<>();
			final AtomicReference< String > saveDirPath = new AtomicReference<>();
			final AtomicInteger minTimepoint = new AtomicInteger( 0 );
			final AtomicInteger maxTimepoint = new AtomicInteger( getAppModel().getMaxTimepoint() );
			try
			{
				SwingUtilities.invokeAndWait( () -> {
					final RecordSnapshotMovieDialog dialog = new RecordSnapshotMovieDialog( bdvNames, maxTimepoint.get() );
					dialog.setVisible( true );
					try
					{
						if ( !dialog.isCanceled() )
						{
							selectedWindowName.set( dialog.getSelectedWindowName() );
							minTimepoint.set( dialog.getMinTimepoint() );
							maxTimepoint.set( dialog.getMaxTimepoint() );
							saveDirPath.set( dialog.getSaveDirPath() );
						}
					}
					finally
					{
						dialog.dispose();
					}
				} );
			}
			catch ( InvocationTargetException | InterruptedException e )
			{
				getLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}
			if ( selectedWindowName.get() != null && saveDirPath.get() != null )
			{
				for ( final MamutViewBdv bdvWindow : bdvWindows )
				{
					if ( bdvWindow.getContextProvider().getName().equals( selectedWindowName.get() ) )
					{
						final ViewerPanelMamut viewerPanel = ( ( ViewerFrameMamut ) bdvWindow.getFrame() ).getViewerPanel();
						for ( int i = minTimepoint.get(); i <= maxTimepoint.get(); i++ )
						{
							viewerPanel.setTimepoint( i );
							try
							{
								Thread.sleep( 1000 );
							}
							catch ( final InterruptedException e )
							{
								getLogger().severe( ExceptionUtils.getStackTrace( e ) );
							}
							final JComponent displayComponent = viewerPanel.getDisplay();
							final BufferedImage image = new BufferedImage( displayComponent.getWidth(), displayComponent.getHeight(), BufferedImage.TYPE_INT_ARGB );
							displayComponent.paint( image.getGraphics() );
							try
							{
								ImageIO.write( image, "png", Paths.get( saveDirPath.get(), String.format( "t%04d.png", i ) ).toFile() );
							}
							catch ( final IOException e )
							{
								getLogger().severe( ExceptionUtils.getStackTrace( e ) );
							}
						}
					}
				}
			}
		}
	}

}

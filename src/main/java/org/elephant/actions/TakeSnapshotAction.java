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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.revised.bdv.ViewerFrameMamut;
import org.mastodon.revised.mamut.MamutViewBdv;

/**
 * Take a snapshot of the specified BDV window.
 * 
 * @author Ko Sugawara
 */
public class TakeSnapshotAction extends AbstractElephantAction
		implements WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] take a snapshot";

	private static final String MENU_TEXT = "Take a Snapshot";

	private static final String[] MENU_KEYS = new String[] { "H" };

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	@Override
	public String[] getMenuKeys()
	{
		return MENU_KEYS;
	}

	public TakeSnapshotAction()
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
			final AtomicReference< String > saveFilePath = new AtomicReference<>();
			try
			{
				SwingUtilities.invokeAndWait( () -> {
					final SnapshotDialog dialog = new SnapshotDialog( bdvNames );
					dialog.setVisible( true );
					try
					{
						if ( !dialog.isCanceled() )
						{
							selectedWindowName.set( dialog.getSelectedWindowName() );
							saveFilePath.set( dialog.getSaveFilePath() );
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
			if ( selectedWindowName.get() != null && saveFilePath.get() != null )
			{
				for ( final MamutViewBdv bdvWindow : bdvWindows )
				{
					if ( bdvWindow.getContextProvider().getName().equals( selectedWindowName.get() ) )
					{
						final JComponent displayComponent = ( ( ViewerFrameMamut ) bdvWindow.getFrame() ).getViewerPanel().getDisplay();
						final BufferedImage image = new BufferedImage( displayComponent.getWidth(), displayComponent.getHeight(), BufferedImage.TYPE_INT_ARGB );
						displayComponent.paint( image.getGraphics() );
						try
						{
							ImageIO.write( image, "png", new File( saveFilePath.get() ) );
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

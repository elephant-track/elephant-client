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
package org.elephant.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.actions.mixins.UnirestMixin;

import kong.unirest.Unirest;

/**
 * Upload an image data (.h5) to the server.
 * 
 * @author Ko Sugawara
 */
public class UploadAction extends AbstractElephantAction
		implements BdvDataMixin, ElephantConstantsMixin, UnirestMixin, URLMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] upload an image data (.h5) to the server";

	private static final String MENU_TEXT = "Upload an Image Data";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public UploadAction()
	{
		super( NAME );
	}

	@Override
	void process()
	{
		File hdf5File = getHdf5File();
		if ( hdf5File == null || !hdf5File.exists() )
		{
			final JFileChooser chooser = new JFileChooser();
			final FileNameExtensionFilter filter = new FileNameExtensionFilter( "BigDataViewer HDF 5", "h5" );
			chooser.setFileFilter( filter );
			final AtomicReference< File > file = new AtomicReference<>();
			try
			{
				SwingUtilities.invokeAndWait( () -> {
					final int returnVal = chooser.showOpenDialog( null );
					if ( returnVal == JFileChooser.APPROVE_OPTION )
					{
						file.set( chooser.getSelectedFile() );
					}
				} );
			}
			catch ( final InvocationTargetException | InterruptedException e )
			{
				getLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}
			hdf5File = file.get();
		}
		if ( hdf5File != null )
		{
			final JFrame frame = new JFrame( "Uploading..." );
			frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			final JProgressBar progressBar = new JProgressBar();
			progressBar.setValue( 0 );
			progressBar.setStringPainted( true );
			frame.add( progressBar );
			frame.pack();
			frame.setLocationRelativeTo( null );
			SwingUtilities.invokeLater( () -> frame.setVisible( true ) );
			Unirest.post( getEndpointURL( ENDPOINT_UPLOAD ) )
					.field( "dataset", getMainSettings().getDatasetName() )
					.field( "file", hdf5File, hdf5File.getName() )
					.uploadMonitor( ( field, fileName, bytesWritten, totalBytes ) -> {
						System.out.println( String.format( "%.2f / %.2f", toMB( bytesWritten ), toMB( totalBytes ) ) );
						progressBar.setValue( ( int ) ( 100 * bytesWritten / totalBytes ) );
						if ( bytesWritten >= totalBytes )
						{
							SwingUtilities.invokeLater( () -> frame.setVisible( false ) );
						}
					} )
					.asEmpty();
		}
	}

	private static double toMB( final long bytes )
	{
		return ( double ) bytes / 1024 / 1024;
	}

}

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
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

	public static final String NAME = "[elephant] upload an image data (.h5) to the server";

	private static final String MENU_TEXT = "Upload an Image Data";

	final static int CHUNK_SIZE = 10 * 1024 * 1024;

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
				getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}
			hdf5File = file.get();
		}
		if ( hdf5File != null )
		{
			final UploadDialog uploadDialog = new UploadDialog();
			SwingUtilities.invokeLater( () -> uploadDialog.setVisible( true ) );
			try
			{
				final byte[] buff = new byte[ CHUNK_SIZE ];
				final long fileSize = hdf5File.length();
				long bytesOffset = 0;
				try (final InputStream fis = new FileInputStream( hdf5File ))
				{
					while ( !uploadDialog.isCancelled() )
					{
						final int readBytes = fis.read( buff );
						if ( readBytes == -1 )
						{
							break;
						}
						final long bytesOffsetFinal = bytesOffset;
						final File tempFile = File.createTempFile( "elephant", ".h5", null );
						try
						{
							try (final FileOutputStream fos = new FileOutputStream( tempFile ))
							{
								fos.write( buff );
							}
							Unirest.post( getEndpointURL( ENDPOINT_UPLOAD ) )
									.field( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
									.field( "filename", hdf5File.getName() )
									.field( "action", bytesOffset == 0 ? "init" : "append" )
									.field( "file", tempFile )
									.uploadMonitor( ( field, fileName, bytesWritten, totalBytes ) -> {
										uploadDialog.setLabelText( String.format( "%.2f MB / %.2f MB", toMB( Math.min( fileSize, bytesOffsetFinal + bytesWritten ) ), toMB( fileSize ) ) );
										uploadDialog.setProgressBarValue( ( int ) ( 100 * ( bytesOffsetFinal + bytesWritten ) / fileSize ) );
									} )
									.asEmpty();
							bytesOffset += readBytes;
						}
						finally
						{
							tempFile.delete();
						}
					}
					Unirest.post( getEndpointURL( ENDPOINT_UPLOAD ) )
							.field( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
							.field( "filename", hdf5File.getName() )
							.field( "action", uploadDialog.isCancelled() ? "cancel" : "complete" )
							.asEmpty();
				}
				SwingUtilities.invokeLater( () -> uploadDialog.dispose() );
			}
			catch ( final IOException e )
			{
				getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
				return;
			}
		}
	}

	private static double toMB( final long bytes )
	{
		return ( double ) bytes / 1024 / 1024;
	}

}

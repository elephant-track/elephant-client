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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.actions.mixins.UnirestMixin;

import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.Partition;
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

	private void uploadFile( final File file, final UploadDialog uploadDialog, final String labelPrefix )
	{
		final byte[] buff = new byte[ CHUNK_SIZE ];
		final long fileSize = file.length();
		long bytesOffset = 0;
		try (final InputStream fis = new FileInputStream( file ))
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
					Unirest.post( getEndpointURL( ENDPOINT_UPLOAD_IMAGE ) )
							.field( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
							.field( "filename", file.getName() )
							.field( "action", bytesOffset == 0 ? "init" : "append" )
							.field( "file", tempFile )
							.uploadMonitor( ( field, fileName, bytesWritten, totalBytes ) -> {
								uploadDialog.setLabelText( labelPrefix + String.format( "%.2f MB / %.2f MB", toMB( Math.min( fileSize, bytesOffsetFinal + bytesWritten ) ), toMB( fileSize ) ) );
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
			Unirest.post( getEndpointURL( ENDPOINT_UPLOAD_IMAGE ) )
					.field( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
					.field( "filename", file.getName() )
					.field( "action", uploadDialog.isCancelled() ? "cancel" : "complete" )
					.asEmpty();
		}
		catch ( final IOException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
			return;
		}
	}

	private void uploadPartition( final Partition partition, final int i, final UploadDialog uploadDialog, final int nPartitions )
	{
		getClientLogger().info( partition.getPath() );
		final File hdf5File = new File( partition.getPath() );
		final String labelPrefix = String.format( "%d / %d: ", i + 1, nPartitions );
		uploadFile( hdf5File, uploadDialog, labelPrefix );
	}

	@Override
	void process()
	{
		if ( !( getImgLoader() instanceof Hdf5ImageLoader ) )
		{
			try
			{
				SwingUtilities.invokeAndWait( () -> JOptionPane.showMessageDialog( null, "ELEPHANT only supports XML/HDF5 data format" ) );
			}
			catch ( InvocationTargetException | InterruptedException e )
			{
				handleError( e );
			}
			return;
		}
		if ( !( ( Hdf5ImageLoader ) getImgLoader() ).getPartitions().isEmpty() )
		{
			final UploadDialog uploadDialog = new UploadDialog();
			SwingUtilities.invokeLater( () -> uploadDialog.setVisible( true ) );
			try
			{
				final ArrayList< Partition > partitions = getPartitions();
				final int nPartitions = partitions.size();
				final AtomicInteger count = new AtomicInteger();
				for ( final Partition partition : partitions )
				{
					getClientLogger().info( partition.toString() );
					uploadPartition( partition, count.getAndIncrement(), uploadDialog, nPartitions );
				}
				if ( uploadDialog.isCancelled() )
				{
					try
					{
						SwingUtilities.invokeAndWait( () -> JOptionPane.showMessageDialog( null, "Upload cancelled" ) );
					}
					catch ( InvocationTargetException | InterruptedException e )
					{
						handleError( e );
					}
					return;
				}
			}
			finally
			{
				SwingUtilities.invokeLater( () -> uploadDialog.dispose() );
			}
		}

		File hdf5File = getHdf5File();
		if ( hdf5File != null )
		{
			final UploadDialog uploadDialog = new UploadDialog();
			SwingUtilities.invokeLater( () -> uploadDialog.setVisible( true ) );
			try
			{
				uploadFile( hdf5File, uploadDialog, "" );
			}
			finally
			{
				SwingUtilities.invokeLater( () -> uploadDialog.dispose() );
			}
		}
	}

	private static double toMB( final long bytes )
	{
		return ( double ) bytes / 1024 / 1024;
	}

}

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
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantConstantsMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.actions.mixins.UnirestMixin;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;

/**
 * Download a model parameter file.
 *
 * @author Ko Sugawara
 */
public class DownloadModelAction extends AbstractElephantAction implements ElephantConstantsMixin, UnirestMixin, URLMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] download %s model";

	private static final String NAME_DETECTION = String.format( NAME_BASE, "detection" );

	private static final String NAME_FLOW = String.format( NAME_BASE, "flow" );

	private static final String MENU_TEXT_BASE = "Download %s Model";

	private static final String MENU_TEXT_DETECTION = String.format( MENU_TEXT_BASE, "Detection" );

	private static final String MENU_TEXT_FLOW = String.format( MENU_TEXT_BASE, "Flow" );

	public enum DownloadModelActionMode
	{
		DETECTION( NAME_DETECTION, MENU_TEXT_DETECTION ),
		FLOW( NAME_FLOW, MENU_TEXT_FLOW );

		private String name;

		private String menuText;

		private DownloadModelActionMode( final String name, final String menuText )
		{
			this.name = name;
			this.menuText = menuText;
		}

		public String getName()
		{
			return name;
		}

		public String getMenuText()
		{
			return menuText;
		}
	}

	private final DownloadModelActionMode mode;

	@Override
	public String getMenuText()
	{
		return mode.getMenuText();
	}

	public DownloadModelAction( final DownloadModelActionMode mode )
	{
		super( mode.getName() );
		this.mode = mode;
	}

	private enum Prefs
	{
		FileLocation;
		private static Preferences prefs = Preferences.userRoot().node( Prefs.class.getName() );

		String get()
		{
			return prefs.get( this.name(), System.getProperty( "user.home" ).replace( "\\", "/" ) );
		}

		void put( String value )
		{
			prefs.put( this.name(), value );
		}
	}

	@Override
	void process()
	{
		final String modelFilename = mode == DownloadModelActionMode.DETECTION ? getMainSettings().getDetectionModelName() : getMainSettings().getFlowModelName();
		final AtomicReference< File > fileReference = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final JFileChooser fileChooser = new JFileChooser()
				{
					// https://stackoverflow.com/a/3729157
					private static final long serialVersionUID = 1L;

					@Override
					public void approveSelection()
					{
						final File f = getSelectedFile();
						if ( f.exists() && getDialogType() == SAVE_DIALOG )
						{
							final int result = JOptionPane.showConfirmDialog( this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION );
							switch ( result )
							{
							case JOptionPane.YES_OPTION:
								super.approveSelection();
								return;
							case JOptionPane.NO_OPTION:
								return;
							case JOptionPane.CLOSED_OPTION:
								return;
							}
						}
						super.approveSelection();
					}
				};
				fileChooser.setDialogTitle( "Specify a file name" );
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				fileChooser.setFileFilter( new FileNameExtensionFilter( "*.pth", "pth" ) );
				fileChooser.setCurrentDirectory( new File( Prefs.FileLocation.get() ) );
				fileChooser.setSelectedFile( new File( Paths.get( modelFilename ).getFileName().toString() ) );
				fileChooser.setAcceptAllFileFilterUsed( false );

				final int userSelection = fileChooser.showSaveDialog( null );

				if ( userSelection == JFileChooser.APPROVE_OPTION )
				{
					fileReference.set( fileChooser.getSelectedFile() );
					Prefs.FileLocation.put( fileChooser.getSelectedFile().getAbsolutePath() );
				}
			} );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		final File file = fileReference.get();
		if ( file == null )
			return;
		final JsonObject jsonRootObject = Json.object()
				.add( JSON_KEY_MODEL_NAME, modelFilename );
		try
		{
			postAsFileAsync( getEndpointURL( ENDPOINT_MODEL_DOWNLOAD ), jsonRootObject.toString(), file.getAbsolutePath(),
					response -> {
						if ( response.getStatus() == HttpURLConnection.HTTP_OK )
						{
							showTextOverlayAnimator( "completed", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
						}
						else
						{
							final StringBuilder sb = new StringBuilder( response.getStatusText() );
							showTextOverlayAnimator( sb.toString(), 3000, TextPosition.CENTER );
							getClientLogger().severe( sb.toString() );
						}
					} );
		}
		catch ( final ElephantConnectException e )
		{
			// already handled by UnirestMixin
		}
	}
}

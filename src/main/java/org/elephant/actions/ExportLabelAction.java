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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantConnectException;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.TimepointMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.elephant.actions.mixins.UnirestMixin;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.pool.PoolCollectionWrapper;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.Zip4jUtil;

/**
 * Export tracking results in a CTC format.
 * 
 * <p>
 * Only spots tagged with {@link STATUS_COMPLETED_TAG_NAME} and its links are
 * exported.
 *
 * @author Ko Sugawara
 */
public class ExportLabelAction extends AbstractElephantDatasetAction
		implements BdvDataMixin, ElephantGraphTagActionMixin, TimepointMixin, UIActionMixin, UnirestMixin, URLMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] export label";

	private static final String MENU_TEXT = "Export Label";

	private static final int MINIMUM_ID = 1;

	private static final int UNSET = -1;

	private static final String RES_ZIPNAME = "res.zip";

	private int id;

	private final double[] pos = new double[ 3 ];

	private final double[][] cov = new double[ 3 ][ 3 ];

	private final double[] cov1d = new double[ 9 ];

	private File dir;

	private JsonObject jsonRootObject;

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ExportLabelAction()
	{
		super( NAME );
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
	boolean prepare()
	{
		final int timepointEnd = getCurrentTimepoint( 0 );
		final int timeRange = getMainSettings().getTimeRange();
		final int timepointStart = Math.max( 0, timepointEnd - ( timeRange - 1 ) );
		id = MINIMUM_ID;
		final AtomicReference< File > dirReference = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle( "Specify a directory to save" );
				fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				fileChooser.setCurrentDirectory( new File( Prefs.FileLocation.get() ) );
				fileChooser.setAcceptAllFileFilterUsed( false );

				final int userSelection = fileChooser.showSaveDialog( null );

				if ( userSelection == JFileChooser.APPROVE_OPTION )
				{
					dirReference.set( fileChooser.getSelectedFile() );
					Prefs.FileLocation.put( fileChooser.getSelectedFile().getAbsolutePath() );
				}
			} );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		dir = dirReference.get();
		if ( dir == null )
			return false;
		final ObjTagMap< Spot, Tag > tagMapDetection = getVertexTagMap( getDetectionTagSet() );
		final Tag detectionUnlabeledTag = getTag( getDetectionTagSet(), DETECTION_UNLABELED_TAG_NAME );
		final Tag detectionTnTag = getTag( getDetectionTagSet(), DETECTION_TN_TAG_NAME );
		final Tag detectionFpTag = getTag( getDetectionTagSet(), DETECTION_FP_TAG_NAME );
		final JsonArray jsonSpots = Json.array();
		getGraph().getLock().readLock().lock();
		try
		{
			final PoolCollectionWrapper< Spot > spots = getGraph().vertices();
			final RefList< Spot > rootSpots = RefCollections.createRefList( spots );
			for ( final Spot spot : spots )
			{
				final Tag spotTag = tagMapDetection.get( spot );
				if ( spotTag != detectionUnlabeledTag )
					jsonSpots.add( Json.object()
							.add( "t", spot.getTimepoint() )
							.add( "pos", Json.array( pos ) )
							.add( "covariance", Json.array( cov1d ) )
							.add( "is_bg", spotTag == detectionTnTag || spotTag == detectionFpTag )
							.add( "value", id ) );
			}
		}
		finally
		{
			getGraph().getLock().readLock().unlock();
		}

		final VoxelDimensions voxelSize = getVoxelDimensions();
		final JsonArray scales = new JsonArray()
				.add( voxelSize.dimension( 0 ) )
				.add( voxelSize.dimension( 1 ) )
				.add( voxelSize.dimension( 2 ) );
		jsonRootObject = Json.object()
				.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
				.add( JSON_KEY_SCALES, scales )
				.add( JSON_KEY_SPOTS, jsonSpots )
				.add( JSON_KEY_T_START, timepointStart )
				.add( JSON_KEY_T_END, timepointEnd )
				.add( JSON_KEY_IS_3D, !is2D() );
		return true;
	}

	@Override
	public void processDataset()
	{
		final String zipAbsolutePath = Paths.get( dir.getAbsolutePath(), RES_ZIPNAME ).toString();
		try
		{
			postAsFileAsync( getEndpointURL( ENDPOINT_DOWNLOAD_CTC ), jsonRootObject.toString(), zipAbsolutePath,
					response -> {
						if ( response.getStatus() == HttpURLConnection.HTTP_OK )
						{
							try
							{
								try (final ZipFile zipFile = new ZipFile( zipAbsolutePath ))
								{
									final List< FileHeader > fileHeaders = zipFile.getFileHeaders();
									final long currentTime = System.currentTimeMillis();
									fileHeaders.forEach( header -> header.setLastModifiedTime( Zip4jUtil.epochToExtendedDosTime( currentTime ) ) );
									zipFile.extractAll( dir.getAbsolutePath() );
								}
								catch ( ZipException e )
								{
									throw e;
								}
								catch ( IOException e )
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							catch ( final ZipException e )
							{
								getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
							}
							showTextOverlayAnimator( "completed", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
						}
						else if ( response.getStatus() == HttpURLConnection.HTTP_NO_CONTENT )
						{
							showTextOverlayAnimator( "cancelled", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
						}
						else
						{
							final StringBuilder sb = new StringBuilder( response.getStatusText() );
							showTextOverlayAnimator( sb.toString(), 3000, TextPosition.CENTER );
							getClientLogger().severe( sb.toString() );
						}
						try
						{
							Files.deleteIfExists( Paths.get( zipAbsolutePath ) );
						}
						catch ( final IOException e )
						{
							getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
						}
					} );
		}
		catch ( final ElephantConnectException e )
		{
			// already handled by UnirestMixin
		}
	}

}

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
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.elephant.actions.mixins.TimepointActionMixin;
import org.elephant.actions.mixins.UIActionMixin;
import org.elephant.actions.mixins.URLMixin;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.pool.PoolCollectionWrapper;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.opencsv.CSVWriter;

import bdv.viewer.animate.TextOverlayAnimator;
import bdv.viewer.animate.TextOverlayAnimator.TextPosition;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
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
public class ExportCTCAction extends AbstractElephantAction
		implements BdvDataMixin, ElephantGraphTagActionMixin, TimepointActionMixin, UIActionMixin, URLMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] export ctc";

	private static final String MENU_TEXT = "Export CTC";

	private static final int MINIMUM_ID = 1;

	private static final int UNSET = -1;

	private static final String RES_FILENAME = "res_track.txt";

	private static final String RES_ZIPNAME = "res.zip";

	private int id;

	private final double[] pos = new double[ 3 ];

	private final double[][] cov = new double[ 3 ][ 3 ];

	private final double[] cov1d = new double[ 9 ];

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ExportCTCAction()
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
	public void process()
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
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		final File dir = dirReference.get();
		if ( dir != null )
		{
			final ObjTagMap< Spot, Tag > tagMapStatus = getVertexTagMap( getStatusTagSet() );
			final Tag statusCompletedTag = getTag( getStatusTagSet(), STATUS_COMPLETED_TAG_NAME );
			final List< CTCTrackEntity > trackList = new ArrayList<>();
			final JsonArray jsonSpots = Json.array();
			getGraph().getLock().readLock().lock();
			try
			{
				final PoolCollectionWrapper< Spot > spots = getGraph().vertices();
				final RefList< Spot > rootSpots = RefCollections.createRefList( spots );
				for ( final Spot spot : spots )
				{
					if ( tagMapStatus.get( spot ) == statusCompletedTag && spot.incomingEdges().isEmpty() )
						rootSpots.add( spot );
				}
				final Spot ref = getGraph().vertexRef();
				for ( int i = 0; i < rootSpots.size(); i++ )
				{
					rootSpots.get( i, ref );
					buildResult( ref, trackList, UNSET, 0, jsonSpots );
				}
				getGraph().releaseRef( ref );
			}
			finally
			{
				getGraph().getLock().readLock().unlock();
			}

			final File file = Paths.get( dir.getAbsolutePath(), RES_FILENAME ).toFile();
			try (final FileWriter fileWriter = new FileWriter( file ))
			{
				final CSVWriter writer = new CSVWriter( fileWriter, ' ', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END );
				try
				{
					for ( int i = 0; i < trackList.size(); i++ )
						writer.writeNext( trackList.get( i ).toCsvEntry() );
				}
				finally
				{
					writer.close();
				}
			}
			catch ( final IOException e )
			{
				getLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}

			final VoxelDimensions voxelSize = getVoxelDimensions();
			final JsonArray scales = new JsonArray()
					.add( voxelSize.dimension( 0 ) )
					.add( voxelSize.dimension( 1 ) )
					.add( voxelSize.dimension( 2 ) );
			final JsonObject jsonRootObject = Json.object()
					.add( JSON_KEY_DATASET_NAME, getMainSettings().getDatasetName() )
					.add( JSON_KEY_SCALES, scales )
					.add( JSON_KEY_SPOTS, jsonSpots )
					.add( JSON_KEY_T_START, timepointStart )
					.add( JSON_KEY_T_END, timepointEnd );

			final String zipAbsolutePath = Paths.get( dir.getAbsolutePath(), RES_ZIPNAME ).toString();
			Unirest.post( getEndpointURL( "export/ctc" ) )
					.body( jsonRootObject.toString() )
					.asFileAsync( zipAbsolutePath, new Callback< File >()
					{

						@Override
						public void failed( final UnirestException e )
						{
							getLogger().severe( ExceptionUtils.getStackTrace( e ) );
							getLogger().severe( "The request has failed" );
							showTextOverlayAnimator( e.getLocalizedMessage(), 3000, TextPosition.CENTER );
						}

						@Override
						public void completed( final HttpResponse< File > response )
						{
							if ( response.getStatus() == 200 )
							{
								try
								{
									final ZipFile zipFile = new ZipFile( zipAbsolutePath );
									final List< FileHeader > fileHeaders = zipFile.getFileHeaders();
									final long currentTime = System.currentTimeMillis();
									fileHeaders.forEach( header -> header.setLastModifiedTime( Zip4jUtil.epochToExtendedDosTime( currentTime ) ) );
									zipFile.extractAll( dir.getAbsolutePath() );
								}
								catch ( final ZipException e )
								{
									getLogger().severe( ExceptionUtils.getStackTrace( e ) );
								}
								showTextOverlayAnimator( "completed", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
							}
							else if ( response.getStatus() == 204 )
							{
								showTextOverlayAnimator( "cancelled", 3000, TextOverlayAnimator.TextPosition.BOTTOM_RIGHT );
							}
							else
							{
								final StringBuilder sb = new StringBuilder( response.getStatusText() );
								showTextOverlayAnimator( sb.toString(), 3000, TextPosition.CENTER );
								getLogger().severe( sb.toString() );
							}
							try
							{
								Files.deleteIfExists( Paths.get( zipAbsolutePath ) );
							}
							catch ( final IOException e )
							{
								getLogger().severe( ExceptionUtils.getStackTrace( e ) );
							}
						}

						@Override
						public void cancelled()
						{
							getLogger().info( "The request has been cancelled" );
						}

					} );
		}

	}

	private class CTCTrackEntity
	{
		private final int id;

		private final int start;

		private final int end;

		private final int parent;

		public CTCTrackEntity( final int id, final int start, final int end, final int parent )
		{
			this.id = id;
			this.start = start;
			this.end = end;
			this.parent = parent;
		}

		public String[] toCsvEntry()
		{
			return new String[] {
					Integer.toString( id ),
					Integer.toString( start ),
					Integer.toString( end ),
					Integer.toString( parent )
			};
		}
	}

	private void buildResult( Spot spot, List< CTCTrackEntity > trackList, int start, int parent, final JsonArray jsonSpots )
	{
		final RefList< Link > outgoingEdges = RefCollections.createRefList( getGraph().edges() );
		for ( final Link edge : spot.outgoingEdges() )
			outgoingEdges.add( edge );
		if ( start == UNSET )
			start = spot.getTimepoint();
		// add json entry
		spot.localize( pos );
		spot.getCovariance( cov );
		for ( int i = 0; i < 3; i++ )
			for ( int j = 0; j < 3; j++ )
				cov1d[ i * 3 + j ] = cov[ i ][ j ];
		jsonSpots.add( Json.object()
				.add( "t", spot.getTimepoint() )
				.add( "pos", Json.array( pos ) )
				.add( "covariance", Json.array( cov1d ) )
				.add( "value", id ) );
		if ( outgoingEdges.size() == 0 )
		{
			trackList.add( new CTCTrackEntity( id, start, spot.getTimepoint(), parent ) );
			id++;
			return;
		}
		if ( 1 < outgoingEdges.size() )
		{
			trackList.add( new CTCTrackEntity( id, start, spot.getTimepoint(), parent ) );
			parent = id;
			id++;
			start = UNSET;
		}
		for ( final Link edge : outgoingEdges )
			buildResult( edge.getTarget( spot ), trackList, start, parent, jsonSpots );
	}

}

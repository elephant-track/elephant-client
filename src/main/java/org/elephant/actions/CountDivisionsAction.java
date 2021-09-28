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
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.pool.PoolCollectionWrapper;

import com.opencsv.CSVWriter;

/**
 * Count number of divisions in a lineage tree and output as a .csv file.
 * 
 * <p>
 * In the {@code ENTIRE} mode, a total number of divisions per timepoint is
 * calculated.
 * 
 * <p>
 * In the {@code TRACKWISE} mode, a trackwise number of divisions per timepoint
 * is calculated.
 * 
 * @author Ko Sugawara
 */
public class CountDivisionsAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] count divisions (%s)";

	private static final String MENU_TEXT = "Count Divisions (%s)";

	private static final String LAST_USED_FOLDER = "last used folder";

	public enum CountDivisionsActionMode
	{
		ENTIRE( String.format( NAME, "entire" ), String.format( MENU_TEXT, "Entire" ) ),
		TRACKWISE( String.format( NAME, "trackwise" ), String.format( MENU_TEXT, "Trackwise" ) );

		private String name;

		private String menuText;

		private CountDivisionsActionMode( final String name, final String menuText )
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

	private final CountDivisionsActionMode mode;

	@Override
	public String getMenuText()
	{
		return mode.getMenuText();
	}

	public CountDivisionsAction( final CountDivisionsActionMode mode )
	{
		super( mode.getName() );
		this.mode = mode;
	}

	@Override
	public void process()
	{
		final AtomicReference< File > fileReference = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final Preferences prefs = Preferences.userRoot().node( getClass().getName() );

				final JFileChooser chooser = new JFileChooser( prefs.get( LAST_USED_FOLDER,
						new File( "." ).getAbsolutePath() ) );
				final FileNameExtensionFilter filter = new FileNameExtensionFilter( "CSV file", "csv" );
				chooser.setFileFilter( filter );
				chooser.setSelectedFile( new File( "result.csv" ) );
				final int selection = chooser.showSaveDialog( null );
				if ( selection == JFileChooser.APPROVE_OPTION )
				{
					File selectedFile = chooser.getSelectedFile();
					prefs.put( LAST_USED_FOLDER, selectedFile.getParent() );
					if ( !selectedFile.getAbsolutePath().endsWith( "csv" ) )
						selectedFile = new File( selectedFile.getAbsolutePath() + ".csv" );
					fileReference.set( selectedFile );
				}
			} );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		final File file = fileReference.get();
		if ( file != null )
		{
			switch ( mode )
			{
			case ENTIRE:
				final ObjTagMap< Spot, Tag > tagMapTracking = getVertexTagMap( getTrackingTagSet() );
				final Tag trackingApprovedTag = getTag( getTrackingTagSet(), TRACKING_APPROVED_TAG_NAME );

				final int[] count = new int[ getNTimepoints() ];

				for ( final Spot spot : getGraph().vertices() )
				{
					if ( tagMapTracking.get( spot ) == trackingApprovedTag && spot.outgoingEdges().size() == 2 )
						count[ spot.getTimepoint() ]++;
				}
				try (final FileWriter fileWriter = new FileWriter( file ))
				{
					final CSVWriter writer = new CSVWriter( fileWriter );
					try
					{
						for ( int i = 0; i < count.length; i++ )
						{
							writer.writeNext( new String[] { Integer.toString( i ), Integer.toString( count[ i ] ) } );
						}
					}
					finally
					{
						writer.close();
					}
				}
				catch ( final IOException e )
				{
					getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
				}
				break;
			case TRACKWISE:
				final ObjTagMap< Spot, Tag > tagMapStatus = getVertexTagMap( getStatusTagSet() );
				final Tag statusCompletedTag = getTag( getStatusTagSet(), STATUS_COMPLETED_TAG_NAME );
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
					final int[][] counts = new int[ getNTimepoints() ][ rootSpots.size() ];
					for ( int i = 0; i < counts.length; i++ )
						for ( int j = 0; j < counts[ 0 ].length; j++ )
							counts[ i ][ j ] = -1;
					final Spot ref = getGraph().vertexRef();
					final String[] labels = new String[ rootSpots.size() + 1 ];
					labels[ 0 ] = "Timepoint";
					for ( int i = 0; i < rootSpots.size(); i++ )
					{
						rootSpots.get( i, ref );
						labels[ i + 1 ] = ref.getLabel();
						countDivisions( ref, counts, i );
					}
					getGraph().releaseRef( ref );
					for ( final Spot spot : spots )
					{
						spot.getLabel();
					}

					try (final FileWriter fileWriter = new FileWriter( file ))
					{
						final CSVWriter writer = new CSVWriter( fileWriter );
						writer.writeNext( labels );
						try
						{
							for ( int i = 0; i < counts.length; i++ )
							{
								final String[] nextLine = new String[ counts[ 0 ].length + 1 ];
								nextLine[ 0 ] = Integer.toString( i );
								for ( int j = 0; j < nextLine.length - 1; j++ )
									nextLine[ j + 1 ] = Integer.toString( counts[ i ][ j ] );
								writer.writeNext( nextLine );
							}
						}
						finally
						{
							writer.close();
						}
					}
					catch ( final IOException e )
					{
						getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
					}
				}
				finally
				{
					getGraph().getLock().readLock().unlock();
				}
				break;
			default:
				break;
			}
		}
	}

	private void countDivisions( Spot spot, int[][] counts, int trackIndex )
	{
		counts[ spot.getTimepoint() ][ trackIndex ] = Math.max( 0, counts[ spot.getTimepoint() ][ trackIndex ] );
		final RefList< Link > outgoingEdges = RefCollections.createRefList( getGraph().edges() );
		for ( final Link edge : spot.outgoingEdges() )
			outgoingEdges.add( edge );
		if ( outgoingEdges.size() == 2 )
			counts[ spot.getTimepoint() ][ trackIndex ]++;
		for ( final Link edge : outgoingEdges )
			countDivisions( edge.getTarget( spot ), counts, trackIndex );
	}

}

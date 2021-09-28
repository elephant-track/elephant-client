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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Import spots and links from a masotodon project file.
 * 
 * @author Ko Sugawara
 */
public class ImportMastodonAction extends AbstractElephantAction implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] import mastodon";

	private static final String LAST_SELECTED_FOLDER = "[import mastodon] last selected folder";

	private static final String MENU_TEXT = "Import Mastodon";

	public ImportMastodonAction()
	{
		super( NAME );
	}

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	@Override
	public void process()
	{
		final AtomicReference< File > fileReference = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> {
				final Preferences prefs = Preferences.userRoot().node( getClass().getName() );

				final JFileChooser chooser = new JFileChooser( prefs.get( LAST_SELECTED_FOLDER,
						new File( "." ).getAbsolutePath() ) );
				final FileNameExtensionFilter filter = new FileNameExtensionFilter( "mastodon file", "mastodon" );
				chooser.setFileFilter( filter );
				final int selection = chooser.showOpenDialog( null );
				if ( selection == JFileChooser.APPROVE_OPTION )
				{
					final File selectedFile = chooser.getSelectedFile();
					prefs.put( LAST_SELECTED_FOLDER, selectedFile.getParent() );
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
			getGraph().getLock().writeLock().lock();
			getActionStateManager().setWriting( true );
			try
			{
				final MamutProject project = new MamutProjectIO().load( file.getAbsolutePath() );
				final MamutProject.ProjectReader reader = project.openForReading();
				final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
				model.loadRaw( reader );
				final List< TagSet > tagSetsSrc = model.getTagSetModel().getTagSetStructure().getTagSets();

				final Map< TagSet, TagSet > tagSetSrcDstMap = new HashMap<>();
				final Map< Tag, Tag > tagSrcDstMap = new HashMap<>();
				final Map< Integer, Integer > srcPoolIndexToDstListIndexMap = new HashMap<>();
				for ( final TagSet tagSetSrc : tagSetsSrc )
				{
					final TagSet tagSetDst = getTagSetOrCreate( tagSetSrc );
					tagSetSrcDstMap.put( tagSetSrc, tagSetDst );
					for ( final Tag tagSrc : tagSetSrc.getTags() )
					{
						final Tag tagDst = getTagOrCreate( tagSetDst, tagSrc );
						tagSrcDstMap.put( tagSrc, tagDst );
					}
				}
				final double[] pos = new double[ 3 ];
				final double[][] cov = new double[ 3 ][ 3 ];
				final Spot spotDst = getGraph().vertexRef();
				final RefList< Spot > spotRefList = RefCollections.createRefList( getGraph().vertices() );
				for ( final Spot spotSrc : model.getGraph().vertices() )
				{
					spotSrc.localize( pos );
					spotSrc.getCovariance( cov );
					spotDst.refTo( getGraph().addVertex().init( spotSrc.getTimepoint(), pos, cov ) );
					spotRefList.add( spotDst );
					srcPoolIndexToDstListIndexMap.put( spotSrc.getInternalPoolIndex(), spotRefList.lastIndexOf( spotDst ) );
					for ( final TagSet tagSetSrc : tagSetsSrc )
					{
						final ObjTagMap< Spot, Tag > vertexTagMapSrc = model.getTagSetModel().getVertexTags().tags( tagSetSrc );
						final Tag tagSrc = vertexTagMapSrc.get( spotSrc );
						if ( tagSrc != null )
						{
							final ObjTagMap< Spot, Tag > vertexTagMapDst = getVertexTagMap( tagSetSrcDstMap.get( tagSetSrc ) );
							vertexTagMapDst.set( spotDst, tagSrcDstMap.get( tagSrc ) );
						}
					}
				}
				final Spot spotSrcEdgeSource = getGraph().vertexRef();
				final Spot spotSrcEdgeTarget = getGraph().vertexRef();
				final Spot spotDstEdgeSource = getGraph().vertexRef();
				final Spot spotDstEdgeTarget = getGraph().vertexRef();
				final Link edgeDst = getGraph().edgeRef();
				for ( final Spot spotSrc : model.getGraph().vertices() )
				{
					for ( final Link edgeSrc : spotSrc.incomingEdges() )
					{
						edgeSrc.getSource( spotSrcEdgeSource );
						edgeSrc.getTarget( spotSrcEdgeTarget );
						spotDstEdgeSource.refTo( spotRefList.get(
								srcPoolIndexToDstListIndexMap.get( spotSrcEdgeSource.getInternalPoolIndex() ) ) );
						spotDstEdgeTarget.refTo( spotRefList.get(
								srcPoolIndexToDstListIndexMap.get( spotSrcEdgeTarget.getInternalPoolIndex() ) ) );
						edgeDst.refTo( getGraph().addEdge( spotDstEdgeSource, spotDstEdgeTarget ).init() );
						for ( final TagSet tagSetSrc : tagSetsSrc )
						{
							final ObjTagMap< Link, Tag > edgeTagMapSrc = model.getTagSetModel().getEdgeTags().tags( tagSetSrc );
							final Tag tagSrc = edgeTagMapSrc.get( edgeSrc );
							if ( tagSrc != null )
							{
								final ObjTagMap< Link, Tag > edgeTagMapDst = getEdgeTagMap( tagSetSrcDstMap.get( tagSetSrc ) );
								edgeTagMapDst.set( edgeDst, tagSrcDstMap.get( tagSrc ) );
							}
						}
					}
				}
			}
			catch ( final IOException e )
			{
				getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}
			finally
			{
				getActionStateManager().setWriting( false );
				getModel().setUndoPoint();
				getGraph().getLock().writeLock().unlock();
				notifyGraphChanged();
			}
		}
	}

}

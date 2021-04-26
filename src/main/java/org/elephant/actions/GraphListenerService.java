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

import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.graph.GraphListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Listen addition/removal of spots/links, writing logs and putting appropriate
 * tags to them.
 *
 * @author Ko Sugawara
 */
public class GraphListenerService extends AbstractElephantService
		implements GraphListener< Spot, Link >, ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	public GraphListenerService( final MamutPluginAppModel pluginAppModel )
	{
		super();
		super.init( pluginAppModel, null );
	}

	/**
	 * GraphListener< Spot, Link >
	 */

	@Override
	public void graphRebuilt()
	{
		getLogger().info( "Graph rebuilt" );
	}

	@Override
	public void vertexAdded( Spot vertex )
	{
		// ignore if modified programatically
		if ( !getStateManager().isWriting() )
		{
			getLogger().info( vertex + " added" );
			final ObjTagMap< Spot, Tag > tagMapDetection = getTagSetModel().getVertexTags().tags( getDetectionTagSet() );
			final ObjTagMap< Spot, Tag > tagMapTracking = getTagSetModel().getVertexTags().tags( getTrackingTagSet() );
			getGraph().getLock().writeLock().lock();
			try
			{
				final Spot ref = getGraph().vertexRef();
				ref.refTo( vertex );
				tagMapDetection.set( ref, getTag( getDetectionTagSet(), DETECTION_FN_TAG_NAME ) );
				tagMapTracking.set( ref, getTag( getTrackingTagSet(), TRACKING_APPROVED_TAG_NAME ) );
				getAppModel().getHighlightModel().highlightVertex( ref );
				getGraph().releaseRef( ref );
			}
			finally
			{
				// not required to set undo point here
				getGraph().getLock().writeLock().unlock();
				notifyGraphChanged();
			}
		}
	}

	@Override
	public void vertexRemoved( Spot vertex )
	{
		// ignore if modified programatically
		if ( !getStateManager().isWriting() )
		{
			getLogger().info( vertex + " removed" );
		}
	}

	@Override
	public void edgeAdded( Link edge )
	{
		// ignore if modified programatically
		if ( !getStateManager().isWriting() )
		{
			getLogger().info( edge + " added" );
			final TagSet tagSetTracking = getTrackingTagSet();
			final ObjTagMap< Link, Tag > tagMapTracking = getTagSetModel().getEdgeTags().tags( tagSetTracking );
			getGraph().getLock().writeLock().lock();
			try
			{
				final Link ref = getGraph().edgeRef();
				ref.refTo( edge );
				tagMapTracking.set( ref, getTag( tagSetTracking, TRACKING_APPROVED_TAG_NAME ) );
				getGraph().releaseRef( ref );
			}
			finally
			{
				// not required to set undo point here
				getGraph().getLock().writeLock().unlock();
				notifyGraphChanged();
			}
		}
	}

	@Override
	public void edgeRemoved( Link edge )
	{
		// ignore if modified programatically
		if ( !getStateManager().isWriting() )
		{
			getLogger().info( edge + " removed" );
		}
	}

}

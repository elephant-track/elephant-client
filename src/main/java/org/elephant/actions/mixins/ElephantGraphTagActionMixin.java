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
package org.elephant.actions.mixins;

import java.util.Collection;
import java.util.function.Predicate;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Handle spots and links.
 * 
 * @author Ko Sugawara
 */
public interface ElephantGraphTagActionMixin
		extends ElephantTagActionMixin, GraphChangeActionMixin, ElephantStateManagerMixin
{

	default void removeSpots( final RefCollection< Spot > spots, final Predicate< Spot > filter )
	{
		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			final RefList< Spot > toRemove = RefCollections.createRefList( spots );
			for ( final Spot spot : spots )
			{
				if ( filter == null || filter.test( spot ) )
					toRemove.add( spot );
			}
			for ( final Spot spot : toRemove )
				getGraph().remove( spot );
		}
		finally
		{
			getStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

	default void removeEdges( final RefCollection< Link > edges, final Predicate< Link > filter )
	{
		getGraph().getLock().writeLock().lock();
		getStateManager().setWriting( true );
		try
		{
			final RefList< Link > toRemove = RefCollections.createRefList( edges );

			for ( final Link edge : edges )
			{
				if ( filter == null || filter.test( edge ) )
					toRemove.add( edge );
			}
			for ( final Link edge : toRemove )
				getGraph().remove( edge );
		}
		finally
		{
			getStateManager().setWriting( false );
			getGraph().getLock().writeLock().unlock();
			notifyGraphChanged();
		}
	}

	default void addSpotsToJson( final Iterable< Spot > spots, final JsonArray jsonSpots, final Predicate< Spot > filter )
	{
		final ObjTagMap< Spot, Tag > tagMap = getVertexTagMap( getDetectionTagSet() );
		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		final double[] cov1d = new double[ 9 ];
		for ( final Spot spot : spots )
		{
			if ( filter == null || filter.test( spot ) )
			{
				spot.localize( pos );
				spot.getCovariance( cov );
				for ( int i = 0; i < 3; i++ )
					for ( int j = 0; j < 3; j++ )
						cov1d[ i * 3 + j ] = cov[ i ][ j ];
				final String label = spot.getLabel();
				final int id = spot.getInternalPoolIndex();

				final JsonObject jsonSpot = Json.object()
						.add( "t", spot.getTimepoint() )
						.add( "pos", Json.array( pos ) )
						.add( "covariance", Json.array( cov1d ) )
						.add( "label", label )
						.add( "id", id )
						.add( "tag", tagMap.get( spot ).label() );
				jsonSpots.add( jsonSpot );
			}
		}
	}

	default void addEdgesToJsonFlow( final Collection< Link > edges, final JsonArray jsonSpots, final Collection< Integer > timepoints )
	{
		final double[] posSource = new double[ 3 ];
		final double[] posTarget = new double[ 3 ];
		final double[] displacement = new double[ 3 ];
		final double[][] covTarget = new double[ 3 ][ 3 ];
		final double[] cov1dTarget = new double[ 9 ];
		final Spot refSource = getGraph().vertexRef();
		final Spot refTarget = getGraph().vertexRef();
		for ( final Link edge : edges )
		{
			edge.getSource( refSource );
			edge.getTarget( refTarget );
			if ( !timepoints.contains( refTarget.getTimepoint() ) )
				continue;
			refSource.localize( posSource );
			refTarget.localize( posTarget );
			for ( int i = 0; i < 3; i++ )
				displacement[ i ] = posSource[ i ] - posTarget[ i ];

			refTarget.getCovariance( covTarget );
			for ( int i = 0; i < 3; i++ )
				for ( int j = 0; j < 3; j++ )
					cov1dTarget[ i * 3 + j ] = covTarget[ i ][ j ];

			final int timepoint = refSource.getTimepoint();
			jsonSpots.add( Json.object()
					.add( "pos", Json.array( posTarget ) )
					.add( "covariance", Json.array( cov1dTarget ) )
					.add( "t", timepoint )
					.add( "displacement", Json.array( displacement ) ) );
		}
	}

}

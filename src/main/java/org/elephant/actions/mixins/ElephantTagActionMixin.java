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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

import com.opencsv.CSVReader;

/**
 * Handle {@link Tag} and {@link TagSet}.
 * 
 * @author Ko Sugawara
 */
public interface ElephantTagActionMixin extends TagActionMixin, ElephantConstantsMixin
{
	final Color transparentColor = new Color( 1, 1, 1, 0 );

	default Collection< Spot > getVerticesTaggedWith( final Tag tag )
	{
		return getTagSetModel().getVertexTags().getTaggedWith( tag );
	}

	default Collection< Link > getEdgesTaggedWith( final Tag tag )
	{
		return getTagSetModel().getEdgeTags().getTaggedWith( tag );
	}

	default ObjTagMap< Spot, Tag > getVertexTagMap( final TagSet tagSet )
	{
		return getTagSetModel().getVertexTags().tags( tagSet );
	}

	default ObjTagMap< Link, Tag > getEdgeTagMap( final TagSet tagSet )
	{
		return getTagSetModel().getEdgeTags().tags( tagSet );
	}

	default Tag getTagOrCreate( final TagSet tagSet, final Tag tag )
	{
		return tagSet.getTags().stream().filter( tagDst -> tagDst.label().equals( tag.label() ) ).findFirst().orElseGet( () -> {
			return tagSet.createTag( tag.label(), tag.color() );
		} );
	}

	default TagSet getTagSetOrCreate( final TagSet tagSet )
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		return tss.getTagSets().stream().filter( ts -> ts.getName().equals( tagSet.getName() ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( tagSet.getName() );
			for ( final Tag tagQuery : tagSet.getTags() )
				tagSetCopy.createTag( tagQuery.label(), tagQuery.color() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
	}

	default TagSet getDetectionTagSet()
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		final TagSet tagSet = tss.getTagSets().stream().filter( ts -> ts.getName().equals( DETECTION_TAGSET_NAME ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( DETECTION_TAGSET_NAME );
			tagSetCopy.createTag( DETECTION_TP_TAG_NAME, Color.CYAN.getRGB() );
			tagSetCopy.createTag( DETECTION_FP_TAG_NAME, Color.MAGENTA.getRGB() );
			tagSetCopy.createTag( DETECTION_TN_TAG_NAME, Color.RED.getRGB() );
			tagSetCopy.createTag( DETECTION_FN_TAG_NAME, Color.YELLOW.getRGB() );
			tagSetCopy.createTag( DETECTION_TB_TAG_NAME, Color.ORANGE.getRGB() );
			tagSetCopy.createTag( DETECTION_FB_TAG_NAME, Color.PINK.getRGB() );
			tagSetCopy.createTag( DETECTION_UNLABELED_TAG_NAME, Color.GREEN.getRGB() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
		return tagSet;
	}

	default TagSet getTrackingTagSet()
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		final TagSet tagSet = tss.getTagSets().stream().filter( ts -> ts.getName().equals( TRACKING_TAGSET_NAME ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( TRACKING_TAGSET_NAME );
			tagSetCopy.createTag( TRACKING_APPROVED_TAG_NAME, Color.CYAN.getRGB() );
			tagSetCopy.createTag( TRACKING_UNLABELED_TAG_NAME, Color.GREEN.getRGB() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
		return tagSet;
	}

	default TagSet getProgenitorTagSet()
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		final TagSet tagSet = tss.getTagSets().stream().filter( ts -> ts.getName().equals( PROGENITOR_TAGSET_NAME ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( PROGENITOR_TAGSET_NAME );
			try
			{
				final InputStream input = getClass().getResourceAsStream( "/glasbey_no_black.txt" );
				final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( input ) );
				final CSVReader reader = new CSVReader( bufferedReader );

				reader.readNext();
				// https://stackoverflow.com/questions/14827322/assign-ascii-character-a-z-in-a-list#answer-14827598
				for ( int i = 1; i < 256; i++ )
				{
					final String[] line = reader.readNext();
					final Color color = new Color( Integer.valueOf( line[ 0 ] ), Integer.valueOf( line[ 1 ] ), Integer.valueOf( line[ 2 ] ) );
					tagSetCopy.createTag( String.valueOf( i ), color.getRGB() );
				}
				reader.close();
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}

			tagSetCopy.createTag( TRACKING_UNLABELED_TAG_NAME, transparentColor.getRGB() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
		return tagSet;
	}

	default TagSet getStatusTagSet()
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		final TagSet tagSet = tss.getTagSets().stream().filter( ts -> ts.getName().equals( STATUS_TAGSET_NAME ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( STATUS_TAGSET_NAME );
			tagSetCopy.createTag( STATUS_COMPLETED_TAG_NAME, Color.CYAN.getRGB() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
		return tagSet;
	}

	default TagSet getProliferatorTagSet()
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		final TagSet tagSet = tss.getTagSets().stream().filter( ts -> ts.getName().equals( PROLIFERATOR_TAGSET_NAME ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( PROLIFERATOR_TAGSET_NAME );
			tagSetCopy.createTag( PROLIFERATOR_PROLIFERATOR_TAG_NAME, Color.CYAN.getRGB() );
			tagSetCopy.createTag( PROLIFERATOR_NONPROLIFERATOR_TAG_NAME, Color.MAGENTA.getRGB() );
			tagSetCopy.createTag( PROLIFERATOR_INVISIBLE_TAG_NAME, transparentColor.getRGB() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
		return tagSet;
	}

	default TagSet getDivisionTagSet()
	{
		final TagSetStructure tss = getTagSetModel().getTagSetStructure();
		final TagSet tagSet = tss.getTagSets().stream().filter( ts -> ts.getName().equals( DIVISION_TAGSET_NAME ) ).findFirst().orElseGet( () -> {
			final TagSetStructure tssCopy = new TagSetStructure();
			tssCopy.set( tss );
			final TagSet tagSetCopy = tssCopy.createTagSet( DIVISION_TAGSET_NAME );
			tagSetCopy.createTag( DIVISION_DIVIDING_TAG_NAME, Color.CYAN.getRGB() );
			tagSetCopy.createTag( DIVISION_DIVIDED_TAG_NAME, Color.YELLOW.getRGB() );
			tagSetCopy.createTag( DIVISION_NONDIVIDING_TAG_NAME, Color.MAGENTA.getRGB() );
			tagSetCopy.createTag( DIVISION_INVISIBLE_TAG_NAME, transparentColor.getRGB() );
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
			return tagSetCopy;
		} );
		return tagSet;
	}

	@Override
	default TagSet getTagSetByName( final String name )
	{
		if ( name.equals( DETECTION_TAGSET_NAME ) )
			return getDetectionTagSet();
		else if ( name.equals( TRACKING_TAGSET_NAME ) )
			return getTrackingTagSet();
		else if ( name.equals( PROGENITOR_TAGSET_NAME ) )
			return getProgenitorTagSet();
		else if ( name.equals( STATUS_TAGSET_NAME ) )
			return getStatusTagSet();
		else if ( name.equals( PROLIFERATOR_TAGSET_NAME ) )
			return getProliferatorTagSet();
		else if ( name.equals( DIVISION_TAGSET_NAME ) )
			return getDivisionTagSet();
		throw new NoSuchElementException();
	}

}

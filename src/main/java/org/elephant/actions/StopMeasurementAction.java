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

import java.util.Arrays;
import java.util.List;

import org.elephant.actions.mixins.ElephantGraphTagActionMixin;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;

/**
 * Log a stop time of the measurement.
 * 
 * @author Ko Sugawara
 */
public class StopMeasurementAction extends AbstractElephantAction
		implements ElephantGraphTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] stop measurement";

	private static final String MENU_TEXT = "Stop Measurement";

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public StopMeasurementAction()
	{
		super( NAME );
	}

	@Override
	public void run()
	{
		try
		{
			super.run();
		}
		catch ( final ActionNotInitializedException e )
		{
			return;
		}
		final ObjTagMap< Spot, Tag > tagMap = getVertexTagMap( getDetectionTagSet() );
		final Tag tpTag = getTag( getDetectionTagSet(), DETECTION_TP_TAG_NAME );
		final Tag fnTag = getTag( getDetectionTagSet(), DETECTION_FN_TAG_NAME );
		final List< Tag > tagsToProcess = Arrays.asList( tpTag, fnTag );
		final StringBuilder sb = new StringBuilder();
		for ( final Spot spot : getGraph().vertices() )
		{
			if ( tagsToProcess.contains( tagMap.get( spot ) ) )
			{
				if ( sb.length() != 0 )
					sb.append( ", " );
				sb.append( spot.getInternalPoolIndex() );
			}
		}
		getLogger().info( sb.toString() );
		getLogger().info( "********** STOP MEASUREMENT **********" );
	}
}

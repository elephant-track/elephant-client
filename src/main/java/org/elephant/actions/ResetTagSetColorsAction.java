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

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.elephant.actions.mixins.ElephantTagActionMixin;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

/**
 * Reset tag set colors.
 * 
 * @author Ko Sugawara
 */
public class ResetTagSetColorsAction extends AbstractElephantAction implements ElephantTagActionMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] reset tag set colors";

	private static final String MENU_TEXT = "Reset Tag Set Colors";

	private static final String[] CHOICES = { "Basic", "Advanced" };

	private static final Map< String, Map< String, Color > > COLOR_MAPS = new HashMap< String, Map< String, Color > >()
	{
		private static final long serialVersionUID = 1L;

		{
			put( "Basic", new HashMap< String, Color >()
			{
				private static final long serialVersionUID = 1L;

				{
					put( DETECTION_TP_TAG_NAME, Color.CYAN );
					put( DETECTION_FP_TAG_NAME, Color.MAGENTA );
					put( DETECTION_TN_TAG_NAME, Color.MAGENTA );
					put( DETECTION_FN_TAG_NAME, Color.CYAN );
					put( DETECTION_TB_TAG_NAME, Color.ORANGE );
					put( DETECTION_FB_TAG_NAME, Color.ORANGE );
					put( DETECTION_UNLABELED_TAG_NAME, Color.GREEN );
				}
			} );
			put( "Advanced", new HashMap< String, Color >()
			{
				private static final long serialVersionUID = 1L;

				{
					put( DETECTION_TP_TAG_NAME, Color.CYAN );
					put( DETECTION_FP_TAG_NAME, Color.MAGENTA );
					put( DETECTION_TN_TAG_NAME, Color.RED );
					put( DETECTION_FN_TAG_NAME, Color.YELLOW );
					put( DETECTION_TB_TAG_NAME, Color.ORANGE );
					put( DETECTION_FB_TAG_NAME, Color.PINK );
					put( DETECTION_UNLABELED_TAG_NAME, Color.GREEN );
				}
			} );
		}
	};

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ResetTagSetColorsAction()
	{
		super( NAME );
	}

	@Override
	void process()
	{
		final AtomicReference< String > choice = new AtomicReference<>();
		try
		{
			SwingUtilities.invokeAndWait( () -> choice.set( ( String ) JOptionPane.showInputDialog( null, "Choose color set",
					"Reset tag set colors", JOptionPane.QUESTION_MESSAGE, null,
					CHOICES,
					CHOICES[ 0 ] ) ) );
		}
		catch ( InvocationTargetException | InterruptedException e )
		{
			handleError( e );
		}
		final Map< String, Color > colorMap = COLOR_MAPS.get( choice.get() );
		final TagSetStructure tssCopy = new TagSetStructure();
		tssCopy.set( getTagSetModel().getTagSetStructure() );
		final TagSet detectionTagSet = tssCopy.getTagSets().stream().filter( ts -> ts.getName().equals( DETECTION_TAGSET_NAME ) ).findFirst().orElse( null );
		if ( detectionTagSet == null )
		{
			try
			{
				SwingUtilities.invokeAndWait( () -> JOptionPane.showMessageDialog( null, "Detection tag set not found." ) );
			}
			catch ( InvocationTargetException | InterruptedException e )
			{
				handleError( e );
			}
		}
		else
		{
			for ( final Tag tag : detectionTagSet.getTags() )
			{
				tag.setColor( colorMap.get( tag.label() ).getRGB() );
			}
			getTagSetModel().pauseListeners();
			try
			{
				getTagSetModel().setTagSetStructure( tssCopy );
			}
			finally
			{
				getTagSetModel().resumeListeners();
			}
		}
	}

}

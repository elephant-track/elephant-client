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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.elephant.actions.mixins.GraphActionMixin;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelOverlayProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.NavigationHandler;
import org.mastodon.views.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.views.bdv.overlay.wrap.OverlayVertexWrapper;

/**
 * Autofocus to the highlighted spot when the {@code isAutoFocus} is on.
 * 
 * <p>
 * Users can toggle autofocus by {@link ToggleAutoFocusAction}.
 * 
 * @author Ko Sugawara
 */
public class HighlightListenerService extends AbstractElephantService
		implements GraphActionMixin, HighlightListener
{

	private static final long serialVersionUID = 1L;

	private final OverlayGraphWrapper< Spot, Link > overlayGraphWrapper;

	private int lastHightedVertexId;

	public HighlightListenerService( final MamutPluginAppModel pluginAppModel )
	{
		super();
		super.init( pluginAppModel, null );
		overlayGraphWrapper = new OverlayGraphWrapper<>(
				getAppModel().getModel().getGraph(),
				getAppModel().getModel().getGraphIdBimap(),
				getAppModel().getModel().getSpatioTemporalIndex(),
				getAppModel().getModel().getGraph().getLock(),
				new ModelOverlayProperties( getAppModel().getModel().getGraph(), getAppModel().getRadiusStats() ) );
	}

	/**
	 * HighlightListener
	 */
	@Override
	public void highlightChanged()
	{
		if ( !ElephantActionStateManager.INSTANCE.isAutoFocus() )
			return;
		final Spot spotRef = getGraph().vertexRef();
		final OverlayVertexWrapper< Spot, Link > reusableRightRef = overlayGraphWrapper.getVertexMap().reusableRightRef();
		try
		{
			final Spot highlightedVertex = getAppModel().getHighlightModel().getHighlightedVertex( spotRef );
			if ( highlightedVertex != null && highlightedVertex.getInternalPoolIndex() != lastHightedVertexId )
			{
				overlayGraphWrapper.getVertexMap().getRight( highlightedVertex, reusableRightRef );
				getPluginAppModel().getWindowManager().forEachBdvView( view -> {
					try
					{
						@SuppressWarnings( "unchecked" )
						final NavigationHandler< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > navigationHandler = ( NavigationHandler< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > ) FieldUtils.readField( view, "navigationHandler", true );
						navigationHandler.notifyNavigateToVertex( reusableRightRef );
					}
					catch ( final IllegalAccessException e )
					{
						getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );;
					}
				} );
				lastHightedVertexId = highlightedVertex.getInternalPoolIndex();
			}
		}
		finally
		{
			getGraph().releaseRef( spotRef );
			overlayGraphWrapper.getVertexMap().releaseRef( reusableRightRef );
		}
	}

}

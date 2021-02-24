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
package org.elephant;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elephant.actions.AbstractElephantAction;
import org.elephant.actions.RandomSampleAction;
import org.elephant.actions.StartMeasurementAction;
import org.elephant.actions.StopMeasurementAction;
import org.elephant.actions.TrackingStatisticsAction;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.plugin.MastodonPlugin;
import org.mastodon.plugin.MastodonPluginAppModel;
import org.mastodon.revised.ui.keymap.Keymap.UpdateListener;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;

/**
 * An extra plugin for ELEPHANT. This class is not included in a .jar package by
 * default. This behavior can be modified in pom.xml in the root directory.
 * 
 * @author Ko Sugawara
 */
@Plugin( type = ElephantExtra.class, priority = Priority.LOW )
public class ElephantExtra implements MastodonPlugin, UpdateListener
{

	private MastodonPluginAppModel pluginAppModel;

	private GroupHandle groupHandle;

	// ToggleAutoFocusAction is experimental
//	private final AbstractElephantAction toggleAutoFocusAction;

	private final AbstractElephantAction startMeasurementAction;

	private final AbstractElephantAction stopMeasurementAction;

	private final AbstractElephantAction randomSampleAction;

	private final AbstractElephantAction trackingStatisticsAction;

	private final List< AbstractElephantAction > pluginActions = new ArrayList<>();

	public ElephantExtra()
	{
//		toggleAutoFocusAction = new ToggleAutoFocusAction();
//		pluginActions.add( toggleAutoFocusAction );
		startMeasurementAction = new StartMeasurementAction();
		pluginActions.add( startMeasurementAction );
		stopMeasurementAction = new StopMeasurementAction();
		pluginActions.add( stopMeasurementAction );
		randomSampleAction = new RandomSampleAction();
		pluginActions.add( randomSampleAction );
		trackingStatisticsAction = new TrackingStatisticsAction();
		pluginActions.add( trackingStatisticsAction );
	}

	/**
	 * Set up {@link MastodonPluginAppModel}-dependent modules.
	 */
	@Override
	public void setAppModel( final MastodonPluginAppModel pluginAppModel )
	{
		this.pluginAppModel = pluginAppModel;
		// Create a GroupHandle instance
		groupHandle = pluginAppModel.getAppModel().getGroupManager().createGroupHandle();
		groupHandle.setGroupId( 0 );
		// Initialize actions
		for ( final AbstractElephantAction pluginAction : pluginActions )
		{
			pluginAction.init( pluginAppModel, groupHandle );
		}
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins",
						menu( "ELEPHANT",
								menu( "Utils",
										separator(),
										// item( toggleAutoFocusAction.name() ),
										item( randomSampleAction.name() ) ),
								menu( "Analysis",
										separator(),
										item( trackingStatisticsAction.name() ),
										item( startMeasurementAction.name() ),
										item( stopMeasurementAction.name() ) ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		final Map< String, String > menuTexts = new HashMap<>();
		for ( final AbstractElephantAction pluginAction : pluginActions )
			menuTexts.put( pluginAction.name(), pluginAction.getMenuText() );
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		pluginActions.forEach( pluginAction -> actions.namedAction( pluginAction, pluginAction.getMenuKeys() ) );
	}

	/**
	 * UpdateListener
	 */
	@Override
	public void keymapChanged()
	{
		installGlobalActions( pluginAppModel.getAppModel().getPlugins().getPluginActions() );
	}

}

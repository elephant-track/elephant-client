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

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.BdvDataMixin;
import org.elephant.setting.ElephantSettingsConfigPage;
import org.elephant.setting.ElephantSettingsConfigPageFactory;
import org.elephant.setting.ElephantSettingsConfigPageFactory.TreePath;
import org.elephant.setting.ElephantSettingsDialog;
import org.elephant.setting.TypeMismatchException;
import org.elephant.setting.main.ElephantMainSettings;
import org.elephant.setting.main.ElephantMainSettingsListener;
import org.elephant.setting.main.ElephantMainSettingsManager;
import org.elephant.setting.server.ElephantServerSettings;
import org.elephant.setting.server.ElephantServerSettingsManager;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.plugin.MamutPluginAppModel;

/**
 * Show a preferences dialog.
 * 
 * @author Ko Sugawara
 */
public class ShowPreferencesAction extends AbstractElephantAction implements BdvDataMixin
{

	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] preferences...";

	private static final String MENU_TEXT = "Preferences...";

	private ElephantSettingsDialog dialog = new ElephantSettingsDialog();

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ShowPreferencesAction()
	{
		super( NAME );
	}

	@Override
	public void init( MamutPluginAppModel pluginAppModel, GroupHandle groupHandle )
	{
		super.init( pluginAppModel, groupHandle );

		dialog = new ElephantSettingsDialog();
		final ElephantSettingsConfigPageFactory< ElephantMainSettingsManager, ElephantMainSettings > mainPageFactory = new ElephantSettingsConfigPageFactory<>( ElephantMainSettingsManager.getInstance() );
		final ElephantSettingsConfigPageFactory< ElephantServerSettingsManager, ElephantServerSettings > serverPageFactory = new ElephantSettingsConfigPageFactory<>( ElephantServerSettingsManager.getInstance() );
		try
		{
			final ElephantSettingsConfigPage< ElephantMainSettings > mainConfigPage = mainPageFactory.create( TreePath.MAIN, getVoxelDimensions().unit() );
			final ElephantSettingsConfigPage< ElephantServerSettings > serverConfigPage = serverPageFactory.create( TreePath.SERVER );

			dialog.addPage( mainConfigPage );
			dialog.addPage( serverConfigPage );
		}
		catch ( UnsupportedOperationException | TypeMismatchException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	@Override
	public void process()
	{
		SwingUtilities.invokeLater( () -> dialog.setVisible( true ) );
	}

	public void addSettingsListener( ElephantMainSettingsListener listener )
	{
		dialog.addSettingsListener( listener );
	}

}

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
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.mixins.ElephantActionMixin;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.GroupHandleMixin;
import org.elephant.actions.mixins.LoggerMixin;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.ProjectModel;

/**
 * An abstract class for ELEPHANT actions.
 * 
 * <p>
 * ELEPHANT actions are supposed to extend this class.
 * 
 * <p>
 * This class provides accesses to {@link MastodonPluginAppModel},
 * {@link GroupHandle} and {@link Logger} instances, which can be shared among
 * all actions and services.
 * 
 * @author Ko Sugawara
 */
public abstract class AbstractElephantAction extends AbstractConcurrentRunnableAction
		implements ElephantActionMixin, ElephantSettingsMixin, GroupHandleMixin, LoggerMixin
{

	private static final long serialVersionUID = 1L;

	private static final String CLIENT_LOG_KEY = "elephantClient";

	private static final String SERVER_LOG_KEY = "elephantServer";

	private static final String LOG_DIR = System.getProperty( "user.home" ).replace( "\\", "/" ) + "/.mastodon/logs";

	private ProjectModel pluginAppModel;

	private GroupHandle groupHandle;

	public AbstractElephantAction( final String name )
	{
		super( name );
	}

	public String getMenuText()
	{
		return "";
	};

	public String[] getMenuKeys()
	{
		return new String[] { "not mapped" };
	}

	@Override
	public ProjectModel getPluginAppModel()
	{
		return pluginAppModel;
	}

	@Override
	public GroupHandle getGroupHandle()
	{
		return groupHandle;
	}

	public void init( final ProjectModel pluginAppModel )
	{
		init( pluginAppModel, null );
	}

	public void init( final ProjectModel pluginAppModel, final GroupHandle groupHandle )
	{
		this.pluginAppModel = pluginAppModel;
		this.groupHandle = groupHandle;
	}

	@Override
	public Logger getClientLogger()
	{
		return Logger.getLogger( CLIENT_LOG_KEY );
	}

	@Override
	public Logger getServerLogger()
	{
		return Logger.getLogger( SERVER_LOG_KEY );
	}

	@Override
	public String getClientLogFileName()
	{
		final File logDir = new File( LOG_DIR );
		if ( !logDir.exists() )
			logDir.mkdir();

		return Paths.get( LOG_DIR, "client_" + getMainSettings().getLogFileName() ).toString();
	}

	@Override
	public String getServerLogFileName()
	{
		final File logDir = new File( LOG_DIR );
		if ( !logDir.exists() )
			logDir.mkdir();

		return Paths.get( LOG_DIR, "server_" + getMainSettings().getLogFileName() ).toString();
	}

	@Override
	public void run()
	{
		if ( pluginAppModel == null )
		{
			try
			{
				SwingUtilities.invokeAndWait( () -> JOptionPane.showMessageDialog( null,
						"Please create/load a project first.",
						"Warning",
						JOptionPane.WARNING_MESSAGE ) );
			}
			catch ( InvocationTargetException | InterruptedException e )
			{
				getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
			}
		}
		else
		{
			process();
		}
	}

	abstract void process();

}

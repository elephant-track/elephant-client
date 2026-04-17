/*-
 * #%L
 * elephant
 * %%
 * Copyright (C) 2019 - 2026 Ko Sugawara
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.elephant.actions;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.ElephantStateManagerMixin;

public class ShowControlPanelAction extends AbstractElephantAction
		implements ElephantServerStatusListener, ElephantStateManagerMixin, RabbitMQStatusListener
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] control panel";

	private static final String MENU_TEXT = "Control Panel";

	private static final ControlPanelWindow dialog = new ControlPanelWindow();

	@Override
	public String getMenuText()
	{
		return MENU_TEXT;
	}

	public ShowControlPanelAction()
	{
		super( NAME );
		dialog.setVisible( true );
	}

	@Override
	void process()
	{
		SwingUtilities.invokeLater( () -> dialog.setVisible( true ) );
	}

	@Override
	public void serverStatusUpdated()
	{
		final ElephantStatus status = getServerStateManager().getElephantServerStatus();
		final String url = getServerSettings().getServerURL();
		final String errorMessage = getServerStateManager().getElephantServerErrorMessage();
		try
		{
			dialog.updateElephantServerStatus( status, url, errorMessage );
		}
		catch ( final IOException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		final List< GPU > gpus = getServerStateManager().getGpus();
		dialog.updateGpuTableModel( gpus );
	}

	@Override
	public void rabbitMQStatusUpdated()
	{
		final ElephantStatus status = getServerStateManager().getRabbitMQStatus();
		final String protocol = getServerSettings().getUseSslProtocol() ? "amqps://" : "amqp://";
		final String url = protocol + getServerSettings().getRabbitMQHost() + ":"
				+ String.valueOf( getServerSettings().getRabbitMQPort() )
				+ getServerSettings().getRabbitMQVirtualHost();
		final String errorMessage = getServerStateManager().getRabbitMQErrorMessage();
		try
		{
			dialog.updateRabbitMQStatus( status, url, errorMessage );
		}
		catch ( final IOException e )
		{
			getClientLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

}

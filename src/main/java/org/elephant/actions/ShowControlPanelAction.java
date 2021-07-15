package org.elephant.actions;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.ElephantSettingsMixin;
import org.elephant.actions.mixins.ElephantStateManagerMixin;

public class ShowControlPanelAction extends AbstractElephantAction
		implements ElephantServerStatusListener, ElephantSettingsMixin, ElephantStateManagerMixin, RabbitMQStatusListener
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "[elephant] control panel";

	private static final String MENU_TEXT = "Control Panel";

	private static final ControlPanelDialog dialog = new ControlPanelDialog();

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
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
		final List< GPU > gpus = getServerStateManager().getGpus();
		dialog.updateGpuTableModel( gpus );
	}

	@Override
	public void rabbitMQStatusUpdated()
	{
		final ElephantStatus status = getServerStateManager().getRabbitMQStatus();
		final String url = "amqp://" + getServerSettings().getRabbitMQHost() + ":" + String.valueOf( getServerSettings().getRabbitMQPort() );
		final String errorMessage = getServerStateManager().getRabbitMQErrorMessage();
		try
		{
			dialog.updateRabbitMQStatus( status, url, errorMessage );
		}
		catch ( final IOException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

}

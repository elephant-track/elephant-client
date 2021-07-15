package org.elephant.actions;

import java.io.IOException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;

public class ShowControlPanelAction extends AbstractElephantAction implements ElephantServerStatusListener, ElephantGpuStatusListener
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
	public void statusUpdated( final ElephantStatus status, final String url )
	{
		try
		{
			if ( url.startsWith( "amqp://" ) )
			{
				dialog.updateRabbitMQStatus( status, url );
			}
			else
			{
				dialog.updateElephantServerStatus( status, url );
			}
		}
		catch ( final IOException e )
		{
			getLogger().severe( ExceptionUtils.getStackTrace( e ) );
		}
	}

	@Override
	public void statusUpdated( Collection< GPU > gpus )
	{
		dialog.updateGpuTableModel( gpus );
	}

}

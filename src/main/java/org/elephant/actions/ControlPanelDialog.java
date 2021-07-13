package org.elephant.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.elephant.actions.ElephantStatusService.ElephantStatus;

public class ControlPanelDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	final JLabel lblElephantServerStatus = new JLabel( "Checking..." );

	final JLabel lblElephantServerAddress = new JLabel( "http://localhost:8080" );

	final JLabel lblRabbitMQStatus = new JLabel( "Checking..." );

	final JLabel lblRabbitMQAddress = new JLabel( "amqp://localhost:5672" );

	public ControlPanelDialog()
	{
		setTitle( "ELEPHANT Control Panel" );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 1, 1 };
		gridBagLayout.rowHeights = new int[] { 1, 1 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
		getContentPane().setLayout( gridBagLayout );

		final JLabel lblElephantServer = new JLabel( "ELEPHANT server" );
		final GridBagConstraints gbc_lblElephantServer = new GridBagConstraints();
		gbc_lblElephantServer.weighty = 1.0;
		gbc_lblElephantServer.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblElephantServer.gridx = 0;
		gbc_lblElephantServer.gridy = 0;
		getContentPane().add( lblElephantServer, gbc_lblElephantServer );

		final GridBagConstraints gbc_lblElephantServerStatus = new GridBagConstraints();
		gbc_lblElephantServerStatus.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblElephantServerStatus.weightx = 1.0;
		gbc_lblElephantServerStatus.weighty = 1.0;
		gbc_lblElephantServerStatus.gridx = 1;
		gbc_lblElephantServerStatus.gridy = 0;
		getContentPane().add( lblElephantServerStatus, gbc_lblElephantServerStatus );

		final GridBagConstraints gbc_lblElephantServerAddress = new GridBagConstraints();
		gbc_lblElephantServerAddress.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblElephantServerAddress.weighty = 1.0;
		gbc_lblElephantServerAddress.weightx = 1.0;
		gbc_lblElephantServerAddress.gridx = 2;
		gbc_lblElephantServerAddress.gridy = 0;
		getContentPane().add( lblElephantServerAddress, gbc_lblElephantServerAddress );

		final JLabel lblRabbitMQ = new JLabel( "RabbitMQ" );
		final GridBagConstraints gbc_lblRabbitMQ = new GridBagConstraints();
		gbc_lblRabbitMQ.weighty = 1.0;
		gbc_lblRabbitMQ.insets = new Insets( 0, 0, 0, 5 );
		gbc_lblRabbitMQ.gridx = 0;
		gbc_lblRabbitMQ.gridy = 1;
		getContentPane().add( lblRabbitMQ, gbc_lblRabbitMQ );

		final GridBagConstraints gbc_lblRabbitMQStatus = new GridBagConstraints();
		gbc_lblRabbitMQStatus.insets = new Insets( 0, 0, 0, 5 );
		gbc_lblRabbitMQStatus.gridx = 1;
		gbc_lblRabbitMQStatus.gridy = 1;
		getContentPane().add( lblRabbitMQStatus, gbc_lblRabbitMQStatus );

		final GridBagConstraints gbc_lblRabbitMQAddress = new GridBagConstraints();
		gbc_lblRabbitMQAddress.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblRabbitMQAddress.weighty = 1.0;
		gbc_lblRabbitMQAddress.weightx = 1.0;
		gbc_lblRabbitMQAddress.gridx = 2;
		gbc_lblRabbitMQAddress.gridy = 1;
		getContentPane().add( lblRabbitMQAddress, gbc_lblRabbitMQAddress );

		pack();
		setLocationRelativeTo( null );
	}

	public void updateElephantServerStatus( final ElephantStatus status, final String url ) throws IOException
	{
		lblElephantServerStatus.setIcon( getImageIcon( status ) );
		lblElephantServerStatus.setText( status.toString() );
		lblElephantServerAddress.setText( url );
	}

	public void updateRabbitMQStatus( final ElephantStatus status, final String url ) throws IOException
	{
		lblRabbitMQStatus.setIcon( getImageIcon( status ) );
		lblRabbitMQStatus.setText( status.toString() );
		lblRabbitMQAddress.setText( url );
	}

	public ImageIcon getImageIcon( final ElephantStatus status ) throws IOException
	{
		String iconPath = null;
		switch ( status )
		{
		case AVAILABLE:
			iconPath = "/org/elephant/bullet_green.png";
			break;
		case UNAVAILABLE:
			iconPath = "/org/elephant/bullet_red.png";
			break;
		default:
			break;
		}
		final Image img = ImageIO.read( getClass().getResource( iconPath ) );
		return new ImageIcon( img );
	}

}

package org.elephant.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.lang3.StringUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.AWTMixin;

import au.id.mcc.adapted.swing.SVGIcon;

public class ControlPanelDialog extends JDialog implements AWTMixin
{
	private static final long serialVersionUID = 1L;

	final JLabel lblElephantServerStatus = new JLabel( "Checking..." );

	final JLabel lblElephantServerAddress = new JLabel( "http://localhost:8080" );

	final JLabel lblRabbitMQStatus = new JLabel( "Checking..." );

	final JLabel lblRabbitMQAddress = new JLabel( "amqp://localhost:5672" );

	private final static String COLAB_SVG_URL = "https://colab.research.google.com/assets/colab-badge.svg";

	private final static String COLAB_NOTEBOOK_URL = "https://colab.research.google.com/github/elephant-track/elephant-server/blob/main/elephant_server.ipynb";

	private final static String ELEPHANT_DOCS_URL = "https://elephant-track.github.io/#/v0.1/?id=setting-up-the-elephant-server";

	private final JButton btnHelp = new JButton( "Help" );

	private final JButton btnColab = new JButton( "" );

	private final JTable gpuTable = new JTable();

	private final JScrollPane scrollPane = new JScrollPane();

	public ControlPanelDialog()
	{
		setTitle( "ELEPHANT Control Panel" );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 1, 1 };
		gridBagLayout.rowHeights = new int[] { 1, 1, 1 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, 1.0 };
		getContentPane().setLayout( gridBagLayout );

		{
			final JLabel lblElephantServer = new JLabel( "ELEPHANT server" );
			final GridBagConstraints gbc_lblElephantServer = new GridBagConstraints();
			gbc_lblElephantServer.weighty = 1.0;
			gbc_lblElephantServer.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblElephantServer.gridx = 0;
			gbc_lblElephantServer.gridy = 0;
			getContentPane().add( lblElephantServer, gbc_lblElephantServer );
		}

		{
			final GridBagConstraints gbc_lblElephantServerStatus = new GridBagConstraints();
			gbc_lblElephantServerStatus.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblElephantServerStatus.weightx = 1.0;
			gbc_lblElephantServerStatus.weighty = 1.0;
			gbc_lblElephantServerStatus.gridx = 1;
			gbc_lblElephantServerStatus.gridy = 0;
			getContentPane().add( lblElephantServerStatus, gbc_lblElephantServerStatus );
		}

		{
			final GridBagConstraints gbc_lblElephantServerAddress = new GridBagConstraints();
			gbc_lblElephantServerAddress.insets = new Insets( 5, 5, 5, 0 );
			gbc_lblElephantServerAddress.weighty = 1.0;
			gbc_lblElephantServerAddress.weightx = 1.0;
			gbc_lblElephantServerAddress.gridx = 2;
			gbc_lblElephantServerAddress.gridy = 0;
			getContentPane().add( lblElephantServerAddress, gbc_lblElephantServerAddress );
		}

		{
			final JLabel lblRabbitMQ = new JLabel( "RabbitMQ" );
			final GridBagConstraints gbc_lblRabbitMQ = new GridBagConstraints();
			gbc_lblRabbitMQ.weighty = 1.0;
			gbc_lblRabbitMQ.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblRabbitMQ.gridx = 0;
			gbc_lblRabbitMQ.gridy = 1;
			getContentPane().add( lblRabbitMQ, gbc_lblRabbitMQ );
		}

		{
			final GridBagConstraints gbc_lblRabbitMQStatus = new GridBagConstraints();
			gbc_lblRabbitMQStatus.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblRabbitMQStatus.gridx = 1;
			gbc_lblRabbitMQStatus.gridy = 1;
			getContentPane().add( lblRabbitMQStatus, gbc_lblRabbitMQStatus );
		}

		{
			final GridBagConstraints gbc_lblRabbitMQAddress = new GridBagConstraints();
			gbc_lblRabbitMQAddress.insets = new Insets( 5, 5, 5, 0 );
			gbc_lblRabbitMQAddress.weighty = 1.0;
			gbc_lblRabbitMQAddress.weightx = 1.0;
			gbc_lblRabbitMQAddress.gridx = 2;
			gbc_lblRabbitMQAddress.gridy = 1;
			getContentPane().add( lblRabbitMQAddress, gbc_lblRabbitMQAddress );
		}
		{
			gpuTable.getTableHeader().setDefaultRenderer( new SimpleHeaderRenderer() );
			gpuTable.setPreferredScrollableViewportSize(
					new Dimension(
							gpuTable.getPreferredSize().width,
							gpuTable.getRowHeight() * ( gpuTable.getRowCount() + 1 ) ) );
			gpuTable.setEnabled( false );
			gpuTable.setBackground( UIManager.getColor( "control" ) );
			gpuTable.setRowSelectionAllowed( false );
			gpuTable.setModel( new DefaultTableModel(
					new Object[][] {
							{ "-", "-", "-" },
					},
					new String[] {
							"GPU", "Name", "Memory-Usage"
					} )
			{
				private static final long serialVersionUID = 1L;

				Class< ? >[] columnTypes = new Class[] {
						String.class, String.class, String.class
				};

				@Override
				public Class< ? > getColumnClass( int columnIndex )
				{
					return columnTypes[ columnIndex ];
				}

				boolean[] columnEditables = new boolean[] {
						false, false, false
				};

				@Override
				public boolean isCellEditable( int row, int column )
				{
					return columnEditables[ column ];
				}
			} );
			scrollPane.setBackground( UIManager.getColor( "control" ) );
			scrollPane.setViewportView( gpuTable );
			final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.weighty = 1.0;
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridwidth = 3;
			gbc_scrollPane.insets = new Insets( 0, 0, 5, 0 );
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			getContentPane().add( scrollPane, gbc_scrollPane );
		}

		{
			final GridBagConstraints gbc_btnHelp = new GridBagConstraints();
			gbc_btnHelp.insets = new Insets( 0, 0, 0, 5 );
			gbc_btnHelp.gridx = 0;
			gbc_btnHelp.gridy = 3;
			btnHelp.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent event )
				{
					try
					{
						openBrowser( ELEPHANT_DOCS_URL );
					}
					catch ( URISyntaxException | IOException e )
					{
						e.printStackTrace();
					}
				}
			} );
			getContentPane().add( btnHelp, gbc_btnHelp );
		}
		{
			SVGIcon svgIcon = null;
			try
			{
				svgIcon = new SVGIcon( COLAB_SVG_URL );
			}
			catch ( final TranscoderException e )
			{
				e.printStackTrace();
			}
			btnColab.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent event )
				{
					try
					{
						openBrowser( COLAB_NOTEBOOK_URL );
					}
					catch ( URISyntaxException | IOException e )
					{
						e.printStackTrace();
					}
				}
			} );
			btnColab.setBorderPainted( false );
			btnColab.setBorder( null );
			btnColab.setMargin( new Insets( 0, 0, 0, 0 ) );
			btnColab.setContentAreaFilled( false );
			btnColab.setIcon( svgIcon );

			final GridBagConstraints gbc_btnColab = new GridBagConstraints();
			gbc_btnColab.insets = new Insets( 5, 5, 0, 0 );
			gbc_btnColab.weighty = 1.0;
			gbc_btnColab.weightx = 1.0;
			gbc_btnColab.gridx = 2;
			gbc_btnColab.gridy = 3;
			getContentPane().add( btnColab, gbc_btnColab );
		}

		pack();
		setLocationRelativeTo( null );
	}

	public void updateElephantServerStatus( final ElephantStatus status, final String url ) throws IOException
	{
		lblElephantServerStatus.setIcon( getImageIcon( status ) );
		lblElephantServerStatus.setText( StringUtils.capitalize( status.toString().toLowerCase() ) );
		lblElephantServerAddress.setText( url );
	}

	public void updateRabbitMQStatus( final ElephantStatus status, final String url ) throws IOException
	{
		lblRabbitMQStatus.setIcon( getImageIcon( status ) );
		lblRabbitMQStatus.setText( StringUtils.capitalize( status.toString().toLowerCase() ) );
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

	class SimpleHeaderRenderer extends JLabel implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public SimpleHeaderRenderer()
		{
			setBorder( BorderFactory.createEtchedBorder() );
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column )
		{
			setText( value.toString() );
			return this;
		}

	}

}

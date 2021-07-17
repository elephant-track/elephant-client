package org.elephant.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.lang3.StringUtils;
import org.elephant.actions.ElephantStatusService.ElephantStatus;
import org.elephant.actions.mixins.AWTMixin;
import org.elephant.swing.IntSpinner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import au.id.mcc.adapted.swing.SVGIcon;
import bdv.util.BoundedValue;

public class ControlPanelDialog extends JDialog implements AWTMixin
{
	private static final long serialVersionUID = 1L;

	private static final JSch jsch = new JSch();

	private final Set< Session > sessionSet = new HashSet<>();

	private final JLabel lblElephantServerStatus = new JLabel( "Checking..." );

	private final JLabel lblElephantServerAddress = new JLabel( "http://localhost:8080" );

	private final JLabel lblElephantServerErrorMessage = new JLabel( ElephantServerStateManager.NO_ERROR_MESSAGE );

	private final JLabel lblRabbitMQStatus = new JLabel( "Checking..." );

	private final JLabel lblRabbitMQAddress = new JLabel( "amqp://localhost:5672" );

	private final JLabel lblRabbitMQErrorMessage = new JLabel( ElephantServerStateManager.NO_ERROR_MESSAGE );

	private final static String COLAB_SVG_URL = "https://colab.research.google.com/assets/colab-badge.svg";

	private final static String COLAB_NOTEBOOK_URL = "https://colab.research.google.com/github/elephant-track/elephant-server/blob/main/elephant_server.ipynb";

	private final static String ELEPHANT_DOCS_URL = "https://elephant-track.github.io/#/v0.1/?id=setting-up-the-elephant-server";

	private final JButton btnHelp = new JButton( "Help" );

	private final JButton btnColab = new JButton( "" );

	private final JTable tableGpu = new JTable();

	private static final String[] DEFAULT_ROW_VALUES_GPU = new String[] { "-", "-", "-" };

	private final GpuTableModel gpuTableModel = new GpuTableModel(
			new String[][] { DEFAULT_ROW_VALUES_GPU },
			new String[] {
					"GPU ID", "Name", "Memory-Usage"
			} );

	private final JTable tablePortForward = new JTable();

	private static final Object[] DEFAULT_ROW_VALUES_PORT_FORWARD = new Object[] { "-", "-", null };

	private final PortForwardTableModel portForwardTableModel = new PortForwardTableModel(
			new Object[][] { DEFAULT_ROW_VALUES_PORT_FORWARD },
			new String[] { "SSH host", "Forwarded ports", "" } );

	private final JTextField textFieldSshUser = new JTextField( "root" );

	private final JTextField textFieldSshHost = new JTextField( "0.tcp.ngrok.io" );

	private final IntSpinner spinnerSshPort = new IntSpinner( new BoundedValue( 1, 65535, 22 ), "#" );

	private final IntSpinner spinnerLocalPort = new IntSpinner( new BoundedValue( 1, 65535, 8080 ), "#" );

	private final JTextField textFieldRemoteHost = new JTextField( "localhost" );

	private final IntSpinner spinnerRemotePort = new IntSpinner( new BoundedValue( 1, 65535, 80 ), "#" );

	final JButton btnDelete = new JButton( "Delete" );

	private class GpuTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;

		private final Class< ? >[] columnTypes = new Class[] {
				String.class, String.class, String.class
		};

		public GpuTableModel( Object[][] data, Object[] columnNames )
		{
			super( data, columnNames );
		}

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
	}

	private class PortForwardTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;

		private final Class< ? >[] columnTypes = new Class[] { String.class, String.class, JButton.class };

		public PortForwardTableModel( Object[][] data, Object[] columnNames )
		{
			super( data, columnNames );
		}

		@Override
		public Class< ? > getColumnClass( int columnIndex )
		{
			return columnTypes[ columnIndex ];
		}

		boolean[] columnEditables = new boolean[] {
				false, false, true
		};

		@Override
		public boolean isCellEditable( int row, int column )
		{
			return columnEditables[ column ];
		}
	}

	private final static int[] MIN_COLUMN_WIDTHS_PORT_FORWARD = { 200, 100, 50 };

	public ControlPanelDialog()
	{
		textFieldRemoteHost.setColumns( 10 );
		setTitle( "ELEPHANT Control Panel" );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 50, 200, 50 };
		gridBagLayout.rowHeights = new int[] { 20, 10, 20, 10, 40, 20, 20, 20, 20 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		getContentPane().setLayout( gridBagLayout );

		{
			final JLabel lblElephantServer = new JLabel( "ELEPHANT server" );
			final GridBagConstraints gbc_lblElephantServer = new GridBagConstraints();
			gbc_lblElephantServer.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblElephantServer.gridx = 0;
			gbc_lblElephantServer.gridy = 0;
			getContentPane().add( lblElephantServer, gbc_lblElephantServer );
		}

		{
			final GridBagConstraints gbc_lblElephantServerStatus = new GridBagConstraints();
			gbc_lblElephantServerStatus.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblElephantServerStatus.gridx = 1;
			gbc_lblElephantServerStatus.gridy = 0;
			getContentPane().add( lblElephantServerStatus, gbc_lblElephantServerStatus );
		}

		{
			final GridBagConstraints gbc_lblElephantServerAddress = new GridBagConstraints();
			gbc_lblElephantServerAddress.insets = new Insets( 5, 5, 5, 0 );
			gbc_lblElephantServerAddress.gridx = 2;
			gbc_lblElephantServerAddress.gridy = 0;
			getContentPane().add( lblElephantServerAddress, gbc_lblElephantServerAddress );
		}

		{
			final GridBagConstraints gbc_lblElephantServerErrorMessage = new GridBagConstraints();
			gbc_lblElephantServerErrorMessage.gridwidth = 2;
			gbc_lblElephantServerErrorMessage.insets = new Insets( 0, 5, 5, 0 );
			gbc_lblElephantServerErrorMessage.gridx = 1;
			gbc_lblElephantServerErrorMessage.gridy = 1;
			getContentPane().add( lblElephantServerErrorMessage, gbc_lblElephantServerErrorMessage );
		}

		{
			final JLabel lblRabbitMQ = new JLabel( "RabbitMQ" );
			final GridBagConstraints gbc_lblRabbitMQ = new GridBagConstraints();
			gbc_lblRabbitMQ.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblRabbitMQ.gridx = 0;
			gbc_lblRabbitMQ.gridy = 2;
			getContentPane().add( lblRabbitMQ, gbc_lblRabbitMQ );
		}

		{
			final GridBagConstraints gbc_lblRabbitMQStatus = new GridBagConstraints();
			gbc_lblRabbitMQStatus.insets = new Insets( 5, 5, 5, 5 );
			gbc_lblRabbitMQStatus.gridx = 1;
			gbc_lblRabbitMQStatus.gridy = 2;
			getContentPane().add( lblRabbitMQStatus, gbc_lblRabbitMQStatus );
		}

		{
			final GridBagConstraints gbc_lblRabbitMQAddress = new GridBagConstraints();
			gbc_lblRabbitMQAddress.insets = new Insets( 5, 5, 5, 0 );
			gbc_lblRabbitMQAddress.gridx = 2;
			gbc_lblRabbitMQAddress.gridy = 2;
			getContentPane().add( lblRabbitMQAddress, gbc_lblRabbitMQAddress );
		}

		{
			final GridBagConstraints gbc_lblRabbitMQServerErrorMessage = new GridBagConstraints();
			gbc_lblRabbitMQServerErrorMessage.gridwidth = 2;
			gbc_lblRabbitMQServerErrorMessage.insets = new Insets( 0, 5, 5, 0 );
			gbc_lblRabbitMQServerErrorMessage.gridx = 1;
			gbc_lblRabbitMQServerErrorMessage.gridy = 3;
			getContentPane().add( lblRabbitMQErrorMessage, gbc_lblRabbitMQServerErrorMessage );
		}

		{
			tableGpu.getTableHeader().setDefaultRenderer( new SimpleHeaderRenderer() );
			tableGpu.setPreferredScrollableViewportSize(
					new Dimension(
							tableGpu.getPreferredSize().width,
							tableGpu.getRowHeight() * ( tableGpu.getRowCount() + 1 ) ) );
			tableGpu.setEnabled( false );
			tableGpu.setBackground( UIManager.getColor( "control" ) );
			tableGpu.setRowSelectionAllowed( false );
			tableGpu.setModel( gpuTableModel );
			final JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBackground( UIManager.getColor( "control" ) );
			scrollPane.setViewportView( tableGpu );
			final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridwidth = 3;
			gbc_scrollPane.insets = new Insets( 0, 0, 5, 0 );
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 4;
			getContentPane().add( scrollPane, gbc_scrollPane );
		}

		{
			final GridBagConstraints gbc_btnHelp = new GridBagConstraints();
			btnHelp.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
			gbc_btnHelp.insets = new Insets( 5, 5, 5, 5 );
			gbc_btnHelp.gridx = 0;
			gbc_btnHelp.gridy = 5;
			btnHelp.addActionListener( event -> {
				try
				{
					openBrowser( ELEPHANT_DOCS_URL );
				}
				catch ( final URISyntaxException | IOException e )
				{
					handleError( e );
				}
			} );
			getContentPane().add( btnHelp, gbc_btnHelp );
		}

		{
			final JPanel panelColab = new JPanel();
			final GridBagConstraints gbc_panelColab = new GridBagConstraints();
			gbc_panelColab.gridwidth = 2;
			gbc_panelColab.fill = GridBagConstraints.BOTH;
			gbc_panelColab.insets = new Insets( 5, 5, 5, 0 );
			gbc_panelColab.gridx = 1;
			gbc_panelColab.gridy = 5;
			getContentPane().add( panelColab, gbc_panelColab );

			final GridBagLayout gbl_panelColab = new GridBagLayout();
			gbl_panelColab.columnWidths = new int[] { 1, 1 };
			gbl_panelColab.rowHeights = new int[] { 1 };
			gbl_panelColab.columnWeights = new double[] { 1.0, 1.0 };
			gbl_panelColab.rowWeights = new double[] { 1.0 };
			panelColab.setLayout( gbl_panelColab );

			{
				final JLabel lblColabSetup = new JLabel( "Set up in Colab ->" );
				final GridBagConstraints gbc_lblColabSetup = new GridBagConstraints();
				gbc_lblColabSetup.anchor = GridBagConstraints.EAST;
				gbc_lblColabSetup.insets = new Insets( 5, 5, 5, 5 );
				gbc_lblColabSetup.gridx = 0;
				gbc_lblColabSetup.gridy = 0;
				panelColab.add( lblColabSetup, gbc_lblColabSetup );
			}

			{
				SVGIcon svgIcon = null;
				try
				{
					svgIcon = new SVGIcon( COLAB_SVG_URL );
				}
				catch ( final TranscoderException e )
				{
					handleError( e );
				}
				btnColab.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
				btnColab.addActionListener( event -> {
					try
					{
						openBrowser( COLAB_NOTEBOOK_URL );
					}
					catch ( final URISyntaxException | IOException e )
					{
						handleError( e );
					}
				} );
				btnColab.setBorderPainted( false );
				btnColab.setBorder( null );
				btnColab.setMargin( new Insets( 0, 0, 0, 0 ) );
				btnColab.setContentAreaFilled( false );
				btnColab.setIcon( svgIcon );

				final GridBagConstraints gbc_btnColab = new GridBagConstraints();
				gbc_btnColab.anchor = GridBagConstraints.WEST;
				gbc_btnColab.insets = new Insets( 5, 0, 5, 5 );
				gbc_btnColab.gridx = 1;
				gbc_btnColab.gridy = 0;

				panelColab.add( btnColab, gbc_btnColab );
			}
		}

		{
			final JPanel panelSshUser = new JPanel();
			panelSshUser.setBorder( null );
			final GridBagConstraints gbc_panelSshUser = new GridBagConstraints();
			gbc_panelSshUser.insets = new Insets( 5, 5, 5, 5 );
			gbc_panelSshUser.fill = GridBagConstraints.BOTH;
			gbc_panelSshUser.gridx = 0;
			gbc_panelSshUser.gridy = 6;
			final GridBagLayout gbl_panelSshUser = new GridBagLayout();
			gbl_panelSshUser.columnWidths = new int[] { 1, 1 };
			gbl_panelSshUser.rowHeights = new int[] { 1 };
			gbl_panelSshUser.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panelSshUser.rowWeights = new double[] { 1.0 };
			panelSshUser.setLayout( gbl_panelSshUser );
			{
				final GridBagConstraints gbc_lblSshUser = new GridBagConstraints();
				gbc_lblSshUser.anchor = GridBagConstraints.WEST;
				gbc_lblSshUser.insets = new Insets( 5, 5, 5, 0 );
				gbc_lblSshUser.gridx = 0;
				gbc_lblSshUser.gridy = 0;
				final JLabel label = new JLabel( "SSH user:" );
				panelSshUser.add( label, gbc_lblSshUser );
			}
			{
				final GridBagConstraints gbc_textFieldSshUser = new GridBagConstraints();
				gbc_textFieldSshUser.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldSshUser.anchor = GridBagConstraints.WEST;
				gbc_textFieldSshUser.insets = new Insets( 5, 0, 5, 5 );
				gbc_textFieldSshUser.gridx = 1;
				gbc_textFieldSshUser.gridy = 0;
				panelSshUser.add( textFieldSshUser, gbc_textFieldSshUser );
			}
			getContentPane().add( panelSshUser, gbc_panelSshUser );
		}

		{
			final JPanel panelSshHost = new JPanel();
			panelSshHost.setBorder( null );
			final GridBagConstraints gbc_panelSshHost = new GridBagConstraints();
			gbc_panelSshHost.insets = new Insets( 5, 5, 5, 5 );
			gbc_panelSshHost.fill = GridBagConstraints.BOTH;
			gbc_panelSshHost.gridx = 1;
			gbc_panelSshHost.gridy = 6;
			final GridBagLayout gbl_panelSshHost = new GridBagLayout();
			gbl_panelSshHost.columnWidths = new int[] { 1, 1 };
			gbl_panelSshHost.rowHeights = new int[] { 1 };
			gbl_panelSshHost.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panelSshHost.rowWeights = new double[] { 1.0 };
			panelSshHost.setLayout( gbl_panelSshHost );
			{
				final GridBagConstraints gbc_lblSshHost = new GridBagConstraints();
				gbc_lblSshHost.anchor = GridBagConstraints.WEST;
				gbc_lblSshHost.insets = new Insets( 5, 5, 5, 0 );
				gbc_lblSshHost.gridx = 0;
				gbc_lblSshHost.gridy = 0;
				final JLabel label = new JLabel( "SSH host:" );
				panelSshHost.add( label, gbc_lblSshHost );
			}
			{
				final GridBagConstraints gbc_textFieldSshHost = new GridBagConstraints();
				gbc_textFieldSshHost.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldSshHost.anchor = GridBagConstraints.WEST;
				gbc_textFieldSshHost.insets = new Insets( 5, 0, 5, 5 );
				gbc_textFieldSshHost.gridx = 1;
				gbc_textFieldSshHost.gridy = 0;
				panelSshHost.add( textFieldSshHost, gbc_textFieldSshHost );
			}
			getContentPane().add( panelSshHost, gbc_panelSshHost );
		}

		{
			final JPanel panelSshPort = new JPanel();
			panelSshPort.setBorder( null );
			final GridBagConstraints gbc_panelSshPort = new GridBagConstraints();
			gbc_panelSshPort.insets = new Insets( 5, 5, 5, 0 );
			gbc_panelSshPort.fill = GridBagConstraints.BOTH;
			gbc_panelSshPort.gridx = 2;
			gbc_panelSshPort.gridy = 6;
			final GridBagLayout gbl_panelSshPort = new GridBagLayout();
			gbl_panelSshPort.columnWidths = new int[] { 1, 1 };
			gbl_panelSshPort.rowHeights = new int[] { 1 };
			gbl_panelSshPort.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panelSshPort.rowWeights = new double[] { 1.0 };
			panelSshPort.setLayout( gbl_panelSshPort );
			{
				final GridBagConstraints gbc_lblSshPort = new GridBagConstraints();
				gbc_lblSshPort.anchor = GridBagConstraints.WEST;
				gbc_lblSshPort.insets = new Insets( 5, 5, 5, 0 );
				gbc_lblSshPort.gridx = 0;
				gbc_lblSshPort.gridy = 0;
				final JLabel label = new JLabel( "SSH port:" );
				panelSshPort.add( label, gbc_lblSshPort );
			}
			{
				final GridBagConstraints gbc_textFieldSshPort = new GridBagConstraints();
				gbc_textFieldSshPort.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldSshPort.anchor = GridBagConstraints.WEST;
				gbc_textFieldSshPort.insets = new Insets( 5, 0, 5, 5 );
				gbc_textFieldSshPort.gridx = 1;
				gbc_textFieldSshPort.gridy = 0;
				panelSshPort.add( spinnerSshPort, gbc_textFieldSshPort );
			}
			getContentPane().add( panelSshPort, gbc_panelSshPort );
		}

		{
			final JPanel panelLocalPort = new JPanel();
			panelLocalPort.setBorder( null );
			final GridBagConstraints gbc_panelLocalPort = new GridBagConstraints();
			gbc_panelLocalPort.insets = new Insets( 5, 5, 5, 5 );
			gbc_panelLocalPort.fill = GridBagConstraints.BOTH;
			gbc_panelLocalPort.gridx = 0;
			gbc_panelLocalPort.gridy = 7;
			final GridBagLayout gbl_panelLocalPort = new GridBagLayout();
			gbl_panelLocalPort.columnWidths = new int[] { 1, 1 };
			gbl_panelLocalPort.rowHeights = new int[] { 1 };
			gbl_panelLocalPort.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panelLocalPort.rowWeights = new double[] { 1.0 };
			panelLocalPort.setLayout( gbl_panelLocalPort );
			{
				final GridBagConstraints gbc_lblLocalPort = new GridBagConstraints();
				gbc_lblLocalPort.anchor = GridBagConstraints.WEST;
				gbc_lblLocalPort.insets = new Insets( 5, 5, 5, 0 );
				gbc_lblLocalPort.gridx = 0;
				gbc_lblLocalPort.gridy = 0;
				final JLabel label = new JLabel( "Local port:" );
				panelLocalPort.add( label, gbc_lblLocalPort );
			}
			{
				final GridBagConstraints gbc_spinnerLocalPort = new GridBagConstraints();
				gbc_spinnerLocalPort.fill = GridBagConstraints.HORIZONTAL;
				gbc_spinnerLocalPort.anchor = GridBagConstraints.WEST;
				gbc_spinnerLocalPort.insets = new Insets( 5, 0, 5, 5 );
				gbc_spinnerLocalPort.gridx = 1;
				gbc_spinnerLocalPort.gridy = 0;
				panelLocalPort.add( spinnerLocalPort, gbc_spinnerLocalPort );
			}
			getContentPane().add( panelLocalPort, gbc_panelLocalPort );
		}

		{
			final JPanel panelRemoteHost = new JPanel();
			panelRemoteHost.setBorder( null );
			final GridBagConstraints gbc_panelRemoteHost = new GridBagConstraints();
			gbc_panelRemoteHost.insets = new Insets( 5, 5, 5, 5 );
			gbc_panelRemoteHost.fill = GridBagConstraints.BOTH;
			gbc_panelRemoteHost.gridx = 1;
			gbc_panelRemoteHost.gridy = 7;
			final GridBagLayout gbl_panelRemoteHost = new GridBagLayout();
			gbl_panelRemoteHost.columnWidths = new int[] { 1, 1 };
			gbl_panelRemoteHost.rowHeights = new int[] { 1 };
			gbl_panelRemoteHost.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panelRemoteHost.rowWeights = new double[] { 1.0 };
			panelRemoteHost.setLayout( gbl_panelRemoteHost );
			{
				final GridBagConstraints gbc_lblRemoteHost = new GridBagConstraints();
				gbc_lblRemoteHost.anchor = GridBagConstraints.WEST;
				gbc_lblRemoteHost.insets = new Insets( 5, 5, 5, 0 );
				gbc_lblRemoteHost.gridx = 0;
				gbc_lblRemoteHost.gridy = 0;
				final JLabel label = new JLabel( "Remote host:" );
				panelRemoteHost.add( label, gbc_lblRemoteHost );
			}
			{
				final GridBagConstraints gbc_textFieldRemoteHost = new GridBagConstraints();
				gbc_textFieldRemoteHost.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldRemoteHost.anchor = GridBagConstraints.WEST;
				gbc_textFieldRemoteHost.insets = new Insets( 5, 0, 5, 5 );
				gbc_textFieldRemoteHost.gridx = 1;
				gbc_textFieldRemoteHost.gridy = 0;
				panelRemoteHost.add( textFieldRemoteHost, gbc_textFieldRemoteHost );
			}
			getContentPane().add( panelRemoteHost, gbc_panelRemoteHost );
		}

		{
			final JPanel panelRemotePort = new JPanel();
			panelRemotePort.setBorder( null );
			final GridBagConstraints gbc_panelRemotePort = new GridBagConstraints();
			gbc_panelRemotePort.insets = new Insets( 5, 5, 5, 0 );
			gbc_panelRemotePort.fill = GridBagConstraints.BOTH;
			gbc_panelRemotePort.gridx = 2;
			gbc_panelRemotePort.gridy = 7;
			final GridBagLayout gbl_panelRemotePort = new GridBagLayout();
			gbl_panelRemotePort.columnWidths = new int[] { 1, 1 };
			gbl_panelRemotePort.rowHeights = new int[] { 1 };
			gbl_panelRemotePort.columnWeights = new double[] { 0.0, 1.0 };
			gbl_panelRemotePort.rowWeights = new double[] { 1.0 };
			panelRemotePort.setLayout( gbl_panelRemotePort );
			{
				final GridBagConstraints gbc_lblRemotePort = new GridBagConstraints();
				gbc_lblRemotePort.anchor = GridBagConstraints.WEST;
				gbc_lblRemotePort.insets = new Insets( 5, 5, 5, 0 );
				gbc_lblRemotePort.gridx = 0;
				gbc_lblRemotePort.gridy = 0;
				final JLabel label = new JLabel( "Remote port:" );
				panelRemotePort.add( label, gbc_lblRemotePort );
			}
			{
				final GridBagConstraints gbc_textFieldRemotePort = new GridBagConstraints();
				gbc_textFieldRemotePort.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldRemotePort.anchor = GridBagConstraints.WEST;
				gbc_textFieldRemotePort.insets = new Insets( 5, 0, 5, 5 );
				gbc_textFieldRemotePort.gridx = 1;
				gbc_textFieldRemotePort.gridy = 0;
				panelRemotePort.add( spinnerRemotePort, gbc_textFieldRemotePort );
			}
			getContentPane().add( panelRemotePort, gbc_panelRemotePort );
		}

		{
			final JButton btnPortForward = new JButton( "Add Port Forward" );
			btnPortForward.addActionListener( event -> {
				try
				{
					spinnerSshPort.commitEdit();
				}
				catch ( final ParseException e )
				{
					handleError( e );
				}
				try
				{
					final String sshUser = textFieldSshUser.getText();
					final String sshHost = textFieldSshHost.getText();
					final int sshPort = ( Integer ) spinnerSshPort.getValue();
					final Session session = sessionSet.stream()
							.filter( s -> s.getUserName().equals( sshUser ) &&
									s.getHost().equals( sshHost ) &&
									s.getPort() == sshPort )
							.findFirst()
							.orElse( jsch.getSession( textFieldSshUser.getText(), textFieldSshHost.getText(), sshPort ) );
					sessionSet.add( session );
					if ( !session.isConnected() )
					{
						final UserInfo userInfo = new JschUserInfo();
						session.setUserInfo( userInfo );
						session.connect();
					}
					final int localPort = ( Integer ) spinnerLocalPort.getValue();
					final String remoteHost = textFieldRemoteHost.getText();
					final int remotePort = ( Integer ) spinnerRemotePort.getValue();
					final int allocatedPort = session.setPortForwardingL( localPort, remoteHost, remotePort );
					if ( localPort != allocatedPort )
					{
						System.out.println( "Specified local port " + localPort + " and the allocated port " + allocatedPort + " are different." );
					}
					System.out.println( "localhost:" + allocatedPort + " -> " + remoteHost + ":" + remotePort );
					updatePortForwardTableModel();
				}
				catch ( final JSchException e )
				{
					handleError( e );
				}
			} );
			final GridBagConstraints gbc_btnPortForward = new GridBagConstraints();
			gbc_btnPortForward.insets = new Insets( 5, 5, 5, 5 );
			gbc_btnPortForward.gridx = 0;
			gbc_btnPortForward.gridy = 8;
			getContentPane().add( btnPortForward, gbc_btnPortForward );
		}
		{
			tablePortForward.setDefaultRenderer( JButton.class,
					new JButtonRenderer( tablePortForward.getDefaultRenderer( JButton.class ) ) );
			tablePortForward.setDefaultEditor( JButton.class,
					new JButtonEditor( tablePortForward.getDefaultEditor( JButton.class ) ) );
			btnDelete.addActionListener( ( ActionListener ) tablePortForward.getDefaultEditor( JButton.class ) );
			tablePortForward.getTableHeader().setDefaultRenderer( new SimpleHeaderRenderer() );
			tablePortForward.setPreferredScrollableViewportSize(
					new Dimension(
							tablePortForward.getPreferredSize().width,
							tablePortForward.getRowHeight() * ( tablePortForward.getRowCount() + 1 ) ) );
			tablePortForward.setBackground( UIManager.getColor( "control" ) );
			tablePortForward.setRowSelectionAllowed( false );
			tablePortForward.setModel( portForwardTableModel );
			for ( int i = 0; i < MIN_COLUMN_WIDTHS_PORT_FORWARD.length; i++ )
			{
				final TableColumn column = tablePortForward.getColumnModel().getColumn( i );
				column.setMinWidth( MIN_COLUMN_WIDTHS_PORT_FORWARD[ i ] );
				column.setPreferredWidth( MIN_COLUMN_WIDTHS_PORT_FORWARD[ i ] );
			}
			final JScrollPane scrollPanePortForward = new JScrollPane();
			scrollPanePortForward.setBackground( UIManager.getColor( "control" ) );
			scrollPanePortForward.setViewportView( tablePortForward );
			final GridBagConstraints gbc_scrollPanePortForward = new GridBagConstraints();
			gbc_scrollPanePortForward.insets = new Insets( 5, 5, 5, 5 );
			gbc_scrollPanePortForward.gridwidth = 2;
			gbc_scrollPanePortForward.fill = GridBagConstraints.BOTH;
			gbc_scrollPanePortForward.gridx = 1;
			gbc_scrollPanePortForward.gridy = 8;
			getContentPane().add( scrollPanePortForward, gbc_scrollPanePortForward );
		}

		new Thread( () -> {
			while ( true )
			{
				try
				{
					updatePortForwardTableModel();
					Thread.sleep( 5000 );
				}
				catch ( final InterruptedException | JSchException e )
				{
					handleError( e );
				}
			}
		} ).start();

		setSize( 800, 600 );

		setLocationRelativeTo( null );
	}

	public void updateElephantServerStatus( final ElephantStatus status, final String url, final String errorMessage ) throws IOException
	{
		lblElephantServerStatus.setIcon( getImageIcon( status ) );
		lblElephantServerStatus.setText( StringUtils.capitalize( status.toString().toLowerCase() ) );
		lblElephantServerAddress.setText( url );
		lblElephantServerErrorMessage.setText( errorMessage );
	}

	public void updateRabbitMQStatus( final ElephantStatus status, final String url, final String errorMessage ) throws IOException
	{
		lblRabbitMQStatus.setIcon( getImageIcon( status ) );
		lblRabbitMQStatus.setText( StringUtils.capitalize( status.toString().toLowerCase() ) );
		lblRabbitMQAddress.setText( url );
		lblRabbitMQErrorMessage.setText( errorMessage );
	}

	public ImageIcon getImageIcon( final ElephantStatus status ) throws IOException
	{
		String iconPath = "/org/elephant/bullet_gray.png";
		switch ( status )
		{
		case AVAILABLE:
			iconPath = "/org/elephant/bullet_green.png";
			break;
		case UNAVAILABLE:
			iconPath = "/org/elephant/bullet_red.png";
			break;
		case WARNING:
			iconPath = "/org/elephant/bullet_yellow.png";
			break;
		default:
			break;
		}
		final Image img = ImageIO.read( getClass().getResource( iconPath ) );
		return new ImageIcon( img );
	}

	public void updateGpuTableModel( final Collection< GPU > gpus )
	{
		// Clear all first
		gpuTableModel.setRowCount( 0 );
		// Add GPU data
		for ( final GPU gpu : gpus )
		{
			gpuTableModel.addRow( new String[] {
					String.valueOf( gpu.getId() ),
					gpu.getName(),
					String.format( "%.0fMiB / %.0fMiB", gpu.getUsedMemory(), gpu.getTotalMemory() )
			} );
		}
		if ( gpuTableModel.getRowCount() == 0 )
		{
			gpuTableModel.addRow( DEFAULT_ROW_VALUES_GPU );
		}
		SwingUtilities.invokeLater( () -> gpuTableModel.fireTableDataChanged() );
	}

	private void updatePortForwardTableModel() throws JSchException
	{
		// Clear all first
		portForwardTableModel.setRowCount( 0 );
		// Add rows
		for ( final Session session : sessionSet )
		{
			if ( session.isConnected() )
			{
				for ( final String portForwardString : session.getPortForwardingL() )
				{
					portForwardTableModel.addRow( new Object[] {
							session.getUserName() + "@" + session.getHost() + ":" + session.getPort(),
							portForwardString,
							btnDelete
					} );
				}
			}
		}
		if ( portForwardTableModel.getRowCount() == 0 )
		{
			portForwardTableModel.addRow( DEFAULT_ROW_VALUES_PORT_FORWARD );
		}
		SwingUtilities.invokeLater( () -> portForwardTableModel.fireTableDataChanged() );
	}

	private class SimpleHeaderRenderer extends JLabel implements TableCellRenderer
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

	private class JButtonRenderer implements TableCellRenderer
	{
		private final TableCellRenderer defaultRenderer;

		public JButtonRenderer( final TableCellRenderer renderer )
		{
			defaultRenderer = renderer;
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
		{
			if ( value instanceof Component )
				return ( Component ) value;
			return defaultRenderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
		}
	}

	private class JButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
	{
		private static final long serialVersionUID = -5170997571338622848L;

		private final TableCellEditor defaultEditor;

		private Object editorValue;

		public JButtonEditor( final TableCellEditor editor )
		{
			this.defaultEditor = editor;
		}

		@Override
		public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
		{
			editorValue = value;
			if ( value instanceof Component )
				return ( Component ) value;
			return defaultEditor.getTableCellEditorComponent( table, value, isSelected, row, column );
		}

		@Override
		public Object getCellEditorValue()
		{
			return editorValue;
		}

		@Override
		public void actionPerformed( ActionEvent event )
		{
			final int row = tablePortForward.convertRowIndexToModel( tablePortForward.getEditingRow() );
			fireEditingStopped();
			final String sshString = ( String ) tablePortForward.getValueAt( row, 0 );
			final String[] userHostPort = sshString.replace( "@", ":" ).split( ":" );
			final Session session = sessionSet.stream()
					.filter( s -> s.getUserName().equals( userHostPort[ 0 ] ) &&
							s.getHost().equals( userHostPort[ 1 ] ) &&
							s.getPort() == Integer.parseInt( userHostPort[ 2 ] ) )
					.findFirst()
					.orElse( null );
			if ( session != null && session.isConnected() )
			{
				final String portForwardString = ( String ) tablePortForward.getValueAt( row, 1 );
				try
				{
					session.delPortForwardingL( Integer.parseInt( portForwardString.split( ":" )[ 0 ] ) );
					updatePortForwardTableModel();
				}
				catch ( final JSchException e )
				{
					handleError( e );
				}
			}
		}

	}

	private void handleError( final Exception e )
	{
		SwingUtilities.invokeLater( () -> JOptionPane.showMessageDialog(
				ControlPanelDialog.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE ) );
	}

	private static class JschUserInfo implements UserInfo, UIKeyboardInteractive
	{
		@Override
		public String getPassword()
		{
			return passwd;
		}

		@Override
		public boolean promptYesNo( String str )
		{
			final Object[] options = { "yes", "no" };
			final int foo = JOptionPane.showOptionDialog( null,
					str,
					"Warning",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null, options, options[ 0 ] );
			return foo == 0;
		}

		String passwd;

		JTextField passwordField = new JPasswordField( 20 );

		@Override
		public String getPassphrase()
		{
			return null;
		}

		@Override
		public boolean promptPassphrase( String message )
		{
			return true;
		}

		@Override
		public boolean promptPassword( String message )
		{
			final Object[] ob = { passwordField };
			final int result =
					JOptionPane.showConfirmDialog( null, ob, message,
							JOptionPane.OK_CANCEL_OPTION );
			if ( result == JOptionPane.OK_OPTION )
			{
				passwd = passwordField.getText();
				return true;
			}
			else
			{
				return false;
			}
		}

		@Override
		public void showMessage( String message )
		{
			JOptionPane.showMessageDialog( null, message );
		}

		final GridBagConstraints gbc =
				new GridBagConstraints( 0, 0, 1, 1, 1, 1,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets( 0, 0, 0, 0 ), 0, 0 );

		private Container panel;

		@Override
		public String[] promptKeyboardInteractive( String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo )
		{
			panel = new JPanel();
			panel.setLayout( new GridBagLayout() );

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add( new JLabel( instruction ), gbc );
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			final JTextField[] texts = new JTextField[ prompt.length ];
			for ( int i = 0; i < prompt.length; i++ )
			{
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add( new JLabel( prompt[ i ] ), gbc );

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if ( echo[ i ] )
				{
					texts[ i ] = new JTextField( 20 );
				}
				else
				{
					texts[ i ] = new JPasswordField( 20 );
				}
				panel.add( texts[ i ], gbc );
				gbc.gridy++;
			}

			if ( JOptionPane.showConfirmDialog( null, panel,
					destination + ": " + name,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION )
			{
				final String[] response = new String[ prompt.length ];
				for ( int i = 0; i < prompt.length; i++ )
				{
					response[ i ] = texts[ i ].getText();
				}
				return response;
			}
			else
			{
				return null; // cancel
			}
		}
	}
}

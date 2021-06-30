package org.elephant.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.mastodon.ui.context.ContextChooserPanel;
import org.mastodon.views.context.ContextChooser;

public class ContextChooserDialog< V > extends JDialog
{
	private static final long serialVersionUID = 1L;

	private boolean isCanceled = true;

	public ContextChooserDialog( final ContextChooser< V > contextChooser )
	{
		setTitle( "Choose a context" );
		setModal( true );
		getContentPane().add( new ContextChooserPanel<>( contextChooser ), BorderLayout.CENTER );

		final JPanel panel = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panel.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		getContentPane().add( panel, BorderLayout.SOUTH );

		final JButton btnCancel = new JButton( "Cancel" );
		btnCancel.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setVisible( false );
			}
		} );
		panel.add( btnCancel );

		final JButton btnOk = new JButton( "Ok" );
		btnOk.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				isCanceled = false;
				setVisible( false );
			}
		} );
		panel.add( btnOk );
		pack();
		setLocationRelativeTo( null );
	}

	public boolean isCanceled()
	{
		return isCanceled;
	}
}

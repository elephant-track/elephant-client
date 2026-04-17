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

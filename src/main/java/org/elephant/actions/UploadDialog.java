/*******************************************************************************
 * Copyright (C) 2021, Ko Sugawara
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

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class UploadDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private final JProgressBar progressBar = new JProgressBar();

	private final JLabel lblText = new JLabel( "Uploading" );

	private final JButton btnCancel = new JButton( "Cancel" );

	private boolean isCancelled = false;

	public UploadDialog()
	{
		setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
		setTitle( "Upload" );

		progressBar.setValue( 0 );
		progressBar.setStringPainted( true );
		getContentPane().add( progressBar, BorderLayout.CENTER );

		lblText.setHorizontalAlignment( SwingConstants.CENTER );
		getContentPane().add( lblText, BorderLayout.NORTH );
		btnCancel.addActionListener( e -> isCancelled = true );

		getContentPane().add( btnCancel, BorderLayout.SOUTH );
		pack();
		setLocationRelativeTo( null );
	}

	public void setLabelText( final String text )
	{
		lblText.setText( text );
	}

	public void setProgressBarValue( final int value )
	{
		progressBar.setValue( value );
	}

	public boolean isCancelled()
	{
		return isCancelled;
	}

}

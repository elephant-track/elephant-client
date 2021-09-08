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
package org.elephant.swing;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

/**
 * Modified from https://stackoverflow.com/a/30684790
 * 
 * @author Ko Sugawara
 */
public class TextFieldPopup extends JTextField
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new <code>TextField</code>. A default model is created, the
	 * initial string is <code>null</code>, and the number of columns is set to 0.
	 */
	public TextFieldPopup()
	{
		super();
		addPopupMenu();
	}

	/**
	 * Constructs a new <code>TextField</code> initialized with the specified text.
	 * A default model is created and the number of columns is 0.
	 *
	 * @param text
	 *            the text to be displayed, or <code>null</code>
	 */
	public TextFieldPopup( String text )
	{
		super( text );
		addPopupMenu();
	}

	/**
	 * Constructs a new empty <code>TextField</code> with the specified number of
	 * columns. A default model is created and the initial string is set to
	 * <code>null</code>.
	 *
	 * @param columns
	 *            the number of columns to use to calculate the preferred width; if
	 *            columns is set to zero, the preferred width will be whatever
	 *            naturally results from the component implementation
	 */
	public TextFieldPopup( int columns )
	{
		super( columns );
		addPopupMenu();
	}

	/**
	 * Constructs a new <code>TextField</code> initialized with the specified text
	 * and columns. A default model is created.
	 *
	 * @param text
	 *            the text to be displayed, or <code>null</code>
	 * @param columns
	 *            the number of columns to use to calculate the preferred width; if
	 *            columns is set to zero, the preferred width will be whatever
	 *            naturally results from the component implementation
	 */
	public TextFieldPopup( String text, int columns )
	{
		super( columns );
		addPopupMenu();
	}

	/**
	 * Constructs a new <code>JTextField</code> that uses the given text storage
	 * model and the given number of columns. This is the constructor through which
	 * the other constructors feed. If the document is <code>null</code>, a default
	 * model is created.
	 *
	 * @param doc
	 *            the text storage to use; if this is <code>null</code>, a default
	 *            will be provided by calling the <code>createDefaultModel</code>
	 *            method
	 * @param text
	 *            the initial string to display, or <code>null</code>
	 * @param columns
	 *            the number of columns to use to calculate the preferred width
	 *            &gt;= 0; if <code>columns</code> is set to zero, the preferred
	 *            width will be whatever naturally results from the component
	 *            implementation
	 * @exception IllegalArgumentException
	 *                if <code>columns</code> &lt; 0
	 */
	public TextFieldPopup( Document doc, String text, int columns )
	{
		super( doc, text, columns );
		addPopupMenu();
	}

	private void addPopupMenu()
	{
		final JPopupMenu menu = new JPopupMenu();
		final Action cut = new DefaultEditorKit.CutAction();
		cut.putValue( Action.NAME, "Cut" );
		cut.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( "control X" ) );
		menu.add( cut );

		final Action copy = new DefaultEditorKit.CopyAction();
		copy.putValue( Action.NAME, "Copy" );
		copy.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( "control C" ) );
		menu.add( copy );

		final Action paste = new DefaultEditorKit.PasteAction();
		paste.putValue( Action.NAME, "Paste" );
		paste.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( "control V" ) );
		menu.add( paste );

		final Action selectAll = new SelectAll();
		menu.add( selectAll );
		setComponentPopupMenu( menu );
	}

	private class SelectAll extends TextAction
	{
		private static final long serialVersionUID = 1L;

		public SelectAll()
		{
			super( "Select All" );
			putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( "control S" ) );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			final JTextComponent component = getFocusedComponent();
			component.selectAll();
			component.requestFocusInWindow();
		}
	}
}

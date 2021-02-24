package org.elephant.actions.mixins;
/*
 * http://www.java2s.com/Code/Java/Swing-JFC/ExtensionofJTextPanethatallowstheusertoeasilyappendcoloredtexttothedocument.htm
 * Java Swing, 2nd Edition
 * By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
 * ISBN: 0-596-00408-7
 * Publisher: O'Reilly 
*/

// ColorPane.java
//A simple extension of JTextPane that allows the user to easily append
//colored text to the document.
//

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ColorPane extends JTextPane
{
	private static final long serialVersionUID = 1L;

	public void appendNaive( Color c, String s )
	{ // naive implementation
		// bad: instiantiates a new AttributeSet object on each call
		final SimpleAttributeSet aset = new SimpleAttributeSet();
		StyleConstants.setForeground( aset, c );

		final int len = getText().length();
		setCaretPosition( len ); // place caret at the end (with no selection)
		setCharacterAttributes( aset, false );
		replaceSelection( s ); // there is no selection, so inserts at caret
	}

	public void append( Color c, String s )
	{ // better implementation--uses
		// StyleContext
		final StyleContext sc = StyleContext.getDefaultStyleContext();
		final AttributeSet aset = sc.addAttribute( SimpleAttributeSet.EMPTY,
				StyleConstants.Foreground, c );

		final int len = getDocument().getLength(); // same value as
		// getText().length();
		setCaretPosition( len ); // place caret at the end (with no selection)
		setCharacterAttributes( aset, false );
		replaceSelection( s ); // there is no selection, so inserts at caret
	}

}

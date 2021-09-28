package org.elephant.actions.mixins;

import java.net.ConnectException;

public class ElephantConnectException extends ConnectException
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new ConnectException with the specified detail message as to why
	 * the connect error occurred. A detail message is a String that gives a
	 * specific description of this error.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public ElephantConnectException( final String msg )
	{
		super( msg );
	}

	/**
	 * Construct a new ConnectException with no detailed message.
	 */
	public ElephantConnectException()
	{}
}

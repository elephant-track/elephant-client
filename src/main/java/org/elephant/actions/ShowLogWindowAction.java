/*******************************************************************************
 * Copyright (C) 2020, Ko Sugawara
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

import javax.swing.SwingUtilities;

import org.elephant.actions.mixins.LoggerMixin;

/**
 * Show a log window.
 * 
 * @author Ko Sugawara
 */
public class ShowLogWindowAction extends AbstractElephantAction implements LoggerMixin
{
	private static final long serialVersionUID = 1L;

	private static final String NAME_BASE = "[elephant] show %s log window";

	private static final String NAME_CLIENT = String.format( NAME_BASE, "client" );

	private static final String NAME_SERVER = String.format( NAME_BASE, "server" );

	private static final String MENU_TEXT_BASE = "%s Log";

	private static final String MENU_TEXT_CLIENT = String.format( MENU_TEXT_BASE, "Client" );

	private static final String MENU_TEXT_Server = String.format( MENU_TEXT_BASE, "Server" );

	public enum LogTarget
	{
		CLIENT( NAME_CLIENT, MENU_TEXT_CLIENT ),
		SERVER( NAME_SERVER, MENU_TEXT_Server );

		private final String name;

		private final String menuText;

		LogTarget( final String name, final String menuText )
		{
			this.name = name;
			this.menuText = menuText;
		}

		public String getName()
		{
			return name;
		}

		public String getMenuText()
		{
			return menuText;
		}
	}

	private final LogTarget logTarget;

	@Override
	public String getMenuText()
	{
		return logTarget.getMenuText();
	}

	public ShowLogWindowAction( final LogTarget logTarget )
	{
		super( logTarget.getName() );
		this.logTarget = logTarget;
	}

	@Override
	public void process()
	{
		switch ( logTarget )
		{
		case CLIENT:
			SwingUtilities.invokeLater( () -> showClientLogWindow() );
			break;
		case SERVER:
			SwingUtilities.invokeLater( () -> showServerLogWindow() );
			break;
		default:
			break;
		}
	}

}

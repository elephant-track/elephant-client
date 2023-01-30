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
package org.elephant.setting.server;

import static org.elephant.setting.StyleElementsEx.intElementEx;
import static org.elephant.setting.StyleElementsEx.passwordElement;
import static org.elephant.setting.StyleElementsEx.stringElement;
import static org.mastodon.app.ui.StyleElements.separator;

import java.util.Arrays;
import java.util.List;

import org.elephant.setting.AbstractElephantSettingsPanel;
import org.mastodon.app.ui.StyleElements.StyleElement;

/**
 * Settings panel for server settings.
 * 
 * @author Ko Sugawara
 */
public class ElephantServerSettingsPanel extends AbstractElephantSettingsPanel< ElephantServerSettings >
{

	private static final long serialVersionUID = 1L;

	public ElephantServerSettingsPanel( final ElephantServerSettings style )
	{
		super();
		build( style );
	}

	@Override
	protected List< StyleElement > styleElements( final ElephantServerSettings style )
	{
		return Arrays.asList(
				stringElement( "ELEPHANT server URL with port number", style::getServerURL, style::setServerURL ),

				separator(),

				stringElement( "RabbitMQ server host name", style::getRabbitMQHost, style::setRabbitMQHost ),
				intElementEx( "RabbitMQ server port", 1, 65535, "#", style::getRabbitMQPort, style::setRabbitMQPort ),
				stringElement( "RabbitMQ server username", style::getRabbitMQUsername, style::setRabbitMQUsername ),
				passwordElement( "RabbitMQ server password", style::getRabbitMQPassword, style::setRabbitMQPassword ) );
	}
}

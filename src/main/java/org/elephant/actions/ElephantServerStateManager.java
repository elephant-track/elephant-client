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

import java.util.ArrayList;
import java.util.List;

import org.elephant.actions.ElephantStatusService.ElephantStatus;

/**
 * A singleton instance for managing the state of the server.
 * 
 * @author Ko Sugawara
 */
public enum ElephantServerStateManager
{
	INSTANCE;

	public static final String NO_ERROR_MESSAGE = "";

	public static final String OUTDATED_MESSAGE = "Outdated server";

	public static final String SERVER_NOT_FOUND_MESSAGE = "Server not found";

	private ElephantStatus elephantServerStatus;

	private String elephantServerErrorMessage = NO_ERROR_MESSAGE;

	private ElephantStatus rabbitMQStatus;

	private String rabbitMQErrorMessage = NO_ERROR_MESSAGE;

	private List< GPU > gpus = new ArrayList<>();

	public synchronized ElephantStatus getElephantServerStatus()
	{
		return elephantServerStatus;
	}

	public synchronized void setElephantServerStatus( final ElephantStatus elephantServerStatus )
	{
		if ( this.elephantServerStatus != elephantServerStatus )
		{
			this.elephantServerStatus = elephantServerStatus;
		}
	}

	public synchronized ElephantStatus getRabbitMQStatus()
	{
		return rabbitMQStatus;
	}

	public synchronized void setRabbitMQStatus( final ElephantStatus rabbitMQStatus )
	{
		if ( this.rabbitMQStatus != rabbitMQStatus )
		{
			this.rabbitMQStatus = rabbitMQStatus;
		}
	}

	public synchronized List< GPU > getGpus()
	{
		return gpus;
	}

	public synchronized void setGpus( final List< GPU > gpus )
	{
		this.gpus = gpus;
	}

	public synchronized String getElephantServerErrorMessage()
	{
		return elephantServerErrorMessage;
	}

	public synchronized void setElephantServerErrorMessage( String message )
	{
		this.elephantServerErrorMessage = message;
	}

	public synchronized String getRabbitMQErrorMessage()
	{
		return rabbitMQErrorMessage;
	}

	public synchronized void setRabbitMQErrorMessage( final String message )
	{
		this.rabbitMQErrorMessage = message;
	}

}

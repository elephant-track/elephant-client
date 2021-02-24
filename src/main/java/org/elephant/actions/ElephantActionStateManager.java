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

import org.elephant.actions.SetControlAxisAction.ControlAxis;
import org.mastodon.util.Listeners;

/**
 * A singleton instance for managing the state of application.
 * 
 * @author Ko Sugawara
 */
public enum ElephantActionStateManager
{
	INSTANCE;

	private boolean isLivemode = false;

	private boolean isAborted = false;

	private boolean isAutoFocus = false;

	private boolean isWriting = false;

	private ControlAxis axis = ControlAxis.X;

	private final Listeners.List< LivemodeListener > livemodeListeners;

	private ElephantActionStateManager()
	{
		livemodeListeners = new Listeners.SynchronizedList<>();
	}

	private void notifyListeners( final boolean isLivemode )
	{
		for ( final LivemodeListener l : livemodeListeners.list )
			l.livemodeCahnged( isLivemode );
	}

	public Listeners< LivemodeListener > livemodeListeners()
	{
		return livemodeListeners;
	}

	public synchronized boolean isLivemode()
	{
		return isLivemode;
	}

	public synchronized void setLivemode( final boolean isLivemode )
	{
		if ( this.isLivemode != isLivemode )
		{
			this.isLivemode = isLivemode;
			notifyListeners( isLivemode );
		}
	}

	public synchronized boolean isAborted()
	{
		return isAborted;
	}

	public synchronized void setAborted( boolean isAborted )
	{
		this.isAborted = isAborted;
	}

	public synchronized boolean isAutoFocus()
	{
		return isAutoFocus;
	}

	public synchronized void setAutoFocus( boolean isAutoFocus )
	{
		this.isAutoFocus = isAutoFocus;
	}

	public synchronized boolean isWriting()
	{
		return isWriting;
	}

	public synchronized void setWriting( boolean isWriting )
	{
		this.isWriting = isWriting;
	}

	public synchronized ControlAxis getAxis()
	{
		return axis;
	}

	public synchronized void setAxis( ControlAxis axis )
	{
		this.axis = axis;
	}

}

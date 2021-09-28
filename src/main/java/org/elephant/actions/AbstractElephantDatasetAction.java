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

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.elephant.actions.mixins.ElephantDatasetMixin;

/**
 * An abstract class for ELEPHANT actions that depends a dataset on the server.
 * 
 * @author Ko Sugawara
 */
public abstract class AbstractElephantDatasetAction extends AbstractElephantAction implements ElephantDatasetMixin
{

	public AbstractElephantDatasetAction( final String name )
	{
		super( name );
	}

	private static final long serialVersionUID = 1L;

	boolean prepare()
	{
		return true;
	}

	@Override
	void process()
	{
		if ( prepare() )
		{
			final boolean isReady = ensureDataset();
			if ( !isReady )
			{
				try
				{
					SwingUtilities.invokeAndWait( () -> JOptionPane.showMessageDialog( null, "Dataset is not ready." ) );
				}
				catch ( InvocationTargetException | InterruptedException e )
				{
					handleError( e );
				}
				return;
			}
			processDataset();
		}
	}

	abstract void processDataset();

}

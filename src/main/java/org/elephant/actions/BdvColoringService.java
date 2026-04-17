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

import org.elephant.actions.mixins.ElephantTagActionMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.mamut.MamutViewBdvWrapper;
import org.mastodon.mamut.WindowManager.ViewCreatedListener;
import org.mastodon.mamut.ProjectModel;

public class BdvColoringService extends AbstractElephantService implements ElephantTagActionMixin, WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private final ViewCreatedListener< MamutViewBdv > bdvViewCreatedListener;

	public BdvColoringService()
	{
		super();
		bdvViewCreatedListener = new ViewCreatedListener< MamutViewBdv >()
		{

			@Override
			public void viewCreated( MamutViewBdv bdv )
			{
				new MamutViewBdvWrapper( bdv ).getColoringModel().colorByTagSet( getDetectionTagSet() );
			}
		};
	}

	@Override
	public void init( ProjectModel pluginAppModel )
	{
		super.init( pluginAppModel, null );

		getWindowManager().viewCreatedListeners( MamutViewBdv.class ).add( bdvViewCreatedListener );
	}
}

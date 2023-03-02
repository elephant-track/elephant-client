package org.elephant.actions;

import org.elephant.actions.mixins.ElephantTagActionMixin;
import org.elephant.actions.mixins.WindowManagerMixin;
import org.mastodon.mamut.MamutViewBdv;
import org.mastodon.mamut.MamutViewBdvWrapper;
import org.mastodon.mamut.WindowManager.BdvViewCreatedListener;
import org.mastodon.mamut.plugin.MamutPluginAppModel;

public class BdvColoringService extends AbstractElephantService implements ElephantTagActionMixin, WindowManagerMixin
{

	private static final long serialVersionUID = 1L;

	private final BdvViewCreatedListener bdvViewCreatedListener;

	public BdvColoringService()
	{
		super();
		bdvViewCreatedListener = new BdvViewCreatedListener()
		{

			@Override
			public void bdvViewCreated( MamutViewBdv bdv )
			{
				new MamutViewBdvWrapper( bdv ).getColoringModel().colorByTagSet( getDetectionTagSet() );
			}
		};
	}

	@Override
	public void init( MamutPluginAppModel pluginAppModel )
	{
		super.init( pluginAppModel, null );

		getWindowManager().bdvViewCreatedListeners().add( bdvViewCreatedListener );
	}
}

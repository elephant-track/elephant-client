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

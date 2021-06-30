package org.elephant.actions.mixins;

import org.elephant.actions.BdvContextService;
import org.mastodon.mamut.model.Spot;

/**
 * Handle BDV context.
 * 
 * @author Ko Sugawara
 */
public interface BdvContextMixin extends ElephantActionMixin
{

	BdvContextService getBdvContextService();

	default Iterable< Spot > getVisibleVertices( final int timepoint )
	{
		return getBdvContextService().getVisibleVertices( timepoint );
	}

	default boolean showContextChooserDialog()
	{
		return getBdvContextService().showContextChooserDialog();
	}

}

package org.mastodon.mamut;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.views.context.ContextProvider;

import bdv.viewer.ViewerPanel;

public class MamutViewBdvWrapper
{

	private MamutViewBdv bdv;

	public MamutViewBdvWrapper( final MamutViewBdv bdv )
	{
		this.bdv = bdv;
	}

	public ColoringModelMain< Spot, Link, BranchSpot, BranchLink > getColoringModel()
	{
		return bdv.getColoringModel();
	}

	public ContextProvider< Spot > getContextProvider()
	{
		return bdv.getContextProvider();
	}

	public ViewerPanel getViewerPanelMamut()
	{
		return bdv.getViewerPanelMamut();
	}

}

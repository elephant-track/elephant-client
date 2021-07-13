package org.elephant.actions.mixins;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public interface AWTMixin
{
	default void openBrowser( final String url ) throws URISyntaxException, IOException
	{
		final Desktop desktop = java.awt.Desktop.getDesktop();
		final URI oURL = new URI( url );
		desktop.browse( oURL );
	}
}

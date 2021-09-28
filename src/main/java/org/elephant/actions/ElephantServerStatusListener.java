package org.elephant.actions;

/**
 * Classes that implement {@link ElephantServerStatusListener} get a
 * notification when the ELEPHANT status is updated.
 */
public interface ElephantServerStatusListener
{

	void serverStatusUpdated();
}

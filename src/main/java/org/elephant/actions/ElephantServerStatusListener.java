package org.elephant.actions;

import org.elephant.actions.ElephantStatusService.ElephantStatus;

/**
 * Classes that implement {@link ElephantServerStatusListener} get a
 * notification when the ELEPHANT status is updated.
 */
public interface ElephantServerStatusListener
{
	void statusUpdated( final ElephantStatus status, final String url );
}

package org.elephant.actions;

import org.elephant.actions.ElephantStatusService.ElephantStatus;

/**
 * Classes that implement {@link ElephantStatusListener} get a notification when
 * the ELEPHANT status is updated.
 */
public interface ElephantStatusListener
{
	void statusUpdated( final ElephantStatus status, final String url );
}

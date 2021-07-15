package org.elephant.actions;

import java.util.Collection;

/**
 * Classes that implement {@link ElephantGpuStatusListener} get a notification
 * when the ELEPHANT status is updated.
 */
public interface ElephantGpuStatusListener
{
	void statusUpdated( final Collection< GPU > gpus );
}

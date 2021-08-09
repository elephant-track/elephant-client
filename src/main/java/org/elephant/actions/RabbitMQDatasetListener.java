package org.elephant.actions;

import com.rabbitmq.client.Delivery;

/**
 * Classes that implement {@link RabbitMQDatasetListener} get a notification
 * when the "dataset" queue of RabbitMQ received a message.
 */
public interface RabbitMQDatasetListener
{

	/**
	 * Called when a <code><b>basic.deliver</b></code> is received for this
	 * consumer.
	 * 
	 * @param consumerTag
	 *            the <i>consumer tag</i> associated with the consumer
	 * @param message
	 *            the delivered message
	 */
	void messageDelivered( String consumerTag, Delivery message );
}

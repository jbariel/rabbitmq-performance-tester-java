package com.barielinc.cloud.rabbitmq;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;

public class RMQProducer extends RMQConnection {

	ScheduledFuture<?> publishFuture;

	public RMQProducer() {
		super();
	}

	public RMQProducer(RMQProperties props) {
		super(props);
	}

	@Override
	public void start() throws IOException {

		connect();

		publishFuture = RabbitMQTester.globalThreadPool.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					currentChannel.basicPublish(exchangeName, routingKey, null,
							RandomUtils.nextBytes(connectionInfo.getProducerMessageSize()));
				} catch (IOException e) {
					o.e("Error publishing message");
					e.printStackTrace();
				}

			}
		}, 0L, connectionInfo.getProducerMessageRate(), TimeUnit.MILLISECONDS);

	}

	@Override
	public void stop() throws IOException {
		publishFuture.cancel(true);
		genericStop();
	}

}

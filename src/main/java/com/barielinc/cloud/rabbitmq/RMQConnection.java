package com.barielinc.cloud.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class RMQConnection {

	protected final Out o = new Out(this.getClass());

	protected Connection currentConnection;
	protected Channel currentChannel;
	protected RMQProperties connectionInfo;

	protected String exchangeName = "defaultExchangeName";
	protected String queueName = "defaultQueueName";
	protected String routingKey;

	public RMQConnection() {
		super();
	}

	public RMQConnection(RMQProperties props) {
		this();
		setConnectionInfo(props);
	}

	public abstract void start() throws IOException;

	public abstract void stop() throws IOException;

	public void genericStop() throws IOException {
		if (null != currentChannel) {
			try {
				currentChannel.close();
			} catch (TimeoutException e) {
				o.w("Timed out closing channel...");
				e.printStackTrace();
			}
		}
		if (null != currentConnection) {
			currentConnection.close();
		}
	}

	public void setConnectionInfo(RMQProperties props) {
		this.connectionInfo = props;
	}

	protected void connect() throws IOException {
		connect(connectionInfo);
	}

	protected void connect(RMQProperties props) throws IOException {
		createConnection(setupFactory(props));
		postConnect();
	}

	protected void connect(String uri) throws IOException {
		createConnection(setupFactory(uri));
		postConnect();
	}

	private ConnectionFactory setupFactory(RMQProperties props) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(props.getUsername());
		factory.setPassword(props.getPassword());
		factory.setVirtualHost(props.getvHost());
		factory.setHost(props.getHostname());
		factory.setPort(props.getPort());
		return factory;
	}

	private ConnectionFactory setupFactory(String uri) {
		ConnectionFactory factory = new ConnectionFactory();
		try {
			factory.setUri(uri);
			// } catch (KeyManagementException | NoSuchAlgorithmException |
			// URISyntaxException e) {
		} catch (Exception e) {
			o.e("Error setting up ConnectionFactory with URI...");
			e.printStackTrace();
		}
		return factory;
	}

	private void createConnection(ConnectionFactory factory) throws IOException {
		currentConnection = null;
		currentChannel = null;
		try {
			currentConnection = factory.newConnection();
			currentChannel = currentConnection.createChannel();
		} catch (TimeoutException e) {
			o.e("Creating connection timed out...");
			e.printStackTrace();
		}
	}

	protected void postConnect() throws IOException {
		routingKey = "routingKey" + queueName + exchangeName;
		currentChannel.exchangeDeclare(exchangeName, "direct", false, true, null);
		currentChannel.queueDeclare(queueName, false, false, true, null);
		currentChannel.queueBind(queueName, exchangeName, "routingKey" + queueName + exchangeName);
	}

}

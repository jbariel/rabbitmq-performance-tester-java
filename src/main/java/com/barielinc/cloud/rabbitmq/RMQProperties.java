package com.barielinc.cloud.rabbitmq;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.barielinc.cloud.rabbitmq.Out.LogLevel;

public class RMQProperties {

	private String username;
	private String password;
	private String vHost;
	private String hostname;
	private int port;
	private LogLevel logLevel;
	private boolean isProducer;
	private boolean isConsumer;
	private int numberOfConsumers;
	private int numberOfProducers;
	private long producerMessageRate;
	private int producerMessageSize;

	public RMQProperties() {
		super();
	}

	public RMQProperties(String username, String password, String vHost, String hostname, int port) {
		this();
		setUsername(username);
		setPassword(password);
		setvHost(vHost);
		setHostname(hostname);
		setPort(port);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getvHost() {
		return vHost;
	}

	public void setvHost(String vHost) {
		this.vHost = vHost;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public boolean isProducer() {
		return isProducer;
	}

	public void setProducer(boolean isProducer) {
		this.isProducer = isProducer;
	}

	public boolean isConsumer() {
		return isConsumer;
	}

	public void setConsumer(boolean isConsumer) {
		this.isConsumer = isConsumer;
	}

	public int getNumberOfConsumers() {
		return numberOfConsumers;
	}

	public void setNumberOfConsumers(int numberOfConsumers) {
		this.numberOfConsumers = numberOfConsumers;
	}

	public int getNumberOfProducers() {
		return numberOfProducers;
	}

	public void setNumberOfProducers(int numberOfProducers) {
		this.numberOfProducers = numberOfProducers;
	}

	public long getProducerMessageRate() {
		return producerMessageRate;
	}

	public void setProducerMessageRate(long producerMessageRate) {
		this.producerMessageRate = producerMessageRate;
	}

	public int getProducerMessageSize() {
		return producerMessageSize;
	}

	public void setProducerMessageSize(int producerMessageSize) {
		this.producerMessageSize = producerMessageSize;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public String toUriString() {
		return String.format("amqp:/%s:%s@%s:%d/%s", getUsername(), getPassword(), getHostname(), getPort(),
				getvHost());
	}

}

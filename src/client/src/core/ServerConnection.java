package core;

import interfaces.MqttMessageReceiver;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ServerConnection implements MqttMessageReceiver {

	private ClientMain client;
	private String hostname;
	private MqttClient clientToServerConnection;
	private String inputChannel, outputChannel;

	public MqttClient getClientToServerConnection() {
		return clientToServerConnection;
	}

	public ServerConnection(ClientMain client, String hostname, int port) {
		this(client);
		// this.hostname = hostname + ":" + port;
	}

	public ServerConnection(ClientMain client) {
		this.client = client;
		this.hostname = "tcp://test.mosquitto.org:1883";

		MemoryPersistence persistence = new MemoryPersistence();
		try {
			clientToServerConnection = new MqttClient(hostname,
					this.client.getClientId(), persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			clientToServerConnection.connect(connOpts);
			if (!clientToServerConnection.isConnected())
				throw new MqttException(0);

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void messageReceived(MqttMessage message) {
		try {
			clientToServerConnection.publish(outputChannel, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void handshake() throws MqttPersistenceException, MqttException {
		MqttMessage msg = new MqttMessage(this.client.getClientId().getBytes());
		System.out.println("Publishing to handshake: " + msg.toString());
		clientToServerConnection.publish("handshake", msg);
		outputChannel = this.client.getClientId() + "/in";
		inputChannel = this.client.getClientId() + "/out";
	}

	public ClientMain getClient() {
		return client;
	}

}

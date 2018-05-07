package test.core;

import interfaces.MqttMessageReceiver;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import core.MqttBroker;
import core.MqttConnection;
import exceptions.NoBrokerAvailableException;

public class MockupApplication implements MqttMessageReceiver {
	public static String brokerURI = "tcp://localhost:12888";
	public static MockupClient client;
	public static String clientId;
	
	public static void main(String[] args){
		MqttBroker.setExistingBroker(brokerURI);
		try {
			clientId = MqttClient.generateClientId();
			System.out.println("Set clientId to " + clientId);
			MqttConnection.initializePublishingConnection(clientId);
//			client = new MockupClient();
//			MqttConnection.initializeConnection(clientId + "/out", clientId+"/out");
//			MqttConnection.getConnection(clientId + "/out").addReceiver(client);
//			MqttConnection.writeMessage("appHandshake", 2, clientId);
			MqttConnection.initializeConnection("appdata", clientId);
			MqttConnection.getConnection("appdata").addReceiver(new MockupApplication());

		} catch (MqttException | NoBrokerAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Thread t;
	@Override
	public void messageReceived(MqttMessage message) {
		System.out.println("Message:" + message.toString());
	}
}

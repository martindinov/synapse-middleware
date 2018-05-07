package test.core;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import interfaces.MqttMessageReceiver;
import core.MqttBroker;
import core.MqttConnection;
import exceptions.NoBrokerAvailableException;

public class MockupClient implements MqttMessageReceiver {
	public static String brokerURI = "tcp://localhost:12888";
	public static MockupClient client;
	public static String clientId;
	
	public static void main(String[] args){
		MqttBroker.setExistingBroker(brokerURI);
		try {
			clientId = MqttClient.generateClientId();
			System.out.println("Set clientId to " + clientId);
			MqttConnection.initializePublishingConnection(clientId);
			client = new MockupClient();
			MqttConnection.initializeConnection(clientId + "/out", clientId+"/out");
			MqttConnection.getConnection(clientId + "/out").addReceiver(client);
			MqttConnection.writeMessage("handshake", 2, clientId);

		} catch (MqttException | NoBrokerAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Thread t;
	@Override
	public void messageReceived(MqttMessage message) {
		System.out.println("Message:" + message.toString());
		if(message.toString().startsWith("getInfo")){
			try {
				MqttConnection.writeMessage(clientId + "/in", 2, "headset:mw");				
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		if(message.toString().startsWith("start")){
				t = new Thread(new MessageLoop());
				t.start();
		}
		if(message.toString().startsWith("stop")){
			t.interrupt();
		}
	}
	
	private static class MessageLoop implements Runnable {
		public boolean interrupted = false;
		@Override
		public void run() {
			while(!interrupted){
			String data ="attention:" + String.valueOf( 60 + Math.random()*40);
			try {
				MqttConnection.writeMessage(clientId + "/in", 1, data);
				Thread.sleep((long) (500 + Math.random() * 100));
			} catch (MqttException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				interrupted = true;
			}
			}
		}
		
	}
	
	
}

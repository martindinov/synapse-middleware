package listeners;

import interfaces.MqttMessageReceiver;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import core.ApplicationDataHandler;

public class HandshakeListener implements MqttMessageReceiver{

	protected ArrayList<EEGListener> listeners = new ArrayList<EEGListener>();	
	protected ApplicationDataHandler appDataHandler;
	
	public HandshakeListener(ApplicationDataHandler appDataHandler){
		this.appDataHandler = appDataHandler;
	}
	
	@Override
	public void messageReceived(MqttMessage message) {
		System.out.println("Handshake received " + message.toString());
		String clientId = message.toString();
		try {
			listeners.add(new EEGListener(clientId, appDataHandler));
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}

package listeners;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import core.MqttConnection;
import exceptions.NoBrokerAvailableException;
import interfaces.MqttMessageReceiver;

public class ApplicationListener implements MqttMessageReceiver{
	protected String topicIn, topicOut;
	
	
	public ApplicationListener(String topicOut, String topicIn) throws MqttException{
			this.topicOut = topicOut;
			this.topicIn = topicIn;
			
			try {
				MqttConnection.initializeConnection(topicOut, "Server-" + topicOut);
				System.out.println("Subscribed to channel " + topicOut);
			} catch (NoBrokerAvailableException e) {
				e.printStackTrace();
			}
			MqttConnection.getConnection(topicOut).addReceiver(this);
			
//			sendStatus(STATUS_AWAITING_INFO);
		}

	@Override
	public void messageReceived(MqttMessage message) {
		
	}

}

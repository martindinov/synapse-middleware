package interfaces;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttMessageReceiver {

	public void messageReceived(MqttMessage message);
}

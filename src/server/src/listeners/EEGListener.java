package listeners;

import interfaces.MqttMessageReceiver;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import core.ApplicationDataHandler;
import core.MqttConnection;
import eeghelper.EEGHelper;
import eeghelper.EEGHelperFactory;
import exceptions.NoBrokerAvailableException;

public class EEGListener implements MqttMessageReceiver {
	protected String clientId;
	protected String topicOut;
	protected String topicIn;
	protected EEGHelper helper;
	protected ApplicationDataHandler appDataHandler;
	public static final int STATUS_START = 1, STATUS_PAUSE = 2,
			STATUS_TERMINATE = 3, STATUS_AWAITING_INFO = 4;

	public EEGListener(String clientId, ApplicationDataHandler appDataHandler)
			throws MqttException {
		this.clientId = clientId;
		this.topicOut = clientId + "/in";
		this.topicIn = clientId + "/out";

		try {
			MqttConnection.initializeConnection(topicOut, "Server-" + topicOut);
			System.out.println("Subscribed to channel " + topicOut);
		} catch (NoBrokerAvailableException e) {
			e.printStackTrace();
		}
		MqttConnection.getConnection(topicOut).addReceiver(this);

		sendStatus(STATUS_AWAITING_INFO);
		this.appDataHandler = appDataHandler;
	}

	@Override
	public void messageReceived(MqttMessage message) {
		if (message.toString().startsWith("headset:")) {
			System.out.println("Creating Helper of type "
					+ message.toString().substring(8));
			helper = EEGHelperFactory.createHelper(message.toString()
					.substring(8));
			sendStatus(STATUS_START);
			System.out.println("Send Message of type start.");
//		} else if  (message.toString().startsWith("raw:")){
//			helper = EEGHelperFactory.createHelper("muse");
//			sendStatus(STATUS_START);
		}else{
			if (helper == null) {
				System.out.println("Helper is null.");
				return;
			}

			/* the below variable contains data of the form dataType:data */
			String transformed = helper.transformInput(clientId,
					message.toString());
			appDataHandler.appendEegData(clientId, transformed);
		}
	}

	public void sendStatus(int status) {
		String content;
		switch (status) {
		case STATUS_AWAITING_INFO:
			content = "getInfo";
			break;
		case STATUS_START:
			content = "start";
			break;
		default:
			return;
		}

		try {
			MqttConnection.writeMessage(topicIn, 2, content);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

}

package core;

import interfaces.MqttMessageReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import exceptions.NoBrokerAvailableException;

public class MqttConnection implements MqttCallback {

	public static MqttConnection publishingInstance;
	private MqttClient client;
	private String clientId;
	private List<MqttMessageReceiver> receivers = new ArrayList<MqttMessageReceiver>();
	private static HashMap<String, MqttConnection> connections = new HashMap<String, MqttConnection>();
	private static boolean shutdown = false;

	public MqttConnection(String clientId) throws MqttException,
			NoBrokerAvailableException {
		this.clientId = clientId;
		setUpClient();
	}

	public static MqttConnection getConnection(String topic) {
		return connections.get(topic);
	}

	public static MqttConnection getPublishingConnection() {
		return publishingInstance;
	}

	public static void initializePublishingConnection(String clientId)
			throws MqttException, NoBrokerAvailableException {
		publishingInstance = new MqttConnection(clientId);
	}

	public static void initializeConnection(String topic, String clientId)
			throws MqttException, NoBrokerAvailableException {
		connections.put(topic, new MqttConnection(clientId));
		connections.get(topic).getClient().subscribe(topic);
		connections.get(topic).getClient().setCallback(connections.get(topic));
	}

	public MqttClient getClient() {
		return client;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// System.out.println("Connection to Broker lost in " + this.toString()
		// + ": " + arg0.getMessage());
		// while(!shutdown){
		// try{
		// setUpClient();
		// return;
		// }catch(MqttException | NoBrokerAvailableException e){
		// System.out.println("Reestablishing connection failed. Trying again in 10 seconds.");
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// System.exit(-1);
		// }
		// }
		// }
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
//		System.out.println(message.getPayload());
		for (MqttMessageReceiver receiver : receivers) {
			receiver.messageReceived(message);
		}
	}

	public void addReceiver(MqttMessageReceiver receiver) {
		receivers.add(receiver);
	}

	public void removeReceiver(MqttMessageReceiver receiver) {
		receivers.remove(receiver);
	}

	private void setUpClient() throws MqttException, NoBrokerAvailableException {
		MemoryPersistence persistence = new MemoryPersistence();
		client = new MqttClient(MqttBroker.getBrokerURI(), clientId,
				persistence);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		client.connect(connOpts);
		client.setCallback(this);
		/* why do we get into here twice in the beginning? */
		//System.out.println("here");
		if (!client.isConnected())
			throw new MqttException(0);
	}

	public static void dispose() {
		try {
			shutdown = true;
			for (MqttConnection connection : connections.values()) {
				if (connection.getClient() != null) {
					connection.getClient().disconnect();
					connection.getClient().close();
				}
			}
			if (publishingInstance != null) {
				publishingInstance.getClient().disconnect();
				publishingInstance.getClient().close();
			}
		} catch (MqttException e) {
			e.printStackTrace();
		} finally {
			publishingInstance = null;
		}
	}

	public static void writeMessage(String topic, int qos, String payload)
			throws MqttPersistenceException, MqttException {
		MqttMessage message = new MqttMessage(payload.getBytes());
		message.setQos(qos);
		publishingInstance.client.publish(topic, message);
	}
}

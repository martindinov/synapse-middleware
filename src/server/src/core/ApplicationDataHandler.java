package core;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.paho.client.mqttv3.MqttException;

public class ApplicationDataHandler implements Runnable {
	private String pubTopic;
	private long interval = 16; // milliseconds
	private long lastSent;
	private int mode = MODE_COLLECT;
	private int qos;
	private HashMap<String, HashMap<String, StringBuilder>> receiverData;
	private final Lock lock = new ReentrantLock();

	public static final int MODE_COLLECT = 1;

	public ApplicationDataHandler(long interval, int mode, int qos,
			String pubTopic) {
		this.interval = interval;
		this.mode = mode;
		this.qos = qos;
		receiverData = new HashMap<String, HashMap<String, StringBuilder>>();
		this.pubTopic = pubTopic;
	}

	public void appendEegData(String eeg, String dataAndType) {
		lock.lock();

		String[] dataAndTypeSplit = dataAndType.split(":");
		String type = dataAndTypeSplit[0];
		String data = dataAndTypeSplit[1];

		if (!receiverData.containsKey(eeg))
			receiverData.put(eeg, new HashMap<String, StringBuilder>());

		if (!receiverData.get(eeg).containsKey(type))
			receiverData.get(eeg).put(type, new StringBuilder());

		if (receiverData.get(eeg).get(type).length() > 0)
			receiverData.get(eeg).get(type).append(", " + data);
		else
			receiverData.get(eeg).get(type).append(data);

//		System.out.println("Appended to " + eeg + "." + type + ":" + data);
		lock.unlock();
	}

	@Override
	public void run() {
		while (true) {
			lock.lock();
			long time = System.currentTimeMillis();

			String payload = buildPayload();
			try {
				MqttConnection.writeMessage(pubTopic, qos, payload);
				clearPayload();
			} catch (MqttException e1) {
				System.out.println("Message to Applications on topic "
						+ pubTopic + " with qos " + qos
						+ " could not be delivered.");
				e1.printStackTrace();
			}

			lock.unlock();
			try {
				if(interval - (System.currentTimeMillis() - time) > 0)
					Thread.sleep(interval - (System.currentTimeMillis() - time));
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/* TODO: fix buildPayload to use type of data as well */
	public String buildPayload() {
		StringBuilder payload = new StringBuilder("{");
		boolean firstEEG = true;
		
		for (String eeg : receiverData.keySet()) {
			if(!firstEEG)
				payload.append(',');
			firstEEG = false;
			payload.append('"' + eeg + "\": {");

			boolean firstType = true;
			for(String key : receiverData.get(eeg).keySet()){
				if(!firstType)
					payload.append(", ");
				firstType = false;
				
				payload.append('"' + key + "\":[" + receiverData.get(eeg).get(key).toString() + ']');
			}
			
			payload.append('}');
					

		}
		payload.append('}');
		return payload.toString();
	}

	public void clearPayload() {
		receiverData.clear();
	}

}

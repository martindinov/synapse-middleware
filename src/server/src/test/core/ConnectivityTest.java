package test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import core.MqttBroker;

public class ConnectivityTest implements MqttCallback {
	static Process process;
	static String port = "1337";
	static MqttClient client;
	String topic = "Test";
	boolean received = false;
	private CountDownLatch lock = new CountDownLatch(1);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String path;
		File f = new File(MqttBroker.getMosquittoPath());
		System.out.println(f.getAbsolutePath());

		try {
			process = new ProcessBuilder(f.getAbsolutePath(), "-p", port)
					.start();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		System.out.println("Mosquitto started.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (client != null && client.isConnected())
			client.disconnect();
		process.destroy();
		System.out.println("Mosquitto stopped.");
	}

	@Test
	public void testCreateClient() {
		try {
			createClient();
			assertEquals(client.isConnected(), true);
			client.disconnect();
		} catch (Exception e) {
			fail("Exception while connecting to the broker: " + e.getMessage());
		}
	}

	@Test
	public void testSubscribeToBroker() {
		try {
			createClient();
			assertEquals(client.isConnected(), true);
			client.subscribe(topic);
			client.disconnect();
		} catch (Exception e) {
			fail("Exception while subscribing: " + e.getMessage());
		}
	}

	@Test
	public void testPublish() {
		String content = "Test message";
		try {
			createClient();
			assertEquals(client.isConnected(), true);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(2);
			client.publish(topic, message);
			client.disconnect();
		} catch (Exception e) {
			fail("Exception while publishing: " + e.getMessage());
		}
	}

	@Test
	public void testReceive() {
		String content = "Test message";
		try {
			createClient();
			assertEquals(client.isConnected(), true);
			client.subscribe(topic);
			client.setCallback(this);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(2);
			received = false;
			client.publish(topic, message);
			lock.await(300, TimeUnit.MILLISECONDS);
			assertEquals(received, true);
			client.disconnect();
		} catch (Exception e) {
			fail("Exception while receiving: " + e.getMessage());
		}
	}

	@Test
	public void testWrongChannel() {
		String content = "Test message";
		try {
			createClient();
			assertEquals(client.isConnected(), true);
			client.subscribe("NotTest");
			client.setCallback(this);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(2);
			received = false;
			client.publish(topic, message);
			lock.await(300, TimeUnit.MILLISECONDS);
			assertEquals(received, false);
			client.disconnect();
		} catch (Exception e) {
			fail("Exception while receiving: " + e.getMessage());
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		System.out.println("Message received: " + arg0);
		received = true;
	}

	private void createClient() throws Exception {
		String broker = "tcp://localhost:" + port;
		String clientId = "JavaSample";
		MemoryPersistence persistence = new MemoryPersistence();
		client = new MqttClient(broker, clientId, persistence);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		client.connect(connOpts);
	}
}

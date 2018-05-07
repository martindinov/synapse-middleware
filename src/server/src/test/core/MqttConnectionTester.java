package test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import core.MqttBroker;
import core.MqttConnection;
import exceptions.NoBrokerAvailableException;

public class MqttConnectionTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MqttBroker.dispose();
		MqttBroker.initializeNewMosquito(1337);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MqttBroker.dispose();
	}

	@Test
	public void testClientInitialize() throws MqttException,
			NoBrokerAvailableException {
		MqttConnection.initializePublishingConnection("TestConnection");
		assertEquals(MqttConnection.getPublishingConnection().getClient().getServerURI(),
				MqttBroker.getBrokerURI());
		assertEquals(MqttConnection.getPublishingConnection().getClient().isConnected(),
				true);
	}

	@Test(expected = MqttException.class)
	public void testBadInitializationFails() throws MqttException,
			NoBrokerAvailableException {
		MqttBroker.setExistingBroker(MqttBroker.getBrokerURI() + "0");
		MqttConnection.initializePublishingConnection("TestConnection");
		assertEquals(MqttConnection.getPublishingConnection().getClient().isConnected(),
				false);
	}

	@Test
	public void testTeardown() throws MqttException, NoBrokerAvailableException {
		MqttConnection.initializePublishingConnection("TestConnection");
		assertEquals(MqttConnection.getPublishingConnection().getClient().isConnected(),
				true);
		MqttConnection.dispose();
		assertNull(MqttConnection.getPublishingConnection());
	}

	@After
	public void tearDown() {
		MqttConnection.dispose();
	}

}

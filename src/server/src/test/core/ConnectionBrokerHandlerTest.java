package test.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import core.MqttBroker;
import exceptions.NoBrokerAvailableException;

public class ConnectionBrokerHandlerTest {

	@Test
	public void testBrokerInitialization() throws NoBrokerAvailableException {
		assertEquals(MqttBroker.getBrokerURI(), "tcp://localhost:1337");
	}

}

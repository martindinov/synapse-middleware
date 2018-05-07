package test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import core.ApplicationDataHandler;
import core.MqttBroker;
import core.MqttConnection;

public class ApplicationHandlerTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MqttBroker.initializeNewMosquito(MqttBroker.DEFAULT_PORT);
		MqttConnection.initializePublishingConnection("TestServer");		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MqttConnection.dispose();
		MqttBroker.dispose();
	}

	@Test
	public void testBuildPayload() {
		ApplicationDataHandler dataHandler = new ApplicationDataHandler(16, ApplicationDataHandler.MODE_COLLECT, 0, "TestApp");
		String data1 = "{'time' : 1261440000, 'data' : '0;1;2;3;4'}";
		dataHandler.appendEegData("testDevice", data1);
		String res = dataHandler.buildPayload();
		assertEquals(res, "[{'testDevice': [" + data1 + "]}]");
		String data2 = "{'time' : 1262040000, 'data' : '0;1;2;3;4;5;6'}";
		dataHandler.appendEegData("testDevice", data2);
		res = dataHandler.buildPayload();
		assertEquals(res, "[{'testDevice': [" + data1 + ", " + data2 + "]}]");
		dataHandler.appendEegData("testDevice2", data1);
		res = dataHandler.buildPayload();
		assertTrue(res.contains("{'testDevice2': [" + data1 + "]}"));
		assertTrue(res.contains("{'testDevice': [" + data1 + ", " + data2 + "]}"));
	}
	
	@Test
	public void testClearPayload() {
		ApplicationDataHandler dataHandler = new ApplicationDataHandler(16, ApplicationDataHandler.MODE_COLLECT, 0, "TestApp");
		String data1 = "{'time' : 1261440000, 'data' : '0;1;2;3;4'}";
		dataHandler.appendEegData("testDevice", data1);
		String res = dataHandler.buildPayload();
		assertEquals(res, "[{'testDevice': [" + data1 + "]}]");
		dataHandler.clearPayload();
		assertEquals(dataHandler.buildPayload(), "[]");
		dataHandler.appendEegData("testDevice", data1);
		res = dataHandler.buildPayload();
		assertEquals(res, "[{'testDevice': [" + data1 + "]}]");		
	}

}

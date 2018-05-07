package core;

import interfaces.CoreServerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import listeners.HandshakeListener;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;

import exceptions.NoBrokerAvailableException;
import exceptions.OSNotSupportedException;

public class ServerMain implements CoreServerInterface {

	public boolean acceptConnection(String client) {
		return false;
	}

	public String getFormattedDataSample(String[] clients, int timeInterval) {
		return null;
	}

	public static void main(String[] args){
		//Read and prepare Program arguments
		List<String> legalOptions = new ArrayList<String>(Arrays.asList(new String[]{"--brokerURI", "--brokerPort", "--handshakeTopic", "--qualityOfService"}));
		List<String> argsList = new ArrayList<String>();
		HashMap<String, String> options = new HashMap<String, String>();
		String option;

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2){
					throw new IllegalArgumentException("Not a valid argument: "+args[i]);
				} else if (legalOptions.contains(args[i])){
					option = args[i];
					if (args.length-1 == i)
						throw new IllegalArgumentException("Expected arg after: "+args[i]);
					i++;
					options.put(option, args[i]);
				}
				break;
			default:
				argsList.add(args[i]);
				break;
			}
		}

		//Prepare the Broker
		int port = MqttBroker.DEFAULT_PORT;
		if(options.containsKey("--brokerURI")){
			MqttBroker.setExistingBroker(options.get("--brokerURI"));
		}else{
			if(options.containsKey("--brokerPort")){
				if(StringUtils.isNumeric(options.get("--brokerPort"))){
					port = Integer.valueOf(options.get("--brokerPort"));
				}else{
					throw new IllegalArgumentException("Non-integer parameter for --brokerPort supplied.");
				}
			}
			try {
				MqttBroker.initializeNewMosquito(port);
			} catch (OSNotSupportedException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			} catch (IOException e) {
				System.err.println("Could not find the local mosquitto executable. Path looked at is " + e.getMessage());
				System.exit(-1);
			}
		}

		//Prepare MqttConnection
		try {
			MqttConnection.initializePublishingConnection("Server");
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (NoBrokerAvailableException e) {
			System.err.println("Broker could not be initialized.");
			e.printStackTrace();
			System.exit(-1);
		}

		//Register in 'Handshake' topic
		int qos = 2;
		if(options.containsKey("--qualityOfService")){
			if(StringUtils.isNumeric(options.get("--qualityOfService"))){
				qos = Integer.valueOf(options.get("--qualityOfService"));
				if(qos < 0 || qos > 2){
					throw new IllegalArgumentException("Illegal argument for --qualityOfService supplied. Has to be 0, 1 or 2.");					
				}
			}else{
				throw new IllegalArgumentException("Non-integer parameter for --qualityOfService supplied.");
			}
		}
		try {
			MqttConnection.initializeConnection("handshake", "Server-handshake");
			System.out.println("Subscribed to handshake");
		} catch (MqttException | NoBrokerAvailableException e) {
			System.err.println("Subscribing to topic 'handshake' failed.");
			e.printStackTrace();
			System.exit(-1);
		}
		ApplicationDataHandler appDataHandler = new ApplicationDataHandler(1000, ApplicationDataHandler.MODE_COLLECT, 2, "appdata");
		HandshakeListener listener = new HandshakeListener(appDataHandler);
		MqttConnection.getConnection("handshake").addReceiver(listener);
		
		Thread t = new Thread(appDataHandler);
		t.start();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while(br.readLine().equalsIgnoreCase("exit") != true){

			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Stopping Server.");
		try {
			MqttConnection.getPublishingConnection().getClient().disconnect();
			System.out.println("Client disconnected");
		} catch (MqttException e1) {
			e1.printStackTrace();
		}
		MqttBroker.dispose();
		System.out.println("MqttBroker disposed.");

		System.exit(0);

	}

}

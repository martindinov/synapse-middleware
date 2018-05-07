package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import exceptions.NoBrokerAvailableException;

/*
 * Super simple client tester - add GUI for basic control/demoing later on?
 */

public class ClientTester {

	private static boolean reading = true;
	private static ClientMain client;

	public ClientTester() {

	}

	public static void main(String[] args) {

		// Read and prepare Program arguments
		List<String> legalOptions = new ArrayList<String>(
				Arrays.asList(new String[] { "--headsetToConnect",
						"--serverHost", "--serverPort" }));
		List<String> argsList = new ArrayList<String>();
		HashMap<String, String> options = new HashMap<String, String>();
		String option;

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2) {
					throw new IllegalArgumentException("Not a valid argument: "
							+ args[i]);
				} else if (legalOptions.contains(args[i])) {
					option = args[i];
					if (args.length - 1 == i)
						throw new IllegalArgumentException(
								"Expected arg after: " + args[i]);
					i++;
					options.put(option, args[i]);
				}
				break;
			default:
				argsList.add(args[i]);
				break;
			}
		}

		String headsetToConnect = "";
		if (options.containsKey("--headsetToConnect")) {
			headsetToConnect = options.get("--headsetToConnect");
		} else {
			headsetToConnect = "all";
		}

		// Register in 'Handshake' topic
		String serverHost = "";
		int serverPort = 0;
		if (options.containsKey("--serverHost")) {
			serverHost = options.get("--serverHost");

			if (options.containsKey("--serverPort")) {
				serverPort = Integer.parseInt(options.get("--serverPort"));
			} else {
				serverPort = 1883;
			}

			serverHost = serverHost + ":" + serverPort;

		} else {
			/*
			 * If not serverHost specified. Also don't care about serverPort
			 * then.
			 */
			serverHost = "tcp://test.mosquitto.org:1883";
		}

		while (reading) {
			if (client == null) {
				System.out.println("Creating and initializing Client object for headset type: " + headsetToConnect + " and to the server: " + serverHost);
				try {
					client = new ClientMain(headsetToConnect, serverHost);
				} catch (ServerConnectionException | NoBrokerAvailableException e) {
					e.printStackTrace();
				}
			} else {

			}
		}
	}
}

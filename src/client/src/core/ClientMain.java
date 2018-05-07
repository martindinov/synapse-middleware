package core;

import headsets.HeadsetConnectionException;
import headsets.HeadsetConnector;
import headsets.epoc.EpocConnector;
import headsets.mindwave.MindWaveConnector;
import headsets.muse.MuseConnector;
import headsets.tsk.TSKConnector;
import interfaces.MqttMessageReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import exceptions.NoBrokerAvailableException;

public class ClientMain implements MqttMessageReceiver {

	// private ArrayList<Server> servers = new ArrayList<Server>();
	private ArrayList<HeadsetConnector> headsets = new ArrayList<HeadsetConnector>();

	private MuseConnector muse;
	private MindWaveConnector mw;
	private TSKConnector tsk;
	private EpocConnector epoc;
	private String clientId = MqttClient.generateClientId();
	private List<ServerConnection> servers = new ArrayList<ServerConnection>();

	public void addServer(String hostname, int port) {
		servers.add(new ServerConnection(this, hostname, port));
	}

	public void startMuseIO() throws IOException {
//		System.out.println("file path: "
//				+ new File("libs/muse/osx/muse-io").getAbsolutePath());
//		new ProcessBuilder(new File("libs/muse/osx/muse-io").getAbsolutePath(),
//				"--osc", "osc.udp://localhost:5001,osc.udp://localhost:5002")
//				.start();
		System.out.println("file path: "
				+ new File("Muse/muse-io.exe").getAbsolutePath());
		new ProcessBuilder(new File("Muse/muse-io.exe").getAbsolutePath(),
				"--osc", "osc.udp://localhost:5001,osc.udp://localhost:5002")
				.start();
	}

	public ClientMain(String headsetToConnect, String serverHost)
			throws ServerConnectionException, NoBrokerAvailableException {

		/* FIXME: rewrite so connections to multiple servers will work properly */

		servers.add(new ServerConnection(this));

		try {
			for (ServerConnection s : servers) {
				s.handshake();
			}
		} catch (MqttException e) {
			throw new ServerConnectionException(e.getMessage());
		}

		try {
			if (headsetToConnect.equals("all")) {
				System.out.println("-------------------------");
				System.out
						.println("Trying to connect all headset types. You will see messages indicating success, on successful connection.");
				/* start MuseIO here */
				
				try {
					startMuseIO();
				} catch (IOException e) {
					e.printStackTrace();
				}
				muse = new MuseConnector(this);
				mw = new MindWaveConnector(this);
				epoc = new EpocConnector(this);
				headsets.add(muse);
				headsets.add(mw);

			} else if (headsetToConnect.equals("mw")) {
				System.out
				.println("Trying to connect only a single MindWave. You will see messages indicating success, on successful connection.");
				mw = new MindWaveConnector(this);
				System.out.println("Successfully connected to a MindWave");
				headsets.add(mw);
			} else if (headsetToConnect.equals("muse")) {
				System.out.println("Trying to connect only a single Muse. You will see messages indicating success, on successful connection.");
				muse = new MuseConnector(this);
				System.out.println("Successfully connected to a Muse");
				headsets.add(muse);
			} else if (headsetToConnect.equals("tsk")) {
				System.out
				.println("Trying to connect only a single TSK. You will see messages indicating success, on successful connection.");

			} else if (headsetToConnect.equals("epoc")) {
				System.out
				.println("Trying to connect only a single Epoc. You will see messages indicating success, on successful connection.");
				epoc = new EpocConnector(this);

			}
		} catch (HeadsetConnectionException hce) {
			System.out
					.println("Could not connect to a Mindwave headset. Make sure one is paired and on.");
		}

	}

	public ArrayList<HeadsetInfo> getAllHeadsetInfo() {
		ArrayList<HeadsetInfo> output = new ArrayList<HeadsetInfo>();
		for (HeadsetConnector headset : headsets) {
			output.add(headset.getHeadsetInfo());
		}
		return output;
	}

	public HeadsetInfo getHeadsetInfo(int headsetNum) {
		return headsets.get(headsetNum).getHeadsetInfo();
	}

	public void messageReceived(MqttMessage message) {
		System.out.println(message.getPayload());
		for (MqttMessageReceiver server : servers) {
			server.messageReceived(message);
		}
	}

	public void pushDataToAllServers(String string) {
		for (ServerConnection server : servers) {
			server.messageReceived(new MqttMessage(string.getBytes()));
		}
	}

	public String getClientId() {
		return clientId;
	}

}

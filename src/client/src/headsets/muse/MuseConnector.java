package headsets.muse;

import headsets.HeadsetConnectionException;
import headsets.HeadsetConnector;

import java.net.SocketException;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

import core.ClientMain;
import core.HeadsetInfo;

public class MuseConnector implements HeadsetConnector {

	private OSCPortIn oscReceiver;
	private HeadsetInfo headsetInfo;
	private String data;
	private final ClientMain client;
	private boolean headsetRegistered = false;

	public MuseConnector(final ClientMain client)
			throws HeadsetConnectionException {
		this.client = client;
		try {
			oscReceiver = new OSCPortIn(5001);
		} catch (SocketException e) {
			System.out.println("-----------------> Closing Muse connection...");
			oscReceiver.close();
			throw new HeadsetConnectionException(
					"Could not connect to Muse. Exception message received: "
							+ e.getMessage());
		}

		OSCListener dataListener = new OSCListener() {

			public void acceptMessage(java.util.Date time, OSCMessage message) {
				String dataString = message.getArguments().toString();
				data = dataString.substring(1, dataString.length() - 1);
				if (!headsetRegistered) {
					client.pushDataToAllServers("headset:muse");
					headsetRegistered = true;
				}
				client.pushDataToAllServers(data);
			}
		};

		OSCListener configListener = new OSCListener() {
			public void acceptMessage(java.util.Date time, OSCMessage message) {
				try {
					String[] configParts = message.getArguments().toString()
							.split((","));
					headsetInfo = new HeadsetInfo();
					headsetInfo.setChannelLocation(1, "TP9");
					headsetInfo.setChannelLocation(2, "FP1");
					headsetInfo.setChannelLocation(3, "FP2");
					headsetInfo.setChannelLocation(4, "TP10");
					headsetInfo.setNumChannels(4);
					headsetInfo.setNotchFreq(Integer.parseInt(configParts[4]
							.split(":")[1]));
					headsetInfo.setEffectiveReadRate(Double
							.parseDouble(configParts[8].split(":")[1]));
				} catch (Exception e) {
					// say something bad because something bad happened :(
					e.printStackTrace();
				}
			}
		};

		oscReceiver.addListener("/muse/config", configListener);
		// oscReceiver.addListener("/muse/eeg", dataListener);
		oscReceiver.addListener("/muse/elements/beta_absolute", dataListener);
		oscReceiver.startListening();
	}

	public HeadsetInfo getHeadsetInfo() {
		return headsetInfo;
	}

}

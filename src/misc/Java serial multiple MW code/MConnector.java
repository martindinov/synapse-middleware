package c3nl.eeg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jssc.SerialPortException;
import c3nl.Connector;

/**
 * Sets up multiple Connector connections to multiple MindWaves
 * 
 * @author Martin Dinov <m.dinov13@imperial.ac.uk>
 */

public class MConnector {

	private HashMap<Integer, Connector> connectors;

	public MConnector() {
		connectors = new HashMap<Integer, Connector>();
	}

	public void addConnector(MindWaveListener listener, boolean debugMessages) {
		// starting with tty.MindWaveMobile-DevA, start enumerating the tty
		// files until we can connect to one
		File[] filesList = (new File("/dev")).listFiles();
		for (File file : filesList) {
			String[] temp = file.getAbsolutePath().split("-");
			String deviceNumberString = temp[temp.length - 1];
			try {
				int deviceNumber = Integer.parseInt(deviceNumberString);
				try {
					connectors.put(deviceNumber, new Connector(
							"/dev/tty.MindWaveMobile-DevA-" + deviceNumber,
							listener, debugMessages));
				} catch (SerialPortException e) {
					// "Port busy", move on to next one.
					continue;
				}
			} catch (NumberFormatException e) {
				/*
				 * We should only come here if this is the device
				 * "tty.MindWaveMobile-DevA", where the final token from temp is
				 * "DevA", which won't parse into an int.
				 */
				try {
					connectors.put(0, new Connector(
							"/dev/tty.MindWaveMobile-DevA", listener,
							debugMessages));
				} catch (SerialPortException e1) {
					// "Port busy", move on to next one.
					continue;
				}
			}
		}
	}

	public ArrayList<Connector> getConnectorList() {
		return new ArrayList<Connector>(connectors.values());
	}

	public Connector getConnector(int deviceNumber) {
		return connectors.get(deviceNumber);
	}

	public HashMap<Integer, Connector> getConnectorsHashMap() {
		return connectors;
	}
}

package headsets.mindwave;

import headsets.HeadsetConnectionException;
import headsets.HeadsetConnector;

import java.io.File;

import jssc.SerialPort;
import jssc.SerialPortException;
import utilities.ApplicationContext;
import core.ClientMain;
import core.HeadsetInfo;

public class MindWaveConnector extends Thread implements HeadsetConnector {

	private final static int CONNECT = 0xc0;
	private final static int DISCONNECT = 0xc1;
	private final static int AUTOCONNECT = 0xc2;
	private final static int ASIC_EEG_POWER = 0x83;
	private final static int SYNC = 0xAA;
	private final static int EXCODE = 0x55;
	private final static int POOR_SIGNAL = 0x02;
	private final static int ATTENTION = 0x04;
	private final static int MEDITATION = 0x05;
	private final static int BLINK = 0x16;
	private final static int RAW = 0x80;
	private final static int HEADSET_CONNECTED = 0xd0;
	private final static int HEADSET_NOT_FOUND = 0xd1;
	private final static int HEADSET_DISCONNECTED = 0xd2;
	private final static int REQUEST_DENIED = 0xd3;
	private final static int STANDBY_SCAN = 0xd4;
	// private final static String STATUS_CONNECTED = "connected";
	// private final static String STATUS_SCANNING = "scanning";
	// private final static String STATUS_STANDBY = "standby";
	private int attention;
	private int meditation;
	private int blink;
	private int poorSignal;
	private int delta, theta, lowAlpha, highAlpha, lowBeta, highBeta, lowGamma,
			midGamma;
	private SerialPort serialPort = null;
	private boolean running = false;
	private String devicePath;
	private boolean debugMessages;
	private int[] raw = new int[512];
	private int rawIndex = 0;
	private HeadsetInfo headsetInfo = new HeadsetInfo();
	private ClientMain client;
	private boolean headsetRegistered = false;

	public static int calculateChecksum(int[] payload) {
		int calculatedChecksum = 0;
		for (int i = 0; i < payload.length; i++) {
			calculatedChecksum += payload[i];
		}
		calculatedChecksum &= 0xFF;
		calculatedChecksum = ~calculatedChecksum & 0xFF;
		return calculatedChecksum;
	}

	public void stopReading() {
		running = false;
	}

	private void triggerAttentionEvent(int attentionLevel) {
		try {
			if (debugMessages) {
				System.err.println(devicePath + ":triggerAttentionEvent("
						+ attentionLevel + ")");
			}
			client.pushDataToAllServers("attention:" + attentionLevel);
		} catch (Exception e) {
			System.err
					.println("Disabling attentionEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerMeditationEvent(int meditationLevel) {
		try {
			if (debugMessages) {
				System.err.println(devicePath + ":triggerMeditationEvent("
						+ meditationLevel + ")");
			}
			client.pushDataToAllServers("meditation:" + meditationLevel);
		} catch (Exception e) {
			System.err
					.println("Disabling meditationEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerPoorSignalEvent(int poorSignalLevel) {
		try {
			if (debugMessages) {
				System.err.println("poorSignalEvent(" + poorSignalLevel + ")");
			}
			client.pushDataToAllServers("poorSignalLevel:" + poorSignalLevel);
		} catch (Exception e) {
			System.err
					.println("Disabling poorSignalEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerBlinkEvent(int blinkStrength) {
		try {
			if (debugMessages) {
				System.err.println("triggerBlinkEvent(" + blinkStrength + ")");
			}
			client.pushDataToAllServers("blinkStrength:" + blinkStrength);
		} catch (Exception e) {
			System.err.println("Disabling blinkEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerEEGEvent(int delta, int theta, int low_alpha,
			int high_alpha, int low_beta, int high_beta, int low_gamma,
			int mid_gamma) {
		try {
			if (debugMessages) {
				System.err.println("triggerEEGEvent(" + delta + "," + theta
						+ "," + low_alpha + "," + high_alpha + "," + low_beta
						+ "," + high_beta + "," + low_gamma + "," + mid_gamma
						+ ")");
			}
			client.pushDataToAllServers("bandData:" + delta + theta + low_alpha
					+ high_alpha + low_beta + high_beta + low_gamma + mid_gamma);
		} catch (Exception e) {
			System.err.println("Disabling EEGEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerRawEvent(int[] values) {
		try {
			if (debugMessages) {
				System.err.println(devicePath + ":triggerRawEvent(" + values
						+ ")");
				System.out.println("------------");
				for (int i : values) {
					System.out.print(i + ",");
				}
				System.out.println("------------");
			}

			String vals = "";
			for (int i : values) {
				vals += i + ",";
			}

			vals = vals.substring(0, vals.length() - 2);

			client.pushDataToAllServers("raw:" + vals);
		} catch (Exception e) {
			System.err.println("Disabling rawEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void startReading() throws SerialPortException {
		running = true;
		new Thread() {
			public void run() {

				while (running) {
					if (debugMessages) {
					}
					try {
						if (serialPort.readIntArray(1)[0] == SYNC) {
							if (serialPort.readIntArray(1)[0] == SYNC) {
								int payloadLength = 0;
								while (true) {
									payloadLength = serialPort.readIntArray(1)[0];

									if (payloadLength > 170) {
										break;
									} else if (payloadLength == 170) {
										continue;
									} else {
										break;
									}
								}
								if (payloadLength > 170) {
									continue;
								} else {
								}

								int[] payload = serialPort
										.readIntArray(payloadLength);

								int receivedChecksum = serialPort
										.readIntArray(1)[0];

								if (calculateChecksum(payload) == receivedChecksum) {
									/*
									 * Hooray! Now we parse all the DataRows
									 * from the packet payload.
									 */

									int excodeCount = 0;
									int vLength = 1;

									for (int j = 0; j < payloadLength;) {
										if (payload[j] == EXCODE) {
											excodeCount++;
											j++;
											continue;
										}

										int code = payload[j++];
										/* multi-byte value field coming up */
										if (code >= 0x80) {
											vLength = payload[j++];
											switch (code) {
											case RAW:
												if (debugMessages) {
													// System.out.println("RAW!");
												}
												int highOrderByte = payload[j];
												int lowOrderByte = payload[j + 1];
												int curRawValue = highOrderByte
														* 256 + lowOrderByte;
												if (curRawValue >= 32768)
													curRawValue -= 65536;
												j += 2;
												raw[rawIndex] = curRawValue;
												rawIndex++;
												if (rawIndex == (raw.length - 1)) {
													rawIndex = 0;
													triggerRawEvent(raw);
												}
												break;
											case ASIC_EEG_POWER:
												delta = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												theta = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												lowAlpha = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												highAlpha = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												lowBeta = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												highBeta = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												lowGamma = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												midGamma = ((payload[j++] & 0xF) << 16)
														| ((payload[j++] & 0xFF) << 8)
														| (payload[j++] & 0xFF);
												triggerEEGEvent(delta, theta,
														lowAlpha, highAlpha,
														lowBeta, highBeta,
														lowGamma, midGamma);
												break;
											case HEADSET_CONNECTED:
												break;
											case HEADSET_NOT_FOUND:
												break;
											case HEADSET_DISCONNECTED:
												break;
											case REQUEST_DENIED:
												break;
											case STANDBY_SCAN:
												break;
											}
										} else { // single byte value field
											if (j == payload.length) {
												break; // out of payload for
														// loop
											}
											int value = payload[j++];
											switch (code) {
											case POOR_SIGNAL:
												triggerPoorSignalEvent(value);
												break;
											case ATTENTION:
												triggerAttentionEvent(value);
												break;
											case MEDITATION:
												triggerMeditationEvent(value);
												break;
											case BLINK:
												triggerBlinkEvent(value);
												break;
											default:
												// triggerAttentionEvent(value);
												break;
											}
										}
									}
								}
							} else {
								if (debugMessages) {
									System.out
											.println("Failed to get a 2nd SYNC byte");
									continue; // continue to next byte in
												// dataBunch
								}
							}
						}
					} catch (SerialPortException e) {
						// maybe throw this back up to the parent thread and
						// handle somehow
						e.printStackTrace();
					} catch (NullPointerException e) {
						continue;
					}
				}
			}
		}.start();
	}

	public MindWaveConnector(final ClientMain client)
			throws HeadsetConnectionException {

		this.client = client;

		/* TODO: see if we can get battery and other info from MW Mobile somehow */
		headsetInfo.setNumChannels(1);
		headsetInfo.setChannelLocation(1, "FP1");

		/* now open the actual serial path to device */

		boolean devicePathSet = false;

		if (ApplicationContext.isUnix() || ApplicationContext.isMac()
				|| ApplicationContext.isSolaris()) {
			File[] filesList = (new File("/dev")).listFiles();
			for (File file : filesList) {
				String devicePath = file.getAbsolutePath();
				if (devicePath.contains("MindWaveMobile")) {
					serialPort = new SerialPort(devicePath);
					try {
						System.out.println("Trying to connect to: "
								+ devicePath);
						if (serialPort.openPort())
							System.out
									.println("Success connecting to MindWave");
						break;
					} catch (SerialPortException e) {
						continue;
					}
				}
			}

		} else if (ApplicationContext.isWindows()) {
			for (int i = 0; i < 13; i++) {
				serialPort = new SerialPort("COM" + i);
				try {
					System.out.println("Trying to connect to: " + "COM" + i);
					if (serialPort.openPort())
						System.out.println("Success connecting to MindWave");
					break;
				} catch (SerialPortException e) {
					continue;
				}
			}
		}

		try {
			serialPort.setParams(57600, 8, 1, 0);
			Thread.sleep(1000);
		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
		}

		/* Successfully connected to MW headset. Send headset identifying msg to servers */
		if (!headsetRegistered) {
			client.pushDataToAllServers("headset:mw");
			headsetRegistered = true;
		}

		try {
			startReading();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	public HeadsetInfo getHeadsetInfo() {
		return headsetInfo;
	}

}

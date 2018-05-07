package c3nl;

import jssc.SerialPort;
import jssc.SerialPortException;
import c3nl.eeg.MindWaveListener;

/**
 * Sets up a connection to a MindWave device and parses packets
 * 
 * @author Martin Dinov <m.dinov13@imperial.ac.uk>
 */

public class Connector extends Thread {

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
	private final String devicePath;
	private MindWaveListener listener;
	private boolean debugMessages;
	private int[] raw = new int[512];
	private int rawIndex = 0;

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
				System.err.println(devicePath + ":triggerAttentionEvent(" + attentionLevel
						+ ")");
			}
			listener.attentionEvent(attentionLevel);
		} catch (Exception e) {
			System.err
					.println("Disabling attentionEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerMeditationEvent(int meditationLevel) {
		try {
			if (debugMessages) {
				System.err.println(devicePath + ":triggerMeditationEvent(" + meditationLevel
						+ ")");
			}
			listener.meditationEvent(meditationLevel);
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
			listener.poorSignalEvent(poorSignalLevel);
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
			listener.blinkEvent(blinkStrength);
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
			listener.eegEvent(delta, theta, low_alpha, high_alpha, low_beta,
					high_beta, low_gamma, mid_gamma);
		} catch (Exception e) {
			System.err.println("Disabling EEGEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	private void triggerRawEvent(int[] values) {
		try {
			if (debugMessages) {
				System.err.println(devicePath + ":triggerRawEvent(" + values + ")");
				System.out.println("------------");
				for (int i : values) {
					System.out.print(i + ",");
				}
				System.out.println("------------");
			}
			listener.rawEvent(values);
		} catch (Exception e) {
			System.err.println("Disabling rawEvent()  because of an error.");
			e.printStackTrace();
		}
	}

	public void startReading() throws SerialPortException {
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
												int curRawValue = highOrderByte*256 + lowOrderByte;
												if(curRawValue >= 32768) curRawValue -= 65536;
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

	public Connector(String devicePath, MindWaveListener listener,
			boolean debugMessages) throws SerialPortException {
		this.debugMessages = debugMessages;
		this.devicePath = devicePath;
		System.out.println("----------> " + devicePath);
		this.listener = listener;
		serialPort = new SerialPort(devicePath);
		serialPort.openPort();
//		serialPort.writeByte((byte)0x11);
//		serialPort.writeByte((byte)0x02);
		serialPort.setParams(57600, 8, 1, 0);
//		serialPort.setParams(57600*2, 8, 1, 0);
	}

	public int getAttention() {
		return attention;
	}

	public int getMeditation() {
		return meditation;
	}

	public int getBlink() {
		return blink;
	}

	@Override
	public void run() {
		try {
			startReading();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

}

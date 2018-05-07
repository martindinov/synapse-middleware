package headsets.epoc;

import java.nio.file.Paths;

import core.ClientMain;
import core.HeadsetInfo;
import headsets.HeadsetConnector;

public class EpocConnector implements HeadsetConnector {

	static {
		String path = Paths.get(".").toAbsolutePath().normalize().toString();
		System.out.println("Loading libEmokit.dylib...");
		try {
//			System.loadLibrary("libEmokit.jnilib");
			System.out.println("Path = " + path);
			System.load(path + "/libEmokit.jnilib");
			
			
		} catch (Exception e) {
			System.err.println("Breaking here");
			e.printStackTrace();
			System.err.println("Breaking here");
		} finally {
			System.out.println("Successfully loaded libEmokit.new.dylib.");
		}

	}

	private native void helloTest();
	// private native int getChannelValue(String channel); //for JNI
	private native void readData();
	private native void connectHeadsets();
	private native void disconnectHeadsets();
	private native void readQuality();
	private native void readBattery();

	private final ClientMain client;

	public EpocConnector(final ClientMain client) {
		this.client = client;
		this.helloTest();
		this.helloTest();
		this.helloTest();
		System.out.println("---1---");
		this.connectHeadsets(); 
		System.out.println("---2---");
		this.readData();
		System.out.println("---3---");
		this.disconnectHeadsets();
	}

	public HeadsetInfo getHeadsetInfo() {
		return null;
	}

}

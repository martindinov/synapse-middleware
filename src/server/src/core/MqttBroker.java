package core;

import java.io.File;
import java.io.IOException;

import utilities.ApplicationContext;
import exceptions.NoBrokerAvailableException;
import exceptions.OSNotSupportedException;

public class MqttBroker {
	public static final int DEFAULT_PORT = 1337;
	private static Process process;
	private static String uri;
	
	public static void initializeNewMosquito(int port) throws IOException, OSNotSupportedException{
		File f = new File(getMosquittoPath());
		if(!f.isFile())
			throw new IOException(f.getAbsolutePath());
		process = new ProcessBuilder(f.getAbsolutePath(), "-p", String.valueOf(port)).start();
		uri = "tcp://localhost:" + String.valueOf(port);
	}

	public static String getMosquittoPath() throws OSNotSupportedException {
		if(ApplicationContext.isWindows()){
			return "mosquitto/win/mosquitto.exe";
		}else if(ApplicationContext.isUnix()){
			return "mosquitto/unix/mosquitto.os";
		}else if(ApplicationContext.isMac()) {
			return "mosquitto/osx/sbin/mosquitto";
		}else{
			throw new OSNotSupportedException("Operating System " + System.getProperty("os.name") + " not supported.");
		}
	}
	
	public static void setExistingBroker(String uri){
		MqttBroker.uri = uri;
	}
	
	public String getUrl(){
		return uri;
	}
	
	public static String getBrokerURI() throws NoBrokerAvailableException{
		return uri;
	}
	
	public static void dispose(){
		if(process!=null)
			process.destroy();
	}
}

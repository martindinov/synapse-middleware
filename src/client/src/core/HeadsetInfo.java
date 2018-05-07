package core;

import java.util.HashMap;

public class HeadsetInfo {

	private int numChannels = 0;
	private HashMap<Integer, String> chanLocs = new HashMap<Integer, String>();
	private double readRate;
	private double battery;
	private int notchFreq = 0;

	public HeadsetInfo() {
	
	}

	public int getNotchFreq() {
		return notchFreq;
	}
	
	public void setNotchFreq(int notchFreq) {
		this.notchFreq = notchFreq;
	}
	
	public double getBattery() {
		return battery;
	}
	
	public void setBattery(double battery) {
		this.battery = battery;
	}
	
	public int getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
	}

	public void setChannelLocation(int chanNumber, String location) {
		this.chanLocs.put(chanNumber, location);
	}

	public HashMap<Integer, String> getChanLocs() {
		return chanLocs;
	}

	public String getChanLocsAsString() {
		/* TODO: Decide on best way to return this formatted really simply */
		return chanLocs.toString();
	}
	
	public void setEffectiveReadRate(double readRate) {
		this.readRate = readRate;
	}
	
	public double getEffectiveReadRate() {
		return readRate;
	}

}

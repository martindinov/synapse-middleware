package eeghelper;

import java.util.ArrayList;
import java.util.Collections;

public class MuseHelper extends EEGHelper {

	private ArrayList<Double> val1History = new ArrayList<Double>();
	private ArrayList<Double> val2History = new ArrayList<Double>();
	private ArrayList<Double> val3History = new ArrayList<Double>();
	private ArrayList<Double> val4History = new ArrayList<Double>();

	private int pos = 0;
	private double attention = 0;

	public String transformInput(String clientId, String input) {
		try{
			String[] parts = input.split(":");
			if(parts[0].equalsIgnoreCase("attention")){
				attention = Double.valueOf(parts[1]);			
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
		return "attention:" + attention;
//		pos++;
//
//		int semicolonPos = input.indexOf(",");
//		String data = input.substring(semicolonPos + 1, input.length());
//		// System.out.println("--->" + data);
//		String[] values = data.split(", ");
//		double val1 = Double.valueOf(values[0]);
//		double val2 = Double.valueOf(values[1]);
//		double val3 = Double.valueOf(values[2]);
//		double val4 = Double.valueOf(values[3]);
//
//		/* reset value histories to keep the constant sorting faster */
//		if ((pos % 100 == 0)) {
//			pos = 0;
//		}
//
//		val1History.add(pos, val1);
//		val2History.add(pos, val2);
//		val3History.add(pos, val3);
//		val4History.add(pos, val4);
//
//		/* TODO: can rewrite this without need for sorting at all. Later. */
//
//		Collections.sort(val1History);
//		double val1Max = val1History.get(val1History.size() - 1);
//		double val1Min = val1History.get(0);
//
//		Collections.sort(val2History);
//		double val2Max = val2History.get(val2History.size() - 1);
//		double val2Min = val2History.get(0);
//
//		Collections.sort(val3History);
//		double val3Max = val3History.get(val3History.size() - 1);
//		double val3Min = val3History.get(0);
//
//		Collections.sort(val4History);
//		double val4Max = val4History.get(val4History.size() - 1);
//		double val4Min = val4History.get(0);
//
//		/* now compute values in range 0-1 */
//		val1 = ((val1 - val1Min) / (val1Max - val1Min));
//		val2 = ((val2 - val2Min) / (val2Max - val2Min));
//		val3 = ((val3 - val3Min) / (val3Max - val3Min));
//		val4 = ((val4 - val4Min) / (val4Max - val4Min));
//
//		/* left beta asymmetry calculation */
//		double sum = 0;
//		for (Double val : val2History) {
//			sum += val;
//		}
//
//		double leftPower = sum / val2History.size();
//
//		sum = 0;
//		for (Double val : val3History) {
//			sum += val;
//		}
//
//		double rightPower = sum / val3History.size();
//
//		double temp = Math.min(val2Min, val3Min);
//		double normedPowerAsymmetry = ((leftPower - rightPower) - temp)
//				/ ((Math.max(val2Max, val3Max) - temp));
//
//		return "" + normedPowerAsymmetry;
		// return val1 + ", " + val2 + ", " + val3 + ", " + val4;

	}
}
/*
 * for normalized [0-1] power values, uncomment
 */

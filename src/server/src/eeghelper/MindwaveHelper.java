package eeghelper;

public class MindwaveHelper extends EEGHelper {

	private double attention = 0;
	
	public String transformInput(String clientId, String input) {

		String[] inputParts = input.split(":");
		/* later on deal with different types of inputs differently. */
		if (inputParts[0].equals("attention")) {
			attention = Double.valueOf(inputParts[1]);
		}

		return "attention:" + (attention / 100.0);
	}
}

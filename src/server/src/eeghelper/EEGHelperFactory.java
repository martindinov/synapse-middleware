package eeghelper;

public class EEGHelperFactory {
	public static EEGHelper createHelper(String eegName) {
		switch (eegName) {
		case "mw":
			return new MindwaveHelper();
		case "muse":
			return new MuseHelper();
		case "tsk":
			return new TSKHelper();
		default:
			System.err.println("Helper type not found!");
			return null;
		}
	}
}

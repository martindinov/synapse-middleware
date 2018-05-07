package processing;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class dsp {

	public static double index2Freq(int i, double samples, int nFFT) {
		return (double) i * (samples / nFFT / 2.);
	}

	public static int freq2Index(double freq, double samples, int nFFT) {
		return (int) (freq / (samples / nFFT / 2.0));
	}

	/*
	 * Really crappy but for now probably functional rectangular filter. NOT YET
	 * TESTED. Might not ever be necessary. Poor code.
	 */
	public Double[] bandpassInFrequencyDomain(double[] data, int srate, double lowFreq,
			double highFreq) {
		FastFourierTransformer transformer = new FastFourierTransformer(
				DftNormalization.STANDARD);
		Complex[] fData = transformer.transform(data, TransformType.FORWARD);
		ArrayList<Double> tempOutput = new ArrayList<Double>();
		for (Complex c : fData) {
			tempOutput.add(c.getReal());
		}
		Double[] output = tempOutput.toArray(new Double[tempOutput.size()]);
		return Arrays.copyOfRange(output, freq2Index(15, srate, data.length),
				freq2Index(30, srate, data.length));
	}

	public double getTwoChanAsymmetry(double[] filtDataChan1, double[] filtDataChan2) {
		return 0;
		//avgFiltData1 = 
		//return avgFiltData1 / avgFiltData2;
	}
}

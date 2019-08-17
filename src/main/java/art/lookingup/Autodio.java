package art.lookingup;

import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.studio.LXStudio;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;

import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class Autodio extends LXComponent {

  public CompoundParameter bestLow = new CompoundParameter("bestLow", 1, 0, 1);
  public CompoundParameter bestMid = new CompoundParameter("bestMid", 1, 0, 1);
  public CompoundParameter bestHigh = new CompoundParameter("bestHigh", 1, 0, 1);

    LXStudio lxs;

    public static final int windowSize = 100;
    private static final Logger logger = Logger.getLogger(Autodio.class.getName());

    public double[] bandVar;
    public double bestVal;
    double[][] window;
    int next;

    static Variance variance = new Variance(false);

    public Autodio(LXStudio lxs) {
	super(lxs, "autodio");
	addParameter(bestLow);
	addParameter(bestMid);
	addParameter(bestHigh);

	GraphicMeter eq = lxs.engine.audio.meter;

	this.lxs = lxs;
	this.window = new double[eq.fft.getNumBands()][];
	this.bandVar = new double[eq.fft.getNumBands()];
	for (int i = 0; i < eq.numBands; i++) {
	    this.window[i] = new double[windowSize];
	}
    }

    public void update() {
	GraphicMeter eq = lxs.engine.audio.meter;

	// EQ size 0.5598034 16 44100 512
	// logger.info("EQ size " +
	// 	    eq.fft.getBandOctaveRatio() + " " +
	// 	    eq.fft.getNumBands() + " " +
	// 	    eq.fft.getSampleRate() + " " +
	// 	    eq.fft.getSize());

	for (int i = 0; i < eq.numBands; i++) {
	    window[i][next] = eq.bands[i].getValue();
	}

	next++;
	next %= eq.fft.getNumBands();
	double maxV = 0;
	double maxB = 0;

 	for (int i = 0; i < eq.numBands; i++) {
	    double v = variance.evaluate(window[i]);
	    bandVar[i] = v;

	    if (v > maxV) {
		maxV = v;
		maxB = eq.bands[i].getValue();
	    }
	}

	bestVal = maxB;
	
	// byte[] fftAudioTex = new byte[1024];
	// for (int i = 0; i < 256; i++) {
	//     int fftValue = (int) (256 * eq.fft.get(i));
	//     // Audio buffer is only 512 bytes, so fft is 256.  Let's
	//     // just add duplicate values.
	//     fftAudioTex[i] = (byte) fftValue;
	//     //fftAudioTex[i*2+1] = (byte) fftValue;
	// }
	// float[] audioSamples = eq.getSamples();
	// for (int i = 512; i < 1024; i++) {
	//     int audioValue = (int) (256 * audioSamples[i-512]);
	//     fftAudioTex[i] = (byte) audioValue;
	// }
	// ByteBuffer audioTexBuf = ByteBuffer.wrap(fftAudioTex);
    }
}

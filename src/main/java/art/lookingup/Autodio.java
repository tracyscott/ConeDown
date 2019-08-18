package art.lookingup;

import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.studio.LXStudio;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;

import java.util.logging.Logger;

import org.apache.commons.math3.stat.StatUtils;

public class Autodio extends LXComponent {

  public CompoundParameter bestLow = new CompoundParameter("bestLow", 1, 0, 1);
  public CompoundParameter bestMid = new CompoundParameter("bestMid", 1, 0, 1);
  public CompoundParameter bestHigh = new CompoundParameter("bestHigh", 1, 0, 1);

    LXStudio lxs;
    GraphicMeter eq;

    public static final int windowSize = 100;
    private static final Logger logger = Logger.getLogger(Autodio.class.getName());

    public double[] bandVar;

    double[][] window;
    int next;

    public Autodio(LXStudio lxs) {
	super(lxs, "autodio");
	addParameter(bestLow);
	addParameter(bestMid);
	addParameter(bestHigh);

	this.eq = lxs.engine.audio.meter;

	this.lxs = lxs;
	this.window = new double[eq.fft.getNumBands()][];
	this.bandVar = new double[eq.fft.getNumBands()];
	for (int i = 0; i < eq.numBands; i++) {
	    this.window[i] = new double[windowSize];
	}
    }

    public void update() {

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

	bestLow.setValue(getBest(0, 5));
	bestMid.setValue(getBest(5, 11));
	bestHigh.setValue(getBest(11, 16));
    }

    double getBest(int from, int to) {
	double maxV = 0;
	int maxI = 0;

 	for (int i = from; i < to; i++) {
	    double v = StatUtils.variance(window[i]);
	    bandVar[i] = v;

	    if (v > maxV) {
		maxV = v;
		maxI = Math.max(i, maxI);
	    }
	}
	return eq.bands[maxI].getValue() / StatUtils.max(window[maxI]);
    }
}

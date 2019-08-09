package art.lookingup.patterns.play;

import static processing.core.PConstants.P2D;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Projection;
import art.lookingup.patterns.RenderImageUtil;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;

import processing.core.PApplet;
import processing.core.PGraphics;

abstract public class Pattern extends LXPattern {
    // "" for builtin, P2D or P3D for opengl
    public static final String gtype = "";

    public static final int superMult = 4;

    public final PApplet app;

    private final int width;
    private final int height;

    public final CompoundParameter speedKnob =
	new CompoundParameter("GlobalSpeed", 1, 0, 2)
        .setDescription("Varies global speed.");

    public static Projection standardProjection;

    boolean init;
    float current;
    float elapsed;

    // TODO eliminate this list; it's only one element
    List<Fragment> frags = new ArrayList<>();

    public void addFragment(FragmentFactory ff) {
	Fragment f = ff.create(lx, width * superMult, height * superMult);
	frags.add(f);
	f.registerParameters((LXParameter cp)->{
		addParameter(cp);
	    });
    }

    public Pattern(LX lx, PApplet app, int width, int height) {
	super(lx);

	synchronized (Pattern.class) {
	    if (standardProjection == null) {
		standardProjection = new Projection(lx.getModel(), superMult);
	    }
	}

	this.app = app;
	this.width = width;
	this.height = height;

	addParameter(speedKnob);
    }

    @Override
    public void run(double deltaMs) {
	render(deltaMs);
    }

    @Override
    public void onActive() {
	// Hmmm
    }

    @Override
    public void onInactive() {
	// Hmm
    }

    static PGraphics createGraphics(PApplet app, int width, int height) {
	if (gtype == "") {
	    return app.createGraphics(width, height);
	}
	return app.createGraphics(width, height, gtype);
    }

    void render(double deltaMs) {
	current += (float)(speedKnob.getValue() * (deltaMs / 1e3));

	if (!init) {
	    init = true;

	    for (Fragment f : frags) {
		f.create(this);
	    }

	    setup();

	    for (Fragment f : frags) {
		f.setup();
	    }
	}

	preDraw(current - elapsed);

	frags.get(frags.size()-1).image.loadPixels();

	// frags.get(frags.size()-1).image.save(String.format("/Users/jmacd/Desktop/dump/canvas-%s.png", counter++));
	
	for (LXPoint p : lx.getModel().points) {
	    colors[p.index] = standardProjection.computePoint(p.index, frags.get(frags.size()-1).image);
	}

	elapsed = current;
    }

    // static int counter;

    public void setup() {
    }

    void preDraw(float vdelta) {
	for (Fragment f : frags) {
	    f.render(vdelta);
	}
    }
}

package art.lookingup.patterns.play;

import static processing.core.PConstants.P2D;

import art.lookingup.CXPoint;
import art.lookingup.ConeDown;
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

    public static final int superSampling = 1; // ConeDown.MAX_SUPER_SAMPLING;

    public final PApplet app;

    private final int width;
    private final int height;

    public final CompoundParameter speedKnob =
	new CompoundParameter("GlobalSpeed", 1, 0, 2)
        .setDescription("Varies global speed.");

    boolean init;
    float current;
    float elapsed;
    Fragment frag;

    public void setFragment(FragmentFactory ff) {
	this.frag = ff.create(lx, width * superSampling, height * superSampling);
	this.frag.registerParameters((LXParameter cp)->{
		addParameter(cp);
	    });
    }

    public Pattern(LX lx, PApplet app, int width, int height) {
	super(lx);

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

	    frag.create(this);

	    setup();

	    frag.setup();
	}

	preDraw(current - elapsed);

	frag.image.loadPixels();

	// if (counter++ % 100 == 0) {
	//     frag.image.save(String.format("/Users/jmacd/Desktop/dump/canvas-%s.png", counter++));
	// }
	
	for (LXPoint p : lx.getModel().points) {
	    colors[p.index] = ConeDown.getProjection(superSampling).
		computePoint((CXPoint) p, frag.image, 0, 0);
	}

	elapsed = current;
    }
    
    // static int counter;

    public void setup() {
    }

    void preDraw(float vdelta) {
	frag.render(vdelta);
    }
}

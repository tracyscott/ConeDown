package art.lookingup.patterns.play;

import static processing.core.PConstants.P2D;

import art.lookingup.CXPoint;
import art.lookingup.ConeDown;
import art.lookingup.Projection;
import art.lookingup.patterns.RenderImageUtil;
import art.lookingup.colors.Colors;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
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

    public static final int superSampling = ConeDown.MAX_SUPER_SAMPLING;

    public final PApplet app;

    private final int width;
    private final int height;

    public final CompoundParameter speedKnob =
	new CompoundParameter("GlobalSpeed", 1, 0, 2)
        .setDescription("Varies global speed.");

    public final DiscreteParameter saturateKnob =
	new DiscreteParameter("Saturation", 100, 0, 101)
        .setDescription("Saturates.");

    boolean init;
    float current;
    float elapsed;
    Fragment frag;
    float[] rgb2hsb;

    public void setFragment(FragmentFactory ff) {
	this.frag = ff.create(lx, width * superSampling, height * superSampling);
	this.frag.registerParameters((Parameter p)->{
		addParameter(p.lxp);
	    });
    }

    public Pattern(LX lx, PApplet app, int width, int height) {
	super(lx);

	this.app = app;
	this.width = width;
	this.height = height;
	this.rgb2hsb = new float[3];

	addParameter(speedKnob);
	addParameter(saturateKnob);
    }

    @Override
    public void run(double deltaMs) {
	render(deltaMs);
    }

    static PGraphics createGraphics(PApplet app, int w, int h) {
	if (gtype == "") {
	    return app.createGraphics(w, h);
	}
	return app.createGraphics(w, h, gtype);
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

	Projection projection = ConeDown.getProjection(superSampling);
	int sat = (int) saturateKnob.getValue();

	for (LXPoint p : lx.getModel().points) {
	    CXPoint cxp = (CXPoint)p;
	    if (cxp.panel == null) {
		// TODO the interior lights!
		continue;
	    }
	    colors[p.index] = projection.computePoint(cxp, frag.image, 0, 0);

	    if (sat < 100) {
		int c = colors[p.index];
		Colors.RGBtoHSB(c, rgb2hsb);
		int a = Colors.alpha(c);
		rgb2hsb[1] = Math.min(sat/100f, rgb2hsb[1]);
		c = Colors.HSBtoRGB(rgb2hsb);
		colors[p.index] = Colors.rgba(Colors.red(c), Colors.green(c), Colors.blue(c), a);
	    }
	}

	elapsed = current;
    }

    public void setup() {
    }

    void preDraw(float vdelta) {
	frag.render(vdelta);
    }
}

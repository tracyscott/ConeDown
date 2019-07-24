package art.lookingup.patterns.play;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.RenderImageUtil;

import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

abstract public class Pattern extends LXPattern {
    public final PApplet app;

    public PGraphics graph;

    public final int width;
    public final int height;

    public final CompoundParameter speedKnob =
	new CompoundParameter("GlobalSpeed", 1, 0, 10)
        .setDescription("Varies global speed.");

    boolean init;
    float current;
    float elapsed;

    // @@@
    Fragment frag;
    public void addFragment(Fragment f) {
	this.frag = f;

	System.err.println("We have " + f.params.size() + " params");
	
	for (Parameter p : f.params) {
	    CompoundParameter cp = new CompoundParameter(p.name, p.value, p.min, p.max);
	    cp.addListener((LXParameter lxp)->{
		    p.setValue((float) lxp.getValue());
		});
	    addParameter(cp);
	}
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

    void render(double deltaMs) {
	current += (float)(speedKnob.getValue() * (deltaMs / 1e4));

	if (!init) {
	    init = true;

	    this.graph = app.createGraphics(width, height);
	    this.frag.area = app.createGraphics(frag.width, frag.height);
		
	    setup();

	    frag.area.beginDraw();
	    frag.setup();
	    frag.area.endDraw();
	}

	preDraw(current - elapsed);

	graph.beginDraw();
	graph.background(0);
	graph.copy(frag.image, 0, 0, width, height, 0, 0, width, height);
	graph.endDraw();
	graph.loadPixels();

	RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), graph, colors);	

	elapsed = current;
    }

    public void setup() {
    }

    void preDraw(float vdelta) {
	frag.area.beginDraw();
	frag.area.background(0);
	frag.elapsed += vdelta * frag.rate.value();
	frag.drawFragment();
	frag.area.endDraw();
	frag.area.loadPixels();

	frag.image.copy(frag.area, 0, 0, width, height, 0, 0, width, height);
	frag.image.loadPixels();
    }
}

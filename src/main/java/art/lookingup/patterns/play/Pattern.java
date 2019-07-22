package art.lookingup.patterns.play;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.RenderImageUtil;

import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

abstract public class Pattern extends LXPattern {
    public final PApplet app;
    public final PGraphics graph;

    public final int width;
    public final int height;

    final CompoundParameter speedKnob =
	new CompoundParameter("GlobalSpeed", 1, 0, 10)
        .setDescription("Varies global speed.");

    boolean init;
    float current;
    float elapsed;

    // @@@
    Fragment frag;
    public void addFragment(Fragment f) {
	this.frag = f;
    }

    public Pattern(LX lx, PApplet app, int width, int height) {
	super(lx);

	this.app = app;
	this.graph = app.createGraphics(width, height);
	this.width = width;
	this.height = height;
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
	current += (float)(speedKnob.getValue() * (deltaMs / 1000));

	float vdelta = current - elapsed;

	if (!init) {
	    init = true;
	    setup();

	    // @@@
	    frag.area.beginDraw();
	    frag.setup();
	    frag.area.endDraw();
	}

	preDraw();

	graph.beginDraw();
	graph.background(0);
	graph.copy(frag.image, 0, 0, width, height, 0, 0, width, height);
	graph.endDraw();
	graph.loadPixels();

	RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), graph, colors);	

	elapsed = current;
    }

    public abstract void setup();

    void preDraw() {
	frag.area.beginDraw();
	frag.drawFragment();
	frag.area.endDraw();
	frag.area.loadPixels();

	frag.image.copy(frag.area, 0, 0, width, height, 0, 0, width, height);
	frag.image.loadPixels();
    }
}

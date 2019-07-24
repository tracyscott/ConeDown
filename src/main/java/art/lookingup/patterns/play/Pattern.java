package art.lookingup.patterns.play;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.RenderImageUtil;

import java.util.ArrayList;
import java.util.List;

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

    List<Fragment> frags = new ArrayList<>();

    public void addFragment(Fragment f) {
	this.frags.add(f);

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

	    for (Fragment f : frags) {
		f.area = app.createGraphics(f.width, f.height);
	    }
		
	    setup();

	    for (Fragment f : frags) {
		f.area.beginDraw();
		f.setup();
		f.area.endDraw();
	    }
	}

	preDraw(current - elapsed);

	graph.beginDraw();
	graph.background(0);
	graph.copy(frags.get(frags.size()-1).image, 0, 0, width, height, 0, 0, width, height);
	graph.endDraw();
	graph.loadPixels();

	RenderImageUtil.imageToPointsPixelPerfect(lx.getModel(), graph, colors);	

	elapsed = current;
    }

    public void setup() {
    }

    void preDraw(float vdelta) {
	for (Fragment f : frags) {
	    f.area.beginDraw();
	    f.area.background(0);
	    f.elapsed += vdelta * f.rate.value();
	    f.drawFragment();
	    f.area.endDraw();
	    f.area.loadPixels();

	    f.image.copy(f.area, 0, 0, width, height, 0, 0, width, height);
	    f.image.loadPixels();
	}
    }
}

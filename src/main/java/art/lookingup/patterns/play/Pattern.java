package art.lookingup.patterns.play;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;

import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

abstract public class Pattern extends LXPattern {
    final PApplet app;
    final PGraphics graph;

    final int width;
    final int height;

    final CompoundParameter speedKnob =
	new CompoundParameter("GlobalSpeed", 1, 0, 10)
        .setDescription("Varies global speed.");
    
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
	setup();
    }
    
    @Override
    public void onInactive() {
	tearDown();
    }

    void render(double deltaMs) {
	current += (float)(speedKnob.getValue() * (deltaMs / 1000));

	float vdelta = current - elapsed;

	preDraw();

	graph.beginDraw();
	graph.background(0);
	graph.copy(frag.image, 0, 0, width, height, 0, 0, width, height);
	// graph.image(frag.image, 0, 0, width, height);
	// graph.background(0xffff0000);
	graph.endDraw();
	graph.loadPixels();

	// frag.image.save("/Users/jmacd/Desktop/frag1.png");
	// graph.save("/Users/jmacd/Desktop/graph.png");

	// System.err.println("FRAG " + Arrays.toString(frag.image.pixels)); // .length + ":" + width + ":" + height);
	// System.err.println("GRAPH " + Arrays.toString(graph.pixels)); // .length + ":" + width + ":" + height);

	for (int i = 0; i < lx.getModel().points.length; i++) {
	    CXPoint p = (CXPoint) lx.getModel().points[i];
	    int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder(p);
	    colors[i] = graph.get(imgCoords[0], imgCoords[1]);
	}

	elapsed = current;
    }

    void setup() {
	System.err.println("Setup called");
    }

    void tearDown() {
	System.err.println("Teardown called");
    }

    void preDraw() {
	frag.area.beginDraw();
	frag.drawFragment();
	frag.area.endDraw();
	frag.area.loadPixels();

	frag.image.copy(frag.area, 0, 0, width, height, 0, 0, width, height);
	frag.image.loadPixels();

	// System.err.println("FRAG " + Arrays.toString(frag.image.pixels)); // .length + ":" + width + ":" + height);
    }
}

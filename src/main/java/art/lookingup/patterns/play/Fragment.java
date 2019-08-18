package art.lookingup.patterns.play;

import static processing.core.PConstants.ARGB;

import art.lookingup.ConeDown;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import art.lookingup.Projection;

import processing.core.PGraphics;
import processing.core.PImage;

abstract public class Fragment {
    public final PImage image;

    public PGraphics area;

    public final List<Parameter> params = new ArrayList<>();
    public final Parameter rate;

    public final int width;
    public final int height;
    public final int num;

    public Pattern pattern;
    float elapsed;

    static int nextNum;

    protected Fragment(int width, int height) {
	this.width = width;
	this.height = height;
	this.elapsed = 0;  // Note: updated by Pattern.preDraw()
	this.image = new PImage(this.width, this.height, ARGB);
	
	this.num = nextNum++;
	// Note, to change this from the default in a Fragment, use
	//   this.rate.setValue(0.2f);

	this.rate = newParameter("rate", 0.2f, -1, 1);
    }

    protected Parameter newParameter(String name, float init, float min, float max) {
	name = String.format("%s-%s", name, this.num);
	Parameter p = new Parameter(this, name, init, min, max);
	params.add(p);
	return p;
    }

    protected void noRateKnob() {
	params.remove(rate);	
    }

    public float elapsed() {
	return elapsed;
    }

    public PGraphics area() {
	return this.area;
    }

    protected Projection getProjection() {
	return ConeDown.getProjection(Pattern.superSampling);
    }

    public void setup() {}

    public void notifyChange() {}

    public void preDrawFragment(float vdelta) {
	elapsed += vdelta * rate.value();
    }

    public abstract void drawFragment();

    public void create(Pattern p) {
	this.pattern = p;
	this.area = p.createGraphics(p.app, width, height);
    }

    public void registerParameters(Parameter.Adder adder) {
	for (Parameter p : params) {
	    adder.registerParameter(p);
	}
    }

    public void render(float vdelta) {
	preDrawFragment(vdelta);
	
	area.beginDraw();
	area.pushMatrix();
	area.background(0, 0, 0, 255);
	drawFragment();
	area.popMatrix();
	area.endDraw();
	area.loadPixels();
	    
	image.copy(area, 0, 0, width, height, 0, 0, width, height);
	image.loadPixels();
    }
};

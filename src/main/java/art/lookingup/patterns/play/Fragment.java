package art.lookingup.patterns.play;

import static processing.core.PConstants.ARGB;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

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

    float elapsed;

    static int nextNum;

    protected Fragment(int width, int height) {
	this.width = width;
	this.height = height;
	this.elapsed = 0;  // Note: updated by Pattern.preDraw()
	this.image = new PImage(this.width, this.height, ARGB);
	
	this.num = nextNum++;
	this.rate = newParameter("rate", 0, -1, 1);
    }

    protected Parameter newParameter(String name, float init, float min, float max) {
	name = String.format("%s-%s", name, this.num);
	Parameter p = new Parameter(this, name, init, min, max);
	params.add(p);
	return p;
    }

    public float elapsed() {
	return elapsed;
    }

    public PGraphics area() {
	return this.area;
    }

    public void setup() {}

    public void notifyChange() {}

    public void preDrawFragment(float vdelta) {
	elapsed += vdelta * rate.value();
    }

    public abstract void drawFragment();

    public void create(Pattern p) {
	this.area = p.createGraphics(p.app, width, height);
    }

    public void registerParameters(Parameter.Adder adder) {
	for (Parameter p : params) {
	    CompoundParameter cp = new CompoundParameter(p.name, p.value, p.min, p.max);
	    cp.addListener((LXParameter lxp)->{
		    p.setValue((float) lxp.getValue());
		});
	    adder.registerParameter(cp);
	}
    }

    public void render(float vdelta) {
	preDrawFragment(vdelta);
	
	area.beginDraw();
	area.pushMatrix();
	area.background(0);
	drawFragment();
	area.popMatrix();
	area.endDraw();
	area.loadPixels();
	    
	image.copy(area, 0, 0, width, height, 0, 0, width, height);
	image.loadPixels();
    }
};

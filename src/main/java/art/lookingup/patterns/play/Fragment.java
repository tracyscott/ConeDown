package art.lookingup.patterns.play;

import static processing.core.PConstants.ARGB;

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

    float elapsed;

    protected Fragment(int width, int height) {
	this.width = width;
	this.height = height;
	this.elapsed = 0;
	this.image = new PImage(this.width, this.height, ARGB);
	this.rate = newParameter(String.format("rate-%s", this.hashCode()), 1, -1, 1);
    }

    protected Parameter newParameter(String name, float init, float min, float max) {
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

    public abstract void drawFragment();
};

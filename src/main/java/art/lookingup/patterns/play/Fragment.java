package art.lookingup.patterns.play;

import static processing.core.PConstants.ARGB;

import processing.core.PGraphics;
import processing.core.PImage;

abstract public class Fragment {
    public final PGraphics area;
    public final PImage image;

    public final int width;
    public final int height;

    float elapsed;

    protected Fragment(PGraphics area) {
	this.area = area;
	this.width = area.width;
	this.height = area.height;
	this.elapsed = 0;  // @@@
	this.image = new PImage(this.width, this.height, ARGB);
    }

    protected Parameter newParameter(String name, float init, float min, float max) {
	return new Parameter(this, name, init, min, max);
    }

    public float elapsed() {
	return elapsed;
    }

    public PGraphics area() {
	return this.area;
    }

    public void setup() {
    }

    public void notifyChange() {
    }

    public void preDrawFragment() {
    }

    public abstract void drawFragment();
};

package art.lookingup.patterns.play;

import static processing.core.PConstants.ARGB;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.parameter.CompoundParameter;
// import processing.core.PApplet;
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

    public void preDraw(double deltaMs) {
	
    }
   
    public void draw(double deltaMs) {
	this.elapsed += deltaMs;
    }

    public float elapsed() {
	return elapsed;
    }

    public PGraphics area() {
	return this.area;
    }

    public abstract void drawFragment();
    public abstract void preDrawFragment();
};

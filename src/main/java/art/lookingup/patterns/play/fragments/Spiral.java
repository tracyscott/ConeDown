package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Spiral extends Fragment {
    static final float period = 10f;
    
    static final int maxCount = 99;

    final Parameter triples;
    final Parameter pitch;
    final Parameter fill;

    static final int numSections = 24;

    Gradient gradients[];
    float strokeWidth;
    
    static public class Factory implements FragmentFactory {
	public Factory() { }

	public Fragment create(LX lx, int width, int height) {
	    return new Spiral(lx, width, height);
	}
    };
    
    protected Spiral(LX lx, int width, int height) {
	super(width, height);
	this.triples = newParameter("triples", 4, 1, 10);
	this.pitch = newParameter("pitch", 640, 8, 1000);
	this.fill = newParameter("fill", 0.1f, 0, 1);
	this.gradients = new Gradient[maxCount+1];

	this.notifyChange();
    }

    public void notifyChange() {

	int count = (int)triples.value() * 3;
	float p = pitch.value() / count;
	float p2 = p * p;
	float w2 = width * width;

	this.strokeWidth = fill.value() * (float) Math.sqrt(p2*w2/(p2+w2));
    }

    @Override
    public void setup() {
	super.setup();

	this.area.beginDraw();
	for (int count = 3; count <= maxCount; count += 3) {
	    this.gradients[count] = Gradient.compute(area, count);
	}
	this.area.endDraw();
    }

    @Override
    public void drawFragment() {
	int count = (int)triples.value() * 3;
	float incr = pitch.value();
	float spin = elapsed() / period;

	area.strokeWeight(strokeWidth);

	for (int idx = 0; idx < count; idx++) {
	    // @@@ broken around here; the pattern *disappears*
	    // (corksrews off screen) after a while.
	    float base = -incr - spin * incr;
	    float spirals = (float) count;
	    float startY = base + ((float) idx / spirals) * incr;
	    int c = gradients[count].index(idx);
	    area.stroke(c);

	    for (float y = startY; y < height+incr; y += incr) {
		for (float s = -1; s <= numSections; s++) {
		    float br = s / numSections;
		    float er = (s + 1) / numSections;

		    float x0 = width * br;
		    float x1 = width * er;
		    float y0 = y + incr * br;
		    float y1 = y + incr * er;

		    if (s == 0) {
			area.line(x0+width, y0, x1+width, y1);
		    } else if (s == numSections - 1) {
			area.line(x0-width, y0, x1-width, y1);
		    }

		    area.line(x0, y0, x1, y1);
		}
	    }
	}
    }
}


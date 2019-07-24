package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Spiral extends Fragment {

    static final int maxCount = 99;

    final Parameter triples;
    final Parameter pitch;
    final Parameter fill;

    // @@@ redo this pattern as a repeating scan over an image?
    Gradient gradients[];
    float strokeWidth;
    
    public Spiral(LX lx, int width, int height) {
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

	for (int count = 3; count <= maxCount; count += 3) {
	    this.gradients[count] = Gradient.compute(area, count);
	}
    }

    @Override
    public void drawFragment() {
	int count = (int)triples.value() * 3;
	float incr = pitch.value();
	float spin = elapsed();

	area.strokeWeight(strokeWidth);

	for (int idx = 0; idx < count; idx++) {
	    float base = -incr - spin * incr;
	    float spirals = (float)count;
	    float y0 = base + ((float)idx / spirals) * incr;
	    int c = gradients[count].index(idx);
	    area.stroke(c);

	    for (float y = y0; y < height+incr; y += incr) {
		area.line(0, y, width, y+incr);
	    }
	}
    }
}

package art.lookingup.patterns.play.fragments;

import processing.core.PGraphics;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

public class Spiral extends art.lookingup.patterns.play.Fragment {

    static final int maxCount = 99;

    final Parameter triples;
    final Parameter pitch;
    final Parameter fill;

    // @@@ redo this pattern as a repeating scan over an image.
    Gradient gradients[];
    
    public Spiral(PGraphics area) {
	super(area);
	this.triples = new Parameter("triples", 4, 1, 10);
	this.pitch = new Parameter("pitch", 64, 8, 1000);
	this.fill = new Parameter("fill", 1, 0, 1);
	this.gradients = new Gradient[maxCount+1];
    }


    @Override
    public void setup() {
	for (int count = 3; count <= maxCount; count += 3) {
	    this.gradients[count] = Gradient.compute(area, count);
	}
    }

    @Override
    public void preDrawFragment() {
    }

    @Override
    public void drawFragment() {
	int count = (int)(triples.value() * 3);
	float incr = (float)pitch.value();
	float spin = elapsed();
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

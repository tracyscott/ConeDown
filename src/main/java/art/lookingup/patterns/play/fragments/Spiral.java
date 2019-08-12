package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.PI;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Spiral extends Fragment {
    static final float period = .01f;

    static final double epsilon = 0.05; // Very small angle special case
    
    static final int maxCount = 99;

    final Parameter triples;

    // TODO Note that the angle changes as a result of the
    // superSampling parameter, because the rise across `width` pixels
    // varies with the super-width.  :shrug:
    final Parameter angle;
    final Parameter fill;

    static final int numSections = 24;

    Gradient gradients[];
    boolean right;
    float strokeWidth;
    float leastX;
    float pitchY;
    float stepX;
    float lengthX;
    float lengthY;
    
    static public class Factory implements FragmentFactory {
	public Factory() { }

	public Fragment create(LX lx, int width, int height) {
	    return new Spiral(lx, width, height);
	}
    };
    
    protected Spiral(LX lx, int width, int height) {
	super(width, height);
	this.triples = newParameter("triples", 4, 1, 10);
	this.angle = newParameter("angle", PI / 8, -PI / 2, PI / 2);
	this.fill = newParameter("fill", 1, 0, 1);
	this.gradients = new Gradient[maxCount+1];

	this.notifyChange();
    }

    public void notifyChange() {
	int count = (int)triples.value() * 3;

	boolean right = angle.value() > 0;
	double theta = Math.abs(angle.value());

	if (theta < epsilon) {
	    theta = epsilon;
	}

	double tanTh = Math.tan(theta);
	double pitch = width * tanTh;
	double least = height * Math.tan(Math.PI / 2 - theta);
	double stepY = pitch / count;
	double stepX = stepY / tanTh;
	double thick = stepX * Math.sin(theta);

	this.right = right;
	this.leastX = (float)least;
	this.pitchY = (float)pitch;
	this.stepX = (float)stepX;
	this.lengthX = 2 * leastX;
	this.lengthY = 2 * height;
	this.strokeWidth = (float)(fill.value() * thick);
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

	// Spin is the offset of the 0th color index into the area
	float spin = ((elapsed() / period) + width) % width;

	area.strokeWeight(strokeWidth);

	int colorIdx = 0;
	
	for (float x = spin; x < width + stepX + (right ? 0 : leastX); x += stepX) {
	    area.stroke(gradients[count].index(colorIdx++));

	    area.line(x - (right ? lengthX : -lengthX), 0 - lengthY,
		      x + (right ? lengthX : -lengthX), 0 + lengthY);

	    colorIdx %= count;
	}

	colorIdx = count - 1;
	
	for (float x = spin - stepX; x >= -stepX - (right ? leastX : 0); x -= stepX) {
	    area.stroke(gradients[count].index(colorIdx--));

	    area.line(x - (right ? lengthX : -lengthX), 0 - lengthY,
		      x + (right ? lengthX : -lengthX), 0 + lengthY);

	    colorIdx += count;
	    colorIdx %= count;
	}
    }
}


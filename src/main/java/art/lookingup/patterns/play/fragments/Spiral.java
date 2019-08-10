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
    
    static final int maxCount = 99;

    final Parameter triples;
    final Parameter angle;
    final Parameter fill;

    static final int numSections = 24;

    Gradient gradients[];
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
	this.angle = newParameter("angle", PI / 8, PI / 128, PI / 2);
	this.fill = newParameter("fill", 1, 0, 1);
	this.gradients = new Gradient[maxCount+1];

	this.notifyChange();
    }

    public void notifyChange() {
	int count = (int)triples.value() * 3;

	double theta = angle.value();
	double tanTh = Math.tan(theta);
	double pitch = width * tanTh;
	double least = -height * Math.tan(Math.PI / 2 - theta);
	double stepY = pitch / count;
	double stepX = stepY / tanTh;
	double thick = stepX * Math.sin(theta);

	this.strokeWidth = (float)(fill.value() * thick);
	this.leastX = (float)least;
	this.pitchY = (float)pitch;
	this.stepX = (float)stepX;
	this.lengthX = 2 * -leastX;
	this.lengthY = 2 * height;
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
	
	for (float x = spin; x < width + stepX; x += stepX) {
	    area.stroke(gradients[count].index(colorIdx++));

	    area.line(x - lengthX, 0 - lengthY,
		      x + lengthX, 0 + lengthY);

	    colorIdx %= count;
	}

	colorIdx = count - 1;
	
	for (float x = spin - stepX; x >= leastX - stepX; x -= stepX) {
	    area.stroke(gradients[count].index(colorIdx--));

	    area.line(x - lengthX, 0 - lengthY,
		      x + lengthX, 0 + lengthY);

	    colorIdx += count;
	    colorIdx %= count;
	}
    }
}


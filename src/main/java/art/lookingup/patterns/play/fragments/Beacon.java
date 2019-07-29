package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.Parameter;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Beacon extends Fragment {
    static final float period = 0.1f;
    
    final Fragment frag0;
    final Fragment frag1;

    public Beacon(LX lx, int width, int height, Fragment f0, Fragment f1) {
	super(width, height);
	this.frag0 = f0;
	this.frag1 = f1;
    }

    @Override
    public void create(Pattern p) {
	super.create(p);
	frag0.create(p);
	frag1.create(p);
    }    

    @Override
    public void preDrawFragment(float vdelta) {
	super.preDrawFragment(vdelta);
	frag0.render(vdelta);
	frag1.render(vdelta);
    }

    @Override
    public void setup() {
	frag0.setup();
	frag1.setup();
    }

    @Override
    public void registerParameters(Parameter.Adder adder) {
	super.registerParameters(adder);
	frag0.registerParameters(adder);
	frag1.registerParameters(adder);
    }
    
    @Override
    public void drawFragment() {
	float f0base = (elapsed() / period) % width;
	int base;
	Fragment whole;
	Fragment split;
	final int halfw = (int)(width/2f + 0.5);


	if (f0base > halfw) {
	    base = (int)(f0base - halfw + 0.5);
	    whole = frag1;
	    split = frag0;
	} else {
	    base = (int)(f0base + 0.5);
	    whole = frag0;
	    split = frag1;
	}

	// TODO HERE shift fractional pixels

	area.copy(whole.image, 0, 0, halfw, height, base, 0, halfw, height);

	int right = width - base - halfw;
	area.copy(split.image, 0, 0, right, height, base+halfw, 0, right, height);
	area.copy(split.image, 0, 0, base, height, 0, 0, base, height);
    }    
}

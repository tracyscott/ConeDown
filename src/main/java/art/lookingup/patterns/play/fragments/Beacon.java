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

    final int halfWidth;

    public Beacon(LX lx, int width, int height, Fragment f0, Fragment f1) {
	super(width, height);
	this.frag0 = f0;
	this.frag1 = f1;
	this.halfWidth = width / 2;
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

	// TODO @@@
	// float position = (elapsed() / period) % width;
	// int posInt = (int) position;
	// float poffset = position - posInt;

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
	int f0pos = (int)(elapsed() / period);

	drawHalf(f0pos, frag0);
	drawHalf(f0pos+halfWidth, frag1);
    }

    void drawHalf(int pos, Fragment f) {
	int drawn = 0;
	while (drawn < halfWidth) {
	    pos %= width;

	    int take = Math.min(width - pos, halfWidth - drawn);

	    area.copy(f.image, pos, 0, take, height, pos, 0, take, height);

	    drawn += take;
	    pos += take;
	}
    }


	// int halfw = width/2;
	// int wholeOff;
	// int splitOff;
	// int base;
	// Fragment whole;
	// Fragment split;
	
	// if (f0base < halfw) {
	//     base = f0pos;
	//     whole = frag0;
	//     wholeOff = 0;
	//     split = frag1;
	//     splitOff = 
	// } else {	    
	//     base = (int)(f0base - halfw);
	//     whole = frag1;
	//     split = frag0;
	// }

	// area.copy(whole.image, base, 0, halfw, height, base, 0, halfw, height);

	// @@@	area.rect(whole.image, base, 0, halfw, height, base, 0, halfw, height);

	// int right = width - base - halfw;
	// area.copy(split.image, 0, 0, right, height, base+halfw, 0, right, height);
	// area.copy(split.image, right, 0, width-right, height, 0, 0, base, height);

}

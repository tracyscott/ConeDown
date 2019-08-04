package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.Parameter;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Beacon extends Fragment {
    static final float period = 0.1f;
    
    final Fragment frag0;
    final Fragment frag1;

    final int halfWidth;

    static public class Factory implements FragmentFactory {
	FragmentFactory ff0;
	FragmentFactory ff1;
	public Factory(FragmentFactory ff0, FragmentFactory ff1) {
	    this.ff0 = ff0;
	    this.ff1 = ff1;
	}

	public Fragment create(LX lx, int width, int height) {
	    return new Beacon(lx, width, height,
			      ff0.create(lx, width, height),
			      ff1.create(lx, width, height));
	}
    };
    
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

	System.err.println("F0pos " + f0pos);

	drawHalf(f0pos, frag0);

	System.err.println("F1pos " + f0pos + halfWidth);

	drawHalf(f0pos+halfWidth, frag1);
    }

    void drawHalf(int pos, Fragment f) {
	int drawn = 0;
	while (drawn < halfWidth) {
	    pos %= width;

	    int take = Math.min(width - pos, halfWidth - drawn);

	    area.copy(f.image, pos, 0, take, height, pos, 0, take, height);

	    System.err.println("copy " + pos + ":" + take);

	    drawn += take;
	    pos += take;
	}
    }
}

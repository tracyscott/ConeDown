package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Blend extends Multi {

    static public class Factory implements FragmentFactory {
	int mode;
	FragmentFactory a, b;

	public Factory(int mode, FragmentFactory a, FragmentFactory b) {
	    this.mode = mode;
	    this.a = a;
	    this.b = b;
	}

	public Fragment create(LX lx, int width, int height) {
	    return new Blend(lx, width, height, mode,
			     a.create(lx, width, height),
			     b.create(lx, width, height));
	}
    };

    final int mode;
    
    protected Blend(LX lx, int width, int height, int mode, Fragment a, Fragment b) {
	super(lx, width, height, a, b);
	this.mode = mode;

	noRateKnob();
    }

    public void drawFragment() {
	area.copy(fragments[0].image, 0, 0, width, height, 0, 0, width, height);
	area.blend(fragments[1].image, 0, 0, width, height, 0, 0, width, height, mode);
    }
};


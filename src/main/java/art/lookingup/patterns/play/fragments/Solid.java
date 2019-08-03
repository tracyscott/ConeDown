package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;
import art.lookingup.Projection;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Solid extends Fragment {
    final Parameter r;
    final Parameter g;
    final Parameter b;
    
    public Solid(LX lx, int width, int height) {
	super(width, height);
	this.r = newParameter("r", 255, 0, 255);
	this.g = newParameter("g", 255, 0, 255);
	this.b = newParameter("b", 255, 0, 255);
    }

    @Override
    public void drawFragment() {
	area.background(r.value(), g.value(), b.value());
    }
}


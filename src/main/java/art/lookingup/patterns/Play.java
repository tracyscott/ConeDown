package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Spiral;

import processing.core.PApplet;
import processing.core.PGraphics;

import heronarts.lx.LX;

public class Play extends Pattern {

    public Play(LX lx) {
	this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
    }

    public Play(LX lx, PApplet app, int width, int height) {
	super(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);

	PGraphics area = app.createGraphics(width, height);

	this.addFragment(new Spiral(area));
    }
};

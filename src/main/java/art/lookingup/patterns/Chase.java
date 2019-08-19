package art.lookingup.patterns;

import static processing.core.PConstants.MULTIPLY;
import static processing.core.PConstants.ADD;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;


import heronarts.lx.LX;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Chase extends Pattern {

    public Chase(LX lx) {
	this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
    }

    public Chase(LX lx, PApplet app, int width, int height) {
	super(lx, app, width, height);

	this.setFragment(new art.lookingup.patterns.play.fragments.Chase.DanceFactory());
    }
};

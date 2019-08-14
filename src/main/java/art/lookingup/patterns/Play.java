package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
simport art.lookingup.patterns.play.fragments.Balls;
import art.lookingup.patterns.play.fragments.Beacon;
import art.lookingup.patterns.play.fragments.Cone;
import art.lookingup.patterns.play.fragments.Solid;
import art.lookingup.patterns.play.fragments.Spiral;

import heronarts.lx.LX;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Play extends Pattern {

    public Play(LX lx) {
	this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
    }

    public Play(LX lx, PApplet app, int width, int height) {
	super(lx, app, width, height);

	// this.setFragment(new Beacon.Factory(new Balls.Factory(),
	// 				    new Spiral.Factory()));

	this.setFragment(new Cone.Factory(new Balls.Factory(),
					  new Spiral.Factory(),
					  new Solid.Factory()));
	
	// this.setFragment(new Spiral.Factory());
    }
};

package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Balls;
import art.lookingup.patterns.play.fragments.Beacon;
import art.lookingup.patterns.play.fragments.Spiral;
import art.lookingup.patterns.play.fragments.Solid;

import heronarts.lx.LX;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Play extends Pattern {

    public Play(LX lx) {
	this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
    }

    public Play(LX lx, PApplet app, int width, int height) {
	super(lx, app, width, height);

	this.addFragment(new Beacon.Factory(new Solid.Factory(),
					    new Solid.Factory()));

	// this.addFragment(new Beacon(lx,
	// 			    width,
	// 			    height,
	// 			    new Spiral(lx, width, height),
	// 			    new Balls(lx, width, height)));

	// this.addFragment(new Spiral(lx, width, height));

	// this.addFragment(new Solid.Factory());
    }
};

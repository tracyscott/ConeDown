package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;

import static processing.core.PConstants.ADD;

import art.lookingup.patterns.play.fragments.Balls;
import art.lookingup.patterns.play.fragments.Beacon;
import art.lookingup.patterns.play.fragments.Blend;
import art.lookingup.patterns.play.fragments.ConeScoop;
import art.lookingup.patterns.play.fragments.Spiral;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import processing.core.PApplet;
import processing.core.PGraphics;

@LXCategory(LXCategory.FORM)
public class Spiral2 extends Pattern {

    public Spiral2(LX lx) {
	this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
    }

    public Spiral2(LX lx, PApplet app, int width, int height) {
	super(lx, app, width, height);
	this.setFragment(new ConeScoop.Factory(new Blend.Factory(ADD,
								 new Spiral.Factory(),
								 new Spiral.InvertedFactory()),
					       new Beacon.Factory(new Balls.Factory(),
								  new Balls.InvertedFactory())));
    }
};

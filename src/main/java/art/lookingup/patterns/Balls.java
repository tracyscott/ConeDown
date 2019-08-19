package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.ConeScoop;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import processing.core.PApplet;
import processing.core.PGraphics;

@LXCategory(LXCategory.FORM)
public class Balls extends Pattern {

    public Balls(LX lx) {
	this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
    }

    public Balls(LX lx, PApplet app, int width, int height) {
	super(lx, app, width, height);
	this.setFragment(new ConeScoop.Factory(new art.lookingup.patterns.play.fragments.Balls.Factory(),
					       new art.lookingup.patterns.play.fragments.Balls.InvertedFactory()));
										     
    }
};

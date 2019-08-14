package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class Cone extends Multi {

    static public class Factory implements FragmentFactory {
	FragmentFactory cone;
	FragmentFactory scoop;
	FragmentFactory dance;

	public Factory(FragmentFactory cone, FragmentFactory scoop, FragmentFactory dance) {
	    this.cone = cone;
	    this.scoop = scoop;
	    this.dance = dance;
	}

	public Fragment create(LX lx, int width, int height) {
	    int factor = width / ConeDownModel.POINTS_WIDE;
	    
	    return new Cone(lx, width, height,
			    cone.create(lx,
					factor * ConeDownModel.conePointsWide,
					factor * ConeDownModel.conePointsHigh),
			    scoop.create(lx,
					 factor * ConeDownModel.scoopPointsWide,
					 factor * ConeDownModel.scoopPointsHigh),
			    cone.create(lx,
					factor * ConeDownModel.dancePointsWide,
					factor * ConeDownModel.dancePointsHigh));
	}
    };
    
    int factor;

    protected Cone(LX lx, int width, int height, Fragment cone, Fragment scoop, Fragment dance) {
	super(lx, width, height, cone, scoop, dance);
	this.factor = width / ConeDownModel.POINTS_WIDE;
    }

    public void drawFragment() {
	area.copy(fragments[0].image,
		  0,
		  0,
		  factor * ConeDownModel.conePointsWide,
		  factor * ConeDownModel.conePointsHigh,
		  0,
		  0,
		  factor * ConeDownModel.conePointsWide,
		  factor * ConeDownModel.conePointsHigh);
    }
};

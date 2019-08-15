package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

public class ConeScoop extends Multi {

    static public class Factory implements FragmentFactory {
	FragmentFactory conescoop;
	FragmentFactory dance;

	public Factory(FragmentFactory conescoop, FragmentFactory dance) {
	    this.conescoop = conescoop;
	    this.dance = dance;
	}

	public Fragment create(LX lx, int width, int height) {
	    int factor = width / ConeDownModel.POINTS_WIDE;
	    
	    return new ConeScoop(lx, width, height,
			    conescoop.create(lx,
					     factor * ConeDownModel.conePointsWide,
					     factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh)),
			    dance.create(lx,
					factor * ConeDownModel.dancePointsWide,
					factor * ConeDownModel.dancePointsHigh));
	}
    };
    
    int factor;

    protected ConeScoop(LX lx, int width, int height, Fragment conescoop, Fragment dance) {
	super(lx, width, height, conescoop, dance);
	this.factor = width / ConeDownModel.POINTS_WIDE;
    }

    public void drawFragment() {
	area.copy(fragments[0].image,
		  0,
		  0,
		  factor * ConeDownModel.conePointsWide,
		  factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh),
		  0,
		  0,
		  factor * ConeDownModel.conePointsWide,
		  factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh));
	area.copy(fragments[1].image,
		  0,
		  0,
		  factor * ConeDownModel.dancePointsWide,
		  factor * ConeDownModel.dancePointsHigh,
		  factor * (ConeDownModel.scoopPointsWide - ConeDownModel.dancePointsWide) / 2,
		  factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh),
		  factor * ConeDownModel.dancePointsWide,
		  factor * ConeDownModel.dancePointsHigh);
    }
};


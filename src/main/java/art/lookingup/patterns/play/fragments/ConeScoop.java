package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import heronarts.lx.LX;

public class ConeScoop extends Multi {

  public static class Factory extends BaseFactory {
    FragmentFactory conescoop;
    FragmentFactory dance;

    public Factory(FragmentFactory conescoop, FragmentFactory dance) {
      super("", conescoop, dance);
      this.conescoop = conescoop;
      this.dance = dance;
    }

    public Fragment create(LX lx, int width, int height) {
      int factor = width / ConeDownModel.POINTS_WIDE;

      return new ConeScoop(
          toString(),
          lx,
          width,
          height,
          conescoop.create(
              lx,
              factor * ConeDownModel.scoopPointsWide,
              factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh)),
          dance.create(
              lx, factor * ConeDownModel.dancePointsWide, factor * ConeDownModel.dancePointsHigh));
    }
  };

  int factor;

  protected ConeScoop(
      String fragName, LX lx, int width, int height, Fragment conescoop, Fragment dance) {
    super(fragName, lx, width, height, conescoop, dance);
    this.factor = width / ConeDownModel.POINTS_WIDE;
    noRateKnob();
  }

  public void drawFragment() {
    area.copy(
        fragments[0].image,
        0,
        0,
        factor * ConeDownModel.scoopPointsWide,
        factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh),
        0,
        0,
        factor * ConeDownModel.scoopPointsWide,
        factor * (ConeDownModel.conePointsHigh + ConeDownModel.scoopPointsHigh));
    area.copy(
        fragments[1].image,
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

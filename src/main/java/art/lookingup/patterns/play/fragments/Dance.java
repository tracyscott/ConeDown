package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import heronarts.lx.LX;

public class Dance extends Multi {

  public static class Factory extends BaseFactory {
    FragmentFactory dance;

    public Factory(FragmentFactory dance) {
      super("", dance);
      this.dance = dance;
    }

    public Fragment create(LX lx, int width, int height) {
      int factor = width / ConeDownModel.POINTS_WIDE;

      return new Dance(
          toString(),
          lx,
          width,
          height,
          dance.create(
              lx, factor * ConeDownModel.dancePointsWide, factor * ConeDownModel.dancePointsHigh));
    }
  };

  int factor;

  protected Dance(String fragName, LX lx, int width, int height, Fragment dance) {
    super(fragName, lx, width, height, dance);
    this.factor = width / ConeDownModel.POINTS_WIDE;
    noRateKnob();
  }

  public void drawFragment() {
    area.copy(
        fragments[0].image,
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

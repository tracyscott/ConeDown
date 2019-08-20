package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import heronarts.lx.LX;

public class Dance extends Multi {

  public static class Factory implements FragmentFactory {
    FragmentFactory dance;

    public Factory(FragmentFactory dance) {
      this.dance = dance;
    }

    public Fragment create(LX lx, int width, int height) {
      int factor = width / ConeDownModel.POINTS_WIDE;

      return new Dance(
          lx,
          width,
          height,
          dance.create(
              lx, factor * ConeDownModel.dancePointsWide, factor * ConeDownModel.dancePointsHigh));
    }
  };

  int factor;

  protected Dance(LX lx, int width, int height, Fragment dance) {
    super(lx, width, height, dance);
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

package art.lookingup.patterns.play.fragments;

import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import heronarts.lx.LX;

public class Cone extends Multi {

  public static class Factory extends BaseFactory {
    FragmentFactory cone;
    FragmentFactory scoop;
    FragmentFactory dance;

    public Factory(FragmentFactory cone, FragmentFactory scoop, FragmentFactory dance) {
      super("", cone, scoop, dance);

      this.cone = cone;
      this.scoop = scoop;
      this.dance = dance;
    }

    public Fragment create(LX lx, int width, int height) {
      int factor = width / ConeDownModel.POINTS_WIDE;

      return new Cone(
          toString(),
          lx,
          width,
          height,
          cone.create(
              lx, factor * ConeDownModel.scoopPointsWide, factor * ConeDownModel.conePointsHigh),
          scoop.create(
              lx, factor * ConeDownModel.scoopPointsWide, factor * ConeDownModel.scoopPointsHigh),
          dance.create(
              lx, factor * ConeDownModel.dancePointsWide, factor * ConeDownModel.dancePointsHigh));
    }
  };

  int factor;

  protected Cone(
      String fragName,
      LX lx,
      int width,
      int height,
      Fragment cone,
      Fragment scoop,
      Fragment dance) {
    super(fragName, lx, width, height, cone, scoop, dance);
    this.factor = width / ConeDownModel.POINTS_WIDE;
    noDefaultKnobs();
  }

  public void drawFragment() {
    area.copy(
        fragments[0].image,
        0,
        0,
        factor * ConeDownModel.scoopPointsWide,
        factor * ConeDownModel.conePointsHigh,
        0,
        0,
        factor * ConeDownModel.scoopPointsWide,
        factor * ConeDownModel.conePointsHigh);

    area.copy(
        fragments[1].image,
        0,
        0,
        factor * ConeDownModel.scoopPointsWide,
        factor * ConeDownModel.scoopPointsHigh,
        0,
        factor * ConeDownModel.conePointsHigh,
        factor * ConeDownModel.scoopPointsWide,
        factor * ConeDownModel.scoopPointsHigh);
    area.copy(
        fragments[2].image,
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

package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.ConeScoop;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class BallsCubes extends Pattern {

  public BallsCubes(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public BallsCubes(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);
    this.setFragment(
        new ConeScoop.Factory(
            new art.lookingup.patterns.play.fragments.Balls.Factory("b"),
            new art.lookingup.patterns.play.fragments.Cubes.Factory("c")));
  }
};

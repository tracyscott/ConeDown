package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class CubesFull extends Pattern {

  public CubesFull(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public CubesFull(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    this.setFragment(new art.lookingup.patterns.play.fragments.Cubes.Factory("c"));
  }
};

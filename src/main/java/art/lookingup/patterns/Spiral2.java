package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Blend;
import art.lookingup.patterns.play.fragments.ConeScoop;
import art.lookingup.patterns.play.fragments.Spiral;
import art.lookingup.patterns.play.fragments.Strange;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class Spiral2 extends Pattern {

  public Spiral2(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public Spiral2(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);
    this.setFragment(
        new ConeScoop.Factory(
            new Blend.Factory("b", new Spiral.Factory("b"), new Spiral.InvertedFactory("t")),
            new Strange.Factory("d")));
  }
};

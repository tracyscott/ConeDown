package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Blend;
import art.lookingup.patterns.play.fragments.Spiral;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class Spiral3 extends Pattern {

  public Spiral3(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public Spiral3(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    this.setFragment(
        // 	     new Blend.Factory(
        // "bst",
        new Blend.Factory(
            "0",
            new Blend.Factory("1", new Spiral.Factory("l"), new Spiral.Factory("r")),
            new Spiral.InvertedFactory("i")));
    // new Strange.Factory("s")));
  }
};

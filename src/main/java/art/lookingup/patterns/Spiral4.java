package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Beacon;
import art.lookingup.patterns.play.fragments.Blend;
import art.lookingup.patterns.play.fragments.Spiral;
import art.lookingup.patterns.play.fragments.Strange;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class Spiral4 extends Pattern {

  public Spiral4(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public Spiral4(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    this.setFragment(
        new Blend.Factory(
            "",
            new Blend.Factory(
                "s",
                new Beacon.Factory(
                    "b",
                    new Spiral.Factory("1"),
                    new Spiral.InvertedFactory("2"),
                    new Spiral.Factory("3"),
                    new Spiral.InvertedFactory("4")),
                new Spiral.InvertedFactory("0")),
            new Strange.Factory("t")));
  }
};

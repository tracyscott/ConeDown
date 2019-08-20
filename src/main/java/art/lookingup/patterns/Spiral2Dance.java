package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Blend;
import art.lookingup.patterns.play.fragments.Dance;
import art.lookingup.patterns.play.fragments.Spiral;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class Spiral2Dance extends Pattern {

  public Spiral2Dance(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public Spiral2Dance(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    this.setFragment(
        new Dance.Factory(
            "Dance",
            new Blend.Factory(
                "Spiral2", new Spiral.Factory("Top"), new Spiral.InvertedFactory("Bottom"))));
  }
};

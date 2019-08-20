package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.ConeScoop;
import art.lookingup.patterns.play.fragments.Spiral;
import art.lookingup.patterns.play.fragments.Strange;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class SpiralStrange extends Pattern {

  public SpiralStrange(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public SpiralStrange(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);
    this.setFragment(new ConeScoop.Factory(new Spiral.InvertedFactory(), new Strange.Factory()));
  }
};

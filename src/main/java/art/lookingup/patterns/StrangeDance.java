package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Dance;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.COLOR)
public class StrangeDance extends Pattern {

  public StrangeDance(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public StrangeDance(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    this.setFragment(
        new Dance.Factory(
            "Dance", new art.lookingup.patterns.play.fragments.Strange.Factory("Strange")));
  }
};

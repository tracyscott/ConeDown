package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.fragments.Balls;
import art.lookingup.patterns.play.fragments.Dance;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import processing.core.PApplet;

@LXCategory(LXCategory.FORM)
public class BallsDance extends Pattern {

  public BallsDance(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public BallsDance(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);
    this.setFragment(new Dance.Factory(new Balls.Factory("b")));
  }
};

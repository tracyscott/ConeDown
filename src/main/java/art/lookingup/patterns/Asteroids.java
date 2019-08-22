package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;
import processing.core.PApplet;

public class Asteroids extends Pattern {

  public Asteroids(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public Asteroids(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    this.setFragment(new art.lookingup.patterns.play.fragments.Asteroids.Factory("b"));
  }
};

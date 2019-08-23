package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.CLOSE;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import heronarts.lx.LX;
import java.util.Random;

public class Asteroids extends Fragment {
  public static class Factory extends BaseFactory {
    public Factory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      return new Asteroids(toString(), lx, width, height);
    }
  };

  protected Asteroids(String fragName, LX lx, int width, int height) {
    super(fragName, width, height);

    this.minDimension = Math.min(width, height);

    for (int i = 0; i < numAsteroids; i++) {
      ast[i] = new Ast();
    }
  }

  public static final int numAsteroids = 12;
  public static final float minRadRatio = 0.04f;
  public static final float maxRadRatio = 0.08f;

  public static final float astWeightRatio = 0.01f;
  public static final float placementMinRatio = 0.2f;

  public static final float placementMaxRatio = 0.8f;

  final int minDimension;

  int randInt(int max, float minRatio, float maxRatio) {
    return (int) (max * (minRatio + (maxRatio - minRatio) * rnd.nextFloat()));
  }

  class Ast {
    float x;
    float y;
    float rad;
    float rot;

    Ast() {
      this.x = randInt(width, placementMinRatio, placementMaxRatio);
      this.y = randInt(height, placementMinRatio, placementMaxRatio);
      this.rad = randInt(minDimension, minRadRatio, maxRadRatio);
      this.rot = 0;
    }

    void draw() {
      area.pushMatrix();
      area.translate(x, y);

      area.noFill();
      area.stroke(255);
      area.strokeWeight(minDimension * astWeightRatio);

      area.rotate(rot);
      area.beginShape();
      area.vertex(-rad, 0);
      area.vertex(0, -rad);
      area.vertex(rad, 0);
      area.vertex(0, rad);
      area.endShape(CLOSE);

      area.popMatrix();
    }
  };

  Ast[] ast = new Ast[numAsteroids];
  Random rnd = new Random();

  @Override
  public void drawFragment() {
    area.background(0);

    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 12; j++) {
        ast[i].draw();
      }
    }
  }
}

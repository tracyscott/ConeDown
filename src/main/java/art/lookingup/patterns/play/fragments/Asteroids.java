package art.lookingup.patterns.play.fragments;

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

    for (int i = 0; i < numAsteroids; i++) {
      ast[i] = new Ast();
    }
  }

  public static final int numAsteroids = 12;
  public static final int minRadRatio = 0.1;
  public static final int maxRadRatio = 0.3;

  class Ast {
    float x;
    float y;
    float rad;

    Ast() {
      this.x = rnd.nextInt(width);
      this.y = rnd.nextInt(height);
      this.rad = rnd.nextInt();

      // area.pushMatrix();
      // TODO seam
      // area.translate(rnd.nextInt(width), rnd.nextInt(height));
      // area.rotate(rnd.nextFloat() % (float) (2 * Math.PI));
      // area.scale(5 + rnd.nextInt(width / 10), 5 + rnd.nextInt(height / 10));
      // area.beginShape();
      // area.vertex(-1 / 2f, 0f);
      // area.vertex(0, -1 / 2f);
      // area.vertex(1 / 2f, 0f);
      // area.vertex(0f, 1 / 2f);
      // area.endShape();
      // area.popMatrix();
    }
  };

  Ast[] ast = new Ast[numAsteroids];
  Random rnd = new Random();

  @Override
  public void drawFragment() {
    area.background(0);
    area.stroke(255);
    area.strokeWeight(2);

    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 12; j++) {}
    }
  }
}

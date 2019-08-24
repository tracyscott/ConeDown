package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.PI;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import heronarts.lx.LX;
import java.util.Random;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Asteroids extends Fragment {
  final Parameter weightParam;

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

    this.weightParam = newParameter("weight", 0.01f, 0, 0.05f);
    this.minDimension = Math.min(width, height);

    for (int i = 0; i < numAsteroids; i++) {
      ast[i] = new Ast(i);
    }
  }

  public static final int numAsteroids = 15;
  public static final float minRadRatio = 0.25f;
  public static final float maxRadRatio = 0.3f;

  public static final float placementMinRatio = 0;
  public static final float placementMaxRatio = 1;

  public static final float velocity = 0.01f;

  public static final int numVertices = 11;

  public final int minDimension;

  public static final NormalDistribution angleDist = new NormalDistribution(1, 1);
  public static final NormalDistribution radiusDist = new NormalDistribution(1, 1);

  int randInt(int max, float minRatio, float maxRatio) {
    return (int) (max * (minRatio + (maxRatio - minRatio) * rnd.nextFloat()));
  }

  class Ast {
    int idx;
    float x;
    float y;
    float dx;
    float dy;
    float rotSpeed;
    float rot;
    float[] xvertices;
    float[] yvertices;

    Ast(int idx) {
      this.idx = idx;
      this.x = randInt(width, placementMinRatio, placementMaxRatio);
      this.y = randInt(height, placementMinRatio, placementMaxRatio);
      this.rotSpeed = rnd.nextFloat();
      this.dx = -1 + 2 * rnd.nextFloat();
      this.dy = -1 + 2 * rnd.nextFloat();
      this.rot = 0;
      this.xvertices = new float[numVertices];
      this.yvertices = new float[numVertices];

      float[] angles = new float[numVertices + 1];
      float sum = 0;

      for (int i = 0; i <= numVertices; i++) {
        do {
          angles[i] = (float) angleDist.sample();
        } while (angles[i] <= 0);

        sum += angles[i];
      }

      for (int i = 0; i <= numVertices; i++) {
        angles[i] /= sum;
        angles[i] *= 2 * PI;
      }

      for (int i = 0; i < numVertices; i++) {
        //   float r;
        //   do {
        //     r = (float) (minRadRatio + radiusDist.sample() * (maxRadRatio - minRadRatio));
        //   } while (r < minRadRatio || r >= maxRadRatio);
        float r = maxRadRatio;

        xvertices[i] = minDimension * r * (float) Math.cos(angles[i]);
        yvertices[i] = minDimension * r * (float) Math.sin(angles[i]);
      }
    }

    void draw() {
      rot += rotSpeed * lastElapsed();
      x += dx * lastElapsed() / velocity;
      y += dy * lastElapsed() / velocity;

      x += width;
      y += height;

      x %= width;
      y %= height;

      area.pushMatrix();
      area.translate(x, y);

      area.noFill();
      area.stroke(255);
      area.strokeWeight(minDimension * weightParam.value());

      drawAt(0, 0);
      drawAt(-width, 0);
      drawAt(width, 0);
      drawAt(0, -height);
      drawAt(0, height);

      area.popMatrix();
    }

    void drawAt(float atX, float atY) {
      area.pushMatrix();
      area.translate(atX, atY);
      area.rotate(rot);

      area.beginShape();

      for (int i = 0; i < numVertices; i++) {
        area.vertex(xvertices[i], yvertices[i]);
      }
      area.endShape(CLOSE);
      area.popMatrix();
    }
  };

  Ast[] ast = new Ast[numAsteroids];
  Random rnd = new Random();

  @Override
  public void drawFragment() {
    area.background(0);

    for (Ast a : ast) {
      a.draw();
    }
  }
}

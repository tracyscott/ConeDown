package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.P2D;

import art.lookingup.colors.Gradient;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.shapes.Discs;
import art.lookingup.patterns.shapes.Parabola;
import heronarts.lx.LX;
import java.util.Random;
import processing.core.PImage;

public class Balls extends Fragment {
  static final float ballPeriod = 10f * Pattern.superSampling;
  static final int maxSize = 100 * Pattern.superSampling;
  static final int maxCount = 100;

  final Parameter sizeParam;
  final Parameter countParam;
  final Parameter brightParam;
  final Parameter rollParam;
  Discs discs;

  public Balls(String fragName, LX lx, int width, int height) {
    super(fragName, width, height, P2D);

    this.sizeParam = newParameter("size", 10 * Pattern.superSampling, 0, maxSize);
    this.countParam = newParameter("count", 20, 1, maxCount);
    this.brightParam = newParameter("bright", 1, 0, 1);
    this.rollParam = newParameter("roll", 0.5f, -4, 4);

    this.balls = new Ball[maxCount];
  }

  public static class Factory extends BaseFactory {
    public Factory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      return new Balls(toString(), lx, width, height);
    }
  };

  public static class InvertedFactory extends BaseFactory {
    public InvertedFactory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      Balls b = new Balls(toString(), lx, width, height);
      b.inverted = true;
      return b;
    }
  };

  public class Ball {
    float dp;
    float xcurrent;
    float x0;
    float vp;
    float elap;
    float period;
    int color;
    Parabola parabola;

    Ball(float dp, float x, float y, float vp, float period, int color) {
      this.dp = dp;
      this.xcurrent = x;
      this.x0 = x;
      this.vp = vp;
      this.period = period;
      this.parabola = new Parabola(200, y);
      this.color = color;
    }

    void update(float e) {
      xcurrent += vp * (e - elap);
      this.elap = e;
    }

    float getX() {
      return xcurrent % width;
    }

    float getY() {
      float xoffset = xcurrent - x0;
      float yoffset = xoffset % period;

      float y = parabola.Value(yoffset / period - 0.5f);

      return y;
    }
  }

  Gradient gradient;
  Ball[] balls;

  @Override
  public void setup() {
    if (gradient != null) {
      return;
    }

    this.discs = new Discs(pattern.app, brightParam);

    area.beginDraw();
    Random random = new Random();

    gradient = Gradient.compute(area, maxCount);
    for (int i = 0; i < maxCount; i++) {
      this.balls[i] =
          new Ball(
              (float) (0.3 + 0.7 * random.nextDouble()),
              (float) random.nextDouble() * width,
              4f * (float) (0.9 + 0.1 * random.nextDouble()) * height,
              (float) (0.1 + 0.9 * random.nextDouble()),
              (float) (0.1 + 0.9 * random.nextDouble()) * width,
              gradient.index(random.nextInt(gradient.size())));
    }

    area.endDraw();
  }

  float relapsed;

  @Override
  public void preDrawFragment(float vdelta) {
    super.preDrawFragment(vdelta);
    relapsed += vdelta * rollParam.value();
  }

  @Override
  public void drawFragment() {
    int count = (int) countParam.value();

    for (int i = 0; i < count; i++) {
      Ball b = balls[i];

      b.update(elapsed() * ballPeriod);

      float x = b.getX();
      float y = (height - 1 - b.getY());

      // TODO there's no `xScale` call here.
      // float scale = getProjection().xScale(0, y);
      float scale = 1;

      area.noStroke();

      area.pushMatrix();
      area.translate(x, y);

      float d = (float) (b.dp * sizeParam.value());
      float radius = d / 2;

      PImage texture = discs.getTexture();

      drawHere(texture, 0, 0, scale * radius, radius);

      if (x >= (width - radius)) {
        drawHere(texture, -width, 0, scale * radius, radius);
      } else if (x <= radius) {
        drawHere(texture, +width, 0, scale * radius, radius);
      }

      area.popMatrix();
    }
  }

  void drawHere(PImage texture, float x, float y, float xw, float yw) {
    area.pushMatrix();
    area.translate(x, y);

    area.rotate(relapsed);

    area.beginShape();
    area.texture(texture);

    float w = texture.width;

    area.vertex(x - xw, y - xw, 0, 0);
    area.vertex(x + yw, y - yw, w, 0);
    area.vertex(x + xw, y + xw, w, w);
    area.vertex(x - yw, y + yw, 0, w);
    area.endShape();
    area.popMatrix();
  }
}

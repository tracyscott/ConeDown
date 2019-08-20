package art.lookingup.patterns.play.fragments;

import art.lookingup.colors.Gradient;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.shapes.Parabola;
import heronarts.lx.LX;
import java.util.Random;

public class Balls extends Fragment {
  static final float period = 0.1f * Pattern.superSampling;
  static final int rateMult = 100 * Pattern.superSampling;
  static final int maxSize = 100 * Pattern.superSampling;
  static final int maxCount = 100;

  final Parameter sizeParam;
  final Parameter countParam;

  public Balls(String fragName, LX lx, int width, int height) {
    super(fragName, width, height);

    this.sizeParam = newParameter("size", 10 * Pattern.superSampling, 0, maxSize);
    this.countParam = newParameter("count", 20, 1, maxCount);

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
      xcurrent += vp * (e - elap) * rateMult;
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

  @Override
  public void drawFragment() {
    int count = (int) countParam.value();

    for (int i = 0; i < count; i++) {
      Ball b = balls[i];

      b.update(elapsed() * period);

      float x = b.getX();
      float y = (height - 1 - b.getY());
      float scale = getProjection().xScale(0, y);

      area.pushMatrix();
      area.translate(x, y);

      area.noStroke();
      area.fill(b.color);

      float d = (float) (b.dp * sizeParam.value());
      float r = d / 2;

      area.ellipse(0, 0, scale * d, d);

      if (x >= (width - r)) {
        area.ellipse(-width, 0, scale * d, d);
      } else if (x <= r) {
        area.ellipse(+width, 0, scale * d, d);
      }

      area.popMatrix();
    }
  }
}

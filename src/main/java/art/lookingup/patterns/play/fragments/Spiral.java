package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.PI;

import art.lookingup.colors.Gradient;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;

public class Spiral extends Fragment {
  static final float period = .01f / Pattern.superSampling;

  static final double epsilon = 0.05; // Very small angle special case

  static final int colorDivisions = 100;
  static final int maxCount = 99;
  static final float colorPeriod = 0.01f / colorDivisions;

  final Parameter triples;
  final Parameter ylevel;
  final Parameter colorRate;

  // TODO Note that the angle changes as a result of the
  // superSampling parameter, because the rise across `width` pixels
  // varies with the super-width.  :shrug:
  final Parameter angle;
  final Parameter fill;

  static final int numSections = 24;

  Gradient gradient;
  boolean right;
  float strokeWidth;
  float leastX;
  float pitchY;
  float stepX;
  float lengthX;
  float lengthY;
  float colorShift;

  public static class Factory extends BaseFactory {
    public Factory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      return new Spiral(toString(), lx, width, height);
    }
  };

  public static class InvertedFactory extends BaseFactory {
    public InvertedFactory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      Spiral s = new Spiral(toString(), lx, width, height);
      s.inverted = true;
      return s;
    }
  };

  protected Spiral(String fragName, LX lx, int width, int height) {
    super(fragName, width, height);
    this.triples = newParameter("triples", 4, 1, 20);
    this.angle = newParameter("angle", PI / 8, -PI / 2, PI / 2);
    this.fill = newParameter("fill", 1, 0, 1);
    this.ylevel = newParameter("ylevel", 0, 0, height);
    this.colorRate = newParameter("color", 0, 0, 1);

    this.removeRateKnob();
    this.update();
  }

  int colorCount() {
    return Math.max((int) triples.value() * 3, 3);
  }

  public void update() {
    int count = colorCount();

    boolean right = angle.value() > 0;
    double theta = Math.abs(angle.value());

    if (theta < epsilon) {
      theta = epsilon;
    }

    double tanTh = Math.tan(theta);
    double pitch = width * tanTh;
    double least = height * Math.tan(Math.PI / 2 - theta);
    double stepY = pitch / count;
    double stepX = stepY / tanTh;
    double thick = stepX * Math.sin(theta);

    this.right = right;
    this.leastX = (float) least;
    this.pitchY = (float) pitch;
    this.stepX = (float) stepX;
    this.lengthX = 2 * leastX;
    this.lengthY = 2 * height;
    this.strokeWidth = (float) (fill.value() * thick);
    this.colorShift += lastElapsed() * colorRate.value() / colorPeriod;
    this.colorShift %= maxCount * colorDivisions;
  }

  @Override
  public void setup() {
    super.setup();

    this.area.beginDraw();
    this.gradient = Gradient.compute(area, maxCount * colorDivisions);
    this.area.endDraw();
  }

  @Override
  public void drawFragment() {
    this.update();

    int count = colorCount();

    area.strokeWeight(strokeWidth);

    int colorIdx = 0;
    float ylev = ylevel.value();
    float rightOffset = 0;
    float leftOffset = 0;
    float ylevRatio = ylev / height;
    float xShift = leastX * ylevRatio;

    if (right) {
      // Positive slope.
      rightOffset = xShift;
      leftOffset = leastX - xShift;
    } else {
      rightOffset = leastX - xShift;
      leftOffset = xShift;
    }

    for (float x = 0; x < width + stepX + rightOffset; x += stepX) {
      area.stroke(getColor(count, colorIdx++));

      area.line(
          x - (right ? lengthX : -lengthX),
          ylev - lengthY,
          x + (right ? lengthX : -lengthX),
          ylev + lengthY);

      colorIdx %= count;
    }

    colorIdx = count - 1;

    for (float x = 0 - stepX; x >= -stepX - leftOffset; x -= stepX) {
      area.stroke(getColor(count, colorIdx--));

      area.line(
          x - (right ? lengthX : -lengthX),
          ylev - lengthY,
          x + (right ? lengthX : -lengthX),
          ylev + lengthY);

      colorIdx += count;
      colorIdx %= count;
    }
  }

  int getColor(int count, int idx) {
    int step = gradient.size() / count;
    int base = (int) colorShift + step * idx;
    base %= gradient.size();

    return gradient.index(base);
  }
}

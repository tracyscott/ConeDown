package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;

public class Beacon extends Multi {
  static final float period = 0.1f / Pattern.superSampling;

  final int halfWidth;

  public static class Factory extends BaseFactory {
    FragmentFactory ff0;
    FragmentFactory ff1;

    public Factory(String fragName, FragmentFactory ff0, FragmentFactory ff1) {
      super(fragName, ff0, ff1);
      this.ff0 = ff0;
      this.ff1 = ff1;
    }

    public Fragment create(LX lx, int width, int height) {
      return new Beacon(
          lx, width, height, ff0.create(lx, width, height), ff1.create(lx, width, height));
    }
  };

  public Beacon(LX lx, int width, int height, Fragment f0, Fragment f1) {
    super("Beacon", lx, width, height, f0, f1);
    this.halfWidth = width / 2;
  }

  @Override
  public void drawFragment() {
    int f0pos = (int) (elapsed() / period);

    drawHalf(f0pos, fragments[0]);
    drawHalf(f0pos + halfWidth, fragments[1]);
  }

  void drawHalf(int pos, Fragment f) {
    int drawn = 0;
    while (drawn < halfWidth) {
      pos %= width;

      int take = Math.min(width - pos, halfWidth - drawn);

      area.copy(f.image, pos, 0, take, height, pos, 0, take, height);

      drawn += take;
      pos += take;
    }
  }
}

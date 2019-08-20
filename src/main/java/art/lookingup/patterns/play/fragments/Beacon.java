package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;

public class Beacon extends Multi {
  static final float period = 0.01f / Pattern.superSampling;

  final int halfWidth;

  public static class Factory extends BaseFactory {
    FragmentFactory[] ffs;

    public Factory(String fragName, FragmentFactory... ffs) {
      super(fragName, ffs);
      this.ffs = ffs;
    }

    public Fragment create(LX lx, int width, int height) {
      Fragment[] fl = new Fragment[ffs.length];
      for (int i = 0; i < ffs.length; i++) {
        fl[i] = ffs[i].create(lx, width, height);
      }
      return new Beacon(toString(), lx, width, height, fl);
    }
  };

  public Beacon(String fragName, LX lx, int width, int height, Fragment[] fs) {
    super(fragName, lx, width, height, fs);
    this.halfWidth = width / 2;
    this.removeRotateKnob();
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

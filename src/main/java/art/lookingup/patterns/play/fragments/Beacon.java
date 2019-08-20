package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;

public class Beacon extends Multi {
  static final float period = 0.01f / Pattern.superSampling;

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
    this.removeRotateKnob();
  }

  @Override
  public void drawFragment() {
    int partWidth = width / fragments.length;
    int pos = (int) (elapsed() / period);

    for (int i = 0; i < fragments.length; i++) {
      int take =
          (i < fragments.length - 1) ? partWidth : width - (fragments.length - 1) * partWidth;
      drawPart(pos, fragments[i], take);
      pos += take;
    }
  }

  void drawPart(int pos, Fragment f, int drawWidth) {
    int drawn = 0;
    while (drawn < drawWidth) {
      pos %= width;

      int take = Math.min(width - pos, drawWidth - drawn);

      area.copy(f.image, pos, 0, take, height, pos, 0, take, height);

      drawn += take;
      pos += take;
    }
  }
}

package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Parameter;
import heronarts.lx.LX;
import processing.core.PConstants;

public class Blend extends Multi {
  int[] modeLookup =
      new int[] {
        PConstants.BLEND,
        PConstants.ADD,
        PConstants.SUBTRACT,
        PConstants.DARKEST,
        PConstants.LIGHTEST,
        PConstants.DIFFERENCE,
        PConstants.EXCLUSION,
        PConstants.MULTIPLY,
        PConstants.SCREEN,
        PConstants.REPLACE,
      };

  final Parameter mode;

  public static class Factory extends BaseFactory {
    FragmentFactory a, b;

    public Factory(String fragName, FragmentFactory a, FragmentFactory b) {
      super(fragName);
      this.a = a;
      this.b = b;
    }

    public Fragment create(LX lx, int width, int height) {
      return new Blend(
          toString(), lx, width, height, a.create(lx, width, height), b.create(lx, width, height));
    }
  };

  protected Blend(String fragName, LX lx, int width, int height, Fragment a, Fragment b) {
    super(fragName, lx, width, height, a, b);
    this.mode = newParameter("mode", 1 /* ADD */, 0, modeLookup.length - 1);

    noRateKnob();
  }

  public void drawFragment() {
    int m = modeLookup[(int) mode.value()];
    area.copy(fragments[0].image, 0, 0, width, height, 0, 0, width, height);
    area.blend(fragments[1].image, 0, 0, width, height, 0, 0, width, height, m);
  }
};

package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import heronarts.lx.LX;

public class Solid extends Fragment {
  final Parameter r;
  final Parameter g;
  final Parameter b;

  public static class Factory extends BaseFactory {
    public Factory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      return new Solid(toString(), lx, width, height);
    }
  };

  protected Solid(String fragName, LX lx, int width, int height) {
    super(fragName, width, height);
    this.r = newParameter("r", 255, 0, 255);
    this.g = newParameter("g", 255, 0, 255);
    this.b = newParameter("b", 255, 0, 255);
    noDefaultKnobs();
  }

  @Override
  public void drawFragment() {
    area.background(r.value(), g.value(), b.value());
  }
}

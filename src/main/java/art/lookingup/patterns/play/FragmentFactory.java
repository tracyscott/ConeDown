package art.lookingup.patterns.play;

import heronarts.lx.LX;

public interface FragmentFactory {
  public Fragment create(LX lx, int width, int height);

  public void setParent(FragmentFactory parent);

  public String toString();
};

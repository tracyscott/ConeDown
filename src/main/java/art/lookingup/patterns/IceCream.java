package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class IceCream extends RPattern {

  public IceCream(LX lx) {
    super(lx);
  }

  public void render(double deltaMs) {
    for (LXPoint scoopPoint : ConeDownModel.scoopPoints) {
      colors[scoopPoint.index] = LXColor.rgb(180, 180, 180);
    }
    for (LXPoint conePoint : ConeDownModel.conePoints) {
      colors[conePoint.index] = LXColor.rgb(127, 127, 25);
    }
  }
}

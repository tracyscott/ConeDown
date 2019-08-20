package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class GalacticTest extends RPattern {

  public BooleanParameter vertical = new BooleanParameter("vert", true);
  public DiscreteParameter numColors = new DiscreteParameter("colors", 2,3, 5);

  int pos = 0;

  public GalacticTest(LX lx) {
    super(lx);
    addParameter(vertical);
    addParameter(numColors);
  }

  public void render(double deltaDrawMs) {
    int panelCount = 0;

    int i = 0;
    for (LXPoint p : lx.getModel().getPoints()) {
      int val = i % numColors.getValuei();
      switch (val) {
        case 0:
          colors[p.index] = Colors.RED;
          break;
        case 1:
          colors[p.index] = Colors.GREEN;
          break;
        case 2:
          colors[p.index] = Colors.BLUE;
          break;
        case 3:
          colors[p.index] = Colors.WHITE;
          break;
      }
      i++;
    }

  }

}

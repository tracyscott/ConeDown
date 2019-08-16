package art.lookingup.patterns;

import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class GalacticWire extends RPattern {
  public DiscreteParameter posKnob = new DiscreteParameter("pos", 0,0, 400);

  public GalacticWire(LX lx) {
    super(lx);
    addParameter(posKnob);
  }

  public void render(double deltaDrawMs) {
    int i = 0;
    for (LXPoint p : lx.getModel().getPoints()) {
      if (i < posKnob.getValuei()) {
        colors[p.index] = Colors.RED;
      } else {
        colors[p.index] = Colors.BLACK;
      }
      i++;
    }
  }
}

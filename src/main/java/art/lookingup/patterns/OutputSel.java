package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Output;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.List;

public class OutputSel extends LXPattern {

  public DiscreteParameter outputNum = new DiscreteParameter("out", 1, 1, 33);

  public OutputSel(LX lx) {
    super(lx);
    addParameter(outputNum);
  }

  public void run(double deltaMs) {
    int i = 0;
    for (List<CXPoint> output : Output.allOutputsPoints) {
      int clr = LXColor.BLACK;
      if (i + 1 == outputNum.getValuei())
        clr = LXColor.WHITE;
      for (CXPoint p : output) {
        colors[p.index] = clr;
      }
      i++;
    }
  }
}

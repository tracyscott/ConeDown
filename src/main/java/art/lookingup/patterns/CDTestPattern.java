package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;

public class CDTestPattern extends RPattern {

  public BooleanParameter vertical = new BooleanParameter("vert", true);

  int pos = 0;

  public CDTestPattern(LX lx) {
    super(lx);
    addParameter(vertical);
  }

  public void render(double deltaDrawMs) {
    int panelCount = 0;

    for (Panel panel : ConeDownModel.allPanels) {
      /*
      for (CXPoint p : panel.getPoints()) {
        colors[p.index] = Colors.BLACK;
      }
      int panelPointNum = pos % panel.getPoints().size();
      CXPoint p = panel.getPoints().get(panelPointNum);
      colors[p.index] = Colors.WHITE;
      */
      for (CXPoint p : panel.getPoints()) {
        int val = (vertical.getValueb()) ? p.yCoord % 4 : p.xCoord % 4;
        switch (val) {
          case 0:
            colors[p.index] = Colors.GREEN;
            break;
          case 1:
            colors[p.index] = Colors.RED;
            break;
          case 2:
            colors[p.index] = Colors.BLUE;
            break;
          case 3:
            colors[p.index] = Colors.YELLOW;
            break;
        }
        if (vertical.getValueb())
          colors[p.index] = LXColor.gray(100f * (float)p.yCoord/ (float)p.panel.pointsHigh);
        else
          colors[p.index] = LXColor.gray(100f * (float)p.xCoord/ (float)p.panel.pointsWide);

        //panelPointNum++;
      }
      panelCount++;
    }
    pos++;

  }

}

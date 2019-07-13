package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;

public class CDTestPattern extends RPattern {

  public CDTestPattern(LX lx) {
    super(lx);
  }

  public void render(double deltaDrawMs) {
    int panelCount = 0;
    for (ConeDownModel.Panel panel : ConeDownModel.dancePanels) {
      colors[panel.getPoints().get(0).index] = Colors.GREEN;
      if (panelCount == 0) {
        colors[panel.getPoints().get(0).index] = Colors.WHITE;
      }
      colors[panel.getPoints().get(1).index] = Colors.RED;
      colors[panel.getPoints().get(2).index] = Colors.BLUE;
      colors[panel.getPoints().get(3).index] = Colors.YELLOW;
      panelCount++;
    }
  }

}

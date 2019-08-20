package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.Panel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

@LXCategory(LXCategory.FORM)
public class RandomWalk extends PanelBase {

  public RandomWalk(LX lx) {
    super(lx);
  }

  @Override
  protected void processPanelPoint(Panel panel, int panelNum) {
    Point pos = positions.get(panelNum);
    int direction =  ThreadLocalRandom.current().nextInt(0, 4);
    if (direction == 0) { // Left.
      pos.x -= 1;
      if (pos.x < 0) pos.x = 0;
    } else if (direction == 1) { // Right
      pos.x += 1;
      if (pos.x == panel.pointsWide) pos.x = panel.pointsWide - 1;
    } else if (direction == 2) { // Up
      pos.y += 1;
      if (pos.y == panel.pointsHigh) pos.y = panel.pointsHigh - 1;
    } else if (direction == 3) {
      pos.y -= 1;
      if (pos.y < 0) pos.y = 0;
    }

    for (CXPoint p : panel.getPoints()) {
      if (p.xCoord == pos.x && p.yCoord == pos.y) {
        colors[p.index] = LXColor.hsb(hueBase.getValuef(), satKnob.getValuef(), brightKnob.getValuef());
      } else {
        colors[p.index] = LXColor.hsba(0f, 0f, 0f, transparencyKnob.getValuef());
      }
    }
  }
}

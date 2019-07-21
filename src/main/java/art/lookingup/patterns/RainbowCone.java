package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.ConeDownModel.Panel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

/**
 * A basic non-processing pattern which paints a rainbow across all the panels by iterating
 * over the panels in the order with which they are present in the model.
 */
public class RainbowCone extends LXPattern {

  public final CompoundParameter speedParam = new CompoundParameter("speed", 0.25, -1, 1);

  private static final int NUM_PANELS = ConeDownModel.allPanels.size();
  private static final float PANEL_HUE_GAP = 1f / NUM_PANELS;

  float startHue = 0;

  public RainbowCone(LX lx) {
    super(lx);
    addParameter(speedParam);
  }

  @Override
  public void run(double deltaMs) {
    float curHue = startHue;
    for (Panel panel : ConeDownModel.allPanels) {
      float pixelHueGap = PANEL_HUE_GAP / panel.getPoints().size();
      for (LXPoint point : panel.getPoints()) {
        colors[point.index] = Colors.hsb(curHue, 1, 1);
        curHue += pixelHueGap;
      }
    }
    startHue -= PANEL_HUE_GAP * speedParam.getValuef();
  }
}


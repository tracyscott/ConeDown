package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import art.lookingup.colors.Colors;
import art.lookingup.ui.UIFirmata;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;


public class ScoopCompEfx extends LXEffect {

  public CompoundParameter intensityKnob = new CompoundParameter("intensity", 0.3, 0.1, 3.0);

  public ScoopCompEfx(LX lx) {

    super(lx);
    addParameter(intensityKnob);
  }

  @Override
  protected void onEnable() {
  }

  @Override
  public void run(double deltaMs, double amount) {
    for (LXPoint point : ConeDownModel.conePoints) {
      float hsb[] = {0f, 0f, 0f};
      Colors.RGBtoHSB(this.colors[point.index], hsb);
      hsb[2] = hsb[2] * intensityKnob.getValuef();
      int color = Colors.HSBtoRGB(hsb);
      this.colors[point.index] = color;
    }
    for (LXPoint point : ConeDownModel.dancePoints) {
      float hsb[] = {0f, 0f, 0f};
      Colors.RGBtoHSB(this.colors[point.index], hsb);
      hsb[2] = hsb[2] * intensityKnob.getValuef();
      int color = Colors.HSBtoRGB(hsb);
      this.colors[point.index] = color;
    }
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}

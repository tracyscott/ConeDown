package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import art.lookingup.ui.UIFirmata;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;


@LXCategory(LXCategory.TEXTURE)
public class DanceTileEfx extends LXEffect {

  public DanceTileEfx(LX lx) {
    super(lx);
  }

  @Override
  protected void onEnable() {
  }

  @Override
  public void run(double deltaMs, double amount) {
      for (LXPoint point : ConeDownModel.conePoints) {
        this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
      }
      for (LXPoint point : ConeDownModel.scoopPoints) {
        this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
      }
      for (Panel dancePanel: ConeDownModel.dancePanels) {
        CompoundParameter cp = ConeDown.firmataPortUI.getCompoundParameter(UIFirmata.NAME_BASE +
            dancePanel.danceXPanel + "_" + dancePanel.danceYPanel);
        for (CXPoint point: dancePanel.getPoints()) {
           if (cp != null && cp.getValuef() > 0.5f) {
             // don't overwrite.
          } else {
             this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
           }
        }

      }
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
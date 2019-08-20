package art.lookingup.ui;

import heronarts.lx.LX;
import heronarts.lx.parameter.*;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;

public class UISensorOverride extends UIConfig {
  public static final String NAME_BASE = "";

  public static final String title = "sensor override";
  public static final String filename = "sensoroverride.json";

  public static final int numTilesWide = 3;
  public static final int numTilesHigh = 3;
  public static final int numTiles = numTilesWide * numTilesHigh;

  public LX lx;

  public UISensorOverride(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int) ui.leftPane.global.getContentWidth();
    this.lx = lx;

    for (int y = numTilesHigh - 1; y >= 0; y--) {
      for (int x = 0; x < numTilesWide; x++) {
        for (int i = 0; i < 4; i++) {
          BooleanParameter bp = registerBooleanParameter(getParameterName(x, y, i), true);
        }
      }
    }
    save();

    buildUIToggles(ui, 4);
  }

  static public String getParameterName(int x, int y, int sensorNum) {
    return NAME_BASE + x + "_" + y + "_" + sensorNum;
  }

  public void buildUIToggles(LXStudio.UI ui, int knobsPerRow) {
    int knobCountThisRow = 0;
    setTitle(title);
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UI2dContainer horizContainer = null;
    for (LXParameter p : parameters) {
      if (p instanceof BooleanParameter) {
        if (knobCountThisRow == 0) {
          horizContainer = new UI2dContainer(0, 0, ui.leftPane.global.getContentWidth(), 25);
          horizContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
          horizContainer.setBorder(true);
          horizContainer.setPadding(0, 0, 0, 0);
          horizContainer.addToContainer(this);
        }

        UIButton button = (UIButton) new UIButton(knobCountThisRow * 20, 0, 35, 25)
            .setParameter((BooleanParameter) p)
            .setLabel(p.getLabel())
            .addToContainer(horizContainer);
        ((LXListenableParameter) p).addListener(this);
        ++knobCountThisRow;
        if (knobCountThisRow == knobsPerRow) {
          knobCountThisRow = 0;
        }
      }
    }
     // Button saving config.
    new UIButton(getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          save();
        }
      }
    }
    .setLabel("\u21BA").setMomentary(true).addToContainer(this);
  }

}

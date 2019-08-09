package art.lookingup.ui;

import art.lookingup.ConeFirmata;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

import java.util.ArrayList;
import java.util.List;

public class UIFirmata extends UIConfig {
  public static final String FIRMATA_PORT = "frmport";
  public static final String START_PIN = "start";
  public static final String NAME_BASE = "DF_";

  public static final String title = "firmata";
  public static final String filename = "firmataconfig.json";

  public static final int numTilesWide = 3;
  public static final int numTilesHigh = 3;
  public static final int numTiles = numTilesWide * numTilesHigh;

  public LX lx;

  public UIFirmata(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    registerStringParameter(FIRMATA_PORT, "COM3");

    for (int y = numTilesHigh-1; y >=0 ; y--) {
      for (int x = 0; x < numTilesWide; x++) {
        CompoundParameter cp = registerCompoundParameter(NAME_BASE + x + "_" + y, 0f, 0f, 1f);
        cp.setValue(0f);  // Turn off sensor value on start up.
      }
    }
    registerDiscreteParameter(START_PIN, 2, 0, 40);
    save();

    buildUI(ui, 3);
  }

  /**
   * @return A list of compound parameters, one for each pin.
   */
  public List<CompoundParameter> getPinParameters() {
    List<CompoundParameter> params = new ArrayList<CompoundParameter>();
    for (LXParameter p : parameters) {
      if (p.getLabel().startsWith(NAME_BASE)) {
        params.add((CompoundParameter)p);
      }
    }
    return params;
  }

  /**
   * Re-saving the config will restart Firmata.  This can be used to manually restart it without a full
   * Cone Down restart in the event that Firmata gets wedged.
   */
  @Override
  public void onSave() {
    // Recreate Firmata
    ConeFirmata.reloadFirmata(getStringParameter(UIFirmata.FIRMATA_PORT).getString(), numTiles * 4,
          getDiscreteParameter(UIFirmata.START_PIN).getValuei(), getPinParameters());
  }
}

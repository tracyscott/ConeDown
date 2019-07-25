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
  public static final String PIN_BASE = "pin_";

  public static final String title = "firmata";
  public static final String filename = "firmataconfig.json";
  public static final int numPins = 16;
  public LX lx;
  private boolean parameterChanged = false;

  public UIFirmata(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    registerStringParameter(FIRMATA_PORT, "COM3");
    registerDiscreteParameter(START_PIN, 8, 0, 40);

    for (int i = 0; i < numPins; i++) {
      registerCompoundParameter(PIN_BASE + i, 0f, 0f, 1f);
    }
    save();

    buildUI(ui);
  }

  /**
   * @return A list of compound parameters, one for each pin.
   */
  public List<CompoundParameter> getPinParameters() {
    List<CompoundParameter> params = new ArrayList<CompoundParameter>();
    for (LXParameter p : parameters) {
      if (p.getLabel().startsWith(PIN_BASE)) {
        params.add((CompoundParameter)p);
      }
    }
    return params;
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
  }

  @Override
  public void onSave() {
    // Only reconfigure if a parameter changed.
    if (parameterChanged) {
      // Recreate Firmata
      ConeFirmata.reloadFirmata(getStringParameter(UIFirmata.FIRMATA_PORT).getString(), numPins,
          getDiscreteParameter(UIFirmata.START_PIN).getValuei(), getPinParameters());
      parameterChanged = false;
    }
  }
}

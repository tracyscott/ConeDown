package art.lookingup.ui;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

public class OutputMapping extends UIConfig {
  public static final String OUTPUTBASE = "out";

  public static final String title = "output map";
  public static final String filename = "outputmap.json";
  public LX lx;
  private boolean parameterChanged = false;

  String[] defaultOutputMapping = {
      "I1_door, H1_door, G1, F1, I2, H2, G2, F2, I3_milli, H3, G3, F3, I4_micro, G4, F4",
      "H4, I5_nano, H5, G5, F5, H6_milli, G6, F6, H7_micro, G7, F7, H8_nano, G8, F8",
      "H9_nano, G9, F9, H10_micro, G10, F10, H11_milli, G11, F11, I6_nano, H12, G12, F12, H13",
      "I7_micro, G13, F13, I8_milli, H14, G14, F14, I9, H15, G15, F15, I10_door, H16_door, G16, F16",
      "upperin_ring, upperout_ring, lowerin_ring, lowerout_ring",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",

      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
  };

  public OutputMapping(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    this.lx = lx;

    for (int i = 1; i <= 32; i++) {
      registerStringParameter(OUTPUTBASE + i, defaultOutputMapping[i-1]);
    }

    save();

    buildUI(ui);
  }

  public String getOutputMapping(int outputNum) {
    return getStringParameter(OUTPUTBASE + outputNum).getString();
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    parameterChanged = true;
  }

  @Override
  public void onSave() {
    if (parameterChanged) {
      //Output.restartOutput(lx);
      parameterChanged = false;
    }
  }
}

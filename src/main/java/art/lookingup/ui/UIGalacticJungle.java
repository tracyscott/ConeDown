package art.lookingup.ui;

import art.lookingup.Output;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;

import java.util.logging.Logger;

public class UIGalacticJungle extends UIConfig {

  private static final Logger logger = Logger.getLogger(UIGalacticJungle.class.getName());

  public static final String ENABLED = "enabled";
  public static final String CAR_1_IP = "ip1";
  public static final String CAR_1_PORT = "port1";
  public static final String CAR_1_UNIV = "univ1";
  public static final String CAR_2_IP = "ip2";
  public static final String CAR_2_PORT = "port2";
  public static final String CAR_2_UNIV = "univ2";
  public static final String CAR_3_IP = "ip3";
  public static final String CAR_3_PORT = "port3";
  public static final String CAR_3_UNIV = "univ3";
  public static final String CAR_4_IP = "ip4";
  public static final String CAR_4_PORT = "port4";
  public static final String CAR_4_UNIV = "univ4";
  public static final String CAR_5_IP = "ip5";
  public static final String CAR_5_PORT = "port5";
  public static final String CAR_5_UNIV = "univ5";

  public static final String title = "Galactic";
  public static final String filename = "galacticjungle.json";
  public LX lx;
  private boolean parameterChanged = false;

  public UIGalacticJungle(final LXStudio.UI ui, LX lx) {
    super(ui, title, filename);
    int contentWidth = (int)ui.leftPane.global.getContentWidth();
    this.lx = lx;

    registerBooleanParameter(ENABLED, false);
    registerStringParameter(CAR_1_IP, "192.168.2.120");
    registerStringParameter(CAR_1_PORT, "6454");
    registerStringParameter(CAR_1_UNIV, "100");
    registerStringParameter(CAR_2_IP, "192.168.2.134");
    registerStringParameter(CAR_2_PORT, "6454");
    registerStringParameter(CAR_2_UNIV, "100");
    registerStringParameter(CAR_3_IP, "192.168.2.134");
    registerStringParameter(CAR_3_UNIV, "100");
    registerStringParameter(CAR_3_PORT, "6454");
    registerStringParameter(CAR_4_IP, "192.168.2.134");
    registerStringParameter(CAR_4_PORT, "6454");
    registerStringParameter(CAR_4_UNIV, "100");
    registerStringParameter(CAR_5_IP, "192.168.2.134");
    registerStringParameter(CAR_5_PORT, "6454");
    registerStringParameter(CAR_5_UNIV, "100");

    save();

    buildUI(ui);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if (p instanceof BooleanParameter) {
      BooleanParameter bp = (BooleanParameter) p;
      logger.info("Setting galactic output: " + bp.getValueb());
      if (Output.carOutput != null)
        Output.carOutput.enabled.setValue(bp.getValueb());
    } else {
      parameterChanged = true;
    }
  }

  @Override
  public void onSave() {
    // Only reconfigure if a parameter changed.
    // If we save new parameters, pause our output, rebuild the datagrams
    // and then put our output enabled back to it's original value.
    // NOTE(tracy): outputGalacticJungle always adds the output in a disabled state.
    if (parameterChanged) {
      boolean originalEnabled = lx.engine.output.enabled.getValueb();
      LXDatagramOutput galacticOutput = Output.carOutput;
      boolean originalGalacticOutput = false;
      if (galacticOutput != null)
        originalGalacticOutput = galacticOutput.enabled.getValueb();
      lx.engine.output.enabled.setValue(false);
      if (galacticOutput != null)
        galacticOutput.enabled.setValue(false);
      Output.outputGalacticJungle(lx);
      if (galacticOutput != null)
        galacticOutput.enabled.setValue(originalGalacticOutput);
      parameterChanged = false;
      lx.engine.output.enabled.setValue(originalEnabled);
    }
  }
}

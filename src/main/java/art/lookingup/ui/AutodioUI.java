package art.lookingup.ui;

import art.lookingup.Autodio;
import heronarts.lx.LX;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

import java.util.logging.Logger;

public class AutodioUI extends UICollapsibleSection {
  private static final Logger logger = Logger.getLogger(AutodioUI.class.getName());
  public Autodio aa;
    public UIKnob bestLow;
    public UIKnob bestMid;
    public UIKnob bestHigh;

  public AutodioUI(LXStudio.UI ui, LX lx, Autodio aa) {
    super (ui,0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("Auto Audio");
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    knobsContainer.addToContainer(this);
    this.aa = aa;
    bestLow = new UIKnob(aa.bestLow);
    bestLow.addToContainer(knobsContainer);
    bestMid = new UIKnob(aa.bestMid);
    bestMid.addToContainer(knobsContainer);
    bestHigh = new UIKnob(aa.bestHigh);
    bestHigh.addToContainer(knobsContainer);
  }
}

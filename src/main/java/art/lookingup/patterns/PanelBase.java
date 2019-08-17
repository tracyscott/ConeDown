package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static art.lookingup.ConeDown.GLOBAL_FRAME_RATE;

/**
 * Base class for per-panel pixel animations.
 */
@LXCategory(LXCategory.FORM)
abstract public class PanelBase extends RPattern {
  protected double currentFrame = 0.0;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", GLOBAL_FRAME_RATE, 0.0, GLOBAL_FRAME_RATE + 10)
          .setDescription("Controls the frames per second.");
  public final CompoundParameter satKnob =
      new CompoundParameter("Sat", 100f, 0f, 100f)
          .setDescription("Saturation");
  public final CompoundParameter brightKnob =
      new CompoundParameter("Bright", 100f, 0f, 100f)
          .setDescription("Brightness");
  public final CompoundParameter hueBase =
      new CompoundParameter("Hue", 0f, 0f, 360f)
          .setDescription("Hue base value");

  public final CompoundParameter transparencyKnob =
      new CompoundParameter("trans", 100f, 0f, 100f)
          .setDescription("Transparency. 0 for decals.");

  public CompoundParameter blurKnob = new CompoundParameter("blur", 0.25, 0.0, 1.0);

  List<Point> positions;

  protected boolean originalBlurEnabled = false;
  protected float originalBlurAmount = 0.0f;


  public PanelBase(LX lx) {
    super(lx);
    addParameter(fpsKnob);
    addParameter(satKnob);
    addParameter(brightKnob);
    addParameter(hueBase);
    addParameter(blurKnob);
    addParameter(transparencyKnob);
    positions = new ArrayList<Point>();
    for (int i = 0; i < ConeDownModel.allPanels.size(); i++) {
      positions.add(new Point(0, 0));
    }
  }

  public void render(double deltaMs) {
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs / 1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int) currentFrame > previousFrame) {
      int i = 0;
      for (Panel panel : ConeDownModel.allPanels) {
        processPanelPoint(panel, i);
        i++;
      }
      previousFrame = (int) currentFrame;
    }
  }

  abstract protected void processPanelPoint(Panel panel, int panelNum);

  @Override
  public void onActive() {
    // Reset the guard that prevents the next text item from starting to show
    // while we are performing our fade transition to the next pattern.

    LXChannel channel = getChannel();
    LXEffect effect = channel.getEffect("Blur");
    if (effect != null) {
      originalBlurEnabled = effect.isEnabled();
      CompoundParameter amount = (CompoundParameter) effect.getParameters().toArray()[2];
      if (amount != null) {
        originalBlurAmount = amount.getValuef();
        amount.setValue(blurKnob.getValue());
      }
      effect.enable();
    }
  }

  @Override
  public void onInactive() {
    LXChannel channel = getChannel();
    LXEffect effect = channel.getEffect("Blur");
    if (effect != null) {
      CompoundParameter amount = (CompoundParameter) effect.getParameters().toArray()[2];
      if (amount != null) {
        amount.setValue(originalBlurAmount);
        if (!originalBlurEnabled) {
          effect.disable();
        }
      }
    }
    super.onInactive();
  }
}

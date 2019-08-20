package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDownModel;
import art.lookingup.Panel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import static art.lookingup.ConeDown.GLOBAL_FRAME_RATE;

@LXCategory(LXCategory.FORM)
public class HuePanel extends RPattern {
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

  public final CompoundParameter hueRange =
      new CompoundParameter("Range", 360f, 1f, 360f)
        .setDescription("Hue range");

  public final CompoundParameter speed =
      new CompoundParameter("Speed", 1f, 1f, 40f);

  public HuePanel(LX lx) {
    super(lx);
    addParameter(fpsKnob);
    addParameter(satKnob);
    addParameter(brightKnob);
    addParameter(hueBase);
    addParameter(hueRange);
    addParameter(speed);
  }

  static public float positiveSin(float input) {
    return (float)Math.sin(input);//*0.5f; // + 0.5f;
  }

  public void render(double deltaMs) {
    int panelCount = 0;
    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs / 1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int) currentFrame > previousFrame) {
      for (Panel panel : ConeDownModel.allPanels) {
        float panelMaxXTexCoord = panel.pointsWide - 1;
        float panelMaxYTexCoord = panel.pointsHigh - 1;
        /*
        float bottomLeftHue = (hueBase.getValuef() + 1f * (hueRange.getValuef() / 4f)) * positiveSin((float)currentFrame);
        float bottomRightHue =  (hueBase.getValuef() + 2f * (hueRange.getValuef() / 4f)) * positiveSin((float)currentFrame);
        float topRightHue = (hueBase.getValuef() + 3f * (hueRange.getValuef() / 4f)) * positiveSin((float)currentFrame);
        float topLeftHue = (hueBase.getValuef() + 3f * (hueRange.getValuef() / 4f)) * positiveSin((float)currentFrame);
        */
        float phase = (speed.getValuef() / 40f) * (float)currentFrame;
        float bottomLeftHue = hueBase.getValuef() + hueRange.getValuef() * (float)Math.sin(phase);
        float bottomRightHue = hueBase.getValuef() + hueRange.getValuef() * (float)Math.sin(phase + Math.PI/4f);
        float topRightHue = hueBase.getValuef() + hueRange.getValuef() * (float)Math.sin(phase + 2f * Math.PI/4f);
        float topLeftHue = hueBase.getValuef() + hueRange.getValuef() * (float)Math.sin(phase + 3f * Math.PI/4f);

        if (bottomLeftHue < 0f) bottomLeftHue += 360f;
        if (bottomRightHue < 0f) bottomRightHue += 360f;
        if (topRightHue < 0f) topRightHue += 360f;
        if (topLeftHue < 0f) topLeftHue += 360f;
        if (bottomLeftHue > 360f) bottomLeftHue -= 360f;
        if (bottomRightHue > 360f) bottomRightHue -= 360f;
        if (topRightHue > 360f) topRightHue -= 360f;
        if (topLeftHue > 360f) topLeftHue -= 360f;
        // if 1, 1 then top right
        // if 0, 1 then top left
        // if 0, 0 then bottom left
        // if 1, 0, then bottom right
        for (CXPoint p : panel.getPoints()) {
          float horizontalInterp = (float) p.xCoord / panelMaxXTexCoord;
          float verticalInterp = (float) p.yCoord / panelMaxYTexCoord;
          float pHue = ((bottomLeftHue + horizontalInterp * (bottomRightHue - bottomLeftHue))
              + (bottomRightHue + verticalInterp * (topRightHue - bottomRightHue))) / 2f;
          if (pHue < 0f) pHue += 360f;
          if (pHue > 360f) pHue -= 360f;
          colors[p.index] = LXColor.hsb(pHue, satKnob.getValue(), brightKnob.getValue());
        }
      }
      previousFrame = (int) currentFrame;
    }
  }
}

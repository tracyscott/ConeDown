package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;

import static java.lang.Math.abs;

@LXCategory(LXCategory.FORM)
public class Noise extends PGPixelPerfect {

  public final CompoundParameter weight =
      new CompoundParameter("weight", 1.0, 0.1, 3.0);
  public final CompoundParameter rotateMult =
      new CompoundParameter("rotateMult", 1.0, 0.1, 5.0);
  public final CompoundParameter length =
      new CompoundParameter("length", 10.0, 1.0, 50.0);
  public final CompoundParameter xDensity =
      new CompoundParameter("xDensity", 5.0, 2.0, 20.0);
  public final CompoundParameter yDensity =
      new CompoundParameter("yDensity", 5.0, 2.0, 20.0);
  public final CompoundParameter swing =
      new CompoundParameter("swing", 6.0, 1.0, 20.0);
  public final CompoundParameter swingAmt =
      new CompoundParameter("swingAmt", 0.0, 1.0, 20.0);
  public final CompoundParameter blur =
      new CompoundParameter("blur", 20f, 0, 255f);
  public final CompoundParameter satKnob =
      new CompoundParameter("Sat", 100f, 0f, 100f)
          .setDescription("Saturation");
  public final CompoundParameter brightKnob =
      new CompoundParameter("Bright", 100f, 0f, 100f)
          .setDescription("Brightness");
  public final CompoundParameter hueKnob =
      new CompoundParameter("Hue", 0f, 0f, 360f)
          .setDescription("Hue base value");



  float xstart, xnoise, ystart, ynoise;

  float xstartNoise, ystartNoise;

  public Noise(LX lx) {
    super(lx, null);
    addParameter(weight);
    addParameter(rotateMult);
    addParameter(length);
    addParameter(xDensity);
    addParameter(yDensity);
    addParameter(swing);
    addParameter(swingAmt);
    addParameter(blur);
    addParameter(hueKnob);
    addParameter(satKnob);
    addParameter(brightKnob);
    xstartNoise = (float)(20.0 * Math.random());
    ystartNoise = (float)(20.0 * Math.random());
    xstart = (float)(10.0 * Math.random());
    ystart = (float)(10.0 * Math.random());
  }

  @Override
  protected void draw(double deltaDrawMs) {
      //System.err.println("WTF " + renderWidth + " " + renderHeight);
    //pg.background(0);
    pg.fill(0, 255 - (int)blur.getValuef());
    pg.rect(0, 0, pg.width, pg.height);
    pg.fill(255);
    pg.smooth();
    pg.colorMode(PConstants.HSB, 360, 100, 100);
    pg.fill(hueKnob.getValuef(), satKnob.getValuef(), brightKnob.getValuef());

    xstartNoise += 0.05;
    ystartNoise += 0.05;

    xstart += 2.0 * Math.sin((ConeDown.pApplet.noise(xstartNoise)*0.5)-0.25);
    ystart += 2.0 * Math.cos((ConeDown.pApplet.noise(ystartNoise)*0.5)-0.25);

    xnoise = xstart;
    ynoise = ystart;

    for (int y = 0; y <= renderHeight; y+=yDensity.getValuef() * getSuperSampling()) {
      ynoise += 0.1;
      xnoise = xstart;
      for (int x = 0; x <= renderWidth; x+=xDensity.getValuef() * getSuperSampling()) {
        xnoise += 0.1;
        drawPointLine(x, y, ConeDown.pApplet.noise(xnoise, ynoise));
      }
    }
  }

  protected void drawPointLine(float x, float y, float noiseFactor) {
    pg.pushMatrix();
    x = x + swingAmt.getValuef() * (float)Math.sin(((float)currentFrame/swing.getValuef())) * getSuperSampling();
    pg.translate(x, y);
    noiseFactor = 1.0f;
    pg.rotate(noiseFactor * ConeDown.pApplet.radians(360) * rotateMult.getValuef());
    pg.strokeWeight(weight.getValuef() * getSuperSampling());
    //pg.stroke(255, 190);
    pg.stroke(hueKnob.getValuef(), satKnob.getValuef(), brightKnob.getValuef());
    float len = 20f * Math.abs((float)Math.sin( ((float)currentFrame)/20f )) + 10f;
    len = length.getValuef() * getSuperSampling();
    pg.line(0, 0, len, 0);
    pg.popMatrix();
  }

}

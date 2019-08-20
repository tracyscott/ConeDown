package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RTracers extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(RTracers.class.getName());

  public CompoundParameter blurKnob = new CompoundParameter("blur", 0f, 0.0, 255f);
  public CompoundParameter bgAlpha = new CompoundParameter("bgalpha", 0.0, 0.0, 1.0);
  // Probability of a new triangle show up each frame.  Should we allow multiple triangles per frame?  nah,
  // probably not.
  public CompoundParameter numTracers = new CompoundParameter("num", 60.0, 0.0, 200.0);
  // Max triangle size.  Allow only small triangles for example.
  public CompoundParameter maxSize = new CompoundParameter("max", 15.0, 3.0, 45.0);
  public CompoundParameter minSize = new CompoundParameter("min", 3.0, 1.0, 30.0);
  public CompoundParameter minVelocity = new CompoundParameter("minv", 1.0, 0.0, 30.0);
  public CompoundParameter maxVelocity = new CompoundParameter("maxv", 20.0, 0.0, 60.0);
  public CompoundParameter maxOffScreen = new CompoundParameter("off", 0.0, 0.0, 30.0);

  public CompoundParameter fillAlpha = new CompoundParameter("falpha", 0.75, 0.0, 1.0);
  public CompoundParameter sizeMultiKnob =new CompoundParameter("sizeMult", 1.0f, 1.0f, 20.0f)
      .setDescription("Dynamic size multiplier (map from bass)");
  public CompoundParameter vertOff = new CompoundParameter("vertOff", 0.0f, 0.0f, 30.0f)
      .setDescription("Dynamic y offset. (map from mid/high freq");
  public BooleanParameter outlinedKnob = new BooleanParameter("outline", true)
      .setDescription("Include black outlines");

  protected boolean originalBlurEnabled = false;
  protected float originalBlurAmount = 0.0f;

  public static class Tracer {
    public LXPoint pos;
    public boolean hasBeenShown = false;
    public int size;  // radius of tracer.
    public float velocityX;
    public float velocityY;
    public float[] hsb;
    int notShownCounter = 0;
  };

  List<Tracer> tracers = new ArrayList<Tracer>();

  public RTracers(LX lx) {
    super(lx, "");
    addParameter(paletteKnob);
    addParameter(blurKnob);
    addParameter(numTracers);
    addParameter(bgAlpha);
    addParameter(maxSize);
    addParameter(minSize);
    addParameter(minVelocity);
    addParameter(maxVelocity);
    addParameter(maxOffScreen);
    addParameter(fillAlpha);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);
    addParameter(sizeMultiKnob);
    addParameter(vertOff);
    addParameter(randomPaletteKnob);
    addParameter(outlinedKnob);
  }

  /*
  A new random polygon with background fades.  Random 3 or 4 points?  Maybe just 3 points
  * since that will always easily render regardless of the point ordering.  Polygon should
  * be rendered with transparency on a 25% (configurable) transparent background for fading away.
  * What to do for coloring?  Be able to specify saturation while randomizing hue.  Or possibly
  * selecting a random number from 0 to sizeof(palette) and then use that color if palette box is
  * checked?  Or if palette dropdown has a selected palette so can be both rainbow and redbull.
   */
  public void draw(double drawDeltaMs) {
    // pg.colorMode(HSB, 255.0f);
    pg.colorMode(PConstants.HSB, 1.0f, 1.0f, 1.0f, 255.0f);
    pg.fill(0, 255 - (int)blurKnob.getValuef());
    pg.rect(0, 0, pg.width, pg.height);
    pg.fill(255);

    pg.smooth();
    //pg.background(0.0f, 0.0f, 0.0f, bgAlpha.getValuef());

    updateTracers();
    processTracers();
    for (Tracer tracer : tracers) {
      drawTracer(tracer);
    }
  }

  /**
   * Check if tracer is in the render window.
   */
  public boolean isTracerVisible(Tracer t) {
    if (t.pos.x >= 0 && t.pos.x < renderWidth && t.pos.y >= 0 && t.pos.y < renderHeight) {
      return true;
    }
    return false;
  }

  /**
   * Check if tracer needs to be reset.
   */
  public boolean tracerNeedsReset(Tracer t) {
    if ((t.hasBeenShown && !isTracerVisible(t)) || t.notShownCounter == 100) {
      return true;
    }
    return false;
  }

  /**
   * Resets a tracer to some initial condition based on our parameter settings.
   * @param tracer
   */
  public void resetTracer(Tracer tracer) {
    // Reset the tracer based on our parameter knobs.
    tracer.pos.y = renderHeight + 10.0f + 20.0f * (float)Math.random();
    tracer.pos.x = (int)(Math.random() * renderWidth);
    tracer.velocityY = (float)(Math.random() * -0.5 * maxVelocity.getValue());
    tracer.velocityX = (float)(Math.random() * 2.0 * maxVelocity.getValue() - maxVelocity.getValue());
    tracer.hasBeenShown = false;
    tracer.notShownCounter = 0;
    tracer.size = (int)(Math.random() * (maxSize.getValuef() - minSize.getValuef()) + minSize.getValuef());
    getNewHSB(tracer.hsb);
  }

  /**
   * Process tracers, culling tracers that have finished and replacing them with new tracers.
   */
  public void processTracers() {
    //logger.info("processing Tracers.");
    for (Tracer tracer : tracers) {
      if (isTracerVisible(tracer))
        tracer.hasBeenShown = true;
      else
        tracer.notShownCounter++;
      tracer.pos.x += tracer.velocityX;
      // TODO(Tracy): Now that we are mapping to a cylinder, we would like to horizontally wrap the tracer.
      // It also requires us to draw it twice on the left and right edges.
      if (tracer.pos.x > renderWidth) {
        tracer.pos.x -= renderWidth;
      }
      if (tracer.pos.x < 0) {
        tracer.pos.x += renderWidth;
      }
      tracer.pos.y += tracer.velocityY;
      if (tracerNeedsReset(tracer)) {
        //logger.info("resetting tracer");
        resetTracer(tracer);
      }
    }
  }

  /**
   * Update tracer positions.
   */
  public void updateTracers() {
    if (tracers.size() < numTracers.getValuef()) {
      logger.info("Initializing " + numTracers.getValuef() + " tracers.");
      int i = tracers.size();
      while (tracers.size() < numTracers.getValuef()) {
        Tracer t = new Tracer();
        t.pos = new LXPoint(i * 5f, 40f, 0f);
        float size = (float)Math.random();
        t.size = (int)(size * (maxSize.getValuef() - minSize.getValuef()) + minSize.getValuef());
        // Assign hue.
        t.hsb = new float[3];
        getNewHSB(t.hsb);
        resetTracer(t);
        tracers.add(t);
        i++;
      }
    }
  }

  public void drawTracer(Tracer tracer) {
    /*
    float centerX = ((float)Math.random() * renderWidth + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float centerY = ((float)Math.random() * renderHeight + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1XDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt1YDelta = ((float)Math.random() * maxTriSize.getValuef());
    float pt2XDelta = ((float)Math.random() * maxTriSize.getValuef());
    float pt2YDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt3XDelta = ((float)Math.random() * 2.0f * maxTriSize.getValuef()) - maxTriSize.getValuef();
    float pt3YDelta = ((float)Math.random() * -1.0f * maxTriSize.getValuef());
    */
    /*
    float pt1X = ((float)Math.random() * renderWidth + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt1Y = (float)Math.random() * renderHeight;
    float pt2X = ((float)Math.random() * renderWidth + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt2Y = (float)Math.random() * renderHeight;
    float pt3X = ((float)Math.random() * renderWidth + 2.0f * maxOffScreen.getValuef()) - maxOffScreen.getValuef();
    float pt3Y = (float)Math.random() * renderHeight;
    */


    if (outlinedKnob.getValueb()) {
      pg.strokeWeight(1f);
    } else {
      pg.strokeWeight(0f);
    }
    pg.fill(tracer.hsb[0], tracer.hsb[1], tracer.hsb[2]); //, fillAlpha.getValuef());
    //pg.triangle(pt1X, pt1Y, pt2X, pt2Y, pt3X, pt3Y);
    // If rightOverlap = renderWidth - (pos.x + tracer.size/2f) < 0 then we need to redraw the ellipse at
    // rightOverlap.
    float multipliedSize = tracer.size * sizeMultiKnob.getValuef();
    float offsetY = tracer.pos.y - vertOff.getValuef();
    pg.ellipse(tracer.pos.x, offsetY, multipliedSize, multipliedSize);
    float rightOverlap = (float)renderWidth - ((float)tracer.pos.x + multipliedSize/2f);
    if (rightOverlap < 0) {
      pg.ellipse(-rightOverlap - multipliedSize/2f, offsetY, multipliedSize, multipliedSize);
    }
    float leftEdge = (float)tracer.pos.x - ((float)multipliedSize)/2f;
    if (leftEdge < 0f) {
      pg.ellipse(renderWidth + leftEdge + multipliedSize/2f, offsetY, multipliedSize, multipliedSize);
    }
  }

  @Override
  public void onActive() {
    super.onActive();
    // Reset the guard that prevents the next text item from starting to show
    // while we are performing our fade transition to the next pattern.

    LXChannel channel = getChannel();
    LXEffect effect = channel.getEffect("Blur");
    if (effect != null) {
      originalBlurEnabled = effect.isEnabled();
      CompoundParameter amount = (CompoundParameter) effect.getParameters().toArray()[2];
      if (amount != null) {
        originalBlurAmount = amount.getValuef();
        //amount.setValue(blurKnob.getValue());
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

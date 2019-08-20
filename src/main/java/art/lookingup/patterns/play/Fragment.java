package art.lookingup.patterns.play;

import static processing.core.PConstants.ARGB;

import art.lookingup.ConeDown;
import art.lookingup.Projection;
import java.util.ArrayList;
import java.util.List;
import processing.core.PGraphics;
import processing.core.PImage;

public abstract class Fragment {
  public static final float rotatePeriod = 0.001f;

  public final PImage image;

  public PGraphics area;

  public final String graphics;
  public final String fragName;
  public final List<Parameter> params = new ArrayList<>();
  public final Parameter rate;
  public final Parameter rotate;

  public final int width;
  public final int height;

  public Pattern pattern;
  float elapsed;
  float rotation;
  public boolean clearBackground;

  public boolean inverted;

  protected Fragment(String fragName, int width, int height) {
    this(fragName, width, height, Pattern.defaultGraphics);
  }

  protected Fragment(String fragName, int width, int height, String graphics) {
    this.fragName = fragName;
    this.width = width;
    this.height = height;
    this.graphics = graphics;
    this.elapsed = 0; // Note: updated by Pattern.preDraw()
    this.image = new PImage(this.width, this.height, ARGB);
    this.clearBackground = true;

    // Note, to change this from the default in a Fragment, use
    //   this.rate.setValue(0.2f);

    this.rate = newParameter("rate", 0.2f, -1, 1);
    this.rotate = newParameter("rotate", 0f, -1, 1);
    this.inverted = false;
  }

  protected Parameter newParameter(String name, float init, float min, float max) {
    name = String.format("%s.%s", this.fragName, name);
    Parameter p = new Parameter(this, name, init, min, max);
    params.add(p);
    return p;
  }

  protected void noRateKnob() {
    params.remove(rate);
    params.remove(rotate);
  }

  public void onActive() {}

  public float elapsed() {
    return elapsed;
  }

  public PGraphics area() {
    return this.area;
  }

  protected Projection getProjection() {
    return ConeDown.getProjection(Pattern.superSampling);
  }

  public void setup() {}

  public void preDrawFragment(float vdelta) {
    elapsed += vdelta * rate.value();
    rotation += (vdelta * rotate.value()) / rotatePeriod;
  }

  public abstract void drawFragment();

  public void create(Pattern p) {
    this.pattern = p;
    this.area = p.createGraphics(p.app, width, height, graphics);
  }

  public void registerParameters(Parameter.Adder adder) {
    for (Parameter p : params) {
      adder.registerParameter(p);
    }
  }

  public void render(float vdelta) {
    preDrawFragment(vdelta);

    area.beginDraw();
    area.pushMatrix();
    if (clearBackground) {
      area.background(0, 0, 0, 255);
    }
    if (inverted) {
      area.translate(width, height);
      area.scale(-1, -1);
    }
    drawFragment();
    area.popMatrix();
    area.endDraw();
    area.loadPixels();

    // TODO Looks like this image copy can be avoided if we use the P2D renderer.  Why?
    // See e.g., how PacmanGame's P2D PGraphics doesn't need a copy.

    int roffset = (int) rotation % width;

    image.copy(area, roffset, 0, width - roffset, height, 0, 0, width - roffset, height);
    if (roffset != 0) {
      image.copy(area, 0, 0, roffset, height, width - roffset, 0, roffset, height);
    }

    image.loadPixels();
  }
};

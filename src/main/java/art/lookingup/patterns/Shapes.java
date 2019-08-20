package art.lookingup.patterns;

import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.List;
import java.util.logging.Logger;

import static java.lang.Math.ceil;
import static processing.core.PConstants.P2D;

public class Shapes extends PGPixelPerfect {
  private static final Logger logger = Logger.getLogger(Shapes.class.getName());

  public DiscreteParameter paletteKnob = new DiscreteParameter("palette", 0, 0, Colors.ALL_PALETTES.length + 1);
  public CompoundParameter blurKnob = new CompoundParameter("blur", 0.25, 0.0, 1.0);
  public CompoundParameter numShapes = new CompoundParameter("num", 20f, 0f, 60f)
      .setDescription("Number of shapes");
  public CompoundParameter sizeMult = new CompoundParameter("sizeMult", 1f, 0.1f, 10f)
      .setDescription("Dynamic size multiplier");
  public CompoundParameter minSize = new CompoundParameter("minSize", 1f, 0.1f, 10f)
      .setDescription("Minimum size");
  public CompoundParameter maxSize = new CompoundParameter("maxSize", 1f, 1f, 100f)
      .setDescription("Maximum size");
  public BooleanParameter filledKnob = new BooleanParameter("filled", false);
  public CompoundParameter hue = new CompoundParameter("hue", 0.0f, 0.0f, 1.0f);
  public CompoundParameter saturation = new CompoundParameter("sat", 0.5, 0.0, 1.0);
  public CompoundParameter bright = new CompoundParameter("bright", 1.0, 0.0, 1.0);
  public final BooleanParameter randomPaletteKnob =
      new BooleanParameter("RandomPlt", true);
  public CompoundParameter minVelocity = new CompoundParameter("minv", 1.0, 0.0, 30.0);
  public CompoundParameter maxVelocity = new CompoundParameter("maxv", 20.0, 0.0, 60.0);
  public CompoundParameter maxOffScreen = new CompoundParameter("off", 0.0, 0.0, 30.0);

  List<Shape> shapes;
  public int[] palette;
  public int randomPalette = 0;


  static public class Shape {
    int size;
    float hue;
    SPoint pos;
    int sides = 4;
    public boolean hasBeenShown = false;
    public float velocityX;
    public float velocityY;
    public float[] hsb;
    int notShownCounter = 0;
  }

  static public class SPoint {
    public SPoint(float x, float y) {
      this.x = x;
      this.y = y;
    }

    float x;
    float y;
  }

  public Shapes(LX lx) {
    super(lx, P2D);
    addParameter(blurKnob);
    addParameter(paletteKnob);
    addParameter(numShapes);
    addParameter(sizeMult);
    addParameter(minSize);
    addParameter(maxSize);
    addParameter(filledKnob);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);
    addParameter(randomPaletteKnob);
    addParameter(minVelocity);
    addParameter(maxVelocity);
    addParameter(maxOffScreen);


  }

  public void draw(double deltaMs) {
    // Shapes shapes.  Use beginPoly to draw a diamond.
    // Diamond shape around 0 is (-1/2, 0), (0, -1/2), (1/2, 0), (0, 1/2)
    // Need to scale by size.

  }

  public void drawDiamond() {
    pg.beginShape();
    pg.vertex(-1/2f, 0f);
    pg.vertex(0, -1/2f);
    pg.vertex(1/2f, 0f);
    pg.vertex(0f, 1/2f);
    pg.endShape();
  }

  /**
   * Update tracer positions.
   */
  public void updateShapes() {
    if (shapes.size() < numShapes.getValuef()) {
      logger.info("Initializing " + numShapes.getValuef() + " shapes.");
      int i = shapes.size();
      while (shapes.size() < numShapes.getValuef()) {
        Shape s = new Shape();
        s.pos = new SPoint(i * 5f, 40f);
        float size = (float)Math.random();
        s.size = (int)(size * (maxSize.getValuef() - minSize.getValuef()) + minSize.getValuef());
        // Assign hue.
        s.hsb = new float[3];
        getNewHSB(s.hsb);
        resetShape(s);
        shapes.add(s);
        i++;
      }
    }
  }

  /**
   * Resets a tracer to some initial condition based on our parameter settings.
   * @param shape
   */
  public void resetShape(Shape shape) {
    // Reset the tracer based on our parameter knobs.
    shape.pos.y = pg.height + 10.0f + 20.0f * (float)Math.random();
    shape.pos.x = (int)(Math.random() * pg.width);
    shape.velocityY = (float)(Math.random() * -0.5 * maxVelocity.getValue());
    shape.velocityX = (float)(Math.random() * 2.0 * maxVelocity.getValue() - maxVelocity.getValue());
    shape.hasBeenShown = false;
    shape.notShownCounter = 0;
    shape.size = (int)(Math.random() * (maxSize.getValuef() - minSize.getValuef()) + minSize.getValuef());
    getNewHSB(shape.hsb);
  }


  public void getNewHSB(float[] hsb) {
    int whichPalette = paletteKnob.getValuei();
    if (randomPaletteKnob.getValueb())
      whichPalette = randomPalette;

    if (whichPalette == 0) {
      hsb[0] = (float) Math.random();
      // TODO(tracy): Add a palette for manual hue set
      hsb[1] = saturation.getValuef();
      hsb[2] = bright.getValuef();
    } if (whichPalette == 1) {
      hsb[0] = hue.getValuef();
      hsb[1] = saturation.getValuef();
      hsb[2] = bright.getValuef();
    }
    else {
      int[] palette = Colors.ALL_PALETTES[whichPalette - 2];
      int index = (int) ceil(Math.random() * (palette.length)) - 2;
      if (index < 0) index = 0;
      int color = palette[index];
      Colors.RGBtoHSB(color, hsb);
    }
  }


}

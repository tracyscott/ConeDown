package art.lookingup.patterns;

import static art.lookingup.ConeDown.GLOBAL_FRAME_RATE;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.P2D;

import art.lookingup.ConeDown;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.fluid.DwFluid2D;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import processing.opengl.PGraphics2D;

/**
 * Utility class for Fluid Simulation.
 */
@LXCategory(LXCategory.FORM)
public class CrazyFluid extends PGPixelPerfect {
  public CompoundParameter radiusKnob = new CompoundParameter("radius", 10f, 1f, 20f)
      .setDescription("Emitter radius");

  public CompoundParameter temperatureKnob = new CompoundParameter("temp", 1f, 0.5f, 10f)
      .setDescription("Temperature multiplier");

  public CompoundParameter freqKnob = new CompoundParameter("freq", 1f, 0.1f, 10f)
      .setDescription("Frequency multiplier");

  public CompoundParameter vorticityKnob = new CompoundParameter("vort", 0.2f, 0.1f, 5.0f);

  public CompoundParameter intensityKnob = new CompoundParameter("intensity", 1.0f, 0.1f, 3.0f);

  private static final Logger logger = Logger.getLogger(FluidPP.class.getName());

  static public class FPoint {
    public FPoint(float x, float y) {
      this.x = x;
      this.y = y;
    }
    float x;
    float y;
  }

  List<FPoint> positions = new ArrayList<FPoint>();

  private class MyFluidData implements DwFluid2D.FluidData {

    // update() is called during the fluid-simulation update step.
    @Override
    public void update(DwFluid2D fluid) {

      float px, py, radius, r, g, b, intensity, temperature;

      py = 5;
      radius = radiusKnob.getValuef();
      intensity = intensityKnob.getValuef();
      // add impulse: density + temperature
      float animator = abs(sin(freqKnob.getValuef() * fluid.simulation_step*0.01f));
      temperature = animator * 10f * temperatureKnob.getValuef();
      float hsb[] = new float[3];
      int color;

      // add impulse: density + temperature
      px = positions.get(0).x;
      getNewHSB(hsb, 0);
      color = LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
      r = ((float)(LXColor.red(color)&0xFF))/255f;
      g = ((float)(LXColor.green(color)&0xFF))/255f;
      b = ((float)(LXColor.blue(color)&0xFF))/255f;

      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = positions.get(1).x;
      getNewHSB(hsb, 1);
      color = LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
      r = ((float)(LXColor.red(color)&0xFF))/255f;
      g = ((float)(LXColor.green(color)&0xFF))/255f;
      b = ((float)(LXColor.blue(color)&0xFF))/255f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = positions.get(2).x;
      getNewHSB(hsb, 2);
      color = LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
      r = ((float)(LXColor.red(color)&0xFF))/255f;
      g = ((float)(LXColor.green(color)&0xFF))/255f;
      b = ((float)(LXColor.blue(color)&0xFF))/255f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = positions.get(3).x;
      getNewHSB(hsb, 3);
      color = LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
      r = ((float)(LXColor.red(color)&0xFF))/255f;
      g = ((float)(LXColor.green(color)&0xFF))/255f;
      b = ((float)(LXColor.blue(color)&0xFF))/255f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = positions.get(4).x;
      getNewHSB(hsb, 4);
      color = LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
      r = ((float)(LXColor.red(color)&0xFF))/255f;
      g = ((float)(LXColor.green(color)&0xFF))/255f;
      b = ((float)(LXColor.blue(color)&0xFF))/255f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);

      px = positions.get(5).x;
      getNewHSB(hsb, 5);
      color = LXColor.hsb(360f * hsb[0], 100f * hsb[1], 100f * hsb[2]);
      r = ((float)(LXColor.red(color)&0xFF))/255f;
      g = ((float)(LXColor.green(color)&0xFF))/255f;
      b = ((float)(LXColor.blue(color)&0xFF))/255f;
      fluid.addDensity(px, py, radius, r, g, b, intensity);
      fluid.addTemperature(px, py, radius, temperature);
    }
  }

  private int fluidgrid_scale = 1;
  private DwFluid2D fluid;
  private PGraphics2D pg_fluid;
  private PGraphics2D pg_obstacles;
  private int     BACKGROUND_COLOR           = 0;
  private boolean UPDATE_FLUID               = true;
  private boolean DISPLAY_FLUID_TEXTURES     = true;
  private boolean DISPLAY_FLUID_VECTORS      = false;
  private int     DISPLAY_fluid_texture_mode = 0;

  public CrazyFluid(LX lx) {
    super(lx, "");
    fpsKnob.setValue(GLOBAL_FRAME_RATE);
    addParameter(paletteKnob);
    addParameter(randomPaletteKnob);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);
    addParameter(radiusKnob);
    addParameter(temperatureKnob);
    addParameter(freqKnob);
    addParameter(vorticityKnob);
    addParameter(intensityKnob);
    for (int i = 0; i < 6; i++) {
      positions.add(new FPoint((float)i * ((float)renderWidth)/6f, 0f));
    }
    updateParams();
  }

  public void updatePositions() {
    int i = 0;
    for (FPoint p : positions) {
      p.x = p.x + (freqKnob.getValuef()/10f); // * (i+1);
      if (p.x > renderWidth) p.x = p.x - renderWidth;
      if (p.x < 0) p.x = p.x + renderWidth;
      i++;
    }
  }

  @Override
  public void updateParams() {
    super.updateParams();

    DwPixelFlow context = new DwPixelFlow(ConeDown.pApplet);
    context.print();
    context.printGL();
    // fluid simulation
    logger.info(renderWidth + "," + renderHeight);
    fluid = new DwFluid2D(context, renderWidth, renderHeight, fluidgrid_scale);
    // set some simulation parameters
    fluid.param.dissipation_density     = 0.999f;
    fluid.param.dissipation_velocity    = 0.99f;
    fluid.param.dissipation_temperature = 0.80f;
    fluid.param.vorticity               = vorticityKnob.getValuef();
    // interface for adding data to the fluid simulation
    MyFluidData cb_fluid_data = new MyFluidData();
    fluid.addCallback_FluiData(cb_fluid_data);
    // pgraphics for fluid
    pg_fluid = (PGraphics2D) ConeDown.pApplet.createGraphics(renderWidth, renderHeight, P2D);
    pg_fluid.smooth(4);
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    // pgraphics for obstacles
    pg_obstacles = (PGraphics2D) ConeDown.pApplet.createGraphics(renderWidth, renderHeight, P2D);
    pg_obstacles.smooth(0);
    pg_obstacles.beginDraw();
    pg_obstacles.clear();
    // border-obstacle
    pg_obstacles.strokeWeight(1);
    pg_obstacles.stroke(100);
    pg_obstacles.noFill();
    pg_obstacles.rect(0, 0, pg_obstacles.width, pg_obstacles.height);
    pg_obstacles.endDraw();
  }

  public void draw(double deltaDrawMs) {
    pg.background(0);
    updatePositions();

    fluid.addObstacles(pg_obstacles);
    fluid.update();
    // clear render target
    pg_fluid.beginDraw();
    pg_fluid.background(BACKGROUND_COLOR);
    pg_fluid.endDraw();
    fluid.renderFluidTextures(pg_fluid, DISPLAY_fluid_texture_mode);
    pg_fluid.loadPixels();
    pg_fluid.updatePixels();
    pg.image(pg_fluid, 0, 0);
    pg.loadPixels();
    pg.updatePixels();
  }

  @Override
  public void onActive() {
    super.onActive();
    updateParams();
  }
}

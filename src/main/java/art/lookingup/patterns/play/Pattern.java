package art.lookingup.patterns.play;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PApplet;
import processing.core.PGraphics;

abstract public class Pattern extends LXPattern {
  final PApplet app;
  final PGraphics graph;

  final int width;
  final int height;

  final CompoundParameter speedKnob =
      new CompoundParameter("GlobalSpeed", 500, 0, 1000)
        .setDescription("Varies global speed.");
    
  float current;
  float elapsed;

  public Pattern(LX lx, PApplet app, int width, int height) {
      super(lx);

      this.app = app;
      this.graph = app.createGraphics(width, height);
      this.width = width;
      this.height = height;
  }

  @Override
  public void run(double deltaMs) {
    render(deltaMs);
  }

  @Override
  public void onActive() {
    setup();
  }
    
  @Override
  public void onInactive() {
    tearDown();
  }

  void render(double deltaMs) {
      current += (float)((speedKnob.getValue() / 1000) * deltaMs);

      float vdelta = current - elapsed;

      preDraw(vdelta);
      graph.beginDraw();
      draw(vdelta);
      graph.endDraw();

      elapsed = current;
  }

  void setup() {
      System.err.println("Setup called");
  }

  void tearDown() {
      System.err.println("Teardown called");
  }

  void preDraw(double deltaDrawMs) {
  }

  void draw(double deltaDrawMs) {
    graph.background(0);
	
  }
}

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
      new CompoundParameter("GlobalSpeed", 0, 0, 10000)
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
  }

  void tearDown() {
  }

  void preDraw(double deltaDrawMs) {
  }

  void draw(double deltaDrawMs) {
    graph.background(0);
	
  }

  // @Override
  // public void draw(double deltaMs) {
  //   int count = (int)cnt3Knob.getValue() * 3;
  //   float incr = (float)heightKnob.getValue();
  //   float spin = relapsed;
  //   for (int idx = 0; idx < count; idx++) {
  // 	float base = -incr - spin * incr;
  // 	float spirals = (float)count;
  // 	float y0 = base + ((float)idx / spirals) * incr;
  // 	int c = gradients[count][idx];
  // 	pg.stroke(c);
  // 	for (float y = y0; y < pg.height+incr; y += incr) {
  // 	    pg.line(0, y, pg.width, y+incr);
  // 	}
  //   }
  // }
}

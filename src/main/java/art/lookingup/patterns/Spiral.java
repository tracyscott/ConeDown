package art.lookingup.patterns;

// import static processing.core.PConstants.P2D;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;
import processing.core.PImage;

import art.lookingup.ConeDown;
import art.lookingup.colors.Colors;

public class Spiral extends PGPixelPerfect {

  final static int number = 20;
    
  public CompoundParameter heightKnob = new CompoundParameter("height", 1.0, 4.0, number);
  public CompoundParameter spinKnob = new CompoundParameter("spin", 0, 0, 1.0);
  public CompoundParameter widthKnob = new CompoundParameter("width", 0, 0, number);
  public CompoundParameter countKnob = new CompoundParameter("count", 1, 1, number);

  PImage []gradients;

  public Spiral(LX lx) {
    super(lx, "");
    addParameter(heightKnob);
    addParameter(spinKnob);
    addParameter(widthKnob);
    addParameter(countKnob);

    setGradients();
  }

  void setGradients() {
      int minColor = Colors.hsb(0, 1, 1);
      int maxColor = Colors.hsb(0.4999999f, 1, 1);

      // System.err.println("minmax " + minColor + " " + maxColor);

      this.gradients = new PImage[number+1];
      for (int count = 0; count <= number; count++) {
	  PGraphics gr = ConeDown.pApplet.createGraphics(count, 1);
	  gr.beginDraw();
	  for (int i = 0; i < count; i++) {
	      float r = (float)i / (float)count;
	      int c = gr.lerpColor(minColor, maxColor, r);
	      //System.err.println("Look r=" + r + " c=" + c + " i=" + i + " count=" + count);
	      gr.set(i, 0, c);
	  }
	  gr.endDraw();
	  gr.loadPixels();
	  this.gradients[count] = gr.get();
      }
  }

  @Override
  public void draw(double drawDeltaMs) {
    pg.background(0);
    pg.strokeWeight((float)widthKnob.getValue());

    int count = (int)countKnob.getValue();
    float incr = (float)heightKnob.getValue();

    for (int idx = 0; idx < count; idx++) {
	float base = -(float)spinKnob.getValue() * incr;
	float spirals = (float)countKnob.getValue();
	float y0 = base + ((float)idx / spirals) * incr;

	for (float y = y0; y < pg.height; y += incr) {
	    pg.stroke(gradients[count].get(idx, 0));
	    pg.line(0, y, pg.width, y+incr);
	}
    }
  }
}

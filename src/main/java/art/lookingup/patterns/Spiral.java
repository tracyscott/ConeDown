package art.lookingup.patterns;

// import static processing.core.PConstants.P2D;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;
import processing.core.PImage;

import art.lookingup.ConeDown;
import art.lookingup.colors.Colors;

public class Spiral extends PGPixelPerfect {

  final static int number = 33;

  public CompoundParameter heightKnob = new CompoundParameter("height", 62, number/4, number*4);
  public CompoundParameter widthKnob = new CompoundParameter("width", 3.9, 0, number/3);
  public CompoundParameter speedKnob = new CompoundParameter("speed", 0.18, 0, 1);
  public CompoundParameter cnt3Knob = new CompoundParameter("count/3", 4, 1, number/3);

  PImage []gradients;

  public Spiral(LX lx) {
    super(lx, "");
    addParameter(heightKnob);
    addParameter(speedKnob);
    addParameter(cnt3Knob);
    addParameter(widthKnob);
    removeParameter(fpsKnob);

    setGradients();
  }

  void setGradients() {
      this.gradients = new PImage[number+1];
      for (int count = 3; count <= number; count += 3) {
	  PGraphics gr = ConeDown.pApplet.createGraphics(count, 1);
	  gr.beginDraw();

	  int min1 = Colors.hsb(0, 1, 1);
	  int max1 = Colors.hsb(0.333333333f, 1, 1);
	  for (int i = 0; i < count/3; i++) {
	      float r = (float)i / (float)(count/3);
	      int c = gr.lerpColor(min1, max1, r);
	      gr.set(i, 0, c);
	  }

	  int min2 = Colors.hsb(0.333333333f, 1, 1);
	  int max2 = Colors.hsb(0.666666667f, 1, 1);
	  for (int i = 0; i < count/3; i++) {
	      float r = (float)i / (float)(count/3);
	      int c = gr.lerpColor(min2, max2, r);
	      gr.set(i+(count/3), 0, c);
	  }

	  int min3 = Colors.hsb(0.666666667f, 1, 1);
	  int max3 = Colors.hsb(1, 1, 1);
	  for (int i = 0; i < count/3; i++) {
	      float r = (float)i / (float)(count/3);
	      int c = gr.lerpColor(min3, max3, r);
	      gr.set(i+(2*count/3), 0, c);
	  }
	  
	  gr.endDraw();
	  gr.loadPixels();

	  this.gradients[count] = gr.get();
      }
  }

  float relapsed;

  @Override
  public void draw(double deltaMs) {
    pg.background(0);
    pg.strokeWeight((float)widthKnob.getValue());

    relapsed += (float)(speedKnob.getValue() * deltaMs / 1000);

    int count = (int)cnt3Knob.getValue() * 3;
    float incr = (float)heightKnob.getValue();
    float spin = relapsed;

    for (int idx = 0; idx < count; idx++) {
	float base = -incr - spin * incr;
	float spirals = (float)count;
	float y0 = base + ((float)idx / spirals) * incr;

	int c = gradients[count].get(idx, 0);

	pg.stroke(c);
	
	for (float y = y0; y < pg.height+incr; y += incr) {
	    pg.line(0, y, pg.width, y+incr);
	}
    }
  }
}

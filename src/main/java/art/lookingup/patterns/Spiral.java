package art.lookingup.patterns;

// import static processing.core.PConstants.P2D;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;
import processing.core.PImage;

import art.lookingup.ConeDown;
import art.lookingup.colors.Colors;
import art.lookingup.colors.Gradient;

public class Spiral extends PGPixelPerfect {

  final static int number = 33;

  public CompoundParameter heightKnob = new CompoundParameter("height", 62, number/4, number*4);
  public CompoundParameter widthKnob = new CompoundParameter("width", 3.9, 0, number/3);
  public CompoundParameter speedKnob = new CompoundParameter("speed", 0.18, 0, 1);
  public CompoundParameter cnt3Knob = new CompoundParameter("count/3", 4, 1, number/3);

  Gradient []gradients;

  public Spiral(LX lx) {
    super(lx, "");
    addParameter(heightKnob);
    addParameter(speedKnob);
    addParameter(cnt3Knob);
    addParameter(widthKnob);
    removeParameter(fpsKnob);
  }

  @Override
  public void preDraw(double deltaMs) {
      if (gradients == null) {
	  setGradients();
      }
  }

  void setGradients() {
      gradients = new Gradient[number+1];

      for (int count = 3; count <= number; count += 3) {
	  this.gradients[count] = Gradient.compute(pg, count);
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

	int c = gradients[count].index(idx);

	pg.stroke(c);
	
	for (float y = y0; y < pg.height+incr; y += incr) {
	    pg.line(0, y, pg.width, y+incr);
	}
    }
  }
}

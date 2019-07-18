package art.lookingup.patterns;

import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;
import processing.core.PImage;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Projection;
import art.lookingup.colors.Colors;
import art.lookingup.colors.Gradient;
import art.lookingup.patterns.shapes.Parabola;

public class Balls extends PGPixelPerfect {
    final static public int maxCount = 100;
    final static public int maxSize = 100;
    final static public float xRate = 100;
    final static public float yRate = 10;

  public CompoundParameter sizeKnob = new CompoundParameter("size", 10, 0, maxSize);
  public CompoundParameter speedKnob = new CompoundParameter("speed", 0.5, 0, 1);
  public CompoundParameter countKnob = new CompoundParameter("count", 1, 1, maxCount);

  public Projection projection;
  Random random = new Random();

  public Balls(LX lx) {
    super(lx, "");
    addParameter(sizeKnob);
    addParameter(speedKnob);
    addParameter(countKnob);
    removeParameter(fpsKnob);

    this.projection = new Projection(lx.getModel());
    this.balls = new Ball[maxCount];
  }

  float relapsed;

  public class Ball {
      float dp;
      float xcurrent;
      float x0;
      float vp;
      float period;
      float elapsed;
      int color;
      Parabola parabola;

      Ball(float dp, float x, float y, float vp, float period, int color) {
	  this.dp = dp;
	  this.xcurrent = x;
	  this.x0 = x;
	  this.vp = vp;
	  this.period = period;
	  this.parabola = new Parabola(200, y);
	  this.color = color;
      }

      void update(float e) {
	  xcurrent += vp * (e - elapsed) * (float) speedKnob.getValue() * xRate;
	  elapsed = e;
      }

      float getX() {
	  return xcurrent % ConeDownModel.POINTS_WIDE;
      }

      float getY() {
	  float xoffset = xcurrent - x0;
	  float yoffset = xoffset % period;

	  float y = parabola.Value(yoffset / period - 0.5f);

	  return y;
      }
  }

  int []gradient;
  Ball []balls;

    @Override
    public void preDraw(double deltaMs) {
	if (gradient != null) {
	    return;
	}

	PGraphics pg = ConeDown.pApplet.createGraphics(1, 1);
	pg.beginDraw();
	gradient = Gradient.get(pg, 100);
	for (int i = 0; i < maxCount; i++) {
	    this.balls[i] = new Ball((float) (0.3 + 0.7 * random.nextDouble()),
				     (float) random.nextDouble() * ConeDownModel.POINTS_WIDE,
				     4f * (float) (0.9 + 0.1 * random.nextDouble()) * ConeDownModel.POINTS_HIGH,
				     (float) (0.1 + 0.9 * random.nextDouble()),
				     (float) (0.1 + 0.9 * random.nextDouble()) * ConeDownModel.POINTS_WIDE,
				     this.gradient[random.nextInt(gradient.length)]);
	}
	pg.endDraw();
    }    

  @Override
  public void draw(double deltaMs) {
    pg.background(0);

    relapsed += (float)(speedKnob.getValue() * deltaMs / 1000);

    int count = (int)countKnob.getValue();

    for (int i = 0; i < count; i++) {
	Ball b = balls[i];

	b.update(relapsed);

	float x = b.getX();
	float y = b.getY();

	pg.pushMatrix();
	pg.translate(x, y);

	pg.noStroke();
	pg.fill(b.color);

	float d = b.dp * (float)sizeKnob.getValue();

	pg.scale(projection.xScale(0, y), 1);

	// TODO fix the seam, double draw near borders
	pg.ellipse(0, 0, d, d);

	pg.popMatrix();
    }
  }
}

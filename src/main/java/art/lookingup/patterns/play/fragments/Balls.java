package art.lookingup.patterns.play.fragments;

import java.util.Random;

import heronarts.lx.LX;

import processing.core.PGraphics;
import processing.core.PImage;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Projection;
import art.lookingup.colors.Colors;
import art.lookingup.colors.Gradient;
import art.lookingup.patterns.shapes.Parabola;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;

public class Balls extends Fragment {
  final static float period = 0.1f;
  final static int rateMult = 100;
  final static int maxCount = 100;
  final static int maxSize = 100;

  final Parameter sizeParam;
  final Parameter countParam;

  public Projection projection;

    public Balls(LX lx, int width, int height) {
	super(width, height);

	this.sizeParam = newParameter("size", 10, 0, maxSize);
	this.countParam = newParameter("count", 1, 1, maxCount);

	// this.projection = new Projection(lx.getModel(), 1);
	this.balls = new Ball[maxCount];
    }

  public class Ball {
      float dp;
      float xcurrent;
      float x0;
      float vp;
      float elap;
      float period;
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
	  xcurrent += vp * (e - elap) * rateMult;
	  this.elap = e;
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

  Gradient gradient;
  Ball []balls;

    @Override
    public void setup() {
	if (gradient != null) {
	    return;
	}

	area.beginDraw();
	Random random = new Random();

	gradient = Gradient.compute(area, maxCount);
	for (int i = 0; i < maxCount; i++) {
	    this.balls[i] = new Ball((float) (0.3 + 0.7 * random.nextDouble()),
				     (float) random.nextDouble() * ConeDownModel.POINTS_WIDE,
				     4f * (float) (0.9 + 0.1 * random.nextDouble()) * ConeDownModel.POINTS_HIGH,
				     (float) (0.1 + 0.9 * random.nextDouble()),
				     (float) (0.1 + 0.9 * random.nextDouble()) * ConeDownModel.POINTS_WIDE,
				     gradient.index(random.nextInt(gradient.size())));
	}

	area.endDraw();
    }    

  @Override
  public void drawFragment() {
      int count = (int)countParam.value();

    for (int i = 0; i < count; i++) {
	Ball b = balls[i];

	b.update(elapsed() * period);

	float x = b.getX();
	float y = b.getY();

	area.pushMatrix();
	area.translate(x, y);

	area.noStroke();
	area.fill(b.color);

	float d = b.dp * sizeParam.value();

	// area.scale(projection.xScale(0, y), 1);

	// TODO @@@ fix the seam, double draw near borders
	area.ellipse(0, 0, d, d);

	area.popMatrix();
    }
  }
}

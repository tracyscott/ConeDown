package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PGraphics;
import processing.core.PImage;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Projection;
import art.lookingup.colors.Colors;

public class Balls extends PGPixelPerfect {
    final static public int maxCount = 100;
    final static public int maxSize = 100;
    final static public float xRate = 100;
    final static public float yRate = 10;

  public CompoundParameter sizeKnob = new CompoundParameter("size", 10, 0, maxSize);
  public CompoundParameter speedKnob = new CompoundParameter("speed", 0, 0, 1);
  public CompoundParameter countKnob = new CompoundParameter("count", 10, 1, maxCount);

  public Projection projection;

  public Balls(LX lx) {
    super(lx, "");
    addParameter(sizeKnob);
    addParameter(speedKnob);
    addParameter(countKnob);
    removeParameter(fpsKnob);

    this.projection = new Projection(lx.getModel());

    this.balls = new Ball[maxCount];

    for (int i = 0; i < maxCount; i++) {
	this.balls[i] = new Ball(1, // (float) (0.3 + 0.7 * Math.random()),
				 (float) Math.random() * pg.width,
				 (float) Math.random() * pg.height,
				 (float) (0.3 + 0.7 * Math.random()));
    }
  }

  float relapsed;

  public class Ball {
      float dp;
      float x;
      float y;
      float vp;
      float elapsed;

      Ball(float dp, float x, float y, float vp) {
	  this.dp = dp;
	  this.x = x;
	  this.y = y;
	  this.vp = vp;
      }

      void update(float elapsed) {
	  this.x += this.vp * (elapsed - this.elapsed) * (float) speedKnob.getValue() * xRate;
	  this.x %= ConeDownModel.POINTS_WIDE;

	  // TODO
	  this.y += this.vp * (elapsed - this.elapsed) * (float) speedKnob.getValue() * yRate;
	  this.y %= ConeDownModel.POINTS_HIGH;

	  this.elapsed = elapsed;
      }
  }

  Ball []balls;

  @Override
  public void draw(double deltaMs) {
    pg.background(0);

    relapsed += (float)(speedKnob.getValue() * deltaMs / 1000);

    int count = (int)countKnob.getValue();

    for (int i = 0; i < count; i++) {
	Ball b = balls[i];

	b.update(relapsed);

	pg.pushMatrix();
	pg.translate(b.x, b.y);

	pg.stroke(255);
	pg.fill(255);

	float d = b.dp * (float)sizeKnob.getValue();

	pg.scale(projection.xScale(0, b.y), 1);

	// TODO fix the seam, double draw near borders
	pg.ellipse(0, 0, d, d);

	pg.popMatrix();
    }
  }
}

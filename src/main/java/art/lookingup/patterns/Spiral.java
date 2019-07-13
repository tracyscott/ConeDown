package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;

public class Spiral extends PGPixelPerfect {
  public CompoundParameter heightKnob = new CompoundParameter("height", 1.0, 4.0, 20.0);

  public Spiral(LX lx) {
    super(lx, "");
    addParameter(heightKnob);
  }

  @Override
  public void draw(double drawDeltaMs) {
    pg.background(0);
    pg.stroke(255);

    float incr = (float)heightKnob.getValue();
 
    for (float y = 0; y < pg.height; y+=incr) {
	pg.line(0, y, pg.width, y+incr);
    }
  }
}

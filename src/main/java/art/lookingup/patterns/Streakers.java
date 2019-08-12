package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;

public class Streakers extends PGFilterBase {
  public CompoundParameter blurPassKnob = new CompoundParameter("blurP", 0.0, 0.0, 40.0);
  public CompoundParameter widthKnob = new CompoundParameter("width", 3.0, 1.0, 46.0);
  public CompoundParameter heightKnob = new CompoundParameter("height", 3.0, 1.0, 46.0);
  public CompoundParameter numBars = new CompoundParameter("bars", 1.0, 1.0, 46.0);
  public CompoundParameter offKnob = new CompoundParameter("off", 2.0, 0.0, 30.0);
  public final CompoundParameter bgKnob =
      new CompoundParameter("BG", 0f,0f, 1f)
          .setDescription("BG brightness");

  protected int yPos = 0;

  public Streakers(LX lx) {
    super(lx);
    addParameter(blurPassKnob);
    addParameter(numBars);
    addParameter(widthKnob);
    addParameter(heightKnob);
    addParameter(offKnob);
    addParameter(bgKnob);
    doDirBlur = true;
    yPos = 0 - (int) offKnob.getValue();
  }

  @Override
  public void draw(double drawDeltaMs) {
    pg.background((int)(bgKnob.getValue()*255f));
    pg.fill(255);
    pg.noStroke();

    int xOffset = (int)(renderWidth / (numBars.getValue()-1));
    int x = 0;
    for (int i = 0; i < (int)(numBars.getValue()); i++) {
      pg.rect(x, yPos, (int) widthKnob.getValue(), (int) heightKnob.getValue());
      x += xOffset;
    }
    yPos++;
    if (yPos >  renderHeight + offKnob.getValue())
      yPos = 0 - (int)offKnob.getValue();


    dirBlurPasses = (int)blurPassKnob.getValue();
    super.draw(drawDeltaMs);
  }
}

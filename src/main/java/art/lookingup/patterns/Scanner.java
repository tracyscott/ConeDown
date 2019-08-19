package art.lookingup.patterns;

import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.FORM)
public class Scanner extends PGPixelPerfect {
  public final CompoundParameter bgKnob =
      new CompoundParameter("BG", 0f,0f, 1f)
          .setDescription("x pos");
  public final CompoundParameter widthKnob =
      new CompoundParameter("Width", 10f, 0f, 93f)
          .setDescription("width");
  public final CompoundParameter blurKnob =
      new CompoundParameter("Blur", 0.2f, 0.0f, 1.0f);

  protected int pos = 30;

  public Scanner(LX lx) {
    super(lx, "");
    addParameter(bgKnob);
    addParameter(widthKnob);
    addParameter(blurKnob);
    //addParameter(renderTarget);
  }

  public void draw(double drawDeltaMs) {
    //pg.background((int)(bgKnob.getValue()*255f));
    pg.fill((int)(bgKnob.getValue()*255f), 255 - 255 * blurKnob.getValuef());
    pg.rect(0, 0, pg.width, pg.height);
    //pos = (int)posKnob.getValue();
    int width = (int) widthKnob.getValue();
    //pos++;
    pg.stroke(Colors.WHITE);
    pg.fill(Colors.WHITE);

    pg.rect(pos,0, (int)(widthKnob.getValue()-1f), pg.height);
    if (pos + width > pg.width) {
      pg.rect(0, 0, (pos+width) - pg.width, pg.height);
      pos++;
      if (pos > pg.width) {
        pos = 1;
      }
    } else {
      pos++;
    }
  }
}

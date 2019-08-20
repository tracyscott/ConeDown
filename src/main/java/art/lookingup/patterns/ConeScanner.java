
package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

public class ConeScanner extends PGPixelPerfect {
  public final CompoundParameter bgKnob =
      new CompoundParameter("BG", 0f,0f, 1f)
          .setDescription("x pos");
  public final CompoundParameter widthKnob =
      new CompoundParameter("Width", 10f, 0f, 93f)
          .setDescription("width");
  public final CompoundParameter posKnob =
      new CompoundParameter("pos", 0f, 0f, 1f);
  public final BooleanParameter manualKnob =
      new BooleanParameter("manual", false);

  protected int pos = 0;

  public ConeScanner(LX lx) {
    super(lx, "");
    fpsKnob.setValue(0);
    addParameter(bgKnob);
    addParameter(widthKnob);
    addParameter(posKnob);
    addParameter(manualKnob);
  }

  public void draw(double drawDeltaMs) {

    if (manualKnob.getValueb()) {
      pos = (int)(posKnob.getValuef() * renderWidth);
    }
    pg.background((int)(bgKnob.getValue()*255f));
    int width = (int) widthKnob.getValue();
    pg.stroke(Colors.WHITE);
    pg.fill(Colors.WHITE);

    pg.rect(pos,0, (int)(widthKnob.getValue()-1f), renderHeight);
    if (pos + width > renderWidth) {
      pg.rect(0, 0, (pos + width) - renderWidth, renderHeight);
      if (!manualKnob.getValueb()) {
        pos++;
        if (pos > renderWidth) {
          pos = 1;
        }
      }
    } else {
      if (!manualKnob.getValueb())
        pos++;
    }
  }
}

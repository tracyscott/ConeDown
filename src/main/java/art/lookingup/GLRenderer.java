package art.lookingup;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class GLRenderer extends UILabel {

  public PGraphics glRender;
  public PImage renderBuffer;
  public long lastRender = 0;
  public int width;
  public int height;

  public GLRenderer(int width, int height) {
    super(0, 0, 10, 10);
    this.width = width;
    this.height = height;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    super.onDraw(ui, pg);
    if (glRender == null) {
      glRender = ConeDown.pApplet.createGraphics(width, height, PConstants.P3D);
      renderBuffer = new PImage(width, height);
    }
    long now = System.currentTimeMillis();
    long delta = now - lastRender;
    render((double)delta);
    glRender.loadPixels();
    renderBuffer.copy(glRender, 0, 0, glRender.width, glRender.height, 0, 0, renderBuffer.width, renderBuffer.height);
    renderBuffer.loadPixels();
    lastRender = now;
  }

  protected void render(double deltaDrawMs) {

  }
}

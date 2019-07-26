package art.lookingup.patterns;

import art.lookingup.GLRenderer;
import heronarts.lx.LX;
import heronarts.p3lx.ui.CustomDeviceUI;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.PI;

public class GLTest extends PGPixelPerfect implements CustomDeviceUI {

  public GLRenderer glRenderer;

  public GLTest(LX lx) {
    super(lx, "");
  }

  public void draw(double drawDeltaMs) {
    if (glRenderer.renderBuffer != null)
      pg.image(glRenderer.renderBuffer, 0, 0);
    glRenderer.setLabel("GL" + currentFrame % 2);
  }

  public void buildDeviceUI(UI ui, UI2dContainer device) {
    device.setContentWidth(100);
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setPadding(0, 0, 0, 0);
    UI2dContainer knobsContainer = new UI2dContainer(0, 30, device.getWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    glRenderer = new GLRenderer(renderWidth, renderHeight) {
      @Override
      public void render(double deltaMs) {
        System.out.println("Rendering!");
        glRender.beginDraw();
        glRender.background(0);
        glRender.lights();
        glRender.rectMode(CENTER);
        glRender.fill(190);
        glRender.noStroke();
        glRender.translate(20, 20, 0);
        glRender.rotateY(((int)currentFrame%16) * PI/16.0f);
        glRender.box(10);
        glRender.endDraw();
      }
    };
    glRenderer.setLabel("GL");
    glRenderer.addToContainer(knobsContainer);
    knobsContainer.addToContainer(device);
  }

}

package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import heronarts.lx.LX;

abstract class PGPixelPerfectWide extends PGBase {
  public PGPixelPerfectWide(LX lx, String drawMode) {
    super(lx, ConeDownModel.POINTS_WIDE * 2,
        ConeDownModel.POINTS_HIGH,
        drawMode);
  }

  protected void imageToPoints() {
    RenderImageUtil.imageToPointsPixelPerfectWide(lx.getModel(), pg, colors);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}

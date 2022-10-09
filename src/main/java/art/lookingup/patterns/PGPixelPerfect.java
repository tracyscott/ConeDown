package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.ConeDown;
import heronarts.lx.LX;

/**
 * Abstract base class for pixel perfect Processing drawings.  Use this
 * class for 1-1 pixel mapping with the rainbow.  The drawing will be
 * a rectangle but in physical space it will be distorted by the bend of
 * the rainbow. Gets FPS knob from PGBase.
 */
abstract class PGPixelPerfect extends PGBase {
  public PGPixelPerfect(LX lx, String drawMode) {
    super(lx, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH, drawMode);
  }

  public PGPixelPerfect(LX lx, String drawMode, int width, int height) {
    super(lx, width, height, drawMode);
  }

  protected void imageToPoints() {
      //RenderImageUtil.sampleRenderTarget(projection, renderTarget.getValuei(), pg, colors, 0, 0, !renderFullSize);
    RenderImageUtil.sampleRenderTarget(renderTarget.getValuei(), pg, colors, 0, 0);
  }

  // Implement PGGraphics drawing code here.  PGPixelPerfect handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);
}

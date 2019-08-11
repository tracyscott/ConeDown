package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

/**
 * Pixel perfect animated GIFs.  Uses base class with directory data/gifpp, default file of life2.gif, and
 * no antialias toggle.
 */
@LXCategory(LXCategory.FORM)
public class RainbowGIFPP extends RainbowGIFBase {

  public RainbowGIFPP(LX lx) {
    super(lx, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH,
        "gifpp/",
        "life2",
        false);

    addParameter(renderTarget);
    renderTarget.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        DiscreteParameter iKnob = (DiscreteParameter) parameter;
        // recreate our our pgraphics.
        int which = iKnob.getValuei();
        switch (which) {
          case 0:  // Default full render.
            imageWidth = ConeDownModel.POINTS_WIDE;
            imageHeight = ConeDownModel.POINTS_HIGH;
            break;
          case 1:
            imageWidth = ConeDownModel.dancePointsWide;
            imageHeight = ConeDownModel.dancePointsHigh;
            break;
          case 2:
            imageWidth = ConeDownModel.scoopPointsWide;
            imageHeight = ConeDownModel.scoopPointsHigh;
            break;
          case 3:
            imageWidth = ConeDownModel.conePointsWide;
            imageHeight = ConeDownModel.conePointsHigh;
            break;
          case 4:  // Scoop + cone
            imageWidth = Math.max(ConeDownModel.conePointsWide, ConeDownModel.scoopPointsWide);
            imageHeight = ConeDownModel.scoopPointsHigh + ConeDownModel.conePointsHigh;
            break;
          case 5:  // Dancefloor + scoop
            imageWidth = ConeDownModel.scoopPointsWide;
            imageHeight = ConeDownModel.scoopPointsHigh + ConeDownModel.dancePointsHigh;
            break;
        }
        // Reload the GIF with new dimensions.
        loadGif(gifKnob.getString());
      }
    });
  }

  protected void renderToPoints() {
    int xOffset = (int) xOff.getValue();
    int yOffset = (int) yOff.getValue();
    // Constrain the values to the right edge and bottom.
    if (xOffset >= images[(int)currentFrame].width - 46)
      xOffset = images[(int)currentFrame].width - 47;
    if (yOffset >= images[(int)currentFrame].height - 46)
      yOffset = images[(int)currentFrame].height - 47;
    RenderImageUtil.sampleRenderTarget(renderTarget.getValuei(), images[(int)currentFrame], colors, xOffset ,yOffset);
  }
}

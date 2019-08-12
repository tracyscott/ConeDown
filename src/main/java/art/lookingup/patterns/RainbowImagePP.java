package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;

@LXCategory(LXCategory.FORM)
public class RainbowImagePP extends RainbowImageBase {
  public RainbowImagePP(LX lx) {
    super(lx, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH,
        "imgpp/",
        "oregon.jpg",
        false,
        false);
  }

  protected void renderToPoints() {
      RenderImageUtil.imageToPointsPixelPerfect(ConeDown.getProjection(ConeDown.DEFAULT_SUPER_SAMPLING), image, colors);
  }
}

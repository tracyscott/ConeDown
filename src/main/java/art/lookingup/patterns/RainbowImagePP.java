package art.lookingup.patterns;

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
    RenderImageUtil.imageToPointsPixelPerfect(image, colors);
  }
}

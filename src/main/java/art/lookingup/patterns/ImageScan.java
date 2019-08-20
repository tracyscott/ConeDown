package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.ConeDown;
import heronarts.lx.LX;

public class ImageScan extends RainbowImageBase {
  protected int xOffset = 0;
  protected int yOffset = 0;

  protected boolean movingVertically = false;
  protected boolean movingForwards = true;
  protected int verticalMovement = 0;

  public ImageScan(LX lx) {
    super(lx, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH,
        "imgpp/",
        "oregon.jpg",
        false,
        true);
  }

  protected void renderToPoints() {
      RenderImageUtil.imageToPointsPixelPerfect(ConeDown.getProjection(ConeDown.DEFAULT_SUPER_SAMPLING), image, colors, xOffset, yOffset);
    if (!movingVertically) {
      if (movingForwards) xOffset++;
      else xOffset--;
      if (xOffset >= tileImage.width - imageWidth) {
        movingForwards = false;
        movingVertically = true;
      } else if (xOffset < 0) {
        movingForwards = true;
        movingVertically = true;
      }
    } else {
      yOffset++;
      verticalMovement++;
      if (verticalMovement > imageHeight) {
        verticalMovement = 0;
        movingVertically = false;
      }
      if (yOffset + imageHeight >= tileImage.height) {
        yOffset = 0;
        xOffset = 0;
      }
    }
  }
}

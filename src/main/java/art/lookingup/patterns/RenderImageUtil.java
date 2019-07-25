package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.util.logging.Logger;

import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Utility class for rendering images.  Implements the mapping of
 * the polar coordinate generated points into an image, including
 * the option of antialiasing.
 */
public class RenderImageUtil {
  private static final Logger logger = Logger.getLogger(RenderImageUtil.class.getName());

  /**
   * Renders the Rainbow Flag into a PImage.  This is used in a multiply mode by the AnimatedTextPP
   * pattern to avoid having to multiply against another channel.
   */
  public static PGraphics rainbowFlagAsPGraphics(int width, int height) {
    PGraphics rainbow = ConeDown.pApplet.createGraphics(width, height);
    rainbow.noSmooth();
    rainbow.beginDraw();
    rainbow.background(0, 0);
    rainbow.noStroke();
    // Draw flag rectangles
    rainbow.fill(228, 3, 3);
    rainbow.rect(0, 0, width, height / 6);
    rainbow.fill(225, 140, 0);
    rainbow.rect(0, height / 6, width, 2 * height / 6);
    rainbow.fill(255, 237, 0);
    rainbow.rect(0, 2 * height / 6, width, 3 * height / 6);
    rainbow.fill(0, 128, 38);
    rainbow.rect(0, 3 * height / 6, width, 4 * height / 6);
    rainbow.fill(0, 77, 255);
    rainbow.rect(0, 4 * height / 6, width, 5 * height / 6);
    rainbow.fill(177, 7, 135);
    rainbow.rect(0, 5 * height / 6, width, 6 * height / 6);
    rainbow.endDraw();
    return rainbow;
  }

  public static void imageToPointsPixelPerfect(LXModel model, PImage image, int[] colors) {
    imageToPointsPixelPerfect(model, image, colors, 0, 0);
  }

  /**
   * Render an image to the installation pixel-perfect.  This effectively treats the
   * installation as a 112x58 image.
   * TODO(tracy): We need to anti-alias in the X dimension because there are fewer LEDs
   * per row near the top where the diameter is smaller.
   * <p>
   * Since we constructed our LXModel from points parsed from the cnc .svg file, our
   * points are in column-normal form so we need to transpose x,y.  Also, There are
   * holes in the signs so the count(pixels) < ConeDownModel.POINTS_WIDE * ConeDownModel.POINTS_HIGH</p>
   * <p>
   * Note that point (0,0) is at the bottom left in {@code colors}.</p>
   */
  public static void imageToPointsPixelPerfectOld(LXModel lxModel, PImage image, int[] colors, int xOffset, int yOffset) {
    // (0, 0) is at the bottom left in the colors array

    image.loadPixels();
    for (int cindex = 0; cindex < colors.length; cindex++) {
      CXPoint p = (CXPoint) lxModel.points[cindex];
      int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder(p, ConeDownModel.POINTS_WIDE,
          ConeDownModel.POINTS_HIGH,0); //ConeDownModel.pointToImageCoordinates(p);
      colors[cindex] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
    }

  }

  public static void imageToPointsPixelPerfect(LXModel lxModel, PImage image, int[] colors, int xOffset, int yOffset) {
    image.loadPixels();
    for (LXPoint p : ConeDownModel.conePoints) {
      int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder((CXPoint)p, ConeDownModel.POINTS_WIDE,
          ConeDownModel.POINTS_HIGH, 0);
      colors[p.index] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
    }
    for (LXPoint p : ConeDownModel.scoopPoints) {
      int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder((CXPoint)p, ConeDownModel.POINTS_WIDE,
          ConeDownModel.POINTS_HIGH, 0);
      colors[p.index] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
    }
    for (LXPoint p : ConeDownModel.dancePoints) {
      int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder((CXPoint)p, ConeDownModel.POINTS_WIDE,
          ConeDownModel.POINTS_HIGH, 0);
      colors[p.index] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
    }
  }

  /**
   * Apply our render target image to the appropriate set of points based on which target
   * we have selected.  0 = default, should just use existing method above for now.
   * 1 = dance floor
   * 2 = scoop
   * 3 = cone
   * @param renderTarget
   * @param image
   * @param colors
   */
  public static void sampleRenderTarget(int renderTarget, PImage image, int[] colors, int xOffset, int yOffset) {
    // Dance floor is easy, since there is no texture coordinate offsets.
    image.loadPixels();
    int yTexCoordOffset = 0;
    int pointsWide = ConeDownModel.POINTS_WIDE;
    int pointsHigh = ConeDownModel.POINTS_HIGH;
    switch (renderTarget) {
      case 0:
        yTexCoordOffset = 0;
        pointsWide = ConeDownModel.POINTS_WIDE;
        pointsHigh = ConeDownModel.POINTS_HIGH;
        break;
      case 1:
        yTexCoordOffset = 0;
        pointsWide = ConeDownModel.dancePointsWide;
        pointsHigh = ConeDownModel.dancePointsHigh;
        break;
      case 2:
        yTexCoordOffset = ConeDownModel.dancePointsHigh;
        pointsWide = ConeDownModel.scoopPointsWide;
        pointsHigh = ConeDownModel.scoopPointsHigh;
        break;
      case 3:
        yTexCoordOffset = ConeDownModel.dancePointsHigh + ConeDownModel.scoopPointsHigh;
        pointsWide = ConeDownModel.conePointsWide;
        pointsHigh = ConeDownModel.conePointsHigh;
        break;
    }

    for (LXPoint p : ConeDownModel.conePoints) {
      if (renderTarget == 0 || renderTarget == 3) {
        int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder((CXPoint) p, pointsWide, pointsHigh, yTexCoordOffset);
        colors[p.index] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
      } else {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }

    for (LXPoint p : ConeDownModel.scoopPoints) {
      if (renderTarget == 0 || renderTarget == 2) {
        int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder((CXPoint) p, pointsWide, pointsHigh, yTexCoordOffset);
        colors[p.index] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
      } else {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }

    for (LXPoint p : ConeDownModel.dancePoints) {
      if (renderTarget == 0 || renderTarget == 1) {
        int[] imgCoords = ConeDownModel.pointToImgCoordsCylinder((CXPoint) p, pointsWide, pointsHigh, yTexCoordOffset);
        colors[p.index] = image.get(imgCoords[0] + xOffset, imgCoords[1] + yOffset);
      } else {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }
  }
}

package art.lookingup.patterns;

import art.lookingup.CXPoint;
import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Projection;

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

  public static void imageToPointsPixelPerfect(Projection proj, PImage image, int[] colors) {
      sampleRenderTarget(proj, 0, image, colors, 0, 0);
  }

  /**
   * Render an image to the installation pixel-perfect.  This effectively treats the
   * installation as a 112x87 image.
   * <p>
   * Since we constructed our LXModel from points parsed from the cnc .svg file, our
   * points are in column-normal form so we need to transpose x,y.  Also, There are
   * holes in the signs so the count(pixels) < ConeDownModel.POINTS_WIDE * ConeDownModel.POINTS_HIGH</p>
   * <p>
   * Note that point (0,0) is at the bottom left in {@code colors}.</p>
   */
  public static void imageToPointsPixelPerfect(Projection proj, PImage image, int[] colors, int xOffset, int yOffset) {
      sampleRenderTarget(proj, 0, image, colors, xOffset, yOffset);
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
    public static void sampleRenderTarget(Projection proj, int renderTarget, PImage image, int[] colors, int xOffset, int yOffset) {
    Projection projection = proj;

    // Dance floor is easy, since there is no texture coordinate offsets.
    image.loadPixels();
    int yTexCoordOffset = 0;
    int pointsWide = ConeDownModel.POINTS_WIDE;
    int pointsHigh = ConeDownModel.POINTS_HIGH;
    switch (renderTarget) {
    case 0:  // whole thing w/ dance floor centered (missing pixels bottom left/right)
        yTexCoordOffset = 0;
        pointsWide = ConeDownModel.POINTS_WIDE;
        pointsHigh = ConeDownModel.POINTS_HIGH;
        break;
    case 1: // dance floor
        yTexCoordOffset = ConeDownModel.scoopPointsHigh + ConeDownModel.conePointsHigh;
        pointsWide = ConeDownModel.dancePointsWide;
        pointsHigh = ConeDownModel.dancePointsHigh;
        break;
    case 2: // scoop
        yTexCoordOffset = ConeDownModel.conePointsHigh;
        pointsWide = ConeDownModel.scoopPointsWide;
        pointsHigh = ConeDownModel.scoopPointsHigh;
        break;
    case 3: // cone
        yTexCoordOffset = 0;
        pointsWide = ConeDownModel.conePointsWide;
        pointsHigh = ConeDownModel.conePointsHigh;
        break;
    case 4:  // scoop + cone
        yTexCoordOffset = 0;
        pointsWide = ConeDownModel.scoopPointsWide;
        pointsHigh = ConeDownModel.scoopPointsHigh + ConeDownModel.conePointsHigh;
        break;
    case 5:  // dance + scoop
        yTexCoordOffset = ConeDownModel.conePointsHigh;
        pointsWide = ConeDownModel.scoopPointsWide;
        pointsHigh = ConeDownModel.dancePointsHigh + ConeDownModel.scoopPointsHigh;
        break;
    }

    // Note: Assume that 'xOffset' and 'yOffset' are pre-scaled b/c
    // the pattern is dealing in super-sampled coordinates.  The three
    // coordinates set in the switch above are not scaled yet.
    pointsWide *= projection.factor();
    pointsHigh *= projection.factor();
    yTexCoordOffset *= projection.factor();

    for (LXPoint p : ConeDownModel.conePoints) {
      if (renderTarget == 0 || renderTarget == 3 || renderTarget == 4) {
	colors[p.index] = getRenderColor((CXPoint) p, projection, image,
					 pointsWide, pointsHigh, yTexCoordOffset,
					 xOffset, yOffset);	  
      } else {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }

    for (LXPoint p : ConeDownModel.scoopPoints) {
      if (renderTarget == 0 || renderTarget == 2 || renderTarget == 5 || renderTarget == 4) {
	colors[p.index] = getRenderColor((CXPoint) p, projection, image,
					 pointsWide, pointsHigh, yTexCoordOffset,
					 xOffset, yOffset);
      } else {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }

    for (LXPoint p : ConeDownModel.dancePoints) {
      if (renderTarget == 0 || renderTarget == 1 || renderTarget == 5) {
	colors[p.index] = getRenderColor((CXPoint) p, projection, image,
					 pointsWide, pointsHigh, yTexCoordOffset,
					 xOffset, yOffset);
      } else {
        colors[p.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }
  }

  // `cxp` is the physical point being rendered,
  // `image` is the source data,
  // `pointsHigh` are the height of the logical image we are mapping from,
  // `xOffset` and `yOffset` are additional offsets into the image being rendered.
  static int getRenderColor(CXPoint cxp,
			    Projection projection,
			    PImage image,
			    int pointsWide,
			    int pointsHigh,
			    int yTexCoordOffset,
			    int xOffset,
			    int yOffset) {
      // The texture begins at yTexCoordOffset.  Subtract by combining w/ yOffset.
      yOffset -= yTexCoordOffset;

      // When rendering only the dance floor, these are different.  In
      // that case, apply an offset.
      int fullWidth = projection.factor() * ConeDownModel.POINTS_WIDE;
      if (pointsWide != fullWidth) {
	  xOffset -= (fullWidth - pointsWide) / 2;
      }
      return projection.computePoint(cxp, image, xOffset, yOffset);
    }
}

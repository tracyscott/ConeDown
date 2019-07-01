package art.lookingup;

import heronarts.lx.model.LXPoint;

/**
 * Custom LXPoint class for ConeDown.  We want to track which panel we are on and our X,Y position on the panel.
 * This will allow us to texture map our point from our render buffer image.
 */
public class CXPoint extends LXPoint {
  ConeDownModel.Panel panel;
  int xCoord;
  int yCoord;

  public CXPoint(ConeDownModel.Panel panel, double x, double y, double z, int xCoord, int yCoord) {
    super(x, y, z);
    this.panel = panel;
    this.xCoord = xCoord;
    this.yCoord = yCoord;
  }
}

package art.lookingup;

import heronarts.lx.model.LXPoint;

import java.util.List;

/**
 * Custom LXPoint class for ConeDown.  We want to track which panel we are on and our X,Y position on the panel.
 * This will allow us to texture map our point from our render buffer image.
 */
public class CXPoint extends LXPoint implements Comparable<CXPoint> {
  public Panel panel;
  public int xCoord;
  public int yCoord;
  public float angle;
  public float radius;
  public float panelLocalX;
  public float panelLocalY;
  public float panelLocalXUnscaled;
  public float panelLocalYUnscaled;
  public float horizontalSpacing;
  public float intensityCompensation;
  public float maxHorizontalSpacing = Float.MIN_VALUE;

  public static final float EPSILON = 4f;

  public CXPoint(Panel panel, double x, double y, double z, int xCoord, int yCoord, float angle, float radius) {
    super(x, y, z);
    this.panel = panel;
    this.xCoord = xCoord;
    this.yCoord = yCoord;
    this.angle = angle;
    this.radius = radius;
  }

  public void storePanelLocalXY() {
    this.panelLocalX = x;
    this.panelLocalY = y;
  }

  public void storePanelLocalXYUnscaled() {
    this.panelLocalXUnscaled = x;
    this.panelLocalYUnscaled = y;
  }

  public void computeHorizontalSpacing() {
    CXPoint pt = findPointLeft(panel.points);
    if (pt != null) {
      horizontalSpacing = Math.abs(panelLocalX - pt.panelLocalX);
    } else {
      pt = findPointRight(panel.points);
      horizontalSpacing = Math.abs(panelLocalX - pt.panelLocalX);
    }
    if (horizontalSpacing > maxHorizontalSpacing) {
      maxHorizontalSpacing = horizontalSpacing;
    }
  }

  /* Note this assumes a flat 2D plane of points in XY plane */
  public void rotX(float degrees) {
    float oldY = y;
    y = y * (float)Math.cos(Math.toRadians(degrees));
    z = - oldY * (float)Math.sin(Math.toRadians(degrees));
  }

  public void rotY(float degrees) {
    float oldX = x;
    x = (float) (x * Math.cos(Math.toRadians(degrees))) - z * (float)Math.sin(Math.toRadians(degrees));
    z = (float) (oldX * Math.sin(Math.toRadians(degrees))) + z * (float)Math.cos(Math.toRadians(degrees));
  }

  public void rot2D(float degrees) {
    float oldX = x;
    x = (float) (x * Math.cos(Math.toRadians(degrees))) - (float)(y * Math.sin(Math.toRadians(degrees)));
    y = (float) (oldX * Math.sin(Math.toRadians(degrees))) + (float)(y * Math.cos(Math.toRadians(degrees)));
  }

  /*
  @Override public int compareTo(final CXPoint pt2) {
    float dy;
    float dx = x - pt2.x;
    if (Math.abs(dx) < EPSILON) {
      dy = y - pt2.y;
      if (Math.abs(dy) < EPSILON) {
        return 0;
      } else if (dy < 0) {
        return -1;
      } else {
        return 1;
      }
    } else if (dx < 0) {
      return -1;
    } else {
      return 1;
    }
  }
  */

  @Override public int compareTo(final CXPoint pt2) {
    if (xCoord == pt2.xCoord) {
      if (yCoord < pt2.yCoord) {
        return -1;
      } else if (yCoord > pt2.yCoord)
        return 1;
      else
        return 0;
    } else if (xCoord > pt2.xCoord) {
      return 1;
    } else {
      return -1;
    }
  }

  // was 0.2f * inchesPerMeter
  static public float rowColDistThresh = 2.0f / ConeDownModel.inchesPerMeter;

  public float distanceSquared(CXPoint p) {
    return (panelLocalX-p.panelLocalX)*(panelLocalX-p.panelLocalX)
      + (panelLocalY-p.panelLocalY)*(panelLocalY-p.panelLocalY);
  }

  public float distance(CXPoint p) {
    return (float)Math.sqrt(distanceSquared(p));
  }

  public float verticalDistance(CXPoint p) {
    return Math.abs(panelLocalY - p.panelLocalY);
  }

  public float horizontalDistance(CXPoint p) {
    return Math.abs(panelLocalX - p.panelLocalX);
  }

  CXPoint findPointAbove(List<CXPoint> points) {
    int closestPointIndex = -1;
    float closestDistance = 10000.0f;
    int i = 0;
    for (CXPoint point : points) {
      if (point.index == index) {
        i++;
        continue;
      }
      float dist = verticalDistance(point);
      // Must be about the same X.
      if (dist < closestDistance && point.panelLocalY - panelLocalY > 0
          && Math.abs(point.panelLocalX - panelLocalX) < rowColDistThresh ) {
        closestDistance = dist;
        closestPointIndex = i;
      }
      i++;
    }
    if (closestPointIndex == -1) return null;
    return points.get(closestPointIndex);
  }

  CXPoint findPointBelow(List<CXPoint> points) {
    int closestPointIndex = -1;
    float closestDistance = 10000.0f;
    int i = 0;
    for (CXPoint point : points) {
      if (point.index == index) {
        i++;
        continue;
      }
      float dist = verticalDistance(point);
      // Must be about the same X.
      if (dist < closestDistance && point.panelLocalY - panelLocalY < 0
          && Math.abs(point.panelLocalX - panelLocalX) < rowColDistThresh) {
        closestDistance = dist;
        closestPointIndex = i;
      }
      i++;
    }
    if (closestPointIndex == -1) return null;
    return points.get(closestPointIndex);
  }

  CXPoint findPointLeft(List<CXPoint> points) {
    int closestPointIndex = -1;
    float closestDistance = 10000.0f;
    int i = 0;
    for (CXPoint point : points) {
      if (point.index == index) {
        i++;
        continue;
      }
      float dist = horizontalDistance(point);
      // Must be about the same X.
      if (dist < closestDistance && point.panelLocalX - panelLocalX < 0
          && Math.abs(point.panelLocalY - panelLocalY) < rowColDistThresh) {
        closestDistance = dist;
        closestPointIndex = i;
      }
      i++;
    }
    if (closestPointIndex == -1) return null;
    return points.get(closestPointIndex);
  }

  CXPoint findPointRight(List<CXPoint> points) {
    int closestPointIndex = -1;
    float closestDistance = 10000.0f;
    int i = 0;
    for (CXPoint point : points) {
      if (point.index == index) {
        i++;
        continue;
      }
      float dist = horizontalDistance(point);
      // Must be about the same X.
      if (dist < closestDistance && point.panelLocalX - panelLocalX > 0
          && Math.abs(point.panelLocalY - panelLocalY) < rowColDistThresh ) {
        closestDistance = dist;
        closestPointIndex = i;
      }
      i++;
    }
    if (closestPointIndex == -1) return null;
    return points.get(closestPointIndex);
  }

  public static CXPoint getCXPointAtTexCoord(List<CXPoint> points, int x, int y) {
    for (CXPoint p : points) {
      if (p.xCoord == x && p.yCoord == y)
        return p;
    }
    return null;
  }

}

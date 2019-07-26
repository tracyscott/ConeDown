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

  public static final float EPSILON = 4f;

  public CXPoint(Panel panel, double x, double y, double z, int xCoord, int yCoord, float angle, float radius) {
    super(x, y, z);
    this.panel = panel;
    this.xCoord = xCoord;
    this.yCoord = yCoord;
    this.angle = angle;
    this.radius = radius;
  }

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
  static public float rowColDistThresh = 3.0f;

  public float distanceSquaredXY(CXPoint p) {
    return (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y);
  }

  public float distanceSquared(CXPoint p) { return (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y); }

  public float distance(CXPoint p) {
    return (float)Math.sqrt(distanceSquared(p));
  }

  public float verticalDistance(CXPoint p) {
    return Math.abs(y - p.y);
  }

  public float horizontalDistance(CXPoint p) {
    return Math.abs(x - p.x);
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
      if (dist < closestDistance && point.y - y > 0 && Math.abs(point.x - x) < rowColDistThresh ) {
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
      if (dist < closestDistance && point.y - y < 0 && Math.abs(point.x - x) < rowColDistThresh) {
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
      if (dist < closestDistance && point.x - x < 0 && Math.abs(point.y - y) < rowColDistThresh) {
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
      if (dist < closestDistance && point.x - x > 0 && Math.abs(point.y - y) < rowColDistThresh ) {
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

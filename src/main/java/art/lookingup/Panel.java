package art.lookingup;

import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.util.*;
import java.util.logging.Logger;

public class Panel {
  private static final Logger logger = Logger.getLogger(Panel.class.getName());

  public static float CNC_SCALE = 1/ConeDownModel.inchesPerMeter;

  public enum PanelRegion {
    SCOOP,
    CONE,
    DANCEFLOOR,
  }

  public enum PanelType {
    A1,
    A2,
    B1,
    B2,
    C,
    D,
    E1,
    E2,
    F,
    G,
    H,
    I,
  }

  // Per panel-type panel margins used to model inter-panel spacing when creating 3D model.  These are
  // specified here in Inches.
  final static public float[] panelLeftMargins = {
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f
  };

  final static public float[] panelBottomMargins = {
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f,
      6f
  };


  final static public String[] panelFilenames = {
      "A",
      "A",
      "B",
      "B",
      "C",
      "D",
      "E",
      "E",
      "F",
      "G",
      "H",
      "I",
  };

  final static public String[] panelTypeNames = {
      "A1",
      "A2",
      "B1",
      "B2",
      "C",
      "D",
      "E1",
      "E2",
      "F",
      "G",
      "H",
      "I",
  };

  final static public int[] numPanelsAround =
  {
      16,
      16,
      16,
      16,
      8,
      8,
      16,
      16,
      16,
      16,
      16,
      16,
  };

  final static public float[] faceSlope = {
      -7f,
      -7f,
      -7f,
      -7f,
      0f,
      -35f,
      0f,
      0f,
      -45f,
      -10f,
      10f,
      30f,
  };

  public PanelRegion panelRegion;
  public PanelType panelType;
  public int panelLayoutNum;
  public String dxfFilename;
  public float topWidth;
  public float bottomWidth;
  public float height;
  public float pitch;  // spacing of pixels in inches.
  public float xPos;
  public float yPos;
  public float zPos;
  public int panelNum;
  public int yCoordOffset;
  public int danceXPanel;
  public int danceYPanel;
  public float radius;
  public boolean scoop;
  // Keep track of how many points exist on this panel.  We need this to properly texture
  // map our points from a render buffer image.
  public int pointsWide;
  public int pointsHigh;
  public boolean mirrored = false;

  public List<CXPoint> points;
  public List<CXPoint> pointsWireOrder;

  public List<Float[]> panelBoundaryPts = new ArrayList<Float[]>(4);
  public BPoint[] bPoints = new BPoint[4];

  /**
   * Create a panel based on reading a panel definition file.
   */
  public Panel(PanelType panelType, float yPos, int panelNum, int yCoordOffset, float radius) {
    this.panelType = panelType;
    this.xPos = xPos;
    this.yPos = yPos;
    this.zPos = zPos;
    this.panelNum = panelNum;
    this.yCoordOffset = yCoordOffset;

    // NOTE(tracy): A hack to increase all radii to account for panel margins (inter panel spacing).  The current
    // radii were just hand tuned visually.  We need the radii to a bottom corner for each panel layer.
    // This number gives some visual gaps so I will leave it as an approximation.
    radius = radius + CNC_SCALE * 20.0f;
    this.radius = radius;


    boolean mirror = false;
    boolean flip = false;

    if (panelType == PanelType.A2 || panelType == PanelType.B2 || panelType == PanelType.E2)
      mirror = true;

    if (panelType == PanelType.I && panelNum > 8)
      mirror = true;

    if (panelType == PanelType.H && panelNum == 0 || (panelType == PanelType.H && panelNum >= 9 && panelNum <= 10))
      mirror = true;

    if (panelType == panelType.H || panelType == panelType.G || panelType == PanelType.F || panelType == PanelType.I) {
      scoop = true;
      panelRegion = PanelRegion.SCOOP;
    } else {
      scoop = false;
      panelRegion = PanelRegion.CONE;
    }

    // We no longer flip a G panel to get an H panel.  We have explicit H panels.
    if (panelType == PanelType.H) {
      // flip = true;
    }
    String filenameBase = panelFilenames[panelType.ordinal()];

    if (panelType == PanelType.H) {
      if (panelNum == 0 || panelNum == 15)
        filenameBase = filenameBase + "_door";
      if (panelNum == 5 || panelNum == 10)
        filenameBase = filenameBase + "_milli";
      if (panelNum == 6 || panelNum == 9)
        filenameBase = filenameBase + "_micro";
      if (panelNum == 7 || panelNum == 8)
        filenameBase = filenameBase + "_nano";
    }

    if (panelType == PanelType.I) {
      // Wait to mess with door.
      if (panelNum == 0 || panelNum == 15)
        filenameBase = filenameBase + "_door";
      if (panelNum == 2 || panelNum == 13)
        filenameBase = filenameBase + "_milli";
      if (panelNum == 3 || panelNum == 12)
        filenameBase = filenameBase + "_micro";
      if (panelNum == 4 || panelNum == 11)
        filenameBase = filenameBase + "_nano";
    }
    String filename = filenameBase + "_LED.dxf";
    dxfFilename = filename;
    // Store this for later debugging.
    mirrored = mirror;
    points = loadDXFPanel(dxfFilename, mirror, flip);

    for (CXPoint p : points) {
      // Keep so we have some values referenced in the units of the DXF file.
      p.storePanelLocalXYUnscaled();
      // Convert to meters.
      p.x *= CNC_SCALE;  // 1/inchesPerMeter for these files in inches.
      p.y *= CNC_SCALE;
      p.storePanelLocalXY();  // Store the XY plane coords before we convert to world space.
    }

    if (panelType == PanelType.H) {
      if (panelNum == 15) {
        textureMapPointsLeftRight();
      } else if (panelNum == 0) {
        textureMapPointsRightLeft();
      } else if (panelNum == 6 || panelNum == 7 || panelNum == 8 || panelNum == 10) {
        textureMapPointsTopBottom();
      } else if (panelNum == 9) {
        textureMapPoints(0, 2);
      } else {
        textureMapPoints();
      }
    } else if (panelType == PanelType.I) {
      if (panelNum == 0) {
        textureMapPoints(getExpectedPointsWide() - 3, 0);
      } else if (panelNum == 2 || panelNum == 13) {
        textureMapPoints(0, 1);
      } else if (panelNum == 3 || panelNum == 12) {
        textureMapPoints(0, 2);
      } else if (panelNum == 11) {
        textureMapPointsTopLeftRight(0);
      } else if (panelNum == 4) {
        textureMapPointsTopRightLeft(getExpectedPointsWide() - 1);
      } else {
        textureMapPoints();
      }
    } else
      textureMapPoints();

    Collections.sort(points);

    float angleIncr = 360f / numFullPanelsAround();
    float panelAngle = faceNum() * angleIncr;

    // panelNum * bottomWidth; //
    double panelXStart =  radius * Math.cos(Math.toRadians(panelAngle));
    double panelZStart = radius * Math.sin(Math.toRadians(panelAngle));

    // Need the panel angle of the endpoint
    double panelXFinish = radius * Math.cos(Math.toRadians(panelAngle + angleIncr));
    double panelZFinish = radius * Math.sin(Math.toRadians(panelAngle + angleIncr));

    // If we are a half-panel type, then we need to adjust our panel start position.
    // For the second half, the panel start is halfway between the panel start and
    // finish for our entire face.
    if (isHalfPanel() && !isFirstHalfPanel()) {
      panelXStart = (panelXFinish - panelXStart) * 0.5f + panelXStart;
      panelZStart = (panelZFinish - panelZStart) * 0.5f + panelZStart;
    }

    // logger.info("yCoordOffset =" + yCoordOffset + " pointsHigh=" + pointsHigh);
    for (CXPoint p : points) {
      // TODO(Tracy): Account for inter-panel spacing by incrementing x,y positions here based on
      // per panel-type left and bottom margins.  We are already converted to meters at this point
      // so margins should be multiplied by CNC_SCALE
      // TODO(tracy): The various radii have to fixed for this work.
      p.x = p.x + CNC_SCALE * panelLeftMargins[panelType.ordinal()];
      p.y = p.y + CNC_SCALE * panelBottomMargins[panelType.ordinal()];
      float angle =  90f + 45f/2f + (angleIncr * faceNum());
      if (panelType == PanelType.I) {
        angle = 90f + (360f / numFacesAround()) / 2f + (angleIncr * faceNum());
      } else if (scoop) {
        // 90f + (360 / numFaces) / 2f  was 45/4f
        angle = 90f + (360f / numFacesAround()) / 2f + (angleIncr * faceNum());
      }
      p.rotX(faceSlope[panelType.ordinal()]);
      p.rotY(angle);
      // Layout panels horizontally for debugging.
      //p.x = p.x  + panelNum * bottomWidth;
      p.y += yPos;
      p.x += panelXStart;
      p.z += panelZStart;
    }
  }

  public void increaseYPos(float y) {
    for (CXPoint p : points) {
      p.y += y;
    }
  }

  public void logBoundary() {
    logger.info("panel type & #: " + panelTypeNames[panelType.ordinal()] + " " + panelNum);
    logger.info("bottom left : " + bPoints[0].x + "," + bPoints[0].y);
    logger.info("bottom right: " + bPoints[1].x + "," + bPoints[1].y);
    logger.info("top right   : " + bPoints[2].x + "," + bPoints[2].y);
    logger.info("top left    : " + bPoints[3].x + "," + bPoints[3].y);
  }

  /**
   * Picks a point and navigates to the bottom left corner.
   * @return
   */
  public CXPoint findBottomLeft() {
    boolean foundOrigin = false;
    boolean movingLeft = true;
    CXPoint nextPoint = null;
    CXPoint prevPoint = points.get(0);
    while (!foundOrigin) {
      if (movingLeft) nextPoint = prevPoint.findPointLeft(points);
      else nextPoint = prevPoint.findPointBelow(points);
      if (nextPoint != null) {
        prevPoint = nextPoint;
      } else {
        if (nextPoint == null && !movingLeft) {
          foundOrigin = true;
        } else {
          movingLeft = false;
        }
      }
    }
    return prevPoint;
  }

  /**
   * Picks a point and navigates to the bottom left corner.
   * @return
   */
  public CXPoint findTopLeft() {
    boolean foundOrigin = false;
    boolean movingUp = true;
    CXPoint nextPoint = null;
    CXPoint prevPoint = points.get(0);
    while (!foundOrigin) {
      if (movingUp) nextPoint = prevPoint.findPointAbove(points);
      else nextPoint = prevPoint.findPointLeft(points);
      if (nextPoint != null) {
        prevPoint = nextPoint;
      } else {
        if (nextPoint == null && !movingUp) {
          foundOrigin = true;
        } else {
          movingUp = false;
        }
      }
    }
    return prevPoint;
  }

  /**
   * Picks a point and navigates to the bottom left corner.
   * @return
   */
  public CXPoint findTopRight() {
    boolean foundOrigin = false;
    boolean movingUp = true;
    CXPoint nextPoint = null;
    CXPoint prevPoint = points.get(0);
    while (!foundOrigin) {
      if (movingUp) nextPoint = prevPoint.findPointAbove(points);
      else nextPoint = prevPoint.findPointRight(points);
      if (nextPoint != null) {
        prevPoint = nextPoint;
      } else {
        if (nextPoint == null && !movingUp) {
          foundOrigin = true;
        } else {
          movingUp = false;
        }
      }
    }
    return prevPoint;
  }


  /**
   * For H0 doors, we neeed to find the bottom right point and work from there.
   * @return
   */
  public CXPoint findBottomRight() {
    boolean foundOrigin = false;
    boolean movingLeft = true;
    CXPoint nextPoint = null;
    CXPoint prevPoint = points.get(0);
    while (!foundOrigin) {
      if (movingLeft) nextPoint = prevPoint.findPointRight(points);
      else nextPoint = prevPoint.findPointBelow(points);
      if (nextPoint != null) {
        prevPoint = nextPoint;
      } else {
        if (nextPoint == null && !movingLeft) {
          foundOrigin = true;
        } else {
          movingLeft = false;
        }
      }
    }
    return prevPoint;
  }

  public boolean isDoor() {
    if (panelType == PanelType.H || panelType == PanelType.I) {
      if (panelNum == 0 || panelNum == 15)
        return true;
    }
    return false;
  }

  /**
   * H and I panels can be intercepted by the ground which reduces their
   * computed pointsHigh.  For correct texture mapping we need to account
   * for the missing points.
   * @return
   */
  public int getExpectedPointsHigh() {
    if (panelType == PanelType.H) {
      return 7;
    }
    if (panelType == PanelType.I) {
      return 6;
    }
    return pointsHigh;
  }

  /**
   * H and I panels have door panels that are missing points where the doors
   * are located.  We need to account for the missing points when generating
   * our texture coordinates.
   * @return
   */
  public int getExpectedPointsWide() {
    if (panelType == PanelType.H) {
      return 7;
    }
    if (panelType == PanelType.I) {
      return 6;
    }
    return pointsWide;
  }

  /**
   * Texture mapping process that starts at bottom right, moves left, and then moves up,
   * etc.
   * NOTE(tracy): Requires the hard-code height to be specified in getExpectedPointsWide().
   */
  public void textureMapPointsRightLeft() {
    CXPoint origin = findBottomRight();
    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    int xCoord = getExpectedPointsWide() - 1;
    int yCoord = 0;
    origin.xCoord = xCoord;
    origin.yCoord = yCoord;
    boolean textureCoordsDone = false;
    boolean movingRight = false;
    int maxXCoord = 0;
    int maxYCoord = 0;
    while (pointsVisited < points.size() && !textureCoordsDone) {
      if (movingRight) nextPoint = prevPoint.findPointRight(points);
      else nextPoint = prevPoint.findPointLeft(points);
      if (nextPoint != null && movingRight) {
        xCoord += 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingRight) {
        xCoord -= 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else {
        movingRight = !movingRight;
        nextPoint = prevPoint.findPointAbove(points);
        if (nextPoint == null) {
          // we are done
          textureCoordsDone = true;
        } else {
          yCoord += 1;
          nextPoint.yCoord = yCoord;
          nextPoint.xCoord = xCoord;
          prevPoint = nextPoint;
        }
      }
      if (xCoord > maxXCoord)
        maxXCoord = xCoord;
      if (yCoord > maxYCoord)
        maxYCoord = yCoord;
    }

    pointsWide = maxXCoord + 1;
    pointsHigh = maxYCoord + 1;
  }

  /**
   * Texture mapping process that starts in the top left corner and moves to the right and then
   * down and then back left, etc.
   * NOTE(tracy): Requires the hard-code height to be specified in getExpectedPointsHigh().
   * @param xStart For partial panels, allow the starting x texture coord to be specified.
   */
  public void textureMapPointsTopLeftRight(int xStart) {
    CXPoint origin = findTopLeft();
    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    int xCoord = xStart;
    int yCoord = getExpectedPointsHigh() - 1;
    origin.xCoord = xCoord;
    origin.yCoord = yCoord;
    boolean textureCoordsDone = false;
    boolean movingRight = true;
    int maxXCoord = 0;
    int maxYCoord = 0;
    while (pointsVisited < points.size() && !textureCoordsDone) {
      if (movingRight) nextPoint = prevPoint.findPointRight(points);
      else nextPoint = prevPoint.findPointLeft(points);
      if (nextPoint != null && movingRight) {
        xCoord += 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingRight) {
        xCoord -= 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else {
        movingRight = !movingRight;
        nextPoint = prevPoint.findPointBelow(points);
        if (nextPoint == null) {
          // we are done
          textureCoordsDone = true;
        } else {
          yCoord -= 1;
          nextPoint.yCoord = yCoord;
          nextPoint.xCoord = xCoord;
          prevPoint = nextPoint;
        }
      }
      if (xCoord > maxXCoord)
        maxXCoord = xCoord;
      if (yCoord > maxYCoord)
        maxYCoord = yCoord;
    }

    pointsWide = maxXCoord + 1;
    pointsHigh = maxYCoord + 1;
  }

  /**
   * Texture mapping process that starts at the top right and starts by moving left
   * and then down and then back right, etc.
   * NOTE(tracy): Requires the hard-code height to be specified in getExpectedPointsHigh().
   * @param xStart For partial panels, allow the starting X texture coord to be specified.
   */
  public void textureMapPointsTopRightLeft(int xStart) {
    CXPoint origin = findTopRight();
    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    int xCoord = xStart;
    int yCoord = getExpectedPointsHigh() - 1;
    origin.xCoord = xStart;
    origin.yCoord = yCoord;
    boolean textureCoordsDone = false;
    boolean movingRight = false;
    int maxXCoord = 0;
    int maxYCoord = 0;
    while (pointsVisited < points.size() && !textureCoordsDone) {
      if (movingRight) nextPoint = prevPoint.findPointRight(points);
      else nextPoint = prevPoint.findPointLeft(points);
      if (nextPoint != null && movingRight) {
        xCoord += 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingRight) {
        xCoord -= 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else {
        movingRight = !movingRight;
        nextPoint = prevPoint.findPointBelow(points);
        if (nextPoint == null) {
          // we are done
          textureCoordsDone = true;
        } else {
          yCoord -= 1;
          nextPoint.yCoord = yCoord;
          nextPoint.xCoord = xCoord;
          prevPoint = nextPoint;
        }
      }
      if (xCoord > maxXCoord)
        maxXCoord = xCoord;
      if (yCoord > maxYCoord)
        maxYCoord = yCoord;
    }

    pointsWide = maxXCoord + 1;
    pointsHigh = maxYCoord + 1;
  }

  /**
   * Texture mapping process that start a bottom left and start by moving horizontally to
   * the right.  And then up and moving back to the left, etc.
   */
  public void textureMapPointsLeftRight() {
    CXPoint origin = findBottomLeft();
    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    int xCoord = 0;
    int yCoord = 0;
    origin.xCoord = xCoord;
    origin.yCoord = yCoord;
    boolean textureCoordsDone = false;
    boolean movingRight = true;
    int maxXCoord = 0;
    int maxYCoord = 0;
    while (pointsVisited < points.size() && !textureCoordsDone) {
      if (movingRight) nextPoint = prevPoint.findPointRight(points);
      else nextPoint = prevPoint.findPointLeft(points);
      if (nextPoint != null && movingRight) {
        xCoord += 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingRight) {
        xCoord -= 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else {
        movingRight = !movingRight;
        nextPoint = prevPoint.findPointAbove(points);
        if (nextPoint == null) {
          // we are done
          textureCoordsDone = true;
        } else {
          yCoord += 1;
          nextPoint.yCoord = yCoord;
          nextPoint.xCoord = xCoord;
          prevPoint = nextPoint;
        }
      }
      if (xCoord > maxXCoord)
        maxXCoord = xCoord;
      if (yCoord > maxYCoord)
        maxYCoord = yCoord;
    }

    pointsWide = maxXCoord + 1;
    pointsHigh = maxYCoord + 1;
  }

  /**
   * Texture mapping process that starts at top left and moves down and then over to the
   * right and then back up, etc.
   * NOTE(tracy): Requires the hard-code height to be specified in getExpectedPointsHigh().
   */
  public void textureMapPointsTopBottom() {
    CXPoint origin = findTopLeft();
    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    int xCoord = 0;
    int yCoord = getExpectedPointsHigh() - 1;
    origin.xCoord = xCoord;
    origin.yCoord = yCoord;
    boolean textureCoordsDone = false;
    boolean movingUp = false;
    int maxXCoord = 0;
    int maxYCoord = 0;
    while (pointsVisited < points.size() && !textureCoordsDone) {
      if (movingUp) nextPoint = prevPoint.findPointAbove(points);
      else nextPoint = prevPoint.findPointBelow(points);
      if (nextPoint != null && movingUp) {
        yCoord += 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingUp) {
        yCoord -= 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else {
        movingUp = !movingUp;
        nextPoint = prevPoint.findPointRight(points);
        if (nextPoint == null) {
          // we are done
          textureCoordsDone = true;
        } else {
          xCoord += 1;
          nextPoint.yCoord = yCoord;
          nextPoint.xCoord = xCoord;
          prevPoint = nextPoint;
        }
      }
      if (xCoord > maxXCoord)
        maxXCoord = xCoord;
      if (yCoord > maxYCoord)
        maxYCoord = yCoord;
    }

    pointsWide = maxXCoord + 1;
    pointsHigh = maxYCoord + 1;
  }

  /**
   * Generate texture coordinates for our points.
   */
  public void textureMapPoints() {
    textureMapPoints(0, 0);
  }

  /**
   * Standard texture mapping process is to start at bottom left and then move up, over to right,
   * and then back down, etc.
   * @param xStartCoord Force the X start coordinate for partial panels.
   * @param yStartCoord Force the Y start coordinate for partial panels.
   */
  public void textureMapPoints(int xStartCoord, int yStartCoord) {
    CXPoint origin = findBottomLeft();
    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    int xCoord = xStartCoord;
    int yCoord = yStartCoord;
    origin.xCoord = xCoord;
    origin.yCoord = yCoord;
    boolean textureCoordsDone = false;
    boolean movingUp = true;
    int maxXCoord = 0;
    int maxYCoord = 0;
    while (pointsVisited < points.size() && !textureCoordsDone) {
      if (movingUp) nextPoint = prevPoint.findPointAbove(points);
      else nextPoint = prevPoint.findPointBelow(points);
      if (nextPoint != null && movingUp) {
        yCoord += 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingUp) {
        yCoord -= 1;
        nextPoint.yCoord = yCoord;
        nextPoint.xCoord = xCoord;
        prevPoint = nextPoint;
      } else {
        movingUp = !movingUp;
        nextPoint = prevPoint.findPointRight(points);
        if (nextPoint == null) {
          // we are done
          textureCoordsDone = true;
        } else {
          xCoord += 1;
          nextPoint.yCoord = yCoord;
          nextPoint.xCoord = xCoord;
          prevPoint = nextPoint;
        }
      }
      if (xCoord > maxXCoord)
        maxXCoord = xCoord;
      if (yCoord > maxYCoord)
        maxYCoord = yCoord;
    }

    pointsWide = maxXCoord + 1;
    pointsHigh = maxYCoord + 1;
  }

  /**
   * Returns the number of panels used around the structure.  Either 8 or 16, depending on panelType.
   * @return
   */
  public int numPanelsAround() {
    return numPanelsAround[panelType.ordinal()];
  }

  /**
   * Some panels are half panels that have their CNC file mirrored in order to define the points on the
   * panel.  Two half-panels will be co-planar with each other.
   * @return true if this panel is a half panel type.
   */
  public boolean isHalfPanel() {
    return (panelType == PanelType.A1 || panelType == PanelType.A2 ||
        panelType == PanelType.B1 || panelType == PanelType.B2 ||
        panelType ==  PanelType.E1 || panelType == PanelType.E2);
  }

  /**
   * Number of full panels around the structure.  This is different than the number of panels around
   * for panels that are half-panels, such as A1 and A2.
   * @return The number of full panels, i.e. number of co-planar faces.
   */
  public int numFullPanelsAround() {
    int p = numPanelsAround[panelType.ordinal()];
    if (isHalfPanel())
      return p/2;
    else
      return p;
  }

  public int numFacesAround() {
    int numFaces = numPanelsAround[panelType.ordinal()];
    return numFaces;
  }

  /**
   * Which face number is this panel.  This is the full panel equivalent.  For normal panels, this is
   * just the numPanel.  But for half-panels, we have 2 co-planar panels per face. Used for panel
   * rotation calculations.
   * @return The face number of this panel.
   */
  public int faceNum() {
    if (isHalfPanel())
      return panelNum / 2;
    else
      return panelNum;
  }

  public boolean isFirstHalfPanel() {
    if (panelType == PanelType.A1 || panelType == PanelType.B1 || panelType == PanelType.E1)
      return true;
    return false;
  }

  /**
   * Constructor for creating the dance panels.  This only requires the X,Y dance panel coordinates, panel
   * width dimension (they are square), width, pitch, and panelLayouNum.  For simplifying the pixel to
   * image texture mapping.  Panels are ordered row by row.  Since we are on the back of the
   * installation, the first panel will have a larger X value and the fourth panel in a row will have
   * a smaller (negative) X value.  They are centered around 0 on the X Axis.  They y coord in 3D space
   * will always be zero.
   * @param danceXPanel Dance panel number in X dimension.
   * @param danceYPanel Dance panel number in Y dimension.
   */
  public Panel(int danceXPanel, int danceYPanel, float width, float height, float pitch, int panelLayoutNum) {
    // The dance floor is on the back of the cone, so the angle = 180 degrees.  For now, we will just
    // throw it down somewhere behind the cone, centered on 0 where the the minimum Z direction is the
    // radius of the cone plus some offset.
    float xOffset = - ConeDownModel.dancePanelsHigh * height;
    // X,Y Panels are
    this.panelRegion = PanelRegion.DANCEFLOOR;
    this.topWidth = width;
    this.bottomWidth = width;
    this.height = height;
    this.pitch = pitch;
    this.danceXPanel = danceXPanel;
    this.danceYPanel = danceYPanel;
    this.panelLayoutNum = panelLayoutNum;

    this.panelNum = danceYPanel * ConeDownModel.dancePanelsWide + danceXPanel;
    this.radius = 0f;
    this.scoop = false;
    float pitchInMeters = pitch / ConeDownModel.inchesPerMeter;


    points = new ArrayList<CXPoint>();

    // The panels are centered around Z=0.  So the first panel starts at X=total width of all panels/2.
    // And decrements by width with each panel.
    float panelXStart = -height * ConeDownModel.dancePanelsHigh + height * danceYPanel; // The panels are centered around 0.
    float panelZStart = (ConeDownModel.dancePanelsWide * width)/2f - (danceXPanel * width); //
    double panelXFinish = panelXStart + height;
    double panelZFinish = panelZStart - width;
    this.xPos = panelXStart;
    this.yPos = 0f;
    this.zPos = panelZStart;

    logger.info("dance panel X start: " + panelXStart);
    logger.info("dance panel Z start: " + panelZStart);
    logger.info("dance panel X finish: " + panelXFinish);
    logger.info("dance panel Z finish: " + panelZFinish);

    pointsHigh = 0;
    int xCoord = 0;
    int yCoord = 0;
    float danceFloorPanelMargin = 3.5f / ConeDownModel.inchesPerMeter;

    // X,Y here are in panel-local coordinates.
    for (float y = danceFloorPanelMargin; y < this.height - danceFloorPanelMargin; y+= pitchInMeters) {
      pointsWide = 0;
      xCoord = 0;
      float percentYDone = y / height;
      for (float x = danceFloorPanelMargin; x < width - danceFloorPanelMargin; x += pitchInMeters)
      {
        float percentXDone = x / width;
        double ptX = panelXStart + (panelXFinish-panelXStart) * percentYDone + xOffset;
        double ptZ = panelZStart + (panelZFinish-panelZStart) * percentXDone;
        double ptY = 0f;
        CXPoint point = new CXPoint(this, ptX, 0f, ptZ, xCoord, yCoord, 0f, 0f);
        // We need to store panel local coordinates that we will rely on for output mapping.  Since the
        // dance floor is rotated to the back side of the installation and flat on the ground, the
        // panel local coordinates are not the same coordinate axis as worldspace.
        point.panelLocalX = point.z;
        point.panelLocalY = point.x;
        points.add(point);
        pointsWide++;
        xCoord++;
      }
      pointsHigh++;
      yCoord++;
    }

    // Since all dance panels are uniform, we will just compute our yCoordOffset from the pointsHigh number
    // we compute above.
    yCoordOffset = danceYPanel * pointsHigh;
    logger.info("yCoordOffset: " + yCoordOffset);
  }

  public float panelStartAngle() {
    float angleIncr = (scoop)?ConeDownModel.scoopAngleIncrement:ConeDownModel.coneAngleIncrement;
    return panelNum * angleIncr;
  }

  public List<CXPoint> getPoints() {
    return points;
  }

  /**
   * Utility class for parsing and processing the DXF file.
   */
  static public class BPoint {
    public BPoint(double x, double y) {
      this.x = (float)x;
      this.y = (float)y;
    }
    public BPoint(Point p) {
      x = (float) p.getX();
      y = (float) p.getY();
    }
    public void subtract(BPoint p) {
      x = x - p.x;
      y = y - p.y;
    }
    public void scale(float s) {
      x = s * x;
      y = s * y;
    }
    public void rot2D(float degrees) {
      float oldX = x;
      x = (float) (x * Math.cos(Math.toRadians(degrees))) - (float)(y * Math.sin(Math.toRadians(degrees)));
      y = (float) (oldX * Math.sin(Math.toRadians(degrees))) + (float)(y * Math.cos(Math.toRadians(degrees)));
    }
    float x;
    float y;

    /**
     * Convert point coordinates to ints and then form a coordinate string int(x),int(y) in order
     * to de-duplicate points.
     * @param pointMap
     * @param point
     */
    static public void dedupPoint(Map<String, BPoint> pointMap, BPoint point) {
      int xInt = (int) point.x;
      int yInt = (int) point.y;
      String key = "" + xInt + "," + yInt;
      if (!pointMap.containsKey(key))
        pointMap.put(key, point);
    }

    /**
     * Given a set of points, convert their vertex-centroid, aka arithmetic mean.
     * This can be used for 4 sided convex polygons to determine the corners.
     * @param pointsMap
     * @return
     */
    static public BPoint vertexCentroid(Map<String, BPoint> pointsMap) {
      float x = 0f;
      float y = 0f;

      for (BPoint bp : pointsMap.values()) {
        x += bp.x;
        y += bp.y;
      }
      x = x / pointsMap.values().size();
      y = y / pointsMap.values().size();

      return new BPoint(x, y);
    }

    static public BPoint findTopLeft(Map<String, BPoint> pointsMap, BPoint centroidPt) {
      for (BPoint bp : pointsMap.values()) {
        if (bp.x < centroidPt.x && bp.y > centroidPt.y)
          return bp;
      }
      return null;
    }

    static public BPoint findTopRight(Map<String, BPoint> pointsMap, BPoint centroidPt) {
      for (BPoint bp : pointsMap.values()) {
        if (bp.x > centroidPt.x && bp.y > centroidPt.y)
          return bp;
      }
      return null;
    }

    static public BPoint findBottomRight(Map<String, BPoint> pointsMap, BPoint centroidPt) {
      for (BPoint bp : pointsMap.values()) {
        if (bp.x > centroidPt.x && bp.y < centroidPt.y) {
          return bp;
        }
      }
      return null;
    }

    static public BPoint findBottomRightI(Map<String, BPoint> pointsMap, BPoint centroidPt) {
      float maxYRight = Float.MIN_VALUE;
      // Centroid-based approach only works with sort of rectangles.  For I_nano, the bottom right
      // is actually above the centroid.y so adding a hack to look for max Y to the right of the
      // centroid.x and then use that as top right so bottom right is not that one.
      for (BPoint bp : pointsMap.values()) {
        if (bp.x > centroidPt.x) {
          if (bp.y > maxYRight)
            maxYRight = bp.y;
        }
      }
      for (BPoint bp : pointsMap.values()) {
        if (bp.x > centroidPt.x && bp.y < (maxYRight - 0.1f))  // add epsilon for float compare
          return bp;
      }
      return null;
    }

    static public BPoint findBottomLeft(Map<String, BPoint> pointsMap, BPoint centroidPt) {
      for (BPoint bp : pointsMap.values()) {
        if (bp.x < centroidPt.x && bp.y < centroidPt.y) {
          return bp;
        }
      }
      return null;
    }
  }

  /**
   * Load a panel from a DXF file.  We use the kabeja library to parse the DXF file.  This works much better
   * than going through an Inkscape conversion step which really really wants to convert everything into
   * pixel coordinates FFS.  Units are inches.  LED positions are circles with a radius greater than 0.5 aka
   * 1/2 inch.  The cut boundaries of the panels are lines, unfortunately in random order wrt the bottom
   * left again.
   *
   * @param filename The name of the .DXF file to load into this panel.
   * @param mirror If true, the points will be mirrored around the right edge of the DXF layout.  This is
   *               necessary for various panels that are actually half panels of a trapezoid.
   * @return
   */
  public List<CXPoint> loadDXFPanel(String filename, boolean mirror, boolean flip) {
    List<CXPoint> points = new ArrayList<CXPoint>();
    Parser parser = ParserBuilder.createDefaultParser();

    logger.info("Loading DXF: " + filename);
    try {
      parser.parse(filename, DXFParser.DEFAULT_ENCODING);
      DXFDocument doc = parser.getDocument();
      DXFLayer layer = doc.getDXFLayer("VISIBLE");
      List<DXFCircle> arcs = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE);
      // logger.info("circles length: " + arcs.size());
      for (DXFCircle c : arcs) {
        Point centerPt = c.getCenterPoint();
        // LED positions have larger radius circles.
        // logger.info("circle: " + centerPt.getX() + "," + centerPt.getY() + " r=" + c.getRadius());
        if (c.getRadius() > 0.5f) {
          points.add(new CXPoint(this, centerPt.getX(), centerPt.getY(), 0f, 0, 0, 0f, 0f));
        }
      }
      Map<String, BPoint> pointMap = new HashMap<String, BPoint>();
      List<DXFLine> lines = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);
      int i = 0;
      for (DXFLine l : lines) {
        Point startPoint = l.getStartPoint();
        Point endPoint = l.getEndPoint();
        // Used this to log line coordinates to figure out the order of all four corners.
        // logger.info("line: " + (int) startPoint.getX() + "," + (int) startPoint.getY() + " to " + (int) endPoint.getX() + "," + (int) endPoint.getY());
        if (panelType == PanelType.A1 || panelType == PanelType.A2) {
          if (i == 0) {
            bPoints[0] = new BPoint(startPoint);
            bPoints[1] = new BPoint(endPoint);
          } else if (i == 2) {
            bPoints[2] = new BPoint(endPoint);
            bPoints[3] = new BPoint(startPoint);
          }
        }
        if (panelType == PanelType.B1 || panelType == PanelType.B2) {
          if (i == 1) {
            bPoints[0] = new BPoint(startPoint);
            bPoints[3] = new BPoint(endPoint);
          } else if (i == 3) {
            bPoints[2] = new BPoint(startPoint);
            bPoints[1] = new BPoint(endPoint);
          }
        }
        if (panelType == PanelType.C) {
          if (i == 0) {
            bPoints[3] = new BPoint(startPoint);
            bPoints[2] = new BPoint(endPoint);
          } else if (i == 2) {
            bPoints[1] = new BPoint(endPoint);
            bPoints[0] = new BPoint(startPoint);
          }
        }
        if (panelType == PanelType.D) {
          if (i == 0) {
            bPoints[3] = new BPoint(startPoint);
            bPoints[2] = new BPoint(endPoint);
          } else if (i == 2) {
            bPoints[1] = new BPoint(endPoint);
            bPoints[0] = new BPoint(startPoint);
          }
        }
        if (panelType == PanelType.E1 || panelType == PanelType.E2) {
          if (i == 0) {
            bPoints[3] = new BPoint(startPoint);
            bPoints[2] = new BPoint(endPoint);
          } else if (i == 2) {
            bPoints[1] = new BPoint(endPoint);
            bPoints[0] = new BPoint(startPoint);
          }
        }
        if (panelType == PanelType.F) {
          BPoint sPoint = new BPoint(startPoint);
          BPoint ePoint = new BPoint(endPoint);
          //logger.info("line: " + (int) sPoint.x + "," + (int) sPoint.y + " to " +
          //    (int) ePoint.x + "," + (int) ePoint.y);
          // -19,-20 to 19,-20  top left to top right
          // -19,-20 to -20,-29 top left to bottom left
          // 20,-29 to -20,-29 bottom right to bottom left
          // 19,-20 to 20,-29 top right to bottom right
          if (i == 0) {
            bPoints[3] = new BPoint(sPoint.x, sPoint.y);
            bPoints[2] = new BPoint(ePoint.x, ePoint.y);
          } else if (i == 2) {
            bPoints[1] = new BPoint(sPoint.x, sPoint.y);
            bPoints[0] = new BPoint(ePoint.x, ePoint.y);
          }
        }
        if (panelType == PanelType.G) {
          //logger.info("line: " + (int) startPoint.getX() + "," + (int) startPoint.getY() + " to " +
          //    (int) endPoint.getX() + "," + (int) endPoint.getY());
          // -20,20 to 20,20 top left to top right
          // -22,-21 to -20,20  bottom left top left
          // 22,-21 to -22,-21 bottom right bottom left
          // 22,-21 to 20,20 bottom right top right
          if (i == 0) {
            bPoints[3] = new BPoint(startPoint);
            bPoints[2] = new BPoint(endPoint);
          } else if (i == 2) {
            bPoints[1] = new BPoint(startPoint);
            bPoints[0] = new BPoint(endPoint);
          }
        }
        if (panelType == PanelType.H) {
          BPoint.dedupPoint(pointMap, new BPoint(startPoint));
          BPoint.dedupPoint(pointMap, new BPoint(endPoint));
        }

        if (panelType == PanelType.I) {
          BPoint.dedupPoint(pointMap, new BPoint(startPoint));
          BPoint.dedupPoint(pointMap, new BPoint(endPoint));
        }
        i++;
      }

      if (panelType == PanelType.H || panelType == PanelType.I) {
        BPoint centroid = BPoint.vertexCentroid(pointMap);
        bPoints[0] = BPoint.findBottomLeft(pointMap, centroid);
        if (panelType == PanelType.I)
          bPoints[1] = BPoint.findBottomRightI(pointMap, centroid);
        else
          bPoints[1] = BPoint.findBottomRight(pointMap, centroid);
        bPoints[2] = BPoint.findTopRight(pointMap, centroid);
        bPoints[3] = BPoint.findTopLeft(pointMap, centroid);
      }
      BPoint bottomLeft = bPoints[0];
      BPoint bottomRight  = bPoints[1];
      BPoint topRight = bPoints[2];
      BPoint topLeft = bPoints[3];

      // TODO(tracy): This are all just visual hacks to get an approximation.  The correct solution
      // would be to position the points in X and Y such that they are relative to the hypothetical
      // full-panel boundary that this partial panel is based on. Would it be possible to just
      // account for the bottom and left deltas?  For half a square panel tall, we could just take the difference
      // in height of the panels and add that to all y coordinates (assuming that it was the bottom half that
      // was missing).  For half a square panel wide we could take the difference in widths and add that to the
      // x coordinate (assuming it was the left half that was missing).  Top and right missing portions don't
      // affect our 3d positioning.
      if (panelType == PanelType.H && (panelNum == 0 || panelNum == 15)) {
        bottomRight.y = bottomLeft.y;
        bottomRight.x = topRight.x - 2f;
        if (panelNum == 15) {
          bottomLeft.x -= 20f;
        }
      }
      if (panelType == PanelType.H) {
        if (panelNum == 5)
          bottomLeft.x -= 15f;
        if (panelNum == 6)
          bottomLeft.x -= 1f;
        if (panelNum == 9) {
          bottomRight.x += 15f;
          bottomRight.y = bottomLeft.y;
        }
        if (panelNum == 10) {
          bottomRight.x += 10f;
          bottomRight.y = bottomLeft.y;
        }
      }
      if (panelType == PanelType.I && (panelNum == 0)) {
        bottomLeft.x -= 20f;
      }
      if (panelType == PanelType.I && panelNum > 8) {
        bottomRight.y = bottomLeft.y;
      }

      // Handle mirror situation
      if (mirror) {
        for (CXPoint p : points) {
          p.x = bPoints[1].x - p.x;
          p.y -= bPoints[1].y;
        }
        float bottomRightYTmp = bottomRight.y;
        float bottomRightXTmp = bottomRight.x;
        bottomRight.x = bottomRight.x - bottomLeft.x;
        bottomRight.y = bottomLeft.y;
        bottomLeft.x = 0f;
        bottomLeft.y = bottomRightYTmp;
        float tempX = topRight.x;
        float tempY = topRight.y;
        topRight.x = bottomRightXTmp - topLeft.x;
        topRight.y = topLeft.y;
        topLeft.x = bottomRightXTmp - tempX;
        topLeft.y = tempY;
      } else if (flip) {
        float bottomRightXTmp = bottomRight.x;
        float bottomRightYTmp = bottomRight.y;
        for (CXPoint p : points) {
          p.y = bottomRight.y - p.y;
        }
        topLeft.y = bottomLeft.y - topLeft.y;
        topRight.y = bottomRight.y - topRight.y;
        // Now we need to swap tops and bottoms.
        BPoint oldTopLeft = topLeft;
        BPoint oldTopRight = topRight;
        topRight = bottomRight;
        topLeft = bottomLeft;
        bottomRight = oldTopRight;
        bottomLeft = oldTopLeft;
        // Now adjust points by bottom left
        for (CXPoint p : points) {
          p.x -= bottomLeft.x;
          p.y -= bottomLeft.y;
        }
        // Now fix up the official boundary points and make bottomLeft at 0,0
        bPoints[0] = bottomLeft;
        bPoints[1] = bottomRight;
        bPoints[2] = topRight;
        bPoints[3] = topLeft;
        bPoints[1].subtract(bPoints[0]);
        bPoints[2].subtract(bPoints[0]);
        bPoints[3].subtract(bPoints[0]);
        bPoints[0].x = 0f;
        bPoints[1].y = 0f;
      } else {
        for (CXPoint p : points) {
          p.x -= bottomLeft.x;
          p.y -= bottomLeft.y;
        }
        bPoints[1].subtract(bPoints[0]);
        bPoints[2].subtract(bPoints[0]);
        bPoints[3].subtract(bPoints[0]);
        bPoints[0].x = 0f;
        bPoints[1].y = 0f;
      }


      for (BPoint p : bPoints) {
        p.scale(CNC_SCALE);
      }

      /*
      if (panelType == PanelType.H || panelType == PanelType.G) {
        for (BPoint bp : pointMap.values()) {
          logger.info("bpoint: " + bp.x + "," + bp.y);
        }
        logBoundary();
      }
      */

      bottomWidth = bPoints[1].x - bPoints[0].x;
      topWidth = bPoints[2].x - bPoints[3].x;
      height = bPoints[2].y - bPoints[1].y;
    } catch (ParseException pex) {
      logger.info("Parse exception: " + pex.getMessage());
    }
    return points;
  }

  public CXPoint getCXPointAtTexCoord(int x, int y) {
    return CXPoint.getCXPointAtTexCoord(points, x, y);
  }

  /**
   * Return the points of this panel in wiring order for this panel.  There is a standard order that
   * starts at the bottom left and moves right and then up and then back left, etc.  The ground intercepted
   * panels and the door panels require custom wiring.  Rather than relying on texture coordinates for those
   * panels, we can just re-navigate the points locally as we did with texture mapping but the start point
   * will be some known texture coordinate and wiring directions might be different from specific strategy
   * used for texture mapping a specific panel.
   * @return
   */
  public List<CXPoint> pointsInWireOrder() {
    if (panelType == PanelType.H && panelNum == 0) {
      return wirePointsFromCoords(6, 0, false);
    } else if (panelType == PanelType.H && panelNum == 15) {
      return wirePointsFromCoords(0, 0, true);
    } else if (panelType == PanelType.H && panelNum == 5) {
      // Right milli
      return wirePointsFromCoords(4, 0, false);
    } else if (panelType == PanelType.H && panelNum == 6) {
      // Right micro
      return wirePointsFromCoords(3, 1, false);
    } else if (panelType == PanelType.H && (panelNum == 7)) {
      // Right nano.
      return wirePointsFromCoords(0, 2, true);
    } else if (panelType == PanelType.H && (panelNum == 8)) {
      // Left nano
      return wirePointsFromCoords(0, 2, true);
    } else if (panelType == PanelType.H && (panelNum == 9)) {
      // Left micro
      return wirePointsFromCoords(3, 1, true);
    } else if (panelType == PanelType.H && (panelNum == 10)) {
      // Left milli
      return wirePointsFromCoords(2, 0, true);
    } else if (panelType == PanelType.I && (panelNum == 0)) {
      // Right door
      return wirePointsFromCoords(3, 0, true);
    } else if (panelType == PanelType.I && (panelNum == 15)) {
      // Left door
      return wirePointsFromCoords(0, 0, true);
    } else if (panelType == PanelType.I && (panelNum == 2)) {
      // Right milli
      return wirePointsFromCoords(0, 1, true);
    } else if (panelType == PanelType.I && (panelNum == 3)) {
      // Right micro
      return wirePointsFromCoords(0, 2, true);
    } else if (panelType == PanelType.I && (panelNum == 4)) {
      // Right nano
      return wirePointsFromCoords(2, 4, false);
    } else if (panelType == PanelType.I && (panelNum == 11)) {
      // Left nano
      return wirePointsFromCoords(3, 4, true);
    } else if (panelType == PanelType.I && (panelNum == 12)) {
      // Left micro
      return wirePointsFromCoords(0, 2, true);
    } else if (panelType == PanelType.I && (panelNum == 13)) {
      // Left milli
      return wirePointsFromCoords(0, 1, true);
    } else if (panelRegion == Panel.PanelRegion.DANCEFLOOR) {
      if (danceXPanel == 2) {
        // The last column the wire starts at top right in texture coordinates.
        return wirePointsByTexCoords(6, 6, false, false);
      } if (danceXPanel == 1) {
        // The middle column of panels has the wire start in the bottom left
        return pointsInWireOrderStandard();
      } else if (danceXPanel == 0) {
        return wirePointsByTexCoords(0, 6, true, false);
      }
    } else {
      return pointsInWireOrderStandard();
    }
    return null;
  }

  /**
   * Retrieve points in wiring order based on start texture coordinates and whether we start moving right or
   * start moving left.
   * @param startXCoord The x texture coordinate of the start point.
   * @param startYCoord The y texture coordinate of the start point.
   * @param movingRight If true, start by moving right, otherwise start by moving left.
   * @return List of points in wire order.
   */

  public List<CXPoint> wirePointsFromCoords(int startXCoord, int startYCoord, boolean movingRight) {
    return wirePointsFromCoords(startXCoord, startYCoord, movingRight, true);
  }

  /**
   * Retrieve points in wiring order based on start texture coordinates and whether we start moving right or
   * start moving left and whether we wire from bottom up or from top down.
   * @param startXCoord The x texture coordinate of the start point.
   * @param startYCoord The y texture coordinate of the start point.
   * @param movingRight If true, start by moving right, otherwise start by moving left.
   * @param movingUp If true, the wire moves up otherwise it moves down.
   * @return List of points in wire order.
   */
  public List<CXPoint> wirePointsFromCoords(int startXCoord, int startYCoord, boolean movingRight, boolean movingUp) {
    List<CXPoint> pointsWireOrder = new ArrayList<CXPoint>();

    CXPoint origin = getCXPointAtTexCoord(startXCoord, startYCoord);
    pointsWireOrder.add(origin);

    if (panelRegion != PanelRegion.DANCEFLOOR)
      logger.info("wire panel: " + panelTypeNames[panelType.ordinal()] + "" + panelNum +
          " start " + startXCoord + "," + startYCoord + " right=" + movingRight);

    CXPoint prevPoint = origin;
    CXPoint nextPoint = null;
    int pointsVisited = 0;
    boolean pointsDone = false;
    while (pointsVisited < points.size() && !pointsDone) {
      if (movingRight) nextPoint = prevPoint.findPointRight(points);
      else nextPoint = prevPoint.findPointLeft(points);
      if (nextPoint != null && movingRight) {
        pointsWireOrder.add(nextPoint);
        prevPoint = nextPoint;
      } else if (nextPoint != null && !movingRight) {
        pointsWireOrder.add(nextPoint);
        prevPoint = nextPoint;
      } else {
        movingRight = !movingRight;
        if (movingUp) nextPoint = prevPoint.findPointAbove(points);
        else nextPoint = prevPoint.findPointBelow(points);
        if (nextPoint == null) {
          // we are done
          pointsDone = true;
        } else {
          pointsWireOrder.add(nextPoint);
          prevPoint = nextPoint;
        }
      }
    }
    // Keep a reference in case we want patterns to reference this.
    this.pointsWireOrder = pointsWireOrder;
    return pointsWireOrder;
  }

  public List<CXPoint> pointsInWireOrderStandard() {
    List<CXPoint> pointsWireOrder = new ArrayList<CXPoint>();
    // For each panel we wire from bottom left to bottom right and then move up one pixel
    // and then wire backwards from right to left, etc.  We can use our texture coordinates
    // to navigate the points on a panel.
    boolean movingLeft = false;
    for (int rowNum = 0; rowNum < pointsHigh; rowNum++) {
      for (int colNum = 0; colNum < pointsWide; colNum++) {
        int x = colNum;
        if (movingLeft) {
          x = (pointsWide - 1) - colNum;
        }
        CXPoint p = getCXPointAtTexCoord(x, rowNum);
        // logger.info("point at: " + x + "," + rowNum);
        pointsWireOrder.add(p);
      }
      movingLeft = !movingLeft;
    }
    // Keep a reference in case we want patterns to reference this.
    this.pointsWireOrder = pointsWireOrder;
    return pointsWireOrder;
  }

  public List<CXPoint> wirePointsByTexCoords(int startXCoord, int startYCoord, boolean movingRight, boolean movingUp) {
    List<CXPoint> pointsWireOrder = new ArrayList<CXPoint>();
    // For each panel we wire from bottom left to bottom right and then move up one pixel
    // and then wire backwards from right to left, etc.  We can use our texture coordinates
    // to navigate the points on a panel.
    for (int rowNum = 0; rowNum < pointsHigh; rowNum++) {
      for (int colNum = 0; colNum < pointsWide; colNum++) {
        int x = colNum;
        if (!movingRight) {
          x = (pointsWide - 1) - colNum;
        }
        int y = rowNum;
        if (!movingUp) {
          y = (pointsHigh - 1) - rowNum;
        }
        CXPoint p = getCXPointAtTexCoord(x, y);
        pointsWireOrder.add(p);
      }
      movingRight = !movingRight;
    }
    // Keep a reference in case we want patterns to reference this.
    this.pointsWireOrder = pointsWireOrder;
    return pointsWireOrder;
  }

  /**
   * Return the appropriate Panel given dance panel/tile coordinates.
   * @param dancePanels The list of dance panels.
   * @param x The x coordinate for the dance panel/tile.
   * @param y The y coordinate for the dance panel/tile.
   * @return The requested panel or null if not found.
   */
  static public Panel getDancePanelXY(List<Panel> dancePanels, int x, int y) {
    for (Panel p : dancePanels) {
      if (p.danceXPanel == x && p.danceYPanel == y) {
        return p;
      }
    }
    return null;
  }

  /**
   * Return a list of points cropped by texture coordinates.  This helps us build a map
   * for the Galactic Jungle cars.  Coordinates are *inclusive*
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  public List<CXPoint> cropPoints(int x1, int y1, int x2, int y2) {
    List<CXPoint> cropPoints = new ArrayList<CXPoint>();
    for (CXPoint p : points) {
      if (p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2)
        cropPoints.add(p);
    }
    return cropPoints;
  }
}

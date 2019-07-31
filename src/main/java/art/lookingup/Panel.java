package art.lookingup;

import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  }

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
      "G",
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
  };

  public PanelRegion panelRegion;
  public PanelType panelType;
  public int panelLayoutNum;
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

  public List<CXPoint> points;
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
    this.radius = radius;

    boolean mirror = false;
    boolean flip = false;

    if (panelType == PanelType.A2 || panelType == PanelType.B2 || panelType == PanelType.E2)
      mirror = true;

    if (panelType == panelType.H || panelType == panelType.G || panelType == PanelType.F) {
      scoop = true;
      panelRegion = PanelRegion.SCOOP;
    } else {
      scoop = false;
      panelRegion = PanelRegion.CONE;
    }

    if (panelType == PanelType.H) {
      flip = true;
    }
    String filename = panelFilenames[panelType.ordinal()];

    points = loadDXFPanel(filename + "_LED.dxf", mirror, flip);
    //points = loadPanelSVG("panel_"+ filename + ".svg", mirror);

    // Points are now in panel local coordinate space.
    textureMapPoints();

    for (CXPoint p : points) {
      // Convert to meters.
      p.x *= CNC_SCALE;
      p.y *= CNC_SCALE;
      p.storePanelLocalXY();
    }

    float angleIncr = 360f / numFullPanelsAround();
    float panelAngle = faceNum() * angleIncr;

    // panelNum * bottomWidth; //
    double panelXStart =  radius * Math.cos(Math.toRadians(panelAngle));
    double panelZStart = radius * Math.sin(Math.toRadians(panelAngle));

    // Need the panel anglea of the endpoint
    double panelXFinish = radius * Math.cos(Math.toRadians(panelAngle + angleIncr));
    double panelZFinish = radius * Math.sin(Math.toRadians(panelAngle + angleIncr));

    // If we are a half-panel type, then we need to adjust our panel start position.
    // For the second half, the panel start is halfway between the panel start and
    // finish for our entire face.
    if (isHalfPanel() && !isFirstHalfPanel()) {
      panelXStart = (panelXFinish - panelXStart) * 0.5f + panelXStart;
      panelZStart = (panelZFinish - panelZStart) * 0.5f + panelZStart;
    }

    logger.info("yCoordOffset =" + yCoordOffset + " pointsHigh=" + pointsHigh);
    for (CXPoint p : points) {
      float angle =  90f + 45f/2f + (angleIncr * faceNum());
      if (panelType == PanelType.G || panelType == PanelType.H || panelType == PanelType.F) {
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

  /**
   * Generate texture coordinates for our points.
   */
  public void textureMapPoints() {
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
    // Origin is prevPoint.  Starting at prevPoint, move around assigning xCoord, yCoord.
    CXPoint origin = prevPoint;
    int pointsVisited = 0;
    int xCoord = 0;
    int yCoord = 0;
    origin.xCoord = 0;
    origin.yCoord = 0;
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
    // logger.info("panel dimensions: " + pointsWide + "x" + pointsHigh);

    Collections.sort(points);


    /*
    for (CXPoint p : points) {
      logger.info("point " + p.x + "," + p.y + " texX,texY: " + p.xCoord + "," + p.yCoord);
    }
    */

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

    try {
      parser.parse(filename, DXFParser.DEFAULT_ENCODING);
      DXFDocument doc = parser.getDocument();
      DXFLayer layer = doc.getDXFLayer("VISIBLE");
      List<DXFCircle> arcs = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE);
      logger.info("circles length: " + arcs.size());
      for (DXFCircle c : arcs) {
        Point centerPt = c.getCenterPoint();
        // logger.info("circle: " + centerPt.getX() + "," + centerPt.getY() + " r=" + c.getRadius());
        if (c.getRadius() > 0.5f) {
          points.add(new CXPoint(this, centerPt.getX(), centerPt.getY(), 0f, 0, 0, 0f, 0f));
        }
      }
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
          sPoint.rot2D(-90f);
          ePoint.rot2D(-90f);
          //logger.info("rot line: " + (int) sPoint.x + "," + (int) sPoint.y + " to " +
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
        if (panelType == PanelType.G || panelType == PanelType.H) {
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
        i++;
      }
      BPoint bottomLeft = bPoints[0];
      BPoint bottomRight  = bPoints[1];
      BPoint topRight = bPoints[2];
      BPoint topLeft = bPoints[3];

      // We need to rotate the F panel by -90 degrees.
      if (panelType == PanelType.F) {
        // logger.info("Rotating F Panel points");
        for (CXPoint p : points) {
          p.rot2D(-90f);
        }
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

      bottomWidth = bottomRight.x - bottomLeft.x;
      topWidth = topRight.x - topLeft.x;
      height = topRight.y - bottomRight.y;
    } catch (ParseException pex) {
      logger.info("Parse exception: " + pex.getMessage());
    }
    return points;
  }

  public CXPoint getCXPointAtTexCoord(int x, int y) {
    return CXPoint.getCXPointAtTexCoord(points, x, y);
  }
}

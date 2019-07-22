package art.lookingup;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Panel {
  private static final Logger logger = Logger.getLogger(Panel.class.getName());

  public static float CNC_SCALE = 1/150f;

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
      8,
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

  List<CXPoint> points;
  List<Float[]> panelBoundaryPts = new ArrayList<Float[]>(4);

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
    if (panelType == PanelType.A2 || panelType == PanelType.B2 || panelType == PanelType.E2)
      mirror = true;
    String filename = panelFilenames[panelType.ordinal()];

    points = loadPanelSVG("panel_"+ filename + ".svg", mirror);
    // Points are now in panel local coordinate space.

    textureMapPoints();

    for (CXPoint p : points) {
      // Convert to meters.
      p.x *= CNC_SCALE;
      p.y *= CNC_SCALE;
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

    System.out.println("yCoordOffset =" + yCoordOffset + " pointsHigh=" + pointsHigh);
    for (CXPoint p : points) {
      float angle = 90f + 45f/2f + (angleIncr * faceNum());
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
    logger.info("panel dimensions: " + pointsWide + "x" + pointsHigh);

    Collections.sort(points);

    logger.info("panelType = " + panelFilenames[panelType.ordinal()]);
    for (CXPoint p : points) {
      logger.info("point " + p.x + "," + p.y + " texX,texY: " + p.xCoord + "," + p.yCoord);
    }
  }

  /**
   * For reference, the interpolation based code
   *
   *     // Need the panel anglea of the endpoint
   *     double panelXFinish = radius * Math.cos(Math.toRadians(panelAngle + angleIncr));
   *     double panelZFinish = radius * Math.sin(Math.toRadians(panelAngle + angleIncr));
   *     double panelXStartTop = radius * 1f * Math.cos(Math.toRadians(panelAngle));
   *     double panelZStartTop = radius * 1f * Math.sin(Math.toRadians(panelAngle));
   *     double panelXFinishTop = radius * 0.9f * Math.cos(Math.toRadians(panelAngle + angleIncr));
   *     double panelZFinishTop = radius * 0.9f * Math.cos(Math.toRadians(panelAngle + angleIncr));
   *
   *      if (isHalfPanel() && isFirstHalfPanel()) {
   *       panelXFinish = (panelXFinish - panelXStart) * 0.5f + panelXStart;
   *       panelZFinish = (panelZFinish - panelZStart) * 0.5f + panelZStart;
   *       panelXFinishTop = (panelXFinishTop - panelXStartTop) * 0.5f + panelXStartTop;
   *       panelZFinishTop = (panelZFinishTop - panelZStartTop) * 0.5f + panelZStartTop;
   *     } else if (isHalfPanel() && !isFirstHalfPanel()) {
   *       panelXStartTop = (panelXFinishTop - panelXStartTop) * 0.5f + panelXStartTop;
   *       panelZStartTop = (panelZFinishTop - panelZStartTop) * 0.5f + panelZStartTop;
   *     }
   *       double percentY = p.y / height;
   *       double widthAtY = topWidth + (1f - percentY) * (bottomWidth - topWidth);
   *       double percentX = p.x / bottomWidth;
   *       double startPtX = panelXStart + percentY * (panelXStartTop - panelXStart);
   *       double startPtZ = panelZStart + percentY * (panelZStartTop - panelZStart);
   *       double endPtX = panelXFinish + percentY * (panelXFinishTop - panelXFinish);
   *       double endPtZ = panelZFinish + percentY * (panelZFinishTop - panelZFinish);
   *       //p.x = (float)(startPtX + percentX * (endPtX - startPtX));
   *       //p.z = (float)(startPtZ + percentX * (endPtZ - startPtZ));
   *       //p.x = (float)(panelXStart + percentX * (panelXFinish - panelXStart));
   *       //p.z = (float)(panelZStart + percentX * (panelZFinish - panelZStart));
   *
   *
   */
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
   * Create a panel based on vacuum forming dimensions and constaint 6" pixel pitch.
   * @param topWidth
   * @param bottomWidth
   * @param height
   * @param pitch
   * @param xPos
   * @param yPos
   * @param zPos
   * @param panelNum
   * @param yCoordOffset
   * @param radius
   * @param scoop
   * @param panelLayoutNum
   */
  public Panel(float topWidth, float bottomWidth, float height, float pitch,
               float xPos, float yPos, float zPos, int panelNum, int yCoordOffset, float radius,
               boolean scoop,
               int panelLayoutNum) {
    this.topWidth = topWidth;
    this.bottomWidth = bottomWidth;
    this.height = height;
    this.pitch = pitch;
    this.xPos = xPos;
    this.yPos = yPos;
    this.zPos = zPos;
    this.panelNum = panelNum;
    this.yCoordOffset = yCoordOffset;
    this.radius = radius;
    this.panelLayoutNum = panelLayoutNum;
    this.scoop = scoop;
    if (scoop) {
      panelRegion = PanelRegion.SCOOP;
    } else {
      panelRegion = PanelRegion.CONE;
    }
    float pitchInMeters = pitch / ConeDownModel.inchesPerMeter;
    points = new ArrayList<CXPoint>();

    // Create LXPoints based on initial x,y,z and width and height and pitch
    float angleIncr = (scoop)?ConeDownModel.scoopAngleIncrement:ConeDownModel.coneAngleIncrement;

    float panelAngle = panelNum * angleIncr;
    double panelXStart = radius * Math.cos(Math.toRadians(panelAngle));
    double panelZStart = radius * Math.sin(Math.toRadians(panelAngle));
    // Need the panel anglea of the endpoint
    double panelXFinish = radius * Math.cos(Math.toRadians(panelAngle + angleIncr));
    double panelZFinish = radius * Math.sin(Math.toRadians(panelAngle + angleIncr));

    System.out.println("yCoordOffset: " + yCoordOffset);
    pointsHigh = 0;
    int xCoord = 0;
    int yCoord = 0;
    for (float y = ConeDownModel.panelMargin; y < this.height - ConeDownModel.panelMargin; y+= pitchInMeters) {
      pointsWide = 0;
      xCoord = 0;
      for (float x = ConeDownModel.panelMargin; x < this.topWidth - ConeDownModel.panelMargin; x += pitchInMeters)
      {
        float percentDone = x / topWidth;
        double ptX = panelXStart + (panelXFinish-panelXStart) * percentDone;
        double ptZ = panelZStart  + (panelZFinish-panelZStart) * percentDone;
        // TODO(tracy): Radius is currently just assigned the panel radius. The actual radius is the intersection of
        // the circle/ring radius at that led position with a chord from the radius of the left edge of the panel to the
        // right edge of the panel.  Also, for non-rectangular panels the radius of the current row is an interpolation
        // between the radius of the bottom rib of the panel and the top rib of the panel.
        CXPoint point = new CXPoint(this, ptX, y + yPos, ptZ, xCoord, yCoord, panelAngle + percentDone * angleIncr, radius);
            /*float angle = panelNum * PolarAngleIncrement + (x / topWidth) * PolarAngleIncrement;
            System.out.println("angle=" + angle);
            LXPoint point = new LXPoint(Radius*Math.sin(Math.toRadians(angle)), y + yPos,
                Radius*Math.cos(Math.toRadians(angle)));
                */

        points.add(point);
        pointsWide++;
        xCoord++;
      }
      pointsHigh++;
      yCoord++;
    }
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

    System.out.println("dance panel X start: " + panelXStart);
    System.out.println("dance panel Z start: " + panelZStart);
    System.out.println("dance panel X finish: " + panelXFinish);
    System.out.println("dance panel Z finish: " + panelZFinish);

    pointsHigh = 0;
    int xCoord = 0;
    int yCoord = 0;
    // X,Y here are in panel-local coordinates.
    for (float y = ConeDownModel.panelMargin; y < this.height - ConeDownModel.panelMargin; y+= pitchInMeters) {
      pointsWide = 0;
      xCoord = 0;
      float percentYDone = y / height;
      for (float x = ConeDownModel.panelMargin; x < width - ConeDownModel.panelMargin; x += pitchInMeters)
      {
        float percentXDone = x / width;
        double ptX = panelXStart + (panelXFinish-panelXStart) * percentYDone + xOffset;
        double ptZ = panelZStart + (panelZFinish-panelZStart) * percentXDone;
        double ptY = 0f;
        if (danceXPanel == 0 && danceYPanel == 0) {
          System.out.println("Adding point: " + ptX + "," + ptZ + " coord: " + xCoord + "," + yCoord);
        }
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
    System.out.println("yCoordOffset: " + yCoordOffset);
  }

  public float panelStartAngle() {
    float angleIncr = (scoop)?ConeDownModel.scoopAngleIncrement:ConeDownModel.coneAngleIncrement;
    return panelNum * angleIncr;
  }

  public List<CXPoint> getPoints() {
    return points;
  }

  /**
   * Loads a panel definition from an SVG file.  We convert the DWG to DXF online and then import DXF into
   * InkScape, select the entire object, reset the page size to fit the selection, and then save
   * the file to an SVG.
   *
   * The LED positions are represented by the path segments below.  NOTE(tracy):  There are also
   * some mounting holes (circles) defined in the file.  They are at the end of the file and have
   * a radius of around 2.8 versus the 4.7 visible in the example below.
   *
   *   <g
   *      inkscape:groupmode="layer"
   *      inkscape:label="Visible"
   *      id="g245"
   *      transform="translate(95.696387,-174.00842)">
   *     <path
   *        d="m -61.553721,417.07872 a 2.362205,2.362205 0 1 0 -4.72441,0 2.362205,2.362205 0 1 0 4.72441,0 z"
   *        style="fill:none;stroke:#000000"
   *        id="path73"
   *        inkscape:connector-curvature="0" />
   *     <path
   *        d="m -62.176841,437.85678 a 2.362205,2.362205 0 1 0 -4.724409,0 2.362205,2.362205 0 1 0 4.724409,0 z"
   *        style="fill:none;stroke:#000000"
   *        id="path75"
   *        inkscape:connector-curvature="0" />
   *
   *        Panel cut boundaries look like this:
   *        <path
   *        d="M -95.19685,495.23448 H 0"
   *        style="fill:none;stroke:#0000FF"
   *        id="path237"
   *        inkscape:connector-curvature="0" />
   *
   * @param filename
   * @return
   */

  public List<CXPoint> loadPanelSVG(String filename, boolean mirror) {
    List<CXPoint> points = new ArrayList<CXPoint>();

    String drawingSvg = "";
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(filename);
      // //@d^='m 0,0 c'
      //String xpathExpression = "//g[path[@d]]/@transform";
      // //div[@id='hero']/img
      // //div[@id='hero']/img
      //  "//g[@inkscape:label='Visible']/path";
      String xpathExpression =  "//g/path";
      XPathFactory xpf = XPathFactory.newInstance();
      XPath xpath = xpf.newXPath();
      XPathExpression expression = xpath.compile(xpathExpression);
      NodeList svgPaths = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
      logger.log(Level.INFO, "Num total nodes: " + svgPaths.getLength());
      float minPanelBoundaryX = Float.MAX_VALUE;
      float minPanelBoundaryY = Float.MAX_VALUE;
      float minPanelBoundarySum = Float.MAX_VALUE;
      for (int i = 0; i < svgPaths.getLength(); i++) {
        Node node = svgPaths.item(i);
        NamedNodeMap nodeMap = node.getAttributes();
        Node dNode = nodeMap.getNamedItem("d");
        String dText = "";
        if (dNode != null) {
          dText = dNode.getNodeValue();
          //logger.log(Level.INFO, "D text = " + dText);
          //  m -71.301488,187.94966 a 2.362205,2.362205 0 1 0 -4.72441,0 2.362205,2.362205 0 1 0 4.72441,0 z
          // Extract m -61.553721,417.07872 for position
          // Extract a 2.362205,2.362205 for led hole
          // Extract a 1.417323,1.417323 for drill hole
          String[] values = dText.split(" ");
          if (values.length == 4 || values.length == 3) {
            // Panel cut boundary.
            // d="M -95.19685,495.23448 H 0"
            // NOTE(Tracy): This directive represents 2 points in our outline
            // d="M 0,495.23448 V 174.50842"
            //
            //  d="M -95.19685,495.23448 H 0"
            //  d="M 0,495.23448 V 174.50842"
            //  d="M -81.377953,174.50842 H 0"
            //  d="M -95.19685,495.23448 -81.377953,174.50842"
            // First, 0,495.23448 and then a Vertical line to 0,174.50842.
            // The fourth svg entry is actually just a path back to our original.
            // We pick up 2 points on the Vertical move
            // NOTE(tracy): This will have to be fixed with future SVG files considering
            // that the move might not necessarily be vertical.
            // A non-orthogonal move with the pen down looks like this:
            // M -95.19685,495.23448 -81.377953,174.50842
            // So that is actually only 3 values.
            if (panelBoundaryPts.size() == 4)
              continue;
            if (panelType == PanelType.A1 || panelType == PanelType.A2) {
              /* d="M -95.19685,495.23448 H 0"  bottom left
                 d="M 0,495.23448 V 174.50842" bottom right + top right
                 d="M -81.377953,174.50842 H 0" top left
                 d="M -95.19685,495.23448 -81.377953,174.50842" */

              String[] pos = values[1].split(",");
              Float[] panelBoundaryPos = new Float[2];
              panelBoundaryPos[0] = Float.parseFloat(pos[0]);
              panelBoundaryPos[1] = Float.parseFloat(pos[1]);
              panelBoundaryPts.add(panelBoundaryPos);
              if ("V".equals(values[2])) {
                float vertical = Float.parseFloat(values[3]);
                Float[] panelBoundaryPos2 = new Float[2];
                panelBoundaryPos2[0] = panelBoundaryPos[0];
                panelBoundaryPos2[1] = vertical; // panelBoundaryPos[1] + vertical;
                panelBoundaryPts.add(panelBoundaryPos2);
              }
            } else if (panelType == PanelType.B1 || panelType == panelType.B2) {
              /* d="M 0,495.94772 < bottom right V 737.13091 < top right"
                   d="M -95.19685,495.94772 H 0"
                   d="M -105.82677,737.13091 <bottom left -95.19685,495.94772 < top left"
                   d="M -105.82677,737.13091 H 0"
              */
              // Once we are done, we need to fix up the panelBoundaryPoints order.
              // Ignore anything with H in this scenario.
              if ("H".equals(values[2])) continue;

              String[] pos = values[1].split(",");
              Float[] panelBoundaryPos = new Float[2];
              panelBoundaryPos[0] = Float.parseFloat(pos[0]);
              panelBoundaryPos[1] = Float.parseFloat(pos[1]);
              panelBoundaryPts.add(panelBoundaryPos);
              if ("V".equals(values[2])) {
                float vertical = Float.parseFloat(values[3]);
                Float[] panelBoundaryPos2 = new Float[2];
                panelBoundaryPos2[0] = panelBoundaryPos[0];
                panelBoundaryPos2[1] =  vertical; // panelBoundaryPos[1] +
                panelBoundaryPts.add(panelBoundaryPos2);
              } else if (values[2].contains(",")) {  // M x,y x,y
                String[] pos2 = values[2].split(",");
                Float[] panelBoundaryPos2 = new Float[2];
                panelBoundaryPos2[0] = Float.parseFloat(pos2[0]);
                panelBoundaryPos2[1] = Float.parseFloat(pos2[1]);
                panelBoundaryPts.add(panelBoundaryPos2);
              }
            } else if (panelType == PanelType.C) {
              /* d="m -105.82677,707.5903 v 68.74016"  top left to bottom left
                 d="M -105.82677,776.33046 H 105.82677"
                 d="m 105.82677,707.5903 v 68.74016"  bottom right to top right
                 d="M -105.82677,707.5903 H 105.82677"
              */
              // just skip the 'M' lines.
              if ("M".equals(values[0])) continue;
              String[] pos = values[1].split(",");
              Float[] panelBoundaryPos = new Float[2];
              panelBoundaryPos[0] = Float.parseFloat(pos[0]);
              panelBoundaryPos[1] = Float.parseFloat(pos[1]);
              panelBoundaryPts.add(panelBoundaryPos);
              if ("v".equals(values[2])) {
                float vertical = Float.parseFloat(values[3]);
                Float[] panelBoundaryPos2 = new Float[2];
                panelBoundaryPos2[0] = panelBoundaryPos[0];
                panelBoundaryPos2[1] = panelBoundaryPos[1] + vertical;
                panelBoundaryPts.add(panelBoundaryPos2);
              }
            }  else if (panelType == PanelType.D) {
              /*
                 d="M -142.32283,1172.9299 H 142.32283"
                 d="M 142.32283,1172.9299 105.82677,1043.8156"  bottom right to top right
                 d="M -105.82677,1043.8156 H 105.82677"
                 d="m -142.32283,1172.9299 36.49606,-129.1143" bottom left to top left.
               */
              if ("H".equals(values[2])) continue;
              String[] pos = values[1].split(",");
              Float[] panelBoundaryPos = new Float[2];
              panelBoundaryPos[0] = Float.parseFloat(pos[0]);
              panelBoundaryPos[1] = Float.parseFloat(pos[1]);
              panelBoundaryPts.add(panelBoundaryPos);
              if (values[2].contains(",")) {  // M x,y x,y
                String[] pos2 = values[2].split(",");
                Float[] panelBoundaryPos2 = new Float[2];
                panelBoundaryPos2[0] = Float.parseFloat(pos2[0]);
                panelBoundaryPos2[1] = Float.parseFloat(pos2[1]);
                panelBoundaryPts.add(panelBoundaryPos2);
              }
            } else if (panelType == PanelType.E1 || panelType == PanelType.E2) {
              /* d="M 0,870.70866 V 1122.5197"    top right to bottom right
                 d="M -142.32283,870.70866 H 0"
                 d="M -142.32283,870.70866 V 1122.5197"  top left to bottom left
                 d="M -142.32283,1122.5197 H 0"
              */
              if ("H".equals(values[2])) continue;
              String[] pos = values[1].split(",");
              Float[] panelBoundaryPos = new Float[2];
              panelBoundaryPos[0] = Float.parseFloat(pos[0]);
              panelBoundaryPos[1] = Float.parseFloat(pos[1]);
              panelBoundaryPts.add(panelBoundaryPos);
              if ("V".equals(values[2])) {
                float vertical = Float.parseFloat(values[3]);
                Float[] panelBoundaryPos2 = new Float[2];
                panelBoundaryPos2[0] = panelBoundaryPos[0];
                panelBoundaryPos2[1] = panelBoundaryPos[1] + vertical;
                panelBoundaryPts.add(panelBoundaryPos2);
              }
            }

            // For now, the bottom left point of the cut boundary is just taken as the first path entry.
            // NOTE(tracy): Because of difference in image space coordinates and our world coordinates, the
            // bottom left point right now has maximum Y.  We need to first adjust all our points relative
            // to this coordinate and then we will perform the Y mirror operation to convert from image
            // space coordinates (y increasing down) to world space coordinates (y increasing up).
          }
          if (values.length < 12)  {
            logger.info("line too short, skipping.");
            continue;
          }
          String[] pos = values[1].split(",");
          float xPos = Float.parseFloat(pos[0]);
          float yPos = Float.parseFloat(pos[1]);

          float radius = Float.parseFloat((values[3].split(","))[0]);
          if (radius < 2f) {
            //logger.info("Skipping drill hole at " + xPos + "," + yPos);
            continue;  // If it is a drill hole, skip it.
          }
          //logger.info("Adding point at " + xPos + "," + yPos);
          int xCoord = 0;
          int yCoord = 0;
          // Create and add point.
          // TODO(tracy): compute xCoord,yCoord panel-local grid coordinates for the point.  Unfortunately
          // the points aren't in the SVG file in any particular order.  We need to quantize the points
          // and then sort by X and then Y as a secondary key.  We could also implement a custom comparator
          // with a slop threshold when comparing points so that if abs(pt1.x - pt2.x) < slop then the
          // comparator considers their X values the same.
          points.add(new CXPoint(this, xPos, yPos, 0f, xCoord, yCoord, 0f, 0f));
        }
      }
    } catch (IOException ioex) {
      logger.log(Level.SEVERE, "Unable to read svg layout file: ", ioex);
    } catch ( javax.xml.parsers.ParserConfigurationException pcex) {
      logger.log(Level.SEVERE, "ParserConfigurationException", pcex);
    } catch (org.xml.sax.SAXException sex) {
      logger.log(Level.SEVERE, "SAXException", sex);
    } catch ( javax.xml.xpath.XPathExpressionException xpex) {
      logger.log(Level.SEVERE, " XPathExpressionException: ", xpex);
    }
    // Need to fix up the boundrary points order.
    if (panelType == PanelType.B1 || panelType == PanelType.B2) {
        // d="M 0,495.94772 < top right V 737.13091 < bottom right"
      //                   d="M -95.19685,495.94772 H 0"
      //                   d="M -105.82677,737.13091 <bottom left -95.19685,495.94772 < top left"
      //                   d="M -105.82677,737.13091 H 0"

      Float[] bottomLeft = panelBoundaryPts.get(2);
      Float[] bottomRight = panelBoundaryPts.get(1);
      Float[] topRight = panelBoundaryPts.get(0);
      Float[] topLeft = panelBoundaryPts.get(3);
      panelBoundaryPts.clear();
      panelBoundaryPts.add(bottomLeft);
      panelBoundaryPts.add(bottomRight);
      panelBoundaryPts.add(topRight);
      panelBoundaryPts.add(topLeft);
    } else if (panelType == PanelType.C) {
      /* d="m -105.82677,707.5903 v 68.74016"  top left to bottom left
         d="m 105.82677,707.5903 v 68.74016" top right to bottom right */
      Float[] bottomLeft = panelBoundaryPts.get(1);
      Float[] bottomRight = panelBoundaryPts.get(3);
      Float[] topRight = panelBoundaryPts.get(2);
      Float[] topLeft = panelBoundaryPts.get(0);
      panelBoundaryPts.clear();
      panelBoundaryPts.add(bottomLeft);
      panelBoundaryPts.add(bottomRight);
      panelBoundaryPts.add(topRight);
      panelBoundaryPts.add(topLeft);
    } else if (panelType == PanelType.D) {
      /* d="M 142.32283,1172.9299 105.82677,1043.8156"  bottom right to top right
         d="m -142.32283,1172.9299 36.49606,-129.1143" bottom left to top left. */
      Float[] bottomLeft = panelBoundaryPts.get(2);
      Float[] bottomRight = panelBoundaryPts.get(0);
      Float[] topRight = panelBoundaryPts.get(1);
      Float[] topLeft = panelBoundaryPts.get(3);
      panelBoundaryPts.clear();
      panelBoundaryPts.add(bottomLeft);
      panelBoundaryPts.add(bottomRight);
      panelBoundaryPts.add(topRight);
      panelBoundaryPts.add(topLeft);
    } else if (panelType == PanelType.E1 || panelType == panelType.E2) {
      /* d="M 0,870.70866 V 1122.5197"    top right to bottom right
         d="M -142.32283,870.70866 V 1122.5197"  top left to bottom left */
      Float[] bottomLeft = panelBoundaryPts.get(3);
      bottomLeft[1] -= 900f;
      Float[] bottomRight = panelBoundaryPts.get(1);
      bottomRight[1] -= 900f;
      Float[] topRight = panelBoundaryPts.get(0);
      topRight[1] -= 900f;
      Float[] topLeft = panelBoundaryPts.get(2);
      topLeft[1] -= 900f;
      panelBoundaryPts.clear();
      panelBoundaryPts.add(bottomLeft);
      panelBoundaryPts.add(bottomRight);
      panelBoundaryPts.add(topRight);
      panelBoundaryPts.add(topLeft);
    }

    if (mirror) {
      // We need to translate everybody by the X position of the bottom right coordinate
      // We then swap the bottom right boundary point with the bottom left boundary point
      // And then swap the top right boundary point with the top left boundary point
      // And then we are ready to
      float bottomRightX = panelBoundaryPts.get(1)[0];
      float bottomRightY = panelBoundaryPts.get(1)[1];
      float bottomLeftX = panelBoundaryPts.get(0)[0];
      float bottomLeftY = panelBoundaryPts.get(0)[1];

      if (panelType == PanelType.E2) {
        System.out.println("E2");
      }

      if (panelType == PanelType.A2) {
        System.out.println("A2");
      }

      if (panelType  == PanelType.B2) {
        System.out.println("B2");
      }

      for (CXPoint p : points) {
        p.x = bottomRightX - p.x;
        p.y -= bottomRightY;
        p.y *= -1f;
      }
      panelBoundaryPts.get(1)[0] = bottomRightX - panelBoundaryPts.get(0)[0];
      panelBoundaryPts.get(1)[1] = panelBoundaryPts.get(0)[1];
      panelBoundaryPts.get(0)[0] = 0f;
      panelBoundaryPts.get(0)[1] = bottomRightY;
      float tempX = panelBoundaryPts.get(2)[0];
      float tempY = panelBoundaryPts.get(2)[1];
      panelBoundaryPts.get(2)[0] = bottomRightX - panelBoundaryPts.get(3)[0];
      panelBoundaryPts.get(2)[1] = panelBoundaryPts.get(3)[1];
      panelBoundaryPts.get(3)[0] = bottomRightX - tempX;
      panelBoundaryPts.get(3)[1] = tempY;

    } else {
      // lets adjust points relative to bottom left, which is first panel boundary pt.
      // TODO(tracy): convert from cm to meters.
      logger.info("bottomRightY " + panelBoundaryPts.get(0)[1]);
      float bottomLeftX = panelBoundaryPts.get(0)[0];
      float bottomLeftY = panelBoundaryPts.get(0)[1];
      float bottomRightX = panelBoundaryPts.get(1)[0];
      float bottomRightY = panelBoundaryPts.get(1)[1];
      if (panelType == PanelType.B1)
        System.out.println("B1");
      for (CXPoint p : points) {
        p.x -= panelBoundaryPts.get(0)[0];
        p.y -= panelBoundaryPts.get(0)[1];
        p.y *= -1f;
      }
      // Shift so that bottom right of the panel is at 0 X
      //float bottomRightX = panelBoundaryPts.get(0)[0];
      panelBoundaryPts.get(0)[0] -= bottomLeftX;
      panelBoundaryPts.get(0)[1] -= bottomLeftY;
      panelBoundaryPts.get(1)[0] -= bottomLeftX;
      panelBoundaryPts.get(1)[1] -= bottomLeftY;
      panelBoundaryPts.get(2)[0] -= bottomLeftX;
      panelBoundaryPts.get(2)[1] -= bottomLeftY;
      panelBoundaryPts.get(3)[0] -= bottomLeftX;
      panelBoundaryPts.get(3)[1] -= bottomLeftY;
    }

    // Convert to meters.
    panelBoundaryPts.get(0)[0] *= CNC_SCALE;
    panelBoundaryPts.get(0)[1] *= CNC_SCALE;
    panelBoundaryPts.get(1)[0] *= CNC_SCALE;
    panelBoundaryPts.get(1)[1] *= CNC_SCALE;
    panelBoundaryPts.get(2)[0] *= CNC_SCALE;
    panelBoundaryPts.get(2)[1] *= CNC_SCALE;
    panelBoundaryPts.get(3)[0] *= CNC_SCALE;
    panelBoundaryPts.get(3)[1] *= CNC_SCALE;

    bottomWidth = panelBoundaryPts.get(1)[0] - panelBoundaryPts.get(0)[0];
    topWidth = panelBoundaryPts.get(2)[0] - panelBoundaryPts.get(3)[0];
    height = panelBoundaryPts.get(2)[1] - panelBoundaryPts.get(1)[1];
    logger.info("Detected panel dimensions: (" + bottomWidth + "-" + topWidth + ")x" + height);
    logger.info("bottom right: " + panelBoundaryPts.get(0)[0] + "," + panelBoundaryPts.get(0)[1]);
    return points;
  }

}

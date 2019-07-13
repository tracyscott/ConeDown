package art.lookingup;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import org.jengineering.sjmply.PLY;
import org.jengineering.sjmply.PLYElementList;
import org.jengineering.sjmply.PLYFormat;
import org.jengineering.sjmply.PLYType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

// TODO(tracy): This doesn't really need to extend LXModel anymore.  We can just construct it directly
// with wrapper.  ConeDownModel should not extend LXModel and should encompass the Tori gate dimensions,
// etc.  It should have a method generateLXModel() that generates our points.  Also, there could be
// multiple versions of generateLXModel() that generate multiple light placement strategies but that
// ship has sailed.
public class ConeDownModel extends LXModel {
  private static final Logger logger = Logger.getLogger(ConeDownModel.class.getName());
  public final static int SIZE = 20;

  public static double minX = Float.MAX_VALUE;
  public static double minY = Float.MAX_VALUE;
  public static double maxX = Float.MIN_VALUE;
  public static double maxY = Float.MIN_VALUE;

  public static double minConeX = Float.MAX_VALUE;
  public static double minConeY = Float.MAX_VALUE;
  public static double maxConeX = Float.MIN_VALUE;
  public static double maxConeY = Float.MIN_VALUE;

  public static double minScoopX = Float.MAX_VALUE;
  public static double minScoopY = Float.MAX_VALUE;
  public static double maxScoopX = Float.MIN_VALUE;
  public static double maxScoopY = Float.MIN_VALUE;

  public static float inchesPerMeter = 39.3701f;
  static public float panelMargin = 2.0f / inchesPerMeter;
  static public float panel8Radius = 9.0f * 12.0f / inchesPerMeter;
  static public float panel7Radius = 8.0f * 12.0f / inchesPerMeter;
  static public float panel6Radius = 7.5f * 12.0f / inchesPerMeter;
  static public float panel5Radius = 7.0f * 12.0f / inchesPerMeter;
  static public float panel4Radius = 6.5f * 12.0f / inchesPerMeter;
  static public float panel3Radius = 6.0f * 12.0f / inchesPerMeter;
  static public float panel2Radius = 5.5f * 12.0f / inchesPerMeter;
  static public float panel1Radius = 5.0f * 12.0f / inchesPerMeter;

  static public float pitch = 6.0f; // Pitch in Inches
  static public float coneTilt = -15.0f; // degrees around Z axis
  static public float scoopSides = 16;
  static public float coneSides = 8;
  static public float scoopAngleIncrement = 360f / scoopSides;
  static public float coneAngleIncrement = 360f / coneSides;

  static public float VerticalAngleIncrement = 30f;  // Pick something random for now.
  static public float XAxisOffset = 2.5f;
  static public float YAxisOffset = 1.0f;

  // Dance floor
  static public int dancePanelsWide = 4;
  static public int dancePanelsHigh = 4;
  static public float panel9Width = (3.0f * 12.0f) / inchesPerMeter;
  static public float panel9Height = (3.0f * 12.0f) / inchesPerMeter;

  static public float panel8Width = (3.0f * 12.0f + 8.125f) / inchesPerMeter;
  static public float panel8Height = panel8Width;
  static public float panel7Width = (3.0f * 12.0f + 4.765625f) / inchesPerMeter;
  static public float panel7Height = 9.96875f / inchesPerMeter;
  static public float panel6Width = (6.0f *12.0f + 3.34375f) / inchesPerMeter;
  static public float panel6Height = 7.0f / inchesPerMeter;
  static public float panel5Width = (6.0f * 12.0f + 3.375f) / inchesPerMeter;
  static public float panel5Height = (4.0f * 12.0f + 5.609375f) / inchesPerMeter;
  static public float panel4Width = (6.0f * 12.0f + 3.34375f) / inchesPerMeter;
  static public float panel4TopWidth = (4.0f * 12.0f + 7.34375f) / inchesPerMeter;
  static public float panel4Height = (2.0f * 12.0f + 9f + 5f/16f) / inchesPerMeter;
  static public float panel3Width = (4.0f * 12.0f + 7.640625f) / inchesPerMeter;
  static public float panel3Height = (1.0f * 12.0f + 7.59375f) / inchesPerMeter;
  static public float panel2Width = (4.0f * 12.0f + 7.640625f) / inchesPerMeter;
  static public float panel2Height = (5.0f * 12.0f + 5.328125f) / inchesPerMeter;
  static public float panel1Width = (4.0f * 12.0f + 2.3125f) / inchesPerMeter;
  static public float panel1Height = (7.0f * 12.0f + 0.578125f) / inchesPerMeter;


  static public int panelsPerScoopLayer = 16;
  static public int numPanel8Layers = 2;
  static public int numPanel7Layer = 1;

  public static double computedWidth = 1f;
  public static double computedHeight= 1f;
  public static double computedConeWidth = 1f;
  public static double computedConeHeight = 1f;
  public static double computedScoopWidth = 1f;
  public static double computedScoopHeight = 1f;

  public static double rowIncrementLength;
  public static double colIncrementLength;
  public static double rowConeIncrLength;
  public static double colConeIncrLength;
  public static double rowScoopIncrLength;
  public static double colScoopIncrLength;

  public static List<LXPoint> conePoints = new ArrayList<LXPoint>();
  public static List<LXPoint> scoopPoints = new ArrayList<LXPoint>();
  public static List<LXPoint> dancePoints = new ArrayList<LXPoint>();
  public static List<Panel> scoopPanels = new ArrayList<Panel>();
  public static List<Panel> conePanels = new ArrayList<Panel>();
  public static List<Panel> dancePanels = new ArrayList<Panel>();


  static public class Panel {
    public enum PanelType {
      SCOOP,
      CONE,
      DANCEFLOOR,
    };

    public PanelType panelType;
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

    List<LXPoint> points;

      public Panel(float topWidth, float bottomWidth, float height, float pitch,
                        float xPos, float yPos, float zPos, int panelNum, int yCoordOffset, float radius,
                   boolean scoop) {
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
        this.scoop = scoop;
        if (scoop) {
          panelType = PanelType.SCOOP;
        } else {
          panelType = PanelType.CONE;
        }
        float pitchInMeters = pitch / inchesPerMeter;
        points = new ArrayList<LXPoint>();

        // Create LXPoints based on initial x,y,z and width and height and pitch
        float angleIncr = (scoop)?scoopAngleIncrement:coneAngleIncrement;

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
        for (float y = panelMargin; y < this.height - panelMargin; y+= pitchInMeters) {
          pointsWide = 0;
          xCoord = 0;
          for (float x = panelMargin; x < this.topWidth - panelMargin; x += pitchInMeters)
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
     * width dimension (they are square), width, and pitch.  For simplifying the pixel to
     * image texture mapping.  Panels are ordered row by row.  Since we are on the back of the
     * installation, the first panel will have a larger X value and the fourth panel in a row will have
     * a smaller (negative) X value.  They are centered around 0 on the X Axis.  They y coord in 3D space
     * will always be zero.
     * @param danceXPanel Dance panel number in X dimension.
     * @param danceYPanel Dance panel number in Y dimension.
     */
    public Panel(int danceXPanel, int danceYPanel, float width, float height, float pitch) {
      // The dance floor is on the back of the cone, so the angle = 180 degrees.  For now, we will just
      // throw it down somewhere behind the cone, centered on 0 where the the minimum Z direction is the
      // radius of the cone plus some offset.
      float xOffset = - dancePanelsHigh * height;
      // X,Y Panels are
      this.panelType = PanelType.DANCEFLOOR;
      this.topWidth = width;
      this.bottomWidth = width;
      this.height = height;
      this.pitch = pitch;
      this.danceXPanel = danceXPanel;
      this.danceYPanel = danceYPanel;

      this.panelNum = danceYPanel * dancePanelsWide + danceXPanel;
      this.radius = 0f;
      this.scoop = false;
      float pitchInMeters = pitch / inchesPerMeter;


      points = new ArrayList<LXPoint>();

      // The panels are centered around Z=0.  So the first panel starts at X=total width of all panels/2.
      // And decrements by width with each panel.
      float panelXStart = -height * dancePanelsHigh + height * danceYPanel; // The panels are centered around 0.
      float panelZStart = (dancePanelsWide * width)/2f - (danceXPanel * width); //
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
      for (float y = panelMargin; y < this.height - panelMargin; y+= pitchInMeters) {
        pointsWide = 0;
        xCoord = 0;
        float percentYDone = y / height;
        for (float x = panelMargin; x < width - panelMargin; x += pitchInMeters)
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
      float angleIncr = (scoop)?scoopAngleIncrement:coneAngleIncrement;
      return panelNum * angleIncr;
    }

      public List<LXPoint> getPoints() {
        return points;
      }
  }


  public static ConeDownModel createModel() {
    List<LXPoint> allPoints = new ArrayList<LXPoint>();
    // Create panels, add points from each panel to allPoints.
    float xOffset = 0f;
    float yOffset = 0f;
    float zOffset = 0f;
    int layerWidth = 0;
    int layerHeight = 0;
    int yCoordOffset = 0;

    List<String> layerDimensions = new ArrayList<String>();

    for (int danceYPanel = 0; danceYPanel < dancePanelsHigh; danceYPanel++) {
      layerWidth = 0;
      for (int danceXPanel = 0; danceXPanel < dancePanelsWide; danceXPanel++) {
        Panel panel = new Panel(danceXPanel, danceYPanel, panel9Width, panel9Height, pitch);
        dancePanels.add(panel);
        allPoints.addAll(panel.getPoints());
        dancePoints.addAll(panel.getPoints());
        System.out.println("Adding dancefloor points: " + panel.getPoints().size());
        System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
        layerHeight = panel.pointsHigh;
        layerWidth += panel.pointsWide;
      }
      // We don't use the yCoordOffset for dance panels because we can just compute the X,Y texture image
      // coordinates easily enough since we are on the bottom of the image.  We build up this number though
      // so that the scoop and cone sections can properly index themselves into the texture map when operating
      // in a "pixel perfect" like fashion (versus some projection mapping from the actual pixels positions in
      // 3D space).
      yCoordOffset += layerHeight;
      layerDimensions.add("" + layerWidth + "x" + layerHeight);
    }

    for (int rows = 0; rows < numPanel8Layers; rows++) {
      layerWidth = 0;
      for (int panelNum = 0; panelNum < scoopSides; panelNum++) {
        Panel panel = new Panel(panel8Width, panel8Width, panel8Height, pitch, xOffset, yOffset, zOffset, panelNum,
            yCoordOffset,
            panel8Radius, true);
        scoopPanels.add(panel);
        xOffset += panel8Width;
        allPoints.addAll(panel.getPoints());
        scoopPoints.addAll(panel.getPoints());
        System.out.println("Adding " + panel.getPoints().size() + " points");
        System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
        layerWidth += panel.pointsWide;
        layerHeight = panel.pointsHigh;
      }
      yCoordOffset += layerHeight;
      yOffset += panel8Height;
      System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
      layerDimensions.add("" + layerWidth + "x" + layerHeight);
      xOffset = 0f;
    }


    layerWidth = 0;
    for (int panelNum = 0; panelNum <scoopSides; panelNum++) {
      Panel panel = new Panel(panel7Width, panel7Width, panel7Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel7Radius, true);
      xOffset += panel7Width;
      allPoints.addAll(panel.getPoints());
      scoopPoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel7Height;

    layerWidth = 0;
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(panel6Width, panel6Width, panel6Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel6Radius, false);
      xOffset += panel6Width;
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel6Height;

    layerWidth = 0;
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(panel5Width, panel5Width, panel5Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel5Radius, false);
      xOffset += panel5Width;
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel5Height;

    layerWidth = 0;
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(panel4Width, panel4Width, panel4Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel4Radius, false);
      xOffset += panel4Width;
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel4Height;

    layerWidth = 0;
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(panel3Width, panel3Width, panel3Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel3Radius, false);
      xOffset += panel3Width;
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel3Height;

    layerWidth = 0;
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(panel2Width, panel2Width, panel2Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel2Radius, false);
      xOffset += panel2Width;
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel2Height;

    layerWidth = 0;
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(panel1Width, panel1Width, panel1Height, pitch, xOffset, yOffset, zOffset, panelNum,
          yCoordOffset, panel1Radius, false);
      xOffset += panel1Width;
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      System.out.println("Adding " + panel.getPoints().size() + " points");
      System.out.println("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    yCoordOffset += layerHeight;
    System.out.println("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel1Height;

    System.out.println("All Layer Dimensions:");
    for (String dim : layerDimensions) {
      System.out.println(dim);
    }

    return new ConeDownModel(allPoints);
  }

  public ConeDownModel(List<LXPoint> points) {
    super(points);
    // Compute some stats on our points.
    int pointCount = 0;
    for (LXPoint p : points) {
      if (p.x < minX) minX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.x > maxX) maxX = p.x;
      if (p.y > maxY) maxY = p.y;
      pointCount++;
    }
    for (LXPoint p : conePoints) {
      if (p.x < minConeX) minConeX = p.x;
      if (p.y < minConeY) minConeY = p.y;
      if (p.x > maxConeX) maxConeX = p.x;
      if (p.x > maxConeY) maxConeY = p.y;
    }
    for (LXPoint p : scoopPoints) {
      if (p.x < minScoopX) minScoopX = p.x;
      if (p.y < minScoopY) minScoopY = p.y;
      if (p.x > maxScoopX) maxScoopX = p.x;
      if (p.x > maxScoopY) maxScoopY = p.y;
    }

    System.out.println("Total points: " + pointCount);

    computedWidth = maxX - minX;
    computedHeight = maxY - minY;
    computedConeWidth = maxConeX - minConeX;
    computedConeHeight = maxConeY - minConeY;
    computedScoopWidth = maxScoopX - minScoopX;
    computedScoopHeight = maxScoopY - minScoopY;

    colIncrementLength = computedWidth / (POINTS_WIDE - 1);
    rowIncrementLength = computedHeight  / (POINTS_HIGH - 1);
    colConeIncrLength = computedConeWidth / (POINTS_WIDE - 1);
    rowConeIncrLength = computedConeHeight / (POINTS_HIGH - 1);
    colScoopIncrLength = computedScoopWidth / (POINTS_WIDE - 1);
    rowScoopIncrLength = computedScoopHeight / (POINTS_HIGH - 1);

    exportPLY(points);
    exportPanelSVG();
  }

  public static void exportPanelSVG() {
    Panel p = scoopPanels.get(0);
    File file = new File("panel.svg");
    //file.getParentFile().mkdirs();

    /*
    <?xml version="1.0" encoding="UTF-8" ?>
<svg width="391" height="391" viewBox="-70.5 -70.5 391 391" xmlns="http://www.w3.org/2000/svg">
  <rect x="25" y="25" width="200" height="200" fill="lime" stroke-width="4" stroke="pink" />
  <circle cx="125" cy="125" r="75" fill="orange" />
  <polyline points="50,150 50,200 200,200 200,100" stroke="red" stroke-width="4" fill="none" />
  <line x1="50" y1="50" x2="200" y2="200" stroke="blue" stroke-width="4" />
</svg>
     */
    float svgWidth = p.topWidth * 1000f;
    float svgHeight = p.height * 1000f;
    float xPos = 0.0f;
    float yPos = 0.0f;
    float panelWidth = p.topWidth * 1000f;
    float panelHeight = p.height * 1000f;
    // viewBox="-70.5 -70.5 391 391"
    try {
      PrintWriter printWriter = new PrintWriter(file);
      printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
      printWriter.println("<svg width=\"" + svgWidth + "\" height=\"" + svgHeight + "\"  xmlns=\"http://www.w3.org/2000/svg\">");
      printWriter.println("  <rect x=\"" + xPos + "\" y=\"" + yPos + "\" width=\"" + panelWidth + "\" height=\"" + panelHeight + "\" fill=\"white\" stroke-width=\"4\" stroke=\"black\" />");
      for (LXPoint pt : p.points) {
        printWriter.println("<circle cx=\"" + (pt.x - p.xPos)*1000f + "\" cy=\"" + (pt.y - p.yPos) * 1000f + "\" r=\"6\" stroke-width=\"1\" stroke=\"black\" fill=\"none\" />");
      }
      printWriter.println("</svg>");
      printWriter.close();
    } catch (IOException ioex) {
      logger.log(Level.SEVERE, "Unable to export SVG: " + ioex.getMessage());
    }

  }

  public static void exportPLY(List<LXPoint> points) {
    PLY plyOut = new PLY(PLYFormat.BINARY_LITTLE_ENDIAN, "1.0");
    PLYElementList plyPoints = new PLYElementList(points.size());
    plyOut.elements.put("vertex", plyPoints);
    plyPoints.addProperty(PLYType.FLOAT32, "x");
    float[] xCoords = plyPoints.property(PLYType.FLOAT32,"x");
    plyPoints.addProperty(PLYType.FLOAT32, "y");
    float[] yCoords = plyPoints.property(PLYType.FLOAT32, "y");
    plyPoints.addProperty(PLYType.FLOAT32, "z");
    float[] zCoords = plyPoints.property(PLYType.FLOAT32, "z");
    plyPoints.addProperty(PLYType.FLOAT32, "nx");
    float[] nx = plyPoints.property(PLYType.FLOAT32, "nx");
    plyPoints.addProperty(PLYType.FLOAT32, "ny");
    float[] ny = plyPoints.property(PLYType.FLOAT32, "ny");
    plyPoints.addProperty(PLYType.FLOAT32, "nz");
    float[] nz = plyPoints.property(PLYType.FLOAT32, "nz");
    plyPoints.addProperty(PLYType.UINT8, "red");
    byte[] redValues = plyPoints.property(PLYType.UINT8, "red");
    plyPoints.addProperty(PLYType.UINT8, "green");
    byte[] greenValues = plyPoints.property(PLYType.UINT8, "green");
    plyPoints.addProperty(PLYType.UINT8, "blue");
    byte[] blueValues = plyPoints.property(PLYType.UINT8, "blue");
    plyPoints.addProperty(PLYType.UINT8, "alpha");
    byte[] alphaValues = plyPoints.property(PLYType.UINT8, "alpha");

    int i = 0;
    for (LXPoint p : points) {
      xCoords[i] = p.x;
      yCoords[i] = p.y;
      zCoords[i] = p.z;
      nx[i] = 0f;
      ny[i] = 0f;
      nz[i] = 0f;
      redValues[i] = 127;
      greenValues[i] = 127;
      blueValues[i] = 127;
      alphaValues[i] = 127;
      i++;
    }
    Path out = Paths.get("lxpoints.ply");
    try {
      plyOut.save(out);
    } catch (IOException ioex) {
      logger.log(Level.SEVERE, ioex.getMessage());
    }
  }

  // Project the point into the image.  This currently just projects onto the
  // X,Y plane.
  public static int[] pointToImageCoordinates(LXPoint p) {
    int[] coordinates = {0, 0};
    double offsetX = p.x - minX;
    double offsetY = p.y - minY;
    int columnNumber = (int)Math.round(offsetX / colIncrementLength);
    int rowNumber = (int)Math.round(offsetY  / rowIncrementLength);
    coordinates[0] = columnNumber;
    // Transpose for Processing Image coordinates, otherwise images are upside down.
    coordinates[1] = (POINTS_HIGH-1)-rowNumber;
    //System.out.println ("x,y " + coordinates[0] + "," + coordinates[1]);
    return coordinates;
  }

  public static int[] pointToImgCoordsCylinder(CXPoint p) {
    int[] coordinates = {0, 0};
    float xScale = POINTS_WIDE / ((float)p.panel.pointsWide * ((p.panel.scoop)?scoopSides:coneSides));
    //xScale = 1f / xScale;
    int yCoord = p.panel.yCoordOffset + p.yCoord;
    int xCoord = (int)((float)(p.panel.panelNum * p.panel.pointsWide + p.xCoord) * xScale);
    if (p.panel.panelType == Panel.PanelType.DANCEFLOOR) {
      int danceFloorPointsWide = p.panel.pointsWide * ConeDownModel.dancePanelsWide;
      int totalImgPointsWide = ConeDownModel.POINTS_WIDE;
      int imgXOffset = totalImgPointsWide/2 - danceFloorPointsWide/2;
      int xImgCoord = imgXOffset + p.panel.danceXPanel * p.panel.pointsWide + p.xCoord;
      xCoord = xImgCoord;

    }
    coordinates[0] = xCoord;

    coordinates[1] = (POINTS_HIGH-1) - yCoord;
    return coordinates;
  }

  // TODO(tracy): This doesn't make much sense for ConeDown.  The equivalent should
  // be a mapping to a cylinder.
  public static int[] pointToImageCoordinatesWide(LXPoint p) {
    int[] coordinates = {0, 0};
    double offsetX = p.x - minX;
    double offsetY = p.y - minY;
    int columnNumber = (int)Math.round(offsetX / colIncrementLength);
    int rowNumber = (int)Math.round(offsetY  / rowIncrementLength);
    coordinates[0] = columnNumber;
    // Transpose for Processing Image coordinates, otherwise images are upside down.
    coordinates[1] = (POINTS_HIGH-1)-rowNumber;
    // Allow for wide images.
    if (p.z > 9.9 && p.z < 10.1) {
      coordinates[0] = 46 + (POINTS_WIDE-1) - coordinates[0];
    }
    //System.out.println ("x,y " + coordinates[0] + "," + coordinates[1]);
    return coordinates;
  }

  public static final int POINTS_WIDE = 112;
  public static final int POINTS_HIGH = 82;
}

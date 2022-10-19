package art.lookingup;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jengineering.sjmply.PLY;
import org.jengineering.sjmply.PLYElementList;
import org.jengineering.sjmply.PLYFormat;
import org.jengineering.sjmply.PLYType;

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
  static public float panel8Radius = 9.7f * 12.0f / inchesPerMeter;
  static public float panel7Radius =  9.0f * 12.0f / inchesPerMeter;
  // panel 6 will be disabled.
  static public float panel6Radius = 2.0f * 8.7f * 12.0f / inchesPerMeter;
  static public float panel5Radius =  8.7f * 12.0f / inchesPerMeter;
  static public float panel4Radius = 8f * 12.0f / inchesPerMeter;
  static public float panel3Radius = 6.1f * 12.0f / inchesPerMeter;
  static public float panel2Radius = 6.1f * 12.0f / inchesPerMeter;
  static public float panel1Radius = 5.4f * 12.0f / inchesPerMeter;

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
  static public int dancePanelsWide = 3;
  static public int dancePanelsHigh = 3;
  static public float panel9Width = (4.0f * 12.0f) / inchesPerMeter;
  static public float panel9Height = (4.0f * 12.0f) / inchesPerMeter;

  static public float panel8Width = (3.0f * 12.0f + 8.125f) / inchesPerMeter;
  static public float panel8Height = panel8Width;
  static public float panel7Width = (3.0f * 12.0f + 4.765625f) / inchesPerMeter;
  static public float panel7Height = 9.96875f / inchesPerMeter;
  static public float panel6Width = (6.0f *12.0f + 3.34375f) / inchesPerMeter;
  static public float panel6Height = 7.0f / inchesPerMeter;
  static public float panel5Width = (6.0f * 12.0f + 3.375f) / inchesPerMeter;
  static public float panel5Height = (5.5f * 12.0f + 5.609375f) / inchesPerMeter; // was 4.0f
  static public float panel4Width = (6.0f * 12.0f + 3.34375f) / inchesPerMeter;
  static public float panel4TopWidth = (4.0f * 12.0f + 7.34375f) / inchesPerMeter;
  // Adjust for rotation 0.55f fudge factor right now.
  static public float panel4Height = 0.8f * (2.0f * 12.0f + 9f + 5f/16f) / inchesPerMeter;
  static public float panel3Width = (4.0f * 12.0f + 7.640625f) / inchesPerMeter;
  static public float panel3Height = (1.0f * 12.0f + 7.59375f) / inchesPerMeter;
  static public float panel2Width = (4.0f * 12.0f + 7.640625f) / inchesPerMeter;
  static public float panel2Height = (5.0f * 12.0f + 5.328125f) / inchesPerMeter;
  // Panel A is half of 1
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
  public static List<CXPoint> interiorPoints = new ArrayList<CXPoint>();

  public static List<Panel> scoopPanels = new ArrayList<Panel>();
  public static List<Panel> conePanels = new ArrayList<Panel>();
  public static List<Panel> dancePanels = new ArrayList<Panel>();
  public static List<Panel> allPanels = new ArrayList<Panel>();

  public static List<List<CXPoint>> coneFloodsByRing = new ArrayList<List<CXPoint>>();
  public static List<CXPoint> allConeFloods = new ArrayList<CXPoint>();
  public static List<CXPoint> insideUpperFloods = new ArrayList<CXPoint>();
  public static List<CXPoint> outsideUpperFloods = new ArrayList<CXPoint>();
  public static List<CXPoint> insideLowerFloods = new ArrayList<CXPoint>();
  public static List<CXPoint> outsideLowerFloods = new ArrayList<CXPoint>();

  static public int dancePointsWide = 0;
  static public int dancePointsHigh = 0;
  static public int scoopPointsWide = 0;
  static public int scoopPointsHigh = 0;
  static public int conePointsWide = 0;
  static public int conePointsHigh = 0;

  static public int numLayers = 8;
  static public List<List<Panel>> panelLayers = new ArrayList<List<Panel>>(numLayers);

  public static ConeDownModel createModel() {
    List<LXPoint> allPoints = new ArrayList<LXPoint>();
    // Create panels, add points from each panel to allPoints.
    float yOffset = -0.5f;
    int layerWidth = 0;
    int layerHeight = 0;
    int yCoordOffset = 0;

    List<String> layerDimensions = new ArrayList<String>();


    //
    //
    //    D A N C E    P A N E L S
    //
    //
    for (int danceYPanel = 0; danceYPanel < dancePanelsHigh; danceYPanel++) {
      layerWidth = 0;
      List<Panel> danceLayer = new ArrayList<Panel>();
      for (int danceXPanel = 0; danceXPanel < dancePanelsWide; danceXPanel++) {
        Panel panel = new Panel(danceXPanel, danceYPanel, panel9Width, panel9Height, pitch, 9);
        dancePanels.add(panel);
        danceLayer.add(panel);
        allPoints.addAll(panel.getPoints());
        dancePoints.addAll(panel.getPoints());
        logger.info("Adding dancefloor points: " + panel.getPoints().size());
        logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
        layerHeight = panel.pointsHigh;
        layerWidth += panel.pointsWide;
      }
      panelLayers.add(danceLayer);
      // We don't use the yCoordOffset for dance panels because we can just compute the X,Y texture image
      // coordinates easily enough since we are on the bottom of the image.  We build up this number though
      // so that the scoop and cone sections can properly index themselves into the texture map when operating
      // in a "pixel perfect" like fashion (versus some projection mapping from the actual pixels positions in
      // 3D space).
      dancePointsHigh += layerHeight;
      dancePointsWide = layerWidth;
      yCoordOffset += layerHeight;
      layerDimensions.add("" + layerWidth + "x" + layerHeight);
    }
    allPanels.addAll(dancePanels);


    //
    //
    //   S C O O P    P A N E L S
    //
    //

    List<Panel> scoopLayer = new ArrayList<Panel>();

    // Partial I panels
    layerWidth = 0;
    float panelHeight = 0f;
    float maxPanelHeight = 0f;
    for (int panelNum = 0; panelNum <scoopSides; panelNum++) {
      if (panelNum <= 4 || panelNum >= 11) {
        Panel panel = new Panel(Panel.PanelType.I, yOffset, panelNum, yCoordOffset, panel8Radius - 27.0f / inchesPerMeter);
        scoopPanels.add(panel);
        scoopLayer.add(panel);

        allPoints.addAll(panel.getPoints());
        scoopPoints.addAll(panel.getPoints());
        logger.info("Adding " + panel.getPoints().size() + " points");
        logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
        layerWidth += panel.pointsWide;
        if (panel.height > maxPanelHeight) {
          maxPanelHeight = panel.height;
        } else {
          panel.increaseYPos(maxPanelHeight - panel.height);
        }
        if (panelNum == 1)  {
          // This the only panel with a full set of points so use it's points high to keep track of texture
          // coordinates.
          layerHeight = panel.pointsHigh;
          // set the world y offset position for the next layer of H panels.
          panelHeight = panel.height;
        }
      }
    }
    panelLayers.add(scoopLayer);
    yCoordOffset += layerHeight;
    yOffset += panelHeight;
    scoopPointsHigh += layerHeight;

    for (int rows = 0; rows < numPanel8Layers; rows++) {
      layerWidth = 0;
      scoopLayer = new ArrayList<Panel>();
      float radius = panel8Radius;
      if (rows == 0) {
        radius = panel8Radius - 5.0f / inchesPerMeter;
      }

      maxPanelHeight = 0f;
      for (int panelNum = 0; panelNum < scoopSides; panelNum++) {
        Panel panel = new Panel((rows==0)? Panel.PanelType.H: Panel.PanelType.G, yOffset, panelNum, yCoordOffset, radius);
        if (panel.panelType == Panel.PanelType.G && (panelNum == 9 || panelNum == 10))
          panel.logBoundary();
        if (panel.panelType == Panel.PanelType.G && (panelNum == 11)) {
          panel.logBoundary();
        }
        // Adjust base height for ground intercepted panels.
        if (panel.height >= maxPanelHeight && panel.panelType == Panel.PanelType.H) {
          maxPanelHeight = panel.height;
        } else if (panel.panelType == Panel.PanelType.H && panel.height < maxPanelHeight) {
          panel.increaseYPos(maxPanelHeight - panel.height);
        }
        scoopPanels.add(panel);
        scoopLayer.add(panel);
        allPoints.addAll(panel.getPoints());
        scoopPoints.addAll(panel.getPoints());
        logger.info("Adding " + panel.getPoints().size() + " points");
        logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
        layerWidth += panel.pointsWide;
        layerHeight = panel.pointsHigh;
      }
      panelLayers.add(scoopLayer);
      if (layerWidth > scoopPointsWide) {
        scoopPointsWide = layerWidth;
      }
      scoopPointsHigh += layerHeight;
      yCoordOffset += layerHeight;
      yOffset += panel8Height;
      logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
      layerDimensions.add("" + layerWidth + "x" + layerHeight);
    }


    layerWidth = 0;
    scoopLayer = new ArrayList<Panel>();
    for (int panelNum = 0; panelNum <scoopSides; panelNum++) {
      Panel panel = new Panel(Panel.PanelType.F, yOffset, panelNum, yCoordOffset, panel7Radius);
      panel.panelType = Panel.PanelType.F;
      scoopPanels.add(panel);
      scoopLayer.add(panel);
      allPoints.addAll(panel.getPoints());
      scoopPoints.addAll(panel.getPoints());
      logger.info("Adding " + panel.getPoints().size() + " points");
      logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    panelLayers.add(scoopLayer);
    /* NOTE(tracy): We don't want this 8x2 i.e. 128x2 layer to drive our render target width since it
     * would cause aliasing in the big panels.  So the smaller width of the bigger panels is used to
     * minimize aliasing.

    if (layerWidth > scoopPointsWide) {
      scoopPointsWide = layerWidth;
    }
    */
    scoopPointsHigh += layerHeight;
    yCoordOffset += layerHeight;
    logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel7Height;


    allPanels.addAll(scoopPanels);

    float topOfScoop = yOffset;

    //
    //
    //   C O N E    P A N E L S
    //
    //

    //
    // PANEL E
    //
    layerWidth = 0;
    List<Panel> coneLayer = new ArrayList<Panel>();
    for (int panelNum = 0; panelNum < Panel.numPanelsAround[Panel.PanelType.E1.ordinal()]; panelNum++) {
      Panel panel = new Panel((panelNum%2==0)? Panel.PanelType.E1: Panel.PanelType.E2, yOffset, panelNum, yCoordOffset, panel5Radius);
      conePanels.add(panel);
      coneLayer.add(panel);
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      logger.info("Adding " + panel.getPoints().size() + " points");
      logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    panelLayers.add(coneLayer);
    if (layerWidth > conePointsWide) {
      conePointsWide = layerWidth;
    }
    conePointsHigh  += layerHeight;
    yCoordOffset += layerHeight;
    logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel5Height;

    //
    // PANEL D
    //
    layerWidth = 0;
    coneLayer = new ArrayList<Panel>();
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(Panel.PanelType.D, yOffset, panelNum, yCoordOffset, panel4Radius);
      conePanels.add(panel);
      coneLayer.add(panel);
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      logger.info("Adding " + panel.getPoints().size() + " points");
      logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    panelLayers.add(coneLayer);
    if (layerWidth > conePointsWide) {
      conePointsWide = layerWidth;
    }
    conePointsHigh += layerHeight;
    yCoordOffset += layerHeight;
    logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel4Height;

    //
    // PANEL C
    //
    layerWidth = 0;
    coneLayer = new ArrayList<Panel>();
    for (int panelNum = 0; panelNum < coneSides; panelNum++) {
      Panel panel = new Panel(Panel.PanelType.C, yOffset, panelNum, yCoordOffset, panel3Radius);
      conePanels.add(panel);
      coneLayer.add(panel);
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      logger.info("Adding " + panel.getPoints().size() + " points");
      logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    panelLayers.add(coneLayer);
    if (layerWidth > conePointsWide) {
      conePointsWide = layerWidth;
    }
    conePointsHigh += layerHeight;
    yCoordOffset += layerHeight;
    logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel3Height;

    //
    // PANEL B
    //
    layerWidth = 0;
    coneLayer = new ArrayList<Panel>();
    for (int panelNum = 0; panelNum < Panel.numPanelsAround[1]; panelNum++) {
      Panel panel = new Panel((panelNum%2==0)? Panel.PanelType.B1: Panel.PanelType.B2, yOffset, panelNum,
          yCoordOffset, panel2Radius);
      conePanels.add(panel);
      coneLayer.add(panel);
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      logger.info("Adding " + panel.getPoints().size() + " points");
      logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    panelLayers.add(coneLayer);
    if (layerWidth > conePointsWide) {
      conePointsWide = layerWidth;
    }
    conePointsHigh += layerHeight;
    yCoordOffset += layerHeight;
    logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel2Height;

    //
    // PANEL A
    //
    layerWidth = 0;
    coneLayer = new ArrayList<Panel>();
    for (int panelNum = 0; panelNum < Panel.numPanelsAround[0]; panelNum++) {
      Panel panel = new Panel((panelNum%2==0)? Panel.PanelType.A1: Panel.PanelType.A2, yOffset, panelNum,
      yCoordOffset, panel1Radius);
      exportPanelPoints(panel);
      conePanels.add(panel);
      coneLayer.add(panel);
      allPoints.addAll(panel.getPoints());
      conePoints.addAll(panel.getPoints());
      logger.info("Adding " + panel.getPoints().size() + " points");
      logger.info("Panel dimensions: " + panel.pointsWide + "x" + panel.pointsHigh);
      layerWidth += panel.pointsWide;
      layerHeight = panel.pointsHigh;
    }
    panelLayers.add(coneLayer);
    if (layerWidth > conePointsWide) {
      conePointsWide = layerWidth;
    }
    conePointsHigh += layerHeight;
    yCoordOffset += layerHeight;
    logger.info("Layer dimensions: " + layerWidth + "x" + layerHeight);
    layerDimensions.add("" + layerWidth + "x" + layerHeight);
    yOffset += panel1Height;

    float topOfCone = yOffset;

    allPanels.addAll(conePanels);

    POINTS_WIDE = scoopPointsWide; // Could compute this by maxing everything but we know it is the most.
    POINTS_HIGH = dancePointsHigh + scoopPointsHigh + conePointsHigh;

    logger.info("Computed POINTS_WIDExPOINTS_HIGH: " + POINTS_WIDE + "x" + POINTS_HIGH);

    // Flood lights
    float insideUpperFloodsRadius = panel1Radius - 6.0f / inchesPerMeter;
    float upperFloodsHeight = topOfCone;
    int lightNum = 0;

    for (float angle = 0f; angle < 360; angle += 360f/8f) {
      //CXPoint p = new CXPoint(null, insideUpperFloodsRadius * Math.cos(Math.toRadians(angle)), upperFloodsHeight,
      //    insideUpperFloodsRadius * Math.sin(Math.toRadians(angle)), lightNum, 0, angle, insideUpperFloodsRadius);
      //insideUpperFloods.add(p);
      lightNum++;
    }
    coneFloodsByRing.add(insideUpperFloods);
    allConeFloods.addAll(insideUpperFloods);
    allPoints.addAll(insideUpperFloods);

    float outsideUpperFloodsRadius = panel1Radius + 12.0f / inchesPerMeter;
    lightNum = 0;
    for (float angle = 360f/32f; angle < 360; angle += 360f/16f) {
      CXPoint p = new CXPoint(null, outsideUpperFloodsRadius * Math.cos(Math.toRadians(angle)), upperFloodsHeight,
          outsideUpperFloodsRadius * Math.sin(Math.toRadians(angle)), lightNum, 0, angle, outsideUpperFloodsRadius);
      outsideUpperFloods.add(p);
      lightNum++;
    }
    coneFloodsByRing.add(outsideUpperFloods);
    allConeFloods.addAll(outsideUpperFloods);
    allPoints.addAll(outsideUpperFloods);

    float insideLowerFloodsRadius = panel5Radius;
    float lowerFloodsHeight = topOfScoop + 6f / inchesPerMeter;
    lightNum = 0;
    for (float angle = 360f/32f; angle < 360; angle += 360f/16f) {
      //CXPoint p = new CXPoint(null, insideLowerFloodsRadius * Math.cos(Math.toRadians(angle)), lowerFloodsHeight,
      //    insideLowerFloodsRadius * Math.sin(Math.toRadians(angle)), lightNum, 0, angle, insideLowerFloodsRadius);
      //insideLowerFloods.add(p);
      lightNum++;
    }
    coneFloodsByRing.add(insideLowerFloods);
    allConeFloods.addAll(insideLowerFloods);
    allPoints.addAll(insideLowerFloods);

    float outsideLowerFloodsRadius = panel5Radius + 18f / inchesPerMeter;
    lightNum = 0;
    for (float angle = 360f/32f; angle < 360; angle += 360f/16f) {
      CXPoint p = new CXPoint(null, outsideLowerFloodsRadius * Math.cos(Math.toRadians(angle)), lowerFloodsHeight,
          outsideLowerFloodsRadius * Math.sin(Math.toRadians(angle)), lightNum, 0, angle, outsideLowerFloodsRadius);
      outsideLowerFloods.add(p);
      lightNum++;
    }
    coneFloodsByRing.add(outsideLowerFloods);
    allConeFloods.addAll(outsideLowerFloods);
    allPoints.addAll(outsideLowerFloods);

    // Interior lighting 8 high power leds + 8 other leds.
    /*
    float interiorRadius = panel5Radius - 6.0f / inchesPerMeter;
    int i = 0;
    for (float angle = 0; angle < 360; angle += 360f/16f) {
      CXPoint p = new CXPoint(null, interiorRadius * Math.cos(Math.toRadians(angle)), 2.0,
          interiorRadius * Math.sin(Math.toRadians(angle)), i, 0, angle, interiorRadius);
      interiorPoints.add(p);
      i++;
    }
    allPoints.addAll(interiorPoints);
    */

    float scoopYOffset = 0.0f; // 0.75f;
    for (LXPoint p : conePoints) {
      float r = (float)Math.sqrt(p.x*p.x+p.y*p.y);
      float pxOrig = p.x;
      p.x = (float)(p.x * Math.cos(Math.toRadians(-15)) + p.y * Math.sin(Math.toRadians(-15)));
      p.y = (float)(-pxOrig * Math.sin(Math.toRadians(-15)) + p.y * Math.cos(Math.toRadians(-15))) + scoopYOffset;
    }
    for (LXPoint p : scoopPoints) {
      float r = (float)Math.sqrt(p.x*p.x+p.y*p.y);
      float pxOrig = p.x;
      p.x = (float)(p.x * Math.cos(Math.toRadians(-15)) + p.y * Math.sin(Math.toRadians(-15)));
      p.y = (float)(-pxOrig * Math.sin(Math.toRadians(-15)) + p.y * Math.cos(Math.toRadians(-15))) + scoopYOffset;

      if (p.y < 0f) {
        logger.info("p below ground: " + p.y);
      }
    }

    for (LXPoint p : interiorPoints) {
      float pxOrig = p.x;
      p.x = (float)(p.x * Math.cos(Math.toRadians(-15)) + p.y * Math.sin(Math.toRadians(-15)));
      p.y = (float)(-pxOrig * Math.sin(Math.toRadians(-15)) + p.y * Math.cos(Math.toRadians(-15))) + scoopYOffset;
    }


    for (LXPoint p : allConeFloods) {
      float pxOrig = p.x;
      p.x = (float)(p.x * Math.cos(Math.toRadians(-15)) + p.y * Math.sin(Math.toRadians(-15)));
      p.y = (float)(-pxOrig * Math.sin(Math.toRadians(-15)) + p.y * Math.cos(Math.toRadians(-15))) + scoopYOffset;
    }


    logger.info("All Layer Dimensions:");
    for (String dim : layerDimensions) {
      logger.info(dim);
    }

    // Export the one each of the panels per panelLayoutNum for use in the interactive
    // wiring Process sketch.  The wirings will be loaded and strung together for each
    // output to configure our ArtNet output.
    Set<Integer> exportedPanelLayoutNums = new HashSet<Integer>();
    for (Panel exportPanel : allPanels) {
      if (!exportedPanelLayoutNums.contains(exportPanel.panelLayoutNum));
        //exportPanelPoints(exportPanel);
    }

    for (LXPoint pt : allPoints) {
      CXPoint cpt = (CXPoint) pt;
      cpt.intensityCompensation = cpt.horizontalSpacing / cpt.maxHorizontalSpacing;
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

    logger.info("Total points: " + pointCount);

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
  }


  static public boolean isPanelInLayer(Panel p, int layerNum) {
    if (layerNum >= panelLayers.size()) return false;
    if (layerNum < 0) return true;
    List<Panel> panels = panelLayers.get(layerNum);
    for (Panel p2 : panels) {
      if (p2.panelType == p.panelType && p2.panelNum == p.panelNum) {
        return true;
      }
    }
    return false;
  }

  static public boolean isPanelInSixteenth(Panel p, int sixteenthNum) {
    if (sixteenthNum >= Output.sixteenthPanels.size()) return false;
    if (sixteenthNum < 0) return true;
    List<Panel> panels = Output.sixteenthPanels.get(sixteenthNum);
    for (Panel p2 : panels) {
      if (p.panelRegion != Panel.PanelRegion.CONE) {
        if (p2.panelType == p.panelType && p2.panelNum == p.panelNum) {
          return true;
        }
      } else if (p2 == p) {
        return true;
      }
    }
    return false;
  }

  static public void exportPanelPoints(Panel panel) {
    String fname = (panel.panelType == Panel.PanelType.A1)?"A1":"A2";
    try {
      PrintWriter lxpointsFile = new PrintWriter("panelpoints_" + fname + ".csv");
      for (LXPoint lp : panel.getPoints()) {
        CXPoint p = (CXPoint) lp;
        lxpointsFile.println(p.x + "," + p.y);
      }
      lxpointsFile.close();
    } catch (IOException ioex) {

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
    //logger.info ("x,y " + coordinates[0] + "," + coordinates[1]);
    return coordinates;
  }

  public static int[] pointToImgCoordsCylinder(CXPoint p, int renderPointsWide, int renderPointsHigh, int yTexCoordOffset) {
    int[] coordinates = {0, 0};
    int yCoord = p.panel.yCoordOffset + p.yCoord - yTexCoordOffset;
    float xCoord = (float)(p.panel.panelNum * p.panel.pointsWide + p.xCoord);
    xCoord = xCoord * xScaleOriginal(p, renderPointsWide);
    if (p.panel.panelRegion == Panel.PanelRegion.CONE && renderPointsWide == ConeDownModel.POINTS_WIDE) {
      // Cone is rotated
      xCoord = xCoord + (1f/16f) * renderPointsWide;
      //xCoord += 40;
      if (xCoord > renderPointsWide) {
        xCoord -= renderPointsWide;
      } else if (xCoord < 0) {
        xCoord += renderPointsWide;
      }
    }

    // For the dancefloor on the full texture, we need to offset into the middle of the image so that we match up
    // with the back of the scoop and cone where the dancefloor is located.
    if (renderPointsWide == POINTS_WIDE && p.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
      int danceFloorPointsWide = p.panel.pointsWide * ConeDownModel.dancePanelsWide;
       int totalImgPointsWide = ConeDownModel.POINTS_WIDE;
       int imgXOffset = totalImgPointsWide/2 - danceFloorPointsWide/2;
       int xImgCoord = imgXOffset + p.panel.danceXPanel * p.panel.pointsWide + p.xCoord;
       xCoord = xImgCoord;

     } else if (p.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
       xCoord = p.panel.danceXPanel * p.panel.pointsWide + p.xCoord;
     }
     coordinates[0] = (int)Math.round(xCoord);

     coordinates[1] = (renderPointsHigh-1) - yCoord;
     return coordinates;
  }

  public static float xScaleOriginal(CXPoint p, int renderPointsWide) {
    /* float xScale = (float)POINTS_WIDE / ((float)p.panel.pointsWide * ((p.panel.scoop)?scoopSides:coneSides)); */
    float x = renderPointsWide / ((float)p.panel.pointsWide * ((p.panel.scoop)?scoopSides:coneSides));
    if (p.panel.isHalfPanel()){
      x = (float)renderPointsWide / ((float)(p.panel.pointsWide * p.panel.numPanelsAround()));
    }
    return x;
  }

  public static float xScale(CXPoint p) {
      float x;
      if (p.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
	  x = (float)dancePanelsWide / (float)dancePanelsHigh; // @@@ Hmmm
      } else if (p.panel.isHalfPanel()){
	  x = POINTS_WIDE / ((float)(p.panel.pointsWide * p.panel.numPanelsAround()));
      } else {
	  x = POINTS_WIDE / ((float)p.panel.pointsWide * ((p.panel.scoop)?scoopSides:coneSides));
      }

    return x;
  }

  public static float[] pointToProjectionCoords(CXPoint p) {
    float[] coordinates = {0, 0};
    float yCoord = p.panel.yCoordOffset + p.yCoord;
    float xCoord;

    if (p.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
	int danceFloorPointsWide = p.panel.pointsWide * ConeDownModel.dancePanelsWide;
	int totalImgPointsWide = POINTS_WIDE;
	float imgXOffset = (float)totalImgPointsWide/2 - (float)danceFloorPointsWide/2;
	float xImgCoord = imgXOffset + p.panel.danceXPanel * p.panel.pointsWide + p.xCoord;

	xCoord = xImgCoord;
    } else {
	xCoord = (p.panel.panelNum * p.panel.pointsWide + p.xCoord) * xScale(p);
    }

    coordinates[0] = xCoord;
    coordinates[1] = (POINTS_HIGH-1) - yCoord;
    return coordinates;
  }

  public static int POINTS_WIDE = 112;
  public static int POINTS_HIGH = 87;
}

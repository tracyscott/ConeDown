package art.lookingup;

import art.lookingup.ui.UIPixliteConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.ArtSyncDatagram;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.LXDatagramOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static art.lookingup.ConeDownModel.panelLayers;

/**
 * Handles output from our 'colors' buffer to our DMX lights.  Currently using E1.31.
 */
public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public static LXDatagramOutput datagramOutput = null;

  public static final int MAX_OUTPUTS = 32;  // 32 outputs in expanded mode.
  public static final int RAVE_OUTPUTS = 8;
  public static final int RAVE_UNIVERSES_PER_OUTPUT = 2;
  public static final int RAVE_UNIVERSES = RAVE_OUTPUTS * RAVE_UNIVERSES_PER_OUTPUT;

  public static List<List<Integer>> outputs = new ArrayList<List<Integer>>(MAX_OUTPUTS);
  public static List<List<Panel>> sixteenthPanels;

  /**
   * Loads a wiring.txt file that is written by PixelMapping Processing sketch.
   *
   * @param filename
   * @return
   */
  static protected boolean loadWiring(String filename) {
    for (int i = 0; i < MAX_OUTPUTS; i++) {
      outputs.add(new ArrayList<Integer>());
    }
    BufferedReader reader;
    int currentOutputNum = 0;
    List<Integer> currentOutputIndices = null;

    try {
      reader = new BufferedReader(new FileReader(filename));
      String line = reader.readLine();
      while (line != null) {
        // logger.log(Level.INFO, "Reading wiring: " + line);
        if (line.startsWith(":")) {
          currentOutputNum = Integer.parseInt(line.replace(":", ""));
          currentOutputIndices = outputs.get(currentOutputNum);
        } else {
          int pointIndex = Integer.parseInt(line);
          currentOutputIndices.add(pointIndex);
        }
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static String artnetIpAddress = "192.168.137.26"; //"192.168.137.149";
  public static int artnetPort = 6454;

  // TODO(tracy): We need to put out the points in the same order for the CNC-based panels that we did for
  // the dimensions-based generated panels.
  public static void configureUnityArtNet(LX lx) {
    List<LXPoint> points = lx.getModel().getPoints();
    int numUniverses = (int)Math.ceil(((double)points.size())/170.0);
    logger.info("Num universes: " + numUniverses);
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    int totalPointsOutput = 0;

    for (int univNum = 0; univNum < numUniverses; univNum++) {
      int[] dmxChannelsForUniverse = new int[170];
      for (int i = 0; i < 170 && totalPointsOutput < points.size(); i++) {
        LXPoint p = points.get(univNum*170 + i);
        dmxChannelsForUniverse[i] = p.index;
        totalPointsOutput++;
      }
      logger.info("Added points for universe number: " + univNum);
      ArtNetDatagram artnetDatagram = new ArtNetDatagram(dmxChannelsForUniverse, univNum);
      try {
        artnetDatagram.setAddress(artnetIpAddress).setPort(artnetPort);
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "Configuring ArtNet: " + artnetIpAddress, uhex);
      }
      datagrams.add(artnetDatagram);
    }


    for (ArtNetDatagram dgram : datagrams) {
      try {
        datagramOutput = new LXDatagramOutput(lx);
        datagramOutput.addDatagram(dgram);
      } catch (SocketException sex) {
        logger.log(Level.SEVERE, "Initializing LXDatagramOutput failed.", sex);
      }
      if (datagramOutput != null) {
        lx.engine.output.addChild(datagramOutput);
      } else {
        logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
      }
    }
  }

  static public List<List<CXPoint>> allOutputsPoints = new ArrayList<List<CXPoint>>();
  static public List<ArtNetDatagram> outputDatagrams = new ArrayList<ArtNetDatagram>();
  static public LXDatagram artSyncDatagram;

  /**
   * Gets the points in wire order for a panel specified by build panel nomenclature.
   * Starts at 1 and the numbers don't skip over missing panels.  So for our model
   * I6 doesn't exist where for the build nomenclature I6 is the first I panel after
   * coming back out of the ground.
   * @param buildPanel
   * @return
   */
  static public List<CXPoint> pointsWireOrderForBuildPanel(String buildPanel) {
    String ledSource = buildPanel.toLowerCase();
    Panel.PanelType panelType = Panel.PanelType.A1;
    int sixteenth = -1;

    if (ledSource.startsWith("i1_door")) {
      //pointsWireOrder.addAll(SirsasanaModel.topCrownSpikeLightsSorted);
      sixteenth = 0;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("h1_door")) {
      sixteenth = 0;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("i3_milli")) {
      sixteenth = 2;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("i4_micro")) {
      sixteenth = 3;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("i5_nano")) {
      sixteenth = 4;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("h6_milli")) {
      sixteenth = 5;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("h7_micro")) {
      sixteenth = 6;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("h8_nano")) {
      sixteenth = 7;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("h9_nano")) {
      sixteenth = 8;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("h10_micro")) {
      sixteenth = 9;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("h11_milli")) {
      sixteenth = 10;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("i6_nano")) {
      sixteenth = 11;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("i7_micro")) {
      sixteenth = 12;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("i8_milli")) {
      sixteenth = 13;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("i10_door")) {
      sixteenth = 15;
      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("h16_door")) {
      sixteenth = 15;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("upperin_ring")) {
      return ConeDownModel.insideUpperFloods;
    } else if (ledSource.startsWith("upper_ring")) {
      return ConeDownModel.outsideUpperFloods;
    } else if (ledSource.startsWith("lowerout_ring")) {
      return ConeDownModel.insideLowerFloods;
    } else if (ledSource.startsWith("lower_ring")) {
      return ConeDownModel.outsideLowerFloods;
    } else if (ledSource.startsWith("h8_nano")) {
      sixteenth = 7;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("i")) {
      logger.info("I panel=" + ledSource);
      int num = Integer.parseInt(ledSource.substring(1)) - 1;
      if (num > 6) num += 6;
      sixteenth = num;
      logger.info("   sixteenth= " + sixteenth);

      panelType = Panel.PanelType.I;
    } else if (ledSource.startsWith("g")) {
      sixteenth = Integer.parseInt(ledSource.substring(1)) - 1;
      panelType = Panel.PanelType.G;
    } else if (ledSource.startsWith("h")) {
      sixteenth = Integer.parseInt(ledSource.substring(1)) - 1;
      panelType = Panel.PanelType.H;
    } else if (ledSource.startsWith("f")) {
      sixteenth = Integer.parseInt(ledSource.substring(1)) - 1;
      panelType = Panel.PanelType.F;
    }
    if (sixteenth == -1) {
      logger.info("Badly formatted panel name, could not translate build panel " + ledSource + " to model name.  e.g. H0 - H15");
      return null;
    }
    Panel panel = getPanel(sixteenth, panelType);
    if (panel != null)
      return panel.pointsInWireOrder();
    return new ArrayList<CXPoint>();
  }

  public static void configurePixliteOutputScoop(LX lx) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    String artNetIpAddress = ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    logger.log(Level.INFO, "Using Pixlite ArtNet: " + artNetIpAddress + ":" + artNetIpPort);


    int universesPerOutput = 3;

    allOutputsPoints.clear();
    outputDatagrams.clear();

    for (int outputNum = 0; outputNum < 32; outputNum++) {
      List<CXPoint> outputPoints = new ArrayList<CXPoint>();
      allOutputsPoints.add(outputPoints);

      List<CXPoint> pointsWireOrder = new ArrayList<CXPoint>();
      // Output Number is 1 based in the UI.
      String mapping = ConeDown.outputMap.getOutputMapping(outputNum + 1);
      logger.info("========== PIXLITE OUTPUT #" + (outputNum + 1) + "     ==============");

      logger.info("mapping=" + mapping);
      // Allow multiple components per output.  With a 1:1 mapping we are fully utilizing each long range receiver
      // so there is no room for future expansion.
      // At most 15 panels on an output for requested configuration so we will do 16.  Each entry will
      // be like 1.1, 1.2, 1.3, 1.4 etc.
      /* "I1_door, H1_door, G1, F1, I2, H2, G2, F2, I3_milli, H3, G3, F3, I4_micro, G4, F4",
      "H4, I5_nano, H5, G5, F5, H6_milli, G6, F6, H7_micro, G7, F7, H8_nano, G8, F8",
      "H9_nano, G9, F9, H10_micro, G10, F10, H11_milli, G11, F11, I6_nano, H12, G12, F12, H13",
      "I7_micro, G13, F13, I8_milli, H14, G14, F14, I9, H15, G15, F15, I10_door, H16_door, G16, F16",
      "upperin_ring, upperout_ring, lowerin_ring, lowerout_ring",
      */
      String[] components = mapping.split(",");
      logger.info("Output " + (outputNum + 1) + " fixtures: " + mapping);
      for (int ci = 0; ci < components.length; ci++) {
        String ledSource = components[ci];
        ledSource = ledSource.trim();
        logger.info("Adding " + ledSource);
        if ("".equalsIgnoreCase(ledSource))
          continue;
        pointsWireOrder.addAll(pointsWireOrderForBuildPanel(ledSource));
      }

      outputPoints.addAll(pointsWireOrder);

      int numUniversesThisWire = (int) Math.ceil((float) pointsWireOrder.size() / 170f);
      int univStartNum = outputNum * universesPerOutput;
      int lastUniverseCount = pointsWireOrder.size() - 170 * (numUniversesThisWire - 1);
      int maxLedsPerUniverse = (pointsWireOrder.size()>170)?170:pointsWireOrder.size();
      int[] thisUniverseIndices = new int[maxLedsPerUniverse];
      int curIndex = 0;
      int curUnivOffset = 0;
      for (LXPoint pt : pointsWireOrder) {
        thisUniverseIndices[curIndex] = pt.index;
        curIndex++;
        if (curIndex == 170 || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
          logger.log(Level.INFO, "Adding datagram: for: " + mapping + " PixLite universe=" + (univStartNum + curUnivOffset + 1) + " ArtNet universe=" + (univStartNum + curUnivOffset) + " points=" + curIndex);
          //ArtNetDatagram datagram = new ArtNetDatagram(lx, thisUniverseIndices, univStartNum + curUnivOffset);
          ArtNetDatagram datagram = new ArtNetDatagram(thisUniverseIndices, curIndex*3, univStartNum + curUnivOffset);
          try {
            datagram.setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort);
          } catch (UnknownHostException uhex) {
            logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
          }
          datagrams.add(datagram);
          curUnivOffset++;
          curIndex = 0;
          if (curUnivOffset == numUniversesThisWire - 1) {
            thisUniverseIndices = new int[lastUniverseCount];
          } else {
            thisUniverseIndices = new int[maxLedsPerUniverse];
          }
        }
      }
    }

    try {
      datagramOutput = new LXDatagramOutput(lx);
      for (ArtNetDatagram datagram : datagrams) {
        datagramOutput.addDatagram(datagram);
      }
      try {
        datagramOutput.addDatagram(new ArtSyncDatagram().setAddress(artNetIpAddress).setPort(artNetIpPort));
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "Unknown host for ArtNet sync.", uhex);
      }
    } catch (SocketException sex) {
      logger.log(Level.SEVERE, "Initializing LXDatagramOutput failed.", sex);
    }
    if (datagramOutput != null) {
      datagramOutput.enabled.setValue(true);
      lx.engine.output.addChild(datagramOutput);
    } else {
      logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
    }
    /*
    for (ArtNetDatagram dgram : datagrams) {
      lx.engine.addOutput(dgram);
      outputDatagrams.add(dgram);
    }

    try {
      artSyncDatagram = new ArtSyncDatagram(lx).setAddress(InetAddress.getByName(artNetIpAddress)).setPort(artNetIpPort);
      lx.engine.addOutput(artSyncDatagram);
    } catch (UnknownHostException unhex) {
      logger.info("Uknown host exception for Pixlite IP: " + artNetIpAddress + " msg: " + unhex.getMessage());
    }
    */
  }

  /**
   * Given a sixteenth number and a panel type, find the matching panel.
   * @param sixteenth
   * @param panelType
   * @return
   */
  static public Panel getPanel(int sixteenth, Panel.PanelType panelType) {
    for (List<Panel> layer : panelLayers) {
      for (Panel panel : layer) {
        if (panel.panelType == panelType && panel.panelNum == sixteenth)
          return panel;
      }
    }
    return null;
  }

  /**
   * Each Pixlite output covers one sixteenth of the installation.  Dance floor is another 1 or 2 outputs.
   * Probably 2.
   * @param lx
   */
  public static void configurePixliteOutput(LX lx) {
    sixteenthPanels = new ArrayList<List<Panel>>();
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    List<Integer> countsPerOutput = new ArrayList<Integer>();
    List<Integer> scoopCountsPerOutput = new ArrayList<Integer>();

    // For each output, track the number of points per panel type so we can log the details to help
    // with output verification.
    List<Map<String, Integer>> countsByPanelType = new ArrayList<Map<String, Integer>>();
    List<Map<String, String>> allDXFByPanelType = new ArrayList<Map<String, String>>();


    String artNetIpAddress = ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    logger.log(Level.INFO, "Using ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    int sixteenthNum = 0;
    // NOTE(tracy): universesPerSixteenth needs to be set correctly.  Some outputs use less than 3 universes but
    // we will just set 3 here and waste a few universes.
    int universesPerSixteenth = 3;
    Set wireFilesWritten = new HashSet();
    for (sixteenthNum = 0; sixteenthNum < 16; sixteenthNum++) {
      List<Panel> thisSixteenthPanel = new ArrayList<Panel>();
      sixteenthPanels.add(thisSixteenthPanel);
      // Some utility datastructures for reporting the results of the output mapping later.  This is just
      // mean to help verify the output mapping.
      Map<String, Integer> pointCountByPanelType = new HashMap<String, Integer>();
      countsByPanelType.add(pointCountByPanelType);
      Map<String, String> dxfByPanelType = new HashMap<String, String>();
      allDXFByPanelType.add(dxfByPanelType);
      // This is a list of panel keys used for wiring.  We want them in wire order so that we can generate
      // some HTML documentation for each sixteenth.
      List<String> panelKeysInWireOrder = new ArrayList<String>();

      int univStartNum = sixteenthNum * universesPerSixteenth;
      // First we will collect all our points in wire order.  These points will span multiple
      // panels and multiple universes.  Once we have all the points for a given sixteenth wire
      // then we will start packing them into ArtNetDatagrams with 170 points per universe.
      List<CXPoint> allPointsWireOrder = new ArrayList<CXPoint>();
      List<CXPoint> allScoopPointsWireOrder = new ArrayList<CXPoint>();
      for (List<Panel> layer : panelLayers) {
        // NOTE(Tracy):
        Panel panel = layer.get(0);
        if (panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) continue;

        logger.info("");
        logger.info("panel layer: " + Panel.panelTypeNames[panel.panelType.ordinal()]);
        logger.info("dim: " + panel.pointsWide + "x" + panel.pointsHigh);
        logger.info("sixteenth: " + sixteenthNum);

        // For the I panel layer, we don't have any panels between 5 and 10.
        if (panel.panelType == Panel.PanelType.I && (sixteenthNum > 4 && sixteenthNum < 11)) {
          logger.info("Skipping output for nonexistent I panel # " + sixteenthNum);
          continue;
        }

        // C and D panels each span an entire octant (2 sixteenths).  To minimize
        // leds per output we alternate C and D on different outputs.
        if (panel.panelType == Panel.PanelType.C && sixteenthNum % 2 == 1) {
          panel = layer.get(sixteenthNum / 2);
        } else if (panel.panelType == Panel.PanelType.D && sixteenthNum % 2 == 1) {
          logger.info("Assign panel to D panel");
          panel = layer.get(sixteenthNum / 2);
        } else if (!(panel.panelType == Panel.PanelType.C || panel.panelType == Panel.PanelType.D)) {
          int iGapOffset = 0;
          // Account for the missing I panels.
          if (panel.panelType == Panel.PanelType.I && sixteenthNum > 10)
            iGapOffset = 6;
          int panelNum = sixteenthNum;
          if (panel.panelRegion == Panel.PanelRegion.CONE) {
            panelNum = sixteenthNum -1;
            if (panelNum < 0)
              panelNum = panel.numPanelsAround() - 1;
          }
          panel = layer.get(panelNum - iGapOffset);
        } else {
          continue; // Skip C or D if it is not their turn.
        }
        thisSixteenthPanel.add(panel);


        logger.info("panelType: " + Panel.panelTypeNames[panel.panelType.ordinal()]);
        List<CXPoint> pointsWireOrder = panel.pointsInWireOrder();

        pointCountByPanelType.put(Panel.panelTypeNames[panel.panelType.ordinal()], pointsWireOrder.size());
        dxfByPanelType.put(Panel.panelTypeNames[panel.panelType.ordinal()], panel.dxfFilename);

        // Write points and wiring file.  These can be used with Pixel Mapper sketch to visually inspect
        // the result of the mapping code.
        // Only write once per panelType.
        String panelKey = Panel.panelTypeNames[panel.panelType.ordinal()] + "_" + panel.panelNum;
        String dxfbase = panel.dxfFilename.replace(".dxf", "").replace("panel_", "").replace("_LED", "");
        panelKey = dxfbase + "_" + panel.panelNum;
        if (panel.mirrored) panelKey = panelKey + "_mirror";
        if (!wireFilesWritten.contains(panelKey)) {
          String pointsFilename = "points_panel_" + panelKey + ".csv";
          String wiringFilename = "wiring_panel_" + panelKey + ".txt";
          writePointsFile(pointsFilename, pointsWireOrder);
          writeWiringFile(wiringFilename, pointsWireOrder);
          wireFilesWritten.add(panelKey);
          panelKeysInWireOrder.add(panelKey);
        }

        // pointsWireOrder contains our points in wiring order for this panel.
        allPointsWireOrder.addAll(pointsWireOrder);

        // Track only the points in scoop type panels.
        if (panel.panelType == Panel.PanelType.F || panel.panelType == Panel.PanelType.G ||
            panel.panelType == Panel.PanelType.H || panel.panelType == Panel.PanelType.I)
          allScoopPointsWireOrder.addAll(pointsWireOrder);
      }

      // Write out HTML documentation for wiring each sixteenth
      writeSixteenthHtmlDoc(sixteenthNum, panelKeysInWireOrder);

      //logger.info("Output " + sixteenthNum + " scoop points: " + allScoopPointsWireOrder.size());
      countsPerOutput.add(allPointsWireOrder.size());
      scoopCountsPerOutput.add(allScoopPointsWireOrder.size());

      // NOTE(tracy): We have to create ArtNetDatagram with the actual numbers of our points or else it
      // will puke internally. i.e. we can't just use 170 but then pass it less than 170 points so we
      // need to figure out how large to make our channel array for the last universe.
      int numUniversesThisWire = (int)Math.ceil((float)allPointsWireOrder.size()/170f);
      int lastUniverseCount = allPointsWireOrder.size() - 170 * (numUniversesThisWire - 1);

      logger.info("Datagrams for 16th " + sixteenthNum);
      int[] thisUniverseIndices = new int[170];
      int curIndex = 0;
      int curUnivOffset = 0;
      for (CXPoint pt : allPointsWireOrder) {
        thisUniverseIndices[curIndex] = pt.index;
        curIndex++;
        if (curIndex == 170 || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
          logger.log(Level.INFO, "Adding datagram: universe=" + (univStartNum+curUnivOffset) + " points=" + curIndex);
          ArtNetDatagram datagram = new ArtNetDatagram(thisUniverseIndices, curIndex*3, univStartNum + curUnivOffset);
          try {
            datagram.setAddress(artNetIpAddress).setPort(artNetIpPort);
          } catch (UnknownHostException uhex) {
            logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
          }
          datagrams.add(datagram);
          curUnivOffset++;
          curIndex = 0;
          if (curUnivOffset == numUniversesThisWire - 1) {
            thisUniverseIndices = new int[lastUniverseCount];
          } else {
            thisUniverseIndices = new int[170];
          }
        }
      }
    }

    // Dance panels.  Requires 2 outputs to minimize strand length.  The first output starts with
    // dance tile 2,0 and then 1,0 and then 0,0 and then moves over and then up so dance tile
    // 0,1 and then 0,2 and then 0,3.
    // The second output is dance tile 2,2 then 2,1, then 2,0.
    List<CXPoint> pointsForDanceOutput1 = new ArrayList<CXPoint>();
    boolean down = true;
    logger.info("Dance output 1");
    for (int x = 0; x < ConeDownModel.dancePanelsWide - 1; x++) {
      for (int y = ConeDownModel.dancePanelsHigh-1; y >= 0; y--) {
        int actualY = (down)?y:(ConeDownModel.dancePanelsHigh - 1 - y);
        Panel panel = Panel.getDancePanelXY(ConeDownModel.dancePanels, x, actualY);
        List<CXPoint> pointsWireOrder = panel.pointsInWireOrder();
        logger.info("dance panel " + panel.danceXPanel + "," + panel.danceYPanel + " points: " +
            pointsWireOrder.size());
        pointsForDanceOutput1.addAll(pointsWireOrder);
      }
      down = false;
    }
    // Cone+Scoop uses 3 universes per sixteenth so we start at universe 48.
    List<ArtNetDatagram> danceOutput1Datagrams = assignPointsToArtNetDatagrams(pointsForDanceOutput1, 48,
        artNetIpAddress, artNetIpPort);
    countsPerOutput.add(pointsForDanceOutput1.size());
    datagrams.addAll(danceOutput1Datagrams);

    // Dance Output 2
    List<CXPoint> pointsForDanceOutput2 = new ArrayList<CXPoint>();
    down = true;
    int x = 2;
    for (int y = ConeDownModel.dancePanelsHigh-1; y >= 0; y--) {
      int actualY = (down)?y:(ConeDownModel.dancePanelsHigh - 1 - y);
      Panel panel = Panel.getDancePanelXY(ConeDownModel.dancePanels, x, actualY);
      List<CXPoint> pointsWireOrder = panel.pointsInWireOrder();
      pointsForDanceOutput2.addAll(pointsWireOrder);
      logger.info("dance panel " + panel.danceXPanel + "," + panel.danceYPanel + " points: " +
          pointsWireOrder.size());
    }

    // Dance output 1 used 2 universes (49 points per panel * 6 panels = 294 points @ 170-per-universe)
    List<ArtNetDatagram> danceOutput2Datagrams = assignPointsToArtNetDatagrams(pointsForDanceOutput2, 50,
        artNetIpAddress, artNetIpPort);
    countsPerOutput.add(pointsForDanceOutput2.size());
    datagrams.addAll(danceOutput2Datagrams);

    // Interior lights.  Dance output 2 used one universe so our start universe is 51.
    List<ArtNetDatagram> interiorDatagrams = assignPointsToArtNetDatagrams(ConeDownModel.interiorPoints,
        51, artNetIpAddress, artNetIpPort);
    countsPerOutput.add(ConeDownModel.interiorPoints.size());
    datagrams.addAll(interiorDatagrams);

    int i = 0;
    for (Integer count : countsPerOutput) {
      logger.info("output " + i + ": " + count + " points");
      if (i < 15) logger.info("    scoop points: " + scoopCountsPerOutput.get(i));
      // Only cone/scoop outputs have counts by panel type.
      if (i < 16) {
        Map<String, Integer> pointCountByPanelType = countsByPanelType.get(i);
        Map<String, String> dxfByPanelType = allDXFByPanelType.get(i);
        ArrayList<String> sortedKeys =
            new ArrayList<String>(pointCountByPanelType.keySet());
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
          logger.info("   key= " + key + " count= " + pointCountByPanelType.get(key) + " dxf= " +
              dxfByPanelType.get(key));
        }
      }
      i++;
    }

    try {
      datagramOutput = new LXDatagramOutput(lx);
      for (ArtNetDatagram datagram : datagrams) {
        datagramOutput.addDatagram(datagram);
      }
      try {
        datagramOutput.addDatagram(new ArtSyncDatagram().setAddress(artNetIpAddress).setPort(artNetIpPort));
      } catch (UnknownHostException uhex) {
        logger.log(Level.SEVERE, "Unknown host for ArtNet sync.", uhex);
      }
    } catch (SocketException sex) {
      logger.log(Level.SEVERE, "Initializing LXDatagramOutput failed.", sex);
    }
    if (datagramOutput != null) {
      datagramOutput.enabled.setValue(true);
      lx.engine.output.addChild(datagramOutput);
    } else {
      logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
    }
    logger.info("layers: " + panelLayers.size());
  }

  /**
   * Given a set of points and a starting universe, assign the points to a series of ArtNetDatagrams.  This encapsulates
   * the logic of chunking 170 pixels per universes.  Callers can determine the number of universes used by
   * checking the length of the returned list.
   * @param pointsWireOrder The points in wire order to map to ArtNet.
   * @param startUniverse The starting universe for the set of points.
   * @param ipAddress The IP Address for the ArtNet destination.
   * @param ipPort The Port number for the ArtNet destination.
   * @return
   */
  static public List<ArtNetDatagram> assignPointsToArtNetDatagrams(List<CXPoint> pointsWireOrder, int startUniverse,
                                                                   String ipAddress, int ipPort) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();

    // NOTE(tracy): We have to create ArtNetDatagram with the actual numbers of our points or else it
    // will puke internally. i.e. we can't just use 170 but then pass it less than 170 points so we
    // need to figure out how large to make our channel array for the last universe.
    int numUniversesThisWire = (int)Math.ceil((float)pointsWireOrder.size()/170f);
    int lastUniverseCount = pointsWireOrder.size() - 170 * (numUniversesThisWire - 1);
    int firstUniverseCount = (numUniversesThisWire>1)?170:lastUniverseCount;

    int[] thisUniverseIndices = new int[firstUniverseCount];
    int curIndex = 0;
    int curUnivOffset = 0;
    for (CXPoint pt : pointsWireOrder) {
      thisUniverseIndices[curIndex] = pt.index;
      curIndex++;
      if (curIndex == 170 || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
        logger.log(Level.INFO, "Adding datagram: universe=" + (startUniverse+curUnivOffset) + " points=" + curIndex);
        ArtNetDatagram datagram = new ArtNetDatagram(thisUniverseIndices, curIndex*3, startUniverse + curUnivOffset);
        try {
          datagram.setAddress(ipAddress).setPort(ipPort);
        } catch (UnknownHostException uhex) {
          logger.log(Level.SEVERE, "Configuring ArtNet: " +ipAddress + ":" + ipPort, uhex);
        }
        datagrams.add(datagram);
        curUnivOffset++;
        curIndex = 0;
        if (curUnivOffset == numUniversesThisWire - 1) {
          thisUniverseIndices = new int[lastUniverseCount];
        } else {
          thisUniverseIndices = new int[170];
        }
      }
    }
    return datagrams;
  }

  static public void writeSixteenthHtmlDoc(int sixteenth, List<String> panelKeysWireOrder) {
    String filename = "sixteenth_" + sixteenth + ".html";
    try {
      PrintWriter htmlFile = new PrintWriter(filename);
      htmlFile.println("<html><head><title>16th " + sixteenth + "</title></head><body><h1>16th " + sixteenth + "<h1>");
      for (int keyNum = panelKeysWireOrder.size() - 1; keyNum >= 0; --keyNum) {
        String panelKey = panelKeysWireOrder.get(keyNum);
        htmlFile.println("<img src=\"wiring/" + panelKey + ".png\"><br/>");
      }
      htmlFile.println("</body></html>");
      htmlFile.close();
    } catch (IOException ioex) {
      logger.info("IOException writing " + filename + ": " + ioex.getMessage());
    }
  }

  static public void writePointsFile(String filename, List<CXPoint> points) {
    try {
      PrintWriter lxpointsFile = new PrintWriter(filename);
      for (CXPoint p : points) {
        lxpointsFile.println(p.panelLocalX + "," + p.panelLocalY);
      }
      lxpointsFile.close();
    } catch (IOException ioex) {
      logger.info("IOException writing " + filename + ": " + ioex.getMessage());
    }
  }

  static public void writeWiringFile(String filename, List<CXPoint> points) {
    try {
      PrintWriter wiringFile = new PrintWriter(filename);
      int pNum = 0;
      wiringFile.println(":0");
      for (LXPoint p : points) {
        wiringFile.println(pNum);
        pNum++;
      }
      wiringFile.close();
    } catch (IOException ioex) {
      logger.info("IOException writing " + filename + ": " + ioex.getMessage());
    }
  }

  static public final int NUM_CARS = 5;
  static public LXDatagramOutput carOutput = null;
  static public int[] fdCarZCounts = {630, 408, 551, 630, 408, 551, 900};
  static public int[][] fdCarZDims = {{30, 21}, {34, 12}, {29, 19}, {30, 21}, {34, 12}, {29, 19}, {45, 20}, {19,16}};

  static public int[][] fdCarEDims = {{23, 19}, {21, 21}, {44, 13}, {17, 36}, {20, 23}, {21, 22}, {44, 12}, {37,18}, {20, 20}, {20, 20}, {20, 20}};
  static public int[][] fdCarLDims = {{32, 21}, {32, 21}, {42, 17}, {23, 13}, {32, 21}, {41, 17}, {44, 28}};
  static public int[][] fdCarRDims = {{20, 20}, {20, 20}, {20, 20}, {36, 24}, {27, 12}, {32, 10}, {32, 14}, {36, 22}, {46, 23}, {34, 10}, {52, 28}};
  static public int[][] fdCarTDims = {{29, 19}, {22, 17}, {20, 20}, {29, 19}, {22, 17}, {36, 19}, {32, 21}, {32, 21}};

  static public String[] carNames = {"zebra", "elephant", "lion", "rhino", "tiger"};
  static public int[][][] fdCarDims;

  static public void outputGalactic(LX lx) {
    List<int[][]> fdCarDims = new ArrayList<int[][]>();

    fdCarDims.add(fdCarZDims);
    fdCarDims.add(fdCarEDims);
    fdCarDims.add(fdCarLDims);
    fdCarDims.add(fdCarRDims);
    fdCarDims.add(fdCarTDims);

    // If we have an existing output, remove it and disable it.
    if (carOutput != null) {
      lx.engine.output.removeChild(carOutput);
    }

    List<ArtNetDatagram> allCarsDatagrams = new ArrayList<ArtNetDatagram>();
    int curUniverseStart = 1;
    int globalPointNum = 0;
    for (int i = 0; i < NUM_CARS; i++) {
      String thisCarIpAddress = ConeDown.galacticJungle.getStringParameter(carNames[i]).getString();
      // Need the base ip address
      String[] ipParts = thisCarIpAddress.split("\\.");
      int lastIpPart = Integer.parseInt(ipParts[3]);
      String baseIpPart = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
      List<ArtNetDatagram> thisCarDatagrams = new ArrayList<ArtNetDatagram>();
      int[][] thisCarDimensions = fdCarDims.get(i);
      for (int fluxDriveNum = 0; fluxDriveNum < thisCarDimensions.length; fluxDriveNum++) {
        int[] thisFluxDriveDims = thisCarDimensions[fluxDriveNum];
        int pointCount = thisFluxDriveDims[0] * thisFluxDriveDims[1];
        // Each fluxDriveNum == 0 has a 200 pt auxillary output.
        if (fluxDriveNum == 0)
          pointCount += 200;
        List<CXPoint> pointsForThisFluxDrive = new ArrayList<CXPoint>();
        for (int ptNum = 0; ptNum < pointCount; ptNum++) {
          CXPoint pt = (CXPoint) lx.getModel().getPoints().get(globalPointNum);
          // Test mode. All points for this flux drive will be red, green, blue, or yellow.
          // CXPoint pt = (CXPoint) lx.getModel().getPoints().get(fluxDriveNum % 4);
          // Currently, we are just throwing our points directly to their artnet universes without
          // any spatial mapping.
          pointsForThisFluxDrive.add(pt);
          globalPointNum++;
          if (globalPointNum >= lx.getModel().getPoints().size()) {
            globalPointNum = 0;
          }
        }
        int thisFluxDriveLastIp = lastIpPart + fluxDriveNum;
        String thisFluxDriveIp = baseIpPart + thisFluxDriveLastIp;
        logger.info("datagrams for: " + thisFluxDriveIp);
        List<ArtNetDatagram> thisFluxDriveDatagrams =
            assignPointsToArtNetDatagrams(pointsForThisFluxDrive, curUniverseStart, thisFluxDriveIp, 6454);
        logger.info("car num: " + i + " fdNum: " + fluxDriveNum + " start: " + curUniverseStart + " univs:" +
            thisFluxDriveDatagrams.size());
        curUniverseStart += thisFluxDriveDatagrams.size();
        thisCarDatagrams.addAll(thisFluxDriveDatagrams);
      }
      logger.info("car num: " + i + " universes: " + thisCarDatagrams.size());
      allCarsDatagrams.addAll(thisCarDatagrams);
      curUniverseStart += 10;  // For reserved space.
    }

    // For now we are just going to have one LXDatagramOutput for all cars.
    try {
      carOutput = new LXDatagramOutput(lx);
      for (ArtNetDatagram datagram : allCarsDatagrams) {
        carOutput.addDatagram(datagram);
      }
    } catch (SocketException sex) {
      logger.log(Level.SEVERE, "Initializing Galactic LXDatagramOutput failed.", sex);
    }
    carOutput.enabled.setValue(false);
    if (carOutput != null) {
      lx.engine.output.addChild(carOutput);
    } else {
      logger.log(Level.SEVERE, "Did not configure Galactic output, error during LXDatagramOutput init");
    }
  }

  static public List<Panel> getPanelLayer(Panel.PanelType pt) {
    for (List<Panel> layer : panelLayers) {
      Panel panel = layer.get(0);
      if (panel.panelType == pt) {
        return layer;
      }
    }
    return null;
  }
}

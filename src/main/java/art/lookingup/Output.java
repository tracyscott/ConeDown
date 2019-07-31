package art.lookingup;

import art.lookingup.ui.UIPixliteConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.ArtSyncDatagram;
import heronarts.lx.output.LXDatagramOutput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  public static String artnetIpAddress = "192.168.2.120";
  public static int artnetPort = 6454;

  // TODO(tracy): We need to put out the points in the same order for the CNC-based panels that we did for
  // the dimensions-based generated panels.
  public static void configureUnityArtNet(LX lx) {
    List<LXPoint> points = lx.getModel().getPoints();
    int numUniverses = (int)Math.ceil(((double)points.size())/170.0);
    logger.info("Num universes: " + numUniverses);
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    int totalPointsOutput = 0;

    // Output by panel.

    int curUnivNum = 0;
    int curDmxAddress = 0;


    /*
    int[] dmxChannelsForUniverse = new int[170];
    for (Panel panel : ConeDownModel.allPanels) {
      int numPanelPoints = panel.getPoints().size();
     System.out.println("Adding points for panelType: " + panel.pointsWide + "," + panel.pointsHigh);
     if (panel.panelRegion == Panel.PanelRegion.CONE) {
       for (int col = 0; col < panel.pointsWide; col++) {
        for (int row = 0; row < panel.pointsHigh; row++) {
           CXPoint p = CXPoint.getCXPointAtTexCoord(panel.getPoints(), col, row);
           dmxChannelsForUniverse[curDmxAddress++] = p.index;
           if (curDmxAddress >= 170) {
             System.out.println("Added points for universe number: " + curUnivNum);
             ArtNetDatagram artnetDatagram = new ArtNetDatagram(dmxChannelsForUniverse, curUnivNum);
             try {
               artnetDatagram.setAddress(artnetIpAddress).setPort(artnetPort);
             } catch (UnknownHostException uhex) {
               logger.log(Level.SEVERE, "Configuring ArtNet: " + artnetIpAddress, uhex);
             }
             datagrams.add(artnetDatagram);
             curUnivNum++;
             curDmxAddress = 0;
             dmxChannelsForUniverse = new int[170];
           }
         }
       }
     } else {
       for (int row = 0; row < panel.pointsHigh; row++) {
         for (int col = 0; col < panel.pointsWide; col++) {
           CXPoint p = CXPoint.getCXPointAtTexCoord(panel.getPoints(), col, row);
           dmxChannelsForUniverse[curDmxAddress++] = p.index;
           if (curDmxAddress >= 170) {
             System.out.println("Added points for universe number: " + curUnivNum);
             ArtNetDatagram artnetDatagram = new ArtNetDatagram(dmxChannelsForUniverse, curUnivNum);
             try {
               artnetDatagram.setAddress(artnetIpAddress).setPort(artnetPort);
             } catch (UnknownHostException uhex) {
               logger.log(Level.SEVERE, "Configuring ArtNet: " + artnetIpAddress, uhex);
             }
             datagrams.add(artnetDatagram);
             curUnivNum++;
             curDmxAddress = 0;
             dmxChannelsForUniverse = new int[170];
           }
         }
       }
     }
    }
    */

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

  /**
   * Each Pixlite output covers one sixteenth of the installation.  Dance floor is another 1 or 2 outputs.
   * Probably 2.
   * @param lx
   */
  public static void configurePixliteOutput(LX lx) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();

    String artNetIpAddress = ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    logger.log(Level.INFO, "Using ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    int sixteenthNum = 0;
    int universesPerSixteenth = 3;
    Set wireFilesWritten = new HashSet();
    for (sixteenthNum = 0; sixteenthNum < 16; sixteenthNum++) {
      int univStartNum = sixteenthNum * universesPerSixteenth;
      // First we will collect all our points in wire order.  These points will span multiple
      // panels and multiple universes.  Once we have all the points for a given sixteenth wire
      // then we will start packing them into ArtNetDatagrams with 170 points per universe.
      List<CXPoint> allPointsWireOrder = new ArrayList<CXPoint>();
      for (List<Panel> layer : panelLayers) {
        // NOTE(Tracy):
        Panel panel = layer.get(0);
        logger.info("");
        logger.info("panel layer: " + Panel.panelTypeNames[panel.panelType.ordinal()]);
        logger.info("dim: " + panel.pointsWide + "x" + panel.pointsHigh);
        logger.info("sixteenth: " + sixteenthNum);
        // C and D panels each span an entire octant (2 sixteenths).  To minimize
        // leds per output we alternate C and D on different outputs.
        if (panel.panelType == Panel.PanelType.C && sixteenthNum % 2 == 0) {
          panel = layer.get(sixteenthNum / 2);
        } else if (panel.panelType == Panel.PanelType.D && sixteenthNum % 2 == 1) {
          logger.info("Assign panel to D panel");
          panel = layer.get(sixteenthNum / 2);
        } else if (!(panel.panelType == Panel.PanelType.C || panel.panelType == Panel.PanelType.D)) {
          panel = layer.get(sixteenthNum);
        } else {
          continue; // Skip C or D if it is not their turn.
        }
        logger.info("panelType: " + Panel.panelTypeNames[panel.panelType.ordinal()]);
        List<CXPoint> pointsWireOrder = new ArrayList<CXPoint>();
        // For each panel we wire from bottom left to bottom right and then move up one pixel
        // and then wire backwards from right to left, etc.  We can use our texture coordinates
        // to navigate the points on a panel.
        boolean movingLeft = false;
        for (int rowNum = 0; rowNum < panel.pointsHigh; rowNum++) {
          for (int colNum = 0; colNum < panel.pointsWide; colNum++) {
            int x = colNum;
            if (movingLeft) {
              x = (panel.pointsWide - 1) - colNum;
            }
            CXPoint p = panel.getCXPointAtTexCoord(x, rowNum);
            // logger.info("point at: " + x + "," + rowNum);
            pointsWireOrder.add(p);
          }
          movingLeft = !movingLeft;
        }

        // Write points and wiring file.  These can be used with Pixel Mapper sketch to visually inspect
        // the result of the mapping code.
        // Only write once per panelType.
        if (!wireFilesWritten.contains(panel.panelType)) {
          String pointsFilename = "points_panel_" + Panel.panelTypeNames[panel.panelType.ordinal()] + ".csv";
          String wiringFilename = "wiring_panel_" + Panel.panelTypeNames[panel.panelType.ordinal()] + ".txt";
          writePointsFile(pointsFilename, pointsWireOrder);
          writeWiringFile(wiringFilename, pointsWireOrder);
          wireFilesWritten.add(panel.panelType);
        }

        // pointsWireOrder contains our points in wiring order for this panel.
        allPointsWireOrder.addAll(pointsWireOrder);
      }

      // NOTE(tracy): We have to create ArtNetDatagram with the actual numbers of our points or else it
      // will puke internally. i.e. we can't just use 170 but then pass it less than 170 points so we
      // need to figure out how large to make our channel array for the last universe.
      int lastUniverseCount = allPointsWireOrder.size() - 170 * (universesPerSixteenth - 1);
      int numUniverses = (int)Math.ceil((float)allPointsWireOrder.size()/170f);

      int[] thisUniverseIndices = new int[170];
      int curIndex = 0;
      int curUnivOffset = 0;
      for (CXPoint pt : allPointsWireOrder) {
        thisUniverseIndices[curIndex] = pt.index;
        curIndex++;
        if (curIndex == 170 || (curUnivOffset == numUniverses - 1 && curIndex == lastUniverseCount)) {
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
          if (curUnivOffset == numUniverses - 1) {
            thisUniverseIndices = new int[lastUniverseCount];
          } else {
            thisUniverseIndices = new int[170];
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
      lx.engine.output.addChild(datagramOutput);
    } else {
      logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
    }
    logger.info("layers: " + panelLayers.size());
  }

  static public void writePointsFile(String filename, List<CXPoint> points) {
    try {
      PrintWriter lxpointsFile = new PrintWriter(filename);
      for (CXPoint p : points) {
        if (p == null) logger.info("p was null");
        else logger.info("p not null");
        if (p.panel.panelType == Panel.PanelType.D) {
          logger.info("D panel");
        }
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

  public static void configureUnityArtNetOutput(LX lx) {
    //loadWiring("wiring.txt");
    // This only works if we have less than 170 lxpoints.
    String artNetIpAddress = ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(ConeDown.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    logger.log(Level.INFO, "Using ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();

    int outputNumber = 1;
    int universeNumber = 0;

    while (universeNumber < RAVE_UNIVERSES) {
      for (List<Integer> indices : outputs) {
        // For the Rave sign, we only have outputs 1 through 4 mapped.  If there is nothing on the output in the
        // wiring.txt file skip it.  We will make 2 passes of the wiring.txt file, one for each side of the sign.
        if (indices.size() == 0) continue;
        // Add point indices in chunks of 170.  After 170 build datagram and then increment the universeNumber.
        // Continuing adding points and building datagrams every 170 points.  After all points for an output
        // have been added to datagrams, start on a new output and reset counters.
        int chunkNumber = 0;
        int pointNum = 0;
        while (pointNum + chunkNumber * 170 < indices.size()) {
          // Compute the dataLength.  For a string of 200 leds, we should have dataLengths of
          // 170 and then 30.  So for the second pass, chunkNumber=1.  Overrun is 2*170 - 200 = 340 - 200 = 140
          // We subtract 170-overrun = 30, which is the remainder number of the leds on the last chunk.
          // 350 leds = chunkNumber = 2, 510 - 350 = 160.  170-160=10.
          int overrun = ((chunkNumber + 1) * 170) - indices.size();
          int dataLength = (overrun < 0) ? 170 : 170 - overrun;
          int[] thisUniverseIndices = new int[dataLength];
          // For each chunk of 170 points, add them to a datagram.
          for (pointNum = 0; pointNum < 170 && (pointNum + chunkNumber * 170 < indices.size());
               pointNum++) {
            int pIndex = indices.get(pointNum + chunkNumber * 170);
            if (outputNumber > RAVE_OUTPUTS/2) pIndex += 1050;
            thisUniverseIndices[pointNum] = pIndex;
           }
          logger.info("thisUniverseIndices.length: " + thisUniverseIndices.length);
          for (int k = 0; k < thisUniverseIndices.length; k++) {
            logger.info("" + thisUniverseIndices[k] + ",");
          }
          logger.log(Level.INFO, "Adding datagram: output=" + outputNumber + " universe=" + universeNumber + " points=" + pointNum);
          ArtNetDatagram artNetDatagram = new ArtNetDatagram(thisUniverseIndices, dataLength*3, universeNumber);
          try {
            artNetDatagram.setAddress(artNetIpAddress).setPort(artNetIpPort);
          } catch (UnknownHostException uhex) {
            logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
          }
          datagrams.add(artNetDatagram);
          // We have either added 170 points and maybe less if it is the last few points for a given output.  Each
          // time we build a datagram for a chunk, we need to increment the universeNumber, reset the pointNum to zero,
          // and increment our chunkNumber
          ++universeNumber;
          pointNum = 0;
          chunkNumber++;
        }
        outputNumber++;
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
      lx.engine.output.addChild(datagramOutput);
    } else {
      logger.log(Level.SEVERE, "Did not configure output, error during LXDatagramOutput init");
    }
  }
}

package art.lookingup;

import art.lookingup.ui.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXModel;
import heronarts.lx.studio.LXStudio;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import processing.core.PApplet;

public class ConeDown extends PApplet {

  static {
    System.setProperty(
        "java.util.logging.SimpleFormatter.format",
        "%3$s: %1$tc [%4$s] %5$s%6$s%n");
  }

  /**
   * Set the main logging level here.
   *
   * @param level the new logging level
   */
  public static void setLogLevel(Level level) {
    // Change the logging level here
    Logger root = Logger.getLogger("");
    root.setLevel(level);
    for (Handler h : root.getHandlers()) {
      h.setLevel(level);
    }
  }


  /**
   * Adds logging to a file. The file name will be appended with a dash, date stamp, and
   * the extension ".log".
   *
   * @param prefix prefix of the log file name
   * @throws IOException if there was an error opening the file.
   */
  public static void addLogFileHandler(String prefix) throws IOException {
    String suffix = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    Logger root = Logger.getLogger("");
    Handler h = new FileHandler(prefix + "-" + suffix + ".log");
    h.setFormatter(new SimpleFormatter());
    root.addHandler(h);
  }

  private static final Logger logger = Logger.getLogger(ConeDown.class.getName());

  public static void main(String[] args) {

    //PApplet.main(ConeDown.class.getName(), args);
    String[] newArgs = new String[2];
    String[] sketchArgs = {"--density=" + 2, "art.lookingup.ConeDown"};
    File hdpiFlag = new File("hdpi");
    if (hdpiFlag.exists())
      pixelDensity = 2;
    PApplet.main(concat(sketchArgs, args));
  }

  private static final String LOG_FILENAME_PREFIX = "lookinguparts";

  // Reference to top-level LX instance
  private heronarts.lx.studio.LXStudio lx;

  public static PApplet pApplet;
  public static final int GLOBAL_FRAME_RATE = 33;

  //public static final Optional<Float> DEFAULT_ZOOM = Optional.empty();
  public static final Optional<Float> DEFAULT_ZOOM = Optional.of(10f);

  public static RainbowOSC rainbowOSC;

  public static OutputMapping outputMap;
  public static UIGammaSelector gammaControls;
  public static UIModeSelector modeSelector;
  public static UIAudioMonitorLevels audioMonitorLevels;
  public static UIPixliteConfig pixliteConfig;
  public static UIMidiControl uiMidiControl;
  public static com.giantrainbow.OSCSensor oscSensor;
  public static OSCSensorUI oscSensorUI;
  public static UISensorOverride sensorOverrideUI;
  public static UIFirmata firmataPortUI;
  public static UIGalacticJungle galacticJungle;

  // The standard projections provide anti-aliasing at levels from some (2) to plenty (4).
  private enum ProjectionMode {
    /** Takes the best available projection */
    BEST_AVAILABLE,

    /** Blocks until the requested projection is ready */
    BLOCKING
  }
  private static final ProjectionMode PROJECTION_MODE = ProjectionMode.BEST_AVAILABLE;
  private static final Set<Integer> LEVELS_TO_COMPUTE = ImmutableSet.of(1, 2, 4);
  public static int DEFAULT_SUPER_SAMPLING = 1;
  public static int MIN_SUPER_SAMPLING = Ordering.natural().min(LEVELS_TO_COMPUTE);
  public static int MAX_SUPER_SAMPLING = Ordering.natural().max(LEVELS_TO_COMPUTE);
  private static final Map<Integer, Future<? extends Projection>> projectionMap = Maps.newConcurrentMap();


  public static Autodio autoAudio;
  public static AutodioUI autoAudioUI;

  private static int pixelDensity = 2;
  static final public boolean FULLSCREEN = false;
  static final public int WIDTH = 1024;
  static final public int HEIGHT = 768;


  @Override
  public void settings() {
    if (FULLSCREEN) {
      fullScreen(PApplet.P3D);
    } else {
      size(WIDTH, HEIGHT, PApplet.P3D);
    }
    pixelDensity(pixelDensity);
  }

  // Returns the best currently available projection falling back to MIN if non are ready
  public static Projection getProjection(int requested) {
    int constrained = Math.max(Math.min(requested, MAX_SUPER_SAMPLING), MIN_SUPER_SAMPLING);
    try {
      return PROJECTION_MODE == ProjectionMode.BLOCKING
          ? projectionMap.get(constrained).get()
          : LEVELS_TO_COMPUTE.stream()
              .sorted(Ordering.natural().reversed())
              .filter(l -> l <= constrained)
              .<Future<? extends Projection>>map(projectionMap::get)
              .filter(Future::isDone)
              .findFirst()
              .orElse(projectionMap.get(MIN_SUPER_SAMPLING))
              .get();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException("Error initializing projection:", e);
    }
  }

  /**
   * Registers all patterns and effects that LX doesn't already have registered.
   * This check is important because LX just adds to a list.
   *
   * @param lx the LX environment
   */
  private void registerAll(LXStudio lx) {
    List<Class<? extends LXPattern>> patterns = lx.getRegisteredPatterns();
    List<Class<? extends LXEffect>> effects = lx.getRegisteredEffects();
    final String parentPackage = getClass().getPackage().getName();

    try {
      ClassPath classPath = ClassPath.from(getClass().getClassLoader());
      for (ClassPath.ClassInfo classInfo : classPath.getAllClasses()) {
        // Limit to this package and sub-packages
        if (!classInfo.getPackageName().startsWith(parentPackage)) {
          continue;
        }
        Class<?> c = classInfo.load();
        if (Modifier.isAbstract(c.getModifiers())) {
          continue;
        }
        if (LXPattern.class.isAssignableFrom(c)) {
          Class<? extends LXPattern> p = c.asSubclass(LXPattern.class);
          if (!patterns.contains(p)) {
            lx.registerPattern(p);
            logger.info("Added pattern: " + p);
          }
        } else if (LXEffect.class.isAssignableFrom(c)) {
          Class<? extends LXEffect> e = c.asSubclass(LXEffect.class);
          if (!effects.contains(e)) {
            lx.registerEffect(e);
            logger.info("Added effect: " + e);
          }
        }
      }
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Error finding pattern and effect classes", ex);
    }
  }

  static String readFile(String path, Charset encoding)
      throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  @Override
  public void setup() {
    // Processing setup, constructs the window and the LX instance
    pApplet = this;

    try {
      addLogFileHandler(LOG_FILENAME_PREFIX);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, "Error creating log file: " + LOG_FILENAME_PREFIX, ex);
    }

    LXModel model = ConeDownModel.createModel();

    ListeningExecutorService executor =
        MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(LEVELS_TO_COMPUTE.size()));
    ListenableFuture<TrueProjection> trueProjection =
        executor.submit(() -> new TrueProjection(model));
    LEVELS_TO_COMPUTE.stream()
        .filter(i -> LEVELS_TO_COMPUTE.contains(i))
        .peek(i -> logger.info("Computing " + i + "x projection asynchronously"))
        .forEach(i -> projectionMap.put(i, i <= 1
            ? trueProjection
            : executor.submit(() -> new AntiAliased(model, i))));

    LXStudio.Flags flags = new LXStudio.Flags();
    //flags.showFramerate = false;
    //flags.isP3LX = true;
    //flags.immutableModel = true;
    flags.useGLPointCloud = false;
    flags.startMultiThreaded = false;
    //flags.showFramerate = true;

    logger.info("Current renderer:" + sketchRenderer());
    logger.info("Current graphics:" + getGraphics());
    logger.info("Current graphics is GL:" + getGraphics().isGL());
    //logger.info("Multithreaded hint: " + MULTITHREADED);
    //logger.info("Multithreaded actually: " + (MULTITHREADED && !getGraphics().isGL()));
    lx = new LXStudio(this, flags, model);

    lx.ui.setResizable(true);
    DEFAULT_ZOOM.ifPresent(lx.ui.preview::setRadius);

    // Put this here because it needs to be after file loads in order to find appropriate channels.
    modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx, audioMonitorLevels).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    modeSelector.standardMode.setActive(true);
    frameRate(GLOBAL_FRAME_RATE);
  }


  public void initialize(final LXStudio lx, LXStudio.UI ui) {
    // Add custom components or output drivers here
    // Register settings
    // lx.engine.registerComponent("yomigaeSettings", new Settings(lx, ui));

    // Common componentaConeDows
    // registry = new Registry(this, lx);

    // Register any patterns and effects LX doesn't recognize
    registerAll(lx);
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    outputMap = (OutputMapping) new OutputMapping(ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    sensorOverrideUI = (UISensorOverride) new UISensorOverride(lx.ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    firmataPortUI = (UIFirmata) new UIFirmata(lx.ui, lx).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    ConeFirmata.reloadFirmata(firmataPortUI.getStringParameter(UIFirmata.FIRMATA_PORT).getString(), firmataPortUI.numTiles,
        firmataPortUI.getDiscreteParameter(UIFirmata.START_PIN).getValuei(), firmataPortUI.getPinParameters());
    oscSensor = new com.giantrainbow.OSCSensor(lx);
    lx.engine.registerComponent("oscsensor", oscSensor);
    //modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx, audioMonitorLevels).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    //modeSelector = (UIModeSelector) new UIModeSelector(lx.ui, lx, audioMonitorLevels).setExpanded(true).addToContainer(lx.ui.leftPane.global);
    oscSensorUI = (OSCSensorUI) new OSCSensorUI(lx.ui, lx, oscSensor).setExpanded(false).addToContainer(lx.ui.leftPane.global);

    autoAudio = new Autodio(lx);
    lx.engine.registerComponent("autoAudio", autoAudio);
    autoAudioUI = (AutodioUI) new AutodioUI(lx.ui, lx, autoAudio).setExpanded(false).addToContainer(lx.ui.leftPane.global);

    audioMonitorLevels = (UIAudioMonitorLevels) new UIAudioMonitorLevels(lx.ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    gammaControls = (UIGammaSelector) new UIGammaSelector(lx.ui).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    uiMidiControl = (UIMidiControl) new UIMidiControl(lx.ui, lx, modeSelector).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    pixliteConfig = (UIPixliteConfig) new UIPixliteConfig(lx.ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
    galacticJungle = (UIGalacticJungle) new UIGalacticJungle(lx.ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);

    lx.engine.midi.addListener(uiMidiControl);
    if (enableOutput) {
      Output.configurePixliteOutput(lx);
      // Output.configureUnityArtNet(lx);
      // By default the output in Galactic Jungle is disabled.
      //Output.outputGalacticBrightnessDown(lx);
      Output.outputGalactic(lx);
    }
    if (disableOutputOnStart)
      lx.engine.output.enabled.setValue(false);

    rainbowOSC = new RainbowOSC(lx);

    // Disable preview for faster UI.
    //lx.ui.preview.setVisible(false);
  }

  public void draw() {
    // All is handled by LX Studio
  }

  // Configuration flags
  private final static boolean MULTITHREADED = false;  // Disabled for anything GL
  // Enable at your own risk!
  // Could cause VM crashes.
  private final static boolean RESIZABLE = true;

  // Helpful global constants
  final static float INCHES = 1.0f / 12.0f;
  final static float IN = INCHES;
  final static float FEET = 1.0f;
  final static float FT = FEET;
  final static float CM = IN / 2.54f;
  final static float MM = CM * .1f;
  final static float M = CM * 100;
  final static float METER = M;

  public static final boolean enableOutput = true;
  public static final boolean disableOutputOnStart = true;

  public static final int LEDS_PER_UNIVERSE = 170;
}

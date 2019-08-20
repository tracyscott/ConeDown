package art.lookingup.patterns;

import static art.lookingup.ConeDown.GLOBAL_FRAME_RATE;
import static processing.core.PConstants.P2D;
import static processing.core.PConstants.P3D;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.Projection;
import com.google.common.annotations.Beta;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;

import java.util.Random;

import processing.core.PGraphics;

/** Abstract base class for all Processing PGraphics drawing and mapping to the Rainbow. */
abstract class PGBase extends RPattern {
  public final CompoundParameter fpsKnob =
      new CompoundParameter("Fps", GLOBAL_FRAME_RATE, 0.0, GLOBAL_FRAME_RATE + 10)
          .setDescription("Controls the frames per second.");

  public final DiscreteParameter renderTarget =
      new DiscreteParameter("Tgt", 0, 0, 6);
  public final DiscreteParameter superSampling =
      new DiscreteParameter("Super", 0, 0, ConeDown.MAX_SUPER_SAMPLING + 1);

  protected PGraphics pg;

  protected double currentFrame = 0.0;
  protected int previousFrame = -1;
  protected double deltaDrawMs = 0.0;
  protected String drawMode = "";
  protected int renderWidth = 0;
  protected int renderHeight = 0;
  protected Projection projection;
  protected boolean renderFullSize = false;

  /** Indicates whether {@link #setup()} has been called. */
  private boolean setupCalled;
  // TODO: Fix this whole pattern lifecycle thing

  /** For subclasses to use. It's better to have one source. */
  protected static final Random random = new Random();

  // For P3D, we need to be on the UI/GL Thread.  We should always be on the GL thread
  // during initialization because we start with Multithreading off.  If somebody enables
  // the Engine thread in the UI we don't want to crash so we will keep track of the GL
  // thread and if the current thread in our run() method doesn't match glThread we will just
  // skip our GL render (image will freeze).
  // UPDATE: Removed this check because Processing already handles this.

  // NOTE(Shawn): The instance is sometimes created on a different thread than the thread
  //              that calls run(). We may not be able to use the value we obtain in the
  //              constructor to check for the GL thread. And besides, Processing makes an
  //              effort to do this anyway, in PGraphicsOpenGL.beginDraw() with the
  //              GL_THREAD_NOT_CURRENT message.

  public PGBase(LX lx, int width, int height, String drawMode) {
    super(lx);
    this.drawMode = drawMode;
    renderWidth = width * getSuperSampling();
    renderHeight = height * getSuperSampling();
    projection = ConeDown.getProjection(getSuperSampling());

    createPGraphics();
    addParameter(fpsKnob);
    addParameter(renderTarget);
    addParameter(superSampling);

    renderTarget.addListener((LXParameter parameter)->{
	updateParams();
    });
    superSampling.addListener((LXParameter parameter)->{
	updateParams();
    });
  }

  protected void updateParams() {
      int mode = renderTarget.getValuei();
      switch (mode) {
      case 0:  // Default full render.
	  renderWidth = ConeDownModel.POINTS_WIDE;
	  renderHeight = ConeDownModel.POINTS_HIGH;
	  break;
      case 1:
	  renderWidth = ConeDownModel.dancePointsWide;
	  renderHeight = ConeDownModel.dancePointsHigh;
	  break;
      case 2:
	  renderWidth = ConeDownModel.scoopPointsWide;
	  renderHeight = ConeDownModel.scoopPointsHigh;
	  break;
      case 3:
	  renderWidth = ConeDownModel.scoopPointsWide;  // Note: full width render
	  renderHeight = ConeDownModel.conePointsHigh;
	  break;
      case 4:  // Scoop + cone
	  renderWidth = Math.max(ConeDownModel.conePointsWide, ConeDownModel.scoopPointsWide);
	  renderHeight = ConeDownModel.scoopPointsHigh + ConeDownModel.conePointsHigh;
	  break;
      case 5:  // Dancefloor + scoop
	  renderWidth = ConeDownModel.scoopPointsWide;
	  renderHeight = ConeDownModel.scoopPointsHigh + ConeDownModel.dancePointsHigh;
	  break;
      }
      if (renderFullSize) {
	  renderWidth = ConeDownModel.POINTS_WIDE;
	  renderHeight = ConeDownModel.POINTS_HIGH;
      }
      renderWidth *= getSuperSampling();
      renderHeight *= getSuperSampling();
      projection = ConeDown.getProjection(getSuperSampling());
      createPGraphics();
      
  }    

  protected void createPGraphics() {
    if (P3D.equals(drawMode) || P2D.equals(drawMode)) {
      pg = ConeDown.pApplet.createGraphics(renderWidth, renderHeight, drawMode);
    } else {
      pg = ConeDown.pApplet.createGraphics(renderWidth, renderHeight);
    }
    pg.beginDraw();
    pg.endDraw();
  }

  /**
   * Subclasses <em>must</em> call {@code super.onInactive()}.
   */
  @Override
  public void onInactive() {
    setupCalled = false;
    tearDown();
  }

  @Override
  public void render(double deltaMs) {
    if (!setupCalled) {
      pg.beginDraw();
      setup();
      pg.endDraw();
      setupCalled = true;
    }

    double fps = fpsKnob.getValue();
    currentFrame += (deltaMs / 1000.0) * fps;
    // We don't call draw() every frame so track the accumulated deltaMs for them.
    deltaDrawMs += deltaMs;
    if ((int) currentFrame > previousFrame) {
      // Time for new frame.  Draw
      // if glThread == null this is the default Processing renderer so it is always
      // okay to draw.  If it is not-null, we need to make sure the pattern is
      // executing on the glThread or else Processing will crash.
      // UPDATE: Removed this code because Processing already makes a best effort,
      //         and, in addition, the program crashes anyway if multithreading is
      //         set to 'true'.
      preDraw(deltaDrawMs);
      pg.beginDraw();
      draw(deltaDrawMs);
      pg.endDraw();

      previousFrame = (int) currentFrame;
      deltaDrawMs = 0.0;
    }
    // Don't let current frame increment forever.  Otherwise float will
    // begin to lose precision and things get wonky.
    if (currentFrame > 10000.0) {
      currentFrame = 0.0;
      previousFrame = -1;
    }
    imageToPoints();
  }

  // Responsible for projecting points into `colors`.
  protected abstract void imageToPoints();

  /**
   * Called once before all the draw calls, similar to how a Processing sketch has a setup()
   * call. onActive()/onInactive() call timings appear not to be able to be treated the same
   * as conceptual setup() and tearDown() calls.
   * <p>
   * Calls to {@link PGraphics#beginDraw()} and {@link PGraphics#endDraw()} will surround a call
   * to this method.</p>
   */
  protected void setup() {
  }

  /**
   * Called when {@link #onInactive()} is called. That method has been made {@code final}
   * so that it can guarantee {@link #setup()} is called. This may change in the future.
   */
  @Beta
  protected void tearDown() {
  }

  // Implement PGGraphics drawing code here.  PGTexture handles beginDraw()/endDraw();
  protected abstract void draw(double deltaDrawMs);

  protected void preDraw(double deltaDrawMs) {
  }

  protected int getSuperSampling() {
      return Math.max(ConeDown.MIN_SUPER_SAMPLING,
		      Math.min(ConeDown.MAX_SUPER_SAMPLING,
			       (int) superSampling.getValue()));
  }
}

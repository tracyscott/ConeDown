package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

// Abstract base pattern for intercepting rendering calls.  Not yet used for ConeDown.
abstract public class RPattern extends LXPattern {

  public RPattern(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    render(deltaMs);
  }

  /**
   * Rave patterns have access to both sides of the installation.  Each side is a subset of a 46x46 grid.
   * The first half of the LXPoints are on the front side.  The second half of the points are on the backside.
   * Most existing patterns from Rainbow will default to isMirrored = true which means that we will copy
   * the colors from the front side to the backside./
   * @param deltaMs
   */
  public abstract void render(double deltaMs);
}

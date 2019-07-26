package art.lookingup.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

// Abstract base pattern for intercepting rendering calls.
abstract public class RPattern extends LXPattern {

  public RPattern(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    render(deltaMs);
  }

  /** Render your pattern here.
   * @param deltaMs
   */
  public abstract void render(double deltaMs);
}

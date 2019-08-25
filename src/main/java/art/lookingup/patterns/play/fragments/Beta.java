package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Multi;
import art.lookingup.patterns.play.Parameter;
import heronarts.lx.LX;
import org.apache.commons.math3.distribution.BetaDistribution;

public class Beta extends Multi {
  final Parameter alpha;
  final Parameter beta;

  float invA, invB;
  final int[] srcH;
  final int[] tgtH;

  BetaDistribution dist;

  public static class Factory extends BaseFactory {
    FragmentFactory source;

    public Factory(String fragName, FragmentFactory source) {
      super(fragName, source);
      this.source = source;
    }

    public Fragment create(LX lx, int width, int height) {
      // TODO this function can be eliminated for most (all?) callers
      // w/ help from BaseFactory.
      return new Beta(toString(), lx, width, height, source.create(lx, width, height));
    }
  };

  protected Beta(String fragName, LX lx, int width, int height, Fragment source) {
    super(fragName, lx, width, height, source);
    this.alpha = newParameter("alpha", 1, 0.1f, 10);
    this.beta = newParameter("beta", 1, 0.1f, 10);
    this.srcH = new int[height + 1];
    this.tgtH = new int[height + 1];
    noDefaultKnobs();
  }

  void update() {
    if (invA == alpha.value() && invB == beta.value()) {
      return;
    }

    invA = alpha.value();
    invB = beta.value();
    dist = new BetaDistribution(invA, invB);

    int idx = 0;
    int ty = 0;

    // j indexes source height
    for (int j = 0; j < height; j++) {
      // cp indexes target height
      int py = (int) (dist.cumulativeProbability((float) j / height) * height);
      if (py == ty) {
        continue;
      }
      srcH[idx] = j;
      tgtH[idx] = py;
      ty = py;
      idx++;
    }
    srcH[idx] = height;
    tgtH[idx] = height;
    idx++;
  }

  @Override
  public void drawFragment() {
    update();

    int s = 0;
    int t = 0;
    int idx = 0;
    for (; s < height; ) {
      area.copy(fragments[0].image, 0, s, width, srcH[idx] - s, 0, t, width, tgtH[idx] - t);
      s = srcH[idx];
      t = tgtH[idx];
      idx++;
    }
  }
}

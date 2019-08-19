package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Parameter;
import heronarts.lx.LX;
import processing.core.PImage;

public class Strange extends Fragment {
  final Parameter period;

  final PImage textures[];

  static final int patterns[][] = {
    {0, 1, 2, 3},
    {4, 5, 6, 7},
    {8, 1, 9, 6, 8, 6, 9, 1},
  };

  public static class Factory implements FragmentFactory {
    public Factory() {}

    public Fragment create(LX lx, int width, int height) {
      return new Strange(lx, width, height);
    }
  };

  public Strange(LX lx, int width, int height) {
    super(width, height);

    this.period = newParameter("period", 10, 1, 20);
    this.textures = new PImage[inputs.length];
  }

  String inputs[] = {
    "images/blend-red-blue.png",
    "images/blend-green-yellow.png",
    "images/blend-blue-red.png",
    "images/blend-yellow-green.png",
    "images/blend-red-green.png",
    "images/blend-yellow-blue.png",
    "images/blend-green-red.png",
    "images/blend-blue-yellow.png",
    "images/blend-blue-green.png",
    "images/blend-green-blue.png",

    // Not so useful:
    //
    // "images/blend-red-yellow.png",
    // "images/blend-yellow-red.png",
  };

  public void setup() {
    super.setup();

    for (int i = 0; i < inputs.length; i++) {
      PImage source = pattern.app.loadImage(inputs[i]);
      source.loadPixels();

      area.beginDraw();
      area.pushMatrix();
      area.copy(source, 0, 0, source.width, source.height, 0, 0, width / 2, height);
      area.translate(width, 0);
      area.scale(-1, 1);
      area.copy(source, 0, 0, source.width, source.height, 0, 0, width / 2, height);
      area.popMatrix();
      area.endDraw();
      area.loadPixels();

      this.textures[i] = new PImage(width, height);
      this.textures[i].copy(area.get(), 0, 0, width, height, 0, 0, width, height);
    }
  }

  public static final float positionPeriod = 0.1f;
  public static final float periodPeriod = 1f;

  float pElapsed;

  @Override
  public void preDrawFragment(float vdelta) {
    super.preDrawFragment(vdelta);

    pElapsed += Math.abs(vdelta * period.value());
  }

  // TODO do not supersample this pattern.

  @Override
  public void drawFragment() {

    int periodNum = (int) (pElapsed / periodPeriod);

    int posIdx = periodNum % patterns.length;

    int positions[] = patterns[posIdx];

    int pattern = positions[((int) (Math.abs(elapsed()) / positionPeriod)) % positions.length];

    PImage img = textures[pattern];

    area.copy(img, 0, 0, width, height, 0, 0, width, height);
  }
}

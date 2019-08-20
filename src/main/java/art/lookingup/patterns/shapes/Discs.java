package art.lookingup.patterns.shapes;

import static processing.core.PConstants.RGB;

import art.lookingup.colors.Colors;
import art.lookingup.patterns.play.Parameter;
import processing.core.PApplet;
import processing.core.PImage;

public class Discs {

  // Texture alpha mask
  public int textureA[];

  public PImage textureLch; // Uniform
  public PImage textureHsv; // Bright
  public PImage texture; // Blended from the above

  final Parameter bright;
  final PApplet app;
  double lastBright;

  public Discs(PApplet app, Parameter bright) {
    this.app = app;
    this.bright = bright;
    this.lastBright = -1;

    loadTextures();
  }

  void loadTextures() {
    // The texture files are square.
    this.textureLch = app.loadImage("images/lch-disc-level=0.60-sat=1.00.png");
    this.textureHsv = app.loadImage("images/hsv-disc-level=1.00-sat=1.00.png");

    this.textureLch.loadPixels();
    this.textureHsv.loadPixels();

    int ta[] = new int[this.textureLch.width * this.textureLch.width];
    PImage img = app.createImage(this.textureLch.width, this.textureLch.width, RGB);
    img.loadPixels();

    this.textureA = ta;
    this.texture = img;

    setTexture(bright.value());
  }

  void setTexture(double bright) {
    if (lastBright == bright) {
      return;
    }
    this.lastBright = bright;
    double dim = 1. - bright;
    for (int i = 0; i < this.textureLch.width; i++) {
      for (int j = 0; j < this.textureLch.width; j++) {
        int idx = i + j * this.textureLch.width;

        int lr = Colors.red(this.textureLch.pixels[idx]);
        int lg = Colors.green(this.textureLch.pixels[idx]);
        int lb = Colors.blue(this.textureLch.pixels[idx]);
        int la = Colors.alpha(this.textureLch.pixels[idx]);

        int hr = Colors.red(this.textureHsv.pixels[idx]);
        int hg = Colors.green(this.textureHsv.pixels[idx]);

        int hb = Colors.blue(this.textureHsv.pixels[idx]);

        this.textureA[idx] = la;
        this.texture.pixels[idx] =
            Colors.rgb(
                (int) (dim * (double) lr + bright * (double) hr),
                (int) (dim * (double) lg + bright * (double) hg),
                (int) (dim * (double) lb + bright * (double) hb));
      }
    }
    this.texture.mask(this.textureA);
    this.texture.updatePixels();
  }

  public PImage getTexture() {
    setTexture(bright.value());
    return this.texture;
  }
};

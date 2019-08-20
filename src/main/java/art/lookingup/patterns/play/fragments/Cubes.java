package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.P3D;
import static processing.core.PConstants.PI;

import art.lookingup.colors.Colors;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.patterns.shapes.Space3D;
import heronarts.lx.LX;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;

public class Cubes extends Fragment {

  public final int MAX_SIZE = 150;
  public final int MAX_CUBES = 200;

  public final float ROLL_RATE = 4;
  public final float MSHZ = 1f;

  public final Vector3f DEFAULT_EYE = new Vector3f(0, Space3D.MIN_Y + 2.5f, 25f);

  public final Parameter rollKnob;
  public final Parameter countKnob;
  public final Parameter paletteKnob;
  public final Parameter randomPaletteKnob;
  public final Parameter eyex;
  public final Parameter eyey;
  public final Parameter eyez;
  public final Parameter camx;
  public final Parameter camy;
  public final Parameter camz;

  public int[] palette;

  public static class Factory extends BaseFactory {
    public Factory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      return new Cubes(toString(), lx, width, height);
    }
  };

  public Cubes(String fragName, LX lx, int width, int height) {
    super(fragName, width, height, P3D);
    this.palette = Colors.RAINBOW_PALETTE;

    this.rollKnob = newParameter("roll", -0.15f, -1, 1);
    this.countKnob = newParameter("count", 25, 10, MAX_CUBES);
    this.paletteKnob = newParameter("palette", 0, 0, Colors.ALL_PALETTES.length - 1);
    this.randomPaletteKnob = newParameter("randpal", 0, 0, 1);
    this.eyex = newParameter("eyex", width / 2, -width, width);
    this.eyey = newParameter("eyey", height / 2, -height, height);
    this.eyez = newParameter("eyez", 10, -100, 100);
    this.camx = newParameter("camx", width / 4, -width, width);
    this.camy = newParameter("camy", height / 4, -height, height);
    this.camz = newParameter("camz", 5, -100, 100);

    space = new Space3D(DEFAULT_EYE);
    boxes = new Box[MAX_CUBES];
    Random rnd = new Random();

    eye = new PVector(space.eye.x, space.eye.y, space.eye.z);
    center = new PVector(space.center.x, space.center.y, space.center.z);

    int trials = 0;
    for (int i = 0; i < boxes.length; i++) {
      Box b;
      do {
        b = new Box(rnd);
        trials++;
      } while (!space.testBox(
          -b.radius(), -b.radius(), -b.radius(), b.radius(), b.radius(), b.radius()));

      boxes[i] = b;
    }

    System.err.printf(
        "Found boxes by %.1f%% rejection sampling\n", 100. * (float) boxes.length / (float) trials);
  }

  Box boxes[];
  double relapsed;
  PImage texture;
  Space3D space;
  PVector eye;
  PVector center;

  public class Box {
    PVector R;
    int W;

    float radius() {
      return (float) W / 2;
    }

    float partW() {
      return W / (float) palette.length;
    }

    Box(Random rnd) {
      W = (int) (rnd.nextFloat() * MAX_SIZE);
      R = PVector.random3D();
    }

    void drawPart(float zoff, int C, int part) {
      area.beginShape();

      area.fill(C);

      float xmin = -radius() + (float) part * partW();
      float xmax = xmin + partW();

      area.vertex(xmin, -radius(), zoff);
      area.vertex(xmax, -radius(), zoff);
      area.vertex(xmax, +radius(), zoff);
      area.vertex(xmin, +radius(), zoff);
      area.endShape();
    }

    void drawRect(float zoff) {
      for (int i = 0; i < palette.length; i++) {
        drawPart(zoff, palette[i], i);
      }
    }

    void drawSides() {
      area.pushMatrix();

      drawRect(radius());
      drawRect(-radius());

      area.popMatrix();
    }

    void draw3Sides() {
      drawSides();

      area.pushMatrix();
      area.rotateX(PI / 2);
      drawSides();
      area.popMatrix();

      area.pushMatrix();
      area.rotateY(PI / 2);
      drawSides();
      area.popMatrix();
    }

    void draw() {
      area.pushMatrix();

      area.rotate((float) (elapsed()), R.x, R.y, R.z);

      draw3Sides();

      area.popMatrix();
    }
  };

  public void onActive() {
    super.onActive();
    if (randomPaletteKnob.value() > 0.99f) {
      int paletteNumber = ThreadLocalRandom.current().nextInt(0, Colors.ALL_PALETTES.length);
      palette = Colors.ALL_PALETTES[paletteNumber];
    } else {
      palette = Colors.ALL_PALETTES[(int) paletteKnob.value()];
    }
  }

  @Override
  public void preDrawFragment(float vdelta) {
    super.preDrawFragment(vdelta);

    relapsed += vdelta * rollKnob.value();
  }

  @Override
  public void drawFragment() {
    // TODO
    // double speed = 0;
    // double knob = Math.abs(speedKnob.value());
    // double direction = knob < 0 ? -1. : 1.;

    // if (knob > 10) {
    //   speed = Math.log10(knob);
    // } else {
    //   speed = knob / 10;
    // }
    // speed *= direction * SPEED_RATE;

    if (randomPaletteKnob.value() <= 0.99f) {
      palette = Colors.ALL_PALETTES[(int) paletteKnob.value()];
    }

    area.noStroke();
    area.background(0);

    float theta = ((float) relapsed) * ROLL_RATE * MSHZ;

    area.camera(
        eye.x + eyex.value(),
        eye.y + eyey.value(),
        eye.z + eyez.value(),
        center.x + camx.value(),
        center.y + camx.value(),
        center.z + camx.value(),
        (float) Math.sin(theta),
        (float) Math.cos(theta),
        0);

    for (int i = 0; i < (int) countKnob.value(); i++) {
      if (i >= boxes.length) {
        break;
      }
      boxes[i].draw();
    }
  }
}

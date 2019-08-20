package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.PI;

import art.lookingup.colors.Colors;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.patterns.shapes.Space3D;
import heronarts.lx.LX;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.joml.Vector3f;
import processing.core.PImage;
import processing.core.PVector;

public class CubeLineup extends Fragment {

  public final int MAX_SIZE = 150;
  public final int MAX_CUBES = 200;
  public final float MAX_SPEED = 100;
  public final float SPEED_RATE = 4;

  public final float ROLL_RATE = 4;
  public final float MSHZ = 1.f / 10000.f;

  public final Vector3f DEFAULT_EYE = new Vector3f(0, Space3D.MIN_Y + 6, 60);

  public final Parameter speedKnob;
  public final Parameter rollKnob;
  public final Parameter countKnob;
  public final Parameter paletteKnob;
  public final Parameter randomPaletteKnob;

  public int[] palette;

  public CubeLineup(String fragName, LX lx, int width, int height) {
    super(fragName, width, height);
    this.palette = Colors.RAINBOW_PALETTE;

    this.speedKnob = newParameter("Speed", (float) Math.sqrt(MAX_SPEED), -MAX_SPEED, MAX_SPEED);
    this.rollKnob = newParameter("Roll", -0.15f, -1, 1);
    this.countKnob = newParameter("Count", 25, 10, MAX_CUBES);
    this.paletteKnob = newParameter("Palette", 0, 0, Colors.ALL_PALETTES.length);
    this.randomPaletteKnob = newParameter("RandomPlt", 1, 0, 1);

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
  double elapsed;
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

      area.rotate((float) (elapsed / 10000), R.x, R.y, R.z);

      draw3Sides();

      area.popMatrix();
    }
  };

  public void onActive() {
    super.onActive();
    if (randomPaletteKnob.getValueb()) {
      int paletteNumber = ThreadLocalRandom.current().nextInt(0, Colors.ALL_PALETTES.length);
      palette = Colors.ALL_PALETTES[paletteNumber];
    } else {
      palette = Colors.ALL_PALETTES[paletteKnob.getValuei()];
    }
  }

  @Override
  public void drawFragment() {
    double speed = 0;
    double knob = Math.abs(speedKnob.getValue());
    double direction = knob < 0 ? -1. : 1.;

    if (knob > 10) {
      speed = Math.log10(knob);
    } else {
      speed = knob / 10;
    }
    speed *= direction * SPEED_RATE;
    elapsed += deltaMs * speed;

    double rollspeed = rollKnob.getValue();
    relapsed += deltaMs * rollspeed;

    area.noStroke();
    area.background(0);

    float theta = ((float) relapsed) * ROLL_RATE * MSHZ;

    area.camera(
        eye.x,
        eye.y,
        eye.z,
        center.x,
        center.y,
        center.z,
        (float) Math.sin(theta),
        (float) Math.cos(theta),
        0);

    for (int i = 0; i < (int) countKnob.getValue(); i++) {
      if (i >= boxes.length) {
        break;
      }
      boxes[i].draw();
    }
  }
}

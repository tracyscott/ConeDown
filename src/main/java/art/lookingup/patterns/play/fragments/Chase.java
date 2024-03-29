package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.CLAMP;

import art.lookingup.patterns.pacman.PacmanBoard;
import art.lookingup.patterns.pacman.PacmanGame;
import art.lookingup.patterns.pacman.PacmanSprite;
import art.lookingup.patterns.play.BaseFactory;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;
import processing.core.PImage;

public class Chase extends Fragment {
  public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
  public static final int BOARD_HEIGHT = PacmanBoard.BOARD_HEIGHT;
  public static final int BOARD_WIDTH = PacmanBoard.BOARD_WIDTH;

  public static final float HALF_WIDTH = BOARD_WIDTH / 2f;
  public static final float HALF_HEIGHT = BOARD_HEIGHT / 2f;

  public static final float FULL_HEIGHT = BOARD_HEIGHT;
  public static final float FULL_WIDTH = BOARD_WIDTH;

  public static final float MAX_GAME_MILLIS = 1000000;
  public static final float MAX_D_FRACTION = 0.2f;

  PacmanBoard board;
  PacmanGame game;
  PacmanSprite pac;
  PImage gboard;
  PImage ctexture;
  float yfactor;

  float rainbowLX;
  float rainbowLY;
  float rainbowRX;
  float rainbowRY;

  float rainbowYOffset;
  float rainbowLROffset;
  float dFraction;
  boolean focus;

  public static class Factory extends BaseFactory {
    public Factory(String fragName) {
      super(fragName);
    }

    public Fragment create(LX lx, int width, int height) {
      float yfact = 2.1f;
      return new Chase(toString(), width, height, yfact);
    }
  };

  public Chase(String fragName, int width, int height, float yfactor) {
    super(fragName, width, height);
    this.yfactor = yfactor;
  }

  public void create(Pattern p) {
    super.create(p);

    this.board = new PacmanBoard(p.app);
    this.pac = new PacmanSprite(p.app);
    this.game = new PacmanGame(p.app, this.board, this.pac);
    this.ctexture = p.app.loadImage("images/xyz-square-lookup.png");
    this.ctexture.loadPixels();

    area.textureWrap(CLAMP);
  }

  void setControlPoints(float aX, float aY, float bX, float bY, float D) {
    dFraction = D / PacmanBoard.MAX_DISTANCE;

    if (focus) {
      focus = dFraction > MAX_D_FRACTION;
    } else {
      focus = dFraction > (MAX_D_FRACTION * 1.1);
    }

    float minSpread = width / 7f;
    float maxSpread = width / 1.75f;

    float spread = minSpread + (maxSpread - minSpread) * dFraction;
    float halfw = width / 2f;
    float halfs = spread / 2f;

    rainbowLX = halfw - halfs;
    rainbowRX = halfw + halfs;
    rainbowLY = height / (float) yfactor;
    rainbowRY = height / (float) yfactor;
    rainbowLROffset = (float) rainbowRX - rainbowLX;
  }

  @Override
  public void preDrawFragment(float deltaMs) {
    super.preDrawFragment(deltaMs);

    float e = elapsed() * 2500;

    if (game.finished() || e > MAX_GAME_MILLIS) {
      this.board.reset();
      game = new PacmanGame(pattern.app, this.board, this.pac);
    }
    game.render(e, null);
    gboard = game.get();
  }

  public void drawFragment() {
    boolean pacIsRight = false;

    float aX = game.pacX();
    float aY = game.pacY();

    float bX = game.redX();
    float bY = game.redY();

    if (aX > bX || (aX == bX && bY > aY)) {
      float tX, tY;

      tX = bX;
      tY = bY;

      bX = aX;
      bY = aY;

      aX = tX;
      aY = tY;

      pacIsRight = true;
    }

    float dX = aX - bX;
    float dY = aY - bY;

    float dAB = (float) Math.sqrt(dX * dX + dY * dY);
    float dRatio = rainbowLROffset / dAB;

    setControlPoints(aX, aY, bX, bY, dAB);

    area.translate(rainbowLX, rainbowLY);

    // Note: this determines whether Pac is on the right or the left side.
    if (!pacIsRight) {
      float xoffset = (rainbowRX - rainbowLX) / 2;
      area.translate(xoffset, 0);
      area.rotate((float) -Math.PI);
      area.translate(-xoffset, 0);
    }

    float rr;
    if (dX == 0) {
      rr = (float) Math.PI * 3 / 2;
    } else {
      rr = (float) Math.atan(dY / dX);
    }

    area.rotate((float) Math.PI * 2f - rr);

    if (focus) {
      float extraScale = 2;
      float scaleBy = extraScale * dFraction / MAX_D_FRACTION;
      float xoffset = (rainbowRX - rainbowLX) / 2;
      float tratio = (dFraction - MAX_D_FRACTION) / (1 - MAX_D_FRACTION);

      tratio = Math.min(tratio * 5, 1);

      area.translate(xoffset * tratio, 0);

      area.scale(scaleBy, scaleBy);
    }

    area.scale(dRatio, dRatio);
    area.translate(-aX, -aY);

    if (game.collision()) {
      area.scale(
          ((float) gboard.width / (float) ctexture.width),
          ((float) gboard.height / (float) ctexture.height));
      area.image(ctexture, 0, 0);
    } else {
      area.copy(
          gboard,
          0,
          0,
          PacmanBoard.BOARD_WIDTH,
          PacmanBoard.BOARD_HEIGHT,
          0,
          0,
          PacmanBoard.BOARD_WIDTH,
          PacmanBoard.BOARD_HEIGHT);
    }
  }
}

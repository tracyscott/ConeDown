package art.lookingup.patterns.play.fragments;

import static processing.core.PConstants.CLAMP;

import heronarts.lx.LX;
import art.lookingup.patterns.pacman.PacmanBoard;
import art.lookingup.patterns.pacman.PacmanGame;
import art.lookingup.patterns.pacman.PacmanSprite;
import processing.core.PImage;

import art.lookingup.patterns.play.Pattern;
import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.colors.Gradient;

import heronarts.lx.LX;

import processing.core.PGraphics;

/**
 */
public class Chase extends Fragment {
    public static final int BLOCK_PIXELS = PacmanBoard.BLOCK_PIXELS;
    public static final int BOARD_HEIGHT = PacmanBoard.BOARD_HEIGHT;
    public static final int BOARD_WIDTH = PacmanBoard.BOARD_WIDTH;

    public final static float HALF_WIDTH = BOARD_WIDTH / 2f;
    public final static float HALF_HEIGHT = BOARD_HEIGHT / 2f;

    public final static float FULL_HEIGHT = BOARD_HEIGHT;
    public final static float FULL_WIDTH = BOARD_WIDTH;

    public final static float MAX_GAME_MILLIS = 1000000;

    PacmanBoard board;
    PacmanGame game;
    PacmanSprite pac;
    PImage gboard;
    PImage ctexture;

    float rainbowLX;
    float rainbowLY;
    float rainbowRX;
    float rainbowRY;

    float rainbowYOffset;
    float rainbowLROffset;

    static public class Factory implements FragmentFactory {
	public Factory() { }

	public Fragment create(LX lx, int width, int height) {
	    return new Chase(width, height);
	}
    };

    public Chase(int width, int height) {
        super(width, height);
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
	float dFraction = D / PacmanBoard.MAX_DISTANCE;

	float minSpread = width / 5f;
	float maxSpread = width / 2f;

	float spread = minSpread + (maxSpread - minSpread) * dFraction;
	float halfw = width / 2f;
	float halfs = spread / 2f;

	rainbowLX = halfw - halfs;
	rainbowRX = halfw + halfs;
	rainbowLY = height / 1.5f;
	rainbowRY = height / 1.5f;
	rainbowLROffset = (float) rainbowRX - rainbowLX;
    }

    @Override
    public void preDrawFragment(float deltaMs) {
	super.preDrawFragment(deltaMs);

	float e = elapsed() * 1000;
	
        if (game.finished() || e > MAX_GAME_MILLIS) {
            this.board.reset();
            game = new PacmanGame(pattern.app, this.board, this.pac);
        }
        game.render(e, null);
        gboard = game.get();
    }

    static int counter;

    public void drawFragment() {
	if (counter++ % 100 == 0) {
	    gboard.save(String.format("/Users/jmacd/Desktop/dump/board-%s.png", counter++));
	}

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

        float dAB = (float)Math.sqrt(dX * dX + dY * dY);
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

        area.rotate((float)Math.PI * 2f - rr);

        area.scale(dRatio, dRatio);

        area.translate(-aX, -aY);

        if (game.collision()) {
            area.scale(((float)gboard.width / (float)ctexture.width),
                     ((float)gboard.height / (float)ctexture.height));
            area.image(ctexture, 0, 0);
        } else {
	    area.copy(gboard, 0, 0, PacmanBoard.BOARD_WIDTH, PacmanBoard.BOARD_HEIGHT, 0, 0, PacmanBoard.BOARD_WIDTH, PacmanBoard.BOARD_HEIGHT);
        }
    }
}

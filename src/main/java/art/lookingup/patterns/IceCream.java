package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.*;

@LXCategory(LXCategory.FORM)
public class IceCream extends PGPixelPerfect {
  public enum Flavor {
    VANILLA,
    CHOCOLATE,
    STRAWBERRY,
    CHOCO_CHIP,
    MINT_CHIP,
    ROCKY_ROAD,
  }

  public static String[] flavors = {
      "Vanilla",
      "Chocolate",
      "Strawberry",
      "Choco Chip",
      "Mint Chip",
      "Rocky Road",
  };

  protected int scoopDancePointsHigh;
  protected int scoopDancePointsWide;
  protected int conePointsHigh;

  static public class Point {
    public Point(int x, int y) { this.x = x; this.y = y; }
    int x;
    int y;
    int color;
    boolean vertical;
  }

  public Point[] chipPoints;
  public Point[] sprinklePoints;

  DiscreteParameter flavor = new DiscreteParameter("Flavor", 0, 0, flavors.length)
      .setDescription("Flavor selector");
  BooleanParameter randomSprinkles = new BooleanParameter("SRand", true)
      .setDescription("Randomly enable sprinkles");
  BooleanParameter sprinkles = new BooleanParameter("Sprinkles", false);
  CompoundParameter numSprinkles = new CompoundParameter("NumSprkls", 10f, 0f, 400f);

  DiscreteParameter chips = new DiscreteParameter("NumChips", 10, 0, 50);
  DiscreteParameter chipSize = new DiscreteParameter("ChipSz", 2, 1, 10);

  DiscreteParameter sprinkleSize = new DiscreteParameter("SpkleSz", 2, 1, 10);
  BooleanParameter sparkle = new BooleanParameter("sparkle", false);


  public IceCream(LX lx) {
    super(lx, "");
    addParameter(flavor);
    addParameter(chips);
    addParameter(chipSize);
    addParameter(randomSprinkles);
    addParameter(sprinkleSize);
    addParameter(sprinkles);
    addParameter(numSprinkles);
    addParameter(hue);
    addParameter(saturation);
    addParameter(bright);
    addParameter(randomPaletteKnob);
    addParameter(paletteKnob);
    addParameter(sparkle);
    //addParameter(hue);
    //addParameter(saturation);
    //addParameter(bright);

    chips.addListener((LXParameter parameter)-> {
      updateParams();
    });

    numSprinkles.addListener((LXParameter parameter) -> {
      updateParams();
    });

    renderTarget.setValue(0);  // Target scoop + dance floor
    renderFullSize = true;
    updateParams();
  }

  protected void updateParams() {
    super.updateParams();
    resetChipPoints();
    resetSprinkles();
    scoopDancePointsWide = ConeDownModel.scoopPointsWide * getSuperSampling();
    scoopDancePointsHigh = (ConeDownModel.scoopPointsHigh + ConeDownModel.dancePointsHigh) * getSuperSampling();
    conePointsHigh = ConeDownModel.conePointsHigh * getSuperSampling();
  }

  @Override
  public void onActive() {
    resetChipPoints();
  }

  public void resetSprinkles() {
    sprinklePoints = new Point[(int)numSprinkles.getValue()];
    for (int i = 0; i < sprinklePoints.length; i++) {
      sprinklePoints[i] = new Point((int)(Math.random() * renderWidth),
          conePointsHigh + (int)(Math.random() * scoopDancePointsHigh));
      sprinklePoints[i].color = getNewRGB();
      sprinklePoints[i].vertical = Math.random() > 0.5f ? true : false;
    }
  }

  public void resetChipPoints() {
    chipPoints = new Point[chips.getValuei()];
    for (int i = 0; i < chipPoints.length; i++) {
      chipPoints[i] = new Point((int)(Math.random() * renderWidth),
          conePointsHigh + (int)(Math.random() * scoopDancePointsHigh));
    }
  }

  public void renderFlavor(Flavor whichFlavor) {
    switch (whichFlavor) {
      case VANILLA:
        renderVanilla();
        break;
      case CHOCOLATE:
        renderChocolate();
        break;
      case STRAWBERRY:
        renderStrawberry();
        break;
      case CHOCO_CHIP:
        renderChocoChip();
        break;
      case MINT_CHIP:
        renderMintChip();
        break;
      case ROCKY_ROAD:
        renderRockyRoad();
        break;
    }
  }

  public void renderVanilla() {
    pg.fill(243, 229, 171);
    pg.noStroke();
    pg.rect(0, conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
  }

  public void renderChocolate() {
    pg.fill(60, 30, 0);
    pg.noStroke();
    pg.rect(0, conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
  }

  public void renderStrawberry() {
    pg.fill(198, 78, 89);
    pg.noStroke();
    pg.rect(0, conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
  }

  public void renderChocoChip() {
    pg.fill(243, 229, 171);
    pg.noStroke();
    pg.rect(0, conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
    chips(true);
  }

  public void renderMintChip() {
    pg.fill(30, 200, 30);
    pg.noStroke();
    pg.rect(0, conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
    chips(true);
  }

  public void renderRockyRoad() {
    pg.fill(60, 30, 0);
    pg.noStroke();
    pg.rect(0, conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
    chips(false);
  }

  public void renderSprinkles() {
    if (sprinklePoints == null) return;
    for (int i = 0; i < sprinklePoints.length; i++) {
      int color = sprinklePoints[i].color;
      if (sparkle.getValueb())
        color = getNewRGB();
      pg.fill(color);
      float dimensions = sprinkleSize.getValuei() + getSuperSampling();
      pg.rect(sprinklePoints[i].x,
          sprinklePoints[i].y,
          sprinklePoints[i].vertical?dimensions/2:dimensions,
          sprinklePoints[i].vertical?dimensions:dimensions/2);
    }
  }

  public void renderCone() {
    //pg.fill(68, 61, 49);
    //pg.fill(68, 58, 29);
    float hsb[] = {hue.getValuef()*360f, saturation.getValuef()*100f, bright.getValuef()*100f};
    int color = Colors.HSBtoRGB(hsb);
    //pg.fill(LXColor.red(color), LXColor.green(color), LXColor.blue(color));
    pg.fill(68, 58, 15);
    pg.stroke(68,58, 15);
    pg.noStroke();
    pg.rect(0, 0, scoopDancePointsWide+2, conePointsHigh+2);
  }

  public void chips(boolean black) {
    if (chipPoints == null) return;
    for (int i = 0; i < chipPoints.length; i++) {
      if (black) pg.fill(0, 0,0);
      else pg.fill(222, 222, 222);
      pg.rect(chipPoints[i].x,
	      chipPoints[i].y,
	      chipSize.getValuei() * getSuperSampling(),
	      chipSize.getValuei() * getSuperSampling());
    }
  }


  public void draw(double deltaMs) {
    renderFlavor(Flavor.values()[flavor.getValuei()]);
    if (sprinkles.getValueb())
      renderSprinkles();
    renderCone();
  }
}

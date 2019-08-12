package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;

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

  static public class Point {
    public Point(int x, int y) { this.x = x; this.y = y; }
    int x;
    int y;
  }

  public Point[] chipPoints;

  DiscreteParameter flavor = new DiscreteParameter("Flavor", 0, 0, flavors.length)
      .setDescription("Flavor selector");

  DiscreteParameter chips = new DiscreteParameter("NumChips", 10, 0, 50);
  DiscreteParameter chipSize = new DiscreteParameter("ChipSz", 2, 1, 10);

  public IceCream(LX lx) {
    super(lx, "");
    addParameter(flavor);
    addParameter(chips);
    addParameter(chipSize);
    resetChipPoints();
    chips.addListener(new LXParameterListener() {
      @Override
      public void onParameterChanged(LXParameter parameter) {
        resetChipPoints();
      }
    });
    renderTarget.setValue(0);  // Target scoop + dance floor
    scoopDancePointsWide = ConeDownModel.scoopPointsWide;
    scoopDancePointsHigh = ConeDownModel.scoopPointsHigh + ConeDownModel.dancePointsHigh;
  }

  @Override
  public void onActive() {
    resetChipPoints();
  }

  public void resetChipPoints() {
    chipPoints = new Point[chips.getValuei()];
    for (int i = 0; i < chipPoints.length; i++) {
      chipPoints[i] = new Point((int)(Math.random() * renderWidth),
          ConeDownModel.conePointsHigh + (int)(Math.random() * scoopDancePointsHigh));
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
    pg.rect(0, ConeDownModel.conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
  }

  public void renderChocolate() {
    pg.fill(60, 30, 0);
    pg.noStroke();
    pg.rect(0, ConeDownModel.conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
  }

  public void renderStrawberry() {
    pg.fill(198, 78, 89);
    pg.noStroke();
    pg.rect(0, ConeDownModel.conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
  }

  public void renderChocoChip() {
    pg.fill(243, 229, 171);
    pg.noStroke();
    pg.rect(0, ConeDownModel.conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
    chips(true);
  }

  public void renderMintChip() {
    pg.fill(60, 100, 60);
    pg.noStroke();
    pg.rect(0, ConeDownModel.conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
    chips(true);
  }

  public void renderRockyRoad() {
    pg.fill(60, 30, 0);
    pg.noStroke();
    pg.rect(0, ConeDownModel.conePointsHigh, scoopDancePointsWide, scoopDancePointsHigh);
    chips(false);
  }

  public void renderCone() {
    //pg.fill(68, 61, 49);
    //pg.fill(68, 58, 29);
    pg.fill(80, 68, 34);
    pg.noStroke();
    pg.rect(0, 0, scoopDancePointsWide, ConeDownModel.conePointsHigh);
  }

  public void chips(boolean black) {
    if (chipPoints == null) return;
    for (int i = 0; i < chipPoints.length; i++) {
      if (black) pg.fill(0, 0,0);
      else pg.fill(222, 222, 222);
      pg.rect(chipPoints[i].x, chipPoints[i].y, chipSize.getValuei(), chipSize.getValuei());
    }
  }


  public void draw(double deltaMs) {
    renderFlavor(Flavor.values()[flavor.getValuei()]);
    renderCone();
  }
}

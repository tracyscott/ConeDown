package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;

@LXCategory(LXCategory.TEXTURE)
public class MaskEffect extends LXEffect {

  public final BooleanParameter cone =
      new BooleanParameter("cone", true)
          .setDescription("Render to cone");
  public final BooleanParameter scoop =
      new BooleanParameter("scoop", true)
          .setDescription("Render to scoop");
  public final BooleanParameter dance =
      new BooleanParameter("dance", true)
          .setDescription("Render to dancefloor");
  public final BooleanParameter floods =
      new BooleanParameter("floods", true)
          .setDescription("Render to floods");


  public MaskEffect(LX lx) {
    super(lx);
    addParameter(cone);
    addParameter(scoop);
    addParameter(dance);
    addParameter(floods);
  }

  @Override
  protected void onEnable() {
  }

  @Override
  public void run(double deltaMs, double amount) {
    if (!cone.isOn()) {
      for (LXPoint point : ConeDownModel.conePoints) {
        this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }
    if (!scoop.isOn()) {
      for (LXPoint point : ConeDownModel.scoopPoints) {
        this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }
    if (!dance.isOn()) {
      for (LXPoint point: ConeDownModel.dancePoints) {
        this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }
    if (!floods.isOn()) {
      for (LXPoint point: ConeDownModel.allConeFloods) {
        this.colors[point.index] = LXColor.rgba(0, 0, 0, 0);
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
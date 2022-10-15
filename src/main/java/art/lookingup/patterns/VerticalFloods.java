package art.lookingup.patterns;

import art.lookingup.AnimUtils;
import art.lookingup.ConeDownModel;
import art.lookingup.EaseUtil;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

// TODO(tracy): Implement speed for not requiring an LFO?
public class VerticalFloods extends LXPattern {

  public String waves[] = { "tri", "step", "stepr", "square"};

  CompoundParameter pos = new CompoundParameter("pos", 0.5f, -2f, 2f).setDescription("Position of center");
  DiscreteParameter wave = new DiscreteParameter("wave", waves);
  CompoundParameter slope = new CompoundParameter("slope", 1f, 0f, 30f).setDescription("Slope if applicable");
  CompoundParameter width = new CompoundParameter("width", 0.1f, 0, 4f).setDescription("Width of square wave");
  CompoundParameter speed = new CompoundParameter("speed", 1f, 0f, 30f).setDescription("Sweep speed");
  CompoundParameter bgintensity = new CompoundParameter("bgi", 0, 0, 1 ).setDescription("Background Intensity");
  ColorParameter color = new ColorParameter("color");
  CompoundParameter maxIntensity = new CompoundParameter("maxi", 1f, 0f, 1f).setDescription("Max intensity");

  BooleanParameter usePal = new BooleanParameter("usePal", false);
  CompoundParameter palStrt = new CompoundParameter("palStrt", 0f, 0f, 1f).setDescription("Palette start");
  DiscreteParameter easeParam = new DiscreteParameter("ease", 0, 0, EaseUtil.MAX_EASE + 1);
  DiscreteParameter swatch = new DiscreteParameter("swatch", 0, 0, 20);
  CompoundParameter sinFreq = new CompoundParameter("sinFreq", 1f, 0f, 10f).setDescription("Freq for sine easing");
  CompoundParameter perlinFreq = new CompoundParameter("perlFreq", 1f, 0f, 20f);

  public EaseUtil ease = new EaseUtil(0);

  public VerticalFloods(LX lx) {
    super(lx);
    //addParameter("fps", fpsKnob);
    addParameter("pos", pos);
    addParameter("wave", wave);
    addParameter("slope", slope);
    addParameter("width", width);

    addParameter("speed", speed);
    addParameter("bgi", bgintensity);
    addParameter("maxi", maxIntensity);
    addParameter("color", color);
    addParameter("usePal", usePal);
    addParameter("palStrt", palStrt);
    addParameter("ease", easeParam);
    addParameter("swatch", swatch);
    addParameter("sinFreq", sinFreq);
    addParameter("perlFreq", perlinFreq);
    color.brightness.setValue(100f);
  }

  public int getColor(float t) {
    int clr = color.getColor();
    float easedT = ease.ease(t);

    if (usePal.getValueb()) {
      if (t < palStrt.getValuef())
        t = palStrt.getValuef();
      clr = LXColor.WHITE; //Colors.getParameterizedPaletteColor(lx, swatch.getValuei(), t, ease);
    }
    if (easedT < bgintensity.getValuef())
      easedT = bgintensity.getValuef();
    clr = Colors.getWeightedColor(clr, easedT);
    return clr;
  }

  public void run(double deltaMs) {
    ease.easeNum = easeParam.getValuei();
    if (easeParam.getValuei() == 8) {
      ease.perlinFreq = perlinFreq.getValuef();
    } else if (easeParam.getValuei() == 8) {
      ease.freq = sinFreq.getValuef();
    }
    for (LXPoint p : ConeDownModel.allConeFloods) {
      float t = (p.y - lx.getModel().yMin) / (lx.getModel().yMax - lx.getModel().yMin);
      float val = 0f;
      if (wave.getValuei() == 0) {
        val = AnimUtils.triangleWave(pos.getValuef(), slope.getValuef(), t);
      } else if (wave.getValuei() == 1) {
        val = AnimUtils.stepDecayWave(pos.getValuef(), width.getValuef(), slope.getValuef(), t, true);
      } else if (wave.getValuei() == 2) {
        val = AnimUtils.stepDecayWave(pos.getValuef(), width.getValuef(), slope.getValuef(), t, false);
      } else if (wave.getValuei() == 3) {
        val = AnimUtils.squareWave(pos.getValuef(), width.getValuef(), t);
      }

      int clr = getColor(val);

      // Now apply the value as an intensity modifier.
      clr = Colors.getWeightedColor(clr, maxIntensity.getValuef());
      colors[p.index] = clr;
    }
  }
}

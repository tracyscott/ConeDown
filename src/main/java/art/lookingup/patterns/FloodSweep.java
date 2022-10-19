package art.lookingup.patterns;

import art.lookingup.ConeDownModel;
import art.lookingup.EaseUtil;
import art.lookingup.colors.Colors;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

@LXCategory(LXCategory.FORM)
public class FloodSweep extends RPattern {
  CompoundParameter speed = new CompoundParameter("speed", 1f, 0f, 30f).setDescription("Sweep speed");
  CompoundParameter angleWidth = new CompoundParameter("angleW", 45, 0, 360).setDescription("Angle width");
  CompoundParameter bgintensity = new CompoundParameter("bgi", 0, 0, 1 ).setDescription("Background Intensity");
  ColorParameter color = new ColorParameter("color");
  CompoundParameter maxIntensity = new CompoundParameter("maxi", 1f, 0f, 1f).setDescription("Max intensity");

  CompoundParameter sparkle = new CompoundParameter("sparkle", 0,0,1).setDescription("Sparkle Effect");
  BooleanParameter usePal = new BooleanParameter("usePal", false);
  DiscreteParameter easeParam = new DiscreteParameter("ease", 0, 0, EaseUtil.MAX_EASE + 1).setDescription("6 sine, 7 mostly 0 and 1, 8 perlin");
  DiscreteParameter swatch = new DiscreteParameter("swatch", 0, 0, 20);
  CompoundParameter perlinFreq = new CompoundParameter("perlFreq", 1f, 0f, 20f);
  CompoundParameter sinFreq = new CompoundParameter("sinFreq", 1f, 0f, 10f).setDescription("Freq for sine easing");
  CompoundParameter palStrt = new CompoundParameter("palStrt", 0f, 0f, 1f).setDescription("Palette start point");

  EaseUtil ease = new EaseUtil(0);
  float currentAngle = 0f;

  public FloodSweep(LX lx) {
    super(lx);
    addParameter("speed", speed);
    addParameter("angleW", angleWidth);
    addParameter("bgi", bgintensity);
    addParameter("maxi", maxIntensity);
    addParameter("sparkle",sparkle);
    addParameter("color", color);
    addParameter("usePal", usePal);
    addParameter("palStrt", palStrt);
    addParameter("ease", easeParam);
    addParameter("swatch", swatch);
    addParameter("perlFreq", perlinFreq);
    addParameter("sinFreq", sinFreq);

    color.brightness.setValue(100.0);
  }

  public void onActive() {
    super.onActive();
    currentAngle = 0f;
  }

  public float angle(LXPoint p) {
    return 360f * (float)(p.azimuth/(Math.PI * 2f));
  }

  /**
   * Return a color based on t value. This function will apply easing the value of t.
   * If usePal is on, it will lookup the color based on eased T, otherwise it uses the
   * configured color.  Brightness reduction is also applied based on eased T.
   * @param t
   * @return
   */
  public int getColor(float t) {
    int clr = color.getColor();
    float easedT = ease.ease(t);
    if (usePal.getValueb()) {
      if (t < palStrt.getValuef())
        t = palStrt.getValuef();
      clr = Colors.getParameterizedPaletteColor(lx, swatch.getValuei(), t, ease);
    }
    if (easedT < bgintensity.getValuef())
      easedT = bgintensity.getValuef();
    clr = Colors.getWeightedColor(clr, easedT);

    return clr;
  }

  public boolean isinRange(float pointAngle) {
    if ( pointAngle > currentAngle - angleWidth.getValuef() /2f && pointAngle < currentAngle + angleWidth.getValue()/ 2f)
      return true;
    float overlap = currentAngle + angleWidth.getValuef() / 2f- 360f;
    if ( overlap > 0f)
      if (pointAngle < overlap)
        return true;
    float underlap = currentAngle - angleWidth.getValuef()/ 2f;
    if (underlap < 0 )
      if (pointAngle > 360f + underlap)
        return true;
    return false;
  }
  public float computeTValue(float pointangle) {
    float distancefromhead = distancefromhead(pointangle);
    if (distancefromhead >= 1f) {
      if (!usePal.isOn()) {
        return bgintensity.getValuef();
      } else {
        return palStrt.getValuef(); // If we are using the palette we will use this to grab lerp'd swatch color.
      }
    }
    if (!usePal.isOn()) {
      return bgintensity.getValuef() + (1 - distancefromhead) * (1 - bgintensity.getValuef());
    } else {
      return palStrt.getValuef() + (1f - distancefromhead) * (1 - palStrt.getValuef());
    }
  }

  /**
   * @param pointAngle
   * @return 1 to 0 based on distance from the head where 1 is equivalent to angleWidth.
   */
  public float distancefromhead(float pointAngle){
    float headPos = currentAngle;
    float distance = Math.abs(headPos - pointAngle);

    float wrappedDistance = Math.abs(headPos + 360f - pointAngle);
    float wrappedDistance2 = Math.abs(pointAngle + 360f - headPos);

    distance = Math.min(distance, wrappedDistance);
    distance = Math.min(distance, wrappedDistance2);

    return distance / angleWidth.getValuef();
  }

  public void render(double deltaMs) {
    ease.easeNum = easeParam.getValuei();
    if (ease.easeNum == 8) {
      ease.perlinFreq = perlinFreq.getValuef();
    } else if (ease.easeNum == 6) {
      ease.freq = sinFreq.getValuef();
    }
    for (LXPoint p : ConeDownModel.allConeFloods) {
      colors[p.index] = LXColor.BLACK;
    }
    for (LXPoint p : ConeDownModel.allConeFloods) {
      float angleDegrees = angle(p);
      float tValue = computeTValue(angleDegrees);
      int clr = getColor(tValue);
      // Apply sparkle/random amount and maximum value.
      float intensityMod = (((1f - sparkle.getValuef() ) + sparkle.getValuef() * (float) Math.random())*1f);
      intensityMod = intensityMod * maxIntensity.getValuef();
      clr = Colors.getWeightedColor(clr, intensityMod);
      colors[p.index] = clr;
    }
    currentAngle += speed.getValuef();
    if (currentAngle > 360f)
      currentAngle  -= 360f;
  }
}

package art.lookingup;

/**
 * Easing functions.
 */

public class EaseUtil {

  static public final int MAX_EASE = 8;

  public int easeNum;

  public boolean perlin2D = false;
  public float perlinFreq = 1f;
  public float t2 = 0f;
  public float freq = 1f;

  public EaseUtil(int easeNum) {
    this.easeNum = easeNum;
  }

  public float ease(float t) {
    if (easeNum == 8 && perlin2D) {
      return ease8(t, t2, perlinFreq);
    } else if (easeNum == 8) {
      return ease8(t, perlinFreq);
    } else if (easeNum == 6) {
      return ease6(t, freq);
    }

    return ease(t, easeNum);
  }

  static public float ease(float t, int which) {
    switch (which) {
      case 0:
        return ease0(t);
      case 1:
        return ease1(t);
      case 2:
        return ease2(t);
      case 3:
        return ease3(t);
      case 4:
        return ease4(t);
      case 5:
        return ease5(t);
      case 6:
        return ease6(t);
      case 7:
        return ease7(t);
      case 8:
        return ease8(t);
    }
    return t;
  }

  static public float ease0(float t) {
    return t;
  }

  static public float ease1(float t) {
    return (float)Math.sin(t * Math.PI / 2);
  }

  static public float ease2(float t) {
    return 1.0f - (1.0f - t) * (1.0f - t);
  }

  static public float ease3(float t) {
    return 1.0f - (float)Math.pow(1.0 - t, 3);
  }

  static public float ease4(float t) {
    return 1.0f - (float)Math.pow(1.0 - t, 4);
  }

  static public float ease5(float t) {
    return 1.0f - (float)Math.pow(1.0 - t, 5);
  }

  static public float ease6(float t) { return ease6(t, 1f); }

  static public float ease6(float t, float freq) { return 0.5f + 0.5f * (float)Math.sin(freq * t * Math.PI * 4); }

  // Mostly stays near 0 and 1 https://easings.net/#easeInOutExpo
  static public float ease7(float t) {
    return (t < 0.5f)? (float)Math.pow(2, 20f * t - 10f)/2f :
        (2f - (float)Math.pow(2, -20f * t + 10))/2f;
  }

  static public float ease8(float t) { return ease8(t, 1f); }
  static public float ease8(float t, float freq) { return ConeDown.pApplet.noise(t * freq); }
  static public float ease8(float t1, float t2, float freq) {
    return ConeDown.pApplet.noise(freq * t1, freq * t2);
  }
}

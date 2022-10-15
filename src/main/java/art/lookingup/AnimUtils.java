package art.lookingup;


public class AnimUtils {
  /**
   * Evaluate a triangle wave.  Linear oscillation between 0 and 1.
   */
  static public float triWave(float t, float p)
  {
    return 2.0f * (float)Math.abs(t / p - Math.floor(t / p + 0.5f));
  }

  /**
   * Step wave with attack slope.
   * Returns value from 0.0f to 1.0f
   */
  static public float stepWave(float stepPos, float slope, float x, boolean forward)
  {
    float value;
    if (forward) {
      if (x < stepPos)
        value = 1.0f;
      else {
        value = -slope * (x - stepPos) + 1.0f;
        if (value < 0f) value = 0f;
      }
    } else {
      if (x > stepPos)
        value = 1.0f;
      else {
        value = slope * (x - stepPos) + 1.0f;
        if (value < 0f) value = 0f;
      }

    }
    return value;
  }

  /**
   * Step wave with attack slope.
   * Returns value from 0.0f to 1.0f
   */
  static public float stepDecayWave(float stepPos, float width, float slope, float x, boolean forward) {
    float value;
    if ((x > stepPos - width/2.0f) && (x < stepPos + width/2.0f))
      return 1.0f;

    if ((x > stepPos + width/2.0f) && forward)
      return 0f;
    else if ((x < stepPos - width/2.0f && !forward))
      return 0f;

    if (forward) {
      value = 1.0f + slope * (x - (stepPos - width/2.0f));
      if (value < 0f) value = 0f;
    } else {
      value = 1.0f - slope * (x - (stepPos + width/2.0f));
      if (value < 0f) value = 0f;
    }
    return value;
  }


  /**
   * Normalized triangle wave function.  Given position of triangle peak and the
   * slope, return value of function at evalAtX.  If less than 0, clip to zero.
   */
  static public float triangleWave(float peakX, float slope, float evalAtX)
  {
    // If we are to the right of the triangle, the slope is negative
    if (evalAtX > peakX) slope = -slope;
    float y = slope * (evalAtX - peakX) + 1.0f;
    if (y < 0f) y = 0f;
    return y;
  }

  static public float squareWave(float posX, float width, float evalAtX) {
    if (Math.abs(posX - evalAtX) < width/2f)
      return 1f;
    else return 0f;
  }
}


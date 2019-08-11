package art.lookingup;

import processing.core.PImage;

public interface Projection {

    public int computePoint(int idx, PImage img);

    public float xScale(float x, float y);

    public CXPoint lookupPoint(float x, float y);
}

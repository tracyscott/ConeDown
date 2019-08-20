package art.lookingup;

import processing.core.PImage;

public interface Projection {

    public int computePoint(CXPoint p, PImage img, int xoffset, int yoffset);

    public CXPoint lookupPoint(float x, float y);

    public float xScale(float x, float y);

    public int factor();
}

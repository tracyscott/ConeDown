package art.lookingup;

import static art.lookingup.ConeDownModel.POINTS_HIGH;
import static art.lookingup.ConeDownModel.POINTS_WIDE;
import static art.lookingup.colors.Colors.alpha;
import static art.lookingup.colors.Colors.blue;
import static art.lookingup.colors.Colors.green;
import static art.lookingup.colors.Colors.red;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.google.common.base.Stopwatch;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.logging.Logger;
import processing.core.PImage;

public class AntiAliased implements Projection {
    RTree<CXPoint, Point> tree = RTree.create();
    int[] positions;
    int[] subpixels;
    // float[] subweights;
    int ssWide;
    int ssHigh;
    int superSampling;

    private static final Logger logger = Logger.getLogger(AntiAliased.class.getName());

    public AntiAliased(LXModel model, int superSampling) {
    	Stopwatch stopwatch =  Stopwatch.createStarted();
	Pixel[] pixels = new Pixel[model.size];
	float ssOff = superSampling / 2f;

	this.superSampling = superSampling;
	this.ssHigh = POINTS_HIGH * superSampling;
	this.ssWide = POINTS_WIDE * superSampling;

	float minDX = ssWide;
	float maxDX = 0;
	float minDY = ssHigh;
	float maxDY = 0;
	float maxOY = 0;

	this.positions = new int[ssHigh * ssWide + 1];

	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;
	    if (cxp.panel == null) {
		// TODO interior lighting
		continue;
	    }
	    float []coords = ConeDownModel.pointToProjectionCoords(cxp);

	    this.tree = this.tree.add(cxp, Geometries.point(coords[0] * superSampling + ssOff,
							    coords[1] * superSampling + ssOff));

	    pixels[cxp.index] = new Pixel(cxp);

	    // Compute the out-of-bounds regions.
	    if (cxp.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
		minDX = Math.min(minDX, (0 + coords[0]) * superSampling);
		maxDX = Math.max(maxDX, (1 + coords[0]) * superSampling);
		minDY = Math.min(minDY, (0 + coords[1]) * superSampling);
		maxDY = Math.max(maxDY, (1 + coords[1]) * superSampling);
	    } else {
		maxOY = Math.max(maxOY, (1 + coords[1]) * superSampling);
	    }
	}

	int pCount = 0;

	for (int jInv = 0; jInv < ssHigh; jInv++) {
	    int j = ssHigh - 1 - jInv;
	    for (int i = 0; i < ssWide; i++) {
		int idx = (j * ssWide) + i;

		if (j >= minDY && (i < minDX || i >= maxDX)) {
		    continue;
		}

		for (Entry<CXPoint, Point> point :
			 this.tree.nearest(Geometries.point(i, j), Double.POSITIVE_INFINITY, 1).
			 toBlocking().toIterable()) {
		    CXPoint cxp = point.value();
		    Geometry geo = point.geometry();

		    if (cxp.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
		    	if (j < minDY) {
			    // Dance floors should not see pixels above the line.
			    // This happens because of the missing pixels between scoop and
			    // dancefloor.
			    continue;
		    	}
		    }
		    double xd = (j - geo.mbr().y1());
		    double yd = (i - geo.mbr().x1());
		    double dist = Math.sqrt(xd * xd + yd * yd);

		    // Somewhat arbitrary limit, this is here because
		    // we do not have a proper perimeter in the area
		    // between the scoop and the dance floor.
		    if (dist > superSampling * 1.5) {
			continue;
		    }

		    pixels[cxp.index].subs.add(idx);
		    pCount++;
		    break;
		}
	    }
	}

	int position = 0;

	this.subpixels = new int[pCount];
	// this.subweights = new float[pCount];

	LXPoint[] points = new LXPoint[model.size];

	// Note: The `subpixels` logic relies on points being sorted.
	// It appears that LX has un-sorted the points, and we'll
	// re-sort:
	for (LXPoint lxp : model.points) {
	    points[lxp.index] = lxp;
	}

	for (LXPoint lxp : points) {
	    positions[lxp.index] = position;

	    if (pixels[lxp.index] == null) {
		continue;
	    }

	    for (int sub : pixels[lxp.index].subs) {
		subpixels[position] = sub;
		position++;
	    }
	}
	positions[model.size] = position;

	// for (LXPoint lxp : model.points) {
	//     buildWeights(lxp);
	// }
	System.out.format("*** Initialized AntiAliased(%s) in %s\n", superSampling, stopwatch);
    }

    public CXPoint lookupPoint(float x, float y) {
        for (Entry<CXPoint, Point> point :
            tree.nearest(Geometries.point(x, y), Double.POSITIVE_INFINITY, 1).toBlocking().toIterable()) {
	    return point.value();
	}
	return null;
    }

    public float xScale(float x, float y) {
	return ConeDownModel.xScale(lookupPoint(x, y));
    }

  static class Pixel {
    ArrayList<Integer> subs = new ArrayList<Integer>();

    Pixel(CXPoint lxp) {}
  }

  public int computePoint(CXPoint cxp, PImage img, int xoffset, int yoffset) {
    float r = 0, g = 0, b = 0, a = 0;
    int end = positions[cxp.index + 1];
    float w = 1f / (end - positions[cxp.index]);

    for (int off = positions[cxp.index]; off < end; off++) {
      int subpos = subpixels[off];

      int subx = subpos % ssWide;
      int suby = subpos / ssWide;

      int s;
      if (xoffset == 0 && yoffset == 0) {
	  s = img.pixels[suby * img.width + subx];
      } else {
	  subx += xoffset;
	  suby += yoffset;
	  
	  s = img.get(subx, suby);
      }

      // float w = subweights[off];
      r += w * (float) red(s);
      g += w * (float) green(s);
      b += w * (float) blue(s);
      a += w * (float) alpha(s);
    }
    return LXColor.rgba((int) r, (int) g, (int) b, (int) a);
  }

    public int factor() {
	return this.superSampling;
    }
}

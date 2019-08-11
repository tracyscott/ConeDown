package art.lookingup;

import art.lookingup.Projection;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Visualizer;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;

import static art.lookingup.colors.Colors.blue;
import static art.lookingup.colors.Colors.green;
import static art.lookingup.colors.Colors.red;
import static art.lookingup.colors.Colors.rgb;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import processing.core.PImage;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class AntiAliased implements Projection {
    RTree<CXPoint, Point> tree = RTree.create();
    int[] positions;
    int[] subpixels;
    float[] subweights;

    public AntiAliased(LXModel model, int superSample) {
	Pixel[] pixels = new Pixel[model.size];
	float ssOff = (superSample - 1f) / 2f;
	int ssHigh = ConeDownModel.POINTS_HIGH * superSample;
	int ssWide = ConeDownModel.POINTS_WIDE * superSample;

	float minDX = ssWide;
	float maxDX = 0;
	float minDY = ssHigh;
	float maxDY = 0;

	this.positions = new int[ssHigh * ssWide + 1];
	
	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;
	    float []coords = ConeDownModel.pointToProjectionCoords(cxp);

	    this.tree = this.tree.add(cxp, Geometries.point(coords[0] * superSample + ssOff,
							    coords[1] * superSample + ssOff));
	    pixels[cxp.index] = new Pixel(cxp);

	    // Compute the out-of-bounds regions.
	    if (cxp.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
		minDX = Math.min(minDX, (0 + coords[0]) * superSample);
		maxDX = Math.max(maxDX, (1 + coords[0]) * superSample);
		minDY = Math.min(minDY, (0 + coords[1]) * superSample);
		maxDY = Math.max(maxDY, (1 + coords[1]) * superSample);
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
			 this.tree.nearest(Geometries.point(i, j), 100, 1).toBlocking().toIterable()) {
		    CXPoint cxp = point.value();
		    Point geo = point.geometry();
		    pixels[cxp.index].subs.add(idx);
		    pCount++;
		    break;
		}
	    }
	}

	int sumSubs = 0;
	
	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;
	    sumSubs += pixels[cxp.index].subs.size();
	}

	int position = 0;

	this.subpixels = new int[pCount];
	this.subweights = new float[pCount];

	LXPoint[] points = new LXPoint[model.size];

	// Note: The `subpixels` logic relies on points being sorted.
	// It appears that LX has un-sorted the points, and we'll
	// re-sort:
	for (LXPoint lxp : model.points) {
	    points[lxp.index] = lxp;
	}

	for (LXPoint lxp : points) {
	    positions[lxp.index] = position;

	    for (int sub : pixels[lxp.index].subs) {
		subpixels[position] = sub;
		position++;
	    }
	}
	positions[model.size] = position;

	// for (LXPoint lxp : model.points) {
	//     buildWeights(lxp);
	// }
    }

    public CXPoint lookupPoint(float x, float y) {
        for (Entry<CXPoint, Point> point :
            tree.nearest(Geometries.point(x, y), Double.POSITIVE_INFINITY, 1).toBlocking().toIterable()) {
	    return point.value();
	}
	return null;
    }

    public float xScale(float x, float y) {
	return ConeDownModel.xScale(lookupPoint(x, y), ConeDownModel.POINTS_WIDE);
    }

  static class Pixel {
    ArrayList<Integer> subs = new ArrayList<Integer>();

    Pixel(CXPoint lxp) {}
  }

  public int computePoint(int idx, PImage img) {
    float r = 0, g = 0, b = 0;
    int end = positions[idx + 1];
    float w = 1f / (end - positions[idx]);

    for (int off = positions[idx]; off < end; off++) {
      int s = img.pixels[subpixels[off]];

      // float w = subweights[off];
      r += w * (float) red(s);
      g += w * (float) green(s);
      b += w * (float) blue(s);
    }
    return rgb((int) r, (int) g, (int) b);
  }
}

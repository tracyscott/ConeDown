package art.lookingup;

import art.lookingup.Projection;

import static art.lookingup.ConeDownModel.POINTS_HIGH;
import static art.lookingup.ConeDownModel.POINTS_WIDE;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Visualizer;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;

import static art.lookingup.colors.Colors.blue;
import static art.lookingup.colors.Colors.green;
import static art.lookingup.colors.Colors.red;
import static art.lookingup.colors.Colors.alpha;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import processing.core.PImage;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.logging.Logger;

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

	    System.err.println("Point " + cxp.panel.panelRegion + " x=" + coords[0] + " y=" + coords[1] +
			       " ss " + (coords[0] * superSampling + ssOff) +  " " + (coords[1] * superSampling + ssOff));

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

	System.err.println("AntiAliased superSampling=" + superSampling + " ssWide " + ssWide + " sshigh " + ssHigh + " mindy " + minDY + " maxOY " + maxOY);

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

		    // if (cxp.panel.panelRegion != Panel.PanelRegion.DANCEFLOOR) {
		    // 	if (j >= minDY) {
		    // 	    System.err.println("Pixel crosser: " + i + ":" + j + " w/ " + minDY);
		    // 	}
		    // }		    

		    if (cxp.panel.panelRegion == Panel.PanelRegion.DANCEFLOOR) {
		    	if (j < minDY) {
		    	    System.err.println("Pixel crosser: " + i + ":" + j + " w/ " + minDY + " geo " + geo);
		    	}
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

    //System.err.println("Compute " + cxp.index);

    for (int off = positions[cxp.index]; off < end; off++) {
      int subpos = subpixels[off];
      
      int subx = subpos % ssWide;
      int suby = subpos / ssWide;

      //System.err.println("  @ " + subx + ":" + suby + " sswide " + ssWide + " subpos " + subpos);

      subx += xoffset;
      suby += yoffset;

      int s = img.get(subx, suby);

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

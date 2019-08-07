package art.lookingup;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Visualizer;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// Blah blah equirectangular coordinates

public class Projection {
    RTree<CXPoint, Point> tree = RTree.create();
    int[] positions;
    int[] subpixels;
    float[] subweights;

    public Projection(LXModel model, int superSample) {
	Pixel[] pixels = new Pixel[model.size];
	float ssOff = (superSample - 1f) / 2f;
	int ssHigh = ConeDownModel.POINTS_HIGH * superSample;
	int ssWide = ConeDownModel.POINTS_WIDE * superSample;

	int minDX = ConeDownModel.POINTS_WIDE;
	int maxDX = 0;
	int minDY = ConeDownModel.POINTS_HIGH;
	int maxDY = 0;

	this.positions = new int[ssHigh * ssWide + 1];
	
	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;
	    float []coords = ConeDownModel.pointToProjectionCoords(cxp);

	    this.tree = this.tree.add(cxp, Geometries.point(coords[0] * superSample + ssOff,
							    coords[1] * superSample + ssOff));
	    pixels[cxp.index] = new Pixel(cxp);

	    // Compute the out-of-bounds regions.  TODO this depends a lot on...
	    if (cxp.panel.panelRegion == Panel.DANCEFLOOR) {
		// @@@ NOTE: this should compare vs. the 
		//   xCoord = (float)(p.panel.panelNum * p.panel.pointsWide + p.xCoord) * xScale(p, POINTS_WIDE);
		// value.
		//
		//   minDY = Math.min(minDY, cxp....
	    }
	}

	for (int j = 0; j < ssHigh; j++) {
	    for (int i = 0; i < ssWide; i++) {
		int idx = (j * ssHigh) + i;

		for (Entry<CXPoint, Point> point :
			 this.tree.nearest(Geometries.point(i, j), 100, 1).toBlocking().toIterable()) {
		    CXPoint cxp = point.value();
		    // @@@ Exclude non-nearby
		    pixels[cxp.index].subs.add(idx);
		    break;
		}
	    }
	}

	int sumSubs = 0;
	
	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;
	    sumSubs += pixels[cxp.index].subs.size();
	}

	System.err.println("SumSubs " + sumSubs);

	int position = 0;

	this.subpixels = new int[ssWide * ssHigh];
	this.subweights = new float[ssWide * ssHigh];

	for (LXPoint lxp : model.points) {
	    positions[lxp.index] = position;

	    // System.err.println("NOTE " + lxp.index + " pos " + position + " " + pixels[lxp.index].subs + " msize " + model.size);

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

    public float xScale(float x, float y) {
        for (Entry<CXPoint, Point> point :
            tree.nearest(Geometries.point(x, y), 100, 1).toBlocking().toIterable()) {
	    return ConeDownModel.xScale(point.value(), ConeDownModel.POINTS_WIDE);
        }
	return 1;
    }

  static class Pixel {
    // float x, y;
    ArrayList<Integer> subs = new ArrayList<Integer>();

    Pixel(CXPoint lxp) {
      // x = lxp.x;
      // y = lxp.y;
    }
  }

}

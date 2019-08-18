package art.lookingup;

import static art.lookingup.ConeDownModel.POINTS_HIGH;
import static art.lookingup.ConeDownModel.POINTS_WIDE;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.google.common.base.Stopwatch;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import processing.core.PImage;

public class TrueProjection implements Projection {
    int []mapping;
    CXPoint []lookup;

    public TrueProjection(LXModel model) {
    	Stopwatch stopwatch = Stopwatch.createStarted();
	RTree<CXPoint, Point> tree = RTree.create();

	this.mapping = new int[model.size];
	this.lookup = new CXPoint[POINTS_WIDE * POINTS_HIGH];

	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;

	    if (cxp.panel == null) {
		// TODO interior lights
		continue;
	    }

	    float []coords = ConeDownModel.pointToProjectionCoords(cxp);

	    int x = (int)(coords[0] + 0.5);
	    int y = (int)(coords[1] + 0.5);

	    tree = tree.add(cxp, Geometries.point(x, y));

	    this.mapping[cxp.index] = (y * POINTS_WIDE) + x;
	}

	for (int y = 0; y < POINTS_HIGH; y++) {
	    for (int x = 0; x < POINTS_WIDE; x++) {
		for (Entry<CXPoint, Point> point :
			 tree.nearest(Geometries.point(x, y), Double.POSITIVE_INFINITY, 1).
			 toBlocking().toIterable()) {
		    this.lookup[y * POINTS_WIDE + x] = point.value();
		}
	    }
	}
	System.out.format("*** Initialized TrueProjection in %s\n", stopwatch);
    }

    public CXPoint lookupPoint(float x, float y) {
	int xi = Math.max(0, Math.min((int)(x + 0.5), POINTS_WIDE-1));
	int yi = Math.max(0, Math.min((int)(y + 0.5), POINTS_HIGH-1));
	int idx = yi * POINTS_WIDE + xi;
	if (idx >= lookup.length) {
	    return null;
	}
	return lookup[idx];
    }

    public float xScale(float x, float y) {
	return ConeDownModel.xScale(lookupPoint(x, y));
    }

    public int computePoint(CXPoint p, PImage img, int xoffset, int yoffset) {
	int idx = mapping[p.index];
	int x = idx % POINTS_WIDE;
	int y = idx / POINTS_WIDE;
	if (xoffset == 0 && yoffset == 0) {
	    return img.pixels[y * img.width + x];
	}
	return img.get(x + xoffset, y + yoffset);
    }

    public int factor() {
	return 1;
    }
}

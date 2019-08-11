package art.lookingup;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Visualizer;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;

import processing.core.PImage;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;

public class TrueProjection implements Projection {
    RTree<CXPoint, Point> tree = RTree.create();

    public TrueProjection(LXModel model) {
	for (LXPoint lxp : model.points) {
	    CXPoint cxp = (CXPoint) lxp;
	    float []coords = ConeDownModel.pointToProjectionCoords(cxp);

	    this.tree = this.tree.add(cxp, Geometries.point(coords[0], coords[1]));
	}
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

    public int computePoint(int idx, PImage img) {
	// @@@
	return 0;
    }
}

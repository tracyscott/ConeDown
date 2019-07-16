package art.lookingup;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

// Blah blah equirectangular coordinates

public class Projection {
    RTree<CXPoint, Point> tree = RTree.create();

    public Projection(LXModel model) {
	for (LXPoint lxp : model.points) {
	    int []coords = ConeDownModel.pointToImgCoordsCylinder((CXPoint) lxp);

	    tree = tree.add((CXPoint) lxp, Geometries.point(coords[0], coords[1]));
	}
    }

    public float xScale(float x, float y) {
        for (Entry<CXPoint, Point> point :
            tree.nearest(Geometries.point(x, y), 100, 1).toBlocking().toIterable()) {
	    return ConeDownModel.xScale(point.value());
        }
	return 1;
    }
}

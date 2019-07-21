package art.lookingup.patterns.play.fragments;

import processing.core.PGraphics;
import art.lookingup.patterns.play.Parameter;

public class Spiral extends art.lookingup.patterns.play.Fragment {

    PGraphics graph;
    Parameter triples;
    Parameter pitch;
    Parameter width;

    public Spiral(PGraphics graph) {
	this.graph = graph;
	this.triples = new Parameter("3x_count", 4, 1, 10);
	this.pitch = new Parameter("sqrt_pitch", 8, 3, 400);
	this.width = new Parameter("fill_pct", 100, 0, 100);

    }

//   @Override
//   public void draw(double deltaMs) {
//     int count = (int)(countx3.getValue() * 3);
//     float incr = (float)height.getValue();
//     float spin = relapsed;
//     for (int idx = 0; idx < count; idx++) {
//   	float base = -incr - spin * incr;
//   	float spirals = (float)count;
//   	float y0 = base + ((float)idx / spirals) * incr;
//   	int c = gradients[count][idx];
//   	graph.stroke(c);
//   	for (float y = y0; y < pg.height+incr; y += incr) {
//   	    graph.line(0, y, pg.width, y+incr);
//   	}
//     }
//   }
}

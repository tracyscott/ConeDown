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
	this.triples = new Parameter("triples", 4, 1, 10);
	this.pitch = new Parameter("pitch", 64, 8, 1000);
	this.fill = new Parameter("fill", 1, 0, 1);

    }

  // @@@ redo this pattern as a repeating scan over an image.

  @Override
  public void draw(double deltaMs) {
    int count = (int)(triples.getValue() * 3);
    float incr = (float)pitch.getValue();
    float spin = felapsed;  // @@@ hide this
    for (int idx = 0; idx < count; idx++) {
  	float base = -incr - spin * incr;
  	float spirals = (float)count;
  	float y0 = base + ((float)idx / spirals) * incr;
  	int c = gradients[count][idx];
  	graph.stroke(c);
  	for (float y = y0; y < pg.height+incr; y += incr) {
  	    graph.line(0, y, pg.width, y+incr);
  	}
    }
  }
}

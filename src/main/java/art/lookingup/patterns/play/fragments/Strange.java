package art.lookingup.patterns.play.fragments;

import art.lookingup.patterns.play.Fragment;
import art.lookingup.patterns.play.FragmentFactory;
import art.lookingup.patterns.play.Parameter;
import art.lookingup.patterns.play.Pattern;
import art.lookingup.colors.Gradient;
import art.lookingup.Projection;

import heronarts.lx.LX;

import processing.core.PImage;
import processing.core.PGraphics;

public class Strange extends Fragment {
    final Parameter period;
    
    final PImage textures[];

    static final int patterns[][] = {
    	{0, 1, 2, 3},
    	{4, 5, 6, 7},
    	{8, 1, 9, 6, 8, 6, 9, 1},
    };

    static public class Factory implements FragmentFactory {
	public Factory() { }

	public Fragment create(LX lx, int width, int height) {
	    return new Strange(lx, width, height);
	}
    };
    
    public Strange(LX lx, int width, int height) {
	super(width, height);

	this.period = new Parameter(this, "period", 100, 1, 200);
	this.textures = new PImage[inputs.length];
    }
    
    String inputs[] = {
	"images/blend-red-blue.png",
	"images/blend-green-yellow.png",
	"images/blend-blue-red.png",
	"images/blend-yellow-green.png",
	"images/blend-red-green.png",
	"images/blend-yellow-blue.png",
	"images/blend-green-red.png",
	"images/blend-blue-yellow.png",
	"images/blend-blue-green.png",
	"images/blend-green-blue.png",

	// Not so useful:
	//
	// "images/blend-red-yellow.png",
	// "images/blend-yellow-red.png",
    };

    public void create(Pattern p) {
	super.create(p);
	for (int i = 0; i < inputs.length; i++) {
	    PImage source = pattern.app.loadImage(inputs[i]);

	    System.err.println("Loaded " + inputs[i]);
	    area.loadPixels();

	    this.textures[i] = new PImage(width, height);
	    this.textures[i].copy(source, 0, 0, source.width, source.height, 0, 0, width, height);
	}
    }

       // if (counter++ % 100 == 0) {
       // 	   img.save(String.format("/Users/jmacd/Desktop/dump/strange-%s.png", counter++));
       // }
    // static int counter;

    public static final float positionPeriod = 0.01f;
    public static final float periodPeriod = 0.0001f;

    float pElapsed;

    @Override
    public void render(float vdelta) {
	pElapsed += vdelta * period.value();
	super.render(vdelta);
    }

    // TODO do not supersample this pattern.
    
    @Override
    public void drawFragment() {

	int periodNum = (int) ((pElapsed) % periodPeriod);

    	int posIdx = periodNum % patterns.length; 
	
	int positions[] = patterns[posIdx];

	int pattern = positions[((int) (elapsed() / positionPeriod)) % positions.length];

	PImage img = textures[pattern];
	// System.err.println("copy " + periodNum + " " + posIdx + " " + pattern + " " + img.width + " " + img.height);

	area.copy(img, 0, 0, width, height, 0, 0, width, height);
    } 
}

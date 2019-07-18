package art.lookingup.colors;

import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PGraphics;

public class Gradient {

    public static int[] get(PGraphics gr, int count) {
	  int colors[] = new int[count];
	  
	  int min1 = Colors.hsb(0, 1, 1);
	  int max1 = Colors.hsb(0.333333333f, 1, 1);
	  for (int i = 0; i < count/3; i++) {
	      float r = (float)i / (float)(count/3);
	      int c = gr.lerpColor(min1, max1, r);
	      colors[i] = c;
	  }

	  int min2 = Colors.hsb(0.333333333f, 1, 1);
	  int max2 = Colors.hsb(0.666666667f, 1, 1);
	  for (int i = 0; i < count/3; i++) {
	      float r = (float)i / (float)(count/3);
	      int c = gr.lerpColor(min2, max2, r);
	      colors[count/3+i] = c;
	  }

	  int min3 = Colors.hsb(0.666666667f, 1, 1);
	  int max3 = Colors.hsb(1, 1, 1);
	  for (int i = 0; i < count/3; i++) {
	      float r = (float)i / (float)(count/3);
	      int c = gr.lerpColor(min3, max3, r);
	      colors[2*count/3+i] = c;
	  }

	  return colors;
    }	
};

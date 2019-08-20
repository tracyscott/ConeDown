package art.lookingup.patterns;

import art.lookingup.ConeDown;
import art.lookingup.ConeDownModel;
import art.lookingup.patterns.play.Pattern;
import heronarts.lx.LX;
import processing.core.PApplet;

public class Play extends Pattern {

  public Play(LX lx) {
    this(lx, ConeDown.pApplet, ConeDownModel.POINTS_WIDE, ConeDownModel.POINTS_HIGH);
  }

  public Play(LX lx, PApplet app, int width, int height) {
    super(lx, app, width, height);

    // this.setFragment(new ConeScoop.Factory(new Beacon.Factory(new Balls.Factory(),
    // 							  new Spiral.Factory()),
    // 				       new Blend.Factory(ADD,
    // 							 new Spiral.Factory(),
    // 							 new Spiral.Factory())));

    // this.setFragment(new Cone.Factory(new Solid.Factory(),
    // 				  new Spiral.Factory(),
    // 				  new Balls.Factory()));
    // this.setFragment(new Cone.Factory(new Balls.Factory(),
    // 				  new Balls.Factory(),
    // 				  new Balls.Factory()));
    // this.setFragment(new ConeScoop.Factory(new Spiral.Factory(),
    // 				       new Spiral.Factory()));

    // this.setFragment(new Blend.Factory(ADD,
    // 				   new Spiral.Factory(),
    // 				   new Spiral.Factory()));

    // this.setFragment(new Spiral.Factory());
    // this.setFragment(new Solid.Factory());
    this.setFragment(new art.lookingup.patterns.play.fragments.Balls.Factory("b"));
    // this.setFragment(new ConeScoop.Factory(new Chase.Factory(), new Spiral.Factory()));
    // this.setFragment(new Cubes.Factory("c"));
  }
};

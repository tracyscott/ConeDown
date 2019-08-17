package art.lookingup.patterns.play;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class Parameter {
    public final LXParameter lxp;

    public static interface Adder {
	void registerParameter(Parameter p);
    }

    public Parameter(Fragment frag, String name, float init, float min, float max) {
	this.lxp = new CompoundParameter(name, init, min, max);
    }

    public float value() {
	return lxp.getValuef();
    }

    public void setValue(double v) {
	lxp.setValue(v);
    }
}

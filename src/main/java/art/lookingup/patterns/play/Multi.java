package art.lookingup.patterns.play;

import heronarts.lx.LX;

public abstract class Multi extends Fragment {
    final protected Fragment[] fragments;

    public Multi(LX lx, int width, int height, Fragment ...fragments) {
	super(width, height);
	this.fragments = fragments;
    }

    @Override
    public void create(Pattern p) {
	super.create(p);
	for (Fragment f : fragments) {
	    f.create(p);
	}
    }    

    @Override
    public void preDrawFragment(float vdelta) {
	super.preDrawFragment(vdelta);
	for (Fragment f : fragments) {
	    f.render(vdelta);
	}
    }

    @Override
    public void setup() {
	super.setup();
	for (Fragment f : fragments) {
	    f.setup();
	}
    }

    @Override
    public void registerParameters(Parameter.Adder adder) {
	super.registerParameters(adder);
	for (Fragment f : fragments) {
	    f.registerParameters(adder);
	}
    }
}

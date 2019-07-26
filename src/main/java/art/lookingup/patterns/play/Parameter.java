package art.lookingup.patterns.play;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
    Fragment frag;
    String name;
    float value;
    float min;
    float max;

    public Parameter(Fragment frag, String name, float init, float min, float max) {
	this.frag = frag;
	this.name = name;
	this.min = min;
	this.max = max;
	this.value = init;
    }

    public float value() {
	return value;
    }

    public void setValue(float v) {
	value = v;
	frag.notifyChange();
    }
}

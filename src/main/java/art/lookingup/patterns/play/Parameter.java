package art.lookingup.patterns.play;

public class Parameter {

    String name;
    float value;
    float min;
    float max;

    public Parameter(String name, float init, float min, float max) {
	this.name = name;
	this.value = init;
	this.min = min;
	this.max = max;
    }

    public float value() {
	return this.value;
    }

    public void setValue(float val) {
	this.value = val;
    }
}

package art.lookingup.patterns.shapes;

public class Parabola {
    // Creates a downward facing parabola.
    //
    // General equation x^2 = -Ay + C
    // i.e., y = (-x^2 + C) / A
    //
    // @param is the height that intercepts y=0 with width=1, i.e.,
    // determines the height that yields x = +/- 0.5.
    public Parabola(int steps, float param) {
	this.yValues = new float[steps];

	float interval = 1f / steps;
	float C = 1 / 4f;
	float A = 1 / (4 * param);

	for (int i = 0; i < steps; i++) {
	    float x = -0.5f + i * interval;

	    yValues[i] = -(x * x - C) / (4 * A);
	}
    }

    public float Value(float x) {
	if (x < -0.5) {
	    return 0;
	}
	if (x >= 0.5) {
	    return 0;
	}
	int interval = (int)((x + 0.5f) * yValues.length);
	return yValues[interval];
    }

    float []yValues;
}

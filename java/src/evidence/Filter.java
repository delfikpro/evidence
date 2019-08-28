package evidence;

@FunctionalInterface
public interface Filter {

	float filter(double x, double y, float color, int channel);

}

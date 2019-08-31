package evidence.render;

@FunctionalInterface
public interface Filter {

	float filter(double x, double y, float color, int channel, float dstColor);

}

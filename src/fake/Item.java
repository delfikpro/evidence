package fake;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public interface Item {

	void draw(Screenshot s, BufferedImage i, int x, int y);

}

package evidence;

import evidence.render.ScaledImage;

import java.awt.image.BufferedImage;

public interface Item {

	void draw(ScaledImage s, BufferedImage i, int x, int y);

}

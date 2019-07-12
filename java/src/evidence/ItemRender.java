package evidence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemRender {

	public static final Map<String, Item> drawer = new HashMap<>();

	public static void draw(Screenshot screen, String name, int slot, int x) throws IOException {
		BufferedImage img = ImageIO.read(new File("pages/" + name + ".png"));
		int y = screen.getHeight() - 38;
		Item item = drawer.get(name);
		if (item != null) item.draw(screen, img, x + 6 + 40 * slot, y);
		else screen.getGraphics().drawImage(img, x + 6 + 40 * slot, y, null);
	}

	static {
		drawer.put("compass", (s, i, x, y) -> s.pasteImage(i, 0, 32 * Generator.compassPos, 32, 32, x, y));
	}

}

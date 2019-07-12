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
		BufferedImage img = ImageIO.read(ResourcePack.get("assets/minecraft/textures/items/" + name + ".png"));
		int y = screen.getHeight() - 38;
		Item item = drawer.get(name);
		int x1 = x + 6 + 40 * slot;
		if (item != null) item.draw(screen, img, x1, y);
		else screen.getGraphics().drawImage(img, x1, y, x1 + 32, y + 32, 0, 0, 16, 16, null);
	}

	static {
		drawer.put("compass", (s, i, x, y) -> s.getGraphics().drawImage(i, x, y, x + 32, y + 32, 0, 16 * Generator.compassPos, 16, 16 * Generator.compassPos + 16, null));
	}

}

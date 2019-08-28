package evidence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemRender {

	public static final Map<String, Item> drawer = new HashMap<>();

	public static void draw(Image screen, String name, int slot, int x) throws IOException {
		BufferedImage img = ImageIO.read(ResourcePack.get("assets/minecraft/textures/items/" + name + ".png"));
		int y = screen.getHeight() - 19;
		Item item = drawer.get(name);
		int x1 = x + 3 + 20 * slot;
		if (item != null) item.draw(screen, img, x1, y);
		else screen.draw(img.getRaster(), 0, 0, 1, 1, x1, y, x1 + 16, y + 16);
	}

	static {
		drawer.put("compass", (s, i, x, y) -> {
			s.draw(i.getRaster(), 0, Test.compassPos / 32d, 1, (Test.compassPos + 1) / 32d, x, y, x + 16, y + 16);
//			s.getGraphics().drawImage(i, x, y, x + 32, y + 32, 0, 16 * Generator.compassPos, 16, 16 * Generator.compassPos + 16, null)
		});
	}

}

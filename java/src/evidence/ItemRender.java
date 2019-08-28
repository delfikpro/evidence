package evidence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemRender {

	public static final Filter GLINT_FILTER = new Glint();

	public static final Map<String, Item> drawer = new HashMap<>();

	public static void draw(Image i, String name, int slot, int x) throws IOException {
		draw(i, name, slot, x, 1);
	}
	public static void draw(Image i, String name, int slot, int x, int size) throws IOException {
		BufferedImage img = ImageIO.read(ResourcePack.get("assets/minecraft/textures/items/" + name + ".png"));
		int y = (i.getHeight() - 19) / size;
		Item item = drawer.get(name);
		int x1 = x + 3 + 20 * slot;
		i.filter(GLINT_FILTER);
		if (item != null) item.draw(i, img, x1, y);
		else i.draw(img.getRaster(), 0, 0, 1, 1, x1, y, x1 + 16 * size, y + 16 * size);
		i.filter(null);
	}

	static {
		drawer.put("compass", (s, i, x, y) -> {
			s.draw(i.getRaster(), 0, Test.compassPos / 32d, 1, (Test.compassPos + 1) / 32d, x, y, x + 16, y + 16);
//			s.getGraphics().drawImage(i, x, y, x + 32, y + 32, 0, 16 * Generator.compassPos, 16, 16 * Generator.compassPos + 16, null)
		});
	}

}

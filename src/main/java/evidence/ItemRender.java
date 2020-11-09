package evidence;

import evidence.render.Filter;
import evidence.render.Glint;
import evidence.render.ScaledImage;
import evidence.resources.ResourcePack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRender {

	public static final Filter GLINT_FILTER = new Glint();
	public static final List<String> ENCHANTED_ITEMS = Arrays.asList(
			"nether_star", "experience_bottle", "potion"
																	);

	public static final Map<String, Item> drawer = new HashMap<>();

	public static void draw(Evidence evidence, ScaledImage i, String name, int slot, int x) throws IOException {
		draw(evidence, i, name, slot, x, 1);
	}
	public static void draw(Evidence evidence, ScaledImage i, String name, int slot, int x, int size) throws IOException {
		BufferedImage img = evidence.getResourceStack().getTexture("assets/minecraft/textures/items/" + name + ".png");
		int y = (i.getHeight() - 19) / size;
		Item item = drawer.get(name);
		int x1 = x + 3 + 20 * slot;
		if (ENCHANTED_ITEMS.contains(name.toLowerCase())) i.filter(GLINT_FILTER);
		if (item != null) item.draw(evidence, i, img, x1, y);
		else i.draw(img, 0, 0, 1, 1, x1, y, x1 + 16 * size, y + 16 * size);
		i.filter(null);
	}

	static {
		drawer.put("compass", (evidence, s, i, x, y) -> {
			double amount = (int) (i.getHeight() / i.getWidth());
			int pos = evidence.compassPos;
			if (pos + 1 > amount) pos = 0;
			s.draw(i, 0, pos / amount, 1, (pos + 1) / amount, x, y, x + 16, y + 16);
//			image.getGraphics().drawImage(i, x, y, x + 32, y + 32, 0, 16 * Generator.compassPos, 16, 16 * Generator.compassPos + 16, null)
		});
	}

}

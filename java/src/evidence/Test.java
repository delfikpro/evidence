package evidence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException {

		new ResourcePack(new File("resourcepacks/vanilla.zip"));
		Image i = new Image(new File("example01.png"), 2);

		BufferedImage t = ImageIO.read(new File("glass_lime.png"));
		System.out.println(t.getType());
		System.out.println(t.getRaster().getNumBands());
		Font.bind(i);
//		i.draw(t.getRaster(), 0, 0, 1, 0.5, 40, 40, 500, 500, 1);
		Font.drawStringWithShadow("§6[G] §7<§f-CHP-§7> §d[H2W] Fedos534 §7[§8Ниггер §6LVL 9§7]: §fДелфик", 10, 10);
		i.flush();
		File file = new File(System.currentTimeMillis() + ".png");
		i.save(file);
		System.out.println("saved to " + file.getName());

	}

}

package evidence.render;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

public class Util {

	public static void main(String[] args) throws IOException {
		BufferedImage read = ImageIO.read(new File("env/resourcepacks/icons.png"));
		System.out.println("TYPE: " + read.getType());
		ImageIO.write(convertColorspace(read, BufferedImage.TYPE_4BYTE_ABGR), "PNG", new File("env/resourcepacks/icons2.png"));
	}

	public static BufferedImage convertColorspace(BufferedImage image, int newType) {

		try {
			BufferedImage raw_image = image;
			image = new BufferedImage(raw_image.getWidth(), raw_image.getHeight(), newType);
			ColorConvertOp xformOp = new ColorConvertOp(null);
			xformOp.filter(raw_image, image);
		} catch (Exception e) {
			System.out.println("Exception " + e + " converting image");

		}

		return image;
	}

}

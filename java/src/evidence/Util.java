package evidence;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class Util {



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

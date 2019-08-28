package evidence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GlintDataFetcher {


	public static void main(String[] args) throws IOException {
		File f = new File("glint.png");
		BufferedImage read = ImageIO.read(f);
		for (int i = 0; i < read.getWidth(); i++) {
			System.out.print(read.getRaster().getPixel(i, 0, new int[3])[0] + ", ");
		}

	}

}

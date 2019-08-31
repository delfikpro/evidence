package evidence.resources;

import evidence.render.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

public class ResourcePack {

	public static final List<ResourcePack> LIST = new ArrayList<>();

	public static BufferedImage getTexture(String resource) throws IOException {
		BufferedImage img = ImageIO.read(get(resource));
		return img.getType() == TYPE_4BYTE_ABGR ? img : Util.convertColorspace(img, TYPE_4BYTE_ABGR);
	}

	public static InputStream get(String resource) throws IOException {
		Exception e = null;
		for (int i = LIST.size() - 1; i >= 0; i--) {
			try {
				ResourcePack rp = LIST.get(i);
				InputStream f = rp.getResource(resource);
				if (f != null) return f;
			} catch (Exception ex) {
				e = ex;
			}
		}
		throw new RuntimeException("Not found " + resource, e);
	}

	public InputStream getResource(String resource) throws IOException {
		return unzipper.getFile(resource);
	}

	private final Unzipper unzipper;

	public ResourcePack(File zip) throws IOException {
		this.unzipper = new Unzipper(zip);
		LIST.add(this);
	}

}

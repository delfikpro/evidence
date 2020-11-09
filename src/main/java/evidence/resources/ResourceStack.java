package evidence.resources;

import evidence.EvidenceException;
import evidence.render.Util;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

@Data
public class ResourceStack {

	private final List<ResourcePack> resourcePacks = new ArrayList<>();
	private final Map<String, BufferedImage> cache = new HashMap<>();

	public void push(ResourcePack resourcePack) {
		this.resourcePacks.add(resourcePack);
	}

	public BufferedImage getTexture(String resource) {
		if (cache.containsKey(resource)) return cache.get(resource);
		InputStream input = get(resource);
		BufferedImage image;
		try {
			image = ImageIO.read(input);
			input.close();
		} catch (IOException exception) {
			throw new EvidenceException("Unable to read texture", exception);
		}
		if (image.getType() != TYPE_4BYTE_ABGR)
			image = Util.convertColorspace(image, TYPE_4BYTE_ABGR);
		cache.put(resource, image);
		return image;
	}

	public InputStream get(String resource) {
		Exception e = null;
		for (int i = resourcePacks.size() - 1; i >= 0; i--) {
			try {
				ResourcePack rp = resourcePacks.get(i);
				InputStream f = rp.getResource(resource);
				System.out.println("Searching for " + resource + " in " + rp + ": " + (f == null ? "missing" : "OK"));
				if (f != null) return f;
			} catch (Exception ex) {
				e = ex;
			}
		}
		throw new RuntimeException("Not found " + resource, e);
	}

}

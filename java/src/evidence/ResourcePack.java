package evidence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ResourcePack {

	public static final List<ResourcePack> LIST = new ArrayList<>();

	public static InputStream get(String resource) throws IOException {
		for (int i = LIST.size() - 1; i >= 0; i--) {
			ResourcePack rp = LIST.get(i);
			InputStream f = rp.getResource(resource);
			if (f != null) return f;
		}
		throw new NoSuchElementException(resource);
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

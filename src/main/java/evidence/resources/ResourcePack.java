package evidence.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourcePack {

	private final String address;
	private final ZipFile zipFile;

	public ResourcePack(String address, File zip) throws IOException {
		this.address = address;
		this.zipFile = new ZipFile(zip);
	}

	@Override
	public String toString() {
		return "ResourcePack(" + address + ")";
	}

	public InputStream getResource(String path) throws IOException {
		ZipEntry entry = zipFile.getEntry(path);
		return zipFile.getInputStream(entry);
	}

}

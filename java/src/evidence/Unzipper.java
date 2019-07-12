package evidence;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Unzipper {

	private final ZipFile zipFile;

	public Unzipper(File file) throws IOException {
		this.zipFile = new ZipFile(file);
	}

	public InputStream getFile(String path) throws IOException {
		ZipEntry entry = zipFile.getEntry(path);
		try {
			return zipFile.getInputStream(entry);
		} catch (NullPointerException ex) {
			System.out.println(path + " not found");
			throw ex;
		}
	}

}

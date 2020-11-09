package evidence;

import evidence.resources.ResourcePack;
import evidence.resources.ResourceStack;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class EvidenceManager {

	private final Map<String, ResourcePack> resourcePacks = new HashMap<>();
	private ResourcePack vanillaPack;

	public void init(Logger logger) {
		try {

			File resourcePackDir = new File("resourcepacks");
			if (!resourcePackDir.isDirectory()) resourcePackDir.mkdir();

			for (File file : Objects.requireNonNull(resourcePackDir.listFiles())) {
				if (file.isDirectory() || !file.getName().endsWith(".zip")) continue;
				String address = file.getName().replace(".zip", "");
				ResourcePack resourcePack = new ResourcePack(address, file);
				resourcePacks.put(address, resourcePack);
			}

			this.vanillaPack = this.resourcePacks.get("vanilla");
			if (this.vanillaPack == null)
				throw new FileNotFoundException("resourcepacks/vanilla.zip is required.");

		} catch (Exception exception) {
			logger.log(Level.SEVERE, "Failed to initialize EvidenceManager: ", exception);
		}

	}

	public ResourceStack createResourceStack() {
		ResourceStack resourceStack = new ResourceStack();
		resourceStack.push(this.vanillaPack);
		return resourceStack;
	}

}

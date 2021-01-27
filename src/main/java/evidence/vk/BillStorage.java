package evidence.vk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

public class BillStorage {

	public final static Gson gson = new GsonBuilder()
			.registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
				@Override
				public void write(JsonWriter out, ZonedDateTime value) throws IOException {
					out.value(value.toString());
				}

				@Override
				public ZonedDateTime read(JsonReader in) throws IOException {
					return ZonedDateTime.parse(in.nextString());
				}
			}).create();

	public EvidenceBill getBill(UUID uuid) {
		try {
			return gson.fromJson(Files.readAllLines(getFile(uuid).toPath()).get(0), EvidenceBill.class);
		} catch (IOException exception) {
			return null;
		}
	}

	public File getFile(UUID uuid) {
		return new File("bills/" + uuid);
	}

	@SneakyThrows
	public void saveBill(EvidenceBill bill) {
		File file = getFile(bill.getUuid());
		file.getParentFile().mkdir();
		Path path = file.toPath();
		Files.write(path, Collections.singleton(gson.toJson(bill)));
	}

}

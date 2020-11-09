package evidence.vimeworld;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import vimeworld.Guild;
import vimeworld.Player;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class VimeWorld {

	public static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
	public static final String API_DOMAIN = "https://api.vimeworld.ru/";

	private final HttpClient client;
	private final Gson gson = new Gson();
	private final String token;

	public <T> CompletableFuture<T> request(Class<T> responseType, String method) {

		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API_DOMAIN + method));
		if (this.token != null) builder.header("Access-Token", this.token);
		return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofInputStream())
				.thenApply(stream -> gson.fromJson(new InputStreamReader(stream.body()), responseType));

	}

	public CompletableFuture<Player> getPlayer(String username) {

		return request(Player[].class, "user/name/" + username).thenApply(users -> users[0]);
	}

	public CompletableFuture<Guild> getGuild(int id) {
		return request(Guild.class, "guild/get?id=" + id);
	}

	public CompletableFuture<List<Player>> getPlayers(String... names) {

		for (String name : names) {
			if (!USERNAME_PATTERN.matcher(name).matches())
				throw new IllegalArgumentException("Username should be 3-16 characters and contain only A-Z, a-z, 0-9 or _");
		}

		return request(Player[].class, "user/name/" + String.join(",", names))
				.thenApply(Arrays::asList);
	}

}

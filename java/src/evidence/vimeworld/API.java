package evidence.vimeworld;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class API {
	private API() {}

	protected static String readRequest(String address) {
		try {
			String server = new String(address.getBytes(StandardCharsets.UTF_8), "windows-1251");
//			server += (server.contains("?") ? "&" : "?") + "token=" + Callisto.getVimeToken();
			URL url = new URL(server);
			URLConnection connection = url.openConnection();
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String result = reader.readLine();
			reader.close();
			if (result.equals("[]") || result.startsWith("{\"error\":")) throw new IllegalArgumentException();
			return result;
		} catch (IllegalArgumentException ex) {
		  throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static evidence.vimeworld.Guild getGuild(String guildname) {
		try {
			String rawJson = readRequest("http://api.vime.world/guild/get?name=" + guildname);
			return new evidence.vimeworld.Guild(new JSONObject(rawJson));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static evidence.vimeworld.Guild getGuild(int id) {
		try {
			String rawJson = readRequest("http://api.vime.world/guild/get?id=" + id);
			return new evidence.vimeworld.Guild(new JSONObject(rawJson));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static evidence.vimeworld.Player getPlayer(String player) {
		try {
			String rawJson = readRequest("http://api.vime.world/user/name/" + player);
			JSONArray array = new JSONArray(rawJson);
			return new evidence.vimeworld.Player(array.getJSONObject(0));
		} catch (IllegalArgumentException e) {
			return null;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<evidence.vimeworld.Player> getPlayers(Integer... ids) {
		List<evidence.vimeworld.Player> list = new ArrayList<>(ids.length);
		String parameter = Utils.merge(ids, String::valueOf, ",");
		String rawJson = readRequest("http://api.vime.world/user/" + parameter);
		try {
			JSONArray array = new JSONArray(rawJson);
			return Utils.transform(Utils.toJavaList(array), Player::new);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}

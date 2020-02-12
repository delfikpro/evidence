package evidence.vimeworld;

import evidence.Evidence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class API {
	private API() {}

	static {
		try {
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static String readRequest(String address) {
		return readRequest(address, true, Collections.emptyMap());
	}
	public static String readRequest(String address, boolean read, Map<String, String> headers) {
		try {
			String server = new String(address.getBytes(StandardCharsets.UTF_8), "windows-1251");
			URL url = new URL(server);
			URLConnection connection = url.openConnection(read ? Evidence.proxy : Proxy.NO_PROXY);
			for (Map.Entry<String, String> e : headers.entrySet()) {
				connection.setRequestProperty(e.getKey(), e.getValue());
			}
			connection.connect();
			try {
				InputStream is = connection.getInputStream();
			} catch (FileNotFoundException ex) {
				if (!read) return "";
				else System.out.println("штото не так");
			}
			if (!read) return "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String result = reader.readLine();
			reader.close();
			if (result.equals("[]") || result.startsWith("{\"error\":")) throw new IllegalArgumentException(address + " returned " + result);
			return result;
		} catch (IllegalArgumentException ex) {
		    if (read) throw ex;
		} catch (Exception ex) {
			if (read) throw new RuntimeException(ex);
		}
		return "";
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
			String rawJson = readRequest("http://api.vimeworld.ru/guild/get?id=" + id);
			return new evidence.vimeworld.Guild(new JSONObject(rawJson));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static evidence.vimeworld.Player getPlayer(String player) {
		try {
			String rawJson = readRequest("http://api.vimeworld.ru/user/name/" + player);
			JSONArray array = new JSONArray(rawJson);
			return new evidence.vimeworld.Player(array.getJSONObject(0));
		} catch (IllegalArgumentException e) {
			System.out.println("PLAYER '" + player + "' NOT FOUND");
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<evidence.vimeworld.Player> getPlayers(Integer... ids) {
		List<evidence.vimeworld.Player> list = new ArrayList<>(ids.length);
		String parameter = Utils.merge(ids, String::valueOf, ",");
		String rawJson = readRequest("http://api.vimeworld.ru/user/" + parameter);
		try {
			JSONArray array = new JSONArray(rawJson);
			return Utils.transform(Utils.toJavaList(array), Player::new);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}

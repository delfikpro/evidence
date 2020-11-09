package evidence.vimeworld;

import vimeworld.Guild;
import vimeworld.Player;
import vimeworld.Rank;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class Vime {

	public static final VimeWorld instance = new VimeWorld(HttpClient.newHttpClient(), null);

	public static final Map<String, Player> playercache = new HashMap<>();
	private static final Map<Integer, Guild> guildcache = new HashMap<>();
	public static Map<String, String> prefixes = new HashMap<>();

	public static Player getPlayer(String name) {
		if (name.contains("[")) {
			String[] ss = name.split(" ");
			name = ss[1];
			prefixes.put(name.toLowerCase(), ss[0]);
		}
		return playercache.computeIfAbsent(name.toLowerCase(), s -> instance.getPlayer(s).join());
	}

	public static String getPlayerMultiplier(Player p) {
		int m = p.getRank().getMultiplier();
		if (p.getGuild() == null) return m + "";
		Guild guild = guildcache.computeIfAbsent(p.getGuild().getId(), id -> instance.getGuild(id).join());
		if (guild == null) return m + "";
		double add = guild.getPerkLevel(Guild.PerkType.COINS_MULT) * 0.1;
		return add == 0 || add == 1 ? String.valueOf((int) (m + add)) : String.valueOf((float) (m + add));
	}

	public static String getDisplayName(Player player) {
		String prefix = prefixes.getOrDefault(player.getName().toLowerCase(), null);
		if (prefix == null) prefix = player.getRank() != Rank.PLAYER ? player.getRank() + " " : "§7";
		else prefix += " ";
		if (!prefix.contains("§")) prefix = player.getRank().getStyle() + prefix;
		return prefix + player.getName();
	}

	public static String getSimpleName(String s) {
		Player p = getPlayer(s);
		return (p.getRank() == Rank.PLAYER ? "§e" : p.getRank().getColor()) + p.getName();
	}

}

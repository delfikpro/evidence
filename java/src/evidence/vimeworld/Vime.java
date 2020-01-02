package evidence.vimeworld;

import java.util.HashMap;
import java.util.Map;

public class Vime {

	public static final Map<String, Player> playercache = new HashMap<>();
	private static final Map<Integer, Guild> guildcache = new HashMap<>();
	public static Map<String, String> prefixes = new HashMap<>();

	public static Player getPlayer(String name) {
		if (name.contains("[")) {
			String[] ss = name.split(" ");
			name = ss[1];
			prefixes.put(name.toLowerCase(), ss[0]);
		}
		return playercache.computeIfAbsent(name.toLowerCase(), API::getPlayer);
	}

	public static Guild getGuild(int id) {
		return guildcache.computeIfAbsent(id, API::getGuild);
	}

	public static String getPlayerMultiplier(Player p) {
		int id = p.getGuildID();
		int m = p.getRank().getMultiplier();
		if (id == 0) return m + "";
		Guild guild = getGuild(id);
		if (guild == null) return m + "";
		float add = guild.getPerk(Guild.Perk.COINS_MULT) * 0.1F;
		return add == 0 || add == 1 ? String.valueOf((int) (m + add)) : String.valueOf(m + add);
	}

	public static String getDisplayName(Player player) {
		String prefix = prefixes.getOrDefault(player.getName().toLowerCase(), null);
		if (prefix == null) prefix = player.getRank() != Player.Rank.PLAYER ? player.getRank() + " " : "§7";
		else prefix += " ";
		if (!prefix.contains("§")) prefix = player.getRank().getStyle() + prefix;
		return prefix + player.getName();
	}

	public static String getSimpleName(String s) {
		Player p = getPlayer(s);
		return (p.getRank() == Player.Rank.PLAYER ? "§e" : p.getRank().getColor()) + p.getName();
	}

}

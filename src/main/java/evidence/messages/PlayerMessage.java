package evidence.messages;

import evidence.vimeworld.Vime;
import vimeworld.Player;
import vimeworld.Rank;

public class PlayerMessage implements Message {

	private final String name;
	private final String message;
	private final Player player;

	public PlayerMessage(String player, String message) {
		this.name = player;
		this.message = message.replace('&', '§');
		this.player = Vime.getPlayer(player);
	}

	@Override
	public String getText() {
		Player.GuildInfo guild = player.getGuild();
		String tag = guild != null && guild.getTag() != null ? "§7<" + guild.getColor().replace('&', '§') + guild.getTag() + "§7> " : "";
		return tag + Vime.getDisplayName(player) + "§7: " + (player.getRank() == Rank.CHIEF || player.getRank() == Rank.ADMIN ? "§a" : "§f") + message;
	}

	@Override
	public String toString() {
		return player + ": " + message;
	}

}

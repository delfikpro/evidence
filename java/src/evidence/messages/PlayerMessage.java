package evidence.messages;

import evidence.vimeworld.Player;
import evidence.vimeworld.Vime;

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
		return player.getTag() + Vime.getDisplayName(player) + "§7: §f" + message;
	}

	@Override
	public String toString() {
		return getText();
	}

}

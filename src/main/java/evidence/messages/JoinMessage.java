package evidence.messages;

import vimeworld.Player;
import evidence.vimeworld.Vime;

public class JoinMessage implements Message {

	private final Player player;

	public JoinMessage(String player) {
		this.player = Vime.getPlayer(player);
	}

	@Override
	public String getText() {
		return "+ " + Vime.getDisplayName(player) + "§f вошел в лобби!";
	}

	@Override
	public String toString() {
		return "+" + player.getName();
	}

}

package vimeworld;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExtendedPlayer extends Player {

	private final Session session;

	public ExtendedPlayer(int id, String name, int level, double levelPercentage, Rank rank, long lastSeen, long playedSeconds, GuildInfo guild, Session session) {
		super(id, name, level, levelPercentage, rank, lastSeen, playedSeconds, guild);
		this.session = session;
	}

}

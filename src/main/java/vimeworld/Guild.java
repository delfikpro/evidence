package vimeworld;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class Guild {

	private final int id, level;
	private final long createdAt, totalCoins;
	private final float levelPercentage;
	private final String name, tag, color;
	private final List<Member> members;
	private final Map<PerkType, Perk> perks;

	@Data
	public static class Member {

		private final String name;
		private final Status status;
		private final long joinedAt;
		private final int guildCoins;
		private final long guildExp;

		@Getter
		@RequiredArgsConstructor
		public enum Status {
			LEADER("Лидер"),
			OFFICER("Офицер"),
			MEMBER("Участник");

			private final String description;
		}

	}

	public enum PerkType {
		MEMBERS,
		COINS,
		PARTY,
		MOTD,
		COINS_MULT,
		TAG,
		COLOR,
		GUILD_WAR
	}

	@Data
	public static class Perk {

		private final String name;
		private final int level;

	}

	public int getPerkLevel(PerkType type) {
		if (!perks.containsKey(type)) return 0;
		return perks.get(type).getLevel();
	}

}

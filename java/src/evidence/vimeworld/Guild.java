package evidence.vimeworld;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Guild {

	private final int id, level;
	private final long createdAt, totalCoins;
	private final float levelPercentage;
	private final String name, tag, color;
	private final List<Member> members;
	private final EnumMap<Perk, Integer> perks = new EnumMap<>(Perk.class);


	Guild(JSONObject json) {
		try {
			this.id = json.getInt("id");
			this.name = json.getString("name");
			String tag = null;
			try {
				tag = json.getString("tag");
			} catch (JSONException ignored) {}
			this.tag = tag;
			this.color = json.getString("color");
			this.createdAt = json.getLong("created");
			this.totalCoins = json.getLong("totalCoins");
			this.level = json.getInt("level");
			this.levelPercentage = (float) json.getDouble("levelPercentage");
			this.members = Utils.transform(Utils.toJavaList(json.getJSONArray("members")), Member::new);
			JSONObject perks = json.getJSONObject("perks");
			for (Perk perk : Perk.values()) {
				this.perks.put(perk, perks.getJSONObject(perk.name()).getInt("level"));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getLevelPoints() {
		return (int) ((level + levelPercentage) * 100);
	}
	
	public Map<String, Member> constructMemberMap() {
		Map<String, Member> map = new HashMap<>();
		for (Member member : members) map.put(member.getName().toLowerCase(), member);
		return map;
	}
	

	public static class Member {
		private final String name;
		private final Status status;
		private final long joinedAt;
		private final int guildCoins;
		private final long guildExp;

		Member(JSONObject json) {
			try {
				this.name = json.getJSONObject("user").getString("username");
				this.status = Status.valueOf(json.getString("status"));
				this.joinedAt = json.getLong("joined");
				this.guildCoins = json.getInt("guildCoins");
				this.guildExp = json.getLong("guildExp");
			} catch (JSONException ex) {
				throw new RuntimeException(ex);
			}
		}

		public enum Status {
			LEADER("Лидер"),
			OFFICER("Staff"),
			MEMBER("Участник");
			private final String description;

			Status(String description) {
				this.description = description;
			}

			@Override
			public String toString() {
				return description;
			}
		}

		public String getName() {
			return name;
		}

		public String getLowerCaseName() {
			return name.toLowerCase();
		}
		
		public int getGuildCoins() {
			return guildCoins;
		}

		public long getGuildExp() {
			return guildExp;
		}

		public long getJoinedAt() {
			return joinedAt;
		}

		public Status getStatus() {
			return status;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	public enum Perk {
		MEMBERS, COINS, PARTY, MOTD, COINS_MULT, TAG, COLOR, GUILD_WAR
	}

	public int getPerk(Perk perk) {
		return perks.get(perk);
	}

	public List<Member> getMembers() {
		return members;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public float getLevelPercentage() {
		return levelPercentage;
	}

	public long getTotalCoins() {
		return totalCoins;
	}

	public String getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}
	
}

package evidence.vimeworld;

import evidence.Color;
import org.json.JSONException;
import org.json.JSONObject;

public class Player {
    private final int id;
    private final String name;
    private final int level;
    private final Rank rank;
    private final int guildID;
	private final String tag;

	Player(JSONObject user) {
        try {
            this.id = user.getInt("id");
            this.name = user.getString("username");
            this.level = user.getInt("level");
            this.rank = Rank.valueOf(user.getString("rank"));
            int id = 0;
            String tag = null;
            try {
				JSONObject g = user.getJSONObject("guild");
				id = g.getInt("id");
				tag = g.getString("tag");
                if (tag != null) tag = g.getString("color").replace('&', '§') + tag;
            } catch (JSONException ignored) {}
            guildID = id;
            this.tag = tag;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public enum Rank {
        PLAYER("Игрок", "§7", 1, Color.GRAY),
        VIP("VIP", "§a[V]", 2, Color.GREEN),
        PREMIUM("Premium", "§b[P]", 3, Color.AQUA),
        HOLY("Holy", "§6[H]", 4, Color.GOLD),
        IMMORTAL("Immortal", "§d[I]", 5, Color.PINK),
        BUILDER("Билдер", "§2[Билдер]", 4, Color.LEAF),
        MAPLEAD("Главный билдер", "§2[Гл. билдер]", 4, Color.LEAF),
        YOUTUBE("YouTube", "§c[You§fTube§c]", 4, Color.RED),
        DEV("Разработчик", "§3[Dev]", 4, Color.CYAN),
        ORGANIZER("Организатор", "§3[Организатор]", 4, Color.CYAN),
        MODER("Модератор", "§9[Модер]", 4, Color.BLUE),
        WARDEN("Проверенный модератор", "§9[Модер]", 4, Color.BLUE),
        CHIEF("Главный модератор", "§9[Гл. модер]", 4, Color.BLUE),
        ADMIN("Главный админ", "§3§l[Гл. админ]", 4, Color.CYAN);

        private final String title, prefix;
        private final int multiplier;
		private final Color color;

		Rank(String title, String prefix, int multiplier, Color color) {
            this.title = title;
            this.multiplier = multiplier;
            this.prefix = prefix;
            this.color = color;
        }

		public int getMultiplier() {
			return multiplier;
		}

		@Override
		public String toString() {
			return prefix;
		}

		public Color getColor() {
        	return color;
		}
	}
    
    public evidence.vimeworld.Session getSession() {
        try {
            JSONObject online = new JSONObject(API.readRequest("http://api.vime.world/user/" + id + "/session"));
            return new evidence.vimeworld.Session(online.getJSONObject("online"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getId() {
        return id;
    }

	public String getTag() {
		return tag == null ? "" : "§7<§f" + tag + "§7> ";
	}

	public int getGuildID() {
        return guildID;
    }

    public Rank getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return rank.prefix + (rank == Rank.PLAYER ? "" : " ") + name + " [id" + id + ", level " + level + ", guild " + guildID + "]";
    }
}

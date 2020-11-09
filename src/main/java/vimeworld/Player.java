package vimeworld;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Player {

    private final int id;

    @SerializedName("username")
    private final String name;
    private final int level;
    private final double levelPercentage;
    private final Rank rank;
    private final long lastSeen;
    private final long playedSeconds;
    private final GuildInfo guild;

    @Data
	public static class GuildInfo {

		private final int id;
		private final String name;
		private final String tag;
		private final String color;
		private final int level;
		private final double levelPercentage;

		@SerializedName("avatar_url")
		private final String avatarUrl;


	}

}

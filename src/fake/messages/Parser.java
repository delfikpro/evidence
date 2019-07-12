package fake.messages;

import fake.vimeworld.Vime;

public class Parser {

	public static Message parse(String s) {
		if (s == null) return null;
		try {
			String head = s.substring(1, s.indexOf('}'));
			String body = null;
			try {
				body = s.substring(head.length() + 3);
			} catch (StringIndexOutOfBoundsException ignored) {}
			String[] sp = head.split("-");
			switch (sp[0]) {
				case "MESSAGE":
					return new PlayerMessage(sp[1], body);
				case "JOIN":
					return new JoinMessage(sp[1]);
				case "SCREENSHOT":
					return new ScreenMessage(body);
				case "FRIEND_JOIN":
					return () -> "§2Друзья §f> §fИгрок §e" + Vime.getSimpleName(sp[1]) + " §fзашел в игру";
				case "FRIEND_QUIT":
					return () -> "§2Друзья §f> §fИгрок §e" + Vime.getSimpleName(sp[1]) + " §fвышел из игры";
				case "CUSTOM":
					String finalBody = body.replace('&', '§');
					return () -> finalBody;
			}
			return () -> "§cОШИБКА: §fНекорректный заголовок строчки чата!";
		} catch (StringIndexOutOfBoundsException ex) {
			return () -> "§cОШИБКА: §fНекорректный формат строчки!";
		} catch (Throwable e) {
			e.printStackTrace();
			return () -> "§cОШИБКА: §fНеизвестная ошибка, пиши делфику";
		}
	}

}

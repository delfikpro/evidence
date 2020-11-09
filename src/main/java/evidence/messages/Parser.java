package evidence.messages;

import evidence.vimeworld.Vime;

public class Parser {

	public static Message routine(String s) {
		if (s == null) return null;
		try {
			s = s.replace("&", "§");
			if (s.startsWith("#")) {
				String finalS = s.substring(1);
				return () -> finalS;
			}
			int i = s.indexOf(": ");
			if (i != -1) {
				String name = s.substring(0, i);
				String msg = s.substring(i + 2);
				return new PlayerMessage(name, msg);
			}
			if (s.startsWith("+ ")) {
				String name = s.substring(2);
				return new JoinMessage(name);
			}
			if (s.toLowerCase().startsWith("f2")) {
				String[] ss = s.split(" ");
				return new ScreenMessage(ss.length < 2 ? null : ss[1]);
			}
			String finalS = s;
			return () -> finalS;
		} catch (StringIndexOutOfBoundsException ex) {
			return () -> "§cОШИБКА: §fНекорректный формат строчки!";
		} catch (Throwable e) {
			e.printStackTrace();
			return () -> "§cОШИБКА: §fНеизвестная ошибка, пиши делфику";
		}
	}

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

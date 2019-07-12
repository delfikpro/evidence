package fake;

import com.sun.jmx.snmp.agent.SnmpGenericMetaServer;
import fake.messages.Message;
import fake.messages.Parser;
import fake.vimeworld.Player;
import fake.vimeworld.Vime;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Generator {

	public static int compassPos;
	static Screenshot s;

	public static void main(String[] args) throws IOException {

		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream("fake.yml");
		Map<String, Object> yml = yaml.load(inputStream);
//		System.out.println(yml);
		System.out.println();

		String playername = (String) yml.get("name");
		String servername = (String) yml.get("server");
		int coins = (int) yml.get("coins");
		int selectedSlot = (int) yml.get("slot");
		compassPos = (int) yml.get("compass-target");
		List<String> chatIn = (List<String>) yml.get("chat");
		List<Message> chat = new ArrayList<>();
		Vime.prefixes = (Map<String, String>) yml.get("prefixes");
		String preset = (String) yml.get("preset");
		int chatOpened = (int) yml.get("chatOpened");


		long start = System.currentTimeMillis();
		execute("Processing chat messages", () -> {
			for (String c : chatIn) chat.add(Parser.parse(c));
			Collections.reverse(chat);
		});
		Player p = Vime.getPlayer(playername);

		execute("Loading file", () -> s = new Screenshot(args.length == 0 ? "screen.png" : args[0], 1));

		execute("Drawing hand", () -> {
			String handPath = "pages/hand" + s.getWidth() + ".png";
			File hand = new File(handPath);
			if (!hand.exists()) {
				System.out.println("Screenshot's width is NOT SUPPORTED! Hand will be distorted!");
				handPath = "pages/hand1920.png";
			}
			s.overlapImage(handPath);
		});

		execute("Adding texteria", () -> {
			s.drawStringWithShadow("[§e" + p.getLevel() + "§f] " + p.getName(), 4, 4);
			s.drawStringWithShadow(servername, 4, 24);
			String multiplier = Vime.getPlayerMultiplier(p);
			s.drawStringWithShadow((multiplier.equals("1") ? "" : ("§e[§dx" + multiplier + "§e] ")) + "§fКоличество коинов: §e" + coins, 4, s.getHeight() - 50);
		});

		execute("Drawing crosshair", () ->
			s.drawImageInversionMask("pages/cross.png", s.getWidth() / 2 - 14, s.getHeight() / 2 - 14)
		);





		int ix = s.getWidth() / 2 - 182;
		execute("Drawing inventory", () -> {
			BufferedImage i = ImageIO.read(new File("pages/widgets.png"));
			s.getGraphics().drawImage(i, ix, s.getHeight() - 44,
					s.getWidth() / 2 + 182, s.getHeight(), 0, 0, 364, 44, null);
			int slot = s.getWidth() / 2 - 184 + 40 * selectedSlot;
			s.getGraphics().drawImage(i, slot, s.getHeight() - 46, slot + 48, s.getHeight() - 2, 0, 44, 48, 88, null);
		});

		int health = 20;
		int hunger = 20;
		execute("Drawing bars", () -> {
			BufferedImage icons = ImageIO.read(new File("pages/icons.png"));
			drawBar(icons, ix, s.getHeight() - 78, 32, 104, 122, 0, health, false);
			drawBar(icons, ix + 182 * 2 - 18, s.getHeight() - 78, 32, 104, 122, 54, hunger, true);

			s.pasteImage(icons, 0, 128, 364, 10, ix, s.getHeight() - 58);
//			for (int i = 0; i < 10; i++) {
//				s.getGraphics().drawImage(icons, s.getWidth() / 2 - 182 + i * 16, s.getHeight() - 78,
//						s.getWidth() / 2 - 182 + i * 16 + 18, s.getHeight() - 60,
//						32, 0, 50, 18, null);
//			}
//			boolean half = health % 2 != 0;
//			int health = health / 2;
//			for (int i = 0; i < health; i++) {
//				s.getGraphics().drawImage(icons, s.getWidth() / 2 - 182 + i * 16, s.getHeight() - 78,
//						s.getWidth() / 2 - 182 + i * 16 + 18, s.getHeight() - 60,
//						104, 0, 122, 18, null);
//			}
//			if (half) {
//				s.getGraphics().drawImage(icons, s.getWidth() / 2 - 182 + health * 16, s.getHeight() - 78,
//						s.getWidth() / 2 - 182 + health * 16 + 10, s.getHeight() - 60,
//						104, 0, 114, 18, null);
//			}
		});

		execute("Drawing items", () -> {
			String[] items;

			switch (preset) {
				case "LOBBY":
					items = new String[] {"compass", "star", null, null, "brewing_stand", null, null, null, "comparator"};
					break;
				case "BP":
					items = new String[] {"slimeball", "trail", null, null, null, null, null, "star", "compass"};
					break;
				case "KP":
					items = new String[] {"emerald", "trail", null, null, null, null, null, "star", "compass"};
					break;
				case "GG":
					items = new String[] {"slimeball", "ender_eye", "trail", null, null, null, null, "star", "compass"};
					break;

				default:
					items = preset.split("\\|");
					for (int i = 0; i < items.length; i++) items[i] = items[i].equals("-") ? null : items[i];
					break;
			}

			int slot = -1;
			for (String item : items) {
				slot++;
				if (item == null) continue;
				ItemRender.draw(s, item, slot, ix);
			}

//			BufferedImage
//					compass = ImageIO.read(new File("pages/compass.png")),
//					comparator = ImageIO.read(new File("pages/comparator.png")),
//					star = ImageIO.read(new File("pages/star.png")),
//					brewing = ImageIO.read(new File("pages/brewing_stand.png"));
//			int iy = s.getHeight() - 38;
//			s.getGraphics().drawImage(star, ix + 6 + 40, iy, null);
//			s.getGraphics().drawImage(comparator, ix + 6 + 40 * 8, iy, null);
//			s.getGraphics().drawImage(brewing, ix + 6 + 40 * 4, iy, null);
//			s.pasteImage(compass, 0, 32 * compassPos, 32, 32, ix + 6, iy);
		});

		execute("Drawing chat", () -> {
			int lines = chat.size();
			for (int i = 0; i < lines; i++) {
				int y = s.getHeight() - 56 - (i + 1) * 18;
				s.drawBlackRect(4, y, 648, 18);
				s.drawStringWithShadow(chat.get(i).getText(), 4, y + 2);
			}
			if (chatOpened != 0) {
				s.drawBlackRect(4, s.getHeight() - 27, s.getWidth() - 8, 24);
				if (chatOpened == 2) {
					s.drawStringWithShadow("_", 8, s.getHeight() - 23);
				}
			}
		});



		execute("Saving result", () -> s.save(System.currentTimeMillis() + ".png"));

		long end = System.currentTimeMillis();

		System.out.println("Done in " + (end - start) + " ms.");

	}

	static void drawBar(BufferedImage image, int x, int y, int txEmpty, int txFull, int txHalf, int ty, int value, boolean reverse) {
		for (int c = 0; c < 10; c++) {
			int i = reverse ? -c : c;
			s.getGraphics().drawImage(image, x + i * 16, y,
					x + i * 16 + 18, y + 18,
					txEmpty, ty, txEmpty + 18, ty + 18, null);
		}
		int h = value % 2;
		value /= 2;
		for (int c = 0; c < value + h; c++) {
			int i = reverse ? -c : c;
			s.getGraphics().drawImage(image, x + i * 16, y,
					x + i * 16 + 18, y + 18,
					i == value ? txHalf : txFull, ty, (i == value ? txHalf : txFull) + 18, ty + 18, null);
		}
	}

	static void execute(String s, DangerousRunnable r) throws IOException {
		System.out.print(s + "...  ");
		long start = System.currentTimeMillis();
		r.run();
		long end = System.currentTimeMillis();
		System.out.print("Done in " + (end - start) + " ms.\n");

	}

	@FunctionalInterface
	private interface DangerousRunnable {
		void run() throws IOException;
	}


}

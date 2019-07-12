package evidence;

import evidence.messages.Message;
import evidence.messages.Parser;
import evidence.vimeworld.Player;
import evidence.vimeworld.Vime;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
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

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		System.out.println("Evidence v1.4 by DelfikPro\n");

		OptionParser parser = new OptionParser();
		OptionSpec<String> inputSpec = parser.accepts("screen", "Path to screenshot with F1 file").withRequiredArg().defaultsTo("example01.png");
		OptionSpec<String> outputSpec = parser.accepts("output", "Folder for output").withRequiredArg().defaultsTo("evidences");
		OptionSpec<String> resourcePacksSpec = parser.accepts("rp", "Enables resourcepack").withRequiredArg();
		OptionSet set = parser.parse(args);
		File screenshotFile = new File(set.valueOf(inputSpec));
		String path = set.valueOf(outputSpec);
		File outputDir = new File(path.length() == 0 ? "." : path);
		if (!outputDir.isDirectory()) {
			System.out.println("Folder " + outputDir.getAbsolutePath() + " doesn't exist");
			return;
		}
		List<String> resourcePacks = set.valuesOf(resourcePacksSpec);
		new ResourcePack(new File("resourcepacks/vanilla.zip"));
		System.out.println("Vanilla resourcepack was loaded successfully.");
		for (String resourcePack : resourcePacks) {
			new ResourcePack(new File("resourcepacks/" + resourcePack));
			System.out.println("Loaded resourcepack '" + resourcePack + "'");
		}


		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream("evidence.yml");
		Map<String, Object> yml = yaml.load(inputStream);

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

		execute("Loading file", () -> Generator.s = new Screenshot(screenshotFile, 1));

		execute("Drawing hand", () -> {
			String handPath = "pages/hand" + Generator.s.getWidth() + ".png";
			File hand = new File(handPath);
			if (!hand.exists()) {
				System.out.println("Screenshot's width is NOT SUPPORTED! Hand will be distorted!");
				handPath = "pages/hand1920.png";
			}
			Generator.s.overlapImage(handPath);
		});

		execute("Adding texteria", () -> {
			Generator.s.drawStringWithShadow("[§e" + p.getLevel() + "§f] " + p.getName(), 4, 4);
			Generator.s.drawStringWithShadow(servername, 4, 24);
			String multiplier = Vime.getPlayerMultiplier(p);
			Generator.s.drawStringWithShadow((multiplier.equals("1") ? "" : ("§e[§dx" + multiplier + "§e] ")) + "§fКоличество коинов: §e" + coins, 4, Generator.s.getHeight() - 50);
		});

		execute("Drawing crosshair", () ->
			Generator.s.drawImageInversionMask("assets/minecraft/textures/gui/icons.png", Generator.s.getWidth() / 2 - 14, Generator.s.getHeight() / 2 - 14, 15, 15)
		);





		int ix = Generator.s.getWidth() / 2 - 182;
		execute("Drawing inventory", () -> {
			BufferedImage i = ImageIO.read(ResourcePack.get("assets/minecraft/textures/gui/widgets.png"));
			Generator.s.getGraphics().drawImage(i, ix, Generator.s.getHeight() - 44,
					Generator.s.getWidth() / 2 + 182, Generator.s.getHeight(),
					0, 0,
					182, 22,
					null);
			int slot = Generator.s.getWidth() / 2 - 184 + 40 * selectedSlot;
			Generator.s.getGraphics().drawImage(i, slot, Generator.s.getHeight() - 46, slot + 48, Generator.s.getHeight() - 2, 0, 22, 24, 44, null);
		});

		int health = 20;
		int hunger = 20;
		execute("Drawing bars", () -> {
			BufferedImage icons = ImageIO.read(new File("pages/icons.png"));
			drawBar(icons, ix, Generator.s.getHeight() - 78, 32, 104, 122, 0, health, false);
			drawBar(icons, ix + 182 * 2 - 18, Generator.s.getHeight() - 78, 32, 104, 122, 54, hunger, true);

			Generator.s.pasteImage(icons, 0, 128, 364, 10, ix, Generator.s.getHeight() - 58);
		});

		execute("Drawing items", () -> {
			String[] items;

			switch (preset) {
				case "LOBBY":
					items = new String[] {"compass", "nether_star", null, null, "brewing_stand", null, null, null, "comparator"};
					break;
				case "BP":
					items = new String[] {"slimeball", "dye_powder_yellow", null, null, null, null, null, "nether_star", "compass"};
					break;
				case "KP":
					items = new String[] {"emerald", "dye_powder_yellow", null, null, null, null, null, "nether_star", "compass"};
					break;
				case "GG":
					items = new String[] {"slimeball", "ender_eye", "dye_powder_yellow", null, null, null, null, "nether_star", "compass"};
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
				ItemRender.draw(Generator.s, item, slot, ix);
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
				int y = Generator.s.getHeight() - 56 - (i + 1) * 18;
				Generator.s.drawBlackRect(4, y, 648, 18);
				Generator.s.drawStringWithShadow(chat.get(i).getText(), 4, y + 2);
			}
			if (chatOpened != 0) {
				Generator.s.drawBlackRect(4, Generator.s.getHeight() - 27, Generator.s.getWidth() - 8, 24);
				if (chatOpened == 2) {
					Generator.s.drawStringWithShadow("_", 8, Generator.s.getHeight() - 23);
				}
			}
		});



		execute("Saving result", () -> Generator.s.save(getFileName(outputDir)));

		long end = System.currentTimeMillis();

		System.out.println("Done in " + (end - start) + " ms.");

	}

	private static String getFileName(File outputDir) {

		File[] files = outputDir.listFiles();
		int number;
		if (files == null) {
			outputDir.mkdir();
			number = 0;
		} else number = files.length;
		String s = String.valueOf(number);
		s = new String(new char[9 - s.length()]).replace('\0', '0') + s;
		String name = s.substring(0, 3) + "_" + s.substring(3,6) + "_" + s.substring(6, 9);
		return outputDir.getPath() + "/" + name + ".png";

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
		String tab = new String(new char[30 - s.length()]).replace('\0', ' ');
		System.out.print(s + "... " + tab);
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

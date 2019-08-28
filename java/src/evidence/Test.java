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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Test {


	public static int compassPos;
	static Image s;

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
			if (outputDir.exists()) {
				System.out.println("WTF");
				return;
			}
			outputDir.mkdirs();
			outputDir.mkdir();
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

		execute("Loading file", () -> {
			s = new Image(screenshotFile, 2);
			Font.bind(s);
		});

		execute("Drawing hand", () -> {
			String handPath = "pages/hand" + s.getWidth() * s.scale + ".png";
			File hand = new File(handPath);
			if (!hand.exists()) {
				System.out.println("Screenshot's width is NOT SUPPORTED! Hand will be distorted!");
				handPath = "pages/hand1920.png";
			}
			BufferedImage handImg = ImageIO.read(new File(handPath));
			s.draw(handImg.getRaster(), 0, 0, 1, 1, 0, 0, s.getWidth(), s.getHeight());
		});

		execute("Adding texteria", () -> {
			Font.drawStringWithShadow("[§e" + p.getLevel() + "§f] " + p.getName(), 2, 2);
			Font.drawStringWithShadow(servername, 2, 12);
			String multiplier = Vime.getPlayerMultiplier(p);
			Font.drawStringWithShadow((multiplier.equals("1") ? "" : ("§e[§dx" + multiplier + "§e] ")) + "§fКоличество коинов: §e" + coins, 2, s.getHeight() - 25);
		});

//		execute("Drawing crosshair", () ->
//				s.drawImageInversionMask("assets/minecraft/textures/gui/icons.png", s.getWidth() / 2 - 14, s.getHeight() / 2 - 14, 15, 15)
//			   );
// ToDo: CrossHair


		ItemRender.draw(s, "nether_star", 0, 100, 4);


		int ix = s.getWidth() / 2 - 91;
		execute("Drawing inventory", () -> {
			BufferedImage i = ImageIO.read(ResourcePack.get("assets/minecraft/textures/gui/widgets.png"));
			s.drawMCFormat(i.getRaster(), 0, 0, 182, 22, ix, s.getHeight() - 22, s.getWidth() / 2 + 91, s.getHeight());

			int slot = s.getWidth() / 2 - 92 + 20 * selectedSlot;
			s.drawMCFormat(i.getRaster(), 0, 22, 24, 44, slot, s.getHeight() - 23, slot + 24, s.getHeight() - 1);
		});

		int health = 20;
		int hunger = 20;
		execute("Drawing bars", () -> {
			BufferedImage icons = ImageIO.read(ResourcePack.get("assets/minecraft/textures/gui/icons.png"));
			drawBar(icons, ix, s.getHeight() - 39, 16, 52, 61, 0, health, false);
			drawBar(icons, ix + 182 - 9, s.getHeight() - 39, 16, 52, 61, 27, hunger, true);

			s.drawMCFormat(icons.getRaster(), 0, 64, 182, 69, ix, s.getHeight() - 29, ix + 182, s.getHeight() - 24);
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
		if (false) execute("Drawing chat", () -> {
			int lines = chat.size();
			for (int i = 0; i < lines; i++) {
				int y = s.getHeight() - 28 - (i + 1) * 9;
				s.rect(2, y, 326, y + 9, 0x80000000);
				Font.drawStringWithShadow(chat.get(i).getText(), 2, y + 1);
			}
			if (chatOpened != 0) {
				s.rect(2, s.getHeight() - 14, s.getWidth() - 2, s.getHeight() - 2, 0x80000000);
				if (chatOpened == 2) Font.drawStringWithShadow("_", 4, s.getHeight() - 12);
			}
		});


		execute("Saving result", () -> s.save(new File(getFileName(outputDir))));

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
			s.drawMCFormat(image.getRaster(), txEmpty, ty, txEmpty + 9, ty + 9, x + i * 8, y, x + i * 8 + 9, y + 9);
		}
		int h = value % 2;
		value /= 2;
		for (int c = 0; c < value + h; c++) {
			int i = reverse ? -c : c;
			int leftX = i == value ? txHalf : txFull;
			s.drawMCFormat(image.getRaster(), leftX, ty, leftX + 9, ty + 9, x + i * 8, y, x + i * 8 + 9, y + 9);
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


	public static void notmain() throws IOException {

		new ResourcePack(new File("resourcepacks/vanilla.zip"));
		Image i = new Image(new File("example01.png"), 2);

		BufferedImage t = ImageIO.read(new File("glass_lime.png"));
		System.out.println(t.getType());
		System.out.println(t.getRaster().getNumBands());
		Font.bind(i);
		i.draw(t.getRaster(), 0, 0, 1, 0.5, 40, 40, 500, 500);
		Font.drawStringWithShadow("§6[G] §7<§f-CHP-§7> §d[H2W] Fedos534 §7[§8Ниггер §6LVL 9§7]: §fДелфик", 10, 10);
		i.flush();
		File file = new File(System.currentTimeMillis() + ".png");
		i.save(file);
		System.out.println("saved to " + file.getName());

	}

}

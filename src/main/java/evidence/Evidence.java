package evidence;

import implario.Environment;
import implario.LoggerUtils;
import evidence.hand.TextureMap;
import evidence.messages.Message;
import evidence.messages.Parser;
import evidence.render.Font;
import evidence.render.ScaledImage;
import evidence.render.Util;
import evidence.resources.ResourcePack;
import evidence.resources.ResourceStack;
import evidence.vimeworld.Vime;
import evidence.vk.EvidenceBot;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;
import vimeworld.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class Evidence {

	public static final Yaml yaml = new Yaml();

	private final EvidenceManager manager;
	private final ResourceStack resourceStack;
	private Font font;
	public int compassPos;
	ScaledImage image;

	public Evidence(EvidenceManager manager) {
		this.manager = manager;
		this.resourceStack = manager.createResourceStack();
	}

	public static void main(String[] args) throws Exception {

		System.out.println("Evidence v3.0 by vk.com/DelfikPro\n");
		File screenshotFile = new File(args.length == 0 ? "example.png" : args[0]);
		boolean transparent = args.length > 0 && args[args.length - 1].equals("--nobackground");
		EvidenceManager manager = new EvidenceManager();
		manager.init(LoggerUtils.simpleLogger("Evidence"));

		Evidence evidence = new Evidence(manager);
		byte[] generate = evidence.generate(new FileInputStream("evidence.yml"), new FileInputStream(screenshotFile), transparent, false);
		Files.write(new File(getFileName(new File("evidences"))).toPath(), generate);
	}

	@SuppressWarnings ("unchecked")
	public byte[] generate(InputStream configStream, InputStream imageStream, boolean transparent, boolean placeWatermark) throws Exception {

		long start = System.currentTimeMillis();

		Map<String, Object> yml = yaml.load(configStream);
		configStream.close();

		System.out.println("Vanilla resourcepack was loaded successfully.");
		for (String packAddress : (List<String>) yml.get("resourcepacks")) {
			ResourcePack pack = manager.getResourcePacks().get(packAddress);
			if (pack == null) throw new EvidenceException("Resourcepack '" + packAddress + "' not found.");
			resourceStack.push(pack);
			System.out.println("Loaded resourcepack '" + packAddress + "'");
		}

		String playername = (String) yml.get("name");
		String servername = (String) yml.get("server");
		int coins = (int) yml.get("coins");
		int selectedSlot = (int) yml.get("slot");
		compassPos = (int) yml.get("compass-target");
		List<String> chatIn = (List<String>) yml.get("chat");
		String preset = (String) yml.get("preset");
		int chatOpened = (int) yml.get("chatOpened");

		Map<String, Boolean> modules = (Map<String, Boolean>) yml.get("modules");

		this.font = new Font(this);

		List<Message> chat = new ArrayList<>();
		for (String c : chatIn) chat.add(Parser.routine(c));
		Collections.reverse(chat);

		Player p = Vime.getPlayer(playername);

		BufferedImage baseImage = Util.convertColorspace(ImageIO.read(imageStream), BufferedImage.TYPE_4BYTE_ABGR);
		imageStream.close();

		if (modules.get("hand")) try (InputStream skinStream = EvidenceBot.download("https://skin.vimeworld.ru/raw/skin/" + playername + ".png")) {
			BufferedImage skin = Util.convertColorspace(ImageIO.read(skinStream), BufferedImage.TYPE_4BYTE_ABGR);
			for (int i = 0; i < 3; i++) {
				if (skin.getHeight() < 64 && i != 1) continue;
				File srcMap = new File("hand/layer" + i + "-texture.png");
				File dstMap = new File("hand/layer" + i + "-" + baseImage.getWidth() + "x" + baseImage.getHeight() + ".png");
				if (!dstMap.exists() || !dstMap.isFile())
					throw new EvidenceException(baseImage.getWidth() + "x" + baseImage.getHeight() + " is not supported.");
				BufferedImage srcImage = Util.convertColorspace(ImageIO.read(srcMap), BufferedImage.TYPE_4BYTE_ABGR);
				BufferedImage dstImage = Util.convertColorspace(ImageIO.read(dstMap), BufferedImage.TYPE_4BYTE_ABGR);
				TextureMap.doMap(skin, srcImage, baseImage, dstImage);
			}
		}

		this.image = new ScaledImage(transparent ? new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR) : baseImage, 2);

		//		if (modules.get("hand")) execute("Drawing hand", () -> {
		//			String handPath = "pages/hand" + image.getWidth() * image.scale + ".png";
		//			File hand = new File(handPath);
		//			if (!hand.exists()) {
		//				msg = "Screenshot's width is NOT SUPPORTED! Hand will be distorted!";
		//				handPath = "pages/hand1920.png";
		//			}
		//			BufferedImage handImg = ImageIO.read(new File(handPath));
		//			image.draw(handImg, 0, 0, 1, 1, 0, 0, image.getWidth(), image.getHeight());
		//		});

		if (modules.get("crosshair")) execute("Drawing crosshair", () -> {
			image.filter((x, y, color, channel, dstColor) -> channel == 3 ? color : 1 - dstColor);
			BufferedImage img = resourceStack.getTexture("assets/minecraft/textures/gui/icons.png");
			this.image.drawMCFormat(img, 0, 0, 15, 15, this.image.getWidth() / 2 - 7, this.image.getHeight() / 2 - 7, this.image.getWidth() / 2 + 8, this.image.getHeight() / 2 + 8);
			this.image.filter(null);
		});

		int ix = image.getWidth() / 2 - 91;
		if (modules.get("hotbar")) execute("Drawing inventory", () -> {
			BufferedImage i = resourceStack.getTexture("assets/minecraft/textures/gui/widgets.png");
			image.drawMCFormat(i, 0, 0, 182, 22, ix, image.getHeight() - 22, image.getWidth() / 2 + 91, image.getHeight());

			int slot = image.getWidth() / 2 - 92 + 20 * selectedSlot;
			image.drawMCFormat(i, 0, 22, 24, 44, slot, image.getHeight() - 23, slot + 24, image.getHeight() - 1);
		});

		int health = 20;
		int hunger = 20;
		if (modules.get("indicators")) execute("Drawing bars", () -> {
			BufferedImage icons = resourceStack.getTexture("assets/minecraft/textures/gui/icons.png");
			drawBar(icons, ix, image.getHeight() - 39, 16, 52, 61, 0, health, false);
			drawBar(icons, ix + 182 - 9, image.getHeight() - 39, 16, 52, 61, 27, hunger, true);

			image.drawMCFormat(icons, 0, 64, 182, 69, ix, image.getHeight() - 29, ix + 182, image.getHeight() - 24);
		});

		if (modules.get("items")) execute("Drawing items", () -> {
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
				ItemRender.draw(this, image, item, slot, ix);
			}

		});

		if (modules.get("chat")) execute("Drawing chat", () -> {
			int lines = chat.size();
			for (int i = 0; i < lines; i++) {
				int y = image.getHeight() - 28 - (i + 1) * 9;
				if (modules.get("chat_background")) image.rect(2, y, 326, y + 9, 0x80000000);
				font.drawStringWithShadow(chat.get(i).getText(), 2, y + 1);
			}
			if (chatOpened != 0) {
				image.rect(2, image.getHeight() - 14, image.getWidth() - 2, image.getHeight() - 2, 0x80000000);
				if (chatOpened == 2) font.drawStringWithShadow("_", 4, image.getHeight() - 12);

				// Полоса прокрутки (скопировано из майнкрафта)
				image.rect(0, image.getHeight() - 145, 1, image.getHeight() - 28, 0x603333aa);
				image.rect(0, image.getHeight() - 145, 1, image.getHeight() - 28, 0x60cccccc);
			}
		});

		if (modules.get("texteria")) execute("Adding texteria", () -> {
			font.drawStringWithShadow("[§e" + p.getLevel() + "§f] " + p.getName(), 2, 2);
			font.drawStringWithShadow(servername, 2, 12);
			String multiplier = Vime.getPlayerMultiplier(p);
			font.drawStringWithShadow((multiplier.equals("1") ? "" : ("§e[§dx" + multiplier + "§e] ")) + "§fКоличество коинов: §e" + coins, 2, image.getHeight() - 25);
		});


		if (placeWatermark) {
			String watermark = Environment.get("WATERMARK", "EVIDENCE BY DELFIKPRO ");

			image.scale = 4;
			image.filter((x, y, color, channel, dstColor) -> Math.min(channel == 3 ? color : dstColor + color * 0.2f, 1));
			for (int i = 0; i < image.getHeight() / 9 + 1; i++) {
				float x = 1;
				while (true) {
					if ((x = font.drawString(watermark, x, i * 9)) > image.getWidth()) break;
				}
			}

		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		execute("Saving result", () -> image.save(stream, transparent ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR));

		long end = System.currentTimeMillis();

		System.out.println("Done in " + (end - start) + " ms.");

		return stream.toByteArray();
	}

	private static String getFileName(File outputDir) {
		if (!outputDir.exists() || !outputDir.isDirectory()) outputDir.mkdir();
		File[] files = outputDir.listFiles();
		int number;
		if (files == null) {
			outputDir.mkdir();
			number = 0;
		} else number = files.length;
		String s = String.valueOf(number);
		s = new String(new char[9 - s.length()]).replace('\0', '0') + s;
		String name = s.substring(0, 3) + "_" + s.substring(3, 6) + "_" + s.substring(6, 9);
		return outputDir.getPath() + "/" + name + ".png";

	}

	void drawBar(BufferedImage image, int x, int y, int txEmpty, int txFull, int txHalf, int ty, int value, boolean reverse) {
		for (int c = 0; c < 10; c++) {
			int i = reverse ? -c : c;
			this.image.drawMCFormat(image, txEmpty, ty, txEmpty + 9, ty + 9, x + i * 8, y, x + i * 8 + 9, y + 9);
		}
		int h = value % 2;
		value /= 2;
		for (int c = 0; c < value + h; c++) {
			int i = reverse ? -c : c;
			int leftX = i == value ? txHalf : txFull;
			this.image.drawMCFormat(image, leftX, ty, leftX + 9, ty + 9, x + i * 8, y, x + i * 8 + 9, y + 9);
		}
	}

	static String msg;

	static void execute(String s, DangerousRunnable r) throws IOException {
		String tab = new String(new char[30 - s.length()]).replace('\0', ' ');
		System.out.print(s + "... " + tab);
		long start = System.currentTimeMillis();
		r.run();
		long end = System.currentTimeMillis();
		if (msg != null) {
			System.out.print(msg + ", ");
			msg = null;
		}
		System.out.print("Done in " + (end - start) + " ms.\n");

	}

	@FunctionalInterface
	private interface DangerousRunnable {

		void run() throws IOException;

	}

}

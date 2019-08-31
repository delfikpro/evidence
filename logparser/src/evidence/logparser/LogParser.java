package evidence.logparser;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class LogParser {


	public static void lol(String name, BufferedReader reader, int offset) throws IOException {


		String s;
		while ((s = reader.readLine()) != null) {
			if (s.endsWith("вошел в лобби!")) new Prefix(name, s);
		}
		reader.close();

	}

	public static void plain(File f) throws IOException {
		if (f.exists() && f.isFile()) lol(f.getName(), new BufferedReader(new InputStreamReader(new FileInputStream(f), "Cp1251")), 45);
		else System.out.println("1.6.4 not found");
	}

	public static void gzip(File f) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), "Cp1251"));
		lol(f.getName(), reader, 42);

	}


	public static void read(File f) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

		String s;
		while ((s = reader.readLine()) != null) {
			Prefix.deserialize(s);
		}
		reader.close();
	}

	public static void main(String[] args) throws IOException {
//		File dir = new File(args[0]).getParentFile();
//		if (!dir.exists() || !dir.isDirectory()) {
//			System.out.println("not found " + dir.getAbsolutePath());
//			return;
//		}

		if (args.length != 0) {
			for (String arg : args) {
				File f = new File(arg);
				read(f);
			}
			save("united.txt");
			return;
		}

		File dir = new File("logs");
		plain(new File("output-client.log"));
		for (File file : dir.listFiles()) if (file.getName().endsWith(".gz")) gzip(file);
		save("prefixes.txt");

	}
	public static void save(String filename) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		for (Map.Entry<String, List<Prefix>> e : Prefix.map.entrySet()) {
			w.write(e.getKey() + ": ");
			boolean notfirst = false;
			List<Prefix> list = e.getValue();
			Collections.sort(list);
			for (Prefix prefix : list) {
				if (notfirst) w.write(", ");
				notfirst = true;
				w.write(prefix.serialize());
			}
			w.write("\n");
		}
		w.flush();
		w.close();
	}

	public static class Prefix implements Comparable<Prefix> {
		private final String prefix, player;
		private final Date time;
		public static final Map<String, List<Prefix>> map = new HashMap<>();

		private static final Pattern COLOR_PATTERN = Pattern.compile("§.");

		public static String removeColors(String s) {
			if (!s.contains("§")) return s;
			StringBuilder builder = new StringBuilder();
			int x = 0;
			Matcher m = COLOR_PATTERN.matcher(s);
			while (m.find()) {
				builder.append(s, x, m.start());
				x = m.end();
			}
			return builder.toString();
		}

		public static List<Prefix> deserialize(String s) {
			String[] a = s.split(": ");
			String name = a[0].toLowerCase();
			String[] b = a[1].split(", ");
			List<Prefix> list = map.computeIfAbsent(name, n -> new ArrayList<>());
			for (String entry : b) {
				String[] c = entry.split("#");
				boolean redundant = false;
				Date time = new Date(Long.parseLong(c[0]) * 1000);
				for (Prefix p : list) {
					if (p.time.getTime() > time.getTime()) break;
					redundant = p.prefix.equals(c[1]);
				}
				if (!redundant) list.add(new Prefix(c[1], name, time));
				Collections.sort(list);
			}
			return list;
		}

		public Prefix(String prefix, String player, Date time) {
			this.prefix = prefix;
			this.player = player;
			this.time = time;
		}

		public Prefix(String filename, String logLine) {
			boolean old = !logLine.startsWith("[");
			time = old ? parse164(logLine) : parse188(filename, logLine);
			String sub = removeColors(logLine).substring(old ? 45 : 42);
			String[] ss = sub.replace("[", "").replace("]", "").split(" ");
			prefix = ss[0];
			player = ss[1].toLowerCase();
			List<Prefix> list = map.get(player);
			if (list == null) {
				List<Prefix> list1 = new ArrayList<>();
				list1.add(this);
				map.put(player, list1);
			} else {
				Collections.sort(list);
				boolean redundant = false;
				for (Prefix prefix1 : list) redundant = prefix1.prefix.equals(prefix);
				if (!redundant) list.add(this);
			}
		}


		private static Date parse164(String logLine) {
			String[] ss = logLine.substring(0, 19).split(" ");
			return parse(ss[0], ss[1]);
		}

		private static Date parse188(String filename, String logLine) {
			return parse(filename, logLine.substring(1, 8));
		}

		private static Date parse(String date, String time) {
			String[] dateElements = date.split("-");
			int year = Integer.parseInt(dateElements[0]);
			int month = Integer.parseInt(dateElements[1]);
			int day = Integer.parseInt(dateElements[2]);
			String[] timeElements = time.split(":");
			int hours = Integer.parseInt(timeElements[0]);
			int minutes = Integer.parseInt(timeElements[1]);
			int seconds = Integer.parseInt(timeElements[2]);
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month - 1);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.HOUR_OF_DAY, hours);
			c.set(Calendar.MINUTE, minutes);
			c.set(Calendar.SECOND, seconds);
			return c.getTime();
		}

		public String serialize() {
			return time.getTime() / 1000 + "#" + prefix;
		}

		@Override
		public int compareTo(Prefix prefix) {
			return time.compareTo(prefix.time);
		}

	}

}

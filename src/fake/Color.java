package fake;

public enum Color {

	BLACK ('0', 0x00, 0x00, 0x00),
	INDIGO('1', 0x00, 0x00, 0xaa),
	LEAF('2', 0x00, 0xaa, 0x00),
	CYAN  ('3', 0x00, 0xaa, 0xaa),
	BLOOD ('4', 0xaa, 0x00, 0x00),
	VIOLET('5', 0xaa, 0x00, 0xaa),
	GOLD('6', 0xff, 0xaa, 0x00),
	GRAY  ('7', 0xaa, 0xaa, 0xaa),
	DARK  ('8', 0x55, 0x55, 0x55),
	BLUE  ('9', 0x55, 0x55, 0xff),
	GREEN ('a', 0x55, 0xff, 0x55),
	AQUA('b', 0x55, 0xff, 0xff),
	RED   ('c', 0xff, 0x55, 0x55),
	PINK  ('d', 0xff, 0x55, 0xff),
	YELLOW('e', 0xff, 0xff, 0x55),
	WHITE ('f', 0xff, 0xff, 0xff);

//	static final Color[] array = new Color[16];
//	static {
//		for (Color color : values()) array["0123456789abcdef".indexOf(color.c)] = color;
//		for (int i = 0; i < array.length; i++) if (array[i] == null) array[i] = WHITE;
//	}

	final int r, g, b;
	final char c;

	Color(char c, int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.c = c;
	}

	@Override
	public String toString() {
		return "§" + c;
	}
}

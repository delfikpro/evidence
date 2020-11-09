package vimeworld;

import evidence.render.Color;

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
	ADMIN("Главный админ", "§3§l[Гл. админ]", 4, Color.CYAN, true);

	private final String title, prefix;
	private final int multiplier;
	private final Color color;
	private final boolean bold;

	Rank(String title, String prefix, int multiplier, Color color) {
		this(title, prefix, multiplier, color, false);
	}

	Rank(String title, String prefix, int multiplier, Color color, boolean bold) {
		this.title = title;
		this.multiplier = multiplier;
		this.prefix = prefix;
		this.color = color;
		this.bold = bold;
	}

	public boolean isBold() {
		return bold;
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

	public String getStyle() {
		return color.toString() + (isBold() ? "§l" : "");
	}
}

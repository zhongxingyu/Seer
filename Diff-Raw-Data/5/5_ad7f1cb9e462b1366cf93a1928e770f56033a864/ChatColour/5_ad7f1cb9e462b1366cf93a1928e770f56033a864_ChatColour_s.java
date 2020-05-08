 package no.runsafe.framework.output;
 
 import org.bukkit.ChatColor;
 
 import java.util.regex.Pattern;
 
 public enum ChatColour
 {
 	BLACK('0', ChatColor.BLACK, ConsoleColors.BLACK),
 	DARK_BLUE('1', ChatColor.DARK_BLUE, ConsoleColors.DARK_BLUE),
 	DARK_GREEN('2', ChatColor.DARK_GREEN, ConsoleColors.DARK_GREEN),
 	DARK_AQUA('3', ChatColor.DARK_AQUA, ConsoleColors.DARK_AQUA),
 	DARK_RED('4', ChatColor.DARK_RED, ConsoleColors.DARK_RED),
 	DARK_PURPLE('5', ChatColor.DARK_PURPLE, ConsoleColors.DARK_PURPLE),
 	GOLD('6', ChatColor.GOLD, ConsoleColors.GOLD),
 	GRAY('7', ChatColor.GRAY, ConsoleColors.GRAY),
 	DARK_GRAY('8', ChatColor.DARK_GRAY, ConsoleColors.DARK_GRAY),
 	BLUE('9', ChatColor.BLUE, ConsoleColors.BLUE),
 	GREEN('a', ChatColor.GREEN, ConsoleColors.GREEN),
 	AQUA('b', ChatColor.AQUA, ConsoleColors.AQUA),
 	RED('c', ChatColor.RED, ConsoleColors.RED),
 	LIGHT_PURPLE('d', ChatColor.LIGHT_PURPLE, ConsoleColors.LIGHT_PURPLE),
 	YELLOW('e', ChatColor.YELLOW, ConsoleColors.YELLOW),
 	WHITE('f', ChatColor.WHITE, ConsoleColors.WHITE),
 	MAGIC('k', ChatColor.MAGIC, ConsoleColors.MAGIC),
 	BOLD('l', ChatColor.BOLD, ConsoleColors.BOLD),
 	STRIKETHROUGH('m', ChatColor.STRIKETHROUGH, ConsoleColors.STRIKETHROUGH),
 	UNDERLINE('n', ChatColor.UNDERLINE, ConsoleColors.UNDERLINE),
 	ITALIC('o', ChatColor.ITALIC, ConsoleColors.ITALIC),
 	RESET('r', ChatColor.RESET, ConsoleColors.RESET);
 
 	ChatColour(char colourCode, ChatColor bukkitColour, String ansiCode)
 	{
 		code = String.format("&%s", colourCode);
 		colour = bukkitColour;
 		bukkitCode = colour.toString();
 		consoleCode = ansiCode;
 	}
 
 	@Override
 	public String toString()
 	{
 		return code;
 	}
 
 	public String toConsole()
 	{
 		return consoleCode;
 	}
 
 	public String toBukkit()
 	{
 		return bukkitCode;
 	}
 
 	public static String ToMinecraft(String message)
 	{
 		message = BUKKIT_DARK_GREEN.matcher(message).replaceAll(DARK_GREEN.toBukkit());
 		message = BUKKIT_DARK_BLUE.matcher(message).replaceAll(DARK_BLUE.toBukkit());
 		message = BUKKIT_BLACK.matcher(message).replaceAll(BLACK.toBukkit());
 		message = BUKKIT_DARK_AQUA.matcher(message).replaceAll(DARK_AQUA.toBukkit());
 		message = BUKKIT_DARK_RED.matcher(message).replaceAll(DARK_RED.toBukkit());
 		message = BUKKIT_DARK_PURPLE.matcher(message).replaceAll(DARK_PURPLE.toBukkit());
 		message = BUKKIT_GOLD.matcher(message).replaceAll(GOLD.toBukkit());
 		message = BUKKIT_GRAY.matcher(message).replaceAll(GRAY.toBukkit());
 		message = BUKKIT_DARK_GRAY.matcher(message).replaceAll(DARK_GRAY.toBukkit());
 		message = BUKKIT_BLUE.matcher(message).replaceAll(BLUE.toBukkit());
 		message = BUKKIT_GREEN.matcher(message).replaceAll(GREEN.toBukkit());
 		message = BUKKIT_AQUA.matcher(message).replaceAll(AQUA.toBukkit());
 		message = BUKKIT_RED.matcher(message).replaceAll(RED.toBukkit());
 		message = BUKKIT_LIGHT_PURPLE.matcher(message).replaceAll(LIGHT_PURPLE.toBukkit());
 		message = BUKKIT_YELLOW.matcher(message).replaceAll(YELLOW.toBukkit());
 		message = BUKKIT_WHITE.matcher(message).replaceAll(WHITE.toBukkit());
 		message = BUKKIT_MAGIC.matcher(message).replaceAll(MAGIC.toBukkit());
 		message = BUKKIT_BOLD.matcher(message).replaceAll(BOLD.toBukkit());
 		message = BUKKIT_STRIKETHROUGH.matcher(message).replaceAll(STRIKETHROUGH.toBukkit());
 		message = BUKKIT_UNDERLINE.matcher(message).replaceAll(UNDERLINE.toBukkit());
 		message = BUKKIT_ITALIC.matcher(message).replaceAll(ITALIC.toBukkit());
 		message = BUKKIT_RESET.matcher(message).replaceAll(RESET.toBukkit());
		return message + RESET;
 	}
 
 	public static String ToConsole(String message)
 	{
 		message = BUKKIT_DARK_GREEN.matcher(message).replaceAll(DARK_GREEN.toConsole());
 		message = BUKKIT_DARK_BLUE.matcher(message).replaceAll(DARK_BLUE.toConsole());
 		message = BUKKIT_BLACK.matcher(message).replaceAll(BLACK.toConsole());
 		message = BUKKIT_DARK_AQUA.matcher(message).replaceAll(DARK_AQUA.toConsole());
 		message = BUKKIT_DARK_RED.matcher(message).replaceAll(DARK_RED.toConsole());
 		message = BUKKIT_DARK_PURPLE.matcher(message).replaceAll(DARK_PURPLE.toConsole());
 		message = BUKKIT_GOLD.matcher(message).replaceAll(GOLD.toConsole());
 		message = BUKKIT_GRAY.matcher(message).replaceAll(GRAY.toConsole());
 		message = BUKKIT_DARK_GRAY.matcher(message).replaceAll(DARK_GRAY.toConsole());
 		message = BUKKIT_BLUE.matcher(message).replaceAll(BLUE.toConsole());
 		message = BUKKIT_GREEN.matcher(message).replaceAll(GREEN.toConsole());
 		message = BUKKIT_AQUA.matcher(message).replaceAll(AQUA.toConsole());
 		message = BUKKIT_RED.matcher(message).replaceAll(RED.toConsole());
 		message = BUKKIT_LIGHT_PURPLE.matcher(message).replaceAll(LIGHT_PURPLE.toConsole());
 		message = BUKKIT_YELLOW.matcher(message).replaceAll(YELLOW.toConsole());
 		message = BUKKIT_WHITE.matcher(message).replaceAll(WHITE.toConsole());
 		message = BUKKIT_MAGIC.matcher(message).replaceAll(MAGIC.toConsole());
 		message = BUKKIT_BOLD.matcher(message).replaceAll(BOLD.toConsole());
 		message = BUKKIT_STRIKETHROUGH.matcher(message).replaceAll(STRIKETHROUGH.toConsole());
 		message = BUKKIT_UNDERLINE.matcher(message).replaceAll(UNDERLINE.toConsole());
 		message = BUKKIT_ITALIC.matcher(message).replaceAll(ITALIC.toConsole());
 		message = BUKKIT_RESET.matcher(message).replaceAll(RESET.toConsole());
		return message + RESET;
 	}
 
 	private final String code;
 	private final ChatColor colour;
 	private final String bukkitCode;
 	private final String consoleCode;
 
 	private static final Pattern BUKKIT_BLACK = Pattern.compile(ChatColour.BLACK.toString());
 	private static final Pattern BUKKIT_DARK_GREEN = Pattern.compile(ChatColour.DARK_GREEN.toString());
 	private static final Pattern BUKKIT_DARK_BLUE = Pattern.compile(ChatColour.DARK_BLUE.toString());
 	private static final Pattern BUKKIT_DARK_AQUA = Pattern.compile(ChatColour.DARK_AQUA.toString());
 	private static final Pattern BUKKIT_DARK_RED = Pattern.compile(ChatColour.DARK_RED.toString());
 	private static final Pattern BUKKIT_DARK_PURPLE = Pattern.compile(ChatColour.DARK_PURPLE.toString());
 	private static final Pattern BUKKIT_GOLD = Pattern.compile(ChatColour.GOLD.toString());
 	private static final Pattern BUKKIT_GRAY = Pattern.compile(ChatColour.GRAY.toString());
 	private static final Pattern BUKKIT_DARK_GRAY = Pattern.compile(ChatColour.DARK_GRAY.toString());
 	private static final Pattern BUKKIT_BLUE = Pattern.compile(ChatColour.BLUE.toString());
 	private static final Pattern BUKKIT_GREEN = Pattern.compile(ChatColour.GREEN.toString());
 	private static final Pattern BUKKIT_AQUA = Pattern.compile(ChatColour.AQUA.toString());
 	private static final Pattern BUKKIT_RED = Pattern.compile(ChatColour.RED.toString());
 	private static final Pattern BUKKIT_LIGHT_PURPLE = Pattern.compile(ChatColour.LIGHT_PURPLE.toString());
 	private static final Pattern BUKKIT_YELLOW = Pattern.compile(ChatColour.YELLOW.toString());
 	private static final Pattern BUKKIT_WHITE = Pattern.compile(ChatColour.WHITE.toString());
 	private static final Pattern BUKKIT_MAGIC = Pattern.compile(ChatColour.MAGIC.toString());
 	private static final Pattern BUKKIT_BOLD = Pattern.compile(ChatColour.BOLD.toString());
 	private static final Pattern BUKKIT_STRIKETHROUGH = Pattern.compile(ChatColour.STRIKETHROUGH.toString());
 	private static final Pattern BUKKIT_UNDERLINE = Pattern.compile(ChatColour.UNDERLINE.toString());
 	private static final Pattern BUKKIT_ITALIC = Pattern.compile(ChatColour.ITALIC.toString());
 	private static final Pattern BUKKIT_RESET = Pattern.compile(ChatColour.RESET.toString());
 }

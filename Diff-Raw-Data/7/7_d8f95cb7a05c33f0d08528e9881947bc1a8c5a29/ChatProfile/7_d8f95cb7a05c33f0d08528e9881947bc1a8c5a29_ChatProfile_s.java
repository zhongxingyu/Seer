 package net.milkycraft;
 
 import org.bukkit.ChatColor;
 
 public class ChatProfile {
 
 	private String g;
 	private String prefix;
 	private String suffix;
 	private String muffix;
 	private String format;
 
 	public ChatProfile(String g, String prefix2, String suffix2,
 			String muffix2, String format2) {
 		this.g = g;
 		this.prefix = prefix2;
 		prefix = prefix.replace("&", "");
 		this.suffix = suffix2;
 		suffix = suffix.replace("&", "");
 		this.muffix = muffix2;
 		muffix = muffix.replace("&", "");
 		this.format = format2;
 		format();
 	}
 
 	public final ChatColor getPrefix() {
 		return ChatColor.getByChar(this.prefix);
 	}
 
 	public final ChatColor getSuffix() {
 		return ChatColor.getByChar(this.suffix);
 	}
 
 	public final ChatColor getMuffix() {
 		return ChatColor.getByChar(this.muffix);
 	}
 
 	public final String getGroup() {
 		return g;
 	}
 
 	public final String getFormat() {
 		return format;
 	}
 
 	private void format() {
 		format = format
				.replace(format.substring(0, 1) + "g",
 						getPrefix() + format.substring(0, 1) + getGroup())
				.replace("p", getSuffix() + "%s")
				.replace("m", getMuffix() + "%s")
 				.replace("null", ChatColor.WHITE.toString());
 	}
 }

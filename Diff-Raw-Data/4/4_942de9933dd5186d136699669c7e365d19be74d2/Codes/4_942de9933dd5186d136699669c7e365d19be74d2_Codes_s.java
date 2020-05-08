 package fr.ribesg.alix.api.enums;
 import java.text.DecimalFormat;
 
 /**
  * This Enum provides convenient access to all IRC special Strings,
  * like Color codes, formatting codes, and others.
  * <p/>
  * Color codes and formatting codes are not defined by any real convention,
  * and may not work on all clients.
  * This API is based on the mIRC character codes for colors and formatting.
  *
  * @author Ribesg
  */
 public enum Codes {
 
 	// ########## //
 	// Formatting //
 	// ########## //
 
 	BOLD(CodesUtils.get(0x02)),
 	ITALIC(CodesUtils.get(0x09)),
 	STRIKETHROUGH(CodesUtils.get(0x13)),
 	UNDERLINE(CodesUtils.get(0x1F)),
 	UNDERLINE2(CodesUtils.get(0x15)),
 	REVERSE(CodesUtils.get(0x16)),
 
 	// ###### //
 	// Colors //
 	// ###### //
 
 	WHITE(Color.CODE + Color.WHITE),
 	BLACK(Color.CODE + Color.BLACK),
 	BLUE(Color.CODE + Color.BLUE),
 	GREEN(Color.CODE + Color.GREEN),
 	RED(Color.CODE + Color.RED),
 	BROWN(Color.CODE + Color.BROWN),
 	PURPLE(Color.CODE + Color.PURPLE),
 	ORANGE(Color.CODE + Color.ORANGE),
 	YELLOW(Color.CODE + Color.YELLOW),
 	LIGHT_GREEN(Color.CODE + Color.LIGHT_GREEN),
 	TEAL(Color.CODE + Color.TEAL),
 	LIGHT_CYAN(Color.CODE + Color.LIGHT_CYAN),
 	LIGHT_BLUE(Color.CODE + Color.LIGHT_BLUE),
 	PINK(Color.CODE + Color.PINK),
 	GREY(Color.CODE + Color.GREY),
 	LIGHT_GREY(Color.CODE + Color.LIGHT_GREY),
 
 	// ##################### //
 	// Reset colors & format //
 	// ##################### //
 
 	RESET(CodesUtils.get(0x0f)),
 
 	// ########### //
 	// Other codes //
 	// ########### //
 
 	/**
 	 * Space character, used to separate prefix, command and parameters in
 	 * IRC messages
 	 */
 	SP(CodesUtils.get(0x20)),
 
 	/** Carriage return, used to separate different IRC messages */
 	CRLF(CodesUtils.get(0x0D) + CodesUtils.get(0x0A)),
 
 	/**
	 * ASCII Colon, used as first character of any IRC message. Is not
	 * separated from the prefix by SP
 	 */
 	COLON(CodesUtils.get(0x3b)),
 
 	/**
 	 * 'Blank' character. Will not appear in clients that supports UTF-8.
 	 * Used to prevent pinging someone by inserting this character into its
 	 * name
 	 */
 	EMPTY(CodesUtils.get(0x200B));
 
 	// ###################### //
 	// ## END OF ENUM LIST ## //
 	// ###################### //
 
 	/**
 	 * The only purpose of this inner class is to allow contained methods
 	 * to be called from Codes enum definition.
 	 */
 	private static class CodesUtils {
 
 		/**
 		 * Transform a char into a String
 		 *
 		 * @param charCode the integer code of the char
 		 *
 		 * @return a String containing the char
 		 */
 		private static String get(int charCode) {
 			return Character.toString((char) charCode);
 		}
 	}
 
 	/**
 	 * This enum provides a list of color number, their number being their
 	 * ordinal
 	 */
 	private static enum Color {
 		WHITE,
 		BLACK,
 		BLUE,
 		GREEN,
 		RED,
 		BROWN,
 		PURPLE,
 		ORANGE,
 		YELLOW,
 		LIGHT_GREEN,
 		TEAL,
 		LIGHT_CYAN,
 		LIGHT_BLUE,
 		PINK,
 		GREY,
 		LIGHT_GREY;
 
 		/**
 		 * Code used in color codes. A valid color code is composed of this code
 		 * + the color number
 		 */
 		private static final String CODE = CodesUtils.get(0x03);
 
 		/**
 		 * Allows to have 2-chars number, even if it's in the 0-9 range
 		 * (00-09)
 		 */
 		private static final DecimalFormat FORMAT = new DecimalFormat("00");
 
 		/** @return a String of 2 chars containing the color number */
 		public String toString() {
 			return FORMAT.format(this.ordinal());
 		}
 	}
 
 	/** The code that this Enum value represents. */
 	private String code;
 
 	private Codes(String code) {
 		this.code = code;
 	}
 
 	public String toString() {
 		return code;
 	}
 }

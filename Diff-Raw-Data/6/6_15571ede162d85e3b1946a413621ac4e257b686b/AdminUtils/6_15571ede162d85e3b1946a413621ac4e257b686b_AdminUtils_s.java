 package admin;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import common.Utils;
 import data.Person;
 
 public class AdminUtils extends Utils {
 
 	// A clean way to handle themes with minimal code.
 	enum THEMES {
 		// ThemeName(Foreground Color,Background Color);
 		Western(new Color(255, 255, 255), new Color(79, 38, 100), Color.WHITE), 
 		Snow(new Color(0, 0, 255), new Color(255, 255, 255), Color.BLUE), 
 		BandW(new Color(255, 255, 255), new Color(0, 0, 0), Color.GRAY);
 		Color fore, back, table;
 
 		THEMES(Color f, Color b, Color t) {
 			fore = f;
 			back = b;
 			table = t;
 		}
 
 		public Color getBackground() {
 			return back;
 		}
 
 		public Color getForeground() {
 			return fore;
 		}
 		
 		public Color getTableHighlight() {
 			return table;
 		}
 	};
 
 	private static THEMES theme = THEMES.Snow;
 
 	/**
 	 * The background colour of current theme
 	 * 
 	 * @return Color get the background colour of current theme
 	 */
 	public static Color getThemeBG() {
 		return theme.getBackground();
 	}
 
 	/**
 	 * The foreground colour of current theme
 	 * 
 	 * @return Color get the foreground colour of current theme
 	 */
 	public static Color getThemeFG() {
 		return theme.getForeground();
 	}
 	
 	/**
 	 * The table highlight colour.
 	 * @return Color of the table to highlight.
 	 */
 	public static Color getThemeTableHighlight() {
 		return theme.getTableHighlight();
 	}
 
 	/**
 	 * Change the current theme
 	 * 
 	 * @see getThemes() to get all themes
 	 * @param name
 	 *            The theme's name to change to
 	 */
 	public static void changeTheme(String name) {
 		THEMES[] t = THEMES.values();
 		for (int i = 0; i < t.length; i++) {
 			if (t[i].name().equalsIgnoreCase(name))
 				theme = t[i];
 		}
 	}
 
 	/**
 	 * Gets a string array of all themes possible
 	 * 
 	 * @return String array with the themes names
 	 */
 	public static String[] getThemes() {
 		THEMES[] t = THEMES.values();
 		String[] themeString = new String[t.length];
 		for (int i = 0; i < t.length; i++) {
 			themeString[i] = t[i].name();
 		}
 		return themeString;
 	}
 
 	/**
 	 * modified from http://today.java.net/pub/a/today/2003/10/14/swingcss.html
 	 * Changes theme of the current component.If a component is a
 	 * container(Panel,frame,etc.) The background and foreground of all the
 	 * components within the container.
 	 * 
 	 * @param comp
 	 *            The component to apply the theme
 	 */
 	//
 	public static void style(Component comp) {
 
 		comp.setForeground(theme.getForeground());
 		comp.setBackground(theme.getBackground());
 		if (!(comp instanceof Container)) {
 			return;
 		}
 
 		Component[] comps = ((Container) comp).getComponents();
 		for (int i = 0; i < comps.length; i++) {
 			style(comps[i]);
 		}
 	}
 
 	/**
 	 * Uses the stored first and last name, and the currently running Game to
 	 * generate a unique user ID.
 	 * 
 	 * @param activeCon
 	 *            person to use data from.
 	 * @param a
 	 *            Data to check through
 	 */
 	public static String generateID(Person activeCon, List<Person> a) {
 		if (activeCon.getLastName() == null || activeCon.getFirstName() == null) {
 			System.out.println("generateContestantID: first or last name null");
 			return new String();
 		}
 
 		ArrayList<String> ids = new ArrayList<String>(a.size());
 		for (Person p : a)
 			if (p.getID() != null)
 				ids.add(p.getID());
 
 		String newID;
 		String lastName = activeCon.getLastName().toLowerCase()
 				.replaceAll("\\s+", "");
 		String firstName = activeCon.getFirstName().toLowerCase();
 		int lastSub = Math.min(6, lastName.length());
 		int num = 0;
 		boolean valid = false;
 
 		do {
 			// take the first letter of first name
 			// take substring of lastName length 6 or full name
 			newID = firstName.charAt(0) + lastName.substring(0, lastSub);
 			if (num != 0) {
 				newID += Integer.toString(num);
 			}
 			num++;
 
 			valid = true;
 			for (String id : ids)
 				if (id.equalsIgnoreCase(newID)) {
 					valid = false;
 					break;
 				}
 		} while (!valid); // check if the ID is present
 
 		System.out.println("Generated: " + newID);
 		return newID;
 	}
 
 	/**
 	 * Removes all 'null' items from a list.
 	 * 
 	 * @param l
 	 * @return
 	 */
 	public static <T> List<T> noNullList(List<T> l) {
 		List<T> list = new ArrayList<T>(l);
 
 		int i = 0;
 		while ((i = list.indexOf(null)) != -1)
 			list.remove(i);
 
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> List<T> uncastListToCast(List list, T t) {
 		List<T> newList = new ArrayList<T>(list.size());
 		for (Object o: list) {
 			if (o != null) {
 				newList.add((T) o);
 			}
 		}
 		
 		return newList;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List castListToUncast(List<T> list) {
		List newList = new ArrayList(list.size());
 		for (T t: list) {
 			if (t != null) {
 				newList.add((Object) t);
 			}
 		}
 		
 		return newList;
 	}
 	
 	/**
 	 * removes null entries before performing binary search on a list.
 	 * 
 	 * @param list
 	 * @param target
 	 * @param comp
 	 * @return
 	 */
 	public static <T> int BinSearchSafe(List list, T target,
 			Comparator<T> comp) {
 		List<T> newList = uncastListToCast(list, target);
 		
 		return Collections.binarySearch(newList, target, comp);
 	}
 }

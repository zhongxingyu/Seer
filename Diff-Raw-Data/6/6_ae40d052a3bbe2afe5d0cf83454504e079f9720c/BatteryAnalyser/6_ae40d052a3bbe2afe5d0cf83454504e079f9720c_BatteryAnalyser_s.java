 package de.og.batterycreator.systemuianalyser.analyser;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.ImageIcon;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import de.og.batterycreator.systemuianalyser.data.BatteryType;
 
 public class BatteryAnalyser {
 
 	/**
 	 * the Logger for this Class
 	 */
 	private static final Logger				LOG						= LoggerFactory.getLogger(BatteryAnalyser.class);
 	private final File						extractDir;
 	private final Map<String, BatteryType>	batteryTypeMap			= new HashMap<String, BatteryType>();
 	private int								anzahlOnePercentMods	= 0;
 
 	public BatteryAnalyser(final File extractDir) {
 		this.extractDir = extractDir;
 	}
 
 	public void analyse() {
 		findAllBatteriesInDirTree(extractDir);
 		finalizeResults();
 	}
 
 	private void finalizeResults() {
 		LOG.info("##################################################");
 		LOG.info(" Battery Analyser");
 		LOG.info("##################################################");
 		for (final BatteryType type : batteryTypeMap.values()) {
 			LOG.info(" Found: " + type.toDebugString());
 			if (type.isOnPercentMod()) {
 				anzahlOnePercentMods++;
 			}
 		}
 		LOG.info("##################################################");
 		if (anzahlOnePercentMods == 0) {
 			LOG.info(" Sorry your SystemUI has no 1% MOD");
 		} else {
 			LOG.info(" Your SystemUI has {} 1% MOD(s)", anzahlOnePercentMods);
 		}
 		LOG.info("##################################################");
 	}
 
 	private void findAllBatteriesInDirTree(final File file) {
 		if (file.isDirectory()) {
 			final File[] subDirs = findSubDirs(file);
 			for (int i = 0; i < subDirs.length; i++) {
 				findAllBatteriesInDirTree(subDirs[i]);
 			}
 			final File[] files = findBatteryPNGs(file);
 			for (final File png : files) {
 				final ImageIcon icon = new ImageIcon(png.getPath());
 				final File parent = new File(png.getParent());
 				final String drawableFolder = parent.getName();
 				addToBatteryType(png.getName(), icon, drawableFolder);
 			}
 		}
 	}
 
 	private void addToBatteryType(final String filename, final ImageIcon icon, final String drawableFolder) {
 		// suchen nach der Nummer
 		int index = indexOfAnyNumber(filename);
 		// keine nummer gefunden ? dann suchen wir nach "full.png"
 		if (index < 0)
 			index = filename.indexOf("full.png");
 
 		if (index > 0) {
 			String pattern = filename.substring(0, index);
 			final int indexCharge = filename.indexOf(BatteryType.CHARGE_ANIM);
 			boolean ischarge = false;
 			if (indexCharge > 0) {
 				ischarge = true;
 				pattern = filename.substring(0, indexCharge);
 			}
			BatteryType type = batteryTypeMap.get(pattern);
 			if (type == null) {
 				type = new BatteryType(pattern, drawableFolder);
				batteryTypeMap.put(pattern, type);
 			}
 			icon.setDescription(filename);
 			type.addIcon(icon, ischarge);
 		}
 	}
 
 	public Map<String, BatteryType> getBatteryTypeMap() {
 		return batteryTypeMap;
 	}
 
 	private static File[] findBatteryPNGs(final File dir) {
 		final File[] pngs = dir.listFiles(new FilenameFilter() {
 
 			@Override
 			public boolean accept(final File dir, final String name) {
 				return name.toLowerCase().endsWith(".png") && name.toLowerCase().startsWith(BatteryType.BATTERY_PREFIX);
 			}
 		});
 		return pngs;
 	}
 
 	/**
 	 * @param file
 	 * @return
 	 */
 	private File[] findSubDirs(final File file) {
 		// Liste aller Subdirs
 		final File[] subDirs = file.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(final File pathname) {
 				return pathname.isDirectory();
 			}
 		});
 		return subDirs;
 	}
 
 	public static int indexOfAnyNumber(final String str) {
 		final char[] searchChars = {
 				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
 		};
 
 		if (str == null || str.length() == 0) {
 			return -1;
 		}
 		for (int i = 0; i < str.length(); i++) {
 			final char ch = str.charAt(i);
 			for (int j = 0; j < searchChars.length; j++) {
 				if (searchChars[j] == ch) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * @return the hasOnePercentMod
 	 */
 	public boolean hasOnePercentMod() {
 		return anzahlOnePercentMods > 0;
 	}
 
 }

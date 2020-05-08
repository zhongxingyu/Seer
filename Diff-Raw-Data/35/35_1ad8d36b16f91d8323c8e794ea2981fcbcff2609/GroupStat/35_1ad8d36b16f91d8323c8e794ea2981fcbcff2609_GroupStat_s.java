 package fm.flickr.stat.perform;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
import java.util.Map;
 import java.util.Vector;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
 import fm.flickr.api.wrapper.service.FlickrService;
 import fm.flickr.api.wrapper.service.param.GroupItem;
 import fm.flickr.api.wrapper.service.param.GroupItemsSet;
 import fm.flickr.api.wrapper.service.param.PhotoItem;
 import fm.flickr.api.wrapper.service.param.PhotoItemsSet;
 import fm.flickr.api.wrapper.util.ServiceException;
 import fm.flickr.stat.param.GroupItemStat;
 import fm.flickr.stat.param.GroupsPerPhoto;
 import fm.util.Config;
 import fm.util.Util;
 
 /**
  * Collect the number of groups an explored photo belongs to.
  * 
  * Daily stats load result files over a period of time and sort groups by number of occurences, ie. the number of
  * explored photos that belong to each group.
  * A feature of daily stats also calculate the ratio number of explored photos / number of uploads to the group 
  * over the period considered, to figure out some kind of "probability of being explored of a group".
  * 
  * Monthly stats will compute the average and max number of groups a photo belongs to.
  * 
  * @author Atreyu
  */
 public class GroupStat
 {
 	private static Logger logger = Logger.getLogger(GroupStat.class.getName());
 
 	private static Configuration config = Config.getConfiguration();
 
 	private static FlickrService service = new FlickrService();
 
 	/** Groups collected or loaded. The key is the group id */
 	private static HashMap<String, GroupItemStat> statistics = new HashMap<String, GroupItemStat>();
 
 	/** Stats of number of groups a photo belongs to */
 	private static List<GroupsPerPhoto> statisticsGpP = new ArrayList<GroupsPerPhoto>();
 
 	/**
 	 * Retrieve the list of groups that photos from Interestingness belong to, and counts the number
 	 * of times the same group is entcountered.
 	 * 
 	 * A maximum of 'fm.flickr.stat.group.maxgroups' groups will be stored. 
 	 * The results are saved into a file.
 	 * 
 	 * @param date date of photos from Interestingness, given in format "YYY-MM-DD"
 	 * @param photos set of photos retrieved from Interestingness
 	 * @throws IOException in case the file can't be saved
 	 */
 	public static void collecAdditionalData(String date, PhotoItemsSet photos) throws IOException {
 
 		HashMap<String, GroupItemStat> stats = new HashMap<String, GroupItemStat>();
 
 		int nbPhotosProcessed = 0; // Number of photos explored during the whole process
 		int sumGroups = 0; // Sum of the number of groups each photo belongs to
 		int maxGroups = 0; // max number of groups a photo belongs to
 		Vector<Integer> listNumGroups = new Vector<Integer>(); // list of number of groups that photos belong to
 
 		// Loop on all photos retrieved at once from Interstingness
 		for (PhotoItem photo : photos.getPhotosList()) {
 			nbPhotosProcessed++;
 
 			// Get all groups that the photo belongs to
 			GroupItemsSet groupSet = service.getPhotoPools(photo.getPhotoId());
 			if (groupSet != null) {
 				if (groupSet.size() > maxGroups)
 					maxGroups = groupSet.size();
 				listNumGroups.add(groupSet.size());
 
 				// For each group check if it is already in the list, add it if it is not
 				for (GroupItem group : groupSet.getGroupsList()) {
 					sumGroups++;
 					String groupId = group.getGroupId();
 
 					GroupItemStat groupStat = stats.get(groupId);
 					if (groupStat != null) {
 						// Either increment the number of occurences by one if the group already exists
 						groupStat.incNbOccurences();
 						logger.trace("Updating group " + groupId + ": " + groupStat.getNbOccurences());
 					} else {
 						// Or create a new group element with 1 occurence, but limit the maximum number of groups
 						if (stats.size() < config.getInt("fm.flickr.stat.group.maxgroups")) {
 							stats.put(groupId, new GroupItemStat(group, 1));
 							logger.trace("Adding group " + groupId);
 						} else {
 							logger.warn("Ignoring group " + group.getGroupName() + ", already reached " + config.getInt("fm.flickr.stat.group.maxgroups") + " groups");
 						}
 					}
 				}
 
 				// Trace activity every 10 photos
 				if (nbPhotosProcessed % 10 == 0)
 					logger.info("Processed " + nbPhotosProcessed + " photos, registered " + stats.size() + " groups.");
 			}
 
 			// Sleep between each photo... just not to be overloading
 			try {
 				Thread.sleep(config.getInt("fm.flickr.stat.sleepms"));
 			} catch (InterruptedException e) {
 				logger.warn("Unepected interruption: " + e.toString());
 				e.printStackTrace();
 			}
 		}
 
 		logger.info("### Processed " + stats.size() + " groups from " + nbPhotosProcessed + " photos");
 		logger.info("### Average number of groups a photo belongs to: " + sumGroups / nbPhotosProcessed);
 		logger.info("### Maximum number of groups a photo belongs to: " + maxGroups);
 
 		// Calculate the standard deviation
 		int avg = sumGroups / nbPhotosProcessed;
 		int sumDeviations = 0;
 		for (Integer nbgrp : listNumGroups)
 			if (nbgrp - avg > 0)
 				sumDeviations += nbgrp - avg;
 
 		int stdDev = 0;
 		if (!listNumGroups.isEmpty()) {
 			logger.info("### Standard deviation of number of groups a photo belongs to: " + sumDeviations / listNumGroups.size());
 			stdDev = sumDeviations / listNumGroups.size();
 		}
 		saveGroupsFromInterestingPhotos(date, stats, nbPhotosProcessed, sumGroups, maxGroups, stdDev);
 	}
 
 	/**
 	 * Save the list of groups into a file, including group id, name and count of explored photos
 	 * that belong to each group.
 	 * 
 	 * @param date
 	 * @param stats list of groups retrieved
 	 * @param nbPhotosProcessed Number of photos explored during the whole process
 	 * @param sumGroups Sum of the number of groups each photo belongs to (to be able to compute the average number of groups per photo)
 	 * @param maxGroups Max number of groups each photo belongs to
 	 * @param stdDeviation Standard deviation of number of groups a photo belongs to
 	 * @throws IOException
 	 */
 	private static void saveGroupsFromInterestingPhotos(String date, HashMap<String, GroupItemStat> stats, int nbPhotosProcessed, int sumGroups, int maxGroups, int stdDeviation) throws IOException {
 
 		File file = new File(Util.getDir(config.getString("fm.flickr.stat.group.dir")), date + ".log");
 		FileOutputStream fos = new FileOutputStream(file);
 		PrintWriter writer = new PrintWriter(fos);
 
 		logger.info("Saving " + stats.size() + " groups into file " + file.getCanonicalPath());
 
 		writer.println("# Processed " + stats.size() + " groups from " + nbPhotosProcessed + " photos");
 		writer.println("# Number of photos explored: " + nbPhotosProcessed);
 		writer.println("# Total number of groups: " + sumGroups);
 		writer.println("#");
 		writer.println("# Average number of groups a photo belongs to: " + sumGroups / nbPhotosProcessed);
 		writer.println("# Maximum number of groups a photo belongs to: " + maxGroups);
 		writer.println("# Standard deviation of the number of groups a photo belongs to: " + stdDeviation);
 		writer.println("#");
 		writer.println("# groupId SEPARATOR group name SEPARATOR number of occurences of that group");
 		writer.println("#");
 
 		Collection<GroupItemStat> grpset = stats.values();
 		Iterator<GroupItemStat> iter = grpset.iterator();
 		while (iter.hasNext()) {
 			GroupItemStat entry = iter.next();
 			writer.println(entry.toFile());
 		}
 		writer.close();
 		fos.close();
 	}
 
 	/**
 	 * Load the content of the file for the given date, into the map of statistics.
 	 * The file name should be <date>.log.
 	 * 
 	 * @param date date of data collected from Interestingness, given in format "YYY-MM-DD"
 	 */
 	public static void loadFileByDay(String date) throws ServiceException {
 
 		String fileName = config.getString("fm.flickr.stat.group.dir") + date + ".log";
 		loadFile(new File(fileName));
 		logger.info("### " + statistics.size() + " total groups loaded.");
 	}
 
 	/**
 	 * Load the content of the all files within the given month, into the map of statistics.
 	 * Any file name starting with "yyyy-mm" will be loaded, may there be 1 or 31 files for that month.
 	 * 
 	 * @param yearMonth year and month formatted as yyyy-mm
 	 */
 	public static void loadFilesByMonth(String yearMonth) throws ServiceException {
 		// Empty the current data if any
 		statistics.clear();
 		statisticsGpP.clear();
 
 		File dir = new File(config.getString("fm.flickr.stat.group.dir"));
 		if (!dir.exists() || !dir.isDirectory()) {
 			String errMsg = "Error: data directory " + config.getString("fm.flickr.stat.group.dir") + " does not exists.";
 			logger.warn(errMsg);
 			throw new ServiceException(errMsg);
 		}
 
 		for (File file : dir.listFiles())
 			if (file.getName().startsWith(yearMonth))
 				loadFile(file);
 
 		logger.info("### " + statistics.size() + " total groups loaded for period " + yearMonth);
 	}
 
 	/**
 	 * Parse the content of the given data file and store its content into static maps
 	 * statistics and statisticsGpP
 	 * 
 	 * @param file
 	 */
 	private static void loadFile(File file) throws ServiceException {
 		try {
 			if (!file.exists()) {
 				logger.warn("No file: " + file.getAbsolutePath());
 				return;
 			}
 
 			FileInputStream fis = new FileInputStream(file);
 			BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
 			logger.info("### Loading file " + file.getAbsolutePath());
 			String str = buffer.readLine();
 
 			while (str != null) {
 				if (str.trim().startsWith("#")) {
 					// Process comment lines like:
 					// # Average number of groups a photo belongs to: 5
 					// # Maximum number of groups a photo belongs to: 53
 					// # Standard deviation of the number of groups a photo belongs to: 2
 					GroupsPerPhoto gpp = new GroupsPerPhoto();
 
 					String strSeeked = "# Average number of groups a photo belongs to: ";
 					if (str.startsWith(strSeeked)) {
 						gpp.setAvgGroupsPerPhoto(Integer.valueOf(str.substring(strSeeked.length())));
 
 						str = buffer.readLine();
 						strSeeked = "# Maximum number of groups a photo belongs to: ";
 						if (str.startsWith(strSeeked))
 							gpp.setMaxGroupsPerPhoto(Integer.valueOf(str.substring(strSeeked.length())));
 
 						str = buffer.readLine();
 						strSeeked = "# Standard deviation of the number of groups a photo belongs to: ";
 						if (str.startsWith(strSeeked))
 							gpp.setStdDevGroupsPerPhoto(Integer.valueOf(str.substring(strSeeked.length())));
 
 						statisticsGpP.add(gpp);
 					}
 
 				} else {
 					String[] tokens = str.split(GroupItemStat.FIELD_SEPARATOR);
 					if (tokens.length != 3)
 						logger.warn("Wrong format on line: " + str);
 					else {
 						String groupId = tokens[0];
 
 						GroupItemStat groupStat = statistics.get(groupId);
 						if (groupStat != null) {
 							// Either increment the number of occurences if the group already exists
 							groupStat.incNbOccurences(Integer.valueOf(tokens[2]));
 							logger.trace("Updating group " + groupId + ": " + groupStat.getNbOccurences());
 
 						} else {
 							// Or create a new group element
 							GroupItemStat group = new GroupItemStat(tokens[0], tokens[1], Integer.valueOf(tokens[2]));
 							logger.trace("Adding group " + groupId);
 							statistics.put(groupId, group);
 						}
 					}
 				}
 				str = buffer.readLine();
 			}
 			fis.close();
 
 		} catch (IOException e) {
 			String errMsg = "Error when reading file " + file.getName() + ". Exception: " + e.toString();
 			logger.warn(errMsg);
 			throw new ServiceException(errMsg);
 		}
 	}
 
 	/**
 	 * Print the header line following the csv format
 	 * @param ps
 	 * @throws FileNotFoundException
 	 */
 	public static void initComputeMonthly(PrintStream ps) throws FileNotFoundException {
 		ps.print("#month; ");
 		ps.println("avg groups/photo; std dev groups/user; max groups/user");
 	}
 
 	/**
 	 * Display groups sorted by score (number of exploreed photos in that group during the considered pediod.
 	 * Then, calculate the ratio of explored photos / uploaded photos.
 	 * 
 	 * @param ps where to print the output. The ratio of explored photos / uploaded photos does not take this param into account, 
 	 * and always outputs data to a file /group_explore_proba_<start date>_<end date>.csv
 	 */
 	public static void computeStatistics(PrintStream ps) {
 		logger.info("Computing statistincs of groups");
 		Collection<GroupItemStat> grpset = statistics.values();
 		ArrayList<GroupItemStat> grpList = new ArrayList<GroupItemStat>(grpset);
 
 		// Sort the groups by number of occurences (hx to method GroupItemStat.compareTo() 
 		Collections.sort(grpList);
 
 		// Display the n first result groups sorted by decreasing number of explored photos they contain 
 		for (int i = 0; i < grpList.size() && i < config.getInt("fm.flickr.stat.group.maxresults"); i++) {
 			GroupItemStat entry = grpList.get(i);
 			ps.println((i + 1) + ": " + entry.toStringL());
 		}
 		ps.println();
 
 		// Calculate the "probability of being explored thanks to that group"
 		if (config.getString("fm.flickr.stat.group.proba").equalsIgnoreCase("on")) {
 			String fn = config.getString("fm.flickr.stat.group.dir") + "/group_explore_proba_" + config.getString("fm.flickr.stat.startdate") + "_" + config.getString("fm.flickr.stat.enddate") + ".csv";
 			try {
 				PrintStream psGrpProba = new PrintStream(fn);
 				postProcessStat(psGrpProba, grpList);
 			} catch (FileNotFoundException e) {
 				logger.error("Can't write file " + fn);
 			}
 		}
 	}
 
 	/**
 	 * Display the average/std deviation and maximum number of groups and photos
 	 * 
 	 * @param ps where to print the output
 	 * @param month in case of processing data by month, this string denotes the current month formatted as yyyy-mm. May be null.
 	 */
 	public static void computeMonthlyStatistics(PrintStream ps, String month) {
 		int sumAvg = 0;
 		int sumStdDev = 0;
 		int sumMax = 0;
 		for (GroupsPerPhoto gpp : statisticsGpP) {
 			sumAvg += gpp.getAvgGroupsPerPhoto();
 			sumStdDev += gpp.getStdDevGroupsPerPhoto();
 			sumMax += gpp.getMaxGroupsPerPhoto();
 		}
 
 		ps.print(month + "; ");
 		ps.print(Math.abs(sumAvg / statisticsGpP.size()) + "; ");
 		ps.print(Math.abs(sumStdDev / statisticsGpP.size()) + "; ");
 		ps.println(Math.abs(sumMax / statisticsGpP.size()));
 	}
 
 	/**
 	 * For each group sorted by computeStatistics(), this method will calculate the 
 	 * "probability" of a photo to be explored just by being posted on that group:
 	 * <code>proba = number of explored photos during a time slot / total number of photos posted in that 
 	 * group during the same time slot * 100</code>
 	 * That processing require additional queries to Flickr.
 	 * @param ps
 	 * @param grpList
 	 */
 	private static void postProcessStat(PrintStream ps, ArrayList<GroupItemStat> grpList) {
 
 		ps.println("group ID; group name; total nb photos; nb members; nb photos posted; nb explored photos; explore proba; is moderated");
 
 		for (int i = 0; i < grpList.size() && i < config.getInt("fm.flickr.stat.group.maxresults"); i++) {
 			GroupItemStat entry = grpList.get(i);
 			logger.info("Processing group " + entry.toStringShort());
 
 			// Get the general information about the group (number of members, of photos, is moderated)
 			GroupItem grpItem = service.getGroupInfo(entry.getGroupId());
 			if (grpItem != null) {
 				Long nbPosted = service.getNbOfPhotosAddedToGroup(grpItem.getGroupId(), config.getString("fm.flickr.stat.startdate"), config.getString("fm.flickr.stat.enddate"));
 
 				if (nbPosted != null) {
 					float proba = entry.getNbOccurences() * 100;
 					proba = proba / nbPosted;
					ps.print(grpItem.getGroupId() + "; " + grpItem.getGroupName() + "; " + grpItem.getNbPhotos() + "; " + grpItem.getNbMembers() + "; ");
 					ps.print(nbPosted + "; " + entry.getNbOccurences() + "; ");
 					ps.printf("%2.4f;", proba);
 					ps.println(grpItem.getIsModerated()? "moderated": "");
 				} else
 					logger.info("Skipping group " + entry.toStringShort());
 			}
 		}
 	}
 }

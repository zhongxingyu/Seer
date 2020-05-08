 package gov.nih.nci.cagrid.syncgts.core;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.syncgts.bean.DateFilter;
 import gov.nih.nci.cagrid.syncgts.bean.SyncReport;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * @author <A href="mailto:langella@bmi.osu.edu">Stephen Langella </A>
  * @author <A href="mailto:oster@bmi.osu.edu">Scott Oster </A>
  * @author <A href="mailto:hastings@bmi.osu.edu">Shannon Hastings </A>
  * @author <A href="mailto:David.Ervin@osumc.edu">David W. Ervin</A>
  * @version $Id: ArgumentManagerTable.java,v 1.2 2004/10/15 16:35:16 langella
  *          Exp $
  */
 public class HistoryManager {
 
 	public static final int DEFAULT_MAX_REPORTS = 150;
 	public static final String MAX_REPORTS_PROPERTY = "max.sync.reports";
 	
 	private static final QName REPORTS_QN = new QName(SyncGTSDefault.SYNC_GTS_NAMESPACE, "SyncReport");
 	
 	private static Log LOG = LogFactory.getLog(HistoryManager.class);
 
 	public File addReport(SyncReport report) throws Exception {
 		File r = getFile(report.getTimestamp());
 		Utils.serializeDocument(r.getAbsolutePath(), report, REPORTS_QN);
 		return r;
 	}
 	
 	
 	public SyncReport getReport(String fileName) throws Exception {
         return Utils.deserializeDocument(fileName, SyncReport.class);
     }
 
 
 	public SyncReport getLastReport() throws Exception {
 		File dir = getLastDayDir();
 		if (dir != null) {
 			File last = null;
 			if (dir != null && dir.exists()) {
 	            File[] files = dir.listFiles(new FileFilter() {
 	                public boolean accept(File pathname) {
 	                    return pathname.getName().endsWith(".xml");
 	                }
 	            });
 	            if (files != null) {
 	                sortByFilename(files);
 	                last = files[files.length - 1];
 	            }
 	        }
 			if (last != null) {
 				return getReport(last.getAbsolutePath());
 			}
 		}
 		return null;
 	}
 
 
 	private File getEarliestDayDir() {
 		return getEarliestDir(getEarliestMonthDir());
 	}
 
 
 	private File getEarliestMonthDir() {
 		return getEarliestDir(getEarliestYearDir());
 	}
 
 
 	private File getEarliestYearDir() {
 		return getEarliestDir(getHistoryDirectory());
 	}
 
 
 	/**
 	 * Gets the earliest named sub-directory
 	 * 
 	 * @param dir
 	 * @return
 	 */
 	private File getEarliestDir(File dir) {
 	    File earliest = null;
 	    if (dir != null && dir.exists()) {
 	        File[] dirs = dir.listFiles(new FileFilter() {
 	            public boolean accept(File pathname) {
 	                return pathname.isDirectory();
 	            }
 	        });
 	        if (dirs != null) {
 	            sortByFilename(dirs);
 	            earliest = dirs[0];
 	        }
 	    }
 	    return earliest;
 	}
 
 
 	private File getLastDayDir() {
 		return getLastDir(getLastMonthDir());
 	}
 
 
 	private File getLastMonthDir() {
 		return getLastDir(getLastYearDir());
 	}
 
 
 	private File getLastYearDir() {
 		return getLastDir(getHistoryDirectory());
 	}
 
 
 	private File getLastDir(File dir) {
 		File last = null;
 		if (dir != null && dir.exists()) {
             File[] dirs = dir.listFiles(new FileFilter() {
                 public boolean accept(File pathname) {
                     return pathname.isDirectory();
                 }
             });
             if (dirs != null) {
                 sortByFilename(dirs);
                 last = dirs[dirs.length - 1];
             }
         }
 		return last;
 	}
 
 
 	private DateFilter getEarliestDateFilter() throws Exception {
 		File day = getEarliestDayDir();
 		File month = day.getParentFile();
 		File year = month.getParentFile();
 
 		if ((day == null) || (month == null) || (year == null)) {
 			return null;
 		} else {
 			DateFilter d = new DateFilter();
 			d.setDay(Integer.valueOf(day.getName()).intValue());
 			d.setMonth(Integer.valueOf(month.getName()).intValue());
 			d.setYear(Integer.valueOf(year.getName()).intValue());
 			return d;
 		}
 	}
 
 	
 	private boolean isAfter(DateFilter start, DateFilter end){
 	    if (start.getYear() <= end.getYear()) {
 			if (start.getMonth() <= end.getMonth()) {
 				if (start.getDay() <= end.getDay()) {
 					return false;
 				} else {
 					return true;
 				}
 			} else {
 				return true;
 			}
 		} else {
 			return true;
 		}
 	}
 	
 
 	public SyncReport[] search(DateFilter startDate, DateFilter end) throws Exception {
 		DateFilter start = new DateFilter();
 		start.setDay(startDate.getDay());
 		start.setMonth(startDate.getMonth());
 		start.setYear(startDate.getYear());
 		SyncReport[] reports = new SyncReport[DEFAULT_MAX_REPORTS];
 		int checkMax = 0;
 		int iterator = 0;
 		this.incrementDate(end);
 		while (!start.equals(end)) {
 			File startDir = getDirectory(start);
 			if ((startDir.exists()) && (startDir.isDirectory())) {
 				String[] fileList = startDir.list();
 				checkMax = checkMax + fileList.length;
 				if (checkMax > DEFAULT_MAX_REPORTS)
 					throw new Exception();
 				else {
 					for (int i = 0; i < fileList.length; i++) {
 						File inputFile = new File(startDir.getAbsolutePath() + File.separator + fileList[i]);
 						FileReader in = new FileReader(inputFile);
 						if (in.read() != -1) {
 							reports[iterator] = this.getReport(startDir.getAbsolutePath() + File.separator
 								+ fileList[i]);
 							iterator++;
 						} else
 							throw new Exception();
 						in.close();
 					}
 				}
 			}
 			this.incrementDate(start);
 		}
 		SyncReport[] returnReports = new SyncReport[checkMax];
 		System.arraycopy(reports, 0, returnReports, 0, returnReports.length);
 
 		return returnReports;
 	}
 
 
 	public SyncReport[] search(DateFilter startDate, DateFilter end, File histDir) throws Exception {
 		DateFilter start = new DateFilter();
 		start.setDay(startDate.getDay());
 		start.setMonth(startDate.getMonth());
 		start.setYear(startDate.getYear());
 		SyncReport[] reports = new SyncReport[DEFAULT_MAX_REPORTS];
 		int checkMax = 0;
 		int iterator = 0;
 		this.incrementDate(end);
 		while (!start.equals(end)) {
 			File startDir = getDirectory(start, histDir);
 			if ((startDir.exists()) && (startDir.isDirectory())) {
 				String[] fileList = startDir.list();
 				checkMax = checkMax + fileList.length;
 				if (checkMax > DEFAULT_MAX_REPORTS)
 					throw new Exception();
 				else {
 					for (int i = 0; i < fileList.length; i++) {
 						File inputFile = new File(startDir.getAbsolutePath() + File.separator + fileList[i]);
 						FileReader in = new FileReader(inputFile);
 						if (in.read() != -1) {
 							reports[iterator] = this.getReport(startDir.getAbsolutePath() + File.separator
 								+ fileList[i]);
 							iterator++;
 						} else {
 							throw new Exception();
 						}
 						in.close();
 					}
 				}
 			}
 			this.incrementDate(start);
 		}
 		SyncReport[] returnReports = new SyncReport[checkMax];
 		System.arraycopy(reports, 0, returnReports, 0, returnReports.length);
 
 		return returnReports;
 	}
 
 
 	private File getFile(String timestamp) {
 		File dir = getDirectory(timestamp);
 		return new File(dir.getAbsolutePath() + File.separator + timestamp + ".xml");
 	}
 
 
 	protected File getHistoryDirectory() {
 		File dir = new File(SyncGTSDefault.getSyncGTSUserDir() + File.separator + "history");
 		dir.mkdirs();
 		return dir;
 	}
 
 
 	private File getDirectory(DateFilter f) {
 		File histDir = getHistoryDirectory();
 		return getDirectory(f, histDir);
 	}
 
 
 	private File getDirectory(DateFilter f, File histDir) {
 		String month;
 		String day;
 		if (f.getMonth() < 10) {
 			month = "0" + f.getMonth();
 		} else {
 			month = "" + f.getMonth();
 		}
 
 		if (f.getDay() < 10) {
 			day = "0" + f.getDay();
 		} else {
 			day = "" + f.getDay();
 		}
 
 		File dir = new File(histDir, 
 		    f.getYear() + File.separator + month + File.separator + day);
 		return dir;
 	}
 
 
 	private File getDirectory(String timestamp) {
 		String year = timestamp.substring(0, 4);
 		String month = timestamp.substring(4, 6);
 		String days = timestamp.substring(6, 8);
 		File histDir = getHistoryDirectory();
 		File dir = new File(histDir,
 		    year + File.separator + month + File.separator + days);
 		dir.mkdirs();
 		return dir;
 	}
 
 
 	private void incrementDate(DateFilter f) {
 	    Calendar cal = Calendar.getInstance();
 	    // lenient calendar since the old implementation allowed
 	    // conditions like "Feb 31st" to exist...
 	    cal.setLenient(true);
 	    // set up the current date
 	    cal.set(Calendar.YEAR, f.getYear());
 	    cal.set(Calendar.MONTH, f.getMonth() - 1);
 	    cal.set(Calendar.DAY_OF_MONTH, f.getDay());
 	    // move forward one day
 	    cal.roll(Calendar.DAY_OF_MONTH, 1);
 	    
 	    // put the date back into the DateFilter bean
 	    f.setDay(cal.get(Calendar.DAY_OF_MONTH));
 	    f.setMonth(cal.get(Calendar.MONTH + 1));
 	    f.setYear(cal.get(Calendar.YEAR));
 	}
 	
 	
 	private void sortByFilename(File[] files) {
 	    Comparator<File> comp = new Comparator<File>() {
             public int compare(File o1, File o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         };
         Arrays.sort(files, comp);
 	}
 	
 	
 	public void prune(DateFilter filter) throws Exception {
 	    // filter tells us how old of a history file we can keep
 	    // create a calendar of the current date
 	    Calendar startCal = Calendar.getInstance();
	    startCal.setLenient(true);
 	    // roll it BACK by the ammount in the filter
	    startCal.set(Calendar.YEAR, startCal.get(Calendar.YEAR) - filter.getYear());
	    startCal.set(Calendar.MONDAY, startCal.get(Calendar.MONTH) - filter.getMonth());
	    startCal.set(Calendar.DAY_OF_MONTH, startCal.get(Calendar.DAY_OF_MONTH) - filter.getDay());
	    /*
 	    startCal.roll(Calendar.YEAR, -filter.getYear());
 	    startCal.roll(Calendar.MONTH, -filter.getMonth());
 	    startCal.roll(Calendar.DAY_OF_MONTH, -filter.getDay());
	    */
 	    // create the cuttoff date
 	    DateFilter cuttoff = new DateFilter(
 	        startCal.get(Calendar.DAY_OF_MONTH),
 	        startCal.get(Calendar.MONTH) + 1,
 	        startCal.get(Calendar.YEAR));
 	    // list all the history files and dirs in date order
         List<File> historyFiles = listHistoryFilesByDate();
         for (File f : historyFiles) {
             if (f.isFile() && f.getName().endsWith(".xml")) {
                 // if a history file, figure its creation date
                 File dayDir = f.getParentFile();
                 File monthDir = dayDir.getParentFile();
                 File yearDir = monthDir.getParentFile();
                 DateFilter current = new DateFilter(
                     Integer.parseInt(dayDir.getName()),
                     Integer.parseInt(monthDir.getName()),
                     Integer.parseInt(yearDir.getName()));
                 if (!isAfter(current, cuttoff)) {
                     // file is from before the cuttoff date and can be deleted
                     f.delete();
                     LOG.info("Pruned old sync report " + f.getAbsolutePath());
                 }
             }
         }
         
         // clean up any empty directories
         for (File f : historyFiles) {
             if (f.exists() && f.isDirectory()) {
                 List<File> contents = Utils.recursiveListFiles(f, new FileFilter() {
                     public boolean accept(File pathname) {
                         return true;
                     }
                 });
                 boolean empty = true;
                 for (File content : contents) {
                     if (content.isFile()) {
                         empty = false;
                     }
                 }
                 if (empty) {
                     Utils.deleteDir(f);
                 }
             }
         }
     }
 	
 	
 	private List<File> listHistoryFilesByDate() {
 	    final File historyDir = getHistoryDirectory();
 	    List<File> historyFiles = Utils.recursiveListFiles(historyDir, new FileFilter() {
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith(".xml") || pathname.isDirectory();
             }
         });
 	    Comparator<File> comp = new Comparator<File>() {
             public int compare(File o1, File o2) {
                 int val = 0;
                 try {
                     String rel1 = Utils.getRelativePath(historyDir, o1);
                     String rel2 = Utils.getRelativePath(historyDir, o2);
                     val = rel1.compareTo(rel2);
                 } catch (Exception ex) {
                     // uhoh
                 }
                 return val;
             }
         };
         Collections.sort(historyFiles, comp);
         return historyFiles;
 	}
 	
 	
 	private int countXmlDocs(List<File> files) {
 	    int count = 0;
 	    for (File f : files) {
 	        if (f.isFile() && f.getName().endsWith(".xml")) {
 	            count++;
 	        }
 	    }
 	    return count;
 	}
 
 
     private void deleteIfEmpty(File dir) {
         if (dir.isDirectory()) {
             File files[] = dir.listFiles();
             if ((files == null) || (files.length == 0)) {
                 dir.delete();
             }
         }
     }
 	
 	
 	private static int getMaxReports() {
 	    int max = DEFAULT_MAX_REPORTS;
 	    String val = System.getProperty(MAX_REPORTS_PROPERTY);
 	    try {
 	        max = Integer.valueOf(val).intValue();
 	    } catch (Exception ex) {
 	        // whatever
 	    }
 	    return max;
 	}
 }

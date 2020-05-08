 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 import play.mvc.Result;
 
 import com.avaje.ebean.*;
 
 /**
  * Run entity managed by Ebean
  */
 @Entity 
 public class Run extends Model {
 	//ID
 	@Column(name="Run_ID")
     @Id
 	@GeneratedValue
     public Long id;
     //Name
 	@Column(name="Run_Name")
     @Constraints.Required
     public String name;
 	//Version
 	@Column(name="Version_ID")
 	@OneToOne
 	@Constraints.Required
 	@JoinColumn(name="Version_ID")
 	public Version version;
 	//Format
 	@Column(name="Format_ID")
 	@OneToOne
 	@Constraints.Required
 	@JoinColumn(name="Format_ID")
 	public FileFormat format;
 	//Date
 	@Column(name="Date_ID")
 	@OneToOne
 	@JoinColumn(name="Date_ID")
 	public Date date;
 	//SVN
 	@Column(name="SVN_ID")
 	@OneToOne
 	@JoinColumn(name="SVN_ID")
 	public SVN svn;
 	//Performance
 	@Column(name="Performance_ID")
 	@OneToOne
 	@JoinColumn(name="Performance_ID")
 	@Formats.DateTime(pattern="hh:mm:ss")
 	public Performance performance;
 	
 	
 	/** Transient values for form submission*/
 	//Path to issues folder
 	@Transient
 	public String path;
 	
 	//Path to comparison directory
 	@Transient
 	public String compDir;
 	//Path to input directory
 	@Transient
 	public String inputDir;
 	
 	/**
      * Generic query helper for entity Run with id Long
      */
     public static Finder<Long,Run> find = new Finder<Long,Run>(Long.class, Run.class); 
     
 	
 	/**
      * Return a page of run
      *
      * @param page Page to display
      * @param pageSize Number of computers per page
      * @param sortBy Run property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static Page<Run> page(int page, int pageSize, String sortBy, String order, String filter, String filterBy) {
         return 
             find.where()
                 .ilike(filterBy, "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .fetch("version")
 				.fetch("format")
 				.fetch("date")
 				.fetch("svn")
 				.fetch("performance")
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 	
 	/**
 	 * This method returns the name of the run given a run id
 	 * 
 	 * @param runID The run ID to search for
 	 * 
 	 * @return Name of the run corresponding to runID
 	 */
 	public static String nameByID(Long runID){
 		Run run=find.where()
 			.eq("id",runID)
 			.findUnique();
 		return run.name;
 	}
 	
 	/**
 
 	 * This method returns the a run given a run id
 	 * 
 	 * @param runID The run ID to search for
 	 * 
 	 * @return The run corresponding to runID
 	 */
 	public static List<Run> runByID(Long runID){
 		Run run=find.where()
 			.eq("id",runID)
 			.findUnique();
 		List<Run> runs = new ArrayList<Run>();
 		runs.add(run);
 		return runs;
 	}
     /**
 	 * This method returns the run for the run given a run id
 	 * @param runID The run ID to search for
 	 * 
 	 * @return Corresponding run to given runID
 	 */
 	public static Run getRunByID(Long runID){
 		Run run=find.where()
 			.eq("id",runID)
 			.findUnique();
 		return run;
 	}
 	
 	
 	/**
 	 * This method returns the sortable fields of Run class
 	 * 
 	 * @return List of sortable fields by a text box
 	 */
 	public static List<SortType> getSortFields(){
 		SortType name = new SortType("name", "Name");
 		SortType date = new SortType("date.name", "Date");
 		SortType versionName = new SortType("version.name", "Version Name");
 		SortType versionPlatform = new SortType("version.platform.name", "Version Platform Name");
 		SortType subVersion = new SortType("svn.num", "SVN");
 		SortType performance = new SortType("performance.time", "Performance Time");
 		SortType[] sortable = {name, date, versionName, versionPlatform, subVersion, performance};
 		List<SortType> sortFields = Arrays.asList(sortable);
 		return sortFields;
 	}
 	
 
 	
 	/**
 	 * 
 	 * @return name of the run.
 	 */
 	public String getRunName(){
 		return name;
 	}
 	public Long getRunID() {
 		return id;
 	}
 	/**
 	 * 
 	 * @return all runs.
 	 */
 	public static List<Run> getList(){
 		return find.all();
 	}
 
 	/**
 	 * 
 	 * @return a string with the platform name and the format name.
 	 */
 	public String getPlatformFormat(){
 		return version.platform.getPlatformName() + "\\" + format.getFileFormatName();
 	
 	}
 	/**
 	 * 
 	 * @return a string with the platform name and the version name.
 	 */
 	public String getPlatformVersion(){
 		return version.platform.getPlatformName() + "\\" + version.getVersionName();
 	
 	}
 	/**
 	 * 
 	 * @return a string with the date name.
 	 */
 	public String getDateName() {
 		return date.getDateName();
 	}
 	
 	
 
     /**
 	 * This method calculates how many differences of diffType occured in given run
 	 * @param run Which run to calculate
 	 * @param diffType Which difftpye of run to calculate
 	 * @return Number of differences of diffType in run
 	 */
 	public static int calculateDifferences(Run run, DiffType difftype){
 		return PageOut.calculateDifferences(run,difftype);
 	}
 
 
 	/**
 	 * This method is used to generate a list of runs which share similar characteristics.
 	 * Param1 and Param2 are the data types you are looking for (example: Format or Date)
 	 * Filter1 and Filter2, respectively, are what you are searching for (example: PDF or 12/12/2000)
 	 * @return list generated. 
 	 */
 	public static List<Run> dataSet(String filter1, String filter2, String param1, String param2) {
 		List<Run> list =find.where()
 				.eq(param1, filter1)
 				.eq(param2, filter2)
 				.findList();
 				return list;
 	}
 
 
 
 	
 	/**
 	 * This method calculates how many bugs appeared in a given run
 	 * @param run Which run to calculate
 	 * @return Number of bugs from that run
 	 */
 	public static int calculateBugs(Run run){
 		return Bug.listBugs(run).size();
 	}
	/**
	 * Used to create an object which contains data about a list of runs
	 * @param list the list used to populate the information in call data
	 * @return the calldata object which has information about a list of runs
	 */
 	public static CallData getData(List<Run> list) {
 		return new CallData(list);
 	}
 	
 	/**
 	 * This method calculates if a given date occurs in any current runs in the DB
 	 */
 	public static boolean dateOccurs(Date date){
 		int occurences=find.where()
 				.eq("date",date)
 				.findRowCount();
 		if(occurences>0){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 }

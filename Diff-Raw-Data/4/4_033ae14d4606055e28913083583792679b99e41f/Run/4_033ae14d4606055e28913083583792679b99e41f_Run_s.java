 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
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
 	@JoinColumn(name="Version_ID")
 	public Version version;
 	//Format
 	@Column(name="Format_ID")
 	@OneToOne
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
 	
 	
 	/**
      * Generic query helper for entity Computer with id Long
      */
     public static Finder<Long,Run> find = new Finder<Long,Run>(Long.class, Run.class); 
     
 	
 	/**
      * Return a page of run
      *
      * @param page Page to display
      * @param pageSize Number of computers per page
      * @param sortBy Computer property used for sorting
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
 	 * This method returns the sortable fields of Run class
 	 * 
 	 * @return List of sortable fields by a text box
 	 */
 	public static List<SortType> getSortFields(){
 		SortType name = new SortType("name", "Name");
 		SortType versionName = new SortType("version.name", "Version Name");
 		SortType versionPlatform = new SortType("version.platform.name", "Version Platform Name");
 		SortType subVersion = new SortType("svn.num", "SVN");
 		SortType performance = new SortType("performance.time", "Performance Time");
		SortType[] sortable = {name, versionName, versionPlatform, subVersion, performance};
 		List<SortType> sortFields = Arrays.asList(sortable);
 		return sortFields;
 	}
 	
     
 }

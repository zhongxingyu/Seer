 package models;
 
 import play.*;
 import play.data.validation.InPast;
 import play.data.validation.Max;
 import play.data.validation.Min;
 import play.data.validation.Range;
 import play.data.validation.Required;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 
 import org.hibernate.mapping.Array;
 
 import java.text.DateFormat;
 import java.util.*;
 
 @Entity
 public class Dive extends CommentModel {
 
 	/** date of the dive */
 	@Required @InPast
 	public Date date;
 	
 	/** location of this dive */
 	@ManyToOne
 	public Spot spot;
 
 	public double maxDepth;
 	/** has the value been set ? */
 	public boolean maxDepthSet;
 	
 	@Range(min = 0, max = 3600)
 	public long duration;
 	/** has the value been set ? */
 	public boolean durationSet;	
 	
 	public double airTemperature;
 	/** has the value been set ? */
 	public boolean airTemperatureSet;
 	
 	public double waterSurfaceTemperature;
 	/** has the value been set ? */
 	public boolean waterSurfaceTemperatureSet;
 	
 	public double waterBottomTemperature;
 	/** has the value been set ? */
 	public boolean waterBottomTemperatureSet;
 	
 	@ManyToMany
 	public List<DiveType> type;
 	
 	@ManyToOne
 	public WaterType water;
 	
 	/** was it a dive dedicated to training ? */
 	public boolean training;
 	
 	@ManyToOne
 	public Center center;
 	
 	@ManyToMany(mappedBy = "dives", cascade = CascadeType.ALL)
 	public List<Trip> trips;
 	
 	@ManyToMany
 	public List<Fish> fishes;
 	
 	/** divers in this dive who have an account */
 	@ManyToMany(mappedBy = "dives", cascade = CascadeType.ALL)
 	public List<User> userDivers;
 	
 	
 	@OneToMany(mappedBy="dive", cascade=CascadeType.ALL)
 	public List<Picture> pictures;
 	
 	/** divers in this dive who don't have an account */
 	// TODO
 	//public List<nonUser> otherDivers;
 	
 	public Dive() {
 		pictures = new ArrayList<Picture>();
 		fishes = new ArrayList<Fish>();
 	}
 	
 	/**
 	 * Associate this dive with these fishes
 	 * @param fishIdList : a comma separated integer list of fish ids
 	 */
 	public void addFish(String fishIdList) {
 		
 		Logger.info(fishIdList);
 		
 		// read the string id, interpret them as int
 		String[] idStringArray = fishIdList.split(",");
 		
 		String cleanFishIdList = "";
 		List<Long> ids = new ArrayList<Long>();
 		
 		for (String fishStringid : idStringArray) {
			ids.add( Long.parseLong(fishStringid.trim()) );
 		}
 		
 		// Create a final string for teh query
         StringBuffer buffer = new StringBuffer();
         Iterator iter = ids.iterator();
         while (iter.hasNext()) {
         	buffer.append("'");
             buffer.append(iter.next());
             buffer.append("'");
             if (iter.hasNext()) {
                 buffer.append(",");
             }
         }
         cleanFishIdList = buffer.toString();
 
 
 		this.fishes = Fish.find("select f from Fish f where f.id in (" + cleanFishIdList + ")").fetch();
 	}
 	
 	public String toString() {
 	    return date.toString();
 	}
 }

 package cours.ulaval.glo4003.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class Course {
 
 	public final static int HOURS_PER_CREDITS = 3;
 
 	private String acronym;
 	private String title;
 	private int credits;
 	private String description;
 	private Cycle cycle;
 	private TimeDedicated timeDedicated;
 	private List<Prerequisite> prerequisites = new ArrayList<Prerequisite>();
 
 	public Course() {
 		super();
 	}
 
 	public Course(String acronym, String title, int credits, String description, Cycle cycle, List<Prerequisite> prerequisites,
 			TimeDedicated timeDedicated) {
 		super();
 		this.acronym = acronym;
 		this.title = title;
 		this.credits = credits;
 		this.description = description;
 		this.cycle = cycle;
 		this.prerequisites = prerequisites;
 		this.timeDedicated = timeDedicated;
 	}
 
 	public boolean isConcomitting(Course course) {
		for (Prerequisite prerequisite : prerequisites) {
			if (prerequisite.containsAcronym(course.getAcronym()) && prerequisite.getIsConcomitant()) {
				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getAcronym() {
 		return acronym;
 	}
 
 	public void setAcronym(String acronym) {
 		this.acronym = acronym;
 	}
 
 	public void setCycle(Cycle cycle) {
 		this.cycle = cycle;
 	}
 
 	public Cycle getCycle() {
 		return cycle;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setCredits(int credits) {
 		this.credits = credits;
 	}
 
 	public int getCredits() {
 		return credits;
 	}
 
 	public void setTimeDedicated(TimeDedicated timeDedicated) {
 		this.timeDedicated = timeDedicated;
 	}
 
 	public TimeDedicated getTimeDedicated() {
 		return timeDedicated;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public List<Prerequisite> getPrerequisites() {
 		return prerequisites;
 	}
 
 	public void setPrerequisites(List<Prerequisite> prerequisites) {
 		this.prerequisites = prerequisites;
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return EqualsBuilder.reflectionEquals(this, obj);
 	}
 
 }

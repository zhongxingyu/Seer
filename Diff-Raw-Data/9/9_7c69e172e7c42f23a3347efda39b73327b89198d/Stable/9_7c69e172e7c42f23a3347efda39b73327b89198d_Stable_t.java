 package ejb.stablepkg;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="Stable")
 public class Stable implements Serializable{
 	
 	
 	
 	private long stableNumber;
 	private String stableName;
 	private Set<Horse> horses;
 	
 	@Id
	@Column(name="stableNumber")
 	public long getStableNumber() {
 		return stableNumber;
 	}
 	public void setStableNumber(long stableNumber) {
 		this.stableNumber = stableNumber;
 	}
 	
 	@Column(name="stableName")
 	public String getStableName() {
 		return stableName;
 	}
 	public void setStableName(String stableName) {
 		this.stableName = stableName;
 	}
 	
 	@OneToMany(mappedBy="stable", fetch=FetchType.EAGER)
 	public Set<Horse> getHorses() {
 		return horses;
 	}
 	public void setHorses(Set<Horse> horses) {
 		this.horses = horses;
 	}
 	
 }

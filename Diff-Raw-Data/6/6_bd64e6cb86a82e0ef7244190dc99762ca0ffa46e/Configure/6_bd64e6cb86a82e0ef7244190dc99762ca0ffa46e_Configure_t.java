 package klim.orthodox_calendar;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class Configure {
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Long id;
 	
 	@Persistent
 	private Integer daysForward;
 
 	@Persistent
 	private Integer daysToStore;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Integer getDaysForward() {
 		return daysForward;
 	}
 
 	public void setDaysForward(Integer daysForward) {
 		this.daysForward = daysForward;
 	}
 
 	public Integer getDaysToStore() {
 		return daysToStore;
 	}
 
 	public void setDaysToStore(Integer daysToStore) {
 		this.daysToStore = daysToStore;
 	}
 	
 	public Configure(int forward, int store) {
 		this.daysForward=forward;
 		this.daysToStore=store;
 	}
 }

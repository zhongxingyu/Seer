 package ejb.stablepkg;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="Horse")
 public class Horse implements Serializable{
 	
 	private static final long serialVersionUID = 7044414250696714325L;
 	
 	private long horseNumber;
 	private String name;
 	private String color;
 	private Stable stable;
 	
 	@Id
 	@Column(name="horseNumber")
 	public long getHorseNumber() {
 		return horseNumber;
 	}
 	public void setHorseNumber(long horseNumber) {
 		this.horseNumber = horseNumber;
 	}
 	
 	@Column(name="name")
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Column(name="color")
 	public String getColor() {
 		return color;
 	}
 	public void setColor(String color) {
 		this.color = color;
 	}
 	
 	@ManyToOne
	@JoinColumn(name ="stableNumber", referencedColumnName ="stableNumber")
 	public Stable getStable() {
 		return stable;
 	}
 	public void setStable(Stable stable) {
 		this.stable = stable;
 	}
 	
 	
 }

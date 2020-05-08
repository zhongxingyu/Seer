 package models;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 @Entity
 @SequenceGenerator(name = "target_seq", initialValue = 1)
 public class Target extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "target_seq")
 	public Long id;
 
	@Column(unique = true)
 	@Required
 	public String targetTag;
 
 	@Required
 	public String targetName;
 
 	public String comments;
 
 	@ManyToOne
 	public Account account;
 
 	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 	public List<Point> points;
 
 	public static Finder<Long, Target> find = new Finder<Long, Target>(
 			Long.class, Target.class);
 
 	public boolean isExist() {
		Target target = find.where().eq("targetTag", targetTag).findUnique();
 		return (target == null ? false : true);
 	}
 
 	public Target findByTargetTag() {
		return find.where().eq("targetTag", targetTag).findUnique();
 	}
 
 }

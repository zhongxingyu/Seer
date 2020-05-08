 package au.org.scoutmaster.domain;
 
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 /**
  * A Role within the Scout Group.
  * 
  * Typical Roles are 'Scout Leader', "President', 'Youth Member'.
  * 
  * The system has a number of built in roles which may not be modified or deleted 
  * but an administrator can add additional roles.
  * 
  * @author bsutton
  *
  */
 @Entity(name="GroupRole")
 @Table(name="GroupRole")
 @Access(AccessType.FIELD)
 
 @NamedQueries(
 {
 		@NamedQuery(name = GroupRole.FIND_BY_NAME, query = "SELECT grouprole FROM GroupRole grouprole WHERE grouprole.name like :name") 
 })
 
 public class GroupRole extends BaseEntity
 {
 	private static final long serialVersionUID = 1L;
 	
 	static public final String FIND_BY_NAME = "GroupRole.findByName";
 
 	/**
 	 * Note: this enum is bound to the field 'enumName' changing any of these names will break the database linkage.
 	 * 
 	 * @author bsutton
 	 *
 	 */
 	
 	public enum BuiltIn
 	{
		None, YouthMember, Parent, Guardian, SectionHelper, Volunteer, Leaders, AssistantLeader, President, Secretary, Treasurer, QuarterMaster, GroupLeader, CommitteeMember, CouncilMember, RecruitmentOfficer, Custom
 	
 	}
 
 
 	/**
 	 * Name of the section.
 	 */
 	@Column(unique=true)
 	private String name;
 	
 	
 	/**
 	 * If a group role is one of the built in roles then this field
 	 * will provide a link between the role and the BuiltIn enum.
 	 * Non-builtin roles will use the Builtin type of 'Other'.
 	 */
 	@Enumerated(EnumType.STRING)
 	private BuiltIn enumName;
 	
 
 	/**
 	 * Name of the section.
 	 */
 	@Column(unique=true)
 	private String description;
 	
 	
 	/**
 	 * The set of tags that should be added to a contact when this role is
 	 * assigned to the contact.
 	 */
	@ManyToMany(targetEntity=Tag.class, fetch=FetchType.EAGER)
 	private Set<Tag> tags = new HashSet<Tag>();
 
 
 	public Set<Tag> getTags()
 	{
 		return tags;
 	}
 
 
 	public String getName()
 	{
 		return name;
 	}
 
 
 	public BuiltIn getBuiltIn()
 	{
 		return enumName;
 	}
 
 
 	public String getDescription()
 	{
 		return description;
 	}
 	
 	public String toString()
 	{
 		return name;
 	}
 	
 }

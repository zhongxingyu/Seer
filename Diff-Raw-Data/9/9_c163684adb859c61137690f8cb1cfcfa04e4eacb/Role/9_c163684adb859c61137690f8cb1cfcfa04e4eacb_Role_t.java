 package no.niths.domain.security;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import no.niths.common.AppConstants;
 import no.niths.domain.Domain;
 import no.niths.domain.Student;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @XmlRootElement
 @Entity
 @Table(name = AppConstants.ROLES)
@XmlAccessorType(XmlAccessType.FIELD)
 @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
 public class Role implements Domain {
 	
 	@Transient
 	private static final long serialVersionUID = 3089189542515256814L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	
 	@Column(unique = true, name="role_name")
 	@XmlElement(name="rolename")
 	private String roleName;
 
 	@JsonIgnore
 	@XmlTransient
 	@Transient
 	@SuppressWarnings("unused")
 	private String trimedRoleName;
 	
 	@JsonIgnore
	@XmlTransient
 	@ManyToMany(fetch = FetchType.LAZY, targetEntity = Student.class)
 	@JoinTable(name = "students_roles", 
 		joinColumns = @JoinColumn(name = "roles_id"), 
 		inverseJoinColumns = @JoinColumn(name = "students_id"))
 	@Cascade(CascadeType.ALL)
 	private List<Student> students = new ArrayList<Student>();
 	
 	public Role(){
 		this(null);
 		setStudents(null);
 		
 	}
 	
 	public Role(String roleName){
 		this.roleName = roleName;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getRoleName() {
 		return roleName;
 	}
 
 	public void setRoleName(String roleName) {
 		this.roleName = roleName;
 	}
 	
 	@Override
 	public boolean equals( final Object obj ){
 		if( this == obj )
 			return true;
 		if( obj == null )
 			return false;
 		if( this.getClass() != obj.getClass() )
 			return false;
 		final Role other = (Role) obj;
 		if(roleName.equals(other.roleName)){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString() {
 		return "Role: " + roleName;
 	}
 
 	public String getTrimedRoleName() {
 		String role = roleName.replace("ROLE_", "");
 		role = role.charAt(0) + role.substring(1, role.length()).toLowerCase() +"";
 		return role;
 	}
 	
	
 	public List<Student> getStudents() {
 		return students;
 	}
 
 	public void setStudents(List<Student> students) {
 		this.students = students;
 	}
 
 }

 package usersManagement;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import businessLayer.Department;
 
 /**
  * Entity implementation class for Entity: User
  * 
  */
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "serialNumber" }))
 @NamedQueries({ @NamedQuery(name = "User.findBySerialNumber", query = "SELECT u FROM User U where u.serialNumber= :number ") })
 public class User implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private int id;
 
 	private String email;
 	private String name;
 	private String surname;
 
 	@ManyToOne
 	private Department department;
 
 	private String serialNumber;
 
 	// @Enumerated(EnumType.STRING)
 	// private RolePermission rolePermission;
 
 	@OneToMany(cascade = { CascadeType.REMOVE, CascadeType.PERSIST })
 	private List<Role> roles;
 
 
	public User() {
		this.roles = new ArrayList<>();
	}
 
 
 	public User(String name, String surname, Department department, String serialNumber) {
 		super();
 		this.name = name;
 		this.surname = surname;
 		this.department = department;
 		this.serialNumber = serialNumber;
		this.roles = new ArrayList<>();
 	}
 
 
 	public boolean hasRolePermission(RolePermission rolePermission) {
 		boolean found = false;
 		Iterator<Role> it = this.roles.iterator();
 
 		while (!found && it.hasNext()) {
 			found = it.next().hasRolePermission(rolePermission);
 		}
 
 		return found;
 	}
 
 
 	public String getEmail() {
 		return email;
 	}
 
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 
 	public List<Role> getRoles() {
 		return new ArrayList<>(roles);
 	}
 
 
 	public void addRole(Role role) {
 		if (role != null) {
 			this.roles.add(role);
 		}
 	}
 
 
 	public boolean removeRole(Role role) {
 		if (null == role) {
 			return false;
 		}
 		return this.roles.remove(role);
 	}
 
 
 	public int getId() {
 		return id;
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public String getSurname() {
 		return surname;
 	}
 
 
 	public Department getDepartment() {
 		return department;
 	}
 
 
 	public void setDepartment(Department department) {
 		this.department = department;
 	}
 
 
 	public List<String> getBelongingDeptsCodes() {
 		return null;
 		// List<String> result = new ArrayList<>();
 		// for (Department d : belongingDepts) {
 		// result.add(d.getCode());
 		// }
 		// return result;
 	}
 
 
 	@Override
 	public String toString() {
 		return "User [id=" + id + ", email=" + email + ", name=" + name + ", surname=" + surname + ", department=" + department + ", serialNumber="
 				+ serialNumber + ", roles=" + roles + "]";
 	}
 
 
 	public void copy(User copy) {
 		this.email = copy.getEmail();
 		this.name = copy.getName();
 		this.surname = copy.getSurname();
 		this.department = copy.getDepartment();
 		this.serialNumber = copy.getSerialNumber();
 		this.roles = copy.getRoles();
 	}
 
 }

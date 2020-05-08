 package usersManagement;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
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
 
 	// c'era indecisione sul numero di dipartimenti che può essere associato ad
 	// un utente (se uno o più d'uno). Attualmente ogni utente è associato ad un
 	// solo dipartimento, ma abbiamo mantenuto la lista per rendere un'eventuale
 	// modifica più semplice in futuro
 	@ManyToMany
 	private List<Department> belongingDepts;
 
 	private String serialNumber;
 
 	@Enumerated(EnumType.STRING)
 	private Role role;
 
 
 	public User() {
 		super();
 		this.belongingDepts = new ArrayList<>();
 	}
 
 
 	public boolean hasRole(Role role) {
 		return (role == this.role) ? true : false;
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
 
 
 	public void setSerialNumber(String serialNumber) {
 		this.serialNumber = serialNumber;
 	}
 
 
 	public Role getRole() {
 		return role;
 	}
 
 
 	public void setRole(Role role) {
 		this.role = role;
 	}
 
 
 	public int getId() {
 		return id;
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	public String getSurname() {
 		return surname;
 	}
 
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 
 	public Department getDepartment() {
 		return belongingDepts.get(0);
 	}
 
 
 	public void addDepartment(Department d) {
		if (belongingDepts.isEmpty()) {
			belongingDepts.add(d);
		}
		else {
			belongingDepts.set(0, d);
		}
 	}
 
 
 	public List<String> getBelongingDeptsCodes() {
 		List<String> result = new ArrayList<>();
 		for (Department d : belongingDepts) {
 			result.add(d.getCode());
 		}
 		return result;
 	}
 
 
 	@Override
 	public String toString() {
 		return "User [id=" + id + ", email=" + email + ", name=" + name + ", surname=" + surname + ", belongingDepts=" + belongingDepts
 				+ ", serialNumber=" + serialNumber + ", role=" + role + "]";
 	}
 
 }

 package no.niths.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
 
 import org.hibernate.validator.constraints.Email;
 
 @Entity
 @Table(name = AppConstants.STUDENTS)
 @XmlRootElement
 public class Student implements Serializable {
 
 	@Transient
 	private static final long serialVersionUID = 8441269238845961513L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 
 	@Column(name = "first_name")
 	@NotNull
 	@Size(min = 1, max = 55, message = "Must be minimun 1 char and max 55 chars")
 	private String firstName;
 
 	@Column(name = "last_name")
 	@NotNull
 	@Size(min = 1, max = 55, message = "Must be minimun 1 char and max 55 chars")
 	private String lastName;
 	
 	
 	@Column
 	@Pattern(regexp = "M|F", message = "Must be M=male, or F=female")
 	private String sex;
 
 	@Column
 	@Past
 	private Date birthday;
 	
 	@Column
	@Max(3)
	@Min(1)
	//@Pattern(regexp = "1|2|3", message = "Must be 1, 2 or 3")	
 	private Integer grade;
 
 	@Column
 	@Email(message = "Not a valid email")
 	private String email;
 
 	@Column(name = "phone_number")
 	@Pattern(regexp = "(^$)|([0-9]{8})", message = "Not a valid number")
 	private String telephoneNumber;
 
 	@Column
 	@Size(min = 0, max = 255, message = "Can not be more then 255 chars")
 	private String description;
 
 	@ManyToMany(fetch = FetchType.LAZY)
 	private List<Course> courses;
 
 	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "members")
 	private List<Committee> committees;
 
 	public Student() {
 		this("", "", "");
 	}
 
 	public Student(String firstName, String lastName) {
 		this(firstName, lastName, "");
 	}
 
 	public Student(String firstName, String lastName, String email) {
 		this(firstName, lastName, "M" , 1, email, "", "");
 	}
 
 	public Student(String firstName, String lastName, String sex, Integer grade, String email,
 		String telephoneNumber, String description) {
 		setFirstName(firstName);
 		setLastName(lastName);
 		setEmail(email);
 		setTelephoneNumber(telephoneNumber);
 		setDescription(description);
 		setCourses(new ArrayList<Course>());
 		setCommittees(new ArrayList<Committee>());
 		setSex(sex);
 		setGrade(grade);
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	
 	public Date getBirthday() {
 		return birthday;
 	}
 
 	public void setBirthday(Date birthday) {
 		this.birthday = birthday;
 	}
 
 	public String getSex() {
 		return sex;
 	}
 
 	public void setSex(String sex) {
 		this.sex = sex;
 	}
 
 	public Integer getGrade() {
 		return grade;
 	}
 
 	public void setGrade(Integer grade) {
 		this.grade = grade;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getTelephoneNumber() {
 		return telephoneNumber;
 	}
 
 	public void setTelephoneNumber(String telephoneNumber) {
 		this.telephoneNumber = telephoneNumber;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List<Course> getCourses() {
 		return courses;
 	}
 
 	public void setCourses(List<Course> courses) {
 		this.courses = courses;
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Student)) {
 			return false;
 		}
 		Student s = (Student) obj;
 		return s == this ? true : s.getId() == id ? true : false;
 	}
 
 	public List<Committee> getCommittees() {
 		return committees;
 	}
 
 	public void setCommittees(List<Committee> committees) {
 		this.committees = committees;
 	}
 
 	@Override
 	public String toString() {
 		String s = "{\"name\":\"" + firstName + "\",\"id\"";
 
 		return s;
 	}
 }

 package no.niths.domain;
 
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
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import no.niths.common.AppConstants;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 
 @Entity
 @Table(name=AppConstants.COURSES)
 @XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
 @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
 public class Course implements Domain {
 
 	@Transient
 	private static final long serialVersionUID = -1898014630305240760L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     
     @Column(unique=true)
     @Size(min = 3, max = 30, message ="The length of the name must be between 3 to 30 letters")
     private String name;
 
     @Column(length=500)
     @Size(max = 500, message ="The length of the description must not exceed 500 letters")
     private String description;
     
     @ManyToMany(fetch = FetchType.LAZY)
     @Cascade(CascadeType.ALL)
 	private List<Subject> subjects = new ArrayList<Subject>();
 	
     @JsonIgnore
     @XmlTransient
     @ManyToMany(fetch = FetchType.LAZY)
 	@JoinTable(name = "students_courses", 
 		joinColumns = @JoinColumn(name = "courses_id"), 
 		inverseJoinColumns = @JoinColumn(name = "students_id"))
     @Cascade(CascadeType.ALL)
     private List<Student> students = new ArrayList<Student>();
     
     @OneToMany(fetch = FetchType.LAZY)
 	@JoinTable(name = "courses_representatives", 
 		joinColumns = @JoinColumn(name = "courses_id"), 
 		inverseJoinColumns = @JoinColumn(name = "representatives_id"),
 		uniqueConstraints = @UniqueConstraint(columnNames = {"courses_id","representatives_id"}))
     @Cascade(CascadeType.ALL)
     private List<Student> courseRepresentatives = new ArrayList<Student>();
 
     public Course() {
         this(null, null, null);
         setStudents(null);
         setSubjects(null);
         setCourseRepresentatives(null);
     }
 
     public Course(String name, String description) {
         this(null, name, description);
     }
 
     public Course(Long id, String name, String description) {
         this.id = id;
         this.name = name;
         this.description = description;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public List<Subject> getSubjects() {
 		return subjects;
 	}
 
 	public void setSubjects(List<Subject> subject) {
 		this.subjects = subject;
 	}
 
 	public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
 	@Override
     public boolean equals(Object that) {
     	if(!(that instanceof Course)) return false;
         Course course = (Course) that;
 
         return course == this ? true : course.getId() == id
                 ? true : false;
     }
 
     @Override
     public String toString() {
         return String.format("[%s][%s][%s]", id, name, description);
     }
 
 	public List<Student> getStudents() {
 		return students;
 	}
 
 	public void setStudents(List<Student> students) {
 		this.students = students;
 	}
 
 	public List<Student> getCourseRepresentatives() {
 		return courseRepresentatives;
 	}
 
 	public void setCourseRepresentatives(List<Student> courseRepresentatives) {
 		this.courseRepresentatives = courseRepresentatives;
 	}
 }

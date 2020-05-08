 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entities;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author Ashish
  */
 @Entity
 @Table(name = "current_student")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "CurrentStudent.findAll", query = "SELECT c FROM CurrentStudent c"),
     @NamedQuery(name = "CurrentStudent.findByIdCurrentStudent", query = "SELECT c FROM CurrentStudent c WHERE c.idCurrentStudent = :idCurrentStudent"),
     @NamedQuery(name = "CurrentStudent.findBySemester", query = "SELECT c FROM CurrentStudent c WHERE c.semester = :semester"),
    @NamedQuery(name = "CurrentStudent.findUltimate", query = "SELECT c FROM CurrentStudent c WHERE c.semester = :semester AND c.division = :division AND c.batch = :batch AND c.idCourse = :idCourse"),
    @NamedQuery(name = "CurrentStudent.findUltimateTheory", query = "SELECT c FROM CurrentStudent c WHERE c.semester = :semester AND c.division = :division AND c.idCourse = :idCourse"),
     @NamedQuery(name = "CurrentStudent.findByDivision", query = "SELECT c FROM CurrentStudent c WHERE c.division = :division"),
     @NamedQuery(name = "CurrentStudent.findByBatch", query = "SELECT c FROM CurrentStudent c WHERE c.batch = :batch"),
     @NamedQuery(name = "CurrentStudent.findByRollNo", query = "SELECT c FROM CurrentStudent c WHERE c.rollNo = :rollNo"),
     @NamedQuery(name = "CurrentStudent.findByProvisional", query = "SELECT c FROM CurrentStudent c WHERE c.provisional = :provisional"),
     @NamedQuery(name = "CurrentStudent.findByAcademicYear", query = "SELECT c FROM CurrentStudent c WHERE c.academicYear = :academicYear")})
 public class CurrentStudent implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id  
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id_current_student")
     private Integer idCurrentStudent;
     @Basic(optional = false)
     @NotNull
     @Column(name = "semester")
     private short semester;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 1)
     @Column(name = "division")
     private String division;
     @Column(name = "batch")
     private Short batch;
     @Size(max = 10)
     @Column(name = "roll_no")
     private String rollNo;
     @Basic(optional = false)
     @NotNull
     @Column(name = "provisional")
     private boolean provisional;
     @Basic(optional = false)
     @NotNull
     @Column(name = "academic_year")
     @Temporal(TemporalType.DATE)
     private Date academicYear;
     @JoinColumn(name = "id_course", referencedColumnName = "id_course")
     @ManyToOne(optional = false)
     private Course idCourse;
     @JoinColumn(name = "admn_no", referencedColumnName = "admn_no")
     @OneToOne(optional = false)
     private Student admnNo;
     @OneToOne(mappedBy = "idCurrentStudent")
     private Attendance attendance;
     @Transient
     private boolean selected;
     
     public boolean isSelected () {
         return selected;
     }
     
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
     
     
 
     public CurrentStudent() {
     }
 
     public CurrentStudent(Integer idCurrentStudent) {
         this.idCurrentStudent = idCurrentStudent;
     }
 
     public CurrentStudent(Integer idCurrentStudent, short semester, String division, boolean provisional, Date academicYear) {
         this.idCurrentStudent = idCurrentStudent;
         this.semester = semester;
         this.division = division;
         this.provisional = provisional;
         this.academicYear = academicYear;
     }
 
             public Integer getIdCurrentStudent() {
         return idCurrentStudent;
     }
 
     public void setIdCurrentStudent(Integer idCurrentStudent) {
         this.idCurrentStudent = idCurrentStudent;
     }
 
     public short getSemester() {
         return semester;
     }
 
     public void setSemester(short semester) {
         this.semester = semester;
     }
 
     public String getDivision() {
         return division;
     }
 
     public void setDivision(String division) {
         this.division = division;
     }
 
     public Short getBatch() {
         return batch;
     }
 
     public void setBatch(Short batch) {
         this.batch = batch;
     }
 
     public String getRollNo() {
         return rollNo;
     }
 
     public void setRollNo(String rollNo) {
         this.rollNo = rollNo;
     }
 
     public boolean getProvisional() {
         return provisional;
     }
 
     public void setProvisional(boolean provisional) {
         this.provisional = provisional;
     }
 
     public Date getAcademicYear() {
         return academicYear;
     }
 
     public void setAcademicYear(Date academicYear) {
         
         this.academicYear = academicYear;
     }
 
     public Course getIdCourse() {
         return idCourse;
     }
 
     public void setIdCourse(Course idCourse) {
         this.idCourse = idCourse;
     }
 
     public Student getAdmnNo() {
         return admnNo;
     }
 
     public void setAdmnNo(Student admnNo) {
         this.admnNo = admnNo;
     }
 
     public Attendance getAttendance() {
         return attendance;
     }
 
     public void setAttendance(Attendance attendance) {
         this.attendance = attendance;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idCurrentStudent != null ? idCurrentStudent.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof CurrentStudent)) {
             return false;
         }
         CurrentStudent other = (CurrentStudent) object;
         if ((this.idCurrentStudent == null && other.idCurrentStudent != null) || (this.idCurrentStudent != null && !this.idCurrentStudent.equals(other.idCurrentStudent))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entities.CurrentStudent[ idCurrentStudent=" + idCurrentStudent + " ]";
     }
     
 }

 package amu.licence.edt.model.beans;
 
 import java.awt.Color;
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.*;
 
 @Entity
 @Table (name="T_TU")
 public class TU implements Serializable {
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column (name="ID_TU")
     private int id;
 
     @Column (name="CODE_TU", nullable=false, unique=true)
     private String code;
 
     @Column (name="LIBEL_TU")
     private String libel;
 
     @Column (name="COLOR_TU")
     private Color color;
 
     @Column (name="CLASS_HOURS")
     private int classHours;
 
     @Column (name="TUTOR_HOURS")
     private int tutorHours;
 
     @Column (name="PRACTICAL_HOURS")
     private int practicalHours;
 
     @Column (name="PROJECT_HOURS")
     private int projectHours;
 
     @ManyToOne
     @JoinColumn (name="ID_LEVEL", nullable=false)
     private Level level;
 
     @ManyToOne
     @JoinColumn (name="ID_RESP_TEACHER", nullable=false)
     private Teacher resp;
 
     @ManyToMany (fetch=FetchType.LAZY)
     @JoinTable (name="T_COMPETENT_TEACHER",
                joinColumns={@JoinColumn(name="ID_TEACHER")},
                inverseJoinColumns={@JoinColumn(name="ID_TU")})
     private Set<Teacher> competentTeachers;
 
     public TU() {}
 
     public TU(String libelle, Color couleur) {
         this.libel = libelle;
         this.color = couleur;
     }
 
     public TU(String code, String libel, Color color, int classHours,
             int tutorHours, int practicalHours, int projectHours, Level level,
             Teacher resp, Set<Teacher> competentTeachers) {
         super();
         this.code = code;
         this.libel = libel;
         this.color = color;
         this.classHours = classHours;
         this.tutorHours = tutorHours;
         this.practicalHours = practicalHours;
         this.projectHours = projectHours;
         this.level = level;
         this.resp = resp;
         this.competentTeachers = competentTeachers;
     }
 
     @Override
     public String toString() {
         return "TU [id=" + id + ", code=" + code + ", libel=" + libel
                 + ", color=" + color + ", classHours=" + classHours
                 + ", tutorHours=" + tutorHours + ", practicalHours="
                 + practicalHours + ", projectHours=" + projectHours
                 + ", level=" + level + ", resp=" + resp
                 + ", competentTeachers=" + competentTeachers + "]";
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + id;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         TU other = (TU) obj;
         if (id != other.id)
             return false;
         return true;
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public String getLibel() {
         return libel;
     }
 
     public void setLibel(String libel) {
         this.libel = libel;
     }
 
     public Color getColor() {
         return color;
     }
 
     public void setColor(Color color) {
         this.color = color;
     }
 
     public int getClassHours() {
         return classHours;
     }
 
     public void setClassHours(int classHours) {
         this.classHours = classHours;
     }
 
     public int getTutorHours() {
         return tutorHours;
     }
 
     public void setTutorHours(int tutorHours) {
         this.tutorHours = tutorHours;
     }
 
     public int getPracticalHours() {
         return practicalHours;
     }
 
     public void setPracticalHours(int practicalHours) {
         this.practicalHours = practicalHours;
     }
 
     public int getProjectHours() {
         return projectHours;
     }
 
     public void setProjectHours(int projectHours) {
         this.projectHours = projectHours;
     }
 
     public Level getLevel() {
         return level;
     }
 
     public void setLevel(Level level) {
         this.level = level;
     }
 
     public Teacher getResp() {
         return resp;
     }
 
     public void setResp(Teacher resp) {
         this.resp = resp;
     }
 
     public Set<Teacher> getCompetentTeachers() {
         return competentTeachers;
     }
 
     public boolean addCompetentTeacher(Teacher teacher) {
         return this.competentTeachers.add(teacher);
     }
 
     public boolean removeCompetentTeacher(Teacher teacher) {
         return this.competentTeachers.remove(teacher);
     }
 
     public int getId() {
         return id;
     }
 
 }

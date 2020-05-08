 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.entities;
 
 import java.io.Serializable;
 import java.util.Collection;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author cyril
  */
 @Entity
 @Table(name = "userstory")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Userstory.findAll", query = "SELECT u FROM Userstory u"),
    @NamedQuery(name = "Userstory.findAllOrderByImportance", query = "SELECT u FROM Userstory u ORDER BY u.importance DESC"),
     @NamedQuery(name = "Userstory.findByIdUserstory", query = "SELECT u FROM Userstory u WHERE u.idUserstory = :idUserstory"),
     @NamedQuery(name = "Userstory.findByTitle", query = "SELECT u FROM Userstory u WHERE u.title = :title"),
     @NamedQuery(name = "Userstory.findByImportance", query = "SELECT u FROM Userstory u WHERE u.importance = :importance"),
     @NamedQuery(name = "Userstory.findByEstimation", query = "SELECT u FROM Userstory u WHERE u.estimation = :estimation"),
     @NamedQuery(name = "Userstory.findByCategory", query = "SELECT u FROM Userstory u WHERE u.category = :category"),
     @NamedQuery(name = "Userstory.quickSearchSimple", query = "SELECT u FROM Userstory u WHERE u.title like :pattern or u.demonstration like :pattern or u.note like :pattern or u.category like :pattern"),
     @NamedQuery(name = "Userstory.quickSearchExact", query = "SELECT u FROM Userstory u WHERE u.title = :pattern or u.demonstration = :pattern or u.note = :pattern or u.category = :pattern")})
 public class Userstory implements Serializable {
     @Column(name = "importance")
     private Integer importance;
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id_userstory")
     private Integer idUserstory;
     @Size(max = 100)
     @Column(name = "title")
     private String title;
     @Size(max = 20)
     @Column(name = "estimation")
     private String estimation;
     @Lob
     @Size(max = 65535)
     @Column(name = "demonstration")
     private String demonstration;
     @Lob
     @Size(max = 65535)
     @Column(name = "note")
     private String note;
     @Size(max = 45)
     @Column(name = "category")
     private String category;
     @OneToMany(mappedBy = "idUserstory")
     private Collection<Task> taskCollection;
     @JoinColumn(name = "id_project", referencedColumnName = "id_project")
     @ManyToOne
     private Project idProject;
 
     public Userstory() {
     }
 
     public Userstory(Integer idUserstory) {
         this.idUserstory = idUserstory;
     }
 
     public Integer getIdUserstory() {
         return idUserstory;
     }
 
     public void setIdUserstory(Integer idUserstory) {
         this.idUserstory = idUserstory;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getEstimation() {
         return estimation;
     }
 
     public void setEstimation(String estimation) {
         this.estimation = estimation;
     }
 
     public String getDemonstration() {
         return demonstration;
     }
 
     public void setDemonstration(String demonstration) {
         this.demonstration = demonstration;
     }
 
     public String getNote() {
         return note;
     }
 
     public void setNote(String note) {
         this.note = note;
     }
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     @XmlTransient
     public Collection<Task> getTaskCollection() {
         return taskCollection;
     }
 
     public void setTaskCollection(Collection<Task> taskCollection) {
         this.taskCollection = taskCollection;
     }
 
     public Project getIdProject() {
         return idProject;
     }
 
     public void setIdProject(Project idProject) {
         this.idProject = idProject;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idUserstory != null ? idUserstory.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Userstory)) {
             return false;
         }
         Userstory other = (Userstory) object;
         if ((this.idUserstory == null && other.idUserstory != null) || (this.idUserstory != null && !this.idUserstory.equals(other.idUserstory))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.scrumble.server.entities.Userstory[ idUserstory=" + idUserstory + " ]";
     }
 
     public Integer getImportance() {
         return importance;
     }
 
     public void setImportance(Integer importance) {
         this.importance = importance;
     }
     
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package spockdataaccess.entity;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import javax.ejb.EJBException;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 
 /**
  *
  * @author Loren Chorley
  */
 @Entity
@Table(name = "Environments")
 public class Environment implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     private String id;
     @NotNull
     protected Boolean isActive;
     @ManyToMany(mappedBy="environments")
     protected Collection<Experiment> experiments;
     @NotNull
     protected String CodeURL;
     @NotNull
     protected String DataURL;
     
    public Environment() {
        experiments = new ArrayList<Experiment>();
    }
    
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public Boolean getIsActive() {
         return isActive;
     }
 
     public void setIsActive(Boolean isActive) {
         this.isActive = isActive;
     }
 
     public Collection<Experiment> getExperiments() {
         return experiments;
     }
     
     public void addExperiment(Experiment experiment) {
         experiments.add(experiment);
     }
     
     public String getCodeURL() {
         return CodeURL;
     }
 
     public void setCodeURL(String CodeURL) {
         this.CodeURL = CodeURL;
     }
 
     public String getDataURL() {
         return DataURL;
     }
 
     public void setDataURL(String DataURL) {
         this.DataURL = DataURL;
     }
     
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Environment)) {
             return false;
         }
         Environment other = (Environment) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "spockdataaccess.entity.Environment[ id=" + id + " ]";
     }
     
 }

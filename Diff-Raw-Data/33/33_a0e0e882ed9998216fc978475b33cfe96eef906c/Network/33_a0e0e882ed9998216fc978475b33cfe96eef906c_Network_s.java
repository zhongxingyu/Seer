 package spockdataaccess.entity;
 
 import java.io.Serializable;
 import java.util.Collection;
 import javax.persistence.CascadeType;
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
 @Table(name = "Networks")
@NamedQuery(name = "findAllNetworks", query = "SELECT n FROM Network n")
 public class Network implements Serializable {
     private static final long serialVersionUID = 1L;
     
     @Id
     private String id;
     @NotNull
     protected Boolean isActive;
     @ManyToMany(mappedBy="networks")
     protected Collection<Experiment> experiments;
 
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
     
     
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Network)) {
             return false;
         }
         Network other = (Network) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "spockdataaccess.entity.Network[ id=" + id + " ]";
     }
     
 }

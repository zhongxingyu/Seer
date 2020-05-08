 package spockdataaccess.entity;
 
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
 @Table(name = "Experiments")
@NamedQuery(name = "findAllExperiments", query = "SELECT e FROM Experiment e")
 public class Experiment implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     private String id;
     @NotNull
     protected Boolean isActive;
     @ManyToMany
     protected Collection<Network> networks;
     @ManyToMany
     protected Collection<Environment> environments;
     
     public Experiment() {
         networks = new ArrayList<Network>();
     }
     
     public Experiment(String id, boolean isActive) {
         this.id = id;
         this.isActive = isActive;
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
 
     public void setIsActive(Boolean IsActive) {
         this.isActive = IsActive;
     }
 
     public Collection<Network> getNetworks() {
         return networks;
     }
     
     public void addNetwork(Network network) {
         if (networks.contains(network)) {
             throw new EJBException("Experiment threw: Already contains network " + network.getId());
         }
         networks.add(network);
     }
     
     public void removeNetwork(Network network) {
         if (!networks.contains(network)) {
             throw new EJBException("Experiment threw: Does not contain network " + network.getId());
         }
         networks.remove(network);
     }
     
     public void addEnvironment(Environment environment) {
         if (environments.contains(environment)) {
             throw new EJBException("Experiment threw: Already contains environment " + environment.getId());
         }
         environments.add(environment);
     }
     
     public void removeEnvironment(Environment environment) {
         if (!environments.contains(environment)) {
             throw new EJBException("Experiment threw: Does not contain environment " + environment.getId());
         }
         environments.remove(environment);
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
         if (!(object instanceof Experiment)) {
             return false;
         }
         Experiment other = (Experiment) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "spockdataaccess.entity.Experiment[ id=" + id + " ]";
     }
     
 }

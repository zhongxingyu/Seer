 package fr.cg95.cvq.business.payment.external;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 
 import org.apache.commons.lang.StringUtils;
 
 import fr.cg95.cvq.business.users.external.MappingState;
 
 @Entity
 @Table(name="external_home_folder")
 public class ExternalHomeFolder {
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
 
     @Column(name="mapping_state",nullable=false)
     @Enumerated(EnumType.STRING)
     private MappingState mappingState;
 
     @Column(name="external_id")
     private String externalId;
 
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="external_application_id")
     private ExternalApplication externalApplication;
 
     private String address;
 
    @OneToMany(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
     @JoinColumn(name="external_home_folder_id")
     @OrderColumn(name="external_home_folder_index")
     private List<ExternalIndividual> individuals = new ArrayList<ExternalIndividual>();
 
     public ExternalHomeFolder() {
     }
 
     public ExternalHomeFolder(String externalId, ExternalApplication externalApplication,
             String address) {
         this.externalId = externalId;
         this.externalApplication = externalApplication;
         this.address = address;
         this.mappingState = MappingState.FREE;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public MappingState getMappingState() {
         return mappingState;
     }
 
     public void setMappingState(MappingState mappingState) {
         this.mappingState = mappingState;
     }
 
     public String getExternalId() {
         return externalId;
     }
 
     public void setExternalId(String externalId) {
         this.externalId = externalId;
     }
 
     public ExternalApplication getExternalApplication() {
         return externalApplication;
     }
 
     public void setExternalApplication(ExternalApplication externalApplication) {
         this.externalApplication = externalApplication;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = StringUtils.upperCase(address);
     }
 
     public List<ExternalIndividual> getIndividuals() {
         return individuals;
     }
 
     public void setIndividuals(List<ExternalIndividual> individuals) {
         this.individuals = individuals;
     }
 
     public ExternalIndividual getResponsible() {
         for(ExternalIndividual ei : individuals)
             if (ei.isResponsible())
                 return ei;
         return null;
     }
 
     public String getCompositeIdForMapping() {
         return externalApplication.getLabel() + ":"+externalId;
     }
 
     @Override
     public boolean equals(Object object) {
         if (this == object)
             return true;
         if (!(object instanceof ExternalHomeFolder))
             return false;
         ExternalHomeFolder eh = (ExternalHomeFolder) object;
         return (this.externalApplication.getLabel().equals(eh.getExternalApplication().getLabel())
                 && (this.externalId.equals(eh.getExternalId())));
     }
 
     @Override
     public int hashCode() {
         return id == null ? System.identityHashCode(this) : id.hashCode();
     }
 
 }

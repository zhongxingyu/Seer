 package fr.cg95.cvq.business.request;
 
 import java.io.Serializable;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @Entity
 @Table(name="request_type")
 public class RequestType implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public static final String SEARCH_BY_CATEGORY_ID = "categoryId";
     public static final String SEARCH_BY_STATE = "active";
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
 
     private String label;
 
     /** whether or not this request type is activated for the current local authority */
     private Boolean active;
 
     /** the category which is in charge of this request type */
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="category_id")
     private Category category;
 
     /** this group in which request type will be display */
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="display_group_id")
     private DisplayGroup displayGroup;
 
     /** the set of requirements to fulfill this request type */
    @ElementCollection(fetch=FetchType.LAZY)
     @CollectionTable(
           name="requirement",
           joinColumns=@JoinColumn(name="request_type_id")
     )
     private Set<Requirement> requirements;
 
     /** the set of forms associated with this request type */
     @ManyToMany(mappedBy="requestTypes",fetch=FetchType.LAZY)
     private Set<RequestForm> forms;
 
     /** the set of seasons associated with this request type */
    @OneToMany(mappedBy="requestType",cascade=CascadeType.ALL, fetch=FetchType.LAZY)
     private Set<RequestSeason> seasons;
 
     /** 
      * whether or not an user can issue multiple registration requests for a given season.
      * only applicable to request types that have seasons defined. 
      */
     @Column(name="authorize_multiple_registrations_per_season")
     private Boolean authorizeMultipleRegistrationsPerSeason;
 
     /** the maximum delay (in days) to deal with a request's instruction */
     @Column(name="instruction_max_delay")
     private Integer instructionMaxDelay;
 
     /** the number of days before the maximum delay timeout where we send an alert email */
     @Column(name="instruction_alert_delay")
     private Integer instructionAlertDelay;
 
     public RequestType() {
         setSeasons(new TreeSet<RequestSeason>());
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getLabel() {
         return this.label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public Boolean getActive() {
         return this.active;
     }
 
     public void setActive(Boolean active) {
         this.active = active;
     }
 
     public Category getCategory() {
         return this.category;
     }
 
     public void setCategory(Category category) {
         this.category = category;
     }
 
     public DisplayGroup getDisplayGroup() {
         return displayGroup;
     }
 
     public void setDisplayGroup(DisplayGroup displayGroup) {
         this.displayGroup = displayGroup;
     }
 
     public Set<Requirement> getRequirements() {
         return this.requirements;
     }
 
     public void setRequirements(Set<Requirement> requirements) {
         this.requirements = requirements;
     }
 
     public Set<RequestForm> getForms() {
         return this.forms;
     }
 
     public void setForms(Set<RequestForm> forms) {
         this.forms = forms;
     }
 
     public final Set<RequestSeason> getSeasons() {
         return seasons;
     }
 
     public final void setSeasons(Set<RequestSeason> seasons) {
         this.seasons = seasons;
     }
 
     public final Boolean getAuthorizeMultipleRegistrationsPerSeason() {
         return authorizeMultipleRegistrationsPerSeason;
     }
 
     public final void setAuthorizeMultipleRegistrationsPerSeason(
             Boolean authorizeMultipleRegistrationPerSeason) {
         this.authorizeMultipleRegistrationsPerSeason = authorizeMultipleRegistrationPerSeason;
     }
 
     public Integer getInstructionAlertDelay() {
         return instructionAlertDelay;
     }
 
     public void setInstructionAlertDelay(Integer instructionAlertDelay) {
         this.instructionAlertDelay = instructionAlertDelay;
     }
 
     public Integer getInstructionMaxDelay() {
         return instructionMaxDelay;
     }
 
     public void setInstructionMaxDelay(Integer instructionMaxDelay) {
         this.instructionMaxDelay = instructionMaxDelay;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this)
             .append("id", getId())
             .toString();
     }
 }

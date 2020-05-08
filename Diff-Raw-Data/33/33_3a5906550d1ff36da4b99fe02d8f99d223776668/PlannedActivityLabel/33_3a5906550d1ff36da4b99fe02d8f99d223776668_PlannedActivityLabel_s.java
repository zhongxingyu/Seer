 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 
 import javax.persistence.*;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 
 
 @Entity
 @Table(name = "planned_activity_labels")
 @GenericGenerator(name = "id-generator", strategy = "native",
     parameters = {
         @Parameter(name = "sequence", value = "seq_planned_activity_labels_id")
     }
 )
public class PlannedActivityLabel extends AbstractMutableDomainObject {
     private PlannedActivity plannedActivity;
     private Integer repetitionNumber;
     private Label label;
 
     ////// BEAN PROPERTIES
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "planned_activity_id")
     public PlannedActivity getPlannedActivity() {
         return plannedActivity;
     }
 
     public void setPlannedActivity(PlannedActivity plannedActivity) {
         this.plannedActivity = plannedActivity;
     }
     @Column(name="rep_num")
     public Integer getRepetitionNumber() {
         return repetitionNumber;
     }
 
     public void setRepetitionNumber(Integer repetitionNumber) {
         this.repetitionNumber = repetitionNumber;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "label_id")
     public Label getLabel() {
         return label;
     }
 
     public void setLabel(Label label) {
         this.label = label;
     }
 }

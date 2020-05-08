 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import edu.nwu.bioinformatics.commons.ComparisonUtils;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.CollectionOfElements;
 import org.hibernate.annotations.Columns;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.IndexColumn;
 import org.hibernate.annotations.Parameter;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 import org.hibernate.annotations.Type;
 import org.hibernate.validator.NotNull;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * @author Rhett Sutphin
  */
 @Entity
 @Table
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_scheduled_activities_id")
     }
 )
 public class ScheduledActivity extends AbstractMutableDomainObject implements Comparable<ScheduledActivity> {
     private ScheduledStudySegment scheduledStudySegment;
     private PlannedActivity plannedActivity;
     private Date idealDate;
     private String notes;
     private ScheduledActivityState currentState;
     private List<ScheduledActivityState> previousStates = new LinkedList<ScheduledActivityState>();
     private SortedSet<String> labels = new TreeSet<String>();
     private String details;
     private Activity activity;
     private Amendment sourceAmendment;
     private Integer repetitionNumber;
 
     ////// LOGIC
 
     public void changeState(ScheduledActivityState newState) {
         if (isChangeable()){
             if (getCurrentState() != null) {
                 previousStates.add(getCurrentState());
             }
             setCurrentState(newState);
         }
     }
 
     public void addLabel(String label) {
         getLabels().add(label);
     }
 
     public void removeLabel(String label) {
         getLabels().remove(label);
     }
     
     public int compareTo(ScheduledActivity o) {
         if (getPlannedActivity()!= null && o.getPlannedActivity()!=null){
             Integer weightOne = o.getPlannedActivity().getWeight();
             Integer weightTwo = getPlannedActivity().getWeight();
 
             if (weightOne == null) {
                 weightOne = 0;
             }
             if (weightTwo == null) {
                 weightTwo = 0;
             }
 
             int weightDiff = weightOne.compareTo(weightTwo);
             if (weightDiff != 0) return weightDiff;
 
         }
         if(getActivity().getType() !=null && o.getActivity().getType()!=null) {
             // by type first
             int typeDiff = getActivity().getType().compareTo(o.getActivity().getType());
             if (typeDiff != 0) return typeDiff;
         }
         // then by name
         return ComparisonUtils.nullSafeCompare(getActivity().getName(),o.getActivity().getName());
     }
 
     @Transient
     private boolean isChangeable() {
         Date endDate;
         if (scheduledStudySegment != null
                 && scheduledStudySegment.getScheduledCalendar() != null
                 && scheduledStudySegment.getScheduledCalendar().getAssignment() != null
                 && scheduledStudySegment.getScheduledCalendar().getAssignment().getEndDateEpoch() != null) {
             endDate = scheduledStudySegment.getScheduledCalendar().getAssignment().getEndDateEpoch();
             return getActualDate().before(endDate);
         }
         return true;
     }
 
     @Transient
     public List<ScheduledActivityState> getAllStates() {
         List<ScheduledActivityState> all = new ArrayList<ScheduledActivityState>();
         if (getPreviousStates() != null) all.addAll(getPreviousStates());
         if (getCurrentState() != null) all.add(getCurrentState());
         return all;
     }
 
     @Transient
     public Date getActualDate() {
         Date actualDate = null;
         List<ScheduledActivityState> states = getAllStates();
         Collections.reverse(states);
         for (ScheduledActivityState state : states) {
              actualDate = state.getDate();
              break;
         }
         
         if (actualDate == null) {
             actualDate = getIdealDate();
         }
         return actualDate;
     }
     
     @Transient
     public DayNumber getDayNumber() {
         int number = 0;
         DayNumber dayNumber;
         if(repetitionNumber == null||plannedActivity == null) {
             return null;
         }
         else {
             if(plannedActivity.getPeriod()!=null) {
                 number = ((plannedActivity.getDay() + plannedActivity.getPeriod().getStartDay()-1) + ((plannedActivity.getPeriod().getDuration().getDays())*repetitionNumber));
                 dayNumber = DayNumber.createCycleDayNumber(number, scheduledStudySegment.getStudySegment().getCycleLength());
                 return dayNumber;
             } else {
                 return null;
             }
        }
     }
 
     @Transient
     public boolean isOutstanding() {
         return getCurrentState().getMode().isOutstanding();
     }
 
     @Transient
     public boolean isConditionalState() {
         return ScheduledActivityMode.CONDITIONAL == getCurrentState().getMode();
     }
 
     @Transient
     public boolean isValidNewState(Class<? extends ScheduledActivityState> newStateClass) {
         return getCurrentState().getAvailableStates(isConditionalEvent()).contains(newStateClass);
     }
 
     @Transient
     public boolean isConditionalEvent() {
         for (ScheduledActivityState state : getAllStates()) {
             if (state.getMode() == ScheduledActivityMode.CONDITIONAL) return true;
         }
         return false;
     }
 
     public void unscheduleIfOutstanding(String reason) {
         if (getCurrentState().getMode().isOutstanding()) {
             ScheduledActivityState newState
                 = getCurrentState().getMode().getUnscheduleMode().createStateInstance();
             newState.setReason(reason);
             changeState(newState);
         }
     }
 
     ////// BEAN PROPERTIES
 
     @ManyToOne
     public ScheduledStudySegment getScheduledStudySegment() {
         return scheduledStudySegment;
     }
 
     public void setScheduledStudySegment(ScheduledStudySegment scheduledStudySegment) {
         this.scheduledStudySegment = scheduledStudySegment;
     }
 
     @ManyToOne
     public PlannedActivity getPlannedActivity() {
         return plannedActivity;
     }
 
     public void setPlannedActivity(PlannedActivity plannedActivity) {
         this.plannedActivity = plannedActivity;
     }
 
     @Type(type = "edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate.ScheduledActivityStateType")
     @Columns(columns = {
         @Column(name = "current_state_mode_id"),
         @Column(name = "current_state_reason"),
         @Column(name = "current_state_date")
     })
     public ScheduledActivityState getCurrentState() {
         return currentState;
     }
 
     private void setCurrentState(ScheduledActivityState currentState) {
         this.currentState = currentState;
     }
 
     @OneToMany(cascade = javax.persistence.CascadeType.ALL)
     @JoinColumn(name = "scheduled_activity_id", insertable = true, updatable = true, nullable = false)
     @Cascade({CascadeType.ALL, CascadeType.DELETE_ORPHAN})
     @IndexColumn(name = "list_index")
     @NotNull
     public List<ScheduledActivityState> getPreviousStates() {
         return previousStates;
     }
 
     public void setPreviousStates(List<ScheduledActivityState> previousStates) {
         this.previousStates = previousStates;
     }
 
     public Date getIdealDate() {
         return idealDate;
     }
 
     public void setIdealDate(Date idealDate) {
         this.idealDate = idealDate;
     }
 
     public String getNotes() {
         return notes;
     }
 
     public void setNotes(String notes) {
         this.notes = notes;
     }
 
     public String getDetails() {
         return details;
     }
 
     public void setDetails(String details) {
         this.details = details;
     }
 
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     @JoinColumn(name = "activity_id")
     public Activity getActivity() {
         return activity;
     }
 
     public void setActivity(Activity activity) {
         this.activity = activity;
     }
 
     @ManyToOne(optional = false)
     public Amendment getSourceAmendment() {
         return sourceAmendment;
     }
 
     public void setSourceAmendment(Amendment sourceAmendment) {
         this.sourceAmendment = sourceAmendment;
     }
 
     @CollectionOfElements
     @Sort(type = SortType.COMPARATOR, comparator = LabelComparator.class)
     @JoinTable(name = "scheduled_activity_labels", joinColumns = @JoinColumn(name = "scheduled_activity_id"))
     @Column(name = "label", nullable = false)
     public SortedSet<String> getLabels() {
         return labels;
     }
 
     public void setLabels(SortedSet<String> labels) {
         this.labels = labels;
     }
 
     /**
      * The repetition of the source period from which this event was created.
      * Zero-based.
      *
      * @see Period
      */
     public Integer getRepetitionNumber() {
         return repetitionNumber;
     }
 
     public void setRepetitionNumber(Integer repetitionNumber) {
         this.repetitionNumber = repetitionNumber;
     }
 
     @Transient
     public boolean isPlannedScheduledActivity() {
         return (getPlannedActivity() != null);
     }
 
     ////// OBJECT METHODS
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(getClass().getSimpleName())
             .append("[idealDate=").append(getIdealDate());
 
             // Create flag for reconsent events
             if (isPlannedScheduledActivity()) {
                 sb.append("; plannedActivity=").append(getPlannedActivity().getId());
             }
             sb.append("; repetition=").append(getRepetitionNumber())
               .append("; labels=").append(getLabels())
               .append(']');
         return sb.toString();
     }
 }

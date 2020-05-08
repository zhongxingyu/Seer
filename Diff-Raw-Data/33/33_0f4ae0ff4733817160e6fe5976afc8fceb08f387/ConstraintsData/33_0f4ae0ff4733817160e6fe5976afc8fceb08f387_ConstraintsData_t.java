 package org.ffplanner.controller.constraints;
 
 import com.google.common.base.Objects;
 import org.ffplanner.bean.UserScheduleBean;
 import org.ffplanner.bean.programme.FestivalEditionProgramme;
import org.ffplanner.def.ScheduleConstraintsDefinition;
 import org.ffplanner.entity.*;
 
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.ffplanner.entity.ScheduleConstraintType.*;
 
 /**
  * @author Bogdan Dumitriu
  */
 public class ConstraintsData {
 
     private final UserScheduleBean userScheduleBean;
 
     private final FestivalEditionProgramme festivalProgramme;
 
     private final Map<Long, QualifiedConstraint> constraints;
 
     private UserSchedule userSchedule;
 
     private ConstraintsDefinition constraintsDefinition;
 
     public ConstraintsData(UserScheduleBean userScheduleBean, FestivalEditionProgramme festivalProgramme) {
         this.userScheduleBean = userScheduleBean;
         this.festivalProgramme = festivalProgramme;
         this.constraints = new HashMap<>();
     }
 
     public int size() {
         return constraints.size();
     }
 
     public void loadFor(User user) {
         constraints.clear();
         userSchedule = userScheduleBean.findOrCreateBy(user.getId(), festivalProgramme.getFestivalEdition());
         addMovieConstraints();
         addShowingConstraints();
         addShowingElsewhereConstraints();
         constraintsDefinition = null;
     }
 
     private void addMovieConstraints() {
         for (MovieBundleConstraint movieConstraint : userSchedule.getMovieConstraints()) {
             addConstraintsBasedOn(movieConstraint);
         }
     }
 
     private void addConstraintsBasedOn(MovieBundleConstraint movieConstraint) {
         final MovieBundleInFestival movieBundle = movieConstraint.getMovieBundle();
         final Long movieBundleId = movieBundle.getId();
         for (Showing showing : festivalProgramme.getShowingsFor(movieBundle)) {
             final Long showingId = showing.getId();
             final QualifiedConstraint qualifiedConstraint =
                     new QualifiedMovieConstraint(showingId, movieBundleId, movieConstraint.getPriority());
             constraints.put(showingId, qualifiedConstraint);
         }
     }
 
     private void addShowingConstraints() {
         for (ShowingConstraint showingConstraint : userSchedule.getShowingConstraints()) {
             addConstraint(showingConstraint);
         }
     }
 
     private void addConstraint(ShowingConstraint showingConstraint) {
         final Long showingId = showingConstraint.getShowing().getId();
         final QualifiedConstraint qualifiedConstraint =
                 new QualifiedShowingConstraint(showingId, showingConstraint.getPriority());
         constraints.put(showingId, qualifiedConstraint);
     }
 
     private void addShowingElsewhereConstraints() {
         for (ShowingConstraint showingConstraint : userSchedule.getShowingConstraints()) {
             addConstraintsBasedOn(showingConstraint);
         }
     }
 
     private void addConstraintsBasedOn(ShowingConstraint showingConstraint) {
         for (Showing showing : festivalProgramme.getShowingsForSameMovieAs(showingConstraint.getShowing())) {
             final Long showingId = showing.getId();
             QualifiedConstraint qualifiedConstraint = constraints.get(showingId);
             if (qualifiedConstraint == null || qualifiedConstraint.getScheduleConstraintType() == MOVIE) {
                 qualifiedConstraint = new QualifiedShowingElsewhereConstraint(showingId);
                 constraints.put(showingId, qualifiedConstraint);
             }
         }
     }
 
     public boolean reloadNeeded() {
         final Date lastUpdate = userSchedule.getLastModified();
         final UserSchedule currentSchedule = userScheduleBean.find(userSchedule.getId());
         return scheduleDeleted(currentSchedule) || scheduleUpdatedSince(lastUpdate, currentSchedule);
     }
 
     private static boolean scheduleDeleted(UserSchedule currentSchedule) {
         return currentSchedule == null;
     }
 
     private static boolean scheduleUpdatedSince(Date lastUpdate, UserSchedule currentSchedule) {
         return currentSchedule != null && !Objects.equal(lastUpdate, currentSchedule.getLastModified());
     }
 
     /**
     * @return a {@link ScheduleConstraintsDefinition} based on the current state of the constraints. This result is not
     *         updated automatically if the constraints change.
      */
    public ScheduleConstraintsDefinition asScheduleConstraints() {
         if (constraintsDefinition == null) {
             constraintsDefinition = new ConstraintsDefinition();
             constraintsDefinition.initializeFrom(constraints);
         }
         return constraintsDefinition;
     }
 
     public boolean isConstraintSelected(Long showingId, ScheduleConstraintType constraintType) {
         final QualifiedConstraint qualifiedConstraint = constraints.get(showingId);
         if (qualifiedConstraint != null) {
             final ScheduleConstraintType scheduleConstraintType = qualifiedConstraint.getScheduleConstraintType();
             return scheduleConstraintType == constraintType;
         } else {
             return false;
         }
     }
 
     /**
      * @return true if one of the {@link ScheduleConstraintType#WEAK_CONSTRAINTS WEAK_CONSTRAINTS} is selected as a
      * constraint for {@code showingId}.
      */
     public boolean hasWeakConstraintFor(Long showingId) {
         return isConstraintSelected(showingId, WEAK_CONSTRAINTS);
     }
 
     /**
      * @return true if one of the {@link ScheduleConstraintType#USER_CONSTRAINTS USER_CONSTRAINTS} is selected as a
      * constraint for {@code showingId}.
      */
     public boolean hasUserConstraintFor(Long showingId) {
         return isConstraintSelected(showingId, USER_CONSTRAINTS);
     }
 
     private boolean isConstraintSelected(Long showingId, EnumSet<ScheduleConstraintType> constraintsSet) {
         final QualifiedConstraint qualifiedConstraint = constraints.get(showingId);
         return qualifiedConstraint != null && constraintsSet.contains(qualifiedConstraint.getScheduleConstraintType());
     }
 
     /**
      * @return true if no constraint is set for {@code showingId}.
      */
     public boolean hasNoConstraintFor(Long showingId) {
         return constraints.get(showingId) == null;
     }
 
     public Short getConstraintPriority(Long showingId) {
         final QualifiedConstraint qualifiedConstraint = constraints.get(showingId);
         if (qualifiedConstraint != null) {
             return qualifiedConstraint.getPriority();
         } else {
             return null;
         }
     }
 }

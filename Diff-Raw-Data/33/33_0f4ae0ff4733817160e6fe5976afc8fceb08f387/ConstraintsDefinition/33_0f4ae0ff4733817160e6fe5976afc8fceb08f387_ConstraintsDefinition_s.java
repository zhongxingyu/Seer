 package org.ffplanner.controller.constraints;
 
 import com.google.common.collect.ImmutableRangeSet;
 import com.google.common.collect.Range;
 import org.ffplanner.def.ConstraintDefinition;
import org.ffplanner.def.ScheduleConstraints;
 import org.joda.time.DateTime;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.TreeSet;
 
 /**
  * @author Bogdan Dumitriu
  */
public class ConstraintsDefinition implements ScheduleConstraints {
 
     private Collection<ConstraintDefinition.Showing> showingConstraints;
 
     private Collection<ConstraintDefinition.Movie> movieConstraints;
 
     public void initializeFrom(Map<Long, QualifiedConstraint> constraints) {
         this.showingConstraints = new TreeSet<>();
         this.movieConstraints = new TreeSet<>();
         for (QualifiedConstraint qualifiedConstraint : constraints.values()) {
             if (qualifiedConstraint instanceof ConstraintDefinition.Showing) {
                 this.showingConstraints.add((ConstraintDefinition.Showing) qualifiedConstraint);
             } else if (qualifiedConstraint instanceof ConstraintDefinition.Movie) {
                 this.movieConstraints.add((ConstraintDefinition.Movie) qualifiedConstraint);
             }
         }
     }
 
     @Override
     public Collection<ConstraintDefinition.Showing> getShowingConstraints() {
         return showingConstraints;
     }
 
     @Override
     public Collection<ConstraintDefinition.Movie> getMovieConstraints() {
         return movieConstraints;
     }
 
     @Override
     public ImmutableRangeSet<DateTime> getTimeConstraints() {
         /** todo: handle time constraints */
         return ImmutableRangeSet.of(Range.closedOpen(new DateTime(Long.MIN_VALUE), new DateTime(Long.MAX_VALUE)));
     }
 }

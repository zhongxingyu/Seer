 package org.drools.planner.examples.ras2012.util.model;
 
 import java.math.BigDecimal;
 import java.util.ArrayDeque;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.drools.planner.examples.ras2012.Directed;
 import org.drools.planner.examples.ras2012.model.Arc;
 
 public class OccupationTracker {
 
     protected static final class ArcRange {
 
         private final Arc arc;
 
         private final boolean full, empty;
 
         private final BigDecimal start, end;
 
         public ArcRange(final Arc a) {
             this(a, BigDecimal.ZERO);
         }
 
         public ArcRange(final Arc a, final BigDecimal start) {
             this(a, start, null);
         }
 
         public ArcRange(final Arc a, final BigDecimal start, final BigDecimal end) {
             if (a == null) {
                 throw new IllegalArgumentException("Arc must not be null.");
             }
             this.arc = a;
             if (start.signum() < 0) {
                 throw new IllegalArgumentException("Arc range start must be in the range of <0,"
                         + a.getLength() + ">.");
             }
             this.start = start;
             if (end == null) {
                 this.end = a.getLength();
             } else {
                 if (end.compareTo(this.start) < 0 || end.compareTo(a.getLength()) > 0) {
                     throw new IllegalArgumentException("Arc range end must be in the range of <"
                             + this.start + "," + a.getLength() + ">.");
                 }
                 this.end = end;
             }
             this.full = this.getStart().equals(BigDecimal.ZERO)
                     && this.getEnd().equals(a.getLength());
             this.empty = this.getStart().equals(this.getEnd());
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof ArcRange)) {
                 return false;
             }
             final ArcRange other = (ArcRange) obj;
             if (this.arc != other.arc) {
                 return false;
             }
             if (this.isFull() && other.isFull() || this.isEmpty() && other.isEmpty()) {
                 return true;
             }
             if (!this.end.equals(other.end)) {
                 return false;
             }
             if (!this.start.equals(other.start)) {
                 return false;
             }
             return true;
         }
 
         public Arc getArc() {
             return this.arc;
         }
 
         public BigDecimal getConflictingMileage(final ArcRange other) {
             if (this.getArc() != other.getArc()) {
                 // ranges cannot conflict when they're of two different arcs
                 return BigDecimal.ZERO;
             } else if (this.isFull() && other.isEmpty()) {
                 // the intersection is empty
                 return BigDecimal.ZERO;
             } else if (other.isFull() && this.isEmpty()) {
                 // the intersection is empty
                 return BigDecimal.ZERO;
             } else if (this.isFull() && other.isFull()) {
                 // full conflict
                 return this.getArc().getLength();
             } else {
                 // we need to calculate the actual conflicting range
                 final BigDecimal start = this.getStart().max(other.getStart());
                 final BigDecimal end = this.getEnd().min(other.getEnd());
                 final BigDecimal result = end.subtract(start);
                 if (result.signum() < 0) {
                     // ranges don't overlap
                     return BigDecimal.ZERO;
                 }
                 return end.subtract(start);
             }
         }
 
         private BigDecimal getEnd() {
             return this.end;
         }
 
         private BigDecimal getStart() {
             return this.start;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + (this.arc == null ? 0 : this.arc.hashCode());
             result = prime * result + (this.end == null ? 0 : this.end.hashCode());
             result = prime * result + (this.start == null ? 0 : this.start.hashCode());
             return result;
         }
 
         protected boolean isEmpty() {
             return this.empty;
         }
 
         protected boolean isFull() {
             return this.full;
         }
 
         @Override
         public String toString() {
             final StringBuilder builder = new StringBuilder();
             builder.append("ArcRange [arc=").append(this.arc).append(", start=").append(this.start)
                     .append(", end=").append(this.end).append("]");
             return builder.toString();
         }
 
     }
 
     public static class Builder {
 
         private static final OccupationTracker EMPTY = new OccupationTracker();
 
         public static OccupationTracker empty() {
             return Builder.EMPTY;
         }
 
         private final Directed        directed;
         private final Deque<ArcRange> ranges = new ArrayDeque<ArcRange>();
 
         public Builder(final Directed d) {
             this.directed = d;
         }
 
         public void add(final Arc a, final BigDecimal start, final BigDecimal end) {
             this.add(this.create(a, start, end));
         }
 
         private void add(final ArcRange range) {
             if (this.directed.isEastbound()) {
                 this.ranges.addLast(range);
             } else {
                 this.ranges.addFirst(range);
             }
         }
 
         public void addFrom(final Arc a, final BigDecimal start) {
             this.add(this.create(a, start, a.getLength()));
         }
 
         public void addTo(final Arc a, final BigDecimal end) {
             this.add(this.create(a, BigDecimal.ZERO, end));
         }
 
         public void addWhole(final Arc a) {
             this.add(new ArcRange(a));
         }
 
         public OccupationTracker build() {
             return new OccupationTracker(this.ranges.toArray(new ArcRange[this.ranges.size()]));
         }
 
         protected ArcRange create(final Arc a, final BigDecimal start, final BigDecimal end) {
             if (this.directed.isEastbound()) {
                 return new ArcRange(a, start, end);
             } else {
                 return new ArcRange(a, a.getLength().subtract(end), a.getLength().subtract(start));
             }
         }
     }
 
     private final Map<Arc, ArcRange> ranges = new HashMap<Arc, ArcRange>();
     private final Collection<Arc>    arcs   = new HashSet<Arc>();
 
     private OccupationTracker(final ArcRange... arcRanges) {
         for (final ArcRange range : arcRanges) {
             final Arc a = range.getArc();
             this.ranges.put(a, range);
             this.arcs.add(a);
         }
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof OccupationTracker)) {
             return false;
         }
         final OccupationTracker other = (OccupationTracker) obj;
         if (this.ranges == null) {
             if (other.ranges != null) {
                 return false;
             }
         } else if (!this.ranges.equals(other.ranges)) {
             return false;
         }
         return true;
     }
 
     public BigDecimal getConflictingMileage(final OccupationTracker other) {
         BigDecimal mileage = BigDecimal.ZERO;
         if (this.isEmpty() || other.isEmpty()) {
             return mileage;
         }
         final Collection<Arc> otherIncludedArcs = other.getIncludedArcs();
         for (final Arc a : this.getIncludedArcs()) {
             if (!otherIncludedArcs.contains(a)) {
                 continue;
             }
             mileage = mileage.add(this.getRange(a).getConflictingMileage(other.getRange(a)));
         }
         return mileage;
     }
 
     public Collection<Arc> getIncludedArcs() {
         return this.arcs;
     }
 
     private ArcRange getRange(final Arc a) {
         return this.ranges.get(a);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.ranges == null ? 0 : this.ranges.hashCode());
         return result;
     }
 
     public boolean isEmpty() {
        return this.ranges.size() == 0;
     }
 
 }

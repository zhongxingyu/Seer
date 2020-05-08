 package adintervall;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 public class MultiIntervals implements Intervals {
 
 	// As Intervals is an Interval, and we might return an Interval, always use Interval as return value
 	// As Intervals is an Interval, and functions might be given an Intervals as an Interval, and an explicit
 	// supertype cast might be given, we probably need to extend the supertype function instead.
 	
 	private MultiIntervals(LinkedList<Interval> intervals) {
 		// This MUST be used ONLY by collapse. In theory, we could also integrate the collapse code right here!
 		this.intervals = new HashSet<Interval>(intervals);
 	}
 	
 	@Override
 	public double getLowerBound() {
 		// The lower bound is the lowest lower bound in our interval set
		double lbound = Double.POSITIVE_INFINITY;
 		for (Interval i : intervals)
 			lbound = Math.min(lbound, i.getLowerBound());
 		return lbound;
 	}
 
 	@Override
 	public double getUpperBound() {
 		// The upper bound is the highest upper bound in our interval set
		double ubound = Double.NEGATIVE_INFINITY;
 		for (Interval i : intervals)
 			ubound = Math.min(ubound, i.getLowerBound());
 		return ubound;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean notEquals(Object other) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
     @Override
     public String toString() {
     	StringBuilder sb = new StringBuilder('{');
     	Iterator<Interval> i = intervals.iterator();
     	while (i.hasNext()) {
     		sb.append(i.next());
     		if (i.hasNext()) sb.append(',');
     	}
     	return sb.append('}').toString();
     }
 
     @Override
     public int hashCode() {
     	// Arrays and lists have a great hashCode implementation.
     	return intervals.hashCode();
     }
 
 	@Override
 	public double length() {
 		// Given our interval is collapsed, then our length is the sum of all our intervalls' length.
 		double length = 0;
 		for (Interval i : intervals)
 			length += i.length();
 		return length;
 	}
 
 	@Override
 	public Interval plus(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval minus(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval multi(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval div(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval square() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean contains(double value) {
 		// If any of our intervals contains the value, we contain it.
 		for (Interval i : intervals)
 			if (i.contains(value)) return true;
 		return false;
 	}
 
 	@Override
 	public Interval plus(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval minus(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval multi(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval div(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval plusKom(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval minusKom(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval multiKom(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval divKom(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Interval union(Interval other) {
 		// We join our interval with the other.
 		@SuppressWarnings("unchecked")
 		LinkedList<Interval> result = (LinkedList<Interval>) intervals.clone();
 		if (other instanceof Intervals)
 			for (Interval i : (Intervals) other)
 				result.add(i);
 		else
 			result.add(other);
 		return collapse(result);
 	}
 
 	@Override
 	public Interval intersection(Interval other) {
 		// We intersect all our intervals with the other
 		LinkedList<Interval> result = new LinkedList<Interval>();
 		for (Interval i1 : intervals)
 			if (other instanceof Intervals)
 				// If we got multiple, then we join all the intersections
 				for (Interval i2 : (Intervals) other)
 					result.add(i1.intersection(i2));
 			else
 				result.add(i1.intersection(other));
 		return collapse(result);
 	}
 
 	@Override
 	public Interval difference(Interval other) {
 		// I act on the assumption that a difference means that only parts that are not in the other interval remain.
 		if (other instanceof Intervals) {
 			// We create lots of intervals, that have one Interval from the others removed and intersect them.
 			Interval intersection = Interval.realInterval;
 			for (Interval i : (Intervals) other) {
 				intersection = intersection.intersection(difference(i));
 			}
 			return intersection;
 		} else {
 			// We remove the other interval from all our intervals and join them.
 			LinkedList<Interval> result = new LinkedList<Interval>();
 			for (Interval i : intervals)
 				result.add(i.difference(other));
 			return collapse(result);
 		}
 	}
 
 	@Override
 	public Boolean contains(Interval other) {
 		// If other is an intervals, any of them must be contained. Using the same method.
 		// Otherwise checks if any of our intervals contains other.
 		if (other instanceof Intervals) {
 			for (Interval i : (Intervals) other)
 				if (!contains(i)) return false;
 			return true;
 		} else {
 			for (Interval i : intervals)
 				if (i.contains(other))
 					return true;
 			return false;
 		}
 	}
 
 	@Override
 	public Boolean less(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean lessEqual(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean greater(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean greaterEqual(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pLess(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pLessEqual(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pGreater(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pGreaterEqual(Interval other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean less(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean lessEqual(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean greater(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean greaterEqual(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pLess(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pLessEqual(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pGreater(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Boolean pGreaterEqual(double other) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// Should probably be made final.
 	private HashSet<Interval> intervals = new HashSet<Interval>();
 	
 	@Override
 	public Iterator<Interval> iterator() {
 		return intervals.iterator();
 	}
 	
 	@SuppressWarnings("unused")
 	private Interval collapse() {
 		// We got a hashset. And we want to stay immutable.
 		return collapse(new LinkedList<Interval>(intervals));
 	}
 	
 	private Interval collapse(LinkedList<Interval> intervals) {
 		// We order all our intervals. Then we iterate:
 		// If we find a gap, we push what we've got.
 		// Else, we expand the bounds making them one larger interval.
 		Collections.sort(intervals, new Comparator<Interval>() {
 			public int compare(Interval arg0, Interval arg1) {
 				return Double.compare(arg0.getLowerBound(), arg1.getLowerBound());
 			}			
 		});
 		LinkedList<Interval> result = new LinkedList<Interval>();
 		Double lbound = null;
 		Double ubound = null;
 		for (Interval i : intervals)
 			if (i == Interval.NaI) continue;
 			else if (i == Interval.realInterval) return i;
 			else if (ubound == null || ubound < i.getLowerBound()) {
 				if (ubound != null)
 					result.push(new NormalInterval(lbound, ubound));
 				lbound = i.getLowerBound();
 				ubound = i.getUpperBound();
 			} else if (ubound < i.getUpperBound())
 				ubound = i.getUpperBound();
 		// Don't forget about the last part!
 		if (ubound != null)
 			result.push(new NormalInterval(lbound, ubound));
 		// Here comes the part that lets us return Interval instead of Intervals
 		if (result.size() == 0)
 			return Interval.NaI;
 		else if (result.size() == 1)
 			return result.get(0);
 		else
 			// We are the only function in the code allowed to do this.
 			return new MultiIntervals(result);
 	}
 }

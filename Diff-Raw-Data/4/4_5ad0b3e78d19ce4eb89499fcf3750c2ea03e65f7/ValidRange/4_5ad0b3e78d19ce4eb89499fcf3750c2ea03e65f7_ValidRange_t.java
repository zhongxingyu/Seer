 package harschware.collections.ranges;
 
 import harschware.collections.sequences.Sequence;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.builder.CompareToBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 import com.google.common.collect.Lists;
 
 /**
  * 
  * 
  * @author tharsch
  * 
  * @see http://gleichmann.wordpress.com/2008/01/21/declarative-programming-a-range-type-for-java/
  * @see http://martinfowler.com/eaaDev/Range.html
  * @see http://commons.apache.org/lang/api-2.3/org/apache/commons/lang/math/Range.html
  * 
  * @param <T>
  */
 @SuppressWarnings("unchecked")
 public class ValidRange<T extends Comparable> extends Range<T> {
 	private ValidRange() {
 	};
 
 	/**
 	 * package private factory method - used only by abstract parent
 	 * 
 	 * @param <S>
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	static <S extends Comparable> Range<S> newInstance(S start, S end) {
 		Range<S> newRange = new ValidRange();
 		newRange.start = start;
 		newRange.end = end;
 		return newRange;
 	} // end method
 	
 	@Override
 	public boolean before(T value) {
 		return end.compareTo(value) < 0;
 	} // end method
 
 	@Override
 	public boolean after(T value) {
 		return start.compareTo(value) > 0;
 	} // end method
 
 	@Override
 	public boolean contains(T value) {
 		return start.compareTo(value) <= 0 && end.compareTo(value) >= 0;
 	} // end method
 
 	@Override
 	public boolean contains(Range<T> arg) {
 		if( arg == EmptyRange.INSTANCE ) return false;
 		return contains(arg.getStart()) && contains(arg.getEnd());
 	} // end contains range
 	
 	@Override
 	public boolean overlaps(Range<T> arg) {
 		return arg.contains(start) || arg.contains(end);
 	} // end method
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("[");
 		sb.append(start);
 		sb.append(",");
 		sb.append(end);
 		sb.append("]");
 		return sb.toString();
 	} // end method
 
 	@Override
 	public final boolean equals(Object o) {
 		if (!(o instanceof ValidRange<?>))
 			return false;
 		final ValidRange<?> other = (ValidRange<?>) o;
 
 		return equal(getStart(), other.getStart())
 				&& equal(getEnd(), other.getEnd());
 	} // end method
 
 	private static final boolean equal(Object o1, Object o2) {
 		if (o1 == null) {
 			return o2 == null;
 		}
 		return o1.equals(o2);
 	} // end method
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 13).append(this.start).append(this.end)
 				.toHashCode();
 	} // end method
 
 	@Override
 	public int compareTo(Range<T> other) {
 		return new CompareToBuilder().append(this.start, other.getStart()).append(
 				this.end, other.getEnd()).toComparison();
 	} // end method
 
 	@Override
 	public boolean isEmpty() {
 		if (this.equals(EmptyRange.INSTANCE))
 			return true;
 		return false;
 	} // end method
 
 	@Override
 	public Iterator<T> iterator() {
 		try {
 			start.getClass();
 		} catch(NullPointerException npe) {
 			return new EmptyIterator();
 		} // end try/catch
 		
 		Sequence<T> sequence = this.getSequence(start);
 
 		return new RangeIterator(sequence, end);
 	} // end method
 
 	private Sequence<T> getSequence( T newStart ) {
 		Sequence<T> sequence = null;
 
 		Class<? extends Comparable> startClazz = start.getClass();
		String className = "harschware.collections.sequences."
 			+ startClazz.getSimpleName() + "Sequence";
		logger.debug( "Sequence strategy '" + className + "' chosen for '" + startClazz.getCanonicalName() + "'" );
 		
 		try {
 			Class clazz = Class.forName(className);
 
 			sequence = (Sequence<T>) clazz.getDeclaredConstructor(startClazz)
 					.newInstance(newStart);
 
 		} catch (Exception e) {
 			throw new UnsupportedOperationException("No Sequence found for type "
 					+ startClazz);
 		} // end try/catch
 		return sequence; 
 	} // end method
 	
 	@Override
 	public Range<T> gap(Range<T> arg) {
 		if (this.overlaps(arg))
 			return (Range<T>) EmptyRange.INSTANCE;
 		Range<T> lower, higher;
 		if (this.compareTo(arg) < 0) {
 			lower = this;
 			higher = arg;
 		} else {
 			lower = arg;
 			higher = this;
 		} // end if
 
 		Sequence<T> lowSeq = getSequence(lower.getEnd());
 		Sequence<T> highSeq = getSequence(higher.getStart());
 		return ValidRange.create(lowSeq.next().value(), highSeq.previous().value());
 	} // end method
 
 	@Override
 	public boolean abuts(Range<T> arg) {
 		return !this.overlaps(arg) && this.gap(arg).isEmpty();
 	} // end method
 
 	@Override
 	public boolean partitionedBy(Collection<Range<T>> ranges) {
 		// need a copy so as not to disturb the order of the input collection
 		logger.debug("partitionedBy making list copy of input collection");
 		List<Range<T>> listCopy = Lists.newArrayList(ranges);
 		return Range.sortAndCollapse(listCopy).equals(this);
 	} // end method
 
 } // end class

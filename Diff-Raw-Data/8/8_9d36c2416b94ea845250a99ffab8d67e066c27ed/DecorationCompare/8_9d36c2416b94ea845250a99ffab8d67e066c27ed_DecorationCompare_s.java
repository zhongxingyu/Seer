 package enduro.racer.comparators;
 
 import java.util.Comparator;
 
 import enduro.racer.Racer;
 
 /**
  * This is the abstract decorationcompare class.
  * All subclasses of this class implements the Decorator design pattern
  * (see: http://en.wikipedia.org/wiki/Decorator_pattern )
  * 
  * Meaning that they can be combined and supplant each other, eg. if
  * one compare returns 0 then it falls back to a lesser important one until either
  * something other than 0 is obtained - or if the next fallback is null - in which case it returns 0.
  * 
  * Subclasses should implement a basic constructor in addition to the required one - sending null to the super constructor means "no fallback"
  * 
  */
 public abstract class DecorationCompare implements Comparator<Racer> {
 	
 	private DecorationCompare fallback;
 	
 	public DecorationCompare(DecorationCompare fallback) {
 		this.fallback = fallback;
 	}
 	
 	public int compare(Racer arg0, Racer arg1) {
		int res = compareRacers(arg0, arg1);
 		
 		if(res == 0)
 			if(fallback != null)
 				return fallback.compare(arg0, arg1);
 		return res;
 	}
 	
 	/**
 	 * this is equivalent to what Comparator<Racer>'s compare(a, b) would mean.
 	 * but is in an abstract function allowing multiple compare to benefit from each other.
 	 * 
 	 * @param arg0 Racer to be compared with
 	 * @param arg1 other Racer to be compared with
 	 * @return and int returning the result of a comparison between objects.
 	 */
 	protected abstract int compareRacers(Racer arg0, Racer arg1);
 }

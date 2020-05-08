 package org.bodytrack.client;
 
 /**
  * A class holding a date and value, which makes the value easy to graph.
  * 
  * <p>The date is stored as a <tt>double</tt>, and the value is stored as a
  * <tt>double</tt>.  Once created, objects of this class are designed to
  * be immutable, although in JavaScript all objects are mutable, and are
  * subject to change if an attacker can exploit a cross-site scripting
  * vulnerability.</p>
  */
 public final class PlottablePoint {
 	private double myDate;
 	private double myValue;
 	
 	/**
 	 * Saves copies of date and value in this PlottablePoint.
 	 * 
 	 * @param date
 	 * 		the date for this PlottablePoint, represented as the number
 	 * 		of seconds since 1/1/1970 (the epoch)
 	 * @param value
 	 * 		the value of this PlottablePoint
 	 */
 	public PlottablePoint(double date, double value) {
 		myDate = date;
 		myValue = value;
 	}
 
 	/**
 	 * Returns the date for this PlottablePoint.
 	 * 
 	 * @return
 	 * 		the date for this PlottablePoint
 	 */
 	public double getDate() {
 		return myDate;
 	}
 
 	/**
 	 * Returns the value of this PlottablePoint.
 	 * 
 	 * @return
 	 * 		the value of this PlottablePoint
 	 */
 	public double getValue() {
 		return myValue;
 	}
 
 	/**
 	 * Returns a hashcode based on the date of this <tt>PlottablePoint</tt>.
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp = (long) Math.floor(myDate);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	/**
 	 * Considers this as equal to obj iff obj is a <tt>PlottablePoint</tt>
 	 * object with the same date as this, disregarding any fractional
 	 * part.
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (! (obj instanceof PlottablePoint))
 			return false;
 
		// Two dates are equal if their floors are less than 0.5
		// apart, which is an important distinction whenever we
		// are dealing with numbers that are outside the range
		// of values representable in a long
		return
			Math.abs(Math.floor(myDate) -
				Math.floor(((PlottablePoint) obj).myDate)) < 0.5;
 	}
 }

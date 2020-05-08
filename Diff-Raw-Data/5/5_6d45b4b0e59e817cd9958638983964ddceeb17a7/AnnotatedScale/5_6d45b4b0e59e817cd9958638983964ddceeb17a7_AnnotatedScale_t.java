 package eu.mapperproject.jmml.specification.annotated;
 
 import eu.mapperproject.jmml.specification.Range;
 import eu.mapperproject.jmml.specification.Scale;
 import eu.mapperproject.jmml.specification.Unit;
 import eu.mapperproject.jmml.util.numerical.SIUnit;
 
 /**
  * Adds functionality dealing with regular scales and number of steps.
  * @author Joris Borgdorff
  */
 public class AnnotatedScale extends Scale {
 	private transient int steps;
 	
 	public AnnotatedScale() {
 		super();
 		this.steps = -1;
 	}
 	
 	/**
 	 * Calculate the number of steps that can be taken given the ranges
 	 * Returns -1 if delta or max is not set or not definite
 	 */
 	public int getSteps() {
 		if (steps == -1) {
 			AnnotatedUnit dt = getRegularDelta(), max = getRegularTotal();
 			if ((dt == null && !delta.isDefinite()) || (max == null && !total.isDefinite())) {
 				return -1;
 			}
 
 			SIUnit d = (dt == null ? delta.meanSIUnit() : dt.interpret());
 			SIUnit l = (max == null ? total.meanSIUnit() : max.interpret());
 			steps = (int) Math.round(l.div(d).doubleValue());
 		}
 		return steps;
 	}
 	
 	public boolean deltaIsRegular() {
 		if (this.delta != null) {
 			this.setDelta(delta);
 		}
 		return this.regularDelta != null;
 	}
 
 	public boolean totalIsRegular() {
 		if (this.total != null) {
 			this.setTotal(total);
 		}
 		return this.regularTotal != null;
 	}
 	
 	public boolean isDefinite() {
 		return (this.deltaIsRegular() || (this.delta != null && this.delta.isDefinite()))
 			&& (this.totalIsRegular() || (this.total != null && this.total.isDefinite()));
 	}
 
 	public SIUnit getMinDelta() {
 		if (this.deltaIsRegular()) return ((AnnotatedUnit)this.regularDelta).interpret();
 		else if (this.delta != null) return this.delta.minSIUnit();
 		return null;
 	}
 	public SIUnit getMeanDelta() {
 		if (this.deltaIsRegular()) return ((AnnotatedUnit)this.regularDelta).interpret();
 		else if (this.delta != null) return this.delta.meanSIUnit();
 		return null;
 	}
 	public SIUnit getMaxDelta() {
 		if (this.deltaIsRegular()) return ((AnnotatedUnit)this.regularDelta).interpret();
 		else if (this.delta != null) return this.delta.maxSIUnit();
 		return null;
 	}
 	public SIUnit getMinTotal() {
 		if (this.totalIsRegular()) return ((AnnotatedUnit)this.regularTotal).interpret();
 		else if (this.total != null) return this.total.minSIUnit();
 		return null;
 	}
 	public SIUnit getMeanTotal() {
 		if (this.totalIsRegular()) return ((AnnotatedUnit)this.regularTotal).interpret();
 		else if (this.total != null) return this.total.meanSIUnit();
 		return null;
 	}
 	public SIUnit getMaxTotal() {
 		if (this.totalIsRegular()) return ((AnnotatedUnit)this.regularTotal).interpret();
 		else if (this.total != null) return this.total.maxSIUnit();
 		return null;
 	}
 	
 	@Override
 	public AnnotatedUnit getRegularDelta() {
 		return (deltaIsRegular() ? (AnnotatedUnit) this.regularDelta : null);
 	}
 
 	@Override
 	public AnnotatedUnit getRegularTotal() {
 		return (totalIsRegular() ? (AnnotatedUnit) this.regularTotal : null);
 	}
 
 	@Override
 	public AnnotatedRange getDelta() {
 		if (this.regularDelta == null) {
 			this.setDelta(delta);
 		}
 		return this.delta;
 	}
 
 	@Override
 	public AnnotatedRange getTotal() {
 		if (this.regularTotal == null) {
 			this.setTotal(total);
 		}
 		return this.total;
 	}
 
 	@Override
 	public void setRegularDelta(Unit u) {
 		if (this.delta == null) {
 			this.steps = -1;
 			this.regularDelta = (AnnotatedUnit) u;
 		}
 	}
 
 	@Override
 	public void setRegularTotal(Unit u) {
 		if (this.total == null) {
 			this.steps = -1;
 			this.regularTotal = (AnnotatedUnit) u;
 		}
 	}
 
 	@Override
 	public void setTotal(Range r) {
 		this.steps = -1;
 		
 		AnnotatedRange ar = (AnnotatedRange) r;
		if (ar != null && ar.isRegular()) {
 			this.regularTotal = ar.getMin();
 			this.total = null;
 		} else {
 			this.regularTotal = null;
 			this.total = ar;
 		}
 	}
 
 	@Override
 	public void setDelta(Range r) {
 		this.steps = -1;
 		
 		AnnotatedRange ar = (AnnotatedRange) r;
		if (ar != null && ar.isRegular()) {
 			this.regularDelta = ar.getMin();
 			this.delta = null;
 		} else {
 			this.regularDelta = null;
 			this.delta = ar;
 		}
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o == null || !getClass().equals(o.getClass())) return false;
 		AnnotatedScale as = (AnnotatedScale)o;
 		boolean dr = this.deltaIsRegular(), tr = this.totalIsRegular();
 		return (dr == as.deltaIsRegular()) && (tr != as.totalIsRegular())
 				&& (dr ? this.regularDelta.equals(as.regularDelta)
 					: (this.delta == null ? as.delta == null : this.delta.equals(as.delta)))
 				&& (tr ? this.regularTotal.equals(as.regularTotal)
 					: (this.total == null ? as.total == null : this.total.equals(as.total)));
 	}
 	
 	public boolean isContiguous(AnnotatedScale s) {
 		return (this.getMinTotal().compareTo(s.getMinDelta()) <= 0
 				&& s.getMinDelta().compareTo(this.getMaxTotal()) <= 0
 				&& this.getMaxTotal().compareTo(s.getMaxDelta()) <= 0)
 			||
 				(s.getMinTotal().compareTo(this.getMinDelta()) <= 0
 				&& this.getMinDelta().compareTo(s.getMaxTotal()) <= 0
 				&& s.getMaxTotal().compareTo(this.getMaxDelta()) <= 0);
 	}
 
 	public boolean isSeparated(AnnotatedScale s) {
 		return (this.getMaxTotal().compareTo(s.getMinDelta()) < 0
 			|| s.getMaxTotal().compareTo(this.getMinDelta()) < 0);
 	}
 
 	public boolean isOverlapping(AnnotatedScale s) {
 		return (this.getMaxDelta().compareTo(s.getMinTotal()) < 0
 			&& s.getMaxDelta().compareTo(this.getMinTotal()) < 0);
 	}
 	
 	public boolean hasGreaterOrEqualMaximumTo(AnnotatedScale s) {
 		return this.getMaxTotal().compareTo(s.getMaxTotal()) >= 0;
 	}
 	
 	@Override
 	public int hashCode() {
 		int hash = 3;
 		if (this.deltaIsRegular()) hash = hash * 31 + 17 * this.regularDelta.hashCode();
 		else if (delta != null)    hash = hash * 29 + 17 * this.delta.hashCode();
 		if (this.totalIsRegular()) hash = hash * 31 + 17 * this.regularTotal.hashCode();
 		else if (total != null)    hash = hash * 29 + 17 * this.total.hashCode();
 		return hash;
 	}
 }

 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 import java.math.BigDecimal;
 
 /**
  * Implementation of {@link Discount} interface containing as much general functionality for a
  * discount as possible.
  * 
  * @author Tarinaky
  *
  */
 public abstract class AbstractDiscount implements Discount {
	/** Stores the value of the discount when serialised.
	 * This is used to allow for this value to be serialised - removing 
	 * source-code dependence on the implementation. 
 	 */
 	private BigDecimal savedValue = null;
	/**
	 * Is set true when the discount is finalised
	 */
 	private boolean finalised = false;
 
 	public String toString() { return getDescription(); }
 	
 	protected void setValue(BigDecimal value) {
 		savedValue = value;
 	}
 	
 	public BigDecimal getValue() {
		if (isFinalised() == false) {
 			return calculateValue();
 		} else {
 			return savedValue;
 		}
 	}
 	
	
 	public void finalise(Order order) {
 		setValue(getValue().multiply(new BigDecimal(match(order))));
		setFinalised(order.isFinalised());
 	}
 	
 	public abstract BigDecimal calculateValue();
 	
 	public String getStringValue() { return ""+savedValue; }
 	public void setStringValue(String s) { savedValue = new BigDecimal(s); }
 
	public boolean isFinalised() {
		return finalised;
	}

	public void setFinalised(boolean finalised) {
		this.finalised = finalised;
	}

 }

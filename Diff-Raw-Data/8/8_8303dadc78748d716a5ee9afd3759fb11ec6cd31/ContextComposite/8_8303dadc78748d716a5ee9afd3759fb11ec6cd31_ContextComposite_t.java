 /**
  * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
  */
 package sim.monitor.subscribers.mbean;
 
 import java.beans.ConstructorProperties;
 
 /**
  * @author val
  *
  */
// TODO define typed value, as in mbean (long, double, bigdecimal or date)
 public class ContextComposite {
 
 	private String contextValue;
 	private String value;
 
 	@ConstructorProperties("contextValue, value")
 	public ContextComposite(String contextValue, String value) {
 		this.contextValue = contextValue;
 		this.value = value;
 	}
 
 	/**
 	 * @return the contextValue
 	 */
 	public String getContextValue() {
 		return contextValue;
 	}
 
 	/**
 	 * @param contextValue
 	 *            the contextValue to set
 	 */
 	public void setContextValue(String contextValue) {
 		this.contextValue = contextValue;
 	}
 
 	/**
 	 * @return the value
 	 */
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * @param value
 	 *            the value to set
 	 */
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 }

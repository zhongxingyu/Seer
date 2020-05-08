 /*
  * $Log: not supported by cvs2svn $
  * 
 * $Id: TransientInterferenceMethod.java,v 1.1 2006-10-17 16:14:36 pandyas Exp $
  */
 package gov.nih.nci.camod.domain;
 
 import gov.nih.nci.camod.util.Duplicatable;
 import gov.nih.nci.camod.util.HashCodeUtil;
 import java.io.Serializable;
 
 public class TransientInterferenceMethod extends BaseObject implements
 		Comparable, Serializable, Duplicatable {
 
 	private static final long serialVersionUID = 3259095453799404851L;
 
 	private String name;
 
 	private String conceptCode;
 
 	/**
 	 * @return Returns the name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            The name to set.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return Returns the conceptCode.
 	 */
 	public String getConceptCode() {
 		return conceptCode;
 	}
 
 	/**
 	 * @param conceptCode
 	 *            The conceptCode to set.
 	 */
 	public void setConceptCode(String conceptCode) {
 		this.conceptCode = conceptCode;
 	}
 
 	public String toString() {
 		String result = super.toString() + " - ";
 		result += this.getName();
 		return result;
 	}
 
 	public boolean equals(Object o) {
 		if (!super.equals(o))
 			return false;
 		if (!(this.getClass().isInstance(o)))
 			return false;
 		final TransientInterferenceMethod obj = (TransientInterferenceMethod) o;
 		if (HashCodeUtil.notEqual(this.getName(), obj.getName()))
 			return false;
 		return true;
 	}
 
 	public int hashCode() {
 		int result = HashCodeUtil.SEED;
 		result = HashCodeUtil.hash(result, this.getName());
 		return result + super.hashCode();
 	}
 
 	public int compareTo(Object o) {
 		if ((o instanceof TransientInterferenceMethod)
 				&& (this.getName() != null)
 				&& (((TransientInterferenceMethod) o).getName() != null)) {
			int result = this.getName().compareTo(((Disease) o).getName());
 			if (result != 0) {
 				return result;
 			}
 		}
 
 		return super.compareTo(o);
 	}
 }

 /**
  * Generated with Acceleo
  */
 package org.eclipse.emf.eef.middle.middlenonreg.parts;
 
 // Start of user code for imports
 
import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 
 // End of user code
 /**
  * 
  */
 public interface NamedElementPropertiesEditionPart {
 
 	/**
 	 * @return the name
 	 */
 	public String getName();
 
 	/**
 	 * Defines a new name
 	 * @param newValue the new name to set
 	 */
 	public void setName(String newValue);
 
 	public void setMessageForName(String msg, int msgLevel);
 
 	public void unsetMessageForName();
 
 
 
 
 	/**
 	 * @return the DocumentedElement referenced view
 	 */
 	public IPropertiesEditionPart getDocumentedElementReferencedView();
 	/**
 	 * @return the documentation
 	 */
 	public String getDocumentation();
 
 	/**
 	 * Defines a new documentation
 	 * @param newValue the new documentation to set
 	 */
 	public void setDocumentation(String newValue);
 
 	public void setMessageForDocumentation(String msg, int msgLevel);
 
 	public void unsetMessageForDocumentation();
 
 
 
 	// Start of user code for additional methods

 	// End of user code
 }
 

 
 package edu.wustl.common.querysuite.metadata.category;
 
 import java.io.Serializable;
 
 /**
  * @version 1.0
  * @created 28-Dec-2006 2:01:13 AM
  * @hibernate.joined-subclass table="CATEGORIAL_CLASS"
  * @hibernate.joined-subclass-key column="ID" 
  */
 public class CategorialClass
 		extends
 			AbstractCategorialClass<CategorialAttribute, Category, CategorialClass>
 		implements
 			Serializable
 {
 
 	private static final long serialVersionUID = -136227480173654340L;
 
 }

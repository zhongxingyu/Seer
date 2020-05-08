 package gov.nih.nci.iso21090.hibernate.tuple;
     
 import gov.nih.nci.iso21090.Any;
 import gov.nih.nci.iso21090.DSet;
 import gov.nih.nci.iso21090.Int;
 import gov.nih.nci.iso21090.Ivl;
 import gov.nih.nci.iso21090.NullFlavor;
 import gov.nih.nci.iso21090.Pq;
 import gov.nih.nci.iso21090.Real;
 import gov.nih.nci.iso21090.hibernate.node.ComplexNode;
 import gov.nih.nci.iso21090.hibernate.node.ConstantNode;
 import gov.nih.nci.iso21090.hibernate.node.Node;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.math.BigDecimal;
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.HibernateException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Helper class which reads the IsoConstants.xml file and sets the constant values in the 
  * classes loaded by Hibernate.
  * 
  * @author patelsat
  *
  */
 @SuppressWarnings("PMD.CyclomaticComplexity")
 public class IsoConstantTuplizerHelper {
 
     /**
      * Prefix for the entity name.
      */
     public static final String ENTITY_NAME_PREFIX = "_xxEntityxx_";
     private static final String NULL_FLAVOR_ATTRIBUTE = "nullFlavor";
     private static ApplicationContext ctx = new ClassPathXmlApplicationContext("IsoConstants.xml");
 
     
     
     /**
      * Sets the nullFlavor in entity of propertyValue is null otherwise sets the constant values in the 
      * propertyValue. entityName and propertyName combination is used to locate the entry in the 
      * IsoConstants.xml file
      * @param entity Parent object in which nullflavor should be set if propertyValue is null
      * @param propertyValue object in which constants are to be set
      * @param entityName name of the entity
      * @param propertyName name of the property
      * @param processParts indicates if the innerNode is a collection part then whether to process it or not
      */
     public void setConstantValues(Object entity, Object propertyValue, String entityName, 
             String propertyName, boolean processParts) {
         Node node = getComplexNodeBean(entityName, propertyName);
         setConstantValues(entity, propertyValue, propertyName, (ComplexNode) node, processParts);
     }
     
     @SuppressWarnings("PMD.CyclomaticComplexity")
     private void setConstantValues(Object parent, Object property, String propertyName, ComplexNode complexNode, boolean processParts) {
         
         if (complexNode == null && property == null) {
         	setNullFlavor(parent, propertyName);
         	return;
         } else if (complexNode == null && property != null) {
         	setNullFlavor(property);
         	return;
         }
         
         if (property == null) {
             setNullFlavor(parent, complexNode);
             return;
         } else if (Any.class.isAssignableFrom(property.getClass()) && ((Any) property).getNullFlavor() !=  null) {
             return;
         } else if (DSet.class.isAssignableFrom(property.getClass()) && (((DSet) property).getItem() == null || ((DSet) property).getItem().size() == 0)) {
             setNullFlavor(parent, complexNode);
             return;
         } else {
             for (Node node : complexNode.getInnerNodes()) {
                 if ((node instanceof ConstantNode) && !(NULL_FLAVOR_ATTRIBUTE.equals(node.getName()))) {
                     setValue(property, node.getName(), ((ConstantNode) node).getInstance());
                 }
             }
             
             for (Node node : complexNode.getInnerNodes()) {
                 
                 if (node instanceof ComplexNode) {
                     
                     if (!node.getName().startsWith("part_")) {
                         Object existingObject = getPropertyObject(property, node.getName());
                         if (existingObject == null) {
                             setNullFlavor(property, (ComplexNode) node);
                         } else {
                             
                             if (Any.class.isAssignableFrom(existingObject.getClass())) {
                                 setConstantValues(property, existingObject, node.getName(), (ComplexNode) node, processParts);
                             } else if (Set.class.isAssignableFrom(existingObject.getClass())) {
                                 for (Object obj : (Set) existingObject) {
                                     setConstantValues(property, obj, node.getName(), (ComplexNode) node, processParts);
                                 }
                             }
                         }
                     } else if (processParts) {
                         List partList = (List) getPropertyObject(property, "part");
                         Integer index = Integer.parseInt(node.getName().substring("part_".length()));
 
                         if (partList != null) {
                             if (partList.size() < index) {
                                 throw new HibernateException("Can not set constant as part.size()<part[index]");
                             }
                             setConstantValues(property, partList.get(index), node.getName(), (ComplexNode) node, processParts);
                         }
                     }
                 }
             }
             setNullFlavor(property);
         }
     }
 
 
 
     private Object getPropertyObject(Object parent, String propertyName) {
         try {
             
             Field propertyField = findFieldInClass(parent.getClass(), propertyName);
             propertyField.setAccessible(true);
             return propertyField.get(parent);
             
         } catch (SecurityException e) {
             throw new HibernateException(e);
         } catch (IllegalAccessException e) {
             throw new HibernateException(e);
         }
     }
 
     private void setValue(Object parent, String propertyName, Object instance) {
         try {
             
             Field propertyField = findFieldInClass(parent.getClass(), propertyName);        
             propertyField.setAccessible(true);
             Object converteValue = convertValue(instance, propertyField.getType()); 
             propertyField.set(parent, converteValue);
             
         } catch (SecurityException e) {
             throw new HibernateException(e);
         } catch (IllegalAccessException e) {
             throw new HibernateException(e);
         } catch (IllegalArgumentException e) {
             throw new HibernateException(e);
         }
     }
     
     private Object convertValue(Object value, Class targetType) {
         if (value == null) {
             return null;
         }
         if (targetType.isAssignableFrom(value.getClass())) {
             return value;
         }
         
         try {
             
             if (value.getClass() == String.class) {
                 if (Boolean.class == targetType) {
                     if("1".equals(value)) {
                         return Boolean.TRUE;
                     } else if ("0".equals(value)) {
                         return Boolean.FALSE;
                     } else {
                         return Boolean.valueOf((String) value);
                     }
                 } else if (Integer.class == targetType) {
                     return Integer.valueOf((String) value);
                 } else if (BigDecimal.class == targetType) {
                     return BigDecimal.valueOf(Double.valueOf((String) value));
                 } else if (URI.class == targetType) {
                     return new URI((String) value);
                 }
             }
         } catch (Exception e) {
             throw new HibernateException("Error in converting String tyoe to " 
                     + targetType.getName()  + " for " + value, e);
         }
         throw new HibernateException("Can not convert Non-String type to target type of " 
                 + targetType.getName());
     }
 
     /**
      * Finds field in class by going to super class.
      * @param klass klass in which the filedName is to be searched
      * @param fieldName name of the field to be retrieved
      * @return Field or exception
      */
     @SuppressWarnings("PMD.EmptyCatchBlock")
     public Field findFieldInClass(Class klass, String fieldName) {
         Class tempKlass = klass;
         while (tempKlass != null && Object.class != tempKlass) {
             try {
                 Field field = tempKlass.getDeclaredField(fieldName);
                 if (field != null) {
                     return field;
                 }
             } catch (SecurityException e) {
                 //No operation
             } catch (NoSuchFieldException e) {
                 //No operation                
             }
             
             tempKlass = tempKlass.getSuperclass();
         }
         throw new HibernateException("No field found in class " + klass.getName() + " for " + fieldName);
     }
     
     private Object intantiatePropertyObject(String typeName) {
         
         try {
             //Field propertyField = findFieldInClass(parent.getClass(), propertyName);
             //Class propertyTypeClass = propertyField.getType();
             Class propertyTypeClass = Class.forName(typeName);
             return propertyTypeClass.newInstance();
             
         } catch (SecurityException e) {
             throw new HibernateException(e);
         } catch (IllegalAccessException e) {
             throw new HibernateException(e);
         } catch (InstantiationException e) {
             throw new HibernateException(e);
         } catch (ClassNotFoundException e) {
             throw new HibernateException(e);
         }
     }    
 
     private void setNullFlavor(Object parent, ComplexNode rootNode) {
         NullFlavor nullFlavor = NullFlavor.NI;
         for (Node node : rootNode.getInnerNodes()) {
             if (NULL_FLAVOR_ATTRIBUTE.equals(node.getName())) {
                 ConstantNode nullFlavorNode = (ConstantNode) node;
                 nullFlavor = (NullFlavor) nullFlavorNode.getInstance();
                 break;
             }
         }
 
         Object nullValueObject = intantiatePropertyObject(rootNode.getIsoClassName());
 
         if (!Any.class.isAssignableFrom(nullValueObject.getClass())) {
             return;
         }
         
         setValue(nullValueObject, NULL_FLAVOR_ATTRIBUTE, nullFlavor);
         setValue(parent, rootNode.getName(), nullValueObject);
     }
 
    private void setNullFlavor(Object parent) {
 
     	Class klass = parent.getClass();
     	Boolean allNullFlavors = true;
     	if(Any.class.isAssignableFrom(klass))
     	{
     		if(((Any)parent).getNullFlavor() == null)
     		{
 		    	try {
 			    	while (klass != null && klass != Object.class) {
 			    		for (Field field : klass.getDeclaredFields()) {
 			    			if (!Modifier.isStatic(field.getModifiers())) 
 			    			{
 				    			field.setAccessible(true);
 				    			Object value = field.get(parent);
 				    			if(value == null) {
 				    				setNullFlavor(parent, field.getName());
 				    			}
 				    			else {
 				    				if (Any.class.isAssignableFrom(value.getClass()))
 				    				{
				    					setNullFlavor(value);
 				    					if(((Any) value).getNullFlavor() != null) 
 				    						allNullFlavors = false;
 				    				}
 				    				else
 				    				{
 				    					allNullFlavors = false;
 				    				}
 				    			}
 			    			}
 			    		}
 			    		
 			    		klass = klass.getSuperclass();
 			    	}
 		        } catch (SecurityException e) {
 		            throw new HibernateException(e);
 		        } catch (IllegalAccessException e) {
 		            throw new HibernateException(e);
 		        } catch (IllegalArgumentException e) {
 		            throw new HibernateException(e);
 				}
 		        if (allNullFlavors)
 		        	((Any) parent).setNullFlavor(NullFlavor.NI);
     		}
     	}    	
     }
     
     private void setNullFlavor(Object parent, String propertyName) {
     	
         NullFlavor nullFlavor = NullFlavor.NI;
 
     	Class propertyTypeClass = determinePropertyClass(parent, propertyName);
         if (!Any.class.isAssignableFrom(propertyTypeClass)) {
             return;
         }
         
     	Object nullValueObject = intantiatePropertyObject(propertyTypeClass.getName());
         
         setValue(nullValueObject, NULL_FLAVOR_ATTRIBUTE, nullFlavor);
         setValue(parent, propertyName, nullValueObject);
     }
     
     private Class determinePropertyClass(Object parent, String propertyName)
     {
     	if(parent instanceof Ivl) {
     		Ivl ivl = (Ivl) parent;
     		Class type = Object.class;
     		Class widthType = Object.class;
     		if (ivl.getAny() != null) {
     			type = ivl.getAny().getClass();
     		} else if (ivl.getHigh() != null) {
     			type = ivl.getHigh().getClass();
     		} else if (ivl.getLow() != null) {
     			type = ivl.getLow().getClass();
     		} 
     		
     		if ("any".equals(propertyName) || "low".equals(propertyName) || "high".equals(propertyName)) {
     			return type;
     		}
     		
     		if ("width".equals(propertyName))
     		{
         		if(Int.class == type) {
         			return Int.class;
         		} else if(Real.class == type) {
         			return Real.class;
         		}
         		return Pq.class;
     		}
     	} 
     	
    		Field field = findFieldInClass(parent.getClass(),propertyName);
    		return field.getType();
     }
 
     /**
      * Returns the RootNode by reading the Spring configuration file.
      * 
      * @param dataTypeObject key in the file
      * @return RootNode instance or null
      */
     private ComplexNode getComplexNodeBean(String entityName, String propertyName) {
         String convertedEntityName = convertEntityName(entityName);
         String convertedPropertyName = convertPropertyName(entityName, propertyName);
         
         String configLookupKey = convertedEntityName + "." + convertedPropertyName;
         ComplexNode complexNode = getComplexNodeBean(configLookupKey);
         
         if (complexNode == null) {
             return null;
         } else if (entityName.startsWith(ENTITY_NAME_PREFIX)) {
             for (Node node : complexNode.getInnerNodes()) {
                 if (node.getName().equals(propertyName)) {
                     //Could be constant or complex node
                     return (ComplexNode) node;
                 }
             }
             return null;
         }
         return complexNode;
     }
 
     @SuppressWarnings("PMD.EmptyCatchBlock")
     private ComplexNode getComplexNodeBean(String configLookupKey) {
         
         try {
             return (ComplexNode) ctx.getBean(configLookupKey);
         } catch (Exception e) {
             //Do Nothing
         }
         return null;
     }
     
     /**
      * Determines the name of the property to be used for processing.
      * 
      * @param entityName Name of the entity
      * @param propertyName name of the property
      * @return name of the property
      */
     public String convertPropertyName(String entityName, String propertyName) {
         if (!entityName.startsWith(ENTITY_NAME_PREFIX)) {
             return propertyName;
         }
         //_xxEntityxx_gov_nih_nci_cacoresdk_domain_other_datatype_AdDataType_value9        
         return entityName.substring(entityName.lastIndexOf('_') + 1, entityName.length());
     }
 
     /**
      * Determines name of the entity.
      * @param entityName Name of the entity
      * @return  name of the entity
      */
     public String convertEntityName(String entityName) {
         
         if (!entityName.startsWith(ENTITY_NAME_PREFIX)) {
             return entityName;
         }
         //_xxEntityxx_gov_nih_nci_cacoresdk_domain_other_datatype_AdDataType_value9        
         String convertedName = entityName.substring(ENTITY_NAME_PREFIX.length(), entityName.lastIndexOf('_'));
         convertedName = convertedName.replace('_', '.');
         return convertedName;
     }
     
     
     /**
      * Sets the constant values for AD and EN part collection.
      * 
      * @param component object in which the null flavor will be set if the value is null
      * @param value object in which the constant values are to be stored
      * @param roleName name of the component used to locate constant entry in config file
      * @param propertyName name of the value property for which constants are to be set
      */
     @SuppressWarnings("PMD.CyclomaticComplexity")
     public void setConstantPartValues(Object component, Object value, String roleName, String propertyName)
     {
         ComplexNode complexNode = getComplexNodeBean(roleName);
         if (complexNode == null) {
             return;
         } else if (NULL_FLAVOR_ATTRIBUTE.equals(propertyName)) {
             setNullFlavor(component, complexNode);
         } else {
             if (value == null) {
                 return;
             }
             for (Node node : complexNode.getInnerNodes()) {
                 if (propertyName.equals(node.getName()) && node instanceof ComplexNode) {
                     setConstantValues(null, value, node.getName(), (ComplexNode) node, true);
                     return;
                 }
             }
         }
     }
 }

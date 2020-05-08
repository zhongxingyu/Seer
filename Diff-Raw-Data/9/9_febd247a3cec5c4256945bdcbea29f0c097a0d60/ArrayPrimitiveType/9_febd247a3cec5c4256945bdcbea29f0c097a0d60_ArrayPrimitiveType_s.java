 /****************************************************
  * 
  * Universidad Francisco de Paula Santander UFPS
  * Ccuta, Colombia
  * (c) 2014 by UFPS. All rights reserved.
  * 
  ****************************************************/
 
 package classmodeler.domain.uml.types.java;
 
 import org.eclipse.uml2.uml.Type;
 
 /**
  * Metamodel class for primitive array Java type.
  *
  * @author Gabriel Leonardo Diaz, 27.03.2014.
  */
 public class ArrayPrimitiveType extends CollectionGenericType {
   
   public static final String ARRAY_TYPE_NAME = "array";
   
   public ArrayPrimitiveType (Type genericType) {
     super(ARRAY_TYPE_NAME, genericType);
   }
   
   @Override
   public String getName() {
    return getGenericType().getName() + " [] ";
   }
   
 }

 package net.contextfw.web.application.dom;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
import com.sun.istack.internal.Builder;

 public final class ObjectBuilder<T>  {
 
     private final Set<Field> attributeFields = new HashSet<Field>();
     private final Set<Field> textFields = new HashSet<Field>();
     private final String elementName;
 
     private ObjectBuilder(String elementName) {
         this.elementName = elementName;
     }
     
     public void build(DOMBuilder superb, T t) {
         DOMBuilder b = elementName == null ? superb : superb.descend(elementName);
         try {
             for (Field field : attributeFields) {
                 Object obj = field.get(t);
                 if (obj != null) {
                     b.attr(field.getName(), obj);
                 }
             }
             for (Field field : textFields) {
                 Object obj = field.get(t);
                 if (obj != null) {
                     b.descend(field.getName()).text(obj);
                 }
             }
         }
         catch (IllegalArgumentException e) {
             e.printStackTrace();
         }
         catch (IllegalAccessException e) {
             e.printStackTrace();
         }
     }
     
     public static <T> ObjectBuilder<T> forClass (Class<T> clazz) {
         return forClass(clazz, clazz.getSimpleName());
     }
     
     public static <T> ObjectBuilder<T> forClass (Class<T> clazz, String elementName) {
         ObjectBuilder<T> builder = new ObjectBuilder<T>(elementName);
         
         Class<?> currentClass = clazz;
 
         while (currentClass instanceof Object) {
             for (Field field : currentClass.getDeclaredFields()) {
                 if (isUsable(field)) {
                     field.setAccessible(true);
                     builder.attributeFields.add(field);
                 }
             }
             currentClass = currentClass.getSuperclass();
         }
         
         return builder;
     }
 
     private static boolean isUsable(Field field) {
         return !field.getName().equals("serialVersionUID") && !field.getType().isArray() 
                 && !Collection.class.isAssignableFrom(field.getType());
     }
     
     public ObjectBuilder<T> asText(String fieldName) {
         
         Field textField = null;
         
         for (Field attr : attributeFields) {
             if (attr.getName().equals(fieldName)) {
                 textField = attr;
                 break;
             }
         }
         
         if (textField == null) {
             throw new RuntimeException("Could not find field " + fieldName);
         }
         else {
             attributeFields.remove(textField);
             textFields.add(textField);
             return this;
         }
     }
     
     public ObjectBuilder<T> ignore(String fieldName) {
         
         Field textField = null;
         
         for (Field attr : attributeFields) {
             if (attr.getName().equals(fieldName)) {
                 textField = attr;
                 break;
             }
         }
         
         if (textField == null) {
             throw new RuntimeException("Could not find field " + fieldName);
         }
         else {
             attributeFields.remove(textField);
             textFields.remove(textField);
             return this;
         }
     }
 }

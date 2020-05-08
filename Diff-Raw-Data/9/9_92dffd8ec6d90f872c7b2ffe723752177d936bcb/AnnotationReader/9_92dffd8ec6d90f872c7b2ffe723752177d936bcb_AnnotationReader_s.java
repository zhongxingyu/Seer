 package microejb;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 public class AnnotationReader {
     public List<WrappedField> readClass(Class clazz) {
         List list = new ArrayList();
         System.out.println("" + clazz.getDeclaredFields()[0] );
         for ( Field field : clazz.getDeclaredFields()) {
             System.out.println("field.getName() " + field.getName());
             System.out.println("field.getClass() " + field.getClass());
             System.out.println(field.getDeclaredAnnotations()[0]);
            list.add(new WrappedField(field.getName(), field.getDeclaringClass(), field.getDeclaredAnnotations()[0].toString()));
         }
 
         return list;
     }
 
 }

 package me.tehbeard.utils.syringe;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 
 /**
  * Allows injecting of values into classes
  * @author James
  *
  * @param <T> Class type to be injected
  * @param <A> Annotation to inject for
  */
 public abstract class Injector<T,A extends Annotation> {
 
     private Class<A> annotation;
     
     public Injector(Class<A> annotation){
         this.annotation = annotation;
     }
     
     public void inject(T object){
         for(Field field : object.getClass().getDeclaredFields()){
             
             A an = field.getAnnotation(annotation);
             if(an!=null){
                 try{
                 field.setAccessible(true);
                 doInject(an,object, field);
                 field.setAccessible(false);
                 }catch(Exception exception){
                    System.out.println("!COULD NOT INJECT!");
                    exception.printStackTrace();
                 } 
             }
             
         }
     }
     
     protected abstract void doInject(A annotation,T object,Field field) throws IllegalArgumentException,IllegalAccessException;
     
 }

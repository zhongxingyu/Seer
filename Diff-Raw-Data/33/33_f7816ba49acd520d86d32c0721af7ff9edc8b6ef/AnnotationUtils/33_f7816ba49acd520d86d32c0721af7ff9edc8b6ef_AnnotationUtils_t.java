 package org.analogweb.util;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * アノテーションを操作、取得する為のユーティリティです。
  * @author snowgoose
  */
 public final class AnnotationUtils {
 
     /**
      * 指定した型のアノテーションがクラスに存在する場合は、そのアノテーションのインスタンスを
      * 取得します。アノテーションがクラスに存在しない場合は、nullを返します。
      * @param <T> インスタンスを取得するアノテーションの型
      * @param target インスタンスを取得するアノテーションの型
      * @param annotationContainsClass アノテーションのインスタンスを探す対象のクラス
      * @return アノテーションのインスタンス
      */
     public static <T extends Annotation> T findAnnotation(Class<T> target,
             Class<?> annotationContainsClass) {
         Assertion.notNull(annotationContainsClass, "Class must not be null");
        if(Annotation.class.isAssignableFrom(annotationContainsClass)
                && !annotationContainsClass.getPackage().equals(Annotation.class.getPackage())){
            return findAnnotation(target, annotationContainsClass.getAnnotations());
        }
         T annotation = annotationContainsClass.getAnnotation(target);
         if (annotation != null) {
             return annotation;
         }
         for (Class<?> ifc : annotationContainsClass.getInterfaces()) {
             annotation = findAnnotation(target, ifc);
             if (annotation != null) {
                 return annotation;
             }
         }
         if (!Annotation.class.isAssignableFrom(annotationContainsClass)) {
             for (Annotation ann : annotationContainsClass.getAnnotations()) {
                 annotation = findAnnotation(target, ann.annotationType());
                 if (annotation != null) {
                     return annotation;
                 }
             }
         }
         Class<?> superClass = annotationContainsClass.getSuperclass();
         if (superClass == null || superClass == Object.class) {
             return null;
         }
         return findAnnotation(target, superClass);
     }
 
     /**
      * 指定した型のアノテーションが配列の中に存在する場合は、そのアノテーションのインスタンスを
      * 取得します。アノテーションが配列の中に存在しない場合は、nullを返します。
      * @param <T> インスタンスを取得するアノテーションの型
      * @param target インスタンスを取得するアノテーションの型
      * @param annotations アノテーションのインスタンスを探す対象の配列
      * @return アノテーションのインスタンス
      */
     @SuppressWarnings("unchecked")
     public static <T extends Annotation> T findAnnotation(Class<T> target,
             Annotation... annotations) {
         for (Annotation annotation : annotations) {
             Class<?> annotationType = annotation.annotationType();
             if (annotationType.equals(target)
                     || (annotation = findAnnotation(target, annotationType)) != null) {
                 return (T) annotation;
             }
         }
         return null;
     }
 
     public static <T extends Annotation> List<T> findAnnotations(Class<T> target, Method method) {
         List<T> annotations = new ArrayList<T>();
         for (Annotation ann : method.getAnnotations()) {
             T an = findAnnotation(target, ann);
             if (an != null) {
                 annotations.add(an);
             }
         }
         return annotations;
     }
 }

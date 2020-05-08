 package fi.jawsy.jawwa.validation;
 
 import com.google.common.collect.ImmutableList;
 
 public final class Validations {
 
     private Validations() {
     }
 
     public static <E> ImmutableList<E> collectErrors(Validation<E, ?>... validations) {
         ImmutableList.Builder<E> errors = ImmutableList.builder();
         for (Validation<E, ?> v : validations) {
             if (v.isErrors())
                 errors.addAll(v.getErrors());
         }
         return errors.build();
     }
 
     public static boolean hasErrors(Validation<?, ?>... validations) {
         for (Validation<?, ?> v : validations) {
             if (v.isErrors())
                 return true;
         }
         return false;
     }
 
 }

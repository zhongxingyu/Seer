 package name.kazennikov.annotations;
 
 import com.google.common.base.Predicate;
 
 import javax.annotation.Nullable;
 
 /**
  * Helper predicates for annotation query
  * User: kzn
  * Date: 14.10.12
  * Time: 15:01
  */
 public class AnnotationPredicates {
 
     public static Predicate<Annotation> name(final String name) {
         return new Predicate<Annotation>() {
             @Override
             public boolean apply(@Nullable Annotation annotation) {
                 return annotation.getName().equals(name);
             }
         };
     }
 
 
     public static Predicate<Annotation> coextensive(final int start, final int end) {
         return new Predicate<Annotation>() {
             @Override
             public boolean apply(@Nullable Annotation a) {
                 return a.getStart() == start && a.getEnd() == end;
             }
         };
     }
 
     public static Predicate<Annotation> coextensive(Annotation a) {
         return coextensive(a.getStart(), a.getEnd());
     }
 
     public static Predicate<Annotation> within(final int start, final int end) {
         return new Predicate<Annotation>() {
             @Override
             public boolean apply(@Nullable Annotation a) {
                return a.getStart() >= start && a.getEnd() <= end;
             }
         };
     }
     public static Predicate<Annotation> within(Annotation a) {
         return within(a.getStart(), a.getEnd());
     }
 
     public static Predicate<Annotation> covers(final int start, final int end) {
         return new Predicate<Annotation>() {
             @Override
             public boolean apply(@Nullable Annotation a) {
                return a.getStart() <= start && a.getEnd() >= end;
             }
         };
     }
 
     public static Predicate<Annotation> covers(Annotation a) {
         return covers(a.getStart(), a.getEnd());
     }
 
 
     public static Predicate<Annotation> overlaps(final int start, final int end) {
         return new Predicate<Annotation>() {
             @Override
             public boolean apply(@Nullable Annotation a) {
                 return (a.getStart() >= start && a.getStart() <= end) || (a.getEnd() >= start && a.getEnd() <= end);
             }
         };
     }
 
     public static Predicate<Annotation> overlaps(Annotation a) {
         return overlaps(a.getStart(), a.getEnd());
     }
 
 
 
 }

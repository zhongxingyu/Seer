 package languish.lib;
 
 import languish.base.Term;
 import languish.interpreter.DependencyManager;
import languish.interpreter.dep.FileSystemDependencyManager;
 import languish.interpreter.error.DependencyUnavailableError;
 import languish.util.Lambda;
 
 import com.google.common.collect.ImmutableList;
 
 public class Lists {
   private static final DependencyManager DEPMAN =
       new FileSystemDependencyManager(ImmutableList.of("languish"));
   private static final Term LIB;
 
   static {
     try {
       LIB = DEPMAN.getResource("lang/lists");
     } catch (DependencyUnavailableError e) {
       throw new LanguishLoadError(e);
     }
   }
 
   public static Term map() {
     return Lambda.car(LIB);
   }
 
   public static Term reduce() {
     return Lambda.car(Lambda.cdr(LIB));
   }
 
   public static Term member() {
     return Lambda.car(Lambda.cdr(Lambda.cdr(LIB)));
   }
 
   private Lists() {
   }
 }

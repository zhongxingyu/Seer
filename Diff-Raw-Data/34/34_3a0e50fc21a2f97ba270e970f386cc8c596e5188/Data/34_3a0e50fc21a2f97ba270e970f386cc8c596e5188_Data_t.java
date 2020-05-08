 package languish.lib;
 
 import languish.base.Term;
import languish.depman.FileSystemDependencyManager;
 import languish.interpreter.DependencyManager;
 import languish.interpreter.error.DependencyUnavailableError;
 import languish.util.Lambda;
 
 import com.google.common.collect.ImmutableList;
 
 public class Data {
   private static final DependencyManager DEPMAN =
       new FileSystemDependencyManager(ImmutableList.of("languish"));
   private static final Term LIB;
 
   static {
     try {
       LIB = DEPMAN.getResource("lang/data");
     } catch (DependencyUnavailableError e) {
       throw new LanguishLoadError(e);
     }
   }
 
   public static Term equals() {
     return Lambda.car(LIB);
   }
 
 }

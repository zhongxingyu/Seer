 package languish.depman;
 
 import java.util.Map;
 
 import languish.base.NativeFunction;
 import languish.base.Term;
 import languish.base.Terms;
 import languish.interpreter.DependencyManager;
 import languish.interpreter.error.DependencyUnavailableError;
 
 public class NativeFunctionDependencyManager implements DependencyManager {
   private final String prefix;
   private final Map<String, NativeFunction> functions;
 
   public NativeFunctionDependencyManager(String prefix,
       Map<String, NativeFunction> functions) {
     this.prefix = prefix;
     this.functions = functions;
   }
 
   @Override
   public Term getResource(String resourceName)
       throws DependencyUnavailableError {
     if (!hasResource(resourceName)) {
       throw new DependencyUnavailableError(resourceName);
     }
 
     NativeFunction nativeFunc =
         functions.get(resourceName.substring(prefix.length()));
 
    return Terms.abs(Terms.nativeApply(nativeFunc, Terms.ref(1)));
   }
 
   @Override
   public boolean hasResource(String resourceName) {
     if (!resourceName.startsWith(prefix)) {
       return false;
     }
 
     return functions.containsKey(resourceName.substring(prefix.length()));
   }
 
 }

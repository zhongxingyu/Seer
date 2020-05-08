 package xhl.core.validator;
 
 import xhl.core.Error;
 import xhl.core.elements.Symbol;
 
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ValidationResult {
     private final Type type;
     private final List<Error> errors;
     private Map<Symbol, Type> defined;
 
     public ValidationResult(Type type, List<Error> errors) {
        this(type, errors, new HashMap<Symbol, Type>());
     }
 
     public ValidationResult(Type type, List<Error> errors, Map<Symbol,
             Type> defined) {
         this.type = type;
        this.errors = errors;
         this.defined = defined;
     }
 
     public Type getType() {
         return type;
     }
 
     public List<Error> getErrors() {
         return errors;
     }
 
     public Map<Symbol, Type> getDefined() {
         return defined;
     }
 }

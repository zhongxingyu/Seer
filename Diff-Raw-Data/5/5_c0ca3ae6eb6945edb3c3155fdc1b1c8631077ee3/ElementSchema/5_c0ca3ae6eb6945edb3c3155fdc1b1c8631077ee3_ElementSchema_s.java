 package xhl.core.validator;
 
 import java.util.List;
 
 import xhl.core.Environment;
 import xhl.core.Error;
 import xhl.core.elements.*;
 import xhl.core.validator.Validator.ValidationResult;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 public class ElementSchema {
     private final Symbol symbol;
     private List<ParamSpec> params = newArrayList();
     private Type type = Type.Null;
     private final List<DefSpec> defines = newArrayList();
 
     public ElementSchema(Symbol sym) {
         this.symbol = sym;
     }
 
     public boolean isVariadic() {
         return params.get(params.size()-1).variadic;
     }
 
     public Environment<Type> definedSymbols(SList args, boolean onlyBackward) {
         Environment<Type> symbols = new Environment<Type>();
         for (DefSpec def : defines) {
             if (onlyBackward && !def.backward)
                 continue;
             ParamSpec argspec = params.get(def.arg - 1);
             if (argspec.method == PassingMethod.SYM
                    && argspec.type == Type.Symbol) {
                 Expression arg = args.get(def.arg - 1);
                 if (!(arg instanceof Symbol))
                     continue;
                 Symbol name = (Symbol) arg;
                 Type type = def.type;
                 symbols.put(name, type);
             } else if (argspec.method == PassingMethod.SYM
                    && argspec.type == Type.Map) {
                 Expression arg = args.get(def.arg - 1);
                 if (!(arg instanceof SMap))
                     continue;
                 SMap map = (SMap) arg;
                 Type type = def.type;
                 for (Expression key : map.keySet()) {
                     Symbol sym = (Symbol) key;
                     symbols.put(sym, type);
                 }
             }
         }
         return symbols;
     }
 
     public ValidationResult checkCombination(Validator validator, SList tail) {
         List<Error> errors = newArrayList();
         // Check number of arguments
         int minArgsSize = isVariadic() ? params.size() - 1 : params.size();
         if (tail.size() < minArgsSize
                 || (!isVariadic() && tail.size() > minArgsSize)) {
             errors.add(new Error(tail.getPosition(),
                     "Wrong number of arguments"));
             return new ValidationResult(type, errors);
         }
         // Check arguments types
         for (int i = 0; i < minArgsSize; i++) {
             errors.addAll(checkArgument(validator, params.get(i), tail.get(i)));
         }
         // Variadic arguments
         if (isVariadic()) {
             List<Expression> varargs = tail.subList(params.size(), tail.size());
             ParamSpec spec = params.get(params.size()-1);
             for (Expression arg : varargs)
                 errors.addAll(checkArgument(validator, spec, arg));
         }
         return new ValidationResult(type, errors);
     }
 
     private List<Error> checkArgument(Validator validator, ParamSpec spec,
             Expression arg) {
         List<Error> errors = newArrayList();
         Type argtype;
         if (spec.method == PassingMethod.SYM)
             argtype = Type.typeOfElement(arg);
         else
             argtype = validator.check(arg);
         if (!argtype.is(spec.type))
             errors.add(new Error(arg.getPosition(),
                     "Wrong type of an argument (expected " + spec.type
                             + ", found " + argtype + ")"));
         return errors;
     }
 
     public Symbol getSymbol() {
         return symbol;
     }
 
     public List<ParamSpec> getParams() {
         return params;
     }
 
     public void setParams(List<ParamSpec> params) {
         this.params = params;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public List<DefSpec> getDefines() {
         return defines;
     }
 
     public void addDefine(DefSpec spec) {
         defines.add(spec);
     }
 
     public static enum PassingMethod { VAL, SYM }
 
     public static class ParamSpec {
         /** Argument specification */
         public final Type type;
         public final PassingMethod method;
         public final boolean variadic;
         public final boolean block;
 
         public static ParamSpec val(Type type) {
             return new ParamSpec(PassingMethod.VAL, type, false, false);
         }
 
         public static ParamSpec sym(Type type) {
             return new ParamSpec(PassingMethod.SYM, type, false, false);
         }
 
         public static ParamSpec variadic(ParamSpec spec) {
             return new ParamSpec(spec.method, spec.type, true, false);
         }
 
         public static ParamSpec block(ParamSpec spec) {
             return new ParamSpec(spec.method, spec.type, false, true);
         }
 
         private ParamSpec(PassingMethod method, Type type, boolean variadic,
                 boolean block) {
             this.method = method;
             this.type = type;
             this.variadic = variadic;
             this.block = block;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (!(obj instanceof ParamSpec))
                 return false;
             ParamSpec that = (ParamSpec) obj;
             return this.type.equals(that.type)
                     && this.method.equals(that.method);
         }
     }
 
     public static class DefSpec {
         public final Type type;
         public final int arg;
         public final boolean backward;
 
         public DefSpec(int arg, Type type) {
             this(arg, type, false);
         }
 
         public DefSpec(int arg, Type type, boolean forward) {
             this.arg = arg;
             this.type = type;
             this.backward = forward;
         }
     }
 }

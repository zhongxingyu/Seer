 package xhl.core.validator;
 
 import java.util.List;
 import java.util.Map;
 
 import xhl.core.Error;
 import xhl.core.SymbolTable;
 import xhl.core.elements.*;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 
 public class Validator implements ElementVisitor<Type> {
     private final SymbolTable<Type> table = new SymbolTable<Type>();
     private final Map<Symbol, ElementSchema> elements = newHashMap();
     private final List<Error> errors = newArrayList();
 
     public void addElements(Schema schema) {
         for (ElementSchema element : schema) {
             this.elements.put(element.getSymbol(), element);
             if (element.getParams().size() == 0)
                 table.put(element.getSymbol(), element.getType());
             else
                 table.put(element.getSymbol(), Type.Element);
         }
     }
 
     public List<Error> getErrors() {
         return errors;
     }
 
     public Type check(Expression expression) {
         return expression.accept(this);
     }
 
     @Override
     public Type visit(SNumber num) {
         return Type.Number;
     }
 
     @Override
     public Type visit(SBoolean bool) {
         return Type.Boolean;
     }
 
     @Override
     public Type visit(SString str) {
         return Type.String;
     }
 
     @Override
     public Type visit(SList lst) {
         for (Expression exp : lst) {
             check(exp);
         }
         return Type.List;
     }
 
     @Override
     public Type visit(SMap map) {
         for (Expression key : map.keySet()) {
             check(map.get(key));
         }
         return Type.Map;
     }
 
     @Override
     public Type visit(Symbol sym) {
         if (table.containsKey(sym))
             if (table.get(sym).is(Type.Element)) {
                 ElementSchema schema = elements.get(sym);
                 ValidationResult result =
                         schema.checkCombination(this,
                                 new SList(sym.getPosition()));
                 errors.addAll(result.errors);
                 return result.type;
             } else
                 return table.get(sym);
         else
             return Type.AnyType;
     }
 
     @Override
     public Type visit(Combination cmb) {
         if (!(cmb.get(0) instanceof Symbol))
             return Type.AnyType;
         Symbol head = (Symbol) cmb.get(0);
         if (!elements.containsKey(head))
             return Type.AnyType;
         ElementSchema schema = elements.get(head);
         table.putAll(schema.definedSymbols(cmb.tail(), false));
         ValidationResult result = schema.checkCombination(this, cmb.tail());
         errors.addAll(result.errors);
         return result.type;
     }
 
     @Override
     public Type visit(Block blk) {
         collectForwardDefunitions(blk);
         for (Expression exp : blk)
             check(exp);
         return Type.Block;
     }
 
     private void collectForwardDefunitions(Block blk) {
         for (Expression exp : blk) {
             try {
                 Combination cmb = (Combination) exp;
                 Symbol head = (Symbol) cmb.head();
                 SList tail = cmb.tail();
                 ElementSchema schema = elements.get(head);
                table.putAll(schema.definedSymbols(tail, true));
             } catch (ClassCastException e) {
                 // Ignore cases, where types did not match expectations
             }
         }
     }
 
     static class ValidationResult {
         public final Type type;
         public final List<Error> errors;
 
         public ValidationResult(Type type, List<Error> errors) {
             this.type = type;
             this.errors = errors;
         }
     }
 }

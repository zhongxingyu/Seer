 package cs444.parser.symbols.ast.factories.expressions;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.Terminal;
 import cs444.parser.symbols.ast.NameSymbol;
 import cs444.parser.symbols.ast.TypeSymbol;
 import cs444.parser.symbols.ast.expressions.CastExpressionSymbol;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public class CastExpressionFactory extends ASTSymbolFactory{
 
     private static final String CAST_EXPRESSION = "CASTEXPRESSION";
 
     private static final Set<String> validCastTypes= new HashSet<String>();
 
     static{
         validCastTypes.add("CHAR");
         validCastTypes.add("BYTE");
         validCastTypes.add("SHORT");
         validCastTypes.add("INT");
         validCastTypes.add("BOOLEAN");
         validCastTypes.add("Name");
     }
 
     @Override
     protected ISymbol convert(ISymbol from) throws UnsupportedException, IllegalModifierException {
         if(!from.getName().equals(CAST_EXPRESSION)){
             return from;
         }
         ANonTerminal castExpr = (ANonTerminal) from;
 
         boolean isArray = false;
         if (castExpr.children.size() == 3 && castExpr.children.get(1).getName().equalsIgnoreCase(JoosNonTerminal.DIMS)){
             isArray = true;
         }
 
         if (castExpr.children.size() != 2 && !isArray)
             throw new UnsupportedException("Cast expression doesn't have two operands");
 
         ISymbol castType = castExpr.children.get(0);
         String firstChild = castType.getName();
         if (!validCastTypes.contains(firstChild)){
             throw new UnsupportedException("Something other than Name or primitive type in Cast Expression.");
         }
 
         TypeSymbol type;
         if (castType instanceof NameSymbol){
             type = new TypeSymbol(((NameSymbol) castType).value, isArray, true);
         }else if (castType instanceof Terminal){
             type = new TypeSymbol(((Terminal) castType).value, isArray, true);
         }else{
             throw new UnsupportedException("Cast Expression has invalid cast type.");
         }
 
 
        ISymbol operandExpression = castExpr.children.get(1);
 
         return new CastExpressionSymbol(type, operandExpression);
     }
 }

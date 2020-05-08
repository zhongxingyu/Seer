 package IntermediateCodeGeneration.AST;
 
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.ClassEntry;
 import IntermediateCodeGeneration.SymbolTable.ConstructorEntry;
 import IntermediateCodeGeneration.SymbolTable.MethodEntry;
 import IntermediateCodeGeneration.Token;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 
 /**
  * Representacion de un nodo return-expresion
  *
  * @author Ramiro Agis
  * @author Victoria Martinez de la Cruz
  */
 public class ReturnExpNode extends ReturnNode {
 
     protected ExpressionNode expression;
 
     public ReturnExpNode(SymbolTable symbolTable, ExpressionNode expression, Token token) {
         super(symbolTable, token);
         this.expression = expression;
     }
 
     @Override
     public void checkNode() throws SemanticException {
         expression.checkNode();
 
         String currentClass = symbolTable.getCurrentClass();
         String currentService = symbolTable.getCurrentService();
 
         ConstructorEntry currentConstructorEntry = symbolTable.getClassEntry(currentClass).getConstructorEntry();
         MethodEntry currentMethodEntry = symbolTable.getClassEntry(currentClass).getMethodEntry(currentService);
 
         if (currentConstructorEntry.getName().equals(currentService)) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: Un constructor no puede tener un retorno.");
         }
 
        if (!expression.getExpressionType().checkConformity(currentMethodEntry.getReturnType())) {
             if (currentMethodEntry.getReturnType().getTypeName().equals("void")) {
                 throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: Un metodo de tipo void no puede retornar un valor.");
             }
 
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El tipo de la expresion retornada no es conforme al tipo de retorno del metodo actual. El tipo de la expresion retornada es '" + expression.getExpressionType().getTypeName() + "' y el tipo de retorno del metodo es '" + symbolTable.getClassEntry(currentClass).getMethodEntry(currentService).getReturnType().getTypeName() + "'.");
         }
 
         this.setSentenceType(expression.getExpressionType());
     }
 
     @Override
     public void generateCode() throws SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         String currentMethod = symbolTable.getCurrentService();
         ClassEntry currentClassEntry = symbolTable.getClassEntry(currentClass);
         MethodEntry currentMethodEntry = currentClassEntry.getMethodEntry(currentMethod);
 
         int parametersCount = currentMethodEntry.getParameters().size();
         int localVariablesCount = currentMethodEntry.getLocalVariables().size();
         int offsetRet = parametersCount + 1;
 
         expression.setICG(ICG);
         expression.generateCode();
 
         ICG.GEN(".CODE");
         ICG.GEN("; Retorno de expresion del metodo '" + currentMethod + "' de la clase '" + currentClass + "'");
         offsetRet = offsetRet + 3; // parametros, variables locales, puntero de retorno, enlace dinamico y this.
 
         ICG.GEN("STORE", offsetRet, "ReturnExpNode. Almacenamos el retorno del metodo '" + currentMethod + "' de la clase '" + currentClass + "'.");
 
         if (localVariablesCount > 0) {
             // El metodo tiene variables locales
             ICG.GEN("FMEM", localVariablesCount, "ReturnExpNode. Liberamos el espacio usado por las variables locales del metodo '" + currentMethod + "' de la clase '" + currentClass + "'.");
         }
 
         ICG.GEN("STOREFP", "ReturnExpNode. Actualizamos el FP para que apunte al RA del llamador");
         ICG.GEN("RET", parametersCount + 1, "ReturnExpNode. Retornamos de la unidad liberando el espacio que ocupaban los parametros y el THIS del metodo '" + currentMethod + "' de la clase '" + currentClass + "'.");
     }
 }

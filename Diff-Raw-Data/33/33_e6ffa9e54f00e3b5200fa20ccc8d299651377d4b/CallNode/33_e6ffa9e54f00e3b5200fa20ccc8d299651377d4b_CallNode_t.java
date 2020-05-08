 package IntermediateCodeGeneration.AST;
 
 import IntermediateCodeGeneration.SemanticException;
 import IntermediateCodeGeneration.SymbolTable.ClassEntry;
 import IntermediateCodeGeneration.SymbolTable.ParameterEntry;
 import IntermediateCodeGeneration.SymbolTable.Type.Type;
 import IntermediateCodeGeneration.Token;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 import java.util.Collection;
 import java.util.LinkedList;
 
 /**
  * Representacion de un nodo llamada
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class CallNode extends PrimaryNode {
 
     protected Token id;
     // callerType representa el tipo del llamador - e.g. id.method1(), la clase de Id.
     // callReturnType representa el tipo de retorno del metodo - e.g. si method1() retorna un objeto de clase Clase
     protected Type callerType, callReturnType;
     protected LinkedList<ExpressionNode> actualArgs;
     private boolean VT, isSystem, isStatic; // VT en Pila, es metodo estatico, es metodo de System
     private String staticMethodClass;
 
     public CallNode(SymbolTable symbolTable, Token id, LinkedList<ExpressionNode> actualArgs, Token token) {
         super(symbolTable, token);
         this.id = id;
         this.actualArgs = actualArgs; // actual arguments
         VT = false;
         isStatic = false;
         isSystem = false;
         staticMethodClass = null;
     }
 
     /**
      * Retorna el identificador asociado a la llamada
      *
      * @return id
      */
     public Token getId() {
         return id;
     }
 
     public void setCallerType(Type callerType) {
         this.callerType = callerType;
     }
 
     public Type getCallReturnType() {
         return callReturnType;
     }
 
     public void setVT(boolean VT) {
         this.VT = VT;
     }
 
     public void setStatic(boolean isStatic, String staticMethodClass) {
         this.isStatic = isStatic;
         this.staticMethodClass = staticMethodClass;
     }
 
     public void setSystem(boolean isSystem) {
         this.isSystem = isSystem;
     }
 
     @Override
     public void checkNode() throws SemanticException {
         checkId();
 
         for (ExpressionNode actualArg : actualArgs) {
             actualArg.checkNode();
         }
 
         // Compatibilidad con argumentos formales
         controlFormalArgs();
     }
 
     /**
      * Controla que sea un metodo de una clase (cualquiera en la tabla de
      * simbolos) Si es un constructor o no esta definido, ocurrira un error Se
      * utiliza en el nodo de (CallNode)
      *
      * @throws SemanticException
      */
     private void checkId() throws SemanticException {
         // o bien es un metodo de la clase (una clase cualquiera de la tabla de simbolos)
         // o bien representa a un constructor (lo que seria un error)
         String callerTypeName = callerType.getTypeName();
 
         if (symbolTable.isMethodInClass(callerTypeName, id.getLexeme())) {
             callReturnType = symbolTable.getClassEntry(callerTypeName).getMethodEntry(id.getLexeme()).getReturnType();
             setExpressionType(callReturnType);
         } else if (symbolTable.isConstructor(id.getLexeme()) != null) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: No puede realizarse una llamada a un constructor.");
         } else {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: El metodo invocado no esta declarado en clase " + callerTypeName);
         }
     }
 
     /**
      * Control de conformidad de tipos entre argumentos formales y argumentos
      * actuales de un metodo
      *
      * @throws SemanticException
      */
     private void controlFormalArgs() throws SemanticException {
         String currentClass = symbolTable.getCurrentClass();
         String callerTypeName = callerType.getTypeName();
         Collection<ParameterEntry> formalArgs = symbolTable.getClassEntry(callerTypeName).getMethodEntry(id.getLexeme()).getParameters().values();
        
 
         if (formalArgs.size() != actualArgs.size()) {
             throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: Las listas de argumentos actuales y formales para el metodo " + id.getLexeme() + " de la clase " + currentClass + " tienen diferente longitud.");
         }
        
        int index = 0;
         for (ParameterEntry formalArg : formalArgs) {
             actualArgs.get(index).checkNode();
             if (!formalArg.getType().checkConformity(actualArgs.get(index).getExpressionType())) {
                int position = index + 1;
                throw new SemanticException("Linea: " + token.getLineNumber() + " - Error semantico: En la llamada al metodo '" + id.getLexeme() + "' el tipo del argumento actual en la posicion (" + position + ") no conforma con el tipo del argumento formal."
                        + " El tipo del argumento actual es " + actualArgs.get(index).getExpressionType().getTypeName() + " y el tipo del argumento formal es " + formalArg.getType().getTypeName() + ".");
             }
             index++;
         }
     }
 
     @Override
     public void generateCode() throws SemanticException {
       
         // Los controles sobre el metodo se realizan durante el checkNode()
         String currentClass = symbolTable.getCurrentClass();
 
         ICG.GEN(".CODE");
 
         if (!callReturnType.getTypeName().equals("void")) {
             ICG.GEN("RMEM", 1, "Reservamos una locacion de memoria para el resultado del metodo '" + id.getLexeme() + "' de la clase '" + currentClass + "'");
             ICG.GEN("SWAP", "Acomodamos el THIS haciendo un SWAP con RETVAL");
         }
 
         for (ExpressionNode expression : actualArgs) {
             expression.setICG(ICG);
             expression.generateCode();
             ICG.GEN("SWAP", "Acomodamos el THIS cada vez que generamos el codigo para un parametro.");
         }
 
         ICG.GEN(".CODE");
 
         if (isSystem) {
             ICG.GEN("PUSH L_MET_System1_" + id.getLexeme());
             ICG.GEN("CALL", "Llamada al metodo '" + id.getLexeme() + "' de System.");
         } else {
             if (isStatic) {
                 ClassEntry classEntry = symbolTable.getClassEntry(staticMethodClass);
                 ICG.GEN("PUSH VT_" + staticMethodClass + classEntry.getClassNumber());
             }
 
             if (!VT) {
                 ICG.GEN("DUP", "CallNode. Duplicamos la referencia al CIR para utilizarla en el LOADREF al asociar la VT para invocar al metodo '" + id.getLexeme() + "'.");
                 ICG.GEN("LOADREF", 0, "CallNode. El offset de la VT en el CIR es siempre 0. Accedemos a la VT.");
             }
 
             int offsetId = symbolTable.getClassEntry(callerType.getTypeName()).getMethodEntry(id.getLexeme()).getOffset();
            System.out.println(id.getLexeme() + " " + offsetId + " caller type: " + callerType.getTypeName());
             ICG.GEN("LOADREF", offsetId, "CallNode. Recuperamos la direccion del metodo '" + id.getLexeme() + "'.");
             ICG.GEN("CALL", "CallNode. Llamamos al metodo '" + id.getLexeme() + "'.");
         }
     }
 }

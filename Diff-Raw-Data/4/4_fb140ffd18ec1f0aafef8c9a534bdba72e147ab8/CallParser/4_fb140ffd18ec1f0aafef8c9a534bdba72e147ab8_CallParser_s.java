 package de.weltraumschaf.caythe.frontend.pascal.parsers;
 
 import de.weltraumschaf.caythe.intermediate.typeimpl.TypeChecker;
 import de.weltraumschaf.caythe.intermediate.Definition;
 import java.util.EnumSet;
 import de.weltraumschaf.caythe.intermediate.TypeForm;
 import de.weltraumschaf.caythe.intermediate.TypeSpecification;
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.TokenType;
 import de.weltraumschaf.caythe.frontend.pascal.PascalErrorCode;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTokenType;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTopDownParser;
 import de.weltraumschaf.caythe.intermediate.CodeFactory;
 import de.weltraumschaf.caythe.intermediate.CodeNode;
 import de.weltraumschaf.caythe.intermediate.RoutineCode;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 
 import de.weltraumschaf.caythe.intermediate.codeimpl.CodeNodeTypeImpl;
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.DefinitionImpl;
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.Predefined;
 import de.weltraumschaf.caythe.intermediate.typeimpl.TypeFormImpl;
 import java.util.ArrayList;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.RoutineCodeImpl.*;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.SymbolTableKeyImpl.*;
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeNodeTypeImpl.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class CallParser extends StatementParser {
 
     public CallParser(PascalTopDownParser parent) {
         super(parent);
     }
 
     @Override
     public CodeNode parse(Token token) throws Exception {
         SymbolTableEntry pfId = symbolTableStack.lookup(token.getText().toLowerCase());
         RoutineCode routineCode = (RoutineCode) pfId.getAttribute(ROUTINE_CODE);
         StatementParser callParser;
 
         if (( routineCode == DECLARED ) || ( routineCode == FORWARD )) {
             callParser = new CallDeclaredParser(this);
         } else {
             callParser = new CallStandardParser(this);
         }
 
         return callParser.parse(token);
     }
     // Synchronization set for the , token.
     private static final EnumSet<PascalTokenType> COMMA_SET =
             ExpressionParser.EXPRESSION_START_SET.clone();
 
     static {
         COMMA_SET.add(PascalTokenType.COMMA);
         COMMA_SET.add(PascalTokenType.RIGHT_PAREN);
     }
 
     protected CodeNode parseActualParameters(Token token, SymbolTableEntry pfId, boolean isDecalred, boolean isReadReadln, boolean isWriteWriteln) throws Exception {
         ExpressionParser            expressionParser = new ExpressionParser(this);
         CodeNode                    parmsNode        = CodeFactory.createCodeNode(PARAMETERS);
         ArrayList<SymbolTableEntry> formalParms      = null;
         int paramCount = 0;
         int paramIndex = -1;
 
         if (isDecalred) {
             formalParms = (ArrayList<SymbolTableEntry>) pfId.getAttribute(ROUTINE_PARAMS);
             paramCount  = formalParms != null
                           ? formalParms.size()
                           : 0;
         }
 
         if (token.getType() != PascalTokenType.LEFT_PAREN) {
             if (paramCount != 0) {
                 errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARMS, this);
             }
 
             return null;
         }
 
         token = nextToken(); // consume opening (
 
         // Loop to parse each actual parameter.
         while (token.getType() != PascalTokenType.RIGHT_PAREN) {
             CodeNode actualNode = expressionParser.parse(token);
             // Declared procedure or function: Check the number of actual
             // parameters, and check each actual parameter against the
             // corresponding formal parameter.
             if (isDecalred) {
                 if (++paramIndex < paramCount) {
                     SymbolTableEntry formalId = formalParms.get(paramIndex);
                     checkActualParameter(token, formalId, actualNode);
                 } else if (paramIndex == paramCount) {
                     errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARMS, this);
                 }
             }
             // read or readln: Each actual variable must be a variable that is
             //                 a scalar, boolean or subrange of integer,
             else if (isReadReadln) {
                 TypeSpecification type = actualNode.getTypeSpecification();
                TypeForm form = type.getForm();
 
                 if (!( ( actualNode.getType() == CodeNodeTypeImpl.VARIABLE )
                         && ( ( form == TypeFormImpl.SCALAR )
                         || ( type == Predefined.booleanType )
                         || ( ( form == TypeFormImpl.SUBRANGE )
                         && ( type.baseType() == Predefined.integerType ) ) ) )) {
                     errorHandler.flag(token, PascalErrorCode.INVALID_VAR_PARAM, this);
                 }
             }
             // write or writeln: The type of each actual parameter must be a
             //                   scalar, boolean, or a Pascal string. Parse any field
             //                   width and precision.
             else if (isWriteWriteln) {
                 CodeNode exprNode = actualNode;
                 actualNode = CodeFactory.createCodeNode(WRITE_PARM);
                 actualNode.addChild(exprNode);
                 TypeSpecification type = exprNode.getTypeSpecification().baseType();
                 TypeForm form = type.getForm();
 
                 if (!( ( form == TypeFormImpl.SCALAR ) || ( type == Predefined.booleanType )
                         || ( type.isPascalString() ) )) {
                     errorHandler.flag(token, PascalErrorCode.INCOMPATIBLE_TYPES, this);
                 }
 
                 // Optional field width.
                 token = currentToken();
                 actualNode.addChild(parseWriteSpec(token));
                 // Optional precision.
                 token = currentToken();
                 actualNode.addChild(parseWriteSpec(token));
             }
 
             parmsNode.addChild(actualNode);
             token = synchronize(COMMA_SET);
             TokenType tokenType = token.getType();
 
             // Look for the comma.
             if (tokenType == PascalTokenType.COMMA) {
                 token = nextToken();
             } else if (ExpressionParser.EXPRESSION_START_SET.contains(tokenType)) {
                 errorHandler.flag(token, PascalErrorCode.MISSING_COMMA, this);
             } else if (tokenType != PascalTokenType.RIGHT_PAREN) {
                 token = synchronize(ExpressionParser.EXPRESSION_START_SET);
             }
         }
 
         token = nextToken(); // Consume closing )
 
         if (parmsNode.getChildren().isEmpty() || ( isDecalred && ( paramIndex != paramCount - 1 ) )) {
             errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARMS, this);
         }
 
         return parmsNode;
     }
 
     private void checkActualParameter(Token token, SymbolTableEntry formalId, CodeNode actualNode) {
         Definition formalDefn = formalId.getDefinition();
         TypeSpecification formalType = formalId.getTypeSpecification();
         TypeSpecification actualType = actualNode.getTypeSpecification();
 
         // VAR parameter: The actual parameter must be a variable of the same
         //                type as the formal parameter.
         if (formalDefn == DefinitionImpl.VAR_PARAM) {
             if (( actualNode.getType() != CodeNodeTypeImpl.VARIABLE )
                     || ( actualType != formalType )) {
                 errorHandler.flag(token, PascalErrorCode.INVALID_VAR_PARAM, this);
             }
         } // Value parameter: The actual parameter must be assignment-compatible
         //                  with the formal parameter.
         else if (!TypeChecker.areAssignmentCompatible(formalType, actualType)) {
             errorHandler.flag(token, PascalErrorCode.INCOMPATIBLE_TYPES, this);
         }
     }
 
     private CodeNode parseWriteSpec(Token token) throws Exception {
         if (token.getType() == PascalTokenType.COLON) {
             token = nextToken();  // consume :
 
             ExpressionParser expressionParser = new ExpressionParser(this);
             CodeNode specNode = expressionParser.parse(token);
 
             if (specNode.getType() == INTEGER_CONSTANT) {
                 return specNode;
             }
             else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_NUMBER, this);
                 return null;
             }
         }
         else {
             return null;
         }
     }
 }

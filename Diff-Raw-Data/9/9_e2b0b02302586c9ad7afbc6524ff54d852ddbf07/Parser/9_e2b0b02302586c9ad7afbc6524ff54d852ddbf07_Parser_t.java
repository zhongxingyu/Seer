 package compiler.parser;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import compiler.Scanner;
 import compiler.Token;
 import compiler.TokenType;
 import compiler.analyzer.Analyzer;
 import compiler.analyzer.LabelGenerator;
 import compiler.parser.symbol.Attribute;
 import compiler.parser.symbol.Classification;
 import compiler.parser.symbol.FunctionRow;
 import compiler.parser.symbol.Mode;
 import compiler.parser.symbol.ProcedureRow;
 import compiler.parser.symbol.Row;
 import compiler.parser.symbol.SymbolTable;
 import compiler.parser.symbol.Type;
 
 public class Parser {
    private static final boolean DEBUG = false;
 
     Token lookAhead;
     Token lookAhead2;
     Scanner scanner;
     PrintWriter out;
     Stack<SymbolTable> symboltables;
     Analyzer analyzer;
 
     public Parser(Scanner scanner, String outputFile) throws IOException {
         this.scanner = scanner;
         symboltables = new Stack<SymbolTable>();
         lookAhead = scanner.getToken();
         lookAhead2 = scanner.getToken();
         try {
             out = new PrintWriter(new FileWriter("parse-tree"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         analyzer = new Analyzer(symboltables, new File(outputFile));
 
         systemGoal();
         out.flush();
         out.close();
         System.out.println("The input program parses!");
     }
 
     private void getToken() {
         lookAhead = lookAhead2;
         lookAhead2 = scanner.getToken();
     }
 
     public void match(TokenType tokenInput) {
         if (lookAhead.getType() == tokenInput) {
             if (DEBUG) {
                 System.out.println("Matched token: " + lookAhead.getType() + " with " + tokenInput);
             }
             //get next lookahead
             getToken();
         } else {
             matchError(tokenInput.toString());
         }
     }
 
     public void matchError(String expectedToken) {
         System.out.println("Match error found on line " + lookAhead.getLineNumber() + ", column "
                 + lookAhead.getColumnNumber() + ": expected '" + expectedToken
                 + "', but found '" + lookAhead.getLexeme() + "'");
     }
 
     public void syntaxError(String expectedToken) {
         System.out.println("Syntax error found on line " + lookAhead.getLineNumber() + ", column "
                 + lookAhead.getColumnNumber() + ": expected one of the following tokens {" + expectedToken
                 + "}, but found '" + lookAhead.getLexeme() + "'");
         if (DEBUG) {
             System.out.println("Current lookahead token: " + lookAhead.toString());
         }
         out.flush();
         out.close();
         System.exit(1);
     }
 
     public static void semanticError(String errorMsg) {
         System.out.println("Semantic Error: " + errorMsg);
         System.exit(1);
     }
 
     private boolean addSymbolTable(String scopeName, String branchLbl)
     {
         boolean exists = false;
         for (SymbolTable t : symboltables)
         {
             if (t.getScopeName().equalsIgnoreCase(scopeName))
             {
                 exists = true;
                 break;
             }
         }
         if (!exists)
         {
             symboltables.push(new SymbolTable(scopeName, branchLbl));
             return true;
         }
         else
         {
             semanticError("Symbol table with name " + scopeName + " already exists");
         }
         return false;
     }
 
     private void removeSymbolTable(String scopeName)
     {
         symboltables.pop();
         SymbolTable.decrementNestingLevel();
     }
 
     private void removeSymbolTable() {
         symboltables.pop();
         SymbolTable.decrementNestingLevel();
     }
 
     public void printSymbolTables()
     {
         for (SymbolTable st : symboltables)
         {
             st.print();
         }
     }
 
     public void debug() {
         if (DEBUG) {
             System.out.println("\tExpanding nonterminal: " + Thread.currentThread().getStackTrace()[2].getMethodName()
                     + "() and current lookahead: " + lookAhead.getType());
         }
     }
 
     public void lambda() {
         if (DEBUG) {
             System.out.println("\tExpanding lambda rule in "
                     + Thread.currentThread().getStackTrace()[2].getMethodName()
                     + "()");
         }
     }
 
     public void systemGoal()
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_PROGRAM: //1 SystemGoal -> Program mp_eof
             out.println("1");
             program();
             match(TokenType.MP_EOF);
             break;
         default:
             syntaxError("program");
         }
     }
 
     public void program()
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_PROGRAM: //2 Program -> Programheading #create_symbol_table(program_identifier_rec) mp_scolon Block mp_period
             out.println("2");
             String scopeName = programHeading();
             String branch = LabelGenerator.getNextLabel();
             SemanticRec rec = new SemanticRec(RecordType.LABEL, branch);
             addSymbolTable(scopeName, branch);
             symboltables.peek().addDataSymbolToTable(Classification.DISREG, "Old Display Register Value",
                     new Attribute(Type.STRING, Mode.VALUE));
             match(TokenType.MP_SCOLON);
             analyzer.gen_branch_unconditional_to(rec); //after the activation record branch to where the begin block starts
 
             block(scopeName, new SemanticRec(RecordType.BLOCK, "program"), rec); //sends in the branch lbl and that it is a program block type
 
             match(TokenType.MP_PERIOD);
 
             SemanticRec name_rec = new SemanticRec(RecordType.SYM_TBL, symboltables.peek().getScopeName(), ""
                     + symboltables.peek().getNestingLevel(), "" + symboltables.peek().getTableSize());
             //#gen_deactivation_rec(name_rec)
             analyzer.gen_prog_deactivation_rec(name_rec);
             printSymbolTables();
             removeSymbolTable(scopeName);
             //#gen_halt()
             analyzer.gen_halt();
             break;
         default:
             syntaxError("program");
         }
     }
 
     public String programHeading()
     {
         String name = "";
         debug();
         switch (lookAhead.getType()) {
         case MP_PROGRAM: //3 ProgramHeading -> mp_program ProgramIdentifier
             out.println("3");
             match(TokenType.MP_PROGRAM);
             name = programIdentifier();
             break;
         default:
             syntaxError("program");
         }
         return name;
     }
 
     public void block(String scopeName, SemanticRec blockType, SemanticRec branchLbl)
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_BEGIN: //Fix to allow programs without "var" sections
         case MP_FUNCTION:
         case MP_PROCEDURE:
         case MP_VAR: //4 Block -> VariableDeclarationPart ProcedureAndFunctionDeclarationPart StatementPart
             out.println("4");
             variableDeclarationPart();
             SemanticRec name_rec = new SemanticRec(RecordType.SYM_TBL, scopeName, ""
                     + symboltables.peek().getNestingLevel(), "" + symboltables.peek().getTableSize());
 
             procedureAndFunctionDeclarationPart();
             analyzer.gen_specified_label(branchLbl); //place the label at the begin block
             //#gen_activation_rec(name_rec, block_type)
             analyzer.gen_activation_rec(name_rec, blockType);
             statementPart();
             break;
         default:
             syntaxError("var, begin, function, procedure");
         }
     }
 
     public void variableDeclarationPart()
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_VAR: //5 VariableDeclarationPart -> mp_var VariableDeclaration mp_scolon VariableDeclarationTail
             out.println("5");
             match(TokenType.MP_VAR);
             variableDeclaration();
             match(TokenType.MP_SCOLON);
             variableDeclarationTail();
             break;
         case MP_BEGIN:
         case MP_FUNCTION: //Fix to allow programs without "var" sections
         case MP_PROCEDURE: //107 VariableDeclarationPart -> lambda
             out.println("107");
             lambda();
             break;
         default:
             syntaxError("var, begin, function, procedure");
         }
     }
 
     public void variableDeclarationTail()
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER: //6 VariableDeclarationTail -> VariableDeclaration mp_scolon VariableDeclarationTail
             out.println("6");
             variableDeclaration();
             match(TokenType.MP_SCOLON);
             variableDeclarationTail();
             break;
         case MP_BEGIN:
         case MP_PROCEDURE:
         case MP_FUNCTION: //7 VariableDeclarationTail -> lambda
             out.println("7");
             lambda();
             break;
         default:
             syntaxError("identifier, begin, procedure, function");
         }
     }
 
     public void variableDeclaration()
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER: //8 VariableDeclaration -> IdentifierList mp_colon Type #insert
             out.println("8");
             //identifierList(), match colon, type()
             List<String> ids = identifierList();
 
             match(TokenType.MP_COLON);
             Type t = type();
 
             symboltables.peek().addDataSymbolsToTable(Classification.VARIABLE, ids, new Attribute(t, null));
             break;
         default:
             syntaxError("identifier");
         }
     }
 
     public Type type()
     {
         Type type = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_INTEGER: //9 Type -> mp_integer
             out.println("9");
             match(TokenType.MP_INTEGER);
             type = Type.INTEGER;
             break;
         case MP_BOOLEAN: //110 Type -> mp_boolean
             out.println("110");
             match(TokenType.MP_BOOLEAN);
             type = Type.BOOLEAN;
             break;
         case MP_FLOAT: //108 Type -> mp_float
             out.println("108");
             match(TokenType.MP_FLOAT);
             type = Type.FLOAT;
             break;
         case MP_STRING: //109 Type -> mp_string
             out.println("109");
             match(TokenType.MP_STRING);
             type = Type.STRING;
             break;
         default:
             syntaxError("Integer, Float, Boolean, String");
         }
         return type;
     }
 
     public void procedureAndFunctionDeclarationPart()
     {
         debug();
         switch (lookAhead.getType()) {
         case MP_PROCEDURE: //10 ProcedureAndFunctionDeclarationPart -> ProcedureDeclaration ProcedureAndFunctionDeclarationPart
             out.println("10");
             procedureDeclaration();
             procedureAndFunctionDeclarationPart();
             break;
         case MP_FUNCTION: //11 ProcedureAndFunctionDeclarationPart -> FunctionDeclaration ProcedureAndFunctionDeclarationPart
             out.println("11");
             functionDeclaration();
             procedureAndFunctionDeclarationPart();
             break;
         case MP_BEGIN: //12 ProcedureAndFunctionDeclarationPart -> lambda
             out.println("12");
             lambda();
             break;
         default:
             syntaxError("procedure, function, begin");
         }
     }
 
     public void procedureDeclaration()
     {
         String lbl = LabelGenerator.getNextLabel();
         SemanticRec branchLbl = new SemanticRec(RecordType.LABEL, lbl);
         debug();
         switch (lookAhead.getType()) {
         case MP_PROCEDURE: //13 ProcedureDeclaration -> ProcedureHeading mp_scolon Block mp_scolon #destroy
             out.println("13");
             String procId = procedureHeading(branchLbl);
             match(TokenType.MP_SCOLON);
             block(procId, new SemanticRec(RecordType.BLOCK, "procedure"), branchLbl);
             match(TokenType.MP_SCOLON);
 
             SemanticRec name_rec = new SemanticRec(RecordType.SYM_TBL, symboltables.peek().getScopeName(), ""
                     + symboltables.peek().getNestingLevel(), "" + symboltables.peek().getTableSize());
             //#gen_deactivation_rec(name_rec)
             analyzer.gen_proc_deactivation_rec(name_rec);
             System.out.println("About to pop procedure table");
             printSymbolTables();
             removeSymbolTable();
             break;
         default:
             syntaxError("procedure");
         }
     }
 
     public void functionDeclaration()
     {
         String lbl = LabelGenerator.getNextLabel();
         SemanticRec branchLbl = new SemanticRec(RecordType.LABEL, lbl);
         debug();
         switch (lookAhead.getType()) {
         case MP_FUNCTION: //14 FunctionDeclaration -> FunctionHeading mp_scolon Block mp_scolon #Destroy
             out.println("14");
             String funcId = functionHeading(branchLbl);
             match(TokenType.MP_SCOLON);
             block(funcId, new SemanticRec(RecordType.BLOCK, "function"), branchLbl);
             match(TokenType.MP_SCOLON);
 
             SemanticRec name_rec = new SemanticRec(RecordType.SYM_TBL, symboltables.peek().getScopeName(), ""
                     + symboltables.peek().getNestingLevel(), "" + symboltables.peek().getTableSize());
             //#gen_deactivation_rec(name_rec)
             analyzer.gen_func_deactivation_rec(name_rec);
             System.out.println("About to pop function table");
             printSymbolTables();
             removeSymbolTable();
             FunctionRow row = (FunctionRow) analyzer.findSymbol(funcId, Classification.FUNCTION);
             if (!row.hasReturnValue())
             {
                 semanticError("Function " + funcId + " is missing return value assigned to '" + funcId + "'");
             }
             break;
         default:
             syntaxError("function");
         }
     }
 
     public String procedureHeading(SemanticRec branchLbl)
     {
         String procId = null;
         List<ParamSR> parameters;
         List<Attribute> attributes = new ArrayList<Attribute>();
         List<String> ids = new ArrayList<String>();
         debug();
         switch (lookAhead.getType()) {
         case MP_PROCEDURE: //15 ProcedureHeading -> mp_procedure ProcedureIdentifier #create OptionalFormalParameterList #insert
             out.println("15");
             match(TokenType.MP_PROCEDURE);
 
             procId = procedureIdentifier();
             parameters = optionalFormalParameterList();
             for (ParamSR param : parameters)
             {
                 attributes.add(param.getAttribute());
                 ids.add(param.getLexeme());
             }
             symboltables.peek().addModuleSymbolsToTable(Classification.PROCEDURE, procId, null, attributes,
                     branchLbl.getDatum(0));
             addSymbolTable(procId, branchLbl.getDatum(0));
             symboltables.peek().addDataSymbolToTable(Classification.DISREG, "Old Display Register Value",
                     new Attribute(Type.STRING, Mode.VALUE));
             symboltables.peek().addDataSymbolsToTable(Classification.PARAMETER, ids, attributes);
             symboltables.peek().addDataSymbolToTable(Classification.RETADDR, "Caller's Return Address",
                     new Attribute(Type.STRING, Mode.VALUE));
             break;
         default:
             syntaxError("procedure");
         }
         return procId;
     }
 
     public String functionHeading(SemanticRec branchLbl)
     {
         String funcId = null;
         List<ParamSR> parameters;
         List<Attribute> attributes = new ArrayList<Attribute>();
         List<String> ids = new ArrayList<String>();
         debug();
         switch (lookAhead.getType()) {
         case MP_FUNCTION: //16 FunctionHeading -> mp_function FunctionIdentifier OptionalFormalParameterList mp_colon Type
             out.println("16");
             match(TokenType.MP_FUNCTION);
 
             funcId = functionIdentifier();
             parameters = optionalFormalParameterList();
             for (ParamSR param : parameters)
             {
                 attributes.add(param.getAttribute());
                 ids.add(param.getLexeme());
             }
             match(TokenType.MP_COLON);
             Type t = type();
 
             symboltables.peek().addModuleSymbolsToTable(Classification.FUNCTION, funcId, t, attributes,
                     branchLbl.getDatum(0));
             addSymbolTable(funcId, branchLbl.getDatum(0));
             symboltables.peek().addDataSymbolToTable(Classification.DISREG, "Old Display Register Value",
                     new Attribute(Type.STRING, Mode.VALUE));
             symboltables.peek().addDataSymbolsToTable(Classification.PARAMETER, ids, attributes);
             symboltables.peek().addDataSymbolToTable(Classification.RETADDR, "Return Address",
                     new Attribute(Type.STRING, Mode.VALUE));
             break;
         default:
             syntaxError("function");
         }
         return funcId;
     }
 
     public List<ParamSR> optionalFormalParameterList()
     {
         List<ParamSR> parameters = new ArrayList<ParamSR>();
         debug();
         switch (lookAhead.getType())
         {
         case MP_LPAREN: //17 OptionalFormalParameterList -> mp_lparen FormalParameterSection FormalParameterSectionTail mp_rparen
             out.println("17");
             match(TokenType.MP_LPAREN);
             parameters = formalParameterSection();
             formalParameterSectionTail(parameters);
             match(TokenType.MP_RPAREN);
             break;
         case MP_SCOLON:
         case MP_COLON: //18 OptionalFormalParameterList -> lambda
             out.println("18");
             lambda();
             break;
         default:
             syntaxError("(, ;, :");
         }
         return parameters;
     }
 
     public void ifStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_IF: //51 IfStatement -> mp_if BooleanExpression mp_then #gen_branch_false Statement OptionalElsePart
             out.println("51");
             match(TokenType.MP_IF);
             booleanExpression();
             match(TokenType.MP_THEN);
             analyzer.gen_comment("if");
             SemanticRec elseLabel = analyzer.gen_branch_false(); //brfs to else statements
             statement();
             analyzer.gen_comment("skip else part");
             SemanticRec endLabel = analyzer.gen_branch_unconditional(); //br to end of if
             analyzer.gen_comment("else part");
             analyzer.gen_specified_label(elseLabel);
             optionalElsePart();
             analyzer.gen_comment("if end label");
             analyzer.gen_specified_label(endLabel);
             break;
         default:
             syntaxError("if");
         }
     }
 
     public void optionalElsePart()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_ELSE: //52 OptionalElsePart -> mp_else Statement
             out.println("52");
             match(TokenType.MP_ELSE);
             statement();
             break;
         case MP_UNTIL:
         case MP_SCOLON:
         case MP_END: //53 OptionalElsePart -> lambda
             out.println("53");
             lambda();
             break;
         default:
             syntaxError("else, until, ;, end");
         }
     }
 
     public void repeatStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_REPEAT: //54 RepeatStatement -> mp_repeat StatementSequence mp_until BooleanExpression
             out.println("54");
             match(TokenType.MP_REPEAT);
             analyzer.gen_comment("begin repeat");
             SemanticRec repeatLabel = analyzer.gen_label();
             statementSequence();
             match(TokenType.MP_UNTIL);
             booleanExpression();
             analyzer.gen_comment("evaluate boolean expression and jump to beginning of repeat if necessary");
             analyzer.gen_branch_false_to(repeatLabel);
             break;
         default:
             syntaxError("repeat");
         }
     }
 
     public void whileStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_WHILE: //55 WhileStatement -> mp_while BooleanExpression mp_do Statement
             out.println("55");
             match(TokenType.MP_WHILE);
             analyzer.gen_comment("while");
             SemanticRec whileLabel = analyzer.gen_label();
             booleanExpression();
             SemanticRec endLabel = analyzer.gen_branch_false();
             match(TokenType.MP_DO);
             statement();
             analyzer.gen_branch_unconditional_to(whileLabel);
             analyzer.gen_specified_label(endLabel);
             break;
         default:
             syntaxError("while");
         }
     }
 
     public void forStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_FOR: //56 ForStatement -> mp_for ControlVariable mp_assign InitialValue StepValue FinalValue mp_do Statement
             out.println("56");
             match(TokenType.MP_FOR);
 
             String controlIdentifier = controlVariable();
 
             Row controlSymbol = analyzer.findSymbol(controlIdentifier);
             SemanticRec controlId = new SemanticRec(RecordType.IDENTIFIER,
                     controlSymbol.getClassification().toString(), controlIdentifier);
             match(TokenType.MP_ASSIGN);
             SemanticRec exp = initialValue();
             analyzer.gen_comment("assign control variable to initial value");
             analyzer.gen_assign_for(controlId, exp); //checks that the variableIdentifier is type Integer, casts the initial variable if needed
             SemanticRec forLabel = analyzer.gen_label();
             analyzer.gen_push_variable(controlId);
             SemanticRec forDirection = stepValue();
             SemanticRec finalExpr = finalValue();
             analyzer.gen_assign_cast(controlId, finalExpr); //casts finalExpr to Integer if needed
             analyzer.gen_comment("compare controlvariable to finalvalue");
             analyzer.gen_for_comparison(forDirection); //now there is a boolean on top of the stack
             SemanticRec endForLabel = analyzer.gen_branch_false();
             match(TokenType.MP_DO);
             statement();
             analyzer.gen_comment("increment/decrement control variable");
             analyzer.gen_for_controller(controlId, forDirection);
             analyzer.gen_branch_unconditional_to(forLabel);
             analyzer.gen_specified_label(endForLabel);
             break;
         default:
             syntaxError("for");
         }
     }
 
     public String controlVariable()
     {
         String id = "";
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //57 ControlVariable -> VariableIdentifier
             out.println("57");
             id = variableIdentifier();
             break;
         default:
             syntaxError("identifier");
         }
         return id;
     }
 
     public SemanticRec initialValue()
     {
         SemanticRec expr = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS: //58 InitialValue -> OrdinalExpression
             out.println("58");
             expr = ordinalExpression(null);
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
         return expr;
     }
 
     public SemanticRec stepValue()
     {
         SemanticRec forDirection = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_TO: //59 StepValue -> mp_to
             out.println("59");
             match(TokenType.MP_TO);
             forDirection = new SemanticRec(RecordType.FOR_DIRECTION, TokenType.MP_TO.toString());
             break;
         case MP_DOWNTO: //60 StepValue -> mp_downto
             out.println("60");
             match(TokenType.MP_DOWNTO);
             forDirection = new SemanticRec(RecordType.FOR_DIRECTION, TokenType.MP_DOWNTO.toString());
             break;
         default:
             syntaxError("to, downto");
         }
         return forDirection;
     }
 
     public SemanticRec finalValue() {
         SemanticRec expr = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS: //61 FinalValue -> OrdinalExpression
             out.println("61");
             expr = ordinalExpression(null);
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
         return expr;
     }
 
     public void procedureStatement() {
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER: //62 ProcedureStatement -> ProcedureIdentifier OptionalActualParameterList
             out.println("62");
             String procID = procedureIdentifier();
             ProcedureRow row = (ProcedureRow) analyzer.findSymbol(procID, Classification.PROCEDURE);
             if (row != null)
             {
                 FormalParamSR formalParams = new FormalParamSR(procID, row.getAttributes()); //list of all the formal parameters for this procedure
 
                 SemanticRec procRec = new SemanticRec(RecordType.IDENTIFIER, Classification.PROCEDURE.toString(),
                         procID);
                 analyzer.gen_comment("call to " + procID + " start");
                 analyzer.gen_dis_reg_slot(); //reserve space for the register slot
                 optionalActualParameterList(formalParams);
                 analyzer.gen_proc_call(procRec);
                 analyzer.gen_comment("call to " + procID + " end");
             }
             else
             {
                 semanticError(procID + " is not a Procedure that is currently in scope");
             }
             break;
         default:
             syntaxError("identifier");
         }
     }
 
     public void optionalActualParameterList(FormalParamSR formalParams) {
         debug();
         switch (lookAhead.getType()) {
         case MP_COMMA:
         case MP_RPAREN:
         case MP_AND:
         case MP_MOD:
         case MP_DIV:
         case MP_DIV_INT: //added for / vs div division
         case MP_TIMES:
         case MP_OR:
         case MP_MINUS:
         case MP_PLUS:
         case MP_NEQUAL:
         case MP_GEQUAL:
         case MP_LEQUAL:
         case MP_GTHAN:
         case MP_LTHAN:
         case MP_EQUAL:
         case MP_DOWNTO:
         case MP_TO:
         case MP_DO:
         case MP_UNTIL:
         case MP_ELSE:
         case MP_THEN:
         case MP_SCOLON:
         case MP_END: //64 OptionalActualParameterList -> lambda
             out.println("64");
             lambda();
             if (!formalParams.isCorrectParamCount())
             {
                 semanticError("Invalid call: Actual parameter count for " + formalParams.getName()
                         + " is not equal to the formal parameter count");
             }
             break;
         case MP_LPAREN: //63 OptionalActualParameterList -> mp_lparen ActualParameter ActualParameterTail mp_rparen
             out.println("63");
             match(TokenType.MP_LPAREN);
             actualParameter(formalParams);
             actualParameterTail(formalParams);
 
             if (!formalParams.isCorrectParamCount())
             {
                 semanticError("Invalid call: Actual parameter count for " + formalParams.getName()
                         + " is not equal to the formal parameter count");
             }
             match(TokenType.MP_RPAREN);
             break;
         default:
             syntaxError("',', ), and, mod, div, / , *, 'or', -, +, <>, >=, <=, <, >, =, downto, to, do, until, else, then, ;, end");
         }
     }
 
     public void actualParameterTail(FormalParamSR formalParams) {
         debug();
         switch (lookAhead.getType()) {
         case MP_COMMA: //65 ActualParameterTail -> mp_comma ActualParameter ActualParameterTail
             out.println("65");
             match(TokenType.MP_COMMA);
             actualParameter(formalParams);
             actualParameterTail(formalParams);
             break;
         case MP_RPAREN: //66 ActualParameterTail -> lambda
             out.println("66");
             lambda();
             break;
         default:
             syntaxError("',', )");
         }
     }
 
     public void actualParameter(FormalParamSR formalParams) {
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS: //67 ActualParameter -> OrdinalExpression
             out.println("67");
             Attribute formalParam = formalParams.getCurrentAttributeAndIncrement();
             if (formalParam != null)
             {
                 SemanticRec exp = ordinalExpression(formalParam);
                 //TODO:type check
                 analyzer.gen_param_cast(exp, new SemanticRec(RecordType.FORMAL_PARAM, formalParam.getType().toString(),
                         formalParam.getMode().toString()));
             }
             else
             {
                 semanticError("Too many parameters for module " + formalParams.getName());
             }
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
     }
 
     /**
      * 
      * @param formalParam
      * @return SemanticRec either RecordType.IDENTIFIER or RecordType.LITERAL
      */
     public SemanticRec expression(Attribute formalParam) {
         SemanticRec simpEx;
         SemanticRec opt;
 
         SemanticRec expr = null;
 
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS: //68 Expression -> SimpleExpression OptionalRelationalPart
             out.println("68");
             simpEx = simpleExpression(formalParam);
             opt = optionalRelationalPart(simpEx);
             if (opt != null)
             {
                 expr = opt;
             }
             else
             {
                 expr = simpEx;
             }
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
         return expr;
     }
 
     /**
      * 
      * @param left
      *            {@link SemanticRec} from {@link compiler.parser.Parser#simpleExpression()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @return {@link SemanticRec} either null or {@link RecordType#LITERAL}
      */
     public SemanticRec optionalRelationalPart(SemanticRec left) {
         SemanticRec op;
         SemanticRec right;
 
         SemanticRec opt = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_COMMA:
         case MP_RPAREN:
         case MP_DOWNTO:
         case MP_TO:
         case MP_DO:
         case MP_UNTIL:
         case MP_ELSE:
         case MP_THEN:
         case MP_SCOLON:
         case MP_END: //70 OptionalRelationalPart -> lambda
             out.println("70");
             lambda();
             break;
         case MP_NEQUAL:
         case MP_GEQUAL:
         case MP_LEQUAL:
         case MP_GTHAN:
         case MP_LTHAN:
         case MP_EQUAL: //69 OptionalRelationalPart -> RelationalOperator SimpleExpression
             out.println("69");
             op = relationalOperator();
             right = simpleExpression(null);
             //#gen_opt_rel_part(left, op, right) //leaves boolean on stack
             analyzer.gen_opt_rel_part(left, op, right);
             opt = new SemanticRec(RecordType.LITERAL, Type.BOOLEAN.toString());
             break;
         default:
             syntaxError("',', ), downto, to, do, until, else, then, ;, end, <>, >=, <=, >, <, =");
         }
         return opt;
     }
 
     /**
      * 
      * @return SemanticRec RecordType.REL_OP
      */
     public SemanticRec relationalOperator() {
         SemanticRec relOp = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_NEQUAL: //76 RelationalOperator -> mp_nequal
             out.println("76");
             match(TokenType.MP_NEQUAL);
             relOp = new SemanticRec(RecordType.REL_OP, TokenType.MP_NEQUAL.toString());
             break;
         case MP_GEQUAL: //75 RelationalOperator -> mp_gequal
             out.println("75");
             match(TokenType.MP_GEQUAL);
             relOp = new SemanticRec(RecordType.REL_OP, TokenType.MP_GEQUAL.toString());
             break;
         case MP_LEQUAL: //74 RelationalOperator -> mp_lequal
             out.println("74");
             match(TokenType.MP_LEQUAL);
             relOp = new SemanticRec(RecordType.REL_OP, TokenType.MP_LEQUAL.toString());
             break;
         case MP_GTHAN: //73 RelationalOperator -> mp_gthan
             out.println("73");
             match(TokenType.MP_GTHAN);
             relOp = new SemanticRec(RecordType.REL_OP, TokenType.MP_GTHAN.toString());
             break;
         case MP_LTHAN: //72 RelationalOperator -> mp_lthan
             out.println("72");
             match(TokenType.MP_LTHAN);
             relOp = new SemanticRec(RecordType.REL_OP, TokenType.MP_LTHAN.toString());
             break;
         case MP_EQUAL: //71 RelationalOperator -> mp_equal
             out.println("71");
             match(TokenType.MP_EQUAL);
             relOp = new SemanticRec(RecordType.REL_OP, TokenType.MP_EQUAL.toString());
             break;
         default:
             syntaxError("<>, >=, <= , >, <, =");
         }
         return relOp;
     }
 
     /**
      * 
      * @param formalParam
      * @return SemanticRec RecordType.LITERAL or RecordType.IDENTIFIER
      */
     public SemanticRec simpleExpression(Attribute formalParam) {
         SemanticRec optSign;
         SemanticRec term;
         SemanticRec tt;
 
         SemanticRec simpEx = null;
 
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS: //77 SimpleExpression -> OptionalSign Term TermTail
             out.println("77");
             optSign = optionalSign();
             term = term(formalParam);
             //#gen_opt_sim_negate(optSign, term) value on stack
             analyzer.gen_opt_sim_negate(optSign, term);
             tt = termTail(term);
             if (tt == null)
             {
                 tt = term; //if there isn't a termTail use the type from term else use the result of termTail
             }
             simpEx = tt; //return the type from term
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
         return simpEx;
     }
 
     /**
      * 
      * @param left
      * @return SemanticRec RecordType.LITERAL or RecordType.IDENTIFIER
      */
     public SemanticRec termTail(SemanticRec left) {
         SemanticRec addOp;
         SemanticRec term;
 
         SemanticRec tt = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_COMMA:
         case MP_RPAREN:
         case MP_NEQUAL:
         case MP_GEQUAL:
         case MP_LEQUAL:
         case MP_GTHAN:
         case MP_LTHAN:
         case MP_EQUAL:
         case MP_DOWNTO:
         case MP_TO:
         case MP_DO:
         case MP_UNTIL:
         case MP_ELSE:
         case MP_THEN:
         case MP_SCOLON:
         case MP_END: //79 TermTail -> lambda
             out.println("79");
             lambda();
             break;
         case MP_OR:
         case MP_MINUS:
         case MP_PLUS: //78 TermTail -> AddingOperator Term TermTail
             out.println("78");
             addOp = addingOperator();
             term = term(null);
             //#gen_addOp(left, addOp, term)
             analyzer.gen_addOp(left, addOp, term);
             tt = termTail(term);
             if (tt == null)
             {
                 tt = term; //if there isn't a termTail use the type from term else use the result of termTail
             }
             break;
         default:
             syntaxError("',', ), <>, >=, <=, >, <, =, downto, to, do, until, else, then, ;, end, or, -, +");
         }
         return tt;
     }
 
     /**
      * 
      * @return SemanticRec RecordType.OPT_SIGN
      */
     public SemanticRec optionalSign() {
         SemanticRec optSign = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT: //82 OptionalSign -> lambda
             out.println("82");
             lambda();
             break;
         case MP_MINUS: //81 OptionalSign -> mp_minus
             out.println("81");
             match(TokenType.MP_MINUS);
             optSign = new SemanticRec(RecordType.OPT_SIGN, TokenType.MP_MINUS.toString());
             break;
         case MP_PLUS: //80 OptionalSign -> mp_plus
             out.println("80");
             match(TokenType.MP_PLUS);
             optSign = new SemanticRec(RecordType.OPT_SIGN, TokenType.MP_PLUS.toString());
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
         return optSign;
     }
 
     public SemanticRec addingOperator() {
         SemanticRec addOp = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_OR: //85 AddingOperator -> mp_or
             out.println("85");
             match(TokenType.MP_OR);
             addOp = new SemanticRec(RecordType.ADD_OP, TokenType.MP_OR.toString());
             break;
         case MP_MINUS: //84 AddingOperator -> mp_minus
             out.println("84");
             match(TokenType.MP_MINUS);
             addOp = new SemanticRec(RecordType.ADD_OP, TokenType.MP_MINUS.toString());
             break;
         case MP_PLUS: //83 AddingOperator -> mp_plus
             out.println("83");
             match(TokenType.MP_PLUS);
             addOp = new SemanticRec(RecordType.ADD_OP, TokenType.MP_PLUS.toString());
             break;
         default:
             syntaxError("or, -, +");
         }
         return addOp;
     }
 
     /**
      * 
      * @param formalParam
      * @return SemanticRec RecordType.LITERAL or RecordType.IDENTIFIER
      */
     public SemanticRec term(Attribute formalParam) {
         SemanticRec factor;
         SemanticRec ft;
         SemanticRec term = null;
 
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT: //86 Term -> Factor FactorTail
             out.println("86");
             factor = factor(formalParam);
             ft = factorTail(factor);
             if (ft == null)
             {
                 ft = factor;
             }
             term = ft; //return factor type
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer");
         }
         return term;
     }
 
     /**
      * 
      * @param left
      * @return SemanticRec RecordType.LITERAL or RecordType.IDENTIFIER
      */
     public SemanticRec factorTail(SemanticRec left) {
         SemanticRec mulOp;
         SemanticRec factor;
 
         SemanticRec ft = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_COMMA:
         case MP_RPAREN:
         case MP_OR:
         case MP_MINUS:
         case MP_PLUS:
         case MP_NEQUAL:
         case MP_GEQUAL:
         case MP_LEQUAL:
         case MP_GTHAN:
         case MP_LTHAN:
         case MP_EQUAL:
         case MP_DOWNTO:
         case MP_TO:
         case MP_DO:
         case MP_UNTIL:
         case MP_ELSE:
         case MP_THEN:
         case MP_SCOLON:
         case MP_END: //88 FactorTail -> lambda
             out.println("88");
             lambda();
             break;
         case MP_AND:
         case MP_MOD:
         case MP_DIV:
         case MP_DIV_INT: //added for / vs div division
         case MP_TIMES: //87 FactorTail -> MultiplyingOperator Factor FactorTail
             out.println("87");
             mulOp = multiplyingOperator();
             factor = factor(null);
             //#gen_mulOp(left, mulOp, factor)fa
             factor = analyzer.gen_mulOp(left, mulOp, factor); //result is on stack and type could have been cast
             ft = factorTail(factor);
             if (ft == null)
             {
                 ft = factor;
             }
             break;
         default:
             syntaxError("',', ), or, -, +, <>, >=, <=, >, <, =, downto, to, do, until, else, then, ;, end, and, mod, div, / , *");
         }
         return ft;
     }
 
     /**
      * 
      * @return SemanticRec RecordType.MUL_OP
      */
     public SemanticRec multiplyingOperator() {
 
         SemanticRec mulOp = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_AND: //92 MultiplyingOperator -> mp_and
             out.println("92");
             match(TokenType.MP_AND);
             mulOp = new SemanticRec(RecordType.MUL_OP, TokenType.MP_AND.toString());
             break;
         case MP_MOD: //91 MultiplyingOperator -> mp_mod
             out.println("91");
             match(TokenType.MP_MOD);
             mulOp = new SemanticRec(RecordType.MUL_OP, TokenType.MP_MOD.toString());
             break;
         case MP_DIV: //112 MultiplyingOperator -> mp_div "/"
             out.println("112");
             match(TokenType.MP_DIV);
             mulOp = new SemanticRec(RecordType.MUL_OP, TokenType.MP_DIV.toString());
             break;
         case MP_DIV_INT: //90 MultiplyingOperator -> mp_div_int "div"
             out.println("90");
             match(TokenType.MP_DIV_INT);
             mulOp = new SemanticRec(RecordType.MUL_OP, TokenType.MP_DIV_INT.toString());
             break;
         case MP_TIMES: //89 MultiplyingOperator -> mp_times
             out.println("89");
             match(TokenType.MP_TIMES);
             mulOp = new SemanticRec(RecordType.MUL_OP, TokenType.MP_TIMES.toString());
             break;
         default:
             syntaxError("and, mod, div, / , *");
         }
         return mulOp;
     }
 
     /**
      * 
      * @param formalParam
      * @return SemanticRec RecordType.IDENTIFIER or RecordType.LITERAL
      */
     public SemanticRec factor(Attribute formalParam) {
         SemanticRec factor = null;
 
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER:
             Row factorVar = analyzer.findSymbol(lookAhead.getLexeme());
             if (factorVar != null) {
                 if (factorVar.getClassification() == Classification.VARIABLE
                         || factorVar.getClassification() == Classification.PARAMETER) {
                     out.println("94"); //94 Factor -> VariableIdentifier
                     String id = variableIdentifier();
                     factor = new SemanticRec(RecordType.IDENTIFIER, factorVar.getClassification().toString(), id);
                     if (formalParam != null)
                     {
                         SemanticRec formalParamRec = new SemanticRec(RecordType.FORMAL_PARAM, formalParam.getType()
                                 .toString(), formalParam.getMode().toString());
                         factor = analyzer.gen_push_id(factor, formalParamRec);
                     }
 
                     else
                     {
                         //#gen_push_id(factor)
                         factor = analyzer.gen_push_id(factor); //once the Ident has been pushed onto the stack it is now a literal value
                     }
                 } else if (factorVar.getClassification() == Classification.FUNCTION) {
                     out.println("97"); //97 Factor -> FunctionIdentifier OptionalActualParameterList
                     String id = functionIdentifier();
                     analyzer.gen_comment("call to " + id + " start");
                     analyzer.gen_func_return_slot(); //reserve space for the return value
                     analyzer.gen_dis_reg_slot(); //reserve space for the register slot
 
                     FunctionRow row = (FunctionRow) factorVar;
                     FormalParamSR formalParams = new FormalParamSR(id, row.getAttributes()); //list of all the formal parameters for this procedure
 
                     optionalActualParameterList(formalParams);
 
                     //#gen_func_call(funcRec)
                     SemanticRec funcRec = new SemanticRec(RecordType.IDENTIFIER, Classification.FUNCTION.toString(), id);
                     analyzer.gen_func_call(funcRec); //the return value will be on the top of the stack
                     analyzer.gen_comment("call to " + id + " end");
                     factor = new SemanticRec(RecordType.LITERAL, factorVar.getType().toString()); //return value of type is on top of the stack
                 } else {
                     semanticError("Cannot use Procedure identifier '" + lookAhead.getLexeme() + "' as factor.");
                 }
             } else {
                 semanticError("Undeclared identifier: '" + lookAhead.getLexeme() + "'");
             }
 
             break;
         case MP_LPAREN: //96 Factor -> mp_lparen Expression mp_rparen
             out.println("96");
             match(TokenType.MP_LPAREN);
             factor = expression(null);
             match(TokenType.MP_RPAREN);
             break;
         case MP_NOT: //95 Factor -> mp_not Factor
             out.println("95");
             match(TokenType.MP_NOT);
             factor = factor(null);
             //#gen_not_bool(factor)
             analyzer.gen_not_bool(factor);
             factor = new SemanticRec(RecordType.LITERAL, Type.BOOLEAN.toString()); //the result is a boolean on the top of the stack
             break;
         case MP_INTEGER_LIT: //93 Factor -> mp_integer_lit
             out.println("93");
             String lex = lookAhead.getLexeme();
             match(TokenType.MP_INTEGER_LIT);
             factor = new SemanticRec(RecordType.LITERAL, Type.INTEGER.toString());
             //#gen_push_lit(factor, lex)
             analyzer.gen_push_lit(factor, lex);
             break;
         case MP_FALSE: //116 Factor -> mp_false
             out.println("116");
             lex = lookAhead.getLexeme();
             match(TokenType.MP_FALSE);
             factor = new SemanticRec(RecordType.LITERAL, Type.BOOLEAN.toString());
             //#gen_push_lit(factor, lex)
             analyzer.gen_push_lit(factor, lex);
             break;
         case MP_TRUE: //115 Factor -> mp_true
             out.println("115");
             lex = lookAhead.getLexeme();
             match(TokenType.MP_TRUE);
             factor = new SemanticRec(RecordType.LITERAL, Type.BOOLEAN.toString());
             //#gen_push_lit(factor, lex)
             analyzer.gen_push_lit(factor, lex);
             break;
         case MP_STRING_LIT: //114 Factor -> mp_string_lit
             out.println("114");
             lex = lookAhead.getLexeme();
             match(TokenType.MP_STRING_LIT);
             factor = new SemanticRec(RecordType.LITERAL, Type.STRING.toString());
             //#gen_push_lit(factor, lex)
             analyzer.gen_push_lit(factor, lex);
             break;
         case MP_FLOAT_LIT: //113 Factor -> mp_float_lit
             out.println("113");
             lex = lookAhead.getLexeme();
             match(TokenType.MP_FLOAT_LIT);
             factor = new SemanticRec(RecordType.LITERAL, Type.FLOAT.toString());
             //#gen_push_lit(factor, lex)
             analyzer.gen_push_lit(factor, lex);
             break;
         default:
             syntaxError("identifier, (, not, Integer, false, true, String, Float");
         }
 
         return factor;
     }
 
     public String programIdentifier() {
         String progIdentifier = "";
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER: //98 ProgramIdentifier -> mp_identifier
             out.println("98");
             progIdentifier = lookAhead.getLexeme();
             match(TokenType.MP_IDENTIFIER);
             break;
         default:
             syntaxError("identifier");
         }
         return progIdentifier;
     }
 
     public String variableIdentifier() {
         String lex = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER: //99 VariableIdentifier -> mp_identifier
             out.println("99");
             lex = lookAhead.getLexeme(); //current lexeme
             match(TokenType.MP_IDENTIFIER);
             break;
         default:
             syntaxError("identifier");
             break;
         }
         return lex;
     }
 
     public String procedureIdentifier() {
         String procId = null;
         debug();
         switch (lookAhead.getType()) {
         case MP_IDENTIFIER: //100 ProcedureIdentifier -> mp_identifier
             out.println("100");
             procId = lookAhead.getLexeme();
             match(TokenType.MP_IDENTIFIER);
             break;
         default:
             syntaxError("identifier");
         }
         return procId;
     }
 
     public void formalParameterSectionTail(List<ParamSR> parameters)
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_SCOLON: //19 FormalParameterSectionTail -> mp_scolon FormalParameterSection FormalParameterSectionTail
             out.println("19");
             match(TokenType.MP_SCOLON);
             parameters.addAll(formalParameterSection());
             formalParameterSectionTail(parameters);
             break;
         case MP_RPAREN: //20 FormalParameterSectionTail -> &epsilon
             out.println("20");
             lambda();
             break;
         default:
             syntaxError("function, )");
         }
     }
 
     public List<ParamSR> formalParameterSection()
     {
         List<ParamSR> parameters = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //21 FormalParameterSection -> ValueParameterSection #insert
             out.println("21");
             parameters = valueParameterSection();
             break;
         case MP_VAR: //22 FormalParameterSection -> VariableParameterSection #insert
             out.println("22");
             parameters = variableParameterSection();
             break;
         default:
             syntaxError("identifier, var");
         }
         return parameters;
     }
 
     public List<ParamSR> valueParameterSection()
     {
         List<ParamSR> parameters = new ArrayList<ParamSR>();
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //23 ValueParameterSection -> IdentifierList mp_colon Type
             out.println("23");
             //identifierList() match colon type()
             List<String> ids = identifierList();
             match(TokenType.MP_COLON);
             Type t = type();
             for (String id : ids)
             {
                 parameters.add(new ParamSR(id, new Attribute(t, Mode.VALUE)));
             }
             break;
         default:
             syntaxError("identifier");
         }
         return parameters;
     }
 
     public List<ParamSR> variableParameterSection()
     {
         List<ParamSR> parameters = new ArrayList<ParamSR>();
         debug();
         switch (lookAhead.getType())
         {
         case MP_VAR: //24 VariableParameterSection -> mp_var IdentifierList mp_colon Type
             out.println("24");
             match(TokenType.MP_VAR);
             //identifierList() match colon type()
             List<String> ids = identifierList();
             match(TokenType.MP_COLON);
             Type t = type();
             for (String id : ids)
             {
                 parameters.add(new ParamSR(id, new Attribute(t, Mode.VARIABLE)));
             }
             break;
         default:
             syntaxError("var");
         }
         return parameters;
     }
 
     public void statementPart()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_BEGIN: //25 StatementPart -> CompoundStatement
             out.println("25");
             compoundStatement();
             break;
         default:
             syntaxError("begin");
         }
     }
 
     public void compoundStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_BEGIN: //26 CompoundStatement -> mp_begin StatementSequence mp_end
             out.println("26");
             match(TokenType.MP_BEGIN);
             statementSequence();
             match(TokenType.MP_END);
             break;
         default:
             syntaxError("begin");
         }
     }
 
     public void statementSequence()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //27 StatementSequence -> Statement StatementTail
         case MP_FOR:
         case MP_WHILE:
         case MP_UNTIL:
         case MP_REPEAT:
         case MP_IF:
         case MP_WRITELN: //added writeln
         case MP_WRITE:
         case MP_READ:
         case MP_SCOLON:
         case MP_END:
         case MP_BEGIN:
             out.println("27");
             statement();
             statementTail();
             break;
         default:
             syntaxError("identifier, for, while, until, repeat, if, write, writeln, read, ;, end, begin");
         }
     }
 
     public void statementTail()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_SCOLON: //28 StatementTail -> mp_scolon Statement StatementTail
             out.println("28");
             match(TokenType.MP_SCOLON);
             statement();
             statementTail();
             break;
         case MP_UNTIL: //29 StatementTail -> &epsilon
         case MP_END:
             out.println("29");
             lambda();
             break;
         default:
             syntaxError(";, until, end");
         }
     }
 
     public void statement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_UNTIL: //30 Statement -> EmptyStatement
         case MP_ELSE:
         case MP_SCOLON:
         case MP_END:
             out.println("30");
             emptyStatement();
             break;
         case MP_BEGIN: //31 Statement -> CompoundStatement
             out.println("31");
             compoundStatement();
             break;
         case MP_READ: //32 Statement -> ReadStatement
             out.println("32");
             readStatement();
             break;
         case MP_WRITELN:
         case MP_WRITE: //33 Statement -> WriteStatement
             out.println("33");
             writeStatement();
             break;
         case MP_IDENTIFIER:
             if (lookAhead2.getType() == TokenType.MP_ASSIGN) {
                 out.println("34");
                 assignmentStatement(); //34 Statement  -> AssigmentStatement
             } else {
                 out.println("39");
                 procedureStatement();//39 Statement  -> ProcedureStatement
             }
             break;
         case MP_IF:
             ifStatement(); //35 Statement  -> IfStatement
             out.println("35");
             break;
         case MP_WHILE:
             whileStatement(); //36 Statement  -> WhileStatement
             out.println("36");
             break;
         case MP_REPEAT:
             repeatStatement(); //37 Statement  -> RepeatStatement
             out.println("37");
             break;
         case MP_FOR:
             forStatement(); //38 Statement  -> ForStatement
             out.println("38");
             break;
         default:
             syntaxError("until, else, ;, end, begin, Read, Write, Writeln, identifier, if, while, repeat, for");
         }
     }
 
     public void emptyStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_UNTIL: //40 EmptyStatement -> &epsilon
         case MP_ELSE:
         case MP_SCOLON:
         case MP_END:
             out.println("40");
             lambda();
             break;
         default:
             syntaxError("until, else, ;, end");
         }
     }
 
     public void readStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_READ: //41 ReadStatement ->  mp_read mp_lparen ReadParameter ReadParameterTail mp_rparen
             out.println("41");
             match(TokenType.MP_READ);
             match(TokenType.MP_LPAREN);
             readParameter();
             readParameterTail();
             match(TokenType.MP_RPAREN);
             break;
         default:
             syntaxError("Read");
         }
     }
 
     public void readParameterTail()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_COMMA: //42 ReadParameterTail -> mp_comma ReadParameter ReadParameterTail
             out.println("42");
             match(TokenType.MP_COMMA);
             readParameter();
             readParameterTail();
             break;
         case MP_RPAREN: //43 ReadParameterTail -> &epsilon
             out.println("43");
             lambda();
             break;
         default:
             syntaxError("Read, )");
         }
     }
 
     public void readParameter()
     {
         SemanticRec readID = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //44 ReadParameter -> VariableIdentifier
             out.println("44");
             String id = variableIdentifier();
            Row varSym = analyzer.findSymbol(id);
            readID = new SemanticRec(RecordType.IDENTIFIER, varSym.getClassification().toString(), id);
             //#gen_readStmt(readID)
             analyzer.gen_readStmt(readID);
             break;
         default:
             syntaxError("identifier");
         }
     }
 
     public void writeStatement()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_WRITE: //45 WriteStatement -> mp_write mp_lparen WriteParameter WriteParameterTail mp_rparen
             out.println("45");
             match(TokenType.MP_WRITE);
             match(TokenType.MP_LPAREN);
             SemanticRec mpwrite = new SemanticRec(RecordType.WRIT_STMT, TokenType.MP_WRITE.toString());
             writeParameter(mpwrite);
             writeParameterTail(mpwrite);
 
             match(TokenType.MP_RPAREN);
             break;
         case MP_WRITELN: //111 WriteStatement -> mp_writeln mp_lparen WriteParameter WriteParameterTail mp_rparen.
             out.println("111");
             match(TokenType.MP_WRITELN);
             match(TokenType.MP_LPAREN);
             SemanticRec mpwriteln = new SemanticRec(RecordType.WRIT_STMT, TokenType.MP_WRITELN.toString());
             writeParameter(mpwriteln);
             writeParameterTail(mpwriteln);
             match(TokenType.MP_RPAREN);
             break;
         default:
             syntaxError("Write, WriteLn");
         }
     }
 
     /**
      * 
      * @param writeStmt
      *            {@link RecordType#WRITE_STMT}
      */
     public void writeParameterTail(SemanticRec writeStmt)
     {
 
         debug();
         switch (lookAhead.getType())
         {
         case MP_COMMA: //46 WriteParameterTail -> mp_comma WriteParameter WriteParameterTail
             out.println("46");
             match(TokenType.MP_COMMA);
             writeParameter(writeStmt);
             writeParameterTail(writeStmt);
             break;
         case MP_RPAREN: //47 WriteParameterTail -> &epsilon
             out.println("47");
             lambda();
             break;
         default:
             syntaxError("',', )");
         }
     }
 
     /**
      * 
      * @param writeStmt
      *            {@link RecordType#WRITE_STMT}
      */
     public void writeParameter(SemanticRec writeStmt)
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //48 WriteParameter -> OrdinalExpression
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS:
             out.println("48");
             SemanticRec exp = ordinalExpression(null);
             //#gen_writestmt(writestmt, exp)
             analyzer.gen_writeStmt(writeStmt, exp);
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
     }
 
     public void assignmentStatement()
     {
         String id;
         SemanticRec varId;
         SemanticRec exp;
         SemanticRec symTable = new SemanticRec(RecordType.SYM_TBL, symboltables.peek().getScopeName(), ""
                 + symboltables.peek().getNestingLevel(), "" + symboltables.peek().getTableSize());
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: {
             Row assign = analyzer.findSymbol(lookAhead.getLexeme());
 
             if (assign == null) {
                 semanticError("Undeclared identifier " + lookAhead.getLexeme() + " found.");
             } else {
                 if (assign.getClassification() == Classification.VARIABLE
                         || assign.getClassification() == Classification.PARAMETER) {
                     //49 AssignmentStatement -> VariableIdentifier mp_assign Expression
                     out.println("49");
                     id = variableIdentifier();
                     varId = new SemanticRec(RecordType.IDENTIFIER, assign.getClassification().toString(), id);
                     match(TokenType.MP_ASSIGN);
                     exp = expression(null);
                     //#gen_assign(varId, exp)
                     analyzer.gen_assign(varId, exp, symTable);
                 } else if (assign.getClassification() == Classification.FUNCTION) {
                     //50 AssignmentStatement -> FunctionIdentifier mp_assign Expression
                     out.println("50");
                     id = functionIdentifier();
                     varId = new SemanticRec(RecordType.IDENTIFIER, assign.getClassification().toString(), id);
                     match(TokenType.MP_ASSIGN);
                     exp = expression(null);
                     //#gen_assign(funcId, exp) //pushes the value on the stack
                     analyzer.gen_assign(varId, exp, symTable);
                 } else {
                     semanticError("Cannot assign value to a Procedure"); //TODO: you can assign to parameter
                 }
             }
         }
 
        break;
         default:
             syntaxError("identifier");
         }
     }
 
     //Now starting at Rule line 103
     public String functionIdentifier()
     {
         String funcId = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //101 FunctionIdentifier -> mp_identifier
             out.println("101");
             funcId = lookAhead.getLexeme();
             match(TokenType.MP_IDENTIFIER);
             break;
         default:
             syntaxError("identifier");
         }
         return funcId;
     }
 
     public void booleanExpression()
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //102 BooleanExpression ->  Expression
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS:
             out.println("102");
             SemanticRec rec = expression(null);
             if (rec.getRecType() == RecordType.LITERAL)
             {
                 Type type = Type.valueOf(rec.getDatum(0));
                 if (type != Type.BOOLEAN)
                 {
                     semanticError("BooleanExpression requires a boolean literal type found type " + type.toString());
                 }
             }
             else if (rec.getRecType() == RecordType.IDENTIFIER) //shouldn't be an Identifier here but just in case
             {
                 Type type = analyzer.findSymbol(rec.getDatum(1), Classification.valueOf(rec.getDatum(0))).getType();
                 if (type != Type.BOOLEAN)
                 {
                     semanticError("BooleanExpression requires a boolean identifier type found " + rec.getDatum(1)
                             + " with type " + type.toString());
                 }
             }
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
     }
 
     public SemanticRec ordinalExpression(Attribute formalParam)
     {
         SemanticRec ord = null;
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //103 OrdinalExpression ->  Expression
         case MP_FALSE:
         case MP_TRUE:
         case MP_STRING_LIT:
         case MP_FLOAT_LIT: //added boolean values, string, float
         case MP_LPAREN:
         case MP_NOT:
         case MP_INTEGER_LIT:
         case MP_MINUS:
         case MP_PLUS:
             out.println("103");
             ord = expression(formalParam);
             break;
         default:
             syntaxError("identifier, false, true, String, Float, (, not, Integer, -, +");
         }
         return ord;
     }
 
     public List<String> identifierList()
     {
         ArrayList<String> idList = new ArrayList<String>();
         debug();
         switch (lookAhead.getType())
         {
         case MP_IDENTIFIER: //104 IdentifierList -> mp_identifier IdentifierTail
             out.println("104");
             idList.add(lookAhead.getLexeme());
             match(TokenType.MP_IDENTIFIER);
             identifierTail(idList);
             break;
         default:
             syntaxError("identifier");
         }
         return idList;
     }
 
     public void identifierTail(List<String> idList)
     {
         debug();
         switch (lookAhead.getType())
         {
         case MP_COMMA: //105 IdentifierTail -> mp_comma mp_identifier IdentifierTail
             out.println("105");
             match(TokenType.MP_COMMA);
             idList.add(lookAhead.getLexeme());
             match(TokenType.MP_IDENTIFIER);
             identifierTail(idList);
             break;
         case MP_COLON: //106 IdentifierTail -> &epsilon
             out.println("106");
             lambda();
             break;
         default:
             syntaxError("',', :");
         }
     }
 }

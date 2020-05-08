 package org.zza.visitor;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.zza.codegenerator.DataMemoryManager;
 import org.zza.codegenerator.InstructionMemoryManager;
 import org.zza.codegenerator.MemoryOutOfBoundsException;
 import org.zza.codegenerator.ProgramFrame;
 import org.zza.codegenerator.StackFrame;
 import org.zza.codegenerator.threeaddresscode.Addition3AC;
 import org.zza.codegenerator.threeaddresscode.Assignment3AC;
 import org.zza.codegenerator.threeaddresscode.Division3AC;
 import org.zza.codegenerator.threeaddresscode.ComparisonHeader3AC;
 import org.zza.codegenerator.threeaddresscode.FunctionCall3AC;
 import org.zza.codegenerator.threeaddresscode.IfRest3AC;
 import org.zza.codegenerator.threeaddresscode.Multiplication3AC;
 import org.zza.codegenerator.threeaddresscode.Negative3AC;
 import org.zza.codegenerator.threeaddresscode.Print3AC;
 import org.zza.codegenerator.threeaddresscode.Return3AC;
 import org.zza.codegenerator.threeaddresscode.Subtraction3AC;
 import org.zza.codegenerator.threeaddresscode.TerribleImplementationToGetTempUsageVisitor;
 import org.zza.codegenerator.threeaddresscode.WhileFooter3AC;
 import org.zza.parser.semanticstack.nodes.*;
 
 
 public class ThreeAddressCodeGenerator extends NodeVisitor {
     
     private int tempCount = 0;
     private int lineNumber = 1;
     private DataMemoryManager dataManager;
     private InstructionMemoryManager instructionManager;
     private TerribleImplementationToGetTempUsageVisitor usageManager;
     
 
     public ThreeAddressCodeGenerator(TerribleImplementationToGetTempUsageVisitor terribleUsageVisitor) {
         usageManager = terribleUsageVisitor;
         dataManager = new DataMemoryManager();
         instructionManager = new InstructionMemoryManager();
     }
     
     @Override
     public String visit(ProgramNode node) {
         
         String declarations = node.getDeclarations().accept(this);
         String parameters = node.getHeader().accept(this);
         String[] splitParam = parameters.split(",");
         System.out.println("0:   LDA  7,"+lineNumber+"(6)");
         try {
             int localCount = usageManager.getLocalsCountFrom("program");
             int tempCount = usageManager.getTempsCountFrom("program");
             ProgramFrame program = new ProgramFrame(localCount, tempCount);
             dataManager.addStackFrame(program);
             initializeRegisters(program.getSize());
             handleCommandLineArguments(splitParam);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
 
         String body = node.getbody().accept(this);
         System.out.println(lineNumber + ":   HALT  0,0,0");
         return "program: \n"+declarations +"\n" +body;
     }
     
     private void initializeRegisters(int size) {
         System.out.println(lineNumber++ + ":   LDC  2,1(6)");
         System.out.println(lineNumber++ + ":   LDC  3,1(6)");
         System.out.println(lineNumber++ + ":   LDC  4,"+(size+1)+"(6)");
         System.out.println(lineNumber++ + ":   LDC  5,0(6)");
     }
 
     private void handleCommandLineArguments(String[] splitParam) throws MemoryOutOfBoundsException {
         for (int i = splitParam.length; i > 0; i--) {
             dataManager.addLocalVariable(splitParam[i-1]);
         }
     }
 
     @Override
     public String visit(VariableDeclarationNode node) {
         return handleTwoFieldNode(node, "vardec");
     }
     
     @Override
     public String visit(FunctionNode node) {
         String header = node.getHeader().accept(this);
         String[] headerParts = header.split(";");
         String functionName = headerParts[0];
         String[] params = new String[] {};
         if(headerParts.length > 1) {
             params = headerParts[1].split(",");
         }
         int locals = usageManager.getLocalsCountFrom("function"+functionName);
         int temps = usageManager.getTempsCountFrom("function"+functionName);
         StackFrame frame = new StackFrame(params.length, locals, temps, functionName);
         instructionManager.addFunction(functionName, lineNumber, frame.getSize());
         try {
             dataManager.addStackFrame(frame);
             for (String param : params) {
                 dataManager.addLocalVariable(param);
             }
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         
         
         System.out.println("*Entering function: " +functionName);
         node.getBody().accept(this); 
         System.out.println("*Finished function: "+functionName);
         //load old r4
        System.out.println(lineNumber++ + ":    LD  4,1(3)");
         //load old r3
        System.out.println(lineNumber++ + ":    LD  3,2(3)");
         //load control link
         System.out.println(lineNumber++ + ":   ADD  0,3,4");
         System.out.println(lineNumber++ + ":   LD  7,0(0)");
         System.out.println("*Finished reloading registers");
         
         
         
         return getNextTemporary();
     }
     
     @Override
     public String visit(ParameterNode node) {
         
         return node.getLeftHand().accept(this);
     }
     
     @Override
     public String visit(AssignmentExpressionNode node) {
         String left = node.acceptVisitorLeftHand(this);
         String right = node.acceptVisitorRightHand(this);
         try {
             dataManager.addLocalVariable(left);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
             dataManager.dump();
         }
         Assignment3AC assign = new Assignment3AC(lineNumber, dataManager);
         assign.setParameters(right, left, "");
         lineNumber += assign.getEmittedSize();
         assign.emitCode();
         return "assignment";
     }
     
     @Override
     public String visit(CompoundStatementNode node) {
         String toReturn = "(";
         for (SemanticNode n : node.getStatements()) {
             toReturn += n.accept(this) + ",";
         }
         return trimEnd(toReturn)+")";
     }
     
     @Override
     public String visit(DivisionExpressionNode node) {
         String temp = getNextTemporary();
         try {
             dataManager.addNewTemporaryVariable(temp);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         String left = node.acceptVisitorLeftHand(this);
         String right = node.acceptVisitorRightHand(this);
         Division3AC division = new Division3AC(lineNumber, dataManager);
         division.setParameters(temp, left, right);
         lineNumber += division.getEmittedSize();
         division.emitCode();
         return temp;
     }
     
     @Override
     public String visit(IdentifierNode node) {
         return node.getValue();
     }
     
     @Override
     public String visit(IntegerNode node) {
         return node.getValue();
     }
     
     @Override
     public String visit(MinusExpressionNode node) {
         String temp = getNextTemporary();
         try {
             dataManager.addNewTemporaryVariable(temp);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         String left = node.acceptVisitorLeftHand(this);
         String right = node.acceptVisitorRightHand(this);
         Subtraction3AC subtraction = new Subtraction3AC(lineNumber, dataManager);
         subtraction.setParameters(temp, left, right);
         lineNumber += subtraction.getEmittedSize();
         subtraction.emitCode();
         return temp;
     }
     
     
     @Override
     public String visit(MultiplicationExpressionNode node) {
         String temp = getNextTemporary();
         try {
             dataManager.addNewTemporaryVariable(temp);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         String left = node.acceptVisitorLeftHand(this);
         String right = node.acceptVisitorRightHand(this);
         Multiplication3AC multiplication = new Multiplication3AC(lineNumber, dataManager);
         multiplication.setParameters(temp, left, right);
         lineNumber += multiplication.getEmittedSize();
         multiplication.emitCode();
         return temp;
     }
     
     @Override
     public String visit(PlusExpressionNode node) {
         String temp = getNextTemporary();
         try {
             dataManager.addNewTemporaryVariable(temp);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         String left = node.acceptVisitorLeftHand(this);
         String right = node.acceptVisitorRightHand(this);
         Addition3AC addition = new Addition3AC(lineNumber, dataManager);
         addition.setParameters(temp, left, right);
         lineNumber += addition.getEmittedSize();
         addition.emitCode();
         return temp;
     }
     
     @Override
     public String visit(RealNode node) {
         return node.getValue();
     }
     
     @Override
     public String visit(TypeNode node) {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public String visit(AllParametersNode node) {
         String toReturn = "";
         for (SemanticNode n : node.getArray()){
             toReturn += n.accept(this) + ",";
         }
         if (toReturn.length() > 0 ) {
             toReturn = toReturn.substring(0, toReturn.length()-1);
         }
         return toReturn;
     }
     
     @Override
     public String visit(AllVariableDeclarationsNode node) {
         return null;
     }
     
     @Override
     public String visit(ArgumentNode node) {
         String toReturn = "";
         for (SemanticNode n : node.getArguments()) {
             toReturn += n.accept(this) + ",";
         }
         return trimEnd(toReturn);
     }
     
     private String trimEnd(String toReturn) {
         int max = 0;
         if(toReturn.length() > 1) {
             max = toReturn.length() - 1;
         }
         return toReturn.substring(0, max);
     }
 
     @Override
     public String visit(CompareOperatorNode node) {
         return node.getValue();
     }
     
     @Override
     public String visit(ComparisonNode node) {
         return node.acceptVisitorLeftHand(this) + "," + node.acceptVisitorMiddle(this) + "," +
                 node.acceptVisitorRightHand(this);
     }
     
     @Override
     public String visit(WhileExpressionNode node) {
         String whilePart = node.acceptVisitorLeftHand(this);
         String[] whileParts = whilePart.split(",");
         int oldLineNumber = lineNumber;
         lineNumber += 4;
         node.acceptVisitorRightHand(this);
         //build footer
         WhileFooter3AC whileFooter = new WhileFooter3AC(lineNumber, dataManager);
         whileFooter.setParameters("", "", "", oldLineNumber);
         whileFooter.emitCode();
         lineNumber += whileFooter.getEmittedSize();
         ComparisonHeader3AC whileHeader = new ComparisonHeader3AC(oldLineNumber, dataManager);
         whileHeader.setParameters(whileParts[0], whileParts[2], whileParts[1], lineNumber - oldLineNumber - 4);
         whileHeader.emitCode();
         return "while";
     }
 
     @Override
     public String visit(NegativeExpressionNode node) {
         String temp = getNextTemporary();
         try {
             dataManager.addNewTemporaryVariable(temp);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         String content = node.getContent().accept(this);
         Negative3AC negation = new Negative3AC(lineNumber, dataManager);
         negation.setParameters(temp, content, "");
         lineNumber += negation.getEmittedSize();
         negation.emitCode();
         return temp;
     }
     
     @Override
     public String visit(ProgramHeaderNode node) {
         return node.getParameters().accept(this);
     }
     
     @Override
     public String visit(DeclarationsNode node) {
         String functionDec = node.getFunctionDeclarations().accept(this);
         return functionDec;
     }
     
     @Override
     public String visit(PrintStatementNode node) {
         String parameters = node.getArgument().accept(this);
         String[] params = parameters.split(",");
         Print3AC printer = null;
         for (String param : params) {
             printer = new Print3AC(lineNumber, dataManager);
             printer.setParameters(param, "", "");
             lineNumber += printer.getEmittedSize();
             printer.emitCode();
         }
         return "print";
     }
     
     @Override
     public String visit(FunctionCallNode node) {
         String temp = getNextTemporary();
         try {
             dataManager.addNewTemporaryVariable(temp);
         } catch (MemoryOutOfBoundsException e) {
             e.printStackTrace();
         }
         String params = node.acceptVisitorRightHand(this);
         String name = node.acceptVisitorLeftHand(this);
          
         FunctionCall3AC function = new FunctionCall3AC(lineNumber, name, params, temp, dataManager, instructionManager);
         function.emitCode();
         
         lineNumber += function.getEmittedSize();
         return temp;
     }
     
     @Override
     public String visit(FunctionHeadingNode node) {
 //        return handleThreeFieldNode(node, "", "");
         String name = node.acceptVisitorLeftHand(this);
 //        return node.acceptVisitorLeftHand(this);
         String param = node.acceptVisitorMiddle(this);
         return name+";" +param;
     }
 
     @Override
     public String visit(AllFunctionDeclarationsNode node) {
         String toReturn = "";
         ArrayList<SemanticNode> array = node.getArray();
         Collections.reverse(array);
         for (SemanticNode n : array) {
             toReturn += n.accept(this) + ", ";
         }
         return toReturn;
     }
     
     @Override
     public String visit(FunctionBodyNode node) {
         String functionBody = node.getBody().accept(this);
         return functionBody;
     }
     
     @Override
     public String visit(ReturnStatementNode node) {
         String arguments = node.getArguments().accept(this);
         Return3AC returnStatement = new Return3AC(lineNumber, dataManager);
         returnStatement.setParameters(arguments, "", "");
         System.out.println("*starting return statement");
         returnStatement.emitCode();
         System.out.println("*finished return statement");
         lineNumber += returnStatement.getEmittedSize();
         return "return";
     }
     
     @Override
     public String visit(IfStatementNode node) {
         String ifPart = node.acceptVisitorLeftHand(this);
         String[] ifParts = ifPart.split(",");
         int oldLineNumber = lineNumber;
         lineNumber += 4; 
         node.acceptVisitorMiddle(this);
         ComparisonHeader3AC ifHeader = new ComparisonHeader3AC(oldLineNumber, dataManager);
         ifHeader.setParameters(ifParts[0], ifParts[2], ifParts[1], lineNumber - oldLineNumber - 2);
         ifHeader.emitCode();
         oldLineNumber = lineNumber;
         lineNumber += 2;
         node.acceptVisitorRightHand(this);
         
         IfRest3AC ifRest = new IfRest3AC(oldLineNumber, dataManager);
         ifRest.setParameters("", "", "", lineNumber - oldLineNumber - 2);
         ifRest.emitCode();
         return "if";
     }
     
     @Override
     public String visit(EmptyNode node) {
         return "";
     }
     
     
     private String handleTwoFieldNode(TwoFieldNode node, String op) {
         String left = node.acceptVisitorLeftHand(this);
         String right = node.acceptVisitorRightHand(this);
         String nextTemp = getNextTemporary();
         System.out.println(nextTemp + " := " + left +" "+ op +" "+ right);
 //        return "twofield:\n"+left + " "+op + " " + right;
         return nextTemp;
     }
 
     private String getNextTemporary() {
         return "t"+tempCount++;
     }
 }

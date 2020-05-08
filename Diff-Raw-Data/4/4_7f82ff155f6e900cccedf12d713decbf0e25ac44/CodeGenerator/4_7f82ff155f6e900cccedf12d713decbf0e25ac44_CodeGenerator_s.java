 package cps450;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.io.PrintWriter;
 
 import cps450.oodle.analysis.*;
 import cps450.oodle.node.*;
 
 public class CodeGenerator extends DepthFirstAdapter {
 	
 	PrintWriter writer;
 
 	int ifStatementCount = 0;
 	Stack<Integer> ifStatementCounts;
 	int loopStatementCount;
 	Stack<Integer> loopStatementCounts;
 	int stringCount = 0;
 	int callCount = 0;
 	Hashtable<String, ClassDeclaration> classTable;
 	Hashtable<Node, Type> typeDecorations;
 	MethodDeclaration currentMethodDeclaration = null;
 	String currentClassName;
 	ClassDeclaration currentClassDeclaration = null;
 	
 	public CodeGenerator(PrintWriter _writer, Hashtable<String, ClassDeclaration> _classTable, Hashtable<Node, Type> _typeDecorations) {
 		super();
 		writer = _writer;
 		ifStatementCounts = new Stack<Integer>();
 		loopStatementCounts = new Stack<Integer>();
 		classTable = _classTable;
 		typeDecorations = _typeDecorations;
 	}
 	
 	private void emit(String sourceLine) {
 		writer.println(sourceLine);
 	}
 	
 	private void emitOodleStatement(Token token) {
 		emit("");
 		emit("# " + token.getLine() + ": " + SourceHolder.instance().getLine(token.getLine()-1));
 	}
 	
 	private void emitStringExpressionFor(String text) {
 		String label = "strlit" + stringCount;
 		
 		emit(".data");
 		emit(label + ": .string " + text);
 		
 		emit(".text");
 		emit("pushl $" + label);
 		emit("call string_fromlit");
 		emit("movl %eax, (%esp) # Cleanup parameter and push return value all at once");
 		
 		stringCount++;
 	}
 	
 	public String getMethodLabel(String className, String methodName) {
 		if (!(className.equals("in") || className.equals("out"))) {
 			return className + "_" + methodName;
 		} else {
 			return methodName;
 		}
 	}
 	
 	public void emitClassInstantiationExpressionFor(ClassDeclaration klass) {
 		// Allocate space for the object
 		// - 2 * 4 bytes reserved + 4 * instance variable count
 		emit("# Instantiate object");
 		emit("pushl $4"); // Size of each reserved element is 4 bytes
 		emit("pushl $" + (2 + klass.getInstanceVariableCount())); // Total allocated elements
 		emit("call calloc");
 		emit("addl $8, %esp");
 		emit("pushl %eax");
 	}
 	
 	/*
 	 * Start the generated with the predefined data directives. 
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inAStart(cps450.oodle.node.AStart)
 	 */
 	@Override
 	public void inAStart(AStart node) {
 		emit(".data");
 		emit(".comm _out, 4, 4");
 		emit(".comm _in, 4, 4");
 		emit(".comm errorLine, 4, 4");
 	}
 
 	@Override
 	public void outStart(Start node) {
 		emit("");
 		emit(".text");
 		emit(".global main");
 		emit("main:");
 		
 		// Instantiate globals
 		emitClassInstantiationExpressionFor(classTable.get("Reader"));
 		emit("popl _in");
 		emitClassInstantiationExpressionFor(classTable.get("Writer"));
 		emit("popl _out");
 		
 		// Instantiate the main class
 		emitClassInstantiationExpressionFor(currentClassDeclaration);
 		
 		// Self argument is left on the stack from the instantiation expression
 		emit("call " + getMethodLabel(currentClassName, "start"));
 		emit("addl $4, %esp # Cleanup method argument");
 		
 		// End the program
 		emit("push $0");
 		emit("call exit");
 		
 		emit("");
 		emit("# Global helpers");
 		emit(".text");
 		emit("__npe__:");
 		emit("pushl _out");
		emitStringExpressionFor("\"The little gremlin running your program is scratching his head wondering how he is supposed to look up a method on a null object at line:\"");
		emit("call Writer_writeln");
 		//emit("addl $8, %esp");
 		emit("pushl _out");
 		emit("pushl errorLine");
 		emit("call Writer_writeint");
 		//emit("addl $8, %esp");
 		emit("pushl $1");
 		emit("call exit");
 	}
 
 	/*
 	 * Start the generated with the predefined data directives. 
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inAClassDef(cps450.oodle.node.AClassDef)
 	 */
 	@Override
 	public void inAClassDef(AClassDef node) {
 		currentClassName = node.getBeginName().getText();
 		currentClassDeclaration = classTable.get(node.getBeginName().getText());
 	}
 	
 	/*
 	 * Generate assembly for addition expressions.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAAddExpression(cps450.oodle.node.AAddExpression)
 	 */
 	@Override
 	public void outAAddExpression(AAddExpression node) {
 		emit("popl %eax # AddExpression");
 		emit("popl %ebx");
 		
 		if (node.getOperator() instanceof APlusOperator) {
 			emit("addl %eax, %ebx");
 		} else if (node.getOperator() instanceof AMinusOperator) {
 			emit("subl %eax, %ebx");
 		}
 		
 		emit("pushl %ebx # Store AddExpression result");
 	}
 	
 	/*
 	 * Generate assembly for And expressions.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAAndExpression(cps450.oodle.node.AAndExpression)
 	 */
 	@Override
 	public void outAAndExpression(AAndExpression node) {
 		emit("popl %eax # AndExpression");
 		emit("popl %ebx");
 		emit("andl %ebx, %eax");
 		emit("pushl %eax # Store AndExpression result");
 	}
 	
 	/*
 	 * Output a comment with the oodle source.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inAAssignmentStatement(cps450.oodle.node.AAssignmentStatement)
 	 */
 	@Override
 	public void inAAssignmentStatement(AAssignmentStatement node) {
 		emitOodleStatement(node.getId());
 	}
 	
 	/*
 	 * Generate assembly for assignment statements.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAAssignmentStatement(cps450.oodle.node.AAssignmentStatement)
 	 */
 	@Override
 	public void outAAssignmentStatement(AAssignmentStatement node) {
 		emit("popl %eax # AssignmentStatement");
 		
 		String name = node.getId().getText();
 		
 		VariableDeclaration local = currentMethodDeclaration.getVariable(name);
 		if (local != null) {
 			emit("movl %eax, " + local.getStackOffset() + "(%ebp) # Move value to local variable '" + name + "'");
 		} else {
 			VariableDeclaration instance = currentClassDeclaration.getVariable(name);
 			if (instance != null) {
 				VariableDeclaration self = currentMethodDeclaration.getVariable("me");
 				emit("movl " + self.getStackOffset() + "(%ebp), %ebx # Get reference to self");
 				emit("movl %eax, " + instance.getInstanceOffset() +"(%ebx) # Move value to Klass#" + name);
 			} else {
 				// Global variable
 				emit("movl %eax, _" + name);
 			}
 		}
 	}
 	
 	/*
 	 * Generate assembly to push the self argument before a method call
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inACallExpression(cps450.oodle.node.ACallExpression)
 	 */
 	@Override
 	public void inACallExpression(ACallExpression node) {
 		if (node.getObject() == null) {
 			// Method call has implicit callee; pass along the current lexical self
 			VariableDeclaration self = currentMethodDeclaration.getVariable("me");
 			emit("pushl " + self.getStackOffset() + "(%ebp) # Push reference to self as argument");
 		}
 		// Else: explicit object callee; self value pushed by the expression evaluation
 	}
 
 	/*
 	 * Generate assembly for the actual calling of the method
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outACallExpression(cps450.oodle.node.ACallExpression)
 	 */
 	@Override
 	public void outACallExpression(ACallExpression node) {
 		String klass = (node.getObject() == null) ? currentClassName : typeDecorations.get(node.getObject()).getName();
 		String methodName = node.getMethod().getText();
 		String methodLabel = getMethodLabel(klass, methodName);
 		Integer argCount = node.getArguments().size() + 1; // Offset for the "self" argument
 		
 		// Dynamic null pointer checking
 		emit("cmpl $0, " + (argCount - 1)*4 + "(%esp)");
 		emit("jne call" + callCount);
 		emit("movl $" + SourceHolder.instance().getLineNumberFor(node.getMethod()) + ", errorLine");
 		emit("jmp __npe__");
 		
 		// Call method
 		emit("call" + callCount + ":");
 		emit("call " + methodLabel);
 		emit("addl $" + ((argCount) * 4) + ", %esp # Clean up the argument values");
 		emit("pushl %eax # Assume that we got a return value");
 		
 		callCount++;
 	}
 	
 	@Override
 	public void caseACallExpression(ACallExpression node) {
 		inACallExpression(node);
         if(node.getObject() != null)
         {
             node.getObject().apply(this);
         }
         if(node.getMethod() != null)
         {
             node.getMethod().apply(this);
         }
         {
             List<PExpression> copy = new ArrayList<PExpression>(node.getArguments());
             java.util.Collections.reverse(copy);
             for(PExpression e : copy)
             {
                 e.apply(this);
             }
         }
         outACallExpression(node);
 	}
 
 	/*
 	 * Output a comment with the oodle source.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inACallStatement(cps450.oodle.node.ACallStatement)
 	 */
 	@Override
 	public void inACallStatement(ACallStatement node) {
 		ACallExpression expr = (ACallExpression)node.getExpression();
 		emitOodleStatement(expr.getMethod());
 	}
 	
 	/*
 	 * Generate assembly to cleanup after the call statements.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outACallStatement(cps450.oodle.node.ACallStatement)
 	 */
 	@Override
 	public void outACallStatement(ACallStatement node) {
 		emit("popl %eax # Cleanup unused return value in CallStatement");
 	}
 	
 	/*
 	 * Generate assembly for the comparison expressions.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAComparisonExpression(cps450.oodle.node.AComparisonExpression)
 	 */
 	@Override
 	public void outAComparisonExpression(AComparisonExpression node) {
 		emit("popl %eax # ComparisonExpression");
 		emit("popl %ebx");
 		emit("cmpl %eax, %ebx");
 		
 		if (node.getOperator() instanceof AEqualOperator) {
 			emit("sete %al # Equal");
 		} else if (node.getOperator() instanceof AGreaterOperator) {
 			emit("setg %al # Greater");
 		} else if (node.getOperator() instanceof AGreaterEqualOperator) {
 			emit("setge %al # GreaterOrEqual");
 		}
 		emit("movzbl %al, %eax");
 		
 		emit("pushl %eax # Store ComparisonExpression result");
 	}
 	
 	/*
 	 * Generate assembly for a false keyword.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAFalseExpression(cps450.oodle.node.AFalseExpression)
 	 */
 	@Override
 	public void outAFalseExpression(AFalseExpression node) {
 		emit("pushl $0 # FalseExpression");
 	}
 	
 	/*
 	 * Generate assembly to evaluate the value of a variable.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAIdentifierExpression(cps450.oodle.node.AIdentifierExpression)
 	 */
 	@Override
 	public void outAIdentifierExpression(AIdentifierExpression node) {
 		String name = node.getId().getText();
 		
 		VariableDeclaration local = currentMethodDeclaration.getVariable(name);
 		if (local != null) {
 			emit("pushl " + local.getStackOffset() + "(%ebp) # Push local variable '" + name + "'");
 		} else {
 			VariableDeclaration instance = currentClassDeclaration.getVariable(name);
 			if (instance != null) {
 				VariableDeclaration self = currentMethodDeclaration.getVariable("me");
 				emit("movl " + self.getStackOffset() + "(%ebp), %ebx # Get reference to self");
 				emit("pushl " + instance.getInstanceOffset() +"(%ebx) # Push value of Klass#" + name);
 			} else {
 				// Global variable
 				emit("pushl _" + name);
 			}
 		}
 	}
 	
 	/*
 	 * Output oodle source as comment in assembly.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inAIfStatement(cps450.oodle.node.AIfStatement)
 	 */
 	@Override
 	public void inAIfStatement(AIfStatement node) {
 		emitOodleStatement(node.getIf());
 	}
 	
 	/*
 	 * Generate the assembly to test the if expression and execute the correct statements.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAIfHelper(cps450.oodle.node.AIfHelper)
 	 */
 	@Override
 	public void outAIfHelper(AIfHelper node) {
 		this.ifStatementCounts.push(this.ifStatementCount);
 		this.ifStatementCount++;
 		emit("popl %eax # Get comparison value for IfStatement");
 		emit("cmpl $0, %eax");
 		emit("jne _true_statements_" + this.ifStatementCounts.peek());
 		emit("jmp _false_statements_" + this.ifStatementCounts.peek());
 		emit("_true_statements_" + this.ifStatementCounts.peek() + ":");
 	}
 	
 	/*
 	 * Generate the code in the middle of the if so we only execute the correct statements.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAElseHelper(cps450.oodle.node.AElseHelper)
 	 */
 	@Override
 	public void outAElseHelper(AElseHelper node) {
 		emit("jmp _end_if_statement_" + this.ifStatementCounts.peek());
 		emit("_false_statements_" + this.ifStatementCounts.peek() + ":");
 	}
 	
 	/*
 	 * Generate the end label for the if statements.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAIfStatement(cps450.oodle.node.AIfStatement)
 	 */
 	@Override
 	public void outAIfStatement(AIfStatement node) {
 		emit("_end_if_statement_" + this.ifStatementCounts.peek() + ":");
 		
 		this.ifStatementCounts.pop();
 	}
 	
 	/*
 	 * Generate assembly to evaluate an integer literal.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAIntegerExpression(cps450.oodle.node.AIntegerExpression)
 	 */
 	@Override
 	public void outAIntegerExpression(AIntegerExpression node) {
 		emit("pushl $" + node.getIntlit().getText());
 	}
 	
 	/*
 	 * Output oodle source as a comment.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inALoopStatement(cps450.oodle.node.ALoopStatement)
 	 */
 	@Override
 	public void inALoopStatement(ALoopStatement node) {
 		emitOodleStatement(node.getLoop());
 		
 		this.loopStatementCounts.push(this.loopStatementCount);
 		this.loopStatementCount++;
 		
 		emit("_begin_loop_statement_" + this.loopStatementCounts.peek() + ":");
 	}
 	
 	/*
 	 * Generate test expression evaluation in loop statement
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outALoopHelper(cps450.oodle.node.ALoopHelper)
 	 */
 	@Override
 	public void outALoopHelper(ALoopHelper node) {
 		emit("popl %eax # Get comparison value for LoopStatement");
 		emit("cmpl $0, %eax");
 		emit("jne _loop_statements_" + this.loopStatementCounts.peek());
 		emit("jmp _end_loop_statement_" + this.loopStatementCounts.peek());
 		emit("_loop_statements_" + this.loopStatementCounts.peek() + ":");
 	}
 	
 	/*
 	 * Generate the end label of a loop statement and reset to the beginning of the loop.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outALoopStatement(cps450.oodle.node.ALoopStatement)
 	 */
 	@Override
 	public void outALoopStatement(ALoopStatement node) {
 		emit("jmp _begin_loop_statement_" + this.loopStatementCounts.peek());
 		emit("_end_loop_statement_" + this.loopStatementCounts.peek() + ":");
 		
 		this.loopStatementCounts.pop();
 	}
 	
 	@Override
 	public void outAMeExpression(AMeExpression node) {
 		VariableDeclaration self = currentMethodDeclaration.getVariable("me");
 		emit("pushl " + self.getStackOffset() + "(%ebp) # MeExpression");
 	}
 
 	/*
 	 * Generate assembly labels for a method beginning.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#inAMethodDeclaration(cps450.oodle.node.AMethodDeclaration)
 	 */
 	@Override
 	public void inAMethodDeclaration(AMethodDeclaration node) {
 		currentMethodDeclaration = currentClassDeclaration.getMethod(node.getBeginName().getText());
 		emit(".text");
 		
 		String name = node.getBeginName().getText();
 		emit("\n# Method: " + currentClassName + "#" + name);
 		
 		emit(getMethodLabel(currentClassName, name) + ":");
 		
 		// Activation record
 		emit("pushl %ebp");
 		emit("movl %esp, %ebp");
 		emit("subl $" + ((currentMethodDeclaration.getLocalCount()) * 4) + ", %esp # Make space for local variables");
 	}
 	
 	/*
 	 * Generate the assembly to end the program.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAMethodDeclaration(cps450.oodle.node.AMethodDeclaration)
 	 */
 	@Override
 	public void outAMethodDeclaration(AMethodDeclaration node) {
 		// Set return value
 		emit("movl " + currentMethodDeclaration.getVariable(node.getBeginName().getText()).getStackOffset() + "(%ebp), %eax # Set return value");
 		
 		// Cleanup activation record
 		emit("addl $" + ((node.getVarDeclaration().size() + 1) * 4) + ", %esp # Cleanup local variables");
 		
 		// Replace old EBP value
 		emit("popl %ebp");
 		
 		emit("ret");
 	}
 	
 	/*
 	 * Generate assembly to evaluate multiplication and division operations.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAMultExpression(cps450.oodle.node.AMultExpression)
 	 */
 	@Override
 	public void outAMultExpression(AMultExpression node) {
 		emit("popl %ebx # MultExpression");
 		emit("popl %eax");
 		
 		if (node.getOperator() instanceof AMultOperator) {
 			emit("imull %ebx, %eax");
 		} else if (node.getOperator() instanceof ADivOperator) {
 			emit("cdq");
 			emit("idivl %ebx");
 		}
 		
 		emit("pushl %eax # Store MultExpression result");
 	}
 	
 	@Override
 	public void outANewObjectExpression(ANewObjectExpression node) {
 		String className = typeDecorations.get(node).getName();
 		ClassDeclaration klass = classTable.get(className);
 		emitClassInstantiationExpressionFor(klass);
 	}
 
 	/*
 	 * Generate code to save the value of null.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outANullExpression(cps450.oodle.node.ANullExpression)
 	 */
 	@Override
 	public void outANullExpression(ANullExpression node) {
 		emit("pushl $0");
 	}
 	
 	/*
 	 * Generate code to evaluate and Or expression.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAOrExpression(cps450.oodle.node.AOrExpression)
 	 */
 	@Override
 	public void outAOrExpression(AOrExpression node) {
 		emit("popl %eax # AndExpression");
 		emit("popl %ebx");
 		emit("orl %ebx, %eax");
 		emit("pushl %eax # Store AndExpression result");
 	}
 	
 	@Override
 	public void outAStringExpression(AStringExpression node) {
 		emitStringExpressionFor(node.getStrlit().getText());
 	}
 	
 	/*
 	 * Generate code to evaluate the value of the true expression.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outATrueExpression(cps450.oodle.node.ATrueExpression)
 	 */
 	@Override
 	public void outATrueExpression(ATrueExpression node) {
 		emit("pushl $1 # TrueExpression");
 	}
 	
 	/*
 	 * Generate assembly code to evaluate unary (+/-) expressions.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAUnaryExpression(cps450.oodle.node.AUnaryExpression)
 	 */
 	@Override
 	public void outAUnaryExpression(AUnaryExpression node) {
 		emit("popl %eax # Begin UnaryExpression");
 		if (node.getOperator() instanceof AMinusOperator) {
 			emit("negl %eax");
 		} else if (node.getOperator() instanceof ANotOperator) {
 			emit("xorl $1, %eax # Not");
 		}
 		emit("pushl %eax # End UnaryExpression");
 		
 	}
 
 	/*
 	 * Generate assembly code to define a variable's storage space.
 	 * @see cps450.oodle.analysis.DepthFirstAdapter#outAVarDeclaration(cps450.oodle.node.AVarDeclaration)
 	 */
 	@Override
 	public void outAVarDeclaration(AVarDeclaration node) {
 		//if (currentMethodDeclaration == null) {
 		//	emit(".comm _" + node.getName().getText() + ", 4, 4");
 		//}
 	}
 	
 	
 	
 }

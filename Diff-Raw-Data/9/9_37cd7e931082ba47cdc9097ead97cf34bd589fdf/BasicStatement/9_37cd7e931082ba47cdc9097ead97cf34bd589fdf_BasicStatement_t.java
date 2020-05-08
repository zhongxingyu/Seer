 package basic_tree;
 
 import java.util.ArrayList;
 
 
 public class BasicStatement extends Statement {
 
 	private ArrayList<Variable> variables = new ArrayList<Variable>();
 	private ArrayList<Statement> statements = new ArrayList<Statement>();
 	
 	public BasicStatement(int line) {
		super(line); 
 	}
 
 	public ArrayList<Variable> getVariables() {
 		return variables;
 	}
 
 	public void addVariable(Variable variable) {
 		variables.add(variable);
 	}
 
 	public ArrayList<Statement> getStatements() {
 		return statements;
 	}
 
 	public void addStatement(Statement statement) {
 		statements.add(statement);
 	}
 	
 	
 	
 }

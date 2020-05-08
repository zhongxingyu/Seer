 package org.jmlspecs.jml4.boogie.ast;
 
 import java.util.ArrayList;
 
 import org.jmlspecs.jml4.boogie.BoogieSource;
 
 public class Program extends BoogieNode implements Scope {
 	private boolean expanded;
 	private ArrayList/*<TypeDeclaration>*/ types;
 	private ArrayList/*<VariableDeclaration>*/ globals;
 	private ArrayList/*<Statement>*/ statements;
 	private ArrayList/*<Procedure>*/ procedures;
 	
 	public Program() {
 		super(null, null);
 		this.expanded = false;
 		this.types = new ArrayList();
 		this.statements = new ArrayList();
 		this.procedures = new ArrayList();
 		this.globals = new ArrayList();
 	}
 	
 	public boolean isExpanded() {
 		return expanded;
 	}
 	
 	public ArrayList getTypes() {
 		return types;
 	}
 
 	public ArrayList getStatements() {
 		return statements;
 	}
 	
 	public ArrayList getProcedures() {
 		return procedures;
 	}
 
 	public ArrayList getGlobals() {
 		return globals;
 	}
 
 	public TypeDeclaration lookupType(String name) {
 		for (int i = 0; i < getTypes().size(); i++) {
 			TypeDeclaration decl = (TypeDeclaration)getTypes().get(i);
 			if (decl.getType().getTypeName().equals(name)) {
 				return decl;
 			}
 		}
 		return null;
 	}
 
 	public VariableDeclaration lookupVariable(String name) {
		for (int i = 0; i < getGlobals().size(); i++) {
			VariableDeclaration decl = (VariableDeclaration)getGlobals().get(i);
			if (decl.getName().getName().equals(name)) {
 				return decl;
 			}
 		}
 		return null;
 	}
 	
 	public Procedure lookupProcedure(String name) {
 		for (int i = 0; i < getProcedures().size(); i++) {
 			Procedure proc = (Procedure)getProcedures().get(i);
 			if (proc.getName().equals(name)) {
 				return proc;
 			}
 		}
 		return null;
 	}
 
 	public void toBuffer(BoogieSource out) {
 		resolve(); // resolve before printing buffer out
 		for (int i = 0; i < getStatements().size(); i++) {
 			((Statement)getStatements().get(i)).toBuffer(out);
 		}
		out.appendLine("/*!BOOGIESTART!*/"); //$NON-NLS-1$
 		for (int i = 0; i < getProcedures().size(); i++) {
 			((Procedure)getProcedures().get(i)).toBuffer(out);
 		}
 	}
 	
 	public Program resolve() {
 		if (expanded) return this;
 		DecoratorVisitor visitor = new DecoratorVisitor();
 		traverse(visitor);
 		expanded = true;
 		return this;
 	}
 
 	public void addType(TypeDeclaration type) {
 		getTypes().add(type);
 	}
 
 	public void addVariable(VariableDeclaration decl) {
 		getGlobals().add(decl);
 	}
 
 	public void traverse(Visitor visitor) {
 		if (visitor.visit(this)) {
 			for (int i = 0; i < getTypes().size(); i++) {
 				((BoogieNode)getTypes().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getGlobals().size(); i++) {
 				((BoogieNode)getGlobals().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getStatements().size(); i++) {
 				((BoogieNode)getStatements().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getProcedures().size(); i++) {
 				((BoogieNode)getProcedures().get(i)).traverse(visitor);
 			}
 		}
 		visitor.endVisit(this);
 	}
 
 	public Procedure getProcedureScope() {
 		return null;
 	}
 
 	public Program getProgramScope() {
 		return this;
 	}
 }

 /*
  * MIPL: Mining Integrated Programming Language
  *
 * File: InstructionWriter.java
  * Author A: YoungHoon Jung <yj2244@columbia.edu>
  * Author B: Akshai Sarma <as4107@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: JVM Byte Code Writer
  */
 package edu.columbia.mipl.codegen;
 
 import java.util.*;
 
 import org.apache.bcel.*;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.datastr.*;
 
 public class JvmBytecodeWriter extends InstructionWriter {
 
 	InstructionList il;
 
 	/* read http://commons.apache.org/bcel/manual.html */
 	public JvmBytecodeWriter() {
 	}
 
 	public void init(String path, String filename) {
 		ClassGen cg = new ClassGen(filename, "java.lang.Object",
 				"<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER,
 				null);
 		ConstantPoolGen cp = cg.getConstantPool();
 		il = new InstructionList();
 
 		MethodGen mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC,
 				Type.VOID,
 				new Type[] {new ArrayType(Type.STRING, 1)},
 				new String[] {"argv"},
 				"main", filename,
 				il, cp);
 
 		InstructionFactory factory = new InstructionFactory(cg);
 	}
 
 	public String getName() {
 		return "JVM";
 	}
 
 	public void createTerm(Term.Type type, Term term1, Expression expr1) {
 	// Term.Type.IS
 	}
 
 	public void createTerm(Term.Type type, Expression expr1, Expression expr2) {
 		switch (type) {
 			case EQ:
 				break;
 			case LT:
 				break;
 			case LE:
 				break;
 			case GT:
 				break;
 			case GE:
 				break;
 			case NE:
 				break;
 		}
 
 	}
 
 	public void createTerm(Term.Type type, String name,
 										PrimitiveMatrix<Double> matrix) {
 	// Term.Type.MATRIX
 	}
 
 	public void createTerm(Term.Type type, Term term1, Term term2) {
 		switch (type) {
 			case ANDTERMS:
 				break;
 			case ORTERMS:
 				break;
 		}
 	}
 
 	public void createTerm(Term.Type type, Term term1) {
 	// Term.Type.NOTTERM
 	}
 
 	public void createTerm(Term.Type type, String name, List<Term> arguments) {
 		switch (type) {
 			case REGEXTERM:
 				break;
 			case TERM:
 				break;
 		}
 	}
 
 	public void createTerm(Term.Type type, double value) {
 	// Term.Type.NUMBER
 
 	}
 
 	public void createTerm(Term.Type type, String name) {
 		switch (type) {
 			case VARIABLE:
 				break;
 			case QUERYALL:
 				break;
 			case REGEXQUERYALL:
 				break;
 			case STRING:
 				break;
 		}
 	}
 
 	public void createTerm(Term.Type type, Expression expr1) {
 	// Term.Type.EXPRESSION
 
 	}
 
 	public void createExpression(Expression.Type type, Term term1) {
 	// Term.Type.Term
 
 	}
 
 	public void createExpression(Expression.Type type, Expression expr1,
 									Expression expr2) {
 		switch (type) {
 			case MINUS:
 				break;
 			case PLUS:
 				break;
 			case MULTI:
 				break;
 			case DIVIDE:
 				break;
 		}
 	}
 
 	public void createFact(Fact.Type type, Term term) {
 	// Fact.Type.FACT
 
 	}
 
 	public void createFact(Fact.Type type, String name, List<String> names,
 							List<Term> terms) {
 	// Fact.Type.MATRIXASFACTS
 
 	}
 
 	public void createRule(Term term, Term source) {
 
 	}
 
 	public void createQuery(Term term) {
 
 	}
 
 	public void createJob(String name, List<Term> args, List<JobStmt> stmts) {
 
 	}
 
 	public void createJobStmt(JobStmt.Type type, JobExpr expr, JobStmt stmt1,
 								JobStmt stmt2) {
 		switch (type) {
 			case IF:
 				break;
 			case WHILE:
 				break;
 			case DOWHILE:
 				break;
 		}
 	}
 
 	public void createJobStmt(JobStmt.Type type, List<JobStmt> stmts) {
 	// JobStmt.Type.COMPOUND
 
 	}
 
 	public void createJobStmt(JobStmt.Type type, JobExpr expr) {
 	// Maybe should use helper functions for this as tasks are really different
 		switch (type) {
 			case RETURN:
 				break;
 			case EXPR:
 				break;
 		}
 	}
 
 	public void createJobExpr(JobExpr.Type type, String name, JobExpr expr) {
 		switch (type) {
 			case ASSIGN:
 				break;
 			case MULASSIGN:
 				break;
 			case DIVASSIGN:
 				break;
 			case MODASSIGN:
 				break;
 			case ADDASSIGN:
 				break;
 			case SUBASSIGN:
 				break;
 		}
 	}
 
 	public void createJobExpr(JobExpr.Type type, JobExpr expr1,	JobExpr expr2) {
 		switch (type) {
 			case OR:
 				break;
 			case AND:
 				break;
 			case EQ:
 				break;
 			case NE:
 				break;
 			case LT:
 				break;
 			case GT:
 				break;
 			case LE:
 				break;
 			case GE:
 				break;
 			case ADD:
 				break;
 			case SUB:
 				break;
 			case MULT:
 				break;
 			case DIV:
 				break;
 			case MOD:
 				break;
 			case MULT_CELL:
 				// TO DO Function needs to be implemented in PrimitiveOperations
 				break;
 			case DIV_CELL:
 				// TO DO Function needs to be implemented in PrimitiveOperations
 				break;
 			case EXP_CELL:
 				// TO DO Function needs to be implemented in PrimitiveOperations
 				break;
 		}
 	}
 
 	public void createJobExpr(JobExpr.Type type, JobExpr expr1) {
 	// JobExpr.Type.NEGATE
 
 	}
 
 	public void createJobExpr(JobExpr.Type type, Term term,
 										List<ArrayIndex> indices1,
 										List<ArrayIndex> indices2) {
 	// JobExpr.Type.ARRAY
 
 	}
 
 	public void createJobExpr(JobExpr.Type type, String name,
 										List<JobExpr> exprs) {
 	// JobExpr.Type.JOBCALL
 
 	}
 
 	public void createJobExpr(JobExpr.Type type, Term term) {
 	// JobExpr.Type.TERM
 
 	}
 
 // ObjectType i_stream = new ObjectType("java.io.InputStream");
 	public void finish() {
 	}
 }

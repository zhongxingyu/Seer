 package org.net9.simplex.ppmc.checker;
 
 import java.util.HashMap;

 import org.lsmp.djep.sjep.PolynomialCreator;
 import org.lsmp.djep.xjep.XJep;
 import org.net9.simplex.ppmc.core.SimpleDTMC;
 import org.net9.simplex.ppmc.mat.MatrixIndex;
 import org.net9.simplex.ppmc.mat.SmartMatrix;
 import org.net9.simplex.ppmc.mat.SparseMatrix;
 import org.net9.simplex.ppmc.util.Stdio;
 import org.nfunk.jep.Node;
 import org.nfunk.jep.ParseException;
 
 public class SimpleReachabilityChecker {
 	SimpleDTMC model;
 	SmartMatrix M=null;
 	Node detM = null;
 	HashMap <MatrixIndex, String> cofactor = new HashMap <MatrixIndex, String>();
 	XJep jep = new XJep();
 	PolynomialCreator simplifier = new PolynomialCreator(jep);
 	
 	public SimpleReachabilityChecker(SimpleDTMC model) {
 		this.model = model;
 		jep.setAllowUndeclared(true);
 		jep.setAllowAssignment(false);
 	}
 	
 	Node getDetM() {
 		if (this.detM == null){
 			SmartMatrix sm = this.getM();
 			this.detM = this.simplify(sm.determinant());
 		}
 		return this.detM;
 	}
 	SmartMatrix getM(){
 		if(this.M==null){
 			int dim = model.numTransients;
 			SmartMatrix trans = model.getTrans();
 			double smd[][]=new double[dim][dim];
			SparseMatrix<String> vars = trans.getVars().clone();
 			for(int i=0;i<dim;i++){
 				for(int j=0;j<dim;j++){
 					if (trans.isSymbolicEntry(i, j)) {
 						vars.put(i, j, ((i==j)? "1":"") + "-(" +trans.getEntry(i, j, true)+")");
 						smd[i][j] = -1;
 					} else {
 						smd[i][j]=((i==j)?1:0)-trans.getNumericEntry(i, j);
 					}
 				}
 			}
 			this.M=new SmartMatrix(smd,vars);
 		}
 		return this.M;
 	}
 	
 	public Node check(int from, int to) {
 		if (model.isAbsorbingState(from))
 			return this.checkA2S(from, to);
 		if (model.isAbsorbingState(to)) 
 			return this.checkT2A(from, to);
 		return this.checkT2T(from, to);
 	}
 	
 	public String checkToString(int from, int to) {
 		return jep.toString(this.check(from, to));
 		
 	}
 	
 	Node checkA2S(int from, int to) {
 		assert(from>=model.numTransients);
 		try {
 			return jep.parse((from==to)?"1":"0");
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Check the reachability from transient state 'from' to transient state 'to'
 	 * @param from
 	 * @param to
 	 * @return
 	 */
 	Node checkT2T(int from, int to) {
 		//assert(from<model.numTransients);
 		//assert(to<model.numTransients);
 		String aji = this.getCofactor(to, from);
 		String ajj = this.getCofactor(to, to);
 		Node nji = simplify(aji);
 		Node njj = simplify(ajj);
 		return jepDivide(nji,njj);
 	}
 	
 	/**
 	 * Check the reachability from transient state 'from' to absorbing state 'to'
 	 * @param from
 	 * @param to
 	 * @return
 	 */
 	public Node checkT2A(int from, int to) {
 		//assert(from<model.numTransients);
 		//assert(to>=model.numTransients);
 
 		StringBuffer num = new StringBuffer("0");
 		for(int i=0;i<model.numTransients;i++){	
 			if(model.getTrans().getNumericEntry(i, to)!=0){
 				String n = this.getCofactor(i, from);
 				if (!n.equals("0")) {
 					char coef=(from+i)%2==0?'+':'-';
 					num.append(coef).append('(').append(n).append(")*")
 						.append(model.getTrans().getEntry(i, to, true));
 				}
 			}
 		}
 		Node numerator = simplify(num.toString());
 		Node d = this.getDetM();
 		Node simpl= jepDivide(numerator,d);
 		return simpl; 
 	}
 	
 	Node jepDivide(Node n1,Node n2){
 		try {
 			Node r = jep.getNodeFactory().buildOperatorNode(
 					jep.getOperatorSet().getDivide(),
 					n1, n2);
 			return simplifier.simplify(r);
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 	String getCofactor(int i, int j) {
 		MatrixIndex index = new MatrixIndex(i,j);
 		String c = cofactor.get(index);
 		if (c==null) {
 			SmartMatrix mino=this.getM().minor(i, j);
 			c = mino.determinant();
 			cofactor.put(index, c);
 		}
 		return c;
 	}
 	
 	Node simplify(String expr){
 		try {
 			return simplifier.simplify(jep.parse(expr));
 		} catch (ParseException e) {
 			Stdio.out.println(expr);
 			e.printStackTrace();
 			return null;
 		}
 	}
 		
 }

 package fatworm.logicplan;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fatworm.driver.Record;
 import fatworm.driver.Schema;
 import fatworm.util.Env;
 import fatworm.util.Util;
 import fatworm.absyn.FuncCall;
 
 // This Logic plan implements the upper levels of the interpreter
 // TODO add liveness analysis
 
 public abstract class Plan {
 
 	public Plan parent;
 	public boolean hasEval;
 	public List<FuncCall> myAggr;
 	public Plan() {
 		hasEval = false;
 		myAggr = new ArrayList<FuncCall>();
 	}
 	public Plan(Plan parent) {
 		this();
 		this.parent = parent;
 	}
 	
 	// Eval is a signal that the global environment has changed, you might need to re-eval your results.
 	public abstract void eval(Env env);
 
 	public abstract String toString();
 	
 	public abstract boolean hasNext();
 	
 	public abstract Record next();
 
 	public abstract void reset();
 
 	public List<FuncCall> getAggr() {
 		return myAggr;
 	}
 	public abstract Schema getSchema();
 	public abstract void close();
 	public abstract List<String> getColumns();
 	// for propagation of outer values into inner subquery-expression
 	public abstract List<String> getRequestedColumns();
 	public abstract void rename(String oldName, String newName);
 	public void setSrc(Plan oldChild, Plan newChild) {
 		newChild.parent = this;
 		Plan plan = this;
 		if(plan instanceof Distinct){
 			Distinct p = (Distinct)plan;
 			p.src = newChild;
 		}else if(plan instanceof FetchTable){
 		}else if(plan instanceof Group){
 			Group p = (Group)plan;
 			p.src = newChild;
 		}else if(plan instanceof Join){
 			Join p = (Join)plan;
 			if(p.left == oldChild)
 				p.left = newChild;
 			else if(p.right == oldChild)
 				p.right = newChild;
 		}else if(plan instanceof None){
 		}else if(plan instanceof One){
 		}else if(plan instanceof Order){
 			Order p = (Order)plan;
 			p.src = newChild;
 		}else if(plan instanceof Project){
 			Project p = (Project)plan;
 			p.src = newChild;
 		}else if(plan instanceof Rename){
 			Rename p = (Rename)plan;
 			p.src = newChild;
 		}else if(plan instanceof RenameTable){
 			RenameTable p = (RenameTable)plan;
 			p.src = newChild;
 		}else if(plan instanceof Select){
 			Select p = (Select)plan;
 			p.src = newChild;
 		} else if(plan instanceof ThetaJoin){
 			ThetaJoin p = (ThetaJoin)plan;
 			if(p.left == oldChild)
 				p.left = newChild;
 			else if(p.right == oldChild)
 				p.right = newChild;
 		} else {
 			Util.warn("setSrc:meow!!!");
 		}
 	}
 }

 package fatworm.opt;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import fatworm.absyn.Expr;
 import fatworm.logicplan.*;
 import fatworm.util.Util;
 
 public class Optimize {
 
 	public static Plan optimize(Plan plan){
 		//TODO extract QueryCall which is just a fake subquery.
 		plan = decomposeAnd(plan);
 		plan = pushSelect(plan);
 		Util.warn("After push:"+plan.toString());
 		// after the push we transform into theta-join and merge-join
 		// note that by now there shouldn't have been any ThetaJoin plan
 		transformTheta(plan);
 		// TODO next push all single orders inwards to fetchTable
 		Util.warn("After opt:"+plan.toString());
 		return plan;
 	}
 
 	private static Plan pushSelect(Plan plan) {
 		if(plan instanceof Distinct){
 			Distinct p = (Distinct)plan;
 			p.src = pushSelect(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof FetchTable){
 			return plan;
 		}else if(plan instanceof Group){
 			Group p = (Group)plan;
 			p.src = pushSelect(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Join){
 			Join p = (Join)plan;
 			p.left = pushSelect(p.left);
 			p.right = pushSelect(p.right);
 			p.left.parent = p;
 			p.right.parent = p;
 			return p;
 		}else if(plan instanceof None){
 			return plan;
 		}else if(plan instanceof One){
 			return plan;
 		}else if(plan instanceof Order){
 			Order p = (Order)plan;
 			p.src = pushSelect(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Project){
 			Project p = (Project)plan;
 			p.src = pushSelect(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Rename){
 			Rename p = (Rename)plan;
 			p.src = pushSelect(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof RenameTable){
 			RenameTable p = (RenameTable)plan;
 			p.src = pushSelect(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Select){
 			Select p = (Select)plan;
 			if(p.hasPushed){
 				p.src = pushSelect(p.src);
 				p.src.parent = p;
 				return p;
 			}
 			Plan head = p;
 			Plan child = p.src;
 			Select cur = p;
 			
 			while(cur.isPushable()){
 				Util.warn("Pushing Select down!!!"+cur.toString()+", hasPushed="+cur.hasPushed);
 				if(cur.src instanceof RenameTable){
 					RenameTable curChild = (RenameTable) cur.src;
 					String tbl = curChild.alias;
 					for(String newName : curChild.src.getColumns()){
 						String oldName = tbl + "." + Util.getAttr(newName);
 						cur.pred.rename(oldName, newName);
 					}
 				}else if(cur.src instanceof Rename){
 					Rename curChild = (Rename) cur.src;
 					for(int i=0;i<curChild.as.size();i++){
 						String oldName = curChild.as.get(i);
 						String newName = curChild.src.getColumns().get(i);
 						cur.pred.rename(oldName, newName);
 					}
 				}
 				push(cur);
 				if(head == p)
 					head = child;
 			}
 			cur.hasPushed=true;
 			head = pushSelect(head);
 			return head;
 		} else {
 			Util.warn("Optimize:meow!!!");
 		}
 		return plan;
 	}
 
 	private static void push(Select cur) {
 		Plan parent = cur.parent;
 		Plan child = cur.src;
 		if(parent!=null)
 			parent.setSrc(cur, child);
		else{
			child.parent=null;
		}
 		if(child instanceof Distinct){
 			Distinct p = (Distinct)child;
 			cur.setSrc(child, p.src);
 			child.setSrc(p.src, cur);
 		}else if(child instanceof Join){
 			Join p = (Join)child;
 			if(Util.subsetof(cur.getRequestedColumns(), p.left.getColumns())){
 				cur.setSrc(cur.src, p.left);
 				child.setSrc(p.left, cur);
 			}else if(Util.subsetof(cur.getRequestedColumns(), p.right.getColumns())){
 				cur.setSrc(cur.src, p.right);
 				child.setSrc(p.right, cur);
 			}else{
 				Util.warn("select pushing meow!");
 			}
 		}else if(child instanceof Order){
 			Order p = (Order)child;
 			cur.setSrc(child, p.src);
 			child.setSrc(p.src, cur);
 		}else if(child instanceof Project){
 			Project p = (Project)child;
 			cur.setSrc(child, p.src);
 			child.setSrc(p.src, cur);
 		}else if(child instanceof Rename){
 			Rename p = (Rename)child;
 			cur.setSrc(child, p.src);
 			child.setSrc(p.src, cur);
 		}else if(child instanceof RenameTable){
 			RenameTable p = (RenameTable)child;
 			cur.setSrc(child, p.src);
 			child.setSrc(p.src, cur);
 		}else if(child instanceof Select){
 			Select p = (Select)child;
 			cur.setSrc(child, p.src);
 			child.setSrc(p.src, cur);
 		}else{
 			Util.warn("select pushing meow!");
 		}
 	}
 
 	private static void transformTheta(Plan plan) {
 		if(plan instanceof Distinct){
 			Distinct p = (Distinct)plan;
 			transformTheta(p.src);
 		}else if(plan instanceof FetchTable){
 		}else if(plan instanceof Group){
 			Group p = (Group)plan;
 			transformTheta(p.src);
 		}else if(plan instanceof Join){
 			Join p = (Join)plan;
 			transformTheta(p.left);
 			transformTheta(p.right);
 			Plan parent = p.parent;
 			Plan cur = p;
 			if(!(parent instanceof Select))
 				return;
 			ThetaJoin ret = new ThetaJoin(p);
 			while(parent instanceof Select){
 				ret.add(((Select)parent).pred);
 				cur = parent;
 				parent = parent.parent;
 			}
 			if(parent!=null){
 				parent.setSrc(cur, ret);
 			}
 			ret.parent = parent;
 		}else if(plan instanceof None){
 		}else if(plan instanceof One){
 		}else if(plan instanceof Order){
 			Order p = (Order)plan;
 			transformTheta(p.src);
 		}else if(plan instanceof Project){
 			Project p = (Project)plan;
 			transformTheta(p.src);
 		}else if(plan instanceof Rename){
 			Rename p = (Rename)plan;
 			transformTheta(p.src);
 		}else if(plan instanceof RenameTable){
 			RenameTable p = (RenameTable)plan;
 			transformTheta(p.src);
 		}else if(plan instanceof Select){
 			Select p = (Select)plan;
 			transformTheta(p.src);
 		} else if(plan instanceof ThetaJoin){
 			ThetaJoin p = (ThetaJoin)plan;
 			transformTheta(p.left);
 			transformTheta(p.right);
 		} else {
 			Util.warn("Optimize:meow!!!");
 		}
 	}
 
 	private static Plan decomposeAnd(Plan plan) {
 		if(plan instanceof Distinct){
 			Distinct p = (Distinct)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof FetchTable){
 			return plan;
 		}else if(plan instanceof Group){
 			Group p = (Group)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Join){
 			Join p = (Join)plan;
 			p.left = decomposeAnd(p.left);
 			p.right = decomposeAnd(p.right);
 			p.left.parent = p;
 			p.right.parent = p;
 			return p;
 		}else if(plan instanceof None){
 			return plan;
 		}else if(plan instanceof One){
 			return plan;
 		}else if(plan instanceof Order){
 			Order p = (Order)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Project){
 			Project p = (Project)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Rename){
 			Rename p = (Rename)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof RenameTable){
 			RenameTable p = (RenameTable)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			return p;
 		}else if(plan instanceof Select){
 			Select p = (Select)plan;
 			p.src = decomposeAnd(p.src);
 			p.src.parent = p;
 			if(!p.pred.isAnd())
 				return p;
 			List<Expr> exprList = new LinkedList<Expr>();
 			p.pred.collectCond(exprList);
 			Plan ret = p.src;
 			for (Expr e : exprList){
 				ret = new Select(ret, e);
 			}
 			ret.parent = p.parent;
 			return ret;
 		} else {
 			Util.warn("Optimize:meow!!!");
 		}
 		return null;
 	}
 }

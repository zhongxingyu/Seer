 package oop.JavaGrinder.cc;
 
 
 import oop.ccMethod;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class ccStatement extends Visitor{
 	String line="";
 	private boolean assignmentFlag;
 	private ccBlock block;
 	
 	public ccStatement(GNode n, ccBlock parent) {
 		block = parent;
 		dispatch(n);
 	}
 	
 	/**
 	 * DON'T USE THIS CONSTRUCTOR OUTSIDE OF ccMASTER
 	 */
 	public ccStatement(GNode n) {
 		dispatch(n);
 	}
 	
 	public void visitExpression(GNode n){
 		if (n.get(1).equals("=")){
 			assignmentFlag = true;
 		}
 		visit(n);
 	}
 	
 	public void visitType(GNode n){
 		//for arrays
 		if((n.get(1) instanceof Node)&&n.getNode(1).hasName("Dimensions")){
 			line+="__rt::Ptr<__rt::Array<";
 			dispatch(n.getNode(0));
 			line+="> >";
 
 		}
 		else{
 			visit(n);
 		}
 	}
 	public void visitReturnStatement(GNode n){
 		line += "return ";
 		visit(n);
 		line+=";";
 	}
 	public void visitCallExpression(GNode n){
 		if(n.getString(2).contains("print")){
 			line+="std::cout << ";
 			dispatch(n.getGeneric(3));
 			if(n.getString(2).equals("println")){
 				line+=" << std::endl";
 			}
 		}
 		else{
 			String objectType = block.currentClass;
 			String __this = "__this";
 			ccMethod methodInQuestion;
 			if(null != n.getNode(0)){
 				if(n.getNode(0).get(0) instanceof Node && n.getNode(0).getNode(0).getName().equals("CastExpression")){
 					visit(n.getNode(0).getNode(0));
 				}
 				else if(n.getNode(0).get(0) instanceof String && block.variables.containsKey(n.getNode(0).getString(0))){
 					__this = n.getNode(0).getString(0);
 					objectType = block.variables.get(__this).getType();
 				}
 			} 
 			for(int i=0; i< block.classList.size(); i++){
 				if(block.classList.get(i).getName().startsWith(objectType)){ 
 //					System.out.println("  " + findArgumentTypes(n.getNode(3)).length);
 //					System.out.println("  " + block.classList.get(i).getMethodName(n.getString(2), findArgumentTypes(n.getNode(3))));
 					for(String s : findArgumentTypes(n.getNode(3))){
 					}
 					methodInQuestion = block.classList.get(i).getMethod(n.getString(2), findArgumentTypes(n.getNode(3)), block.classList);
 //					System.out.println("    " + methodInQuestion.isStatic);
 					if(methodInQuestion.isStatic){
 						line+= "__" + objectType + "::" + methodInQuestion.getName() + "(";
 					}
 					else if(methodInQuestion.access.contentEquals("private")){
 						line+= "__" + objectType + "::" + methodInQuestion.getName() + "(" + __this;
 					}
 					else {
 						line+= __this + "->__vptr->" + methodInQuestion.getName() + "(" + __this;
 					}
 					for(int j = 0; j < n.getNode(3).size(); j++){
 						line+= ", ";
 						if(n.getNode(3).getNode(j).hasName("StringLiteral")){
 							line+="new __String("+n.getNode(3).getNode(j).getString(0)+")";
 						}
 						else{
 							dispatch(n.getNode(3).getNode(j));
 						}
 					}
 					line+= ")";
 				}
 			}
 		}
 	}
 	public void visitCastExpression(GNode n){
 //		System.out.println(n);
 		line+="(";
 		dispatch(n.getNode(0));
 		line+=")";
 		dispatch(n.getNode(1));
 		
 	}
	public void visitBasicCastExpression(GNode n){
		line+="(";
		dispatch(n.getNode(0));
		line+=")";
		dispatch(n.getNode(2));
	}
 	public void visitUnaryExpression(GNode n){
 		visit(n);
 	}
 	
 	public void visitMultiplicativeExpression(GNode n){
 		visit(n);
 	}
 	public void visitSubscriptExpression(GNode n){
 		line+="(*";
 		dispatch(n.getNode(0));
 		line+=")[";
 		dispatch(n.getNode(1));
 		line+="]";
 		
 	}
 	public void visitIntegerLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitFloatingPointLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitStringLiteral(GNode n){
 		if (assignmentFlag){
 			line+=" new __String(" + n.get(0) +")";
 		}
 		else{
 			line+=(String) n.get(0);
 		}
 	}
 	public void visitBooleanLiteral(GNode n){
 		line+=(String) n.get(0);		
 	}
 	
 	public void visitBlock(GNode n){
 //		line+="{\n";
 		ccBlock blockStatement = new ccBlock(n, block.variables, block.localVariables, block.classList, block.currentClass, false);
 //		line+="}\n";
 		while (!blockStatement.blockLines.isEmpty())
 			line += blockStatement.blockLines.remove()+"\n";
 	}
 	
 	public void visitConditionalStatement(GNode n){
 		line += "if("+new ccStatement((GNode)n.get(0), block).publish()+")\n";
 		dispatch(n.getNode(1));
 		line += "\n else ";
 		dispatch(n.getNode(2));
 //		System.out.println(line);
 	}
 	public void visitForStatement(GNode n){
 		line = "for(";
 		dispatch(n.getGeneric(0));
 		line+=")\n";
 		dispatch(n.getGeneric(1));
 	}
 	public void visitBasicForControl(GNode n){
 		dispatch(n.getNode(0));
 		dispatch(n.getNode(1));
 		dispatch(n.getNode(2));
 		dispatch(n.getNode(3));
 		line+=";";
 		dispatch(n.getNode(4));
 	}
 	public void visitBreakStatement(GNode n){
 		line = "break;";
 	}
 	public void visitWhileStatement(GNode n){
 		line = "while("+new ccStatement((GNode)n.get(0), block).publish()+")";
 //		System.out.println(line);
 	}
 	public void visitDeclarator(GNode n){
 		line+=n.getString(0);
 		if(n.getNode(1)==null)
 			line+="= ";
 		else
 			line+=n.getString(1);
 		dispatch(n.getGeneric(2));
 		line+=";";
 	}
 	public void visitRelationalExpression(GNode n){
 		visit(n);
 	}
 	public void visitBitwiseOrExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+="|";
 		dispatch(n.getNode(1));
 	}
 	public void visitBitwiseAndExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+=" & ";
 		dispatch(n.getNode(1));
 	}
 	public void visitExpressionStatement(GNode n){
 		visit(n);
 		line+=";";
 	}
 	public void visitArguments(GNode n){
 		line += "(";
 		int i = 0;
 		for (Object o : n){
 			if(i > 0){line += ", ";}
 			if (o instanceof Node){
 				dispatch((Node)o);
 			}
 			i++;
 		}
 		line += ")";
 	}
 	
 	public void visitSelectionExpression(GNode n){
 		if(block.variables.get(n.getString(n.size()-1)) == null){
 			ccClass c;
 			String oType = block.variables.get(n.getNode(0).getString(0)).getType();
 			for(int i = 3; i<block.classList.size(); i++){
 				if(block.classList.get(i).getName().contentEquals(oType)){
 					c = block.classList.get(i);
 					line += c.get_Name() + c.findField(n.getString(n.size()-1)).publish();
 				}
 			}
 		}
 		else{
 			line+= block.variables.get((n.getString(n.size()-1))).publish();
 		}
 		
 	}
 	public void visitThisExpression(GNode n){
 		line+="__this";
 	}
 	public void visitNewArrayExpression(GNode n){
 		line+="new __rt::Array<";
 		dispatch(n.getNode(0));
 		line+=">(";
 		dispatch(n.getNode(1));
 		line+=")";
 	}
 	public void visitPrimaryIdentifier(GNode n){
 		if(block.variables.isEmpty()&&block.variables.get(n.get(0))!=null){
 			line+= block.variables.get(n.get(0)).publish();
 		}
 		else{
 			if(!((block==null)||(block.getLocalVariables().containsKey(n.get(0)))||(block.getIsConstructorBlock()))){
 				line+= "__this->" + ccHelper.convertType(n.getString(0));	
 			}	
 			else{
 				line+= ccHelper.convertType(n.getString(0));
 			}
 		}
 	}
 	
 	public void visitNullLiteral(GNode n){
 		line += "__rt::null()";
 	}
 	
 	public void visitNewClassExpression(GNode n){
 		line+= "new __";
 		visit(n);
 	}
 	
 	public  String publish() {
 		return line;
 	}
 	
 	public String[] findArgumentTypes(Node n){
 		String[] argTypes = new String[n.size()];
 		for(int i=0; i< n.size(); i++){
 			if(n.getNode(i).getName().matches("IntegerLiteral")){
 				argTypes[i] = "int32_t";
 			}
 			else if(n.getNode(i).getName().matches("FloatingPointLiteral")){
 				argTypes[i] = "double";
 			}
 			else if(n.getNode(i).getName().matches("StringLiteral")){
 				argTypes[i] = "String";
 			}
 			else if(n.getNode(i).getName().matches("BooleanLiteral")){
 				argTypes[i] = "bool";
 			}
 			else if(n.getNode(i).getName().matches("ThisExpression")){
 				argTypes[i] = block.currentClass;
 			}
 			else if(n.getNode(i).getName().matches("PrimaryIdentifier")){
 				argTypes[i] = block.variables.get(n.getNode(i).getString(0)).getType();
 			}
 			else if(n.getNode(i).getName().matches("AdditiveExpression")){
 				argTypes[i] = "int32_t";
 			}
 			else if(n.getNode(i).getName().matches("CastExpression")){
 				argTypes[i] = n.getNode(i).getNode(0).getNode(0).getString(0);
 			}
 		}
 		return argTypes;
 	}
 	
 	 public void visitLogicalAndExpression(GNode n){
 	        if(n.getGeneric(0).toString().contains("LogicalAndExpression")){
 	            dispatch(n.getGeneric(0));
 	        }
 	        else{
 	            line+="(";        
 	            dispatch(n.getGeneric(0));
 	            line+=")";
 	            }
 	        line+=" && ";
 	        line+="(";        
 	        dispatch(n.getGeneric(1));
 	        line+=")";
 
  }
 	 public void visitEqualityExpression(GNode n){ 
 		 visit(n);
 	}
 	public void visitLogicalNegationExpression(GNode n){
 		line+="!(";
 		visit(n);
 		line+=")";
 	}
 	public void visitInstanceOfExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+=" instanceof ";
 		dispatch(n.getNode(1));
 	}
 	
 	public void visit(Node n) {
 		for (Object o : n){
 			if (o instanceof Node){
 				dispatch((Node)o);
 			}
 			else if(o instanceof String){
 				if(((String) o).matches("=")){
 					line+=" = ";
 				}
 				else{
 					line+=ccHelper.convertType(o.toString());
 				}
 			}
 		}
 	}
 }

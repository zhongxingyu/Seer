 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Vector;
 
 import miniX10.syntaxtree.*;
 import miniX10.visitor.*;
 import helperFunctions.*;
 
 public class MHP extends DepthFirstVisitor{
 
 	private HashMap<Node, MOL> molSet;
 	private HashMap<String, Node> nameSet;
 	
 	//public MHP(Node file) {
 	public MHP() {
 		molSet = new HashMap<Node, MOL>();
 	}
 	
 	public HashMap<Node, MOL> getMolSet(){return molSet;}
 	public HashMap<String, Node> getNameSet(){return nameSet;}
 	
 	//get MOL
 	public MOL getMOL(Node n){
 
 		if( molSet.containsKey(n.hashCode()) )
 			return (MOL)molSet.get(n.hashCode());
 		else
 			return null;
 	}
 	
 	//helper for crossproduct
 	public HashSet<Pair <Node, Node>> crossProduct(HashSet<Node> l, HashSet<Node> r){
 		HashSet<Pair <Node, Node>> result = new HashSet<Pair <Node, Node>>();
 				
 		Iterator<Node> lIter = l.iterator();
 		while(lIter.hasNext()){
 			
 			Node lNode = lIter.next();
 			Iterator<Node> rIter = r.iterator();
 			while(rIter.hasNext()){
 				
 				Node rNode = rIter.next();
 				result.add(new Pair<Node, Node>(lNode, rNode));
 			}
 		}
 		
 		return result;
 	}
 	
 	//helper function for MOL
 	public MOL nodeVectorMOL(Vector<Node> v){
 		MOL result = new MOL();
 		Iterator<Node> iter = v.iterator();
 		while(iter.hasNext()){
 			
 			Node temp = iter.next();
 			temp.accept(this);
 
 			MOL tempMOL = getMOL(temp);
 
 			if( tempMOL != null ){	
 				result.M.addAll(tempMOL.M);
 				result.O.addAll(tempMOL.O);
 				result.L.addAll(tempMOL.L);
 			
 				result.M.addAll(crossProduct(result.O, tempMOL.L));		
 			}
 			
 		}
 		
 		return result;
 	}
 	
 	//helper function for sequence
 	public MOL sequenceMOL(Node l, Node r){
 		MOL result = new MOL();
 		MOL left = getMOL(l);
 		MOL right = getMOL(r);
 
 		if( left != null ){
 			result.M.addAll(left.M);
 			result.O.addAll(left.O);
 			result.L.addAll(left.L);
 		}
 
 		if( right != null ){
 			result.M.addAll(right.M);
 			result.O.addAll(right.O);
 			result.L.addAll(right.L);
 		}
 		
 		if( left != null && right != null)
 		result.M.addAll(crossProduct(left.O, right.L));
 				
 		return result;
 	}
 	
 	//helper function for adding MOL for node n to molSet
 	public void putMOLforNode(Node n, MOL mol){
 		
 		if( mol != null ){
 			MOL result = new MOL(mol);
 			molSet.put(n, result);
 		}
 
 	}
 	
	
 	@Override
 	public void visit(MethodDeclaration n) {
 		super.visit(n);
		
 		nameSet.put(n.identifier.nodeToken.tokenImage, n);
 		MOL result = getMOL(n.block);
 		if( result != null )
 			molSet.put(n, result);
 	}
 	@Override
 	public void visit(BlockStatement n) {
 		super.visit(n);
 		MOL result = getMOL(n.nodeChoice.choice);
 		if( result != null )
 			molSet.put(n, result);
 	}
 	@Override
 	public void visit(FinalVariableDeclaration n) {
 		super.visit(n);
 		MOL result = getMOL(n.expression);
 		if( result == null ){
 			result = new MOL();
 		}
 		result.L.add(n);
 		molSet.put(n, result);
 	}
 	@Override
 	public void visit(UpdatableVariableDeclaration n) {
 		super.visit(n);
 		MOL result = getMOL(n.expression);
 		if( result == null ){
 			result = new MOL();
 		}
 		result.L.add(n);
 		molSet.put(n, result);
 	}
 	
 	//STATEMENTS	
 
 
 
 
 
 	@Override
 	public void visit(Statement n) {
 		super.visit(n);
 		MOL choice = getMOL(n.nodeChoice.choice);
 		putMOLforNode(n, choice);
 	}
 	@Override
 	public void visit(Assignment n) {
 		
 		super.visit(n);
 		
 		MOL result = sequenceMOL(n.expression, n.expression1);
 		result.L.add(n);
 		molSet.put(n, result);
 		
 	}
 	@Override
 	public void visit(AsyncStatement n) {
 		
 		super.visit(n);
 		
 		MOL result = new MOL();
 		MOL expression = getMOL(n.expression);
 		MOL block = getMOL(n.block);
 		
 		if( expression != null ){
 			result.M.addAll(expression.M);
 			result.O.addAll(expression.O);
 			result.L.addAll(expression.L);
 		}
 		if( block != null ){
 			result.M.addAll(block.M);
 			result.O.addAll(block.L);
 			result.L.addAll(block.L);
 		}
 		if( expression != null && block != null )
 		result.M.addAll(crossProduct(expression.O, block.L));
 		
 		result.L.add(n);
 		molSet.put(n, result);		
 	}
 	@Override
 	public void visit(Block n) {
 		
 		super.visit(n);	
 		MOL result = nodeVectorMOL(n.nodeListOptional.nodes);
 	
 		molSet.put(n, result);
 	
 	}	
 	@Override
 	public void visit(FinishStatement n) {
 		
 		super.visit(n);
 		
 		MOL result = new MOL();
 		MOL statement= getMOL(n.statement);
 		
 		if( statement != null ){
 			result.M.addAll(statement.M);
 			//Empty O
 			result.L.addAll(statement.L);			
 		}
 
 		result.L.add(n);
 		
 		molSet.put(n, result);
 		
 	}	
 	@Override
 	public void visit(IfStatement n) {
 		super.visit(n);
 					
 		MOL result = sequenceMOL(n.expression, n.statement);
 		MOL expression = getMOL(n.expression);
 		
 		//node optional could be empty
 		if( n.nodeOptional.node != null ){
 			
 			MOL tempNode = getMOL(n.nodeOptional.node);
 			if( tempNode != null ){
 				result.M.addAll(tempNode.M);
 				result.O.addAll(tempNode.O);
 				result.L.addAll(tempNode.L);
 			}
 			
 			if( expression != null && tempNode != null )
 				result.M.addAll(crossProduct(expression.O, tempNode.L));
 		}
 		
 		molSet.put(n, result);
 	}
 	@Override
 	public void visit(LoopStatement n) {
 		super.visit(n);
 		
 		MOL expression = getMOL(n.expression);
 		MOL statement = getMOL(n.statement);
 		
 		MOL result = sequenceMOL(n.expression, n.statement);	
 		if( expression != null && statement != null )
 			result.M.addAll(crossProduct(statement.O, expression.L));
 		
 		result.L.add(n);
 		
 		molSet.put(n, result);
 		
 	}
 	@Override
 	public void visit(PostfixStatement n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 	@Override
 	public void visit(PrintlnStatement n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 	@Override
 	public void visit(ReturnStatement n) {
 
 		super.visit(n);
 		//node is optional, it could be null 
 		if( n.nodeOptional.node != null ){
 			MOL node = getMOL(n.nodeOptional.node);
 			putMOLforNode(n, node);
 		}
 		
 	}
 	@Override
 	public void visit(ThrowStatement n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 	@Override
 	public void visit(WhileStatement n) {
 		super.visit(n);
 		
 		MOL result = new MOL();
 		MOL expression = getMOL(n.expression);
 		MOL statement = getMOL(n.statement);
 		
 		if( expression != null ) {
 			result.M.addAll(expression.M);
 			result.O.addAll(expression.O);
 			result.L.addAll(expression.L);
 		}
 
 		if( statement != null ){
 			result.M.addAll(statement.M);
 			result.O.addAll(statement.O);
 			result.L.addAll(statement.L);
 		}
 		
 		//because expression and statement are executed repeatedly
 		if( expression != null && statement != null )
 			result.M.addAll(crossProduct(result.O, result.L));
 		
 		result.L.add(n);
 		
 		molSet.put(n, result);
 
 		
 	}
 
 	
 	//EXPRESSIONS
 	
 	@Override
 	public void visit(Expression n) {
 		super.visit(n);
 		MOL nodeTemp = getMOL(n.nodeChoice.choice);
 		if( nodeTemp !=  null ){
 			MOL result = new MOL(nodeTemp);
 			molSet.put(n, result);	
 		}
 	}
 
 	@Override
 	public void visit(InclusiveOrExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(EqualsExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(NotEqualsExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(GreaterThanExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(PlusExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(MinusExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(TimesExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(DivideExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.expression);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(SinExpression n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 
 	@Override
 	public void visit(CosExpression n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 
 	@Override
 	public void visit(PowExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.expression, n.expression1);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(AbsExpression n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 
 	@Override
 	public void visit(MapExpression n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.primaryExpression, n.primaryExpression1);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(RegionConstant n) {
 		super.visit(n);
 		//node optional may or may not exist
 		MOL molResult = ( n.nodeOptional.node == null ) ? getMOL(n.colonExpression) : sequenceMOL(n.colonExpression, n.nodeOptional.node); 	
 		putMOLforNode(n, molResult);
 	}
 	@Override
 	public void visit(ColonExpression n) {
 		super.visit(n);
 		MOL choice = getMOL(n.nodeChoice.choice);
 		putMOLforNode(n, choice);
 	}
 	@Override
 	public void visit(ColonPair n) {
 		super.visit(n);
 		MOL result = sequenceMOL(n.expression, n.expression1);
 		molSet.put(n, result);
 	}
 	@Override
 	public void visit(ColonRest n) {
 		super.visit(n);
 		MOL node = getMOL(n.colonExpression);
 		putMOLforNode(n, node);
 	}
 		
 
 	@Override
 	public void visit(UnaryMinusExpression n) {
 		super.visit(n);
 		MOL primaryExpression = getMOL(n.primaryExpression);
 		putMOLforNode(n, primaryExpression);
 	}
 	@Override
 	public void visit(CoercionToIntExpression n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 	@Override
 	public void visit(CoercionToDoubleExpression n) {
 		super.visit(n);
 		MOL expression = getMOL(n.expression);
 		putMOLforNode(n, expression);
 	}
 
 	@Override
 	public void visit(TypeAnnotatedExpression n) {
 		// TODO Auto-generated method stub
 		super.visit(n);
 	}
 
 	@Override
 	public void visit(FactoryBlock n) {
 		super.visit(n);
 		MOL result = getMOL(n.expression);
 		putMOLforNode(n, result);
 	}
 
 	@Override
 	public void visit(ArrayAccess n) {
 		// TODO Auto-generated method stub
 		super.visit(n);
 	}
 
 	@Override
 	public void visit(DotMethodCall n) {
 		super.visit(n);
 		Vector<Node> tempVector = new Vector<Node>();
 		
 		//node optional may be null
 		if( n.nodeOptional.node != null ){			
 			String bodyName = n.identifier.nodeToken.tokenImage;
 			Node bodyNode = nameSet.get(bodyName);
 			tempVector.add(bodyNode);
 		}
 	
 		MOL result = nodeVectorMOL(tempVector);
 		molSet.put(n, result);
 	}
 
 	@Override
 	public void visit(DotDistribution n) {
 		super.visit(n);
 		MOL nodeMOL = getMOL(n.primaryExpression);
 		putMOLforNode(n, nodeMOL);
 	}
 	@Override
 	public void visit(DotIsFirst n) {
 		super.visit(n);
 		MOL nodeMOL = getMOL(n.primaryExpression);
 		putMOLforNode(n, nodeMOL);
 	}
 
 	@Override
 	public void visit(DotIdentifier n) {
 		super.visit(n);
 		
 	}	
 	
 	@Override
 	public void visit(PrimaryExpression n) {
 		// TODO Auto-generated method stub
 		super.visit(n);
 	}
 
 	@Override
 	public void visit(AllocationExpression n) {
 		super.visit(n);
 		MOL result = getMOL(n.nodeChoice.choice);
 		putMOLforNode(n, result);
 	}
 
 	@Override
 	public void visit(NewObject n) {
 		super.visit(n);
 		if( n.nodeOptional.node != null ){
 			MOL result = getMOL(n.nodeOptional.node);
 			putMOLforNode(n, result);
 		}
 		
 	}
 	@Override
 	public void visit(NewUpdatableArray n) {
 		super.visit(n);
 		if( n.nodeOptional.node != null ){
 			MOL result = getMOL(n.nodeOptional.node);
 			putMOLforNode(n, result);
 		}
 	}
 
 	@Override
 	public void visit(ExpressionList n) {
 		super.visit(n);
 		Vector<Node> expr = new Vector<Node>();
 		expr.add(n.expression);
 		
 		Vector<Node> args = n.nodeListOptional.nodes;
 		Iterator<Node> iter = args.iterator();
 		while( iter.hasNext() ){
 			ArgumentRest temp = (ArgumentRest) iter.next();
 			expr.add(temp.expression);
 		}
 		
 		MOL result = nodeVectorMOL(expr);
 		molSet.put(n, result);
 			
 	}
 
 	@Override
 	public void visit(ArrayInitializer n) {
 		super.visit(n);
 		MOL result = getMOL(n.block);
 		putMOLforNode(n, result);
 	}	
 	
 	
 }

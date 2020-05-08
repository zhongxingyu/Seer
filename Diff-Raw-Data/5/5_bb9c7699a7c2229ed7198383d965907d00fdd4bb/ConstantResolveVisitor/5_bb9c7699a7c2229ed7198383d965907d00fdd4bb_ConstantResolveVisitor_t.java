 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.semAnalysis;
 
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import com.sc2mod.andromeda.environment.types.BasicType;
 import com.sc2mod.andromeda.environment.types.Type;
 import com.sc2mod.andromeda.environment.variables.VarDecl;
 import com.sc2mod.andromeda.notifications.CompilationError;
 import com.sc2mod.andromeda.parsing.VisitorErrorAdapater;
 import com.sc2mod.andromeda.syntaxNodes.BinaryExpression;
 import com.sc2mod.andromeda.syntaxNodes.BinaryOperator;
 import com.sc2mod.andromeda.syntaxNodes.CastExpression;
 import com.sc2mod.andromeda.syntaxNodes.Expression;
 import com.sc2mod.andromeda.syntaxNodes.FieldAccess;
 import com.sc2mod.andromeda.syntaxNodes.KeyOfExpression;
 import com.sc2mod.andromeda.syntaxNodes.LiteralExpression;
 import com.sc2mod.andromeda.syntaxNodes.UnaryExpression;
 import com.sc2mod.andromeda.syntaxNodes.UnaryOperator;
 import com.sc2mod.andromeda.syntaxNodes.VariableAssignDecl;
 import com.sc2mod.andromeda.vm.data.BoolObject;
 import com.sc2mod.andromeda.vm.data.DataObject;
 import com.sc2mod.andromeda.vm.data.Fixed;
 import com.sc2mod.andromeda.vm.data.FixedObject;
 import com.sc2mod.andromeda.vm.data.IntObject;
 import com.sc2mod.andromeda.vm.data.StringObject;
 
 public class ConstantResolveVisitor extends VisitorErrorAdapater{
 
 	public ConstantResolveVisitor() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	private int resolveCount = 0;
 	private boolean resolveRemaining;
 	
 	private LinkedList<Expression> expressionsToResolve = new LinkedList<Expression>();
 	private LinkedList<VariableAssignDecl> declsToResolve = new LinkedList<VariableAssignDecl>();
 	private void addToResolveList(Expression e) {
 		if(resolveRemaining) return;
 		expressionsToResolve.add(e);		
 	}
 
 	private void addToResolveList(VariableAssignDecl vd) {
 		if(resolveRemaining) return;
 		declsToResolve.add(vd);
 	}
 	
 	public void resolveRemainingExprs(){
 		resolveRemaining = true;
 		int resolveCount = this.resolveCount;
 		int resolveCountBefore = -1;
 		
 		//As long as something happens try again
 		while(resolveCountBefore < resolveCount){
 			resolveCountBefore = resolveCount;
 			
 			ListIterator<Expression> li = expressionsToResolve.listIterator();
 			while(li.hasNext()){
 				Expression e = li.next();
 				e.accept(this);
 				
 				//Resolve successful!
 				if(this.resolveCount != resolveCount){
 					li.remove();
 					resolveCount = this.resolveCount;
 				}
 			}
 			
 			ListIterator<VariableAssignDecl> li2 = declsToResolve.listIterator();
 			while(li2.hasNext()){
 				VariableAssignDecl e = li2.next();
 				e.accept(this);
 				
 				//Resolve successful!
 				if(this.resolveCount != resolveCount){
 					li2.remove();
 					resolveCount = this.resolveCount;
 				}
 			}
 		}
 	}
 
 	
 	@Override
 	public void visit(LiteralExpression literalExpression) {
 		literalExpression.setValue(literalExpression.getLiteral().getValue());
 		resolveCount++;
 	}
 	
 	@Override
 	public void visit(KeyOfExpression keyOfExpression) {
 		Type t = keyOfExpression.getInferedType();
 		if(!t.isKeyType()){
 			throw new CompilationError(keyOfExpression,"The keyof operator can only be used on key type extensions (extensions with the iskey modifier)");
 		}
 		keyOfExpression.setValue(t.getNextKey());
 		
 		
 	}
 	
 	@Override
 	public void visit(UnaryExpression unaryExpression) {	
 		Type type = unaryExpression.getExpression().getInferedType();
 		int op = unaryExpression.getOperator();
 		Expression expr = unaryExpression.getExpression();
 		DataObject val = expr.getValue();
 		if(val == null){
 			addToResolveList(unaryExpression);
 			return;
 		}
 		resolveCount++;
 				
 		switch (op) {
 		case UnaryOperator.POSTMINUSMINUS:
 		case UnaryOperator.PREMINUSMINUS:
 		case UnaryOperator.POSTPLUSPLUS:
 		case UnaryOperator.PREPLUSPLUS:
 			break;
 		case UnaryOperator.MINUS:
 			if(type == BasicType.INT){
 				int i = val.getIntValue();
 				unaryExpression.setValue(new IntObject(-i));
 				break;
 			} 
 			if(type == BasicType.FLOAT){
 				Fixed f = val.getFixedValue();
 				unaryExpression.setValue(new FixedObject(f.negate()));
 				break;
 			}
 			throw new Error("!");
 		case UnaryOperator.COMP:	
 		{
 			int i = val.getIntValue();
 			unaryExpression.setValue(new IntObject(~i));
 			break;				
 		}	
 		case UnaryOperator.NOT:
 			//Not can be a boolean not or a test for "not null"
 			if(type == BasicType.BOOL){
 				boolean i = val.getBoolValue();
 				unaryExpression.setValue(BoolObject.getBool(!i));
 				break;	
 			} else if(type.canBeNull()){
 				boolean i = val.isNull();
 				unaryExpression.setValue(BoolObject.getBool(!i));
 				break;	
 			}
 			throw new Error("!");
 		default:
 			throw new CompilationError(unaryExpression,"UnknownOperand");
 		
 		}
 
 	}
 	
 	@Override
 	public void visit(FieldAccess nameExpression) {
 		VarDecl v = (VarDecl) nameExpression.getSemantics();
 		DataObject d = v.getValue();
 		if(d == null){
 			addToResolveList(nameExpression);
 		} else {
 			nameExpression.setValue(d);
 			resolveCount++;
 		}
 	}
 	
 	@Override
 	public void visit(VariableAssignDecl variableAssignDecl) {
 		VarDecl vd = (VarDecl) variableAssignDecl.getSemantics();
 		DataObject d = variableAssignDecl.getInitializer().getValue();
 		if(d == null){
 			addToResolveList(variableAssignDecl);
 		} else {
 			vd.setValue(d.castTo(vd.getType()));
 			resolveCount++;
 		}
 	}
 	
 	@Override
 	public void visit(CastExpression castExpression) {
 		DataObject val = castExpression.getRightExpression().getValue();
 		if(val == null){
 			addToResolveList(castExpression);
 			return;
 		}
 		
 		castExpression.setValue(val.castTo(castExpression.getInferedType()));
 	}
 
 	@Override
 	public void visit(BinaryExpression binaryExpression) {
 		
 		Type resultType = binaryExpression.getInferedType();
 		
 		//The type of a binop is related to the operator
 		int op = binaryExpression.getOperator();
 		Expression lExpr = binaryExpression.getLeftExpression();
 		Expression rExpr = binaryExpression.getRightExpression();
 		Type left = lExpr.getInferedType().getBaseType();
 		Type right = rExpr.getInferedType().getBaseType();
 		DataObject lVal = lExpr.getValue();
 		DataObject rVal = rExpr.getValue();
 		
 
 		if(lVal == null || rVal == null){
 			addToResolveList(binaryExpression);
 			return;
 		}
 		resolveCount++;
 		
 		switch (op) {
 		case BinaryOperator.OROR:
 		case BinaryOperator.ANDAND:
 		{
 			boolean b1 = lVal.getBoolValue();
 			boolean b2 = rVal.getBoolValue();
 			boolean result;
 			switch (op) {
 			case BinaryOperator.OROR:	result = b1 || b2; break;
 			case BinaryOperator.ANDAND:	result = b1 && b2; break;
 			default:					throw new Error("!");
 			}
 			binaryExpression.setValue(BoolObject.getBool(result));
 			break;
 		}
 		case BinaryOperator.XOR: 
 		{
 			if(left == BasicType.BOOL){
 				boolean b1 = lVal.getBoolValue();
 				boolean b2 = rVal.getBoolValue();
 				boolean result;
 				result = b1 ^ b2;
 				binaryExpression.setValue(BoolObject.getBool(result));
 				break;
 			} else {
 				int b1 = lVal.getIntValue();
 				int b2 = rVal.getIntValue();
 				int result;
 				result = b1 ^ b2;
 				binaryExpression.setValue(new IntObject(result));
 				break;
 			}
 		
 		}
 		case BinaryOperator.PLUS:
 		{
 			if(resultType == BasicType.TEXT|| resultType == BasicType.STRING){
				//XPilot: replaced getStringValue() with toString()
				String s1 = lVal.toString();
				String s2 = rVal.toString();
 				binaryExpression.setValue(new StringObject(s1 + s2));
 				break;
 			}
 		}
 		case BinaryOperator.MINUS:
 		case BinaryOperator.DIV:
 		case BinaryOperator.MULT:
 		case BinaryOperator.MOD:
 		{
 			//If one of both operands is float, then the result is float, else int
 			if(resultType == BasicType.FLOAT){
 				Fixed f1 = lVal.getFixedValue();
 				Fixed f2 = rVal.getFixedValue();
 				Fixed result;
 				switch(op){
 				case BinaryOperator.PLUS:	result = Fixed.sum(f1, f2); break;
 				case BinaryOperator.MINUS:	result = Fixed.difference(f1, f2); break;
 				case BinaryOperator.DIV:	result = Fixed.quotient(f1, f2); break;
 				case BinaryOperator.MULT:	result = Fixed.product(f1, f2); break;
 				case BinaryOperator.MOD:	result = Fixed.modulus(f1, f2); break;
 				default:					throw new Error("!");
 				}
 				binaryExpression.setValue(new FixedObject(result));
 			} else {
 				int i1 = lVal.getIntValue();
 				int i2 = rVal.getIntValue();
 				int result;
 				switch(op){
 				case BinaryOperator.PLUS:	result = i1 + i2; break;
 				case BinaryOperator.MINUS:	result = i1 - i2; break;
 				case BinaryOperator.DIV:	result = i1 / i2; break;
 				case BinaryOperator.MULT:	result = i1 * i2; break;
 				case BinaryOperator.MOD:	result = i1 % i2; break;
 				default:					throw new Error("!");
 				}
 				binaryExpression.setValue(new IntObject(result));	
 			}
 			break;
 		}
 		case BinaryOperator.EQEQ:
 		case BinaryOperator.NOTEQ:
 			if(left == BasicType.BOOL&&right == BasicType.BOOL){
 				boolean b1 = lVal.getBoolValue();
 				boolean f2 = rVal.getBoolValue();
 				boolean result;
 				switch(op){
 				case BinaryOperator.EQEQ:	result = b1 == f2; break;
 				case BinaryOperator.NOTEQ:	result = b1 != f2; break;
 				default:					throw new Error("!");
 				}
 				binaryExpression.setValue(BoolObject.getBool(result));
 				break;
 			}
 		case BinaryOperator.LT:
 		case BinaryOperator.GT:
 		case BinaryOperator.LTEQ:
 		case BinaryOperator.GTEQ:
 			if(left == BasicType.FLOAT||right == BasicType.FLOAT){
 				Fixed f1 = lVal.getFixedValue();
 				Fixed f2 = rVal.getFixedValue();
 				boolean result;
 				switch(op){
 				case BinaryOperator.EQEQ:	result = Fixed.equal(f1, f2); break;
 				case BinaryOperator.NOTEQ:	result = Fixed.notEqual(f1, f2); break;
 				case BinaryOperator.LT:		result = Fixed.lessThan(f1, f2); break;
 				case BinaryOperator.GT:		result = Fixed.greaterThan(f1, f2); break;
 				case BinaryOperator.LTEQ:	result = Fixed.lessThanOrEqualTo(f1, f2); break;
 				case BinaryOperator.GTEQ:	result = Fixed.greaterThanOrEqualTo(f1, f2); break;
 				default:					throw new Error("!");
 				}
 				binaryExpression.setValue(BoolObject.getBool(result));
 			} else {
 				int i1 = lVal.getIntValue();
 				int i2 = rVal.getIntValue();
 				boolean result;
 				switch(op){
 				case BinaryOperator.EQEQ:	result = i1 == i2; break;
 				case BinaryOperator.NOTEQ:	result = i1 != i2; break;
 				case BinaryOperator.LT:		result = i1 < i2; break;
 				case BinaryOperator.GT:		result = i1 > i2; break;
 				case BinaryOperator.LTEQ:	result = i1 <= i2; break;
 				case BinaryOperator.GTEQ:	result = i1 >= i2; break;
 				default:					throw new Error("!");
 				}
 				binaryExpression.setValue(BoolObject.getBool(result));
 			}
 			break;
 		case BinaryOperator.AND:
 		case BinaryOperator.OR:
 		case BinaryOperator.LSHIFT:
 		case BinaryOperator.RSHIFT:
 		case BinaryOperator.URSHIFT:
 			{
 				int i1 = lVal.getIntValue();
 				int i2 = rVal.getIntValue();
 				int result;
 				switch(op){
 				case BinaryOperator.AND:	result = i1 & i2; break;
 				case BinaryOperator.OR:		result = i1 | i2; break;
 				case BinaryOperator.LSHIFT:	result = i1 << i2; break;
 				case BinaryOperator.RSHIFT:	result = i1 >> i2; break;
 				case BinaryOperator.URSHIFT:result = i1 >>> i2; break;
 				default:					throw new Error("!");
 				}
 				binaryExpression.setValue(new IntObject(result));
 				
 			}
 			break;
 		default:
 			throw new CompilationError(binaryExpression,"Unknown binary operator!");
 		}
 		
 		
 	}
 }

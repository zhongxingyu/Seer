 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.vm.data;
 
 import com.sc2mod.andromeda.environment.types.BasicType;
 import com.sc2mod.andromeda.environment.types.RuntimeType;
 import com.sc2mod.andromeda.environment.types.Type;
 import com.sc2mod.andromeda.syntaxNodes.Expression;
 import com.sc2mod.andromeda.syntaxNodes.Literal;
 import com.sc2mod.andromeda.syntaxNodes.LiteralExpression;
 import com.sc2mod.andromeda.syntaxNodes.LiteralType;
 
 public class IntObject extends DataObject {
 
 	private int val;
 	
 	public IntObject(int result) {
 		this.val = result;
 	}
 	
 	@Override
 	public String toString() {
 		return String.valueOf(val);
 	}
 
 	@Override
 	public Expression getExpression() {
 		return getLiteralExpr(LiteralType.INT);
 	}
 	
 	@Override
 	public int getIntValue() {
 		return val;
 	}
 	
 	@Override
	public float getFloatValue() {
		return val;
 	}
 	
 	@Override
 	public DataObject castTo(Type type) {
 		switch(type.getRuntimeType()){
 		case RuntimeType.INT:{
 			if(type.getBaseType() == BasicType.BYTE){
 				if(val<0||val>255){
 					return new IntObject(val & 0x000000FF);
 				}
 			}
 
 			return this;
 		}
 		case RuntimeType.FLOAT: return new FixedObject(val);
 		case RuntimeType.STRING: return new StringObject(String.valueOf(val));
 		case RuntimeType.TEXT: return new TextObject(String.valueOf(val));
 		}
		return super.castTo(type);	
 	}
 	
 	@Override
 	public Type getType() {
 		return BasicType.INT;
 	}
 }

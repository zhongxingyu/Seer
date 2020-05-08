 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 
 package org.caltoopia.codegen;
 
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.Assign;
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.ForEach;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.Guard;
 import org.caltoopia.ir.IfExpression;
 import org.caltoopia.ir.IfStatement;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.LiteralExpression;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Port;
 import org.caltoopia.ir.PortPeek;
 import org.caltoopia.ir.PortRead;
 import org.caltoopia.ir.PortWrite;
 import org.caltoopia.ir.ProcCall;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Scope;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeBool;
 import org.caltoopia.ir.TypeConstructor;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeExternal;
 import org.caltoopia.ir.TypeFloat;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeProc;
 import org.caltoopia.ir.TypeString;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.TypeUndef;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.types.TypeSystem;
 import org.eclipse.emf.ecore.EObject;
 import org.caltoopia.codegen.UtilIR;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 public class IR2CIR extends IR2IRBase {
 
 	private Map<String,VariableExternal> stdLib = new HashMap<String,VariableExternal>();
 	private Stack<Declaration> declStack = new Stack<Declaration>();
 	private Map<TypeConstructor,List<Declaration>> declMap = new HashMap<TypeConstructor,List<Declaration>>();
 	private Set<EObject> visited = new HashSet<EObject>();
 	private Set<Block> forEachBlock = new HashSet<Block>();
 	private Set<EObject> finishedAssigns = new HashSet<EObject>();
 	private Network topNetwork = null;
 	private Expression NullExpression = null;
 	private Map<String,VariableExternal> portMapIn = new HashMap<String,VariableExternal>();
 	private Map<String,VariableExternal> portMapOut = new HashMap<String,VariableExternal>();
 	public Map<Actor,Block> actorConstructor = new HashMap<Actor,Block>();
 	private TypeMapper mapper = null;
 	
 	public IR2CIR() {
 		declStack.clear();
 		declMap.clear();
 		makeStdLib();
 		visited.clear();
 		forEachBlock.clear();
 		finishedAssigns.clear();
 		actorConstructor.clear();
 		NullExpression = IrFactory.eINSTANCE.createExpression();
 		NullExpression.setType(TypeSystem.createTypeUndef());
 		NullExpression.setId(Util.getDefinitionId());
 	}
 
 	private void makeStdLib() {
 		stdLib.clear();
 		//Standard c heap handling
 		stdLib.put("malloc", UtilIR.createExternalFunctionDecl("malloc", (Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)), 
 				Arrays.asList("size"), 
 				Arrays.asList((Type)TypeSystem.createTypeUInt(32))
 				));
 		stdLib.put("free", UtilIR.createExternalFunctionDecl("free", (Type)TypeSystem.createTypeExternal("void",null), 
 				Arrays.asList("src"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)))
 				));
 		stdLib.put("memcpy", UtilIR.createExternalFunctionDecl("memcpy", (Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)), 
 				Arrays.asList("dst","src","size"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeUndef()),(Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)),(Type)TypeSystem.createTypeUInt(32))
 				));
 		stdLib.put("sizeof", UtilIR.createExternalFunctionDecl("sizeof", (Type)TypeSystem.createTypeUInt(32), 
 				Arrays.asList("type"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)))
 				));
 		//Generic last resort
 		stdLib.put("dummy", UtilIR.createExternalFunctionDecl("dummy", (Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)), 
 				Arrays.asList("len"), 
 				Arrays.asList((Type)TypeSystem.createTypeUInt(32))
 				));	
 		//Token FIFO handling
 		List<String> types = Arrays.asList("int32_t", "uint32_t", "bool_t", "double");
 		for(String type : types) {
 			stdLib.put("pinAvailIn_"+ type, UtilIR.createExternalFunctionDecl("pinAvailIn_"+ type, TypeSystem.createTypeExternal("unsigned int",null), 
 					Arrays.asList("p"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)))
 					));			
 			stdLib.put("pinAvailOut_"+ type, UtilIR.createExternalFunctionDecl("pinAvailOut_"+ type, TypeSystem.createTypeExternal("unsigned int",null), 
 					Arrays.asList("p"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalOutputPort",null)))
 					));
 			stdLib.put("pinWrite_"+ type, UtilIR.createExternalFunctionDecl("pinWrite_"+ type, TypeSystem.createTypeExternal("void",null), 
 					Arrays.asList("p", "token"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalOutputPort",null)),
 							(Type)TypeSystem.createTypeExternal(type,null))
 					));
 			stdLib.put("pinWriteRepeat_"+ type, UtilIR.createExternalFunctionDecl("pinWriteRepeat_"+ type, TypeSystem.createTypeExternal("void",null), 
 					Arrays.asList("p", "buf", "n"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalOutputPort",null)),
 							(Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal(type,null)),
 							(Type)TypeSystem.createTypeExternal("int",null))
 					));
 			stdLib.put("pinRead_"+ type, UtilIR.createExternalFunctionDecl("pinRead_"+ type, TypeSystem.createTypeExternal(type,null), 
 					Arrays.asList("p"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalInputPort",null)))
 					));	
 			stdLib.put("pinReadRepeat_"+ type, UtilIR.createExternalFunctionDecl("pinReadRepeat_"+ type, TypeSystem.createTypeExternal("void",null), 
 					Arrays.asList("p", "buf", "n"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalInputPort",null)),
 							(Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal(type,null)),
 							(Type)TypeSystem.createTypeExternal("int",null))
 					));
 			stdLib.put("pinPeekFront_"+ type, UtilIR.createExternalFunctionDecl("pinPeekFront_"+ type, TypeSystem.createTypeExternal(type,null), 
 					Arrays.asList("p"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)))
 					));			
 			stdLib.put("pinPeek_"+ type, UtilIR.createExternalFunctionDecl("pinPeek_"+ type, TypeSystem.createTypeExternal(type,null), 
 					Arrays.asList("p","offset"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)),
 							(Type)TypeSystem.createTypeExternal("int",null))
 					));	
 			stdLib.put("pinPeekRepeat_"+ type, UtilIR.createExternalFunctionDecl("pinPeekRepeat_"+ type, TypeSystem.createTypeExternal("void",null), 
 					Arrays.asList("p", "buf", "n", "offset"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)),
 							(Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal(type,null)),
 							(Type)TypeSystem.createTypeExternal("int",null),
 							(Type)TypeSystem.createTypeExternal("int",null))
 					));
 		}
 		stdLib.put("pinAvailIn_bytes", UtilIR.createExternalFunctionDecl("pinAvailIn_bytes", TypeSystem.createTypeExternal("unsigned int",null), 
 				Arrays.asList("p","bytes"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)),
 						(Type)TypeSystem.createTypeExternal("int",null))
 				));			
 		stdLib.put("pinAvailOut_bytes", UtilIR.createExternalFunctionDecl("pinAvailOut_bytes", TypeSystem.createTypeExternal("unsigned int",null), 
 				Arrays.asList("p","bytes"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalOutputPort",null)),
 						(Type)TypeSystem.createTypeExternal("int",null))
 				));
 		stdLib.put("pinWrite_bytes", UtilIR.createExternalFunctionDecl("pinWrite_bytes", TypeSystem.createTypeExternal("void",null), 
 				Arrays.asList("p", "buf","bytes"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalOutputPort",null)),
 						(Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null))),
 						(Type)TypeSystem.createTypeExternal("int",null))
 				));
 		stdLib.put("pinRead_bytes", UtilIR.createExternalFunctionDecl("pinRead_bytes", TypeSystem.createTypeExternal("void",null), 
 				Arrays.asList("p","buf", "bytes"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalInputPort",null)),
 						(Type)TypeSystem.createTypeList(UtilIR.lit(0),TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null))),
 						(Type)TypeSystem.createTypeExternal("int",null))
 				));			
 		stdLib.put("pinPeek_bytes", UtilIR.createExternalFunctionDecl("pinPeek_bytes", TypeSystem.createTypeExternal("void",null), 
 				Arrays.asList("p", "buf", "bytes","offset"), 
 				Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)),
 						(Type)TypeSystem.createTypeList(UtilIR.lit(0),TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null))),
 						(Type)TypeSystem.createTypeExternal("int",null),
 						(Type)TypeSystem.createTypeExternal("int",null))
 				));			
 	}
 	
 	private String portTypeString(Type type) {
 		if(type instanceof TypeInt)
 			return "int32_t";
 		else if(type instanceof TypeUint)
 			return "uint32_t";
 		else if(type instanceof TypeBool)
 			return "bool_t";
 		else if(type instanceof TypeFloat)
 			return "double";
 		else 
 			return "int32_t"; //TODO Good default???
 	}
 
 	private Declaration findFuncDecl(String name, Network top) {
 		for(Declaration d : declStack) {
 			if(((Variable) d).getType() instanceof TypeProc) {
 				if(d.getName().equals(name)) {
 					return (Declaration) d;
 				}
 			}
 		}
 		if(top != null) {
 			for(Declaration d : top.getDeclarations()) {
 				if(d instanceof Variable && (((Variable) d).getType() instanceof TypeProc || ((Variable) d).getType() instanceof TypeLambda)) {
 					if(d.getName().equals(name)) {
 						return (Declaration) d;
 					}
 				}
 			}
 		}
 		return UtilIR.createExternalFunctionDecl("dummy__" + name, (Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)), 
 				Arrays.asList("len"), 
 				Arrays.asList((Type)TypeSystem.createTypeUInt(32))
 				);
 	}
 	
 	public Expression createDeepSizeof(Scope body, Type type) {
 		return createDeepSizeofInner(null,body, type);
 	}
 
 	private Expression createDeepSizeofInner(Expression expr, Scope body, Type type) {
 		FunctionCall sizeOf = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofTypeE(body,type)));
 		if(expr==null) {
 			expr = sizeOf;
 		} else {
 			expr = UtilIR.createExpression(expr, "+", sizeOf);			
 		}
 		if(UtilIR.isRecord(type)) {
 			TypeRecord struct = (TypeRecord) UtilIR.getTypeDeclaration(type).getType();
 			//each member
 			for(int i=0;i<struct.getMembers().size();i++) {
 				Variable def=struct.getMembers().get(i);
 				Type memberType = UtilIR.getType(def.getType());
 				if (memberType instanceof TypeRecord) {
 					Expression sizeOfMember=createDeepSizeofInner(null,body,def.getType());
 					expr = UtilIR.createExpression(expr, "+", sizeOfMember);
 				} else
 				if (memberType instanceof TypeList) {
 					Type listType = ((TypeList)memberType).getType();
 					Type memberTypeE = UtilIR.getType(listType);
 					if(memberTypeE instanceof TypeRecord) {
 						Expression sizeOfMember=createDeepSizeofInner(null,body,listType);
 						Expression listSize = UtilIR.createExpression(((TypeList) memberType).getSize(), "*", sizeOfMember);
 						expr = UtilIR.createExpression(expr, "+", listSize);
 					} else if(memberTypeE instanceof TypeList) {
 						//Multi-dim list
 						Expression sizeOfMember=createDeepSizeofInner(null,body,listType);
 						Expression listSize = UtilIR.createExpression(((TypeList) memberType).getSize(), "*", sizeOfMember);
 						expr = UtilIR.createExpression(expr, "+", listSize);					
 					} else {
 						Expression sizeOfMember = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofType(body,memberTypeE)));
 						Expression listSize = UtilIR.createExpression(((TypeList) memberType).getSize(), "*", sizeOfMember);
 						expr = UtilIR.createExpression(expr, "+", listSize);
 					}
 				}
 			}
 		} else if(UtilIR.isList(type)) {
 			Type listType = ((TypeList)type).getType();
 			if(listType instanceof TypeRecord) {
 				Expression sizeOfMember=createDeepSizeofInner(null,body,listType);
 				Expression listSize = UtilIR.createExpression(((TypeList) type).getSize(), "*", sizeOfMember);
 				expr = UtilIR.createExpression(expr, "+", listSize);
 			} else if(listType instanceof TypeList) {
 				//Multi-dim list
 				Expression sizeOfMember=createDeepSizeofInner(null,body,listType);
 				Expression listSize = UtilIR.createExpression(((TypeList) type).getSize(), "*", sizeOfMember);
 				expr = UtilIR.createExpression(expr, "+", listSize);					
 			} else {
 				Expression sizeOfMember = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofType(body,listType)));
 				Expression listSize = UtilIR.createExpression(((TypeList) type).getSize(), "*", sizeOfMember);
 				expr = UtilIR.createExpression(expr, "+", listSize);
 			}
 		}
 		return expr;
 	}
 
 	/* This is only for use with the sizeof(type) we create a variable def 
 	 * with empty name. The c-printer will print the name of the type instead. 
 	 * We fall back to sizeof(var) if we don't know what to do.
 	 */
 	private Expression fakeSizeofType(Scope scope, Variable var){
 		Type type=var.getType() instanceof TypeList? ((TypeList)var.getType()).getType() : var.getType();
 		Type typePrint=type;
 		type = UtilIR.getType(type);
 		if(type instanceof TypeRecord || type instanceof TypeInt || type instanceof TypeBool || 
 			type instanceof TypeFloat || type instanceof TypeUint || type instanceof TypeString ||
 			type instanceof TypeUndef || type instanceof TypeExternal) {
 			return UtilIR.createExpressionByType(scope, typePrint);
 		} else {
 			return UtilIR.createExpression(scope,var);
 		}
 	}
 	private Expression fakeSizeofType(Scope scope, Type typeIn){
 		Type type=typeIn instanceof TypeList? ((TypeList)typeIn).getType() : typeIn;
 		Type typePrint=type;
 		type = UtilIR.getType(type);
 		if(type instanceof TypeRecord || type instanceof TypeInt || type instanceof TypeBool || 
 			type instanceof TypeFloat || type instanceof TypeUint || type instanceof TypeString ||
 			type instanceof TypeUndef || type instanceof TypeExternal) {
 			return UtilIR.createExpressionByType(scope, typePrint);			
 		} else {
 			return UtilIR.createExpression(scope,UtilIR.createVarDef(null, "", TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null))));			
 		}
 	}
 	private Expression fakeSizeofTypeE(Scope scope, Type type){
 		Type typePrint=type;
 		type = UtilIR.getType(type);
 		if(type instanceof TypeRecord || type instanceof TypeInt || type instanceof TypeBool || 
 			type instanceof TypeFloat || type instanceof TypeUint || type instanceof TypeString ||
 			type instanceof TypeUndef || type instanceof TypeExternal || type instanceof TypeList) {
 			return UtilIR.createExpressionByType(scope, typePrint);			
 		} else {
 			return UtilIR.createExpressionByType(scope,TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)));			
 		}
 	}
 	
 	private Expression fakeNull(){
 		return NullExpression;
 	}
 
 
 	private Assign createMalloc(Scope body, Variable target, Variable member, Expression size) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofType(body,member)));
 		if(size.getType()==null)
 			size.setType(TypeSystem.createTypeInt());
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		FunctionCall malloc = UtilIR.createFunctionCall(body, stdLib.get("malloc"), Arrays.asList(byteSize));
 		return UtilIR.createStructAssign(body,target,member,malloc);
 	}
 	//The index is on the member!!!
 	private Assign createMalloc(int pos, Scope body, Variable target, Variable member, List<Expression> index, Expression size, Type type) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofTypeE(body,type)));
 		if(size.getType()==null)
 			size.setType(TypeSystem.createTypeInt());
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		FunctionCall malloc = UtilIR.createFunctionCall(body, stdLib.get("malloc"), Arrays.asList(byteSize));
 		VariableReference var = UtilIR.createVarRef(target,null,member,index);
 		if(pos==-2) {
 			return UtilIR.createAssignN(body,var, malloc);
 		} else {
 			Assign assign = UtilIR.createAssign(pos, body, var, malloc);
 			finishedAssigns.add(assign);
 			return assign;
 		}
 	}
 	private Assign createMalloc(int pos, Scope body, Variable target, List<Expression> index, Expression size, Type type) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofTypeE(body,type)));
 		if(size.getType()==null)
 			size.setType(TypeSystem.createTypeInt());
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		FunctionCall malloc = UtilIR.createFunctionCall(body, stdLib.get("malloc"), Arrays.asList(byteSize));
 		VariableReference var = UtilIR.createVarRef(target,index);
 		if(pos==-2) {
 			return UtilIR.createAssignN(body,var, malloc);
 		} else {
 			Assign assign = UtilIR.createAssign(pos, body, var, malloc);
 			finishedAssigns.add(assign);
 			return assign;
 		}
 	}
 	
 	private Assign createMalloc(int pos, Scope body, VariableReference var, Expression size, Type type) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofTypeE(body,type)));
 		if(size.getType()==null)
 			size.setType(TypeSystem.createTypeInt());
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		FunctionCall malloc = UtilIR.createFunctionCall(body, stdLib.get("malloc"), Arrays.asList(byteSize));
 		if(pos==-2) {
 			return UtilIR.createAssignN(body,var, malloc);
 		} else {
 			Assign assign = UtilIR.createAssign(pos, body, var, malloc);
 			finishedAssigns.add(assign);
 			return assign;
 		}
 	}
 
 	private Assign createMalloc(int pos, Scope body, Variable target, Expression size, boolean sizeOfElem) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(sizeOfElem?fakeSizeofType(body,target):fakeSizeofTypeE(body,target.getType())));
 		if(size.getType()==null)
 			size.setType(TypeSystem.createTypeInt());
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		FunctionCall malloc = UtilIR.createFunctionCall(body, stdLib.get("malloc"), Arrays.asList(byteSize));
 		if(pos==-2) {
 			return UtilIR.createAssignN(body,target,malloc);
 		} else {
 			Assign assign = UtilIR.createAssign(pos, body,target,malloc);
 			finishedAssigns.add(assign);
 			return assign;
 		}
 	}
 
 	private Assign createMalloc(int pos, Scope body, Variable target, Variable size, boolean sizeOfElem) {
 		return createMalloc(pos, body,target,UtilIR.createExpression(body,size),sizeOfElem);
 	}
 	private Assign createMalloc(Scope body, Variable target, Variable size, boolean sizeOfElem) {
 		return createMalloc(-1, body,target,size,sizeOfElem);
 	}
 	private Assign createMalloc(Scope body, Variable target, Expression size, boolean sizeOfElem) {
 		return createMalloc(-1, body,target,size,sizeOfElem);
 	}
 
 	private ProcCall createFree(int pos, Scope body, VariableExpression var) {
 		if(pos==-2) {
 			return UtilIR.createProcCallN(body, stdLib.get("free"), Arrays.asList((Expression)var),null);
 		} else if(pos>=0) {
 			return UtilIR.createProcCall(pos,body, stdLib.get("free"), Arrays.asList((Expression)var),null);
 		} else {
 			return UtilIR.createProcCall(body, stdLib.get("free"), Arrays.asList((Expression)var),null);
 		}
 	}
 
 	private ProcCall createFree(int pos, Scope body, Variable target, Variable member) {
 		if(pos==-2) {
 			return UtilIR.createProcCallN(body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target, member)),null);
 		} else {
 			return UtilIR.createProcCall(body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target, member)),null);
 		}
 	}
 	private ProcCall createFree(int pos, Scope body, Variable target) {
 		if(pos==-2) {
 			return UtilIR.createProcCallN(body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target)),null);
 		} else if(pos>=0) {
 			return UtilIR.createProcCall(pos,body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target)),null);
 		} else {
 			return UtilIR.createProcCall(body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target)),null);
 		}
 	}
 	
 	private ProcCall createFree(int pos, Scope body, Variable target, List<Expression> index) {
 		if(pos==-2) {
 			return UtilIR.createProcCallN(body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target,index,null,null)),null);
 		} else if(pos>=0) {
 			return UtilIR.createProcCall(pos,body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target,index,null,null)),null);
 		} else {
 			return UtilIR.createProcCall(body, stdLib.get("free"), Arrays.asList(UtilIR.createExpression(body,target,index,null,null)),null);
 		}
 	}
 
 	private ProcCall createMemcpy(int pos, Scope body, VariableReference dst, VariableExpression src, Expression size, Type type) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofTypeE(body,type)));
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		VariableExpression dstExpr = (VariableExpression) UtilIR.createExpression(body, dst.getDeclaration(), dst.getIndex(), dst.getMember());
 		if(pos==-2) {
 			return UtilIR.createProcCallN(body, stdLib.get("memcpy"), Arrays.asList(dstExpr,src,byteSize),null);
 		} else if(pos>=0) {
 			return UtilIR.createProcCall(pos, body, stdLib.get("memcpy"), Arrays.asList(dstExpr,src,byteSize),null);
 		} else {
 			return UtilIR.createProcCall(body, stdLib.get("memcpy"), Arrays.asList(dstExpr,src,byteSize),null);
 		}
 	}
 
 	private ProcCall createMemcpy(Scope body, Variable dst, Variable dstMember, Variable src, Variable srcMember, Expression size) {
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofType(body,dstMember)));
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		return UtilIR.createProcCall(body, stdLib.get("memcpy"), 
 				Arrays.asList(UtilIR.createExpression(body,dst, dstMember),UtilIR.createExpression(body,src, srcMember),byteSize),null);
 	}
 	private ProcCall createMemcpy(Scope body, Variable dst, Variable src, Expression size) {
 		//Need to fake a variable with the name of the type
 		FunctionCall size_of = UtilIR.createFunctionCall(body, stdLib.get("sizeof"), Arrays.asList(fakeSizeofType(body,dst)));
 		Expression byteSize = UtilIR.createExpression(size_of,"*",size);
 		return UtilIR.createProcCall(body, stdLib.get("memcpy"), 
 				Arrays.asList(UtilIR.createExpression(body,dst),UtilIR.createExpression(body,src),byteSize),null);
 	}
 	private ProcCall createMemcpy(Scope body, Variable dst, Variable src, Variable size) {
 		return createMemcpy(body,dst,src,UtilIR.createExpression(body,size));
 	}
 	
 	//When calling the network should be flattened and only one top network exist (i.e. the outer namespace should only have one abstract actor the top network)
 	@Override
 	public EObject caseNetwork(Network top) {
 //		mapper = new TypeMapper(top);
 		//mapper.print();
 		
 		topNetwork = top;
 		Network net = (Network) super.caseNetwork(top);
 		if(!declStack.isEmpty()) {
 			List<Declaration> decl = net.getDeclarations();
 			for(int i = 0; i<decl.size(); i++) {
 				Declaration d = decl.get(i);
 				if(d instanceof TypeDeclaration) {
 					decl.addAll(i+1, declMap.get(((TypeDeclaration) d).getConstructor()));
 					i+=declMap.get(((TypeDeclaration) d).getConstructor()).size();
 					decl.add(i+1,((TypeDeclaration) d).getConstructor());
 					i++;
 				}
 			}
 			declMap.clear();
 			declStack.clear();
 		}
 		return net;
 	}
 
 	@Override
 	public EObject caseActor(Actor actor) {
 		if(!visitedActor.contains(actor)) {
 			hierarchy.push(actor);
 			//Actors don't have constructor anymore, but we need one to do all the heap etc management in the CIR
 			//We create one and keep it on the side
 			Block block = IrFactory.eINSTANCE.createBlock();
 			actorConstructor.put(actor, block);
 			hierarchy.push(block);
 			List<Declaration> decl = new ArrayList<Declaration>();
 			decl.clear();
 			for(Declaration d : actor.getDeclarations()) {			
 				//Move initValues to assigments in the constructor
 				if(UtilIR.isNormalInit(d)) {
 					Assign assign = UtilIR.createAssign(block,(Variable)d, ((Variable)d).getInitValue());
 					((Variable)d).setInitValue(null);
 					Statement stmt = (Statement) caseAssign(assign);
 					if(!stmt.equals(assign)) {
 						int i = block.getStatements().indexOf(assign);
 						block.getStatements().set(i, stmt);
 					}
 				}
 				//Pick the declarations that need heap management
 				if(UtilIR.isNormalDef(d) || UtilIR.isNormalInit(d)) {
 					decl.add(d);
 				}
 			}
 			insertHeapManagment(block,false,decl,block.getStatements());
 			hierarchy.pop();
 			hierarchy.pop();
 		}
 		return super.caseActor(actor);
 	}
 	
 	private enum ft {
 		alloc, free, copy, pinwrite, pinread, pinpeek
 	}
 
 	private Statement createCallStatement(ft func, Scope scope, int pos, VariableReference varRef, VariableExpression varExpr, Expression size, Type type, VariableExternal port, int offset) {
 		switch (func) {
 		case alloc :
 			//Uses scope, pos, varRef, size, type
 			if(UtilIR.isRecord(type)) {
 				Declaration allocDef = findFuncDecl("alloc__"+UtilIR.getTypeDeclaration(type).getName(),topNetwork);
 				return UtilIR.createProcCall(pos,scope, allocDef, 
 						Arrays.asList(fakeNull(),size),
 						Arrays.asList(varRef));				
 			} else {
 				return createMalloc(pos,scope,varRef,size,type);
 			}
 		case free :
 			//Uses scope, pos, varRef, type
 			if(UtilIR.isRecord(type)) {
 				Declaration freeDef = findFuncDecl("free__"+UtilIR.getTypeDeclaration(type).getName(), topNetwork);
 				return UtilIR.createProcCall(pos,scope, freeDef, 
 						Arrays.asList(UtilIR.createExpression(scope, varRef), size), null);
 			} else {
 				return createFree(pos,scope,(VariableExpression)UtilIR.createExpression(scope, varRef));				
 			}
 		case copy :
 			//Uses scope, pos, varRef (as dest), varExpr (as src), type
 			if(UtilIR.isRecord(type)) {
 				Declaration copyDef = findFuncDecl("copy__"+UtilIR.getTypeDeclaration(type).getName(),topNetwork);
 				return UtilIR.createProcCall(pos, scope, copyDef, 
 						Arrays.asList(varExpr, size),
 						Arrays.asList(varRef));
 			} else {
 				return createMemcpy(pos, scope, varRef, varExpr, size, type);
 			}
 		case pinwrite :
 			//Uses scope, pos, varRef (as next), varExpr (as token), type, port
 			if(UtilIR.isRecord(type)) {
 				Declaration writeDef = findFuncDecl("pinWriteRepeat__"+UtilIR.getTypeDeclaration(type).getName(),topNetwork);
 				return UtilIR.createProcCall(pos, scope, writeDef, 
 						Arrays.asList(UtilIR.createExpression(scope,port), varExpr,	size),
 						Arrays.asList(varRef));				
 			} else if(UtilIR.isList(type)) {
 				return UtilIR.createProcCall(pos,scope, stdLib.get("pinWrite_bytes"), 
 						Arrays.asList(UtilIR.createExpression(scope,port),
 								UtilIR.createExpression(scope,varRef),
 								UtilIR.createExpression(UtilIR.createFunctionCall(scope, stdLib.get("sizeof"), 
 										Arrays.asList(fakeSizeofType(scope,type))), "*", size)), null);
 			} else {
 				return UtilIR.createProcCall(pos, scope, stdLib.get("pinWriteRepeat_"+portTypeString(type)), 
 						Arrays.asList(UtilIR.createExpression(scope,port),varExpr,size),null);
 			}
 		case pinread :
 			//Uses scope, pos, varRef (as token), type, port
 			if(UtilIR.isRecord(type)) {
 				return 	UtilIR.createProcCall(pos,scope, stdLib.get("pinRead_bytes"), 
 						Arrays.asList(UtilIR.createExpression(scope,port), UtilIR.createExpression(scope,varRef), size),null);
 			} else if(UtilIR.isList(type)) {
 				return UtilIR.createProcCall(pos,scope, stdLib.get("pinReadRepeat_"+portTypeString(((TypeList)type).getType())), 
 						Arrays.asList(UtilIR.createExpression(scope,port), UtilIR.createExpression(scope,varRef), size), null);
 			} else {
 				FunctionCall pinread = UtilIR.createFunctionCall(scope, stdLib.get("pinRead_"+portTypeString(type)), 
 						Arrays.asList(UtilIR.createExpression(scope,port)));
 				Assign assign = UtilIR.createAssign(pos,scope,varRef,pinread);
 				finishedAssigns.add(assign);
 				return assign;
 			}
 		case pinpeek :
 			//Uses scope, pos, varRef (as token), type, port, offset
 			if(UtilIR.isRecord(type)) {
 				return UtilIR.createProcCall(pos, scope, stdLib.get("pinPeek_bytes"), 
 						Arrays.asList(UtilIR.createExpression(scope,port),
 								UtilIR.createExpression(scope,varRef), size, UtilIR.lit(offset)), null);	
 			} else if(UtilIR.isList(type)) {
 				//FIXME pinPeekRepeat does not exist
 				return UtilIR.createProcCall(pos,scope, stdLib.get("pinPeekRepeat_"+portTypeString(((TypeList)type).getType())), 
 						Arrays.asList(UtilIR.createExpression(scope,port),
 								UtilIR.createExpression(scope,varRef),
 								UtilIR.lit(offset),	size),null);
 			} else {
 				FunctionCall pinpeek = UtilIR.createFunctionCall(scope, stdLib.get("pinPeek_"+portTypeString(type)), 
 						Arrays.asList(UtilIR.createExpression(scope,port),UtilIR.lit(offset)));
 				Assign assign = UtilIR.createAssign(pos,scope,varRef,pinpeek);
 				finishedAssigns.add(assign);
 				return assign;
 			}
 		default :
 			return null;
 		}
 	}
 	
 	private Block createMultiDimArray(ft func, Scope outer, Type typeParent, Variable var, Variable member, Variable src, Variable srcMember) {
 		Block temp = IrFactory.eINSTANCE.createBlock();
 		Block innerBlock = UtilIR.createBlock(outer);
 		Block returnBlock = innerBlock;
 		Type listType = ((TypeList)typeParent).getType();
 		Type elemType = listType;
 		while(UtilIR.isList(elemType)) {
 			elemType = ((TypeList)elemType).getType();
 		}
 		List<Expression> index = new ArrayList<Expression>();
 		index.clear();
 		int j=0;
 		switch (func) {
 		case alloc :
 			createCallStatement(func, innerBlock, 0, UtilIR.createVarRef(var, null, member, index), (VariableExpression) UtilIR.createExpression(innerBlock, src, null, srcMember, index), ((TypeList)typeParent).getSize(), TypeSystem.createTypeList(((TypeList)typeParent).getSize(), elemType), null, 0);
 			break;
 		default :
 		}
 		while(UtilIR.isList(listType)) {
 			Block loop = UtilIR.createBlock(innerBlock);
 			Variable ii = UtilIR.createSimpleLoop(-1,temp, loop, member.getName()+"_loop_count"+j, 0, ((TypeList)typeParent).getSize());
 			if(innerBlock instanceof Block)
 				((Block)innerBlock).getStatements().addAll(temp.getStatements());
 			else if(innerBlock instanceof Action)
 				((Action)innerBlock).getStatements().addAll(temp.getStatements());
 			temp.getStatements().clear();
 			index.add(UtilIR.createExpression(innerBlock, ii));
 			typeParent = listType;
 			listType = ((TypeList)listType).getType();
 			switch (func) {
 			case alloc :
 			case free :
 				if(listType instanceof TypeList) {
 					createCallStatement(func, loop, 0, UtilIR.createVarRef(var, null, member, index), (VariableExpression) UtilIR.createExpression(innerBlock, src, null, srcMember, index), ((TypeList)listType).getSize(), TypeSystem.createTypeList(((TypeList)typeParent).getSize(), elemType), null, 0);
 				} else if(UtilIR.isRecord(listType)) {
 					createCallStatement(func, loop, 0, UtilIR.createVarRef(var, null, member, index), (VariableExpression) UtilIR.createExpression(innerBlock, src, null, srcMember, index), ((TypeList)typeParent).getSize(), listType, null, 0);
 				} else {
 					//Elementary type
 					createCallStatement(func, loop, 0, UtilIR.createVarRef(var, null, member, index), (VariableExpression) UtilIR.createExpression(innerBlock, src, null, srcMember, index), ((TypeList)typeParent).getSize(), elemType, null, 0);
 				}
 				break;
 			case copy :
 				if(UtilIR.isRecord(listType)) {
 					createCallStatement(func, loop, 0, UtilIR.createVarRef(var, null, member, index), (VariableExpression) UtilIR.createExpression(innerBlock, src, null, srcMember, index), ((TypeList)typeParent).getSize(), listType, null, 0);
 				} else {
 					//Elementary type
 					createCallStatement(func, loop, 0, UtilIR.createVarRef(var, null, member, index), (VariableExpression) UtilIR.createExpression(innerBlock, src, null, srcMember, index), ((TypeList)typeParent).getSize(), elemType, null, 0);
 				}
 				break;
 			default :
 			}
 			j++;
 			innerBlock.getDeclarations().addAll(temp.getDeclarations());
 			temp.getDeclarations().clear();
 			innerBlock = loop;
 		}
 		switch (func) {
 		case free :
 			createCallStatement(func, returnBlock, -1, UtilIR.createVarRef(var, null, member, null), null, null, TypeSystem.createTypeList(UtilIR.lit(0), elemType), null, 0);
 			break;
 		default :
 		}
 		return returnBlock;
 	}
 
 	@Override
 	public EObject caseTypeConstructor(TypeConstructor tc) {
 		Scope scope = getThisAbstractActor();
 		List<Declaration> funcs = new ArrayList<Declaration>();
 		if(scope == null) 
 			scope = topNetwork; 
 		
 		{//Separate context to allow copy-paste
 			//Create an Allocator function
 			////Function Signature
 			Type type = UtilIR.createTypeUser(tc.getTypedef());
 			Variable fun = UtilIR.createProcDefSignature(scope.getOuter(),scope,"alloc__" + tc.getName(), 
 					"var", TypeSystem.createTypeList(UtilIR.lit(0), type),
 					Arrays.asList("s","len"), 
 					Arrays.asList((Type) type,(Type) TypeSystem.createTypeInt())
 					);
 			UtilIR.tag(fun,"CIR_func",true);
 			////Function body
 			Block body = UtilIR.createBlock(scope);
 			Block thenBlock = UtilIR.createBlock(body);
 			Block elseBlock = UtilIR.createBlock(body);
 			Variable var = ((ProcExpression)fun.getInitValue()).getOutputs().get(0);
 			Variable p = UtilIR.createVarDef(body, "p", type);
 			//s=malloc(sizeof*len);
 			createMalloc(thenBlock,((ProcExpression)fun.getInitValue()).getParameters().get(0),((ProcExpression)fun.getInitValue()).getParameters().get(1),true);
 			//if(s==null)
 			UtilIR.createIf(body,
 					UtilIR.createExpression(UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(0)), "==", fakeNull()), 
 					thenBlock, elseBlock);
 			//var=s
 			finishedAssigns.add(UtilIR.createAssign(body, var, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(0))));
 			TypeRecord struct = (TypeRecord) tc.getTypedef().getType();
 			Block loop = UtilIR.createBlock(body);
 			//each member
 			for(int i=0;i<struct.getMembers().size();i++) {
 				Variable def=struct.getMembers().get(i);
 				Type memberType = UtilIR.getType(def.getType());
 				if (memberType instanceof TypeRecord) {
 					//The sub struct alloc function should exist
 					Declaration subAllocDef = findFuncDecl("alloc__"+((TypeUser) def.getType()).getDeclaration().getName(),topNetwork);
 					UtilIR.createProcCall(loop, subAllocDef, 
 							Arrays.asList(fakeNull(),(Expression)UtilIR.lit(1)),
 							Arrays.asList(UtilIR.createVarRef(p,def)));
 				} else
 				if (memberType instanceof TypeList) {
 					Type listType = ((TypeList)memberType).getType();
 					if(UtilIR.isRecord(listType)) {
 						//The sub struct alloc function should exist
 						Declaration subAllocDef = findFuncDecl("alloc__"+((TypeDeclaration) ((TypeUser) listType).getDeclaration()).getName(),topNetwork);
 						UtilIR.createProcCall(loop, subAllocDef, 
 								Arrays.asList(fakeNull(),((TypeList)def.getType()).getSize()),
 								Arrays.asList(UtilIR.createVarRef(p,def)));
 					} else if(UtilIR.isList(listType)) {
 						//Multi-dimensional array
 						loop.getStatements().add(createMultiDimArray(ft.alloc,loop, memberType, p, def,null,null));
 					} else {
 						//Elementary type so do a malloc
 						createMalloc(loop,p,def,((TypeList)def.getType()).getSize());
 					}
 				}
 			}
 			if(!loop.getStatements().isEmpty()) {
 				//p=s
 				finishedAssigns.add(UtilIR.createAssign(body, p, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(0))));
 				Assign assign = UtilIR.createAssign(loop,p,
 						UtilIR.createExpression(UtilIR.createExpression(loop,p), "+", UtilIR.lit(1)));
 				finishedAssigns.add(assign);
 				loop.getStatements().add(assign);
 				UtilIR.createSimpleLoop(body, loop, "i", 0, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(1)));
 			} else {
 				//remove p since unused
 				body.getDeclarations().remove(body.getDeclarations().size()-1); 
 			}
 			((ProcExpression) fun.getInitValue()).setBody(body);
 			declStack.add(fun);
 			funcs.add(fun);
 		}
 		{//Separate context to allow copy-paste
 			//Create an Free function
 			////Function Signature
 			Type type = UtilIR.createTypeUser(tc.getTypedef());
 			Variable fun = UtilIR.createProcDefSignature(scope.getOuter(),scope,"free__" + tc.getName(), "", TypeSystem.createTypeUndef(), 
 					Arrays.asList("s","len"), 
 					Arrays.asList((Type) type,(Type)TypeSystem.createTypeInt())
 					);
 			UtilIR.tag(fun,"CIR_func",true);
 			////Function body
 			Block body = UtilIR.createBlock(scope);
 			Variable p = UtilIR.createVarDef(body, "p", type);
 			TypeRecord struct = (TypeRecord) tc.getTypedef().getType();
 			Block loop = UtilIR.createBlock(body);
 			for(int i=0;i<struct.getMembers().size();i++) {
 				Variable def=struct.getMembers().get(i);
 				Type memberType = (def.getType() instanceof TypeUser) ? ((TypeDeclaration) ((TypeUser) def.getType()).getDeclaration()).getType() : def.getType();
 				if (memberType instanceof TypeRecord) {
 					//The sub struct free function should exist
 					Declaration subFreeDef = findFuncDecl("free__"+((TypeUser) def.getType()).getDeclaration().getName(),topNetwork);
 					UtilIR.createProcCall(loop, subFreeDef, 
 							Arrays.asList(UtilIR.createExpression(loop,p,def),(Expression)UtilIR.lit(1)),null);
 				} else
 				if (memberType instanceof TypeList) {
 					Type listType = ((TypeList)memberType).getType();
 					if(UtilIR.isRecord(listType)) {
 						//The sub struct free function should exist
 						Declaration subFreeDef = findFuncDecl("free__"+((TypeDeclaration) ((TypeUser) listType).getDeclaration()).getName(),topNetwork);
 						UtilIR.createProcCall(loop, subFreeDef, 
 								Arrays.asList(UtilIR.createExpression(loop,p,def),((TypeList)def.getType()).getSize()),null);
 					} else if(UtilIR.isList(listType)) {
 						//Multi-dimensional array
 						loop.getStatements().add(createMultiDimArray(ft.free,loop, memberType, p, def,null,null));
 					} else {
 						//Elementary type so do a free
 						createFree(-1,loop,p,def);
 					}
 				}
 			}
 			if(!loop.getStatements().isEmpty()) {
 				finishedAssigns.add(UtilIR.createAssign(body, p, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(0))));
 				Assign assign = UtilIR.createAssign(loop,p,
 						UtilIR.createExpression(UtilIR.createExpression(loop,p), "+", UtilIR.lit(1)));
 				finishedAssigns.add(assign);
 				loop.getStatements().add(assign);
 				UtilIR.createSimpleLoop(body, loop, "i", 0, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(1)));
 			} else {
 				//remove p since unused
 				body.getDeclarations().remove(body.getDeclarations().size()-1); 
 			}
 			createFree(-1,body,((ProcExpression) fun.getInitValue()).getParameters().get(0));			
 			((ProcExpression) fun.getInitValue()).setBody(body);
 			declStack.add(fun);
 			funcs.add(fun);
 		}
 		{//Separate context to allow copy-paste
 			//Create an Copy function
 			////Function Signature
 			Type type = UtilIR.createTypeUser(tc.getTypedef());
 			Variable fun = UtilIR.createProcDefSignature(scope.getOuter(),scope,"copy__" + tc.getName(), "dst", type, 
 					Arrays.asList("src","len"), 
 					Arrays.asList((Type) type,(Type)TypeSystem.createTypeInt())
 					);
 			UtilIR.tag(fun,"CIR_func",true);
 			////Function body
 			Block body = UtilIR.createBlock(scope);
 			Variable psrc = UtilIR.createVarDef(body, "psrc", type);
 			Variable pdst = UtilIR.createVarDef(body, "pdst", type);
 			TypeRecord struct = (TypeRecord) tc.getTypedef().getType();
 			Block loop = UtilIR.createBlock(body);
 			for(int i=0;i<struct.getMembers().size();i++) {
 				Variable def=struct.getMembers().get(i);
 				Type memberType = (def.getType() instanceof TypeUser) ? ((TypeDeclaration) ((TypeUser) def.getType()).getDeclaration()).getType() : def.getType();
 				if (memberType instanceof TypeRecord) {
 					//The sub struct copy function should exist
 					Declaration subCopyDef = findFuncDecl("copy__"+((TypeUser) def.getType()).getDeclaration().getName(),topNetwork);
 					UtilIR.createProcCall(loop, subCopyDef, 
 							Arrays.asList(UtilIR.createExpression(loop,psrc,def), (Expression)UtilIR.lit(1)),
 							Arrays.asList(UtilIR.createVarRef(pdst,def)));
 				} else if (memberType instanceof TypeList) {
 						Type listType = ((TypeList)memberType).getType();
 						if(UtilIR.isRecord(listType)) {
 							//The sub struct copy function should exist
 							Declaration subCopyDef = findFuncDecl("copy__"+((TypeDeclaration) ((TypeUser) listType).getDeclaration()).getName(),topNetwork);
 							UtilIR.createProcCall(loop, subCopyDef, 
 									Arrays.asList(UtilIR.createExpression(loop,psrc,def), (Expression)((TypeList)def.getType()).getSize()),
 									Arrays.asList(UtilIR.createVarRef(pdst,def)));
 						} else if(UtilIR.isList(listType)) {
 							//Multi-dimensional array
 							loop.getStatements().add(createMultiDimArray(ft.copy,loop, memberType, pdst, def, psrc, def));
 						} else {
 							//Elementary type so do a memcpy
 							createMemcpy(loop,pdst,def,psrc,def,((TypeList)def.getType()).getSize());
 						}
 					} else {
 						//Builtin type just do assigment
 						finishedAssigns.add(UtilIR.createStructAssign(loop, pdst, def, psrc, def));							
 					}
 
 			}
 			if(!loop.getStatements().isEmpty()) {
 				finishedAssigns.add(UtilIR.createAssign(body, psrc, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getParameters().get(0))));
 				finishedAssigns.add(UtilIR.createAssign(body, pdst, UtilIR.createExpression(body,((ProcExpression)fun.getInitValue()).getOutputs().get(0))));
 				Assign assign = UtilIR.createAssign(loop,psrc,
 						UtilIR.createExpression(UtilIR.createExpression(loop,psrc), "+", UtilIR.lit(1)));
 				finishedAssigns.add(assign);
 				loop.getStatements().add(assign);
 				assign = UtilIR.createAssign(loop,pdst,
 						UtilIR.createExpression(UtilIR.createExpression(loop,pdst), "+", UtilIR.lit(1)));
 				finishedAssigns.add(assign);
 				loop.getStatements().add(assign);
 				UtilIR.createSimpleLoop(body, loop, "i", 0, UtilIR.createExpression(loop,((ProcExpression)fun.getInitValue()).getParameters().get(1)));
 			}
 			((ProcExpression) fun.getInitValue()).setBody(body);
 			declStack.add(fun);
 			funcs.add(fun);
 		}
 		{//Separate context to allow copy-paste
 			//Create an port operation functions
 			////Function Signature
 			Type type = UtilIR.createTypeUser(tc.getTypedef());
 			Variable funAvailIn = UtilIR.createFuncDefSignature(scope.getOuter(),scope,"pinAvailIn_" + tc.getName(), 
 					TypeSystem.createTypeInt(),
 					Arrays.asList("port"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalInputPort",null)))
 					);
 			Variable funAvailOut = UtilIR.createFuncDefSignature(scope.getOuter(),scope,"pinAvailOut_" + tc.getName(), 
 					TypeSystem.createTypeInt(),
 					Arrays.asList("port"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("const LocalOutputPort",null)))
 					);
 			//void pinWriteRepeat__T(const LocalOutputPort port, T1* tokens, int nbr, T1** pTokens )
 			Variable funWriteRepeat = UtilIR.createProcDefSignature(scope.getOuter(),scope,"pinWriteRepeat__" + tc.getName(), 
 					"pTokens", TypeSystem.createTypeList(UtilIR.lit(0), type), //C-code will have T** pTokens
 					Arrays.asList("port","tokens","nbr"), 
 					Arrays.asList((Type)TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("LocalOutputPort",null)),
 							type,TypeSystem.createTypeInt())
 					);
 			UtilIR.tag(funAvailIn,"CIR_func",true);
 			UtilIR.tag(funAvailOut,"CIR_func",true);
 			UtilIR.tag(funWriteRepeat,"CIR_func",true);
 
 			//Available tokens on In/Out-port functions
 			FunctionCall avail = UtilIR.createFunctionCall(scope, stdLib.get("pinAvailIn_bytes"), 
 					Arrays.asList( UtilIR.createExpression(scope, ((LambdaExpression)funAvailIn.getInitValue()).getParameters().get(0)),
 							createDeepSizeof(scope,UtilIR.createTypeUser(tc.getTypedef()))));
 			((LambdaExpression)funAvailIn.getInitValue()).setBody(avail);
 			declStack.add(funAvailIn);
 			funcs.add(funAvailIn);
 			avail = UtilIR.createFunctionCall(scope, stdLib.get("pinAvailOut_bytes"), 
 					Arrays.asList( UtilIR.createExpression(scope, ((LambdaExpression)funAvailOut.getInitValue()).getParameters().get(0)),
 							createDeepSizeof(scope,UtilIR.createTypeUser(tc.getTypedef()))));
 			((LambdaExpression)funAvailOut.getInitValue()).setBody(avail);
 			declStack.add(funAvailOut);
 			funcs.add(funAvailOut);
 			
 			////Function body
 			/* Example pinWriteRepeat_T1 function
 			 void pinWriteRepeat__T(const LocalOutputPort port, T1* tokens, int nbr, T1** pTokens )
 			 {
 			  T1* buf;
 			  void* next;
 			  pinWrite_bytes(port,&buf,sizeof(T1)*nbr);  
 			  memcpy(buf,tokens,sizeof(T1)*nbr);
 			  pTokens = buf;
 			  for(int i=0;i<nbr;i++) {
 				pinWriteRepeat__TypeMember1(port,tokens->memberA,list_size_litA,&next);
 				buf->memberA = next;
 				pinWriteRepeat__TypeMember2(port,tokens->memberB,list_size_litB,&next);
 				buf->memberB = next;
 				pinWrite_bytes(port,&next,list_size_litC*sizeof(int));
 				memcpy(next,tokens->memberC,list_size_litC*sizeof(int));
 				buf->memberC = next;
 				tokens++;
 				buf++
 			  }
 			}*/
 			Block bodyWriteRepeat = UtilIR.createBlock(scope);
 
 			Variable pTokens = ((ProcExpression)funWriteRepeat.getInitValue()).getOutputs().get(0);
 			Variable port = ((ProcExpression)funWriteRepeat.getInitValue()).getParameters().get(0);
 			Variable token = ((ProcExpression)funWriteRepeat.getInitValue()).getParameters().get(1);
 			Variable nbr = ((ProcExpression)funWriteRepeat.getInitValue()).getParameters().get(2);
 			Variable buf = UtilIR.createVarDef(bodyWriteRepeat, "buf", type);
 			Variable next = UtilIR.createVarDef(bodyWriteRepeat, "next", TypeSystem.createTypeList(UtilIR.lit(0), TypeSystem.createTypeExternal("void",null)));
 			UtilIR.createProcCall(bodyWriteRepeat, stdLib.get("pinWrite_bytes"), 
 					Arrays.asList(UtilIR.createExpression(bodyWriteRepeat,port),
 							UtilIR.createExpression(bodyWriteRepeat,buf),
 							UtilIR.createExpression(UtilIR.createFunctionCall(bodyWriteRepeat, stdLib.get("sizeof"), 
 									Arrays.asList(fakeSizeofType(bodyWriteRepeat,type))), "*", UtilIR.createExpression(bodyWriteRepeat, nbr))), null);
 			UtilIR.createProcCall(bodyWriteRepeat, stdLib.get("memcpy"), 
 					Arrays.asList(UtilIR.createExpression(bodyWriteRepeat,buf),
 							UtilIR.createExpression(bodyWriteRepeat,token),
 							UtilIR.createExpression(UtilIR.createFunctionCall(bodyWriteRepeat, stdLib.get("sizeof"), 
 									Arrays.asList(fakeSizeofType(bodyWriteRepeat,type))), "*", UtilIR.createExpression(bodyWriteRepeat, nbr))), null);
 			finishedAssigns.add(UtilIR.createAssign(bodyWriteRepeat, pTokens, UtilIR.createExpression(bodyWriteRepeat,buf)));
 
 			TypeRecord struct = (TypeRecord) tc.getTypedef().getType();
 			Block loop = UtilIR.createBlock(bodyWriteRepeat);
 			//each member
 			for(int i=0;i<struct.getMembers().size();i++) {
 				Variable def=struct.getMembers().get(i);
 				Type memberType = (def.getType() instanceof TypeUser) ? ((TypeDeclaration) ((TypeUser) def.getType()).getDeclaration()).getType() : def.getType();
 				if (memberType instanceof TypeRecord) {
 					Declaration subWriteDef = findFuncDecl("pinWriteRepeat__"+((TypeUser) def.getType()).getDeclaration().getName(),topNetwork);
 					UtilIR.createProcCall(loop, subWriteDef, 
 							Arrays.asList(UtilIR.createExpression(loop,port),
 									UtilIR.createExpression(loop,token,def),
 									(Expression)UtilIR.lit(1)),
 							Arrays.asList(UtilIR.createVarRef(next)));
 					finishedAssigns.add(UtilIR.createStructAssign(loop, buf, def, UtilIR.createExpression(loop,next)));
 				} else
 				if (memberType instanceof TypeList) {
 					Type listType = ((TypeList)memberType).getType();
 					memberType = (listType instanceof TypeUser) ? ((TypeDeclaration) ((TypeUser) listType).getDeclaration()).getType() : listType;
 					if(memberType instanceof TypeRecord) {
 						Declaration subWriteDef = findFuncDecl("pinWriteRepeat__"+((TypeDeclaration) ((TypeUser) listType).getDeclaration()).getName(),topNetwork);
 						UtilIR.createProcCall(loop, subWriteDef, 
 								Arrays.asList(UtilIR.createExpression(loop,port),
 										UtilIR.createExpression(loop,token,def),
 										((TypeList)def.getType()).getSize()),
 								Arrays.asList(UtilIR.createVarRef(next)));
 						finishedAssigns.add(UtilIR.createStructAssign(loop, buf, def, UtilIR.createExpression(loop,next)));
 					} else {
 						UtilIR.createProcCall(loop, stdLib.get("pinWrite_bytes"), 
 								Arrays.asList(UtilIR.createExpression(loop,port),
 										UtilIR.createExpression(loop,next),
 										UtilIR.createExpression(UtilIR.createFunctionCall(loop, stdLib.get("sizeof"), 
 												Arrays.asList(fakeSizeofType(loop,memberType))), "*", ((TypeList)def.getType()).getSize())), null);
 						UtilIR.createProcCall(loop, stdLib.get("memcpy"), 
 								Arrays.asList(UtilIR.createExpression(loop,next),
 										UtilIR.createExpression(loop,token,def),
 										UtilIR.createExpression(UtilIR.createFunctionCall(loop, stdLib.get("sizeof"), 
 												Arrays.asList(fakeSizeofType(loop,memberType))), "*", ((TypeList)def.getType()).getSize())), null);
 						finishedAssigns.add(UtilIR.createStructAssign(loop, buf, def, UtilIR.createExpression(loop,next)));
 					}
 				}
 			}
 			if(!loop.getStatements().isEmpty()) {
 				finishedAssigns.add(UtilIR.createAssign(loop,token,
 						UtilIR.createExpression(UtilIR.createExpression(loop,token), "+", UtilIR.lit(1))));
 				finishedAssigns.add(UtilIR.createAssign(loop,buf,
 						UtilIR.createExpression(UtilIR.createExpression(loop,buf), "+", UtilIR.lit(1))));
 				UtilIR.createSimpleLoop(bodyWriteRepeat, loop, "i", 0, UtilIR.createExpression(bodyWriteRepeat,nbr));
 			} else {
 				//remove next since unused
 				bodyWriteRepeat.getDeclarations().remove(bodyWriteRepeat.getDeclarations().size()-1); 
 			}
 			((ProcExpression) funWriteRepeat.getInitValue()).setBody(bodyWriteRepeat);
 			declStack.add(funWriteRepeat);
 			funcs.add(funWriteRepeat);
 		}
 		declMap.put(tc, funcs);
 		return super.caseTypeConstructor(tc);
 	}
 
 	private void insertHeapManagment(Scope block, boolean hasFree, List<Declaration> defs, List<Statement> stmts) {
 		if(!defs.isEmpty()) {
 			List<Statement> free = new ArrayList<Statement>(); 
 			Block tempA = IrFactory.eINSTANCE.createBlock();
 			Block tempF = IrFactory.eINSTANCE.createBlock();
 			for (Declaration d : defs) {
 				if(d instanceof Variable && !UtilIR.containsTagBool(d.getAttributes(), "inhibitHeapMgmt")
 						&& !UtilIR.isNormalConstant(d)) {
 					Variable variableDef = (Variable) d;
 					if (UtilIR.isRecord(variableDef.getType())) {
 						TypeDeclaration decl = UtilIR.getTypeDeclaration(variableDef.getType());
 						//alloc
 						Declaration allocDef = findFuncDecl("alloc__"+decl.getName(), topNetwork);
 						ProcCall allocCall = UtilIR.createProcCall(0,block, allocDef, 
 								Arrays.asList(fakeNull(),(Expression)UtilIR.lit(1)),Arrays.asList(UtilIR.createVarRef(variableDef)));
 						//free
 						Declaration freeDef = findFuncDecl("free__"+decl.getName(), topNetwork);
 						ProcCall freeCall = UtilIR.createProcCallN(block, freeDef, 
 								Arrays.asList(UtilIR.createExpression(block,variableDef),(Expression)UtilIR.lit(1)),null);
 						free.add(freeCall);						
 					} else if (UtilIR.isList(variableDef.getType())) {
 						Type listType = ((TypeList)variableDef.getType()).getType();
 						if(listType instanceof TypeList) {
 							if(!UtilIR.containsTagBool(d.getAttributes(), "shallowHeapMgmt")) {
 								//Multi-dimensional array
 								Scope innerBlockA = block;
 								Scope innerBlockF = block;
 								while(UtilIR.isList(listType)) {
 									listType = ((TypeList)listType).getType();
 								}
 								Type elemType = listType;
 								Type type = variableDef.getType();
 								listType = ((TypeList)variableDef.getType()).getType();
 								List<Expression> indexA = new ArrayList<Expression>();
 								List<Expression> indexF = new ArrayList<Expression>();
 								indexA.clear();
 								indexF.clear();
 								int i=0;
 								Statement stmt = createMalloc(0,block,variableDef,indexA,((TypeList)type).getSize(),TypeSystem.createTypeList(((TypeList)type).getSize(), elemType));
 								while(UtilIR.isList(listType)) {
 									Block loopA = UtilIR.createBlock(innerBlockA);
 									Block loopF = UtilIR.createBlock(innerBlockF);
 									Variable iiA = UtilIR.createSimpleLoop(-1,tempA, loopA, variableDef.getName()+"_alloc_loop_count"+i, 0, ((TypeList)type).getSize());
 									if(innerBlockA instanceof Block)
 										((Block)innerBlockA).getStatements().addAll(1,tempA.getStatements());
 									else if(innerBlockA instanceof Action)
 										((Action)innerBlockA).getStatements().addAll(1,tempA.getStatements());
 									tempA.getStatements().clear();
 									Variable iiF = UtilIR.createSimpleLoop(-1,tempF, loopF, variableDef.getName()+"_free_loop_count"+i, 0, ((TypeList)type).getSize());
 									indexA.add(UtilIR.createExpression(innerBlockA, iiA));
 									indexF.add(UtilIR.createExpression(innerBlockF, iiF));
 									type = listType;
 									listType = ((TypeList)listType).getType();
 									if(listType instanceof TypeList) {
 										stmt = createMalloc(0,loopA,variableDef,indexA,((TypeList)listType).getSize(),TypeSystem.createTypeList(((TypeList)type).getSize(), elemType));
 										createFree(0,loopF,variableDef,indexF);
 									} else if(UtilIR.isRecord(listType)) {
 										TypeDeclaration decl = UtilIR.getTypeDeclaration(listType);
 										//alloc
 										Declaration allocDef = findFuncDecl("alloc__"+decl.getName(), topNetwork);
 										ProcCall allocCall = UtilIR.createProcCall(0,loopA, allocDef, 
 												Arrays.asList(fakeNull(),((TypeList)type).getSize()),
 												Arrays.asList(UtilIR.createVarRef(variableDef,indexA)));
 										//free
 										Declaration freeDef = findFuncDecl("free__"+decl.getName(), topNetwork);
 										ProcCall freeCall = UtilIR.createProcCall(0,loopF, freeDef, 
 												Arrays.asList(UtilIR.createExpression(loopF,variableDef,indexF,null,null),
 														((TypeList)type).getSize()),null);
 									} else {
 										//Elementary type so do a malloc and free
 										stmt = createMalloc(0,loopA,variableDef,indexA,((TypeList)type).getSize(),elemType);
 										createFree(0,loopF,variableDef,indexF);
 									}
 									if(innerBlockF==block)
 										free.addAll(tempF.getStatements());
 									else
 										((Block)innerBlockF).getStatements().addAll(0,tempF.getStatements());
 									tempF.getStatements().clear();
 									i++;
 									innerBlockA = loopA;
 									innerBlockF = loopF;
 								}
 								indexF.clear();
 								free.add(createFree(-2,block,variableDef,indexF));
 							} else {
 								//Shallow (TODO can any code get here???)
 								Statement stmt = createMalloc(0,block,variableDef,((TypeList)variableDef.getType()).getSize(),false);
 								free.add(createFree(-2,block,variableDef));								
 							}
 						} else if(UtilIR.isRecord(listType)) {
 							if(!UtilIR.containsTagBool(d.getAttributes(), "shallowHeapMgmt")) {
 								TypeDeclaration decl = UtilIR.getTypeDeclaration(listType);
 								//alloc
 								Declaration allocDef = findFuncDecl("alloc__"+decl.getName(), topNetwork);
 								ProcCall allocCall = UtilIR.createProcCall(0,block, allocDef, 
 										Arrays.asList(fakeNull(),((TypeList)variableDef.getType()).getSize()),
 										Arrays.asList(UtilIR.createVarRef(variableDef)));
 								//free
 								Declaration freeDef = findFuncDecl("free__"+decl.getName(), topNetwork);
 								ProcCall freeCall = UtilIR.createProcCallN(block, freeDef, 
 										Arrays.asList(UtilIR.createExpression(block,variableDef),
 												((TypeList)variableDef.getType()).getSize()),null);
 								free.add(freeCall);
 							} else {
 								//Shallow
 								Statement stmt = createMalloc(0,block,variableDef,((TypeList)variableDef.getType()).getSize(),false);
 								free.add(createFree(-2,block,variableDef));								
 							}
 						} else {
 							//Elementary type so do a malloc and free
 							Statement stmt = createMalloc(0,block,variableDef,((TypeList)variableDef.getType()).getSize(),true);
 							free.add(createFree(-2,block,variableDef));
 						}
 					}
 				}
 			}
 			block.getDeclarations().addAll(tempA.getDeclarations());
 			//Assumes always runs to last statement and no returns in the middle.
 			int i;
 			if(hasFree) {
 				defs.addAll(tempF.getDeclarations());
 				for(i=stmts.size()-1;i>=0;i--) {
 					if(stmts.get(i) instanceof ReturnValue) {
 						break;
 					}
 				}
 				if(i>=0) {
 					//Insert just before the last return
 					stmts.addAll(i,free);				
 				} else {
 					stmts.addAll(free);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public EObject caseBlock(Block block) {
 		if(!visited.contains(block)) {
 			visited.add(block);
 			//Insert declaration in scope for forEach statements
 			if(!forEachBlock.contains(block)) {
 				for(int i=0; i< block.getStatements().size(); i++) {
 					Statement s = block.getStatements().get(i);
 					if(s instanceof ForEach) {
 						ForEach stmt = (ForEach) s;
 						Block iblock = UtilIR.createBlock(block);
 						iblock.getStatements().add(stmt);
 						for(Generator g : stmt.getGenerators()) {
 							if(g.getSource() instanceof BinaryExpression && ((BinaryExpression) g.getSource()).getOperator().equals("..")) {
 								iblock.getDeclarations().addAll(g.getDeclarations());
 							} else {
 								throw new RuntimeException("[IR2CIR] Expecting foreach expressions to have A .. B syntax");
 							}
 						}
 						block.getStatements().set(i, iblock);
 						forEachBlock.add(iblock);
 					}
 				}
 			}
 			//Variable declarations might have a initValue that contains assignments to params, hence all initValues are converted to statements
 			//to allow the malloc to happen first
 			int pos=0;
 			for (Declaration d : block.getDeclarations()) {
 				if(UtilIR.isListOrRecord(((Variable)d).getType()) && UtilIR.isNormalInit(d)) {
 					UtilIR.createAssign(pos,block,(Variable)d, ((Variable)d).getInitValue());
 					((Variable)d).setInitValue(null);
 					pos++;
 				}
 			}
 			insertHeapManagment(block,true,block.getDeclarations(),block.getStatements());
 			return super.caseBlock(block);
 		} else {
 			return block;
 		}
 	}
 	
 	@Override
 	public EObject caseAction(Action action) {
 		if(!visited.contains(action)) {
 			visited.add(action);
 			cntpp(action);
 			//Insert declaration in scope for forEach statements
 			for(int i=0; i< action.getStatements().size(); i++) {
 				Statement s = action.getStatements().get(i);
 				if(s instanceof ForEach) {
 					ForEach stmt = (ForEach) s;
 					Block block = UtilIR.createBlock(action);
 					block.getStatements().add(stmt);
 					for(Generator g : stmt.getGenerators()) {
 						if(g.getSource() instanceof BinaryExpression && ((BinaryExpression) g.getSource()).getOperator().equals("..")) {
 							block.getDeclarations().addAll(g.getDeclarations());
 						} else {
 							throw new RuntimeException("[IR2CIR] Expecting foreach expressions to have A .. B syntax");
 						}
 					}
 					action.getStatements().set(i, block);
 					forEachBlock.add(block);
 				}
 			}
 			//Variable declarations might have a initValue that contains read port variables, hence all initValues are converted to statements
 			//To allow the port read to happen first
 			int pos=0;
 			for (Declaration d : action.getDeclarations()) {
 				if(UtilIR.isNormalInit(d)) {
 					UtilIR.createAssign(pos,action,(Variable)d, ((Variable)d).getInitValue());
 					((Variable)d).setInitValue(null);
 					pos++;
 				}
 			}
 			portMapIn.clear();
 			portMapOut.clear();
 			for(int i=0; i< lastActor.getInputPorts().size();i++) {
 				Port port = lastActor.getInputPorts().get(i);
 				portMapIn.put(port.getName(), (VariableExternal) UtilIR.createVarDef(action, "IN"+i+"_"+port.getName(), port.getType(),false,true,null));
 			}
 			for(int i=0; i< lastActor.getOutputPorts().size();i++) {
 				Port port = lastActor.getOutputPorts().get(i);
 				portMapOut.put(port.getName(), (VariableExternal) UtilIR.createVarDef(action, "OUT"+i+"_"+port.getName(), port.getType(),false,true,null));
 			}
 			lastAction = action;
 			//Need to insert port handling before heap handling is put last and first
 			for (PortRead read : action.getInputs()) {
 				Block loop = null;
 				Variable loopCounter = null;
 				Variable copy = null;
 				Declaration copyFunc = null;
 				for (int varIndex = read.getVariables().size()-1;varIndex>=0;varIndex--) {
 					VariableReference var = read.getVariables().get(varIndex);
 					if (UtilIR.isList(var.getDeclaration().getType())) {
 						TypeList t = (TypeList) var.getDeclaration().getType();
 						if (UtilIR.isRecord(t.getType())) {
 							if(loop==null) {
 								loop = UtilIR.createBlock(action);
 								copy = (Variable) UtilIR.createVarDef(action, "read_copy_"+read.getPort().getName(),  t.getType(), false, false, null);
 								UtilIR.tag(copy, "inhibitHeapMgmt", true);
 								copyFunc = findFuncDecl("copy__"+UtilIR.getTypeDeclaration(t.getType()).getName(),topNetwork);
 							}
 							ProcCall pinread = UtilIR.createProcCall(0,loop, stdLib.get("pinRead_bytes"), 
 									Arrays.asList(UtilIR.createExpression(loop,portMapIn.get(read.getPort().getName())),
 											UtilIR.createExpression(loop,copy),
 											createDeepSizeof(loop, t.getType())
 											),null);
 							if(loopCounter == null) {
 								loopCounter = UtilIR.createSimpleLoop(0,action, loop, "loop__counter__"+read.getPort().getName(), 0, t.getSize());
 							}
 							var.getIndex().add(UtilIR.createExpression(loop, loopCounter));
 							UtilIR.createProcCall(1,loop, copyFunc, 
 									Arrays.asList(UtilIR.createExpression(loop,copy), UtilIR.lit(1)),
 									Arrays.asList(var));
 						} else {
 							if(read.getVariables().size()>1) {
 								//More than one variable and repeat need each var to read a token at a time in a loop
 								if(loop==null) {
 									loop = UtilIR.createBlock(action);
 								}
 								FunctionCall pinread = UtilIR.createFunctionCall(loop, stdLib.get("pinRead_"+portTypeString(t)), 
 										Arrays.asList(UtilIR.createExpression(action,portMapIn.get(read.getPort().getName()))));
 								finishedAssigns.add(UtilIR.createAssign(0,loop,var,pinread));
 								if(loopCounter == null) {
 									loopCounter = UtilIR.createSimpleLoop(0,action, loop, "loop__counter__"+read.getPort().getName(), 0, t.getSize());
 								}
 								var.getIndex().add(UtilIR.createExpression(loop, loopCounter));
 							} else {
 								ProcCall pinread = UtilIR.createProcCall(0,action, stdLib.get("pinReadRepeat_"+portTypeString(t.getType())), 
 										Arrays.asList(UtilIR.createExpression(action,portMapIn.get(read.getPort().getName())),
 												UtilIR.createExpression(action,var.getDeclaration()),
 												t.getSize()
 												),null);
 							}
 						}
 					} else {
 						Type t = var.getDeclaration().getType();
 						if (UtilIR.isRecord(t)) {
 							copy = (Variable) UtilIR.createVarDef(action, "read_copy_"+var.getDeclaration().getName(), t, false, false, null);
 							UtilIR.tag(copy, "inhibitHeapMgmt", true);
 							ProcCall pinread = UtilIR.createProcCall(0,action, stdLib.get("pinRead_bytes"), 
 									Arrays.asList(UtilIR.createExpression(action,portMapIn.get(read.getPort().getName())),
 											UtilIR.createExpression(action,copy),
 											createDeepSizeof(action, t)
 											),null);
 							copyFunc = findFuncDecl("copy__"+UtilIR.getTypeDeclaration(t).getName(),topNetwork);
 							UtilIR.createProcCall(1,action, copyFunc, 
 									Arrays.asList(UtilIR.createExpression(action,copy), UtilIR.lit(1)),
 									Arrays.asList(var));
 						} else {
 							FunctionCall pinread = UtilIR.createFunctionCall(action, stdLib.get("pinRead_"+portTypeString(t)), 
 									Arrays.asList(UtilIR.createExpression(action,portMapIn.get(read.getPort().getName()))));
 							finishedAssigns.add(UtilIR.createAssign(0,action,var.getDeclaration(),pinread));
 						}
 					}
 				}				
 			}
 			for (PortWrite write : action.getOutputs()) {
 				//Move the port statements and defines directly into action statements and defines to get proper order
 				action.getStatements().addAll(write.getStatements());
 				action.getDeclarations().addAll(write.getDeclarations());
 				write.getStatements().clear();
 				Type type = write.getPort().getType(); //Johan promise all expressions have same type (but obviously not include list repeat)
 				TypeDeclaration decl = null;
 				Block loop = null;
 				Variable dummy = null;
 				Declaration pinWriteDecl = null;
 				Variable index = null;
 				for (int i=0; i < write.getExpressions().size();i++) {
 					if(write.getExpressions().get(i) instanceof VariableExpression) {
 						VariableExpression expr = (VariableExpression) write.getExpressions().get(i);
 						if (write.getRepeat()!=null) {
 							if (UtilIR.isRecord(type)) {
 								if(loop==null) {
 									decl = UtilIR.getTypeDeclaration(type);
 									dummy = (Variable) UtilIR.createVarDef(action, "X_"+write.getPort().getName(), type, false, false, null);
 									UtilIR.tag(dummy, "inhibitHeapMgmt", true);
 									pinWriteDecl = findFuncDecl("pinWriteRepeat__"+decl.getName(), topNetwork);
 									loop = UtilIR.createBlock(action);
 									index = UtilIR.createSimpleLoop(-1,action, loop, "loop__counter__"+write.getPort().getName(), 0, write.getRepeat());
 								}
 								if(!expr.getIndex().isEmpty() && index != null)
 									throw new RuntimeException("[IR2CIR] Index collision, expecting port write expressions to be VariableExpressions without index when in repeat!");									
 								ProcCall pinwrite = UtilIR.createProcCall(loop.getStatements().size()-1,loop, pinWriteDecl, 
 										Arrays.asList(UtilIR.createExpression(loop,portMapOut.get(write.getPort().getName())),
 													  UtilIR.createExpression(loop, (Variable)expr.getVariable(), Arrays.asList(UtilIR.createExpression(loop,index)), null, null),
 												UtilIR.lit(1)),
 										Arrays.asList(UtilIR.createVarRef(dummy)));
 							} else {
 								if(write.getExpressions().size()>1) {
 									if(loop==null) {
 										loop = UtilIR.createBlock(action);
 										index = UtilIR.createSimpleLoop(-1,action, loop, "loop__counter__"+write.getPort().getName(), 0, write.getRepeat());
 									}
 									if(!expr.getIndex().isEmpty() && index != null)
 										throw new RuntimeException("[IR2CIR] Index collision, expecting port write expressions to be VariableExpressions without index when in repeat!");									
 									ProcCall pinwrite = UtilIR.createProcCall(loop.getStatements().size()-1,loop, stdLib.get("pinWriteRepeat_"+portTypeString(type)), 
 											Arrays.asList(UtilIR.createExpression(loop,portMapOut.get(write.getPort().getName())),
 													UtilIR.createExpression(loop, (Variable)expr.getVariable(),Arrays.asList(UtilIR.createExpression(loop,index)), null, null),
 													UtilIR.lit(1)
 													),null);
 								} else {
 									ProcCall pinwrite = UtilIR.createProcCall(action, stdLib.get("pinWriteRepeat_"+portTypeString(type)), 
 											Arrays.asList(UtilIR.createExpression(action,portMapOut.get(write.getPort().getName())),
 													UtilIR.createExpression(action, expr),
 													write.getRepeat()
 													),null);
 								}
 							}
 						} else {
 							if (UtilIR.isRecord(type)) {
 								decl = UtilIR.getTypeDeclaration(type);
 								dummy = (Variable) UtilIR.createVarDef(action, "X_"+expr.getVariable().getName(), type, false, false, null);
 								UtilIR.tag(dummy, "inhibitHeapMgmt", true);
 								pinWriteDecl = findFuncDecl("pinWriteRepeat__"+decl.getName(), topNetwork);
 								ProcCall pinwrite = UtilIR.createProcCall(action, pinWriteDecl, 
 										Arrays.asList(UtilIR.createExpression(action,portMapOut.get(write.getPort().getName())),
 												UtilIR.createExpression(action, expr),
 												UtilIR.lit(1)),
 										Arrays.asList(UtilIR.createVarRef(dummy)));
 							} else {
 								ProcCall pinwrite = UtilIR.createProcCall(action, stdLib.get("pinWrite_"+portTypeString(type)), 
 										Arrays.asList(UtilIR.createExpression(action,portMapOut.get(write.getPort().getName())),
 												UtilIR.createExpression(action, expr)
 												),null);
 							}
 						}
 					} else if(UtilIR.isLiteralExpression(write.getExpressions().get(i))) {
 						LiteralExpression expr = (LiteralExpression) write.getExpressions().get(i);
 						if (write.getRepeat()!=null) {
 							if(write.getExpressions().size()>1) {
 								if(loop==null) {
 									loop = UtilIR.createBlock(action);
 									index = UtilIR.createSimpleLoop(-1,action, loop, "loop__counter__"+write.getPort().getName(), 0, write.getRepeat());
 								}
 								ProcCall pinwrite = UtilIR.createProcCall(loop.getStatements().size()-1,loop, stdLib.get("pinWriteRepeat_"+portTypeString(type)), 
 										Arrays.asList(UtilIR.createExpression(loop,portMapOut.get(write.getPort().getName())),
 												expr,
 												UtilIR.lit(1)
 												),null);
 							} else {
 								ProcCall pinwrite = UtilIR.createProcCall(action, stdLib.get("pinWriteRepeat_"+portTypeString(type)), 
 										Arrays.asList(UtilIR.createExpression(action,portMapOut.get(write.getPort().getName())),
 												expr,
 												write.getRepeat()
 												),null);
 							}
 						} else {
 							ProcCall pinwrite = UtilIR.createProcCall(action, stdLib.get("pinWrite_"+portTypeString(type)), 
 									Arrays.asList(UtilIR.createExpression(action,portMapOut.get(write.getPort().getName())),
 											expr),null);
 						}
 					} else {
 						throw new RuntimeException("[IR2CIR] Expecting port write expressions to be VariableExpressions or LiteralExpressions!");
 					}
 				}
 			}
 			
 			insertHeapManagment(action,true,action.getDeclarations(),action.getStatements());
 
 			for (Declaration d : action.getDeclarations()) {
 				doSwitch(d);
 			}
 			for (Guard g : action.getGuards()) {
 				doSwitch(g);
 			}
 			for (Statement s : action.getStatements()) {
 				Statement s2=(Statement) doSwitch(s);
 				if(!s2.equals(s)) {
 					int i = action.getStatements().indexOf(s);
 					action.getStatements().set(i, s2);
 				}
 			}
 			leave();
 			//Don't call super, we visit all children in the order we need!!!
 		}
 		return action;
 	}
 
 	@Override
 	public EObject caseAssign(Assign assign) {
 		cntpp(assign);
 		Type finalType = assign.getTarget().getType();
 		if(!finishedAssigns.contains(assign) && UtilIR.isListOrRecord(finalType)) {
 			if(assign.getExpression() instanceof VariableExpression || 
 					(assign.getExpression() instanceof ListExpression && ((ListExpression) assign.getExpression()).getGenerators().isEmpty())) {
 
 				Type type = UtilIR.getType(finalType);
 				TypeDeclaration typeDef = null;
 				if(type instanceof TypeRecord)
 					typeDef = UtilIR.getTypeDeclaration(finalType);
 				if(type == null)
 					throw new RuntimeException("[IR2CIR] Missing type of target " + assign.getTarget().getDeclaration().getName() + " in an assignment");
 				
 				Scope scope=getThisScope();
 				if(UtilIR.isList(type)) {
 					Expression expr = assign.getExpression();
 					if(assign.getExpression() instanceof ListExpression) {
 						boolean literal=true;
 						for(Expression e : ((ListExpression) assign.getExpression()).getExpressions())
 							if(!(e instanceof LiteralExpression))
 								literal=false;
 						if(literal) {
 							Variable tempLiteral = (Variable) UtilIR.createVarDef(scope, assign.getTarget().getDeclaration().getName() + "_" + assign.getExpression().getId(), assign.getTarget().getType(), true, false, assign.getExpression());
 							expr = UtilIR.createExpression(scope, tempLiteral);
 						} else {
 							//CAL allows assignment of none constant lists but C don't, hence we need roll out the expressions into separate assignments
 							Block block = IrFactory.eINSTANCE.createBlock();
 							block.setOuter(getThisScope());
 							for(int i = 0; i < ((ListExpression) assign.getExpression()).getExpressions().size(); i++) {
 								UtilIR.createAssign(block, UtilIR.createVarRef((Variable) assign.getTarget().getDeclaration(), Arrays.asList((Expression)UtilIR.lit(i))), ((ListExpression) assign.getExpression()).getExpressions().get(i));
 							}
 							//To expand assignments that are none-scalar
 							caseBlock(block);
 							return block;
 						}
 					}
 					if( UtilIR.isRecord(((TypeList)type).getType())) {
 						Declaration copyDef = findFuncDecl("copy__"+UtilIR.getTypeDeclaration(((TypeList)type).getType()).getName(),topNetwork);
 						ProcCall memcpy = UtilIR.createProcCallN(scope, copyDef, 
 								Arrays.asList(expr, ((TypeList)type).getSize()),
 								Arrays.asList(assign.getTarget()));
 						leave();
 						return memcpy;						
 					} else {
 						FunctionCall size_of = UtilIR.createFunctionCall(scope, stdLib.get("sizeof"), 
 								Arrays.asList(fakeSizeofType(scope,assign.getTarget().getType())));
 						Expression byteSize = UtilIR.createExpression(size_of,"*",
 								((TypeList)type).getSize());
 						ProcCall memcpy = UtilIR.createProcCallN(scope, stdLib.get("memcpy"), 
 								Arrays.asList(UtilIR.createExpression(scope,assign.getTarget()),
 										expr,byteSize),null);
 						leave();
 						return memcpy;
 					}
 				} else	if(UtilIR.isRecord(type)) {
 					Declaration copyDef = findFuncDecl("copy__"+typeDef.getName(),topNetwork);
 					ProcCall memcpy = UtilIR.createProcCallN(scope, copyDef, 
 							Arrays.asList(assign.getExpression(), UtilIR.lit(1)),
 							Arrays.asList(assign.getTarget()));
 					leave();
 					return memcpy;						
 				}
 			} else if(assign.getExpression() instanceof TypeConstructorCall) {
 				EObject obj = caseTypeConstructorCall((TypeConstructorCall)assign.getExpression());
 				if(obj instanceof Block) {
 					//The expression have been forced to be changed to a block since e.g. it used list expressions in the call
 					//Add the assignment last (the params have been updated to temp variables inside caseTypeConstructorCall)
 					((Block)obj).getStatements().add(assign);
 					return obj;
 				}
 			} else if(assign.getExpression() instanceof FunctionCall) {
 				//We add the target as last input to functions when they are struct or list since it has been allocated outside
 				//we just want to change the content
 				FunctionCall func = (FunctionCall) assign.getExpression();
 				func.getParameters().add(UtilIR.createExpression(getThisScope(), assign.getTarget()));
 				caseFunctionCall(func);
 			} else if(assign.getExpression() instanceof IfExpression) {
 				//Got an if expression but the assignment is list or struct, hence it needs to be made into an if statement
 				//with each of the 2 potential assigns into separate statements (could require copy or type constructor call, etc) 
 				IfExpression ifexpr = (IfExpression) assign.getExpression();
 				IfStatement ifstmt = UtilIR.createIf(null, ifexpr.getCondition(), (Statement) caseAssign(UtilIR.createAssign(null, assign.getTarget(), 
 						ifexpr.getThenExpression())), (Statement) caseAssign(UtilIR.createAssign(null, assign.getTarget(), ifexpr.getElseExpression())));
 				leave();
 				return ifstmt;				
 			} else if((assign.getExpression() instanceof ListExpression && !((ListExpression) assign.getExpression()).getGenerators().isEmpty())) {
 				//Need to generate the assignment from code
 				//FIXME only first incarnation of generating the values from Generator
 				Block block = IrFactory.eINSTANCE.createBlock();
 				block.setOuter(getThisScope());
 				List<Generator> gens = ((ListExpression) assign.getExpression()).getGenerators();
 				ForEach foreach = UtilIR.createForEach(block, gens);
 				Expression index = UtilIR.lit(0);
 				Expression size = null;
 				for(int i = gens.size()-1; i>=0; i--) {
 					Generator g = gens.get(i);
 					if(size!=null)
 						index = UtilIR.createExpression(index, "*", size);
 					index = UtilIR.createExpression(index, "+", UtilIR.createExpression((Scope) block, (Variable) g.getDeclarations().get(0)));
 					if(g.getSource() instanceof BinaryExpression && ((BinaryExpression) g.getSource()).getOperator().equals("..")) {
 						index = UtilIR.createExpression(index, "-", ((BinaryExpression) g.getSource()).getOperand1());
 						size = UtilIR.createExpression(
 								UtilIR.createExpression(((BinaryExpression) g.getSource()).getOperand2(), "-", ((BinaryExpression) g.getSource()).getOperand1()),
 								"+",
 								UtilIR.lit(1));
 						block.getDeclarations().addAll(g.getDeclarations());
 					} else
 						throw new RuntimeException("[IR2CIR] Expecting ListExpressions only have A .. B syntaxed generators");
 				}
 				VariableReference target = assign.getTarget();
 				target.getIndex().add(index);
 				for(Expression e : ((ListExpression) assign.getExpression()).getExpressions()) {
 					UtilIR.createAssign(foreach.getBody(), target, e);
 				}
 				leave();
 				return block;
 			} else {
 				throw new RuntimeException("[IR2CIR] Unsupported expression (" + assign.getExpression().getClass().toString() + ") when translating assignment to " + assign.getTarget().getDeclaration().getName());
 			}
 		}
 		leave();
 		return assign;
 	}
 
 	@Override
 	public EObject caseLambdaExpression(LambdaExpression obj) {
 		if(!visited(obj)) {
 			//Need to see if any declaration that is not parameters exist
 			List<Declaration> nonparams = new ArrayList<Declaration>();
 			nonparams.clear();
 			for(Declaration d : obj.getDeclarations()) {
 				boolean found = false;
 				for(Variable v : obj.getParameters()) {
 					if(v.getName().equals(d.getName())) {
 						found=true;
 						break;
 					}
 				}
 				if(!found)
 					nonparams.add(d);
 			}
 			if(nonparams.isEmpty())
 				return obj;
 			cntpp(obj);
 			doSwitch(obj.getType());
 			for (Variable d : obj.getParameters()) {
 				doSwitch(d);
 			}
 			//Lambda only have an expression but we need statements to be able to insert malloc/free around the expression
 			//Hence we set the expression to a anonymous proc expression (with a return of temp var of the original expression)
 			ProcExpression pe = IrFactory.eINSTANCE.createProcExpression();
 			Block body = IrFactory.eINSTANCE.createBlock();
 			pe.setBody(body);
 			pe.setContext(obj);
 			pe.setOuter(obj.getOuter());
 			pe.setId(Util.getDefinitionId());
 			pe.setType(TypeSystem.createTypeUndef()); //Normally a TypeProc, but this is a nameless parameterless ProcExpression
 			//Variable declarations might have a initValue that contains list/structs variables, hence all initValues are converted to statements
 			//To allow the allocation to happen first
 			body.getDeclarations().addAll(nonparams);
 			for (Declaration d : body.getDeclarations()) {
 				if(UtilIR.isNormalInit(d)) {
 					Assign assign = UtilIR.createAssign(body,(Variable)d, ((Variable)d).getInitValue());
 					((Variable)d).setInitValue(null);
 					caseAssign(assign);
 				}
 			}
 			//The original expr as return stmt
 			Variable ret;
 			if(UtilIR.isListOrRecord(((TypeLambda)obj.getType()).getOutputType())) {
 				TypeLambda lambda = (TypeLambda)obj.getType();
 				ret = UtilIR.createVarDef(null, "ret", lambda.getOutputType());
 				lambda.getInputTypes().add(lambda.getOutputType());
 				obj.getParameters().add(ret);
 			} else {
 				ret = UtilIR.createVarDef(body, "ret", ((TypeLambda)obj.getType()).getOutputType());
 			}
 			Statement assign = UtilIR.createAssign(body, ret, obj.getBody());
 			assign=(Statement)caseAssign((Assign)assign);
 			body.getStatements().add(assign);
 			ReturnValue retstmt = IrFactory.eINSTANCE.createReturnValue();
 			retstmt.setValue(UtilIR.createExpression(body,ret));
 			body.getStatements().add(retstmt);
 			body.setOuter(obj);
 			obj.setBody(pe);
 			insertHeapManagment(body,true,body.getDeclarations(),body.getStatements());
 			leave();
 		}
 		return obj;
 	}
 
 	@Override
 	public EObject caseGuard(Guard guard) {
 		cntpp(guard);
 		doSwitch(guard.getType());
 		for (Variable d : guard.getParameters()) {
 			doSwitch(d);
 		}
 		//Guard only have an expression but we need statements to be able to insert malloc/free and peek call statements around the expression
 		//Hence we set the expression to a anonymous proc expression (with a return of temp var of the original expression)
 		ProcExpression pe = IrFactory.eINSTANCE.createProcExpression();
 		Block body = IrFactory.eINSTANCE.createBlock();
 		pe.setBody(body);
 		pe.setContext(guard);
 		pe.setOuter(guard.getOuter());
 		pe.setId(Util.getDefinitionId());
 		pe.setType(TypeSystem.createTypeBool()); //Normally a TypeProc, but this is a nameless parameterless ProcExpression
 		Variable ret = UtilIR.createVarDef(body, "ret", TypeSystem.createTypeBool());
 		body.getStatements().add(UtilIR.createAssign(body, ret, guard.getBody()));
 		ReturnValue retstmt = IrFactory.eINSTANCE.createReturnValue();
 		retstmt.setValue(UtilIR.createExpression(body,ret));
 		body.getStatements().add(retstmt);
 		body.setOuter(guard);
 		guard.setBody(pe);
 		Actor actor = getThisActor();
 		//Need to know how many variables on each port
 		Map<String,Integer> nbrVarsOnPort = new HashMap<String,Integer>();
 		for(PortRead p : ((Action)guard.getOuter()).getInputs()) {
 			nbrVarsOnPort.put(p.getPort().getName(),p.getVariables().size());
 		}
 		Set<Declaration> decl = new HashSet<Declaration>();
 		for (PortPeek p : guard.getPeeks()) {
 			doSwitch(p);
 			VariableReference var = p.getVariable();
 			for(Declaration d : guard.getOuter().getDeclarations()) {
 				if(d.getName().equals(var.getDeclaration().getName())) {
 					if(UtilIR.isList(var.getDeclaration().getType())) {
 						//Need to allocate the top list since the tokens are stored as individual tokens
 						UtilIR.tag(d, "shallowHeapMgmt", true);
 						decl.add(d);
 					} else {
 						UtilIR.tag(d, "inhibitHeapMgmt", true);
 					}
 					break;
 				}
 			}
 			if (UtilIR.isList(var.getDeclaration().getType())) {
 				TypeList t = (TypeList) var.getDeclaration().getType();
 				if (UtilIR.isRecord(t.getType())) {
 					Expression offset = var.getIndex().get(0);
 					offset = UtilIR.createExpression(offset,"*",UtilIR.lit(nbrVarsOnPort.get(p.getPort().getName())));
 					if(!var.getIndex().isEmpty()) {
 						offset = UtilIR.createExpression(offset,"+",UtilIR.lit(p.getPosition()));
 					}
 
 					ProcCall pinpeek = UtilIR.createProcCall(0,body, stdLib.get("pinPeek_bytes"), 
 							Arrays.asList(UtilIR.createExpression(body,portMapIn.get(p.getPort().getName())),
 									UtilIR.createExpression(body,var),
 									createDeepSizeof(body, t.getType()),
 									offset),null);	
 				} else {
 					Expression offset = var.getIndex().get(0);
 					offset = UtilIR.createExpression(offset,"*",UtilIR.lit(nbrVarsOnPort.get(p.getPort().getName())));
 					if(!var.getIndex().isEmpty()) {
 						offset = UtilIR.createExpression(offset,"+",UtilIR.lit(p.getPosition()));
 					}
 					FunctionCall pinpeek = UtilIR.createFunctionCall(body, stdLib.get("pinPeek_"+portTypeString(t)), 
 							Arrays.asList(UtilIR.createExpression(body,portMapIn.get(p.getPort().getName())),offset));
 					finishedAssigns.add(UtilIR.createAssign(0,body,var,pinpeek));
 				}
 			} else {
 				Type t = var.getDeclaration().getType();			
 				if (UtilIR.isRecord(t)) {
 					ProcCall pinpeek = UtilIR.createProcCall(0,body, stdLib.get("pinPeek_bytes"), 
 							Arrays.asList(UtilIR.createExpression(body,portMapIn.get(p.getPort().getName())),
 									UtilIR.createExpression(body,var.getDeclaration()),
 									createDeepSizeof(body, t),
 									UtilIR.lit(p.getPosition())
 									),null);
 				} else {
 					FunctionCall pinpeek = UtilIR.createFunctionCall(body, stdLib.get("pinPeek_"+portTypeString(t)), 
 							Arrays.asList(UtilIR.createExpression(body,portMapIn.get(p.getPort().getName())),UtilIR.lit(p.getPosition())));
 					finishedAssigns.add(UtilIR.createAssign(0,body,var.getDeclaration(),pinpeek));
 				}
 			}
 		}
 		List<Declaration> asList= new ArrayList<Declaration>();
 		asList.addAll(decl);
 		int size=asList.size();
 		insertHeapManagment(body,true,asList,body.getStatements());
 		if(asList.size()>size) {
 			//For example a looping index was added in the heap management code
 			//Add that to the guard body declarations
 			body.getDeclarations().addAll(asList.subList(size, asList.size()));
 		}
 		leave();
 		return guard;
 	}
 
 	@Override
 	public EObject caseProcCall(ProcCall call) {
 		Scope b = null;
 		boolean found=false;
 		for(int i = 0; i<call.getInParameters().size();i++) {
 			Expression e = call.getInParameters().get(i);
 			if(e instanceof ListExpression) {
 				//Need to create temp variable for list expression
 				found=true;
 				Type type = UtilIR.getProcInputType(call.getProcedure(), i);
 				if(b==null)
 					b = UtilIR.createBlock(getThisScope());
 				Variable var = UtilIR.createVarDef(b, "tempList_"+e.getId(), type);
 				((Block)b).getStatements().add((Statement)caseAssign(UtilIR.createAssignN(b, var, e)));
 				call.getInParameters().set(i, UtilIR.createExpression(b, var));
 			}
 		}
 		if(found) {
 			((Block)b).getStatements().add(call);
 			return b;
 		}
 		return super.caseProcCall(call);
 	}
 
 	@Override
 	public EObject caseTypeConstructorCall(TypeConstructorCall call) {
 		Scope b = null;
 		boolean found=false;
 		for(int i = 0; i<call.getParameters().size();i++) {
 			Expression e = call.getParameters().get(i);
 			if(e instanceof ListExpression) {
 				//Need to create temp variable for list expression
 				found=true;
 				TypeRecord structType = (TypeRecord) UtilIR.getType(call.getTypedef());
 				Type type = structType.getMembers().get(i).getType();
 				if(b==null)
 					b = UtilIR.createBlock(getThisScope());
 				Variable var = UtilIR.createVarDef(b, "tempList_"+e.getId(), type);
 				var.setInitValue(e);
 				//((Block)b).getStatements().add((Statement)caseAssign(UtilIR.createAssignN(b, var, e)));
				//call.getParameters().set(i, UtilIR.createExpression(b, var));
 			} else if(e instanceof TypeConstructorCall) {
 				//FIXME we need to handle the params of this one as well...
 			} else {
 				EObject obj = doSwitch(e);
 				if(obj!=e) {
 					throw new RuntimeException("We have not implemented type constructor calls with param of " + e.getClass());
 				}
 			}
 		}
 		if(found) {
 			//The call needs to be e.g. placed last in the block by the outer (since this call is just an expression
 			//inside a statement) 
 			return b;
 		}
 		return super.caseTypeConstructorCall(call);
 	}
 }

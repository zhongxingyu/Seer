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
 
 package org.caltoopia.types;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.transformer.FixMovedExpr;
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.ForwardDeclaration;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.IfExpression;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.UnaryExpression;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableImport;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.WhileLoop;
 import org.caltoopia.ir.util.IrReplaceSwitch;
 import org.eclipse.emf.ecore.EObject;
 
 public class TypeAnnotater extends IrReplaceSwitch {
 	
     @Override
     public Expression caseFunctionCall(FunctionCall call) {
         super.caseFunctionCall(call);
 
         if(call.getType() == null && call.getFunction() instanceof VariableExpression) {
             VariableExpression var = (VariableExpression) call.getFunction();
             if(var.getType() instanceof TypeLambda) {
                 call.setType(((TypeLambda)var.getType()).getOutputType());
             }
         }
         return call;
     }
 
     @Override
     public Expression caseTypeConstructorCall(TypeConstructorCall call) {
         super.caseTypeConstructorCall(call);
         
         if(call.getType() == null) {
             Declaration decl = call.getTypedef();
             if (decl instanceof TypeDeclarationImport) {
                 try {
                     decl =  ActorDirectory.findTypeDeclaration((TypeDeclarationImport)decl);
                 } catch (DirectoryException x) {
                     System.err.println("[TypeAnnotater] Internal Error - caseTypeConstructorCall");
                     return call;
                 }
             } else if (decl instanceof ForwardDeclaration) {
                 decl = ((ForwardDeclaration) decl).getDeclaration();
             }
             if(decl instanceof TypeDeclaration) {
                 TypeUser typeUser = IrFactory.eINSTANCE.createTypeUser();
                 typeUser.setDeclaration(decl);
                 call.setType(typeUser);
             }
         }        
         return call;
     }
 
     @Override
     public EObject caseListExpression(ListExpression expr) {
         super.caseListExpression(expr);
         
         if(expr.getType()==null && !expr.getExpressions().isEmpty()) {
             Type elemType = expr.getExpressions().get(0).getType();
             if(elemType!=null) {
                 Expression sz = null;
                 if(expr.getGenerators().isEmpty()) {
                     sz = Util.createIntegerLiteral(expr.getExpressions().size());
                 } else if(expr.getGenerators().size()==1){
                     //Only one first generator supported otherwise size is made dynamic (i.e. null)
                     Generator g = expr.getGenerators().get(0);
                     if(g.getSource() instanceof VariableExpression) {
                         if(g.getSource().getType() instanceof TypeList) {
                             sz = ((TypeList)g.getSource().getType()).getSize();
                         }
                     } else if(g.getSource() instanceof BinaryExpression) {
                         BinaryExpression exprSrc = (BinaryExpression) g.getSource();
                         if(exprSrc.getOperator().equals("..") &&
                             (exprSrc.getOperand1().getType() instanceof TypeUint
                             ||exprSrc.getOperand1().getType() instanceof TypeInt
                             ||exprSrc.getOperand2().getType() instanceof TypeUint
                             ||exprSrc.getOperand2().getType() instanceof TypeInt)) {
                            if(UtilIR.isLiteralExpression(exprSrc.getOperand1()) && UtilIR.isLiteralExpression(exprSrc.getOperand1())) {
                                sz = UtilIR.createExpression(exprSrc.getOperand2(), "-", exprSrc.getOperand1());
                                 sz.setContext(expr.getContext());
                                 sz.setType(exprSrc.getOperand1().getType());
                             }
                         }
                     } else if(g.getSource() instanceof ListExpression) {
                         ListExpression lit = (ListExpression) g.getSource();
                         if(lit.getType() instanceof TypeList) {
                             sz = ((TypeList)lit.getType()).getSize();
                         } else if(lit.getGenerators().isEmpty()){
                             //Fallback when list expression misses type
                             sz = UtilIR.lit(lit.getExpressions().size());
                         }
                     } else if(g.getSource() instanceof FunctionCall) {
                         FunctionCall call = (FunctionCall) g.getSource();
                         if(call.getType() instanceof TypeList) {
                             sz = ((TypeList)call.getType()).getSize();
                         }
                     }
                 }
                 TypeList listType = IrFactory.eINSTANCE.createTypeList();
                 listType.setType(elemType);
                 listType.setSize(sz);
                 expr.setType(listType);
             }
         }
         return expr;
     }
 
 	@Override
 	public VariableExpression caseVariableExpression(VariableExpression varExpr) {
 		super.caseVariableExpression(varExpr);
 
 		Declaration decl = varExpr.getVariable();
 		if (decl instanceof VariableImport) {
 			try {
 				decl =  ActorDirectory.findVariable((VariableImport) decl);
 			} catch (DirectoryException x) {
 				System.err.println("[TypeAnnotater] Internal Error - caseVariableExpression");
 			}
 		} else if (decl instanceof ForwardDeclaration) {
 			decl = ((ForwardDeclaration) decl).getDeclaration();
 		}
 		if(decl instanceof VariableExternal) {
 			//When external just use the type of the variable
 			varExpr.setType(((VariableExternal) decl).getType());
 		} else {
 			Type type = ((Variable) decl).getType();
 			if(varExpr.getIndex().isEmpty()) {
 				varExpr.setType(type);
 			} else {
 				Type typeElem = type;
 				for(int i=varExpr.getIndex().size();i>0;i--) {
 					typeElem = TypeSystem.getElementType(typeElem);
 				}
 				varExpr.setType(typeElem);
 			}			
 			if (!varExpr.getMember().isEmpty()) {						
 				for (Member member : varExpr.getMember()) {
 					while (TypeSystem.isList(type)) {
 						type = TypeSystem.getElementType(type);
 					}
 					type = TypeSystem.asRecord(type);
 					
 					Variable found = null;
 					for (Variable field : ((TypeRecord) type).getMembers()) {					
 						if (field.getName().equals(member.getName())) {
 							found = field;
 						}
 					}
 					assert(found != null);
 					type = found.getType();
 					member.setType(type);
 					if(member.getIndex().isEmpty()) {
 						varExpr.setType(type);
 					} else {
 						Type typeElem = type;
 						for(int i=member.getIndex().size();i>0;i--) {
 							typeElem = TypeSystem.getElementType(typeElem);
 						}
 						varExpr.setType(typeElem);
 					}
 				}			
 			}
 		}
 		return varExpr;
 	}
 	
 	@Override 
 	public VariableReference caseVariableReference(VariableReference varRef) {
 		super.caseVariableReference(varRef);
 		
 		Declaration decl = varRef.getDeclaration();
 		if (decl instanceof VariableImport) {
 			try {
 				decl =  ActorDirectory.findVariable((VariableImport) decl);
 			} catch (DirectoryException x) {
 				System.err.println("[TypeAnnotater] Internal Error - caseVariableExpression");
 			}
 		} else if (decl instanceof ForwardDeclaration) {
 			decl = ((ForwardDeclaration) decl).getDeclaration();
 		}
 		
 		Type type = ((Variable) decl).getType();
 		if(varRef.getIndex().isEmpty()) {
 			varRef.setType(type);
 		} else {
 			Type typeElem = type;
 			for(int i=varRef.getIndex().size();i>0;i--) {
 				typeElem = TypeSystem.getElementType(typeElem);
 			}
 			varRef.setType(typeElem);
 		}
 		if (!varRef.getMember().isEmpty()) {			
 			for (Member member : varRef.getMember()) {
 				while (TypeSystem.isList(type)) {
 					type = TypeSystem.getElementType(type);
 				}
 				type = TypeSystem.asRecord(type);
 
 				Variable found = null;
 				for (Variable field : ((TypeRecord) type).getMembers()) {					
 					if (field.getName().equals(member.getName())) {
 						found = field;
 					}
 				}
 				assert(found != null);
 				type = found.getType();
 				member.setType(type);
 				if(member.getIndex().isEmpty()) {
 					varRef.setType(type);
 				} else {
 					Type typeElem = type;
 					for(int i=member.getIndex().size();i>0;i--) {
 						typeElem = TypeSystem.getElementType(typeElem);
 					}
 					varRef.setType(typeElem);
 				}
 			}			
 		}
 		
 		return varRef;
 	}
 
    @Override
     public Expression caseBinaryExpression(BinaryExpression binaryExpression) {
        super.caseBinaryExpression(binaryExpression);
        String operator = binaryExpression.getOperator();
        Type type = null;
         if (Arrays.asList("and","or","=",">","<","<=",">=","!=","<>").contains(operator)) {
             type = TypeSystem.createTypeBool();
         } else if (Arrays.asList("*","/","-","+","bitand","bitor","mod",">>>",">>","<<").contains(operator)) {
             if(binaryExpression.getType()==null && binaryExpression.getOperand1()!=null) {
                 type = binaryExpression.getOperand1().getType();
                 if(type==null && binaryExpression.getOperand2()!=null) {
                     type = binaryExpression.getOperand2().getType();
                 }
             }
         }
         if(type!=null) {
             binaryExpression.setType(type);
         }
         return binaryExpression;
     }
 
     @Override
     public Expression caseUnaryExpression(UnaryExpression unaryExpression) {
         super.caseExpression(unaryExpression);
         
        String operator = unaryExpression.getOperator();
        Type type = null;
         if (Arrays.asList("!","not").contains(operator)) {
             type = TypeSystem.createTypeBool();
         } else if (Arrays.asList("#").contains(operator)) {
             type = TypeSystem.createTypeInt();
         } else if (Arrays.asList("-","+").contains(operator)) {
             if(unaryExpression.getType()==null && unaryExpression.getOperand()!=null) {
                 type = unaryExpression.getOperand().getType();
             }
         }
         if(type!=null) {
             unaryExpression.setType(type);
         }
         return unaryExpression;
     }
     
     @Override
     public EObject caseIfExpression(IfExpression expr) {
         super.caseIfExpression(expr);
         if(expr.getType()==null) {
             Type typeT = null;
             Type typeE = null;
             typeT = expr.getThenExpression().getType();
             if(TypeSystem.isList(typeT)) {
                 typeE = expr.getElseExpression().getType();
                 Stack<Expression> sz = new Stack<Expression>();
                 boolean equal = true;
                 if(typeT != null && typeE != null) {
                     while(typeT instanceof TypeList) {
                         if(((TypeList)typeT).getSize() != ((TypeList)typeE).getSize()) {
                             if(((TypeList)typeT).getSize() instanceof IntegerLiteral && ((TypeList)typeE).getSize() instanceof IntegerLiteral && 
                                 ((IntegerLiteral)((TypeList)typeT).getSize()).getValue() == ((IntegerLiteral)((TypeList)typeE).getSize()).getValue()) {
                                 sz.push(((TypeList)typeT).getSize());
                             } else {
                                 sz.push(null);
                                 equal = false;
                             }
                         } else {
                             sz.push(((TypeList)typeT).getSize());
                         }
                         typeT = ((TypeList)typeT).getType();
                         typeE = ((TypeList)typeE).getType();
                     }
                     if(equal) {
                         typeT = expr.getThenExpression().getType();
                     } else {
                         //Not equal size of lists, build new type with some null sizes
                         while(!sz.isEmpty()) {
                             typeT = TypeSystem.createTypeList(sz.pop(), typeT);
                         }
                     }
                 }
             }
             expr.setType(typeT);
         }
         return expr;
     }
 
 }

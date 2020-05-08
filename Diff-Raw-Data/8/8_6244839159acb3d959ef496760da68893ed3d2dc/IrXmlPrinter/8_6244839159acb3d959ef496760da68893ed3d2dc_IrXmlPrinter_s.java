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
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.caltoopia.ast2ir.Stream;
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.ast2ir.PriorityGraph;
 import org.caltoopia.ir.AbstractActor;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.ActorInstance;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.Assign;
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.BooleanLiteral;
 import org.caltoopia.ir.Connection;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.ExternalActor;
 import org.caltoopia.ir.FloatLiteral;
 import org.caltoopia.ir.ForEach;
 import org.caltoopia.ir.ForwardDeclaration;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.Guard;
 import org.caltoopia.ir.IfExpression;
 import org.caltoopia.ir.IfStatement;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Namespace;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.Point2PointConnection;
 import org.caltoopia.ir.FromSource;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.TaggedExpression;
 import org.caltoopia.ir.ToSink;
 import org.caltoopia.ir.Port;
 import org.caltoopia.ir.PortInstance;
 import org.caltoopia.ir.PortPeek;
 import org.caltoopia.ir.PortRead;
 import org.caltoopia.ir.PortWrite;
 import org.caltoopia.ir.ProcCall;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Schedule;
 import org.caltoopia.ir.State;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.StringLiteral;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeBool;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeConstructor;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeFloat;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeProc;
 import org.caltoopia.ir.TypeString;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.TypeUndef;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.UnaryExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableImport;
 import org.caltoopia.ir.WhileLoop;
 import org.caltoopia.ir.util.IrSwitch;
 
 public class IrXmlPrinter extends IrSwitch<Stream> {
 	
 	final boolean debugPrint = false;
 
 	Stream s;
 	String folder;
 	String instance;
 	
 	Map<String, String> inputPortMap = new HashMap<String, String>();
 	
 	Map<String, String> outputPortMap = new HashMap<String, String>();
 	
 	Set<String> includes = new HashSet<String>();
 	
 	public IrXmlPrinter(String folder) {
 		this.folder = folder;
 		this.instance = null;
 	}
 
 	public IrXmlPrinter(String folder, String instance) {
 		this.folder = folder;
 		this.instance = instance;
 	}
 
 	public void run(Namespace ns) {		
 		caseNamespace(ns);
 	}
 	
 	public void run(AbstractActor actor) {
 		doSwitch(actor);
 	}
 	
 	@Override
 	public Stream caseNamespace(Namespace ns) {
 		File file = new File(folder + File.separator + UtilIR.namespace2Path(ns.getName()));
 		if (!file.exists()) 
 			file.mkdir();
 		
 		String path = Util.getPathAnnotation(ns.getAnnotations());
 		String filename = path.replace(File.separator, "$");
 		filename = Util.removeWindowsDriveLetter(filename.replace(".cal", ".xml"));
 		
 		s = new Stream(folder + File.separator + UtilIR.namespace2Path(ns.getName()) + File.separator + filename);				
 		s.print("<Namespace name=\"" + Util.packQualifiedName(ns.getName()) + "\""
 			    + " id=\"" + ns.getId() + "\">");
 		
 		doAnnotations(ns);
 		
 		for (Declaration d : ns.getDeclarations()) {
 			doSwitch(d);
 		}
 				
 		s.dec();
 		s.println("</Namespace>");
 		s.close();
 	
 		for (AbstractActor a : ns.getActors()) {
 			doSwitch(a);
 		}
 		
 		return s;
  	}
  	
 	@Override
 	public Stream caseNetwork(Network network) {
 		if(debugPrint) {
 			System.out.println("[IrXmlPrinter] ---- Network: List all type decl (import) ------");
 			for(Declaration d: network.getDeclarations()) {
 				if(d instanceof TypeDeclarationImport || d instanceof TypeDeclaration) {
 					System.out.println("[IrXmlPrinter] " + (d instanceof TypeDeclarationImport?"TypeDeclImport ":"TypeDecl ") + d.getId() + " " + d.getName());
 					typedeclimports.add(d.getId());
 				}
 			}
 		}
 		s = new Stream(folder + File.separator + UtilIR.namespace2Path(network.getType().getNamespace()) + File.separator + network.getType().getName() + ".xml");		
 		s.printlnInc("<Network id=\"" + network.getId() + "\" >"); ;
 
 		doAnnotations(network);
 
 		doSwitch(network.getType());		     
 				    
     	for (Port port : network.getInputPorts()) { 
 			s.printlnInc("<Port name=\"" + port.getName() + "\""
 					    + " id=\"" + port.getId() + "\"" 
 					    + " direction=\"in\">");
 			doSwitch(port.getType());
 			s.printlnDec("</Port>");
 		}
 					
 		for (Port port : network.getOutputPorts()) { 
 			s.printlnInc("<Port name=\"" + port.getName() + "\""
 					    + " id=\"" + port.getId() + "\"" 
 					    + " direction=\"out\">");
 			doSwitch(port.getType());
 			s.printlnDec("</Port>");
 		}				    
 		
 		for (Declaration def : network.getDeclarations()) {
 			doSwitch(def);				
 		}
 		
 		for (ActorInstance instance : network.getActors()) {
 			caseActorInstance(instance);			
 		}				
 			  		
 		for (Connection c : network.getConnections()) {
 			if (c instanceof Point2PointConnection) {
 				PortInstance source = ((Point2PointConnection) c).getSource();
 				PortInstance target = ((Point2PointConnection) c).getTarget();
 				s.print("<Connection kind=\"point2point\""
 						 + " dst=\"" + target.getActor().getId() + "\""
 						 + " dst-port=\"" + target.getName() + "\""
 				         + " src=\"" + source.getActor().getId() + "\""
 				         + " src-port=\"" + source.getName() + "\"" 
 				         + " id=\"" + c.getId() + "\"");
 				if (c.getAttributes().isEmpty() && c.getAnnotations().isEmpty()) {
 					s.println("/>");
 				} else {
 					s.printlnInc(">");
 					doAnnotations(c);
 					for (TaggedExpression te : c.getAttributes()) {
 						s.printlnInc("<Attribute tag=\"" + te.getTag() + "\">");
 						doSwitch(te.getExpression());
 						s.printlnDec("</Attribute>");
 					}
 					s.printlnDec("</Connection>");
 				}
 			} else if (c instanceof FromSource) {
 				Port source = ((FromSource) c).getSource();
 				PortInstance target = ((FromSource) c).getTarget();
 				s.print("<Connection kind=\"fromSource\""
 						 + " dst=\"" + target.getActor().getId() + "\""
 						 + " dst-port=\"" + target.getName() + "\""
 				         + " src-port=\"" + source.getName() + "\""
 				         + " id=\"" + c.getId() + "\"");
 				if (c.getAttributes().isEmpty() && c.getAnnotations().isEmpty()) {
 					s.println("/>");
 				} else {
 					s.printlnInc(">");
 					doAnnotations(c);
 					for (TaggedExpression te : c.getAttributes()) {
 						s.printlnInc("<Attribute tag=\"" + te.getTag() + "\">");
 						doSwitch(te.getExpression());
 						s.printlnDec("</Attribute>");
 					}
 					s.printlnDec("</Connection>");
 				}
 			} else {
 				PortInstance source = ((ToSink) c).getSource();
 				Port target = ((ToSink) c).getSink();
 				s.print("<Connection kind=\"toSink\""
 						 + " dst-port=\"" + target.getName() + "\""
 				         + " src=\"" + source.getActor().getId() + "\""
 				         + " src-port=\"" + source.getName() + "\""
 				         + " id=\"" + c.getId() + "\"");
 				if (c.getAttributes().isEmpty() && c.getAnnotations().isEmpty()) {
 					s.println("/>");
 				} else {
 					s.printlnInc(">");
 					doAnnotations(c);
 					for (TaggedExpression te : c.getAttributes()) {
 						s.printlnInc("<Attribute tag=\"" + te.getTag() + "\">");
 						doSwitch(te.getExpression());
 						s.printlnDec("</Attribute>");
 					}
 					s.printlnDec("</Connection>");
 				}
 			}
 		}
 
 		s.printlnDec("</Network>");
 		s.close();
 		
 		return s;		
 	}
 
 	private Set<String> typedeclimports = new HashSet<String>();
 	
 	@Override
 	public Stream caseActor(Actor actor) {
 		if(debugPrint) {
 			System.out.println("[IrXmlPrinter] ---- Actor: List all type decl (import) ------");
 			for(Declaration d: actor.getDeclarations()) {
 				if(d instanceof TypeDeclarationImport) {
 					typedeclimports.add(d.getId());
 					System.out.println("[IrXmlPrinter] TypeDeclImport " + d.getId() + " " + d.getName());
 				}
 			}
 		}
 		s = new Stream(folder + File.separator + UtilIR.namespace2Path(actor.getType().getNamespace()) +
 				File.separator + actor.getType().getName() + 
 				((instance!=null)?"_$"+instance:"") + 
 				".xml");				
 		s.printlnInc("<Actor id=\"" + actor.getId() + "\" >"); 
 		doAnnotations(actor);
 		
 		doSwitch(actor.getType());
 		
 		for (Port port : actor.getInputPorts()) { 
 			s.printlnInc("<Port name=\"" + port.getName() + "\""
 				    	+ " id=\"" + port.getId() + "\"" 
 				    	+ " direction=\"in\">");
 			doSwitch(port.getType());
 			s.printlnDec("</Port>");
 		}
 		
 		for (Port port : actor.getOutputPorts()) { 
 			s.printlnInc("<Port name=\"" + port.getName() + "\""
 				    	+ " id=\"" + port.getId() + "\"" 
 				    	+ " direction=\"out\">");
 			doSwitch(port.getType());
 			s.printlnDec("</Port>");
 		}
 
 		for (Declaration def : actor.getDeclarations()) {
 			doSwitch(def);
 		}	
 	
 		for (Action a : actor.getActions()) {
 			caseAction(a);
 		}
 		
 		for (Action a : actor.getInitializers()) {
 			caseInitializer(a);
 		}
 
 		doActionScheduler(actor);
 		
 		s.printlnDec("</Actor>");
 		s.close();
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseExternalActor(ExternalActor actor) {
 		s = new Stream(folder + File.separator + UtilIR.namespace2Path(actor.getType().getNamespace()) + File.separator + actor.getType().getName() + ".xml");				
 		s.printlnInc("<ExternalActor name=\"" +  actor.getType().getName() 
 				    + "\" namespace=\"" + Util.packQualifiedName(actor.getType().getNamespace())  + "\""
 				    + " id=\"" + actor.getId() + "\">"); 
 
 		doAnnotations(actor);
 		
 		doSwitch(actor.getType());
 		
 		for (Port port : actor.getInputPorts()) { 
 			s.printlnInc("<Port name=\"" + port.getName() + "\""
 				    	+ " id=\"" + port.getId() + "\"" 
 				    	+ " direction=\"in\">");
 			doSwitch(port.getType());
 			s.printlnDec("</Port>");
 		}
 		
 		for (Port port : actor.getOutputPorts()) { 
 			s.printlnInc("<Port name=\"" + port.getName() + "\""
 				     	+ " id=\"" + port.getId() + "\"" 
 						+ " direction=\"out\">");
 			doSwitch(port.getType());
 			s.printlnDec("</Port>");
 		}
 		
 		for (Declaration def : actor.getParameters()) {
 			doSwitch(def);				
 		}
 		
 		s.printlnDec("</ExternalActor>");		
 		s.close();
 
 		return s;
 	}
 	
 	public Stream caseInitializer(Action action) {
 		s.printlnInc("<Initializer id=\"" + action.getId() + "\"" 
 				    + " outer-scope=\"" + action.getOuter().getId() + "\""
 	                + " time= \"1\" >");	
 		s.printlnInc("<QID>");
 		for (String id : action.getTag()) {
 			s.println("<ID name=\"" + id + "\"/>");
 		}
 		s.printlnDec("</QID>");
 		
 		for (Guard guard : action.getGuards()) {
 			caseGuard(guard);
 		}
 
 		for (PortRead r : action.getInputs()) {
 			doSwitch(r);
 		}
 		
 		for (Declaration v : action.getDeclarations() ) {
 			doSwitch(v);
 		}
 		
 		for (Statement s : action.getStatements()) {
 			doSwitch(s);
 		}
 
 		for (PortWrite w : action.getOutputs()) {
 			doSwitch(w);
 		}
 		s.printlnDec("</Initializer>");
 		return s;
 	}
 
 	@Override
 	public Stream caseAction(Action action) {
 		s.printlnInc("<Action id=\"" + action.getId() + "\"" 
 				    + " outer-scope=\"" + action.getOuter().getId() + "\""
 	                + " time= \"1\" >");	
 		s.printlnInc("<QID>");
 		for (String id : action.getTag()) {
 			s.println("<ID name=\"" + id + "\"/>");
 		}
 		s.printlnDec("</QID>");	
 		doAnnotations(action);
 		
 		for (Guard guard : action.getGuards()) {
 			caseGuard(guard);
 		}
 
 		for (PortRead r : action.getInputs()) {
 			doSwitch(r);
 		}
 		
 		for (Declaration v : action.getDeclarations() ) {
 			doSwitch(v);
 		}
 		
 		for (Statement s : action.getStatements()) {
 			doSwitch(s);
 		}
 
 		for (PortWrite w : action.getOutputs()) {
 			doSwitch(w);
 		}
 		s.printlnDec("</Action>");
 		return s;
 	}
 	
 	@Override
 	public Stream caseGuard(Guard guard) {
 		s.printlnInc("<Guard id=\"" + guard.getId() + "\""
 			        + " outer-scope=\"" + guard.getOuter().getId() + "\">");
 		doAnnotations(guard);
 		
 		for (Declaration d : guard.getDeclarations()) {
 			doSwitch(d);
 		}
 
 		for (PortPeek peek : guard.getPeeks()) {
 			doSwitch(peek);
 		}
 
 		doSwitch(guard.getBody());
 		s.printlnDec("</Guard>");
 
 		return s;
 	}
 	
 	public Stream caseActorInstance(ActorInstance instance) {
 		s.printlnInc("<Instance name=\"" + instance.getName() + "\""
 				    + " id=\"" + instance.getId() + "\">");
 
 		doAnnotations(instance);
 		
 		doSwitch(instance.getType());
 		for (PortInstance port : instance.getInputs()) {
 			s.println("<PortInstance name=\"" + port.getName() + "\""
 				        + " id = \"" + port.getId() + "\""
 					    + " direction=\"in\" />");
 		}
 		
 		for (PortInstance port : instance.getOutputs()) {
 			s.println("<PortInstance name=\"" + port.getName() + "\""
 					    + " id =\"" + port.getId() + "\""
 					    + " direction=\"out\" />");
 		}
 
 		for (TaggedExpression te : instance.getActualParameters()) {
 			s.printlnInc("<ActualParameter name=\"" + te.getTag() + "\">");
 			doSwitch(te.getExpression());
 			s.printlnDec("</ActualParameter>");
 		}
 		
 		caseType(instance.getType());
 		
 		s.printlnDec("</Instance>");
 		return s;
 	}
 	
 	@Override
 	public Stream caseVariable(Variable variable) {
 		s.printlnInc("<Decl kind=\"Variable\" name=\"" + variable.getName() + "\""
 				    + " id=\"" + variable.getId() + "\""
 				    + " scope=\"" + variable.getScope().getId() + "\""
 				    + " constant=\""  + (variable.isConstant()  ? "true":"false") + "\""
 				    + " parameter=\"" + (variable.isParameter() ? "true":"false") + "\" >");
 		
 		doAnnotations(variable);
 		
 		doSwitch(variable.getType());
 		if (variable.getInitValue() != null) {
 			s.printlnInc("<InitialValue>");
 			doSwitch(variable.getInitValue());
 			s.printlnDec("</InitialValue>");
 		}
 		s.printlnDec("</Decl>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseVariableExternal(VariableExternal variable) {
 		s.printlnInc("<Decl kind=\"VariableExternal\"" 
 					+ " name=\"" + variable.getName() + "\""
 					+ " id=\"" + variable.getId() + "\""
 					+ " scope=\"" + variable.getScope().getId() + "\">");	
 
 		doAnnotations(variable);
 		
 		doSwitch(variable.getType());
 		s.printlnDec("</Decl>");
 	
 		return s;		
 	}
 
 	@Override
 	public Stream caseVariableImport(VariableImport variable) {
 		s.print("<Decl kind=\"VariableImport\" name=\"" + variable.getName() + "\""
 				  + " namespace=\"" + Util.packQualifiedName(variable.getNamespace())  + "\""
 				  + " id=\"" + variable.getId() + "\"");
 		if (variable.getAnnotations().isEmpty()) {
 			s.println("/>");		
 		} else {
 			s.printlnInc(">");		
 			doAnnotations(variable);
			s.printlnDec("/Decl>");
 		}
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeDeclarationImport(TypeDeclarationImport typedef) {
 		s.print("<Decl kind=\"TypeImport\" name=\"" + typedef.getName() + "\""
 				  + " namespace=\"" + Util.packQualifiedName(typedef.getNamespace())  + "\""
 				  + " id=\"" + typedef.getId() + "\"");		
 		if (typedef.getAnnotations().isEmpty()) {
 			s.println("/>");		
 		} else {
			s.printlnInc(">");		
 			doAnnotations(typedef);
			s.printlnDec("/Decl>");
 		}
 		
 		return s;
 	}		
 	
 	@Override
 	public Stream caseForwardDeclaration(ForwardDeclaration decl) {
 		s.printlnInc("<Decl kind=\"Forward\""
 					+ " name=\"" + decl.getName() + "\""
 					+ " id=\"" + decl.getId() + "\""
 					+ " scope=\"" + decl.getScope().getId() + "\""
 					+ " forward-id=\"" + decl.getDeclaration().getId() + "\">");
 
 		doAnnotations(decl);
 		doSwitch(decl.getType());
 		s.printlnDec("</Decl>");
 	
 		return s;		
 	}
 	
 	@Override
 	public Stream caseTypeDeclaration(TypeDeclaration typeDecl) {
 		s.printlnInc("<Decl kind=\"Type\" name=\"" + typeDecl.getName() + "\"" 
 				    + " id=\"" + typeDecl.getId() + "\""
 				    + " scope=\"" + typeDecl.getScope().getId() + "\">");	
 		doAnnotations(typeDecl);
 		doSwitch(typeDecl.getType());
 		doSwitch(typeDecl.getConstructor());
 		s.printlnDec("</Decl>");
 
 		return s;
 	}	
 
 	@Override
 	public Stream caseTypeConstructor(TypeConstructor tc) {
 		s.printlnInc("<TypeConstructor name=\"" + tc.getName() + "\""
 				    + " id=\"" + tc.getId() + "\""
 				    + " typedef-id=\"" + tc.getTypedef().getId() + "\""
 				    + " scope=\"" + tc.getScope().getId() + "\">");	
 		doAnnotations(tc);
 		for (Variable par : tc.getParameters()) {
 			doSwitch(par);
 		}
 		s.printlnDec("</TypeConstructor>");	
 		
 		return s;
 	}
 
 	
 	@Override
 	public Stream caseUnaryExpression(UnaryExpression expr) {
 		s.printlnInc("<Expr kind=\"Unary\" id=\"" + expr.getId() + "\""
 				    + " context-scope=\"" + expr.getContext().getId() + "\""
 				    + " operator=\"" + UtilIR.marshall(expr.getOperator()) + "\">");
 		doAnnotations(expr);
 		doSwitch(expr.getOperand());
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseBinaryExpression(BinaryExpression expr) {
 		s.printlnInc("<Expr kind=\"Binary\" id=\"" + expr.getId() + "\""
 				    + " context-scope=\"" + expr.getContext().getId() + "\""
 				    + " operator=\"" + UtilIR.marshall(expr.getOperator()) + "\">");
 		doAnnotations(expr);
 	    doSwitch(expr.getOperand1());
 		doSwitch(expr.getOperand2());
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}		
 
 	@Override
 	public Stream caseFunctionCall(FunctionCall expr) {
 		s.printlnInc("<Expr kind=\"Call\" id=\"" + expr.getId() + "\""
 				    + " context-scope=\"" + expr.getContext().getId() + "\">"); 
 		doAnnotations(expr);
         if(expr.getType()!=null) {
             doSwitch(expr.getType());
         }
 		doSwitch(expr.getFunction());
 		s.printlnInc("<Args>");
 		for (Expression e : expr.getParameters()) {
 			doSwitch(e);
 		}
 		s.printlnDec("</Args>");
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseTypeConstructorCall(TypeConstructorCall expr) {
 		s.printlnInc("<Expr kind=\"Construction\" id=\"" + expr.getId() + "\""
 				    + " name=\"" + expr.getName() + "\""
 				    + " typedef-id=\"" + expr.getTypedef().getId() + "\""
 				    + " context-scope=\"" + expr.getContext().getId() + "\">"); 
 		doAnnotations(expr);
 		s.printlnInc("<Args>");
 		for (Expression e : expr.getParameters()) {
 			doSwitch(e);
 		}
 		s.printlnDec("</Args>");		
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 	
 	
 	@Override
 	public Stream caseIfExpression(IfExpression expr) {
 		s.printlnInc("<Expr kind=\"If\" id=\"" + expr.getId() + "\""
 				    + " context-scope=\"" + expr.getContext().getId() + "\">"); 
 		doAnnotations(expr);
 		doSwitch(expr.getCondition()); 	
 		doSwitch(expr.getThenExpression());	
 		doSwitch(expr.getElseExpression());
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseVariableExpression(VariableExpression var) {
 		if (var.getIndex().isEmpty() && var.getMember().isEmpty() && var.getAnnotations().isEmpty() && var.getType()==null) {
 			s.println("<Expr kind=\"Var\"" 
 		             + " id=\"" + var.getId() + "\""
 		             + " context-scope=\"" + var.getContext().getId() + "\""
 					 + " decl-id=\"" + var.getVariable().getId() + "\"/>");		
 		} else {
 			s.printlnInc("<Expr kind=\"Var\"" 
 		             + " id=\"" + var.getId() + "\""
 		             + " context-scope=\"" + var.getContext().getId() + "\""
 					 + " decl-id=\"" + var.getVariable().getId() + "\">");		
 			doAnnotations(var);
 			if(var.getType()!=null) {
 				doSwitch(var.getType());
 			}
 			if (!var.getIndex().isEmpty()) {
 				s.printlnInc("<Indices>");
 				for (Expression e : var.getIndex()) {
 					doSwitch(e);
 				}
 				s.dec();
 				s.println("</Indices>");
 			}
 
 			if (!var.getMember().isEmpty()) {
 				s.printlnInc("<Members>");
 				for (Member m : var.getMember()) {
 					doSwitch(m);
 				}
 				s.printlnDec("</Members>");
 			}
 			s.printlnDec("</Expr>");
 		}
 		return s;
 	}
 	
 	@Override
 	public Stream caseLambdaExpression(LambdaExpression lambda) {
 		s.printlnInc("<Expr kind=\"Lambda\""
 				    + " id=\"" + lambda.getId() + "\""
 				    + " context-scope=\"" + lambda.getContext().getId() + "\""
 				    + " outer-scope=\"" + lambda.getOuter().getId() + "\">");
 		doAnnotations(lambda);
 		doSwitch(lambda.getType());
 
 		for (Declaration decl : lambda.getDeclarations()) {
 			doSwitch(decl);
 		}
 		
 		doSwitch(lambda.getBody()); 
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseProcExpression(ProcExpression proc) {
 		s.printlnInc("<Expr kind=\"Proc\""
 				    + " id=\"" + proc.getId() + "\""
 				    + " context-scope=\"" + proc.getContext().getId() + "\""
 				    + " outer-scope=\"" + proc.getOuter().getId() + "\">");
 		doAnnotations(proc);
 		doSwitch(proc.getType());
 
 		for (Declaration decl : proc.getDeclarations()) {
 			doSwitch(decl);
 		}
 			
 		doSwitch(proc.getBody());
 		
 		if (!proc.getOutputs().isEmpty()) {
 			s.printlnInc("<Output>");
 			for (Declaration decl : proc.getOutputs()) {
 				doSwitch(decl);
 			}
 			s.printlnDec("</Output>");
 		}
 		
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseListExpression(ListExpression expr) {
 		s.printlnInc("<Expr kind=\"List\" " 
 				    + " id=\"" + expr.getId() + "\""
 				    + " context-scope=\"" + expr.getContext().getId() + "\" >");
 		doAnnotations(expr);
         if(expr.getType()!=null) {
             doSwitch(expr.getType());
         }
 		for (Generator g : expr.getGenerators()) {
 			caseGenerator(g);
 		}
 
 		for (Expression element : expr.getExpressions()) {
 			doSwitch(element);
 		}
 	
 		s.printlnDec("</Expr>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseIntegerLiteral(IntegerLiteral lit) {
 		s.println("<Expr kind=\"literal-integer\" value=\"" + Long.toString(lit.getValue()) + "\"/>");
 
 		return s;
 	}
 
 	@Override
 	public Stream caseFloatLiteral(FloatLiteral lit) {
 		s.println("<Expr kind=\"literal-float\" value=\"" + Double.toString(lit.getValue()) + "\"/>");
 
 		return s;
 	}
 
 	@Override
 	public Stream caseBooleanLiteral(BooleanLiteral lit) {
 		s.println("<Expr kind=\"literal-bool\" value=\"" + (lit.isValue() ? "true" : "false") + "\"/>");
 
 		return s;
 	}
 
 	@Override
 	public Stream caseStringLiteral(StringLiteral lit) {
 		s.println("<Expr kind=\"literal-string\" value=\"" + lit.getValue() + "\"/>");
 		return s;
 	}
 	
 	@Override 
 	public Stream caseGenerator(Generator g) {
 		s.printlnInc("<Generator id=\"" + g.getId() + "\""
 				+ " outer-scope=\"" + g.getOuter().getId() + "\">");
 		doAnnotations(g);
 		for (Declaration d : g.getDeclarations()) {
 			doSwitch(d);
 		}
 		doSwitch(g.getSource());
 		s.printlnDec("</Generator>");
 
 		return s;
 	}
 	
 	@Override
 	public Stream caseVariableReference(VariableReference var) {
 		if (var.getIndex().isEmpty() && var.getMember().isEmpty() && var.getAnnotations().isEmpty() && var.getType()==null) {
 			s.println("<Var name=\"" + var.getDeclaration().getName() + "\""				   
 			     	+ " decl-id=\"" + var.getDeclaration().getId() + "\"/>");
 		} else {
 			s.printlnInc("<Var name=\"" + var.getDeclaration().getName() + "\""				   
 				     	+ " decl-id=\"" + var.getDeclaration().getId() + "\">");
 			doAnnotations(var);
 			if(var.getType()!=null) {
 				doSwitch(var.getType());
 			}
 			if (!var.getIndex().isEmpty()) {
 				s.printlnInc("<Indices>");
 				for (Expression e : var.getIndex()) {
 					doSwitch(e);
 				}
 				s.dec();
 				s.println("</Indices>");
 			}
 			if (!var.getMember().isEmpty()) {
 				s.printlnInc("<Members>");
 				for (Member m : var.getMember()) {
 					doSwitch(m);
 				}
 				s.printlnDec("</Members>");
 			}	
 			s.printlnDec("</Var>");
 		}
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseMember(Member member) {
 		if (member.getIndex().isEmpty() &&  member.getAnnotations().isEmpty() && member.getType()==null) {
 			s.println("<Member name=\"" + member.getName() + "\"/>");
 		} else {
 			s.printlnInc("<Member name=\"" + member.getName() + "\">");
 			doAnnotations(member);
 			if(member.getType()!=null) {
 				doSwitch(member.getType());
 			}
 			if (!member.getIndex().isEmpty()) {
 				if (!member.getIndex().isEmpty()) {
 					s.printlnInc("<Indices>");
 					for (Expression e : member.getIndex()) {
 						doSwitch(e);
 					}
 					s.printlnDec("</Indices>");
 				}
 			}
 			s.printlnDec("</Member>");
 		}
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseAssign(Assign assign) {
 		s.printlnInc("<Stmt kind=\"Assign\">");
 		doAnnotations(assign);
 		doSwitch(assign.getTarget());
 		doSwitch(assign.getExpression());
 		s.printlnDec("</Stmt>"); 
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseProcCall(ProcCall call) {
 		s.printlnInc("<Stmt kind=\"Call\"" 
 				    + " decl-id=\"" +  call.getProcedure().getId() + "\">"); 	
 		doAnnotations(call);
 		s.printlnInc("<Args>");
 		for (Expression e : call.getInParameters()) {
 			doSwitch(e);
 		}
 		
 		for (VariableReference e : call.getOutParameters()) {
 			doSwitch(e);
 		}
 
 		s.printlnDec("</Args>");
 		s.printlnDec("</Stmt>");
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseWhileLoop(WhileLoop stmt) {
 		s.printlnInc("<Stmt kind=\"While\">"); 
 		doAnnotations(stmt);
 		doSwitch(stmt.getCondition());
 		doSwitch(stmt.getBody());
 		s.printlnDec("</Stmt>"); 
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseForEach(ForEach stmt) {
 		s.printlnInc("<Stmt kind=\"ForEach\">"); 
 		doAnnotations(stmt);
 		for (Generator g : stmt.getGenerators()) {
 			caseGenerator(g);
 		}
 		doSwitch(stmt.getBody());
 		s.printlnDec("</Stmt>"); 
 		
 		return s;
 	}
 
 	
 	@Override
 	public Stream caseIfStatement(IfStatement stmt) {
 		s.printlnInc("<Stmt kind=\"If\">"); 
 		doAnnotations(stmt);
 		doSwitch(stmt.getCondition());
 		doSwitch(stmt.getThenBlock());
 		if (stmt.getElseBlock() != null && !stmt.getElseBlock().getStatements().isEmpty()) {
 			doSwitch(stmt.getElseBlock());
 		}
 		s.printlnDec("</Stmt>"); 
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseBlock(Block block) {
 		s.printlnInc("<Stmt kind=\"Block\" "
 				    + " id=\"" + block.getId( ) + "\""
 				    + " outer-scope=\"" + block.getOuter().getId() + "\">"); 
 		doAnnotations(block);
 		for (Declaration d : block.getDeclarations()) {
 			doSwitch(d);
 		}
 		
 		for (Statement s : block.getStatements()) {
 			doSwitch(s);
 		}
 				
 		s.printlnDec("</Stmt>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseReturnValue(ReturnValue returnValue) {
 		s.printlnInc("<Stmt kind=\"ReturnValue\">");
 		doSwitch(returnValue.getValue());
 		s.printlnDec("</Stmt>");
 
 		return s;
 	}
 
 	@Override
 	public Stream casePortRead(PortRead read) {
 		s.printlnInc("<PortRead port=\"" + read.getPort().getName() 
 				    + "\" id=\"" + read.getId( ) + "\">");
 		doAnnotations(read);
 		if (read.getRepeat() != null) {
 			s.printlnInc("<Repeat>");
 			doSwitch(read.getRepeat());
 			s.printlnDec("</Repeat>");
 		}
 		
 		for (VariableReference var : read.getVariables()) {
 			doSwitch(var);
 		}
 		s.printlnDec("</PortRead>");
  
 		return s;
 	}
 	
 	@Override
 	public Stream casePortPeek(PortPeek peek) {
 		s.printlnInc("<PortPeek port=\"" + peek.getPort().getName() + "\""
 				    + " position=\"" + peek.getPosition() + "\""
 				    + " id=\"" + peek.getId( )+ "\">");
 		doAnnotations(peek);
 		if (peek.getRepeat() != null) {
 			s.printlnInc("<Repeat>");
 			doSwitch(peek.getRepeat());
 			s.printlnDec("</Repeat>");
 		}
 		doSwitch(peek.getVariable());
 		s.printlnDec("</PortPeek>");
 		return s;
 	}
 	
 	@Override
 	public Stream casePortWrite(PortWrite write) {
 		s.printlnInc("<PortWrite  port=\"" + write.getPort().getName() 
 				    + "\" id=\"" + write.getId() 
 				    + "\" outer-scope=\"" + write.getOuter().getId() + "\">");
 		doAnnotations(write);
 		for (Declaration decl : write.getDeclarations()) {
 			doSwitch(decl);
 		}
 	
 		for (Statement stmt : write.getStatements()) {
 			doSwitch(stmt);
 		}
 		
 		for (Expression expr : write.getExpressions()) {
 			doSwitch(expr);
 		}
 
 		if (write.getRepeat() != null) {
 			s.printlnInc("<Repeat>");
 			doSwitch(write.getRepeat());
 			s.printlnDec("</Repeat>");
 		}
 		
 		s.printlnDec("</PortWrite>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeInt(TypeInt type) {
 		if (type.getSize() != null) {
 			s.printlnInc("<Type kind=\"int\">");
 			doSwitch(type.getSize());
 			s.printlnDec("</Type>");
 		} else {
 			s.println("<Type kind=\"int\"/>");
 		}
 
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeUint(TypeUint type) {
 		if (type.getSize() != null) {
 			s.printlnInc("<Type kind=\"uint\">");
 			doSwitch(type.getSize());
 			s.printlnDec("</Type>");
 		} else {
 			s.println("<Type kind=\"uint\"/>");
 		}
 		
 		return s;
 	}	
 	
 	@Override
 	public Stream caseTypeBool(TypeBool type) {
 		s.println("<Type kind=\"bool\"/>");
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeFloat(TypeFloat type) {
 		s.println("<Type kind=\"float\"/>");
 
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeString(TypeString object) {
 		s.println("<Type kind=\"string\"/>");
 
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeList(TypeList type) {
 		s.printlnInc("<Type kind=\"List\">");
 		doSwitch(type.getType());
 		if (type.getSize() != null) {
 			doSwitch(type.getSize());
 		}
 		s.printlnDec("</Type>");
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseTypeUser(TypeUser user) {
 		if(debugPrint) {
 			if(!typedeclimports.contains(user.getDeclaration().getId())) {
 				System.err.println("[IrXmlPrinter] Prints user type with non-existing decl " + (user.getDeclaration()instanceof TypeDeclarationImport?"(imported)":"(real)") + " ID " +
 									user.getDeclaration().getId() + " " + user.getDeclaration().getName());
 				//new Throwable().printStackTrace();
 			}
 		}
 		s.println("<Type kind=\"user\" type-declaration-id=\"" + user.getDeclaration().getId() + "\"/>");	
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeRecord(TypeRecord type){		
 		s.printlnInc("<Type kind=\"record\" id=\"" + type.getId() + "\">");
 		doAnnotations(type);
 		for (Variable m : type.getMembers()) {
 			doSwitch(m);
 		}
 		s.printlnDec("</Type>");
 		return s;
 	}
 
 	@Override
 	public Stream caseTypeLambda(TypeLambda lambda) {
 		s.printlnInc("<Type kind=\"lambda\" >");
 		s.printlnInc("<Input>");
 		for (Type t : lambda.getInputTypes()) {
 			doSwitch(t);
 		}
 		s.printlnDec("</Input>");
 		if (lambda.getOutputType() != null) {
 			s.printlnInc("<Output>");
 			doSwitch(lambda.getOutputType());
 			s.printlnDec("</Output>");
 		}
 
 		s.printlnDec("</Type>");	
 		
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeProc(TypeProc proc) {
 		s.printlnInc("<Type kind=\"proc\" >");
 		s.printlnInc("<Input>");
 		for (Type t : proc.getInputTypes()) {
 			doSwitch(t);
 		}
 		s.printlnDec("</Input>");
 		if (!proc.getOutputTypes().isEmpty()) {
 			s.printlnInc("<Output>");
 			for (Type t : proc.getOutputTypes()) {
 				doSwitch(t);
 			}
 			s.printlnDec("</Output>");
 		}
 
 		s.printlnDec("</Type>");	
 		
 		return s;
 	}
 
 	@Override
 	public Stream caseTypeActor(TypeActor type) {
 		s.println("<Type kind=\"actor\" name=\"" + type.getName() + "\""
 				 + " namespace=\"" + Util.packQualifiedName(type.getNamespace()) + "\"/>");
 		return s;
 	}
 	
 	@Override
 	public Stream caseTypeUndef(TypeUndef object) {
 		s.println("<Type kind=\"undef\"/>");
 		
 		return s;
 	}
 	
  	public void doActionScheduler(Actor actor) {
  		Schedule schedule = actor.getSchedule();
  		s.printlnInc("<Schedule>");
 
  		s.println("<InitialState name=\"" + actor.getSchedule().getInitialState().getName() + "\"/>"); 
 
  		for (State state : schedule.getStates()) {
  			PriorityGraph graph = (PriorityGraph) state.getPriorityGraph();
  			s.printlnInc("<State name=\"" + state.getName() + "\">");
  			
  			s.printlnInc("<PriorityGraph kind=\"local\">");
  			for (PriorityGraph.Vertex vertex : graph.getVertices()) {
  	 			s.println("<Vertex action=\"" + vertex.getAction().getId() + "\"/>");
  	 		}
  			
  			for (PriorityGraph.Vertex vertex : graph.getVertices()) {
  	 			for (PriorityGraph.Edge edge : vertex.getOutgoingEdges()) { 	
  	 				s.println("<Edge source=\"" + edge.getSource().getAction().getId() 
  							  + "\" target=\"" + edge.getTarget().getAction().getId() + "\"/>");
  	 			}
  	 	 	}
  			
  			@SuppressWarnings("unchecked")
 			Map<Action, String> map = (Map<Action, String>) state.getAction2TargetMap();
 			for (Action action : map.keySet()) {
 	 			s.println("<TargetState action=\"" + action.getId() + "\" target=\"" + map.get(action) + "\"/>");
 			}
  			
  	 		s.printlnDec("</PriorityGraph>"); 
  	 		
  			s.printlnDec("</State>");
  		}
  		
  		for (Action action : schedule.getFreeRunners()) {
  			s.println("<FreeRunner action=\"" + action.getId() + "\" />");
  		}
  		s.printlnInc("<PriorityGraph kind=\"global\">");
  		
  		PriorityGraph graph = (PriorityGraph) schedule.getPriorityGraph();
 		for (PriorityGraph.Vertex vertex : graph.getVertices()) {
  			s.println("<Vertex action=\"" + vertex.getAction().getId() + "\"/>");
  		}
 		
 		for (PriorityGraph.Vertex vertex : graph.getVertices()) {
  			for (PriorityGraph.Edge edge : vertex.getOutgoingEdges()) { 	
  				s.println("<Edge source=\"" + edge.getSource().getAction().getId() 
 						  + "\" target=\"" + edge.getTarget().getAction().getId() + "\"/>");
  			}
  	 	}
 
  		s.printlnDec("</PriorityGraph>"); 		
  		s.printlnDec("</Schedule>");
  	}
 
 	private void doAnnotations(Node node) {
 		for (Annotation annotation : node.getAnnotations()) {
 			if (annotation.getArguments().isEmpty()) {
 				s.println("<Annotation name=\"" + annotation.getName() + "\"/>");
 			} else {
 				s.printlnInc("<Annotation name=\"" + annotation.getName() + "\">");
 				for (AnnotationArgument arg : annotation.getArguments()) {
 					s.println("<AnnotationArgument id=\"" + arg.getId() + "\" value=\"" + arg.getValue() + "\"/>");
 				}
 				s.printlnDec("</Annotation>");
 			}
 		}		
 	}
 	
 }

 /*
  * Copyright 2010-2012 napile.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.asm.io.xml.out;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.asm.Modifier;
 import org.napile.asm.io.AsmWriter;
 import org.napile.asm.io.AsmWriterOption;
 import org.napile.asm.io.xml.InstructionNameUtil;
 import org.napile.asm.resolve.name.FqName;
 import org.napile.asm.tree.members.*;
 import org.napile.asm.tree.members.bytecode.Instruction;
 import org.napile.asm.tree.members.bytecode.InstructionInCodePosition;
 import org.napile.asm.tree.members.bytecode.InstructionVisitor;
 import org.napile.asm.tree.members.bytecode.MethodRef;
 import org.napile.asm.tree.members.bytecode.VariableRef;
 import org.napile.asm.tree.members.bytecode.impl.*;
 import org.napile.asm.tree.members.bytecode.tryCatch.CatchBlock;
 import org.napile.asm.tree.members.bytecode.tryCatch.TryCatchBlockNode;
 import org.napile.asm.tree.members.types.TypeNode;
 import org.napile.asm.tree.members.types.constructors.ClassTypeNode;
 import org.napile.asm.tree.members.types.constructors.MethodTypeNode;
 import org.napile.asm.tree.members.types.constructors.MultiTypeNode;
 import org.napile.asm.tree.members.types.constructors.ThisTypeNode;
 import org.napile.asm.tree.members.types.constructors.TypeParameterValueTypeNode;
 import org.napile.asm.util.IntIntPair;
 import org.napile.asm.util.StringWrapper;
 
 /**
  * @author VISTALL
  * @date 17:51/31.08.12
  */
 public abstract class AbstractAsmXmlWriter<A> extends AsmWriter<Element, Element, A> implements InstructionVisitor<Element, Element>
 {
 	protected Document document;
 	protected FqName fqName;
 
 	@Override
 	protected void start()
 	{
 		document = DocumentHelper.createDocument();
 	}
 
 	@Override
 	public Element visitAnnotationNode(AnnotationNode annotationNode, Element o)
 	{
 		final Element temp = o.addElement("annotation");
 		visitCode(temp, annotationNode.code);
 
 		if(annotationNode.parameters.length > 0)
 		{
 			Element parametersElement = o.addElement("parameters");
 
 			for(String p : annotationNode.parameters)
 				parametersElement.addElement("parameter").setText(p);
 		}
 		return temp;
 	}
 
 	@Override
 	public Element visitClassNode(ClassNode classNode, Element a2)
 	{
 		Element element;
 		if(a2 == null)
 		{
 			element = document.addElement("class");
 			fqName = classNode.name;
 		}
 		else
 			element = a2.addElement("class");
 
 		element.addAttribute("version", String.valueOf(langVersion.ordinal()));
 		element.addAttribute("name", classNode.name.getFqName());
 
 		addMemberElements(element, classNode);
 
 		ifNotEmptyAdd(classNode.annotations, "annotations", element);
 
 		ifNotEmptyAdd(classNode.supers, "extends", element);
 
 		for(AbstractMemberNode memberNode : classNode.getMembers())
 			memberNode.accept(this, element);
 		return element;
 	}
 
 	@Override
 	public Element visitMethodNode(MethodNode methodNode, Element a2)
 	{
 		final Element temp = a2.addElement("method");
 		temp.addAttribute("name", methodNode.name.getIdentifier());
 
 		addMemberElements(temp, methodNode);
 
 		ifNotEmptyAdd(methodNode.annotations, "annotations", temp);
 
 		methodNode.returnType.accept(this, temp.addElement("return_type"));
 
 		ifNotEmptyAdd(methodNode.parameters, "parameters", temp);
 
 		visitCode(temp, methodNode.code);
 		return temp;
 	}
 
 	@Override
 	public Element visitMacroNode(MacroNode macroNode, Element a2)
 	{
 		final Element temp = a2.addElement("macro");
 		temp.addAttribute("name", macroNode.name.getIdentifier());
 
 		addMemberElements(temp, macroNode);
 
 		ifNotEmptyAdd(macroNode.annotations, "annotations", temp);
 
 		macroNode.returnType.accept(this, temp.addElement("return_type"));
 
 		ifNotEmptyAdd(macroNode.parameters, "parameters", temp);
 
 		visitCode(temp, macroNode.code);
 		return temp;
 	}
 
 	private void visitCode(@NotNull Element temp, @Nullable CodeInfo code)
 	{
 		if(code == null)
 			return;
 
 		Element parent = temp.addElement("code");
 		parent.addAttribute("max-locals", String.valueOf(code.maxLocals));
 
 		if(!code.instructions.isEmpty())
 		{
 			Element instructions = parent.addElement("instructions");
 
 			int i = 0;
 			for(Instruction instruction : code.instructions)
 			{
 				Element e = instruction.accept(this, instructions);
 
 				if(hasOption(AsmWriterOption.INSTRUCTION_INDEX_IN_COMMENT))
 					e.addComment(String.valueOf(i ++));
 			}
 		}
 
 		if(!code.tryCatchBlockNodes.isEmpty())
 			ifNotEmptyAdd(code.tryCatchBlockNodes, "try-catch-block", parent.addElement("try-catch-blocks"));
 	}
 
 	@Override
 	public Element visitVariableNode(VariableNode variableNode, Element a2)
 	{
 		final Element temp = a2.addElement("variable");
 		temp.addAttribute("name", variableNode.name.getIdentifier());
 
 		addMemberElements(temp, variableNode);
 
 		ifNotEmptyAdd(variableNode.annotations, "annotations", temp);
 
 		variableNode.returnType.accept(this, temp.addElement("return_type"));
 		return temp;
 	}
 
 	@Override
 	public Element visitMethodParameterNode(MethodParameterNode methodParameterNode, Element element)
 	{
 		final Element temp = element.addElement("parameter");
 		temp.addAttribute("name", methodParameterNode.name.getIdentifier());
 
 		addMemberElements(temp, methodParameterNode);
 
 		ifNotEmptyAdd(methodParameterNode.annotations, "annotations", element);
 
 		methodParameterNode.returnType.accept(this, temp);
 
 		if(methodParameterNode.defaultValue != null)
 			temp.addElement("default-value").setText(methodParameterNode.defaultValue);
 		return temp;
 	}
 
 	@Override
 	public Element visitTypeParameter(TypeParameterNode typeParameterNode, Element a2)
 	{
		Element temp = a2.addElement("type-parameter");
 		temp.addAttribute("name", typeParameterNode.name.getIdentifier());
 
 		ifNotEmptyAdd(typeParameterNode.supers, "extends", temp);
 
 		for(List<MethodParameterNode> p : typeParameterNode.constructors)
			ifNotEmptyAdd(p, "parameters", temp.addElement("constructor"));
 		return temp;
 	}
 
 	@Override
 	public Element visitTypeNode(TypeNode typeNode, Element a2)
 	{
 		final Element element = a2.addElement("type");
 		element.addAttribute("nullable", String.valueOf(typeNode.nullable));
 
 		ifNotEmptyAdd(typeNode.annotations, "annotations", element);
 
 		typeNode.typeConstructorNode.accept(this, element);
 
 		ifNotEmptyAdd(typeNode.arguments, "type_arguments", element);
 		return element;
 	}
 
 	@Override
 	public Element visitClassTypeNode(ClassTypeNode classTypeNode, Element a2)
 	{
 		final Element temp = a2.addElement("class_type");
 		temp.addAttribute("name", classTypeNode.className.getFqName());
 		return temp;
 	}
 
 	@Override
 	public Element visitThisTypeNode(ThisTypeNode thisTypeNode, Element a2)
 	{
 		return a2.addElement("this_type");
 	}
 
 	@Override
 	public Element visitMethodTypeNode(MethodTypeNode methodTypeNode, Element a2)
 	{
 		final Element temp = a2.addElement("method_type");
 
 		methodTypeNode.returnType.accept(this, temp.addElement("return_type"));
 
 		ifNotEmptyAdd(methodTypeNode.parameters, "parameters", temp);
 
 		return temp;
 	}
 
 	@Override
 	public Element visitMultiTypeNode(MultiTypeNode multiTypeNode, Element a2)
 	{
 		final Element temp = a2.addElement("multi_type");
 
 		for(VariableNode variableNode : multiTypeNode.variables)
 			variableNode.accept(this, temp);
 		return temp;
 	}
 
 	@Override
 	public Element visitTypeParameterValueTypeNode(TypeParameterValueTypeNode typeParameterValueTypeNode, Element a2)
 	{
 		final Element temp = a2.addElement("type_parameter_value_type");
 		temp.addAttribute("name", typeParameterValueTypeNode.name.getIdentifier());
 		return temp;
 	}
 
 	@Override
 	public Element visitTryCatchBlockNode(TryCatchBlockNode tryCatchBlockNode, Element arg)
 	{
 		Element tryElement = arg.addElement("try");
 		tryElement.addAttribute("start_index", String.valueOf(tryCatchBlockNode.tryBlock.startIndex));
 		tryElement.addAttribute("end_index", String.valueOf(tryCatchBlockNode.tryBlock.endIndex));
 
 		for(CatchBlock catchBlock : tryCatchBlockNode.catchBlocks)
 		{
 			Element catchElement = arg.addElement("catch");
 			catchElement.addAttribute("start_index", String.valueOf(catchBlock.startIndex));
 			catchElement.addAttribute("end_index", String.valueOf(catchBlock.endIndex));
 			catchElement.addAttribute("variable_index", String.valueOf(catchBlock.variableIndex));
 
 			catchBlock.exception.accept(this, catchElement);
 		}
 		return arg;
 	}
 
 	private void ifNotEmptyAdd(Collection<? extends Node> list, String name, Element parent)
 	{
 		if(list.size() > 0)
 		{
 			Element p = parent.addElement(name);
 			for(Node asmNode : list)
 				asmNode.accept(this, p);
 		}
 	}
 
 	private void addMemberElements(Element parent, AbstractMemberNode<?> m)
 	{
 		if(m.modifiers.length > 0)
 		{
 			Element tag = parent.addElement("modifiers");
 			for(Modifier modifier : m.modifiers)
 				tag.addElement(modifier.name().toLowerCase());
 		}
 
		ifNotEmptyAdd(m.typeParameters, "type-parameters", parent);
 	}
 
 	//-----------------------------------------------------------------------------
 
 	@Override
 	public Element visitLocalGet(LocalGetInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.varIndex));
 		return element;
 	}
 
 	@Override
 	public Element visitDup(DupInstruction instruction, Element a)
 	{
 		return addInstructionToElement(a, instruction);
 	}
 
 	@Override
 	public Element visitPutAnonym(PutAnonymInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		Element require = element.addElement("require");
 		for(IntIntPair pair : instruction.require)
 		{
 			Element localElement = require.addElement("local");
 			localElement.addAttribute("from", String.valueOf(pair.a));
 			localElement.addAttribute("to", String.valueOf(pair.b));
 		}
 		visitCode(element, instruction.code);
 		return element;
 	}
 
 	@Override
 	public Element visitDup1x1(Dup1x1Instruction instruction, Element a)
 	{
 		return addInstructionToElement(a, instruction);
 	}
 
 	@Override
 	public Element visitLocalPut(LocalPutInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.varIndex));
 		return element;
 	}
 
 	@Override
 	public Element visitNewObject(NewObjectInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		instruction.value.accept(this, element);
 		ifNotEmptyAdd(instruction.parameters, "parameters", element);
 		return element;
 	}
 
 	@Override
 	public Element visitNewByte(NewByteInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewShort(NewShortInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewInt(NewIntInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewLong(NewLongInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewFloat(NewFloatInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewDouble(NewDoubleInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewChar(NewCharInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitNewString(NewStringInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", StringWrapper.wrapToXml(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitReturn(ReturnInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.count));
 		return element;
 	}
 
 	@Override
 	public Element visitThrow(ThrowInstruction instruction, Element a)
 	{
 		return addInstructionToElement(a, instruction);
 	}
 
 	@Override
 	public Element visitSwap(SwapInstruction instruction, Element a)
 	{
 		return addInstructionToElement(a, instruction);
 	}
 
 	@Override
 	public Element visitPop(PopInstruction instruction, Element a)
 	{
 		return addInstructionToElement(a, instruction);
 	}
 
 	@Override
 	public Element visitJumpIf(JumpIfInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitJump(JumpInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		element.addAttribute("val", String.valueOf(instruction.value));
 		return element;
 	}
 
 	@Override
 	public Element visitTypeOf(TypeOfInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		instruction.value.accept(this, element);
 		return element;
 	}
 
 	@Override
 	public Element visitClassOf(ClassOfInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		instruction.value.accept(this, element);
 		return element;
 	}
 
 	@Override
 	public Element visitIs(IsInstruction instruction, Element a)
 	{
 		Element element = addInstructionToElement(a, instruction);
 		instruction.value.accept(this, element);
 		return element;
 	}
 
 	@Override
 	public Element visitInvokeStatic(InvokeStaticInstruction instruction, Element a)
 	{
 		return visitInvoke(instruction, a);
 	}
 
 	@Override
 	public Element visitInvokeSpecial(InvokeSpecialInstruction instruction, Element a)
 	{
 		return visitInvoke(instruction, a);
 	}
 
 	@Override
 	public Element visitInvokeVirtual(InvokeVirtualInstruction instruction, Element a)
 	{
 		return visitInvoke(instruction, a);
 	}
 
 	@Override
 	public Element visitInvokeAnonym(InvokeAnonymInstruction instruction, Element a)
 	{
 		return visitInvoke(instruction, a);
 	}
 
 	private Element visitInvoke(InvokeInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		if(instruction.nullable)
 			temp.addElement("nullable");
 
 		Element temp2 = temp.addElement("method");
 		MethodRef methodRef = instruction.methodRef;
 		// anonym not need name
 		if(!(instruction instanceof InvokeAnonymInstruction))
 			temp2.addAttribute("name", methodRef.method.toString());
 
 		methodRef.returnType.accept(this, temp2.addElement("return_type"));
 		ifNotEmptyAdd(methodRef.parameters, "parameters", temp2);
 		ifNotEmptyAdd(methodRef.typeArguments, "type_arguments", temp2);
 		return temp;
 	}
 
 	@Override
 	public Element visitMacroJump(MacroJumpInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		Element temp2 = temp.addElement("method");
 		MethodRef methodRef = instruction.methodRef;
 		temp2.addAttribute("name", methodRef.method.toString());
 		methodRef.returnType.accept(this, temp2.addElement("return_type"));
 		ifNotEmptyAdd(methodRef.parameters, "parameters", temp2);
 		ifNotEmptyAdd(methodRef.typeArguments, "type_arguments", temp2);
 		return temp;
 	}
 
 	@Override
 	public Element visitMacroStaticJump(MacroStaticJumpInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		Element temp2 = temp.addElement("method");
 		MethodRef methodRef = instruction.methodRef;
 		temp2.addAttribute("name", methodRef.method.toString());
 		methodRef.returnType.accept(this, temp2.addElement("return_type"));
 		ifNotEmptyAdd(methodRef.parameters, "parameters", temp2);
 		ifNotEmptyAdd(methodRef.typeArguments, "type_arguments", temp2);
 		return temp;
 	}
 
 	@Override
 	public Element visitPutToVariable(PutToVariableInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		Element temp2 = temp.addElement("variable");
 		VariableRef variableRef = instruction.variableRef;
 		temp2.addAttribute("name", variableRef.variable.toString());
 		variableRef.returnType.accept(this, temp2);
 		return temp;
 	}
 
 	@Override
 	public Element visitPutToStaticVariable(PutToStaticVariableInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		Element temp2 = temp.addElement("variable");
 		VariableRef variableRef = instruction.variableRef;
 		temp2.addAttribute("name", variableRef.variable.toString());
 		variableRef.returnType.accept(this, temp2);
 		return temp;
 	}
 
 	@Override
 	public Element visitGetVariable(GetVariableInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		Element temp2 = temp.addElement("variable");
 		VariableRef variableRef = instruction.variableRef;
 		temp2.addAttribute("name", variableRef.variable.toString());
 		variableRef.returnType.accept(this, temp2);
 		return temp;
 	}
 
 	@Override
 	public Element visitGetStaticVariable(GetStaticVariableInstruction instruction, Element a)
 	{
 		final Element temp = addInstructionToElement(a, instruction);
 		Element temp2 = temp.addElement("variable");
 		VariableRef variableRef = instruction.variableRef;
 		temp2.addAttribute("name", variableRef.variable.toString());
 		variableRef.returnType.accept(this, temp2);
 		return temp;
 	}
 
 	private Element addInstructionToElement(Element element, Instruction instruction)
 	{
 		Element instructionElement = element.addElement(InstructionNameUtil.toXmlTag(instruction.getClass()));
 		if(instruction.position != InstructionInCodePosition.EMPTY_POSITION)
 		{
 			Element positionElement = instructionElement.addElement("position");
 			positionElement.addAttribute("line", String.valueOf(instruction.position.getLine()));
 			positionElement.addAttribute("column", String.valueOf(instruction.position.getColumn()));
 			positionElement.addAttribute("file", String.valueOf(instruction.position.getFile()));
 		}
 		return instructionElement;
 	}
 }

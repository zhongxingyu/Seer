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
 
 package org.napile.asm.io.xml.in;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 import org.jetbrains.annotations.NotNull;
 import org.napile.asm.Modifier;
 import org.napile.asm.io.xml.InstructionNameUtil;
 import org.napile.asm.resolve.name.FqName;
 import org.napile.asm.resolve.name.Name;
 import org.napile.asm.tree.members.*;
 import org.napile.asm.tree.members.bytecode.Instruction;
 import org.napile.asm.tree.members.bytecode.InstructionInCodePosition;
 import org.napile.asm.tree.members.bytecode.MethodRef;
 import org.napile.asm.tree.members.bytecode.VariableRef;
 import org.napile.asm.tree.members.bytecode.impl.*;
 import org.napile.asm.tree.members.bytecode.tryCatch.CatchBlock;
 import org.napile.asm.tree.members.bytecode.tryCatch.TryBlock;
 import org.napile.asm.tree.members.bytecode.tryCatch.TryCatchBlockNode;
 import org.napile.asm.tree.members.types.TypeNode;
 import org.napile.asm.tree.members.types.constructors.ClassTypeNode;
 import org.napile.asm.tree.members.types.constructors.MethodTypeNode;
 import org.napile.asm.tree.members.types.constructors.MultiTypeNode;
 import org.napile.asm.tree.members.types.constructors.ThisTypeNode;
 import org.napile.asm.tree.members.types.constructors.TypeConstructorNode;
 import org.napile.asm.tree.members.types.constructors.TypeParameterValueTypeNode;
 import org.napile.asm.util.IntIntPair;
 import org.napile.asm.util.StringWrapper;
 import com.intellij.util.ArrayUtil;
 
 /**
  * @author VISTALL
  * @date 0:13/21.09.12
  */
 public class AsmXmlFileReader
 {
 	private final SAXReader reader = new SAXReader(false);
 
 	public ClassNode read(@NotNull Reader r) throws IOException
 	{
 		try
 		{
 			return readClass(reader.read(r).getRootElement());
 		}
 		catch(DocumentException e)
 		{
 			throw new IOException(e);
 		}
 	}
 
 	public ClassNode read(@NotNull InputStream stream) throws IOException
 	{
 		try
 		{
 			return readClass(reader.read(stream).getRootElement());
 		}
 		catch(DocumentException e)
 		{
 			throw new IOException(e);
 		}
 	}
 
 	private ClassNode readClass(Element element)
 	{
 		Element classElement = throwIfNotExpected(element, "class");
 
 		FqName fqName = new FqName(classElement.attributeValue("name"));
 		Modifier[] modifiers = readModifiers(classElement);
 		ClassNode classNode = new ClassNode(modifiers, fqName);
 
 		readSupers(classElement, classNode.supers);
 
 		readTypeParameters(classElement, classNode);
 
 		readAnnotations(classElement, classNode);
 
 		for(Element child : classElement.elements())
 		{
 			AbstractMemberNode<?> node = null;
 
 			if("variable".equals(child.getName()))
 				node = readVariable(child);
 			else if("method".equals(child.getName()))
 				node = readMethodOrMacro(child, new MethodNode(readModifiers(child), Name.identifier(child.attributeValue("name")), readType(child.element("return_type").element("type"))));
 			else if("macro".equals(child.getName()))
 				node = readMethodOrMacro(child, new MacroNode(readModifiers(child), Name.identifier(child.attributeValue("name")), readType(child.element("return_type").element("type"))));
 			else if("class".equals(child.getName()))
 				node = readClass(child);
 
 			if(node != null)
 				classNode.addMember(node);
 		}
 
 		return classNode;
 	}
 
 	private VariableNode readVariable(@NotNull Element child)
 	{
 		VariableNode variableNode = new VariableNode(readModifiers(child), Name.identifier(child.attributeValue("name")), readType(child.element("return_type").element("type")));
 
 		readTypeParameters(child, variableNode);
 
 		readAnnotations(child, variableNode);
 
 		return variableNode;
 	}
 
 	private List<MethodParameterNode> readParameters(@NotNull Element parent, List<MethodParameterNode> parameters)
 	{
 		Element parametersElement = parent.element("parameters");
 		if(parametersElement != null)
 			for(Element parameterElement : parametersElement.elements())
 			{
 				Element defaultValue = parameterElement.element("default-value");
 
 				parameters.add(new MethodParameterNode(readModifiers(parameterElement), Name.identifier(parameterElement.attributeValue("name")), readType(parameterElement.element("type")), defaultValue != null ? defaultValue.getText() : null));
 
 			}
 		return parameters;
 	}
 
 	private MethodNode readMethodOrMacro(@NotNull Element parent, @NotNull MethodNode methodNode)
 	{
 		readTypeParameters(parent, methodNode);
 
 		readAnnotations(parent, methodNode);
 
 		readParameters(parent, methodNode.parameters);
 
 		methodNode.code = readCode(parent);
 
 		return methodNode;
 	}
 
 	private CodeInfo readCode(@NotNull Element p)
 	{
 		Element codeElement = p.element("code");
 		if(codeElement != null)
 		{
 			CodeInfo codeInfo = new CodeInfo();
 			codeInfo.maxLocals = Integer.parseInt(codeElement.attributeValue("max-locals"));
 
 			Element instructionsElement = codeElement.element("instructions");
 			if(instructionsElement != null)
 				for(Element instructionElement : instructionsElement.elements())
 				{
 					Instruction instruction = null;
 					Class<?> clazz = InstructionNameUtil.fromXmlTag(instructionElement.getName());
 					String instructionName = instructionElement.getName();
 
 					if(clazz == NewByteInstruction.class)
 						instruction = new NewByteInstruction(Byte.parseByte(instructionElement.attributeValue("val")));
 					else if(clazz == NewShortInstruction.class)
 						instruction = new NewShortInstruction(Short.parseShort(instructionElement.attributeValue("val")));
 					else if(clazz == NewIntInstruction.class)
 						instruction = new NewIntInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 					else if(clazz == NewLongInstruction.class)
 						instruction = new NewLongInstruction(Long.parseLong(instructionElement.attributeValue("val")));
 					else if(clazz == NewCharInstruction.class)
 						instruction = new NewCharInstruction(instructionElement.attributeValue("val").charAt(0));
 					else if(clazz == NewFloatInstruction.class)
 						instruction = new NewFloatInstruction(Float.parseFloat(instructionElement.attributeValue("val")));
 					else if(clazz == NewDoubleInstruction.class)
 						instruction = new NewDoubleInstruction(Double.parseDouble(instructionElement.attributeValue("val")));
 					else if(clazz == NewStringInstruction.class)
 						instruction = new NewStringInstruction(StringWrapper.unwrapFromXml(instructionElement.attributeValue("val")));
 					else if(clazz == NewObjectInstruction.class)
 					{
 						List<TypeNode> parameters = new ArrayList<TypeNode>(0);
 						Element typeArgumentsElement = instructionElement.element("parameters");
 						if(typeArgumentsElement != null)
 							for(Element typeArgumentElement : typeArgumentsElement.elements())
 								parameters.add(readType(typeArgumentElement));
 						instruction = new NewObjectInstruction(readType(instructionElement.element("type")), parameters);
 					}
 					else if(clazz == LocalPutInstruction.class)
 						instruction = new LocalPutInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 					else if(clazz == LocalGetInstruction.class)
 						instruction = new LocalGetInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 					else if(clazz == DupInstruction.class)
 						instruction = new DupInstruction();
 					else if(clazz == Dup1x1Instruction.class)
 						instruction = new Dup1x1Instruction();
 					else if(clazz == PopInstruction.class)
 						instruction = new PopInstruction();
 					else if(clazz == SwapInstruction.class)
 						instruction = new SwapInstruction();
 					else if(clazz == ReturnInstruction.class)
 						instruction = new ReturnInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 					else if(clazz == ThrowInstruction.class)
 						instruction = new ThrowInstruction();
 					else if(clazz == InvokeStaticInstruction.class)
 						instruction = new InvokeStaticInstruction(readMethodRef(instructionElement), instructionElement.element("nullable") != null);
 					else if(clazz == InvokeSpecialInstruction.class)
 						instruction = new InvokeSpecialInstruction(readMethodRef(instructionElement), instructionElement.element("nullable") != null);
 					else if(clazz == InvokeVirtualInstruction.class)
 						instruction = new InvokeVirtualInstruction(readMethodRef(instructionElement), instructionElement.element("nullable") != null);
 					else if(clazz == MacroStaticJumpInstruction.class)
 						instruction = new MacroStaticJumpInstruction(readMethodRef(instructionElement));
 					else if(clazz == MacroJumpInstruction.class)
 						instruction = new MacroJumpInstruction(readMethodRef(instructionElement));
 					else if(clazz == InvokeAnonymInstruction.class)
 					{
 						Element element = instructionElement.element("method");
 
 						Element returnElement = element.element("return_type");
 
 						TypeNode returnType = readType(returnElement.element("type"));
 
 						List<TypeNode> typeArguments = new ArrayList<TypeNode>();
 						Element typeArgumentsElement = element.element("type_arguments");
 						if(typeArgumentsElement != null)
 							for(Element typeArgumentElement : typeArgumentsElement.elements())
 								typeArguments.add(readType(typeArgumentElement));
 
 						instruction = new InvokeAnonymInstruction(readParameters(element, new ArrayList<MethodParameterNode>()), typeArguments, returnType, instructionElement.element("nullable") != null);
 					}
 					else if(clazz == PutToVariableInstruction.class)
 						instruction = new PutToVariableInstruction(readVariableRef(instructionElement));
 					else if(clazz == PutToStaticVariableInstruction.class)
 						instruction = new PutToStaticVariableInstruction(readVariableRef(instructionElement));
 					else if(clazz == GetVariableInstruction.class)
 						instruction = new GetVariableInstruction(readVariableRef(instructionElement));
 					else if(clazz == GetStaticVariableInstruction.class)
 						instruction = new GetStaticVariableInstruction(readVariableRef(instructionElement));
 					else if(clazz == JumpIfInstruction.class)
 						instruction = new JumpIfInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 					else if(clazz == JumpInstruction.class)
 						instruction = new JumpInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 					else if(clazz == ClassOfInstruction.class)
 						instruction = new ClassOfInstruction(readType(instructionElement.element("type")));
 					else if(clazz == TypeOfInstruction.class)
 						instruction = new TypeOfInstruction(readType(instructionElement.element("type")));
 					else if(clazz == IsInstruction.class)
 						instruction = new IsInstruction(readType(instructionElement.element("type")));
 					else if(clazz == PutAnonymInstruction.class)
 					{
 						Element require = instructionElement.element("require");
 						List<IntIntPair> requireList = new ArrayList<IntIntPair>();
 						for(Element e : require.elements())
 						{
 							requireList.add(new IntIntPair(Integer.parseInt(e.attributeValue("from")), Integer.parseInt(e.attributeValue("to"))));
 						}
 						instruction = new PutAnonymInstruction(requireList, readCode(instructionElement));
 					}
 
 					if(instruction.getClass() != clazz)
 						throw new IllegalArgumentException("Wrong element clazz and object clazz: " + clazz.getSimpleName() + "/" + instruction.getClass().getSimpleName());
 					if(instruction != null)
 					{
 						Element positionElement = instructionElement.element("position");
 						if(positionElement != null)
 						{
 							instruction.position = new InstructionInCodePosition(positionElement.attributeValue("file"), Integer.parseInt(positionElement.attributeValue("line")), Integer.parseInt(positionElement.attributeValue("column")));
 						}
 						codeInfo.instructions.add(instruction);
 					}
 					else
 						throw new IllegalArgumentException("Unknown instruction: " + instructionName);
 				}
 
 			Element tryCatchBlocks = codeElement.element("try_catch_blocks");
 			if(tryCatchBlocks != null)
 			{
 				for(Element tryCatchBlockElement : tryCatchBlocks.elements())
 				{
 					TryBlock tryBlock = null;
 					List<CatchBlock> catchBlocks = new ArrayList<CatchBlock>(0);
 
 					for(Element childTryBlock : tryCatchBlockElement.elements())
 					{
 						String temp = childTryBlock.getName();
 						if("try".equals(temp))
 							tryBlock = new TryBlock(Integer.parseInt(childTryBlock.attributeValue("start_index")), Integer.parseInt( childTryBlock.attributeValue("end_index")));
 						else if("catch".equals(temp))
 						{
 							int startIndex = Integer.parseInt(childTryBlock.attributeValue("start_index"));
 							int endIndex = Integer.parseInt(childTryBlock.attributeValue("end_index"));
 							int variableIndex = Integer.parseInt(childTryBlock.attributeValue("variable_index"));
 							TypeNode typeNode = readType(childTryBlock.element("type"));
 							catchBlocks.add(new CatchBlock(startIndex, endIndex, variableIndex, typeNode));
 						}
 						else
 							throw new UnsupportedOperationException("Unknown element: " + temp);
 					}
 
 					if(tryBlock == null)
 						throw new UnsupportedOperationException("TryCatch Block cant be without 'try' part");
 
 					codeInfo.tryCatchBlockNodes.add(new TryCatchBlockNode(tryBlock, catchBlocks));
 				}
 			}
 			return codeInfo;
 		}
 
 		return null;
 	}
 
 	private VariableRef readVariableRef(@NotNull Element child)
 	{
 		Element element = child.element("variable");
 		FqName fqName = new FqName(element.attributeValue("name"));
 
 		TypeNode typeNode = readType(element.element("type"));
 		return new VariableRef(fqName, typeNode);
 	}
 
 	private MethodRef readMethodRef(@NotNull Element child)
 	{
 		Element element = child.element("method");
 		FqName fqName = new FqName(element.attributeValue("name"));
 		Element returnElement = element.element("return_type");
 
 		TypeNode returnType = readType(returnElement.element("type"));
 
 		List<TypeNode> typeArguments = new ArrayList<TypeNode>();
 		Element typeArgumentsElement = element.element("type_arguments");
 		if(typeArgumentsElement != null)
 			for(Element typeArgumentElement : typeArgumentsElement.elements())
 				typeArguments.add(readType(typeArgumentElement));
 
 		return new MethodRef(fqName, readParameters(element, new ArrayList<MethodParameterNode>()), typeArguments, returnType);
 	}
 
 	private void readSupers(@NotNull Element element, @NotNull List<TypeNode> list)
 	{
 		Element extendsElement = element.element("extends");
 		if(extendsElement == null)
 			return;
 
 		for(Element child : extendsElement.elements())
 			list.add(readType(child));
 	}
 
 	@NotNull
 	private Modifier[] readModifiers(Element element)
 	{
 		Element modifiersElement = element.element("modifiers");
 		if(modifiersElement == null)
 			return Modifier.EMPTY;
 
 		List<Element> children = modifiersElement.elements();
 		List<Modifier> modifierList = new ArrayList<Modifier>(children.size());
 		for(Element child : children)
 			modifierList.add(Modifier.valueOf(child.getName().toUpperCase()));
 
 		return modifierList.toArray(new Modifier[modifierList.size()]);
 	}
 
 	private void readTypeParameters(@NotNull Element element, @NotNull AbstractMemberNode<?> node)
 	{
		Element typeParametersElement = element.element("type-parameters");
 		if(typeParametersElement == null)
 			return;
 
 		for(Element child : typeParametersElement.elements())
 		{
			child = throwIfNotExpected(child, "type-parameter");
 
 			TypeParameterNode typeParameterNode = new TypeParameterNode(Name.identifier(child.attributeValue("name")));
 
 			readSupers(child, typeParameterNode.supers);
 
			for(Element constructorElement : child.elements("constructor"))
			{
				typeParameterNode.constructors.add(readParameters(constructorElement, new ArrayList<MethodParameterNode>()));
			}
 
 			node.typeParameters.add(typeParameterNode);
 		}
 	}
 
 	private void readAnnotations(@NotNull Element element, @NotNull AnnotableNode<?> node)
 	{
 		Element annotationsElement = element.element("annotations");
 		if(annotationsElement == null)
 			return;
 
 		for(Element child : annotationsElement.elements())
 		{
 			child = throwIfNotExpected(child, "annotation");
 
 			String[] array = ArrayUtil.EMPTY_STRING_ARRAY;
 			Element parametersElement = child.element("parameters");
 			if(parametersElement != null)
 			{
 				List<String> list = new ArrayList<String>();
 				for(Element e : parametersElement.elements())
 					list.add(e.getText());
 				array = list.toArray(new String[list.size()]);
 			}
 
 			AnnotationNode annotationNode = new AnnotationNode(readCode(child), array);
 
 
 			node.annotations.add(annotationNode);
 		}
 	}
 
 	private TypeNode readType(@NotNull Element element)
 	{
 		element = throwIfNotExpected(element, "type");
 
 		boolean nullable = Boolean.parseBoolean(element.attributeValue("nullable"));
 		TypeConstructorNode typeConstructorNode = null;
 		Element constructorElement = element.element("class_type");
 		if(constructorElement != null)
 			typeConstructorNode = new ClassTypeNode(new FqName(constructorElement.attributeValue("name")));
 		else if((constructorElement = element.element("this_type")) != null)
 			typeConstructorNode = new ThisTypeNode();
 		else if((constructorElement = element.element("method_type")) != null)
 		{
 			Element returnElement = constructorElement.element("return_type");
 
 			typeConstructorNode = new MethodTypeNode();
 			((MethodTypeNode) typeConstructorNode).returnType = readType(returnElement.element("type"));
 			readParameters(constructorElement, ((MethodTypeNode) typeConstructorNode).parameters);
 		}
 		else if((constructorElement = element.element("multi_type")) != null)
 		{
 			typeConstructorNode = new MultiTypeNode();
 			for(Element variableElement : constructorElement.elements())
 				((MultiTypeNode) typeConstructorNode) .variables.add(readVariable(variableElement));
 		}
 		else if((constructorElement = element.element("type_parameter_value_type")) != null)
 			typeConstructorNode = new TypeParameterValueTypeNode(Name.identifier(constructorElement.attributeValue("name")));
 
 		if(typeConstructorNode == null)
 			throw new IllegalArgumentException("Unknown constructor of type: " + element.elements());
 
 		TypeNode typeNode = new TypeNode(nullable, typeConstructorNode);
 
 		Element argumentsElement = element.element("type_arguments");
 		if(argumentsElement != null)
 			for(Element child : argumentsElement.elements())
 				typeNode.arguments.add(readType(child));
 
 		readAnnotations(element, typeNode);
 
 		return typeNode;
 	}
 
 	@NotNull
 	private Element throwIfNotExpected(@NotNull Element element, @NotNull String name)
 	{
 		if(!element.getName().equals(name))
 			throw new IllegalArgumentException("Invalid xml element. Expecting: " + name + ". Found: " + element.getName());
 		return element;
 	}
 }

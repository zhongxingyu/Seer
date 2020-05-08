 package com.rx201.dx.translator;
 
 import java.util.EnumSet;
 
 import org.jf.dexlib.Code.Analysis.ClassPath;
 import org.jf.dexlib.Code.Analysis.RegisterType;
 import org.jf.dexlib.Code.Analysis.ValidationException;
 import org.jf.dexlib.Code.Analysis.RegisterType.Category;
 
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareFloat;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Convert;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertFromWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertToWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitchData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_SparseSwitchData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOpWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Unknown;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_ConvertWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_FilledNewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetInternalClassAnnotation;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetInternalMethodAnnotation;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetMethodCaller;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetQueryTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetServiceTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintInteger;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintIntegerConst;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintString;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke.DexPseudoinstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 
 import com.rx201.dx.translator.util.DexRegisterHelper;
 
 public class DexInstructionAnalyzer implements DexInstructionVisitor{
 
     private static final EnumSet<RegisterType.Category> BooleanCategories = EnumSet.of(
             RegisterType.Category.Null,
             RegisterType.Category.One,
             RegisterType.Category.Boolean);
 
 	private AnalyzedDexInstruction instruction;
 	private DexCodeAnalyzer analyzer;
 	
 	public DexInstructionAnalyzer(DexCodeAnalyzer analyzer) {
 		this.analyzer = analyzer;
 	}
 	
 	public void setAnalyzedInstruction(AnalyzedDexInstruction i) {
 		this.instruction = i;
 	}
 	
 	private void setPostRegisterType(AnalyzedDexInstruction inst, DexRegister registerNumber, 
 			RegisterType registerType) {
 		analyzer.setPostRegisterTypeAndPropagateChanges(inst, registerNumber, registerType);
 	}
 
     private void setDestinationRegisterType(AnalyzedDexInstruction analyzedInstruction, 
     		RegisterType registerType) {
     	setPostRegisterType(analyzedInstruction, analyzedInstruction.getDestinationRegister(), registerType);
 	}
 	
     private void setDestinationRegisterType(AnalyzedDexInstruction analyzedInstruction,
             DexRegisterType dexType) {
     	setDestinationRegisterType(analyzedInstruction, DexRegisterTypeHelper.toRegisterType(dexType));
     }
     
     private void setDestinationRegisterType(AnalyzedDexInstruction analyzedInstruction,
             String typeDescriptor) {
     	setDestinationRegisterType(analyzedInstruction, RegisterType.getRegisterTypeForType(typeDescriptor));
     }
 	    
     
 	@Override
 	public void visit(DexInstruction_Nop dexInstruction_Nop) {}
 	
 	private void analyzeMove(DexRegister srcReg) {
 		RegisterType valueType = instruction.getPreRegisterType(srcReg);
 		setDestinationRegisterType(instruction, valueType);
 	}
 
 	@Override
 	public void visit(DexInstruction_Move dexInstruction_Move) {
 		analyzeMove(dexInstruction_Move.getRegFrom());
 //        analyzeMove(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_MoveWide dexInstruction_MoveWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_MoveWide.getRegFrom1(),  dexInstruction_MoveWide.getRegFrom2());
 		assert DexRegisterHelper.isPair(dexInstruction_MoveWide.getRegTo1(),  dexInstruction_MoveWide.getRegTo2());
 		analyzeMove(dexInstruction_MoveWide.getRegFrom1());
 //        analyzeMove(analyzedInstruction);
 
 	}
 	
 	private void analyzeMoveResult(DexRegister srcReg) {
		assert instruction.getPredecessorCount() == 1;
 		
        AnalyzedDexInstruction prevAnalyzedInst = instruction.getPredecessors().get(0);
         
         DexRegisterType resultRegisterType;
         if (prevAnalyzedInst.instruction instanceof DexInstruction_Invoke) {
         	DexInstruction_Invoke i = (DexInstruction_Invoke) prevAnalyzedInst.instruction;
         	resultRegisterType = (DexRegisterType)i.getMethodPrototype().getReturnType();
         } else if (prevAnalyzedInst.instruction instanceof DexInstruction_FilledNewArray) {
         	DexInstruction_FilledNewArray i = (DexInstruction_FilledNewArray)prevAnalyzedInst.instruction;
         	resultRegisterType = i.getArrayType();
         } else {
             throw new ValidationException(instruction.instruction.getOriginalAssembly() + " must occur after an " +
                     "invoke-*/fill-new-array instruction");
         }
         
         setDestinationRegisterType(instruction, resultRegisterType);			
 	}
 	@Override
 	public void visit(DexInstruction_MoveResult dexInstruction_MoveResult) {
 		analyzeMoveResult(dexInstruction_MoveResult.getRegTo());
 //			analyzeMoveResult(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_MoveResultWide dexInstruction_MoveResultWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_MoveResultWide.getRegTo1(),  dexInstruction_MoveResultWide.getRegTo2());
 		analyzeMoveResult(dexInstruction_MoveResultWide.getRegTo1());
 //            analyzeMoveResult(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_MoveException dexInstruction_MoveException) {
 		assert instruction.getPredecessorCount() == 1;
 		DexCodeElement catchElement = instruction.getPredecessors().get(0).auxillaryElement;
 		assert catchElement != null && (catchElement instanceof DexCatch || catchElement instanceof DexCatchAll);
 		String exception = null;
 		if (catchElement instanceof DexCatch)
 			exception = ((DexCatch)catchElement).getExceptionType().getDescriptor();
 		else 
 			exception = "Ljava/lang/Throwable;";
 				
 		setDestinationRegisterType(instruction, RegisterType.getRegisterTypeForType(exception));
 //            analyzeMoveException(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_ReturnVoid dexInstruction_ReturnVoid) {}
 	
 	@Override
 	public void visit(DexInstruction_Return dexInstruction_Return) {}
 	
 	@Override
 	public void visit(DexInstruction_ReturnWide dexInstruction_ReturnWide) {}
 	
 	@Override
 	public void visit(DexInstruction_Const dexInstruction_Const) {
 		long value = dexInstruction_Const.getValue();
 		// TODO: Need more detailed type information, because translator needs it to 
 		// instantiate the appropriate constant object.
 		setDestinationRegisterType(instruction, RegisterType.getRegisterTypeForLiteral(value));
 //            analyzeConst(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_ConstWide dexInstruction_ConstWide) {
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(RegisterType.Category.LongLo, null));
 //            analyzeConstWide(analyzedInstruction);
 	}
 	
 	@Override
 	public void visit(DexInstruction_ConstString dexInstruction_ConstString) {
 		setDestinationRegisterType(instruction, "Ljava/lang/String;");
 //            analyzeConstString(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_ConstClass dexInstruction_ConstClass) {
 		setDestinationRegisterType(instruction, dexInstruction_ConstClass.getValue().getDescriptor());
 //            analyzeConstClass(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_Monitor dexInstruction_Monitor) {}
 	
 	@Override
 	public void visit(DexInstruction_CheckCast dexInstruction_CheckCast) {
 		setDestinationRegisterType(instruction, dexInstruction_CheckCast.getValue().getDescriptor());
 //            analyzeCheckCast(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_InstanceOf dexInstruction_InstanceOf) {
         setDestinationRegisterType(instruction,
                 RegisterType.getRegisterType(RegisterType.Category.Boolean, null));
 //            analyzeInstanceOf(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_ArrayLength dexInstruction_ArrayLength) {
         setDestinationRegisterType(instruction,
                 RegisterType.getRegisterType(RegisterType.Category.Boolean, null));
 //            analyzeArrayLength(analyzedInstruction);
 	}
 	@Override
 	public void visit(DexInstruction_NewInstance dexInstruction_NewInstance) {
         RegisterType destRegisterType = instruction.getPostRegisterType(instruction.getDestinationRegister());
         if (destRegisterType.category != RegisterType.Category.Unknown) {
             assert destRegisterType.category == RegisterType.Category.UninitRef;
             //the post-instruction destination register will only be set if we have already analyzed this instruction
             //at least once. If this is the case, then the uninit reference has already been propagated to all
             //successors and nothing else needs to be done.
             return;
         }
         
         RegisterType classType = RegisterType.getRegisterTypeForType(dexInstruction_NewInstance.getValue().getDescriptor());
 
         setDestinationRegisterType(instruction,
                 RegisterType.getUnitializedReference(classType.type));
 //            analyzeNewInstance(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_NewArray dexInstruction_NewArray) {
 		setDestinationRegisterType(instruction, dexInstruction_NewArray.getValue().getDescriptor());
 	}
 	
 	@Override
 	public void visit(DexInstruction_FilledNewArray dexInstruction_FilledNewArray) {}
 	
 	@Override
 	public void visit(DexInstruction_FillArray dexInstruction_FillArray) {}
 	
 	@Override
 	public void visit(DexInstruction_FillArrayData dexInstruction_FillArrayData) {}
 	
 	@Override
 	public void visit(DexInstruction_Throw dexInstruction_Throw) {}
 	
 	@Override
 	public void visit(DexInstruction_Goto dexInstruction_Goto) {}
 	
 	@Override
 	public void visit(DexInstruction_Switch dexInstruction_Switch) {}
 	
 	@Override
 	public void visit(DexInstruction_PackedSwitchData dexInstruction_PackedSwitchData) {}
 	
 	@Override
 	public void visit(DexInstruction_SparseSwitchData dexInstruction_SparseSwitchData) {}
 	
 	@Override
 	public void visit(DexInstruction_CompareFloat dexInstruction_CompareFloat) {
 		setDestinationRegisterType(instruction,
                 RegisterType.getRegisterType(RegisterType.Category.Byte, null));
 //			  analyzeFloatWideCmp(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_CompareWide dexInstruction_CompareWide) {
 		setDestinationRegisterType(instruction,
                 RegisterType.getRegisterType(RegisterType.Category.Byte, null));
 //            analyzeFloatWideCmp(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_IfTest dexInstruction_IfTest) {}
 	
 	@Override
 	public void visit(DexInstruction_IfTestZero dexInstruction_IfTestZero) {}
 	
 	@Override
 	public void visit(DexInstruction_ArrayGet inst) {
     	if (inst.getOpcode() == Opcode_GetPut.Object) {
 
             RegisterType arrayRegisterType = instruction.getPreRegisterType(inst.getRegArray());
             assert arrayRegisterType != null;
 
             if (arrayRegisterType.category != RegisterType.Category.Null) {
                 assert arrayRegisterType.type != null;
                 if (arrayRegisterType.type.getClassType().charAt(0) != '[') {
                     throw new ValidationException(String.format("Cannot use aget-object with non-array type %s",
                             arrayRegisterType.type.getClassType()));
                 }
 
                 assert arrayRegisterType.type instanceof ClassPath.ArrayClassDef;
                 ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRegisterType.type;
 
                 ClassPath.ClassDef elementClassDef = arrayClassDef.getImmediateElementClass();
                 char elementTypePrefix = elementClassDef.getClassType().charAt(0);
                 if (elementTypePrefix != 'L' && elementTypePrefix != '[') {
                     throw new ValidationException(String.format("Cannot use aget-object with array type %s. Incorrect " +
                             "array type for the instruction.", arrayRegisterType.type.getClassType()));
                 }
 
                 setDestinationRegisterType(instruction,
                         RegisterType.getRegisterType(RegisterType.Category.Reference, elementClassDef));
             } else {
                 setDestinationRegisterType(instruction,
                         RegisterType.getRegisterType(RegisterType.Category.Null, null));
             }
     		
     	} else {
     		
 	    	RegisterType.Category category;
 	    	switch (inst.getOpcode()) {
 			case Boolean:
 				category = RegisterType.Category.Boolean;
 				break;
 			case Byte:
 				category = RegisterType.Category.Byte;
 				break;
 			case Char:
 				category = RegisterType.Category.Char;
 				break;
 			case IntFloat:
 				category = RegisterType.Category.Integer; // Ambiguity here does not matter, type merging will deal with it.
 				break;
 			case Short:
 				category = RegisterType.Category.Short;
 				break;
 			default:
 				throw new ValidationException("wrong type AGET");
 	    	}
 	    	setDestinationRegisterType(instruction, RegisterType.getRegisterType(category, null));
     	}
 	}		
 	@Override
 	public void visit(DexInstruction_ArrayGetWide dexInstruction_ArrayGetWide) {
 
         RegisterType arrayRegisterType = instruction.getPreRegisterType(dexInstruction_ArrayGetWide.getRegArray());
         assert arrayRegisterType != null;
 
         if (arrayRegisterType.category != RegisterType.Category.Null) {
             assert arrayRegisterType.type != null;
             if (arrayRegisterType.type.getClassType().charAt(0) != '[') {
                 throw new ValidationException(String.format("Cannot use aget-wide with non-array type %s",
                         arrayRegisterType.type.getClassType()));
             }
 
             assert arrayRegisterType.type instanceof ClassPath.ArrayClassDef;
             ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRegisterType.type;
 
             char arrayBaseType = arrayClassDef.getBaseElementClass().getClassType().charAt(0);
             if (arrayBaseType == 'J') {
                 setDestinationRegisterType(instruction,
                         RegisterType.getRegisterType(RegisterType.Category.LongLo, null));
             } else if (arrayBaseType == 'D') {
                 setDestinationRegisterType(instruction,
                         RegisterType.getRegisterType(RegisterType.Category.DoubleLo, null));
             } else {
                 throw new ValidationException(String.format("Cannot use aget-wide with array type %s. Incorrect " +
                         "array type for the instruction.", arrayRegisterType.type.getClassType()));
             }
         } else {
             setDestinationRegisterType(instruction,
                         RegisterType.getRegisterType(RegisterType.Category.LongLo, null));
         }
 //            analyzeAgetWide(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_ArrayPut dexInstruction_ArrayPut) {}
 	
 	@Override
 	public void visit(DexInstruction_ArrayPutWide dexInstruction_ArrayPutWide) {}
 	
 	@Override
 	public void visit(DexInstruction_InstanceGet dexInstruction_InstanceGet) {
 		setDestinationRegisterType(instruction, dexInstruction_InstanceGet.getFieldType());
 //            analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Integer);
 //            analyzeIgetWideObject(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(
 			DexInstruction_InstanceGetWide dexInstruction_InstanceGetWide) {
 		setDestinationRegisterType(instruction, dexInstruction_InstanceGetWide.getFieldType());
 //            analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Integer);
 //            analyzeIgetWideObject(analyzedInstruction);
 		
 	}
 	@Override
 	public void visit(DexInstruction_InstancePut dexInstruction_InstancePut) {}
 	
 	@Override
 	public void visit(DexInstruction_InstancePutWide dexInstruction_InstancePutWide) {}
 	
 	@Override
 	public void visit(DexInstruction_StaticGet dexInstruction_StaticGet) {
 		setDestinationRegisterType(instruction, dexInstruction_StaticGet.getFieldType());
 //            analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Integer);
 //            analyzeSgetWideObject(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_StaticGetWide dexInstruction_StaticGetWide) {
 		setDestinationRegisterType(instruction, dexInstruction_StaticGetWide.getFieldType());
 //            analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Integer);
 //            analyzeSgetWideObject(analyzedInstruction);
 
 	}
 	@Override
 	public void visit(DexInstruction_StaticPut dexInstruction_StaticPut) {}
 	
 	@Override
 	public void visit(DexInstruction_StaticPutWide dexInstruction_StaticPutWide) {}
 	
 	@Override
 	public void visit(DexInstruction_Invoke dexInstruction_Invoke) {
         //the only time that an invoke instruction changes a register type is when using invoke-direct on a
         //constructor (<init>) method, which changes the uninitialized reference (and any register that the same
         //uninit reference has been copied to) to an initialized reference
 		if (dexInstruction_Invoke.getCallType() != Opcode_Invoke.Direct) return;
 		if (!dexInstruction_Invoke.getMethodName().equals("<init>")) return;
 		
 		DexRegister objectRegister = dexInstruction_Invoke.getArgumentRegisters().get(0);
 		RegisterType objectRegisterType = instruction.getPreRegisterType(objectRegister);
         assert objectRegisterType != null;
 
         if (objectRegisterType.category != RegisterType.Category.UninitRef &&
                 objectRegisterType.category != RegisterType.Category.UninitThis) {
             return;
         }
 
         setPostRegisterType(instruction, objectRegister,
                 RegisterType.getRegisterType(RegisterType.Category.Reference, objectRegisterType.type));
 
         for(Integer i : instruction.getPostRegisters()) {
             RegisterType postInstructionRegisterType = instruction.getPostRegisterType(i);
             if (postInstructionRegisterType.category == RegisterType.Category.Unknown) {
                 RegisterType preInstructionRegisterType = instruction.getPreRegisterType(i);
 
                 if (preInstructionRegisterType.category == RegisterType.Category.UninitRef ||
                     preInstructionRegisterType.category == RegisterType.Category.UninitThis) {
 
                     RegisterType registerType;
                     if (preInstructionRegisterType == objectRegisterType) {
                         registerType = instruction.getPostRegisterType(objectRegister);
                     } else {
                         registerType = preInstructionRegisterType;
                     }
 
                     setPostRegisterType(instruction, new DexRegister(i), registerType);
                 }
             }
         }			
 		//INVOKE_DIRECT:
 //            analyzeInvokeDirect(analyzedInstruction);
 	}
 
 	@Override
 	public void visit(DexInstruction_UnaryOp dexInstruction_UnaryOp) {
 		Category category;
 		switch (dexInstruction_UnaryOp.getInsnOpcode()) {
 			case NegFloat:
 				category = Category.Float;
 				break;
 			case NegInt:
 			case NotInt:
 				category = Category.Integer;
 				break;
 		    default:
 		    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOp");
 		}
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 //            analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Integer);
 
 	}
 	@Override
 	public void visit(DexInstruction_UnaryOpWide dexInstruction_UnaryOpWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_UnaryOpWide.getRegFrom1(), dexInstruction_UnaryOpWide.getRegFrom2());
 		assert DexRegisterHelper.isPair(dexInstruction_UnaryOpWide.getRegTo1(), dexInstruction_UnaryOpWide.getRegTo2());
 		Category category;
 	    switch (dexInstruction_UnaryOpWide.getInsnOpcode()) {
 	    case NegDouble:
 	    	category = Category.DoubleLo;
 	    	break;
 	    case NegLong:
 	    case NotLong:
 	    	category = Category.LongLo;
 	    	break;
 	    default:
 	    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOpWide");
 	    }
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 //          analyzeUnaryOp(analyzedInstruction, RegisterType.Category.LongLo);
 	}
 	@Override
 	public void visit(DexInstruction_Convert dexInstruction_Convert) {
 		Category category;
 	    switch (dexInstruction_Convert.getInsnOpcode()) {
 		case FloatToInt:
 			category = Category.Integer;
 			break;
 		case IntToByte:
 			category = Category.Byte;
 			break;
 		case IntToChar:
 			category = Category.Char;
 			break;
 		case IntToFloat:
 			category = Category.Float;
 			break;
 		case IntToShort:
 			category = Category.Short;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_Convert");
 	    
 	    }
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 	}
 	@Override
 	public void visit(DexInstruction_ConvertWide dexInstruction_ConvertWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_ConvertWide.getRegFrom1(), dexInstruction_ConvertWide.getRegFrom2());
 		assert DexRegisterHelper.isPair(dexInstruction_ConvertWide.getRegTo1(), dexInstruction_ConvertWide.getRegTo2());
 		Category category;
 	    if (dexInstruction_ConvertWide.getInsnOpcode() == Opcode_ConvertWide.DoubleToLong) {
 			category = Category.LongLo;
 	    } else {
 	    	category = Category.DoubleLo;
 	    }
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 	}
 	@Override
 	public void visit(DexInstruction_ConvertFromWide dexInstruction_ConvertFromWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_ConvertFromWide.getRegFrom1(), dexInstruction_ConvertFromWide.getRegFrom2());
 		Category category;
 	    switch (dexInstruction_ConvertFromWide.getInsnOpcode()) {
 		case DoubleToFloat:
 		case LongToFloat:
 			category = Category.Float;
 			break;
 		case DoubleToInt:
 		case LongToInt:
 			category = Category.Integer;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_ConvertFromWide");
 	    }
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 	}
 	@Override
 	public void visit(DexInstruction_ConvertToWide dexInstruction_ConvertToWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_ConvertToWide.getRegTo1(), dexInstruction_ConvertToWide.getRegTo2());
 		Category category;
 	    switch (dexInstruction_ConvertToWide.getInsnOpcode()) {
 	    case FloatToDouble:
 		case IntToDouble:
 			category = Category.DoubleLo;
 			break;
 		case FloatToLong:
 		case IntToLong:
 			category = Category.LongLo;
 			break;
 		default:
 	    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOpWide");
 	    }
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 	}
 	
     private void analyzeBinaryOp(DexRegister srcReg1, DexRegister srcReg2, 
     		Category destRegisterCategory, boolean checkForBoolean) {
 		if (checkForBoolean) {
 			RegisterType source1RegisterType =
 			    instruction.getPreRegisterType(srcReg1);
 			RegisterType source2RegisterType =
 			    instruction.getPreRegisterType(srcReg2);
 			
 			if (BooleanCategories.contains(source1RegisterType.category) &&
 			BooleanCategories.contains(source2RegisterType.category)) {
 				destRegisterCategory = RegisterType.Category.Boolean;
 			}
 		}
 		
 		setDestinationRegisterType(instruction,
 		RegisterType.getRegisterType(destRegisterCategory, null));
 	}		
     
 	@Override
 	public void visit(DexInstruction_BinaryOp dexInstruction_BinaryOp) {
 		Category category;
 		boolean checkForBoolean = false; // Is this int operation actually representing boolean?
 		switch(dexInstruction_BinaryOp.getInsnOpcode()) {
 		case AddFloat:
 		case SubFloat:
 		case DivFloat:
 		case MulFloat:
 		case RemFloat:
 			category = Category.Float;
 			break;
 		case AddInt:
 		case SubInt:
 		case MulInt:
 		case DivInt:
 		case RemInt:
 		case ShlInt:
 		case ShrInt:
 		case UshrInt:
 			category = Category.Integer;
 			break;
 		case AndInt:
 		case OrInt:
 		case XorInt:
 			category = Category.Integer;
 			checkForBoolean = true;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOp");
 		}
 		analyzeBinaryOp(dexInstruction_BinaryOp.getRegSourceA(), dexInstruction_BinaryOp.getRegSourceB(),
         		category, checkForBoolean);
 //            analyzeBinaryOp(analyzedInstruction, RegisterType.Category.Integer, true);
 	}
 	
 	private Category getDestTypeForLiteralShiftRight(RegisterType sourceRegisterType, long literalShift, boolean signedShift) {
 		if (literalShift == 0) {
 			return sourceRegisterType.category;
 		}
 
 		RegisterType.Category destRegisterCategory;
 		if (!signedShift) {
 			destRegisterCategory = RegisterType.Category.Integer;
 		} else {
 			destRegisterCategory = sourceRegisterType.category;
 		}
 
 		if (literalShift >= 32) {
 			// TODO: add warning
 			return destRegisterCategory;
 		}
 
 		switch (sourceRegisterType.category) {
 		case Integer:
 		case Float:
 			if (!signedShift) {
 				if (literalShift > 24) {
 					return RegisterType.Category.PosByte;
 				}
 				if (literalShift >= 16) {
 					return RegisterType.Category.Char;
 				}
 			} else {
 				if (literalShift >= 24) {
 					return RegisterType.Category.Byte;
 				}
 				if (literalShift >= 16) {
 					return RegisterType.Category.Short;
 				}
 			}
 			break;
 		case Short:
 			if (signedShift && literalShift >= 8) {
 				return RegisterType.Category.Byte;
 			}
 			break;
 		case PosShort:
 			if (literalShift >= 8) {
 				return RegisterType.Category.PosByte;
 			}
 			break;
 		case Char:
 			if (literalShift > 8) {
 				return RegisterType.Category.PosByte;
 			}
 			break;
 		case Byte:
 			break;
 		case PosByte:
 			return RegisterType.Category.PosByte;
 		case Null:
 		case One:
 		case Boolean:
 			return RegisterType.Category.Null;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOp");
 		}
 
 		return destRegisterCategory;
 	}
     		
 	@Override
 	public void visit(DexInstruction_BinaryOpLiteral dexInstruction_BinaryOpLiteral) {
 		Category category = Category.Integer;
 		RegisterType sourceRegisterType = instruction.getPreRegisterType(dexInstruction_BinaryOpLiteral.getRegSource());
 		switch(dexInstruction_BinaryOpLiteral.getInsnOpcode()){
 		case Add:
 		case Div:
 		case Mul:
 		case Rem:
 		case Rsub:
 		case Shl:
 			break;
 		case And:
 		case Or:
 		case Xor:
 			// Check for potential boolean operations
 			if (BooleanCategories.contains(sourceRegisterType.category)) {
                 long literal = dexInstruction_BinaryOpLiteral.getLiteral();
                 if (literal == 0 || literal == 1) {
                 	category = Category.Boolean;
                 }
 			}
 			break;
 		case Shr:
 			category = getDestTypeForLiteralShiftRight(sourceRegisterType, dexInstruction_BinaryOpLiteral.getLiteral(), true);
 			break;
 		case Ushr:
 			category = getDestTypeForLiteralShiftRight(sourceRegisterType, dexInstruction_BinaryOpLiteral.getLiteral(), false);
 			break;
 		default:
 			break;
 		
 		}
 		setDestinationRegisterType(instruction, 
 				RegisterType.getRegisterType(category, null));
 //            analyzeLiteralBinaryOp(analyzedInstruction, RegisterType.Category.Integer, true);
 	}
 	@Override
 	public void visit(DexInstruction_BinaryOpWide dexInstruction_BinaryOpWide) {
 		assert DexRegisterHelper.isPair(dexInstruction_BinaryOpWide.getRegSourceA1(), dexInstruction_BinaryOpWide.getRegSourceA2());
 		assert DexRegisterHelper.isPair(dexInstruction_BinaryOpWide.getRegSourceB1(), dexInstruction_BinaryOpWide.getRegSourceB2());
 		assert DexRegisterHelper.isPair(dexInstruction_BinaryOpWide.getRegTarget1(), dexInstruction_BinaryOpWide.getRegTarget2());
 		Category category;
 		switch(dexInstruction_BinaryOpWide.getInsnOpcode()){
 		case AddDouble:
 		case SubDouble:
 		case MulDouble:
 		case DivDouble:
 		case RemDouble:
 			category = Category.DoubleLo;
 			break;
 		case AddLong:
 		case AndLong:
 		case DivLong:
 		case MulLong:
 		case OrLong:
 		case RemLong:
 		case ShlLong:
 		case ShrLong:
 		case SubLong:
 		case UshrLong:
 		case XorLong:
 			category = Category.LongLo;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOpWide");
 		}
 		setDestinationRegisterType(instruction,
 				RegisterType.getRegisterType(category, null));
 //            analyzeBinaryOp(analyzedInstruction, RegisterType.Category.LongLo, false);
 
 	}
 	@Override
 	public void visit(DexInstruction_Unknown dexInstruction_Unknown) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_FilledNewArray dexPseudoinstruction_FilledNewArray) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetInternalClassAnnotation dexPseudoinstruction_GetInternalClassAnnotation) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetInternalMethodAnnotation dexPseudoinstruction_GetInternalMethodAnnotation) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetMethodCaller dexPseudoinstruction_GetMethodCaller) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetObjectTaint dexPseudoinstruction_GetObjectTaint) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetQueryTaint dexPseudoinstruction_GetQueryTaint) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetServiceTaint dexPseudoinstruction_GetServiceTaint) {}	
 	
 	@Override
 	public void visit(DexPseudoinstruction_PrintInteger dexPseudoinstruction_PrintInteger) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_PrintIntegerConst dexPseudoinstruction_PrintIntegerConst) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_PrintString dexPseudoinstruction_PrintString) {}	
 	
 	@Override
 	public void visit(DexPseudoinstruction_PrintStringConst dexPseudoinstruction_PrintStringConst) {}
 
 	@Override
 	public void visit(DexPseudoinstruction_SetObjectTaint dexPseudoinstruction_SetObjectTaint) {}
 	
 	@Override
 	public void visit(DexPseudoinstruction_Invoke dexPseudoinstruction_Invoke) {}
 	
 };

 package com.rx201.dx.translator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import lombok.Getter;
 
 import org.jf.dexlib.Code.Analysis.ClassPath;
 import org.jf.dexlib.Code.Analysis.RegisterType;
 import org.jf.dexlib.Code.Analysis.ValidationException;
 import org.jf.dexlib.Code.Analysis.RegisterType.Category;
 
 import com.rx201.dx.translator.util.DexRegisterHelper;
 
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
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
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 
 public class UseDefTypeAnalyzer implements DexInstructionVisitor {
 
 	private HashMap<Integer, RegisterType> useSet;
 	private HashMap<Integer, RegisterType> defSet;
 	// <regSource, regDestination>
 	private HashMap<Integer, Integer> moveSet;
 	
 	// Some of these are partial type information, we don't distinguish them
 	// for now, but this may be changed in future.
 	private final static RegisterType Primitive = null;
 	private final static RegisterType PrimitiveWide = null;
 	private final static RegisterType PrimitiveOrReference = null;
 	private final static RegisterType Reference = ObjectType("Ljava/lang/Object;");
 	private final static RegisterType Array = null;
 	private final static RegisterType Boolean = RegisterType.getRegisterType(Category.Boolean, null);
 	private final static RegisterType Byte = RegisterType.getRegisterType(Category.Byte, null);
 	private final static RegisterType Short = RegisterType.getRegisterType(Category.Short, null);
 	private final static RegisterType Char = RegisterType.getRegisterType(Category.Char, null);
 	private final static RegisterType Integer = RegisterType.getRegisterType(Category.Integer, null);
 	private final static RegisterType Float = RegisterType.getRegisterType(Category.Float, null);
 	private final static RegisterType LongLo = RegisterType.getRegisterType(Category.LongLo, null);
 	private final static RegisterType LongHi = RegisterType.getRegisterType(Category.LongHi, null);
 	private final static RegisterType DoubleLo = RegisterType.getRegisterType(Category.DoubleLo, null);
 	private final static RegisterType DoubleHi = RegisterType.getRegisterType(Category.DoubleHi, null);
 	private final static RegisterType StringObj = ObjectType("Ljava/lang/String;");
 	private static RegisterType ObjectType(String descriptor) {
 		return RegisterType.getRegisterTypeForType(descriptor);
     }
 	private static RegisterType LiteralType(long value) {
 		return RegisterType.getRegisterTypeForLiteral(value);
 	}
 	private static RegisterType ArrayElementType(DexArrayType arrayType) {
 		return RegisterType.getRegisterTypeForType(arrayType.getElementType().getDescriptor());
 	}
 	public UseDefTypeAnalyzer() {
 		useSet = new HashMap<Integer, RegisterType>();
 		defSet = new HashMap<Integer, RegisterType>();
 		moveSet = new HashMap<Integer, Integer>();
 	}
 	
 	public void reset() {
 		useSet.clear();
 		defSet.clear();
 		moveSet.clear();
 	}
 
 	private void defineRegister(DexRegister regTo, RegisterType registerType) {
 		defSet.put(DexRegisterHelper.normalize(regTo), registerType);
 	}
 	private void useRegister(DexRegister regFrom, RegisterType registerType) {
 		useSet.put(DexRegisterHelper.normalize(regFrom), registerType);
 	}
 	private void moveRegister(DexRegister regFrom, DexRegister regTo) {
 		moveSet.put(DexRegisterHelper.normalize(regFrom), DexRegisterHelper.normalize(regTo));
 	}
 	
 	public RegisterType getPrecisePostRegisterType(DexRegister reg, AnalyzedDexInstruction instruction) {
 		// bfs for register type propagation
 		HashMap<AnalyzedDexInstruction, HashSet<Integer>> visited = new HashMap<AnalyzedDexInstruction, HashSet<Integer>>(); 
 		// These two linkedList are always paired together, maybe a better way is to have a separate class for them?
 		LinkedList<AnalyzedDexInstruction> queue = new LinkedList<AnalyzedDexInstruction>();
 		LinkedList<Integer> targetReg_queue = new LinkedList<Integer>();
 		
 		int regNum = DexRegisterHelper.normalize(reg);
 		for(AnalyzedDexInstruction successor : instruction.getSuccesors()) {
 			queue.add(successor);
 			targetReg_queue.add(regNum);
 		}
 		
 		RegisterType type = RegisterType.getRegisterType(RegisterType.Category.Unknown, null);
 		
 		while(queue.size() > 0) {
 			AnalyzedDexInstruction head = queue.remove();
 			int head_reg = targetReg_queue.remove();
 			
 			if (!visited.containsKey(head))
 				visited.put(head, new HashSet<Integer>());
 			if (visited.get(head).contains(head_reg) ) continue;
 			visited.get(head).add(head_reg);
 			
 			boolean deadEnd = false;
 			this.reset();
 			if (head.instruction != null) {
 				head.instruction.accept(this);
 				
 				RegisterType typeInfo = useSet.get(head_reg);
 				if (typeInfo != null) {
 					//TODO: UGLY HACK WARNING
 					/* This is valid, so deal with this special case here
 					 *     const/4 v8, 0x0
 					 *     const/16 v7, 0x20
 					 *     shl-long/2addr v5, v7
 					 *     
 					 * a.k.a merging Integer with LongLo/Hi should be allowed    
 					 */
 					
 					type = TypeUnification.permissiveMerge(head.instruction.getParentFile(), type, typeInfo);
 					assert type.category != Category.Conflicted;
 				}
 				// Do not search further is this instruction overwrites the target register.
 				if (defSet.containsKey(head_reg))
 					deadEnd = true;
 			}
 			
 			if (!deadEnd) {
 				for(AnalyzedDexInstruction successor : head.getSuccesors()) {
 					queue.add(successor);
 					targetReg_queue.add(head_reg);
 					if (moveSet.containsKey(head_reg)) {
 						queue.add(successor);
 						targetReg_queue.add(moveSet.get(head_reg));
 					}
 				}
 			}
 		}
 		
 		return type;
 	} 
 	
 	@Override
 	public void visit(DexInstruction_Nop instruction) {
 		return;
 	}
 
 	@Override
 	public void visit(DexInstruction_Move instruction) {
 		useRegister(instruction.getRegFrom(), instruction.isObjectMoving() ? Reference : Primitive);
 		defineRegister(instruction.getRegTo(), instruction.isObjectMoving() ? Reference : Primitive);
 		moveRegister(instruction.getRegFrom(), instruction.getRegTo());
 	}
 
 	@Override
 	public void visit(DexInstruction_MoveWide instruction) {
 		useRegister(instruction.getRegFrom1(), PrimitiveWide);
 		useRegister(instruction.getRegFrom2(), PrimitiveWide);
 		defineRegister(instruction.getRegTo1(), PrimitiveWide);
 		defineRegister(instruction.getRegTo2(), PrimitiveWide);
 		moveRegister(instruction.getRegFrom1(), instruction.getRegTo1());
 		moveRegister(instruction.getRegFrom2(), instruction.getRegTo2());
 	}
 
 	@Override
 	public void visit(DexInstruction_MoveResult instruction) {
 		defineRegister(instruction.getRegTo(), instruction.isObjectMoving() ? Reference : Primitive);
 	}
 
 	@Override
 	public void visit(DexInstruction_MoveResultWide instruction) {
 		defineRegister(instruction.getRegTo1(), PrimitiveWide);
 		defineRegister(instruction.getRegTo2(), PrimitiveWide);
 	}
 
 	@Override
 	public void visit(DexInstruction_MoveException instruction) {
 		defineRegister(instruction.getRegTo(), Reference);
 	}
 
 	@Override
 	public void visit(DexInstruction_ReturnVoid instruction) {
 		return;
 	}
 
 	@Override
 	public void visit(DexInstruction_Return instruction) {
 		String returnType = instruction.getParentMethod().getPrototype().getReturnType().getDescriptor();
 		useRegister(instruction.getRegFrom(), ObjectType(returnType));
 	}
 
 	@Override
 	public void visit(DexInstruction_ReturnWide instruction) {
 		if (instruction.getParentMethod().getPrototype().getReturnType().getDescriptor().equals("J")) {
 			useRegister(instruction.getRegFrom1(), LongLo);
 			useRegister(instruction.getRegFrom2(), LongHi);
 		} else {
 			useRegister(instruction.getRegFrom1(), DoubleLo);
 			useRegister(instruction.getRegFrom2(), DoubleHi);
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_Const instruction) {
 		defineRegister(instruction.getRegTo(), LiteralType(instruction.getValue()));
 	}
 
 	@Override
 	public void visit(DexInstruction_ConstWide instruction) {
 		defineRegister(instruction.getRegTo1(), PrimitiveWide);
 		defineRegister(instruction.getRegTo2(), PrimitiveWide);
 	}
 
 	@Override
 	public void visit(DexInstruction_ConstString instruction) {
 		defineRegister(instruction.getRegTo(), StringObj);
 	}
 
 	@Override
 	public void visit(DexInstruction_ConstClass instruction) {
 		defineRegister(instruction.getRegTo(), ObjectType(instruction.getValue().getDescriptor()));
 	}
 
 	@Override
 	public void visit(DexInstruction_Monitor instruction) {
 		useRegister(instruction.getRegMonitor(), Reference);
 	}
 
 	@Override
 	public void visit(DexInstruction_CheckCast instruction) {
 		useRegister(instruction.getRegObject(), Reference);
 	}
 
 	@Override
 	public void visit(DexInstruction_InstanceOf instruction) {
 		useRegister(instruction.getRegObject(), Reference);
 		defineRegister(instruction.getRegTo(), Boolean);
 	}
 
 	@Override
 	public void visit(DexInstruction_ArrayLength instruction) {
 		useRegister(instruction.getRegArray(), Array);
 		defineRegister(instruction.getRegTo(), Integer);
 	}
 
 	@Override
 	public void visit(DexInstruction_NewInstance instruction) {
 		defineRegister(instruction.getRegTo(), ObjectType(instruction.getValue().getDescriptor()));
 	}
 
 	@Override
 	public void visit(DexInstruction_NewArray instruction) {
 		useRegister(instruction.getRegSize(), Integer);
 		defineRegister(instruction.getRegTo(), Array);
 	}
 
 	@Override
 	public void visit(DexInstruction_FilledNewArray instruction) {
 		RegisterType elementType = ArrayElementType(instruction.getArrayType());
 		for(DexRegister argument : instruction.getArgumentRegisters()) {
 			useRegister(argument, elementType);
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_FillArray instruction) {
 //		defineRegister(instruction.getRegArray(), Array);
 	}
 
 	@Override
 	public void visit(DexInstruction_FillArrayData instruction) {
 		return;
 	}
 
 	@Override
 	public void visit(DexInstruction_Throw instruction) {
 		useRegister(instruction.getRegFrom(), Reference);
 	}
 
 	@Override
 	public void visit(DexInstruction_Goto instruction) {
 		return;
 	}
 
 	@Override
 	public void visit(DexInstruction_Switch instruction) {
 		useRegister(instruction.getRegTest(), Integer);
 	}
 
 	@Override
 	public void visit(DexInstruction_PackedSwitchData instruction) {
 		return;
 	}
 
 	@Override
 	public void visit(DexInstruction_SparseSwitchData instruction) {
 		return;
 	}
 
 	@Override
 	public void visit(DexInstruction_CompareFloat instruction) {
 		useRegister(instruction.getRegSourceA(), Float);
 		useRegister(instruction.getRegSourceB(), Float);
 		defineRegister(instruction.getRegTo(), Boolean);
 	}
 
 	@Override
 	public void visit(DexInstruction_CompareWide instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case CmpLong:
 			useRegister(instruction.getRegSourceA1(), LongLo);
 			useRegister(instruction.getRegSourceA2(), LongHi);
 			useRegister(instruction.getRegSourceB1(), LongLo);
 			useRegister(instruction.getRegSourceB2(), LongHi);
 			break;
 		case CmpgDouble:
 		case CmplDouble:
 			useRegister(instruction.getRegSourceA1(), DoubleLo);
 			useRegister(instruction.getRegSourceA2(), DoubleHi);
 			useRegister(instruction.getRegSourceB1(), DoubleLo);
 			useRegister(instruction.getRegSourceB2(), DoubleHi);
 			break;
 		default:
 			assert false;
 			break;
 		}
 		defineRegister(instruction.getRegTo(), Boolean);
 	}
 
 	@Override
 	public void visit(DexInstruction_IfTest instruction) {
 		//TODO: Can we assume this is integer-only? what about compare to NULL?
 	}
 
 	@Override
 	public void visit(DexInstruction_IfTestZero instruction) {
 		//TODO: Can we assume this is integer-only? what about compare to NULL?
 	}
 
 	@Override
 	public void visit(DexInstruction_ArrayGet instruction) {
 		useRegister(instruction.getRegArray(), Array);
 		useRegister(instruction.getRegIndex(), Integer);
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			defineRegister(instruction.getRegTo(), Boolean);
 			break;
 		case Byte:
 			defineRegister(instruction.getRegTo(), Byte);
 			break;
 		case Char:
 			defineRegister(instruction.getRegTo(), Char);
 			break;
 		case IntFloat:
			defineRegister(instruction.getRegTo(), Integer);
 			break;
 		case Object:
 			defineRegister(instruction.getRegTo(), Reference);
 			break;
 		case Short:
 			defineRegister(instruction.getRegTo(), Short);
 			break;
 		default:
 			assert false;
 			break;
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_ArrayGetWide instruction) {
 		useRegister(instruction.getRegArray(), Array);
 		useRegister(instruction.getRegIndex(), Integer);
 		defineRegister(instruction.getRegTo1(), PrimitiveWide);
 		defineRegister(instruction.getRegTo2(), PrimitiveWide);
 	}
 
 	@Override
 	public void visit(DexInstruction_ArrayPut instruction) {
 		useRegister(instruction.getRegArray(), Array);
 		useRegister(instruction.getRegIndex(), Integer);
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			useRegister(instruction.getRegFrom(), Boolean);
 			break;
 		case Byte:
 			useRegister(instruction.getRegFrom(), Byte);
 			break;
 		case Char:
 			useRegister(instruction.getRegFrom(), Char);
 			break;
 		case IntFloat:
			useRegister(instruction.getRegFrom(), Integer);
 			break;
 		case Object:
 			useRegister(instruction.getRegFrom(), Reference);
 			break;
 		case Short:
 			useRegister(instruction.getRegFrom(), Short);
 			break;
 		default:
 			assert false;
 			break;
 		}
 		
 	}
 
 	@Override
 	public void visit(DexInstruction_ArrayPutWide instruction) {
 		useRegister(instruction.getRegArray(), Array);
 		useRegister(instruction.getRegIndex(), Integer);
 		useRegister(instruction.getRegFrom1(), PrimitiveWide);
 		useRegister(instruction.getRegFrom2(), PrimitiveWide);
 	}
 
 	@Override
 	public void visit(DexInstruction_InstanceGet instruction) {
 		useRegister(instruction.getRegObject(), ObjectType(instruction.getFieldClass().getDescriptor()));
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			defineRegister(instruction.getRegTo(), Boolean);
 			break;
 		case Byte:
 			defineRegister(instruction.getRegTo(), Byte);
 			break;
 		case Char:
 			defineRegister(instruction.getRegTo(), Char);
 			break;
 		case Short:
 			defineRegister(instruction.getRegTo(), Short);
 			break;
 			
 		case IntFloat:
 		case Object:
 			defineRegister(instruction.getRegTo(), ObjectType(instruction.getFieldType().getDescriptor()));
 			break;
 			
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_InstanceGetWide instruction) {
 		useRegister(instruction.getRegObject(), ObjectType(instruction.getFieldClass().getDescriptor()));
 		defineRegister(instruction.getRegTo1(), ObjectType(instruction.getFieldType().getDescriptor()));
 		defineRegister(instruction.getRegTo2(), ObjectType(instruction.getFieldType().getDescriptor())); //Convert Lo->Hi ?
 	}
 
 	@Override
 	public void visit(DexInstruction_InstancePut instruction) {
 		useRegister(instruction.getRegObject(), ObjectType(instruction.getFieldClass().getDescriptor()));
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			useRegister(instruction.getRegFrom(), Boolean);
 			break;
 		case Byte:
 			useRegister(instruction.getRegFrom(), Byte);
 			break;
 		case Char:
 			useRegister(instruction.getRegFrom(), Char);
 			break;
 		case Short:
 			useRegister(instruction.getRegFrom(), Short);
 			break;
 			
 		case IntFloat:
 		case Object:
 			useRegister(instruction.getRegFrom(), ObjectType(instruction.getFieldType().getDescriptor()));
 			break;
 			
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_InstancePutWide instruction) {
 		useRegister(instruction.getRegObject(), ObjectType(instruction.getFieldClass().getDescriptor()));
 		useRegister(instruction.getRegFrom1(), ObjectType(instruction.getFieldType().getDescriptor()));
 		useRegister(instruction.getRegFrom2(), ObjectType(instruction.getFieldType().getDescriptor())); //Convert Lo->Hi ?
 	}
 
 	@Override
 	public void visit(DexInstruction_StaticGet instruction) {
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			defineRegister(instruction.getRegTo(), Boolean);
 			break;
 		case Byte:
 			defineRegister(instruction.getRegTo(), Byte);
 			break;
 		case Char:
 			defineRegister(instruction.getRegTo(), Char);
 			break;
 		case Short:
 			defineRegister(instruction.getRegTo(), Short);
 			break;
 			
 		case IntFloat:
 		case Object:
 			defineRegister(instruction.getRegTo(), ObjectType(instruction.getFieldType().getDescriptor()));
 			break;
 			
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_StaticGetWide instruction) {
 		defineRegister(instruction.getRegTo1(), ObjectType(instruction.getFieldType().getDescriptor()));
 		defineRegister(instruction.getRegTo2(), ObjectType(instruction.getFieldType().getDescriptor())); //Convert Lo->Hi ?
 	}
 
 	@Override
 	public void visit(DexInstruction_StaticPut instruction) {
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			useRegister(instruction.getRegFrom(), Boolean);
 			break;
 		case Byte:
 			useRegister(instruction.getRegFrom(), Byte);
 			break;
 		case Char:
 			useRegister(instruction.getRegFrom(), Char);
 			break;
 		case Short:
 			useRegister(instruction.getRegFrom(), Short);
 			break;
 			
 		case IntFloat:
 		case Object:
 			useRegister(instruction.getRegFrom(), ObjectType(instruction.getFieldType().getDescriptor()));
 			break;
 			
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_StaticPutWide instruction) {
 		useRegister(instruction.getRegFrom1(), ObjectType(instruction.getFieldType().getDescriptor()));
 		useRegister(instruction.getRegFrom2(), ObjectType(instruction.getFieldType().getDescriptor())); //Convert Lo->Hi ?
 	}
 
 	@Override
 	public void visit(DexInstruction_Invoke instruction) {
 		List<DexRegister> arguments = instruction.getArgumentRegisters();
 		List<DexRegisterType> parameterTypes = instruction.getMethodPrototype().getParameterTypes();
 		
 		int regIndex = 0;
 		if (!instruction.isStaticCall()) {
 			useRegister(arguments.get(regIndex++), ObjectType(instruction.getClassType().getDescriptor()));
 		}
 		
 		for(int i=0 ;i<parameterTypes.size(); i++) {
 			DexRegisterType paramType = parameterTypes.get(i);
 			useRegister(arguments.get(regIndex), ObjectType(paramType.getDescriptor()));
 			regIndex += paramType.getRegisters();
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_UnaryOp instruction) {
 		RegisterType regType = null;
 		switch(instruction.getInsnOpcode()) {
 		case NegFloat:
 			regType = Float;
 			break;
 		case NegInt:
 		case NotInt:
 			regType = Integer;
 			break;
 		default:
 			assert false;
 			break;
 		}
 		useRegister(instruction.getRegFrom(), regType);
 		defineRegister(instruction.getRegTo(), regType);
 	}
 
 	@Override
 	public void visit(DexInstruction_UnaryOpWide instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case NegDouble:
 			useRegister(instruction.getRegFrom1(), DoubleLo);
 			useRegister(instruction.getRegFrom2(), DoubleHi);
 			defineRegister(instruction.getRegTo1(), DoubleLo);
 			defineRegister(instruction.getRegTo2(), DoubleHi);
 			break;
 		case NegLong:
 		case NotLong:
 			useRegister(instruction.getRegFrom1(), LongLo);
 			useRegister(instruction.getRegFrom2(), LongHi);
 			defineRegister(instruction.getRegTo1(), LongLo);
 			defineRegister(instruction.getRegTo2(), LongHi);
 			break;
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_Convert instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case FloatToInt:
 			useRegister(instruction.getRegFrom(), Float);
 			defineRegister(instruction.getRegTo(), Integer);
 			break;
 		case IntToByte:
 			useRegister(instruction.getRegFrom(), Integer);
 			defineRegister(instruction.getRegTo(), Byte);
 			break;
 		case IntToChar:
 			useRegister(instruction.getRegFrom(), Integer);
 			defineRegister(instruction.getRegTo(), Char);
 			break;
 		case IntToFloat:
 			useRegister(instruction.getRegFrom(), Integer);
 			defineRegister(instruction.getRegTo(), Float);
 			break;
 		case IntToShort:
 			useRegister(instruction.getRegFrom(), Integer);
 			defineRegister(instruction.getRegTo(), Short);
 			break;
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_ConvertWide instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case DoubleToLong:
 			useRegister(instruction.getRegFrom1(), DoubleLo);
 			useRegister(instruction.getRegFrom2(), DoubleHi);
 			defineRegister(instruction.getRegTo1(), LongLo);
 			defineRegister(instruction.getRegTo2(), LongHi);
 			break;
 		case LongToDouble:
 			useRegister(instruction.getRegFrom1(), LongLo);
 			useRegister(instruction.getRegFrom2(), LongHi);
 			defineRegister(instruction.getRegTo1(), DoubleLo);
 			defineRegister(instruction.getRegTo2(), DoubleHi);
 			break;
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_ConvertFromWide instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case DoubleToFloat:
 			useRegister(instruction.getRegFrom1(), DoubleLo);
 			useRegister(instruction.getRegFrom2(), DoubleHi);
 			defineRegister(instruction.getRegTo(), Float);
 			break;
 		case DoubleToInt:
 			useRegister(instruction.getRegFrom1(), DoubleLo);
 			useRegister(instruction.getRegFrom2(), DoubleHi);
 			defineRegister(instruction.getRegTo(), Integer);
 			break;
 		case LongToFloat:
 			useRegister(instruction.getRegFrom1(), LongLo);
 			useRegister(instruction.getRegFrom2(), LongHi);
 			defineRegister(instruction.getRegTo(), Float);
 			break;
 		case LongToInt:
 			useRegister(instruction.getRegFrom1(), LongLo);
 			useRegister(instruction.getRegFrom2(), LongHi);
 			defineRegister(instruction.getRegTo(), Integer);
 			break;
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_ConvertToWide instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case FloatToDouble:
 			useRegister(instruction.getRegFrom(), Float);
 			defineRegister(instruction.getRegTo1(), DoubleLo);
 			defineRegister(instruction.getRegTo2(), DoubleHi);
 			break;
 		case FloatToLong:
 			useRegister(instruction.getRegFrom(), Float);
 			defineRegister(instruction.getRegTo1(), LongLo);
 			defineRegister(instruction.getRegTo2(), LongHi);
 			break;
 		case IntToDouble:
 			useRegister(instruction.getRegFrom(), Integer);
 			defineRegister(instruction.getRegTo1(), DoubleLo);
 			defineRegister(instruction.getRegTo2(), DoubleHi);
 			break;
 		case IntToLong:
 			useRegister(instruction.getRegFrom(), Integer);
 			defineRegister(instruction.getRegTo1(), LongLo);
 			defineRegister(instruction.getRegTo2(), LongHi);
 			break;
 		default:
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_BinaryOp instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case AddFloat:
 		case DivFloat:
 		case MulFloat:
 		case RemFloat:
 		case SubFloat:
 			useRegister(instruction.getRegSourceA(), Float);
 			useRegister(instruction.getRegSourceB(), Float);
 			defineRegister(instruction.getRegTarget(), Float);
 			break;
 		case AddInt:
 		case AndInt:
 		case DivInt:
 		case MulInt:
 		case OrInt:
 		case RemInt:
 		case ShlInt:
 		case ShrInt:
 		case SubInt:
 		case UshrInt:
 		case XorInt:
 			useRegister(instruction.getRegSourceA(), Integer);
 			useRegister(instruction.getRegSourceB(), Integer);
 			defineRegister(instruction.getRegTarget(), Integer);
 			break;
 		default:
 			assert false;
 			break;
 		
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_BinaryOpLiteral instruction) {
 		useRegister(instruction.getRegSource(), Integer);
 		defineRegister(instruction.getRegTarget(), Integer);
 	}
 
 	@Override
 	public void visit(DexInstruction_BinaryOpWide instruction) {
 		switch(instruction.getInsnOpcode()){
 		case AddDouble:
 		case SubDouble:
 		case MulDouble:
 		case DivDouble:
 		case RemDouble:
 			useRegister(instruction.getRegSourceA1(), DoubleLo);
 			useRegister(instruction.getRegSourceA2(), DoubleHi);
 			useRegister(instruction.getRegSourceB1(), DoubleLo);
 			useRegister(instruction.getRegSourceB2(), DoubleHi);
 			defineRegister(instruction.getRegTarget1(), DoubleLo);
 			defineRegister(instruction.getRegTarget2(), DoubleHi);
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
 			useRegister(instruction.getRegSourceA1(), LongLo);
 			useRegister(instruction.getRegSourceA2(), LongHi);
 			useRegister(instruction.getRegSourceB1(), LongLo);
 			useRegister(instruction.getRegSourceB2(), LongHi);
 			defineRegister(instruction.getRegTarget1(), LongLo);
 			defineRegister(instruction.getRegTarget2(), LongHi);
 			break;
 		default:
 			assert false;
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_Unknown instruction) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_FilledNewArray dexPseudoinstruction_FilledNewArray) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetInternalClassAnnotation dexPseudoinstruction_GetInternalClassAnnotation) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetInternalMethodAnnotation dexPseudoinstruction_GetInternalMethodAnnotation) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetMethodCaller dexPseudoinstruction_GetMethodCaller) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetObjectTaint dexPseudoinstruction_GetObjectTaint) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetQueryTaint dexPseudoinstruction_GetQueryTaint) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_GetServiceTaint dexPseudoinstruction_GetServiceTaint) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_PrintInteger dexPseudoinstruction_PrintInteger) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_PrintIntegerConst dexPseudoinstruction_PrintIntegerConst) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_PrintString dexPseudoinstruction_PrintString) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_PrintStringConst dexPseudoinstruction_PrintStringConst) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_SetObjectTaint dexPseudoinstruction_SetObjectTaint) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexPseudoinstruction_Invoke dexPseudoinstruction_Invoke) {
 		assert false;
 	}
 
 }

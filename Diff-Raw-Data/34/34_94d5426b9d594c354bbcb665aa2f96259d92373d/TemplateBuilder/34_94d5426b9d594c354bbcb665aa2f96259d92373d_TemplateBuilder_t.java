 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryStart;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 import uk.ac.cam.db538.dexter.dex.code.macro.DexMacro;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 import com.rx201.dx.translator.AnalyzedDexInstruction;
 import com.rx201.dx.translator.DexCodeAnalyzer;
 import com.rx201.dx.translator.RopType;
 
 public class TemplateBuilder {
 
 	private final DexCode code;
 	private final DexCodeAnalyzer analyzedCode;
 	private final CodeGenerator codeGen;
 	private final RuntimeHierarchy hierarchy;
 	private final DexTypeCache cache;
 	
 	public TemplateBuilder(DexCode code, DexCodeAnalyzer analyzedCode, CodeGenerator codeGen) {
 		this.code = code;
 		this.analyzedCode = analyzedCode;
 		this.codeGen = codeGen;
 		this.hierarchy = code.getHierarchy();
 		this.cache = this.hierarchy.getTypeCache();
 	}
 	
 	/**
 	 * Generates instrumentation of given instruction. If it succeeds,
 	 * the instrumentation will be executed. If it throws, taint of
 	 * the referenced registers will be propagated into the exception.
 	 * 
 	 * @param insn       Original instruction
 	 * @param tainting   Tainting instrumentation. 
 	 *                   Must define the taint register of the register defined by the instruction. 
 	 *                   Must never actually throw!
 	 */
 	public DexMacro create(DexInstruction insn, DexCodeElement tainting) {
 		return create(insn, insn, tainting);
 	}
 	
 	public DexMacro create(DexInstruction insn, DexCodeElement replacementInsn, DexCodeElement tainting) {
 		return new DexMacro(
 			generateThrowingPath(insn, replacementInsn),
 			nonthrowingTaintDefinition(insn, tainting));
 	}
 
     private DexCodeElement generateThrowingPath(DexInstruction insn, DexCodeElement inside) {
     	if (!insn.canThrow() || insn.lvaReferencedRegisters().isEmpty())
     		return insn;
     	
     	DexSingleRegister auxCombinedTaint = codeGen.auxReg();
     	DexCodeElement taintCombination = combineReferencedTaint(insn, auxCombinedTaint);
     	return taintException(inside, taintCombination, auxCombinedTaint);
     }
     
     public DexCodeElement taintException(DexCodeElement insideBlock, DexCodeElement afterMoveException, DexSingleRegister regTaint) {
     	DexTryStart block = codeGen.tryBlock(codeGen.catchAll());
     	DexLabel lAfter = codeGen.label();
     	
     	DexSingleRegister auxExObj = codeGen.auxReg();
     	
     	return new DexMacro(
     			block,
     			insideBlock,
     			block.getEndMarker(),
     			codeGen.jump(lAfter),
     			block.getCatchAllHandler(),
     			codeGen.move_ex(auxExObj),
     			afterMoveException,
     			codeGen.taintLookup(null, auxExObj, regTaint, hierarchy.classifyType(cache.TYPE_Throwable)),
     			codeGen.thrw(auxExObj),
     			lAfter);
     }
     
     public DexCodeElement nonthrowingTaintDefinition(DexCodeElement tainting, List<Pair<DexRegister, Boolean>> defRegs) {
     	if (!tainting.canThrow())
     		return tainting;
     	
     	DexTryStart block = codeGen.tryBlock(codeGen.catchAll());
     	DexLabel lAfter = codeGen.label();
     	
    	DexSingleRegister auxEx = codeGen.auxReg();
    	
     	if (defRegs == null || defRegs.isEmpty())
         	return new DexMacro(
         			block,
         			tainting,
         			block.getEndMarker(),
         			block.getCatchAllHandler());
     	else
     		return new DexMacro(
     			block,
     			tainting,
     			block.getEndMarker(),
     			codeGen.jump(lAfter),
     			
     			/*
     			 * Should never happen. Only for the compiler's sake.
     			 */
     			block.getCatchAllHandler(),
    			codeGen.move_ex(auxEx),
    			nonthrowingTaintDefinition(codeGen.printStackTrace(auxEx), null),
     			defineAllRegisters(defRegs),
     			
     			lAfter);
     }
     
     public DexCodeElement nonthrowingTaintDefinition(DexInstruction insn, DexCodeElement tainting) {
     	return nonthrowingTaintDefinition(tainting, getDefinedRegisters(insn));
     }
     
     private DexCodeElement combineReferencedTaint(DexInstruction insn, DexSingleRegister auxCombinedTaint) {
     	DexSingleRegister auxObjTaint = codeGen.auxReg();
 
     	List<DexCodeElement> taintCombination = new ArrayList<DexCodeElement>();
     	taintCombination.add(codeGen.setEmptyTaint(auxCombinedTaint));
     	
     	for (DexRegister regRef : insn.lvaReferencedRegisters()) {
     		RopType type = analyzedCode.reverseLookup(insn).getUsedRegisterSolver(regRef).getType();
     		if (isPrimitive(type))
     			taintCombination.add(codeGen.combineTaint(auxCombinedTaint, auxCombinedTaint, regRef.getTaintRegister()));
     		else {
     			taintCombination.add(codeGen.taintClearVisited(type.type));
     			taintCombination.add(codeGen.getTaint(auxObjTaint, regRef.getTaintRegister()));
     			taintCombination.add(codeGen.combineTaint(auxCombinedTaint, auxCombinedTaint, auxObjTaint));
     		}
     	}
 
     	return new DexMacro(taintCombination);
     }
     
     private List<Pair<DexRegister, Boolean>> getDefinedRegisters(DexInstruction insn) {
     	if (insn == null)
     		return Collections.emptyList();
     	
     	List<Pair<DexRegister, Boolean>> defRegs = new ArrayList<Pair<DexRegister, Boolean>>();
     	
     	AnalyzedDexInstruction aInsn = analyzedCode.reverseLookup(insn);
     	for (DexRegister defReg : insn.lvaDefinedRegisters()) {
     		RopType type = aInsn.getDefinedRegisterSolver(defReg).getType();
 			defRegs.add(Pair.create(defReg, isPrimitive(type)));
     	}
     	
     	return defRegs;
     }
     
     private DexCodeElement defineAllRegisters(List<Pair<DexRegister, Boolean>> defRegs) {
     	List<DexCodeElement> taintAssignment = new ArrayList<DexCodeElement>(defRegs.size());
     	
     	for (Pair<DexRegister, Boolean> pair : defRegs) {
     		if (pair.getValB()) // is primitive?
     			taintAssignment.add(codeGen.setEmptyTaint(pair.getValA().getTaintRegister()));
     		else // no => set to NULL
     			taintAssignment.add(codeGen.setZero(pair.getValA().getTaintRegister()));
     	}
     	
     	return new DexMacro(taintAssignment);
     }
 
     private boolean isPrimitive(RopType type) {
 		switch(type.category) {
 		case Boolean:
 		case Byte:
 		case Char:
 		case DoubleHi:
 		case DoubleLo:
 		case Float:
 		case Integer:
 		case IntFloat:
 		case LongHi:
 		case LongLo:
 		case One:
 		case Zero:
 		case Primitive:
 		case Short:
 		case Wide:
 			return true;
 			
 		case Reference:
 		case Null:
 		case WildcardRef:
 			return false;
 			
 		default:
 			throw new AssertionError("Type of register is " + type.category.name());
 		}
     }
 }

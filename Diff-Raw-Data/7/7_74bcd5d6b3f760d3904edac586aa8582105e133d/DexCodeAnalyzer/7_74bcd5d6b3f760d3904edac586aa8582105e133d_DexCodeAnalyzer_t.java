 package com.rx201.dx.translator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 import com.rx201.dx.translator.util.DexRegisterHelper;
 
 import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
 import uk.ac.cam.db538.dexter.analysis.cfg.CfgBlock;
 import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 
 public class DexCodeAnalyzer {
 	private DexCode code;
 
     private HashMap<DexCodeElement, AnalyzedDexInstruction> instructionMap;
     private ArrayList<AnalyzedDexInstruction> instructions;
 
     private static final int NOT_ANALYZED = 0;
     private static final int ANALYZED = 1;
     
     private int analyzerState = NOT_ANALYZED;
 
     private int maxInstructionIndex;
     
     //This is a dummy instruction that occurs immediately before the first real instruction. We can initialize the
     //register types for this instruction to the parameter types, in order to have them propagate to all of its
     //successors, e.g. the first real instruction, the first instructions in any exception handlers covering the first
     //instruction, etc.
     private AnalyzedDexInstruction startOfMethod;
 
     public DexCodeAnalyzer(DexCode code) {
         this.code = code;
         maxInstructionIndex = 0;
         buildInstructionList();
     }
 
     public boolean isAnalyzed() {
         return analyzerState >= ANALYZED;
     }
 
     private void analyzeParameters() {
     	boolean isStatic = code.getParentMethod().isStatic();
     	DexPrototype prototype = code.getParentMethod().getPrototype();
     	List<DexRegister> parameterMapping = code.getParentMethod().getParameterMappedRegisters();
     	
     	for(int i=0; i<prototype.getParameterCount(isStatic); i++) {
     		DexRegisterType dexRegType = prototype.getParameterType(i, isStatic, code.getParentClass());
     		RopType regType = RopType.getRopType(dexRegType.getDescriptor());
 			int paramRegIndex = prototype.getFirstParameterRegisterIndex(i, isStatic);
 			DexRegister paramReg = parameterMapping.get(paramRegIndex);
 			
 			switch (dexRegType.getTypeSize()) {
 	        case SINGLE:
 	        	startOfMethod.defineRegister(paramReg, regType, true);
 	        	break;
 	        case WIDE:
 	        	startOfMethod.defineRegister(paramReg, regType, true);
 	        	startOfMethod.defineRegister(DexRegisterHelper.next(paramReg), regType.lowToHigh(), true);
 	        	break;
 			}
 	        	
     	}
     }
     
     // Perform type propagation.
     public void analyze() {
         assert code.getParentMethod() != null;
 
         if (analyzerState >= ANALYZED) {
             //the instructions have already been analyzed, so there is nothing to do
             return;
         }
 
         // Collect use/def information; initialise all TypeResolver sites.
         buildUseDefSets();
         
         // Initialise TypeResolver of StartOfMethod, add constraints from function declaration
         analyzeParameters();
 
         // Compute all use-def chains and link TypeSolver together accordingly
         livenessAnalysis();
         
         // Add constraints from uses and defs to TypeSolver
        typeConstraintAnalysis();
         
         analyzerState = ANALYZED;
     }
 
 
 	private void buildUseDefSets() {
 		DexInstructionAnalyzer analyzer = new DexInstructionAnalyzer(this);
 		
 		// First collect use/def information
 		for (AnalyzedDexInstruction inst : instructions) {
 			if (inst.instruction != null) {
 				analyzer.setAnalyzedInstruction(inst);
 				inst.instruction.accept(analyzer);
 			}
 		}
 		
 		
 	}
 
 	private void livenessAnalysis() {
 		for (AnalyzedDexInstruction inst : instructions) {
 			for(DexRegister usedReg : inst.getUsedRegisters()) {
 				Set<TypeSolver> definers = getDefinedSites(inst, usedReg);
 				TypeSolver master = null;
 				for(TypeSolver definer : definers) {
 					if (master == null)
 						master = definer;
 					else
 						master.unify(definer);
 				}
 				assert master != null;
 				inst.associateDefinitionSite(usedReg, master);
 			}
 		}
 		
 		// Create the register constraint graph
 		for (AnalyzedDexInstruction inst : instructions) {
 			inst.createConstraintEdges();
 		}
 	}
 	
 	private Set<TypeSolver> getDefinedSites(AnalyzedDexInstruction location, DexRegister reg) {
 		HashSet<TypeSolver> result = new HashSet<TypeSolver>();
 		
 		HashSet<AnalyzedDexInstruction> visitedNormal = new HashSet<AnalyzedDexInstruction>();
 		HashSet<AnalyzedDexInstruction> visitedException = new HashSet<AnalyzedDexInstruction>();
 		ArrayList<Pair<AnalyzedDexInstruction, Boolean>> stack = new ArrayList<Pair<AnalyzedDexInstruction, Boolean>>();
 		for(AnalyzedDexInstruction pred : location.getPredecessors() )
 			stack.add(new Pair<AnalyzedDexInstruction, Boolean>(pred, location.isExceptionPredecessor(pred)));
 		
 		while(stack.size() > 0) {
 			Pair<AnalyzedDexInstruction, Boolean> headPair = stack.remove(stack.size() - 1);
 			AnalyzedDexInstruction head = headPair.getValA();
 			Boolean isExceptionPath = headPair.getValB();
 			
 			if (isExceptionPath) {
 				if (visitedException.contains(head)) continue;
 				visitedException.add(head);
 			} else {
 				if (visitedNormal.contains(head)) continue;
 				visitedNormal.add(head);
 			}
 			
 			TypeSolver definer = head.getDefinedRegisterSolver(reg);
 			if (definer != null && (!isExceptionPath)) {
 				result.add(definer);
 			} else {
 				// If this register is also accessed here in the current path
 				// and we've obtained its definer, we can reuse the result.
 				// This would be most efficient if we perform liveness analysis
 				// from top to bottom.
 				definer = head.getUsedRegisterTypeSolver(reg);
 				if (definer != null) {
 					result.add(definer);
 				} else {
 					for(AnalyzedDexInstruction pred : head.getPredecessors() )
 						stack.add(new Pair<AnalyzedDexInstruction, Boolean>(pred, head.isExceptionPredecessor(pred)));
 				}
 			}
 		}
 		return result;
 	}
 	
	private void typeConstraintAnalysis() {
		// First add all definition constraints,
 		// then refine it with usage constraints
 		startOfMethod.initDefinitionConstraints();
 		for (AnalyzedDexInstruction inst : instructions) {
 			inst.initDefinitionConstraints();
 		}
 		for (AnalyzedDexInstruction inst : instructions) {
 			inst.propagateUsageConstraints();
 		}
 	}
 
 
 	public AnalyzedDexInstruction getStartOfMethod() {
         return startOfMethod;
     }
 
     private void buildInstructionList() {
     	instructions = new ArrayList<AnalyzedDexInstruction>();
     	instructionMap = new HashMap<DexCodeElement, AnalyzedDexInstruction>();
     	for(DexCodeElement inst : code.getInstructionList()) {
     		AnalyzedDexInstruction analyzedInst = buildFromDexCodeElement(instructions.size(), inst);
     		instructionMap.put(inst, analyzedInst);
     		instructions.add(analyzedInst);
     		
     	}
 
     	ControlFlowGraph cfg = new ControlFlowGraph(code);
     	
     	for(CfgBasicBlock bb : cfg.getBasicBlocks()) {
         	AnalyzedDexInstruction prevA = null;
         	Set<DexCodeElement> prevExceptionSuccessors = null;
         	
         	// Connect predecessor/successor within a basic block
     		for(DexCodeElement cur: bb.getInstructions()) {
     			AnalyzedDexInstruction curA = instructionMap.get(cur);
     			if (prevA != null) {
     				prevA.linkToSuccessor(curA, false);
     				// Cannot have exception path within a basic block
     				assert !prevExceptionSuccessors.contains(curA.getCodeElement());
     			}
     			prevA = curA;
     			prevExceptionSuccessors = prevA.getCodeElement().cfgGetExceptionSuccessors();
     		}
     		// Connect with successor basic block
     		for(CfgBlock nextBB : bb.getSuccessors()) {
     			if (nextBB instanceof CfgBasicBlock) {
     				DexCodeElement cur = ((CfgBasicBlock)nextBB).getFirstInstruction();
         			AnalyzedDexInstruction curA = instructionMap.get(cur);
     				prevA.linkToSuccessor(curA, prevExceptionSuccessors.contains(curA.getCodeElement()));
     			}
     		}
     	}
     	
         //override AnalyzedInstruction and provide custom implementations of some of the methods, so that we don't
         //have to handle the case this special case of instruction being null, in the main class
         startOfMethod = new AnalyzedDexInstruction(-1, null);
         
         for (CfgBlock startBB: cfg.getStartBlock().getSuccessors()) {
         	if (startBB instanceof CfgBasicBlock) {
         		AnalyzedDexInstruction realHead = instructionMap.get(((CfgBasicBlock)startBB).getFirstInstruction());
         		startOfMethod.linkToSuccessor(realHead, false);
         	}
         }
     }
 
     private AnalyzedDexInstruction buildFromDexCodeElement(int index, DexCodeElement element) {
     	if (index > maxInstructionIndex)
     		maxInstructionIndex = index;
     	if (element instanceof DexInstruction) {
     		return new AnalyzedDexInstruction(index, (DexInstruction) element);
     	} else /* DexCatch, DexCatchAll, DexLabel, DexTryBlockStart, DexTryBlockEnd */ {
     		return new AnalyzedDexInstruction(index, null, element);
     	}
 	}
 
 
     public AnalyzedDexInstruction reverseLookup(DexCodeElement element) {
     	assert element != null;
     	return instructionMap.get(element);
     }
     
     public int getMaxInstructionIndex() {
     	return maxInstructionIndex;
     }
     
 }

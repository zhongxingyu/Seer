 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 
 public abstract class DexInstruction extends DexCodeElement {
 
   public DexInstruction(DexCode methodCode) {
     super(methodCode);
   }
 
   // PARSING
 
   protected static final InstructionParsingException FORMAT_EXCEPTION = new InstructionParsingException("Unknown instruction format or opcode");
 
   // INSTRUCTION INSTRUMENTATION
 
   public void instrument(DexCode_InstrumentationState state) {
     throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have instrumentation implemented");
   }
   
   // THROWING INSTRUCTIONS
 
   @Override
   public final boolean cfgExitsMethod() {
 	val jumpTargets = this.cfgJumpTargets();
 	if (jumpTargets.isEmpty())
 		return true;
     val exceptions = this.throwsExceptions();
     if (exceptions != null)
     	for (val exception : exceptions)
     		if (throwingInsn_CanExitMethod(exception))
     			return true;
     return false;
   }
   
 //  protected final boolean throwingInsn_CanExitMethod() {
 //    return throwingInsn_CanExitMethod(
 //             DexClassType.parse("Ljava/lang/Throwable;",
 //                                getParentFile().getParsingCache()));
 //  }
 
   protected final boolean throwingInsn_CanExitMethod(DexClassType thrownExceptionType) {
     val code = getMethodCode();
     val classHierarchy = getParentFile().getClassHierarchy();
 
     for (val tryBlockEnd : code.getTryBlocks()) {
       val tryBlockStart = tryBlockEnd.getBlockStart();
 
       // check that the instruction is in this try block
       if (code.isBetween(tryBlockStart, tryBlockEnd, this)) {
 
         // if the block has CatchAll handler, it can't exit the method
         if (tryBlockStart.getCatchAllHandler() != null)
           return false;
 
         // if there is a catch block catching the exception or its ancestor,
         // it can't exit the method either
         for (val catchBlock : tryBlockStart.getCatchHandlers())
           if (classHierarchy.isAncestor(thrownExceptionType, catchBlock.getExceptionType()))
             return false;
       }
     }
 
     return true;
   }
 
   protected final Set<DexCodeElement> throwingInsn_CatchHandlers(DexClassType thrownExceptionType) {
     val set = new HashSet<DexCodeElement>();
 
     val code = getMethodCode();
     val classHierarchy = getParentFile().getClassHierarchy();
 
     for (val tryBlockEnd : code.getTryBlocks()) {
       val tryBlockStart = tryBlockEnd.getBlockStart();
 
       // check that the instruction is in this try block
       if (code.isBetween(tryBlockStart, tryBlockEnd, this)) {
 
         // if the block has CatchAll handler, it can jump to it
         val catchAllHandler = tryBlockStart.getCatchAllHandler();
         if (catchAllHandler != null)
           set.add(catchAllHandler);
 
         // similarly, add all catch blocks as possible successors
        // if they catch the given exception type or its ancestor
         for (val catchBlock : tryBlockStart.getCatchHandlers())
          if (thrownExceptionType == null || classHierarchy.isAncestor(thrownExceptionType, catchBlock.getExceptionType()))
             set.add(catchBlock);
       }
     }
 
     return set;
   }
 
   protected final Set<DexCodeElement> throwingInsn_CatchHandlers() {
     return throwingInsn_CatchHandlers(null);
   }
 
   protected final DexTryBlockEnd getSurroundingTryBlock() {
     val code = getMethodCode();
     for (val tryBlockEnd : code.getTryBlocks())
       // check that the instruction is in this try block
       if (code.isBetween(tryBlockEnd.getBlockStart(), tryBlockEnd, this))
         return tryBlockEnd;
     return null;
   }
 
   protected final List<DexCodeElement> throwingInsn_GenerateSurroundingCatchBlock(DexCodeElement[] tryBlockCode, DexCodeElement[] catchBlockCode, DexRegister regException) {
     val code = getMethodCode();
 
     val catchAll = new DexCatchAll(code);
     val tryStart = new DexTryBlockStart(code);
     tryStart.setCatchAllHandler(catchAll);
     val tryEnd = new DexTryBlockEnd(code, tryStart);
 
     val labelSucc = new DexLabel(code);
     val gotoSucc = new DexInstruction_Goto(code, labelSucc);
 
     val moveException = new DexInstruction_MoveException(code, regException);
     val throwException = new DexInstruction_Throw(code, regException);
 
     val instrumentedCode = new ArrayList<DexCodeElement>();
     instrumentedCode.add(tryStart);
     instrumentedCode.addAll(Arrays.asList(tryBlockCode));
     instrumentedCode.add(gotoSucc);
     instrumentedCode.add(tryEnd);
     instrumentedCode.add(catchAll);
     instrumentedCode.add(moveException);
     instrumentedCode.addAll(Arrays.asList(catchBlockCode));
     instrumentedCode.add(throwException);
     instrumentedCode.add(labelSucc);
 
     return instrumentedCode;
   }
   
   abstract public void accept(DexInstructionVisitor visitor);
   
   // Subclasses should overrides this if the instruction may throw exceptions during execution.
   protected DexClassType[] throwsExceptions() {
 	return null;  
   }
   
   @Override
   public boolean cfgEndsBasicBlock() {
 	DexClassType[] exceptions = throwsExceptions();
 	return  exceptions != null && exceptions.length > 0;
   }
 
   @Override
   public final Set<DexCodeElement> cfgGetSuccessors() {
 	  // uses the DexCodeElement definition of cfgGetSuccessors
 	  // (non-throwing semantics) but adds behavior after exceptions
 	  // are thrown
 	  
 	  val set = super.cfgGetSuccessors();
 	  
 	  DexClassType[] exceptions = throwsExceptions();
 	  if (exceptions != null) {
 		  for(DexClassType exception : exceptions)
 			  set.addAll(throwingInsn_CatchHandlers(exception));
 	  }
 	  
 	  return set;
   }
   
 }

 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Format.Instruction23x;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 
 public class DexInstruction_CompareWide extends DexInstruction {
 
   @Getter private final DexRegister regTo;
   @Getter private final DexRegister regSourceA1;
   @Getter private final DexRegister regSourceA2;
   @Getter private final DexRegister regSourceB1;
   @Getter private final DexRegister regSourceB2;
   @Getter private final Opcode_CompareWide insnOpcode;
 
   public DexInstruction_CompareWide(DexCode methodCode,
                                     DexRegister target,
                                     DexRegister sourceA1, DexRegister sourceA2,
                                     DexRegister sourceB1, DexRegister sourceB2,
                                     Opcode_CompareWide opcode) {
     super(methodCode);
 
     regTo = target;
     regSourceA1 = sourceA1;
     regSourceA2 = sourceA2;
     regSourceB1 = sourceB1;
     regSourceB2 = sourceB2;
     insnOpcode = opcode;
   }
 
   public DexInstruction_CompareWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
     super(methodCode);
 
     int regA, regB, regC;
 
     if (insn instanceof Instruction23x && Opcode_CompareWide.convert(insn.opcode) != null) {
 
       val insnBinaryOpWide = (Instruction23x) insn;
       regA = insnBinaryOpWide.getRegisterA();
       regB = insnBinaryOpWide.getRegisterB();
       regC = insnBinaryOpWide.getRegisterC();
 
     } else
       throw FORMAT_EXCEPTION;
 
     regTo = parsingState.getRegister(regA);
     regSourceA1 = parsingState.getRegister(regB);
     regSourceA2 = parsingState.getRegister(regB + 1);
     regSourceB1 = parsingState.getRegister(regC);
     regSourceB2 = parsingState.getRegister(regC + 1);
     insnOpcode = Opcode_CompareWide.convert(insn.opcode);
   }
 
   @Override
   public String getOriginalAssembly() {
     return insnOpcode.getAssemblyName() + " " + regTo.getOriginalIndexString()
            + ", " + regSourceA1.getOriginalIndexString() + "|" + regSourceA2.getOriginalIndexString()
            + ", " + regSourceB1.getOriginalIndexString() + "|" + regSourceB2.getOriginalIndexString();
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     val regAlloc = state.getRegisterAllocation();
     int rTarget = regAlloc.get(regTo);
     int rSourceA1 = regAlloc.get(regSourceA1);
     int rSourceA2 = regAlloc.get(regSourceA2);
     int rSourceB1 = regAlloc.get(regSourceB1);
     int rSourceB2 = regAlloc.get(regSourceB2);
 
     if (!formWideRegister(rSourceA1, rSourceA2) || !formWideRegister(rSourceB1, rSourceB2))
       return throwWideRegistersExpected();
 
     if (fitsIntoBits_Unsigned(rTarget, 8) && fitsIntoBits_Unsigned(rSourceA1, 8) && fitsIntoBits_Unsigned(rSourceB1, 8))
       return new Instruction[] { new Instruction23x(Opcode_CompareWide.convert(insnOpcode), (short) rTarget, (short) rSourceA1, (short) rSourceB1)	};
     else
       return throwNoSuitableFormatFound();
   }
 
   @Override
   public Set<DexRegister> lvaDefinedRegisters() {
     return createSet(regTo);
   }
 
   @Override
   public Set<DexRegister> lvaReferencedRegisters() {
     return createSet(regSourceA1, regSourceA2, regSourceB1, regSourceB2);
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(
              new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT),
              new GcRangeConstraint(regSourceA1, ColorRange.RANGE_8BIT),
              new GcRangeConstraint(regSourceB1, ColorRange.RANGE_8BIT));
   }
 
   @Override
   public Set<GcFollowConstraint> gcFollowConstraints() {
     return createSet(
              new GcFollowConstraint(regSourceA1, regSourceA2),
              new GcFollowConstraint(regSourceB1, regSourceB2));
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_CompareWide(
              getMethodCode(),
              mapping.get(regTo),
              mapping.get(regSourceA1),
              mapping.get(regSourceA2),
              mapping.get(regSourceB1),
              mapping.get(regSourceB2),
              insnOpcode);
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) {
     // need to combine the taint of the two wide registers (total of four) and assign that to the operation result
     val code = getMethodCode();
     code.replace(this,
                  new DexCodeElement[] {
                    this,
                   new DexInstruction_BinaryOp(code, state.getTaintRegister(regTo), state.getTaintRegister(regSourceA1), state.getTaintRegister(regSourceB1), Opcode_BinaryOp.OrInt),
                  });
   }
 }
 

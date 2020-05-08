 package de.weltraumschaf.caythe.intermediate.symboltableimpl;
 
 import de.weltraumschaf.caythe.intermediate.RoutineCode;
 import de.weltraumschaf.caythe.intermediate.Definition;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import de.weltraumschaf.caythe.intermediate.SymbolTableStack;
 import de.weltraumschaf.caythe.intermediate.TypeFactory;
 import de.weltraumschaf.caythe.intermediate.TypeSpecification;
 import java.util.ArrayList;
 
 import static de.weltraumschaf.caythe.intermediate.typeimpl.TypeFormImpl.*;
 import static de.weltraumschaf.caythe.intermediate.typeimpl.TypeKeyImpl.*;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.SymbolTableKeyImpl.*;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.DefinitionImpl.*;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.RoutineCodeImpl.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class Predefined {
     // Build in types.
     public static TypeSpecification integerType;
     public static TypeSpecification realType;
     public static TypeSpecification booleanType;
     public static TypeSpecification charType;
     public static TypeSpecification undefinedType;
     // Build type symbols.
     public static SymbolTableEntry integerId;
     public static SymbolTableEntry realId;
     public static SymbolTableEntry booleanId;
     public static SymbolTableEntry charId;
     public static SymbolTableEntry falseId;
     public static SymbolTableEntry trueId;
     // Build in function symbols.
     public static SymbolTableEntry readId;
     public static SymbolTableEntry readlnId;
     public static SymbolTableEntry writeId;
     public static SymbolTableEntry writelnId;
     public static SymbolTableEntry absId;
     public static SymbolTableEntry arctanId;
     public static SymbolTableEntry chrId;
     public static SymbolTableEntry cosId;
     public static SymbolTableEntry eofId;
     public static SymbolTableEntry eolnId;
     public static SymbolTableEntry expId;
     public static SymbolTableEntry lnId;
     public static SymbolTableEntry oddId;
     public static SymbolTableEntry ordId;
     public static SymbolTableEntry predId;
     public static SymbolTableEntry roundId;
     public static SymbolTableEntry sinId;
     public static SymbolTableEntry sqrId;
     public static SymbolTableEntry sqrtId;
     public static SymbolTableEntry succId;
     public static SymbolTableEntry truncId;
 
     public static void initialize(SymbolTableStack symbolTableStack) {
         initializeTypes(symbolTableStack);
         initializeConstants(symbolTableStack);
         initializeStandardRoutines(symbolTableStack);
     }
 
     private static void initializeTypes(SymbolTableStack symbolTableStack) {
         // Type integer.
         integerId   = symbolTableStack.enterLocal("integer");
         integerType = TypeFactory.createType(SCALAR);
         integerType.setIdentifier(integerId);
         integerId.setDefinition(DefinitionImpl.TYPE);
         integerId.setTypeSpecification(integerType);
 
         // Type real.
         realId   = symbolTableStack.enterLocal("real");
         realType = TypeFactory.createType(SCALAR);
         realType.setIdentifier(realId);
         realId.setDefinition(DefinitionImpl.TYPE);
         realId.setTypeSpecification(realType);
 
         // Type boolean.
         booleanId   = symbolTableStack.enterLocal("boolean");
         booleanType = TypeFactory.createType(ENUMERATION);
         booleanType.setIdentifier(booleanId);
         booleanId.setDefinition(DefinitionImpl.TYPE);
         booleanId.setTypeSpecification(booleanType);
 
         // Type char.
         charId   = symbolTableStack.enterLocal("char");
         charType = TypeFactory.createType(SCALAR);
         charType.setIdentifier(charId);
         charId.setDefinition(DefinitionImpl.TYPE);
         charId.setTypeSpecification(charType);
 
         // undefined type.
         undefinedType = TypeFactory.createType(SCALAR);
     }
 
     private static void initializeConstants(SymbolTableStack symbolTableStack) {
         // Boolean enumeration constant false.
         falseId = symbolTableStack.enterLocal("false");
         falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
         falseId.setTypeSpecification(booleanType);
         falseId.setAttribute(CONSTANT_VALUE, new Integer(0));
 
         // Boolean enumeration constant true.
         falseId = symbolTableStack.enterLocal("true");
         falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
         falseId.setTypeSpecification(booleanType);
         falseId.setAttribute(CONSTANT_VALUE, new Integer(1));
 
         // Add false and true to the boolean enumeration type.
         ArrayList<SymbolTableEntry> constants = new ArrayList<SymbolTableEntry>();
         constants.add(falseId);
         constants.add(trueId);
         booleanType.setAttribute(ENUMERATION_CONSTANTS, constants);
     }
 
     private static void initializeStandardRoutines(SymbolTableStack symbolTableStack) {
         readId    = enterStandard(symbolTableStack, PROCEDURE, "read", READ);
         readlnId  = enterStandard(symbolTableStack, PROCEDURE, "readln", READLN);
         writeId   = enterStandard(symbolTableStack, PROCEDURE, "write", WRITE);
         writelnId = enterStandard(symbolTableStack, PROCEDURE, "writeln", WRITELN);
 
         absId     = enterStandard(symbolTableStack, FUNCTION, "abs", ABS);
         arctanId  = enterStandard(symbolTableStack, FUNCTION, "arctan", ARCTAN);
         chrId     = enterStandard(symbolTableStack, FUNCTION, "chr", CHR);
         cosId     = enterStandard(symbolTableStack, FUNCTION, "cos", COS);
         eofId     = enterStandard(symbolTableStack, FUNCTION, "eof", EOF);
         eolnId    = enterStandard(symbolTableStack, FUNCTION, "eoln", EOLN);
         expId     = enterStandard(symbolTableStack, FUNCTION, "exp", EXP);
         lnId      = enterStandard(symbolTableStack, FUNCTION, "ln", LN);
         oddId     = enterStandard(symbolTableStack, FUNCTION, "odd", ODD);
         ordId     = enterStandard(symbolTableStack, FUNCTION, "ord", ORD);
         predId    = enterStandard(symbolTableStack, FUNCTION, "pred", PRED);
         sinId     = enterStandard(symbolTableStack, FUNCTION, "sin", SIN);
         sqrId     = enterStandard(symbolTableStack, FUNCTION, "sqr", SQR);
         sqrtId    = enterStandard(symbolTableStack, FUNCTION, "sqrt", SQRT);
         succId    = enterStandard(symbolTableStack, FUNCTION, "succ", SUCC);
         truncId   = enterStandard(symbolTableStack, FUNCTION, "trunc", TRUNC);
     }
 
     private static SymbolTableEntry enterStandard(SymbolTableStack symTabStack, Definition defn, String name, RoutineCode routineCode) {
         SymbolTableEntry procId = symTabStack.enterLocal(name);
         procId.setDefinition(defn);
         procId.setAttribute(ROUTINE_CODE, routineCode);
         return procId;
     }
 }

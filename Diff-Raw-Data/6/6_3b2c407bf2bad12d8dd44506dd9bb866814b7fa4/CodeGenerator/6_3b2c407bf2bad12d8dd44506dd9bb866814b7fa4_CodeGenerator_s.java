 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.aux.TaintConstants;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode.Parameter;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryEnd;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryStart;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.macro.DexMacro;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleAuxiliaryRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleOriginalRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexTaintRegister;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
 import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy.TypeClassification;
 import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;
 import uk.ac.cam.db538.dexter.transform.MethodCall;
 
 public final class CodeGenerator {
 
     private final AuxiliaryDex dexAux;
     private final RuntimeHierarchy hierarchy;
     private final DexTypeCache cache;
 
     private final DexClassType typeObject;
     private final DexClassType typeClass;
     private final DexArrayType typeClassArray;
     private final DexClassType typeMethod;
     private final DexClassType typeAnnotation;
     private final DexClassType typeInteger;
     private final DexClassType typeString;
     private final DexClassType typeThreadLocal;
     private final DexClassType typeThrowable;
     private final DexClassType typeStackTraceElement;
     private final DexClassType typeLog;
     private final DexClassType typeUri;
     private final DexArrayType typeIntArray;
     private final DexArrayType typeStackTraceElementArray;
 
     private final ClassDefinition defThrowable;
 
     private final MethodDefinition method_Object_getClass;
     private final MethodDefinition method_ThreadLocal_Get;
     private final MethodDefinition method_ThreadLocal_Set;
     private final MethodDefinition method_Integer_intValue;
     private final MethodDefinition method_Integer_valueOf;
     private final MethodDefinition method_Throwable_constructor;
     private final MethodDefinition method_Throwable_getStackTrace;
     private final MethodDefinition method_Throwable_printStackTrace;
     private final MethodDefinition method_StackTraceElement_getClassName;
     private final MethodDefinition method_Class_forName;
     private final MethodDefinition method_Class_getAnnotation;
     private final MethodDefinition method_Class_getMethod;
     private final MethodDefinition method_Class_getDeclaredMethod;
     private final MethodDefinition method_Class_getSuperclass;
     private final MethodDefinition method_Method_getAnnotation;
     private final MethodDefinition method_Log_d;
     private final MethodDefinition method_Uri_toString;;
 
     private int regAuxId;
     private int labelId;
     private int catchId;
     private int tryId;
 
     public CodeGenerator(AuxiliaryDex dexAux) {
         this.dexAux = dexAux;
 
         this.hierarchy = dexAux.getHierarchy();
         this.cache = hierarchy.getTypeCache();
 
         this.typeObject = DexClassType.parse("Ljava/lang/Object;", cache);
         this.typeClass = DexClassType.parse("Ljava/lang/Class;", cache);
         this.typeClassArray = DexArrayType.parse("[Ljava/lang/Class;", cache);
         this.typeMethod = DexClassType.parse("Ljava/lang/reflect/Method;", cache);
         this.typeAnnotation = DexClassType.parse("Ljava/lang/annotation/Annotation;", cache);
         this.typeInteger = DexClassType.parse("Ljava/lang/Integer;", cache);
         this.typeString = DexClassType.parse("Ljava/lang/String;", cache);
         this.typeThreadLocal = DexClassType.parse("Ljava/lang/ThreadLocal;", cache);
         this.typeThrowable = DexClassType.parse("Ljava/lang/Throwable;", cache);
         this.typeStackTraceElement = DexClassType.parse("Ljava/lang/StackTraceElement;", cache);
         this.typeIntArray = DexArrayType.parse("[I", cache);
         this.typeStackTraceElementArray = DexArrayType.parse("[Ljava/lang/StackTraceElement;", cache);
         this.typeLog = DexClassType.parse("Landroid/util/Log;", cache);
         this.typeUri = DexClassType.parse("Landroid/net/Uri;", cache);
 
         this.defThrowable = hierarchy.getClassDefinition(typeThrowable);
 
         DexPrototype prototype_void_to_void = DexPrototype.parse(cache.getCachedType_Void(), null, cache);
         DexPrototype prototype_void_to_Object = DexPrototype.parse(typeObject, null, cache);
         DexPrototype prototype_void_to_Class = DexPrototype.parse(typeClass, null, cache);
         DexPrototype prototype_void_to_String = DexPrototype.parse(typeString, null, cache);
         DexPrototype prototype_void_to_TraceElemArray = DexPrototype.parse(typeStackTraceElementArray, null, cache);
         DexPrototype prototype_Object_to_void = DexPrototype.parse(cache.getCachedType_Void(), Arrays.asList(typeObject), cache);
         DexPrototype prototype_void_to_int = DexPrototype.parse(cache.getCachedType_Integer(), null, cache);
         DexPrototype prototype_int_to_Integer = DexPrototype.parse(typeInteger, Arrays.asList(cache.getCachedType_Integer()), cache);
         DexPrototype prototype_String_to_Class = DexPrototype.parse(typeClass, Arrays.asList(typeString), cache);
         DexPrototype prototype_Class_to_Annotation = DexPrototype.parse(typeAnnotation, Arrays.asList(typeClass), cache);
         DexPrototype prototype_String_ClassArray_to_Method = DexPrototype.parse(typeMethod, Arrays.asList(typeString, typeClassArray), cache);
         DexPrototype prototype_String_String_to_int = DexPrototype.parse(cache.getCachedType_Integer(), Arrays.asList(typeString, typeString), cache);
 
         DexMethodId methodId_getClass_void_to_Class = DexMethodId.parseMethodId("getClass", prototype_void_to_Class, cache);
         DexMethodId methodId_getSuperclass_void_to_Class = DexMethodId.parseMethodId("getSuperclass", prototype_void_to_Class, cache);
         DexMethodId methodId_get_void_to_Object = DexMethodId.parseMethodId("get", prototype_void_to_Object, cache);
         DexMethodId methodId_set_Object_to_void = DexMethodId.parseMethodId("set", prototype_Object_to_void, cache);
         DexMethodId methodId_intValue_void_to_int = DexMethodId.parseMethodId("intValue", prototype_void_to_int, cache);
         DexMethodId methodId_valueOf_int_to_Integer = DexMethodId.parseMethodId("valueOf", prototype_int_to_Integer, cache);
         DexMethodId methodId_constructor_void_to_void = DexMethodId.parseMethodId("<init>", prototype_void_to_void, cache);
         DexMethodId methodId_getStackTrace_void_to_TraceElemArray = DexMethodId.parseMethodId("getStackTrace", prototype_void_to_TraceElemArray, cache);
         DexMethodId methodId_printStackTrace_void_to_void = DexMethodId.parseMethodId("printStackTrace", prototype_void_to_void, cache);
         DexMethodId methodId_getClassName_void_to_String = DexMethodId.parseMethodId("getClassName", prototype_void_to_String, cache);
         DexMethodId methodId_forName_String_to_Class = DexMethodId.parseMethodId("forName", prototype_String_to_Class, cache);
         DexMethodId methodId_getAnnotation_Class_to_Annotation = DexMethodId.parseMethodId("getAnnotation", prototype_Class_to_Annotation, cache);
         DexMethodId methodId_getMethod_String_ClassArray_to_Method = DexMethodId.parseMethodId("getMethod", prototype_String_ClassArray_to_Method, cache);
         DexMethodId methodId_getDeclaredMethod_String_ClassArray_to_Method = DexMethodId.parseMethodId("getDeclaredMethod", prototype_String_ClassArray_to_Method, cache);
         DexMethodId methodId_d_String_String_to_int = DexMethodId.parseMethodId("d", prototype_String_String_to_int, cache);
         DexMethodId methodId_toString_void_to_String = DexMethodId.parseMethodId("toString", prototype_void_to_String, cache);
 
         this.method_Object_getClass = lookupMethod(typeObject, methodId_getClass_void_to_Class);
         this.method_ThreadLocal_Get = lookupMethod(typeThreadLocal, methodId_get_void_to_Object);
         this.method_ThreadLocal_Set = lookupMethod(typeThreadLocal, methodId_set_Object_to_void);
         this.method_Integer_intValue = lookupMethod(typeInteger, methodId_intValue_void_to_int);
         this.method_Integer_valueOf = lookupMethod(typeInteger, methodId_valueOf_int_to_Integer);
         this.method_Throwable_constructor = lookupMethod(typeThrowable, methodId_constructor_void_to_void);
         this.method_Throwable_getStackTrace = lookupMethod(typeThrowable, methodId_getStackTrace_void_to_TraceElemArray);
         this.method_Throwable_printStackTrace = lookupMethod(typeThrowable, methodId_printStackTrace_void_to_void);
         this.method_StackTraceElement_getClassName = lookupMethod(typeStackTraceElement, methodId_getClassName_void_to_String);
         this.method_Class_forName = lookupMethod(typeClass, methodId_forName_String_to_Class);
         this.method_Class_getAnnotation = lookupMethod(typeClass, methodId_getAnnotation_Class_to_Annotation);
         this.method_Class_getMethod = lookupMethod(typeClass, methodId_getMethod_String_ClassArray_to_Method);
         this.method_Class_getDeclaredMethod = lookupMethod(typeClass, methodId_getDeclaredMethod_String_ClassArray_to_Method);
         this.method_Class_getSuperclass = lookupMethod(typeClass, methodId_getSuperclass_void_to_Class);
         this.method_Method_getAnnotation = lookupMethod(typeMethod, methodId_getAnnotation_Class_to_Annotation);
         this.method_Log_d = lookupMethod(typeLog, methodId_d_String_String_to_int);
         this.method_Uri_toString = lookupMethod(typeUri, methodId_toString_void_to_String);
     }
 
     private MethodDefinition lookupMethod(DexReferenceType classType, DexMethodId methodId) {
         MethodDefinition def = hierarchy.getClassDefinition(classType).getMethod(methodId);
 
         if (def == null)
             throw new Error("Cannot find method " + methodId + " in class " + classType);
 
         return def;
     }
 
     public void resetAsmIds() {
         regAuxId = 0;
         labelId = 0;
         catchId = 0;
         tryId = 0;
         
         cachedAuxRegs = new ArrayList<DexSingleAuxiliaryRegister>();
     }
     
     private List<DexSingleAuxiliaryRegister> cachedAuxRegs;
     private int usedAuxRegs;
     
     public void reuseAuxRegs() {
     	usedAuxRegs = 0;
     }
 
     public DexSingleAuxiliaryRegister auxReg() {
     	// add new aux reg if necessary
     	while (usedAuxRegs >= cachedAuxRegs.size())
     		cachedAuxRegs.add(new DexSingleAuxiliaryRegister(regAuxId++));
         return cachedAuxRegs.get(usedAuxRegs++);
     }
 
     public DexLabel label() {
         return new DexLabel(labelId++);
     }
 
     public DexCatchAll catchAll() {
         return new DexCatchAll(catchId++);
     }
 
     public DexTryStart tryBlock(DexCatchAll ctch) {
         DexTryEnd tryEnd = new DexTryEnd(tryId++);
         DexTryStart tryStart = new DexTryStart(tryEnd, ctch, null);
         return tryStart;
     }
 
     public DexMacro initTaints(DexCode code, boolean internalOrigin) {
         DexSingleAuxiliaryRegister auxPrimitiveParamArray = auxReg();
         DexSingleAuxiliaryRegister auxReferenceParamArray = auxReg();
         DexSingleAuxiliaryRegister auxAddedTaint = auxReg();
         DexSingleAuxiliaryRegister auxIndex = auxReg();
 
         List<Parameter> args = code.getParameters();
         List<DexCodeElement> insns = new ArrayList<DexCodeElement>();
         
         if (internalOrigin) {
         	insns.add(getPrimitiveParamArray(auxPrimitiveParamArray));
         	insns.add(getReferenceParamArray(auxReferenceParamArray));
         }
         
         insns.add(setEmptyTaint(auxAddedTaint));
         
         boolean firstArgOfConstructor = code.isConstructor();
 
         int i = 0;
         for (Parameter arg : args) {
         	
            DexSingleRegister argRegTaint = arg.getRegister().getTaintRegister();
             DexRegisterType argType = arg.getType();
             
     		insns.add(constant(auxIndex, i++));
     		
     		if (firstArgOfConstructor && !internalOrigin) {
     			firstArgOfConstructor = false;
     			
     			insns.add(taintCreate_Internal_Undefined(arg.getRegister().getTaintRegister()));
     		
     		} else if (argType instanceof DexPrimitiveType) {
 
             	if (internalOrigin)
             		insns.add(new DexInstruction_ArrayGet(argRegTaint, auxPrimitiveParamArray, auxIndex, Opcode_GetPut.IntFloat, hierarchy));
             	else
             		insns.add(combineTaint(argRegTaint, auxAddedTaint));
             	
             } else if (argType instanceof DexReferenceType) {
             	
                 DexReferenceType argTypeRef = (DexReferenceType) argType;
                 
             	if (internalOrigin) {
             		insns.add(new DexInstruction_ArrayGet(argRegTaint, auxReferenceParamArray, auxIndex, Opcode_GetPut.Object, hierarchy));
            		insns.add(cast(argRegTaint, (DexReferenceType) taintType(argTypeRef)));
             	} else
             		insns.add(taintLookup(argRegTaint, (DexSingleRegister) arg.getRegister(), auxAddedTaint, hierarchy.classifyType(argTypeRef)));
                 
             } else
             	throw new Error();
 
         }
 
         return new DexMacro(insns);
     }
 
     public DexMacro setParamTaints(List<DexRegister> argRegs, DexPrototype prototype, DexReferenceType calledClass, boolean isStatic) {
         DexSingleAuxiliaryRegister auxPrimitiveParamArray = auxReg();
         DexSingleAuxiliaryRegister auxReferenceParamArray = auxReg();
         DexSingleAuxiliaryRegister auxIndex = auxReg();
 
         int argCount = prototype.getParameterCount(isStatic); 
         List<DexCodeElement> insns = new ArrayList<DexCodeElement>(argCount * 3);
         
         insns.add(getPrimitiveParamArray(auxPrimitiveParamArray));
         insns.add(getReferenceParamArray(auxReferenceParamArray));
         
         for (int i = 0; i < argCount; i++) {
         	insns.add(constant(auxIndex, i));
         	
         	DexRegister regArg = argRegs.get(i);
         	DexTaintRegister regArgTaint = regArg.getTaintRegister();
         	DexRegisterType argType = prototype.getParameterType(i, isStatic, calledClass);
 
         	if (argType instanceof DexPrimitiveType)
         		insns.add(new DexInstruction_ArrayPut(regArgTaint, auxPrimitiveParamArray, auxIndex, Opcode_GetPut.IntFloat, hierarchy));
         	else if (argType instanceof DexReferenceType)
     			insns.add(new DexInstruction_ArrayPut(regArgTaint, auxReferenceParamArray, auxIndex, Opcode_GetPut.Object, hierarchy));
         	else
         		throw new Error();
         }
         
         return new DexMacro(insns);
     }
 
     private DexMacro getPrimitiveParamArray(DexSingleRegister regTo) {
     	DexSingleAuxiliaryRegister auxThreadLocal = auxReg();
     	
         return new DexMacro(
                    sget(auxThreadLocal, dexAux.getField_CallPrimitiveParamTaint().getFieldDef()),
                    invoke_result_obj(regTo, method_ThreadLocal_Get, auxThreadLocal),
                    cast(regTo, typeIntArray));
     }
 
     private DexMacro getReferenceParamArray(DexSingleRegister regTo) {
     	DexSingleAuxiliaryRegister auxThreadLocal = auxReg();
     	
         return new DexMacro(
                    sget(auxThreadLocal, dexAux.getField_CallReferenceParamTaint().getFieldDef()),
                    invoke_result_obj(regTo, method_ThreadLocal_Get, auxThreadLocal),
                    cast(regTo, dexAux.getArraytype_Taint()));
     }
 
     public DexMacro getResultPrimitiveTaint(DexTaintRegister regTo) {
         DexSingleAuxiliaryRegister auxReg1 = auxReg(), auxReg2 = auxReg();
 
         return new DexMacro(
                    sget(auxReg1, dexAux.getField_CallPrimitiveResultTaint().getFieldDef()),
                    invoke_result_obj(auxReg2, method_ThreadLocal_Get, auxReg1),
                    cast(auxReg2, typeInteger),
                    invoke_result_prim(regTo, method_Integer_intValue, auxReg2));
     }
 
     public DexMacro getResultReferenceTaint(DexTaintRegister regTo, DexReferenceType taintType) {
     	DexSingleAuxiliaryRegister auxResult = auxReg();
     	
         return new DexMacro(
         		   sget(auxResult, dexAux.getField_CallReferenceResultTaint().getFieldDef()),
         		   invoke_result_obj(regTo, method_ThreadLocal_Get, auxResult),
         		   cast(regTo, taintType));
     }
 
     public DexMacro setResultPrimitiveTaint(DexTaintRegister regFrom) {
         DexSingleAuxiliaryRegister auxReg1 = auxReg(), auxReg2 = auxReg();
 
         return new DexMacro(
         		   invoke_result_obj(auxReg1, method_Integer_valueOf, regFrom),
         		   sget(auxReg2, dexAux.getField_CallPrimitiveResultTaint().getFieldDef()),
         		   invoke(method_ThreadLocal_Set, auxReg2, auxReg1));
     }
 
     public DexMacro setResultReferenceTaint(DexTaintRegister regFrom) {
     	DexSingleRegister auxReg = auxReg();
     	
         return new DexMacro(
         		   sget(auxReg, dexAux.getField_CallReferenceResultTaint().getFieldDef()),
         		   invoke(method_ThreadLocal_Set, auxReg, regFrom));
     }
 
     public DexCodeElement setInternalCallFlag() {
     	return invoke(dexAux.getMethod_Call_SetInternalCall());
     }
 
     public DexCodeElement isInternalCall(DexSingleRegister regResult) {
     	return invoke_result_prim(regResult, dexAux.getMethod_Call_IsInternalCall());
     }
 
     public DexCodeElement prepareExternalCall(final DexSingleAuxiliaryRegister regCombinedTaint, DexInstruction_Invoke insnInvoke) {
         for (DexRegister regArg : insnInvoke.getArgumentRegisters())
             if (regArg.equals(regCombinedTaint) || regArg.getTaintRegister().equals(regCombinedTaint))
                 throw new Error("Conflicting registers used");
 
         final List<DexCodeElement> insns = new ArrayList<DexCodeElement>();
         final DexSingleAuxiliaryRegister regAux = auxReg();
 
         // COMBINE TAINT OF ALL PARAMETERS
 
         // Initialize the combined taint variable
         if (countValidParameters(insnInvoke) == 0) {
         	insns.add(setEmptyTaint(regCombinedTaint));
         } else {
 
             // Clear the TaintInternal VisitedSet (only needs to be done once per traversal)
             insns.add(taintClearVisited());
 
             // Get and combine the taint of each parameter
             // (skip the first argument if method a constructor)
             forEachValidParameter(insnInvoke, new ParamCallback() {
             	private boolean first = true;
             	
                 @Override
                 public void apply(DexRegister regParam, DexRegisterType typeParam) {
                     DexTaintRegister regArgTaint = regParam.getTaintRegister();
                     if (isPrimitive(typeParam)) {
                     	if (first) {
                     		insns.add(move_prim(regCombinedTaint, regArgTaint));
                     		first = false;
                     	} else
                     		insns.add(combineTaint(regCombinedTaint, regCombinedTaint, regArgTaint));
                     } else {
                         if (first) {
                         	insns.add(getTaint(regCombinedTaint, regArgTaint));
                         	first = false;
                         } else {
                         	insns.add(getTaint(regAux, regArgTaint));
                         	insns.add(combineTaint(regCombinedTaint, regCombinedTaint, regAux));
                         }
                     }
                 }
             });
 
             // DISTRIBUTE TAINT TO ALL MUTABLE NON-NULL PARAMETERS
 
             if ((countMutableParameters(insnInvoke) > 0) && 
         		(countValidParameters(insnInvoke) > 1)) /* no need to redistribute if there is only one param */ { 
 
                 // Clear the TaintInternal VisitedSet (only needs to be done once per traversal)
                 insns.add(new DexInstruction_Invoke(dexAux.getMethod_TaintInternal_ClearVisited(), null, hierarchy));
 
                 // Set the taint to each mutable parameter
                 // (skip the first argument if method a constructor)
                 forEachValidParameter(insnInvoke, new ParamCallback() {
                     @Override
                     public void apply(DexRegister regParam, DexRegisterType typeParam) {
                         if (isMutable(typeParam))
                             insns.add(setTaint(regCombinedTaint, regParam.getTaintRegister()));
                     }
                 });
 
             }
 
         }
 
         return new DexMacro(insns);
     }
 
     public DexCodeElement combineArgumentsTaint(DexSingleRegister regCombinedTaint, DexInstruction_FilledNewArray insnFilledArray) {
     	DexSingleRegister auxObjectTaint = auxReg();
     	
         for (DexRegister regArg : insnFilledArray.getArgumentRegisters())
             if (regArg.equals(regCombinedTaint) || regArg.getTaintRegister().equals(regCombinedTaint))
                 throw new Error("Conflicting registers used");
 
         final List<DexCodeElement> insns = new ArrayList<DexCodeElement>();
 
         // Initialize the combined taint variable
         insns.add(setEmptyTaint(regCombinedTaint));
 
         // Get and combine the taint of each parameter
         for (DexSingleRegister regArg : insnFilledArray.getArgumentRegisters()) {
         	if (insnFilledArray.getArrayType().getElementType() instanceof DexPrimitiveType)
         		insns.add(combineTaint(regCombinedTaint, regCombinedTaint, regArg.getTaintRegister()));
         	else {
         		insns.add(getTaint(auxObjectTaint, regArg.getTaintRegister()));
         		insns.add(combineTaint(regCombinedTaint, regCombinedTaint, auxObjectTaint));
         	}
         }
 
         return new DexMacro(insns);
     }
 
     public DexCodeElement taintClearVisited() {
         return new DexInstruction_Invoke(dexAux.getMethod_TaintInternal_ClearVisited(), null, hierarchy);
     }
 
     private boolean needsTaintClearVisited(DexRegisterType type) {
     	switch (hierarchy.classifyType(type)) {
         case REF_UNDECIDABLE:
         case REF_INTERNAL:
         case ARRAY_REFERENCE:
             return true;
         case ARRAY_PRIMITIVE:
         case REF_EXTERNAL:
             return false;
         default:
             throw new UnsupportedOperationException();
         }
     }
 
     public DexCodeElement taintClearVisited(DexRegisterType type) {
         if (type != null && needsTaintClearVisited(type))
             return taintClearVisited();
         else
             return empty();
     }
 
     public DexCodeElement taintClearVisited(DexRegisterType type1, DexRegisterType type2) {
         if (needsTaintClearVisited(type1) || needsTaintClearVisited(type2))
             return taintClearVisited();
         else
             return empty();
     }
 
     private static interface ParamCallback {
         public void apply(DexRegister regParam, DexRegisterType typeParam);
     }
 
     private void forEachValidParameter(DexInstruction_Invoke insnInvoke, ParamCallback callback) {
         DexReferenceType clazz = insnInvoke.getClassType();
         DexPrototype prototype = insnInvoke.getMethodId().getPrototype();
         List<DexRegister> regArgs = insnInvoke.getArgumentRegisters();
         boolean isStatic = isStatic(insnInvoke);
 
         int startParamId = (isConstructor(insnInvoke) ? 1 : 0);
 
         for (int paramId = startParamId; paramId < regArgs.size(); paramId++) {
             DexRegister regArg = regArgs.get(paramId);
             DexRegisterType argType = prototype.getParameterType(paramId, isStatic, clazz);
 
             callback.apply(regArg, argType);
         }
     }
 
     private static abstract class ParamCounter implements ParamCallback {
         private int count = 0;
         protected void inc() {
             count++;
         };
         public int getCount() {
             return count;
         }
     }
 
     private int countValidParameters(DexInstruction_Invoke insnInvoke) {
         ParamCounter counter = new ParamCounter() {
             @Override
             public void apply(DexRegister regParam, DexRegisterType typeParam) {
                 inc();
             }
         };
         forEachValidParameter(insnInvoke, counter);
         return counter.getCount();
     }
 
     private int countMutableParameters(DexInstruction_Invoke insnInvoke) {
         ParamCounter counter = new ParamCounter() {
             @Override
             public void apply(DexRegister regParam, DexRegisterType typeParam) {
                 if (isMutable(typeParam))
                     inc();
             }
         };
         forEachValidParameter(insnInvoke, counter);
         return counter.getCount();
     }
 
     public DexCodeElement finishExternalCall(DexSingleAuxiliaryRegister regCombinedTaint, DexInstruction_Invoke insnInvoke, DexInstruction_MoveResult insnMoveResult) {
         DexPrototype prototype = insnInvoke.getMethodId().getPrototype();
         List<DexRegister> regArgs = insnInvoke.getArgumentRegisters();
         boolean isConstructor = isConstructor(insnInvoke);
         boolean hasResult = insnMoveResult != null;
 
         assert(!isConstructor || !hasResult); // constructors cannot have a result
 
         if (isConstructor) {
             DexSingleRegister regObject = (DexSingleOriginalRegister) regArgs.get(0);
             return taintDefineExternal(regObject, regCombinedTaint);
         }
 
         if (!hasResult)
             return empty();
 
         if (isPrimitive((DexRegisterType) prototype.getReturnType()))
             return combineTaint(insnMoveResult.getRegTo(), regCombinedTaint);
         else {
             DexSingleRegister regTo = (DexSingleRegister) insnMoveResult.getRegTo();
             DexReferenceType returnType = (DexReferenceType) prototype.getReturnType();
             return taintLookup(regTo.getTaintRegister(), regTo, regCombinedTaint, hierarchy.classifyType(returnType));
         }
     }
     
     public DexCodeElement getClassObject(DexSingleRegister regTo, DexSingleRegister regObject) {
         return invoke_result_obj(regTo, method_Object_getClass, regObject);
     }
     
     public DexCodeElement getMethodObject(DexSingleRegister regTo, DexSingleRegister regObject, MethodCall methodCall) {
         DexInstruction_Invoke insnInvoke = methodCall.getInvoke();
         DexMethodId mid = insnInvoke.getMethodId();
         DexPrototype prototype = mid.getPrototype();
 
         DexSingleRegister regClassObject = auxReg();
         DexSingleRegister regMethodName = auxReg();
         DexSingleRegister regTemp = auxReg();
         DexSingleRegister regMethodArgumentsArray = auxReg();
         DexSingleRegister regParamType = auxReg();
 
         // we're using TRUE everywhere for isStatic because the class argument
         // is not to be included in the parameter type list
 
         int paramCount = prototype.getParameterCount(true);
 
         // generate type array initialization
 
         List<DexCodeElement> paramTypeInit = new ArrayList<DexCodeElement>(paramCount * 3);
         for (int i = 0; i < paramCount; i++) {
             DexRegisterType paramType = prototype.getParameterType(i, true, null);
 
             // load the index
             paramTypeInit.add(new DexInstruction_Const(regTemp, i, hierarchy));
 
             // load the param type Class object
             if (paramType instanceof DexPrimitiveType) {
                 StaticFieldDefinition fieldDef = ((DexPrimitiveType) paramType).getPrimitiveClassConstantField(hierarchy);
                 paramTypeInit.add(new DexInstruction_StaticGet(regParamType, fieldDef, hierarchy));
             } else
                 paramTypeInit.add(new DexInstruction_ConstClass(regParamType, (DexReferenceType) paramType, hierarchy));
 
             // store it in the array
             paramTypeInit.add(new DexInstruction_ArrayPut(regParamType, regMethodArgumentsArray, regTemp, Opcode_GetPut.Object, hierarchy));
         }
 
         // the implementation differs for public methods and the rest
         BaseClassDefinition classDef = hierarchy.getBaseClassDefinition(insnInvoke.getClassType());
         MethodDefinition methodDef = classDef.getSomeMethodImplementation(insnInvoke.getMethodId(), insnInvoke.getCallType());
 
         DexCodeElement invokeGetMethod;
         if (methodDef.isPublic())
             invokeGetMethod = invoke_result_obj(regTo, method_Class_getMethod, regClassObject, regMethodName, regMethodArgumentsArray);
         else {
             DexCatchAll catchBlock = catchAll();
             DexTryStart tryStart = tryBlock(catchBlock);
             DexLabel labelBefore = label();
             DexLabel labelAfter = label();
 
             DexSingleRegister regCurrentClassObject = auxReg();
 
             invokeGetMethod = new DexMacro(
                 // copy the object class reference
                 move_obj(regCurrentClassObject, regClassObject),
 
                 // try to acquire the Method object
                 labelBefore,
                 tryStart,
                 invoke_result_obj(regTo, method_Class_getDeclaredMethod, regCurrentClassObject, regMethodName, regMethodArgumentsArray),
                 jump(labelAfter),
                 tryStart.getEndMarker(),
 
                 // move to superclass if failed and try again
                 catchBlock,
                 invoke_result_obj(regCurrentClassObject, method_Class_getSuperclass, regCurrentClassObject),
                 jump(labelBefore),
 
                 labelAfter);
         }
 
         return new DexMacro(
                    // create method-argument array
                    new DexInstruction_Const(regTemp, paramCount, hierarchy),
                    new DexInstruction_NewArray(regMethodArgumentsArray, regTemp, typeClassArray, hierarchy),
                    // fill it with argument types
                    new DexMacro(paramTypeInit),
                    // get the Class object
                    getClassObject(regClassObject, regObject),
                    // load the method name
                    new DexInstruction_ConstString(regMethodName, insnInvoke.getMethodId().getName(), hierarchy),
                    // find the method object
                    invokeGetMethod);
     }
 
     
     public DexCodeElement getMethodAnnotation(DexSingleRegister regTo, MethodCall methodCall) {
     	assert(methodCall.getInvoke().getCallType() != Opcode_Invoke.Static);
     	
     	DexSingleRegister regObject = (DexSingleRegister) methodCall.getInvoke().getArgumentRegisters().get(0);
     	DexSingleRegister regAnnoClass = auxReg();
     	DexSingleRegister regMethodObject = auxReg();
     	
     	return new DexMacro(
     			// lookup the Method object
     			getMethodObject(regMethodObject, regObject, methodCall),
     			// load the annotation class object
     			constant(regAnnoClass, dexAux.getAnno_InternalMethod()),
     			// invoke the getAnnotation method
     			invoke_result_obj(regTo, method_Method_getAnnotation, regMethodObject, regAnnoClass));
     }
     
     public DexCodeElement getQueryTaint(DexSingleRegister regTo, DexSingleRegister regUri) {
     	return new DexMacro(
     			invoke_result_obj(regTo, method_Uri_toString, regUri),
     			invoke_result_obj(regTo, dexAux.getMethod_TaintConstants_QueryTaint(), regTo));
     }
     
     public DexCodeElement getServiceTaint(DexSingleRegister regTo, DexSingleRegister regStringName) {
     	return new DexMacro(
     			invoke_result_obj(regTo, dexAux.getMethod_TaintConstants_ServiceTaint(), regStringName));
     }
 
     public DexCodeElement logLeakage(DexSingleRegister regTaint, String leakType) {
     	DexSingleRegister auxLeakType = auxReg();
     	return new DexMacro(
     			constant(auxLeakType, leakType),
     			invoke(dexAux.getMethod_TaintConstants_LogLeakage(), regTaint, auxLeakType));
     }
 
     public DexCodeElement taintDefineExternal(DexSingleRegister regObject, DexSingleRegister regInitialTaint) {
         return invoke(dexAux.getMethod_Assigner_DefineExternal(), regObject, regObject.getTaintRegister(), regInitialTaint);
     }
 
     public DexCodeElement taintCreate_External_Undefined(DexSingleRegister regTo, DexSingleRegister regType) {
     	return invoke_result_obj(regTo, dexAux.getMethod_Assigner_NewExternal_Undefined(), regType);    	
     }
     
     public DexCodeElement taintCreate_External(DexSingleRegister regObject, DexSingleRegister regTaint) {
     	return taintCreate_External(regObject.getTaintRegister(), regObject, regTaint);
     }
 
     public DexCodeElement taintCreate_External(DexSingleRegister regTo, DexSingleRegister regObject, DexSingleRegister regTaint) {
         return invoke_result_obj(regTo, dexAux.getMethod_Assigner_NewExternal(), regObject, regTaint);
     }
     
     public DexCodeElement taintCreate_External_Null(DexSingleRegister regTo, DexSingleRegister regTaint) {
         return invoke_result_obj(regTo, dexAux.getMethod_Assigner_NewExternal_Null(), regTaint);
     }
     
     public DexCodeElement taintCreate_Internal_Undefined(DexSingleRegister regTo) {
     	return invoke_result_obj(regTo, dexAux.getMethod_Assigner_NewInternal_Undefined());    	
     }
 
     public DexCodeElement taintCreate_Internal_Null(DexSingleRegister regTo, DexSingleRegister regTaint) {
         return new DexMacro(
                    new DexInstruction_Invoke(dexAux.getMethod_Assigner_NewInternal_Null(), Arrays.asList(regTaint), hierarchy),
                    new DexInstruction_MoveResult(regTo, true, hierarchy));
     }
 
     public DexCodeElement taintCreate_ArrayPrimitive(DexSingleRegister regObject, DexSingleRegister regLength, DexSingleRegister regLengthTaint) {
     	return taintCreate_ArrayPrimitive(regObject.getTaintRegister(), regObject, regLength, regLengthTaint);
     }
     
     public DexCodeElement taintCreate_ArrayPrimitive(DexSingleRegister regTo, DexSingleRegister regObject, DexSingleRegister regLength, DexSingleRegister regLengthTaint) {
         return new DexMacro(
                    new DexInstruction_Invoke(dexAux.getMethod_Assigner_NewArrayPrimitive(), Arrays.asList(regObject, regLength, regLengthTaint), hierarchy),
                    new DexInstruction_MoveResult(regTo, true, hierarchy));
     }
 
     public DexCodeElement taintCreate_ArrayReference(DexSingleRegister regObject, DexSingleRegister regLength, DexSingleRegister regLengthTaint) {
     	return taintCreate_ArrayReference(regObject.getTaintRegister(), regObject, regLength, regLengthTaint);
     }
     
     public DexCodeElement taintCreate_ArrayReference(DexSingleRegister regTo, DexSingleRegister regObject, DexSingleRegister regLength, DexSingleRegister regLengthTaint) {
         return new DexMacro(
                    new DexInstruction_Invoke(dexAux.getMethod_Assigner_NewArrayReference(), Arrays.asList(regObject, regLength, regLengthTaint), hierarchy),
                    new DexInstruction_MoveResult(regTo, true, hierarchy));
     }
     
     public DexCodeElement taintSetPendingDefinition(DexSingleRegister regObjectTaint, DexSingleRegister regInitTaint) {
     	return invoke(dexAux.getMethod_Assigner_SetConstructedSuperTaint(), regObjectTaint, regInitTaint);
     }
 
     public DexCodeElement taintErasePendingDefinition() {
     	return invoke(dexAux.getMethod_Assigner_EraseConstructedSuperTaint());
     }
 
     public DexCodeElement taintNull(DexSingleRegister regTo, DexSingleRegister regNullObject, DexSingleRegister regNullTaint, TypeClassification type) {
         switch (type) {
         case REF_UNDECIDABLE:
         case REF_EXTERNAL:
         	return taintCreate_External_Null(regTo, regNullTaint);
         case REF_INTERNAL:
         	return taintCreate_Internal_Null(regTo, regNullTaint);
         case ARRAY_PRIMITIVE:
         {
         	DexSingleRegister auxZero = auxReg(); 
         	return new DexMacro(
     			setEmptyTaint(auxZero),
     			taintCreate_ArrayPrimitive(regTo, regNullObject, auxZero, regNullTaint));
         }
         case ARRAY_REFERENCE:
         {
         	DexSingleRegister auxZero = auxReg(); 
         	return new DexMacro(
     			setEmptyTaint(auxZero),
     			taintCreate_ArrayReference(regTo, regNullObject, auxZero, regNullTaint));
         }
         default:
             throw new Error();
         }
     }
 
     public DexCodeElement taintNull(DexSingleRegister regNullObject, DexSingleRegister regNullTaint, TypeClassification type) {
         return taintNull(regNullObject.getTaintRegister(), regNullObject, regNullTaint, type);
     }
 
     public DexCodeElement taintNull(DexSingleRegister regNullObject, DexSingleRegister regNullTaint, DexReferenceType type) {
         return taintNull(regNullObject, regNullTaint, hierarchy.classifyType(type));
     }
     
     public DexCodeElement taintLookup(DexSingleRegister regTo, DexSingleRegister regObject, DexSingleRegister regAddedTaint, TypeClassification type) {
         DexMethod lookupMethod;
         switch (type) {
         case REF_EXTERNAL:
             lookupMethod = dexAux.getMethod_Assigner_LookupExternal();
             break;
         case REF_INTERNAL:
             lookupMethod = dexAux.getMethod_Assigner_LookupInternal();
             break;
         case REF_UNDECIDABLE:
             lookupMethod = dexAux.getMethod_Assigner_LookupUndecidable();
             break;
         case ARRAY_PRIMITIVE:
             lookupMethod = dexAux.getMethod_Assigner_LookupArrayPrimitive();
             break;
         case ARRAY_REFERENCE:
             lookupMethod = dexAux.getMethod_Assigner_LookupArrayReference();
             break;
         default:
             throw new Error();
         }
 
         return invoke_result_obj(regTo, lookupMethod, regObject, regAddedTaint);
     }
 
     public DexCodeElement taintLookup_NoExtraTaint(DexSingleRegister regTo, DexSingleRegister regObject, TypeClassification type) {
     	DexSingleRegister auxZero = auxReg();
     	return new DexMacro(
     			setEmptyTaint(auxZero),
     			taintLookup(regTo, regObject, auxZero, type));
     }
 
     private boolean isStatic(DexInstruction_Invoke insnInvoke) {
         return insnInvoke.getCallType() == Opcode_Invoke.Static;
     }
 
     private boolean isConstructor(DexInstruction_Invoke insnInvoke) {
         return insnInvoke.getMethodId().getName().equals("<init>");
     }
 
     private boolean isPrimitive(DexRegisterType type) {
         return type instanceof DexPrimitiveType;
     }
 
     private boolean isImmutable(DexRegisterType type) {
         for (Class<?> immutable :  TaintConstants.IMMUTABLES)
             if (immutable.getName().equals(type.getJavaDescriptor()))
                 return true;
         return false;
     }
 
     private boolean isMutable(DexRegisterType type) {
         return !(isPrimitive(type) || isImmutable(type));
     }
     
     public DexCodeElement isSourceTaint(DexSingleRegister regTo, DexSingleRegister regTaint) {
     	return invoke_result_prim(regTo, dexAux.getMethod_TaintConstants_IsSourceTaint(), regTaint);
     }
 
     public DexCodeElement isSinkTaint(DexSingleRegister regTo, DexSingleRegister regTaint) {
     	return invoke_result_prim(regTo, dexAux.getMethod_TaintConstants_IsSinkTaint(), regTaint);
     }
 
     public DexCodeElement getTaint(DexSingleRegister regTo, DexSingleRegister regTaintObject) {
         return invoke_result_prim(regTo, dexAux.getMethod_Taint_Get(), taint(regTaintObject));
     }
 
     public DexCodeElement getTaintExternal(DexSingleRegister regTo, DexSingleRegister regTaintObject) {
         return invoke_result_prim(regTo, dexAux.getMethod_Taint_GetExternal(), taint(regTaintObject));
     }
 
     public DexCodeElement setTaint(DexSingleRegister regFrom, DexSingleRegister regTaint) {
         return invoke(dexAux.getMethod_Taint_Set(), taint(regTaint), regFrom);
     }
 
     public DexCodeElement setTaintExternal(DexSingleRegister regFrom, DexSingleRegister regTaint) {
         return invoke(dexAux.getMethod_Taint_SetExternal(), taint(regTaint), regFrom);
     }
 
     public DexCodeElement propagateTaint(DexSingleRegister regTo, DexSingleRegister regFrom, DexReferenceType typeTo, DexReferenceType typeFrom) {
         DexSingleAuxiliaryRegister regAux = auxReg();
         return new DexMacro(
         		   taintClearVisited(typeFrom),
                    getTaint(regAux, regFrom),
                    taintClearVisited(typeTo),
                    setTaint(regAux, regTo));
     }
     
     public DexCodeElement printStackTrace(DexSingleRegister regException) {
     	return invoke(method_Throwable_printStackTrace, regException);
     }
 
     /*
      * Combines taint of all the given registers. Does not matter if the given registers
      * are taint registers or not, because it automatically converts all of them to taint registers.
      */
     public DexCodeElement combineTaint(DexRegister output, DexRegister ... inputs) {
         DexSingleRegister outputTaint = taint(output);
 
         if (inputs.length == 0)
             return setEmptyTaint(outputTaint);
         else if (inputs.length == 1)
             return new DexInstruction_Move(outputTaint, taint(inputs[0]), false, hierarchy);
         else if (inputs.length == 2)
             return new DexInstruction_BinaryOp(outputTaint, taint(inputs[0]), taint(inputs[1]), Opcode_BinaryOp.OrInt, hierarchy);
         else {
             int count = inputs.length;
             List<DexCodeElement> insns = new ArrayList<DexCodeElement>(count - 1);
 
             // might have output equal to one of the inputs !
             DexSingleRegister aux = auxReg();
 
             insns.add(new DexInstruction_Move(aux, taint(inputs[0]), false, hierarchy));
             for (int i = 1; i < count; i++)
                 insns.add(new DexInstruction_BinaryOp(aux, aux, taint(inputs[i]), Opcode_BinaryOp.OrInt, hierarchy));
             insns.add(new DexInstruction_Move(outputTaint, aux, false, hierarchy));
 
             return new DexMacro(insns);
         }
     }
     
     public DexCodeElement addTaint(DexRegister output, int addedTaint) {
     	return new DexInstruction_BinaryOpLiteral(taint(output), taint(output), addedTaint, Opcode_BinaryOpLiteral.Or, hierarchy);
     }
     
     public DexCodeElement addTaint(DexRegister output, TaintConstants addedTaint) {
     	return addTaint(output, addedTaint.value);
     }
 
     public DexCodeElement getTaint_Array_Length(DexTaintRegister regTo, DexTaintRegister regArrayTaint) {
         return new DexInstruction_InstanceGet(regTo, regArrayTaint, dexAux.getField_TaintArray_TLength(), hierarchy);
     }
 
     public DexCodeElement getTaint_ArrayPrimitive(DexTaintRegister regTo, DexTaintRegister regArrayTaint, DexSingleRegister regIndex) {
     	DexSingleRegister auxTArray = auxReg();    	
         return new DexMacro(
                    new DexInstruction_InstanceGet(auxTArray, regArrayTaint, dexAux.getField_TaintArrayPrimitive_TArray(), hierarchy),
                    new DexInstruction_ArrayGet(regTo, auxTArray, regIndex, Opcode_GetPut.IntFloat, hierarchy));
     }
 
     public DexCodeElement getTaint_ArrayReference(DexTaintRegister regTo, DexTaintRegister regArrayTaint, DexSingleRegister regIndex) {
     	DexSingleRegister auxTArray = auxReg();    	
         return new DexMacro(
                    new DexInstruction_InstanceGet(auxTArray, regArrayTaint, dexAux.getField_TaintArrayReference_TArray(), hierarchy),
                    new DexInstruction_ArrayGet(regTo, auxTArray, regIndex, Opcode_GetPut.Object, hierarchy));
     }
 
     public DexCodeElement setTaint_ArrayPrimitive(DexTaintRegister regFromTaint, DexTaintRegister regArrayTaint, DexSingleRegister regIndex) {
     	DexSingleRegister regAux = auxReg();
     	return new DexMacro(
     			new DexInstruction_InstanceGet(regAux, regArrayTaint, dexAux.getField_TaintArrayPrimitive_TArray(), hierarchy),
     			new DexInstruction_ArrayPut(regFromTaint, regAux, regIndex, Opcode_GetPut.IntFloat, hierarchy));
     }
     
     public DexCodeElement setTaint_ArrayPrimitive(DexSingleRegister regTaint, DexSingleRegister regArray, int indexStart, int count) {
         DexSingleRegister regTArray = auxReg();
         DexSingleRegister regIndex = auxReg();
         
     	List<DexCodeElement> insns = new ArrayList<DexCodeElement>(count * 2);
     	for (int i = 0; i < count; i++) {
     		insns.add(constant(regIndex, i));
     		insns.add(new DexInstruction_ArrayPut(regTaint, regTArray, regIndex, Opcode_GetPut.IntFloat, hierarchy));
     	}
         
         return new DexMacro(
                    new DexInstruction_InstanceGet(regTArray, taint(regArray), dexAux.getField_TaintArrayPrimitive_TArray(), hierarchy),
                    new DexMacro(insns));
     }
 
     public DexCodeElement setTaint_ArrayReference(DexTaintRegister regFromTaint, DexTaintRegister regArrayTaint, DexSingleRegister regIndex) {
         DexSingleRegister regAux = auxReg();
         return new DexMacro(
                    new DexInstruction_InstanceGet(regAux, regArrayTaint, dexAux.getField_TaintArrayReference_TArray(), hierarchy),
                    new DexInstruction_ArrayPut(regFromTaint, regAux, regIndex, Opcode_GetPut.Object, hierarchy));
     }
     
     public DexCodeElement taintCast(DexSingleRegister regObject, DexSingleRegister regObjectTaint,  DexReferenceType objType) {
     	DexLabel lNull = label(), lAfter = label();
     	DexSingleRegister auxTaint = auxReg();
     	DexSingleRegister auxNullObject = auxReg();
     	
     	return new DexMacro(
     			
     		    ifZero(regObject, lNull),
      		   
      		    // Non-NULL objects must have the correct taint assigned already, but maybe not cast
      		    // to the correct type, i.e. TaintInternal being passed as Taint;
      		    // so just do the correct cast
      		   
                 cast(regObjectTaint, (DexReferenceType) taintType(objType)),
                 
                 jump(lAfter),
                 lNull,
                 
                 // NULL objects can be cast from anything to anything. Need to recreate the Taint object
                 
                 getTaint(auxTaint, regObjectTaint), // don't need to clear visited set (with NULL it can't loop)
                 setZero(auxNullObject),
                 taintNull(regObjectTaint, auxNullObject, auxTaint, hierarchy.classifyType(objType)), // method will pick the correct Taint class
                 
                 lAfter);
     }
 
     private static DexSingleRegister taint(DexRegister reg) {
         if (reg instanceof DexTaintRegister)
             return (DexTaintRegister) reg;
         else if (reg instanceof DexSingleAuxiliaryRegister)
             return (DexSingleAuxiliaryRegister) reg;
         else
             return reg.getTaintRegister();
     }
 
     public DexMacro setAllTo(List<? extends DexSingleRegister> regs, long constant) {
         List<DexInstruction> insns = new ArrayList<DexInstruction>(regs.size());
 
         for (DexSingleRegister reg : regs)
             insns.add(new DexInstruction_Const(reg, constant, hierarchy));
 
         return new DexMacro(insns);
     }
 
     public DexMacro setAllTo(List<? extends DexSingleRegister> regs, DexSingleRegister regConstant) {
         List<DexInstruction> insns = new ArrayList<DexInstruction>(regs.size());
 
         for (DexSingleRegister reg : regs)
             insns.add(new DexInstruction_Move(reg, regConstant, false, hierarchy));
 
         return new DexMacro(insns);
     }
     
     public DexCodeElement newObj_SimpleInit(DexSingleRegister regTo, BaseClassDefinition def) {
     	DexTypeCache cache = hierarchy.getTypeCache();
     	DexMethodId paramlessInit = DexMethodId.parseMethodId(
 			"<init>", 
 			DexPrototype.parse(
 				cache.getCachedType_Void(),
 				null,
 				cache),
 			cache);
     	return new DexMacro(
     			newInst(regTo, def),
     			invoke(def.getMethod(paramlessInit), regTo));
     }
     
     public DexCodeElement newInst(DexSingleRegister regTo, BaseClassDefinition def) {
     	return new DexInstruction_NewInstance(regTo, def, hierarchy);
     }
 
     public DexCodeElement jump(DexLabel target) {
         return new DexInstruction_Goto(target, hierarchy);
     }
 
     public DexCodeElement setZero(DexRegister regTo) {
         return new DexInstruction_Const(regTo, 0, hierarchy);
     }
 
     public DexCodeElement setEmptyTaint(DexSingleRegister regTo) {
     	return constant(regTo, TaintConstants.EMPTY);
     }
 
     public DexCodeElement constant(DexSingleRegister reg, int value) {
         return new DexInstruction_Const(reg, value, hierarchy);
     }
 
     public DexCodeElement constant(DexSingleRegister reg, TaintConstants value) {
         return constant(reg, value.value);
     }
 
     public DexCodeElement constant(DexSingleRegister reg, BaseClassDefinition typeDef) {
         return new DexInstruction_ConstClass(reg, typeDef, hierarchy);
     }
 
     public DexCodeElement constant(DexSingleRegister reg, String str) {
         return new DexInstruction_ConstString(reg, str, hierarchy);
     }
 
     public DexCodeElement ifZero(DexSingleRegister reg, DexLabel target) {
         return new DexInstruction_IfTestZero(reg, target, Opcode_IfTestZero.eqz, hierarchy);
     }
 
     public DexCodeElement ifNotZero(DexSingleRegister reg, DexLabel target) {
         return new DexInstruction_IfTestZero(reg, target, Opcode_IfTestZero.nez, hierarchy);
     }
 
     public DexCodeElement move_obj(DexSingleRegister to, DexSingleRegister from) {
         if (to.equals(from))
             return empty();
         return new DexInstruction_Move(to, from, true, hierarchy);
     }
 
     public DexCodeElement move_tobj(DexSingleRegister to, DexSingleRegister from) {
         return move_obj(taint(to), taint(from));
     }
 
     public DexCodeElement move_prim(DexSingleRegister to, DexSingleRegister from) {
         if (to.equals(from))
             return empty();
         return new DexInstruction_Move(to, from, false, hierarchy);
     }
 
     public DexCodeElement move_ex(DexSingleRegister to) {
         return new DexInstruction_MoveException(to, hierarchy);
     }
 
     public DexCodeElement thrw(DexSingleRegister ex) {
         return new DexInstruction_Throw(ex, hierarchy);
     }
 
     public DexCodeElement retrn() {
         return new DexInstruction_ReturnVoid(hierarchy);
     }
 
     public DexCodeElement return_prim(DexSingleRegister regTotalTaint) {
         return new DexInstruction_Return(regTotalTaint, false, hierarchy);
     }
 
     public DexCodeElement iput(DexRegister from, DexSingleRegister obj, InstanceFieldDefinition fieldDef) {
         return new DexInstruction_InstancePut(from, obj, fieldDef, hierarchy);
     }
 
     public DexCodeElement iget(DexRegister to, DexSingleRegister obj, InstanceFieldDefinition fieldDef) {
         return new DexInstruction_InstanceGet(to, obj, fieldDef, hierarchy);
     }
 
     public DexCodeElement sput(DexRegister from, StaticFieldDefinition fieldDef) {
         return new DexInstruction_StaticPut(from, fieldDef, hierarchy);
     }
 
     public DexCodeElement sget(DexRegister to, StaticFieldDefinition fieldDef) {
         return new DexInstruction_StaticGet(to, fieldDef, hierarchy);
     }
 
     public DexCodeElement cast(DexSingleRegister obj, DexReferenceType type) {
         return new DexInstruction_CheckCast(obj, type, hierarchy);
     }
 
     public DexCodeElement invoke_result_prim(DexSingleRegister regTo, MethodDefinition method, DexRegister ... args) {
         return new DexMacro(
                    new DexInstruction_Invoke(method, Arrays.asList(args), hierarchy),
                    regTo == null ? empty() : new DexInstruction_MoveResult(regTo, false, hierarchy));
     }
 
     public DexCodeElement invoke_result_prim(DexSingleRegister regTo, DexMethod method, DexRegister ... args) {
         return invoke_result_prim(regTo, method.getMethodDef(), args);
     }
 
     public DexCodeElement invoke(DexMethod method, DexRegister ... args) {
         return invoke(method.getMethodDef(), args);
     }
 
     public DexCodeElement invoke(MethodDefinition methodDef, DexRegister ... args) {
         return invoke_result_prim(null, methodDef, args);
     }
 
     public DexCodeElement invoke_result_obj(DexSingleRegister regTo, DexMethod method, DexRegister ... args) {
         return invoke_result_obj(regTo, method.getMethodDef(), args);
     }
 
     public DexCodeElement invoke_result_obj(DexSingleRegister regTo, MethodDefinition method, DexRegister ... args) {
         return new DexMacro(
                    new DexInstruction_Invoke(method, Arrays.asList(args), hierarchy),
                    regTo == null ? empty() : new DexInstruction_MoveResult(regTo, true, hierarchy));
     }
 
     public DexCodeElement log(String str) {
         DexSingleRegister regAppName = auxReg();
         DexSingleRegister regString = auxReg();
         return new DexMacro(
                    new DexInstruction_ConstString(regAppName, "DEXTER_DEBUG", hierarchy),
                    new DexInstruction_ConstString(regString, str, hierarchy),
                    new DexInstruction_Invoke(method_Log_d, Arrays.asList(regAppName, regString), hierarchy));
     }
 
     /*
      * Call the implementation of the given method in its superclass.
      */
     public DexCodeElement call_super_int(DexClass clazz, DexMethod method, DexSingleRegister to, List<? extends DexRegister> args) {
         return new DexMacro(
                    new DexInstruction_Invoke(
                        clazz.getClassDef().getSuperclass().getType(),
                        method.getMethodDef().getMethodId(),
                        args,
                        Opcode_Invoke.Super,
                        hierarchy),
                    (to == null ? empty() : new DexInstruction_MoveResult(to, false, hierarchy)));
     }
 
     public DexMacro empty() {
         return DexMacro.empty();
     }
     
     public DexRegisterType taintType(DexRegisterType type) {
         switch(hierarchy.classifyType(type)) {
         case PRIMITIVE:
             return hierarchy.getTypeCache().getCachedType_Integer();
         case REF_EXTERNAL:
             return dexAux.getType_TaintExternal().getClassDef().getType();
         case REF_INTERNAL:
             return dexAux.getType_TaintInternal().getClassDef().getType();
         case ARRAY_PRIMITIVE:
             return dexAux.getType_TaintArrayPrimitive().getClassDef().getType();
         case ARRAY_REFERENCE:
             return dexAux.getType_TaintArrayReference().getClassDef().getType();
         case REF_UNDECIDABLE:
             return dexAux.getType_Taint().getClassDef().getType();
         default:
             throw new UnsupportedOperationException();
         }
     }
 
     public DexRegisterType taintFieldType(DexRegisterType type) {
         switch(hierarchy.classifyType(type)) {
         case PRIMITIVE:
             return hierarchy.getTypeCache().getCachedType_Integer();
         case REF_EXTERNAL:
         case REF_INTERNAL:
         case ARRAY_PRIMITIVE:
         case ARRAY_REFERENCE:
         case REF_UNDECIDABLE:
             return dexAux.getType_Taint().getClassDef().getType();
         default:
             throw new UnsupportedOperationException();
         }
     }
 }

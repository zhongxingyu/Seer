 package com.rx201.dx.translator;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.List;
 
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.CodeItem.TryItem;
 import org.jf.dexlib.DexFileFromMemory;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Util.AccessFlags;
 
 import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithBody;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 
 import com.android.dx.dex.DexOptions;
 import com.android.dx.dex.code.DalvCode;
 import com.android.dx.dex.file.ClassDefItem;
 import com.android.dx.dex.file.DexFile;
 import com.android.dx.dex.file.EncodedMethod;
 import com.android.dx.rop.cst.CstMethodRef;
 import com.android.dx.rop.cst.CstNat;
 import com.android.dx.rop.cst.CstString;
 import com.android.dx.rop.cst.CstType;
 import com.android.dx.rop.type.StdTypeList;
 import com.android.dx.rop.type.Type;
 import com.android.dx.util.ExceptionWithContext;
 
 class DalvCodeBridge {
 
 	private DalvCode dvCode;
 	private CodeItem codeItem;
 
	public DalvCodeBridge(DalvCode dvCode, DexMethodWithBody dxMethod) {
 		this.dvCode = dvCode;
 		process(dxMethod);
 	}
 
 	private CstType toCstType(String descriptor) {
 		return CstType.intern(Type.intern(descriptor));
 	}
 	
 	private CstNat toNat(String methodName, DexPrototype prototype) {
 		return new CstNat(new CstString(methodName),
 						  new CstString(prototype.getDescriptor())
 				);
 	}
 	
	private void process(DexMethodWithBody dxMethod) {
 		MethodDefinition methodDef = dxMethod.getMethodDef();
 		
 		String methodName = methodDef.getMethodId().getName();
 		DexPrototype dexPrototype = methodDef.getMethodId().getPrototype();
 		DexClass dexClass = dxMethod.getParentClass();
 		
 		CstType thisClass = toCstType(dexClass.getClassDef().getType().getDescriptor());
 		CstType superClass = toCstType(dexClass.getClassDef().getSuperclass().getType().getDescriptor());
 		
 		StdTypeList interfaces = StdTypeList.EMPTY;
 		for(DexClassType intf : dexClass.getInterfaceTypes())
 			interfaces.withAddedType(Type.intern(intf.getDescriptor()));
 
 		int classAccessFlags = 0;
 		for (AccessFlags flag : dexClass.getClassDef().getAccessFlags())
 			classAccessFlags |= flag.getValue();
 		
 		DexFile outputDex = new DexFile(new DexOptions());
 		ClassDefItem outClass = new ClassDefItem(thisClass, classAccessFlags,
 				superClass, interfaces, null);
 
 		try {
 			CstMethodRef meth = new CstMethodRef(thisClass, toNat(methodName, dexPrototype));
 			int methodAccessFlags = 0;
 			for(AccessFlags flag : dxMethod.getMethodDef().getAccessFlags()) 
 				methodAccessFlags |= flag.getValue();
 					
 			boolean isStatic = methodDef.isStatic();
 			boolean isPrivate = methodDef.isPrivate();
 			boolean isNative = methodDef.isNative();
 			boolean isConstructor = methodDef.isConstructor();
 
 			assert methodDef.hasBytecode();
 			
 			// Preserve the synchronized flag as its "declared" variant...
 			// It ought to use com.android.dx.rop.code.AccessFlags, not
 			// org.jf.dexlib.Util.AccessFlags, but their value should be the same
 			if (dxMethod.getMethodDef().getAccessFlags().contains(AccessFlags.SYNCHRONIZED) ||
 					dxMethod.getMethodDef().getAccessFlags().contains(AccessFlags.DECLARED_SYNCHRONIZED)) {
 				methodAccessFlags |= AccessFlags.DECLARED_SYNCHRONIZED.getValue(); 
 
 				/*
 				 * ...but only native methods are actually allowed to be
 				 * synchronized.
 				 */
 				if (!isNative) {
 					methodAccessFlags &= ~AccessFlags.SYNCHRONIZED.getValue();
 				}
 			}
 
 			if (isConstructor) {
 				methodAccessFlags |= AccessFlags.CONSTRUCTOR.getValue();
 			}
 
 			EncodedMethod mi = new EncodedMethod(meth, methodAccessFlags, dvCode,
 					StdTypeList.EMPTY);
 
 			if (meth.isInstanceInit() || meth.isClassInit() || isStatic
 					|| isPrivate) {
 				outClass.addDirectMethod(mi);
 			} else {
 				outClass.addVirtualMethod(mi);
 			}
 
 			/*TODO
 			Annotations annotations = AttributeTranslator
 					.getMethodAnnotations(one);
 			if (annotations.size() != 0) {
 				outClass.addMethodAnnotations(meth, annotations);
 			}
 			
 			AnnotationsList list = AttributeTranslator
 					.getParameterAnnotations(one);
 			if (list.size() != 0) {
 				outClass.addParameterAnnotations(meth, list);
 			}
  		    */
 		} catch (RuntimeException ex) {
 			String msg = "...while processing " + methodName + " "
 					+ dexPrototype.getDescriptor();
 			throw ExceptionWithContext.withContext(ex, msg);
 		}
 		
 		outputDex.add(outClass);
 		
 		OutputStreamWriter humanOut = null; //new OutputStreamWriter(System.out);
 		try {
 			byte[] outArray = outputDex.toDex(humanOut, true);
 			DexFileFromMemory tmpDexFile = new DexFileFromMemory(outArray);
 		
 			List<CodeItem> codeItems = tmpDexFile.CodeItemsSection.getItems();
 			assert codeItems.size() == 1;
 			codeItem = codeItems.get(0);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			codeItem = null;
 		}
 	}
 
 	public Instruction[] getInstructions() {
 		return codeItem.getInstructions();
 	}
 
 	public TryItem[] getTries() {
 		return codeItem.getTries();
 	}
 
 	public int getRegisterCount() {
 		return codeItem.getRegisterCount();
 	}
 
 }

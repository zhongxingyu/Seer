 package uk.ac.cam.db538.dexter.transform.taint.sourcesink;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.InterfaceDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.transform.MethodCall;
 import uk.ac.cam.db538.dexter.transform.taint.CodeGenerator;
 
 public abstract class SourceSinkDefinition {
 	
 	protected final MethodCall methodCall;
 	protected final LeakageAlert leakageAlert;
 	protected final RuntimeHierarchy hierarchy;
 	
 	protected SourceSinkDefinition(MethodCall methodCall, LeakageAlert leakageAlert) {
 		this.methodCall = methodCall;
 		this.leakageAlert = leakageAlert;
 		this.hierarchy = methodCall.getInvoke().getHierarchy();
 	}
 	
 	protected SourceSinkDefinition(MethodCall methodCall) {
 		this(methodCall, null);
 	}
 	
 	protected abstract boolean isApplicable();
 	
 	public void doBefore() { }
 	public DexCodeElement insertBefore(CodeGenerator codeGen) { return codeGen.empty(); }
 	public DexCodeElement insertJustBefore(DexSingleRegister regCombinedTaint, CodeGenerator codeGen) { return codeGen.empty(); }
 	public DexCodeElement insertAfter(CodeGenerator codeGen) { return codeGen.empty(); }
 	public DexCodeElement insertJustAfter(DexSingleRegister regCombinedTaint, CodeGenerator codeGen) { return codeGen.empty(); }
 	
 	private static final void addDef(List<SourceSinkDefinition> list, SourceSinkDefinition def) {
 		if (def.isApplicable())
 			list.add(def);
 	}
 	
 	public static final SourceSinkDefinition findApplicableDefinition(MethodCall methodCall, LeakageAlert leakageAlert) {
 		List<SourceSinkDefinition> list = new ArrayList<SourceSinkDefinition>();
 		
 		// Add new source/sink definitions here
		addDef(list, new Error_Proxy(methodCall));
 		addDef(list, new Source_Query(methodCall));
 		addDef(list, new Source_SystemService(methodCall));
 		addDef(list, new Source_Browser(methodCall));
 		addDef(list, new Source_GMaps_MyLocationOverlay(methodCall));
 		addDef(list, new Sink_Log(methodCall, leakageAlert));
 		addDef(list, new Sink_HttpClient(methodCall, leakageAlert));
 		addDef(list, new Sink_Intent(methodCall, leakageAlert));
 		
 		if (list.isEmpty())
 			return null;
 		else if (list.size() == 1)
 			return list.get(0);
 		else
 			throw new AssertionError("Multiple source/sink definitions applicable to a method call");
 	}
 	
 	protected boolean isVirtualCall() {
 		Opcode_Invoke opcode = methodCall.getInvoke().getCallType();
 		return opcode == Opcode_Invoke.Virtual || opcode == Opcode_Invoke.Interface;
 	}
 	
 	protected boolean isStaticCall() {
 		return methodCall.getInvoke().isStaticCall();
 	}
 	
 	protected boolean isDirectCall() {
 		return methodCall.getInvoke().getCallType() == Opcode_Invoke.Direct;
 	}
 
 	protected boolean classIs(String desc) {
 		DexReferenceType calledOn = methodCall.getInvoke().getClassType();
 		return calledOn.getDescriptor().equals(desc);
 	}
 	
 	protected boolean classIsChildOf(String desc) {
 		DexReferenceType calledOn = methodCall.getInvoke().getClassType();
 		
 		BaseClassDefinition calledOnDef = hierarchy.getBaseClassDefinition(calledOn);
 		BaseClassDefinition descDef = hierarchy.getBaseClassDefinition(DexReferenceType.parse(desc, hierarchy.getTypeCache()));
 		
 		return calledOnDef.isChildOf(descDef);
 	}
 	
 	protected boolean classImplements(String desc) {
 		DexReferenceType calledOn = methodCall.getInvoke().getClassType();
 		
 		BaseClassDefinition calledOnDef = hierarchy.getBaseClassDefinition(calledOn);
 		InterfaceDefinition descDef = hierarchy.getInterfaceDefinition(DexReferenceType.parse(desc, hierarchy.getTypeCache()));
 		
 		return calledOnDef.implementsInterface(descDef);
 	}
 
 	protected boolean methodIsCalled(String name) {
 		return methodCall.getInvoke()
 				.getMethodId()
 				.getName()
 				.equals(name);
 	}
 	
 	protected boolean movesResult() {
 		return methodCall.movesResult();
 	}
 	
 	protected boolean returnTypeIs(String desc) {
 		return methodCall.getInvoke()
 				.getMethodId()
 				.getPrototype()
 				.getReturnType()
 				.getDescriptor()
 				.equals(desc);
 	}
 	
 	protected boolean paramIsOfType(int index, String desc) {
 		try {
 			return methodCall.getInvoke()
 				.getMethodId()
 				.getPrototype()
 				.getParameterType(index, isStaticCall(), methodCall.getInvoke().getClassType())
 				.getDescriptor()
 				.equals(desc);
 		} catch (IndexOutOfBoundsException ex) {
 			return false;
 		}
 	}
 	
 	protected DexRegister getParamRegister(int index) {
 		return methodCall.getInvoke().getArgumentRegisters().get(index);
 	}
 	
 	protected DexRegister getResultRegister() {
 		assert movesResult();
 		return methodCall.getResult().getRegTo();
 	}
 }

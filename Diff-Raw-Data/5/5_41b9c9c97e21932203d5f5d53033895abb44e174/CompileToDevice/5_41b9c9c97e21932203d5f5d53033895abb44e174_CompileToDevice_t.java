 /**
  * 
  */
 package hu.e.compiler.tasks;
 
 import hu.e.compiler.IModembedTask;
 import hu.e.compiler.ITaskContext;
 import hu.e.compiler.TaskUtils;
 import hu.modembed.model.behavior.AtomicOperationExecution;
 import hu.modembed.model.behavior.BehaviorCall;
 import hu.modembed.model.behavior.CodeSymbolPlacement;
 import hu.modembed.model.behavior.SequentialAction;
 import hu.modembed.model.behavior.SequentialBehavior;
 import hu.modembed.model.behavior.Symbol;
 import hu.modembed.model.behavior.SymbolValueAssignment;
 import hu.modembed.model.behavior.SymbolValueMap;
 import hu.modembed.model.core.assembler.InstructionParameter;
 import hu.modembed.model.core.assembler.code.AssemblerObject;
 import hu.modembed.model.core.assembler.code.CodeFactory;
 import hu.modembed.model.core.assembler.code.CodePackage;
 import hu.modembed.model.core.assembler.code.InstructionCall;
 import hu.modembed.model.core.assembler.code.InstructionCallParameter;
 import hu.modembed.model.emodel.types.TypeDefinition;
 import hu.modembed.model.platform.InstructionCallOperationStep;
 import hu.modembed.model.platform.InstructionParameterMapping;
 import hu.modembed.model.platform.OperationArgument;
 import hu.modembed.model.platform.OperationDefinition;
 import hu.modembed.model.platform.OperationStep;
 import hu.modembed.model.platform.PlatformDefinition;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * @author balazs.grill
  *
  */
 public class CompileToDevice implements IModembedTask {
 
 	public static final String INPUT = "input";
 	public static final String PLATFORM = "platform";
 	public static final String SYMBOLMAP = "symmap";
 	
 	public static final String OUTPUT = "output";
 	
 	private static class SymbolSignature{
 		
 		public final TypeDefinition type;
 		public final int indirectionLevel;
 		public SymbolSignature(TypeDefinition type, int indirectionLevel) {
 			this.type = type;
 			this.indirectionLevel = indirectionLevel;
 		}
 	}
 	
 	private static class SymbolValue{
 		
 		public final Symbol symbol;
 		public final int bitOffset;
 		public SymbolValue(Symbol symbol, int bitOffset) {
 			this.symbol = symbol;
 			this.bitOffset = bitOffset;
 		}
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see hu.e.compiler.IModembedTask#execute(hu.e.compiler.ITaskContext, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void execute(ITaskContext context, IProgressMonitor monitor) {
 		
 		SequentialBehavior behavior = TaskUtils.getInput(context, INPUT, SequentialBehavior.class);
 		PlatformDefinition platform = TaskUtils.getInput(context, PLATFORM, PlatformDefinition.class);
 		SymbolValueMap symmap = TaskUtils.getInput(context, SYMBOLMAP, SymbolValueMap.class);
 
 		AssemblerObject output = (AssemblerObject) TaskUtils.createOutput(context, OUTPUT, CodePackage.eINSTANCE.getAssemblerObject());
 		
 		Map<Symbol, Long> codeSymbols = new HashMap<Symbol, Long>();
 		Map<InstructionCallParameter, SymbolValue> valueMappings = new LinkedHashMap<InstructionCallParameter, SymbolValue>();
 		
 		long instructionWordCounter = 0;
 		for(SequentialAction sa : behavior.getActions()){
 			if (sa instanceof AtomicOperationExecution){
 				String op = ((AtomicOperationExecution) sa).getOperation();
 				List<Symbol> symbols = ((AtomicOperationExecution) sa).getArguments();
 				SymbolSignature[] signature = new SymbolSignature[symbols.size()];
 				for(int i=0;i<symbols.size();i++){
 					Symbol sy = symbols.get(i);
 					SymbolValueAssignment sva = findAssignment(symmap, sy);
 					if (sva != null){
 						signature[i] = new SymbolSignature(sy.getType(), sva.getIndirectionLevel());
 					}else{
 						signature[i] = new SymbolSignature(sy.getType(), 0);
 					}
 				}
 				
 				OperationDefinition operation = findOperation(platform, op, signature);
 				if (operation == null){
 					context.logStatus(TaskUtils.error("Could not find definition for "+ printOperationSignature(op, signature), sa));
 				}else{
 					for(OperationStep step : operation.getSteps()){
 						if (step instanceof InstructionCallOperationStep){
 							InstructionCallOperationStep icop = (InstructionCallOperationStep)step;
 							InstructionCall ic = CodeFactory.eINSTANCE.createInstructionCall();
 							ic.setInstruction(icop.getInstruction());
 							int i=0;
 							for(InstructionParameter ip : ic.getInstruction().getParameters()){
 								if (i < icop.getArguments().size()){
 									InstructionParameterMapping ipm = icop.getArguments().get(i);
 									InstructionCallParameter icp =  CodeFactory.eINSTANCE.createInstructionCallParameter();
 									icp.setDefinition(ip);
 									ic.getParameters().add(icp);
 									
 									OperationArgument oa = ipm.getValue();
 									int argi = operation.getArguments().indexOf(oa);
									if (argi >= 0){
 										valueMappings.put(icp, new SymbolValue(symbols.get(argi), ipm.getBitOffset()));
 									}
 								}
 								i++;
 							}
 							output.getInstructions().add(ic);
 							instructionWordCounter += ic.getInstruction().getWords().size();
 						}
 					}
 				}
 			}
 			if (sa instanceof CodeSymbolPlacement){
 				codeSymbols.put(((CodeSymbolPlacement) sa).getSymbol(), instructionWordCounter);
 			}
 			if (sa instanceof BehaviorCall){
 				// Not allowed here
 			}
 		}
 		
 		for(InstructionCallParameter icp : valueMappings.keySet()){
 			SymbolValue sv = valueMappings.get(icp);
 			Long value = null;
 			if(codeSymbols.containsKey(sv.symbol)){
 				value = codeSymbols.get(sv.symbol);
 			}else{
 				SymbolValueAssignment sva = findAssignment(symmap, sv.symbol);
 				if (sva != null){
 					value = sva.getValue();
 				}
 			}
 			
 			if (value != null){
 				value = value << sv.bitOffset;
 				icp.setValue(value);
 			}else{
 				TaskUtils.error("Could not resolve symbol: "+sv.symbol.getName());
 			}
 		}
 		
 	}
 
 	private String printOperationSignature(String op, SymbolSignature[] signature){
 		StringBuilder sb = new StringBuilder();
 		sb.append(op);
 		sb.append("<");
 		boolean first = true;
 		for(SymbolSignature sva : signature){
 			if (first) first = false; else sb.append(", ");
 			for(int i=0;i<sva.indirectionLevel;i++) sb.append("&");
 			sb.append(TaskUtils.printType(sva.type));
 		}
 		sb.append(">");
 		return sb.toString();
 	}
 	
 	private SymbolValueAssignment findAssignment(SymbolValueMap map, Symbol symbol){
 		for(SymbolValueAssignment sva : map.getValues()){
 			if (symbol.equals(sva.getSymbol())) return sva; 
 		}
 		return null;
 	}
 	
 	private OperationDefinition findOperation(PlatformDefinition pd, String operation, SymbolSignature[] signature){
 		for(OperationDefinition od : pd.getOperations()) if (operation.equals(od.getOperation())){
 			List<OperationArgument> args = od.getArguments();
 			if (args.size() == signature.length){
 				boolean ok = true;
 				for(int i=0;i<signature.length;i++) if (ok){
 					ok = check(args.get(i), signature[i]);
 				}
 				if (ok) return od;
 			}
 		}
 		return null;
 	}
 	
 	private boolean check(OperationArgument arg, SymbolSignature signature){
 		if (arg.getIndirectionLevel() == signature.indirectionLevel){
			return TaskUtils.canCast(signature.type, arg.getType());
 		}
 		return false;
 	}
 	
 }

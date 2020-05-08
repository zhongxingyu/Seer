 package genlab.gui.graphiti.editors;
 
 import genlab.core.model.meta.ExistingAlgos;
 import genlab.core.model.meta.IAlgo;
 import genlab.core.model.meta.IAlgoContainer;
 import genlab.core.model.meta.IConstantAlgo;
 import genlab.core.model.meta.IFlowType;
 import genlab.core.model.meta.IInputOutput;
 import genlab.core.usermachineinteraction.GLLogger;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * In charge of providing intuitive ways to manipulate 
  * efficiently diagrams. For instance, automatic creation of constants.
  * 
  * TODO display menu if many possibilities ? 
  * TODO propose different steps for links creation, when input and outputs are not compliant.
  *  
  * @author Samuel Thiriot
  *
  */
 public class IntuitiveObjectCreation {
 
 	private IntuitiveObjectCreation() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	/**
 	 * 
 	 * @author Samuel Thiriot
 	 */
 	public static class ProposalObjectCreation {
 		
 		public final IAlgo algoToCreate;
 		public final IInputOutput<?> ioToUse;
 		
 		public ProposalObjectCreation(IAlgo algoToCreate,
 				IInputOutput<?> ioToUse) {
 			super();
 			this.algoToCreate = algoToCreate;
 			this.ioToUse = ioToUse;
 		}
 		
 		
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @author Samuel Thiriot
 	 */
 	public static class ContextObjectCreation {
 	
 		public final IAlgoContainer container;
 		public final IInputOutput<?> inputOrOutputInstance;
 		
 		public ContextObjectCreation(IAlgoContainer container, IInputOutput<?> inputInstance) {
 			super();
 			this.container = container;
 			this.inputOrOutputInstance = inputInstance;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			ContextObjectCreation other = (ContextObjectCreation) obj;
 			if (container == null) {
 				if (other.container != null)
 					return false;
 			} else if (!container.equals(other.container))
 				return false;
 			if (inputOrOutputInstance == null) {
 				if (other.inputOrOutputInstance != null)
 					return false;
 			} else if (!inputOrOutputInstance.equals(other.inputOrOutputInstance))
 				return false;
 			return true;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((container == null) ? 0 : container.hashCode());
 			result = prime * result
 					+ ((inputOrOutputInstance == null) ? 0 : inputOrOutputInstance.hashCode());
 			return result;
 		}
 		
 		@Override
 		public String toString() {
 			StringBuffer sb = new StringBuffer(); 
 			sb.append("container:").append(container.getName());
 			sb.append(", ");
 			sb.append("input:").append(inputOrOutputInstance.getName());
 			return sb.toString();
 		}
 		
 		
 	}
 	
 	
 	/**
 	 * Cache for the searching of an object creation for a given output
 	 */
 	protected static Map<ContextObjectCreation,ProposalObjectCreation> cacheOutput2ProposalAutoInput 
 							= new HashMap<ContextObjectCreation, ProposalObjectCreation>(100);
 	
 	
 	/**
 	 * From a collection of algos, keep only the ones having an output compliant with the search type of interest
 	 * @param algosProposed
 	 * @param searchedType
 	 * @return
 	 */
 	private static Collection<IAlgo> keepAlgosWithOneOutputCompliantWithOutputs(Collection<IAlgo> algosProposed, IFlowType<?> searchedType) {
 		
 		Collection<IAlgo> res = new LinkedList<IAlgo>();
 		
 		for (IAlgo algo: algosProposed) {
 			
 			for (IInputOutput<?> output : algo.getOuputs()) {
 				// if one output is compliant, then add this algo to results
				if (searchedType.compliantWith(output.getType()) && output.getType().compliantWith(searchedType)) {
 					res.add(algo);
 					break;
 				}
 			}
 			
 		}
 		
 		return res;
 		
 	}
 	
 	/**
 	 * From a collection of algos, keep only the ones having an input compliant with the search type of interest
 	 * @param algosProposed
 	 * @param searchedType
 	 * @return
 	 */
 	private static Collection<IAlgo> keepAlgosWithOneOutputCompliantWithInputs(Collection<IAlgo> algosProposed, IFlowType<?> searchedType) {
 		
 		Collection<IAlgo> res = new LinkedList<IAlgo>();
 		
 		for (IAlgo algo: algosProposed) {
 			
 			for (IInputOutput<?> input : algo.getInputs()) {
 				// if one output is compliant, then add this algo to results
				if (searchedType.compliantWith(input.getType()) && input.getType().compliantWith(searchedType)) {
 					res.add(algo);
 					break;
 				}
 			}
 			
 		}
 		
 		return res;
 		
 	}
 	
 	
 	/**
 	 * returns the best algos, that is the ones having the higher priority
 	 * @return
 	 */
 	private static IAlgo selectBestAlgo(Map<IAlgo,Integer> proposals) {
 		
 		// select the best(S) algos
 		Set<IAlgo> betterAlgos = new HashSet<IAlgo>();
 		Integer higherPrority = -100;
 		for (Map.Entry<IAlgo,Integer> algo2priority : proposals.entrySet()) {
 			
 			final IAlgo algo = algo2priority.getKey();
 			final Integer priority = algo2priority.getValue();
 			
 			if (priority == higherPrority) {
 				betterAlgos.add(algo);
 			} else if (priority > higherPrority) {
 				betterAlgos.clear();
 				betterAlgos.add(algo);
 				higherPrority = priority;
 			}
 		}
 		
 		// and select only one
 		IAlgo bestAlgo = null;
 		if (betterAlgos.size() > 1) {
 			GLLogger.warnTech("found several ("+betterAlgos.size()+") best solutions for intuitive creation, so the selection will be random :"+betterAlgos, IntuitiveObjectCreation.class);
 			return  betterAlgos.iterator().next();
 		} else if (betterAlgos.size() == 1) {
 			GLLogger.traceTech("found a solution for intuitive creation:"+bestAlgo, IntuitiveObjectCreation.class);
 			return betterAlgos.iterator().next();
 		} else {
 			return null;
 		}
 		 
 	}
 
 	
 	/**
 	 * 
 	 * Search an intuitive creation for this context. searched both in the container and among constants.
 	 * Principles: we identify the algos which could be created in an automatic way: container proposals
 	 * (for instance create container-specific elements), constants (with a priority between 0 and 99,
 	 * the higher the stronger) and any other algorithm: those having 0 inputs have priority -1, those
 	 * with two inputs priority -2, etc.
 	 * @param context
 	 * @return
 	 */
 	protected static ProposalObjectCreation searchForAnAutoOutputForContext(ContextObjectCreation context) {
 		
 		// TODO adapt for output
 		GLLogger.debugTech("searching an intuitive output for context: "+context, IntuitiveObjectCreation.class);
 		
 		Map<IAlgo,Integer> proposals = new HashMap<IAlgo, Integer>(100);
 				
 		IFlowType<?> searchedType = context.inputOrOutputInstance.getType();
 
 		// load all the algorithms which have only an output, not an input
 		for (IAlgo algo: keepAlgosWithOneOutputCompliantWithOutputs(
 								ExistingAlgos.getExistingAlgos().getAlgos(),
 								searchedType)
 								) {
 			// add the proposal with its default priority
 			proposals.put(algo, algo.getPriorityForIntuitiveCreation());
 		}
 		
 		// let the container propose intuitive inputs
 		// in some way, it is a "context"
 		// ... but only keep the proposals compliant with our type !
 		{
 			Map<IAlgo,Integer> proposalsFromContainer = context.container.recommandAlgosContained();
 			for (IAlgo algoCompliant: keepAlgosWithOneOutputCompliantWithOutputs(
 									proposalsFromContainer.keySet(),
 									searchedType)
 									) {
 				// add the proposed algo from the container with the priority defined by the container
 				proposals.put(algoCompliant, proposalsFromContainer.get(algoCompliant));
 			}
 		}
 
 		// and select only one
 		IAlgo bestAlgo = selectBestAlgo(proposals);
 		
 		// now find the corresponding output of interest
 		IInputOutput<?> io = null;
 		for (IInputOutput<?> o : bestAlgo.getOuputs()) {
 			if (context.inputOrOutputInstance.getType().compliantWith(o.getType())) {
 				io = o;
 				break;
 			}
 		}
 		if (io == null) {
 			GLLogger.warnTech("the algo found ("+bestAlgo+") provides no compliant output; cancelling the intuitive proposal", IntuitiveObjectCreation.class);
 			return null;
 		}
 		return new ProposalObjectCreation(bestAlgo, io);
 		
 	}
 	
 	protected static ProposalObjectCreation searchForAnAutoInputForContext(ContextObjectCreation context) {
 		
 		// TODO adapt for output
 		GLLogger.debugTech("searching an intuitive input for context: "+context, IntuitiveObjectCreation.class);
 		
 		Map<IAlgo,Integer> proposals = new HashMap<IAlgo, Integer>(100);
 				
 		IFlowType<?> searchedType = context.inputOrOutputInstance.getType();
 
 		// load all the algorithms which have only an input, not an output
 		for (IAlgo algo: keepAlgosWithOneOutputCompliantWithInputs(
 								ExistingAlgos.getExistingAlgos().getAlgos(),
 								searchedType)
 								) {
 			// add the proposal with its default priority
 			proposals.put(algo, algo.getPriorityForIntuitiveCreation());
 		}
 		
 		// let the container propose intuitive inputs
 		// in some way, it is a "context"
 		// ... but only keep the proposals compliant with our type !
 		{
 			Map<IAlgo,Integer> proposalsFromContainer = context.container.recommandAlgosContained();
 			for (IAlgo algoCompliant: keepAlgosWithOneOutputCompliantWithInputs(
 									proposalsFromContainer.keySet(),
 									searchedType)
 									) {
 				// add the proposed algo from the container with the priority defined by the container
 				proposals.put(algoCompliant, proposalsFromContainer.get(algoCompliant));
 			}
 		}
 		
 		// and select only one
 		IAlgo bestAlgo = selectBestAlgo(proposals);
 
 		IInputOutput<?> io = null;
 		for (IInputOutput<?> o : bestAlgo.getInputs()) {
 			if (o.getType().compliantWith(context.inputOrOutputInstance.getType())) {
 				io = o;
 				break;
 			}
 		}
 		if (io == null) {
 			GLLogger.warnTech("the algo found ("+bestAlgo+") provides no compliant output; cancelling the intuitive proposal", IntuitiveObjectCreation.class);
 			return null;
 		}
 		return new ProposalObjectCreation(bestAlgo, io);
 		
 	}
 	
 	/**
 	 * Provides a constant that provides an output of this type.
 	 * @param output
 	 * @return
 	 */
 	public static ProposalObjectCreation getAutoInputForOutput(IAlgoContainer algoContainer, IInputOutput<?> output) {
 	
 		ContextObjectCreation context = new ContextObjectCreation(algoContainer, output);
 		
 		ProposalObjectCreation res = cacheOutput2ProposalAutoInput.get(context);
 		
 		if (!cacheOutput2ProposalAutoInput.containsKey(context)) {
 		
 			// not in cache, search it.
 			res = searchForAnAutoInputForContext(context);
 			
 			// store in cache
 			cacheOutput2ProposalAutoInput.put(context, res);
 			
 		}
 		
 		return res;
 	}
 	
 
 	/**
 	 * Provides a constant that provides an output of this type.
 	 * @param output
 	 * @return
 	 */
 	public static ProposalObjectCreation getAutoOutputForInput(IAlgoContainer algoContainer, IInputOutput<?> input) {
 	
 		ContextObjectCreation context = new ContextObjectCreation(algoContainer, input);
 		
 		ProposalObjectCreation res = cacheOutput2ProposalAutoInput.get(context);
 		
 		if (!cacheOutput2ProposalAutoInput.containsKey(context)) {
 		
 			// not in cache, search it.
 			res = searchForAnAutoOutputForContext(context);
 			
 			// store in cache
 			cacheOutput2ProposalAutoInput.put(context, res);
 			
 		}
 		
 		return res;
 	}
 	
 }

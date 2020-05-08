 package ReactorEE.pcomponents;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 import java.util.Map.Entry;
 
 /**
  * ConnectorPipe class only really extends PlantComponent for consistency
  * in the model.
  * 
  * It is effectively completely invisible to the user and is used to connect
  * up multiple paths between components in the system.
  * 
  * @author Lamprey
  *
  */
 public class ConnectorPipe extends PlantComponent {
 	
 	private static final long serialVersionUID = 4562191438219268556L;
 	
 	/* Since these objects will effectively be invisible to the user,
 	 * they should never fail!
 	 */
 	private final static int FAILURE_RATE = 0;
 	private final static int REPAIR_TIME = 0;
 	private final static int MAX_FAILURE_RATE = 0;
 	
 	private List<PlantComponent> inputs;
 	private Map<PlantComponent, Boolean> outputs; // Boolean is true if that path is blocked.
 	
 	public ConnectorPipe() {
 		super(FAILURE_RATE, REPAIR_TIME, MAX_FAILURE_RATE);
 		this.inputs = new ArrayList<PlantComponent>();
 		this.outputs = new HashMap<PlantComponent, Boolean>();
 	}
 	
 	/**
 	 * Adds an input to this ConnectorPipe. Use this method 
 	 * instead of setInput().
 	 * 
 	 * @param input Component to connect as an input to this
 	 * 			    ConnectorPipe.
 	 */
 	public void addInput(PlantComponent input) {
 		if (this.getInput() != null) {
 			this.setInput(input);
 		} else {
 			this.inputs.add(input);
 		}
 	}
 	
 	/**
 	 * Adds an output to this ConnectorPipe. Use this method
 	 * instead of setOutput().
 	 * 
 	 * @param output Component to connect to the output of this
 	 * 				 ConnectorPipe.
 	 */
 	public void addOutput(PlantComponent output) {
 		this.outputs.put(output, false);
 	}
 	
 	/**
 	 *  Use this method instead of getInput().
 	 *
 	 * @return an ArrayList of all PlantComponents connected as inputs 
 	 * 		   to this ConnectorPipe.
 	 */
 	public ArrayList<PlantComponent> getInputs() {
 		ArrayList<PlantComponent> inputs = new ArrayList<PlantComponent>(this.inputs);
 		inputs.add(this.getInput());
 		return inputs;
 	}
 
 	/**
 	 * Use this method instead of getOutput().
 	 * 
 	 * @return an ArrayList of all PlantComponents connected to the output 
 	 * 		   of this ConnectorPipe.
 	 */
 	public ArrayList<PlantComponent> getOutputs() {
 		return new ArrayList<PlantComponent>(this.outputs.keySet());
 	}
 	
 	/**
 	 * 
 	 * @return the outputs map of component & blocked boolean.
 	 */
 	public Map<PlantComponent, Boolean> getOutputsMap() {
 		return this.outputs;
 	}
 	
 	
 	/**
 	 * 
 	 * @param blockedComponent 
 	 */
 	public void setComponentBlocked(PlantComponent blockedComponent) {
 		if (this.outputs.containsKey(blockedComponent)) {
 			this.outputs.put(blockedComponent, true);
 		} else {
 			throw new IllegalArgumentException("Attempt to block an output with a reference to a "
 											  +"component that is not an output to this connector "
 											  +"pipe.");
 		}
 	}
 	
 	/**
 	 * 
 	 * @return the number of non-blocked outputs.
 	 */
 	public int numOutputs() {
 		int totalOutputs = 0;
 		for (PlantComponent pc : this.outputs.keySet()) {
 			if (!this.outputs.get(pc)) totalOutputs++; 
 		}
 		return totalOutputs;
 	}
 	
 	/**
 	 * Reset's all the outputs to not-blocked.
 	 */
 	public void resetState() {
 		for (Entry<PlantComponent, Boolean> entry : outputs.entrySet()) {
 			entry.setValue(false);
 		}
 	}
 	
 	/**
 	 * Does nothing (nothing to update).
 	 */
 	@Override
 	public void updateState() {
 		// Do nothing.
 	}
 
 	/**
 	 * Never breaks - returns false always.
 	 */
 	@Override
 	public boolean checkFailure() {
 		return false; //Never breaks.
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ConnectorPipe other = (ConnectorPipe) obj;
 		if (inputs == null) {
 			if (other.inputs != null)
 				return false;
		} else if (inputs.hashCode() != other.inputs.hashCode())
 			return false;
 		if (outputs == null) {
 			if (other.outputs != null)
 				return false;
		} else if (outputs.hashCode() != other.outputs.hashCode())
 			return false;
 		return true;
 	}
 
 }

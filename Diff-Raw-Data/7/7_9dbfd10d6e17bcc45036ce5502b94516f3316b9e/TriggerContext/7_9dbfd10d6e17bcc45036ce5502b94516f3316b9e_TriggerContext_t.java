 package gt.general.trigger;
 
 import gt.general.trigger.persistance.YamlSerializable;
 import gt.general.trigger.response.Response;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 public class TriggerContext {
 
 	private final Set<Trigger> triggers;
 	private final Set<Response> responses;
 	
 	private InputFunction inputFunction;
 
 	private String label;
 	
 	private final Set<Trigger> activeTriggers;
 	
 	/**
 	 * Generates a new TriggerContext
 	 * 
 	 */
 	public TriggerContext() {
 		triggers = new HashSet<Trigger>();
 		responses = new HashSet<Response>();
 		
 		activeTriggers = new HashSet<Trigger>();
 		
 		setLabel("context_" + hashCode());
 		
 		inputFunction = InputFunction.OR;
 	}
 
 	/**
 	 * @return the inputFunction
 	 */
 	public InputFunction getInputFunction() {
 		return inputFunction;
 	}
 
 	/**
 	 * @param inputFunction the inputFunction to set
 	 */
 	public void setInputFunction(final InputFunction inputFunction) {
 		this.inputFunction = inputFunction;
 	}
 	
 	/**
 	 * toggle (AND / OR) input function
 	 */
 	public void toggleInputFunction() {
 		if(inputFunction == InputFunction.OR) {
 			inputFunction = InputFunction.AND;
 		} else {
 			inputFunction = InputFunction.OR;
 		}
 	}
 	
 	public void updateTriggerState(final Trigger trigger, final boolean state) {		
 		boolean oldState = evalInputFuntion();
 		
 		if(state) {
 			activeTriggers.add(trigger);
 		} else {
 			activeTriggers.remove(trigger);
 		}
 		boolean newState = evalInputFuntion();
 		
 		if(oldState != newState) {
 			for(Response response : responses) {
 				response.triggered(newState);
 			}
 		}
 		
 		
 	}
 	
 	private boolean evalInputFuntion() {
 		switch (inputFunction) {
 		case OR:
 			return activeTriggers.size() != 0;
 		case AND:
 			return activeTriggers.size() == triggers.size();
 		}
 		return false;
 	}
 	
 	/**
 	 * @return true if the TriggerContext has a trigger and a response
 	 */
 	public boolean isComplete() {
 		return !triggers.isEmpty() && !responses.isEmpty();
 	}
 
 	public Collection<Trigger> getTriggers() {
 		return triggers;
 	}
 
 	public Collection<Response> getResponses() {
 		return responses;
 	}
 	
 	public void removeSerializable(YamlSerializable serializable) {
 		triggers.remove(serializable);
 		responses.remove(serializable);
 		activeTriggers.remove(serializable);
		serializable.dispose();
 	}
 	
 	public String getLabel() {
 		return label;
 	}
 	
 	public void setLabel(final String label) {
 		this.label = label;
 	}
 	
 	public void addTrigger(final Trigger trigger) {
 		triggers.add(trigger);
 		trigger.setContext(this);
 	}
 	
 	public void addResponse(final Response response) {
 		responses.add(response);
 	}
 	
 }

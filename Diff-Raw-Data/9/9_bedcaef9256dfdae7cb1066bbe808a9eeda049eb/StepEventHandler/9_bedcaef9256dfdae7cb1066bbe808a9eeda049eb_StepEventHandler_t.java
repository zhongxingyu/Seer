 package events;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import sourceparser.SourceParser;
 
 import log.Logger;
 
 import com.sun.jdi.AbsentInformationException;
 import com.sun.jdi.Field;
 import com.sun.jdi.IncompatibleThreadStateException;
 import com.sun.jdi.LocalVariable;
 import com.sun.jdi.Location;
 import com.sun.jdi.ObjectReference;
 import com.sun.jdi.ReferenceType;
 import com.sun.jdi.StackFrame;
 import com.sun.jdi.ThreadReference;
 import com.sun.jdi.Value;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.event.Event;
 import com.sun.jdi.event.StepEvent;
 import com.sun.jdi.request.EventRequestManager;
 import com.sun.jdi.request.StepRequest;
 
 public class StepEventHandler implements IEventHandler {
 
 	private SourceParser sourceParser;
 	private Logger logger;
 	private VirtualMachine vm;
 	
 	private Threads threads;
 	
 	public StepEventHandler(SourceParser sourceParser, Logger logger, VirtualMachine vm) {
 
 		this.sourceParser = sourceParser;
 		this.logger = logger;
 		this.vm = vm;
 		
 		this.threads = new Threads();
 		
 		requestEvents();
 	}
 	
 	private void requestEvents() {
 		EventRequestManager erm = vm.eventRequestManager();
 		
 		for (ThreadReference ref : vm.allThreads()) {
 			StepRequest request = erm.createStepRequest(ref, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
 			/* TODO: Change this to match what our command line params are */
 			request.addClassFilter("client.*");
 			request.enable();
 		}
 	}
 	
 	@Override
 	public int handle(Event event) {
 		StepEvent stepEvent = (StepEvent)event;
 		
 		Location location = stepEvent.location();
 		
 		String sourcePath;
 		int lineNumber;
 		
 		try {
 			sourcePath = location.sourcePath();
 			lineNumber = location.lineNumber();
 		} catch (AbsentInformationException e) {
 			// No location information available for this step
 			
 			return 0;
 		}
 			
 		for (ThreadReference thread : vm.allThreads()) {					
 			String threadName = thread.name();
 			
 			int stackDepth;
 			
 			try {
 				stackDepth = thread.frameCount();
 			} catch (IncompatibleThreadStateException e) {
 				// No stack variables available for this thread
 				
 				continue;
 			}
 			
 			if (threads.containsKey(threadName) == false) {
 				Steps steps = new Steps();
 				
 				threads.put(threadName, steps);
 			}
 			
 			Steps steps = threads.get(threadName);
 			
 			if (steps.size() < stackDepth) {
 				// Stack has grown so make a new Step
 				
 				steps.push(new Step());
 			} else if (steps.size() > stackDepth) {
 				// Stack has shrunk so pop a Step
 				
 				steps.pop();
 			}
 			
 			if (steps.size() > 0) {				
 				Step lastStep = steps.pop();
 				Set<String> knownVariables = lastStep.getKnownVariables();
 				
 				Map<String, String> lastVariables = lastStep.getLastVariables();
 				
 				// Check used variables from the last step to check for changes
 				if (lastVariables != null) {
 					for (String lastVariable : lastVariables.keySet()) {
 						String lastValue = lastVariables.get(lastVariable);						
 						String currentValue = getValue(thread, location, lastVariable);
 						
 						if (lastValue.equals(currentValue) == false) {
 							logger.logVarChanged(lastVariable, currentValue);
 						}
 					}
 				}
 				
 				// Log step event
 				logger.logLines(sourcePath, lineNumber);
 				
 				// Log used variables and record their values
 				List<String> variables = sourceParser.getVariables(sourcePath, lineNumber);
 				Map<String, String> currentValues = new HashMap<String, String>();
 				
 				if (variables != null) {
 					for (String variable : variables) {
 						if (knownVariables.add(variable)) {
 							
 							String type = "UNKNOWN";
 							LocalVariable lv = null;
 							try {
 								lv = thread.frame(0).visibleVariableByName(variable);
 								if (lv != null) {
 									type = lv.typeName();
 								}
 							} catch (AbsentInformationException e) {
 								// TODO Auto-generated catch block
								//e.printStackTrace();
 							} catch (IncompatibleThreadStateException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 
 							logger.logVarCreated(variable, type );
 						}
 						
 						String value = getValue(thread, location, variable);
 						
 						currentValues.put(variable, value);
 						
 						logger.logVarUsed(variable);
 					}
 				}
 				
 				// Push the new step
 				Step newStep = new Step(knownVariables, lineNumber, currentValues);
 				steps.push(newStep);
 			}
 		}
 		
 		return 0;
 	}
 	
 	private String getValue(ThreadReference thread, Location location, String variableName) {		
 		StackFrame stackFrame;
 		try {
 			stackFrame = thread.frame(0);
 		} catch (IncompatibleThreadStateException e1) {
 			// TODO Auto-generated catch block			
 
 			return "null";
 		}
 		
 		LocalVariable variable;
 		try {
 			variable = stackFrame.visibleVariableByName(variableName);
 		} catch (AbsentInformationException e) {
 			// TODO Auto-generated catch block
 			
 			return "null";
 		}
 		
 		if (variable != null) {
 			// The variable was on the stack
 			
 			return stackFrame.getValue(variable).toString();
 		} else {
 			// The variable was not on the stack
 
 			Value value = null;
 											
 			ObjectReference objectReference = stackFrame.thisObject();
 			if (objectReference != null) {
 				ReferenceType referenceType = objectReference.referenceType();
 				Field field = referenceType.fieldByName(variableName);
 				value = objectReference.getValue(field);
 			}
 
 			// handle static fields
 			if (value == null){
 				ReferenceType referenceType = location.declaringType();
 				Field field = referenceType.fieldByName(variableName);
 				if (field != null) {
 					value = referenceType.getValue(field);
 				}
 			}
 
 			if (value != null) {
 				return value.toString();
 			} else {
 				return "null"; // TODO: This returns null for static fields outside the current class as well...
 			}
 		}
 	}
 	
 	private class Threads extends HashMap<String, Steps> {
 
 		private static final long serialVersionUID = -58718752877349620L;
 	}
 	
 	private class Steps extends Stack<Step> {
 
 		private static final long serialVersionUID = -7640570711643014510L;
 	}
 	
 	private class Step {
 
 		private Set<String> knownVariables;
 		
 		private int lastLine;
 		private Map<String, String> lastVariables; // TODO: Clean this up so that it just needs to be lastVariable
 		
 		public Step() {
 			knownVariables = new HashSet<String>();
 			
 			lastLine = -1;
 			lastVariables = null;
 		}
 		
 		public Step(Set<String> knownVariables, int lastLine, Map<String, String> lastVariables) {
 			this.knownVariables = knownVariables;
 			
 			this.lastLine = lastLine;
 			this.lastVariables = lastVariables;
 		}
 		
 		public Set<String> getKnownVariables() {
 			return knownVariables;
 		}
 		
 		public int getLastLine() {
 			return lastLine;
 		}
 		
 		public Map<String, String> getLastVariables() {
 			return lastVariables;
 		}
 	}
 }

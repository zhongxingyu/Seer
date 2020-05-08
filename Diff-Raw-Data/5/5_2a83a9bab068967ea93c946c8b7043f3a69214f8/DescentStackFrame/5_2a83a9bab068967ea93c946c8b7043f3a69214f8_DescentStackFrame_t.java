 package descent.internal.launching.model;
 
 import java.io.IOException;
 
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IRegisterGroup;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.debug.core.model.IVariable;
 
 import descent.launching.model.ICli;
 
 
 public class DescentStackFrame extends DescentDebugElement implements IStackFrame {
 
 	private final IThread fThread;
 	private final ICli fCli;
 	private String fName;
 	private int fNumber;
 	private String fSourceName;
 	private int fLineNumber;
 	
 	private IVariable[] fVariables;
 	
 	public DescentStackFrame(IDebugTarget target, ICli cli, IThread thread, String name, int number, String sourceName, int lineNumber) {
 		super(target);
 		this.fCli = cli;
 		this.fThread = thread;
 		this.fName = name;
 		this.fNumber = number;
 		this.fSourceName = sourceName;
 		this.fLineNumber = lineNumber;
 	}
 	
 	public ICli getCli() {
 		return fCli;
 	}
 	
 	public int getNumber() {
 		return fNumber;
 	}
 	
 	public int getCharEnd() throws DebugException {
		return -1;
 	}
 
 	public int getCharStart() throws DebugException {
		return -1;
 	}
 
 	public int getLineNumber() throws DebugException {
 		return fLineNumber;
 	}
 	
 	public String getSourceName() {
 		return fSourceName;
 	}
 
 	public String getName() throws DebugException {
 		StringBuilder sb = new StringBuilder();
 		sb.append(fName);
 		if (fLineNumber != -1) {
 			sb.append(": line ");
 			sb.append(fLineNumber);
 		}
 		return sb.toString();
 	}
 
 	public IRegisterGroup[] getRegisterGroups() throws DebugException {
 		return ((DescentThread) getThread()).getRegisterGroups();
 	}
 
 	public IThread getThread() {
 		return fThread;
 	}
 
 	public IVariable[] getVariables() throws DebugException {
 		try {
 			IVariable[] newVariables = fCli.getVariables(fNumber);
 			fVariables = mergeVariables(fVariables, newVariables);
 			return fVariables;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return new IVariable[0];
 		}
 	}
 
 	private IVariable[] mergeVariables(IVariable[] variables, IVariable[] newVariables) throws DebugException {
 		if (variables == null) {
 			return newVariables;
 		}
 		
 		for(int i = 0; i < variables.length && i < newVariables.length; i++) {
 			DescentVariable oldVar = (DescentVariable) variables[i];
 			DescentVariable newVar = (DescentVariable) newVariables[i];
 			if (oldVar.getName().equals(newVar.getName())) {
 				if (oldVar.getValue() != null && newVar.getValue() != null && 
 						oldVar.getValue().getValueString() != null && newVar.getValue().getValueString() != null) {
 				
 					if (!oldVar.getValue().getValueString().equals(newVar.getValue().getValueString())) {
 						newVar.setValue(oldVar.getValue());
 						newVar.setHasValueChanged(true);
 					}
 					if (oldVar.getValue().hasVariables() &&  oldVar.getValue().hasVariables() == newVar.getValue().hasVariables()) {
 						mergeVariables(oldVar.getValue().getVariables(), newVar.getValue().getVariables());
 					}
 				}
 			}
 		}
 		return newVariables;
 	}
 
 	public boolean hasRegisterGroups() throws DebugException {
 		return true;
 	}
 
 	public boolean hasVariables() throws DebugException {
 		return true;
 	}
 
 	public boolean canStepInto() {
 		return getThread().canStepInto();
 	}
 
 	public boolean canStepOver() {
 		return getThread().canStepOver();
 	}
 
 	public boolean canStepReturn() {
 		return getThread().canStepReturn();
 	}
 
 	public boolean isStepping() {
 		return getThread().isStepping();
 	}
 
 	public void stepInto() throws DebugException {
 		getThread().stepInto();
 	}
 
 	public void stepOver() throws DebugException {
 		getThread().stepOver();
 	}
 
 	public void stepReturn() throws DebugException {
 		getThread().stepReturn();
 	}
 
 	public boolean canResume() {
 		return getThread().canResume();
 	}
 
 	public boolean canSuspend() {
 		return getThread().canSuspend();
 	}
 
 	public boolean isSuspended() {
 		return getThread().isSuspended();
 	}
 
 	public void resume() throws DebugException {
 		getThread().resume();
 	}
 
 	public void suspend() throws DebugException {
 		getThread().suspend();
 	}
 
 	public boolean canTerminate() {
 		return getThread().canTerminate();
 	}
 
 	public boolean isTerminated() {
 		return getThread().isTerminated();
 	}
 
 	public void terminate() throws DebugException {
 		getThread().terminate();
 	}
 	
 	public boolean isInSameFunction(DescentStackFrame other) {
 		return fName != null && other.fName != null && fName.equals(other.fName) && 
 			fSourceName != null && other.fSourceName != null && fSourceName.equals(other.fSourceName) &&
 			fNumber == other.fNumber;
 	}
 	
 	public void merge(DescentStackFrame other) {
 		this.fLineNumber = other.fLineNumber;
 		this.fName = other.fName;
 		this.fNumber = other.fNumber;
 		this.fSourceName = other.fSourceName;
 	}
 	
 	@Override
 	public String toString() {
 		try {
 			return getName();
 		} catch (DebugException e) {
 			return super.toString();
 		}
 	}
 
 	
 
 }

 package org.eclipse.dltk.internal.debug.core.model;
 
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.dltk.dbgp.IDbgpStatus;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.internal.debug.core.model.operations.DbgpDebugger;
 import org.eclipse.dltk.internal.debug.core.model.operations.IDbgpDebuggerFeedback;
 
 public class ScriptThreadStateManager implements IDbgpDebuggerFeedback {
 	public static interface IStateChangeHandler {
 		void handleSuspend(int detail);
 
 		void handleResume(int detail);
 
 		void handleTermination(DbgpException e);
 	}
 
 	private IStateChangeHandler handler;
 
 	private final DbgpDebugger engine;
 
 	// Abilities of debugging engine
 	private boolean canSuspend;
 
 	// Number of suspends
 	private int suspendCount;
 
 	// States
 	private boolean stepping;
 
 	private boolean suspended;
 
 	private boolean terminated;
 
 	protected void handleStatus(DbgpException exception, IDbgpStatus status,
 			int suspendDetail) {
 		if (exception != null) {
 			setTerminated(exception);
 		}

 		if (status.isBreak()) {
 			setSuspended(true, suspendDetail);
 		} else if (status.isStopping()) {
 			// Temporary solution!
 			try {
 				terminate();
 			} catch (DebugException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else if (status.isStopped()) {
 			setTerminated(null);
 		}
 	}
 
 	// State management
 	protected void setSuspended(boolean value, int detail) {
 		suspended = value;
 		if (value) {
 			++suspendCount;
 		}
 
 		if (value) {
 			handler.handleSuspend(detail);
 		} else {
 			handler.handleResume(detail);
 		}
 	}
 
 	private void setTerminated(DbgpException e) {
 		terminated = true;
 		handler.handleTermination(e);
 	}
 
 	private boolean canStep() {
 		return !terminated && suspended;
 	}
 
 	private void beginStep(int detail) {
 		stepping = true;
 		setSuspended(false, detail);
 	}
 
 	private void endStep(DbgpException execption, IDbgpStatus status) {
 		stepping = false;
 		handleStatus(execption, status, DebugEvent.STEP_END);
 	}
 
 	public ScriptThreadStateManager(ScriptThread thread) {
 		this.handler = thread;
 		this.engine = new DbgpDebugger(thread, this);
 
 		canSuspend = true; // engine.isSupportsAsync();
 		this.suspendCount = 0;
 
 		this.suspended = true;
 		this.terminated = false;
 		this.stepping = this.suspended;
 	}
 
 	public DbgpDebugger getEngine() {
 		return engine;
 	}
 
 	// Stepping
 	public boolean isStepping() {
 		return !terminated && stepping;
 	}
 
 	// StepInto
 	public boolean canStepInto() {
 		return canStep();
 	}
 
 	public void endStepInto(DbgpException e, IDbgpStatus status) {
 		endStep(e, status);
 	}
 
 	public void stepInto() throws DebugException {
 		beginStep(DebugEvent.STEP_INTO);
 		engine.stepInto();
 	}
 
 	// StepOver
 	public boolean canStepOver() {
 		return canStep();
 	}
 
 	public void endStepOver(DbgpException e, IDbgpStatus status) {
 		endStep(e, status);
 	}
 
 	public void stepOver() throws DebugException {
 		beginStep(DebugEvent.STEP_OVER);
 		engine.stepOver();
 	}
 
 	// StepReturn
 	public boolean canStepReturn() {
 		return canStep();
 	}
 
 	public void endStepReturn(DbgpException e, IDbgpStatus status) {
 		endStep(e, status);
 	}
 
 	public void stepReturn() throws DebugException {
 		beginStep(DebugEvent.STEP_RETURN);
 		engine.stepReturn();
 	}
 
 	// Suspend
 	public boolean isSuspended() {
 		return suspended;
 	}
 
 	public boolean canSuspend() {
 		return canSuspend && !terminated && !suspended;
 	}
 
 	public void endSuspend(DbgpException e, IDbgpStatus status) {
 		handleStatus(e, status, DebugEvent.CLIENT_REQUEST);
 	}
 
 	public void suspend() throws DebugException {
 		setSuspended(true, DebugEvent.CLIENT_REQUEST);
 		engine.suspend();
 	}
 
 	public int getSuspendCount() {
 		return suspendCount;
 	}
 
 	// Resume
 	public boolean canResume() {
 		return !terminated && suspended;
 	}
 
 	public void endResume(DbgpException e, IDbgpStatus status) {
 		handleStatus(e, status, DebugEvent.BREAKPOINT);
 	}
 
 	public void resume() throws DebugException {
 		setSuspended(false, DebugEvent.CLIENT_REQUEST);
 
 		engine.resume();
 	}
 
 	// Terminate
 	public boolean isTerminated() {
 		return terminated;
 	}
 
 	public boolean canTerminate() {
 		return !terminated;
 	}
 
 	public void endTerminate(DbgpException e, IDbgpStatus status) {
 		handleStatus(e, status, DebugEvent.CLIENT_REQUEST);
 	}
 
 	public void terminate() throws DebugException {
 		engine.terminate();
 	}
 }

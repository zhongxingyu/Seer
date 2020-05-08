 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.internal.core.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.dbgp.IDbgpNotification;
 import org.eclipse.dltk.dbgp.IDbgpNotificationListener;
 import org.eclipse.dltk.dbgp.IDbgpSession;
 import org.eclipse.dltk.dbgp.IDbgpStatus;
 import org.eclipse.dltk.dbgp.commands.IDbgpCoreCommands;
 import org.eclipse.dltk.dbgp.commands.IDbgpExtendedCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.internal.core.model.operations.DbgpDebugger;
 import org.eclipse.dltk.debug.internal.core.model.operations.IDbgpDebuggerFeedback;
 
 public class ScriptThread extends ScriptDebugElement implements IScriptThread,
 	IThreadManagement, IDbgpDebuggerFeedback {
 
 	private boolean canSuspend;
 
 	private ScriptThreadManager manager;
 
 	private IScriptThreadStreamProxy streamProxy;
 
 	// Access to frames should be synchronized
 	private List frames = new ArrayList();
 
 	private String name;
 
 	private DbgpDebugger engine;
 		
 	// Session
 	private IDbgpSession session;
 
 	// State variables
 	private volatile boolean stepping;
 
 	private volatile boolean suspended;
 
 	private volatile boolean terminated;
 	
 	private IScriptDebugTarget target;
 
 	// Stop
 	private IDbgpStatus stopDebugger() throws DbgpException {
 		return session.getCoreCommands().stop();
 	}
 
 	// Stack frames
 	private List retrieveStackFrames() throws DbgpException {
 		IDbgpCoreCommands core = session.getCoreCommands();
 
 		int stackDepth = core.getStackDepth();
 		List frames = new ArrayList(stackDepth);
 		for (int i = 0; i < stackDepth; ++i) {
 			frames.add(new ScriptStackFrame(this, core.getStackLevel(i), core, stackDepth));
 		}
 
 		return frames;
 	}
 
 	protected void processOperationEnd(DbgpException exception,
 			IDbgpStatus status, int suspendDetail) {
 		try {
 			if (exception != null) {
 				throw exception;
 			}
 
 			if (status.isBreak()) {
 				setStackFrames();
 				setSuspended(true, suspendDetail);
 			} else if (status.isStopping()) {
 				// Stop
 				if (stopDebugger().isStopped()) {
 					setTerminated();
 				}
 			} else if (status.isStopped()) {				
 				setTerminated();
 			}
 		} catch (DbgpException e) {
 			try {
 				streamProxy.getStdout().write(e.getMessage().getBytes());				
 				setTerminated();
 			} catch (IOException e1) {
 				// TODO: log properly
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	// Steps
 	protected boolean canStep() {
 		return !isTerminated() && isSuspended();
 	}
 
 	protected void beginStep(int detail) {
 		stepping = true;
 		setSuspended(false, detail);
 	}
 
 	protected void endStep(DbgpException execption, IDbgpStatus status) {
 		stepping = false;
 		processOperationEnd(execption, status, DebugEvent.STEP_END);
 	}
 	
 	public ScriptThread(IScriptDebugTarget target, IDbgpSession session,
 			ScriptThreadManager manager) throws DbgpException, CoreException {
 		
 		this.target = target;
 		
 
 		this.manager = manager;
 
 		this.streamProxy = target.getStreamManager().makeThreadStreamProxy();
 
 		this.session = session;
 		this.name = session.getInfo().getThreadId();
 
 		// Initial states
 		this.suspended = true;
 		this.terminated = false;
 
 		// Stepping
 		this.stepping = this.suspended;
 
 		engine = new DbgpDebugger(this, this);
 
 		if (DLTKCore.DEBUG) {
 			DbgpDebugger.printEngineInfo(engine);
 		}
 
 		final IDbgpExtendedCommands extended = session.getExtendedCommands();
 
 		canSuspend = true; //  engine.isSupportsAsync();
 
 		engine.setMaxChildren(256);
 		engine.setMaxDepth(2);
 		engine.setMaxData(8192);
 
 		engine.redirectStdin();
 		engine.setNotifyOk(true);
 
 		engine.redirectStdout();
 		engine.redirectStderr();
 
 		session.getNotificationManager().addNotificationListener(
 				new IDbgpNotificationListener() {
 					private BufferedReader reader = new BufferedReader(
 							new InputStreamReader(getStreamProxy().getStdin()));
 
 					public void dbgpNotify(IDbgpNotification notification) {
 						try {
 							extended.sendStdin(reader.readLine() + "\n");
 						} catch (IOException e) {
 							// TODO: log exception
 							e.printStackTrace();
 						} catch (DbgpException e) {
 							// TODO: log exception
 							e.printStackTrace();
 						}
 					}
 				});
 	}
 
 	public boolean hasStackFrames() throws DebugException {
 		synchronized (frames) {
 			return !frames.isEmpty();
 		}
 	}
 
 	// IThread
 	public IStackFrame[] getStackFrames() throws DebugException {	
 		synchronized (frames) {
 			return (IStackFrame[]) frames
 					.toArray(new IStackFrame[frames.size()]);
 		}
 	}
 
 	public int getPriority() throws DebugException {
 		return 0;
 	}
 
 	public IStackFrame getTopStackFrame() throws DebugException {
 		if (hasStackFrames()) {
 			return getStackFrames()[0];
 		}
 
 		return null;
 	}
 
 	public String getName() throws DebugException {
 		return name;
 	}
 
 	public IBreakpoint[] getBreakpoints() {
 		return new IBreakpoint[0];
 	}
 
 	protected void setStackFrames() throws DbgpException {
		List retrieveStackFrames = retrieveStackFrames();
 		synchronized (frames) {
 			frames.clear();
			frames.addAll(retrieveStackFrames);
 		}
 	}
 
 	protected void setSuspended(boolean value, int detail) {
 		suspended = value;
 		if (value) {
 			
 			DebugEventHelper.fireSuspendEvent(this, detail);
 			
 		} else {
			synchronized( this.frames ) {
				this.frames.clear();
			}
 			DebugEventHelper.fireResumeEvent(this, detail);
 			DebugEventHelper.fireChangeEvent(this);
 		}
 	}
 
 	protected void setTerminated() {
 		terminated = true;
 		session.requestTermination();
 		manager.terminateThread(this);
 	}
 
 	// ISuspendResume
 
 	// Suspend
 	public boolean isSuspended() {
 		return suspended;
 	}
 
 	public boolean canSuspend() {
 		return canSuspend && !isTerminated() && !isSuspended();
 	}
 
 	public void endSuspend(DbgpException e, IDbgpStatus status) {
 		processOperationEnd(e, status, DebugEvent.CLIENT_REQUEST);
 	}
 
 	public void suspend() throws DebugException {
 		setSuspended(true, DebugEvent.CLIENT_REQUEST);
 		engine.suspend();
 	}
 
 	// Resume
 	public boolean canResume() {
 		return !isTerminated() && isSuspended();
 	}
 
 	public void endResume(DbgpException e, IDbgpStatus status) {
 		processOperationEnd(e, status, DebugEvent.BREAKPOINT);
 	}
 
 	public void resume() throws DebugException {
 		setSuspended(false, DebugEvent.CLIENT_REQUEST);
 		
 		engine.resume();
 	}
 
 	// IStep
 	public boolean isStepping() {
 		return !isTerminated() && stepping;
 	}
 
 	// Step into
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
 
 	// Step over
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
 
 	// Step return
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
 
 	// ITerminate
 	public boolean isTerminated() {
 		return terminated;
 	}
 
 	public boolean canTerminate() {
 		return !isTerminated();
 	}
 
 	public void endTerminate(DbgpException e, IDbgpStatus status) {
 		processOperationEnd(e, status, DebugEvent.CLIENT_REQUEST);
 	}
 
 	public void terminate() throws DebugException {
 		engine.terminate();
 	}
 
 	public boolean canTerminateEvaluation() {
 		// TODO: imlement
 		return false;
 	}
 
 	public IDbgpSession getDbgpSession() {
 		return session;
 	}
 
 	public IScriptThreadStreamProxy getStreamProxy() {
 		return streamProxy;
 	}
 
 	public String toString() {
 		return "Thread (" + session.getInfo().getThreadId() + ")";
 	}
 
 	public IDebugTarget getDebugTarget() {
 		return target.getDebugTarget();
 	}
 }

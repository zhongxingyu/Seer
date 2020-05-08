 /**
  * EPLIC - A Tool to Assist Locating Interested Code.
  * Copyright (C) 2013 Frank Wang <eternnoir@gmail.com>
  * 
  * This file is part of EPLIC.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eplic.core.mode;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.debug.core.model.IVariable;
 
 import eplic.core.breakpoint.BreakpointManager;
 import eplic.core.breakpoint.breakpointRecoder;
 import eplic.core.eventHandler.EventCenter;
 /**
  * Interested Mode 紀錄使用者感興趣的操作 
  * 
  * @author FrankWang
  *
  */
 public class InterestedMode extends AbstractMode {
 	private breakpointRecoder _bpr;	
 	private IDebugTarget[] _debugTargets = null;
 	
 	public InterestedMode(){
 		_bpr = new breakpointRecoder();
 		_modeType = 2;
 	}
 	
 	public void onBreakPointTriggered(IVariable[] variables,IBreakpoint breakpoint, IStackFrame[] stacks) {
 		ILineBreakpoint lineBreakpoint = (ILineBreakpoint) breakpoint;
 		_debugTargets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
 		try{
 			IMarker m =lineBreakpoint.getMarker();
 			if(m!=null){
 				_bpr.addBreakPoint(lineBreakpoint);
 			}
 			this.cont();
 		}
 		catch(Exception ex){
 			System.err.print("get breakpoint info error");
 			this.cont();
 		}	
 
 	}
 	
 	public breakpointRecoder getBreakPointRecorder(){
 		assert(_bpr != null);
 		return _bpr;
 	}
 	public void cont(){
 		for(IDebugTarget debugTarget : _debugTargets){
 			try {
 				if(debugTarget.canResume()){
 					debugTarget.resume();
 				}
 			} catch (DebugException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void onTargetTerminated() {		
 		AbstractMode nm = EventCenter.getInstance().getNorMode();
 		if(nm == null){
 			System.err.print("Can't get normalMode");
 			return;
 		}
		BreakpointManager.getInstance().diffResult(this._bpr.getBPS()
				,nm.getBreakPointRecorder().getBPS());
 	}
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }

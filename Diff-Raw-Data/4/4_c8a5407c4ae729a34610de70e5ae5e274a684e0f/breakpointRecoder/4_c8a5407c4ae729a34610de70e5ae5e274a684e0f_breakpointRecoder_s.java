 package eplic.core.breakpoint;
 
 import java.util.ArrayList;
 
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 /**
  * EPLIC - A Tool to Assist Locating Interested Code.
  * Copyright (C) 2013 Frank Wang <eternnoir@gmail.com>
  * 
  * This file is part of EPLIC.
  * 
  * @author FrankWang
  *
  */
 public class breakpointRecoder {
 	private ArrayList<ILineBreakpoint> _bps;
 	
 	public breakpointRecoder(){
 		_bps = new ArrayList<ILineBreakpoint>();
 	}
 	
 	public void addBreakPoint(ILineBreakpoint lineBreakpoint){
 	
 		_bps.add(lineBreakpoint);
 	
 	}
 	
 	public ArrayList<ILineBreakpoint> getBPS(){
 		return	_bps;
 	}
 }

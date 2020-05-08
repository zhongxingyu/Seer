 package de.altimos.mdsd.majordomo.simulator.assemblies;
 
 
 
 
 public class MAssemblyProcessor {
 
 	private MAssemblyRunnable runnable;
 	private boolean lastProcessingRequestState;
 	
 	public MAssemblyProcessor(MAssemblyRunnable mAssemblyRunnable) {
 		runnable = mAssemblyRunnable;
 	}
 
 	public void invoke() {
 		boolean requestState = runnable.processReq();
 		
 		if(lastProcessingRequestState == requestState)
 			return;
 		
 		lastProcessingRequestState = requestState;
 		if(requestState) runnable.process();
 	}
 }

 package org.remus.work;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.remus.RemusPath;
 import org.remus.RemusInstance;
 
 public class SplitGenerator implements WorkGenerator {
 	RemusApplet applet;
 	RemusInstance inst;
 	boolean done;
 	@Override
 	public Set<WorkKey> getActiveKeys(RemusApplet applet,
 			RemusInstance instance, long reqCount) {
 		this.applet = applet;	
 		this.inst = instance;
 		done = false;
 		Set<WorkKey> outList = new HashSet<WorkKey>();
 		if ( applet.datastore.containsKey(applet.getPath() + "@done", instance.toString(), "0" ) &&
				!applet.datastore.containsKey(applet.getPath() + "@error", instance.toString(), "0" )
 		) {
 			done = true;
 			return outList;
 		}		
 		if ( applet.hasInputs() ) {
 			RemusPath iRef = applet.getInput();		
 			String pathStr = "";
 			if ( iRef.getInputType() == RemusPath.AppletInput )
 				pathStr = iRef.getInstancePath();
 			if ( iRef.getInputType() == RemusPath.AttachInput )
 				pathStr = iRef.getPath();
 
 			WorkKey w =  new WorkKey(instance, 0);
 			outList.add( w );
 			w.pathStr = pathStr;
 			//}
 			//}
 		} else {
 			outList.add( new WorkKey( instance, 0) );	
 		}
 		return outList;
 	}
 
 
 	@Override
 	public AppletInstance getAppletInstance() {
 		return new AppletInstance(applet,inst) {		
 			@Override
 			public Object formatWork(Set<WorkKey> keys) {
 				Map obj = new HashMap();
 				Map pathMap = new HashMap();
 				for ( WorkKey wk : keys ) {
 					pathMap.put(wk.jobID, wk.pathStr);
 				}
 				obj.put("input", pathMap );
 				return obj;
 			}
 		};
 	}
 
 	@Override
 	public boolean isDone() {
 		return done;
 	}
 
 }

 package org.kisst.gft.action;
 
 import java.util.LinkedHashMap;
 
 import org.kisst.cfg4j.LayeredProps;
 import org.kisst.cfg4j.Props;
 import org.kisst.cfg4j.SimpleProps;
 import org.kisst.gft.filetransfer.Channel;
 import org.kisst.gft.task.Task;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ActionList  implements Action {
 	final static Logger logger=LoggerFactory.getLogger(ActionList.class); 
 	
 	private final LinkedHashMap<String,Action> actions=new LinkedHashMap<String,Action>();
 
 	public ActionList(Channel chan, Props props) {
 
 		String actions=props.getString("actions");
 		String[] parts=actions.split(",");
 		//this.actions=new Action[parts.length];
 		for (String name: parts) {
 			name=name.trim();
 			LayeredProps lprops=new LayeredProps();
 			SimpleProps top=new SimpleProps();
 			top.put("action",chan.gft.actions.get(name));
 			top.put("channel",props);
 			lprops.addLayer(top);
 			if (props.get(name,null) instanceof Props)
 				lprops.addLayer(props.getProps(name));
 			lprops.addLayer(chan.gft.actions.get(name));
 			lprops.addLayer(props);
 			lprops.addLayer(chan.gft.props.getProps("gft.global"));
 				
 			Action a=chan.createAction(lprops);
 			if (a==null)
 				throw new RuntimeException("Unknown action "+name);
 			this.actions.put(name,a);
 		}
 	}
 	public boolean safeToRetry() { return false; } // TODO: 
 
 	public Object execute(Task task) {
 		for (String name: actions.keySet()) {
 			try {
 				Action a=actions.get(name);
 				if (logger.isDebugEnabled())
 					logger.debug("action "+name+" started");
 				a.execute(task);
 				if (logger.isInfoEnabled())
 					logger.info("action "+name+" succesful");
 			}
 			catch (RuntimeException e) {
 				logger.error("action "+name+" had error: "+e.getMessage());
 				throw e;
 			}
 		}
 		return null;
 	}
 }

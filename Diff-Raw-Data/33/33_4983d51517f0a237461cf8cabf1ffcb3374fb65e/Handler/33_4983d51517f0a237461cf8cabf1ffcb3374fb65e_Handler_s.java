 package ncl.military.controller.handle;
 
 import org.apache.log4j.Logger;
 
 import java.util.Map;
 
 /**
  * @author gural
  * @version 1.0
  *          Date: 13.04.12
  *          Time: 17:06
  */
 public class Handler implements Handlable {
     private Executable executor;
     private String view;
     private String path;
     private String action;
 
     public Executable getExecutor() {
         return executor;
     }
 
     public Handler(Executable executor, String path, String action, String view) {
         this.executor = executor;
         this.path = path;
         this.view = view;
         this.action = action;
     }
 
     public Map<String, Object> execute(Map<String, Object> params) {
         Map<String, Object> result = executor.execute(params);
        result.put("viewType", params.get("userPath"));
 
        Logger.getLogger("controller").debug("[Handler Executed]Puted into result Map: viewType => " + params.get("userPath"));
         Logger.getLogger("controller").debug("[Handler Executed]registered view => " + view);
 
         if (result.get("action") == null) {
             result.put("action", action);
         }
 
         Logger.getLogger("controller").debug("[Handler Executed] Action => " + action);
         Logger.getLogger("controller").debug("[Handler Executed]Puted into result Map: action =>" + action);
 
         return result;
     }
 
     public String getView() {
         return view;
     }
 }

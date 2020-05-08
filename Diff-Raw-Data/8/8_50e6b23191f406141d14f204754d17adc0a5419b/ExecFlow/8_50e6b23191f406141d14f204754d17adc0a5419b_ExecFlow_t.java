 package org.amplafi.flow.shell;
 
 import java.io.Console;
 import java.net.URI;
 import java.util.Map;
 
 import org.amplafi.flow.utils.GeneralFlowRequest;
 import org.amplafi.json.JSONObject;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 /**
  * @author Tuan Nguyen
  */
 public class ExecFlow extends Action {
     static String USAGE = 
         "Usage: \n"+
         "  flow --name=FlowName --param1=<value> --param2=<value>\n" ;
     
     public void exec(Console c, ShellContext context, String args) throws Exception {
         Map<String, String> options = Command.parseOptions(args);
         String fullUri = buildBaseUriString(context) ;
         String flowName = options.get("name") ;
         if(flowName == null) {
             c.printf(USAGE) ;
         } else {
             options.remove("name") ;
             NameValuePair[] params = new NameValuePair[options.size() + 1] ;
             params[0] = fsRenderResult ;
             int index = 1 ;
             for(Map.Entry<String, String> entry : options.entrySet()) {
                 params[index] = new BasicNameValuePair(entry.getKey(), entry.getValue()) ;
                 index++ ;
             }
             GeneralFlowRequest request = new GeneralFlowRequest(URI.create(fullUri), flowName, params);
             String result = request.get();
             if(!result.startsWith("{")) {
                 result = "{\"result\": " + result + "}" ;
             }
            try {
                JSONObject jsonObject = new JSONObject(result ) ;
                c.printf("%1s%n", jsonObject.toString(2));
            } catch(Throwable t) {
               System.out.println(result); 
            }
         }
     }
 
     public String getHelpInstruction() {
         return "Run a flow" ;
     }
 }

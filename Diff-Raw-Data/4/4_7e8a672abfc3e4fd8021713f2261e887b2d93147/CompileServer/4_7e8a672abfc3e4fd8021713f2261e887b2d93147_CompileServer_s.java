 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.net.InetSocketAddress;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import net.minidev.json.JSONObject;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Parser;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
 import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
 import com.thetransactioncompany.jsonrpc2.server.MessageContext;
 import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
 
 
 /*
  * TODO:
  * DONE pull out the filemanager and anything else that is common
  * DONE return compile errors
  * *handle missing file errors, etc
  *
  */
 
 public class CompileServer {
 	public static void main(String[] args) {
 		int portNumber = Integer.parseInt(args[0]);
 		new CompileServer(portNumber);
 	}
 
 	public static String inputStreamToString(InputStream in)  {
 		try{
 			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 			StringBuilder sb = new StringBuilder();
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line).append("\n");
 			}
 			in.close();
 			return sb.toString();
 		}
 		catch (IOException e) {
 			return "";
 		}
 	}
 
 	public static class CompileHandler implements RequestHandler {
 		JavaCompiler compiler;
 		StandardJavaFileManager fileManager;
 
 		public CompileHandler()
 		{
 			compiler = ToolProvider.getSystemJavaCompiler();
 			fileManager = compiler.getStandardFileManager(null, null, null);
 		}
 
 		@Override
 		public String[] handledRequests() {
 			return new String[] {"compile"};
 		}
 
 		@Override
 		public JSONRPC2Response process(JSONRPC2Request req,
 				MessageContext ctx) {
 			String filename = (String) req.getNamedParams().get("filename");
 
 			List<File> files = new LinkedList<File>();
 			files.add(new File(filename));
 
 			StringWriter errors = new StringWriter();
 
 			boolean res = compiler.getTask(errors, fileManager, null, null, null, 
 					fileManager.getJavaFileObjectsFromFiles(files)).call();
 
 			JSONObject obj = new JSONObject();
 			obj.put("compile",res);
			if(!res) obj.put("errors", errors);
			return new JSONRPC2Response(obj);
 		}	
 	}
 
 
 	Dispatcher dispatcher;
 	JSONRPC2Parser parse;
 
 	public CompileServer(int portNumber) {
 		dispatcher = new Dispatcher();
 		parse = new JSONRPC2Parser();
 		dispatcher.register(new CompileHandler());
 		HttpServer server;
 		try {
 			server = HttpServer.create(new InetSocketAddress(portNumber),0);
 			server.createContext("/", new MyHandler());
 			server.setExecutor(null); // creates a default executor
 			server.start();	
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	class MyHandler implements HttpHandler {
 		public void handle(HttpExchange t) throws IOException {
 			String response = "";
 			try {
 				InputStream is = t.getRequestBody();
 				String content = inputStreamToString(is);
 				if(content.length()==0) response = "no request";
 				else {
 					JSONRPC2Request req = parse.parseJSONRPC2Request(content);
 					JSONRPC2Response res = dispatcher.process(req, null);
 					response = res.toJSONObject().toJSONString();
 				}
 			} catch (JSONRPC2ParseException e) {
 				JSONObject obj = new JSONObject();
 				obj.put("compile",false);
 				obj.put("errors", "server error: " + e.getStackTrace());
 				response = obj.toJSONString();
 			} 
 
 			t.sendResponseHeaders(200, response.length());
 			OutputStream os = t.getResponseBody();
 			os.write(response.getBytes());
 			os.close();
 		}
 	}    
 
 }

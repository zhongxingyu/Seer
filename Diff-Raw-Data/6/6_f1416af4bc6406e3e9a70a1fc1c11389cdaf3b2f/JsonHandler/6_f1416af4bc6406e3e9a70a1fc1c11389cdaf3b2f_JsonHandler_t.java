 package il.technion.ewolf.server.handlers;
 
 import static il.technion.ewolf.server.EWolfResponse.RES_SUCCESS;
 import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.server.HttpSessionStore;
 import il.technion.ewolf.server.jsonDataHandlers.JsonDataHandler;
 
 import java.io.IOException;
 import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpDateGenerator;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.inject.Inject;
 
 public class JsonHandler implements HttpRequestHandler {
 	private Map<String,JsonDataHandler> handlers = new HashMap<String,JsonDataHandler>();
 	HttpSessionStore sessionStore;
 
 	@Inject
 	JsonHandler(HttpSessionStore sessionStore) {
 		this.sessionStore = sessionStore;
 	}
 
 	public JsonHandler addHandler(String key, JsonDataHandler handler) {
 		handlers.put(key, handler);
 		return this;
 	}
 
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 
 		boolean authorized = (Boolean) context.getAttribute("authorized");
 
 		String jsonReqAsString = EntityUtils.toString(((HttpEntityEnclosingRequest)req).getEntity());
 		JsonParser parser = new JsonParser();
 		JsonObject jsonReq = parser.parse(jsonReqAsString).getAsJsonObject();
 
 		JsonObject jsonRes = new JsonObject();
 		Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
 
 		Set<Entry<String, JsonElement>> jsonReqAsSet = jsonReq.entrySet();
 		for (Entry<String, JsonElement> obj : jsonReqAsSet) {
 			String key = obj.getKey();
 			if (!authorized) {
 				if (!key.equals("login") && !key.equals("createAccount")) {
 					res.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
 					return;
 				}
 			}
 
 			if (key.equals("logout")) {
				DateFormat dateFormat = new SimpleDateFormat(HttpDateGenerator.PATTERN_RFC1123);
 				Header[] headers = req.getHeaders("Cookie");
 				for (Header h : headers) {
 					String cookie = h.getValue();
 					String content = cookie.substring("session=".length());
 //					String[] nameContent = cookie.split("=");
 //					String content = nameContent[1];
 					sessionStore.deleteSession(content);
 					res.setHeader("Set-Cookie", "session=;Expires=" +
 							dateFormat.format(new Date()));
 				}
 				return;
 			}
 			JsonDataHandler handler = handlers.get(key);
 			if (handler != null) {
 				EWolfResponse handlerRes = handler.handleData(obj.getValue());
 				jsonRes.add(key, gson.toJsonTree(handlerRes));
 				if (handlerRes.getResult().equals(RES_SUCCESS) && !authorized) {
 					String cookie = sessionStore.createSession();
 					res.addHeader("Set-Cookie", "session=" + cookie);
 				}
 			} else {
 				System.out.println("No handlers are registered to handle request " +
 						"with keyword \"" + key + "\"");
 				jsonRes.addProperty("result", "unavailable request");
 			}     	
 		}
 
 		String json = gson.toJson(jsonRes);
 		res.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
 	}
 }

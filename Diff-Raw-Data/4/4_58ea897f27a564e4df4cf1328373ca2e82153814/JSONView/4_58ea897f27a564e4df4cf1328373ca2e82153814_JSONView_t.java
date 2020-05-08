 package nz.co.searchwellington.views;
 
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.model.Resource;
 
 import org.springframework.web.servlet.View;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.SingleValueConverter;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 import com.thoughtworks.xstream.io.json.JsonWriter;
 
 public class JSONView implements View {
 	
 	public String getContentType() {
 		return "text/plain";
 	}
 
 	@SuppressWarnings("unchecked")
 	public void render(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {
 		res.setContentType("text/plain");
 		res.setCharacterEncoding("UTF-8");
 
 		StringBuilder output = new StringBuilder();
 	
 		String jsonString = createJSONString(model);
 		String callbackName = (String) model.get("callback");		
 		if (callbackName != null) {
 			final String callback = (String) callbackName;
 			output.append(callback + "(");
 			output.append(jsonString);
 			output.append(");");
 		} else {
 			output.append(jsonString);
 		}
 		
		res.getWriter().print(output.toString());        
 		res.getOutputStream().flush();		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private String createJSONString(Map model) {
 		List <Resource> mainContent =  (List <Resource>) model.get("main_content");
 		List<JSONFeedItem> jsonItems = new ArrayList<JSONFeedItem>();
 		for (Resource rssFeedable : mainContent) {
 			JSONFeedItem jsonFeeditem;			
 			if (rssFeedable.getGeocode() != null && rssFeedable.getGeocode().isValid()) {
 				jsonFeeditem = new JSONFeedItem(
 						rssFeedable.getName(), 
 						rssFeedable.getUrl(), 
 						rssFeedable.getDate(), 
 						rssFeedable.getDescription(), rssFeedable.getGeocode().getLatitude(), rssFeedable.getGeocode().getLongitude());				
 			} else {				
 				jsonFeeditem = new JSONFeedItem(
 						rssFeedable.getName(), 
 						rssFeedable.getUrl(), 
 						rssFeedable.getDate(), 
 						rssFeedable.getDescription(), null, null);				
 			}
 			
 			jsonItems.add(jsonFeeditem);
 		}
 					
 		XStream xstream = new XStream(new JettisonMappedXmlDriver() {
 		    public HierarchicalStreamWriter createWriter(Writer writer) {
 		        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
 		    }
 		});
 		
 		SingleValueConverter resourceDateConverter = new ResourceDateConvertor();
 		xstream.registerLocalConverter(JSONFeedItem.class, "date", resourceDateConverter);	
 		xstream.alias("date", java.sql.Date.class);
 		
 		JSONBucket bucket = new JSONBucket();
 		if (model.get("main_content_total") != null) {
 			bucket.setTotalItems((Integer) model.get("main_content_total"));
 		}
 		if (model.get("start_index") != null) {
 			bucket.setShowingFrom((Integer) model.get("start_index"));
 		}
 		if (model.get("end_index") != null) {
 			bucket.setShowingTo((Integer) model.get("end_index"));
 		}
 		
 		bucket.setNewsitems(jsonItems);
 		String jsonString = xstream.toXML(bucket);
 		return jsonString;
 	}
 	
 }

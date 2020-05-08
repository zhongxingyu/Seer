 package fr.utc.nf28.moka.rest.resource;
 
 // Plain old Java Object it does not extend as class or implements
 // an interface
 
 // The class registers its methods for the HTTP GET request using the @GET annotation.
 // Using the @Produces annotation, it defines that it can deliver several MIME types,
 // text, XML and HTML.
 
 // The browser requests per default the HTML MIME type.
 
 import fr.utc.nf28.moka.environment.HistoryEntry;
 import fr.utc.nf28.moka.util.JSONParserUtils;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 //Sets the path to base URL + /hello
 @Path("/history")
 public class HelloResource {
	private final List<HistoryEntry> mHistoryEntries = new ArrayList<HistoryEntry>(5);

	public HelloResource() {
		for (int i = 1; i <= 5; i++) {
			mHistoryEntries.add(new HistoryEntry("Historique n°" + i));
		}
	}
 
 	// This method is called if TEXT_PLAIN is request
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sayPlainTextHello() {
		return mHistoryEntries.toString();
 	}
 
	// This method is called if JSON is request
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String sayJSONHello() {
 		try {
			return JSONParserUtils.serializeHistory(mHistoryEntries);
 		} catch (IOException e) {
 			System.out.println("Error");
 			return "";
 		}
 	}
 }

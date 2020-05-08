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
 
 	// This method is called if TEXT_PLAIN is request
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sayPlainTextHello() {
		return "Hello Jersey";
	}

	// This method is called if XML is request
	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXMLHello() {
		return "<?xml version=\"1.0\"?>" + "<hello> Hello Jersey" + "</hello>";
	}

	// This method is called if HTML is request
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {
		return "<html> " + "<title>" + "Hello Jersey" + "</title>"
				+ "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String sayJSONHello() {
		final List<HistoryEntry> historyEntries = new ArrayList<HistoryEntry>(5);
		for (int i = 1; i <= 5; i++) {
			historyEntries.add(new HistoryEntry("Historique n°" + i));
		}
 		try {
			return JSONParserUtils.serializeHistory(historyEntries);
 		} catch (IOException e) {
 			System.out.println("Error");
 			return "";
 		}
 	}
 }

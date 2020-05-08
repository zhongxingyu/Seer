 package il.technion.ewolf.server;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
import org.apache.http.Consts;
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 public class SFSUploadHandler implements HttpRequestHandler {
 
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
		//TODO move adding server header to response intercepter
 		res.addHeader(HTTP.SERVER_HEADER, "e-WolfNode");
 		
 		String uri = req.getRequestLine().getUri();
 	
 		try {
 			List<NameValuePair> parameters = 
					URLEncodedUtils.parse(new URI(uri).getQuery(),Consts.UTF_8);
 			
 			for (NameValuePair v : parameters) {
 				System.out.println(v.getName() + ": "+v.getValue());
 			}
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 
 		((HttpEntityEnclosingRequest)req).getEntity();
 	}
 }

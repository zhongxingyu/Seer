 package jewas.http;
 
 
 
 public interface HttpRequest {
 	public HttpMethod method();
 	public String uri();
 	public Headers headers();
 	public String path();
 	public Parameters parameters();
 	public JsonResponse respondJson();
 	
 	public HttpRequest addContentHandler(ContentHandler h);
 }

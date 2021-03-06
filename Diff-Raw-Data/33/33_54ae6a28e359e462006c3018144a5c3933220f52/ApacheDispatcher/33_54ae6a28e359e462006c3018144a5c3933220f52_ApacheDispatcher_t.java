 package br.com.caelum.restfulie.http.apache;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.URI;
 import java.util.Map;
 
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
 import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpTrace;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.ContentProducer;
 import org.apache.http.entity.EntityTemplate;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import br.com.caelum.restfulie.Response;
 import br.com.caelum.restfulie.RestClient;
 import br.com.caelum.restfulie.RestfulieException;
 import br.com.caelum.restfulie.http.Request;
 import br.com.caelum.restfulie.mediatype.MediaType;
 import br.com.caelum.restfulie.request.RequestDispatcher;
 
 public class ApacheDispatcher implements RequestDispatcher {
 
 	private final HttpClient http = new DefaultHttpClient();
 	private final RestClient client;
 	private final HttpContext context;
 	private ApacheResponse lastExecuted;
 
 	public ApacheDispatcher(RestClient client) {
 		this.client = client;
 		this.context = new BasicHttpContext();
 		context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
 	}
 
 	public Response process(Request details, String verb, URI uri,
 			final Object payload) {
 
 		if (payload == null) {
 			return access(details, verb, uri);
 		}
 
 		final Map<String, String> headers = details.getHeaders();
 
 		if (!headers.containsKey("Content-type")) {
 			throw new RestfulieException(
 					"You should set a content type prior to sending some payload.");
 		}
 
 		ContentProducer cp = new ContentProducer() {
 			public void writeTo(OutputStream outstream) throws IOException {
 				Writer writer = new OutputStreamWriter(outstream, "UTF-8");
 				String type = headers.get("Content-type");
 				handlerFor(type).marshal(payload, writer);
 				writer.flush();
 			}
 		};
 		HttpPost post = new HttpPost(uri);
 		add(post, headers);
 		post.setEntity(new EntityTemplate(cp));
 
 		return execute(details, post);
 	}
 
 	private HttpContext getContext() {
 		return this.context;
 	}
 
 	private MediaType handlerFor(String type) {
 		return client.getMediaTypes().forContentType(type);
 	}
 
 	private void add(HttpRequest method, Map<String, String> headers) {
 		for (String header : headers.keySet()) {
 			method.addHeader(header, headers.get(header));
 		}
 	}
 
	private Response access(Request request, String method, URI uri) {
		HttpUriRequest verb = verbFor(method, uri);
		add(verb, request.getHeaders());
		return execute(request, verb);
	}
 
	private HttpUriRequest verbFor(String method, URI uri) {
		method = method.toUpperCase();
		if(method.equals("GET")) {
			return new HttpGet(uri);
		} else if(method.equals("DELETE")) {
			return new HttpDelete(uri);
		} else if(method.equals("TRACE")) {
			return new HttpTrace(uri);
		} else if(method.equals("OPTIONS")) {
			return new HttpOptions(uri);
		} else if(method.equals("HEAD")) {
			return new HttpHead(uri);
		}
		throw new RestfulieException("You can not " + method + " to " + uri + " without a payload.");
 	}
 
 	private ApacheResponse execute(Request details, HttpUriRequest method) {
 		try {
 			if (lastExecuted != null) {
 				lastExecuted.discard();
 			}
 			HttpResponse response = http.execute(method, getContext());
 			return responseFor(response);
 		} catch (ClientProtocolException e) {
 			throw new RestfulieException(
 					"Unable to execute " + method.getURI(), e);
 		} catch (IOException e) {
 			throw new RestfulieException(
 					"Unable to execute " + method.getURI(), e);
 		}
 	}
 
 	private ApacheResponse responseFor(HttpResponse response)
 			throws IOException {
 		this.lastExecuted = new ApacheResponse(response, client);
 		return lastExecuted;
 	}
 
 }

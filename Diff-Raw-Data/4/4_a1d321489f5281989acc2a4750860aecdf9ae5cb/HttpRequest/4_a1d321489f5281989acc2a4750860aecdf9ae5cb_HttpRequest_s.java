 /**
  * 
  */
 package net.anyflow.menton.http;
 
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.Channel;
 import io.netty.handler.codec.DecoderResult;
 import io.netty.handler.codec.http.ClientCookieEncoder;
 import io.netty.handler.codec.http.Cookie;
 import io.netty.handler.codec.http.CookieDecoder;
 import io.netty.handler.codec.http.DefaultFullHttpRequest;
 import io.netty.handler.codec.http.FullHttpRequest;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpMethod;
 import io.netty.handler.codec.http.QueryStringDecoder;
 import io.netty.util.CharsetUtil;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author anyflow
  */
 public class HttpRequest extends DefaultFullHttpRequest {
 
 	private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
 
 	private Channel channel;
 	private final Map<String, List<String>> parameters;
 	private final URI uri;
 	private final Set<Cookie> cookies;
 
 	/**
 	 * @param channel
 	 * @param httpVersion
 	 * @param method
 	 * @param uri
 	 * @param content
 	 * @param headers
 	 * @param decoderResult
 	 * @throws URISyntaxException
 	 */
 	public HttpRequest(Channel channel, FullHttpRequest fullHttpRequest) throws URISyntaxException {
 		super(fullHttpRequest.getProtocolVersion(), fullHttpRequest.getMethod(), fullHttpRequest.getUri(), fullHttpRequest.content().copy());
 
 		this.channel = channel;
 		this.headers().set(fullHttpRequest.headers());
 		this.trailingHeaders().set(fullHttpRequest.trailingHeaders());
 		this.setDecoderResult(fullHttpRequest.getDecoderResult());
 		this.uri = new URI(fullHttpRequest.getUri());
 		this.parameters = parameters();
 		this.cookies = cookies();
 	}
 
 	public String host() {
 		return this.headers().get(HttpHeaders.Names.HOST);
 	}
 
 	/**
 	 * @return
 	 */
 	public Set<Cookie> cookies() {
 		if(cookies != null) { return cookies; }
 
 		String cookie = headers().get(HttpHeaders.Names.COOKIE);
 		if(cookie == null || "".equals(cookie)) { return new HashSet<Cookie>(); }
 
 		Set<Cookie> ret = CookieDecoder.decode(cookie);
 
 		if(ret.isEmpty()) {
 			return new HashSet<Cookie>();
 		}
 		else {
 			return ret;
 		}
 	}
 
 	public Map<String, List<String>> parameters() {
 
 		if(parameters != null) { return parameters; }
 
 		String queryString = null;
 
 		if(getMethod().equals(HttpMethod.GET)) {
 			queryString = getUri();
 		}
 		else if(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED.equals(headers().get(HttpHeaders.Names.CONTENT_TYPE))
 				&& (HttpMethod.POST.equals(getMethod()) || HttpMethod.PUT.equals(getMethod()))) {
 			String dummy = "/dummy?";
 			queryString = dummy + content().toString(CharsetUtil.UTF_8);
 		}
 		else {
 			return new HashMap<String, List<String>>();
 		}
 
 		Map<String, List<String>> ret = (new QueryStringDecoder(queryString)).parameters();
 
 		return ret.isEmpty() ? new HashMap<String, List<String>>() : ret;
 	}
 
 	/**
 	 * Get single parameter. In case of multiple values, the method returns the first.
 	 * 
 	 * @param name
 	 *            parameter name.
 	 * @return The first value of the parameter name. If it does not exist, it returns an empty string.
 	 */
 	public String parameter(String name) {
 
 		if(parameters().containsKey(name) == false || parameters.get(name).size() <= 0) { return null; }
 
 		return parameters().get(name).get(0);
 	}
 
 	public HttpRequest addParameter(String name, String value) {
 
 		List<String> values = parameters().get(name);
 		if(values == null) {
 			values = new ArrayList<String>();
 			values.add(value);
 			parameters().put(name, values);
 		}
 		else {
 			values.clear();
 			values.add(value);
 		}
 
 		return this;
 	}
 
 	public Channel channel() {
 		return channel;
 	}
 
 	public void setChannel(Channel channel) {
 		this.channel = channel;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see io.netty.handler.codec.http.DefaultHttpRequest#toString()
 	 */
 	@Override
 	public String toString() {
 
 		StringBuilder buf = new StringBuilder();
 
 		buf.setLength(0);
 		buf.append("VERSION: ").append(this.getProtocolVersion()).append("\r\n");
 		buf.append("HOSTNAME: ").append(HttpHeaders.getHost(this, "unknown")).append("\r\n");
 		buf.append("REQUEST_URI: ").append(this.getUri()).append("\r\n\r\n");
 
 		List<Entry<String, String>> headers = this.headers().entries();
 		if(!headers.isEmpty()) {
 			for(Entry<String, String> h : this.headers().entries()) {
 				String key = h.getKey();
 				String value = h.getValue();
 				buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
 			}
 			buf.append("\r\n");
 		}
 
 		Map<String, List<String>> params = parameters();
 		if(!params.isEmpty()) {
 			for(Entry<String, List<String>> p : params.entrySet()) {
 				String key = p.getKey();
 				List<String> vals = p.getValue();
 				for(String val : vals) {
 					buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
 				}
 			}
 			buf.append("\r\n");
 		}
 
 		DecoderResult result = this.getDecoderResult();
 
 		if(result.isSuccess() == false) {
 			buf.append(".. WITH DECODER FAILURE: ");
 			buf.append(result.cause());
 			buf.append("\r\n");
 		}
 
 		return buf.toString();
 	}
 
 	public void setContent(String content) {
 		if(content == null) {
 			content = "";
 		}
 
		content().writeBytes(content.getBytes(CharsetUtil.UTF_8));
 		logger.debug(content().toString(CharsetUtil.UTF_8));
 	}
 
 	public URI uri() {
 		return uri;
 	}
 
 	public void normalize() {
 		setupParameters();
 
 		headers().set(HttpHeaders.Names.COOKIE, ClientCookieEncoder.encode(cookies));
 	}
 
 	private String convertParametersToString() {
 
 		StringBuilder builder = new StringBuilder();
 
 		for(String name : parameters().keySet()) {
 
 			for(String value : parameters().get(name)) {
 				builder = builder.append(name).append("=").append(value).append("&");
 			}
 		}
 
 		String ret = builder.toString();
 		if(ret.length() <= 0) { return ""; }
 
 		if(ret.charAt(ret.length() - 1) == '&') {
 			return ret.substring(0, ret.length() - 1);
 		}
 		else {
 			return ret;
 		}
 	}
 
 	private void setupParameters() {
 
 		String address = (new StringBuilder()).append(uri().getScheme()).append("://").append(uri().getAuthority()).append(uri().getPath())
 				.toString();
 
 		if(getMethod() == HttpMethod.GET) {
 			address += "?" + convertParametersToString();
 		}
 		else if(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED.equals(headers().get(HttpHeaders.Names.CONTENT_TYPE))
 				&& (HttpMethod.POST.equals(getMethod()) || HttpMethod.PUT.equals(getMethod()))) {
 
 			ByteBuf content = Unpooled.copiedBuffer(convertParametersToString(), Charset.forName("UTF-8"));
 
 			headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
 			content().clear();
 			content().writeBytes(content);
 		}
 
 		setUri(address);
 	}
 }

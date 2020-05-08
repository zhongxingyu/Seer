 package com.nityankhanna.androidutils.http;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created by Nityan Khanna on Jan 18 2014.
  */
 public final class HttpRequestMessage implements HttpCookieStore, HttpHeaderStore, HttpMessage, HttpParameterStore {
 
 	private ContentType contentType;
 	private List<HttpCookie> cookies;
 	private Encoding encoding;
 	private List<HttpHeader> headers;
 	private List<HttpParameter> params;
 	private RequestType requestType;
 	private String url;
 
 	public HttpRequestMessage(String url, RequestType requestType) {
 		this.url = url;
 		this.requestType = requestType;
 	}
 
 	public HttpRequestMessage(String url, RequestType requestType, ContentType contentType, Encoding encoding) {
 		this(url, requestType);
		cookies = new ArrayList<HttpCookie>();
 		this.contentType = contentType;
 		this.encoding = encoding;
		headers = new ArrayList<HttpHeader>();
		params = new ArrayList<HttpParameter>();
 	}
 
 	/**
 	 * Checks if the HttpRequestMessage contains cookies.
 	 *
 	 * @return Returns true if the HttpRequestMessage contains cookies.
 	 */
 	@Override
 	public boolean containsCookies() {
 		return cookies.size() > 0;
 	}
 
 	/**
 	 * Checks if the HttpRequestMessage contains headers.
 	 *
 	 * @return Returns true if the HttpRequestMessage contains headers.
 	 */
 	@Override
 	public boolean containsHeaders() {
 		return headers.size() > 0;
 	}
 
 	/**
 	 * Checks if the HttpRequestMessage contains parameters.
 	 *
 	 * @return Returns true if the HttpRequestMessage contains parameters.
 	 */
 	@Override
 	public boolean containsParameters() {
 		return params.size() > 0;
 	}
 
 	/**
 	 * Gets the content type of the message.
 	 *
 	 * @return Returns an HttpHeader with the content type.
 	 */
 	@Override
 	public ContentType getContentType() {
 		return contentType;
 	}
 
 	/**
 	 * Gets the encoding of the message.
 	 *
 	 * @return Returns the encoding.
 	 */
 	@Override
 	public Encoding getEncoding() {
 		return encoding;
 	}
 
 	/**
 	 * Adds a cookie to the collection.
 	 *
 	 * @param cookie The cookie.
 	 */
 	@Override
 	public void addCookie(HttpCookie cookie) {
 
 		if (cookie == null) {
 			throw new IllegalArgumentException("The cookie parameter cannot be null");
 		}
 
 		removeDuplicateCookie(cookie);
 
 		if (!cookie.isExpired(new Date())) {
 			cookies.add(cookie);
 		}
 	}
 
 	/**
 	 * Adds a cookie to the collection at the specified index.
 	 *
 	 * @param index  The index.
 	 * @param cookie The cookie.
 	 */
 	@Override
 	public void addCookie(int index, HttpCookie cookie) {
 
 		if (index < 0) {
 			throw new IllegalArgumentException("Invalid index " + index);
 		}
 
 		if (cookie == null) {
 			throw new IllegalArgumentException("The cookie parameter cannot be null");
 		}
 
 		removeDuplicateCookie(cookie);
 
 		cookies.add(index, cookie);
 	}
 
 	/**
 	 * Gets a list of cookies.
 	 *
 	 * @return Returns a list of cookies.
 	 */
 	@Override
 	public List<HttpCookie> getCookies() {
 		return cookies;
 	}
 
 	/**
 	 * Clears the expired cookies from the collection.
 	 *
 	 * @param date The date.
 	 */
 	@Override
 	public void clearExpired(Date date) {
 
 		for (Iterator<HttpCookie> it = cookies.iterator(); it.hasNext(); ) {
 
 			if (it.next().isExpired(date)) {
 				it.remove();
 			}
 		}
 	}
 
 	/**
 	 * Removes a cookie from the collection.
 	 *
 	 * @param cookie The cookie.
 	 */
 	@Override
 	public void removeCookie(HttpCookie cookie) {
 
 		if (cookie == null) {
 			throw new IllegalArgumentException("The cookie parameter cannot be null");
 		}
 
 		cookies.remove(cookie);
 	}
 
 	/**
 	 * Removes a cookie from the collection at the specified index.
 	 *
 	 * @param index The index.
 	 */
 	@Override
 	public void removeCookie(int index) {
 
 		if (index < 0) {
 			throw new IllegalArgumentException("Invalid index " + index);
 		}
 
 		cookies.remove(index);
 	}
 
 	/**
 	 * Removes all cookies from the collection.
 	 */
 	@Override
 	public void removeAllCookies() {
 		cookies.clear();
 	}
 
 	/**
 	 * Adds an HTTP header to the request.
 	 *
 	 * @param header The HTTP header to add to the collection.
 	 */
 	@Override
 	public void addHeader(HttpHeader header) {
 
 		if (header == null) {
 			throw new IllegalArgumentException("The header parameter cannot be null");
 		}
 
 		removeDuplicateHeader(header);
 
 		headers.add(header);
 	}
 
 	/**
 	 * Adds an HTTP header to the collection at the specified index.
 	 *
 	 * @param index  The index of where to add the header.
 	 * @param header The HTTP header to add to the collection.
 	 */
 	@Override
 	public void addHeader(int index, HttpHeader header) {
 
 		if (index < 0) {
 			throw new IllegalArgumentException("Invalid index " + index);
 		}
 
 		if (header == null) {
 			throw new IllegalArgumentException("The header parameter cannot be null");
 		}
 
 		removeDuplicateHeader(header);
 
 		headers.add(index, header);
 	}
 
 	/**
 	 * Returns a list of HTTP headers.
 	 *
 	 * @return Returns a list of HTTP headers.
 	 */
 	@Override
 	public List<HttpHeader> getHeaders() {
 		return headers;
 	}
 
 	/**
 	 * Removes an HTTP Header.
 	 *
 	 * @param header The HTTP header object to be removed.
 	 */
 	@Override
 	public void removeHeader(HttpHeader header) {
 
 		if (header == null) {
 			throw new IllegalArgumentException("The header parameter cannot be null");
 		}
 
 		headers.remove(header);
 	}
 
 	/**
 	 * Removes an HTTP header.
 	 *
 	 * @param index The index of the header to remove.
 	 */
 	@Override
 	public void removeHeader(int index) {
 
 		if (index < 0) {
 			throw new IllegalArgumentException("Invalid index " + index);
 		}
 
 		headers.remove(index);
 	}
 
 	/**
 	 * Removes all of the current HTTP headers.
 	 */
 	@Override
 	public void removeAllHeaders() {
 		headers.clear();
 	}
 
 	/**
 	 * Adds an HttpParameter to the collection.
 	 *
 	 * @param parameter The HttpParameter to add.
 	 */
 	@Override
 	public void addParameter(HttpParameter parameter) {
 
 		if (parameter == null) {
 			throw new IllegalArgumentException("The parameter object cannot be null");
 		}
 
 		removeDuplicateParameter(parameter);
 
 		params.add(parameter);
 	}
 
 	/**
 	 * Adds an HttpParameter to the collection at the specified index.
 	 *
 	 * @param index     The index.
 	 * @param parameter The HttpParameter to add.
 	 */
 	@Override
 	public void addParameter(int index, HttpParameter parameter) {
 
 		if (index < 0) {
 			throw new IllegalArgumentException("Invalid index " + index);
 		}
 
 		if (parameter == null) {
 			throw new IllegalArgumentException("The parameter object cannot be null");
 		}
 
 		removeDuplicateParameter(parameter);
 
 		params.add(index, parameter);
 	}
 
 	/**
 	 * Gets a list of HttpParameters.
 	 *
 	 * @return Returns a list of HttpParameters.
 	 */
 	@Override
 	public List<HttpParameter> getParameters() {
 		return params;
 	}
 
 	/**
 	 * Removes a parameter.
 	 *
 	 * @param parameter The HttpParameter to be removed.
 	 */
 	@Override
 	public void removeParameter(HttpParameter parameter) {
 
 		if (parameter == null) {
 			throw new IllegalArgumentException("The parameter object cannot be null");
 		}
 
 		params.remove(parameter);
 	}
 
 	/**
 	 * Removes the parameter at the specified index.
 	 *
 	 * @param index The index.
 	 */
 	@Override
 	public void removeParameter(int index) {
 
 		if (index < 0) {
 			throw new IllegalArgumentException("Invalid index " + index);
 		}
 
 		params.remove(index);
 	}
 
 	/**
 	 * Removes all the parameters from the collection.
 	 */
 	@Override
 	public void removeAllParameters() {
 		params.clear();
 	}
 
 	public List<HttpParameter> getParams() {
 		return params;
 	}
 
 	public RequestType getRequestType() {
 		return requestType;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	private void removeDuplicateCookie(HttpCookie cookie) {
 
 		for (Iterator<HttpCookie> it = cookies.iterator(); it.hasNext(); ) {
 			if (cookie.equals(it.next())) {
 				it.remove();
 				break;
 			}
 		}
 	}
 
 	private void removeDuplicateHeader(HttpHeader header) {
 
 		for (Iterator<HttpHeader> it = headers.iterator(); it.hasNext(); ) {
 			if (header.equals(it.next())) {
 				it.remove();
 				break;
 			}
 		}
 	}
 
 	private void removeDuplicateParameter(HttpParameter parameter) {
 
 		for (Iterator<HttpParameter> it = params.iterator(); it.hasNext(); ) {
 			if (parameter.equals(it.next())) {
 				it.remove();
 				break;
 			}
 		}
 	}
 }

 /*
  * Reactor - HttpState.java - Copyright © 2013 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.reactor.states;
 
 import java.io.UnsupportedEncodingException;
 
 import net.pterodactylus.reactor.State;
 import net.pterodactylus.reactor.queries.HttpQuery;
 
 import org.apache.http.HeaderElement;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicHeaderValueParser;
 
 /**
  * {@link State} that contains the results of an {@link HttpQuery}.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class HttpState extends AbstractState {
 
 	/** The URI that was requested. */
 	private final String uri;
 
 	/** The protocol code. */
 	private final int protocolCode;
 
 	/** The content type. */
 	private final String contentType;
 
 	/** The result. */
 	private final byte[] rawResult;
 
 	/**
 	 * Creates a new HTTP state.
 	 *
 	 * @param uri
 	 *            The URI that was requested
 	 * @param protocolCode
 	 *            The code of the reply
 	 * @param contentType
 	 *            The content type of the reply
 	 * @param rawResult
 	 *            The raw result
 	 */
 	public HttpState(String uri, int protocolCode, String contentType, byte[] rawResult) {
 		this.uri = uri;
 		this.protocolCode = protocolCode;
 		this.contentType = contentType;
 		this.rawResult = rawResult;
 	}
 
 	//
 	// ACCESSORS
 	//
 
 	/**
 	 * Returns the URI that was requested.
 	 *
 	 * @return The URI that was request
 	 */
 	public String uri() {
 		return uri;
 	}
 
 	/**
 	 * Returns the protocol code of the reply.
 	 *
 	 * @return The protocol code of the reply
 	 */
 	public int protocolCode() {
 		return protocolCode;
 	}
 
 	/**
 	 * Returns the content type of the reply.
 	 *
 	 * @return The content type of the reply
 	 */
 	public String contentType() {
 		return contentType;
 	}
 
 	/**
 	 * Returns the raw result of the reply.
 	 *
 	 * @return The raw result of the reply
 	 */
 	public byte[] rawResult() {
 		return rawResult;
 	}
 
 	/**
 	 * Returns the decoded content of the reply. This method uses the charset
 	 * information from the {@link #contentType()}, if present, or UTF-8 if no
 	 * content type is present.
 	 *
 	 * @return The decoded content
 	 */
 	public String content() {
 		try {
 			return new String(rawResult(), extractCharset(contentType()));
 		} catch (UnsupportedEncodingException uee1) {
 			throw new RuntimeException(String.format("Could not decode content as %s.", extractCharset(contentType())), uee1);
 		}
 	}
 
 	//
 	// STATIC METHODS
 	//
 
 	/**
 	 * Extracts charset information from the given content type.
 	 *
 	 * @param contentType
 	 *            The content type response header
 	 * @return The extracted charset, or UTF-8 if no charset could be extracted
 	 */
 	private static String extractCharset(String contentType) {
 		if (contentType == null) {
			return "UTF-8";
 		}
 		HeaderElement headerElement = BasicHeaderValueParser.parseHeaderElement(contentType, new BasicHeaderValueParser());
 		NameValuePair charset = headerElement.getParameterByName("charset");
		return (charset != null) ? charset.getValue() : "UTF-8";
 	}
 
 	//
 	// OBJECT METHODS
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		return String.format("%s[uri=%s,protocolCode=%d,contentType=%s,rawResult=(%s bytes)]", getClass().getSimpleName(), uri(), protocolCode(), contentType(), rawResult().length);
 	}
 
 }

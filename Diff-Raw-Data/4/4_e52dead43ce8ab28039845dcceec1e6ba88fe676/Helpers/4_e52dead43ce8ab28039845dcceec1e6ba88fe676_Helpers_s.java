 package com.ted.helpers;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 public final class Helpers {
 	private static final String CHARSET_FOR_URL_ENCODING = "UTF-8";;
 
 	private Helpers() {
 		// prevent instantiation;
 	}
 
 	public static String encodeUri(String baseLink, String parameter)
 			throws UnsupportedEncodingException {
 		return encodeString(baseLink, parameter);
 	}
 
 	private static String encodeString(String baseLink, String parameter)
 			throws UnsupportedEncodingException {
 		return String.format(baseLink + "%s",
 				URLEncoder.encode(parameter, CHARSET_FOR_URL_ENCODING));
 	}
 
 	public static String decodeRequest(String parameter)
 			throws UnsupportedEncodingException {
 		if (parameter == null)
 			return null;
 		System.out.println("decode - request.getBytes(\"iso-8859-1\"):"
 				+ new String(parameter.getBytes("iso-8859-1")));
 		System.out.println("decode - request.getBytes(\"iso-8859-1\") BYTES:"
 				+ parameter.getBytes("iso-8859-1"));
 		for (byte iterable_element : parameter.getBytes("iso-8859-1")) {
 			System.out.println(iterable_element);
 		}
 		System.out.println("decode - request.getBytes(\"UTF-8\"):"
 				+ new String(parameter.getBytes(CHARSET_FOR_URL_ENCODING)));
		return URLDecoder.decode(new String(parameter.getBytes("iso-8859-1")),
 				CHARSET_FOR_URL_ENCODING);
 	}
 }

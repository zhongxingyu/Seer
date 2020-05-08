 /* 
  * Eddie RSS and Atom feed parser
  * Copyright (C) 2006  David Pashley
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  * Linking this library statically or dynamically with other modules is making a
  * combined work based on this library. Thus, the terms and conditions of the GNU
  * General Public License cover the whole combination.
  * 
  * As a special exception, the copyright holders of this library give you
  * permission to link this library with independent modules to produce an
  * executable, regardless of the license terms of these independent modules, and
  * to copy and distribute the resulting executable under a liense certified by the
  * Open Source Initative (http://www.opensource.org), provided that you also meet,
  * for each linked independent module, the terms and conditions of the license of
  * that module. An independent module is a module which is not derived from or
  * based on this library. If you modify this library, you may extend this
  * exception to your version of the library, but you are not obligated to do so.
  * If you do not wish to do so, delete this exception statement from your version.
  */
 package uk.org.catnip.eddie.parser;
 
 import java.io.*;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CodingErrorAction;
 import java.nio.charset.MalformedInputException;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.apache.xerces.parsers.SAXParser;
 import uk.org.catnip.eddie.FeedData;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 /**
  * Main class for parsing feeds. To parse a file you need to create a new parser
  * object, optionally let it know any HTTP headers and than ask it to parse the
  * file. You will receive a Feed object in return.
  * 
  * <pre>
  * Parser parser = new Parser();
  * parser.setHeaders(headers);
  * Feed feed = parser.parse(filename);
  * </pre>
  * 
  * @author david
  */
 public class Parser {
 	private static Logger log = Logger.getLogger(Parser.class);
 	private String encoding;
     private String defaultEncoding = "utf-8";
 	private Map headers;
     private DetectEncoding de = new DetectEncoding();
     boolean error  = false;
 	/**
 	 * Inform the parser of any external HTTP headers. The parser currently
 	 * understands Content-Location and Content-Language which are used to set
 	 * the default base and language values.
 	 * 
 	 * @param headers
 	 *            set of HTTP headers
 	 */
 	public void setHeaders(Map headers) {
 		this.headers = headers;
         if (headers.containsKey("Content-type")) {
             String contenttype = (String) headers.get("Content-type");
             Pattern pattern = Pattern.compile("([^;]*)(;\\s*charset\\s*=\\s*[\"']?([^\"']+)[\"']?)?.*?");
             Matcher matcher = pattern.matcher(contenttype);
             if (matcher.matches()) {  
                 encoding = matcher.group(3);
                 String type = matcher.group(1);
                 if ("text/atom+xml".equals(type) 
                         || "text/rss+xml".equals(type)
                         || "text/xml-external-parsed-entity".equals(type)
                         ) {
                     defaultEncoding = "us-ascii";
                     encoding = "us-ascii";
                 } else if ("text/xml".equals(type) && encoding == null) {
                     defaultEncoding = "us-ascii";
                     encoding = "us-ascii";
                     //if (!contenttype.contains(";")) {
                     //    error = true;
                     //}
                 }
                 log.debug("detected '"+encoding+"' from contenttype header ct="+type);
             } 
         }
 	}
 
 	/**
 	 * @param filename
 	 *            filename you wish to parse
 	 * @return returns a Feed object representing the feed
 	 * @throws SAXException
 	 */
 	public FeedData parse(String filename) throws SAXException {
 		try {
 
             if (encoding == null) {
                 encoding = de.detect(filename, defaultEncoding);
             }
  
             
             //if(true) { throw new RuntimeException(); }
             FileInputStream is = new FileInputStream(filename);
             de.stripBOM(is);
             Charset charset = Charset.forName(de.alias(encoding));
             CharsetDecoder csd = charset.newDecoder();
             csd.onMalformedInput(CodingErrorAction.REPORT);
 
 
             
 			return parse(is, csd );
 		} catch (java.io.FileNotFoundException e) {
 			log.info("FileNotFoundException", e);
 		} catch (UnsupportedEncodingException e) {
             log.warn(de.alias(encoding), e);
         } catch (IOException e) {
             e.printStackTrace();
         }
 		return new FeedData();
 	}
 
 	public FeedData parse(byte[] data) {
         if (encoding == null) {
             encoding = de.detect(data, defaultEncoding);
         }
 		try {
             return parse(new ByteArrayInputStream(data), de.alias(encoding));
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         return new FeedData();
 	}
 
 	public FeedData parse(InputStream istream) throws IOException {
         if (!istream.markSupported()) {
             istream = new MarkableInputStream(istream);
         }
         if (encoding == null) {
             istream.mark(0);
             encoding = de.detect(istream);
             istream.reset();
         }
 		return parse(new InputStreamReader(istream));
 	}
     public FeedData parse(InputStream istream, String encoding) throws UnsupportedEncodingException {
         if (!istream.markSupported()) {
             istream = new MarkableInputStream(istream);
         }
         return parse(new InputStreamReader(istream, encoding));
     }
     public FeedData parse(InputStream istream, CharsetDecoder encoding) throws UnsupportedEncodingException {
         if (!istream.markSupported()) {
             istream = new MarkableInputStream(istream);
         }
         return parse(new InputStreamReader(istream, encoding));
     }
 	public FeedData parse(InputStreamReader in)  {
            
 		FeedData ret = new FeedData();
         
 		try {
 			SAXParser xr = new SAXParser();
 			FeedSAXParser handler = new FeedSAXParser();
 			xr.setContentHandler(handler);
 			xr.setErrorHandler(handler);
 			xr.setProperty("http://xml.org/sax/properties/lexical-handler",	handler);
 			xr.setFeature("http://apache.org/xml/features/continue-after-fatal-error",true);
 			xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",	false);
             xr.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
 			if (headers != null) {
 				if (headers.containsKey("Content-Location")) {
 					handler.setContentLocation((String) headers
 							.get("Content-Location"));
 				}
 				if (headers.containsKey("Content-Language")) {
 					handler.setContentLanguage((String) headers
 							.get("Content-Language"));
 				}
 			}
 			
 			xr.parse(new InputSource(in));
 			ret = handler.getFeed();
            //ret.set("encoding", encoding);
 
 		} catch (SAXException e) {
 			log.info("SAXException: failed to parse", e);
         } catch (MalformedInputException e) {
             log.info(e);
             ret.error = true;
         } catch (java.io.IOException e) {
             log.info("IOException", e);
 
 		} catch (NullPointerException e) {
 		    log.info("got NullPointerException:", e);
 		}
 
 		return ret;
 	}
 }

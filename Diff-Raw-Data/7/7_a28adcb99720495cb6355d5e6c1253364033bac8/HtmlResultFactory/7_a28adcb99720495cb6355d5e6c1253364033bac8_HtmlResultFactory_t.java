 /* Make ChildContainerResults for HTML.
  *
  * Copyright (C) 2010, 2011 Darrell Karbott
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either
  * version 2.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * Author: djk@isFiaD04zgAgnrEC5XJt1i4IE7AkNPqhBG5bONi6Yks
  *
  *  This file was developed as component of
  * "fniki" (a wiki implementation running over Freenet).
  */
 package fniki.wiki;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import wormarc.IOUtil;
 
 import static ys.wikiparser.Utils.*;
 // INTENT: Encapsulate the hacks we need to do handle the fact that the Freenet plugin
 //         only wants the html body, not the full document.
 public class HtmlResultFactory {
     private final static String ENCODING = "UTF-8";
 
     public static ChildContainerResult makeResult(String utf8Title,
                                                   String utf8Body,
                                                   boolean includeOuterHtml,
                                                   int refreshSeconds)
         throws ServerErrorException {
 
         try {
             return new StaticResult(ENCODING, "text/html", utf8Title, refreshSeconds,
                                     makeData(utf8Title, utf8Body, refreshSeconds, includeOuterHtml));
         } catch (IOException ioe) {
             throw new ServerErrorException("HtmlResultFactory.makeResult failed. Bad data?");
         }
     }
     public static ChildContainerResult makeResult(String utf8Title,
                                                   String utf8Body,
                                                   boolean includeOuterHtml)
         throws ServerErrorException {
         return makeResult(utf8Title, utf8Body, includeOuterHtml, 0);
     }
 
     public static byte[] makeData(String utf8Title,
                                   String utf8Body,
                                   int refreshSeconds,
                                   boolean includeOuterHtml)
         throws IOException {
 
         if (!includeOuterHtml) {
             return utf8Body.getBytes(ENCODING);
         }
 
         StringBuilder buffer = new StringBuilder();
         buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
                       "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
         buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        buffer.append("<head>\n");
        // The freenet content filter rips out the <?xml > line above.
        // We need to do this to get correct utf-8 charater handling
        // when running stand alone.
        buffer.append("<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n");
        buffer.append("<title>\n");
         buffer.append(escapeHTML(utf8Title));
         buffer.append("</title>\n");
 
         // CAREFUL: MUST audit .css files built into .jar to make sure they are safe.
         // Load .css snippet from jar. Names can only have 1 '/' and must be globally unique.
         String css = getStaticCss();
         buffer.append("<style>\n");
         buffer.append(css);
         buffer.append("</style>\n");
 
         if (refreshSeconds > 0) {
             buffer.append("<meta http-equiv=\"refresh\" content=\"");
             buffer.append(refreshSeconds);
             buffer.append("\" />\n");
         }
 
         buffer.append("</head>\n");
         buffer.append("<body>\n");
 
         // Road. Meet rubber. Rubber. Meet road.
         buffer.append(utf8Body);
         buffer.append("</body></html>\n");
 
         return buffer.toString().getBytes(ENCODING);
     }
 
     // MUST exist and not be empty.
     private static String getStaticCss() throws IOException {
         // Seems fast enough. If people complain we can cache the value.
         InputStream resourceStream =
             HtmlResultFactory.class.getResourceAsStream("/standalone_jfniki.css");
         if (resourceStream == null) {
             throw new IOException("Not in Jar!");
         }
 
         return IOUtil.readUtf8StringAndClose(resourceStream);
     }
 }
 

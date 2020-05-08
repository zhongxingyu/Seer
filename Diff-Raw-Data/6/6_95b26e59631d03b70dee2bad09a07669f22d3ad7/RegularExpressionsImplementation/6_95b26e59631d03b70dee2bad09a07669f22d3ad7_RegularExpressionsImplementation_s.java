 // Copyright (C) 2006 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.plugin.http.tcpproxyfilter;
 
 import java.util.regex.Pattern;
 
 
 /**
  * Compiled regular expressions.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class RegularExpressionsImplementation
   implements RegularExpressions {
 
   private final Pattern m_basicAuthorizationHeaderPattern;
   private final Pattern m_headerPattern;
   private final Pattern m_messageBodyPattern;
   private final Pattern m_requestLinePattern;
   private final Pattern m_responseLinePattern;
 
   private final Pattern m_lastPathElementPathPattern;
 
   /**
    * Constructor.
    */
   public RegularExpressionsImplementation() {
 
     // We're generally flexible about SP and CRLF, see RFC 2616, 19.3.
 
     // From RFC 2616:
     //
     // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
     // HTTP-Version = "HTTP" "/" 1*DIGIT "." 1*DIGIT
     // http_URL = "http:" "//" host [ ":" port ] [ abs_path [ "?" query ]]
     //
 
     m_requestLinePattern = Pattern.compile(
       "^([A-Z]+)[ \\t]+" +          // Method.
       "(?:https?://[^/]+)?"  +      // Ignore scheme, host, port.
       "(.+)" +                      // Path, query string, fragment.
       "[ \\t]+HTTP/\\d.\\d[ \\t]*\\r?\\n",
       Pattern.MULTILINE | Pattern.UNIX_LINES);
 
     // RFC 2616, 6.1:
     //
     // Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
 
     m_responseLinePattern = Pattern.compile(
       "^HTTP/\\d.\\d[ \\t]+" +
       "(\\d+)" +                   // Status-Code
       "[ \\t]+" +
       "(.*)" +                     // Reason-Phrase
       "[ \\t]*\\r?\\n");
 
     m_messageBodyPattern = Pattern.compile("\\r\\n\\r\\n(.*)", Pattern.DOTALL);
 
     m_headerPattern = Pattern.compile(
       "^([^:\\r\\n]*)[ \\t]*:[ \\t]*(.*?)\\r?\\n",
       Pattern.MULTILINE | Pattern.UNIX_LINES);
 
     m_basicAuthorizationHeaderPattern = Pattern.compile(
       "^Authorization[ \\t]*:[ \\t]*Basic[  \\t]*([a-zA-Z0-9+/]*=*).*?\\r?\\n",
       Pattern.MULTILINE | Pattern.UNIX_LINES);
 
    // Ignore maximum amount of stuff that's not a '?', ';', or '#' followed by
    // a '/', then grab the next until the first '?', ';', or '#'.
    m_lastPathElementPathPattern = Pattern.compile("^[^\\?;#]*/([^\\?;#]*)");
   }
 
   /**
    * A pattern that matches the first line of an HTTP request.
    *
    * @return The pattern.
    */
   public Pattern getRequestLinePattern() {
     return m_requestLinePattern;
   }
 
   /**
    * A pattern that matches the first line of an HTTP response.
    *
    * @return The pattern.
    */
   public Pattern getResponseLinePattern() {
     return m_responseLinePattern;
   }
 
   /**
    * A pattern that matches an HTTP message body.
    *
    * @return The pattern.
    */
   public Pattern getMessageBodyPattern() {
     return m_messageBodyPattern;
   }
 
   /**
    * A pattern that matches an HTTP header.
    *
    * @return The pattern
    */
   public Pattern getHeaderPattern() {
     return m_headerPattern;
   }
 
   /**
    * A pattern that matches an HTTP Basic Authorization header.
    *
    * @return The pattern
    */
   public Pattern getBasicAuthorizationHeaderPattern() {
     return m_basicAuthorizationHeaderPattern;
   }
 
   /**
    * A pattern that matches the last element in a path.
    *
    * @return The pattern
    */
   public Pattern getLastPathElementPathPattern() {
     return m_lastPathElementPathPattern;
   }
 }

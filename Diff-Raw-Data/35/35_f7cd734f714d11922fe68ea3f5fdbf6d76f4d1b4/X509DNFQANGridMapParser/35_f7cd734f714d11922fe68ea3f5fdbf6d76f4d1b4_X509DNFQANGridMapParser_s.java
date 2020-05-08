 /*
  * Copyright 2008 EGEE Collaboration
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.common.obligation.provider.gridmap;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UnknownFormatConversionException;
 import java.util.Vector;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.glite.authz.common.obligation.provider.gridmap.GridMap.Entry;
 import org.glite.authz.common.obligation.provider.gridmap.GridMap.GridMapKeyMatchFunction;
 import org.glite.authz.common.util.Strings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** A parser of things nominally called gridmaps. */
 public class X509DNFQANGridMapParser implements GridmapParser {
 
     /** Class logger. */
     private final Logger LOG = LoggerFactory.getLogger(X509DNFQANGridMapParser.class);
 
     /** Constructor. */
     public X509DNFQANGridMapParser() {
     }
 
     /** {@inheritDoc} */
     public Map<Class<? extends GridMapKey>, GridMapKeyMatchFunction> getKeyMatchFunctions() {
         HashMap<Class<? extends GridMapKey>, GridMapKeyMatchFunction> functions = new HashMap<Class<? extends GridMapKey>, GridMapKeyMatchFunction>();
         functions.put(X509DistinguishedName.class, new X509DistinguishedName.MatchFunction());
         functions.put(FQAN.class, new FQAN.MatchFunction());
         return functions;
     }
 
     /** {@inheritDoc} */
     public Vector<Entry> parse(Reader gridMapReader) throws IOException {
         BufferedReader bufferedReader = new BufferedReader(gridMapReader);
         Vector<Entry> gridMap = new Vector<Entry>();
 
         int lineNumber = 0;
         Entry mapEntry;
         String line = Strings.safeTrimOrNullString(bufferedReader.readLine());
         do {
             lineNumber++;
             mapEntry = parseLine(line, lineNumber);
             if (mapEntry != null) {
                 gridMap.add(mapEntry);
             }
            line = Strings.safeTrim(bufferedReader.readLine());
         } while (line != null);
 
         return gridMap;
     }
 
     /**
      * Parses a single line in a grid map file.
      * 
      * @param gridMap the grid map to populate with the entry, if any, contained in this line
      * @param line the line to parse
      * @param lineNumber the current line number
      */
     private Entry parseLine(String line, int lineNumber) {
         if (line == null || line.startsWith("#")) {
             LOG.trace("Line number {} is a comment, no processing performed", lineNumber);
             return null;
         }
 
         int lastDQuote = line.lastIndexOf("\"");
         String name = Strings.safeTrimOrNullString(line.substring(1, lastDQuote));
         List<String> ids = Strings.toList(line.substring(++lastDQuote), ",");
 
         GridMapKey key;
         if (isFQAN(name)) {
             key = FQAN.parseFQAN(name);
         } else {
             if (name.startsWith("/")) {
                 key = parseGridDN(name);
             } else {
                 key = new X509DistinguishedName(new X500Principal(unescapeString(name)));
             }
         }
         LOG.debug("Line number {} maps {} to {}", new Object[] { lineNumber, name, ids });
         return new BasicGridMapEntry(key, ids);
     }
 
     /**
      * Checks if the given name is a FQAN.
      * 
      * @param name the name to check
      * 
      * @return true
      */
     private boolean isFQAN(String name) {
         // DNs must contain a = in their first component
         // a FQAN must not
 
         String[] components = name.split("/");
         // both DNs and FQANs must begin with a / so there is an empty
         // component preceding the first real component
         if (components.length > 1 && !components[1].contains("=")) {
             return true;
         }
 
         return false;
     }
 
     private X509DistinguishedName parseGridDN(String dnString) {
         String[] components = dnString.split("/");
         
         StringBuilder rfc2253DN = new StringBuilder();
         for(int i = components.length - 1; i > 0; i--){
             rfc2253DN.append(components[i]);
             if(i > 1){
                 rfc2253DN.append(",");
             }
         }
         
         return new X509DistinguishedName(new X500Principal(unescapeString(rfc2253DN.toString())));
     }
 
     /**
      * Unescapes a string. The standard Java escape sequences (b, f, n, r, t, u, \, ', ") are supported as well as \xXX
      * for hexadecimal character representation.
      * 
      * @param string the string to unescape
      * 
      * @return the unescaped string
      * 
      * @throws UnknownFormatConversionException thrown if an unsupported escape sequence is found
      */
     private String unescapeString(String string) throws UnknownFormatConversionException {
         char[] stringChars = string.toCharArray();
         StringBuilder unescapedString = new StringBuilder();
         char[] hexChars;
 
         for (int i = 0; i < stringChars.length; i++) {
             if (stringChars[i] != '\\') {
                 unescapedString.append(stringChars[i]);
                 continue;
             }
 
             switch (stringChars[i + 1]) {
                 case 'b':
                     unescapedString.append('\b');
                     i++;
                     break;
                 case 'f':
                     unescapedString.append('\f');
                     i++;
                     break;
                 case 'n':
                     unescapedString.append('\n');
                     i++;
                     break;
                 case 'r':
                     unescapedString.append('\r');
                     i++;
                     break;
                 case 't':
                     unescapedString.append('\t');
                     i++;
                     break;
                 case '\'':
                     unescapedString.append('\'');
                     i++;
                     break;
                 case '"':
                     unescapedString.append('"');
                     i++;
                     break;
                 case '\\':
                     unescapedString.append('\\');
                     i++;
                     break;
                 case 'x':
                     hexChars = new char[2];
                     hexChars[0] = stringChars[i + 2];
                     hexChars[1] = stringChars[i + 3];
                     unescapedString.append((char) Integer.parseInt(new String(hexChars), 16));
                     i += 3;
                     break;
                 case 'u':
                     hexChars = new char[4];
                     hexChars[0] = stringChars[i + 2];
                     hexChars[1] = stringChars[i + 3];
                     hexChars[2] = stringChars[i + 4];
                     hexChars[3] = stringChars[i + 5];
                     unescapedString.append((char) Integer.parseInt(new String(hexChars), 16));
                     i += 5;
                     break;
                 default:
                     throw new UnknownFormatConversionException("Escape sequence '\\" + stringChars[i + 1]
                             + " in string '" + string + "' is not supported");
             }
         }
 
         return unescapedString.toString().trim();
     }
 }

 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  */
 
 package org.mulgara.itql;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Map;
 
 /**
  * A set of methods for managing common URI operations.
  *
  * @created 2007-08-09
  * @author Paul Gearon
  * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 public class URIUtil {
 
   /**
    * Replace an alias in a URI, if one is recognized.
    * @param uriString A string with the initial uri to check for aliases.
    * @param aliasMap The map of known aliases to the associated URIs
    * @return A new URI with the alias replaced, or the original if no alias is found.
    */
   public static URI convertToURI(String uriString, Map<String,URI> aliasMap) {
     try {
       URI uri = new URI(uriString);
       if (uri.isOpaque()) {
         // Attempt qname-to-URI substitution for aliased namespace prefixes
         URI mapping = aliasMap.get(uri.getScheme());
         if (mapping != null) {
           uri = new URI(mapping + uri.getSchemeSpecificPart());
         }
       }
       return uri;
     } catch (URISyntaxException e) {
      throw new RuntimeException("Bad URI syntax in resource", e);
     }
   }
   
 }

 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.esciencecenter.vbrowser.vrs.vrl;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import nl.esciencecenter.ptk.net.URIFactory;
 import nl.esciencecenter.ptk.object.Duplicatable;
 import nl.esciencecenter.ptk.util.StringUtil;
 import nl.esciencecenter.ptk.util.logging.ClassLogger;
 import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
 
 /**
  * Virtual Resource Locator Class. 
  * URI compatible Class. 
  * See URIFactory.  
  */
 public final class VRL implements Cloneable,Comparable<VRL>, Duplicatable<VRL>, Serializable
 {
     private static final long serialVersionUID = -3255450059796404575L;
 
     private static ClassLogger logger; 
     
     static
     {
         logger=ClassLogger.getLogger(VRL.class);
     }
     
     public static VRL createVRL(URIFactory factory, boolean duplicateFactory)
     {
         if (duplicateFactory)
             factory=factory.duplicate();
         return new VRL(factory.duplicate());
     }
     
     public static VRL createDosVRL(String vrlstr) throws VRLSyntaxException
     {
         String newStr=vrlstr.replace('\\','/'); 
         // constructor might change ! 
         return new VRL(newStr); 
     }
     
     // ========
     // Instance 
     // ========
     
     protected URIFactory uriFactory;
     
     protected VRL()
     {
     }
     
     protected VRL(URIFactory factory)
     {
         uriFactory=factory;
     }
     
     public VRL(String uristr) throws VRLSyntaxException
     {
         init(uristr); 
     }
 
     public VRL(URL url) throws VRLSyntaxException
     {
         try
         {
             init(url.toURI());
         }
         catch (URISyntaxException e)
         {
             throw new VRLSyntaxException(e);
         }
     }
  
     public VRL(URI uri)
     {
        init(uri);
     }
 
     public VRL(VRL other)
     {
         this.uriFactory=other.uriFactory.duplicate(); 
     }
     
     public VRL duplicate()
     {
         return createVRL(uriFactory,true); 
     }
     
     public VRL(String scheme, String host, String path)
     {
         init(scheme,null,host,-1,path, null,null); 
     }
 
     public VRL(String scheme, String host,int port, String path)
     {
         init(scheme,null,host,port,path, null,null); 
     }
 
     public VRL(String scheme,String userInfo, String host,int port, String path)
     {
         init(scheme,userInfo,host,port,path, null,null); 
     }
 
     public VRL(String scheme,
             String userInfo, 
             String hostname, 
             int port,
             String path,
             String query,
             String fragment)
     {
         init(scheme,userInfo,hostname,port,path,query,fragment); 
     }
 
     private void init(String uriStr) throws VRLSyntaxException
     {
         try
         {
             this.uriFactory=new URIFactory(uriStr);
         }
         catch (URISyntaxException e)
         {
             throw new VRLSyntaxException("Cannot create URIFactory from:uristr",e);
         } 
     }
     
     private void init(URI uri)
     {
         this.uriFactory=new URIFactory(uri);
     }
 
     private void init(String scheme, 
                 String userInfo, 
                 String host, 
                 int port, 
                 String path, 
                 String query, 
                 String fragment)
     {
         this.uriFactory=new URIFactory(scheme,userInfo,host,port,path,query,fragment);
     }
     
     @Override
     public boolean shallowSupported()
     {
         return false;
     }
 
     @Override
     public VRL duplicate(boolean shallow)
     {
         return duplicate();
     }
 
     /**
      * Compares this VRL with other VRL based on normalized String representations. 
      */
     @Override
     public int compareTo(VRL other)
    {   
         return StringUtil.compare(this.toNormalizedString(), other.toNormalizedString(),false);
     }
     
     public int compareToObject(Object other)
     {   
         if ((other instanceof VRL)==false)
             return -1; 
             
         return compareTo((VRL)other);
     }
     
     /**
      * Returns hash code of normalized URI String. 
      */
     @Override
     public int hashCode()
     {
         return toNormalizedString().hashCode(); 
     }
     
     @Override
     public boolean equals(Object other)
     {
         if (other==null)
             return false; 
         
         if ((other instanceof VRL)==false)
             return false;
         
         return (compareTo((VRL)other)==0);
     }
     
     // ========================================================================
     // String/URI Formatters
     // ========================================================================
  
     public String toString()
     {
         return uriFactory.toString(); 
     }
     
     public java.net.URI toURI() throws URISyntaxException
     {
         return uriFactory.toURI(); 
     }
     
     public String toNormalizedString()
     {
         return uriFactory.toNormalizedString(); 
     }
     
     /** Calls toURI().toURL() */ 
     public java.net.URL toURL() throws MalformedURLException
     {
         try
         {
             return uriFactory.toURI().toURL();
         }
         catch (URISyntaxException e)
         {
           throw new MalformedURLException("Bad URI:"+uriFactory.toNormalizedString());
         } 
     }
     
     // ========================================================================
     // Getters
     // ========================================================================
     
     public String getScheme()
     {
         return uriFactory.getScheme();
     }
     
     /** 
      * Returns username part from userinfo if it as one 
      */
     public String getUsername()
     {
         String info = uriFactory.getUserInfo();
 
         if (info == null)
             return null;
 
         // strip password:
         String parts[] = info.split(":");
 
         if ((parts == null) || (parts.length == 0))
             return info;
 
         if (parts[0].length() == 0)
             return null;
 
         return parts[0];
     }
 
     /**
      * Returns password part (if specified !) from userInfo string.
      * @deprecated It is NOT safe to use clear text password in any URI!
      */
     public String getPassword()
     {
         String info = uriFactory.getUserInfo();
 
         if (info == null)
             return null;
 
         String parts[] = info.split(":");
 
         if ((parts == null) || (parts.length < 2))
             return null;
 
         return parts[1];
     }
     
     public String getBasename()
     {
         return uriFactory.getBasename();
     }
     
     public String getHostname()
     {
         return uriFactory.getHostname();
     }    
     
     public int getPort()
     {
         return uriFactory.getPort();
     }
 
     public String getPath()
     {
         return uriFactory.getPath();
     }
 
     public String getQuery()
     {
         return uriFactory.getQuery();
     }
     
     public String getFragment()
     {
         return uriFactory.getFragment();
     }
     
     public boolean hasHostname(String otherHostname)
     {
         return StringUtil.equals(this.uriFactory.getHostname(), otherHostname); 
     }
     
     public VRL getParent()
     {
         return createVRL(uriFactory.getParent(),true);
     }
     
     public boolean isVLink()
     {
         return hasExtension("vlink",false); 
     }
 
     public boolean isRootPath()
     {
         // normalize path: 
         String upath=URIFactory.uripath(uriFactory.getPath());
         //Debug("isRootPath(): uri path="+upath); 
         
         if (StringUtil.isEmpty(upath))
             return true; 
         
         // "/"
         if (upath.compareTo(URIFactory.SEP_CHAR_STR)==0) 
             return true; 
         
         // uripath normalized windosh root "/X:/" 
         if (upath.length()==4)
             if ((upath.charAt(0)==URIFactory.SEP_CHAR) && (upath.substring(2,4).compareTo(":/")==0))
                 return true; 
         
         return false; 
     }
 
     public String getUserinfo()
     {
        return this.uriFactory.getUserInfo(); 
     }
     
     // ========================================================================
     // Resolvers 
     // ========================================================================
     
     public VRL uriResolve(String relUri) throws VRLSyntaxException
     {
         try
         {
             return createVRL(uriFactory.duplicate().uriResolve(relUri),false);
         }
         catch (URISyntaxException e)
         {
             throw new VRLSyntaxException("Failed to resolve lreative String:"+relUri,e);
         } 
     }
    
     public VRL resolvePath(String path) throws VRLSyntaxException
     {
         try
         {
             String newPath=uriFactory.resolvePath(path); 
             return createVRL(uriFactory.duplicate().setPath(newPath),false);
         }
         catch (URISyntaxException e)
         {
             throw new VRLSyntaxException("Failed to resolve path:"+path,e);
         } 
     }
     
     /**
      * Append path to this VRL and return new VRL 
      */ 
     public VRL appendPath(String path)
     {
         // Use URI factory here. 
         return createVRL(uriFactory.duplicate().appendPath(path),false);
     }
 
     public VRL replacePath(String path)
     {
         // note: a reference path must be not made absolute when the uri has Authority!
         return new VRL(getScheme(),
                 getUserinfo(),
                 getHostname(),
                 getPort(), 
                 URIFactory.uripath(path,hasAuthority()),
                 getQuery(),
                 getFragment());
     }
 
     /** 
      * Check whether URI (and path) is a parent location of <code>subLocation</code>.  
      * @param subLocation child path of this VRL. 
      * @return true if the subLocation is a child location of this VRL. 
      */
     public boolean isParentOf(VRL subLocation)
     {
         String pathStr=toString(); 
         String subPath=subLocation.toString(); 
         
         // Current implementation is based on simple string comparison.
         // For this to work, both VRL strings must be normalized ! 
         
         if (subPath.startsWith(pathStr)==true)
         {
             // To prevent that paths like '<..>/dir123' appear to be subdirs of '<..>/dir' 
             // last part of subpath after '<..>/dir' must be '/' 
             // Debug("subPath.charAt="+subPath.charAt(pathStr.length()));
             
             if ((subPath.length()>pathStr.length()) && (subPath.charAt(pathStr.length())==URIFactory.SEP_CHAR)) 
                 return true; 
         }
         
         return false; 
     }
 
     public VRL resolvePath(VRL relvrl) throws VRLSyntaxException
     {
         // Ambiguous: 
         if (relvrl.uriFactory.isAbsolute())
             return relvrl;
          
         return resolvePath(relvrl.getPath()); 
     }
    
     // =============================
     // Extra VRL interface methods. 
     // =============================
 
     public String getExtension()
     {
         return uriFactory.getExtension(); 
     }
 
     public String getDirPath()
     {
         return this.getPath() + "/";
     }
 
     public boolean isRelative()
     {
         return uriFactory.isRelative();
     }
     
     public boolean isAbsolute()
     {
         return this.uriFactory.isAbsolute(); 
     }
 
     public String getBasename(boolean withExtension)
     {
        return this.uriFactory.getBasename(withExtension);
     }
 
     public String getDirname()
     {
         return uriFactory.getDirname(); 
     }
     
     /**
      * Returns granparent dirname. Calls dirname on dirname result
      */
     public String getDirdirname()
     {
         return URIFactory.dirname(URIFactory.dirname(getPath()));
     }
     
     public String[] getPathElements()
     {
        return uriFactory.getPathElements(); 
     }
 
     public boolean hasExtension(String ext, boolean matchCase)
     {
         String uriext = this.getExtension();
         if (uriext == null)
             return false;
 
         if (matchCase == false)
             return uriext.equalsIgnoreCase(ext);
 
         return uriext.equals(ext);
     }
 
     public boolean hasScheme(String otherScheme)
     {
         if (otherScheme==null)
             return false;
         
         return otherScheme.equals(getScheme()); 
     }
 
     /** 
      * Create URI, ignore exceptions. 
      * Use this method if it is sure the URI is valid. 
      * Exceptions are nested into Errors. 
      * 
      * @return URI representation of this VRL.
      */
     public URI toURINoException()
     {
         try
         {
             return this.toURI(); 
         }
         catch (Exception e)
         {
             throw new Error(e.getMessage(),e); 
         }
     }
 
     public String[] getQueryParts()
     {
         if (getQuery() == null)
             return null;
 
         return getQuery().split(URIFactory.ATTRIBUTE_SEPERATOR);
     }
     
     /** 
      * Returns true if this VRL has a non empty fragment part ('?...') in it. 
      */
     public boolean hasQuery()
     {
         return (StringUtil.isEmpty(getQuery()) == false);
     }
 
     /** 
      * Returns true if this VRL has a non empty fragment part ('#...') in it. 
      */
     public boolean hasFragment()
     {
         return (StringUtil.isEmpty(getFragment()) == false);
     }
     
     public boolean hasAuthority()
     {
         return uriFactory.hasAuthority(); 
     }
     
 }

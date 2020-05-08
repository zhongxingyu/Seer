 package org.xbrlapi.cache;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Translates 
  * 1. original URIs into cache File objects or cache URI objects
  * 2. cache URIs into original URI objects
  * The translation from cache URIs to original URIs is a hack that
  * enables relative URIs in cached files to be identified as such an
  * resolved to obtain the original URI of the resource identified
  * by the relative URI.
  * This class also provides a method for testing if a URI is a cache URI.
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
  */
 public class CacheImpl implements Cache {
 
     /**
      * The serial version UID.
      * @see 
      * http://java.sun.com/javase/6/docs/platform/serialization/spec/version.html#6678
      * for information about what changes will require the serial version UID to be
      * modified.
      */
     private static final long serialVersionUID = -4518163581910550322L;
 
     private static final Logger logger = Logger.getLogger(CacheImpl.class);
 	
     /**
      * Root of the local document cache.
      */
     private File cacheRoot;
     
     /**
      * The map of local URIs to use in place of
      * original URIs.  The original URI points to the 
      * local URI in the map that is used.
      */
     private HashMap<URI,URI> uriMap = new HashMap<URI, URI>();      
 
     /**
      * Constructs a URI translator for usage with a local cache location.
      * @param cacheRoot The root directory for the cache.
      * @throws XBRLException if the cacheRoot is null or does not exist or cannot be
      * written to or read from.
      */
 	public CacheImpl(File cacheRoot) throws XBRLException {
 		if (cacheRoot == null) throw new XBRLException("The cache root is null.");
 		if (! cacheRoot.exists()) throw new XBRLException("The cache " + cacheRoot + " does not exist.");
 		if (! cacheRoot.canWrite()) throw new XBRLException("The cache " + cacheRoot + " cannot be written to.");
         if (! cacheRoot.canRead()) throw new XBRLException("The cache " + cacheRoot + " cannot be read.");
 		this.cacheRoot = cacheRoot;
 	}
     
     /**
      * Constructs a URI translator for usage with a local cache location.
      * @param cacheRoot
 	 * @param uriMap The hash map from original URIs to local URIs.
      * @throws XBRLException if the cacheRoot is null or does not exist
      */
 	public CacheImpl(File cacheRoot, HashMap<URI, URI> uriMap) throws XBRLException {
 		this(cacheRoot);
 		if (uriMap == null) throw new XBRLException("The URI map must not be null.");
 		this.uriMap = uriMap;
 	}	
 	
     /**
      * @see org.xbrlapi.cache.Cache#isCacheURI(java.net.URI)
      */
     public boolean isCacheURI(URI uri) throws XBRLException {
     	logger.debug("Checking if " + uri + " is in the cache.");
     	
     	if (! uri.getScheme().equals("file")) {
         	logger.debug("Protocol is wrong so not in cache.");
     		return false;
     	}
 
     	try {
     	    logger.debug("The canonical path to the cache root is: " + cacheRoot.getCanonicalPath());
             logger.debug("The path component of the URI being tested is: " + uri.getPath());
 
             String uriPath = "";
             try {
                 uriPath = new File(uri.getPath()).getCanonicalPath();
                 logger.debug("Canonicalised URI path is: " + uriPath);
             } catch (Exception couldNotCanonicaliseURIPath) {
                 logger.debug("Could not canonicalise URI Path " + uri.getPath() + " so we do not have a cache URI.");
                 return false;
             }
 
             if (uriPath.startsWith(cacheRoot.getCanonicalPath().toString())) {
                 logger.debug("Path is right so is in cache.");
                 return true;
             }
 
     	} catch (Exception e) {
     		throw new XBRLException("The canonical cache root path cannot be determined.",e);
     	}
     	logger.debug("Path is wrong so not in cache.");    	
     	return false;
     }
     
     /**
      * @see org.xbrlapi.cache.Cache#getCacheURI(java.net.URI)
      */
     public URI getCacheURI(URI uri) throws XBRLException {
 
     	logger.debug("About to get the cache URI for " + uri);
 	
     	// First determine the original URI
     	URI originalURI = uri;
     	if (isCacheURI(uri)) {
     		originalURI = getOriginalURI(uri);
 		} else {
         	if (uriMap.containsKey(uri.toString())) {
                 originalURI = uriMap.get(uri);
         	}
 		}
     	
     	// Second determine the cache file from the original URI
     	// so that we can try to cache it if that is necessary.
     	try {
         	File cacheFile = getCacheFile(originalURI);
     		if (! cacheFile.exists()) {
     			copyToCache(originalURI,cacheFile);
     		}
             return cacheFile.toURI();
     	} catch (XBRLException e) {
             logger.debug(e.getMessage());
             return originalURI;
     	}
         
     }
     
     /**
      * @see org.xbrlapi.cache.Cache#getOriginalURI(java.net.URI)
      */
     public URI getOriginalURI(URI uri) throws XBRLException {
     	
     	logger.debug("Getting original URI for " + uri);
     	
     	// Just return the URI if it is not a cache URI
     	if (! isCacheURI(uri)) {
     		logger.debug("Returning the URI as it is already original.");
     		return uri;
     	}
 
 		String data = uri.getPath();
 		
 		try {
 		    data = (new File(data)).getCanonicalPath();
 		} catch (IOException e) {
 		    throw new XBRLException("Canonical path could not be obtained from the URI.",e);
 		}
 
 		// Eliminate the cacheRoot part of the path
 		try {
 			data = data.replace(cacheRoot.getCanonicalPath().toString().substring(1),"").substring(2);
 		} catch (IOException e) {
 			throw new XBRLException("The original URI could not be determined for " + uri);
 		}
 		
 		List<String> parts = new Vector<String>();
 		logger.debug(data);
         StringTokenizer tokenizer = new StringTokenizer(data, File.separator);
         while (tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
             if (token != null)
                 if (! token.equals("")) {
                     logger.debug(token);
                     parts.add(token);
                 }
         }
         
         String scheme = parts.get(0);
         if (scheme.equals("null")) scheme = null;
         String user = parts.get(1);
         if (user.equals("null")) user = null;
         String host = parts.get(2);
         if (host.equals("null")) host = null;
         int port = new Integer(parts.get(3)).intValue();
         String query = parts.get(4);
         if (query.equals("null")) query = null;
         String fragment = parts.get(5);
         if (fragment.equals("null")) fragment = null;
 
         String path = "";
         for (int i=6; i<parts.size(); i++) {
             if (i == 6)
                 if (File.separator.matches("\\\\"))
                     if (parts.get(i).matches("\\w_drive"))
                         parts.set(i,parts.get(i).substring(0,0) + ":");          
             path += "/" + parts.get(i);
         }
 
 		try {
 			URI originalURI = new URI(scheme, user,host, port, path,query,fragment);
 	    	logger.debug("Got the original URI " + originalURI);
 			return originalURI;
 		} catch (URISyntaxException e) {
 			throw new XBRLException("Malformed original URI.",e);
 		}
 
     }
     
     /**
      * @param file The file to test.
      * @return true if the file is in the cache and false otherwise.
      */
     private boolean isCacheFile(File file) {
         File parent = file;
         while (parent != null) {
             if (parent.equals(this.cacheRoot)) {
                 return true;
             }
             parent = parent.getParentFile();
         }
         return false;
     }
     
     /**
      * @see org.xbrlapi.cache.Cache#getOriginalURI(File)
      */
     public URI getOriginalURI(File file) throws XBRLException {
         
         logger.debug("Getting original URI for " + file);
         
         // Just return the URI version of the file if it is not a cache file
         if (! isCacheFile(file)) {
             return file.toURI();
         }
 
         String data = "";
         try {
             data = file.getCanonicalPath();
         } catch (IOException e) {
             throw new XBRLException("Canonical path could not be obtained from the URI.",e);
         }
 
         // Eliminate the cacheRoot part of the path
         try {
             data = data.replace(cacheRoot.getCanonicalPath().toString().substring(1),"").substring(2);
         } catch (IOException e) {
             throw new XBRLException("The original URI could not be determined for " + file);
         }
         
         List<String> parts = new Vector<String>();
         logger.debug(data);
         StringTokenizer tokenizer = new StringTokenizer(data, File.separator);
         while (tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
             if (token != null)
                 if (! token.equals("")) {
                     logger.debug(token);
                     parts.add(token);
                 }
         }
         
         String scheme = parts.get(0);
         if (scheme.equals("null")) scheme = null;
         String user = parts.get(1);
         if (user.equals("null")) user = null;
         String host = parts.get(2);
         if (host.equals("null")) host = null;
         int port = new Integer(parts.get(3)).intValue();
         String query = parts.get(4);
         if (query.equals("null")) query = null;
         String fragment = parts.get(5);
         if (fragment.equals("null")) fragment = null;
 
         String path = "";
         for (int i=6; i<parts.size(); i++) {
             if (i == 6)
                 if (File.separator.matches("\\\\"))
                     if (parts.get(i).matches("\\w_drive"))
                        parts.set(i,parts.get(i).substring(0,1) + ":");          
             path += "/" + parts.get(i);
         }
 
         try {
             URI originalURI = new URI(scheme, user,host, port, path,query,fragment);
             logger.debug("Got the original URI " + originalURI);
             return originalURI;
         } catch (URISyntaxException e) {
             throw new XBRLException("Malformed original URI.",e);
         }
 
     }    
     
     /**
      * @see org.xbrlapi.cache.Cache#getCacheFile(java.net.URI)
      */
     public File getCacheFile(URI uri) throws XBRLException {
         
     	logger.debug("Getting the cache file for " + uri);
 
         String scheme = uri.getScheme();
         String user = uri.getUserInfo();
         String host = uri.getHost();
         String port = (new Integer(uri.getPort())).toString();
         String path = uri.getPath();
         String query = uri.getQuery();
         String fragment = uri.getFragment();
         
         String s = File.separator;
         String relativeLocation = scheme;
         relativeLocation = relativeLocation.concat(s+user);
         relativeLocation = relativeLocation.concat(s+host);
         relativeLocation = relativeLocation.concat(s+port);
         relativeLocation = relativeLocation.concat(s+query);
         relativeLocation = relativeLocation.concat(s+fragment);
         StringTokenizer tokenizer = new StringTokenizer(path, "/");
         while (tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
            if (File.separator.equals("\\")) // If on windows a : is not allowed in a directory name so use _drive instead
                 if (token.matches("\\w\\Q:\\E"))
                     token = token.substring(0,1) + "_drive";
             if (token != null)
                 if (! token.equals(""))
                     relativeLocation = relativeLocation.concat(s+token);
         }
 
         try {
             File cacheFile = new File(this.cacheRoot,relativeLocation);
             logger.debug("Got cacheFile" + cacheFile);
             return cacheFile;
         } catch (Exception e) {
             throw new XBRLException(uri + " cannot be translated into a location in the cache");
         }
     	
     }
     
     /**
      * @see org.xbrlapi.cache.Cache#copyToCache(java.net.URI, java.io.File)
      */
     public void copyToCache(URI originalURI, File cacheFile) {
     	
     	// If necessary, create the directory to contain the cached resource
 		File parent = cacheFile.getParentFile();
 		if (parent != null) parent.mkdirs();
 		
 		try {
 
 			// Establish the connection to the original CacheURIImpl data source
 		    InputStream inputStream = null;
 		    
 		    if (originalURI.getScheme().equals("file")) {
 	            String path = originalURI.getPath();
                 File f = new File(path);
 	            inputStream = new FileInputStream(f);
 		    } else {
                 inputStream =  originalURI.toURL().openConnection().getInputStream();
 		    }
 		    
 		    BufferedInputStream bis = new BufferedInputStream(inputStream);
 		    
 		    // Establish the connection to the destination file
 		    FileOutputStream fos = new FileOutputStream(cacheFile);
 		    BufferedOutputStream bos = new BufferedOutputStream(fos);
 	
 		    // Write the source file to the destination file
 		    int bite = bis.read();
 		    while (bite != -1) {
 				bos.write(bite);
 				bite = bis.read();
 		    }
 	
 		    // Clean up the reader and writer
 		    bos.flush();
 		    bis.close();
 		    bos.close();
 
 		} catch (java.net.NoRouteToHostException e) {
 		    logger.debug(e.getMessage());
 		} catch (FileNotFoundException e) {
             logger.debug(e.getMessage());
 		} catch (IOException e) {
             logger.debug(e.getMessage());
 		}
     }
     
     /**
      * @see org.xbrlapi.cache.Cache#copyToCache(java.net.URI, java.lang.String)
      */
     public void copyToCache(URI originalURI, String xml) throws XBRLException {
     	
     	logger.debug("Attempting to cache a string XML document using : " + originalURI);
 
     	File cacheFile = this.getCacheFile(originalURI);
     	
     	logger.debug("The cache file is : " + cacheFile.toString());
     	
     	// If necessary, create the directory to contain the cached resource
 		File parent = cacheFile.getParentFile();
 		if (parent != null) parent.mkdirs();
 
 		try {
 	        FileWriter out = new FileWriter(cacheFile);
 
 	        out.write(xml);
 	        out.close();		
 		} catch (IOException e) {
 			 throw new XBRLException("The String resource could not be cached.",e);
 		}
     }    
     
     /**
      * @see org.xbrlapi.cache.Cache#purge(java.net.URI)
      */
     public void purge(URI uri) throws XBRLException {
 		File file = this.getCacheFile(uri);
 		file.delete();
         logger.debug("Purged " + file);
     }
     
     /**
      * @return the root directory containing the cache.
      */
     public File getCacheRoot() {
         return this.cacheRoot;
     }
  
     /**
      * @see Cache#getAllUris(URI)
      */
     public List<URI> getAllUris(URI uri) throws XBRLException {
         
         // Get the relevant directory in the cache.
         File file = this.getCacheFile(uri);
         while (!file.isDirectory() && file.getParentFile() != null) {
             file = file.getParentFile();
         }
 
         List<URI> result = new Vector<URI>();
         FileFilter fileFilter = new FileFilter() {
             public boolean accept(File file) {
                 return (!file.isDirectory());
             }
         };
         for (File childFile: file.listFiles(fileFilter)) {
             result.add(this.getOriginalURI(childFile));
         }
 
         FileFilter directoryFilter = new FileFilter() {
             public boolean accept(File file) {
                 return (file.isDirectory());
             }
         };
         for (File childDirectory: file.listFiles(directoryFilter)) {
             result.addAll(getAllUris(this.getOriginalURI(childDirectory)));
         }
         return result;
     }
 
     /**
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
                 + ((cacheRoot == null) ? 0 : cacheRoot.hashCode());
         result = prime * result + ((uriMap == null) ? 0 : ((Map<URI,URI>) uriMap).hashCode());
         return result;
     }
 
     /**
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         CacheImpl other = (CacheImpl) obj;
         if (cacheRoot == null) {
             if (other.cacheRoot != null)
                 return false;
         } else if (!cacheRoot.equals(other.cacheRoot))
             return false;
         if (uriMap == null) {
             if (other.uriMap != null)
                 return false;
         } else if (!((Map<URI,URI>) uriMap).equals((other.uriMap)))
             return false;
         return true;
     }
 
 }

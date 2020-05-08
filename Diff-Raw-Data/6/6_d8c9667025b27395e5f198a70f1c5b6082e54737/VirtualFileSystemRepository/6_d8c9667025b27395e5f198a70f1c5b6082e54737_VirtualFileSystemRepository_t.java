 package org.wyona.yarep.impl.repo.vfs;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.IndexWriter;
 
 import org.wyona.commons.io.FileUtil;
 import org.wyona.yarep.core.Map;
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Revision;
 import org.wyona.yarep.core.Storage;
 import org.wyona.yarep.core.UID;
 import org.wyona.yarep.core.search.Indexer;
 import org.wyona.yarep.core.search.SearchException;
 import org.wyona.yarep.core.search.Searcher;
 
 /**
  * Node based file system repository.
  * A node of type resource is stored as a file.
  * A node of type collection is stored as a directory.
  * Each resource has a myresource.yarep directory which contains:
  * <ul>
  * <li>A meta file containing the properties</li>
  * <li>A revisions directory containing the revisions</li>
  * </ul>
  * This directory and the meta file will be created automatically when a node is
  * accessed which does not have such a .yarep directory yet.
  * <br/><br/>
  * Repository configuration:
  * <pre>
  * &lt;repository class="org.wyona.yarep.impl.repo.vfs.VirtualFileSystemRepository"&gt;
  *   &lt;name&gt;Test Repository&lt;/name&gt;
  *   &lt;content src="data"/&gt;
  *   &lt;meta src="yarep-data"/&gt;
  *     &lt;s:search-index xmlns:s="http://www.wyona.org/yarep/search/2.0" indexer-class="org.wyona.yarep.impl.search.lucene.LuceneIndexer" searcher-class="org.wyona.yarep.impl.search.lucene.LuceneSearcher">
  *       &lt;index-location file="index"/>
  *       &lt;repo-auto-index-fulltext boolean="true"/>
  *       &lt;repo-auto-index-properties boolean="true"/>
  *       &lt;lucene>
  *         &lt;!-- The element 'local-tika-config' attribute 'file' is used to patch the default tika config -->
  *         &lt;local-tika-config file="tika-config.xml"/>
 
  *         &lt;!-- if fulltext-analyzer/class is not set it will use org.apache.lucene.analysis.standard.StandardAnalyzer-->
  *         &lt;fulltext-analyzer class="org.apache.lucene.analysis.standard.StandardAnalyzer"/>
 
  *         &lt;!-- if property-analyzer/class is not set it will use org.apache.lucene.analysis.WhitespaceAnalyzer-->
  *         &lt;property-analyzer class="org.apache.lucene.analysis.WhitespaceAnalyzer"/>
 
  *         &lt;write-lock-timeout ms="3000"/>
  *       &lt;/lucene>
  * &lt;/s:search-index>
  * &lt;/repository&gt;
  * </pre>
  * Explanation:
  * <ul>
  *   <li>name: name of the repository</li>
  *   <li>content: path to the content directory, absolute or relative to the repo config file</li>
  *   <li>meta (optional): path to the meta directory. If this element is omitted, the meta data
  *                        will be written into the content directory.</li>
  *   <li>(deprecated) search-index (optional): enable indexing/searching of repository content<br/>
  *     Attributes:
  *     <ul>
  *       <li>src: path to the search index</li>
  *       <li>index-fulltext (yes/no): do fulltext indexing (default=yes)</li>
  *       <li>index-properties (yes/no): do indexing of properties (default=yes)</li>
  *       <li>TODO: allow to specify tika config file</li>
  *     </ul>
  *   </li>
  *   <li>search (optional) (ns = http://www.wyona.org/yarep/search/2.0): enable indexing/searching of repository content<br/>
  *     Element/Attributes:
  *     <ul>
  *       <li>search/indexer-class: class of yarep Indexer implementation</li>
  *       <li>search/searcher-class: class of yarep Searcher implementation</li>
  *       <li>auto-indexing boolean (true/false): </li>
  *       <li>index-location/file: file location of index</li>
  *       <li>index-fulltext/boolean (true/false): indexing of fulltext</li>
  *       <li>index-properties/boolean (true/false): indexing of properties</li>
  *     </ul>
  *   </li>
  * </ul>
  * 
  */
 public class VirtualFileSystemRepository implements Repository {
 
     private static Logger log = Logger.getLogger(VirtualFileSystemRepository.class);
 
     protected String id;
     protected File configFile;
     protected String name;
     protected Map map;
     protected Storage storage;
     private String alternative =  null;
     private String dirListingMimeType = "application/xml";
     private boolean isFulltextIndexingEnabled = false;
     private boolean isPropertyIndexingEnabled = false;
     private Indexer indexer = null;
     private Searcher searcher = null;
 
     // Configuration parameters of the <splitpath ...> element
     private boolean splitPathEnabled = false;
     private int splitparts = 0;
     private int splitlength = 0;
     private String DEFAULT_DUMMY_SEPARATOR_VALUE = "-";
     private String dummySeparator = DEFAULT_DUMMY_SEPARATOR_VALUE;
     private String[] includepaths = {};
     
     /**
      *
      */
     public VirtualFileSystemRepository() {
     }
     
     /**
      *
      */
     public VirtualFileSystemRepository(String id, File configFile) throws RepositoryException {
         setID(id);
         readConfiguration(configFile);
     }
 
     /**
      * Read respectively load repository configuration
      */
     public void readConfiguration(File configFile) throws RepositoryException {
         this.configFile = configFile;
         DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(true);
         Configuration config;
 
         try {
             config = builder.buildFromFile(configFile);
 
             name = config.getChild("name", false).getValue();
 
             this.contentDir = new File(config.getChild("content", false).getAttribute("src"));
             
             if (!this.contentDir.isAbsolute()) {
                 this.contentDir = FileUtil.file(configFile.getParent(), this.contentDir.toString());
             }
 
             log.info("Content dir: " + this.contentDir.getAbsolutePath());
 
             map = (Map) Class.forName("org.wyona.yarep.impl.VFileSystemMapImpl").newInstance();
             ((org.wyona.yarep.impl.VFileSystemMapImpl) map).setPathsDir(contentDir, configFile);
             ((org.wyona.yarep.impl.VFileSystemMapImpl) map).setIgnorePatterns(config.getChild("content", false).getChildren("ignore"));
 
             Configuration metaDirConfig = config.getChild("meta", false);
             if (metaDirConfig != null) {
                 this.metaDir = new File(metaDirConfig.getAttribute("src"));
             
                 if (!this.metaDir.isAbsolute()) {
                     this.metaDir = FileUtil.file(configFile.getParent(), this.metaDir.toString());
                 }
 
                 log.info("Meta dir: " + this.metaDir);
             }
 
             Configuration directoryConfig = config.getChild("directory", false);
             if (directoryConfig != null) {
                 alternative = directoryConfig.getAttribute("alternative", alternative);
                 dirListingMimeType = directoryConfig.getAttribute("mime-type", dirListingMimeType);
             }
             log.debug("Alternative: " + alternative);
             log.debug("Mime type of directory listing: " + dirListingMimeType);
 
             Configuration searchConfig = config.getChild("search-index", false);
             if(searchConfig != null && searchConfig.getNamespace() != null && searchConfig.getNamespace().equals("http://www.wyona.org/yarep/search/2.0")) {
                 log.info("Use index/search configuration version 2.0!");
                 isFulltextIndexingEnabled = searchConfig.getChild("repo-auto-index-fulltext").getAttributeAsBoolean("boolean", true);
                 isPropertyIndexingEnabled = searchConfig.getChild("repo-auto-index-properties").getAttributeAsBoolean("boolean", true);
             } else {
                 log.warn("Use deprecated configuration version 1.0!");
                 searchConfig = config.getChild("search-index", false);
                 if (searchConfig != null) {
                     isFulltextIndexingEnabled = searchConfig.getAttributeAsBoolean("index-fulltext", true);
                     isPropertyIndexingEnabled = searchConfig.getAttributeAsBoolean("index-properties", true);
                 } else {
                     isFulltextIndexingEnabled = false;
                     isPropertyIndexingEnabled = false;
                 }
             }
             
             String indexerClass = "org.wyona.yarep.impl.search.lucene.LuceneIndexer"; // Default
             String searcherClass = "org.wyona.yarep.impl.search.lucene.LuceneSearcher"; // Default
             if (searchConfig != null) {
                 indexerClass = searchConfig.getAttribute("indexer-class","org.wyona.yarep.impl.search.lucene.LuceneIndexer");
                 searcherClass = searchConfig.getAttribute("searcher-class","org.wyona.yarep.impl.search.lucene.LuceneSearcher");
             }
 
             indexer = (Indexer) Class.forName(indexerClass).newInstance();
             indexer.configure(searchConfig, configFile, this);
             
             searcher = (Searcher) Class.forName(searcherClass).newInstance();
             searcher.configure(searchConfig, configFile, this);
 
             // Read the <splitpath ...> configuration
             Configuration splitConfig = config.getChild("splitpath", false);
             if (splitConfig != null) {
                 splitPathEnabled = true;
                 String depth = splitConfig.getAttribute("depth", "0");
                 splitparts = Integer.parseInt(depth);
 
                 String length;
                 length = splitConfig.getAttribute("length", "0");
                 splitlength = Integer.parseInt(length);
 
                 dummySeparator = splitConfig.getAttribute("escape", DEFAULT_DUMMY_SEPARATOR_VALUE);
 
                 int c = splitConfig.getChildren("include").length;
                 int i = 0;
                 if (c > 0) {
                     includepaths = new String[c];
                     for (Configuration include : splitConfig.getChildren("include")) {
                         includepaths[i++] = include.getAttribute("path");
                     }
                 }
             } 
         } catch (Exception e) {
             log.error(e.toString());
             throw new RepositoryException("Could not read repository configuration: " 
                     + e.getMessage(), e);
         }
     }
 
     /**
     *
     */
    public String toString() {
        return "Repository: ID = " + id + ", Configuration-File = " + configFile + ", Name = " + name;
    }
 
    /**
     * Get repository ID
     */
    public String getID() {
        return id;
    }
 
    /**
     * Set repository ID
     */
    public void setID(String id) {
        this.id = id;
    }
 
    /**
     * Get repository name
     */
    public String getName() {
        return name;
    }
 
    /**
     * Get repository configuration file
     */
    public File getConfigFile() {
        return configFile;
    }
    
     public void addSymbolicLink(Path target, Path link) throws RepositoryException {
         log.warn("Not implemented.");
     }
 
     public boolean delete(Path path) throws RepositoryException {
         getNode(path.toString()).delete();
         return true;
     }
 
     /**
      * @return true if node has been deleted, otherwise false
      */
     public boolean delete(Path path, boolean recursive) throws RepositoryException {
         log.warn("Not implemented yet!");
         if (recursive) throw new RepositoryException("Not implemented yet");
         return delete(path);
     }
 
 
     /**
      * @see org.wyona.yarep.core.Repository#exists(org.wyona.yarep.core.Path)
      */
     public boolean exists(Path path) throws RepositoryException {
         log.warn("DEPRECATED");
         return existsNode(path.toString());
     }
 
     public Path[] getChildren(Path path) throws RepositoryException {
         Node node = getNode(path.toString());
         Node[] childNodes = node.getNodes();
         Path[] childPaths = new Path[childNodes.length];
         for (int i=0; i<childNodes.length; i++) {
             childPaths[i] = new Path(childNodes[i].getPath());
         }
         return childPaths;
     }
 
     public void getContentLength(Path path) throws RepositoryException {
         log.warn("Not implemented.");
     }
 
     public InputStream getInputStream(Path path) throws RepositoryException {
         return getNode(path.toString()).getInputStream();
     }
 
     public long getLastModified(Path path) throws RepositoryException {
         return getNode(path.toString()).getLastModified();
     }
 
     public OutputStream getOutputStream(Path path) throws RepositoryException {
         return getNode(path.toString()).getOutputStream();
     }
 
     public Reader getReader(Path path) throws RepositoryException {
         try {
             return new InputStreamReader(getNode(path.toString()).getInputStream(), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
    /**
     * @see org.wyona.yarep.core.Repository#getRevisions(Path)
     */
     public String[] getRevisions(Path path) throws RepositoryException {
         Node node = getNode(path.toString());
         Revision[] revisions = node.getRevisions();
         String[] revisionNames = new String[revisions.length];
         for (int i=0; i<revisions.length; i++) {
            revisionNames[i] = revisions[i].getRevisionName();
         }
         return revisionNames;
     }
 
     public long getSize(Path path) throws RepositoryException {
         return getNode(path.toString()).getSize();
     }
 
     public UID getUID(Path path) throws RepositoryException {
         log.warn("Not implemented.");
         return null;
     }
 
     public void getURI(Path path) throws RepositoryException {
         log.warn("Not implemented.");
     }
 
     public void getValidity(Path path) throws RepositoryException {
         log.warn("Not implemented.");
     }
 
     public Writer getWriter(Path path) throws RepositoryException {
         try {
             return new OutputStreamWriter(getNode(path.toString()).getOutputStream(), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     public boolean isCollection(Path path) throws RepositoryException {
         return getNode(path.toString()).isCollection();
     }
 
     public boolean isResource(Path path) throws RepositoryException {
         return getNode(path.toString()).isResource();
     }
 
     ///////////////////////////////////////////////////////////////////////////
     // New methods for node based repository
     ///////////////////////////////////////////////////////////////////////////
     
     protected File contentDir = null;
     protected File metaDir = null;
     
     /**
      * @see org.wyona.yarep.core.Repository#copy(java.lang.String, java.lang.String)
      */
     public void copy(String srcPath, String destPath) throws RepositoryException {
         // TODO: not implemented yet
         log.warn("Not implemented yet.");
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#existsNode(java.lang.String)
      */
     public boolean existsNode(String path) throws RepositoryException {
         log.debug("Check whether node exists: " + path);
 
         // INFO: Strip trailing slash, whereas do not strip in the case of the ROOT node denoted by '/':
         String pathWithoutTrailingSlash = path;
         if (path.length() > 1 && path.endsWith("/")) {
             pathWithoutTrailingSlash = path.substring(0, path.length() - 1);
         }
 
         if (splitPathEnabled) {
             String splittedPath = splitPath(path); // IMPORTANT: If a trailing slash exists, then it is important for split path to know about it, because it will be escaped and matched accordingly!
             return map.exists(new Path(splittedPath)) || map.exists(new Path(pathWithoutTrailingSlash)); // INFO: The OR is because of backwards compatibility in case that a node exists with an unsplitted path, because it has not been migrated yet (which can happen if it has only been read so far, but never written since introducing the split path configuration)
         } else {
             return map.exists(new Path(pathWithoutTrailingSlash));
         }
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#getNode(java.lang.String)
      */
     public Node getNode(String path) throws NoSuchNodeException, RepositoryException {
         log.debug("Get node: " + path);
 
         if (existsNode(path)) {
             String uuid = new UID(path).toString();
             return new VirtualFileSystemNode(this, path, uuid);
         } else {
             throw new NoSuchNodeException(path, this);
         }
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#getNodeByUUID(java.lang.String)
      */
     public Node getNodeByUUID(String uuid) throws NoSuchNodeException, RepositoryException {
         //return new VirtualFileSystemNode(this, path, uuid);
         // TODO: not implemented yet
         log.warn("Not implemented yet.");
         return null;
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#getRootNode()
      */
     public Node getRootNode() throws RepositoryException {
         return getNode("/");
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#move(java.lang.String, java.lang.String)
      */
     public void move(String srcPath, String destPath) throws RepositoryException {
         File srcFile = new File(contentDir, srcPath);
         File destFile = new File(contentDir, destPath);
         log.debug("Move source '" + srcFile.getAbsolutePath() + "' to destination '" + destFile.getAbsolutePath() + "'.");
         boolean moveSucceeded = srcFile.renameTo(destFile);
         if (!moveSucceeded) {
             log.error("Move source '" + srcFile.getAbsolutePath() + "' to destination '" + destFile.getAbsolutePath() + "' failed!");
         } else {
             log.info("Move source '" + srcFile.getAbsolutePath() + "' to destination '" + destFile.getAbsolutePath() + "' succeeded");
         }
     }
     
     // implementation specific methods:
     
     public File getContentDir() {
         return this.contentDir;
     }
     
     public File getMetaDir() {
         return this.metaDir;
     }
 
     /**
      * Get map
      */
     Map getMap() {
         return this.map;
     }
 
     /**
      * Get alternative filename
      */
     public String getAlternative() {
         return alternative;
     }
 
     /**
      * Get mime type of directory listing
      */
     public String getDirListingMimeType() {
         return dirListingMimeType;
     }
 
     /**
      *
      */
     public void close() throws RepositoryException {
         log.warn("Closing repository: " + getName() + " (" + getConfigFile() + ")");
 
 /*
         log.warn("Closing index writers");
         IndexWriter iw;
         try {
             iw = getIndexWriter();
             if (iw != null) {
                 iw.close();
             }
             iw = getPropertiesIndexWriter();
             if (iw != null) {
                 iw.close();
             }
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
 */
     }
 
     /**
      * Search content
      */
     public Node[] search(String query) throws RepositoryException {
         try {
             return searcher.search(query);
         } catch (SearchException e) {
             log.error("Could not search for query: " + query, e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     /**
      * Search property
      */
     public Node[] searchProperty(String pName, String pValue, String path) throws RepositoryException {
         try{
             return searcher.searchProperty(pName, pValue, path);
         } catch (SearchException e) {
             log.error("Could not search for " + pName + " " + pValue + " " + path, e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     public Indexer getIndexer() {
         return indexer;
     }
     
     public Searcher getSearcher() {
         return searcher;
     }
 
     public boolean isAutoFulltextIndexingEnabled() {
         return isFulltextIndexingEnabled;
     }
 
     public boolean isAutoPropertyIndexingEnabled() {
         return isPropertyIndexingEnabled;
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#importNode(String, String, Repository)
      */
     public boolean importNode(String destPath, String srcPath, Repository srcRepository) throws RepositoryException {
         try {
             if (existsNode(destPath)) {
                 log.warn("Node '" + destPath + "' already exists and will be overwritten!");
             }
 
             Node srcNode = srcRepository.getNode(srcPath);
             if (srcNode.isCollection()) {
                 log.warn("This seems to be a collection and hence no data will be imported: " + srcNode.getPath());
                 Node destNode = org.wyona.yarep.util.YarepUtil.addNodes(this, destPath, org.wyona.yarep.core.NodeType.COLLECTION);
                 // TODO: What about properties?!
                 return true;
             }
 
             Node destNode = org.wyona.yarep.util.YarepUtil.addNodes(this, destPath, org.wyona.yarep.core.NodeType.RESOURCE);
             OutputStream os = destNode.getOutputStream();
             IOUtils.copy(srcNode.getInputStream(), os);
             os.close();
 
             log.info("Import of revisions and meta/properties ... (src: " + srcPath + ", dest: " + destPath + ")");
 
             // Copy revisions of node
             Revision[] revisions = srcNode.getRevisions();
             for (int i = 0; i < revisions.length; i++) {
                 log.info("Copy revision: " + revisions[i].getRevisionName());
                 File revisionContentFile = ((VirtualFileSystemNode) destNode).getRevisionContentFile(revisions[i].getRevisionName());
                 if (!new File(revisionContentFile.getParent()).exists())
                     new File(revisionContentFile.getParent()).mkdirs();
                 FileOutputStream out = new FileOutputStream(revisionContentFile);
                 IOUtils.copy(revisions[i].getInputStream(), out);
                 out.close();
 
                 // Copy meta/properties of revision
                 File destRevisionMetaFile = ((VirtualFileSystemNode) destNode).getRevisionMetaFile(revisions[i].getRevisionName());
                 copyProperties(revisions[i], destRevisionMetaFile);
             }
 
             // Copy meta/properties of node
             File metaFile = ((VirtualFileSystemNode) destNode).getMetaFile();
             copyProperties(srcNode, metaFile);
         } catch (Exception e) {
             throw new RepositoryException(e);
         }
         return true;
     }
 
     /**
      * Copy properties of source node to destination meta file (also see VirtualFileSystemNode#saveProperties())
      * @param srcNode Source node containing properties
      * @param destMetaFile Destination meta file where properties of source node shall be copied to
      */
     private boolean copyProperties(Node srcNode, File destMetaFile) throws Exception {
         if (!new File(destMetaFile.getParent()).exists())
             new File(destMetaFile.getParent()).mkdirs();
         log.info("Copy properties: " + destMetaFile);
         java.io.PrintWriter writer = new java.io.PrintWriter(new FileOutputStream(destMetaFile));
         org.wyona.yarep.core.Property[] properties = srcNode.getProperties();
         for (int i = 0; i < properties.length; i++) {
             writer.println(properties[i].getName() + "<" + org.wyona.yarep.core.PropertyType.getTypeName(properties[i].getType()) + ">:" + properties[i].getValueAsString());
         }
         writer.flush();
         writer.close();
         return true;
     }
 
     /**
      * Splits a String such that the result can be used as a repo path for a tree-like repo structure.
      *
      * This method splits off n strings (where n = parts) of length partlength, e.g. if
      * splitPath("ec2c0c02-1d7d-4a21-8a39-68f9f72dea09", 3, 4) is called, then:
      * in:  ec2c0c02-1d7d-4a21-8a39-68f9f72dea09, 3, 4
      * out: ec2c/0c02/-1d7/d-4a21-8a39-68f9f72dea09
      *
      * If the strings length is shorter than parts * partslength, then as many
      * parts as possible are split, e.g.
      * in:  foobar, 2, 5
      * out: fooba/r
      * in:  lorem, 3, 10
      * out: lorem
      *
      * An example with "/" characters (whereas "-" is the default escape character):
      * in:  /foobar/lorem/ipsum.txt, parts = 3, length = 3
      * out: /foo/bar/-lo/rem/ipsum.txt
      *
      * Depending on the path and split length, the physical nodes do not have reflect the parent-child relationship as the logical path, e.g.
      * - Assume length = 2
      * - The logical path /aa/ will be split as phyisical path /aa/+
      * - The logical path /aa/bb/ will be split as physical path /aa/+b/b+
      * which demonstrates that although "bb" is a logical child of "/aa", physically "+b" is not a child of "/aa/+"
      *
      * @param path A yarep path
      * @return splitted path or unsplit if it does not match any of the include patters
      */
     String splitPath(String path) {
         log.debug("Split path: " + path);
 
         // NOTE: uuid should be a full yarep path, so we can safely remove the leading slash
         
         // INFO: Check if the given path matches any of the include values in the configuration
         boolean include = false;
         String base = "";
         for (String s : includepaths) {
             if (path.startsWith(s)) {
                 include = true;
                 base = s;
                 break;
             }
         }
 
         // INFO: Return the path unchanged if it doesn't match any of the include values
         if (!include) {
             log.debug("Path '" + path + "' does not need to be splitted.");
             return path;
         }
         
         // remove the leading base string, will be added again later
         path = path.substring(base.length(), path.length());
 
         // Escape/replace "/" characters where necessary
         if (path.length() <= splitparts * splitlength) {
             path = path.replaceAll("/", dummySeparator);
         } else {
             path = String.format("%s%s",
                     path.substring(0, splitparts * splitlength).replaceAll("/", dummySeparator),
                     path.substring(splitparts * splitlength));
         }
         log.debug("Escaped path: " + path);
 
         // now do the actual splitting
         int len = path.length();
         int pos = 0;
         String out = "";
 
         int partc = 0;
         int w;
         while (len > 0 && partc < splitparts) {
             partc++;
             if (len < splitlength) {
                 w = len;
             } else {
                 w = splitlength;
             }
             out += path.substring(pos, pos + w);
             pos += w;
             len -= w;
 
             if (len > 0) {
                 //log.debug("Append slash: " + out);
                 out += "/";
             }
         }
 
         // append remainder/rest
         if (len > 0) {
             //log.debug("Append rest: " + out + ", " + path.substring(pos, pos + len));
             if (out.endsWith("/") && path.substring(pos, pos + len).startsWith("/")) {
                 //log.info("Remove one of the two slashes" + path.substring(pos + 1, pos + len));
                 out += path.substring(pos + 1, pos + len);
             } else {
                 out += path.substring(pos, pos + len);
             }
         }
 
         // finally, add the leading zero again and return the new path
         String splittedPath = base + out;
         log.debug("Splitted path: " + splittedPath);
         return splittedPath;
     }
 
     /**
      * Check whether split path is enabled and make this available to classes within this package
      */
     boolean isSplitPathEnabled() {
         return splitPathEnabled;
     }
 }

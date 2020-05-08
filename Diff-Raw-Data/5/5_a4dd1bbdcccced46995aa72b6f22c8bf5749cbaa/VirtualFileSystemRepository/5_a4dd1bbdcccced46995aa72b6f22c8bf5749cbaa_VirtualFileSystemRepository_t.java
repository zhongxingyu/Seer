 package org.wyona.yarep.impl.repo.vfs;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 import org.apache.log4j.Category;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.WhitespaceAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.IndexSearcher;
 
 import org.apache.tika.config.TikaConfig;
 
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
  *   &lt;search-index src="search-index-data" index-fulltext="yes" index-properties="no"/&gt;
  * &lt;/repository&gt;
  * </pre>
  * Explanation:
  * <ul>
  *   <li>name: name of the repository</li>
  *   <li>content: path to the content directory, absolute or relative to the repo config file</li>
  *   <li>meta (optional): path to the meta directory. If this element is omitted, the meta data
  *                        will be written into the content directory.</li>
  *   <li>search-index (optional): enable indexing/searching of repository content<br/>
  *     Attributes:
  *     <ul>
  *       <li>src: path to the search index</li>
  *       <li>index-fulltext (yes/no): do fulltext indexing (default=yes)</li>
  *       <li>index-properties (yes/no): do indexing of properties (default=yes)</li>
  *       <li>TODO: allow to specify tika config file</li>
  *     </ul>
  *   </li>
  * </ul>
  * 
  */
 public class VirtualFileSystemRepository implements Repository {
 
     private static Category log = Category.getInstance(VirtualFileSystemRepository.class);
 
     protected String id;
     protected File configFile;
 
     protected String name;
 
     protected Map map;
     protected Storage storage;
 
     private String alternative =  null;
     private File fulltextSearchIndexFile = null;
     private File propertiesSearchIndexFile = null;
     private Analyzer analyzer = null;
     private Analyzer whitespaceAnalyzer = null;
     private String dirListingMimeType = "application/xml";
 
     private String FULLTEXT_INDEX_DIR = "fulltext";
     private String PROPERTIES_INDEX_DIR = "properties";
 
     // NOTE: Do not init global IndexWriters because this can lead to problems within a cluster
     //private IndexWriter indexWriter;
     //private IndexWriter propertiesIndexWriter;
 
     private TikaConfig tikaConfig;
     
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
         DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
         Configuration config;
 
         try {
             config = builder.buildFromFile(configFile);
 
             name = config.getChild("name", false).getValue();
 
             this.contentDir = new File(config.getChild("content", false).getAttribute("src"));
             
             if (!this.contentDir.isAbsolute()) {
                 this.contentDir = FileUtil.file(configFile.getParent(), this.contentDir.toString());
             }
 
             log.info("Content dir: " + this.contentDir);
 
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
 
             Configuration searchIndexConfig = config.getChild("search-index", false);
             if (searchIndexConfig != null) {
                 File searchIndexSrcFile = new File(searchIndexConfig.getAttribute("src", "index"));
                 if (!searchIndexSrcFile.isAbsolute()) {
                     searchIndexSrcFile = FileUtil.file(configFile.getParent(), searchIndexSrcFile.toString());
                 }
 
                 boolean isFulltextIndexingEnabled = searchIndexConfig.getAttributeAsBoolean(
                         "index-fulltext", true);
                 boolean isPropertyIndexingEnabled = searchIndexConfig.getAttributeAsBoolean(
                         "index-properties", true);
 
                 analyzer = new StandardAnalyzer();
                 // TODO: For search within properties the WhitespaceAnalyzer is used because the StandardAnalyzer doesn't accept resp. misinterprets escaped query strings, e.g. 03\:07\- ...
                 whitespaceAnalyzer = new WhitespaceAnalyzer();
                 
                 if (isFulltextIndexingEnabled) {
                     fulltextSearchIndexFile = new File(searchIndexSrcFile, FULLTEXT_INDEX_DIR);
                     if (!fulltextSearchIndexFile.isDirectory() && searchIndexSrcFile.exists()) {
                         fulltextSearchIndexFile = searchIndexSrcFile;
                     }
                    log.info("Fulltext search index path: " + fulltextSearchIndexFile);
                     
                     // Create a lucene search index if it doesn't exist yet
                     // IMPORTANT: This doesn't work within a clustered environment, because the cluster node starting first will lock the index and all other nodes will not be able to startup!
                     //this.indexWriter = createIndexWriter(fulltextSearchIndexFile, analyzer);
 
                     String localTikaConfigSrc = searchIndexConfig.getAttribute("local-tika-config", null);
                     if (localTikaConfigSrc != null) {
                         File localTikaConfigFile = new File(localTikaConfigSrc);
                         if (!localTikaConfigFile.isAbsolute()) {
                             localTikaConfigFile = FileUtil.file(configFile.getParent(), localTikaConfigFile.toString());
                         }
                         if (localTikaConfigFile.isFile()) {
                             log.warn("Use local tika config: " + localTikaConfigFile.getAbsolutePath());
                             tikaConfig = new TikaConfig(localTikaConfigFile);
                         } else {
                             log.error("No such file: " + localTikaConfigFile + " (Default tika config will be used)");
                             tikaConfig = TikaConfig.getDefaultConfig();
                         }
                     } else {
                        log.info("Use default tika config");
                         tikaConfig = TikaConfig.getDefaultConfig();
                     }
                 }
 
                 if (isPropertyIndexingEnabled) {
                     // Create properties index dir subdirectory in order to save the lucene index for searching on properties
                     propertiesSearchIndexFile = new File(searchIndexSrcFile, PROPERTIES_INDEX_DIR);
                     log.warn("Properties search index path: " + propertiesSearchIndexFile);
                     
                     // IMPORTANT: This doesn't work within a clustered environment, because the cluster node starting first will lock the index and all other nodes will not be able to startup!
                     //this.propertiesIndexWriter = createIndexWriter(propertiesSearchIndexFile, whitespaceAnalyzer);
                 }
 
             } else {
                 log.warn("No search index dir (<search-index src=\"...\"/>) configured within: " + configFile);
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
 
     public boolean exists(Path path) throws RepositoryException {
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
 
     public String[] getRevisions(Path path) throws RepositoryException {
         Node node = getNode(path.toString());
         Revision[] revisions = node.getRevisions();
         String[] revisionNames = new String[revisions.length];
         for (int i=0; i<revisions.length; i++) {
             revisionNames[i] = revisions[i].getName();
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
         // strip trailing slash:
         if (path.length() > 1 && path.endsWith("/")) {
             path = path.substring(0, path.length() - 1);
         }
         return map.exists(new Path(path));
     }
 
     /**
      * @see org.wyona.yarep.core.Repository#getNode(java.lang.String)
      */
     public Node getNode(String path) throws NoSuchNodeException, RepositoryException {
         // strip trailing slash:
         if (path.length() > 1 && path.endsWith("/")) {
             path = path.substring(0, path.length() - 1);
         }
         String uuid;
 
         if (map.exists(new Path(path))) {
             uuid = new UID(path).toString();
         } else {
             throw new NoSuchNodeException(path, this);
         }
         
         return new VirtualFileSystemNode(this, path, uuid);
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
         // TODO: not implemented yet
         log.warn("Not implemented yet.");
     }
     
     // implementation specific methods:
     
     public File getContentDir() {
         return this.contentDir;
     }
     
     public File getMetaDir() {
         return this.metaDir;
     }
     
     public Map getMap() {
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
             Searcher searcher = new IndexSearcher(getSearchIndexFile().getAbsolutePath());
             if (searcher != null) {
                 try {
                     org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.queryParser.QueryParser("_FULLTEXT", analyzer).parse(query);
                     org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                     log.info("Query \"" + query + "\" returned " + hits.length() + " hits");
                     Node[] results = new Node[hits.length()];
                     for (int i = 0; i < results.length;i++) {
                         results[i] = getNode(hits.doc(i).getField("_PATH").stringValue());
                     }
                     return results;
                 } catch (Exception e) {
                     log.error(e, e);
                     throw new RepositoryException(e.getMessage());
                 }
             } else {
                 log.warn("No search index seems to be configured!");
             }
         } catch (Exception e) {
             log.error(e, e);
             throw new RepositoryException(e.getMessage());
         }
         return null;
     }
 
     /**
      * Search property
      */
     public Node[] searchProperty(String pName, String pValue, String path) throws RepositoryException {
         try {
             Searcher searcher = new IndexSearcher(getPropertiesSearchIndexFile().getAbsolutePath());
             if (searcher != null) {
                 try {
                     org.apache.lucene.search.Query luceneQuery = new org.apache.lucene.queryParser.QueryParser(pName, whitespaceAnalyzer).parse(pValue);
                     org.apache.lucene.search.Hits hits = searcher.search(luceneQuery);
                     log.info("Number of matching documents: " + hits.length());
                     List results = new ArrayList();
                     for (int i = 0; i < hits.length(); i++) {
                         try {
                             String resultPath = hits.doc(i).getField("_PATH").stringValue();
                             
                             // subtree filter
                             if (resultPath.startsWith(path)) {
                                 results.add(getNode(resultPath));
                             }
                         } catch (NoSuchNodeException nsne) {
                             log.warn("Found within search index, but no such node within repository: " + hits.doc(i).getField("_PATH").stringValue());
                         }
                     }
                     return (Node[])results.toArray(new Node[results.size()]);
                     
                 } catch (Exception e) {
                     log.error(e, e);
                     throw new RepositoryException(e.getMessage());
                 }
             }
         } catch (Exception e) {
             log.error(e, e);
             throw new RepositoryException(e.getMessage());
         }
         return null;
     }
     
     /**
      * Get location of properties search index
      */
     public File getPropertiesSearchIndexFile() {
         return propertiesSearchIndexFile;
     }
     
     /**
      *
      */
     public File getSearchIndexFile() {
         return fulltextSearchIndexFile;
     }
     
     /**
      *
      */
     public Analyzer getAnalyzer() {
         return analyzer;
     }
     
     /**
      *
      */
     public IndexWriter createFulltextIndexWriter() throws Exception {
          return createIndexWriter(fulltextSearchIndexFile, analyzer);
         // IMPORTANT: This doesn't work within a clustered environment!
         //return this.indexWriter;
     }
     
     /**
      *
      */
     public IndexWriter createPropertiesIndexWriter() throws Exception {
         return createIndexWriter(propertiesSearchIndexFile, whitespaceAnalyzer);
         // IMPORTANT: This doesn't work within a clustered environment!
         //return this.propertiesIndexWriter;
     }
     
     /**
      * Init an IndexWriter
      * @param indexDir Directory where the index is located
      */
     private IndexWriter createIndexWriter(File indexDir, Analyzer analyzer) throws Exception {
         IndexWriter iw = null;
         if (indexDir != null) {
             if (indexDir.isDirectory()) {
                 iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, false);
             } else {
                 iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
             }
             // TODO: iw.setWriteLockTimeout(long ms)
             //log.debug("Max write.lock timeout: " + iw.getDefaultWriteLockTimeout() + " milliseconds");
             return iw;
         }
         return null;
     }
     
     /**
      *
      */
     public Analyzer getWhitespaceAnalyzer() {
         return whitespaceAnalyzer;
     }
 
     /**
      *
      */
     public TikaConfig getTikaConfig() {
         return tikaConfig;
     }
 }

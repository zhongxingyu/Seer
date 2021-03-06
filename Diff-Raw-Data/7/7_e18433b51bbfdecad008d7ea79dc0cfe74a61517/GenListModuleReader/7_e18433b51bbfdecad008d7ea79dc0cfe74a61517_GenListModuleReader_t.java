 /*
  * This file is part of the DITA Open Toolkit project.
  * See the accompanying license.txt file for applicable licenses.
  */
 
 /*
  * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
  */
 package org.dita.dost.reader;
 
 import static org.dita.dost.util.Constants.*;
 import static org.dita.dost.util.URLUtils.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Map.Entry;
 
 import org.dita.dost.exception.DITAOTException;
 import org.dita.dost.exception.DITAOTXMLErrorHandler;
 import org.dita.dost.log.MessageBean;
 import org.dita.dost.log.MessageUtils;
 import org.dita.dost.util.Job;
 import org.dita.dost.util.KeyDef;
 import org.dita.dost.util.CatalogUtils;
 import org.dita.dost.util.FileUtils;
 import org.dita.dost.util.FilterUtils;
 import org.dita.dost.util.StringUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXNotRecognizedException;
 import org.xml.sax.SAXNotSupportedException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 
 /**
  * This class extends AbstractReader, used to parse relevant dita topics and
  * ditamap files for GenMapAndTopicListModule.
  * 
  * <p>
  * <strong>Not thread-safe</strong>. Instances can be reused by calling
  * {@link #reset()} between calls to {@link #parse(File)}.
  * </p>
  */
 public final class GenListModuleReader extends AbstractXMLReader {
     
     /** XMLReader instance for parsing dita file */
     private XMLReader reader = null;
     /** Filter utils */
     private FilterUtils filterUtils;
     /** Output utilities */
     private Job job;
     /** Basedir of the current parsing file */
     private File currentDir = null;
     /** Flag for conref in parsing file */
     private boolean hasConRef = false;
     /** Flag for href in parsing file */
     private boolean hasHref = false;
     /** Flag for keyref in parsing file */
     private boolean hasKeyRef = false;
     /** Flag for whether parsing file contains coderef */
     private boolean hasCodeRef = false;
     /** Set of all the non-conref and non-copyto targets refered in current parsing file */
     private final Set<Reference> nonConrefCopytoTargets;
     /** Set of conref targets refered in current parsing file */
     private final Set<File> conrefTargets;
     /** Set of href nonConrefCopytoTargets refered in current parsing file */
     private final Set<File> hrefTargets;
     /** Set of href targets with anchor appended */
     private final Set<File> hrefTopicSet;
     /** Set of chunk targets */
     private final Set<File> chunkTopicSet;
     /** Set of subject schema files */
     private final Set<File> schemeSet;
     /** Set of subsidiary files */
     private final Set<File> subsidiarySet;
     /** Set of sources of those copy-to that were ignored */
     private final Set<File> ignoredCopytoSourceSet;
     /** Map of copy-to target to souce */
     private final Map<File, File> copytoMap;
     /** Map of key definitions */
     private final Map<String, KeyDef> keysDefMap;
     /** Map to store multi-level keyrefs */
     private final Map<String, String> keysRefMap;
     /** Flag for conrefpush */
     private boolean hasconaction = false;
     /** Flag used to mark if parsing entered into excluded element */
     private boolean insideExcludedElement = false;
     /** Used to record the excluded level */
     private int excludedLevel = 0;
     /** foreign/unknown nesting level */
     private int foreignLevel = 0;
     /** chunk nesting level */
     private int chunkLevel = 0;
     /** mark topics in reltables */
     private int relTableLevel = 0;
     /** chunk to-navigation level */
     private int chunkToNavLevel = 0;
     /** Topic group nesting level */
     private int topicGroupLevel = 0;
     /** Flag used to mark if current file is still valid after filtering */
     private boolean isValidInput = false;
     /** Contains the attribution specialization from props */
     private String[][] props;
     /** Set of outer dita files */
     private final Set<File> outDitaFilesSet;
     /** Absolute system path to input file parent directory */
     private File rootDir = null;
     /** Absolute system path to file being processed */
     private File currentFile = null;
     private File rootFilePath = null;
     private boolean setSystemid = true;
     /** Stack for @processing-role value */
     private final Stack<String> processRoleStack;
     /** Depth inside a @processing-role parent */
     private int processRoleLevel;
     /** Topics with processing role of "resource-only" */
     private final Set<File> resourceOnlySet;
     /** Topics with processing role of "normal" */
     private final Set<File> crossSet;
     /** Subject scheme relative file paths. */
     private final Set<File> schemeRefSet;
     /** Relationship graph between subject schema. Keys are paths of subject map files and values
      * are paths of subject scheme maps. A key {@code "ROOT"} contains all subject schemes. */
     private Map<File, Set<File>> schemeRelationGraph = null;
     private final List<ExportAnchor> resultList = new ArrayList<ExportAnchor>();
     private ExportAnchor currentExportAnchor;
     /** Flag to show whether a file has <exportanchors> tag */
     private boolean hasExport = false;
     /** For topic/dita files whether a </file> tag should be added */
     private boolean shouldAppendEndTag = false;
     /** Store the href of topicref tag */
     private URI topicHref;
     /** Topicmeta set for merge multiple exportanchors into one. Each topicmeta/prolog can define many exportanchors */
     private final Set<String> topicMetaSet;
     /** Refered topic id */
     private String topicId = "";
     /** Map to store plugin id */
     private final Map<String, Set<String>> pluginMap = new HashMap<String, Set<String>>();
     /** Transtype */
     private String transtype;
     /** Map to store referenced branches. */
     private final Map<String, List<String>> vaildBranches;
     /** Int to mark referenced nested elements. */
     private int level;
     /** Topicref stack */
     private final Stack<String> topicrefStack;
     /** Store the primary ditamap file name. */
     private String primaryDitamap = "";
     /** Store the external/peer keydefs */
     private final Map<String, URI> exKeysDefMap;
 
     /**
      * Constructor.
      */
     public GenListModuleReader() {
         nonConrefCopytoTargets = new HashSet<Reference>(64);
         hrefTargets = new HashSet<File>(32);
         hrefTopicSet = new HashSet<File>(32);
         chunkTopicSet = new HashSet<File>(32);
         schemeSet = new HashSet<File>(32);
         schemeRefSet = new HashSet<File>(32);
         conrefTargets = new HashSet<File>(32);
         copytoMap = new HashMap<File, File>(16);
         subsidiarySet = new HashSet<File>(16);
         ignoredCopytoSourceSet = new HashSet<File>(16);
         outDitaFilesSet = new HashSet<File>(64);
         keysDefMap = new HashMap<String, KeyDef>();
         keysRefMap = new HashMap<String, String>();
         exKeysDefMap = new HashMap<String, URI>();
         processRoleLevel = 0;
         processRoleStack = new Stack<String>();
         resourceOnlySet = new HashSet<File>(32);
         crossSet = new HashSet<File>(32);
         topicMetaSet = new HashSet<String>(16);
         vaildBranches = new HashMap<String, List<String>>(32);
         level = 0;
         topicrefStack = new Stack<String>();
         props = null;
         try {
             reader = StringUtils.getXMLReader();
         } catch (final SAXException e) {
             throw new RuntimeException("Unable to create XML parser: " + e.getMessage(), e);
         }
         reader.setContentHandler(this);
         try {
             reader.setProperty(LEXICAL_HANDLER_PROPERTY, this);
         } catch (final SAXNotRecognizedException e1) {
             logger.logError(e1.getMessage(), e1) ;
         } catch (final SAXNotSupportedException e1) {
             logger.logError(e1.getMessage(), e1) ;
         }
     }
 
     /**
      * Get transtype.
      * 
      * @return the transtype
      */
     public String getTranstype() {
         return transtype;
     }
 
     /**
      * Set transtype.
      * 
      * @param transtype the transtype to set
      */
     public void setTranstype(final String transtype) {
         this.transtype = transtype;
     }
 
     /**
      * @return the pluginMap
      */
     public Map<String, Set<String>> getPluginMap() {
         return pluginMap;
     }
 
     /**
      * Get export anchors.
      * 
      * @return list of export anchors
      */
     public List<ExportAnchor> getExportAnchors() {
         return resultList;
     }
 
     /**
      * Set content filter.
      * 
      * @param filterUtils filter utils
      */
     public void setFilterUtils(final FilterUtils filterUtils) {
         this.filterUtils = filterUtils;
     }
 
     /**
      * Set output utilities.
      * 
      * @param job output utils
      */
     public void setJob(final Job job) {
         this.job = job;
     }
 
     /**
      * Get out file set.
      * 
      * @return out file set
      */
     public Set<File> getOutFilesSet() {
         return outDitaFilesSet;
     }
 
     /**
      * @return the hrefTopicSet
      */
     public Set<File> getHrefTopicSet() {
         return hrefTopicSet;
     }
 
     /**
      * @return the chunkTopicSet
      */
     public Set<File> getChunkTopicSet() {
         return chunkTopicSet;
     }
 
     /**
      * Get scheme set.
      * 
      * @return scheme set
      */
     public Set<File> getSchemeSet() {
         return schemeSet;
     }
 
     /**
      * Get scheme ref set.
      * 
      * @return scheme ref set
      */
     public Set<File> getSchemeRefSet() {
         return schemeRefSet;
     }
 
     /**
      * List of files with "@processing-role=resource-only".
      * 
      * @return the resource-only set
      */
     public Set<File> getResourceOnlySet() {
         final Set<File> res = new HashSet<File>(resourceOnlySet);
         res.removeAll(crossSet);
         return res;
     }
 
     /**
      * Get relationship graph between subject schema. Keys are subject map paths and values
      * are subject scheme paths. A key {@code "ROOT"} contains all subject schemes.
      * 
      * @return relationship graph
      */
     public Map<File, Set<File>> getRelationshipGrap() {
         return schemeRelationGraph;
     }
 
     @Deprecated
     public String getPrimaryDitamap() {
         return primaryDitamap;
     }
 
     public void setPrimaryDitamap(final String primaryDitamap) {
         this.primaryDitamap = primaryDitamap;
     }
 
     /**
      * To see if the parsed file has conref inside.
      * 
      * @return true if has conref and false otherwise
      */
     public boolean hasConRef() {
         return hasConRef;
     }
 
     /**
      * To see if the parsed file has keyref inside.
      * 
      * @return true if has keyref and false otherwise
      */
     public boolean hasKeyRef() {
         return hasKeyRef;
     }
 
     /**
      * To see if the parsed file has coderef inside.
      * 
      * @return true if has coderef and false otherwise
      */
     public boolean hasCodeRef() {
         return hasCodeRef;
     }
 
     /**
      * To see if the parsed file has href inside.
      * 
      * @return true if has href and false otherwise
      */
     public boolean hasHref() {
         return hasHref;
     }
 
     /**
      * Get all targets except copy-to.
      * 
      * @return set of target file path with option format after
      *         {@link org.dita.dost.util.Constants#STICK STICK}
      */
     public Set<Reference> getNonCopytoResult() {
         final Set<Reference> nonCopytoSet = new HashSet<Reference>(128);
 
         nonCopytoSet.addAll(nonConrefCopytoTargets);
         for (final File f : conrefTargets) {
             nonCopytoSet.add(new Reference(f.getPath()));
         }
         for (final File f : copytoMap.values()) {
             nonCopytoSet.add(new Reference(f.getPath()));
         }
         for (final File f : ignoredCopytoSourceSet) {
             nonCopytoSet.add(new Reference(f.getPath()));
         }
         for (final File filename : subsidiarySet) {
             // only activated on /generateout:3 & is out file.
             if (isOutFile(filename) && job.getGeneratecopyouter() == Job.Generate.OLDSOLUTION) {
                 nonCopytoSet.add(new Reference(filename.getPath()));
             }
         }
         // nonCopytoSet.addAll(subsidiarySet);
         return nonCopytoSet;
     }
 
     /**
      * Get the href target.
      * 
      * @return Returns the hrefTargets.
      */
     public Set<File> getHrefTargets() {
         return hrefTargets;
     }
 
     /**
      * Get conref targets.
      * 
      * @return Returns the conrefTargets.
      */
     public Set<File> getConrefTargets() {
         return conrefTargets;
     }
 
     /**
      * Get subsidiary targets.
      * 
      * @return Returns the subsidiarySet.
      */
     public Set<File> getSubsidiaryTargets() {
         return subsidiarySet;
     }
 
     /**
      * Get outditafileslist.
      * 
      * @return Returns the outditafileslist.
      */
     public Set<File> getOutDitaFilesSet() {
         return outDitaFilesSet;
     }
 
     /**
      * Get non-conref and non-copyto targets.
      * 
      * @return Returns the nonConrefCopytoTargets.
      */
     public Set<File> getNonConrefCopytoTargets() {
         final Set<File> res = new HashSet<File>(nonConrefCopytoTargets.size());
         for (final Reference r : nonConrefCopytoTargets) {
             res.add(new File(r.filename));
         }
         return res;
     }
 
     /**
      * Returns the ignoredCopytoSourceSet.
      * 
      * @return Returns the ignoredCopytoSourceSet.
      */
     public Set<File> getIgnoredCopytoSourceSet() {
         return ignoredCopytoSourceSet;
     }
 
     /**
      * Get the copy-to map.
      * 
      * @return copy-to map
      */
     public Map<File, File> getCopytoMap() {
         return copytoMap;
     }
 
     /**
      * Get the Key definitions.
      * 
      * @return Key definitions map
      */
     public Map<String, KeyDef> getKeysDMap() {
         return keysDefMap;
     }
 
     /**
      * Map of external/peek key references.
      * 
      * @return map of keys to targets
      */
     public Map<String, URI> getExKeysDefMap() {
         return exKeysDefMap;
     }
 
     /**
      * Set the relative directory of current file.
      * 
      * @param dir dir
      */
     public void setCurrentDir(final File dir) {
         currentDir = dir;
     }
 
     /**
      * Check if the current file is valid after filtering.
      * 
      * @return true if valid and false otherwise
      */
     public boolean isValidInput() {
         return isValidInput;
     }
 
     /**
      * Check if the current file has conaction.
      * 
      * @return true if has conaction and false otherwise
      */
     public boolean hasConaction() {
         return hasconaction;
     }
 
     /**
      * Init xml reader used for pipeline parsing.
      * 
      * @param ditaDir absolute path to DITA-OT directory
      * @param validate whether validate input file
      * @param rootFile absolute path to input file
      * @throws SAXException parsing exception
      * @throws IOException if getting canonical file path fails
      */
     public void initXMLReader(final File ditaDir, final boolean validate, final File rootFile,
             final boolean arg_setSystemid) throws SAXException, IOException {
         // to check whether the current parsing file's href value is out of inputmap.dir
         rootDir = rootFile.getParentFile().getCanonicalFile();
         rootFilePath = rootFile.getCanonicalFile();
         reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
         if (validate == true) {
             reader.setFeature(FEATURE_VALIDATION, true);
             try {
                 reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
             } catch (final SAXNotRecognizedException e) {
                 // Not Xerces, ignore exception
             }
         } else {
             final String msg = MessageUtils.getInstance().getMessage("DOTJ037W").toString();
             logger.logWarn(msg);
         }
         setGrammarPool(reader);
 
         CatalogUtils.setDitaDir(ditaDir);
         setSystemid = arg_setSystemid;
 
         reader.setEntityResolver(CatalogUtils.getCatalogResolver());
     }
 
     /**
      * 
      * Reset the internal variables.
      */
     public void reset() {
         hasKeyRef = false;
         hasConRef = false;
         hasHref = false;
         hasCodeRef = false;
         currentDir = null;
         insideExcludedElement = false;
         excludedLevel = 0;
         foreignLevel = 0;
         chunkLevel = 0;
         relTableLevel = 0;
         chunkToNavLevel = 0;
         topicGroupLevel = 0;
         isValidInput = false;
         hasconaction = false;
         nonConrefCopytoTargets.clear();
         hrefTargets.clear();
         hrefTopicSet.clear();
         chunkTopicSet.clear();
         conrefTargets.clear();
         copytoMap.clear();
         ignoredCopytoSourceSet.clear();
         outDitaFilesSet.clear();
         keysDefMap.clear();
         keysRefMap.clear();
         exKeysDefMap.clear();
         schemeSet.clear();
         schemeRefSet.clear();
         level = 0;
         topicrefStack.clear();
         processRoleLevel = 0;
         processRoleStack.clear();
         // Don't clean resourceOnlySet por crossSet
     }
 
     /**
      * Parse input xml file.
      * 
      * @param file file
      * @throws SAXException SAXException
      * @throws IOException IOException
      * @throws FileNotFoundException FileNotFoundException
      */
     public void parse(final File file) throws FileNotFoundException, IOException, SAXException {
         currentFile = file.getAbsoluteFile();
         reader.setErrorHandler(new DITAOTXMLErrorHandler(file.getName(), logger));
         final InputSource is = new InputSource(new FileInputStream(file));
         // Set the system ID
         if (setSystemid) {
             // is.setSystemId(URLUtil.correct(file).toString());
             is.setSystemId(file.toURI().toURL().toString());
         }
         reader.parse(is);
     }
 
     /**
      * Check if the current file is a ditamap with
      * "@processing-role=resource-only".
      */
     @Override
     public void startDocument() throws SAXException {
         final File href = FileUtils.getRelativePath(rootFilePath.getAbsoluteFile(), currentFile.getAbsoluteFile());
         if (FileUtils.isDITAMapFile(currentFile.getName()) && resourceOnlySet.contains(href)
                 && !crossSet.contains(href)) {
             processRoleLevel++;
             processRoleStack.push(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
         }
     }
 
     @Override
     public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
             throws SAXException {
         String domains = null;
         final String processingRole = atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
         final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
         final String scope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
         if (processingRole != null) {
             processRoleStack.push(processingRole);
             processRoleLevel++;
             if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
             } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                 if (href != null) {
                     resourceOnlySet.add(FileUtils.resolveFile(currentDir, toFile(href).getPath()));
                 }
             } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processingRole)) {
                 if (href != null) {
                     crossSet.add(FileUtils.resolveFile(currentDir, toFile(href).getPath()));
                 }
             }
         } else if (processRoleLevel > 0) {
             processRoleLevel++;
             if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
             } else if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processRoleStack.peek())) {
                 if (href != null) {
                     resourceOnlySet.add(FileUtils.resolveFile(currentDir, toFile(href).getPath()));
                 }
             } else if (ATTR_PROCESSING_ROLE_VALUE_NORMAL.equals(processRoleStack.peek())) {
                 if (href != null) {
                     crossSet.add(FileUtils.resolveFile(currentDir, toFile(href).getPath()));
                 }
             }
         } else {
             if (href != null) {
                 crossSet.add(FileUtils.resolveFile(currentDir, toFile(href).getPath()));
             }
         }
 
         final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
 
         // has class attribute
         if (classValue != null) {
 
             // when meets topic tag
             if (TOPIC_TOPIC.matches(classValue)) {
                 topicId = atts.getValue(ATTRIBUTE_NAME_ID);
                 // relpace place holder with first topic id
                 // Get relative file name
                 final String filename = FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(),
                         currentFile.getAbsolutePath());
                 for (final ExportAnchor e : resultList) {
                     if (e.topicids.contains(filename + QUESTION)) {
                         e.topicids.add(topicId);
                         e.topicids.remove(filename + QUESTION);
                     }
                 }
 
             }
             // WEK: As of 14 Dec 2009, transtype is sometimes null, not sure
             // under what conditions.
             // System.out.println(" + [DEBUG] transtype=" + transtype);
             // get plugin id only transtype = eclipsehelp
             if (FileUtils.isDITAMapFile(currentFile.getName()) && rootFilePath.equals(currentFile)
                     && MAP_MAP.matches(classValue) && INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
                 String pluginId = atts.getValue(ATTRIBUTE_NAME_ID);
                 if (pluginId == null) {
                     pluginId = "org.sample.help.doc";
                 }
                 final Set<String> set = StringUtils.restoreSet(pluginId);
                 pluginMap.put("pluginId", set);
             }
 
             // merge multiple exportanchors into one
             // Each <topicref> can only have one <topicmeta>.
             // Each <topic> can only have one <prolog>
             // and <metadata> can have more than one exportanchors
             if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
                 if (MAP_TOPICMETA.matches(classValue) || TOPIC_PROLOG.matches(classValue)) {
                     topicMetaSet.add(qName);
                 }
                 // If the file has <exportanchors> tags only transtype = eclipsehelp
                 if (DELAY_D_EXPORTANCHORS.matches(classValue)) {
                     hasExport = true;
                     // If current file is a ditamap file
                     if (FileUtils.isDITAMapFile(currentFile.getName())) {
                         // if dita file's extension name is ".xml"
                         URI editedHref = null;
                         if (topicHref != null && topicHref.getPath().endsWith(FILE_EXTENSION_XML)) {
                             // change the extension to ".dita" for latter compare
                             editedHref = toURI(topicHref.toString().replace(FILE_EXTENSION_XML, FILE_EXTENSION_DITA));
                         } else {
                             editedHref = topicHref;
                         }
                         // editedHref = editedHref.replace(File.separator, "/");
                         currentExportAnchor = new ExportAnchor(editedHref);
                         // if <exportanchors> is defined in topicmeta(topicref), there is only one topic id
                         currentExportAnchor.topicids.add(topicId);
                         // If current file is topic file
                     } else if (FileUtils.isDITATopicFile(currentFile.getName())) {
                         URI filename = toURI(FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(),
                                 currentFile.getAbsolutePath()));
                         // if dita file's extension name is ".xml"
                         if (filename.toString().endsWith(FILE_EXTENSION_XML)) {
                             // change the extension to ".dita" for latter compare
                             filename = toURI(filename.toString().replace(FILE_EXTENSION_XML, FILE_EXTENSION_DITA));
                         }
                         currentExportAnchor = new ExportAnchor(filename);
                         // if <exportanchors> is defined in metadata(topic), there can be many topic ids
                         currentExportAnchor.topicids.add(topicId);
                         shouldAppendEndTag = true;
                     }
                     // meet <anchorkey> tag
                 } else if (DELAY_D_ANCHORKEY.matches(classValue)) {
                     // create keyref element in the StringBuffer
                     // TODO in topic file is no keys
                     final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
                     currentExportAnchor.keys.add(keyref);
                     // meet <anchorid> tag
                 } else if (DELAY_D_ANCHORID.matches(classValue)) {
                     // create keyref element in the StringBuffer
                     final String id = atts.getValue(ATTRIBUTE_NAME_ID);
                     // If current file is a ditamap file
                     // The id can only be element id within a topic
                     if (FileUtils.isDITAMapFile(currentFile.getName())) {
                         // id shouldn't be same as topic id in the case of duplicate insert
                         if (!topicId.equals(id)) {
                             currentExportAnchor.ids.add(id);
                         }
                     } else if (FileUtils.isDITATopicFile(currentFile.getName())) {
                         // id shouldn't be same as topic id in the case of duplicate insert
                         if (!topicId.equals(id)) {
                             // topic id found
                             currentExportAnchor.ids.add(id);
                         }
                     }
                 }
             }
         }
 
         // Generate Scheme relationship graph
         if (SUBJECTSCHEME_SUBJECTSCHEME.matches(classValue)) {
             if (schemeRelationGraph == null) {
                 schemeRelationGraph = new LinkedHashMap<File, Set<File>>();
             }
             // Make it easy to do the BFS later.
             Set<File> children = schemeRelationGraph.get(new File("ROOT"));
             if (children == null || children.isEmpty()) {
                 children = new LinkedHashSet<File>();
             }
             children.add(currentFile.getAbsoluteFile());
             schemeRelationGraph.put(new File("ROOT"), children);
             schemeRefSet.add(FileUtils.getRelativePath(rootFilePath.getAbsoluteFile(),
                     currentFile.getAbsoluteFile()));
         } else if (SUBJECTSCHEME_SCHEMEREF.matches(classValue)) {
             Set<File> children = schemeRelationGraph.get(currentFile.getAbsoluteFile());
             if (children == null) {
                 children = new LinkedHashSet<File>();
                 schemeRelationGraph.put(currentFile.getAbsoluteFile(), children);
             }
             if (href != null) {
                 children.add(FileUtils.resolveFile(rootDir.getAbsoluteFile(), toFile(href).getPath()));
             }
         }
 
         if (foreignLevel > 0) {
             // if it is an element nested in foreign/unknown element
             // do not parse it
             foreignLevel++;
             return;
         } else if (classValue != null && (TOPIC_FOREIGN.matches(classValue) || TOPIC_UNKNOWN.matches(classValue))) {
             foreignLevel++;
         }
 
         if (chunkLevel > 0) {
             chunkLevel++;
         } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null) {
             chunkLevel++;
         }
         if (relTableLevel > 0) {
             relTableLevel++;
         } else if (classValue != null && MAP_RELTABLE.matches(classValue)) {
             relTableLevel++;
         }
 
         if (chunkToNavLevel > 0) {
             chunkToNavLevel++;
         } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null
                 && atts.getValue(ATTRIBUTE_NAME_CHUNK).indexOf("to-navigation") != -1) {
             chunkToNavLevel++;
         }
 
         if (topicGroupLevel > 0) {
             topicGroupLevel++;
         } else if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null
                 && atts.getValue(ATTRIBUTE_NAME_CLASS).contains(MAPGROUP_D_TOPICGROUP.matcher)) {
             topicGroupLevel++;
         }
 
         if (classValue == null && !ELEMENT_NAME_DITA.equals(localName)) {
             logger.logInfo(MessageUtils.getInstance().getMessage("DOTJ030I", localName).toString());
         }
 
         if (classValue != null && TOPIC_TOPIC.matches(classValue)) {
             domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
             if (domains == null) {
                 logger.logInfo(MessageUtils.getInstance().getMessage("DOTJ029I", localName).toString());
             } else {
                 props = StringUtils.getExtProps(domains);
             }
         }
 
         if (insideExcludedElement) {
             ++excludedLevel;
             return;
         }
 
         // Ignore element that has been filtered out.
         if (filterUtils.needExclude(atts, props)) {
             insideExcludedElement = true;
             ++excludedLevel;
             return;
         }
 
         /*
          * For ditamap, set it to valid if element <map> or extended from <map>
          * was found, this kind of element's class attribute must contains
          * 'map/map'; For topic files, set it to valid if element <title> or
          * extended from <title> was found, this kind of element's class
          * attribute must contains 'topic/title'.
          */
 
         if (classValue != null) {
             if ((MAP_MAP.matches(classValue)) || (TOPIC_TITLE.matches(classValue))) {
                 isValidInput = true;
             } else if (TOPIC_OBJECT.matches(classValue)) {
                 parseAttribute(atts, ATTRIBUTE_NAME_DATA);
             }
         }
 
         // onlyTopicInMap is on.
         topicref: if (job.getOnlyTopicInMap() && this.canResolved()) {
             // topicref(only defined in ditamap file.)
             if (MAP_TOPICREF.matches(classValue)) {
 
                 // get href attribute value.
                 final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
 
                 // get conref attribute value.
                 final URI conrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
 
                 // has href attribute and refer to ditamap file.
                 if (hrefValue != null && !hrefValue.toString().isEmpty()) {
                     // exclude external resources
                     final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                     if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                             || hrefValue.toString().indexOf(COLON_DOUBLE_SLASH) != -1 || hrefValue.toString().startsWith(SHARP)) {
                         break topicref;
                     }
                     // normalize href value.
                     final File target = toFile(hrefValue);
                     // caculate relative path for href value.
                     String fileName = null;
                     if (target.isAbsolute()) {
                         fileName = FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(), toFile(hrefValue).getPath());
                     }
                     fileName = FileUtils.normalizeDirectory(currentDir, toFile(hrefValue).getPath()).getPath();
                     // change '\' to '/' for comparsion.
                     fileName = FileUtils.separatorsToUnix(fileName);
 
                     final boolean canParse = parseBranch(atts, hrefValue, fileName);
                     if (!canParse) {
                         break topicref;
                     } else {
                         topicrefStack.push(localName);
                     }
 
                 } else if (conrefValue != null && !conrefValue.toString().isEmpty()) {
 
                     // exclude external resources
                     final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                     if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                             || conrefValue.toString().indexOf(COLON_DOUBLE_SLASH) != -1 || conrefValue.toString().startsWith(SHARP)) {
                         break topicref;
                     }
                     // normalize href value.
                     final File target = new File(conrefValue);
                     // caculate relative path for href value.
                     String fileName = null;
                     if (target.isAbsolute()) {
                         fileName = FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(), toFile(conrefValue).getPath());
                     }
                     fileName = FileUtils.normalizeDirectory(currentDir, toFile(conrefValue).getPath()).getPath();
 
                     // change '\' to '/' for comparsion.
                     fileName = FileUtils.separatorsToUnix(fileName);
 
                     final boolean canParse = parseBranch(atts, conrefValue, fileName);
                     if (!canParse) {
                         break topicref;
                     } else {
                         topicrefStack.push(localName);
                     }
                 }
             }
         }
 
         parseAttribute(atts, ATTRIBUTE_NAME_CONREF);
         parseAttribute(atts, ATTRIBUTE_NAME_HREF);
         parseAttribute(atts, ATTRIBUTE_NAME_COPY_TO);
         parseAttribute(atts, ATTRIBUTE_NAME_IMG);
         parseAttribute(atts, ATTRIBUTE_NAME_CONACTION);
         parseAttribute(atts, ATTRIBUTE_NAME_KEYS);
         parseAttribute(atts, ATTRIBUTE_NAME_CONKEYREF);
         parseAttribute(atts, ATTRIBUTE_NAME_KEYREF);
 
     }
 
     @Override
     public void endElement(final String uri, final String localName, final String qName) throws SAXException {
         // @processing-role
         if (processRoleLevel > 0) {
             if (processRoleLevel == processRoleStack.size()) {
                 processRoleStack.pop();
             }
             processRoleLevel--;
         }
         if (foreignLevel > 0) {
             foreignLevel--;
             return;
         }
         if (chunkLevel > 0) {
             chunkLevel--;
         }
         if (relTableLevel > 0) {
             relTableLevel--;
         }
         if (chunkToNavLevel > 0) {
             chunkToNavLevel--;
         }
         if (topicGroupLevel > 0) {
             topicGroupLevel--;
         }
         if (insideExcludedElement) {
             // end of the excluded element, mark the flag as false
             if (excludedLevel == 1) {
                 insideExcludedElement = false;
             }
             --excludedLevel;
         }
         // <exportanchors> over should write </file> tag
 
         if (topicMetaSet.contains(qName) && hasExport) {
             // If current file is a ditamap file
             if (FileUtils.isDITAMapFile(currentFile.getName())) {
                 resultList.add(currentExportAnchor);
                 currentExportAnchor = null;
                 // If current file is topic file
             }
             hasExport = false;
             topicMetaSet.clear();
         }
 
         if (!topicrefStack.isEmpty() && localName.equals(topicrefStack.peek())) {
             level--;
             topicrefStack.pop();
         }
     }
 
     /**
      * Clean up.
      */
     @Override
     public void endDocument() throws SAXException {
         if (processRoleLevel > 0) {
             processRoleLevel--;
             processRoleStack.pop();
         }
         if (FileUtils.isDITATopicFile(currentFile.getName()) && shouldAppendEndTag) {
             resultList.add(currentExportAnchor);
             currentExportAnchor = null;
             // should reset
             shouldAppendEndTag = false;
         }
         checkMultiLevelKeys(keysDefMap, keysRefMap);
     }
 
     /**
      * Method for see whether a branch should be parsed.
      * 
      * @param atts {@link Attributes}
      * @param hrefValue {@link String}
      * @param fileName normalized file name(remove '#')
      * @return boolean
      */
     private boolean parseBranch(final Attributes atts, final URI hrefValue, final String fileName) {
         // current file is primary ditamap file.
         // parse every branch.
         final String currentFileRelative = FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(),
                 currentFile.getAbsolutePath());
         if (currentDir == null && currentFileRelative.equals(primaryDitamap)) {
             // add branches into map
             addReferredBranches(hrefValue, fileName);
             return true;
         } else {
             // current file is a sub-ditamap one.
             // get branch's id
             final String id = atts.getValue(ATTRIBUTE_NAME_ID);
             // this branch is not referenced
             if (level == 0 && StringUtils.isEmptyString(id)) {
                 // There is occassion that the whole ditamap should be parsed
                 final boolean found = searchBrachesMap(id);
                 if (found) {
                     // Add this branch into map for parsing.
                     addReferredBranches(hrefValue, fileName);
                     // update level
                     level++;
                     return true;
                 } else {
                     return false;
                 }
                 // this brach is a decendent of a referenced one
             } else if (level != 0) {
                 // Add this branch into map for parsing.
                 addReferredBranches(hrefValue, fileName);
                 // update level
                 level++;
                 return true;
                 // This branch has an id but is a new one
             } else if (!StringUtils.isEmptyString(id)) {
                 // search branches map.
                 final boolean found = searchBrachesMap(id);
                 // branch is referenced
                 if (found) {
                     // Add this branch into map for parsing.
                     addReferredBranches(hrefValue, fileName);
                     // update level
                     level++;
                     return true;
                 } else {
                     // this branch is not referenced
                     return false;
                 }
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Search braches map with branch id and current file name.
      * 
      * @param id String branch id.
      * @return boolean true if found and false otherwise.
      */
     private boolean searchBrachesMap(final String id) {
         // caculate relative path for current file.
         final String currentFileRelative = FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(),
                 currentFile.getAbsolutePath());
         // seach the map with id & current file name.
         if (vaildBranches.containsKey(currentFileRelative)) {
             final List<String> branchIdList = vaildBranches.get(currentFileRelative);
             // the branch is referenced.
             if (branchIdList.contains(id)) {
 
                 return true;
             } else if (branchIdList.size() == 0) {
                 // the whole map is referenced
 
                 return true;
             } else {
                 // the branch is not referred
                 return false;
             }
         } else {
             // current file is not refered
             return false;
         }
     }
 
     /**
      * Add branches into map.
      * 
      * @param hrefValue
      * @param fileName
      */
     private void addReferredBranches(final URI hrefValue, final String fileName) {
         final String branchId = hrefValue.getFragment();
         // href value has branch id.
         if (branchId != null) {
             // The map contains the file name
             if (vaildBranches.containsKey(fileName)) {
                 final List<String> branchIdList = vaildBranches.get(fileName);
                 branchIdList.add(branchId);
             } else {
                 final List<String> branchIdList = new ArrayList<String>();
                 branchIdList.add(branchId);
                 vaildBranches.put(fileName, branchIdList);
             }
             // href value has no branch id
         } else {
             vaildBranches.put(fileName, new ArrayList<String>());
         }
     }
 
     /**
      * Parse the input attributes for needed information.
      * 
      * @param atts all attributes
      * @param attrName attributes to process
      */
     private void parseAttribute(final Attributes atts, final String attrName) throws SAXException {
         String attrValue = atts.getValue(attrName);
         String filename = null;
         final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
         final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
         final String attrType = atts.getValue(ATTRIBUTE_NAME_TYPE);
 
         final String codebase = atts.getValue(ATTRIBUTE_NAME_CODEBASE);
 
         if (attrValue == null) {
             return;
         }
 
         // @conkeyref will be resolved to @conref in Debug&Fileter step
         if (ATTRIBUTE_NAME_CONREF.equals(attrName) || ATTRIBUTE_NAME_CONKEYREF.equals(attrName)) {
             hasConRef = true;
         } else if (ATTRIBUTE_NAME_HREF.equals(attrName)) {
             if (attrClass != null && PR_D_CODEREF.matches(attrClass)) {
                 // if current element is <coderef> or its specialization
                 // set hasCodeRef to true
                 hasCodeRef = true;
             } else {
                 hasHref = true;
             }
         } else if (ATTRIBUTE_NAME_KEYREF.equals(attrName)) {
             hasKeyRef = true;
         }
 
         // collect the key definitions
         if (ATTRIBUTE_NAME_KEYS.equals(attrName) && !attrValue.isEmpty()) {
             URI target = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
 
             final String keyRef = atts.getValue(ATTRIBUTE_NAME_KEYREF);
 
             final URI copy_to = toURI(atts.getValue(ATTRIBUTE_NAME_COPY_TO));
             if (copy_to != null && !copy_to.toString().isEmpty()) {
                 target = copy_to;
             }
             // avoid NullPointException
 //            if (target == null) {
 //                target = "";
 //            }
             // store the target
             final URI temp = target;
 
             // Many keys can be defined in a single definition, like
             // keys="a b c", a, b and c are seperated by blank.
             for (final String key : attrValue.split(" ")) {
                 if (!keysDefMap.containsKey(key) && !key.equals("")) {
                     if (target != null && !target.toString().isEmpty()) {
                         if (attrScope != null && (attrScope.equals(ATTR_SCOPE_VALUE_EXTERNAL) || attrScope.equals(ATTR_SCOPE_VALUE_PEER))) {
                             // store external or peer resources.
                             exKeysDefMap.put(key, target);
                             keysDefMap.put(key, new KeyDef(key, target, attrScope, null));
                         } else {
                             String tail = "";
                             if (target.getFragment() != null) {
                                 tail = SHARP + target.getFragment();
                                 target = stripFragment(target);
                             }
                             if (toFile(target).isAbsolute()) {
                                 target = toURI(FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(), target.getPath()));
                             }
                             target = toURI(FileUtils.separatorsToUnix(FileUtils.normalizeDirectory(currentDir, target.getPath()).getPath()));
                             keysDefMap.put(key, new KeyDef(key, toURI(target + tail), ATTR_SCOPE_VALUE_LOCAL, null));
                         }
                     } else if (!StringUtils.isEmptyString(keyRef)) {
                         // store multi-level keys.
                         keysRefMap.put(key, keyRef);
                     } else {
                         // target is null or empty, it is useful in the future
                         // when consider the content of key definition
                         keysDefMap.put(key, new KeyDef(key, (URI) null, (String) null, (URI) null));
                     }
                 } else {
                     logger.logInfo(MessageUtils.getInstance().getMessage("DOTJ045I", key, target != null ? target.toString() : null).toString());
                 }
                 // restore target
                 target = temp;
             }
         }
 
         // external resource is filtered here.
         if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                 || attrValue.indexOf(COLON_DOUBLE_SLASH) != -1 || attrValue.startsWith(SHARP)) {
             return;
         }
         if (attrValue.startsWith("file:/") && attrValue.indexOf("file://") == -1) {
             attrValue = attrValue.substring("file:/".length());
             // Unix like OS
             if (UNIX_SEPARATOR.equals(File.separator)) {
                 attrValue = UNIX_SEPARATOR + attrValue;
             }
         } else if (attrValue.startsWith("file:") && !attrValue.startsWith("file:/")) {
             attrValue = attrValue.substring("file:".length());
         }
         final File target = new File(attrValue);
         if (target.isAbsolute() && !ATTRIBUTE_NAME_DATA.equals(attrName)) {
             attrValue = FileUtils.getRelativeUnixPath(rootFilePath.getAbsolutePath(), attrValue);
             // for object tag bug:3052156
         } else if (ATTRIBUTE_NAME_DATA.equals(attrName)) {
             if (!StringUtils.isEmptyString(codebase)) {
                 filename = FileUtils.normalizeDirectory(codebase, attrValue).getPath();
             } else {
                 filename = FileUtils.normalizeDirectory(currentDir, attrValue).getPath();
             }
         } else {
             // noraml process.
             filename = FileUtils.normalizeDirectory(currentDir, attrValue).getPath();
         }
 
         filename = toFile(filename).getPath();
         // XXX: At this point, filename should be a system path
 
         if (MAP_TOPICREF.matches(attrClass)) {
             if (ATTR_TYPE_VALUE_SUBJECT_SCHEME.equalsIgnoreCase(attrType)) {
                 schemeSet.add(new File(filename));
             }
             // only transtype = eclipsehelp
             if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
                 // For only format of the href is dita topic
                 if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)) {
                     if (attrName.equals(ATTRIBUTE_NAME_HREF)) {
                         topicHref = toURI(filename);
                         // attrValue has topicId
                         if (attrValue.lastIndexOf(SHARP) != -1) {
                             // get the topicId position
                             final int position = attrValue.lastIndexOf(SHARP);
                             topicId = attrValue.substring(position + 1);
                         } else {
                             // get the first topicId(vaild href file)
                             if (FileUtils.isDITAFile(topicHref.getPath())) {
                                 // topicId =
                                 // MergeUtils.getInstance().getFirstTopicId(topicHref,
                                 // (new File(rootFilePath)).getParent(), true);
                                 // to be unique
                                 topicId = topicHref + QUESTION;
                             }
                         }
                     }
                 } else {
                     topicHref = null;
                     topicId = null;
                 }
             }
        } else if (TOPIC_IMAGE.matches(attrClass)) {
            if (attrFormat == null) {
                attrFormat = "image";
            }
         }
         // files referred by coderef won't effect the uplevels, code has already returned.
         if (("DITA-foreign".equals(attrType) && ATTRIBUTE_NAME_DATA.equals(attrName)) || attrClass != null && PR_D_CODEREF.matches(attrClass)) {
             subsidiarySet.add(new File(filename));
             return;
         }
 
         // Collect non-conref and non-copyto targets
         if (filename != null
                 && FileUtils.isValidTarget(filename.toLowerCase())
                 && (StringUtils.isEmptyString(atts.getValue(ATTRIBUTE_NAME_COPY_TO))
                         || !FileUtils.isDITATopicFile(atts.getValue(ATTRIBUTE_NAME_COPY_TO).toLowerCase()) || (atts
                         .getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(
                         "to-content"))) && !ATTRIBUTE_NAME_CONREF.equals(attrName)
                 && !ATTRIBUTE_NAME_COPY_TO.equals(attrName)
                 && (canResolved() || FileUtils.isSupportedImageFile(filename.toLowerCase()))) {
             nonConrefCopytoTargets.add(new Reference(filename, attrFormat));
             // nonConrefCopytoTargets.add(filename);
         }
         // outside ditamap files couldn't cause warning messages, it is stopped here
         if (attrFormat != null && !ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)) {
             // The format of the href is not dita topic
             // The logic after this "if" clause is not related to files other than dita topic.
             // Therefore, we need to return here to filter out those files in other format.
             return;
         }
 
         /*
          * Collect only href target topic files for index extracting.
          */
         if (ATTRIBUTE_NAME_HREF.equals(attrName) && FileUtils.isDITATopicFile(filename) && canResolved()) {
             hrefTargets.add(new File(filename));
             toOutFile(new File(filename));
             if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0 && relTableLevel == 0) {
                 chunkTopicSet.add(new File(filename));
             } else {
                 hrefTopicSet.add(new File(filename));
             }
         }
 
         // Collect only conref target topic files
         if (ATTRIBUTE_NAME_CONREF.equals(attrName) && FileUtils.isDITAFile(filename)) {
             conrefTargets.add(new File(filename));
             toOutFile(new File(filename));
         }
 
         // Collect copy-to (target,source) into hash map
         if (ATTRIBUTE_NAME_COPY_TO.equals(attrName) && FileUtils.isDITATopicFile(filename)) {
             final URI href = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
             if (href != null) {
                 final File value = FileUtils.normalizeDirectory(currentDir, toFile(href).getPath());
     
                 if (href == null || href.toString().isEmpty()) {
                     final StringBuffer buff = new StringBuffer();
                     buff.append("[WARN]: Copy-to task [href=\"\" copy-to=\"");
                     buff.append(filename);
                     buff.append("\"] was ignored.");
                     logger.logWarn(buff.toString());
                 } else if (copytoMap.get(new File(filename)) != null) {
                     if (!value.equals(copytoMap.get(new File(filename)))) {
                         logger.logWarn(MessageUtils.getInstance().getMessage("DOTX065W", href.toString(), filename).toString());
                     }
                     ignoredCopytoSourceSet.add(toFile(href));
                 } else if (!(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(
                         "to-content"))) {
                     copytoMap.put(new File(filename), value);
                 }
             }
             
             final File pathWithoutID = FileUtils.resolveFile(currentDir, toFile(attrValue).getPath());
             if (chunkLevel > 0 && chunkToNavLevel == 0 && topicGroupLevel == 0) {
                 chunkTopicSet.add(pathWithoutID);
             } else {
                 hrefTopicSet.add(pathWithoutID);
             }
 
         }
         // Collect the conaction source topic file
         if (ATTRIBUTE_NAME_CONACTION.equals(attrName)) {
             if (attrValue.equals("mark") || attrValue.equals("pushreplace")) {
                 hasconaction = true;
             }
         }
     }
 //
 //    /**
 //     * Convert URI references to file paths.
 //     * 
 //     * @param filename file reference
 //     * @return file path
 //     */
 //    private String toFile(final String filename) {
 //        if (filename == null) {
 //            return null;
 //        }
 //        String f = filename;
 //        try {
 //            f = URLDecoder.decode(filename, UTF8);
 //        } catch (final UnsupportedEncodingException e) {
 //            throw new RuntimeException(e);
 //        }
 //        if (processingMode == Mode.LAX) {
 //            f = f.replace(WINDOWS_SEPARATOR, File.separator);
 //        }
 //        f = f.replace(URI_SEPARATOR, File.separator);
 //        return f;
 //    }
 
     /**
      * Get multi-level keys list
      */
     private List<String> getKeysList(final String key, final Map<String, String> keysRefMap) {
         final List<String> list = new ArrayList<String>();
         // Iterate the map to look for multi-level keys
         final Iterator<Entry<String, String>> iter = keysRefMap.entrySet().iterator();
         while (iter.hasNext()) {
             final Map.Entry<String, String> entry = iter.next();
             // Multi-level key found
             if (entry.getValue().equals(key)) {
                 // add key into the list
                 final String entryKey = entry.getKey();
                 list.add(entryKey);
                 // still have multi-level keys
                 if (keysRefMap.containsValue(entryKey)) {
                     // rescuive point
                     final List<String> tempList = getKeysList(entryKey, keysRefMap);
                     list.addAll(tempList);
                 }
             }
         }
         return list;
     }
 
     /**
      * Update keysDefMap for multi-level keys
      */
     private void checkMultiLevelKeys(final Map<String, KeyDef> keysDefMap, final Map<String, String> keysRefMap) {
         String key = null;
         KeyDef value = null;
         // tempMap storing values to avoid ConcurrentModificationException
         final Map<String, KeyDef> tempMap = new HashMap<String, KeyDef>();
         final Iterator<Entry<String, KeyDef>> iter = keysDefMap.entrySet().iterator();
         while (iter.hasNext()) {
             final Map.Entry<String, KeyDef> entry = iter.next();
             key = entry.getKey();
             value = entry.getValue();
             // there is multi-level keys exist.
             if (keysRefMap.containsValue(key)) {
                 // get multi-level keys
                 final List<String> keysList = getKeysList(key, keysRefMap);
                 for (final String multikey : keysList) {
                     // update tempMap
                     tempMap.put(multikey, value);
                 }
             }
         }
         // update keysDefMap.
         keysDefMap.putAll(tempMap);
     }
 
     /**
      * Check if path walks up in parent directories
      * 
      * @param toCheckPath path to check
      * @return {@code true} if path walks up, otherwise {@code false}
      */
     private boolean isOutFile(final File toCheckPath) {
         if (!toCheckPath.getPath().startsWith("..")) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check if {@link #currentFile} is a map
      * 
      * @return {@code} true if file is map, otherwise {@code false}
      */
     private boolean isMapFile() {
         final String current = FileUtils.normalize(currentFile.getAbsolutePath()).getPath();
         if (FileUtils.isDITAMapFile(current)) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean canResolved() {
         if ((job.getOnlyTopicInMap() == false) || isMapFile()) {
             return true;
         } else {
             return false;
         }
     }
 
     private void addToOutFilesSet(final File hrefedFile) {
         if (canResolved()) {
             outDitaFilesSet.add(hrefedFile);
         }
     }
 
     private void toOutFile(final File filename) throws SAXException {
         // the filename is a relative path from the dita input file
         final String[] prop = { FileUtils.normalizeDirectory(rootDir.getAbsolutePath(), filename.getPath()).getPath(), FileUtils.normalize(currentFile.getAbsolutePath()).getPath() };
         if ((job.getGeneratecopyouter() == Job.Generate.NOT_GENERATEOUTTER)
                 || (job.getGeneratecopyouter() == Job.Generate.GENERATEOUTTER)) {
             if (isOutFile(filename)) {
                 if (job.getOutterControl() == Job.OutterControl.FAIL) {
                     final MessageBean msgBean = MessageUtils.getInstance().getMessage("DOTJ035F", prop);
                     throw new SAXParseException(null, null, new DITAOTException(msgBean, null, msgBean.toString()));
                 } else if (job.getOutterControl() == Job.OutterControl.WARN) {
                     final String message = MessageUtils.getInstance().getMessage("DOTJ036W", prop).toString();
                     logger.logWarn(message);
                 }
                 addToOutFilesSet(filename);
             }
         }
     }
 
     /**
      * File reference with path and optional format.
      */
     public static class Reference {
         public final String filename;
         public final String format;
 
         public Reference(final String filename, final String format) {
             this.filename = filename;
             this.format = format;
         }
 
         public Reference(final String filename) {
             this(filename, null);
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + ((filename == null) ? 0 : filename.hashCode());
             result = prime * result + ((format == null) ? 0 : format.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof Reference)) {
                 return false;
             }
             final Reference other = (Reference) obj;
             if (filename == null) {
                 if (other.filename != null) {
                     return false;
                 }
             } else if (!filename.equals(other.filename)) {
                 return false;
             }
             if (format == null) {
                 if (other.format != null) {
                     return false;
                 }
             } else if (!format.equals(other.format)) {
                 return false;
             }
             return true;
         }
     }
 
     public static class ExportAnchor {
         public final URI file;
         public final Set<String> topicids = new HashSet<String>();
         public final Set<String> keys = new HashSet<String>();
         public final Set<String> ids = new HashSet<String>();
 
         ExportAnchor(final URI file) {
             this.file = file;
         }
     }
 
 }

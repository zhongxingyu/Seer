 /*
  * This file is part of the DITA Open Toolkit project hosted on
  * Sourceforge.net. See the accompanying license.txt file for
  * applicable licenses.
  */
 
 /*
  * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
  */
 package org.dita.dost.module;
 
 import static org.dita.dost.util.Constants.*;
 import static org.dita.dost.util.Job.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.dita.dost.exception.DITAOTException;
 import org.dita.dost.log.DITAOTLogger;
 import org.dita.dost.log.MessageBean;
 import org.dita.dost.log.MessageUtils;
 import org.dita.dost.pipeline.AbstractPipelineInput;
 import org.dita.dost.pipeline.AbstractPipelineOutput;
 import org.dita.dost.reader.DitaValReader;
 import org.dita.dost.reader.GenListModuleReader;
 import org.dita.dost.reader.GrammarPoolManager;
 import org.dita.dost.util.DelayConrefUtils;
 import org.dita.dost.util.FileUtils;
 import org.dita.dost.util.FilterUtils;
 import org.dita.dost.util.Job;
 import org.dita.dost.util.OutputUtils;
 import org.dita.dost.util.StringUtils;
 import org.dita.dost.util.TimingUtils;
 import org.dita.dost.util.XMLSerializer;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * This class extends AbstractPipelineModule, used to generate map and topic
  * list by parsing all the refered dita files.
  * 
  * @version 1.0 2004-11-25
  * 
  * @author Wu, Zhi Qiang
  */
 public final class GenMapAndTopicListModule implements AbstractPipelineModule {
 
     private static final String ELEMENT_STUB = "stub";
     private static final String ATTRIUBTE_SOURCE = "source";
     private static final String ATTRIBUTE_HREF = "href";
     private static final String ATTRIBUTE_KEYS = "keys";
     private static final String ELEMENT_KEYDEF = "keydef";
 
     /** Set of all dita files */
     private final Set<String> ditaSet;
 
     /** Set of all topic files */
     private final Set<String> fullTopicSet;
 
     /** Set of all map files */
     private final Set<String> fullMapSet;
 
     /** Set of topic files containing href */
     private final Set<String> hrefTopicSet;
 
     /** Set of href topic files with anchor ID */
     private final Set<String> hrefWithIDSet;
 
     /** Set of chunk topic with anchor ID */
     private final Set<String> chunkTopicSet;
 
     /** Set of map files containing href */
     private final Set<String> hrefMapSet;
 
     /** Set of dita files containing conref */
     private final Set<String> conrefSet;
 
     /** Set of topic files containing coderef */
     private final Set<String> coderefSet;
 
     /** Set of all images */
     private final Set<String> imageSet;
 
     /** Set of all images used for flagging */
     private final Set<String> flagImageSet;
 
     /** Set of all html files */
     private final Set<String> htmlSet;
 
     /** Set of all the href targets */
     private final Set<String> hrefTargetSet;
 
     /** Set of all the conref targets */
     private Set<String> conrefTargetSet;
 
     /** Set of all the copy-to sources */
     private Set<String> copytoSourceSet;
 
     /** Set of all the non-conref targets */
     private final Set<String> nonConrefCopytoTargetSet;
 
     /** Set of sources of those copy-to that were ignored */
     private final Set<String> ignoredCopytoSourceSet;
 
     /** Set of subsidiary files */
     private final Set<String> subsidiarySet;
 
     /** Set of relative flag image files */
     private final Set<String> relFlagImagesSet;
 
     /** Map of all copy-to (target,source) */
     private Map<String, String> copytoMap;
 
     /** List of files waiting for parsing */
     private final List<String> waitList;
 
     /** List of parsed files */
     private final List<String> doneList;
 
     /** Set of outer dita files */
     private final Set<String> outDitaFilesSet;
 
     /** Set of sources of conacion */
     private final Set<String> conrefpushSet;
 
     /** Set of files containing keyref */
     private final Set<String> keyrefSet;
 
     /** Set of files with "@processing-role=resource-only" */
     private final Set<String> resourceOnlySet;
 
     /** Map of all key definitions */
     private final Map<String, KeyDef> keysDefMap;
 
     /** Basedir for processing */
     private String baseInputDir;
 
     /** Tempdir for processing */
     private String tempDir;
 
     /** ditadir for processing */
     private String ditaDir;
 
     private String inputFile;
 
     private String ditavalFile;
 
     private int uplevels = 0;
 
     private String prefix = "";
 
     private DITAOTLogger logger;
 
     private GenListModuleReader reader;
     /** Output utilities */
     private OutputUtils outputUtils;
     private boolean xmlValidate = true;
 
     private String relativeValue;
 
     private String formatRelativeValue;
 
     private String rootFile;
 
     private XMLSerializer keydef;
 
     // keydef file from keys used in schema files
     private XMLSerializer schemekeydef;
 
     // Added by William on 2009-06-25 for req #12014 start
     /** Export file */
     private OutputStreamWriter export;
     // Added by William on 2009-06-25 for req #12014 end
 
     private final Set<String> schemeSet;
 
     private final Map<String, Set<String>> schemeDictionary;
     // Added by William on 2009-07-18 for req #12014 start
     private String transtype;
     // Added by William on 2009-07-18 for req #12014 end
 
     // Added by William on 2010-06-09 for bug:3013079 start
     private final Map<String, String> exKeyDefMap;
     // Added by William on 2010-06-09 for bug:3013079 end
 
     private static final String moduleStartMsg = "GenMapAndTopicListModule.execute(): Starting...";
 
     private static final String moduleEndMsg = "GenMapAndTopicListModule.execute(): Execution time: ";
 
     // Added on 2010-08-24 for bug:2994593 start
     /** use grammar pool cache */
     private boolean gramcache = true;
     // Added on 2010-08-24 for bug:2994593 end
 
     // Added on 2010-08-24 for bug:3086552 start
     private boolean setSystemid = true;
 
     // Added on 2010-08-24 for bug:3086552 end
     /**
      * Create a new instance and do the initialization.
      * 
      * @throws ParserConfigurationException never throw such exception
      * @throws SAXException never throw such exception
      */
     public GenMapAndTopicListModule() throws SAXException, ParserConfigurationException {
         ditaSet = new HashSet<String>(INT_128);
         fullTopicSet = new HashSet<String>(INT_128);
         fullMapSet = new HashSet<String>(INT_128);
         hrefTopicSet = new HashSet<String>(INT_128);
         hrefWithIDSet = new HashSet<String>(INT_128);
         chunkTopicSet = new HashSet<String>(INT_128);
         schemeSet = new HashSet<String>(INT_128);
         hrefMapSet = new HashSet<String>(INT_128);
         conrefSet = new HashSet<String>(INT_128);
         imageSet = new HashSet<String>(INT_128);
         flagImageSet = new LinkedHashSet<String>(INT_128);
         htmlSet = new HashSet<String>(INT_128);
         hrefTargetSet = new HashSet<String>(INT_128);
         subsidiarySet = new HashSet<String>(INT_16);
         waitList = new LinkedList<String>();
         doneList = new LinkedList<String>();
         conrefTargetSet = new HashSet<String>(INT_128);
         nonConrefCopytoTargetSet = new HashSet<String>(INT_128);
         copytoMap = new HashMap<String, String>();
         copytoSourceSet = new HashSet<String>(INT_128);
         ignoredCopytoSourceSet = new HashSet<String>(INT_128);
         outDitaFilesSet = new HashSet<String>(INT_128);
         relFlagImagesSet = new LinkedHashSet<String>(INT_128);
         conrefpushSet = new HashSet<String>(INT_128);
         keysDefMap = new HashMap<String, KeyDef>();
         exKeyDefMap = new HashMap<String, String>();
         keyrefSet = new HashSet<String>(INT_128);
         coderefSet = new HashSet<String>(INT_128);
 
         this.schemeDictionary = new HashMap<String, Set<String>>();
 
         // @processing-role
         resourceOnlySet = new HashSet<String>(INT_128);
     }
 
     public void setLogger(final DITAOTLogger logger) {
         this.logger = logger;
     }
 
     public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
         if (logger == null) {
             throw new IllegalStateException("Logger not set");
         }
         final Date startTime = TimingUtils.getNowTime();
 
         try {
             logger.logInfo(moduleStartMsg);
             parseInputParameters(input);
 
             // set grammar pool flag
             GrammarPoolManager.setGramCache(gramcache);
 
             reader = new GenListModuleReader();
             reader.setLogger(logger);
             reader.initXMLReader(ditaDir, xmlValidate, rootFile, setSystemid);
             final FilterUtils filterUtils = parseFilterFile();
             reader.setFilterUtils(filterUtils);
             reader.setOutputUtils(outputUtils);
 
             addToWaitList(inputFile);
             processWaitList();
             // Depreciated function
             // The base directory does not change according to the referenceing
             // topic files in the new resolution
             updateBaseDirectory();
             refactoringResult();
             outputResult();
             keydef.writeEndDocument();
             keydef.close();
             schemekeydef.writeEndDocument();
             schemekeydef.close();
             // Added by William on 2009-06-25 for req #12014 start
             // write the end tag
             export.write("</stub>");
             // close the steam
             export.close();
             // Added by William on 2009-06-25 for req #12014 end
         } catch (final DITAOTException e) {
             throw e;
         } catch (final SAXException e) {
             throw new DITAOTException(e.getMessage(), e);
         } catch (final Exception e) {
             throw new DITAOTException(e.getMessage(), e);
         } finally {
 
             logger.logInfo(moduleEndMsg + TimingUtils.reportElapsedTime(startTime));
 
         }
 
         return null;
     }
 
     private void parseInputParameters(final AbstractPipelineInput input) {
         final String basedir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
         final String ditaInput = input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP);
 
         tempDir = input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR);
         ditaDir = input.getAttribute(ANT_INVOKER_EXT_PARAM_DITADIR);
         ditavalFile = input.getAttribute(ANT_INVOKER_PARAM_DITAVAL);
         xmlValidate = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE));
 
         // Added by William on 2009-07-18 for req #12014 start
         // get transtype
         transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
         // Added by William on 2009-07-18 for req #12014 start
 
         gramcache = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAM_GRAMCACHE));
         setSystemid = "yes".equalsIgnoreCase(input.getAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID));
 
         // For the output control
         outputUtils = new OutputUtils();
         outputUtils.setGeneratecopyouter(input.getAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER));
         outputUtils.setOutterControl(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL));
         outputUtils.setOnlyTopicInMap(input.getAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP));
 
         // Set the OutputDir
         final File path = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR));
         if (path.isAbsolute()) {
             outputUtils.setOutputDir(path.getAbsolutePath());
         } else {
             throw new IllegalArgumentException("Output directory " + tempDir + " must be absolute");
         }
 
         // Resolve relative paths base on the basedir.
         File inFile = new File(ditaInput);
         if (!inFile.isAbsolute()) {
             // XXX Shouldn't this be resolved to current directory, not Ant script base directory?
             inFile = new File(basedir, ditaInput);
         }
         try {
 			inFile = inFile.getCanonicalFile();
 		} catch (IOException e1) {
 			 logger.logException(e1);
 		}
         if (!new File(tempDir).isAbsolute()) {
             throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
         } else {
             tempDir = FileUtils.removeRedundantNames(tempDir);
         }
         if (!new File(ditaDir).isAbsolute()) {
             throw new IllegalArgumentException("DITA-OT installation directory " + tempDir + " must be absolute");
         } else {
             ditaDir = FileUtils.removeRedundantNames(ditaDir);
         }
         if (ditavalFile != null && !new File(ditavalFile).isAbsolute()) {
             // XXX Shouldn't this be resolved to current directory, not Ant script base directory?
             ditavalFile = new File(basedir, ditavalFile).getAbsolutePath();
         }
 
         baseInputDir = new File(inFile.getAbsolutePath()).getParent();
         baseInputDir = FileUtils.removeRedundantNames(baseInputDir);
 
         rootFile = inFile.getAbsolutePath();
         rootFile = FileUtils.removeRedundantNames(rootFile);
        
         inputFile = inFile.getName();
         try {
             keydef = XMLSerializer.newInstance(new FileOutputStream(new File(tempDir, "keydef.xml")));
             keydef.writeStartDocument();
             keydef.writeStartElement(ELEMENT_STUB);
             // Added by William on 2009-06-09 for scheme key bug
             // create the keydef file for scheme files
             schemekeydef = XMLSerializer.newInstance(new FileOutputStream(new File(tempDir, "schemekeydef.xml")));
             schemekeydef.writeStartDocument();
             schemekeydef.writeStartElement(ELEMENT_STUB);
 
             // Added by William on 2009-06-25 for req #12014 start
             // create the export file for exportanchors
             // write the head
             export = new OutputStreamWriter(new FileOutputStream(new File(tempDir, FILE_NAME_EXPORT_XML)));
             export.write(XML_HEAD);
             export.write("<stub>");
             // Added by William on 2009-06-25 for req #12014 end
         } catch (final FileNotFoundException e) {
             logger.logException(e);
         } catch (final IOException e) {
             logger.logException(e);
         } catch (final SAXException e) {
             logger.logException(e);
         }
 
         // Set the mapDir
         outputUtils.setInputMapPathName(inFile.getAbsolutePath());
     }
 
     private void processWaitList() throws DITAOTException {
         // Added by William on 2009-07-18 for req #12014 start
         reader.setTranstype(transtype);
         // Added by William on 2009-07-18 for req #12014 end
 
         if (FileUtils.isDITAMapFile(inputFile)) {
             reader.setPrimaryDitamap(inputFile);
         }
 
         while (!waitList.isEmpty()) {
             processFile(waitList.remove(0));
         }
     }
 
     private void processFile(String currentFile) throws DITAOTException {
         File fileToParse;
         final File file = new File(currentFile);
         if (file.isAbsolute()) {
             fileToParse = file;
             currentFile = FileUtils.getRelativePathFromMap(rootFile, currentFile);
         } else {
             fileToParse = new File(baseInputDir, currentFile);
         }
         try {
         	fileToParse = fileToParse.getCanonicalFile();
 		} catch (IOException e1) {
 			logger.logError(e1.toString());
 		}
         logger.logInfo("Processing " + fileToParse.getAbsolutePath());
         String msg = null;
         final Properties params = new Properties();
         params.put("%1", currentFile);
 
         if (!fileToParse.exists()) {
             logger.logError(MessageUtils.getMessage("DOTX008E", params).toString());
             return;
         }
         try {
             if (FileUtils.isValidTarget(currentFile.toLowerCase())) {
                 reader.setTranstype(transtype);
                 reader.setCurrentDir(new File(currentFile).getParent());
                 reader.parse(fileToParse);
 
             } else {
                 // edited by Alan on Date:2009-11-02 for Work Item:#1590 start
                 // logger.logWarn("Input file name is not valid DITA file name.");
                 final Properties prop = new Properties();
                 prop.put("%1", fileToParse);
                 logger.logWarn(MessageUtils.getMessage("DOTJ053W", params).toString());
                 // edited by Alan on Date:2009-11-02 for Work Item:#1590 end
             }
 
             // don't put it into dita.list if it is invalid
             if (reader.isValidInput()) {
                 processParseResult(currentFile);
                 categorizeCurrentFile(currentFile);
             } else if (!currentFile.equals(inputFile)) {
                 logger.logWarn(MessageUtils.getMessage("DOTJ021W", params).toString());
             }
         } catch (final SAXParseException sax) {
 
             // To check whether the inner third level is DITAOTBuildException
             // :FATALERROR
             final Exception inner = sax.getException();
             if (inner != null && inner instanceof DITAOTException) {// second
                 // level
                 logger.logInfo(inner.getMessage());
                 throw (DITAOTException) inner;
             }
             if (currentFile.equals(inputFile)) {
                 // stop the build if exception thrown when parsing input file.
                 final MessageBean msgBean = MessageUtils.getMessage("DOTJ012F", params);
                 msg = MessageUtils.getMessage("DOTJ012F", params).toString();
                 msg = new StringBuffer(msg).append(":").append(sax.getMessage()).toString();
                 throw new DITAOTException(msgBean, sax, msg);
             }
             final StringBuffer buff = new StringBuffer();
             msg = MessageUtils.getMessage("DOTJ013E", params).toString();
             buff.append(msg).append(LINE_SEPARATOR).append(sax.getMessage());
             logger.logError(buff.toString());
         } catch (final Exception e) {
 
             if (currentFile.equals(inputFile)) {
                 // stop the build if exception thrown when parsing input file.
                 final MessageBean msgBean = MessageUtils.getMessage("DOTJ012F", params);
                 msg = MessageUtils.getMessage("DOTJ012F", params).toString();
                 msg = new StringBuffer(msg).append(":").append(e.getMessage()).toString();
                 throw new DITAOTException(msgBean, e, msg);
             }
             final StringBuffer buff = new StringBuffer();
             msg = MessageUtils.getMessage("DOTJ013E", params).toString();
             buff.append(msg).append(LINE_SEPARATOR).append(e.getMessage());
             logger.logError(buff.toString());
         }
 
         if (!reader.isValidInput() && currentFile.equals(inputFile)) {
 
             if (xmlValidate == true) {
                 // stop the build if all content in the input file was filtered
                 // out.
                 msg = MessageUtils.getMessage("DOTJ022F", params).toString();
                 throw new DITAOTException(msg);
             } else {
                 // stop the build if the content of the file is not valid.
                 msg = MessageUtils.getMessage("DOTJ034F", params).toString();
                 throw new DITAOTException(msg);
             }
 
         }
 
         doneList.add(currentFile);
         reader.reset();
 
     }
 
     private void processParseResult(String currentFile) {
         final Map<String, String> cpMap = reader.getCopytoMap();
         final Map<String, KeyDef> kdMap = reader.getKeysDMap();
         // Added by William on 2010-06-09 for bug:3013079 start
         // the reader's reset method will clear the map.
         final Map<String, String> exKdMap = reader.getExKeysDefMap();
         exKeyDefMap.putAll(exKdMap);
         // Added by William on 2010-06-09 for bug:3013079 end
 
         // Category non-copyto result and update uplevels accordingly
         for (final String file: reader.getNonCopytoResult()) {
             categorizeResultFile(file);
             updateUplevels(file);
         }
 
         // Update uplevels for copy-to targets, and store copy-to map.
         // Note: same key(target) copy-to will be ignored.
         for (final String key: cpMap.keySet()) {
             final String value = cpMap.get(key);
 
             if (copytoMap.containsKey(key)) {
                 // edited by Alan on Date:2009-11-02 for Work Item:#1590 start
                 /*
                  * StringBuffer buff = new StringBuffer();
                  * buff.append("Copy-to task [href=\""); buff.append(value);
                  * buff.append("\" copy-to=\""); buff.append(key);
                  * buff.append("\"] which points to another copy-to target");
                  * buff.append(" was ignored.");
                  * logger.logWarn(buff.toString());
                  */
                 final Properties prop = new Properties();
                 prop.setProperty("%1", value);
                 prop.setProperty("%2", key);
                 logger.logWarn(MessageUtils.getMessage("DOTX065W", prop).toString());
                 // edited by Alan on Date:2009-11-02 for Work Item:#1590 end
                 ignoredCopytoSourceSet.add(value);
             } else {
                 updateUplevels(key);
                 copytoMap.put(key, value);
             }
         }
         // TODO Added by William on 2009-06-09 for scheme key bug(497)
         schemeSet.addAll(reader.getSchemeRefSet());
 
         // collect key definitions
         for (final String key: kdMap.keySet()) {
             // key and value.keys will differ when keydef is a redirect to another keydef
             final KeyDef value = kdMap.get(key);
             if (keysDefMap.containsKey(key)) {
                 // if there already exists duplicated key definition in
                 // different map files.
                 // Should only emit this if in a debug mode; comment out for now
                 /*
                  * Properties prop = new Properties(); prop.put("%1", key);
                  * prop.put("%2", value); prop.put("%3", currentFile); logger
                  * .logInfo(MessageUtils.getMessage("DOTJ048I",
                  * prop).toString());
                  */
             } else {
                 updateUplevels(key);
                 // add the ditamap where it is defined.
                 /*
                  * try { keydef.write("<keydef ");
                  * keydef.write("keys=\""+key+"\" ");
                  * keydef.write("href=\""+value+"\" ");
                  * keydef.write("source=\""+currentFile+"\"/>");
                  * keydef.write("\n"); keydef.flush(); } catch (IOException e) {
                  * 
                  * logger.logException(e); }
                  */
                 keysDefMap.put(key, new KeyDef(key, value.href, currentFile));
             }
             // TODO Added by William on 2009-06-09 for scheme key bug(532-547)
             // if the current file is also a schema file
             if (schemeSet.contains(currentFile)) {
                 // write the keydef into the scheme keydef file
                 try {
                     schemekeydef.writeStartElement(ELEMENT_KEYDEF);
                     schemekeydef.writeAttribute(ATTRIBUTE_KEYS, key);
                     if (value.href != null) {
                         schemekeydef.writeAttribute(ATTRIBUTE_HREF, value.href);
                     }
                     schemekeydef.writeAttribute(ATTRIUBTE_SOURCE, currentFile);
                     schemekeydef.writeEndElement();
                 } catch (final SAXException e) {
                     logger.logException(e);
                 }
             }
 
         }
 
         hrefTargetSet.addAll(reader.getHrefTargets());
         hrefWithIDSet.addAll(reader.getHrefTopicSet());
         chunkTopicSet.addAll(reader.getChunkTopicSet());
         // schemeSet.addAll(reader.getSchemeRefSet());
         conrefTargetSet.addAll(reader.getConrefTargets());
         nonConrefCopytoTargetSet.addAll(reader.getNonConrefCopytoTargets());
         ignoredCopytoSourceSet.addAll(reader.getIgnoredCopytoSourceSet());
         subsidiarySet.addAll(reader.getSubsidiaryTargets());
         outDitaFilesSet.addAll(reader.getOutFilesSet());
         resourceOnlySet.addAll(reader.getResourceOnlySet());
 
         // Generate topic-scheme dictionary
         if (reader.getSchemeSet() != null && reader.getSchemeSet().size() > 0) {
             Set<String> children = null;
             children = this.schemeDictionary.get(currentFile);
             if (children == null) {
                 children = new HashSet<String>();
             }
             children.addAll(reader.getSchemeSet());
             // for Linux support
             currentFile = currentFile.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
 
             this.schemeDictionary.put(currentFile, children);
             final Set<String> hrfSet = reader.getHrefTargets();
             for (String f: hrfSet) {
                 // for Linux support
                 final String filename = f.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
 
                 children = this.schemeDictionary.get(filename);
                 if (children == null) {
                     children = new HashSet<String>();
                 }
                 children.addAll(reader.getSchemeSet());
                 this.schemeDictionary.put(filename, children);
             }
         }
     }
 
     private void categorizeCurrentFile(final String currentFile) {
         final String lcasefn = currentFile.toLowerCase();
 
         ditaSet.add(currentFile);
 
         if (FileUtils.isTopicFile(currentFile)) {
             hrefTargetSet.add(currentFile);
         }
 
         if (reader.hasConaction()) {
             conrefpushSet.add(currentFile);
         }
 
         if (reader.hasConRef()) {
             conrefSet.add(currentFile);
         }
 
         if (reader.hasKeyRef()) {
             keyrefSet.add(currentFile);
         }
 
         if (reader.hasCodeRef()) {
             coderefSet.add(currentFile);
         }
 
         if (FileUtils.isDITATopicFile(lcasefn)) {
             fullTopicSet.add(currentFile);
             if (reader.hasHref()) {
                 hrefTopicSet.add(currentFile);
             }
         }
 
         if (FileUtils.isDITAMapFile(lcasefn)) {
             fullMapSet.add(currentFile);
             if (reader.hasHref()) {
                 hrefMapSet.add(currentFile);
             }
         }
     }
 
     private void categorizeResultFile(String file) {
         // edited by william on 2009-08-06 for bug:2832696 start
         String lcasefn = null;
         String format = null;
         // has format attribute set
         if (file.contains(STICK)) {
             // get lower case file name
             lcasefn = file.substring(0, file.indexOf(STICK)).toLowerCase();
             // get format attribute
             format = file.substring(file.indexOf(STICK) + 1);
             file = file.substring(0, file.indexOf(STICK));
         } else {
             lcasefn = file.toLowerCase();
         }
 
         // Added by William on 2010-03-04 for bug:2957938 start
         // avoid files referred by coderef being added into wait list
         if (subsidiarySet.contains(lcasefn)) {
             return;
         }
         // Added by William on 2010-03-04 for bug:2957938 end
 
         if (FileUtils.isDITAFile(lcasefn)
                 && (format == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(format) || ATTR_FORMAT_VALUE_DITAMAP
                 .equalsIgnoreCase(format))) {
 
             addToWaitList(file);
         } else if (!FileUtils.isSupportedImageFile(lcasefn)) {
             htmlSet.add(file);
         }
         // edited by william on 2009-08-06 for bug:2832696 end
         if (FileUtils.isSupportedImageFile(lcasefn)) {
         	imageSet.add(file);        	      	
 			try {
 				File image = new File (baseInputDir + File.separator + file).getCanonicalFile(); 
 				if (!image.exists()){
 	            	Properties prop = new Properties();
 					prop.put("%1", image.getAbsolutePath());
 					logger.logWarn(MessageUtils.getMessage(
 							"DOTX008W", prop).toString());
 	            }
 			} catch (IOException e) {
 				logger.logError(e.getMessage());
 			}
         }
 
         if (FileUtils.isHTMLFile(lcasefn) || FileUtils.isResourceFile(lcasefn)) {
             htmlSet.add(file);
         }
     }
 
     /**
      * Update uplevels if needed.
      * 
      * @param file
      */
     private void updateUplevels(String file) {
 
         // Added by william on 2009-08-06 for bug:2832696 start
         if (file.contains(STICK)) {
             file = file.substring(0, file.indexOf(STICK));
         }
         // Added by william on 2009-08-06 for bug:2832696 end
 
         // for uplevels (../../)
         // modified start by wxzhang 20070518
         // ".."-->"../"
         final int lastIndex = FileUtils.removeRedundantNames(file).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR)
                 .lastIndexOf("../");
         // modified end by wxzhang 20070518
         if (lastIndex != -1) {
             final int newUplevels = lastIndex / 3 + 1;
             uplevels = newUplevels > uplevels ? newUplevels : uplevels;
         }
     }
 
     /**
      * Add the given file the wait list if it has not been parsed.
      * 
      * @param file
      */
     private void addToWaitList(final String file) {
         if (doneList.contains(file) || waitList.contains(file)) {
             return;
         }
 
         waitList.add(file);
     }
 
     private void updateBaseDirectory() {
         baseInputDir = new File(baseInputDir).getAbsolutePath();
 
         for (int i = uplevels; i > 0; i--) {
             final File file = new File(baseInputDir);
             baseInputDir = file.getParent();
             prefix = new StringBuffer(file.getName()).append(File.separator).append(prefix).toString();
         }
     }
 
     private String getUpdateLevels() {
         int current = uplevels;
         final StringBuffer buff = new StringBuffer();
         while (current > 0) {
             buff.append(".." + FILE_SEPARATOR);
             current--;
         }
         return buff.toString();
     }
 
     /**
      * Escape regular expression special characters.
      * 
      * @param value input
      * @return input with regular expression special characters escaped
      */
     private String formatRelativeValue(final String value) {
         final StringBuffer buff = new StringBuffer();
         if (value == null || value.length() == 0) {
             return "";
         }
         int index = 0;
         // $( )+.[^{\
         while (index < value.length()) {
             final char current = value.charAt(index);
             switch (current) {
             case '.':
                 buff.append("\\.");
                 break;
                 // case '/':
                 // case '|':
             case '\\':
                 buff.append("[\\\\|/]");
                 break;
             case '(':
                 buff.append("\\(");
                 break;
             case ')':
                 buff.append("\\)");
                 break;
             case '[':
                 buff.append("\\[");
                 break;
             case ']':
                 buff.append("\\]");
                 break;
             case '{':
                 buff.append("\\{");
                 break;
             case '}':
                 buff.append("\\}");
                 break;
             case '^':
                 buff.append("\\^");
                 break;
             case '+':
                 buff.append("\\+");
                 break;
             case '$':
                 buff.append("\\$");
                 break;
             default:
                 buff.append(current);
             }
             index++;
         }
         return buff.toString();
     }
 
     private FilterUtils parseFilterFile() {
         final FilterUtils filterUtils = new FilterUtils();
         filterUtils.setLogger(logger);
         if (ditavalFile != null) {
             final DitaValReader ditaValReader = new DitaValReader();
             ditaValReader.setLogger(logger);
             ditaValReader.initXMLReader(setSystemid);
 
             ditaValReader.read(ditavalFile);
             // Store filter map for later use
             filterUtils.setFilterMap(ditaValReader.getFilterMap());
             // Store flagging image used for image copying
             flagImageSet.addAll(ditaValReader.getImageList());
             relFlagImagesSet.addAll(ditaValReader.getRelFlagImageList());
         } else {
             filterUtils.setFilterMap(null);
         }
         return filterUtils;
     }
 
     private void refactoringResult() {
         handleConref();
         handleCopyto();
     }
 
     private void handleCopyto() {
         final Map<String, String> tempMap = new HashMap<String, String>();
         final Set<String> pureCopytoSources = new HashSet<String>(INT_128);
         final Set<String> totalCopytoSources = new HashSet<String>(INT_128);
 
         // Validate copy-to map, remove those without valid sources
         for (final String key: copytoMap.keySet()) {
             final String value = copytoMap.get(key);
             if (new File(baseInputDir + File.separator + prefix, value).exists()) {
                 tempMap.put(key, value);
                 // Add the copy-to target to conreflist when its source has
                 // conref
                 if (conrefSet.contains(value)) {
                     conrefSet.add(key);
                 }
             }
         }
 
         copytoMap = tempMap;
 
         // Add copy-to targets into ditaSet, fullTopicSet
         ditaSet.addAll(copytoMap.keySet());
         fullTopicSet.addAll(copytoMap.keySet());
 
         // Get pure copy-to sources
         totalCopytoSources.addAll(copytoMap.values());
         totalCopytoSources.addAll(ignoredCopytoSourceSet);
         for (final String src: totalCopytoSources) {
             if (!nonConrefCopytoTargetSet.contains(src) && !copytoMap.keySet().contains(src)) {
                 pureCopytoSources.add(src);
             }
         }
 
         copytoSourceSet = pureCopytoSources;
 
         // Remove pure copy-to sources from ditaSet, fullTopicSet
         ditaSet.removeAll(pureCopytoSources);
         fullTopicSet.removeAll(pureCopytoSources);
     }
 
     private void handleConref() {
         // Get pure conref targets
         final Set<String> pureConrefTargets = new HashSet<String>(INT_128);
         for (final String target: conrefTargetSet) {
             if (!nonConrefCopytoTargetSet.contains(target)) {
                 pureConrefTargets.add(target);
             }
         }
         conrefTargetSet = pureConrefTargets;
 
         // Remove pure conref targets from ditaSet, fullTopicSet
         ditaSet.removeAll(pureConrefTargets);
         fullTopicSet.removeAll(pureConrefTargets);
     }
 
     private void outputResult() throws DITAOTException {
         final File dir = new File(tempDir);
         if (!dir.exists()) {
             dir.mkdirs();
         }
         
         Job prop = null;
         try {
             prop = new Job(dir);
         } catch (final IOException e) {
             throw new DITAOTException("Failed to create empty job: " + e.getMessage(), e);
         }
         
         prop.setProperty(INPUT_DIR, baseInputDir);
         prop.setProperty(INPUT_DITAMAP, prefix + inputFile);
 
         prop.setProperty(INPUT_DITAMAP_LIST_FILE_LIST, USER_INPUT_FILE_LIST_FILE);
         final File inputfile = new File(tempDir, USER_INPUT_FILE_LIST_FILE);
         Writer bufferedWriter = null;
         try {
             bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputfile)));
             bufferedWriter.write(prefix + inputFile);
             bufferedWriter.flush();
         } catch (final FileNotFoundException e) {
             logger.logException(e);
         } catch (final IOException e) {
             logger.logException(e);
         } finally {
             if (bufferedWriter != null) {
                 try {
                     bufferedWriter.close();
                 } catch (final IOException e) {
                     logger.logException(e);
                 }
             }
         }
 
         // add out.dita.files,tempdirToinputmapdir.relative.value to solve the
         // output problem
         relativeValue = prefix;
         formatRelativeValue = formatRelativeValue(relativeValue);
         prop.setProperty("tempdirToinputmapdir.relative.value", formatRelativeValue);
 
         prop.setProperty("uplevels", getUpdateLevels());
         addSetToProperties(prop, OUT_DITA_FILES_LIST, outDitaFilesSet);
 
         addSetToProperties(prop, FULL_DITAMAP_TOPIC_LIST, ditaSet);
         addSetToProperties(prop, FULL_DITA_TOPIC_LIST, fullTopicSet);
         addSetToProperties(prop, FULL_DITAMAP_LIST, fullMapSet);
         addSetToProperties(prop, HREF_DITA_TOPIC_LIST, hrefTopicSet);
         addSetToProperties(prop, CONREF_LIST, conrefSet);
         addSetToProperties(prop, IMAGE_LIST, imageSet);
         addSetToProperties(prop, FLAG_IMAGE_LIST, flagImageSet);
         addSetToProperties(prop, HTML_LIST, htmlSet);
         addSetToProperties(prop, HREF_TARGET_LIST, hrefTargetSet);
         addSetToProperties(prop, HREF_TOPIC_LIST, hrefWithIDSet);
         addSetToProperties(prop, CHUNK_TOPIC_LIST, chunkTopicSet);
         addSetToProperties(prop, SUBJEC_SCHEME_LIST, schemeSet);
         addSetToProperties(prop, CONREF_TARGET_LIST, conrefTargetSet);
         addSetToProperties(prop, COPYTO_SOURCE_LIST, copytoSourceSet);
         addSetToProperties(prop, SUBSIDIARY_TARGET_LIST, subsidiarySet);
         addSetToProperties(prop, CONREF_PUSH_LIST, conrefpushSet);
         addSetToProperties(prop, KEYREF_LIST, keyrefSet);
         addSetToProperties(prop, CODEREF_LIST, coderefSet);
 
         // @processing-role
         addSetToProperties(prop, RESOURCE_ONLY_LIST, resourceOnlySet);
 
         addFlagImagesSetToProperties(prop, REL_FLAGIMAGE_LIST, relFlagImagesSet);
 
         // Convert copyto map into set and output
         final Set<String> copytoSet = new HashSet<String>(INT_128);
         for (final Map.Entry<String, String> entry: copytoMap.entrySet()) {
             copytoSet.add(entry.toString());
         }
         addSetToProperties(prop, COPYTO_TARGET_TO_SOURCE_MAP_LIST, copytoSet);
         addKeyDefSetToProperties(prop, KEY_LIST, keysDefMap.values());
 
         try {
             logger.logInfo("Serializing job specification");
             prop.write();
         } catch (final IOException e) {
             throw new DITAOTException("Failed to serialize job configuration files: " + e.getMessage(), e);
         }
         
         // Output relation-graph
         writeMapToXML(reader.getRelationshipGrap(), FILE_NAME_SUBJECT_RELATION);
         // Output topic-scheme dictionary
         writeMapToXML(this.schemeDictionary, FILE_NAME_SUBJECT_DICTIONARY);
 
         // added by Willam on 2009-07-17 for req #12014 start
         if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
             // Output plugin id
             final File pluginIdFile = new File(tempDir, FILE_NAME_PLUGIN_XML);
             final DelayConrefUtils delayConrefUtils = new DelayConrefUtils();
             delayConrefUtils.writeMapToXML(reader.getPluginMap(), pluginIdFile);
             // write the result into the file
             final StringBuffer result = reader.getResult();
             try {
                 export.write(result.toString());
             } catch (final IOException e) {
                 logger.logException(e);
             }
         }
         // added by Willam on 2009-07-17 for req #12014 end
 
     }
 
     private void writeMapToXML(final Map<String, Set<String>> m, final String filename) {
         if (m == null) {
             return;
         }
         final Properties prop = new Properties();
         for (final Map.Entry<String, Set<String>> entry: m.entrySet()) {
             final String key = entry.getKey();
             final String value = StringUtils.assembleString(entry.getValue(), COMMA);
             prop.setProperty(key, value);
         }
         final File outputFile = new File(tempDir, filename);
         OutputStream os = null;
         try {
             os = new FileOutputStream(outputFile);
             prop.storeToXML(os, null);
             os.close();
         } catch (final IOException e) {
             this.logger.logException(e);
         } finally {
             if (os != null) {
                 try {
                     os.close();
                 } catch (final Exception e) {
                     logger.logException(e);
                 }
             }
         }
     }
 
     private void addSetToProperties(final Job prop, final String key, final Set<String> set) {
         // update value
         final Set<String> newSet = new LinkedHashSet<String>(INT_128);
         for (final String file: set) {
             if (new File(file).isAbsolute()) {
                 // no need to append relative path before absolute paths
                 newSet.add(FileUtils.removeRedundantNames(file));
             } else {
                 // In ant, all the file separator should be slash, so we need to
                 // replace all the back slash with slash.
                 final int index = file.indexOf(EQUAL);
                 if (index != -1) {
                     // keyname
                     final String to = file.substring(0, index);
                     final String source = file.substring(index + 1);
                     
                     newSet.add(FileUtils.removeRedundantNames(new StringBuffer(prefix).append(to).toString())
                                   .replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR)
                             + EQUAL
                             + FileUtils.removeRedundantNames(new StringBuffer(prefix).append(source).toString())
                                   .replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR));
                 } else {
                     newSet.add(FileUtils.removeRedundantNames(new StringBuffer(prefix).append(file).toString())
                                   .replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR));
                 }
             }
         }
         prop.setSet(key, newSet);
         // write list file
         final String fileKey = key.substring(0, key.lastIndexOf("list")) + "file";
         prop.setProperty(fileKey, key.substring(0, key.lastIndexOf("list")) + ".list");
         try {
             prop.writeList(key);
         } catch (final IOException e) {
             logger.logError("Failed to write list file: " + e.getMessage(), e);
         }
     }
     
     private void addKeyDefSetToProperties(final Job prop, final String key, final Collection<KeyDef> set) {
         // update value
         final Set<String> newSet = new LinkedHashSet<String>(set.size());
         for (final KeyDef file: set) {
             String keys = FileUtils.removeRedundantNames(prefix + file.keys).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
             String href = file.href;
             String source = file.source;
             if (prefix.length() != 0) {
                 // cases where keymap is in map ancestor folder
                 keys = keys.substring(prefix.length());
                 if (href == null) {
                    href = FileUtils.removeRedundantNames(prefix).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                    source = FileUtils.removeRedundantNames(source).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                 } else {
                     if (!exKeyDefMap.containsKey(file.keys)) {
                         href = FileUtils.removeRedundantNames(prefix + href).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                     }
                     source = FileUtils.removeRedundantNames(prefix + source).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                 }
             }
             final KeyDef keyDef = new KeyDef(keys, href, source);
             newSet.add(keyDef.toString());
 
             writeKeyDef(keyDef);
         }
         prop.setSet(key, newSet);
         // write list file
         final String fileKey = key.substring(0, key.lastIndexOf("list")) + "file";
         prop.setProperty(fileKey, key.substring(0, key.lastIndexOf("list")) + ".list");
         try {
             prop.writeList(key);
         } catch (final IOException e) {
             logger.logError("Failed to write key list file: " + e.getMessage(), e);
         }
     }
 
     // Added by William on 2010-06-10 for bug:3013545 start
     /**
      * Write keydef into keydef.xml.
      * 
      * @param keyName key name.
      * @param result keydef.
      */
     private void writeKeyDef(final KeyDef keyDef) {
         try {
             keydef.writeStartElement(ELEMENT_KEYDEF);
             keydef.writeAttribute(ATTRIBUTE_KEYS, keyDef.keys);
             if (keyDef.href != null) {
                 keydef.writeAttribute(ATTRIBUTE_HREF, keyDef.href);
             }
             if (keyDef.source != null) {
                 keydef.writeAttribute(ATTRIUBTE_SOURCE, keyDef.source);
             }
             keydef.writeEndElement();
         } catch (final SAXException e) {
             logger.logException(e);
         }
     }
 
     // Added by William on 2010-06-10 for bug:3013545 end
 
     /**
      * add FlagImangesSet to Properties, which needn't to change the dir level,
      * just ouput to the ouput dir.
      * 
      * @param prop
      * @param key
      * @param set
      */
     private void addFlagImagesSetToProperties(final Job prop, final String key, final Set<String> set) {
         String value = null;
         final Set<String> newSet = new LinkedHashSet<String>(INT_128);
         for (final String file: set) {
             if (new File(file).isAbsolute()) {
                 // no need to append relative path before absolute paths
                 newSet.add(FileUtils.removeRedundantNames(file));
             } else {
                 // In ant, all the file separator should be slash, so we need to
                 // replace all the back slash with slash.
                 newSet.add(FileUtils.removeRedundantNames(new StringBuffer().append(file).toString()).replace(
                         WINDOWS_SEPARATOR, UNIX_SEPARATOR));
             }
         }
 
         // write list attribute to file
         final String fileKey = key.substring(0, key.lastIndexOf("list")) + "file";
         prop.setProperty(fileKey, key.substring(0, key.lastIndexOf("list")) + ".list");
         final File list = new File(tempDir, prop.getProperty(fileKey));
         Writer bufferedWriter = null;
         try {
             bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
             final Iterator<String> it = newSet.iterator();
             while (it.hasNext()) {
                 bufferedWriter.write(it.next());
                 if (it.hasNext()) {
                     bufferedWriter.write("\n");
                 }
             }
             bufferedWriter.flush();
             bufferedWriter.close();
         } catch (final FileNotFoundException e) {
             logger.logException(e);
         } catch (final IOException e) {
             logger.logException(e);
         } finally {
             if (bufferedWriter != null) {
                 try {
                     bufferedWriter.close();
                 } catch (final IOException e) {
                     logger.logException(e);
                 }
             }
         }
 
         value = StringUtils.assembleString(newSet, COMMA);
 
         prop.setProperty(key, value);
 
         // clear set
         set.clear();
         newSet.clear();
     }
 
     // Nested classes ----------------------------------------------------------
     
     public static class KeyDef {
         public final String keys;
         public final String href;
         public final String source;
         /**
          * Construct new key definition.
          * 
          * @param keys key name
          * @param href href URI, may be {@code null}
          * @param source key definition source, may be {@code null}
          */
         public KeyDef(final String keys, final String href, final String source) {
             this.keys = keys;
             this.href = href;
             this.source = source;
         }
         /**
          * Parse key definition from serialized from.
          * 
          * @param result serialized key definition
          */
         public KeyDef(final String result) {
             final int equalIndex = result.indexOf(EQUAL);
             final int leftBracketIndex = result.lastIndexOf(LEFT_BRACKET);
             final int rightBracketIndex = result.lastIndexOf(RIGHT_BRACKET);
             this.keys = result.substring(0, equalIndex);
             if (equalIndex + 1 < leftBracketIndex) {
                 this.href = result.substring(equalIndex + 1, leftBracketIndex);
             } else {
                 this.href = null;
             }
             if (leftBracketIndex + 1 < rightBracketIndex) {
                 this.source = result.substring(leftBracketIndex + 1, rightBracketIndex);
             } else {
                 this.source = null;
             }
         }
         @Override
         public String toString() {
             final StringBuilder buf = new StringBuilder().append(keys).append(EQUAL);
             if (href != null) {
                 buf.append(href);
             }
             if (source != null) {
                 buf.append(LEFT_BRACKET).append(source).append(RIGHT_BRACKET);
             }
             return buf.toString();
         }
     }
     
 }

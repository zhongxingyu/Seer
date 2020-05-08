 package com.alcshare.docs;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 import com.alcshare.docs.util.AddOnFiles;
 import com.alcshare.docs.util.Logging;
 import com.controlj.green.addonsupport.access.*;
 import com.controlj.green.addonsupport.web.WebContext;
 import com.controlj.green.addonsupport.web.WebContextFactory;
 import org.apache.commons.io.FileUtils;
 import org.jetbrains.annotations.NotNull;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.*;
 import java.util.*;
 
 /**
  *
  */
 public enum DocumentManager {
     INSTANCE;
 
     private static final String[] defaultHeader = new String[]{"Reference Path", "Display Path", "Title", "Document Path", "Path Type", "Category"};
     private static final int REQUIRED_COLUMNS = 4;  // later ones are optional
 
     private final HashMap<String,DocumentList> docRefs = new HashMap<String,DocumentList>();
     public void loadConfiguration(File configFile) throws IOException, SystemException, ActionExecutionException {
         SystemConnection connection = DirectAccess.getDirectAccess().getRootSystemConnection();
         loadConfigurationInternal(new FileReader(configFile), connection);
     }
 
     public DocumentList getAllDocuments() {
         DocumentList result = new DocumentList();
         for (DocumentList documentList : docRefs.values()) {
             result.addAll(documentList);
         }
         return result;
     }
 
     private void loadConfigurationInternal(Reader configReader, SystemConnection connection) throws IOException, SystemException, ActionExecutionException {
         docRefs.clear();
         connection.runReadAction(new LoadConfigurationAction(configReader, docRefs));
     }
 
     private static class LoadConfigurationAction implements ReadAction {
         private CSVReader reader;
         private HashMap<String, DocumentList> docRefs;
         private String[] customColumns = new String[0];
 
         public LoadConfigurationAction(Reader reader, HashMap<String,DocumentList> docRefs) {
             this.reader = new CSVReader(reader);
             this.docRefs = docRefs;
         }
 
         @Override
         public void execute(@NotNull SystemAccess access) throws Exception {
             String[] headers = reader.readNext();
             validateHeaders(headers);
 
             clearConfiguration();
             String[] nextLine;
             while ((nextLine = reader.readNext()) != null) {
                 if (shouldProcess(nextLine)) {
                     try {
                         DocumentReference.PathType pathtype = DocumentReference.stringToPathType(nextLine.length > REQUIRED_COLUMNS ? nextLine[4] : "");
                         //                                            refPath,     title,       docPath,     pathType
                         DocumentReference ref = new DocumentReference(nextLine[0], nextLine[2], nextLine[3], pathtype,
                                 nextLine.length > REQUIRED_COLUMNS +1 ? nextLine[5] : "",   // category
                                 findLocation(access, nextLine[0]),                          // location
                                 loadExtraColumns(nextLine));                                // extraColumns
                         if (ref.getPathType() == DocumentReference.PathType.DIR) {
                             addRef(getDocumentsInDir(ref));
                         } else {
                             addRef(ref);
                         }
                     } catch (UnresolvableException ex) {
                         Logging.println("Error processing a row in docs.csv.  Unable to resolve the location '"+nextLine[0]+"'");
                     } catch (Throwable th) {
                         Logging.println("Unexpected error while adding the row: "+Arrays.toString(nextLine), th);
                     }
                 }
             }
         }
 
         private boolean shouldProcess(String[] next) {
             if (next.length < REQUIRED_COLUMNS) {
                 return false;
             }
 
             for (int i=0; i<REQUIRED_COLUMNS; i++) {
                 String s= next[i];
                 if (s == null || s.length()==0) {
                     return false;
                 }
             }
             return true;
         }
 
         private List<DocumentReference> getDocumentsInDir(DocumentReference base) {
             List<DocumentReference> result = new ArrayList<DocumentReference>();
 
             File docBaseFile = AddOnFiles.getDocDirectory();
             File dir = new File(docBaseFile, base.getDocPath());
 
             File[] files = dir.listFiles();
 
             for (File file : files) {
                 //                                            refPath,                 title,
                 DocumentReference ref = new DocumentReference(base.getReferencePath(), getBaseFileName(file),
                 //      docPath,         pathType
                         getRelativePath(AddOnFiles.getDocDirectory(), file) , DocumentReference.PathType.DISCOVERED,
                         base.getCategory(),     // category
                         base.getLocation(),     // location
                         base.getExtraColumns());// extraColumns
                 result.add(ref);
 
             }
             return result;
         }
 
         private String getRelativePath(File base, File descendent) {
            return "/" + base.toURI().relativize(descendent.toURI()).getPath();
         }
 
         private Map<String,String> loadExtraColumns(String[] line) {
             if (line.length > defaultHeader.length) {
                 HashMap<String,String> result = new HashMap<String, String>();
                 for (int i=defaultHeader.length; i<line.length; i++) {
                     result.put(customColumns[i-defaultHeader.length], line[i]);
                 }
                 return result;
             } else {
                 return Collections.emptyMap();
             }
         }
 
         private void validateHeaders(String[] headers) throws Exception {
             if (headers != null) {
                 if (headers.length >= defaultHeader.length) {
                     if (arrayStartsWith(headers, defaultHeader)) {
                         customColumns = new String[headers.length - defaultHeader.length];
                         for (int i=defaultHeader.length; i<headers.length; i++) {
                             customColumns[i-defaultHeader.length] = headers[i];
                         }
                         return;
                     }
                 }
             }
 
             throw new Exception("Invalid config file header.  Headers should be "+Arrays.toString(defaultHeader));
         }
 
         private boolean arrayStartsWith(String[] test, String[] starts) {
             if (test.length < starts.length) { return false; }
 
             for (int i=0; i<starts.length; i++) {
                 if (!test[i].equals(starts[i])) { return false; }
             }
 
             return true;
         }
 
         private Location findLocation(SystemAccess access, String refPath) throws UnresolvableException {
             return access.resolveGQLPath(refPath);
         }
 
         private void addRef(DocumentReference ref) {
             Location loc = ref.getLocation();
             String luString = loc.getPersistentLookupString(true);
 
             DocumentList refList;
             synchronized (docRefs) {
                 refList = docRefs.get(luString);
                 if (refList == null) {
                     refList = new DocumentList();
                     docRefs.put(luString, refList);
                 }
             }
             refList.add(ref);
         }
 
         private void addRef(List<DocumentReference> refs) {
             for (DocumentReference ref : refs) {
                 addRef(ref);
             }
         }
 
         private void clearConfiguration() {
             synchronized (docRefs) {
                 docRefs.clear();
             }
         }
     }
 
     public void saveConfiguration(File configFile) {
         SystemConnection connection = DirectAccess.getDirectAccess().getRootSystemConnection();
         try {
             FileWriter outWriter = new FileWriter(configFile);
             connection.runReadAction(new WriteConfigurationAction(outWriter, docRefs));
             outWriter.close();
         } catch (Exception e) {
             Logging.println("Error writing configuration file", e);
         }
     }
 
     private static class WriteConfigurationAction implements ReadAction {
         private final CSVWriter output;
         private final HashMap<String, DocumentList> docRefs;
 
         public WriteConfigurationAction(Writer output, HashMap<String,DocumentList> docRefs) {
             this.output = new CSVWriter(output,',','"',System.getProperty("line.separator"));
             this.docRefs = docRefs;
         }
 
         @Override
         public void execute(@NotNull SystemAccess access) throws Exception {
             output.writeNext(defaultHeader);
             access.visit(access.getGeoRoot(), new ConfigWriterVisitor());
         }
 
         private class ConfigWriterVisitor extends Visitor {
             public ConfigWriterVisitor() { super(false); }
 
             @Override
             public void visit(@NotNull Location location) {
                 String lus = location.getPersistentLookupString(true);
                 List<DocumentReference> docRefList;
                 synchronized (docRefs) {
                     docRefList = docRefs.get(lus);
                 }
                 if (docRefList != null) {
                     // todo - insert existing stuff. Right now this only works when creating a new blank config
                     // but eventually we want to update one too.
                 } else {
                     writeDefaultRow(location);
                 }
             }
 
             private void writeDefaultRow(Location location) {
 
                 output.writeNext(new String[] { getGQLPath(location), getFullDisplayPath(location) });
             }
 
             private String getGQLPath(Location location) {
                 String referenceName = location.getReferenceName();
                 if (referenceName.startsWith("#")) {
                     return referenceName;
                 } else if (location.hasParent()) {
                     try {
                         return getGQLPath(location.getParent()) + "/" + referenceName;
                     } catch (UnresolvableException e) {
                         Logging.println("Unexpected exception while creating GQL path", e);
                         return "";
                     }
                 } else {
                     return "/trees/geographic";
                 }
             }
 
             private String getFullDisplayPath(Location location) {
                 String displayName = location.getDisplayName();
                 if (location.hasParent()) {
                     try {
                         return getFullDisplayPath(location.getParent()) + "/" + displayName;
                     } catch (UnresolvableException e) {
                         Logging.println("Error formatting display path", e);
                         return "";
                     }
                 } else {
                     return displayName;
                 }
 
             }
         }
     }
 
     public List<DocumentReference> getAllReferences() {
         List<DocumentReference> result = Collections.emptyList();
         for (DocumentList documentList : docRefs.values())
             result.addAll(documentList);
         return result;
     }
 
     public DocumentList getReferences(final HttpServletRequest request) {
         try {
             SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(request);
 
             return connection.runReadAction(new ReadActionResult<DocumentList>() {
                 @Override
                 public DocumentList execute(@NotNull SystemAccess access) throws Exception {
                     DocumentList result = new DocumentList();
                     try {
                         if (WebContextFactory.hasLinkedWebContext(request)) {
                             WebContext context = WebContextFactory.getLinkedWebContext(request);
                             Location location = context.getLinkedFromLocation(access.getTree(SystemTree.Geographic));
 
                             result = getReferencesForLocation(location.getPersistentLookupString(true), docRefs);
                         }
                     } catch (UnresolvableException e) {  } // ignore and return empty list
 
                     return result;
                 }
             });
         } catch (Exception e) {
             Logging.println("Error getting document references", e);
             return new DocumentList();
 
         }
     }
 
     private static DocumentList getReferencesForLocation(String locationString, HashMap<String,DocumentList> docRefs) {
         DocumentList result = new DocumentList();
 
         DocumentList referenceList;
         synchronized (docRefs) {
             referenceList = docRefs.get(locationString);
             if (referenceList != null) {
                 for (DocumentReference reference : referenceList) {
                     if (reference.getPathType() != DocumentReference.PathType.DIR) {
                         result.add(reference);
                     }
                 }
             }
         }
         return result;
     }
 
     private static String getBaseFileName(File file) {
         String result = file.getName();
         int index = result.lastIndexOf('.');
         if (index >= 0) {
             result = result.substring(0,index);
         }
         return result;
     }
 }

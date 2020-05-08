 package edu.columbia.irt.ccn.services;
 
 import edu.columbia.irt.netserv.core.osgi.Controller;
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 import org.ccnx.ccn.CCNFilterListener;
 import org.ccnx.ccn.CCNHandle;
 import org.ccnx.ccn.config.ConfigurationException;
 import org.ccnx.ccn.impl.support.Log;
 import org.ccnx.ccn.io.CCNFileOutputStream;
 import org.ccnx.ccn.profiles.CommandMarker;
 import org.ccnx.ccn.profiles.SegmentationProfile;
 import org.ccnx.ccn.profiles.VersioningProfile;
 import org.ccnx.ccn.profiles.metadata.MetadataProfile;
 import org.ccnx.ccn.profiles.nameenum.NameEnumerationResponse;
 import org.ccnx.ccn.profiles.nameenum.NameEnumerationResponse.NameEnumerationResponseMessage;
 import org.ccnx.ccn.profiles.nameenum.NameEnumerationResponse.NameEnumerationResponseMessage.NameEnumerationResponseMessageObject;
 import org.ccnx.ccn.profiles.security.KeyProfile;
 import org.ccnx.ccn.protocol.CCNTime;
 import org.ccnx.ccn.protocol.ContentName;
 import org.ccnx.ccn.protocol.Exclude;
 import org.ccnx.ccn.protocol.ExcludeComponent;
 import org.ccnx.ccn.protocol.Interest;
 import org.ccnx.ccn.protocol.MalformedContentNameStringException;
 import java.util.logging.Level;
 
 public class CCNServiceProxy implements CCNFilterListener {
 
     static String DEFAULT_URI = "ccnx:/";
     static int BUF_SIZE = 4096;
     protected boolean _finished = false;
     protected ContentName _prefix;
     protected String _filePrefix;
     protected File _rootDirectory;
     protected CCNHandle _handle;
     boolean processing = false;
     boolean fileTransfer = false;
     private ContentName _responseName = null;
     private Controller _serviceBridge = null;
 
     public static void usage() {
         System.err.println("usage: CCNServiceProxy <file path to serve> [<ccn prefix URI> default: ccn:/]");
     }
 
     public CCNServiceProxy() {
     }
 
     public CCNServiceProxy(String filePrefix, String ccnxURI)
             throws MalformedContentNameStringException, ConfigurationException,
             IOException {
         _prefix = ContentName.fromURI(ccnxURI);
         _filePrefix = filePrefix;
         _rootDirectory = new File(filePrefix);
         if (!_rootDirectory.exists()) {
             Log.severe(
                     "Cannot serve files from directory {0}: directory does not exist!",
                     filePrefix);
             throw new IOException("Cannot serve files from directory "
                     + filePrefix + ": directory does not exist!");
         }
         _handle = CCNHandle.open();
 
         // set response name for NE requests
         _responseName = KeyProfile.keyName(null, _handle.keyManager().getDefaultKeyID());
 
     }
 
     public void start() throws IOException {
         Log.info("Starting service proxy for " + _filePrefix
                 + " on CCNx namespace " + _prefix + "...");
         System.out.println("Starting service proxy for " + _filePrefix
                 + " on CCNx namespace " + _prefix + "...");
         // All we have to do is say that we're listening on our main prefix.
         _handle.registerFilter(_prefix, this);
     }
 
     @Override
     public boolean handleInterest(Interest interest) {
         // Alright, we've gotten an interest. Either it's an interest for a
         // stream we're
         // already reading, or it's a request for a new stream.
         // Test to see if we need to respond to it.
         if (!_prefix.isPrefixOf(interest.name())) {
             Log.info(
                     "Unexpected: got an interest not matching our prefix (which is {0})",
                     _prefix);
             return false;
         }
 
         // We see interests for all our segments, and the header. We want to
         // only
         // handle interests for the first segment of a file, and not the first
         // segment
         // of the header. Order tests so most common one (segments other than
         // first, non-header)
         // fails first.
         if (SegmentationProfile.isSegment(interest.name())
                 && !SegmentationProfile.isFirstSegment(interest.name())) {
             Log.info(
                     "Got an interest for something other than a first segment, ignoring {0}.",
                     interest.name());
             return false;
         } else if (interest.name().contains(
                 CommandMarker.COMMAND_MARKER_BASIC_ENUMERATION.getBytes())) {
             try {
                 Log.info("Got a name enumeration request: {0}", interest);
                 return nameEnumeratorResponse(interest);
             } catch (IOException e) {
                 Log.warning(
                         "IOException generating name enumeration response to {0}: {1}: {2}",
                         interest.name(), e.getClass().getName(), e.getMessage());
                 return false;
             }
         } else if (MetadataProfile.isHeader(interest.name())) {
             Log.info(
                     "Got an interest for the first segment of the header, ignoring {0}.",
                     interest.name());
             return false;
         }
 
         // Write the file
         try {
             return writeFile(interest);
         } catch (IOException e) {
             Log.warning("IOException writing file {0}: {1}: {2}",
                     interest.name(), e.getClass().getName(), e.getMessage());
             return false;
         }
     }
 
     public String getRootDir() {
         return _rootDirectory.toString();
     }
 
     private String serviceProcessing(Class<?> serviceClass, String file,
             String service) {
         Class[] parameterTypes = new Class[]{String.class, String.class};
         Object[] arguments = new Object[]{_filePrefix, file};
         Method concatMethod;
         Object result;
         try {
             concatMethod = serviceClass.getMethod("run", parameterTypes);
             Object instance = null;
             try {
                 instance = serviceClass.newInstance();
             } catch (InstantiationException e) {
                 Log.warning("Error creating instance of {0} ...", service);
                 return null;
             } catch (IllegalAccessException e) {
                 Log.warning("Error accessing service class {0} ...", service);
                 return null;
             }
             try {
                 result = concatMethod.invoke(instance, arguments);
             } catch (IllegalArgumentException e) {
                 Log.warning(
                         "Illegal argument passed to service method {0} ...",
                         service);
                 return null;
             } catch (IllegalAccessException e) {
                 Log.warning("Service method is not accessible {0} ...", service);
                 return null;
             } catch (InvocationTargetException e) {
                 Log.warning("Cannot invoke the target {0} ...", service);
                 return null;
             }
         } catch (SecurityException e) {
             Log.warning("Cannot access service class {0} ...{1}", service,
                     e.getMessage());
             return null;
 
         } catch (NoSuchMethodException e) {
             Log.warning("No such method exists in service {0} ... {1}",
                     service, e.getMessage());
             return null;
         }
 
         if (result == null) {
             Log.warning("Error processing service", service);
             return null;
         } else {
             return (String) result;
         }
     }
 
     /**
      * Depending on the service requested, it downloads corresponding java
      * module, dynamically loads it and produces a file output to be sent to the
      * client.
      */
     private void addService(Interest interest, String _rootDirectory,
             String[] tokens) {
 
         if (processing == true || fileTransfer == true) {
             return;
         }
         if (SegmentationProfile.isSegment(interest.name()) == true) {
             fileTransfer = false;
             return;
         }
         if (MetadataProfile.isHeader(interest.name())) {
             return;
         }
         String file = tokens[0].substring(1, tokens[0].length());
         processing = true;
         DynamicLoader dl;
         for (int i = 1; i < tokens.length; i++) {
             try {
                 dl = new DynamicLoader(_filePrefix, tokens[i]);
                 // Testing for NetServ Integration
                 // new DynamicLoader(_filePrefix, tokens[i], "10.0.1.11");
             } catch (IOException e) {
                 Log.warning("Error loading service {0}.jar.. {1}", tokens[i],
                         e.getMessage());
                 break;
             } catch (ClassNotFoundException e) {
                 Log.warning("Service class not found in {0}.jar.. {1}",
                         tokens[i], e.getMessage());
                 break;
             }
             file = serviceProcessing(dl.getServiceClass(), file, tokens[i]);
             Log.info("service output for {0} is {1}", tokens[i], file);
             if (file == null) {
                 break;
             }
         }
         processing = false;
         fileTransfer = true;
     }
 
     protected void ccnServiceBridge(Interest interest, String _rootDirectory,
             String[] tokens) {
 
         if (processing == true || fileTransfer == true) {
             return;
         }
         if (SegmentationProfile.isSegment(interest.name()) == true) {
             fileTransfer = false;
             return;
         }
         if (MetadataProfile.isHeader(interest.name())) {
             return;
         }
 
         String file = _filePrefix + tokens[0];
         String serviceURL = null;
         String serviceID = null;
         String serviceState = null;
         String outFile = null;
         processing = true;
         HashMap<String, String> param = new HashMap<String, String>();
         // assuming all the service jars are present in the repository
         for (int i = 1; i < tokens.length; i++) {
            // removing ccnx specific meta headers
            if (tokens[i].indexOf("/") > 0) {
                tokens[i] = tokens[i].substring(0, tokens[i].indexOf("/"));
            }
             outFile = file + "%2B" + tokens[i];
             File out = new File(outFile);
             if (!out.exists()) {
                 serviceID = tokens[i];
                 serviceURL = "file:" + _filePrefix + "/" + serviceID + ".jar";
                 Log.info("serviceID = {0} , serviceURL = {1} , file = {2}",
                         serviceID, serviceURL, file);
                 param.put("args", file);
                 serviceState = this._serviceBridge.getModuleState(serviceID);
                 Log.info("serviceState = {0} & serviceBridge = {1}", serviceState,
                         _serviceBridge);
                 try {
                     if (null == serviceState) {
                         Log.info("Setting up CCNService ID = {0} , URL = {1}",
                                 serviceID, serviceURL);
                         _serviceBridge.setupModule(serviceID, serviceURL, param);
                     } else if (serviceState.equals("UNINSTALLED")) {
                         Log.info("Changing CCNService {0} state to ACTIVE", serviceURL);
                         _serviceBridge.setModuleState(serviceID, "ACTIVE");
                     }
 
                     Log.info("Calling CCNService {0} with params {1}", serviceURL, file);
                     file = (String) _serviceBridge.executeModule(serviceID, param);
 
                 } catch (Exception ex) {
                     Log.severe("Exception is executing service {0} \n {1}", serviceURL, ex.toString());
                 }
                 if (null == file) {
                     break;
                 }
             } else {
                 continue;
             }
         }
         processing = false;
     }
 
     /**
      * Changed to Interest from ContentName param
      * @param interest
      * @return 
      */
     protected String ccnNametoFile(Interest interest) {
         ContentName name = interest.name();
         ContentName fileNamePostfix = name.postfix(_prefix);
         if (null == fileNamePostfix) {
             return null;
         }
 
         String fn = fileNamePostfix.toString();
         // CCN Services check
         String[] tokens = fn.split("\\%2B");
         if (tokens.length > 1) {
             ccnServiceBridge(interest, _rootDirectory.toString(), tokens);
         }
         return fn;
     }
 
     /**
      * This method check whether interest has service request as well.
      * @param interest
      * @param name
      * @return 
      */
     protected File ccnNameToFilePath(Interest interest, ContentName name) {
 
         ContentName fileNamePostfix = name.postfix(_prefix);
         if (null == fileNamePostfix) {
             // Only happens if interest.name() is not a prefix of _prefix.
             Log.info(
                     "Unexpected: got an interest not matching our prefix (which is {0})",
                     _prefix);
             return null;
         }
 
         File fileToWrite = new File(_rootDirectory, fileNamePostfix.toString());
         Log.info("Resulting path name: " + fileToWrite.getAbsolutePath());
         Log.info("file postfix {0}, resulting path name {1}", fileNamePostfix,
                 fileToWrite.getAbsolutePath());
         return fileToWrite;
     }
 
     /**
      * Actually write the file; should probably run in a separate thread.
      * 
      * @param fileNamePostfix
      * @throws IOException
      */
     protected boolean writeFile(Interest outstandingInterest)
             throws IOException {
 
         File check = new File(_rootDirectory, "/"
                 + ccnNametoFile(outstandingInterest));
         if (!check.exists()) {
             return false;
         }
 
 
         if (processing == true) {
             return false;
         }
 
 
         File fileToWrite = ccnNameToFilePath(outstandingInterest,
                 outstandingInterest.name());
         Log.info(
                 "CCNServiceProxy: extracted request for file: "
                 + fileToWrite.getAbsolutePath() + " exists? ",
                 fileToWrite.exists());
         if (!fileToWrite.exists()) {
             Log.warning("File {0} does not exist. Ignoring request.",
                     fileToWrite.getAbsoluteFile());
             return false;
         }
 
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(fileToWrite);
         } catch (FileNotFoundException fnf) {
             Log.warning(
                     "Unexpected: file we expected to exist doesn't exist: {0}!",
                     fileToWrite.getAbsolutePath());
             return false;
         }
 
         // Set the version of the CCN content to be the last modification time
         // of the file.
         CCNTime modificationTime = new CCNTime(fileToWrite.lastModified());
         ContentName versionedName = VersioningProfile.addVersion(
                 new ContentName(_prefix, outstandingInterest.name().postfix(_prefix).components()), modificationTime);
 
         // CCNFileOutputStream will use the version on a name you hand it (or if
         // the name
         // is unversioned, it will version it).
         CCNFileOutputStream ccnout = new CCNFileOutputStream(versionedName,
                 _handle);
 
         // We have an interest already, register it so we can write immediately.
         ccnout.addOutstandingInterest(outstandingInterest);
 
         byte[] buffer = new byte[BUF_SIZE];
 
         int read = fis.read(buffer);
         while (read >= 0) {
             ccnout.write(buffer, 0, read);
             read = fis.read(buffer);
         }
         fis.close();
         ccnout.close();
         fileTransfer = false;
         return true;
     }
 
     /**
      * Handle name enumeration requests
      * 
      * @param interest
      * @throws IOException
      * @returns true if interest is consumed
      */
     public boolean nameEnumeratorResponse(Interest interest) throws IOException {
 
         boolean result = false;
         ContentName neRequestPrefix = interest.name().cut(
                 CommandMarker.COMMAND_MARKER_BASIC_ENUMERATION.getBytes());
 
         File directoryToEnumerate = ccnNameToFilePath(interest, neRequestPrefix);
 
         if (!directoryToEnumerate.exists()
                 || !directoryToEnumerate.isDirectory()) {
             // nothing to enumerate
             return result;
         }
 
         NameEnumerationResponse ner = new NameEnumerationResponse();
         ner.setPrefix(new ContentName(neRequestPrefix,
                 CommandMarker.COMMAND_MARKER_BASIC_ENUMERATION.getBytes()));
 
         Log.info("Directory to enumerate: {0}, last modified {1}",
                 directoryToEnumerate.getAbsolutePath(), new CCNTime(
                 directoryToEnumerate.lastModified()));
         // stat() the directory to see when it last changed -- will change
         // whenever
         // a file is added or removed, which is the only thing that will change
         // the
         // list we return.
         ner.setTimestamp(new CCNTime(directoryToEnumerate.lastModified()));
         // See if the resulting response is later than the previous one we
         // released.
 
         // now add the response id
         ContentName prefixWithId = new ContentName(ner.getPrefix(),
                 _responseName.components());
         // now finish up with version and segment
         ContentName potentialCollectionName = VersioningProfile.addVersion(
                 prefixWithId, ner.getTimestamp());
 
         // switch to add response id to name enumeration objects
         // ContentName potentialCollectionName =
         // VersioningProfile.addVersion(ner.getPrefix(), ner.getTimestamp());
 
         potentialCollectionName = SegmentationProfile.segmentName(
                 potentialCollectionName, SegmentationProfile.baseSegment());
         // check if we should respond...
         if (interest.matches(potentialCollectionName, null)) {
 
             // We want to set the version of the NE response to the time of the
             // last modified file in the directory. Unfortunately that requires
             // us to
             // stat() all the files whether we are going to respond or not.
             String[] children = directoryToEnumerate.list();
 
             if ((null != children) && (children.length > 0)) {
                 for (int i = 0; i < children.length; ++i) {
                     ner.add(children[i]);
                 }
 
                 NameEnumerationResponseMessage nem = ner.getNamesForResponse();
                 NameEnumerationResponseMessageObject neResponse = new NameEnumerationResponseMessageObject(
                         prefixWithId, nem, _handle);
                 neResponse.save(ner.getTimestamp(), interest);
                 result = true;
                 Log.info(
                         "sending back name enumeration response {0}, timestamp (version) {1}.",
                         ner.getPrefix(), ner.getTimestamp());
             } else {
                 Log.info(
                         "no children available: we are not sending back a response to the name enumeration interest (interest = {0}); our response would have been {1}",
                         interest, potentialCollectionName);
             }
         } else {
             Log.info(
                     "we are not sending back a response to the name enumeration interest (interest = {0}); our response would have been {1}",
                     interest, potentialCollectionName);
             if (interest.exclude().size() > 1) {
                 Exclude.Element el = interest.exclude().value(1);
                 if ((null != el) && (el instanceof ExcludeComponent)) {
                     Log.info(
                             "previous version: {0}",
                             VersioningProfile.getVersionComponentAsTimestamp(((ExcludeComponent) el).getBytes()));
                 }
             }
         }
         return result;
     }
 
     /**
      * Turn off everything.
      * 
      * @throws IOException
      */
     public void shutdown() throws IOException {
         if (null != _handle) {
             _handle.unregisterFilter(_prefix, this);
             Log.info("Shutting down service proxy for " + _filePrefix
                     + " on CCNx namespace " + _prefix + "...");
             System.out.println("Shutting down service proxy for " + _filePrefix
                     + " on CCNx namespace " + _prefix + "...");
         }
         _finished = true;
     }
 
     public boolean finished() {
         return _finished;
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
 
         if (args.length < 1) {
             usage();
             return;
         }
 
         String filePrefix = args[0];
         String ccnURI = (args.length > 1) ? args[1] : DEFAULT_URI;
         if ((args.length > 2) && args[2].equals("--loggingoff")) {
             Log.setLevel(Log.FAC_ALL, Level.OFF);
             Log.setLevel(Log.FAC_USER15, Level.INFO);
         }
         // BasicConfigurator.configure();
         try {
             CCNServiceProxy proxy = new CCNServiceProxy(filePrefix, ccnURI);
 
             // All we need to do now is wait until interrupted.
             proxy.start();
 
             // create CCN-NetServ service bridge
             proxy._serviceBridge = NetServBridge.create();
 
             while (!proxy.finished()) {
                 // we really want to wait until someone ^C's us.
                 try {
                     Thread.sleep(100000);
                 } catch (InterruptedException e) {
                     // do nothing
                 }
             }
         } catch (Exception e) {
             Log.warning("Exception in CCNServiceProxy: type: "
                     + e.getClass().getName() + ", message:  " + e.getMessage());
             Log.warningStackTrace(e);
             System.err.println("Exception in CCNServiceProxy: type: "
                     + e.getClass().getName() + ", message:  " + e.getMessage());
             e.printStackTrace();
         }
     }
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.tracker.backend.webinterface;
 
 import com.tracker.backend.Bencode;
 import com.tracker.backend.StringUtils;
 import com.tracker.backend.entity.Torrent;
 import com.tracker.backend.webinterface.entity.TorrentData;
 import com.tracker.backend.webinterface.entity.TorrentFile;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 
 /**
  * Responsible for adding torrents.
  * <p>May have to rewrite the torrentfile to accommodate for things like wrong
  * announce or the private flag being set. In these cases, the user should be
  * told to redownload the torrentfile before {s,}he starts seeding.</p>
  * <p>This class does not do any thorough validation of the torrentfile, beyond
  * some simple checks to see if some of the necessary data is there.</p>
  * <p>This class requires the Apache Commons FileUpload package.</p>
  * @author bo
  */
 public class TorrentUpload {
     static Logger log = Logger.getLogger(TorrentUpload.class.getName());
     static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
 
     /**
      * A simple convenience class to make it easier to parse the Servlet request.
      */
     public class UnparsedTorrentData {
         public String name;
         public String description;
 
         public InputStream stream;
     }
 
     /**
      * A convenience method to separate out the interesting information from
      * a HttpServletRequest.
      * @param request the HttpServletRequest to parse the information from.
      * The information sought is "torrentName", "torrentDescription" and the
      * torrent file itself.
      * @return a UnparsedTorrentData object containing the information parsed
      * from the given request.
      * @throws java.lang.Exception if the request is not a multipart request.
      */
     public UnparsedTorrentData getDataFromRequest(HttpServletRequest request) throws Exception {
         if(!ServletFileUpload.isMultipartContent(request)) {
             log.log(Level.SEVERE, "Request received by getDataFromRequest() not a multi-part request?");
             throw new Exception("Request not multi-part?");
         }
 
         UnparsedTorrentData data = new UnparsedTorrentData();
         ServletFileUpload upload = new ServletFileUpload();
         FileItemIterator itr = upload.getItemIterator(request);
 
        // set the maximum size of the torrentfile, avoid loading hundreds of
        // megs into memory. A size limit of 10MB seems decent enough (the
        // biggest .torrent files I could find through some quick searching were
        // just below 2MB.
        // TODO: read from config?
        upload.setFileSizeMax(10485760L);

         while(itr.hasNext()) {
             FileItemStream item = itr.next();
             String name = item.getFieldName();
             InputStream stream = item.openStream();
 
             // is it the name or description?
             if(item.isFormField()) {
                 if(name.equalsIgnoreCase("torrentName")) {
                     // grab content of field
                     data.name = Streams.asString(stream);
                 }
                 else if(name.equalsIgnoreCase("torrentDescription")) {
                     data.description = Streams.asString(stream);
                 }
                 // some weird data?
                 else {
                     log.log(Level.INFO, "Unknown field (" + name + ") in upload request?");
                 }
             }
             // file field
             else {
                 data.stream = stream;
             }
         }
 
         return data;
     }
 
     /**
      * Reads a torrent given in the request, makes changes if necessary, then
      * adds it to the database of tracked torrents.
      * @param torrent the input stream to read the torrent from.
      * @param torrentDescription the torrent description to persist in the database.
      * @param torrentName the torrent name to persist in the database.
      * @param contextPath the context path of the running servlet. Used for
      * checking the announce keys in the torrent file.
      * @return a TreeMap populated with the result of the operation, plus
      * eventual warnings or error messages.
      * <p>The keys this may contain is:
      * <ul>
      * <li><b>"warning reason"</b>: a human readable string describing a warning
      * encountered while adding the torrentfile (wrong announce for example).
      * This is left empty if there is no warning given.</li>
      * <li><b>"error reason"</b>: a human readable description of why this torrent
      * could not be added to the database. This is left empty if there is no
      * errors.</li>
      * <li><b>"redownload"</b>: equals to "true" if the client needs to redownload
      * the torrentfile before {s,}he can begin seeding the torrent (for example
      * if the torrentfile had an incorrect announce URL, and was changed).
      * Equals to "false" if there is no reason to redownload the torrentfile.</li>
      * </ul>
      * @throws java.lang.Exception if there is some problem with the given
      * torrentfile or the persisting operation.
      */
     public static TreeMap<String,String> addTorrent(InputStream torrent,
             String torrentName, String torrentDescription, String contextPath) throws Exception {
         Long torrentLength = new Long(0L);
         Torrent t;
         TorrentData tData;
         TorrentFile tFile;
 
         // list of files and their lengths
         Map<String, Long> torrentFiles = new TreeMap<String, Long>();
 
         // the URL of this trackers announce, used for comparison with the
         // URL given in the torrentfile.
         String ourAnnounce = contextPath + "/Announce";
 
         TreeMap<String,String> response = new TreeMap<String,String>();
         // set some default replies
         response.put("warning_reason", "");
         response.put("error reason", "");
         response.put("redownload", "false");
 
         Map decodedTorrent;
         // set as null to avoid some nonsense when persisting, will be set
         // by the decoded torrent
         Map infoDictionary = null;
 
         // for generating the info_hash
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         
         // process the input stream
         try {
             /*
              * The torrent file layout is roughly like this:
              * Mandatory:
              * String       announce        (the URL of the tracker)
              * Dictionary   info            (describes the files of the torrent)
              *      Mandatory:
              *      Integer piece length    (number of bytes in each piece)
              *      String  pieces          (the SHA1 hashes of all the pieces)
              *
              *      Optional:
              *      Integer private         (determines if the torrent is private)
              *
              *      If this is a single-file torrent:
              *      Mandatory:
              *      String  name            (filename of the file)
              *      Integer length          (length of the file in bytes)
              *      Optional:
              *      String  md5sum          (MD5 sum of the file)
              *
              *      If this is a multiple-file torrent:
              *      Mandatory:
              *      String  name            (the name of the directory to store the files in)
              *      List of Dictionaries files (one for each file)
              *          Mandatory:
              *          Integer length      (length of the file in bytes)
              *          List    path        (a list of strings giving the path of the file)
              *          Optional:
              *          String  md5sum      (MD5 sum of the file)
              *      end of files
              * end of info
              * Optional:
              * List         announce-list   (list of list of trackers)
              * Integer      creation date   (the creation time of the torrent)
              * String       comment         (a comment to the torrent)
              * String       created-by      (gives the program used to create the torrent)
              *
              * The info hash used when tracking is the SHA1 hash of the
              * _value_ of the info key, in other words, a bencoded
              * dictionary.
              *
              * see: http://wiki.theory.org/BitTorrentSpecification
              * for more information and links on optional keys like the
              * announce-list.
              */
 
             decodedTorrent = Bencode.decode(torrent);
             String announceURL;
 
             // make sure that the torrentfile contains a bare minimum
             // of data.
             if(!decodedTorrent.containsKey("announce") ||
                     !decodedTorrent.containsKey("info")) {
                 log.log(Level.WARNING, "Malformed torrentfile received." +
                         "Missing 'announce' or 'info' keys.");
                 throw new Exception("The Torrentfile given in upload is malformed.");
             }
 
             // make sure that we are the ones that track this torrent.
             announceURL = (String) decodedTorrent.get((String)"announce");
             if(!announceURL.equalsIgnoreCase(ourAnnounce) ||
                     decodedTorrent.containsKey("announce-list")) {
                 // check for the optional announce-list, if we are not
                 // given there either, or if this is not set, issue a
                 // warning and change the torrentfile.
                 if(!decodedTorrent.containsKey("announce-list")) {
                     // rewrite the standard announce key
                     log.log(Level.WARNING, "Uploaded torrent does not have" +
                         "our announce (given was: " + announceURL + ").");
                     decodedTorrent.put("announce", ourAnnounce);
 
                     // TODO: apply i10n here
                     response.put("warning reason", "The torrentfile did" +
                             "not contain the correct announce URL, this" +
                             "has been changed.\n");
                     response.put("redownload", "true");
                 }
                 // announce-list found. Makes the "announce" key
                 // irrelevant. See this for more info:
                 // http://home.elp.rr.com/tur/multitracker-spec.txt
                 else {
                     // does it contain our announce string?
                     boolean valid = false;
                     // grab the announce-list and check every element
                     // for our announce URL.
                     Vector<Vector<String> > announceList =
                             (Vector<Vector<String> >) decodedTorrent.get("announce-list");
                     Iterator announceListItr = announceList.iterator();
 
                     while(announceListItr.hasNext()) {
                         Vector<String> innerList =
                                 (Vector<String>) announceListItr.next();
                         Iterator innerItr = innerList.iterator();
 
                         while(innerItr.hasNext()) {
                             String announce = (String) innerItr.next();
                             if(announce.equalsIgnoreCase(ourAnnounce)) {
                                 valid = true;
                                 break;
                             }
                         }
 
                         if(valid)
                             break;
                     }
                     if(!valid) {
                         log.log(Level.WARNING, "Uploaded torrent does not" +
                                 "have our announce in announce-list (given" +
                                 "was: " + announceList.toString() +").");
                         // add our announce URL to the list
                         Vector<String> appendedAnnounce = new Vector<String>();
                         appendedAnnounce.add(ourAnnounce);
                         announceList.add(appendedAnnounce);
 
                         // TODO: apply i10n
                         response.put("warning reason", "The torrentfile" +
                                 "did not contain the correct announce URL" +
                                 "in it's announce list. This has been changed.\n");
                         response.put("redownload", "true");
                     }
                 }
             } // if wrong announce or announce-list
 
             // check for the private setting
             infoDictionary = (Map) decodedTorrent.get("info");
 
             if(infoDictionary.containsKey("private")) {
                 Integer privateField = (Integer) infoDictionary.get("private");
                 // is private enabled?
                 if(privateField == 1) {
                     // remove the private key and set appropriate warnings
                     infoDictionary.remove("private");
 
                     log.log(Level.WARNING, "Torrent uploaded with private" +
                             "key enabled.");
                     String warningReason = response.get("warning reason");
                     warningReason += "The torrentfile was set as private, " +
                             "this has been removed.";
                     response.put("warning reason", warningReason);
                     response.put("redownload", "true");
                 }
             }
 
             // set the torrentlength
             // this is different if this is a single-file torrent or a
             // multi-file torrent. Test for both.
 
             // multi-file torrent - see diagram above
             if(infoDictionary.containsKey("files")) {
                 Vector<Map> files = (Vector<Map>) infoDictionary.get((String)"files");
                 Iterator fileItr = files.iterator();
 
                 while(fileItr.hasNext()) {
                     Map f = (Map) fileItr.next();
 
                     // grab length
                     Long length = (Long) f.get((String)"length");
                     torrentLength += length;
 
                     // grab path
                     Vector<String> filePath = (Vector<String>) f.get((String)"path");
                     Iterator pathItr = filePath.iterator();
                     String path = (String) infoDictionary.get((String)"name");
 
                     // build up the path
                     while(pathItr.hasNext()) {
                         path += "/";
                         path += pathItr.next();
                     }
 
                     // populate the map
                     torrentFiles.put(path, length);
                 }
             }
             // single file torrent - see diagram above
             else {
                 String name = (String) infoDictionary.get((String)"name");
                 Long length = (Long) infoDictionary.get((String)"length");
 
                 torrentLength = length;
 
                 // populate the map
                 torrentFiles.put(name, length);
             }
         }
         catch(Exception ex) {
             log.log(Level.WARNING, "Error when decoding given torrent.", ex);
             throw new Exception("Error when decoding torrent given in upload", ex);
         }
 
         // persist!
                // add the torrent to the database.
         EntityManager em = emf.createEntityManager();
         try {
             t = new Torrent();
             tData = new TorrentData();
             tFile = new TorrentFile();
             // grab the SHA1 hash of the (bencoded) info dictionary.
             // the simplest way is to simply encode the info dictionary again
             // (things may have changed), then do a SHA1-hash of the result.
             String info = Bencode.encode(infoDictionary);
             byte[] rawInfo = new byte[info.length()];
             byte[] rawInfoHash = new byte[20];
             for (int i = 0; i < rawInfo.length; i++) {
                 rawInfo[i] = (byte) info.charAt(i);
             }
             md.update(rawInfo);
             rawInfoHash = md.digest();
             // set the info hash.
             t.setInfoHash(StringUtils.getHexString(rawInfoHash));
             // num seeders and all that is set by the Torrent constructor.
             
             tData.setName(torrentName);
             tData.setDescription(torrentDescription);
             tData.setAdded(Calendar.getInstance().getTime());
             tData.setTorrentSize(torrentLength);
 
             t.setTorrentData(tData);
 
             // set the torrentfile
             String bencodedTorrent = Bencode.encode(decodedTorrent);
             tFile.setTorrentFile(bencodedTorrent);
 
             t.setTorrentFile(tFile);
 
             // persist this
             em.getTransaction().begin();
             em.persist(t);
             em.persist(tData);
             em.persist(tFile);
             em.getTransaction().commit();
         }
         catch(Exception ex) {
             if(em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
             }
             log.log(Level.WARNING, "Error when persisting the uploaded torrent.", ex);
             throw new Exception("Error when persising torrent given in upload", ex);
         }
         finally {
             em.close();
         }
 
         return response;
     }
 }

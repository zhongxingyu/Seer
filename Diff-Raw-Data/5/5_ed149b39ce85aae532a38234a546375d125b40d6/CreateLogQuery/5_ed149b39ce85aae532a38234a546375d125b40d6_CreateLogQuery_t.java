 /*
  * Copyright (c) 2010 Brookhaven National Laboratory
  * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
  * Subject to license terms and conditions.
  */
 package edu.msu.nscl.olog;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.core.Response;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.ibatis.session.SqlSession;
 import org.apache.ibatis.session.SqlSessionFactory;
 
 /**
  * JDBC query to create one log.
  *
  * @author Eric Berryman taken from Ralph Lange <Ralph.Lange@bessy.de>
  */
 public class CreateLogQuery {
 
     private static SqlSessionFactory ssf = MyBatisSession.getSessionFactory();
 
     private CreateLogQuery() {
     }
 
     /**
      * Creates a log and its logbooks/tags in the database.
      *
      * @param log XmlLog object
      * @throws CFException wrapping an SQLException
      */
     public static XmlLog createLog(XmlLog log) throws CFException, UnsupportedEncodingException, NoSuchAlgorithmException {
         SqlSession ss = ssf.openSession();
 
         try {
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
 
             hm.put("source", log.getSource());
             hm.put("owner", log.getOwner());
             hm.put("level", log.getLevel());
             hm.put("subject", log.getSubject());
             hm.put("description", log.getDescription());
 
             ss.insert("mappings.LogMapping.createLog", hm);
             int logId = (Integer) ss.selectOne("mappings.LogMapping.lastId");
             if (logId > 0) {
 
                 hm.clear();
 
                 hm.put("md5entry", getmd5Entry((long) logId, log));
                 hm.put("md5recent", getmd5Recent((long) logId));
                 hm.put("id", (long) logId);
                 if (logIdExists(log)) {
                     hm.put("pid", log.getId());
                 } else {
                     log.setId((long) logId);
                     hm.put("pid", null);
                 }
 
                 ss.update("mappings.LogMapping.updateMD5", hm);
 
                 // Fetch the logbook/tag ids
                 Map<String, Integer> pids = FindLogbookIdsQuery.getLogbookIdMap(log);
 
                 // Insert logbook/tags
                 // Fail if there isn't at least one logbook
                 if (log.getXmlLogbooks().isEmpty()) {
                     throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                             "Must add to at least one logbook '" + log.getSubject() + "'");
                 }
                 if (log.getXmlLogbooks().size() > 0 || log.getXmlTags().size() > 0) {
                     for (XmlLogbook logbook : log.getXmlLogbooks()) {
                         if (pids.get(logbook.getName()) == null) {
                             throw new CFException(Response.Status.NOT_FOUND,
                                     "Logbook '" + logbook.getName() + "' does not exist");
                         }
 
                         hm.clear();
                         hm.put("logid", logId);
                         hm.put("logbookid", FindLogbookIdsQuery.getLogbookId(logbook.getName()));
                        hm.put("state", null);
                         ss.insert("mappings.LogMapping.logsLogbooksEntry", hm);
 
                     }
                     for (XmlTag tag : log.getXmlTags()) {
                         if (pids.get(tag.getName()) == null) {
                             throw new CFException(Response.Status.NOT_FOUND,
                                     "Tag '" + tag.getName() + "' does not exist");
                         }
                         hm.clear();
                         hm.put("logid", logId);
                         hm.put("logbookid", FindLogbookIdsQuery.getLogbookId(tag.getName()));
                        hm.put("state", null);
                         ss.insert("mappings.LogMapping.logsLogbooksEntry", hm);
                     }
                 }
                 if (log.getXmlProperties().size() > 0) {
                     for (XmlProperty property : log.getXmlProperties()) {
                         if (property.getName().isEmpty() || property.getName() == null) {
                             throw new CFException(Response.Status.NOT_FOUND,
                                     "Property name (key) can not be null ");
                         }
                         hm.clear();
                         hm.put("lid", logId);
                         hm.put("name", property.getName());
                         hm.put("value", property.getValue());
                         ss.insert("mappings.PropertyMapping.addProperty", hm);
                     }
                 }
             } else {
                 throw new CFException(Response.Status.NOT_FOUND,
                         "Log could not be created");
             }
 
             ss.commit();
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
 
         return log;
     }
 
     /**
      * Check if log already exist
      *
      * @return TRUE if log exists
      */
     private static boolean logIdExists(XmlLog log) throws CFException {
 
         SqlSession ss = ssf.openSession();
 
         try {
             if (log.getId() == null) {
                 return false;
             }
             
             ArrayList<XmlLog> result = (ArrayList<XmlLog>) ss.selectList("mapping.LogMapping.doesLogExist", log.getId());
 
             if (result != null) {
                 return true;
             } else {
                 return false;
             }
 
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
 
 
     }
 
     /**
      * Compute md5 for 10 most recent log entries from this log id
      *
      * Empty created timestamps are NOT allowed.
      *
      * @return md5Recent String of the last 10 md5Entries
      */
     private static String getmd5Recent(Long logId) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             String md5Recent = "";
 
             ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getPast10md5s", logId);
             if (logs != null) {
                 Iterator<XmlLog> iterator = logs.iterator();
                 while (iterator.hasNext()) {
                     XmlLog log = iterator.next();
                     md5Recent += log.getId() + " " + log.getMD5Entry() + "\n";
                 }
             }
 
             return md5Recent;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
 
 
     }
 
     /**
      * Calculate the md5 for the XmlLog object
      *
      * @return md5Entry String MD5 encoded XmlLog Object
      * @todo Move this to LogEnt as a private function
      */
     private static String getmd5Entry(Long logId, XmlLog log) throws UnsupportedEncodingException, NoSuchAlgorithmException, CFException {
         String entry;
         String explodeRecent = "";
         List<String> explodeRecentArray = new ArrayList<String>();
         explodeRecentArray = Arrays.asList(getmd5Recent(logId).split("\n"));
 
         for (String line : explodeRecentArray) {
             if ((line == null ? "" == null : line.equals("")) || (line == null ? "\n" == null : line.equals("\n"))) {
                 continue;
             }
             explodeRecent += "md5_recent:" + line + "\n";
         }
 
         entry = "id:" + logId + "\n"
                 + "level:" + log.getLevel() + "\n"
                 + "subject:" + log.getSubject() + "\n"
                 + "description:" + log.getDescription() + "\n"
                 + "created:" + log.getCreatedDate() + "\n"
                 + "modified" + log.getModifiedDate() + "\n"
                 + "source:" + log.getSource() + "\n"
                 + "owner:" + log.getOwner() + "\n"
                 + explodeRecent;
 
         byte[] bytesOfMessage = entry.getBytes("UTF-8");
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] md5Entry = md.digest(bytesOfMessage);
         BigInteger md5Number = new BigInteger(1, md5Entry);
         String md5EntryString = md5Number.toString(16);
 
         return md5EntryString;
     }
 }

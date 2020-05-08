 package org.pharmgkb.util;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: Jul 16, 2010
  * Time: 9:55:50 AM
  */
 public class IcpcUtils {
   public static final String NA = "NA";
 
   private static final Logger sf_logger = Logger.getLogger(IcpcUtils.class);
   private static final Pattern sf_alleleRegex = Pattern.compile("\\*\\d+");
 
   public static boolean isBlank(String string) {
     String trimString = StringUtils.trimToNull(string);
    return StringUtils.isBlank(trimString) || trimString.equalsIgnoreCase("na") || trimString.equalsIgnoreCase("n/a") || trimString.equalsIgnoreCase("unknown");
   }
 
   public static File getOutputFile(File inputFile) {
     Date date = new Date();
     SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");
     String newExtension = new StringBuilder()
         .append(".")
         .append(sdf.format(date))
         .append(".xls")
         .toString();
 
     return new File(inputFile.getAbsolutePath().replaceAll("\\.xls", newExtension));  
   }
 
   /**
    * This method takes a String allele and returns a stripped version of that allele.  This way we don't have to store
    * every version of each allele.  For instance, *4K is stripped down to *4 for processing and mapping.
    * @param allele a String allele
    * @return a stripped version of <code>allele</code>
    */
   public static String alleleStrip(String allele) {
     String alleleClean = null;
 
     Matcher m = sf_alleleRegex.matcher(allele);
     if (allele.equalsIgnoreCase("Unknown")) {
       alleleClean = "Unknown";
     }
     else if (m.find()) {
       alleleClean = allele.substring(m.start(),m.end());
       if (allele.toLowerCase().contains("xn")) {
         alleleClean += "XN";
       }
       else if (allele.equalsIgnoreCase("*2a")) {
         alleleClean = "*2A";
       }
     }
     else {
       sf_logger.warn("Malformed allele found: " + allele);
     }
 
     return alleleClean;
   }
 
   public void writeData(Session session, String subjectId, String key, String value) {
     SQLQuery deleteQuery = session.createSQLQuery("delete from sampleProperties where subjectId=:subjectId and datakey=:datakey");
     SQLQuery insertQuery = session.createSQLQuery("insert into sampleProperties(subjectId,datakey,datavalue) values (:subjectId,:datakey,:datavalue)");
 
     int deleteCount = deleteQuery.setParameter("subjectId",subjectId).setParameter("datakey",key).executeUpdate();
     int insertCount = insertQuery.setParameter("subjectId",subjectId).setParameter("datakey",key).setParameter("datavalue",value).executeUpdate();
 
     sf_logger.debug(deleteCount+" records deleted, "+insertCount+" records inserted");
   }
 
 }

 package info.mikaelsvensson.ftpbackup.command.job;
 
 import javax.xml.bind.annotation.XmlEnumValue;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public enum ArchiveFileNameTemplate {
     @XmlEnumValue("dot-date")
     DOT_DATE {
         @Override
         public String getArchivedFileName(String name, Date date) {
             return name + "." + dateFormat.format(date);
         }
 
         private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
         /**
          *                  original file name:       ->|  |<-
          *                  archiving date and time:           |<---------------------------------------------------------------------------------------->|
          *                  archiving date:                    |<--------------------------------------------->|
          *                  archiving time:                                                                      |<-------------------------------------->|
          */
        private Pattern datePattern = Pattern.compile("^(.*)\\.((19|20)[0-9]{2}(0[0-9]|1[0-2])([012][0-9]|3[01])-([01][0-9]|2[0-3])[0-5][0-9][0-5][0-9])$");
 
         @Override
         public Date getArchivingDate(String filename) {
             Matcher matcher = datePattern.matcher(filename);
             if (matcher.matches()) {
                 try {
                     String formattedDate = matcher.group(2);
                     return dateFormat.parse(formattedDate);
                 } catch (ParseException e) {
                     return null;
                 }
             }
             return null;
         }
 
         @Override
         public String getOriginalFileName(String filename) {
             Matcher matcher = datePattern.matcher(filename);
             if (matcher.matches()) {
                 return matcher.group(1);
             }
             return null;
         }
     };
 
 // -------------------------- OTHER METHODS --------------------------
 
     public abstract String getArchivedFileName(String name, Date date);
 
     public abstract String getOriginalFileName(String filename);
 
     public boolean isArchivedFile(String filename) {
         return getArchivingDate(filename) != null;
     }
 
     public abstract Date getArchivingDate(String filename);
 }

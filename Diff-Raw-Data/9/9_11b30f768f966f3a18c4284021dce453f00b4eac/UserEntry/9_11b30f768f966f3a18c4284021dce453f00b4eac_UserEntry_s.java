 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package UserClasses;
 
 /**
  * User object in system. 
  * Contains all user properties.
  * @author Nepochatov Stanislav
  */
 public class UserEntry extends Generic.CsvElder {
     
     /**
      * a2 endian constructor
      * @param givenCsv csv raw line
      * @since RibbonServer a2
      */
    UserEntry(String givenCsv) {
         this.baseCount = 4;
         this.groupCount = 1;
         this.currentFormat = Generic.CsvElder.csvFormatType.ComplexCsv;
         java.util.ArrayList<String[]> parsedStruct = Generic.CsvFormat.fromCsv(this, givenCsv);
         String[] baseArray = parsedStruct.get(0);
         String[] groupsArray = parsedStruct.get(1);
         USER_NAME = baseArray[0];
         COMM = baseArray[1];
         H_PASSWORD = baseArray[2];
         IS_ENABLED = baseArray[3].equals("1") ? true : false;
         GROUPS = groupsArray;
     }
         
     /**
      * User name
      */
     public String USER_NAME;
 
     /**
      * MD5 hashsumm of password
      * @since RibbonServer a2
      */
     public String H_PASSWORD;
 
     /**
      * Comment string
      * @since RibbonServer a2
      */
     public String COMM;
 
     /**
      * Array with groups
      * @since RibbonServer a2
      */
     public String[] GROUPS;
 
     /**
      * State of users account
      * @since RibbonServer a2
      */
     public Boolean IS_ENABLED;
 
     @Override
     public String toCsv() {
         return "{" + this.USER_NAME + "},{" + this.COMM + "}," + Generic.CsvFormat.renderGroup(GROUPS) + "," + this.H_PASSWORD + "," + (IS_ENABLED ? "1" : "0");
     }
 }

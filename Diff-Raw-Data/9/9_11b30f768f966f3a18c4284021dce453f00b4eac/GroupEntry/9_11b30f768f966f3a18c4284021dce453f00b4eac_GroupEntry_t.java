 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package UserClasses;
 
 /**
  * Group entry class. 
  * Contatins all information about single group record.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class GroupEntry extends Generic.CsvElder{
     
     /**
      * Name of the group
      */
     public String GROUP_NAME;
 
     /**
      * Group commentary
      */
     public String COMM;
 
    public GroupEntry(String givenCsv) {
         this.baseCount = 2;
         this.currentFormat = csvFormatType.SimpleCsv;
         String[] parsedStruct = Generic.CsvFormat.fromCsv(this, givenCsv).get(0);
         GROUP_NAME = parsedStruct[0];
         COMM = parsedStruct[1];
     }
 
     @Override
     public String toCsv() {
         return "{" + this.GROUP_NAME + "},{" + this.COMM + "}";
     }
 }

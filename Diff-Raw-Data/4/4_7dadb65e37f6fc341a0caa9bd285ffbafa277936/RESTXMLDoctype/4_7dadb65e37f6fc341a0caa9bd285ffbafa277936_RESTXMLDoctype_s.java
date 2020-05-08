 package org.jboss.pressgang.ccms.rest.v1.entities.enums;
 
 public enum RESTXMLDoctype {
    DOCBOOK_45("Docbook 4.5"),
    DOCBOOK_50("Docbook 5.0");
 
     String commonName;
 
     /**
      * @param commonName The common name for the format
      */
     RESTXMLDoctype(final String commonName) {
         this.commonName = commonName;
     }
 
     public String getCommonName() {
         return commonName;
     }
 
     public static RESTXMLDoctype getXMLDoctype(final int id) {
         switch (id) {
             case 0:
                 return DOCBOOK_45;
             case 1:
                 return DOCBOOK_50;
             default:
                 return null;
         }
     }
 
     public static Integer getXMLDoctypeId(final RESTXMLDoctype doctype) {
         if (doctype == null) return null;
 
         switch (doctype) {
             case DOCBOOK_45:
                 return 0;
             case DOCBOOK_50:
                 return 1;
             default:
                 return null;
         }
     }
 
 
 }

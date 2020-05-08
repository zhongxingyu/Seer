 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package iniconfigurationmanager;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public class ConfigLine {
 
     public static final String SECTION_DEFINITION_START = "[";
 
     public static final String SECTION_DEFINITION_END = "]";
 
     public static final String COMMENT_START = "#";
 
     public static final String EQUALS_SIGN = "=";
     
     private String text;
 
     public ConfigLine( String text ) {
         this.text = text;
     }
 
     public String getText() {
         return text;
     }
     
     public boolean isSectionHeader() {
         return text.startsWith( SECTION_DEFINITION_START );
     }
 
     public boolean isComment() {
         return text.trim().startsWith( COMMENT_START );
     }
 
     public boolean isItemDefinition() {
         return text.indexOf( EQUALS_SIGN ) != -1;
     }
   
 }

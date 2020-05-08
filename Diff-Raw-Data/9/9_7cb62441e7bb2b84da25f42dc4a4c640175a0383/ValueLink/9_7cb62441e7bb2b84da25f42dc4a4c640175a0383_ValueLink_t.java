 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package iniconfigurationmanager.parsing;
 
 import iniconfigurationmanager.schema.LinkVisitor;
 import iniconfigurationmanager.schema.ConfigurationData;
 import iniconfigurationmanager.schema.SectionData;
 import iniconfigurationmanager.schema.OptionData;
import iniconfigurationmanager.schema.SchemaError;
 import java.util.List;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public class ValueLink {
 
     private String sectionName;
 
     private String optionName;
 
     private ConfigurationData configuration;
 
     
     public ValueLink( String link, ConfigurationData configuration ) {
         this.sectionName = getSectionName( link );
         this.optionName = getOptionName( link );
         this.configuration = configuration;
     }
 
 
     private String getSectionName( String link ) {
         return link.substring( 2, link.indexOf("#") );
     }
 
 
     private String getOptionName( String link ) {
         return link.substring( link.indexOf("#") + 1, link.length() - 1 );
     }
     
 
     public <T> List< T > getValues( T type ) {
         LinkVisitor< T > visitor = new LinkVisitor< T >();
         getLinkedOption().accept( visitor );
 
         return visitor.getValues();
     }
 
 
     public SectionData getLinkedSection() {
         if( configuration.hasSection( sectionName ) ) {
             return configuration.getSection( sectionName );
         } else {
            throw new IllegalStateException( String.format(
                    SchemaError.NULL_SECTION_DATA.getMessage(), sectionName ) );
         }
     }
 
     public OptionData getLinkedOption() {
         SectionData section = getLinkedSection();
 
         if( section.hasOption( optionName ) ) {
             return section.getOption( optionName );
         } else {
            throw new IllegalStateException( String.format(
                    SchemaError.NULL_OPTION_DATA.getMessage(), optionName ) );
         }
     }
 
     @Override
     public String toString() {
         return String.format( Format.LINK_FORMAT,
                 getLinkedOption().getCanonicalName() );
     }
    
 }

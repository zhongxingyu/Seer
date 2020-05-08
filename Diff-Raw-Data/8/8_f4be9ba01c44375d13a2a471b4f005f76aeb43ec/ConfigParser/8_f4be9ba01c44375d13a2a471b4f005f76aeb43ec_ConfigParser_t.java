 
 package iniconfigurationmanager;
 
 import iniconfigurationmanager.items.StringConfigItem;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author Ondrej Klejch <ondrej.klejch@gmail.com>
  */
 public class ConfigParser {
 
     public static final char WHITESPACE = ' ';
 
     public static final char ESCAPE = '\\';
     
     public static final String NEWLINE = System.getProperty("line.separator");
 
     private static final Pattern VALID_NAME_PATTERN =
         Pattern.compile("[a-zA-Z.:$][a-zA-Z0-9_~.:$ -]*");
 
     private static final Pattern COMMA_DELIMITER_PATTERN =
         Pattern.compile("(?<!\\\\),");
 
     private static final Pattern COLON_DELIMITER_PATTERN =
         Pattern.compile("(?<!\\\\):");
 
     private static final Pattern NO_DELIMITER_PATTERN = COMMA_DELIMITER_PATTERN;
     
     private ConfigSchema schema;
 
     private ConfigData configuration;
 
     private ConfigSection currentSection;
     
     private StringBuilder currentComment;
     
 
     public ConfigParser( ConfigSchema schema, ConfigData configuration ) {
         this.schema = schema;
         this.configuration = configuration;
         this.configuration.setSchema( schema );
         this.currentComment = new StringBuilder();
     }
 
     
     public ConfigData parse( List<ConfigLine> lines ) {
         for( ConfigLine line : lines) {
             if( line.isComment() ) {
                 parseComment( line );
             } else if (line.isSectionHeader()) {
                 parseSectionHeader( line );
             } else if ( line.isItemDefinition() ) {
                 parseItemDefinition( line );
             } else {
                 //@TODO report error in the configuration file
             }
         }
 
         return configuration;
     }
 
 
     private void parseComment( ConfigLine line ) {
         currentComment.append( line.getText() );
         currentComment.append( NEWLINE );
     }
 
     
     private void parseSectionHeader( ConfigLine line ) {
         String name = getSectionName( line );
 
         if( schema.hasSection( name )) {
             currentSection = schema.getSection( name );
         } else {
             currentSection = new ConfigSection( name );
         }
 
         configuration.addSection( name, currentSection );
     }
 
     
     private String getSectionName( ConfigLine line ) {
         String text = line.getText();
         String name = text.substring(
             text.indexOf( ConfigLine.SECTION_DEFINITION_START ) + 1,
            text.indexOf( ConfigLine.SECTION_DEFINITION_END )
         );
 
         if( ! isValidName( name ) ) {
             //@TODO handle invalid name
         }
 
         return name;
     }
     
 
     private void parseItemDefinition( ConfigLine line ) {
         String name = getItemName( line );
         List< String > values = getItemValues( line );
 
         if( ! currentSection.hasItem( name) ) {
             currentSection.addItem( name, new StringConfigItem( name ) );
         } 
 
         currentSection.getItem( name ).setValues( values );
     }
 
     
     private String getItemName( ConfigLine line ) {
         String text = line.getText();
         int equalsSignPosition = text.indexOf( ConfigLine.EQUALS_SIGN );
        String name = trim( text.substring(0, equalsSignPosition ) );
 
         if( ! isValidName( name ) ) {
             //@TODO handle invalid name
         }
         
         return name;
     }
 
 
     private boolean isValidName( String name ) {
         return match( VALID_NAME_PATTERN, name );
     }
 
     
     private List< String > getItemValues( ConfigLine line ) {
         String text = line.getText();
         int equalsSignPosition = text.indexOf( ConfigLine.EQUALS_SIGN );
         String value = text.substring(equalsSignPosition + 1);
 
         return splitValues( trim( value ) );
     }
 
 
     private List< String > splitValues( String values ) {
         Pattern delimiter = getDelimiterPattern( values );
         String[] valuesArray = delimiter.split( values );
 
         return new LinkedList< String >( Arrays.asList( valuesArray ) );
     }
 
     
     private Pattern getDelimiterPattern( String values ) {
         if( isDelimitedByComma( values ) ) {
             return COMMA_DELIMITER_PATTERN;
         } else if ( isDelimitedByColon( values ) ) {
             return COLON_DELIMITER_PATTERN;
         } else {
             return NO_DELIMITER_PATTERN;
         }
     }
 
     
     private boolean isDelimitedByComma( String values ) {
         return match( COMMA_DELIMITER_PATTERN, values );
     }
 
 
     private boolean isDelimitedByColon( String values ) {
         return match( COLON_DELIMITER_PATTERN, values );
     }
 
     
     private boolean match(Pattern pattern, String text) {
         Matcher m = pattern.matcher( text );
 
         return m.matches();
     }
     
     
     private String trim( String text ) {
         int start = 0;
         while(
             start < text.length() &&
             text.charAt(start) == WHITESPACE
         ) {
             start++;
         }
 
         int end = text.length() - 1;
         while(
             end > start + 1 &&
             text.charAt( end ) == WHITESPACE &&
             text.charAt( end - 1 ) != ESCAPE
         ) {
             end--;
         }
 
        return text.substring( start, end + 1 );
     }
     
 }

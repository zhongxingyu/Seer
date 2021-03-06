 package org.unigram.docvalidator;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.unigram.docvalidator.util.DVCharacter;
 
 /**
  * Contain the default symbols and characters.
  */
 public final class DefaultSymbols {
   /**
    * Get the specified character or symbol.
    * @param name name of symbol
    * @return specified character
    */
   public static DVCharacter get(String name) {
     if (!SYMBOL_TABLE.containsKey(name)) {
       LOG.info(name + " is not defined in DefaultSymbols.");
       return null;
     }
     return SYMBOL_TABLE.get(name);
   }
 
   /**
    * Return all the names of registered characters.
    * @return all names of characters
    */
   public static Iterator<String> getAllCharacterNames() {
     return SYMBOL_TABLE.keySet().iterator();
   }
 
   private static Map<String, DVCharacter> SYMBOL_TABLE
     = new HashMap<String, DVCharacter>();
 
   static {
     /******************************************************************
      * Common symbols
      ******************************************************************/
 
     SYMBOL_TABLE.put("SPACE", new DVCharacter("SPACE", " ", "", false, false));
     SYMBOL_TABLE.put("EXCLAMATION_MARK",
         new DVCharacter("EXCLAMATION_MARK", "!", "", false, false));
     SYMBOL_TABLE.put("LEFT_SINGLE_QUOTATION_MARK",
         new DVCharacter("LEFT_SINGLE_QUOTATION_MARK", "\'", "", false, false));
     SYMBOL_TABLE.put("RIGHT_SINGLE_QUOTATION_MARK",
         new DVCharacter("RIGHT_SINGLE_QUOTATION_MARK", "\'", "", false, false));
     SYMBOL_TABLE.put("LEFT_DOUBLE_QUOTATION_MARK",
         new DVCharacter("LEFT_DOUBLE_QUOTATION_MARK", "\"", "", false, false));
     SYMBOL_TABLE.put("RIGHT_DOBULE_QUOTATION_MARK",
         new DVCharacter("RIGHT_DOBULE_QUOTATION_MARK", "\"", "", false, false));
     SYMBOL_TABLE.put("NUMBER_SIGN",
         new DVCharacter("NUMBER_SIGN", "#", "", false, false));
     SYMBOL_TABLE.put("DOLLAR_SIGN",
        new DVCharacter("DOLLAR_SIGN", "$", "", false, false));
     SYMBOL_TABLE.put("PERCENT_SIGN",
         new DVCharacter("PERCENT_SIGN", "%", "", false, false));
    SYMBOL_TABLE.put("QUESTION_MARK",
        new DVCharacter("QUESTION_MARK", "?", "", false, false));
     SYMBOL_TABLE.put("AMPERSAND",
         new DVCharacter("AMPERSAND", "&", "", false, false));
     SYMBOL_TABLE.put("LEFT_PARENTHESIS",
         new DVCharacter("LEFT_PARENTHESIS", "(", "", false, false));
     SYMBOL_TABLE.put("RIGHT_PARENTHESIS",
         new DVCharacter("RIGHT_PARENTHESIS", ")", "", false, false));
     SYMBOL_TABLE.put("ASTERISK",
         new DVCharacter("ASTERISK", ",", "", false, false));
     SYMBOL_TABLE.put("COMMA", new DVCharacter("COMMA", ",", "", false, false));
     SYMBOL_TABLE.put("COMMENT",
         new DVCharacter("COMMENT", "#", "", false, false));
     SYMBOL_TABLE.put("FULL_STOP",
         new DVCharacter("FULL_STOP", ".", "", false, false));
     SYMBOL_TABLE.put("PLUSH_SIGN", new DVCharacter("+", ",", "", false, false));
     SYMBOL_TABLE.put("HYPHEN_SIGN",
         new DVCharacter("-", ",", "", false, false));
     SYMBOL_TABLE.put("MINUS_SIGN", new DVCharacter("-", ",", "", false, false));
     SYMBOL_TABLE.put("SLASH", new DVCharacter("/", ",", "", false, false));
     SYMBOL_TABLE.put("COLON", new DVCharacter(":", ",", "", false, false));
     SYMBOL_TABLE.put("semicolon", new DVCharacter(";", ",", "", false, false));
     SYMBOL_TABLE.put("LESS_THAN_SIGN",
         new DVCharacter("<", ",", "", false, false));
     SYMBOL_TABLE.put("EQUAL_SIGN", new DVCharacter("=", ",", "", false, false));
     SYMBOL_TABLE.put("GREATER_THAN_SIGN",
         new DVCharacter(">", ",", "", false, false));
     SYMBOL_TABLE.put("AT_MARK", new DVCharacter("@", ",", "", false, false));
     SYMBOL_TABLE.put("LEFT_SQUARE_BRACKET",
         new DVCharacter("[", ",", "", false, false));
     SYMBOL_TABLE.put("RIGHT_SQUARE_BRACKET",
         new DVCharacter("[", ",", "", false, false));
     SYMBOL_TABLE.put("BACKSLASH", new DVCharacter("\\", ",", "", false, false));
     SYMBOL_TABLE.put("CIRCUMFLEX_ACCENT",
         new DVCharacter("^", ",", "", false, false));
     SYMBOL_TABLE.put("LOW_LINE", new DVCharacter("_", ",", "", false, false));
     SYMBOL_TABLE.put("LEFT_CURLY_BRACKET",
         new DVCharacter("{", ",", "", false, false));
     SYMBOL_TABLE.put("RIGHT_CURLY_BRACKET",
         new DVCharacter("}", ",", "", false, false));
     SYMBOL_TABLE.put("VERTICAL_BAR",
         new DVCharacter("|", ",", "", false, false));
     SYMBOL_TABLE.put("TILDE", new DVCharacter("TILDE", "~", "", false, false));
 
     /******************************************************************
      * Digits
      ******************************************************************/
 
     SYMBOL_TABLE.put("DIGIT_ZERO", new DVCharacter("0", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_ONE", new DVCharacter("1", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_TWO", new DVCharacter("2", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_THREE",
         new DVCharacter("3", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_FOUR", new DVCharacter("4", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_FIVE", new DVCharacter("5", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_SIX", new DVCharacter("6", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_SEVEN",
         new DVCharacter("7", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_EIGHT",
         new DVCharacter("8", ",", "", false, false));
     SYMBOL_TABLE.put("DIGIT_NINE", new DVCharacter("9", ",", "", false, false));
   }
 
   private static Logger LOG = LoggerFactory.getLogger(DefaultSymbols.class);
 
   private DefaultSymbols() { }
 }

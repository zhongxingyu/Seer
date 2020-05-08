 package com.undeadscythes.hyperform;
 
 import com.undeadscythes.betterreader.*;
 import java.io.*;
 import java.util.*;
 import java.util.logging.*;
 
 /**
  * {@link HyperForm} stores all the operations that should be executed during
  * parsing and also handles parsing and {@link HyperText} generation.
  *
  * @author UndeadScythes
  */
 public class HyperForm {
     private static final Logger LOGGER = Logger.getLogger(HyperForm.class.getName());
     private static final String PREFIX = "<?ged ";
     private static final String PREFIX_REGEX = "<\\?ged ";
    private static final String SUFFIX = " ?>";
    private static final String SUFFIX_REGEX = " \\?>";
 
     private final Map<String, HyperOp> map = new HashMap<String, HyperOp>(0);
 
     /**
      * Load an {@link HyperOp} and a {@link String} that will appear in an
      * external markup to initiate the operation.
      */
     public void load(final String string, final HyperOp operation) {
         map.put(string, operation);
     }
 
     /**
      * Load an {@link HyperType#INSERT INSERT}ing {@link HyperOp} with a given
      * {@link HyperText}.
      */
     public void load(final String string, final HyperText text) {
         load(string, new HyperOp(HyperType.INSERT, text));
     }
 
     /**
      * Load a {@link HyperType#REPLACE REPLACE}ing {@link HyperOp} with the
      * given {@link String} to be used as the replacement text.
      */
     public void load(final String string, final String replacement) {
         load(string, new HyperOp(HyperType.REPLACE, replacement));
     }
 
     /**
      * Parse an {@link InputStream} and return an {@link HyperText} containing
      * the parsed markup.
      */
     public HyperText parse(final InputStream input) {
         final StringBuilder markup = new StringBuilder("");
         final BetterReader reader = new BetterReader(input);
         while (reader.hasNext()) {
             markup.append(parseLine(reader.getLine(), reader.getLineNo())).append("\n");
         }
         return new HyperText(markup.toString());
     }
 
     /**
      * Parse an {@link HyperText} and return another {@link HyperText}
      * containing the parsed markup.
      */
     public HyperText parse(final HyperText text) {
         final StringBuilder markup = new StringBuilder("");
         final String[] lines = text.getMarkup().split("\n");
         for (int i = 0; i < lines.length; i++) {
             markup.append(parseLine(lines[i], i)).append("\n");
         }
         return new HyperText(markup.toString());
     }
 
     private String parseLine(final String input, final int lineNo) {
         String line = input;
         while (line.contains(PREFIX) && line.contains(SUFFIX)) {
             final String var = line.split(PREFIX_REGEX)[1].split(SUFFIX_REGEX)[0];
             final String replace = PREFIX + var + SUFFIX;
            final HyperOp operation = map.get(var);
             if (operation == null) {
                 LOGGER.warning("Unrecognized variable '" + var + "' on line " + lineNo + ".");
                 line = line.replace(replace, "");
                 continue;
             }
             final Object action = operation.getAction();
             switch (operation.getType()) {
                 case REPLACE:
                     line = line.replace(replace, (String)action);
                     break;
                 case SCRIPT:
                     final HyperScript script = ((HyperScript)operation.getAction());
                     line = line.replace(replace, script.insert(var));
                     break;
                 case INSERT:
                     line = line.replace(replace, ((HyperText)action).getMarkup());
                     break;
                 case PARSE:
                     line = line.replace(replace, parse(getClass().getResourceAsStream((String)action)).getMarkup());
                     break;
                 default:
                     LOGGER.warning("Unrecognized HyperType '" + operation.toString() + "' on line " + lineNo + ".");
             }
         }
         return line;
     }
 
     /**
      * Concatenate an array of {@link HyperText}s.
      */
     public HyperText concat(final HyperText[] texts) {
         if (texts.length == 0) {
             return new HyperText("");
         }
         HyperText result = new HyperText(texts[0]);
         for (int i = 1; i < texts.length; i++) {
             result = new HyperText(result, texts[i]);
         }
         return result;
     }
 
     /**
      * Write the contents of an {@link HyperText} to the given {@link File}.
      */
     public void publish(final HyperText text, final File file) {
         try {
             final BufferedWriter output = new BufferedWriter(new FileWriter(file));
             output.write(text.getMarkup());
             output.close();
         } catch (IOException ex) {
             LOGGER.warning(ex.getMessage());
         }
     }
 }

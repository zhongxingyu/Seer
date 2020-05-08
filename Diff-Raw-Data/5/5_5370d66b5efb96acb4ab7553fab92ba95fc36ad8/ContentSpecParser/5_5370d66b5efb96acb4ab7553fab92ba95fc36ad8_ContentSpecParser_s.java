 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static java.lang.String.format;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.PatternSyntaxException;
 
 import com.google.code.regexp.Matcher;
 import com.google.code.regexp.Pattern;
 import org.jboss.pressgang.ccms.contentspec.Appendix;
 import org.jboss.pressgang.ccms.contentspec.Chapter;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.File;
 import org.jboss.pressgang.ccms.contentspec.FileList;
 import org.jboss.pressgang.ccms.contentspec.KeyValueNode;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.Part;
 import org.jboss.pressgang.ccms.contentspec.Process;
 import org.jboss.pressgang.ccms.contentspec.Section;
 import org.jboss.pressgang.ccms.contentspec.SpecNode;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.TextNode;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.InjectionOptions;
 import org.jboss.pressgang.ccms.contentspec.entities.Relationship;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.contentspec.enums.RelationshipType;
 import org.jboss.pressgang.ccms.contentspec.exceptions.IndentationException;
 import org.jboss.pressgang.ccms.contentspec.exceptions.ParsingException;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.exceptions.InvalidKeyValueException;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.VariableSet;
 import org.jboss.pressgang.ccms.contentspec.processor.utils.ProcessorUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.common.StringUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 
 /**
  * A class that parses a Content Specification and stores the parsed data into a ContentSpec Object. The Object then
  * contains all of the
  * Levels, Topics and relationships to be passed for validation or saving.
  *
  * @author lnewson
  * @author alabbas
  */
 public class ContentSpecParser {
     private static final Pattern LEVEL_PATTERN = Pattern.compile(ProcessorConstants.LEVEL_REGEX);
     private static final Pattern SQUARE_BRACKET_PATTERN = Pattern.compile(format(ProcessorConstants.BRACKET_NAMED_PATTERN, '[', ']'));
     private static final Pattern RELATION_ID_LONG_PATTERN = Pattern.compile(ProcessorConstants.RELATION_ID_LONG_PATTERN);
     private static final Pattern FILE_ID_LONG_PATTERN = Pattern.compile(ProcessorConstants.FILE_ID_LONG_PATTERN);
 
     /**
      * An Enumerator used to specify the parsing mode of the Parser.
      */
     public static enum ParsingMode {
         /**
          * The Content Spec should be Parsed as a new Content Spec.
          */
         NEW,
         /**
          * The Content Spec should be Parsed as an edited Content Spec.
          */
         EDITED,
         /**
          * The Parser shouldn't care if the Content Spec is new or edited.
          */
         EITHER
     }
 
     private final DataProviderFactory providerFactory;
     private final TopicProvider topicProvider;
     private final ErrorLogger log;
     private final ErrorLoggerManager loggerManager;
 
     private int spaces = 2;
     private ContentSpec spec;
     private int indentationLevel = 0;
     private HashMap<String, SpecTopic> specTopics = new HashMap<String, SpecTopic>();
     private HashMap<String, Level> targetLevels = new HashMap<String, Level>();
     private HashMap<String, Level> externalTargetLevels = new HashMap<String, Level>();
     private HashMap<String, SpecTopic> targetTopics = new HashMap<String, SpecTopic>();
     private HashMap<String, List<Relationship>> relationships = new HashMap<String, List<Relationship>>();
     private ArrayList<Process> processes = new ArrayList<Process>();
     private Set<String> parsedMetaDataKeys = new HashSet<String>();
     private Level lvl = null;
     private int lineCounter = 0;
     private LinkedList<String> lines = new LinkedList<String>();
 
     /**
      * Constructor
      *
      * @param providerFactory The Factory to produce various different Entity DataProviders.
      * @param loggerManager   The Logging Manager that contains any errors/warnings produced while parsing.
      */
     public ContentSpecParser(final DataProviderFactory providerFactory, final ErrorLoggerManager loggerManager) {
         this.providerFactory = providerFactory;
         topicProvider = providerFactory.getProvider(TopicProvider.class);
         this.loggerManager = loggerManager;
         log = loggerManager.getLogger(ContentSpecParser.class);
     }
 
     protected int getLineCount() {
         return lineCounter;
     }
 
     protected int getIndentationLevel() {
         return indentationLevel;
     }
 
     protected void setIndentationLevel(int indentationLevel) {
         this.indentationLevel = indentationLevel;
     }
 
     protected int getIndentationSize() {
         return spaces;
     }
 
     protected void setIndentationSize(int indentationSize) {
         spaces = indentationSize;
     }
 
     protected Level getCurrentLevel() {
         return lvl;
     }
 
     protected void setCurrentLevel(final Level level) {
         this.lvl = level;
     }
 
     /**
      * Get the list of lines that are to be processed from a Content Specification File/String.
      *
      * @return The list of lines in the content spec.
      */
     protected LinkedList<String> getLines() {
         return lines;
     }
 
     /**
      * Gets the Content Specification Topics inside of a content specification
      *
      * @return The mapping of topics to their unique Content Specification Topic ID's
      */
     protected HashMap<String, SpecTopic> getSpecTopics() {
         return specTopics;
     }
 
     /**
      * Gets a list of processes that were parsed in the content specification
      *
      * @return A List of Processes
      */
     protected List<Process> getProcesses() {
         return processes;
     }
 
     /**
      * Gets a list of Content Specification Topics that were parsed as being targets
      *
      * @return A list of Content Specification Topics mapped by their Target ID.
      */
     protected HashMap<String, SpecTopic> getTargetTopics() {
         return targetTopics;
     }
 
     /**
      * Gets a list of Levels that were parsed as being targets.
      *
      * @return A List of Levels mapped by their Target ID.
      */
     protected HashMap<String, Level> getTargetLevels() {
         return targetLevels;
     }
 
     /**
      * Gets a list of External Levels that were parsed as being targets.
      *
      * @return A List of External Levels mapped by their Target ID.
      */
     protected HashMap<String, Level> getExternalTargetLevels() {
         return externalTargetLevels;
     }
 
     /**
      * Gets the relationships that were created when parsing the Content Specification.
      *
      * @return The map of Unique id's to relationships
      */
     protected HashMap<String, List<Relationship>> getRelationships() {
         return relationships;
     }
 
     /**
      * Parse a Content Specification to put the string into usable objects that can then be validate.
      *
      * @param contentSpec A string representation of the Content Specification.
      * @return True if everything was parsed successfully otherwise false.
      */
     public boolean parse(final String contentSpec) {
         return parse(contentSpec, ParsingMode.EITHER);
     }
 
     /**
      * Parse a Content Specification to put the string into usable objects that can then be validate.
      *
      * @param contentSpec A string representation of the Content Specification.
      * @param mode        The mode in which the Content Specification should be parsed.
      * @return True if everything was parsed successfully otherwise false.
      */
     public boolean parse(final String contentSpec, final ParsingMode mode) {
         return parse(contentSpec, mode, false);
     }
 
     /**
      * Parse a Content Specification to put the string into usable objects that can then be validate.
      *
      * @param contentSpec      A string representation of the Content Specification.
      * @param mode             The mode in which the Content Specification should be parsed.
      * @param processProcesses Whether or not processes should call the data provider to be processed.
      * @return True if everything was parsed successfully otherwise false.
      */
     public boolean parse(final String contentSpec, final ParsingMode mode, final boolean processProcesses) {
         // Reset the variables
         reset();
 
         // Read in the file contents
         final BufferedReader br = new BufferedReader(new StringReader(contentSpec));
         try {
             readFileData(br);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         // Process the spec contents.
         return processSpec(spec, mode, processProcesses);
     }
 
     /**
      * Reset all of the variables used during parsing.
      */
     protected void reset() {
         // Clear the logs
         log.clearLogs();
 
         spaces = 2;
         spec = new ContentSpec();
         indentationLevel = 0;
         specTopics = new HashMap<String, SpecTopic>();
         targetLevels = new HashMap<String, Level>();
         externalTargetLevels = new HashMap<String, Level>();
         targetTopics = new HashMap<String, SpecTopic>();
         relationships = new HashMap<String, List<Relationship>>();
         processes = new ArrayList<Process>();
         lines = new LinkedList<String>();
         parsedMetaDataKeys = new HashSet<String>();
         lvl = null;
         lineCounter = 0;
     }
 
     /**
      * Gets a list of Topic ID's that are used in a Content Specification.
      *
      * @return A List of topic ID's.
      */
     public List<Integer> getReferencedTopicIds() {
         final Set<Integer> ids = new HashSet<Integer>();
         for (final Map.Entry<String, SpecTopic> entry : getSpecTopics().entrySet()) {
             final SpecTopic specTopic = entry.getValue();
             if (specTopic.getDBId() != null) {
                 ids.add(specTopic.getDBId());
             }
         }
 
         return CollectionUtilities.toArrayList(ids);
     }
 
     /**
      * Gets a list of Topic ID's that are used in a Content Specification.
      * The list only includes topics that don't reference a revision of a
      * topic.
      *
      * @return A List of topic ID's.
      */
     public List<Integer> getReferencedLatestTopicIds() {
         final Set<Integer> ids = new HashSet<Integer>();
         for (final Map.Entry<String, SpecTopic> entry : getSpecTopics().entrySet()) {
             final SpecTopic specTopic = entry.getValue();
             if (specTopic.getDBId() != null && specTopic.getRevision() == null) {
                 ids.add(specTopic.getDBId());
             }
         }
 
         return CollectionUtilities.toArrayList(ids);
     }
 
     /**
      * Gets a list of Topic ID's that are used in a Content Specification.
      * The list only includes topics that reference a topic revision rather
      * then the latest topic revision.
      *
      * @return A List of topic ID's.
      */
     public List<Pair<Integer, Integer>> getReferencedRevisionTopicIds() {
         final Set<Pair<Integer, Integer>> ids = new HashSet<Pair<Integer, Integer>>();
         for (final Map.Entry<String, SpecTopic> entry : getSpecTopics().entrySet()) {
             final SpecTopic specTopic = entry.getValue();
             if (specTopic.getDBId() != null && specTopic.getRevision() != null) {
                 ids.add(new Pair<Integer, Integer>(specTopic.getDBId(), specTopic.getRevision()));
             }
         }
 
         return CollectionUtilities.toArrayList(ids);
     }
 
     /**
      * Get the Content Specification object that represents a Content Specification
      *
      * @return The Content Specification object representation.
      */
     public ContentSpec getContentSpec() {
         return spec;
     }
 
     /**
      * Reads the data from a file that is passed into a BufferedReader and processes it accordingly.
      *
      * @param br A BufferedReader object that has been initialised with a file's data.
      * @throws IOException Thrown if an IO Error occurs while reading from the BufferedReader.
      */
     protected void readFileData(final BufferedReader br) throws IOException {
         // Read in the entire file so we can peek ahead later on
         String line;
         while ((line = br.readLine()) != null) {
             getLines().add(line);
         }
     }
 
     /**
      * Starting method to process a Content Specification string into a ContentSpec object.
      *
      * @param contentSpec      The content spec object to process the string into.
      * @param mode             The mode to parse the string as.
      * @param processProcesses Whether or not processes should call the data provider to be processed.
      * @return True if the content spec was processed successfully otherwise false.
      */
     protected boolean processSpec(final ContentSpec contentSpec, final ParsingMode mode, final boolean processProcesses) {
         // Find the first line that isn't a blank line or a comment
         while (getLines().peek() != null) {
             final String input = getLines().peek();
             if (isCommentLine(input) || isBlankLine(input)) {
                 lineCounter++;
 
                 if (isCommentLine(input)) {
                     contentSpec.appendComment(input);
                 } else if (isBlankLine(input)) {
                     contentSpec.appendChild(new TextNode("\n"));
                 }
 
                 getLines().poll();
                 continue;
             } else {
                 // We've found the first line so break the loop
                 break;
             }
         }
 
         // Process the content spec depending on the mode
         if (mode == ParsingMode.NEW) {
             return processNewSpec(contentSpec, processProcesses);
         } else if (mode == ParsingMode.EDITED) {
             return processEditedSpec(contentSpec, processProcesses);
         } else {
             return processEitherSpec(contentSpec, processProcesses);
         }
     }
 
     /**
      * Process a New Content Specification. That is that it should start with a Title, instead of a CHECKSUM and ID.
      *
      * @param contentSpec      The content spec object to parse the content spec into.
      * @param processProcesses If processes should be processed to populate the relationships.
      * @return True if the content spec was processed successfully otherwise false.
      */
     protected boolean processNewSpec(final ContentSpec contentSpec, final boolean processProcesses) {
         final String input = getLines().poll();
 
         lineCounter++;
         try {
             final Pair<String, String> keyValuePair = ProcessorUtilities.getAndValidateKeyValuePair(input);
             final String key = keyValuePair.getFirst();
             final String value = keyValuePair.getSecond();
 
             if (key.equalsIgnoreCase(CommonConstants.CS_TITLE_TITLE)) {
                 contentSpec.setTitle(value);
 
                 // Process the rest of the spec now that we know the start is correct
                 return processSpecContents(contentSpec, processProcesses);
             } else if (key.equalsIgnoreCase(CommonConstants.CS_CHECKSUM_TITLE)) {
                 log.error(ProcessorConstants.ERROR_INCORRECT_NEW_MODE_MSG);
                 return false;
             } else {
                 log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG);
                 return false;
             }
         } catch (InvalidKeyValueException e) {
             log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG, e);
             return false;
         }
     }
 
     /**
      * Process an Edited Content Specification. That is that it should start with a CHECKSUM and ID, instead of a Title.
      *
      * @param contentSpec      The content spec object to parse the content spec into.
      * @param processProcesses If processes should be processed to populate the relationships.
      * @return True if the content spec was processed successfully otherwise false.
      */
     protected boolean processEditedSpec(final ContentSpec contentSpec, final boolean processProcesses) {
         final String input = getLines().poll();
 
         lineCounter++;
         try {
             final Pair<String, String> keyValuePair = ProcessorUtilities.getAndValidateKeyValuePair(input);
             final String key = keyValuePair.getFirst();
             final String value = keyValuePair.getSecond();
 
             if (key.equalsIgnoreCase(CommonConstants.CS_CHECKSUM_TITLE)) {
                 contentSpec.setChecksum(value);
 
                 // Read in the Content Spec ID
                 final String specIdLine = getLines().poll();
                 lineCounter++;
                 if (specIdLine != null) {
                     final Pair<String, String> specIdPair = ProcessorUtilities.getAndValidateKeyValuePair(specIdLine);
                     final String specIdKey = specIdPair.getFirst();
                     final String specIdValue = specIdPair.getSecond();
                     if (specIdKey.equalsIgnoreCase(CommonConstants.CS_ID_TITLE)) {
                         int contentSpecId;
                         try {
                             contentSpecId = Integer.parseInt(specIdValue);
                         } catch (NumberFormatException e) {
                             log.error(format(ProcessorConstants.ERROR_INVALID_CS_ID_FORMAT_MSG, specIdLine.trim()));
                             return false;
                         }
                         contentSpec.setId(contentSpecId);
 
                         return processSpecContents(contentSpec, processProcesses);
                     } else {
                         log.error(ProcessorConstants.ERROR_CS_NO_CHECKSUM_MSG);
                         return false;
                     }
                 } else {
                     log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG);
                     return false;
                 }
             } else if (key.equalsIgnoreCase(CommonConstants.CS_ID_TITLE)) {
                 int contentSpecId;
                 try {
                     contentSpecId = Integer.parseInt(value);
                 } catch (NumberFormatException e) {
                     log.error(format(ProcessorConstants.ERROR_INVALID_CS_ID_FORMAT_MSG, input.trim()));
                     return false;
                 }
                 contentSpec.setId(contentSpecId);
 
                 return processSpecContents(contentSpec, processProcesses);
             } else {
                 log.error(ProcessorConstants.ERROR_INCORRECT_EDIT_MODE_MSG);
                 return false;
             }
         } catch (InvalidKeyValueException e) {
             log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG, e);
             return false;
         }
     }
 
     /**
      * Process Content Specification that is either NEW or EDITED. That is that it should start with a CHECKSUM and ID or a Title.
      *
      * @param contentSpec      The content spec object to parse the content spec into.
      * @param processProcesses If processes should be processed to populate the relationships.
      * @return True if the content spec was processed successfully otherwise false.
      */
     protected boolean processEitherSpec(final ContentSpec contentSpec, final boolean processProcesses) {
         try {
             final Pair<String, String> keyValuePair = ProcessorUtilities.getAndValidateKeyValuePair(getLines().peek());
             final String key = keyValuePair.getFirst();
 
             if (key.equalsIgnoreCase(CommonConstants.CS_CHECKSUM_TITLE) || key.equalsIgnoreCase(CommonConstants.CS_ID_TITLE)) {
                 return processEditedSpec(contentSpec, processProcesses);
             } else {
                 return processNewSpec(contentSpec, processProcesses);
             }
         } catch (InvalidKeyValueException e) {
             log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG, e);
             return false;
         }
     }
 
     /**
      * Process the contents of a content specification and parse it into a ContentSpec object.
      *
      * @param contentSpec      The content spec object to parse the content spec into.
      * @param processProcesses If processes should be processed to populate the relationships.
      * @return True if the contents were processed successfully otherwise false.
      */
     protected boolean processSpecContents(final ContentSpec contentSpec, final boolean processProcesses) {
         setCurrentLevel(contentSpec.getBaseLevel());
         boolean error = false;
         while (getLines().peek() != null) {
             lineCounter++;
             // Process the content specification and print an error message if an error occurs
             try {
                 if (!parseLine(contentSpec, getLines().poll(), getLineCount())) {
                     error = true;
                 }
             } catch (IndentationException e) {
                 log.error(e.getMessage());
                 return false;
             }
         }
 
         // Before validating the content specification, processes should be loaded first so that the
         // relationships and targets are created
         if (processProcesses) {
             for (final Process process : getProcesses()) {
                 process.processTopics(getSpecTopics(), getTargetTopics(), topicProvider);
             }
         }
 
         // Setup the relationships
         processRelationships();
 
         return !error;
     }
 
     /**
      * Processes a line of the content specification and stores it in objects
      *
      * @param contentSpec The content spec object to add the lines content to.
      * @param line        A line of input from the content specification
      * @return True if the line of input was processed successfully otherwise false.
      * @throws IndentationException Thrown if any invalid indentation occurs.
      */
     protected boolean parseLine(final ContentSpec contentSpec, final String line, int lineNumber) throws IndentationException {
         assert line != null;
 
         // Trim the whitespace
         final String trimmedLine = line.trim();
 
         // If the line is a blank or a comment, then nothing needs processing. So add the line and return
         if (isBlankLine(trimmedLine) || isCommentLine(trimmedLine)) {
             return parseEmptyOrCommentLine(contentSpec, line);
         } else {
             // Calculate the lines indentation level
             int lineIndentationLevel = calculateLineIndentationLevel(line, lineNumber);
             if (lineIndentationLevel > getIndentationLevel()) {
                 // The line was indented without a new level so throw an error
                 throw new IndentationException(format(ProcessorConstants.ERROR_INCORRECT_INDENTATION_MSG, lineNumber, trimmedLine));
             } else if (lineIndentationLevel < getIndentationLevel()) {
                 // The line has left the previous level so move the current level up to the right level
                 Level newCurrentLevel = getCurrentLevel();
                 for (int i = (getIndentationLevel() - lineIndentationLevel); i > 0; i--) {
                     if (newCurrentLevel.getParent() != null) {
                         newCurrentLevel = newCurrentLevel.getParent();
                     }
                 }
                 changeCurrentLevel(newCurrentLevel, lineIndentationLevel);
             }
 
             // Process the line based on what type the line is
             try {
                 if (isMetaDataLine(trimmedLine)) {
                     parseMetaDataLine(contentSpec, line, lineNumber);
                 } else if (isLevelLine(trimmedLine)) {
                     final Level level = parseLevelLine(trimmedLine, lineNumber);
                     if (level instanceof Process) {
                         getProcesses().add((Process) level);
                     }
                     getCurrentLevel().appendChild(level);
 
                     // Change the current level to use the new parsed level
                     changeCurrentLevel(level, getIndentationLevel() + 1);
                     //            } else if (trimmedLine.toUpperCase(Locale.ENGLISH).matches("^CS[ ]*:.*")) {
                     //                processExternalLevelLine(getCurrentLevel(), line);
                 } else if (StringUtilities.indexOf(trimmedLine, '[') == 0 && getCurrentLevel().getLevelType() == LevelType.BASE) {
                     parseGlobalOptionsLine(getCurrentLevel(), line, lineNumber);
                 } else {
                     // Process a new topic
                     final SpecTopic tempTopic = parseTopic(trimmedLine, lineNumber);
                     // Adds the topic to the current level
                     getCurrentLevel().appendSpecTopic(tempTopic);
 
                 }
             } catch (ParsingException e) {
                 log.error(e.getMessage());
                 return false;
             }
 
             return true;
         }
     }
 
     /**
      * Calculates the indentation level of a line using the amount of whitespace and the parsers indentation size setting.
      *
      * @param line The line to calculate the indentation for.
      * @return The lines indentation level.
      * @throws IndentationException Thrown if the indentation for the line isn't valid.
      */
     protected int calculateLineIndentationLevel(final String line, int lineNumber) throws IndentationException {
         char[] lineCharArray = line.toCharArray();
         int indentationCount = 0;
         // Count the amount of whitespace characters before any text to determine the level
         if (Character.isWhitespace(lineCharArray[0])) {
             for (char c : lineCharArray) {
                 if (Character.isWhitespace(c)) {
                     indentationCount++;
                 } else {
                     break;
                 }
             }
             if (indentationCount % getIndentationSize() != 0) {
                 throw new IndentationException(format(ProcessorConstants.ERROR_INCORRECT_INDENTATION_MSG, lineNumber, line.trim()));
             }
         }
 
         return indentationCount / getIndentationSize();
     }
 
     /**
      * Checks to see if a line is represents a Content Specifications Meta Data.
      *
      * @param line The line to be checked.
      * @return True if the line is meta data, otherwise false.
      */
     protected boolean isMetaDataLine(String line) {
         return getCurrentLevel().getLevelType() == LevelType.BASE && line.trim().matches("^\\w[\\w\\.\\s]+=.*");
     }
     /**
      * Checks to see if a line is a blank/empty line.
      *
      * @param line The line to be checked.
      * @return True if the line is an blank line, otherwise false.
      */
     protected boolean isBlankLine(String line) {
         return line.trim().isEmpty();
     }
 
     /**
      * Checks to see if a line is represents a Content Specifications comment.
      *
      * @param line The line to be checked.
      * @return True if the line is a comment, otherwise false.
      */
     protected boolean isCommentLine(String line) {
         return line.trim().startsWith("#");
     }
 
     /**
      * Checks to see if a line is represents a Content Specifications Level.
      *
      * @param line The line to be checked.
      * @return True if the line is meta data, otherwise false.
      */
     protected boolean isLevelLine(String line) {
         final Matcher matcher = LEVEL_PATTERN.matcher(line.trim().toUpperCase(Locale.ENGLISH));
         return matcher.find();
     }
 
     /**
      * Processes a line that represents a comment or an empty line in a Content Specification.
      *
      * @param contentSpec The content spec object to add the comment/empty line to.
      * @param line        The line to be processed.
      * @return True if the line was processed without errors, otherwise false.
      */
     protected boolean parseEmptyOrCommentLine(final ContentSpec contentSpec, final String line) {
         if (isBlankLine(line)) {
             if (getCurrentLevel().getLevelType() == LevelType.BASE) {
                 contentSpec.appendChild(new TextNode("\n"));
             } else {
                 getCurrentLevel().appendChild(new TextNode("\n"));
             }
             return true;
         } else {
             if (getCurrentLevel().getLevelType() == LevelType.BASE) {
                 contentSpec.appendComment(line);
             } else {
                 getCurrentLevel().appendComment(line);
             }
             return true;
         }
     }
 
     /**
      * Processes a line that represents the start of a Content Specification Level. This method creates the level based on the data in
      * the line and then changes the current processing level to the new level.
      *
      * @param line The line to be processed as a level.
      * @return True if the line was processed without errors, otherwise false.
      */
     protected Level parseLevelLine(final String line, int lineNumber) throws ParsingException {
         String tempInput[] = StringUtilities.split(line, ':', 2);
         // Remove the whitespace from each value in the split array
         tempInput = CollectionUtilities.trimStringArray(tempInput);
 
         if (tempInput.length >= 1) {
             final LevelType levelType = LevelType.getLevelType(tempInput[0]);
             try {
                 return parseLevel(lineNumber, levelType, line);
             } catch (ParsingException e) {
                 log.error(e.getMessage());
                 // Create a basic level so the rest of the spec can be processed
                 return createEmptyLevelFromType(lineNumber, levelType, line);
             }
         } else {
             throw new ParsingException(format(ProcessorConstants.ERROR_LEVEL_FORMAT_MSG, lineNumber, line));
         }
     }
 
 //    /**
 //     * TODO The external level processing still needs to be implemented. DO NOT use this method at this time.
 //     *
 //     * @param line        The line to be processed.
 //     */
 //    protected void processExternalLevelLine(final Level currentLevel, final String line) throws ParsingException {
 //        String splitVars[] = StringUtilities.split(line, ':', 2);
 //        // Remove the whitespace from each value in the split array
 //        splitVars = CollectionUtilities.trimStringArray(splitVars);
 //
 //        // Get the mapping of variables
 //        HashMap<RelationshipType, String[]> variableMap = getLineVariables(splitVars[1], '[', ']', ',', false);
 //        final String title = StringUtilities.replaceEscapeChars(getTitle(splitVars[1], '['));
 //        processExternalLevel(currentLevel, variableMap.get(RelationshipType.EXTERNAL_CONTENT_SPEC)[0], title, line);
 //    }
 
     /**
      * Processes a line that represents some Meta Data in a Content Specification.
      *
      * @param contentSpec The content spec object to add the meta data to.
      * @param line        The line to be processed.
      */
     protected void parseMetaDataLine(final ContentSpec contentSpec, final String line, int lineNumber) throws ParsingException {
         // Parse the line and break it up into the key/value pair
         Pair<String, String> keyValue = null;
         try {
             keyValue = ProcessorUtilities.getAndValidateKeyValuePair(line);
         } catch (InvalidKeyValueException e) {
             throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_METADATA_FORMAT_MSG, lineNumber, line));
         }
 
         final String key = keyValue.getFirst();
         final String value = keyValue.getSecond();
 
        if (parsedMetaDataKeys.contains(key)) {
             throw new ParsingException(format(ProcessorConstants.ERROR_DUPLICATE_METADATA_FORMAT_MSG, lineNumber, key, line));
         } else {
            parsedMetaDataKeys.add(key);
 
             // first deal with metadata that is used by the parser or needs to be parsed further
             if (key.equalsIgnoreCase(CSConstants.SPACES_TITLE)) {
                 // Read in the amount of spaces that were used for the content specification
                 try {
                     setIndentationSize(Integer.parseInt(value));
                     if (getIndentationSize() <= 0) {
                         setIndentationSize(2);
                     }
                 } catch (Exception e) {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_NUMBER_MSG, lineNumber, line));
                 }
             } else if (key.equalsIgnoreCase(CSConstants.DEBUG_TITLE)) {
                 if (value.equals("1")) {
                     log.setVerboseDebug(1);
                 } else if (value.equals("2")) {
                     log.setVerboseDebug(2);
                 } else if (value.equals("0")) {
                     log.setVerboseDebug(0);
                 } else {
                     log.warn(ProcessorConstants.WARN_DEBUG_IGNORE_MSG);
                 }
             } else if (key.equalsIgnoreCase(CommonConstants.CS_PUBLICAN_CFG_TITLE)) {
                 int startingPos = StringUtilities.indexOf(value, '[');
                 if (startingPos != -1) {
                     final StringBuilder cfg = new StringBuilder(value);
                     // If the ']' character isn't on this line try the next line
                     if (StringUtilities.indexOf(cfg.toString(), ']') == -1) {
                         cfg.append("\n");
 
                         // Read the next line and increment counters
                         String newLine = getLines().poll();
                         while (newLine != null) {
                             cfg.append(newLine).append("\n");
                             lineCounter++;
                             // If the ']' character still isn't found keep trying
                             if (StringUtilities.lastIndexOf(cfg.toString(), ']') == -1) {
                                 newLine = getLines().poll();
                             } else {
                                 break;
                             }
                         }
                     }
 
                     // Check that the ']' character was found and that it was found before another '[' character
                     final String finalCfg = cfg.toString().trim();
                     if (StringUtilities.lastIndexOf(finalCfg, ']') == -1 || StringUtilities.lastIndexOf(finalCfg, '[') != startingPos) {
                         throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_PUBLICAN_CFG_MSG, lineNumber,
                                 key + " = " + finalCfg.replaceAll("\n", "\n          ")));
                     } else {
                         contentSpec.setPublicanCfg(ProcessorUtilities.replaceEscapeChars(finalCfg).substring(1, finalCfg.length() - 1));
                     }
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_PUBLICAN_CFG_MSG, lineNumber, line));
                 }
             } else if (key.equalsIgnoreCase(CommonConstants.CS_INLINE_INJECTION_TITLE)) {
                 final InjectionOptions injectionOptions = new InjectionOptions();
                 String[] types = null;
                 if (StringUtilities.indexOf(value, '[') != -1) {
                     if (StringUtilities.indexOf(value, ']') != -1) {
                         final Matcher matcher = SQUARE_BRACKET_PATTERN.matcher(value);
 
                         // Find all of the variables inside of the brackets defined by the regex
                         while (matcher.find()) {
                             final String topicTypes = matcher.group(ProcessorConstants.BRACKET_CONTENTS);
                             types = StringUtilities.split(topicTypes, ',');
                             for (final String type : types) {
                                 injectionOptions.addStrictTopicType(type.trim());
                             }
                         }
                     } else {
                         throw new ParsingException(
                                 format(ProcessorConstants.ERROR_NO_ENDING_BRACKET_MSG + ProcessorConstants.CSLINE_MSG, lineNumber, ']', line));
                     }
                 }
                 String injectionSetting = getTitle(value, '[');
                 if (injectionSetting.trim().equalsIgnoreCase("on")) {
                     if (types != null) {
                         injectionOptions.setContentSpecType(InjectionOptions.UserType.STRICT);
                     } else {
                         injectionOptions.setContentSpecType(InjectionOptions.UserType.ON);
                     }
                 } else if (injectionSetting.trim().equalsIgnoreCase("off")) {
                     injectionOptions.setContentSpecType(InjectionOptions.UserType.OFF);
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_INJECTION_MSG, lineNumber, line));
                 }
                 contentSpec.setInjectionOptions(injectionOptions);
             } else if (key.equalsIgnoreCase(CommonConstants.CS_FILE_TITLE) || key.equalsIgnoreCase(CommonConstants.CS_FILE_SHORT_TITLE)) {
                 final FileList files = parseFilesMetaData(value, lineNumber, line);
                 contentSpec.appendKeyValueNode(files);
             } else if (ContentSpecUtilities.isSpecTopicMetaData(key)) {
                 final SpecTopic specTopic = parseSpecTopicMetaData(value, key, lineNumber);
                 contentSpec.appendKeyValueNode(new KeyValueNode<SpecTopic>(key, specTopic, lineNumber));
             } else {
                 try {
                     final KeyValueNode<String> node = new KeyValueNode<String>(key, value, lineNumber);
                     contentSpec.appendKeyValueNode(node);
                 } catch (NumberFormatException e) {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_METADATA_FORMAT_MSG, lineNumber, line));
                 }
             }
         }
     }
 
     /**
      * TODO
      *
      * @param value
      * @param key
      * @return
      */
     private SpecTopic parseSpecTopicMetaData(final String value, final String key, final int lineNumber) throws ParsingException {
         if (value.trim().startsWith("[") && value.trim().endsWith("]")) {
             final String topicString = key + " " + value.trim();
             return parseTopic(topicString, lineNumber);
         } else {
             if (value.trim().startsWith("[")) {
                 throw new ParsingException(format(ProcessorConstants.ERROR_NO_ENDING_BRACKET_MSG, lineNumber, ']'));
             } else if (value.trim().endsWith("]")) {
                 throw new ParsingException(format(ProcessorConstants.ERROR_NO_OPENING_BRACKET_MSG, lineNumber, '['));
             } else {
                 throw new ParsingException(format(ProcessorConstants.ERROR_NO_BRACKET_MSG, lineNumber, '[', ']'));
             }
         }
     }
 
     /**
      * Parse an "Additional Files" metadata component into a List of {@link org.jboss.pressgang.ccms.contentspec.File} objects.
      *
      * @param value The value of the key value pair
      * @param lineNumber The line number of the additional files key
      * @param line The full line of the key value pair
      * @return A list of parsed File objects.
      * @throws ParsingException Thrown if an error occurs during parsing.
      */
     protected FileList parseFilesMetaData(final String value, final int lineNumber, final String line) throws ParsingException {
         int startingPos = StringUtilities.indexOf(value, '[');
         if (startingPos != -1) {
             final List<File> files = new LinkedList<File>();
             final HashMap<RelationshipType, String[]> variables = getLineVariables(value,lineNumber, '[', ']', ',', true);
             final String[] vars = variables.get(RelationshipType.NONE);
 
             // Loop over each file found and parse it
             for (final String var : vars) {
                 final File file = parseFileMetaData(var, lineNumber);
                 if (file != null) {
                     files.add(file);
                 }
             }
 
             return new FileList(CommonConstants.CS_FILE_TITLE, files, lineNumber);
         } else {
             throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_FILES_MSG, lineNumber, line));
         }
     }
 
     /**
      * Parse a File MetaData component into a {@link org.jboss.pressgang.ccms.contentspec.File} object.
      *
      * @param line The line to be parsed.
      * @param lineNumber The line number of the line being parsed.
      * @return A file object initialised with the data from the line.
      * @throws ParsingException Thrown if the line contains invalid content and couldn't be parsed.
      */
     protected File parseFileMetaData(final String line, final int lineNumber) throws ParsingException {
         final File file;
         if (line.matches(ProcessorConstants.FILE_ID_REGEX)) {
             file = new File(Integer.parseInt(line));
         } else if (FILE_ID_LONG_PATTERN.matcher(line).matches()) {
             final Matcher matcher = FILE_ID_LONG_PATTERN.matcher(line);
 
             matcher.find();
             final String id = matcher.group("ID");
             final String title = matcher.group("Title");
             final String rev = matcher.group("REV");
             file = new File(title.trim(), Integer.parseInt(id));
 
             if (rev != null) {
                 file.setRevision(Integer.parseInt(rev));
             }
         } else {
             throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_FILE, lineNumber));
         }
 
         return file;
     }
 
     /**
      * Processes a line that represents the Global Options for the Content Specification.
      *
      * @param line The line to be processed.
      * @return True if the line was processed without errors, otherwise false.
      */
     protected boolean parseGlobalOptionsLine(final Level currentLevel, final String line, int lineNumber) throws ParsingException {
         // Read in the variables from the line
         final HashMap<RelationshipType, String[]> variableMap = getLineVariables(line, lineNumber, '[', ']', ',', false);
 
         // Check the read in values are valid
         if (!variableMap.containsKey(RelationshipType.NONE)) {
             throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineNumber, line));
         } else if (variableMap.size() > 1) {
             throw new ParsingException(format(ProcessorConstants.ERROR_RELATIONSHIP_BASE_LEVEL_MSG, lineNumber, line));
         }
         String[] variables = variableMap.get(RelationshipType.NONE);
 
         // Check that some options were found, if so then parse them
         if (variables.length > 0) {
             addOptions(currentLevel, variables, 0, line, lineNumber);
         } else {
             log.warn(format(ProcessorConstants.WARN_EMPTY_BRACKETS_MSG, lineNumber));
         }
 
         return true;
     }
 
     /**
      * Changes the current level that content is being processed for to a new level.
      *
      * @param newLevel            The new level to process for,
      * @param newIndentationLevel The new indentation level of the level in the Content Specification.
      */
     protected void changeCurrentLevel(final Level newLevel, int newIndentationLevel) {
         indentationLevel = newIndentationLevel;
         setCurrentLevel(newLevel);
     }
 
     /**
      * Processes the input to create a new topic
      *
      * @param line The line of input to be processed
      * @return A topics object initialised with the data from the input line.
      * @throws ParsingException Thrown if the line can't be parsed as a Topic, due to incorrect syntax.
      */
     protected SpecTopic parseTopic(final String line, int lineNumber) throws ParsingException {
         final SpecTopic tempTopic = new SpecTopic(null, lineNumber, line, null);
 
         // Process a new topic
         String[] variables;
         // Read in the variables inside of the brackets
         final HashMap<RelationshipType, String[]> variableMap = getLineVariables(line, lineNumber, '[', ']', ',', false);
         if (!variableMap.containsKey(RelationshipType.NONE)) {
             throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_TOPIC_FORMAT_MSG, lineNumber, line));
         }
         variables = variableMap.get(RelationshipType.NONE);
         int varStartPos = 2;
 
         // Process and validate the Types & ID
         if (variables.length >= 2) {
             // Check the type and the set it
             if (CSConstants.NEW_TOPIC_ID_PATTERN.matcher(variables[0]).matches()) {
                 if (variables[1].matches("^C:[ ]*[0-9]+$")) {
                     variables[0] = "C" + variables[1].replaceAll("^C:[ ]*", "");
                 } else {
                     tempTopic.setType(ProcessorUtilities.replaceEscapeChars(variables[1]));
                 }
             }
             // If we have two variables for a existing topic then check to see if the second variable is the revision
             else if (variables[0].matches(CSConstants.EXISTING_TOPIC_ID_REGEX)) {
                 // Check if the existing topic has a revision specified. If so parse it otherwise the var is a normal option
                 if (variables[1].toLowerCase(Locale.ENGLISH).matches("^rev[ ]*:.*")) {
                     String[] vars = variables[1].split(":");
                     vars = CollectionUtilities.trimStringArray(vars);
 
                     try {
                         tempTopic.setRevision(Integer.parseInt(vars[1]));
                     } catch (NumberFormatException ex) {
                         throw new ParsingException(format(ProcessorConstants.ERROR_TOPIC_INVALID_REVISION_FORMAT, lineNumber, line));
                     }
                 } else {
                     varStartPos = 1;
                 }
             } else {
                 varStartPos = 1;
             }
         } else if (variables.length == 1) {
             if (!variables[0].matches("(" + CSConstants.DUPLICATE_TOPIC_ID_REGEX + ")|(" + CSConstants.CLONED_TOPIC_ID_REGEX + ")|(" +
                     CSConstants.EXISTING_TOPIC_ID_REGEX + ")|(" + CSConstants.NEW_TOPIC_ID_REGEX + ")|(" +
                     CSConstants.CLONED_DUPLICATE_TOPIC_ID_REGEX + ")")) {
                 throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_TITLE_ID_MSG, lineNumber, line));
             } else if (CSConstants.NEW_TOPIC_ID_PATTERN.matcher(variables[0]).matches()) {
                 throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_TYPE_TITLE_ID_MSG, lineNumber, line));
             }
             varStartPos = 1;
         } else {
             throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_TITLE_ID_MSG, lineNumber, line));
         }
 
         // Set the title
         String title = ProcessorUtilities.replaceEscapeChars(getTitle(line, '['));
         tempTopic.setTitle(title);
 
         // Set the topic ID
         tempTopic.setId(variables[0]);
 
         /*
          * Set the Unique ID for the topic. If the ID is already unique and not
          * duplicated then just set the id (e.g. N1). Otherwise create the Unique ID
          * using the line number and topic ID.
          */
         String uniqueId = variables[0];
         if (CSConstants.NEW_TOPIC_ID_PATTERN.matcher(variables[0]).matches() && !variables[0].equals("N") &&
                 !getSpecTopics().containsKey(variables[0])) {
             getSpecTopics().put(uniqueId, tempTopic);
         } else if (variables[0].equals("N") || variables[0].matches(CSConstants.DUPLICATE_TOPIC_ID_REGEX) ||
                 variables[0].matches(CSConstants.CLONED_DUPLICATE_TOPIC_ID_REGEX) || variables[0].matches(
                 CSConstants.CLONED_TOPIC_ID_REGEX) || variables[0].matches(CSConstants.EXISTING_TOPIC_ID_REGEX)) {
             uniqueId = "L" + Integer.toString(lineNumber) + "-" + variables[0];
             getSpecTopics().put(uniqueId, tempTopic);
         } else if (variables[0].startsWith("N") && CSConstants.NEW_TOPIC_ID_PATTERN.matcher(variables[0]).matches()) {
             throw new ParsingException(format(ProcessorConstants.ERROR_DUPLICATE_ID_MSG, lineNumber, variables[0], line));
         } else {
             throw new ParsingException(format(ProcessorConstants.ERROR_TOPIC_INVALID_ID_MSG, lineNumber, line));
         }
         tempTopic.setUniqueId(uniqueId);
 
         // Get the options if the topic is a new or cloned topic
         if (variables[0].matches("(" + CSConstants.NEW_TOPIC_ID_REGEX + ")|(" + CSConstants.CLONED_TOPIC_ID_REGEX +
                 ")|(" + CSConstants.EXISTING_TOPIC_ID_REGEX + ")")) {
             addOptions(tempTopic, variables, varStartPos, line, lineNumber);
         } else if (variables.length > varStartPos) {
             // Display warnings if options are specified for duplicated topics
             if (variables[0].matches(CSConstants.DUPLICATE_TOPIC_ID_REGEX) || variables[0].matches(
                     CSConstants.CLONED_DUPLICATE_TOPIC_ID_REGEX)) {
                 log.warn(format(ProcessorConstants.WARN_IGNORE_DUP_INFO_MSG, lineNumber, line));
             }
         }
 
         // Process the Topic Relationships
         processTopicRelationships(tempTopic, variableMap, line, lineNumber);
 
         return tempTopic;
     }
 
     /**
      * Process the relationships parsed for a topic.
      *
      * @param tempTopic   The temporary topic that will be turned into a full topic once fully parsed.
      * @param variableMap The list of variables containing the parsed relationships.
      * @param input       The line representing the topic and it's relationships.
      * @param lineNumber  The number of the line the relationships are on.
      * @throws ParsingException Thrown if the variables can't be parsed due to incorrect syntax.
      */
     protected void processTopicRelationships(final SpecTopic tempTopic, final HashMap<RelationshipType, String[]> variableMap,
             final String input, int lineNumber) throws ParsingException {
         // Process the relationships
         final String uniqueId = tempTopic.getUniqueId();
         final ArrayList<Relationship> topicRelationships = new ArrayList<Relationship>();
 
         // Refer-To relationships
         processTopicRelationshipList(RelationshipType.REFER_TO, tempTopic, variableMap, topicRelationships, lineNumber);
 
         // Prerequisite relationships
         processTopicRelationshipList(RelationshipType.PREREQUISITE, tempTopic, variableMap, topicRelationships, lineNumber);
 
         // Link-List relationships
         processTopicRelationshipList(RelationshipType.LINKLIST, tempTopic, variableMap, topicRelationships, lineNumber);
 
         // Next and Previous relationships should only be created internally and shouldn't be specified by the user
         if (variableMap.containsKey(RelationshipType.NEXT) || variableMap.containsKey(RelationshipType.PREVIOUS)) {
             throw new ParsingException(format(ProcessorConstants.ERROR_TOPIC_NEXT_PREV_MSG, lineNumber, input));
         }
 
         // Add the relationships to the global list if any exist
         if (!topicRelationships.isEmpty()) {
             getRelationships().put(uniqueId, topicRelationships);
         }
 
         // Process targets
         if (variableMap.containsKey(RelationshipType.TARGET)) {
             final String targetId = variableMap.get(RelationshipType.TARGET)[0];
             if (getTargetTopics().containsKey(targetId)) {
                 throw new ParsingException(
                         format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, getTargetTopics().get(targetId).getLineNumber(),
                                 getTargetTopics().get(targetId).getText(), lineNumber, input));
             } else if (getTargetLevels().containsKey(targetId)) {
                 throw new ParsingException(
                         format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, getTargetLevels().get(targetId).getLineNumber(),
                                 getTargetLevels().get(targetId).getText(), lineNumber, input));
             } else {
                 getTargetTopics().put(targetId, tempTopic);
                 tempTopic.setTargetId(targetId);
             }
         }
 
         // Throw an error for external targets
         if (variableMap.containsKey(RelationshipType.EXTERNAL_TARGET)) {
             // TODO Log an error properly using a constant
             throw new ParsingException("Unable to use external targets on topics.");
         }
 
         // Throw an error for external content spec injections
         if (variableMap.containsKey(RelationshipType.EXTERNAL_CONTENT_SPEC)) {
             // TODO Log an error properly using a constant
             throw new ParsingException("Unable to use external content specs as topics.");
         }
     }
 
     /**
      * Processes a list of relationships for a specific relationship type from some line processed variables.
      *
      * @param relationshipType   The relationship type to be processed.
      * @param tempTopic          The temporary topic that will be turned into a full topic once fully parsed.
      * @param variableMap        The list of variables containing the parsed relationships.
      * @param lineNumber         The number of the line the relationships are on.
      * @param topicRelationships The list of topic relationships.
      * @throws ParsingException Thrown if the variables can't be parsed due to incorrect syntax.
      */
     private void processTopicRelationshipList(final RelationshipType relationshipType, final SpecTopic tempTopic,
             final HashMap<RelationshipType, String[]> variableMap, final List<Relationship> topicRelationships,
             int lineNumber) throws ParsingException {
         final String uniqueId = tempTopic.getUniqueId();
 
         String errorMessageFormat = null;
         switch (relationshipType) {
             case REFER_TO:
                 errorMessageFormat = ProcessorConstants.ERROR_INVALID_REFERS_TO_RELATIONSHIP;
                 break;
             case LINKLIST:
                 errorMessageFormat = ProcessorConstants.ERROR_INVALID_LINK_LIST_RELATIONSHIP;
                 break;
             case PREREQUISITE:
                 errorMessageFormat = ProcessorConstants.ERROR_INVALID_PREREQUISITE_RELATIONSHIP;
                 break;
             default:
                 return;
         }
 
         if (variableMap.containsKey(relationshipType)) {
             final String[] relationshipList = variableMap.get(relationshipType);
             for (final String relationshipId : relationshipList) {
                 if (relationshipId.matches(ProcessorConstants.RELATION_ID_REGEX)) {
                     topicRelationships.add(new Relationship(uniqueId, relationshipId, relationshipType));
                 } else if (relationshipId.matches(ProcessorConstants.RELATION_ID_LONG_REGEX)) {
                     final Matcher matcher = RELATION_ID_LONG_PATTERN.matcher(relationshipId);
 
                     matcher.find();
                     final String id = matcher.group("TopicID");
                     final String relationshipTitle = matcher.group("TopicTitle");
 
                     topicRelationships.add(new Relationship(uniqueId, id, relationshipType, ProcessorUtilities.replaceEscapeChars
                             (relationshipTitle.trim())));
                 } else {
                     if (relationshipId.matches("^(" + ProcessorConstants.TARGET_BASE_REGEX + "|[0-9]+).*?(" +
                             ProcessorConstants.TARGET_BASE_REGEX + "|[0-9]+).*")) {
                         throw new ParsingException(format(ProcessorConstants.ERROR_MISSING_SEPARATOR_MSG, lineNumber, ','));
                     } else {
                         throw new ParsingException(format(errorMessageFormat, lineNumber));
                     }
                 }
             }
         }
     }
 
     /**
      * Creates an empty Level using the LevelType to determine which Level subclass to instantiate.
      *
      * @param lineNumber The line number of the level.
      * @param levelType  The Level Type.
      * @param input      The string that represents the level, if one exists,
      * @return The empty Level subclass object, or a plain Level object if no type matches a subclass.
      */
     protected Level createEmptyLevelFromType(final int lineNumber, final LevelType levelType, final String input) {
         // Create the level based on the type
         switch (levelType) {
             case APPENDIX:
                 return new Appendix(null, lineNumber, input);
             case CHAPTER:
                 return new Chapter(null, lineNumber, input);
             case SECTION:
                 return new Section(null, lineNumber, input);
             case PART:
                 return new Part(null, lineNumber, input);
             case PROCESS:
                 return new Process(null, lineNumber, input);
             default:
                 return new Level(null, lineNumber, input, levelType);
         }
     }
 
     /**
      * Processes and creates a level based on the level type.
      *
      * @param lineNumber The line number the level is on.
      * @param levelType  The type the level will represent. ie. A Chapter or Appendix
      * @param line       The chapter string in the content specification.
      * @return The created level or null if an error occurred.
      * @throws ParsingException Thrown if the line can't be parsed as a Level, due to incorrect syntax.
      */
     protected Level parseLevel(final int lineNumber, final LevelType levelType, final String line) throws ParsingException {
         String splitVars[] = StringUtilities.split(line, ':', 2);
         // Remove the whitespace from each value in the split array
         splitVars = CollectionUtilities.trimStringArray(splitVars);
 
         // Create the level based on the type
         final Level newLvl = createEmptyLevelFromType(lineNumber, levelType, line);
 
         // Parse the input
         if (splitVars.length >= 2) {
             final String title = ProcessorUtilities.replaceEscapeChars(getTitle(splitVars[1], '['));
             newLvl.setTitle(title);
             // Get the mapping of variables
             final HashMap<RelationshipType, List<String[]>> variableMap = getLineVariables(splitVars[1], lineNumber, '[', ']', ',', false,
                     true);
             if (variableMap.containsKey(RelationshipType.NONE)) {
                 boolean optionsProcessed = false;
                 for (final String[] variables : variableMap.get(RelationshipType.NONE)) {
                     if (variables.length >= 1) {
                         if (variables[0].matches(CSConstants.ALL_TOPIC_ID_REGEX)) {
                             final String topicString = title + " [" + StringUtilities.buildString(variables, ", ") + "]";
                             final SpecTopic innerTopic = parseTopic(topicString, lineNumber);
                             newLvl.setInnerTopic(innerTopic);
                         } else {
                             // Process the options
                             if (!optionsProcessed) {
                                 addOptions(newLvl, variables, 0, line, lineNumber);
                                 optionsProcessed = true;
                             } else {
                                 throw new ParsingException(
                                         format(ProcessorConstants.ERROR_DUPLICATED_RELATIONSHIP_TYPE_MSG, lineNumber, line));
                             }
                         }
                     }
                 }
             }
 
             // Add targets for the level
             if (variableMap.containsKey(RelationshipType.TARGET)) {
                 final List<String[]> targets = variableMap.get(RelationshipType.TARGET);
                 if (targets.size() == 1) {
                     final String targetId = targets.get(0)[0];
                     if (getTargetTopics().containsKey(targetId)) {
                         throw new ParsingException(
                                 format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, getTargetTopics().get(targetId).getLineNumber(),
                                         getTargetTopics().get(targetId).getText(), lineNumber, line));
                     } else if (getTargetLevels().containsKey(targetId)) {
                         throw new ParsingException(
                                 format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, getTargetLevels().get(targetId).getLineNumber(),
                                         getTargetLevels().get(targetId).getText(), lineNumber, line));
                     } else {
                         getTargetLevels().put(targetId, newLvl);
                         newLvl.setTargetId(targetId);
                     }
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_DUPLICATED_RELATIONSHIP_TYPE_MSG, lineNumber, line));
                 }
             }
 
             // Check for external targets
 //            if (variableMap.containsKey(RelationshipType.EXTERNAL_TARGET)) {
 //                final String externalTargetId = variableMap.get(RelationshipType.EXTERNAL_TARGET).get(0)[0];
 //                getExternalTargetLevels().put(externalTargetId, newLvl);
 //                newLvl.setExternalTargetId(externalTargetId);
 //            }
 
 //            // Check if the level is injecting data from another content spec
 //            if (variableMap.containsKey(RelationshipType.EXTERNAL_CONTENT_SPEC)) {
 //                processExternalLevel(newLvl, variableMap.get(RelationshipType.EXTERNAL_CONTENT_SPEC)[0], title, line);
 //            }
 
             // Check that no relationships were specified for the appendix
             if (variableMap.containsKey(RelationshipType.REFER_TO) || variableMap.containsKey(
                     RelationshipType.PREREQUISITE) || variableMap.containsKey(RelationshipType.NEXT) || variableMap.containsKey(
                     RelationshipType.PREVIOUS) || variableMap.containsKey(RelationshipType.LINKLIST)) {
 
                 // Check that no relationships were specified for the level
                 if (newLvl.getInnerTopic() == null) {
                     throw new ParsingException(
                             format(ProcessorConstants.ERROR_LEVEL_RELATIONSHIP_MSG, lineNumber, CSConstants.CHAPTER, CSConstants.CHAPTER,
                                     line));
                 } else {
                     final HashMap<RelationshipType, String[]> flattenedVariableMap = new HashMap<RelationshipType, String[]>();
                     for (final Map.Entry<RelationshipType, List<String[]>> lineVariable : variableMap.entrySet()) {
                         flattenedVariableMap.put(lineVariable.getKey(), lineVariable.getValue().get(0));
                     }
 
                     processTopicRelationships(newLvl.getInnerTopic(), flattenedVariableMap, line, lineNumber);
                 }
             }
         }
         return newLvl;
     }
 
     /**
      * Gets the variables from a string. The variables are at the end of a line and are inside of the starting and ending delimiter and are
      * separated by the separator.
      *
      * @param line        The line of input to get the variables for.
      * @param lineNumber  TODO
      * @param startDelim  The starting delimiter of the variables.
      * @param endDelim    The ending delimiter of the variables.
      * @param separator   The separator used to separate the variables.
      * @param ignoreTypes Used if all variables are to be stored inside of the Relationship NONE type.
      * @return A Map of String arrays for different relationship. Inside each string array is the singular variables.
      * @throws ParsingException Thrown if the line can't be successfully parsed.
      */
     public HashMap<RelationshipType, String[]> getLineVariables(final String line, int lineNumber, char startDelim, char endDelim,
             char separator, boolean ignoreTypes) throws ParsingException {
         final HashMap<RelationshipType, List<String[]>> lineVariables = getLineVariables(line, lineNumber, startDelim, endDelim, separator,
                 ignoreTypes, false);
 
         final HashMap<RelationshipType, String[]> retValue = new HashMap<RelationshipType, String[]>();
         for (final Map.Entry<RelationshipType, List<String[]>> lineVariable : lineVariables.entrySet()) {
             retValue.put(lineVariable.getKey(), lineVariable.getValue().get(0));
         }
         return retValue;
     }
 
     /**
      * Gets the variables from a string. The variables are at the end of a line and are inside of the starting and ending delimiter and are
      * separated by the separator.
      *
      * @param line        The line of input to get the variables for.
      * @param lineNumber  TODO
      * @param startDelim  The starting delimiter of the variables.
      * @param endDelim    The ending delimiter of the variables.
      * @param separator   The separator used to separate the variables.
      * @param ignoreTypes Used if all variables are to be stored inside of the Relationship NONE type.
      * @param groupTypes  Used if the relationship types should be group if two or more of the same types are found.
      * @return A Map of String arrays for different relationship. Inside each string array is the singular variables.
      * @throws ParsingException Thrown if the line can't be successfully parsed.
      */
     public HashMap<RelationshipType, List<String[]>> getLineVariables(final String line, int lineNumber, final char startDelim,
             final char endDelim, final char separator, final boolean ignoreTypes, final boolean groupTypes) throws ParsingException {
         final HashMap<RelationshipType, List<String[]>> output = new HashMap<RelationshipType, List<String[]>>();
 
         final int lastStartDelimPos = StringUtilities.lastIndexOf(line, startDelim);
         final int lastEndDelimPos = StringUtilities.lastIndexOf(line, endDelim);
 
         // Check that we have variables to process
         if (lastStartDelimPos == -1 && lastEndDelimPos == -1) return output;
 
         final String nextLine = getLines().peek();
 
         /*
          * Check to see if the line doesn't match the regex even once. Also check to see if the next
          * line is a continuation of the current line. If so then attempt to read the next line.
          */
         if (lastEndDelimPos < lastStartDelimPos || (nextLine != null && nextLine.trim().toUpperCase(Locale.ENGLISH).matches("^\\" +
                 startDelim + "[ ]*(R|L|P|T|B).*")) || line.trim().matches("(.|\n|\r\n)*(?<!\\\\)" + separator + "$")) {
             // Read in a new line and increment relevant counters
             String temp = getLines().poll();
             if (temp != null) {
                 lineCounter++;
 
                 return getLineVariables(line + "\n" + temp, lineNumber, startDelim, endDelim, separator, ignoreTypes, groupTypes);
             }
         }
 
         /* Get the variables from the line */
         final List<VariableSet> varSets = findVariableSets(line, startDelim, endDelim);
 
         // Process the variables that were found
         for (final VariableSet set : varSets) {
             // Check that a opening bracket wasn't missed
             if (set.getStartPos() == null) {
                 throw new ParsingException(format(ProcessorConstants.ERROR_NO_OPENING_BRACKET_MSG, lineNumber, startDelim));
             }
 
             // Check that a closing bracket wasn't missed
             if (set.getEndPos() == null) {
                 throw new ParsingException(format(ProcessorConstants.ERROR_NO_ENDING_BRACKET_MSG, lineNumber, endDelim));
             }
 
             final ArrayList<String> variables = new ArrayList<String>();
             final String variableSet = set.getContents().substring(1, set.getContents().length() - 1);
 
             // Split the variables set into individual variables
             final RelationshipType type = getRelationshipType(variableSet);
             if (!ignoreTypes && (type == RelationshipType.REFER_TO || type == RelationshipType.PREREQUISITE || type == RelationshipType
                     .NEXT || type == RelationshipType.PREVIOUS || type == RelationshipType.LINKLIST)) {
                 // Remove the type specifier from the start of the variable set
                 String splitString[] = StringUtilities.split(variableSet.trim(), ':', 2);
                 // Check that there are actually variables set
                 if (splitString.length > 1) {
                     // Replace any inner content with markers, so that any splitting doesn't get messed up
                     final Matcher matcher = SQUARE_BRACKET_PATTERN.matcher(splitString[1]);
                     final HashMap<String, String> replacements = new HashMap<String, String>();
                     int i = 0;
                     String fixedString = splitString[1];
                     while (matcher.find()) {
                         final String replacement = matcher.group(CSConstants.BRACKET_CONTENTS);
                         // If the content has already been replaced then skip it
                         if (!replacements.containsKey(replacement)) {
                             final String marker = "###" + i + "###";
                             replacements.put(replacement, marker);
                             fixedString = fixedString.replace(replacement, marker);
                             i++;
                         }
                     }
 
                     // Split the string and add the variables
                     splitString = StringUtilities.split(fixedString, separator);
                     for (final String s : splitString) {
                         final String var = s.replaceAll("(^\\s*(\r?\n)*)|((\r?\n)*\\s*$)", "");
 
                         // Replace any markers
                         String fixedVar = var.trim();
                         for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                             fixedVar = fixedVar.replace(entry.getValue(), entry.getKey());
                         }
 
                         // Check that a separator wasn't missed.
                         if (StringUtilities.lastIndexOf(var, startDelim) != StringUtilities.indexOf(var, startDelim) || var.indexOf(
                                 '\n') != -1) {
                             throw new ParsingException(format(ProcessorConstants.ERROR_MISSING_SEPARATOR_MSG, lineNumber, separator));
                         } else if (s.trim().isEmpty()) {
                             throw new ParsingException(format(ProcessorConstants.ERROR_MISSING_ATTRIB_FORMAT_MSG, lineNumber, line));
                         } else {
                             variables.add(fixedVar);
                         }
                     }
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineNumber, line));
                 }
             } else if (!ignoreTypes && type == RelationshipType.TARGET) {
                 variables.add(variableSet.replaceAll("\\s", ""));
             } else if (!ignoreTypes && type == RelationshipType.EXTERNAL_TARGET) {
                 variables.add(variableSet.replaceAll("\\s", ""));
             } else if (!ignoreTypes && type == RelationshipType.EXTERNAL_CONTENT_SPEC) {
                 variables.add(variableSet.trim());
             } else if (!variableSet.trim().isEmpty()) {
                 // Replace any inner content with markers, so that any splitting doesn't get messed up
                 final Matcher matcher = SQUARE_BRACKET_PATTERN.matcher(variableSet);
                 final HashMap<String, String> replacements = new HashMap<String, String>();
                 int i = 0;
                 String fixedString = variableSet;
                 while (matcher.find()) {
                     final String replacement = matcher.group(CSConstants.BRACKET_CONTENTS);
                     final String marker = "###" + i + "###";
                     replacements.put(replacement, marker);
                     fixedString = fixedString.replace(replacement, marker);
                     i++;
                 }
 
                 // Normal set of variables that contains the ID and/or tags
                 final String splitString[] = StringUtilities.split(fixedString, separator);
                 for (final String s : splitString) {
                     if (!s.trim().isEmpty()) {
                         // Replace any markers
                         String fixedVar = s.trim();
                         for (final Map.Entry<String, String> entry : replacements.entrySet()) {
                             fixedVar = fixedVar.replace(entry.getValue(), entry.getKey());
                         }
 
                         variables.add(fixedVar);
                     } else {
                         throw new ParsingException(format(ProcessorConstants.ERROR_MISSING_ATTRIB_FORMAT_MSG, lineNumber, line));
                     }
                 }
             }
 
             // Add the variable set to the mapping
             if (output.containsKey(type)) {
                 if (ignoreTypes || groupTypes) {
                     output.get(type).add(variables.toArray(new String[variables.size()]));
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_DUPLICATED_RELATIONSHIP_TYPE_MSG, lineNumber, line));
                 }
             } else {
                 final ArrayList<String[]> list = new ArrayList<String[]>();
                 list.add(variables.toArray(new String[variables.size()]));
                 output.put(type, list);
             }
         }
 
         return output;
     }
 
     /**
      * Processes s string of variables to find the type of relationship that
      * exists within the string.
      *
      * @param variableString The variable string to be processed.
      * @return The relationship type that was found in the string otherwise
      *         a NONE relationship type is returned.
      */
     protected RelationshipType getRelationshipType(final String variableString) {
         final String uppercaseVarSet = variableString.trim().toUpperCase(Locale.ENGLISH);
         if (uppercaseVarSet.matches(ProcessorConstants.RELATED_REGEX)) {
             return RelationshipType.REFER_TO;
         } else if (uppercaseVarSet.matches(ProcessorConstants.PREREQUISITE_REGEX)) {
             return RelationshipType.PREREQUISITE;
         } else if (uppercaseVarSet.matches(ProcessorConstants.NEXT_REGEX)) {
             return RelationshipType.NEXT;
         } else if (uppercaseVarSet.matches(ProcessorConstants.PREV_REGEX)) {
             return RelationshipType.PREVIOUS;
         } else if (uppercaseVarSet.matches(ProcessorConstants.TARGET_REGEX)) {
             return RelationshipType.TARGET;
         } else if (uppercaseVarSet.matches(ProcessorConstants.EXTERNAL_TARGET_REGEX)) {
             return RelationshipType.EXTERNAL_TARGET;
         } else if (uppercaseVarSet.matches(ProcessorConstants.EXTERNAL_CSP_REGEX)) {
             return RelationshipType.EXTERNAL_CONTENT_SPEC;
         } else if (uppercaseVarSet.matches(ProcessorConstants.LINK_LIST_REGEX)) {
             return RelationshipType.LINKLIST;
         } else {
             return RelationshipType.NONE;
         }
     }
 
     /**
      * Adds the options from an array of variables to a node (Level or Topic). It starts checking the variables from the startPos
      * position of the variable array, then check to see if the variable is a tag or attribute and processes it.
      *
      * @param node          The node to add the options to.
      * @param vars          An array of variables to get the options for.
      * @param startPos      The starting position in the variable array to start checking.
      * @param originalInput The original string used to create these options.
      * @throws ParsingException Thrown if the variables can't be successfully parsed as options.
      */
     protected void addOptions(final SpecNode node, final String[] vars, final int startPos, final String originalInput,
             int lineNumber) throws ParsingException {
         // Process each variable in vars starting from the start position
         for (int i = startPos; i < vars.length; i++) {
             String str = vars[i];
             // If the variable contains a "=" then it isn't a tag so process it separately
             if (StringUtilities.indexOf(str, '=') != -1) {
                 String temp[] = StringUtilities.split(str, '=', 2);
                 temp = CollectionUtilities.trimStringArray(temp);
                 if (temp.length == 2) {
                     if (temp[0].equalsIgnoreCase("URL")) {
                         node.addSourceUrl(ProcessorUtilities.replaceEscapeChars(temp[1]));
                     } else if (temp[0].equalsIgnoreCase("description")) {
                         if (node.getDescription(false) == null) {
                             node.setDescription(ProcessorUtilities.replaceEscapeChars(temp[1]));
                         } else {
                            throw new ParsingException(String.format(ProcessorConstants.ERROR_DUPLICATE_ATTRIBUTE_MSG, lineNumber,
                                    "Description", originalInput));
                         }
                     } else if (temp[0].equalsIgnoreCase("Writer")) {
                         if (node.getAssignedWriter(false) == null) {
                             node.setAssignedWriter(ProcessorUtilities.replaceEscapeChars(temp[1]));
                         } else {
                             throw new ParsingException(String.format(ProcessorConstants.ERROR_DUPLICATE_ATTRIBUTE_MSG, lineNumber,
                                     "Writer", originalInput));
                         }
                     } else if (temp[0].equalsIgnoreCase("condition")) {
                         if (node.getConditionStatement() == null) {
                             final String condition = temp[1];
                             node.setConditionStatement(condition);
                             try {
                                 Pattern.compile(condition);
                             } catch (PatternSyntaxException exception) {
                                 throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_CONDITION_MSG, lineNumber, originalInput));
                             }
                         } else {
                             throw new ParsingException(String.format(ProcessorConstants.ERROR_DUPLICATE_ATTRIBUTE_MSG, lineNumber,
                                     "condition", originalInput));
                         }
                     } else {
                         throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_ATTRIBUTE_MSG, lineNumber, originalInput));
                     }
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineNumber, originalInput));
                 }
             }
             // The variable is a tag with a category specified
             else if (StringUtilities.indexOf(str, ':') != -1) {
                 String temp[] = StringUtilities.split(str, ':', 2);
                 temp = CollectionUtilities.trimStringArray(temp);
                 if (temp.length == 2) {
                     // Check if the category has an array of tags
                     if (StringUtilities.indexOf(temp[1], '(') != -1) {
                         String[] tempTags;
                         final StringBuilder input = new StringBuilder(temp[1]);
                         if (StringUtilities.indexOf(temp[1], ')') == -1) {
                             for (int j = i + 1; j < vars.length; j++) {
                                 i++;
                                 if (StringUtilities.indexOf(vars[j], ')') != -1) {
                                     input.append(", ").append(vars[j]);
                                     break;
                                 } else {
                                     input.append(", ").append(vars[j]);
                                 }
                             }
                         }
 
                         // Get the mapping of variables
                         final HashMap<RelationshipType, String[]> variableMap = getLineVariables(input.toString(), lineNumber, '(', ')',
                                 ',', false);
                         if (variableMap.containsKey(RelationshipType.NONE)) {
                             tempTags = variableMap.get(RelationshipType.NONE);
                         } else {
                             tempTags = null;
                         }
 
                         if (tempTags != null && tempTags.length >= 2) {
                             final String tags[] = new String[tempTags.length];
                             System.arraycopy(tempTags, 0, tags, 0, tempTags.length);
 
                             if (!node.addTags(Arrays.asList(tags))) {
                                 throw new ParsingException(
                                         format(ProcessorConstants.ERROR_MULTI_TAG_DUPLICATED_MSG, lineNumber, originalInput));
                             }
                         } else {
                             throw new ParsingException(
                                     format(ProcessorConstants.ERROR_INVALID_TAG_ATTRIB_FORMAT_MSG, lineNumber, originalInput));
                         }
                     }
                     // Just a single tag so add it straight away
                     else {
                         if (!node.addTag(ProcessorUtilities.replaceEscapeChars(temp[1]))) {
                             throw new ParsingException(format(ProcessorConstants.ERROR_TAG_DUPLICATED_MSG, lineNumber, originalInput));
                         }
                     }
                 } else {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INVALID_TAG_ATTRIB_FORMAT_MSG, lineNumber, originalInput));
                 }
             }
             // Variable is a tag with no category specified
             else {
                 if (str.matches(CSConstants.ALL_TOPIC_ID_REGEX)) {
                     throw new ParsingException(format(ProcessorConstants.ERROR_INCORRECT_TOPIC_ID_LOCATION_MSG, lineNumber, originalInput));
                 }
 
                 if (!node.addTag(str)) {
                     throw new ParsingException(format(ProcessorConstants.ERROR_TAG_DUPLICATED_MSG, lineNumber, originalInput));
                 }
             }
         }
     }
 
     /**
      * Gets the title of a chapter/section/appendix/topic by returning everything before the start delimiter.
      *
      * @param input      The input to be parsed to get the title.
      * @param startDelim The delimiter that specifies that start of options (ie '[')
      * @return The title as a String or null if the title is blank.
      */
     protected String getTitle(final String input, final char startDelim) {
         return input == null || input.equals("") ? null : StringUtilities.split(input, startDelim)[0].trim();
     }
 
     /**
      * Process the relationships without logging any errors.
      */
     protected void processRelationships() {
         for (final Map.Entry<String, List<Relationship>> entry : getRelationships().entrySet()) {
             final String topicId = entry.getKey();
             final SpecTopic specTopic = getSpecTopics().get(topicId);
 
             assert specTopic != null;
 
             for (final Relationship relationship : entry.getValue()) {
                 final String relatedId = relationship.getSecondaryRelationshipTopicId();
                 // The relationship points to a target so it must be a level or topic
                 if (relatedId.toUpperCase(Locale.ENGLISH).matches(ProcessorConstants.TARGET_REGEX)) {
                     if (getTargetTopics().containsKey(relatedId) && !getTargetLevels().containsKey(relatedId)) {
                         specTopic.addRelationshipToTarget(getTargetTopics().get(relatedId), relationship.getType(),
                                 relationship.getRelationshipTitle());
                     } else if (!getTargetTopics().containsKey(relatedId) && getTargetLevels().containsKey(relatedId)) {
                         specTopic.addRelationshipToTarget(getTargetLevels().get(relatedId), relationship.getType(),
                                 relationship.getRelationshipTitle());
                     } else {
                         final SpecTopic dummyTopic = new SpecTopic(-1, "");
                         dummyTopic.setTargetId(relatedId);
                         specTopic.addRelationshipToTarget(dummyTopic, relationship.getType(), relationship.getRelationshipTitle());
                     }
                 }
                 // The relationship isn't a target so it must point to a topic directly
                 else {
                     if (!CSConstants.NEW_TOPIC_ID_PATTERN.matcher(relatedId).matches()) {
                         // The relationship isn't a unique new topic so it will contain the line number in front of
                         // the topic ID
                         if (!relatedId.startsWith("X")) {
                             int count = 0;
                             SpecTopic relatedTopic = null;
 
                             // Get the related topic and count if more then one is found
                             for (final Map.Entry<String, SpecTopic> specTopicEntry : getSpecTopics().entrySet()) {
                                 if (specTopicEntry.getKey().matches("^[\\w\\d]+-" + relatedId + "$")) {
                                     relatedTopic = specTopicEntry.getValue();
                                     count++;
                                 }
                             }
 
                             /*
                              * Add the relationship to the topic even if the relationship isn't duplicated
                              * and the related topic isn't the current topic. This is so it shows up in the
                              * output.
                              */
                             if (count > 0) {
                                 specTopic.addRelationshipToTopic(relatedTopic, relationship.getType(), relationship.getRelationshipTitle());
                             } else {
                                 final SpecTopic dummyTopic = new SpecTopic(-1, "");
                                 dummyTopic.setId(relatedId);
                                 specTopic.addRelationshipToTopic(dummyTopic, relationship.getType(), relationship.getRelationshipTitle());
                             }
                         } else {
                             final SpecTopic dummyTopic = new SpecTopic(-1, "");
                             dummyTopic.setId(relatedId);
                             specTopic.addRelationshipToTopic(dummyTopic, relationship.getType(), relationship.getRelationshipTitle());
                         }
                     } else {
                         if (getSpecTopics().containsKey(relatedId)) {
                             final SpecTopic relatedSpecTopic = getSpecTopics().get(relatedId);
 
                             // Check that a duplicate doesn't exist, because if it does the new topic isn't unique
                             String duplicatedId = "X" + relatedId.substring(1);
                             boolean duplicateExists = false;
                             for (String uniqueTopicId : getSpecTopics().keySet()) {
                                 if (uniqueTopicId.matches("^[\\w\\d]+-" + duplicatedId + "$")) {
                                     duplicateExists = true;
                                     break;
                                 }
                             }
 
                             if (relatedSpecTopic != specTopic) {
                                 if (!duplicateExists) {
                                     specTopic.addRelationshipToTopic(relatedSpecTopic, relationship.getType(),
                                             relationship.getRelationshipTitle());
                                 } else {
                                     // Only create a new target if one doesn't already exist
                                     if (relatedSpecTopic.getTargetId() == null) {
                                         String targetId = ContentSpecUtilities.generateRandomTargetId(relatedSpecTopic.getUniqueId());
                                         while (getTargetTopics().containsKey(targetId) || getTargetLevels().containsKey(targetId)) {
                                             targetId = ContentSpecUtilities.generateRandomTargetId(relatedSpecTopic.getUniqueId());
                                         }
                                         getSpecTopics().get(relatedId).setTargetId(targetId);
                                         getTargetTopics().put(targetId, relatedSpecTopic);
                                     }
                                     specTopic.addRelationshipToTopic(relatedSpecTopic, relationship.getType(),
                                             relationship.getRelationshipTitle());
                                 }
                             }
                         } else {
                             final SpecTopic dummyTopic = new SpecTopic(-1, "");
                             dummyTopic.setId(relatedId);
                             specTopic.addRelationshipToTopic(dummyTopic, relationship.getType(), relationship.getRelationshipTitle());
                         }
                     }
                 }
             }
         }
     }
 
 //    /**
 //     * Process an external level and inject it into the current content specification.
 //     *
 //     * @param lvl                  The level to inject the external levels contents.
 //     * @param externalCSPReference The reference to the external level. The CSP ID and possibly the External Target ID.
 //     * @param title                The title of the external level.
 //     * @param input                The original input used to specify the external level.
 //     */
 //    // TODO Finish External level processing so specs can reference other specs levels
 //    protected void processExternalLevel(final Level lvl, final String externalCSPReference, final String title,
 //            final String input) throws ParsingException {
 //        //TODO Add the level/topic contents to the local parser variables
 //        String[] vars = externalCSPReference.split(":");
 //        vars = CollectionUtilities.trimStringArray(vars);
 //
 //        /* No need to check for an exception as the regex that produces this will take care of it. */
 //        final Integer cspId = Integer.parseInt(vars[0]);
 //        final Integer targetId = vars.length > 1 ? Integer.parseInt(vars[1]) : null;
 //
 //        final TopicWrapper externalContentSpec = topicProvider.getTopic(cspId);
 //
 //        if (externalContentSpec != null) {
 //            /* We are importing part of an external content specification */
 //            if (targetId != null) {
 //                final ContentSpecParser parser = new ContentSpecParser(providerFactory, loggerManager);
 //                boolean foundTargetId = false;
 //                try {
 //                    parser.parse(externalContentSpec.getXml());
 //                    for (final String externalTargetId : parser.getExternalTargetLevels().keySet()) {
 //                        final String id = externalTargetId.replaceAll("ET", "");
 //                        if (id.equals(targetId.toString())) {
 //                            foundTargetId = true;
 //
 //                            final Level externalLvl = parser.getExternalTargetLevels().get(externalTargetId);
 //
 //                            // TODO Deal with processes
 //
 //                            /* Check that the title matches */
 //                            if (externalLvl.getTitle().equals(title)) {
 //                                for (final Node externalChildNode : externalLvl.getChildNodes()) {
 //                                    if (externalChildNode instanceof SpecNode) {
 //                                        lvl.appendChild(externalChildNode);
 //                                    } else if (externalChildNode instanceof Comment) {
 //                                        lvl.appendComment((Comment) externalChildNode);
 //                                    }
 //                                }
 //                            } else {
 //                                // TODO Error Message
 //                                throw new ParsingException("Title doesn't match the referenced target id.");
 //                            }
 //                        }
 //                    }
 //
 //                    if (!foundTargetId) {
 //                        // TODO Error Message
 //                        throw new ParsingException("External target doesn't exist in the content specification");
 //                    }
 //                } catch (Exception e) {
 //                    // TODO Error message
 //                    throw new ParsingException("Failed to pull in external content spec reference");
 //                }
 //            }
 //            /* Import the entire content spec, excluding the metadata */
 //            else if (lvl.getType() == LevelType.BASE) {
 //                // TODO Handle importing the entire content specification
 //            } else {
 //                //TODO Error Message
 //                throw new ParsingException("Invalid place to import external content");
 //            }
 //        } else {
 //            // TODO Error Message
 //            throw new ParsingException("Unable to find the external content specification");
 //        }
 //    }
 
     /**
      * Finds a List of variable sets within a string. If the end of a set
      * can't be determined then it will continue to parse the following
      * lines until the end is found.
      *
      * @param input      The string to find the sets in.
      * @param startDelim The starting character of the set.
      * @param endDelim   The ending character of the set.
      * @return A list of VariableSets that contain the contents of each set
      *         and the start and end position of the set.
      */
     protected List<VariableSet> findVariableSets(final String input, final char startDelim, final char endDelim) {
         final StringBuilder varLine = new StringBuilder(input);
         final List<VariableSet> retValue = new ArrayList<VariableSet>();
         int startPos = 0;
         VariableSet set = ProcessorUtilities.findVariableSet(input, startDelim, endDelim, startPos);
 
         while (set != null && set.getContents() != null) {
             /*
              * Check if we've found the end of a set. If we have add the set to the
              * list and try and see if another set exists. If not then get the next line
              * in the content spec and keep processing the set until the end of the set
              * is found or the end of the content spec.
              */
             if (set.getEndPos() != null) {
                 retValue.add(set);
 
                 final String nextLine = getLines().peek();
                 startPos = set.getEndPos() + 1;
                 set = ProcessorUtilities.findVariableSet(varLine.toString(), startDelim, endDelim, startPos);
 
                 /*
                  * If the next set and/or its contents are empty then it means we found all the sets
                  * for the input line. However the next line in the content spec maybe a continuation
                  * but we couldn't find it originally because of a missing separator. So peek at the next
                  * line and see if it's a continuation (ie another relationship) and if it is then add the
                  * line and continue to find sets.
                  */
                 if ((set == null || set.getContents() == null) && (nextLine != null && nextLine.trim().toUpperCase(Locale.ENGLISH).matches(
                         "^\\" + startDelim + "[ ]*(R|L|P|T|B).*"))) {
                     final String line = getLines().poll();
                     lineCounter++;
 
                     if (line != null) {
                         varLine.append("\n").append(line);
 
                         set = ProcessorUtilities.findVariableSet(varLine.toString(), startDelim, endDelim, startPos);
                     }
                 }
             } else {
                 final String line = getLines().poll();
                 lineCounter++;
 
                 if (line != null) {
                     varLine.append("\n").append(line);
 
                     set = ProcessorUtilities.findVariableSet(varLine.toString(), startDelim, endDelim, startPos);
                 } else {
                     retValue.add(set);
                     break;
                 }
             }
         }
         return retValue;
     }
 }

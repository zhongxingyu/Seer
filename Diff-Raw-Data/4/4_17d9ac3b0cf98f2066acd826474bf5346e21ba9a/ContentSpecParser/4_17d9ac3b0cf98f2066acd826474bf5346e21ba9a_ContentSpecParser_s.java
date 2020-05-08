 package com.redhat.contentspec.processor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import com.google.code.regexp.NamedMatcher;
 import com.google.code.regexp.NamedPattern;
 import com.redhat.contentspec.constants.CSConstants;
 import com.redhat.contentspec.Appendix;
 import com.redhat.contentspec.Chapter;
 import com.redhat.contentspec.Comment;
 import com.redhat.contentspec.ContentSpec;
 import com.redhat.contentspec.Level;
 import com.redhat.contentspec.Node;
 import com.redhat.contentspec.Part;
 import com.redhat.contentspec.Process;
 import com.redhat.contentspec.entities.Relationship;
 import com.redhat.contentspec.Section;
 import com.redhat.contentspec.SpecNode;
 import com.redhat.contentspec.SpecTopic;
 import com.redhat.contentspec.enums.LevelType;
 import com.redhat.contentspec.enums.RelationshipType;
 import com.redhat.contentspec.exception.IndentationException;
 import com.redhat.contentspec.exception.ParsingException;
 import com.redhat.contentspec.processor.constants.ProcessorConstants;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.entities.InjectionOptions;
 import com.redhat.contentspec.utils.ContentSpecUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLogger;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonstructures.Pair;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.StringUtilities;
 import com.redhat.topicindex.rest.entities.interfaces.RESTUserV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTopicV1;
 
 /**
  * A class that parses a Content Specification and stores the parsed data into a ContentSpec Object. The Object then contains all of the
  * Levels, Topics and relationships to be passed for validation or saving. 
  * 
  * @author lnewson
  * @author alabbas
  *
  */
 public class ContentSpecParser
 {
 	/**
 	 * An Enumerator used to specify the parsing mode of the Parser.
 	 */
 	public static enum ParsingMode {NEW, EDITED, EITHER};
 	
 	private final ErrorLogger log;
 	private final ErrorLoggerManager elm;
 	
 	private int spaces = 2;
 	private final ContentSpec spec = new ContentSpec();
 	private int level = 0;
 	private HashMap<String, SpecTopic> specTopics = new HashMap<String, SpecTopic>();
 	private HashMap<String, Level> targetLevels = new HashMap<String, Level>();
 	private HashMap<String, Level> externalTargetLevels = new HashMap<String, Level>();
 	private HashMap<String, SpecTopic> targetTopics = new HashMap<String, SpecTopic>();
 	private HashMap<String, List<Relationship>> relationships = new HashMap<String, List<Relationship>>();
 	private ArrayList<Process> processes = new ArrayList<Process>();
 	private Level lvl = null;
 	private int lineCounter = 0, postProcessedLineCounter = 0;
 	private boolean error = false;
 	private BufferedReader br = null;
 	private final RESTManager restManager;
 	
 	/**
 	 * Constructor
 	 */
 	public ContentSpecParser(String serverUrl) {
 		elm = new ErrorLoggerManager();
 		log = elm.getLogger(ContentSpecParser.class);
 		restManager = new RESTManager(serverUrl);
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param elm An Error Logger Manager that will be used to store all the log messages in case they need to be accessed at a later stage.
 	 */
 	public ContentSpecParser(ErrorLoggerManager elm, RESTManager restManager) {
 		this.elm = elm;
 		log = elm.getLogger(ContentSpecParser.class);
 		this.restManager = restManager;
 	}
 
 	/**
 	 * Parse a Content Specification to put the string into usable objects that can then be validate.
 	 * 
 	 * Note: Relationships in Processes won't be added as they require access to a TopicIndex REST Interface.
 	 * 
 	 * @param contentSpec A string representation of the Content Specification.
 	 * @return True if everything was parsed successfully otherwise false.
 	 * @throws Exception Any unexpected exception that occurred when parsing.
 	 */
 	public boolean parse(final String contentSpec) throws Exception {
 		return parse(contentSpec, null);
 	}
 	
 	/**
 	 * Parse a Content Specification to put the string into usable objects that can then be validate.
 	 * 
 	 * Note: Relationships in Processes won't be added as they require access to a TopicIndex REST Interface.
 	 * 
 	 * @param contentSpec A string representation of the Content Specification.
 	 * @param user The user who requested the parse.
 	 * @return True if everything was parsed successfully otherwise false.
 	 * @throws Exception Any unexpected exception that occurred when parsing.
 	 */
 	public boolean parse(final String contentSpec, final RESTUserV1 user) throws Exception {
 		return parse(contentSpec, user, ParsingMode.EITHER);
 	}
 	
 	/**
 	 * Parse a Content Specification to put the string into usable objects that can then be validate.
 	 * 
 	 * Note: Relationships in Processes won't be added as they require access to a TopicIndex REST Interface.
 	 * 
 	 * @param contentSpec A string representation of the Content Specification.
 	 * @param user The user who requested the parse.
 	 * @param mode The mode in which the Content Specification should be parsed.
 	 * @return True if everything was parsed successfully otherwise false.
 	 * @throws Exception Any unexpected exception that occurred when parsing.
 	 */
 	public boolean parse(final String contentSpec, final RESTUserV1 user, final ParsingMode mode) throws Exception {
 		final BufferedReader br = new BufferedReader(new StringReader(contentSpec));
 		return readFileData(br, user, mode);
 	}
 	
 	/**
 	 * Gets a list of Topic ID's that are used in a Content Specification.
 	 * 
 	 * @return A List of topic ID's.
 	 */
 	public List<Integer> getReferencedTopicIds() {
 		List<Integer> ids = new ArrayList<Integer>();
 		for(String topicId: specTopics.keySet()) {
 			SpecTopic specTopic = specTopics.get(topicId);
 			if (specTopic.getDBId() != 0) ids.add(specTopic.getDBId());
 		}
 		return ids;
 	}
 	
 	/**
 	 * Gets a list of Topic ID's that are used in a Content Specification.
 	 * The list only includes topics that don't reference a revision of a
 	 * topic.
 	 * 
 	 * @return A List of topic ID's.
 	 */
 	public List<Integer> getReferencedLatestTopicIds()
 	{
 		final List<Integer> ids = new ArrayList<Integer>();
 		for(final String topicId: specTopics.keySet())
 		{
 			final SpecTopic specTopic = specTopics.get(topicId);
 			if (specTopic.getDBId() != 0 && specTopic.getRevision() == null) ids.add(specTopic.getDBId());
 		}
 		return ids;
 	}
 
 	/**
 	 * Gets a list of Topic ID's that are used in a Content Specification.
 	 * The list only includes topics that reference a topic revision rather
 	 * then the latest topic revision.
 	 * 
 	 * @return A List of topic ID's.
 	 */
 	public List<Pair<Integer, Integer>> getReferencedRevisionTopicIds()
 	{
 		final List<Pair<Integer, Integer>> ids = new ArrayList<Pair<Integer, Integer>>();
 		for(final String topicId: specTopics.keySet())
 		{
 			final SpecTopic specTopic = specTopics.get(topicId);
 			if (specTopic.getDBId() != 0 && specTopic.getRevision() != null) ids.add(new Pair<Integer, Integer>(specTopic.getDBId(), specTopic.getRevision()));
 		}
 		return ids;
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
 	 * Gets the Content Specification Topics inside of a content specification
 	 * 
 	 * @return The mapping of topics to their unique Content Specification Topic ID's
 	 */
 	public HashMap<String, SpecTopic> getSpecTopics() {
 		return specTopics;
 	}
 	
 	/**
 	 * Gets a list of processes that were parsed in the content specification
 	 * 
 	 * @return A List of Processes
 	 */
 	public List<Process> getProcesses() {
 		return processes;
 	}
 	
 	/**
 	 * Gets a list of Content Specification Topics that were parsed as being targets
 	 * 
 	 * @return A list of Content Specification Topics mapped by their Target ID.
 	 */
 	public HashMap<String, SpecTopic> getTargetTopics() {
 		return targetTopics;
 	}
 	
 	/**
 	 * Gets a list of Levels that were parsed as being targets.
 	 * 
 	 * @return A List of Levels mapped by their Target ID.
 	 */
 	public HashMap<String, Level> getTargetLevels() {
 		return targetLevels;
 	}
 	
 	/**
 	 * Gets the relationships that were created when parsing the Content Specification.
 	 * 
 	 * @return The map of Unique id's to relationships
 	 */
 	public HashMap<String, List<Relationship>> getProcessedRelationships() {
 		return relationships;
 	}
 	
 	/**
 	 * Reads the data from a file that is passed into a BufferedReader and processes it accordingly.
 	 * 
 	 * @param br A BufferedReader object that has been initialised with a file's data.
 	 * @param user The database User entity object for the user who loaded the content specification.
 	 * @param mode The mode to process the Content Spec in (edited, either or new).
 	 * @exception Exception Any uncaught exception that occurs when parsing.
 	 * @return True if the Content Specification was read successfully otherwise false.
 	 */
 	@SuppressWarnings("deprecation")
 	private boolean readFileData(final BufferedReader br, final RESTUserV1 user, final ParsingMode mode) throws Exception {
 		this.br = br;
 		String input = null;
 		boolean editing = false;
 		// Read in the first line of the file
 		while ((input = br.readLine()) != null) {
 			lineCounter++;
 			postProcessedLineCounter++;
 			if (input.trim().startsWith("#") || input.trim().equals("")) {
 				spec.appendPreProcessedLine(input);
 				continue;
 			}
 			String[] temp = StringUtilities.split(input, '=');
 			temp = CollectionUtilities.trimStringArray(temp);
 			if (temp.length >= 2) {
 				// Content Specification
 				if (temp[0].equals("Title")) {
 					if ((mode == ParsingMode.NEW && editing) || (mode == ParsingMode.EDITED && !editing)) {
 						log.error(ProcessorConstants.ERROR_INCORRECT_MODE_MSG);
 						return false;
 					}
 					spec.setTitle(temp[1]);
 					spec.appendPreProcessedLine(input);
 					lvl = spec.getBaseLevel();
 					spec.setCreatedBy(user == null ? null: user.getName());
 					lvl.setAssignedWriter(user == null ? null: user.getName());
 					while ((input = br.readLine()) != null) {
 						lineCounter++;
 						postProcessedLineCounter++;
 						// Process the content specification and print an error message if an error occurs
 						try {
 							if (!processLine(input)) {
 								error = true;
 							}
 						} catch (IndentationException e) {
 							log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
 							return false;
 						}
 					}
 					// Before validating the content specification processes should be loaded first so that the relationships and targets are created
 					for (final Process process: processes) {
 						if (process.processTopics(specTopics, targetTopics, restManager.getReader())) {
 							// Add all of the process topic targets
 							for (String targetId: process.getProcessTargets().keySet()) {
 								targetTopics.put(targetId, process.getProcessTargets().get(targetId));
 							}
 							
 							// Add all of the relationships in the process to the list of content spec relationships
 							for (String uniqueTopicId: process.getProcessRelationships().keySet()) {
 								if (relationships.containsKey(uniqueTopicId)) {
 									relationships.get(uniqueTopicId).addAll(process.getProcessRelationships().get(uniqueTopicId));
 								} else {
 									relationships.put(uniqueTopicId, process.getProcessRelationships().get(uniqueTopicId));
 								}
 							}
 						}
 					}
 					
 					// Setup the relationships
 					processRelationships();
 				}
 				else if (temp[0].equals("ID"))
 				{
 					if (mode == ParsingMode.NEW)
 					{
 						log.error(ProcessorConstants.ERROR_INCORRECT_MODE_MSG);
 						return false;
 					}
 					
 					// ID's aren't stored in the post-processed line data so decrement the post line counter
 					postProcessedLineCounter--;
 					
 					editing = true;
 					int contentSpecId = 0;
 					try {
 						contentSpecId = Integer.parseInt(temp[1].trim());
 					} catch (NumberFormatException e) {
 						log.error(String.format(ProcessorConstants.ERROR_INVALID_CS_ID_FORMAT_MSG, input.trim()));
 						error = true;
 						continue;
 					}
 					spec.setId(contentSpecId);
 					// Read in the revision number
 					input = br.readLine();
 					lineCounter++;
 					if (input == null) {
 						log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG);
 						return false;
 					}
 					temp = StringUtilities.split(input, '=');
 					temp = CollectionUtilities.trimStringArray(temp);
 					if (temp.length >= 2) {
 						if (temp[0].equalsIgnoreCase("SpecRevision")) {
 							// Read in the amount of spaces that were used for the content specification
 							int specRev = 0;
 							try {
 								specRev = Integer.parseInt(temp[1]);
 							} catch (Exception e) {
 								log.error(String.format(ProcessorConstants.ERROR_INVALID_NUMBER_MSG, lineCounter, input));
 								return false;
 							}
 							spec.setRevision(specRev);
 						} else {
 							log.error(ProcessorConstants.ERROR_CS_NO_CHECKSUM_MSG);
 							return false;
 						}
 					} else {
 						log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 						return false;
 					}
 				} else if (temp[0].equals("CHECKSUM")) {
 					if (mode == ParsingMode.NEW) {
 						log.error(ProcessorConstants.ERROR_INCORRECT_MODE_MSG);
 						return false;
 					}
 					
 					// ID's aren't stored in the post-processed line data so decrement the post line counter
 					postProcessedLineCounter--;
 					
 					editing = true;
 					String checksum = temp[1];
 					spec.setChecksum(checksum);
 					// Read in the Content Spec ID
 					input = br.readLine();
 					lineCounter++;
 					if (input == null) {
 						log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG);
 						return false;
 					}
 					temp = StringUtilities.split(input, '=');
 					temp = CollectionUtilities.trimStringArray(temp);
 					if (temp.length >= 2) {
 						if (temp[0].equalsIgnoreCase("ID")) {
 							int contentSpecId = 0;
 							try {
 								contentSpecId = Integer.parseInt(temp[1].trim());
 							} catch (NumberFormatException e) {
 								log.error(String.format(ProcessorConstants.ERROR_INVALID_CS_ID_FORMAT_MSG, input.trim()));
 								error = true;
 								continue;
 							}
 							spec.setId(contentSpecId);
 						} else {
 							log.error(ProcessorConstants.ERROR_CS_NO_CHECKSUM_MSG);
 							return false;
 						}
 					} else {
 						log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 						return false;
 					}
 				} else {
 					log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG);
 					return false;
 				}
 			} else {
 				log.error(ProcessorConstants.ERROR_INCORRECT_FILE_FORMAT_MSG);
 				return false;
 			}
 		}
 		return !error;
 	}
 	
 	/**
 	 * Processes a line of the content specification and stores it in objects
 	 * 
 	 * @param input A line of input from the content specification
 	 * @return True if the line of input was processed successfully otherwise false.
 	 */
 	private boolean processLine(final String line) throws IndentationException
 	{	
 		spec.appendPreProcessedLine(line);
 		char[] tempInputChar= line.toCharArray();
 		int spaceCount = 0;
 		
 		// Trim the whitespace
 		String input = line.trim();
 		if (input.equals("") || input.startsWith("#")) return true;
 		
 		// Count the amount of whitespace characters before any text to determine the level
 		if(Character.isWhitespace(tempInputChar[0]))
 		{
 			for (int count = 0; count< tempInputChar.length; count++)
 			{
 				if (Character.isWhitespace(tempInputChar[count]))
 				{
 					spaceCount++;
 				} else break;
 			}
 			if (spaceCount % spaces != 0) 
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INCORRECT_INDENTATION_MSG, lineCounter, input));
 				throw new IndentationException();
 			}
 		}
 		
 		// Move the level back one if the current level is less than the level last line
 		int curLevel = spaceCount/spaces; 
 		if (curLevel > level)
 		{
 			log.error(String.format(ProcessorConstants.ERROR_INCORRECT_INDENTATION_MSG, lineCounter, input));
 			throw new IndentationException();
 		}
 		if (curLevel < level && !input.startsWith("#"))
 		{
 			for (int i = (level-curLevel); i > 0; i--)
 			{
 				if (lvl.getParent() != null)
 					lvl = lvl.getParent();
 			}
 			level = curLevel;
 		}
 		// Process the input depending on what is parsed
 		if (input.toUpperCase().matches("^SPECREVISION[ ]*((=.*)|$)"))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_CS_INVALID_SPEC_REVISION_MSG, lineCounter));
 			return false;
 		}
 		else if (input.toUpperCase().matches("^CHECKSUM[ ]*((=.*)|$)"))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_CS_INVALID_CHECKSUM_MSG, lineCounter));
 			return false;
 		}
 		else if (input.toUpperCase().matches("^SUBTITLE[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2)
 			{
 				spec.setSubtitle(StringUtilities.replaceEscapeChars(tempInput[1]));
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}			
 		}
 		else if (input.toUpperCase().matches("^EDITION[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2)
 			{
 				String edition = tempInput[1];
 				if (edition.matches(ProcessorConstants.EDITION_VALIDATE_REGEX))
 				{
 					spec.setEdition(edition);
 				}
 				else
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_NUMBER_MSG, lineCounter, input));
 					return false;
 				}
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^PUBSNUMBER[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2)
 			{
 				try
 				{
 					spec.setPubsNumber(Integer.parseInt(tempInput[1]));
 				}
 				catch (Exception e)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_NUMBER_MSG, lineCounter, input));
 					return false;
 				}
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^PRODUCT[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2)
 			{
 				spec.setProduct(StringUtilities.replaceEscapeChars(tempInput[1]));
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^(DESCRIPTION|ABSTRACT)[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2)
 			{
 				spec.setAbstract(StringUtilities.replaceEscapeChars(tempInput[1]));
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^COPYRIGHT HOLDER[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setCopyrightHolder(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^DEBUG[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				if (tempInput[1].equals("1")) {
 					elm.setVerboseDebug(1);
 				} else if (tempInput[1].equals("2")) {
 					elm.setVerboseDebug(2);
 				} else if (!tempInput[1].equals("0")) {
 					log.warn(ProcessorConstants.WARN_DEBUG_IGNORE_MSG);
 				}
 			} 
 		}
 		else if (input.toUpperCase().matches("^VERSION[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setVersion(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^BRAND[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setBrand(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^BUG[ ]*LINKS[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				if (tempInput[1].equalsIgnoreCase("OFF")) {
 					spec.setInjectBugLinks(false);
 				} else if (!tempInput[1].equalsIgnoreCase("ON")) {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_BUG_LINKS_MSG, lineCounter, input));
 					return false;
 				}
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^BZPRODUCT[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setBugzillaProduct(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^BZCOMPONENT[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setBugzillaComponent(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^BZVERSION[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setBugzillaVersion(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^SURVEY[ ]*LINKS[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				if (tempInput[1].equalsIgnoreCase("ON")) {
 					spec.setInjectSurveyLinks(true);
 				} else if (!tempInput[1].equalsIgnoreCase("OFF")) {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_SURVEY_LINKS_MSG, lineCounter, input));
 					return false;
 				}
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^TRANSLATION LOCALE[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setLocale(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^OUTPUT STYLE[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				spec.setOutputStyle(StringUtilities.replaceEscapeChars(tempInput[1]));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		// TODO Fix empty chapter processing
 		/*} else if (input.toUpperCase().matches("^ALLOW EMPTY LEVELS[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				try
 				{
 					spec.setAllowEmptyLevels(Boolean.parseBoolean(StringUtilities.replaceEscapeChars(tempInput[1])));
 				}
 				catch (Exception ex)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 					return false;
 				}
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}*/
 		// TODO Fix the CSP to allow for an spec to not have duplicate topics 
 		/*} else if (input.toUpperCase().matches("^DUPLICATE TOPICS[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				try
 				{
 					spec.setAllowDuplicateTopics(Boolean.parseBoolean(StringUtilities.replaceEscapeChars(tempInput[1])));
 				}
 				catch (Exception ex)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 					return false;
 				}
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}*/
 		} else if (input.toUpperCase().matches("^PUBLICAN\\.CFG[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				int startingPos = StringUtilities.indexOf(tempInput[1], '[');
 				if (startingPos != -1) {
 					String cfg = tempInput[1];
 					int startLineCount = lineCounter;
 					// If the ']' character isn't on this line try the next line
 					if (StringUtilities.indexOf(cfg, ']') == -1) {
 						cfg += "\n";
 						try {
 							// Read the next line and increment counters
 							String newLine = br.readLine();
 							while (newLine != null) {
 								cfg += newLine + "\n";
 								lineCounter++;
 								postProcessedLineCounter++;
 								spec.appendPreProcessedLine(newLine);
 								// If the ']' character still isn't found keep trying
 								if (StringUtilities.lastIndexOf(cfg, ']') == -1) {
 									newLine = br.readLine();
 								} else {
 									break;
 								}
 							}
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 					// Check that the ']' character was found and that it was found before another '[' character
 					if (StringUtilities.lastIndexOf(cfg, ']') == -1 || StringUtilities.lastIndexOf(cfg, '[') != startingPos) {
 						log.error(String.format(ProcessorConstants.ERROR_INVALID_PUBLICAN_CFG_MSG, startLineCount, tempInput[0] + " = " + cfg.replaceAll("\n", "\n          ")));
 						return false;
 					} else {
 						spec.setPublicanCfg(StringUtilities.replaceEscapeChars(cfg).substring(1, cfg.length() - 2));
 					}
 				} else {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_PUBLICAN_CFG_MSG, lineCounter, input));
 					return false;
 				}
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toUpperCase().matches("^INLINE INJECTION[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				InjectionOptions injectionOptions = new InjectionOptions();
 				String[] types = null;
 				if (StringUtilities.indexOf(tempInput[1], '[') != -1) {
 					if (StringUtilities.indexOf(tempInput[1], ']') != -1) {
 						NamedPattern bracketPattern = NamedPattern.compile(String.format(ProcessorConstants.BRACKET_NAMED_PATTERN, '[', ']'));
 						NamedMatcher matcher = bracketPattern.matcher(tempInput[1]);
 						// Find all of the variables inside of the brackets defined by the regex
 						while (matcher.find()) {
 							String topicTypes = matcher.group(ProcessorConstants.BRACKET_CONTENTS);
 							types = StringUtilities.split(topicTypes, ',');
 							for (String type: types) {
 								type = type.trim();
 								injectionOptions.addStrictTopicType(type);
 							}
 						}
 					} else {
 						log.error(String.format(ProcessorConstants.ERROR_NO_ENDING_BRACKET_MSG + ProcessorConstants.CSLINE_MSG, lineCounter, ']', input));
 						return false;
 					}
 				}
 				String injectionSetting = getTitle(tempInput[1], '[');
 				if (injectionSetting.trim().equalsIgnoreCase("on")) {
 					if (types != null) {
 						injectionOptions.setContentSpecType(InjectionOptions.UserType.STRICT);
 					} else {
 						injectionOptions.setContentSpecType(InjectionOptions.UserType.ON);
 					}
 				} else if (injectionSetting.trim().equalsIgnoreCase("off")) {
 					injectionOptions.setContentSpecType(InjectionOptions.UserType.OFF);
 				} else {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_INJECTION_MSG, lineCounter, input));
 					return false;
 				}
 				spec.setInjectionOptions(injectionOptions);
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (input.toLowerCase().matches("^spaces[ ]*((=.*)|$)")) {
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			if (tempInput.length >= 2) {
 				// Read in the amount of spaces that were used for the content specification
 				try {
 					spaces = Integer.parseInt(tempInput[1]);
 					if (spaces <= 0) spaces = 2;
 				} catch (Exception e) {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_NUMBER_MSG, lineCounter, input));
 					return false;
 				}
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^CHAPTER[ ]*((:.*)|$)") || input.toUpperCase().matches("^SECTION[ ]*((:.*)|$)") || input.toUpperCase().matches("^APPENDIX[ ]*((:.*)|$)") 
 				|| input.toUpperCase().matches("^PART[ ]*((:.*)|$)") || input.toUpperCase().matches("^PROCESS[ ]*((:.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, ':', 2);
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			
 			if (tempInput.length >= 1)
 			{
 				// Process the chapter, it's level and title
 				if (tempInput[0].equalsIgnoreCase("Chapter"))
 				{
 					Level newLevel = processLevel(lineCounter, LevelType.CHAPTER, input);
 					if (newLevel == null)
 					{
 						// Create a basic level so the rest of the spec can be processed
 						Chapter chapter = new Chapter(null, lineCounter, input);
 						lvl.appendChild(chapter);
 						lvl = chapter;
 						return false;
 					}
 					else
 					{
 						level = curLevel + 1;
 						lvl.appendChild(newLevel);
 						lvl = newLevel;
 					}
 				// Processes the section, it's level and title
 				}
 				else if (tempInput[0].equalsIgnoreCase("Section"))
 				{
 					Level newLevel = processLevel(lineCounter, LevelType.SECTION, input);
 					if (newLevel == null)
 					{
 						// Create a basic level so the rest of the spec can be processed
 						Section section = new Section(null, lineCounter, input);
 						lvl.appendChild(section);
 						lvl = section;
 						return false;
 					}
 					else 
 					{
 						level = curLevel + 1;
 						lvl.appendChild(newLevel);
 						lvl = newLevel;
 					}
 				// Process an appendix (its done in the same fashion as a chapter
 				}
 				else if (tempInput[0].equalsIgnoreCase("Appendix"))
 				{
 					Level newLevel = processLevel(lineCounter, LevelType.APPENDIX, input);
 					if (newLevel == null)
 					{
 						// Create a basic level so the rest of the spec can be processed
 						Appendix appendix = new Appendix(null, lineCounter, input);
 						lvl.appendChild(appendix);
 						lvl = appendix;
 						return false;
 					}
 					else
 					{
 						level = curLevel + 1;
 						lvl.appendChild(newLevel);
 						lvl = newLevel;
 					}
 				// Process a Process
 				}
 				else if (tempInput[0].equalsIgnoreCase("Process"))
 				{
 					Level newLevel = processLevel(lineCounter, LevelType.PROCESS, input);
 					if (newLevel == null)
 					{
 						// Create a basic level so the rest of the spec can be processed
 						Process process = new Process(null, lineCounter, input);
 						lvl.appendChild(process);
 						lvl = process;
 						return false;
 					}
 					else
 					{
 						level = curLevel + 1;
 						lvl.appendChild(newLevel);
 						lvl = newLevel;
 					}
 					processes.add((Process) lvl);
 				// Process a Part
 				}
 				else if (tempInput[0].equalsIgnoreCase("Part"))
 				{
 					Level newLevel = processLevel(lineCounter, LevelType.PART, input);
 					if (newLevel == null) {
 						// Create a basic level so the rest of the spec can be processed
 						Part part = new Part(null, lineCounter, input);
 						lvl.appendChild(part);
 						lvl = part;
 						return false;
 					}
 					else
 					{
 						level = curLevel + 1;
 						lvl.appendChild(newLevel);
 						lvl = newLevel;
 					}
 				}
 			}
 			else
 			{
 				log.error(String.format(ProcessorConstants.ERROR_LEVEL_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^CS[ ]*:.*"))
 		{
 			String splitVars[] = StringUtilities.split(input, ':', 2);
 			// Remove the whitespace from each value in the split array
 			splitVars = CollectionUtilities.trimStringArray(splitVars);
 			
 			// Get the mapping of variables
 			HashMap<RelationshipType, String[]> variableMap;
 			try {
 				variableMap = getLineVariables(splitVars[1], '[', ']', ',', false);
 				final String title = StringUtilities.replaceEscapeChars(getTitle(splitVars[1], '['));
 				processExternalLevel(lvl, variableMap.get(RelationshipType.EXTERNAL_CONTENT_SPEC)[0],title, input);
 			}
 			catch (Exception e)
 			{
 				log.error(e.getMessage());
 				return false;
 			}
 		}
 		else if (input.toUpperCase().matches("^DTD[ ]*((=.*)|$)"))
 		{
 			String tempInput[] = StringUtilities.split(input, '=');
 			// Remove the whitespace from each value in the split array
 			tempInput = CollectionUtilities.trimStringArray(tempInput);
 			
 			if (tempInput.length >= 2) {
 				if (tempInput[0].equals("DTD")) {
 					spec.setDtd(StringUtilities.replaceEscapeChars(tempInput[1]));
 				}
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return false;
 			}
 		} else if (StringUtilities.indexOf(input, '[') == 0) {
 			String[] variables;
 			try {
 				HashMap<RelationshipType, String[]> variableMap = getLineVariables(input, '[', ']', ',', false);
 				if (!variableMap.containsKey(RelationshipType.NONE)) {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 					return false;
 				} else if (variableMap.size() > 1) {
 					log.error(String.format(ProcessorConstants.ERROR_RELATIONSHIP_BASE_LEVEL_MSG, lineCounter, input));
 					return false;
 				}
 				variables = variableMap.get(RelationshipType.NONE);
 			} catch (Exception e) {
 				log.error(e.getMessage());
 				return false;
 			}
 			if (variables.length > 0) {
 				if (!addOptions(lvl, variables, 0, input)) return false;
 			} else {
 				log.warn(String.format(ProcessorConstants.WARN_EMPTY_BRACKETS_MSG, lineCounter));
 			}
 		} else {
 			// Process a new topic
 			SpecTopic tempTopic = processTopic(input);
 			if (tempTopic == null) {
 				return false;
 			}
 			// Adds the topic to the current level
 			lvl.appendSpecTopic(tempTopic);
 		}
 		return true;
 	}
 	
 	/**
 	 * Processes the input to create a new topic
 	 * 
 	 * @param input The line of input to be processed
 	 * @return A topics object initialised with the data from the input line.
 	 */
 	private SpecTopic processTopic(final String input)
 	{
 		final SpecTopic tempTopic = new SpecTopic(null, postProcessedLineCounter, input, lineCounter, null);
 		
 		// Process a new topic
 		String[] variables;
 		// Read in the variables inside of the brackets
 		HashMap<RelationshipType, String[]> variableMap;
 		try
 		{
 			variableMap = getLineVariables(input, '[', ']', ',', false);
 			if (!variableMap.containsKey(RelationshipType.NONE))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 				return null;
 			}
 			variables = variableMap.get(RelationshipType.NONE);
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage());
 			return null;
 		}
 		int varStartPos = 2;
 		
 		// Process and validate the Types & ID
 		if (variables.length >= 2)
 		{
 			// Check the type and the set it
 			if (variables[0].matches(CSConstants.NEW_TOPIC_ID_REGEX))
 			{
 				if (variables[1].matches("^C:[ ]*[0-9]+$"))
 				{
 					variables[0] = "C" + variables[1].replaceAll("^C:[ ]*", "");
 				}
 				else
 				{
 					tempTopic.setType(StringUtilities.replaceEscapeChars(variables[1]));
 				}
 			}
 			// If we have two variables for a existing topic then check to see if the second variable is the revision
 			else if (variables[0].matches(CSConstants.EXISTING_TOPIC_ID_REGEX))
 			{
 				if (variables[1].toLowerCase().startsWith("rev"))
 				{
 					// Ensure that the attribute syntax is correct
 					if (variables[1].toLowerCase().matches("rev[ ]*:[ ]*\\d+"))
 					{
 						String[] vars = variables[1].split(":");
 						vars = CollectionUtilities.trimStringArray(vars);
 						
 						try
 						{
 							tempTopic.setRevision(Integer.parseInt(vars[1]));
 						}
 						catch (NumberFormatException ex)
 						{
 							log.error(String.format(ProcessorConstants.ERROR_TOPIC_INVALID_REVISION_FORMAT, lineCounter, input));
 							return null;
 						}
 					}
 					else
 					{
 						log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, input));
 						return null;
 					}
 				}
 				else
 				{
 					varStartPos = 1;
 				}
 			}
 			else
 			{
 				varStartPos = 1;
 			}
 		}
 		else if (variables.length == 1) 
 		{
 			if (!variables[0].matches("(" + CSConstants.DUPLICATE_TOPIC_ID_REGEX + ")|(" + CSConstants.CLONED_TOPIC_ID_REGEX + ")|(" + 
 					CSConstants.EXISTING_TOPIC_ID_REGEX + ")|(" + CSConstants.NEW_TOPIC_ID_REGEX +")|(" + CSConstants.CLONED_DUPLICATE_TOPIC_ID_REGEX + ")"))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_TITLE_ID_MSG, lineCounter, input));
 				return null;
 			}
 			else if (variables[0].matches(CSConstants.NEW_TOPIC_ID_REGEX))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_TYPE_TITLE_ID_MSG, lineCounter, input));
 				return null;
 			}
 			varStartPos = 1;
 		}
 		else
 		{
 			log.error(String.format(ProcessorConstants.ERROR_INVALID_TITLE_ID_MSG, lineCounter, input));
 			return null;
 		}
 		
 		// Set the title
 		String title = StringUtilities.replaceEscapeChars(getTitle(input , '['));
 		tempTopic.setTitle(title);
 		
 		// Set the topic ID
 		tempTopic.setId(variables[0]);
 		
 		/*
 		 * Set the Unique ID for the topic. If the ID is already unique and not
 		 * duplicated then just set the id (e.g. N1). Otherwise create the Unique ID
 		 * using the line number and topic ID.
 		 */
 		String uniqueId = variables[0];
 		if (!specTopics.containsKey(variables[0]) && !variables[0].equals("N") && !variables[0].startsWith("X") && !variables[0].startsWith("C") && !variables[0].matches(CSConstants.EXISTING_TOPIC_ID_REGEX))
 		{
 			specTopics.put(uniqueId, tempTopic);
 		}
 		else if (variables[0].equals("N") || variables[0].matches(CSConstants.DUPLICATE_TOPIC_ID_REGEX) || variables[0].matches(CSConstants.CLONED_DUPLICATE_TOPIC_ID_REGEX) 
 				|| variables[0].matches(CSConstants.CLONED_TOPIC_ID_REGEX) || variables[0].matches(CSConstants.EXISTING_TOPIC_ID_REGEX))
 		{
 			uniqueId = Integer.toString(postProcessedLineCounter) + "-" + variables[0];
 			specTopics.put(uniqueId, tempTopic);
 		} else {
 			log.error(String.format(ProcessorConstants.ERROR_DUPLICATE_ID_MSG, lineCounter, variables[0], input));
 			return null;
 		}
 		
 		// Get the options if the topic is a new or cloned topic
 		if (variables[0].matches("(" + CSConstants.NEW_TOPIC_ID_REGEX + ")|(" + CSConstants.CLONED_TOPIC_ID_REGEX + ")|(" + CSConstants.EXISTING_TOPIC_ID_REGEX + ")"))
 		{
 			if (!addOptions(tempTopic, variables, varStartPos, input))
 			{
 				return null;
 			}
 		// Display warnings if options are specified for existing or duplicated topics
 		}
 		else if (variables.length > varStartPos)
 		{
 			if (variables[0].matches(CSConstants.DUPLICATE_TOPIC_ID_REGEX) || variables[0].matches(CSConstants.CLONED_DUPLICATE_TOPIC_ID_REGEX))
 			{
 				log.warn(String.format(ProcessorConstants.WARN_IGNORE_DUP_INFO_MSG, lineCounter, input));
 			}
 		}
 		
 		// Process the relationships
 		final ArrayList<Relationship> topicRelationships = new ArrayList<Relationship>();
 		if (variableMap.containsKey(RelationshipType.RELATED))
 		{
 			String[] related = variableMap.get(RelationshipType.RELATED);
 			for (String relatedId: related)
 			{
 				topicRelationships.add(new Relationship(uniqueId, relatedId, RelationshipType.RELATED));
 			}
 		}
 		if (variableMap.containsKey(RelationshipType.PREREQUISITE))
 		{
 			String[] prerequisites = variableMap.get(RelationshipType.PREREQUISITE);
 			for (String prerequisiteId: prerequisites)
 			{
 				topicRelationships.add(new Relationship(uniqueId, prerequisiteId, RelationshipType.PREREQUISITE));
 			}
 		}
 		
 		// Next and Previous relationships should only be created internally and shouldn't be specified by the user
 		if (variableMap.containsKey(RelationshipType.NEXT) || variableMap.containsKey(RelationshipType.PREVIOUS))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_TOPIC_NEXT_PREV_MSG, lineCounter, input));
 			return null;
 		}
 		
 		/* 
 		 * Branches should only exist within a process. So make sure that
 		 * the current level is a process otherwise throw an error.
 		 */
 		if (variableMap.containsKey(RelationshipType.BRANCH)) {
 			if (lvl instanceof Process) {
 				String[] branches = variableMap.get(RelationshipType.BRANCH);
 				((Process) lvl).addBranches(uniqueId, Arrays.asList(branches));
 			} else {
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_BRANCH_OUTSIDE_PROCESS, lineCounter, input));
 				return null;
 			}
 		}
 		
 		// Add the relationships to the global list if any exist
 		if (!topicRelationships.isEmpty()) {
 			relationships.put(uniqueId, topicRelationships);
 		}
 		
 		// Process targets
 		if (variableMap.containsKey(RelationshipType.TARGET)) {
 			if (targetTopics.containsKey(variableMap.get(RelationshipType.TARGET)[0])) {
 				log.error(String.format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, targetTopics.get(variableMap.get(RelationshipType.TARGET)[0]).getLineNumber(), targetTopics.get(variableMap.get(RelationshipType.TARGET)[0]).getText(), lineCounter, input));
 				return null;
 			} else if (targetLevels.containsKey(variableMap.get(RelationshipType.TARGET)[0])) {
 				log.error(String.format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, targetLevels.get(variableMap.get(RelationshipType.TARGET)[0]).getLineNumber(), targetLevels.get(variableMap.get(RelationshipType.TARGET)[0]).getText(), lineCounter, input));
 				return null;
 			} else {
 				targetTopics.put(variableMap.get(RelationshipType.TARGET)[0], tempTopic);
 				tempTopic.setTargetId(variableMap.get(RelationshipType.TARGET)[0]);
 			}
 		}
 		
 		// Throw an error for external targets
 		if (variableMap.containsKey(RelationshipType.EXTERNAL_TARGET)) 
 		{
 			// TODO Log an error
 			log.error("Unable to use external targets on topics.");
 			return null;
 		}
 		
 		// Throw an error for external content spec injections
 		if (variableMap.containsKey(RelationshipType.EXTERNAL_CONTENT_SPEC)) 
 		{
 			// TODO Log an error
 			log.error("Unable to use external content specs as topics.");
 			return null;
 		}
 		
 		return tempTopic;
 	}
 	
 	/**
 	 * Processes and creates a level based on the level type.
 	 * 
 	 * @param line The line number the level is on.
 	 * @param levelType The type the level will represent. ie. A Chapter or Appendix
 	 * @param input The chapter string in the content specification.
 	 * @return The created level or null if an error occurred.
 	 */
 	private Level processLevel(final int line, final LevelType levelType, final String input) {
 		String splitVars[] = StringUtilities.split(input, ':', 2);
 		// Remove the whitespace from each value in the split array
 		splitVars = CollectionUtilities.trimStringArray(splitVars);
 		
 		// Create the level based on the type
 		Level newLvl;
 		switch (levelType) {
 		case APPENDIX:
 			newLvl = new Appendix(null, line, input);
 			break;
 		case CHAPTER:
 			newLvl = new Chapter(null, line, input);
 			break;
 		case SECTION:
 			newLvl = new Section(null, line, input);
 			break;
 		case PART:
 			newLvl = new Part(null, line, input);
 			break;
 		case PROCESS:
 			newLvl = new Process(null, line, input);
 			break;
 		default:
 			newLvl = new Level(null, line, input, levelType);
 		}
 		
 		// Parse the input
 		if (splitVars.length >= 2)
 		{
 			String[] variables = new String[0];
 			final String title = StringUtilities.replaceEscapeChars(getTitle(splitVars[1], '['));
 			newLvl.setTitle(title);
 			try 
 			{
 				// Get the mapping of variables
 				HashMap<RelationshipType, String[]> variableMap = getLineVariables(splitVars[1], '[', ']', ',', false);
 				if (variableMap.containsKey(RelationshipType.NONE)) 
 				{
 					variables = variableMap.get(RelationshipType.NONE);
 				}
 				
 				// Add targets for the level
 				if (variableMap.containsKey(RelationshipType.TARGET)) 
 				{
 					if (targetTopics.containsKey(variableMap.get(RelationshipType.TARGET)[0])) 
 					{
 						log.error(String.format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, targetTopics.get(variableMap.get(RelationshipType.TARGET)[0]).getLineNumber(), targetTopics.get(variableMap.get(RelationshipType.TARGET)[0]).getText(), lineCounter, input));
 						return null;
 					}
 					else if (targetLevels.containsKey(variableMap.get(RelationshipType.TARGET)[0]))
 					{
 						log.error(String.format(ProcessorConstants.ERROR_DUPLICATE_TARGET_ID_MSG, targetLevels.get(variableMap.get(RelationshipType.TARGET)[0]).getLineNumber(), targetLevels.get(variableMap.get(RelationshipType.TARGET)[0]).getText(), lineCounter, input));
 						return null;
 					}
 					else
 					{
 						targetLevels.put(variableMap.get(RelationshipType.TARGET)[0], newLvl);
 						newLvl.setTargetId(variableMap.get(RelationshipType.TARGET)[0]);
 					}
 				}
 				
 				// Check for external targets
 				if (variableMap.containsKey(RelationshipType.EXTERNAL_TARGET)) 
 				{
 					externalTargetLevels.put(variableMap.get(RelationshipType.EXTERNAL_TARGET)[0], newLvl);
 					newLvl.setExternalTargetId(variableMap.get(RelationshipType.EXTERNAL_TARGET)[0]);
 				}
 				
 				// Check if the level is injecting data from another content spec
 				if (variableMap.containsKey(RelationshipType.EXTERNAL_CONTENT_SPEC)) 
 				{
 					processExternalLevel(newLvl, variableMap.get(RelationshipType.EXTERNAL_CONTENT_SPEC)[0], title, input);
 				}
 				
 				// Check that no relationships were specified for the appendix
 				if (variableMap.containsKey(RelationshipType.RELATED) || variableMap.containsKey(RelationshipType.PREREQUISITE) 
 						|| variableMap.containsKey(RelationshipType.NEXT) || variableMap.containsKey(RelationshipType.PREVIOUS)) 
 				{
 					log.error(String.format(ProcessorConstants.ERROR_LEVEL_RELATIONSHIP_MSG, lineCounter, CSConstants.CHAPTER, CSConstants.CHAPTER, input));
 					return null;
 				}
 			}
 			catch (Exception e)
 			{
 				log.error(e.getMessage());
 				return null;
 			}
 			// Process the options
 			if (variables.length >= 1) {
 				if (!addOptions(newLvl, variables, 0, input)) {
 					return null;
 				}
 			}
 		}
 		return newLvl;
 	}
 	
 	/**
 	 * Gets the variables from a string. The variables are inside of the starting and ending delimiter and are separated by the separator.
 	 * 
 	 * @param input The line of input to get the variables for.
 	 * @param startDelim The starting delimiter of the variables.
 	 * @param endDelim The ending delimiter of the variables.
 	 * @param separator The separator used to separate the variables.
 	 * @param ignoreTypes Used if all variables are to be stored inside of the Relationship NONE type.
 	 * @return A Map of String arrays for different relationship. Inside each string array is the singular variables.
 	 * @throws ParsingException Thrown if the line can't be successfully parsed.
 	 * @throws IOException Thrown if a problem occurs reading a new line.
 	 */
 	public HashMap<RelationshipType, String[]> getLineVariables(String input, char startDelim, char endDelim, char separator, boolean ignoreTypes) throws ParsingException, IOException {
 		return getLineVariables(input, startDelim, endDelim, separator, ignoreTypes, false);
 	}
 	
 	/**
 	 * Gets the variables from a string. The variables are inside of the starting and ending delimiter and are separated by the separator.
 	 * 
 	 * @param input The line of input to get the variables for.
 	 * @param startDelim The starting delimiter of the variables.
 	 * @param endDelim The ending delimiter of the variables.
 	 * @param separator The separator used to separate the variables.
 	 * @param ignoreTypes Used if all variables are to be stored inside of the Relationship NONE type.
 	 * @param groupTypes Used if the relationship types should be group if two or more of the same types are found.
 	 * @return A Map of String arrays for different relationship. Inside each string array is the singular variables.
 	 * @throws ParsingException Thrown if the line can't be successfully parsed.
 	 * @throws IOException Thrown if a problem occurs reading a new line.
 	 */
 	public HashMap<RelationshipType, String[]> getLineVariables(String input, final char startDelim, final char endDelim, final char separator, final boolean ignoreTypes, final boolean groupTypes) throws ParsingException, IOException
 	{
 		HashMap<RelationshipType, String[]> output = new HashMap<RelationshipType, String[]>();
 		
 		final int lastStartDelimPos = StringUtilities.lastIndexOf(input, startDelim);
 		final int lastEndDelimPos = StringUtilities.lastIndexOf(input, endDelim);
 		
 		// Check that we have vairables to process
 		if (lastStartDelimPos == -1) return output;
 		
 		String regex = String.format(ProcessorConstants.BRACKET_PATTERN, startDelim, endDelim);
 		// if the line doesn't match the regex even once then attempt to read the next line
 		String temp = "";
 		int initialCount = lineCounter;
 		while (!input.trim().matches(String.format(ProcessorConstants.BRACKET_VALIDATE_REGEX, regex)) && temp != null && lastEndDelimPos < lastStartDelimPos)
 		{
 			try {
 				// Read in a new line and increment relevant counters
 				temp = br.readLine();
 				lineCounter++;
				postProcessedLineCounter++;
				spec.appendPreProcessedLine(temp);
 				if (temp != null) {
 					input += "\n" + temp;
 				}
 			} catch (IOException e) {
 				log.debug(e.getMessage());
 				e.printStackTrace();
 				throw e;
 			}
 		}
 		regex = String.format(ProcessorConstants.BRACKET_NAMED_PATTERN, startDelim, endDelim);
 		NamedPattern bracketPattern = NamedPattern.compile(regex);
 		NamedMatcher matcher = bracketPattern.matcher(input);
 		// Find all of the variables inside of the brackets defined by the regex
 		while (matcher.find())
 		{
 			ArrayList<String> variables = new ArrayList<String>();
 			String variableSet = matcher.group(ProcessorConstants.BRACKET_CONTENTS).replaceAll("\n", "");
 			// Check that a closing bracket wasn't missed
 			if (StringUtilities.indexOf(variableSet, startDelim) != -1)
 			{
 				throw new ParsingException(String.format(ProcessorConstants.ERROR_NO_ENDING_BRACKET_MSG, initialCount, endDelim));
 			}
 			// Split the variables set into individual variables
 			RelationshipType type = RelationshipType.NONE;
 			if (!ignoreTypes && (variableSet.toUpperCase().matches(ProcessorConstants.RELATED_REGEX) || variableSet.toUpperCase().matches(ProcessorConstants.PREREQUISITE_REGEX)
 					|| variableSet.toUpperCase().matches(ProcessorConstants.NEXT_REGEX) || variableSet.toUpperCase().matches(ProcessorConstants.PREV_REGEX)
 					|| variableSet.toUpperCase().matches(ProcessorConstants.BRANCH_REGEX))) {
 				// Remove the type specifier from the start of the variable set
 				String splitString[] = StringUtilities.split(variableSet.trim(), ':');
 				// Check that there are actually variables set
 				if (splitString.length > 1)
 				{
 					splitString = StringUtilities.split(splitString[1], separator);
 					for (String s: splitString) {
 						variables.add(s.trim());
 					}
 				}
 				else
 				{
 					throw new ParsingException(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, initialCount, input));
 				}
 				// Set the type for this set of variables
 				if (variableSet.toUpperCase().matches(ProcessorConstants.RELATED_REGEX)) type = RelationshipType.RELATED;
 				else if (variableSet.toUpperCase().matches(ProcessorConstants.PREREQUISITE_REGEX)) type = RelationshipType.PREREQUISITE;
 				else if (variableSet.toUpperCase().matches(ProcessorConstants.NEXT_REGEX)) type = RelationshipType.NEXT;
 				else if (variableSet.toUpperCase().matches(ProcessorConstants.PREV_REGEX)) type = RelationshipType.PREVIOUS;
 				else if (variableSet.toUpperCase().matches(ProcessorConstants.BRANCH_REGEX)) type = RelationshipType.BRANCH;
 			}
 			else if (!ignoreTypes && variableSet.toUpperCase().matches(ProcessorConstants.TARGET_REGEX))
 			{
 				type = RelationshipType.TARGET;
 				variables.add(variableSet.trim());
 			}
 			else if (!ignoreTypes && variableSet.toUpperCase().matches(ProcessorConstants.EXTERNAL_TARGET_REGEX))
 			{
 				type = RelationshipType.EXTERNAL_TARGET;
 				variables.add(variableSet.trim());
 			}
 			else
 			{
 				// Normal set of variables that contains the ID and/or tags
 				String splitString[] = StringUtilities.split(variableSet, separator);
 				for (String s: splitString)
 				{
 					variables.add(s.trim());
 				}
 			}
 			// Add the variable set to the mapping
 			if (output.containsKey(type))
 			{
 				if (ignoreTypes || groupTypes)
 				{
 					ArrayList<String> tempVariables = new ArrayList<String>(Arrays.asList(output.get(type)));
 					tempVariables.addAll(variables);
 					output.put(type, tempVariables.toArray(new String[0]));
 				}
 				else
 				{
 					throw new ParsingException(String.format(ProcessorConstants.ERROR_DUPLICATED_RELATIONSHIP_TYPE_MSG, initialCount, input));
 				}
 			}
 			else
 			{
 				output.put(type, variables.toArray(new String[0]));
 			}
 		}
 		return output;
 	}
 	
 	/**
 	 * Adds the options from an array of variables to a node (Level or Topic). It starts checking the variables from the startPos position of the 
 	 * variable array, then check to see if the variable is a tag or attribute and processes it.
 	 * 
 	 * @param node The node to add the options to.
 	 * @param vars An array of variables to get the options for.
 	 * @param startPos The starting position in the variable array to start checking.
 	 * @param originalInput The original string used to create these options.
 	 * @return Returns true if the options were parsed successfully or false if an error occurred.
 	 */
 	private boolean addOptions(final SpecNode node, final String[] vars, final int startPos, final String originalInput)
 	{
 		// Process each variable in vars starting from the start position
 		for (int i = startPos; i < vars.length; i++)
 		{
 			String str = vars[i];
 			// If the variable contains a "=" then it isn't a tag so process it separately
 			if (StringUtilities.indexOf(str, '=') != -1)
 			{
 				String temp[] = StringUtilities.split(str, '=', 2);
 				temp = CollectionUtilities.trimStringArray(temp);
 				if (temp.length == 2)
 				{
 					if (temp[0].equalsIgnoreCase("URL"))
 					{
 						node.addSourceUrl(StringUtilities.replaceEscapeChars(temp[1]));
 					}
 					else if (temp[0].equalsIgnoreCase("description"))
 					{
 						node.setDescription(StringUtilities.replaceEscapeChars(temp[1]));
 					}
 					else if (temp[0].equalsIgnoreCase("Writer"))
 					{
 						node.setAssignedWriter(StringUtilities.replaceEscapeChars(temp[1]));
 					}
 				}
 				else
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_ATTRIB_FORMAT_MSG, lineCounter, originalInput));
 					return false;
 				}
 			}
 			// The variable is a tag with a category specified
 			else if (StringUtilities.indexOf(str, ':') != -1)
 			{
 				String temp[] = StringUtilities.split(str, ':', 2);
 				temp = CollectionUtilities.trimStringArray(temp);
 				if (temp.length == 2)
 				{
 					// Check if the category has an array of tags
 					if (StringUtilities.indexOf(temp[1], '(') != -1)
 					{
 						String[] tempTags = new String[0];
 						String input = temp[1];
 						if (StringUtilities.indexOf(temp[1], ')') == -1)
 						{
 							input = temp[1];
 							for (int j = i + 1; j < vars.length; j++)
 							{
 								i++;
 								if (StringUtilities.indexOf(vars[j], ')') != -1)
 								{
 									input += ", " + vars[j];
 									break;
 								}
 								else
 								{
 									input += ", " + vars[j];
 								}
 							}
 						}
 						
 						try
 						{
 							// Get the mapping of variables
 							HashMap<RelationshipType, String[]> variableMap = getLineVariables(input, '(', ')', ',', false);
 							if (variableMap.containsKey(RelationshipType.NONE))
 							{
 								tempTags = variableMap.get(RelationshipType.NONE);
 							}
 						}
 						catch (Exception e)
 						{
 							log.error(e.getMessage());
 							return false;
 						}
 						
 						if (tempTags.length >= 2)
 						{
 							final String tags[] = new String[tempTags.length - 1];
 							for (int j = 1; j < tempTags.length; j++)
 							{
 								tags[j - 1] = tempTags[j];
 							}
 							
 							if (!node.addTags(Arrays.asList(tags)))
 							{
 								log.error(String.format(ProcessorConstants.ERROR_MULTI_TAG_DUPLICATED_MSG, lineCounter, originalInput));
 								return false;
 							}
 						}
 						else
 						{
 							log.error(String.format(ProcessorConstants.ERROR_INVALID_TAG_ATTRIB_FORMAT_MSG, lineCounter, originalInput));
 							return false;
 						}
 					}
 					// Just a single tag so add it straight away
 					else
 					{
 						if (!node.addTag(StringUtilities.replaceEscapeChars(temp[1])))
 						{
 							log.error(String.format(ProcessorConstants.ERROR_TAG_DUPLICATED_MSG, lineCounter, originalInput));
 							return false;
 						}
 					}
 				}
 				else
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_TAG_ATTRIB_FORMAT_MSG, lineCounter, originalInput));
 					return false;
 				}
 			}
 			// Variable is a tag with no category specified
 			else
 			{
 				if (str.matches(CSConstants.ALL_TOPIC_ID_REGEX))
 				{
 					log.error(String.format(ProcessorConstants.ERROR_INCORRECT_TOPIC_ID_LOCATION_MSG, lineCounter, originalInput));
 					return false;
 				}
 				
 				if (!node.addTag(str))
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TAG_DUPLICATED_MSG, lineCounter, originalInput));
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Gets the title of a chapter/section/appendix/topic by returning everything before the start delimiter.
 	 * 
 	 * @param input The input to be parsed to get the title.
 	 * @param startDelim The delimiter that specifies that start of options (ie '[')
 	 * @return The title as a String or null if the title is blank.
 	 */
 	private String getTitle(final String input, final char startDelim)
 	{
 		return input == null || input.equals("") ? null : StringUtilities.split(input, startDelim)[0].trim();
 	}
 	
 	/**
 	 * Process the relationships without logging any errors.
 	 */
 	private void processRelationships()
 	{
 		for(final String topicId: relationships.keySet())
 		{
 			for (final Relationship relationship: relationships.get(topicId))
 			{
 				final String relatedId = relationship.getSecondaryRelationshipTopicId();
 				// The relationship points to a target so it must be a level or topic
 				if (relatedId.toUpperCase().matches(ProcessorConstants.TARGET_REGEX)) {
 					if (targetTopics.containsKey(relatedId) && !targetLevels.containsKey(relatedId))
 					{
 						final SpecTopic specTopic = specTopics.get(topicId);
 						specTopic.addRelationshipToTarget(targetTopics.get(relatedId), relationship.getType());
 					} else if (!targetTopics.containsKey(relatedId) && targetLevels.containsKey(relatedId))
 					{
 						if (!(relationship.getType() == RelationshipType.NEXT ||relationship.getType() == RelationshipType.PREVIOUS))
 						{
 							final SpecTopic specTopic = specTopics.get(topicId);
 							specTopic.addRelationshipToTarget(targetLevels.get(relatedId), relationship.getType());
 						}
 					}
 				}
 				// The relationship isn't a target so it must point to a topic directly
 				else
 				{
 					if (!relatedId.matches(CSConstants.NEW_TOPIC_ID_REGEX))
 					{
 						// The relationship isn't a unique new topic so it will contain the line number in front of the topic ID
 						if (!relatedId.startsWith("X"))
 						{
 							int count = 0;
 							SpecTopic relatedTopic = null;
 							
 							// Get the related topic and count if more then one is found
 							for (final String specTopicId: specTopics.keySet())
 							{
 								if (specTopicId.matches("^[0-9]+-" + relatedId + "$"))
 								{
 									relatedTopic = specTopics.get(specTopicId);
 									count++;
 								}
 							}
 							
 							/* 
 							 * Add the relationship to the topic if the relationship isn't duplicated
 							 * and the related topic isn't the current topic.
 							 */
 							final SpecTopic specTopic = specTopics.get(topicId);
 							if (count == 1 && relatedTopic != specTopic)
 							{
 								specTopic.addRelationshipToTopic(relatedTopic, relationship.getType());
 							}
 						}
 					}
 					else
 					{
 						if (specTopics.containsKey(relatedId))
 						{
 							// Check that a duplicate doesn't exist, because if it does the new topic isn't unique
 							String duplicatedId = "X" + relatedId.substring(1);
 							boolean duplicateExists = false;
 							for (String specTopicId: specTopics.keySet())
 							{
 								if (specTopicId.matches("^[0-9]+-" + duplicatedId + "$"))
 								{
 									duplicateExists = true;
 									break;
 								}
 							}
 							
 							if (specTopics.get(relatedId) != specTopics.get(topicId))
 							{
 								if (!duplicateExists)
 								{
 									specTopics.get(topicId).addRelationshipToTopic(specTopics.get(relatedId), relationship.getType());
 								}
 								else
 								{
 									// Only create a new target if one doesn't already exist
 									if (specTopics.get(relatedId).getTargetId() == null)
 									{
 										String targetId = ContentSpecUtilities.generateRandomTargetId(specTopics.get(relatedId).getLineNumber());
 										while (targetTopics.containsKey(targetId) || targetLevels.containsKey(targetId))
 										{
 											targetId = ContentSpecUtilities.generateRandomTargetId(specTopics.get(relatedId).getLineNumber());
 										}
 										specTopics.get(relatedId).setTargetId(targetId);
 										targetTopics.put(targetId, specTopics.get(relatedId));
 									}
 									specTopics.get(topicId).addRelationshipToTopic(specTopics.get(relatedId), relationship.getType());
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Process an external level and inject it into the current content specification.
 	 * 
 	 * @param lvl The level to inject the external levels contents.
 	 * @param externalCSPReference The reference to the external level. The CSP ID and possibly the External Target ID.
 	 * @param title The title of the external level.
 	 * @param input The original input used to specify the external level.
 	 */
 	private void processExternalLevel(final Level lvl, final String externalCSPReference, final String title, final String input)
 	{
 		String[] vars = externalCSPReference.split(":");
 		vars = CollectionUtilities.trimStringArray(vars);
 		
 		/* No need to check for an exception as the regex that produces this will take care of it. */
 		final Integer cspId = Integer.parseInt(vars[0]);
 		final Integer targetId = vars.length > 1 ? Integer.parseInt(vars[1]) : null;
 		
 		final RESTTopicV1 externalContentSpec = this.restManager.getReader().getContentSpecById(cspId, null);
 		
 		if (externalContentSpec != null)
 		{
 			/* We are importing part of an external content specification */
 			if (targetId != null)
 			{
 				final ContentSpecParser parser = new ContentSpecParser(new ErrorLoggerManager(), restManager);
 				boolean foundTargetId = false;
 				try {
 					parser.parse(externalContentSpec.getXml());
 					for (final String externalTargetId : parser.externalTargetLevels.keySet())
 					{
 						final String id = externalTargetId.replaceAll("ET", "");
 						if (id.equals(targetId.toString()))
 						{
 							foundTargetId = true;
 							
 							final Level externalLvl = parser.externalTargetLevels.get(externalTargetId);
 							
 							/* Check that the title matches */
 							if (externalLvl.getTitle().equals(title))
 							{
 								for (final Node externalChildNode : externalLvl.getChildNodes())
 								{
 									if (externalChildNode instanceof SpecNode)
 									{
 										lvl.appendChild((SpecNode) externalChildNode);
 									}
 									else if (externalChildNode instanceof Comment)
 									{
 										lvl.appendComment((Comment) externalChildNode);
 									}
 								}
 							}
 							else
 							{
 								// TODO Error Message
 								log.error("Title doesn't match the referenced target id.");
 							}
 						}
 					}
 					
 					if (!foundTargetId)
 					{
 						// TODO Error Message
 						log.error("External target doesn't exist in the content specification");
 					}
 				} catch (Exception e) {
 					// TODO Error message
 					log.error("Failed to pull in external content spec reference");
 				}
 			}
 			/* Import the entire content spec, excluding the metadata */
 			else if (lvl.getType() == LevelType.BASE)
 			{
 				// TODO Handle importing the entire content specification
 			}
 			else
 			{
 				//TODO Error Message
 				log.error("Invalid place to import external content");
 			}
 		}
 		else
 		{
 			// TODO Error Message
 			log.error("Unable to find the external content specification");
 		}
 	}
 }

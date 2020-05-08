 package com.redhat.contentspec.processor.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryTagV1;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.utils.common.StringUtilities;
 
 import com.google.code.regexp.NamedMatcher;
 import com.google.code.regexp.NamedPattern;
 import com.redhat.contentspec.processor.constants.ProcessorConstants;
 import com.redhat.contentspec.processor.structures.VariableSet;
 
 public class ProcessorUtilities
 {
 	private static final Logger log = Logger.getLogger(ProcessorUtilities.class);
 
 	/**
 	 * Converts a list of tags into a mapping of categories to tags. The key is the Category and the value is a List of Tags for that category.
 	 * 
 	 * @param tags The List of tags to be converted.
 	 * @return The mapping of Categories to Tags.
 	 */
 	public static Map<RESTCategoryTagV1, List<RESTTagV1>> getCategoryMappingFromTagList(final List<RESTTagV1> tags)
 	{
 		final HashMap<RESTCategoryTagV1, List<RESTTagV1>> mapping = new HashMap<RESTCategoryTagV1, List<RESTTagV1>>();
 		for (final RESTTagV1 tag: tags)
 		{
 			final List<RESTCategoryTagV1> catList = tag.getCategories().returnItems();
 			if (catList != null)
 			{
 				for (final RESTCategoryTagV1 cat: catList)
 				{
 					if (!mapping.containsKey(cat)) mapping.put(cat, new ArrayList<RESTTagV1>());
 					mapping.get(cat).add(tag);
 				}
 			}
 		}
 		return mapping;
 	}
 
 	/**
 	 * Creates a Post Processed Content Specification from a processed ContentSpec object.
 	 * 
 	 * @param contentSpec The ContenSpec object to create the Post Processed Content Specification for. 
 	 * @param specTopics A HashMap of the all the Content Specification Topics that can exist in the Content Specification. The key is the Topics ID.
 	 * @param editing Whether the content specification is being edited or created.
 	 * @return A string that contains the Post Content Specification or null if an error occurred.
 	 */
 	public static String generatePostContentSpec(final ContentSpec contentSpec, final HashMap<String, SpecTopic> specTopics, final boolean editing)
 	{
 		String output = "ID=" + contentSpec.getId() + "\n";
 		final NamedPattern newTopicPattern = NamedPattern.compile("\\[[ ]*(?<" + ProcessorConstants.TOPIC_ID_CONTENTS + ">[0-9]+)[ ]*(,|\\])");
 		final NamedPattern newTopicPattern2 = NamedPattern.compile("\\[[ ]*(?<" + ProcessorConstants.TOPIC_ID_CONTENTS + ">N[ ]*,.*?)\\]");
 		final NamedPattern newTopicRelationshipPattern = NamedPattern.compile("(B:|P:|PREREQUISITE:|R:|RELATED-TO:|NEXT:|PREV:|,|\\[)[ ]*(?<" + ProcessorConstants.TOPIC_ID_CONTENTS + ">N[0-9]+)[ ]*(?=(,|\\]))");
 		final NamedPattern duplicateTopicPattern = NamedPattern.compile("(B:|P:|PREREQUISITE:|R:|RELATED-TO:|NEXT:|PREV:|,|\\[)[ ]*(?<" + ProcessorConstants.TOPIC_ID_CONTENTS + ">X[0-9]+)[ ]*(?=(,|\\]))");
 		final NamedPattern clonedTopicPattern = NamedPattern.compile("(B:|P:|PREREQUISITE:|R:|RELATED-TO:|NEXT:|PREV:|,|\\[)[ ]*(?<" + ProcessorConstants.TOPIC_ID_CONTENTS + ">C[0-9]+)[ ]*(?=(,|\\]))");
 		final NamedPattern clonedDuplicateTopicPattern = NamedPattern.compile("(B:|P:|PREREQUISITE:|R:|RELATED-TO:|NEXT:|PREV:|,|\\[)[ ]*(?<" + ProcessorConstants.TOPIC_ID_CONTENTS + ">XC[0-9]+)[ ]*(?=(,|\\]))");
 		int count = 1;
 		//if (editing) count += 2;
 		// For each line in the CS check if it matches each pattern and then do an action depending on what pattern is found
 		for (String line: contentSpec.getPreProcessedText())
 		{
 			if (line.trim().matches("^#.*"))
 			{
 				count++;
 				output += line + "\n";
 				continue;
 			}
 			
			if (line.trim().toUpperCase().matches("^((CHECKSUM)|(ID)|(SPECREVISION))[ ]*=.*"))
 			{
 				count++;
 				continue;
 			}
 			
 			log.debug(line);
 			// Existing Topic
 			NamedMatcher m = newTopicPattern.matcher(line.toUpperCase());
 			while (m.find())
 			{
 				log.debug("Existing Topic Match");
 				for (final Entry<String, SpecTopic> entry : specTopics.entrySet())
                 {
                     final SpecTopic specTopic = entry.getValue();
 
 			    	if (specTopic.getLineNumber() == count)
 			    	{
 			    		if (m.group().startsWith("["))
 			    		{
 			    			line = stripVariables(line, specTopic, specTopic.getTitle());
 			    		}
 			    		break;
 			    	}
 			    }
 			}
 
 			// New Topic without an identifying number
 			m = newTopicPattern2.matcher(line.toUpperCase());
 			while (m.find())
 			{
 				log.debug("New Topic without an Identifier Match");
 			    for (final Entry<String, SpecTopic> entry : specTopics.entrySet())
 			    {
 			        final SpecTopic specTopic = entry.getValue();
 
 			    	if (specTopic.getLineNumber() == count)
 			    	{
 			    		line = stripVariables(line, specTopic, null);
 			    		break;
 			    	}
 			    }
 			}
 
 			// New Topic with an identifying number
 			m = newTopicRelationshipPattern.matcher(line.toUpperCase());
 			while (m.find())
 			{
 				log.debug("New Topic with an Identifier Match");
 				final String s = m.group(ProcessorConstants.TOPIC_ID_CONTENTS);
 			    log.debug(s);
 			    final SpecTopic specTopic = specTopics.get(s);
 			    if (m.group().startsWith("["))
 			    {
 			    	line = stripVariables(line, specTopic, null);
 			    	// Add the target id that was created during relationship processing if one exists
 			    	if (specTopic.getTargetId() != null && !line.matches("^.*\\[[ ]*" + specTopic.getTargetId() + "[ ]*\\].*$"))
 			    	{
 			    		line += " [" + specTopic.getTargetId() + "]";
 			    	}
 			    }
 			    line = line.replace(s, specTopic.getTargetId() == null ? Integer.toString(specTopic.getDBId()) : specTopic.getTargetId());
 			}
 
 			// Duplicated Topic
 			m = duplicateTopicPattern.matcher(line.toUpperCase());
 			while (m.find())
 			{
 				log.debug("Duplicated Topic Match");
 				String s = m.group(ProcessorConstants.TOPIC_ID_CONTENTS);
 			    String key = s.replace('X', 'N');
 			    
 			    final SpecTopic specTopic = specTopics.get(key);
 			    if (m.group().startsWith("["))
 			    {
 			    	line = stripVariables(line, specTopic, specTopic.getTitle());
 			    }
 			    line = line.replace(s, Integer.toString(specTopic.getDBId()));
 			}
 
 			// Cloned Topic
 			m = clonedTopicPattern.matcher(line.toUpperCase());
 			while (m.find())
 			{
 				log.debug("Cloned Topic Match");
 				String s = m.group(ProcessorConstants.TOPIC_ID_CONTENTS);
 			    for (final Entry<String, SpecTopic> entry : specTopics.entrySet())
 			    {
 			        final SpecTopic specTopic = entry.getValue();
 			        
 			    	if (specTopic.getLineNumber() == count)
 			    	{
 			    		if (m.group().startsWith("["))
 			    		{
 			    			line = stripVariables(line, specTopic, specTopic.getTitle());
 			    		}
 			    		line = line.replace(s, Integer.toString(specTopic.getDBId()));
 			    		break;
 			    	}
 			    }
 			}
 
 			// Duplicated Cloned Topic
 			m = clonedDuplicateTopicPattern.matcher(line.toUpperCase());
 			while (m.find())
 			{
 				log.debug("Duplicated Cloned Topic Match");
 				String s = m.group(ProcessorConstants.TOPIC_ID_CONTENTS);
 			    // Remove the X
 			    String clonedId = s.substring(1);
 			    for (final Entry<String, SpecTopic> entry : specTopics.entrySet())
 			    {
 			        final String key = entry.getKey();
 			        
 			    	if (key.matches("^[0-9]+-" + clonedId + "$"))
 			    	{
 			    		final SpecTopic specTopic = entry.getValue();
 			    		if (m.group().startsWith("["))
 			    		{
 			    			line = stripVariables(line, specTopic, specTopic.getTitle());
 			    		}
 			    		line = line.replace(s, Integer.toString(specTopic.getDBId()));
 			    		break;
 			    	}
 			    }
 			}
 			count++;
 			
 			output += line + "\n";
 		}
 		return "CHECKSUM=" + HashUtilities.generateMD5(output) + "\n" + output;
 	}
 
     /**
      * Removes all the variables from a topics Content Specification line except the database ID.
      * 
      * @param input The line to remove the variables from.
      * @param DBId The database ID of the topic for the specified line.
      * @param revision The revision of the topic for the specified line.
      * @param topicTitle The title of the topic if it is to be replaced.
      * @return The line with all variables removed.
      */
     private static String stripVariables(final String input, final SpecTopic specTopic, final String topicTitle)
     {
     	VariableSet idSet = findVariableSet(input, '[', ']', 0);
     	String replacementTarget = idSet.getContents();
     	while (replacementTarget.toUpperCase().matches(ProcessorConstants.RELATED_REGEX) || replacementTarget.toUpperCase().matches(ProcessorConstants.PREREQUISITE_REGEX)
 					|| replacementTarget.toUpperCase().matches(ProcessorConstants.NEXT_REGEX) || replacementTarget.toUpperCase().matches(ProcessorConstants.PREV_REGEX)
 					|| replacementTarget.toUpperCase().matches(ProcessorConstants.TARGET_REGEX) || replacementTarget.toUpperCase().matches(ProcessorConstants.BRANCH_REGEX)
 					|| replacementTarget.toUpperCase().matches(ProcessorConstants.LINK_LIST_REGEX) || replacementTarget.toUpperCase().matches(ProcessorConstants.EXTERNAL_TARGET_REGEX)
 					|| replacementTarget.toUpperCase().matches(ProcessorConstants.EXTERNAL_CSP_REGEX) && idSet != null && idSet.getContents() != null && idSet.getEndPos() != null)
     	{
     		idSet = findVariableSet(input, '[', ']', idSet.getEndPos() + 1);
     		if (idSet != null)
     			replacementTarget = idSet.getContents();
     	}
 
 		// Replace the non relationship variable set with the database id.
 		String output = input.replace(replacementTarget, "[" + Integer.toString(specTopic.getDBId()) + (specTopic.getRevision() == null ? "" : (", rev: " + specTopic.getRevision()))
 				+ (specTopic.getConditionStatement() == null ? "" : (", condition=" + specTopic.getConditionStatement())) + "]");
 		// Replace the title
 		if (topicTitle != null && StringUtilities.indexOf(output, '[') != -1)
 		{
 			// Get the original whitespace to add to the line
 			char[] chars = output.toCharArray();
             int i;
             for (i = 0; i < chars.length; i++)
             {
             	char c = chars[i];
             	if (!Character.isWhitespace(c)) break;
             }
 			output = output.substring(0, i) + topicTitle + " " + output.substring(StringUtilities.indexOf(output, '['));
 		}
 		return output;
     }
 
     /**
 	 * Finds a set of variables that are group by delimiters. It also skips nested
 	 * groups and returns them as part of the set so they can be processed separately.
 	 * eg. [var1, var2, [var3, var4], var5]
 	 * 
 	 * @param input The string to find the set for.
 	 * @param startDelim The starting delimiter for the set.
 	 * @param endDelim The ending delimiter for the set.
 	 * @param startPos The position to start searching from in the string.
 	 * @return A VariableSet object that contains the contents of the set, the start position
 	 * in the string and the end position.
 	 */
 	public static VariableSet findVariableSet(final String input, final char startDelim, final char endDelim, final int startPos)
 	{
 		final int startIndex = StringUtilities.indexOf(input, startDelim, startPos);
 		int endIndex = StringUtilities.indexOf(input, endDelim, startPos);
 		int nextStartIndex = StringUtilities.indexOf(input, startDelim, startIndex + 1);
 
 		/* 
 		 * Find the ending delimiter that matches the start delimiter. This is done
 		 * by checking to see if the next start delimiter is before the current end
 		 * delimiter. If that is the case then there is a nested set so look for the
 		 * next end delimiter.
 		 */
 		while (nextStartIndex < endIndex && nextStartIndex != -1 && endIndex != -1)
 		{
 			final int prevEndIndex = endIndex;
 			endIndex = StringUtilities.indexOf(input, endDelim, endIndex + 1);
 			nextStartIndex = StringUtilities.indexOf(input, startDelim, prevEndIndex + 1);
 		}
 
 		// Build the resulting set object
 		final VariableSet set = new VariableSet();
 
 		if (endIndex == -1 && startIndex != -1)
 		{
 			set.setContents(input.substring(startIndex));
 			set.setEndPos(null);
 			set.setStartPos(startIndex);
 		}
 		else if (startIndex != -1)
 		{
 			set.setContents(input.substring(startIndex, endIndex + 1));
 			set.setEndPos(endIndex);
 			set.setStartPos(startIndex);
 		}
 		else
 		{
 			set.setContents(null);
 			set.setEndPos(null);
 			set.setStartPos(null);
 		}
 		return set;
 	}
 }

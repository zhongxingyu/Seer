 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.parser;
 
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.gradle.jarjar.com.google.common.base.Optional;
 import org.jboss.forge.addon.gradle.projects.util.Preconditions;
 
 /**
  * @author Adam Wyłuda
  */
 public class SourceUtil
 {
    public static final String INDENT = "    ";
    public static final Pattern PRECEDING_WHITESPACE_PATTERN = Pattern.compile("\\s*$");
    public static final Pattern SUCCEEDING_WHITESPACE_PATTERN = Pattern.compile("^\\s*\n");
 
    /**
     * Inserts string at specified position in source.
     * 
     * @param lineNumber Position of inserted line, indexed from 1.
     * @param columnNumber Position of inserted line, indexed from 1.
     */
    public static String insertString(String source, String string, int lineNumber, int columnNumber)
    {
       int position = positionInSource(source, lineNumber, columnNumber);
       return source.substring(0, position) + string + source.substring(position);
    }
 
    /**
     * Inserts string at specified position in source.
     */
    public static String insertString(String source, String string, int position)
    {
       return source.substring(0, position) + string + source.substring(position);
    }
 
    /**
     * @param lineNumber Position indexed from 1.
     * @param columnNumber Position indexed from 1.
     * @return Real position of given coordinates in file.
     */
    public static int positionInSource(String source, int lineNumber, int columnNumber)
    {
       Preconditions.checkArgument(lineNumber >= 1, "Line number must be greater than 0");
       Preconditions.checkArgument(columnNumber >= 1, "Column number must be greater than 0");
 
       // Position is indexed from 1, arrays are indexed from 0, so we fix it
       lineNumber--;
       columnNumber--;
 
       String[] sourceLines = source.split("\n");
 
       Preconditions.checkArgument(lineNumber < sourceLines.length, "Given line number exceeds line count");
       Preconditions.checkArgument(columnNumber <= sourceLines[lineNumber].length(),
                "Given column number exceeds column count in line");
 
       int precedingCharactersCount = 0;
       for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++)
       {
          // Add length of the line plus one, which counts for end line character
          precedingCharactersCount += sourceLines[lineIndex].length() + 1;
       }
 
       // Position is sum of character count in preceding lines and given column number
       int position = precedingCharactersCount + columnNumber;
       return position;
    }
 
    public static String removeSourceFragment(String source, SourceCodeElement element)
    {
       return removeSourceFragment(source, element.getLineNumber(), element.getColumnNumber(),
                element.getLastLineNumber(), element.getLastColumnNumber());
    }
 
    public static String removeSourceFragment(String source, int lineNumber, int columnNumber,
             int lastLineNumber, int lastColumnNumber)
    {
       int beginningPosition = positionInSource(source, lineNumber, columnNumber);
       int endingPosition = positionInSource(source, lastLineNumber, lastColumnNumber);
       return removeSourceFragment(source, beginningPosition, endingPosition);
    }
 
    public static String removeSourceFragment(String source, int start, int end)
    {
       return source.substring(0, start) + source.substring(end);
    }
 
    /**
     * {@link #removeSourceFragmentWithLine(String, int, int)}
     */
    public static String removeSourceFragmentWithLine(String source, SourceCodeElement element)
    {
       return removeSourceFragmentWithLine(source, element.getLineNumber(), element.getColumnNumber(),
                element.getLastLineNumber(), element.getLastColumnNumber());
    }
 
    /**
     * {@link #removeSourceFragmentWithLine(String, int, int)}
     */
    public static String removeSourceFragmentWithLine(String source, int lineNumber, int columnNumber,
             int lastLineNumber, int lastColumnNumber)
    {
       int beginningPosition = positionInSource(source, lineNumber, columnNumber);
       int endingPosition = positionInSource(source, lastLineNumber, lastColumnNumber);
       return removeSourceFragmentWithLine(source, beginningPosition, endingPosition);
    }
 
    /**
     * Replaces specified region and surrounding whitespaces with a single new line character.
     */
    public static String removeSourceFragmentWithLine(String source, int start, int end)
    {
       String beforeCode = source.substring(0, start);
       String afterCode = source.substring(end);
 
       Matcher precedingMatcher = PRECEDING_WHITESPACE_PATTERN.matcher(beforeCode);
       precedingMatcher.find();
       String precedingWhitespace = precedingMatcher.group();
 
       Matcher succeedingMatcher = SUCCEEDING_WHITESPACE_PATTERN.matcher(afterCode);
       succeedingMatcher.find();
       String succeedingWhitespace = succeedingMatcher.group();
 
       start -= precedingWhitespace.length();
       end += succeedingWhitespace.length();
 
       return source.substring(0, start) + "\n" + source.substring(end);
    }
 
    /**
     * Appends given code as the last line of the closure.
     */
    public static String appendLineToClosure(String source, InvocationWithClosure invocation, String codeToBeInserted)
    {
       codeToBeInserted = codeToBeInserted.trim();
 
       String sourceToInvocation = source.substring(0,
                positionInSource(source, invocation.getLineNumber(), invocation.getColumnNumber()));
       String invocationIndentation = sourceToInvocation.substring(sourceToInvocation.lastIndexOf("\n") + 1);
       StringBuilder indent = new StringBuilder(invocationIndentation.length());
       for (int i = 0; i < invocationIndentation.length(); i++)
       {
          indent.append(' ');
       }
       invocationIndentation = indent.toString();
 
      /*
       * TODO Fix a bug of not calculating last column properly. For example when there is a space after invocation
       * closure it inserts line after it's closing bracket, not one character before.
       */

       return insertString(source, addNewLineAtEnd(INDENT + codeToBeInserted) + invocationIndentation,
                invocation.getLastLineNumber(),
                invocation.getLastColumnNumber() - 1);
    }
 
    /**
     * Inserts given code into source at path, which means invocation path. For example, path "a", "b", "c" points to:
     * 
     * <pre>
     * a {
     *     b {
     *         c {
     *             // there
     *         }
     *     }
     * }
     * </pre>
     */
    public static String insertIntoInvocationAtPath(String source, String codeToBeInserted, String... path)
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       Optional<InvocationWithClosure> invocationOptional = parser.invocationWithClosureByName(path[0]);
 
       // If the beginning of the path is not present then we append whole path at the end of the source
       if (!invocationOptional.isPresent())
       {
          source = addNewLineAtEnd(source);
          source += createInvocationPath(0, codeToBeInserted, path);
          return source;
       }
 
       for (int level = 1; level < path.length; level++)
       {
          InvocationWithClosure previousInvocation = invocationOptional.get();
          invocationOptional = previousInvocation.invocationWithClosureByName(path[level]);
          if (!invocationOptional.isPresent())
          {
             String invocationPath = createInvocationPath(level, codeToBeInserted,
                      Arrays.copyOfRange(path, level, path.length));
             source = appendLineToClosure(source, previousInvocation, invocationPath);
             return source;
          }
       }
 
       InvocationWithClosure invocation = invocationOptional.get();
       source = appendLineToClosure(source, invocation, codeToBeInserted);
 
       return source;
    }
 
    /**
     * Creates an empty invocation path like described in {@link #insertIntoInvocationAtPath(String, String, String...)}.
     * 
     * @param indentLevel base indent level for each line in generated path
     * @see #indent(int)
     */
    public static String createInvocationPath(int indentLevel, String content, String... path)
    {
       StringBuilder builder = new StringBuilder();
 
       // What goes up...
       for (int level = 0; level < path.length; level++)
       {
          indent(builder, indentLevel + level);
          builder.append(path[level]);
          builder.append(" {\n");
       }
 
       indent(builder, indentLevel + path.length);
       builder.append(addNewLineAtEnd(content));
 
       // ...must come down
       for (int level = path.length - 1; level >= 0; level--)
       {
          indent(builder, indentLevel + level);
          builder.append("}\n");
       }
 
       return builder.toString();
    }
 
    /**
     * Does the same thing as {@link #indent(int)} but appends it directly to string builder.
     */
    public static void indent(StringBuilder builder, int times)
    {
       for (int i = 0; i < times; i++)
       {
          builder.append(INDENT);
       }
    }
 
    /**
     * Returns {@link #INDENT} "times" times.
     */
    public static String indent(int times)
    {
       StringBuilder builder = new StringBuilder(INDENT.length() * times);
       indent(builder, times);
       return builder.toString();
    }
 
    public static String addNewLineAtEnd(String source)
    {
       return source.endsWith("\n") ? source : source + "\n";
    }
 }

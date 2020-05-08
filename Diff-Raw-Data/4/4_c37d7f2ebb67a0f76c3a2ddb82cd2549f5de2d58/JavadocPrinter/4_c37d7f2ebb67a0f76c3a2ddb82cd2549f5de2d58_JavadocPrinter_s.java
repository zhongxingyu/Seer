 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. Neither the name of the Jalopy project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * $Id$
  */
 package de.hunsicker.jalopy.printer;
 
 import de.hunsicker.antlr.CommonAST;
 import de.hunsicker.antlr.collections.AST;
 import de.hunsicker.jalopy.Environment;
 import de.hunsicker.jalopy.parser.JavaNode;
 import de.hunsicker.jalopy.parser.JavaTokenTypes;
 import de.hunsicker.jalopy.parser.JavadocTokenTypes;
 import de.hunsicker.jalopy.parser.Node;
 import de.hunsicker.jalopy.parser.NodeHelper;
 import de.hunsicker.jalopy.parser.TreeWalker;
 import de.hunsicker.jalopy.prefs.Defaults;
 import de.hunsicker.jalopy.prefs.Keys;
 import de.hunsicker.jalopy.prefs.Loggers;
 import de.hunsicker.util.Lcs;
 import de.hunsicker.util.StringHelper;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Level;
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.MatchResult;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.PatternMatcher;
 import org.apache.oro.text.regex.PatternMatcherInput;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 import org.apache.oro.text.regex.StringSubstitution;
 import org.apache.oro.text.regex.Substitution;
 import org.apache.oro.text.regex.Util;
 
 
 /**
  * Printer for Javadoc comments.
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 final class JavadocPrinter
     extends AbstractPrinter
 {
     //~ Static variables/initializers 
 
     /** Singleton. */
     private static final Printer INSTANCE = new JavadocPrinter();
 
     /** The delimeter we use to separate token chunks of strings. */
     private static final String DELIMETER = "|";
 
     /** The empty node. */
     private static final AST EMPTY_NODE = new CommonAST();
 
     /** Indicates that no tag or description was printed yet. */
     private static final int NONE = 0;
 
     /** Indicates that the description section was printed last. */
     private static final int DESCRIPTION = 1;
 
     /** The empty String array. */
     private static final String[] EMPTY_STRING_ARRAY = new String[0];
     private static final String KEY_TAG_REMOVE_OBSOLETE = "TAG_REMOVE_OBSOLETE";
     private static final String KEY_TAG_ADD_MISSING = "TAG_ADD_MISSING";
     private static final String KEY_TAG_MISSPELLED_NAME = "TAG_MISSPELLED_NAME";
     private static final String TAG_OPARA = "<p>";
     private static final String TAG_CPARA = "</p>";
     private static Pattern _pattern;
     private static final PatternMatcher _matcher = new Perl5Matcher();
 
     static
     {
         try
         {
             _pattern = new Perl5Compiler().compile("(?: )*([a-zA-z0-9_.]*)\\s*(.*)",
                                                    Perl5Compiler.READ_ONLY_MASK);
         }
         catch (MalformedPatternException ex)
         {
             ;
         }
     }
 
     //~ Instance variables 
 
     /** The break iterator to use for realigning the comment texts. */
     private ThreadLocal _stringBreaker = new ThreadLocal()
     {
         protected Object initialValue()
         {
             return new BreakIterator();
         }
     };
 
 
     //~ Constructors 
 
     /**
      * Creates a new JavadocPrinter object.
      */
     private JavadocPrinter()
     {
     }
 
     //~ Methods 
 
     /**
      * Returns the sole instance of this class.
      *
      * @return sole instance of this class.
      */
     public static final Printer getInstance()
     {
         return INSTANCE;
     }
 
 
     /**
      * This method is <strong>NOT</strong> implemented. Use {@link
      * #print(AST,AST,NodeWriter)} instead.
      *
      * @param node node to print.
      * @param out stream to print to.
      *
      * @throws UnsupportedOperationException as this method is not supported.
      */
     public void print(AST        node,
                       NodeWriter out)
     {
         throw new UnsupportedOperationException("use print(AST, AST, NodeWriter) instead");
     }
 
 
     /**
      * Prints the given Javadoc comment.
      *
      * @param node node the comment belongs to.
      * @param comment the comment to print.
      * @param out to output to.
      *
      * @throws IOException if an I/O error occured.
      */
     public void print(AST        node,
                       AST        comment,
                       NodeWriter out)
         throws IOException
     {
         // output an auto-generated comment
         if (BasicDeclarationPrinter.GENERATED_COMMENT.equals(comment.getText()))
         {
             String[] lines = split(comment.getFirstChild().getText(), DELIMETER);
 
             if (lines.length > 0)
             {
                 for (int i = 0, size = lines.length - 1; i < size; i++)
                 {
                     out.print(lines[i], JavadocTokenTypes.JAVADOC_COMMENT);
                     out.printNewline();
                 }
 
                 out.print(lines[lines.length - 1],
                           JavadocTokenTypes.JAVADOC_COMMENT);
             }
         }
         else if (!this.prefs.getBoolean(Keys.COMMENT_JAVADOC_PARSE,
                                         Defaults.COMMENT_JAVADOC_PARSE))
         {
             String[] lines = split(comment.getText(), out.originalLineSeparator);
 
             for (int i = 0, size = lines.length - 1; i < size; i++)
             {
                 out.print(lines[i], JavadocTokenTypes.JAVADOC_COMMENT);
                 out.printNewline();
             }
 
             out.print(lines[lines.length - 1],
                       JavadocTokenTypes.JAVADOC_COMMENT);
         }
         else
         {
             out.print(getTopString(node.getType()),
                       JavadocTokenTypes.JAVADOC_COMMENT);
 
             String bottomText = getBottomString(node.getType());
             String asterix = bottomText.substring(0,
                                                   bottomText.indexOf('*') + 1);
             asterix = getAsterix();
 
             AST firstTag = printDescriptionSection(node, comment, asterix, out);
 
             // any tags to print or check needed?
             if ((firstTag != EMPTY_NODE) ||
                 this.prefs.getBoolean(Keys.COMMENT_JAVADOC_CHECK_TAG,
                                       Defaults.COMMENT_JAVADOC_CHECK_TAG))
             {
                 printTagSection(node, comment, firstTag, asterix, out);
             }
 
             out.print(bottomText, JavadocTokenTypes.JAVADOC_COMMENT);
         }
     }
 
 
     /**
      * Returns all valid type names for the given node found as a sibling of
      * the given node (i.e. all exception or parameter types depending on the
      * node).
      *
      * @param node node to search. Either of type METHOD_DEF or CTOR_DEF.
      * @param type type of the node to return the identifiers for. Either
      *        PARAMETERS or LITERAL_throws.
      *
      * @return the valid types names. Returns an empty list if no names were
      *         found for the given type.
      */
     static List getValidTypeNames(AST node,
                                   int type)
     {
         switch (type)
         {
             case JavaTokenTypes.PARAMETERS :
             {
                 switch (node.getType())
                 {
                     case JavaTokenTypes.METHOD_DEF :
                     case JavaTokenTypes.CTOR_DEF :
                         break;
 
                     default :
                         return Collections.EMPTY_LIST;
                 }
 
                 List names = new ArrayList(4);
 
                 for (AST child = NodeHelper.getFirstChild(node, type)
                                            .getFirstChild();
                      child != null;
                      child = child.getNextSibling())
                 {
                     switch (child.getType())
                     {
                         case JavaTokenTypes.PARAMETER_DEF :
                             names.add(NodeHelper.getFirstChild(child,
                                                                JavaTokenTypes.IDENT)
                                                 .getText());
 
                             break;
                     }
                 }
 
                 return names;
             }
 
             case JavaTokenTypes.LITERAL_throws :
             {
                 final List names = new ArrayList(3);
                 AST exceptions = NodeHelper.getFirstChild(node, type);
 
                 if (exceptions != null)
                 {
                     // add all clauses of the exception specification
                     for (AST child = exceptions.getFirstChild();
                          child != null;
                          child = child.getNextSibling())
                     {
                         switch (child.getType())
                         {
                             case JavaTokenTypes.IDENT :
                                 names.add(child.getText());
 
                                 break;
                         }
                     }
                 }
 
                 /**
                  * @todo make this user configurable
                  */
 
                 // add all exceptions actually thrown within the method body
                 TreeWalker walker = new TreeWalker()
                 {
                     public void visit(AST node)
                     {
                         switch (node.getType())
                         {
                             case JavaTokenTypes.LITERAL_throw :
 
                                 switch (node.getFirstChild().getFirstChild()
                                             .getType())
                                 {
                                     case JavaTokenTypes.LITERAL_new :
 
                                         String name = node.getFirstChild()
                                                           .getFirstChild()
                                                           .getFirstChild()
                                                           .getText();
                                         JavaNode slist = ((JavaNode)node).getParent();
 
                                         // only add if the exception is not
                                         // enclosed within a try/catch block
                                         if (isEnclosedWithTry((JavaNode)node))
                                         {
                                             break;
                                         }
 
                                         if (!names.contains(name))
                                         {
                                             names.add(name);
                                         }
 
                                         break;
                                 }
 
                                 break;
                         }
                     }
                 };
 
                 walker.walk(node);
 
                 return names;
             }
         }
 
         return Collections.EMPTY_LIST;
     }
 
 
     /**
      * Determines whether the given node is enclosed with a try/catch block.
      *
      * @param node a LITERAL_throw node.
      *
      * @return <code>true</code> if the node is enclosed with a try/catch
      *         block.
      *
      * @since 1.0b9
      */
     private static boolean isEnclosedWithTry(JavaNode node)
     {
         JavaNode parent = node.getParent();
 
         switch (parent.getType())
         {
             case JavaTokenTypes.METHOD_DEF :
             case JavaTokenTypes.CTOR_DEF :
                 return false;
 
             case JavaTokenTypes.LITERAL_try :
                 return true;
 
             default :
 
                 switch (parent.getType())
                 {
                     case JavaTokenTypes.LITERAL_catch :
                     case JavaTokenTypes.LITERAL_finally :
                         return isEnclosedWithTry(parent.getParent());
 
                     default :
                         return isEnclosedWithTry(parent);
                 }
         }
     }
 
 
     /**
      * Returns the string to start successive Javadoc comment lines with.
      *
      * @return the string to start successive Javadoc comment lines with.
      *
      * @since 1.0b8
      */
     private String getAsterix()
     {
         String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM,
                                      Defaults.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM);
         int asterix = text.indexOf('*');
         int description = StringHelper.indexOfNonWhitespace(text, asterix + 1);
 
         if (description > -1)
         {
             return text.substring(0, description);
         }
         else if (asterix > -1)
         {
             return text.substring(0, asterix + 1);
         }
         else
         {
             return EMPTY_STRING;
         }
     }
 
 
     /**
      * Returns the string to end a Javadoc comment with.
      *
      * @param type type of the node to get the ending comment string for.
      *
      * @return the string to end a Javadoc comment with (usually <code>
      *         &#42;/</code>).
      *
      * @since 1.0b8
      */
     private String getBottomString(int type)
     {
         switch (type)
         {
             case JavaTokenTypes.METHOD_DEF :
                 return this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM,
                                       Defaults.COMMENT_JAVADOC_TEMPLATE_METHOD_BOTTOM);
 
             case JavaTokenTypes.CTOR_DEF :
                 return this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM,
                                       Defaults.COMMENT_JAVADOC_TEMPLATE_CTOR_BOTTOM);
 
             case JavaTokenTypes.VARIABLE_DEF :
 
                 /**
                  * @todo parse user specified string
                  */
                 return " */";
 
             case JavaTokenTypes.CLASS_DEF :
             {
                 String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_CLASS,
                                              Defaults.COMMENT_JAVADOC_TEMPLATE_CLASS)
                                         .trim();
                 int offset = text.lastIndexOf(DELIMETER);
 
                 if (offset > -1)
                 {
                     return text.substring(offset + 1);
                 }
                 else
                 {
                     return " */";
                 }
             }
 
             case JavaTokenTypes.INTERFACE_DEF :
             {
                 String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_INTERFACE,
                                              Defaults.COMMENT_JAVADOC_TEMPLATE_INTERFACE)
                                         .trim();
                 int offset = text.lastIndexOf(DELIMETER);
 
                 if (offset > -1)
                 {
                     return text.substring(offset + 1);
                 }
                 else
                 {
                     return " */";
                 }
             }
 
             default :
                 return " */";
         }
     }
 
 
     /**
      * Returns the number of empty slots in the given list.
      *
      * @param list list to search.
      *
      * @return the number of empty slots. Returns <code>0</code> if no empty
      *         slots were found.
      */
     private int getEmptySlotCount(List list)
     {
         int empty = 0;
 
         for (int i = 0, size = list.size(); i < size; i++)
         {
             if (list.get(i) == null)
             {
                 empty++;
             }
         }
 
         return empty;
     }
 
 
     /**
      * Searchs the given list for a string similiar to the given string. The
      * match is kind of 'fuzzy' as the strings must not be exactly similar.
      *
      * @param string the string to match.
      * @param list list with strings to match against.
      *
      * @return Returns <code>null</code> if no match could be found.
      */
     private String getMatch(String string,
                             List   list)
     {
         if (string == null)
         {
             return null;
         }
 
         if (list.contains(string))
         {
             return string;
         }
 
         Lcs lcs = new Lcs();
 
         for (int i = 0, size = list.size(); i < size; i++)
         {
             String tag = (String)list.get(i);
             lcs.init(string, tag);
 
             double similarity = lcs.getPercentage();
 
             /**
              * @todo evaluate whether this is appropriate
              */
             if (similarity > 75.0)
             {
                 return tag;
             }
         }
 
         return null;
     }
 
 
     /**
      * Returns the index of the next empty slot in the given list. An empty
      * slot has a value of <code>null</code>.
      *
      * @param list list to search.
      *
      * @return index position of the next empty slot. Returns <code>-1</code>
      *         if the list contains no empty slots.
      */
     private int getNextEmptySlot(List list)
     {
         int result = -1;
 
         for (int i = 0, size = list.size(); i < size; i++)
         {
             if (list.get(i) == null)
             {
                 return i;
             }
         }
 
         return result;
     }
 
 
     /**
      * Returns the parameter count of the given METHOD_DEF or CTOR_DEF node.
      *
      * @param node either a METHOD_DEF or CTOR_DEF node.
      *
      * @return the number of parameters in the parameter list of the given
      *         node.
      */
     private int getParamCount(AST node)
     {
         int count = 0;
 
         for (AST param = NodeHelper.getFirstChild(node,
                                                   JavaTokenTypes.PARAMETERS)
                                    .getFirstChild();
              param != null;
              param = param.getNextSibling())
         {
             count++;
         }
 
         return count;
     }
 
 
     /**
      * Returns the template text for the given parameter type.
      *
      * @param node the node to return the template text for.
      * @param typeName the type name of the node. Given the TAG_PARAM type,
      *        this is the type name of the parameter. For
      *        TAG_EXCEPTION/TAG_THROWS, this is the type name of the
      *        exception. May be <code>null</code> for TAG_RETURN.
      * @param type parameter type. Either TAG_PARAM, TAG_RETURN or
      *        TAG_EXCEPTION/TAG_THROWS.
      * @param environment the environment.
      *
      * @return template text for the given param type.
      *
      * @throws IllegalArgumentException if <em>node</em> is no valid node to
      *         add a tag of type <em>type</em> to.
      */
     private String getTagTemplateText(AST         node,
                                       String      typeName,
                                       int         type,
                                       Environment environment)
     {
         switch (type)
         {
             case JavadocTokenTypes.TAG_PARAM :
 
                 switch (node.getType())
                 {
                     case JavaTokenTypes.METHOD_DEF :
 
                         String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM,
                                                      Defaults.COMMENT_JAVADOC_TEMPLATE_METHOD_PARAM);
 
                     // fall through
                     case JavaTokenTypes.CTOR_DEF :
                         text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM,
                                               Defaults.COMMENT_JAVADOC_TEMPLATE_CTOR_PARAM);
 
                         int offset = text.indexOf('*');
                         environment.set(Environment.Variable.TYPE_PARAM.getName(),
                                         typeName);
 
                         if (offset > -1)
                         {
                             text = text.substring(offset + 1).trim();
                         }
 
                         text = environment.interpolate(text);
                         environment.unset(Environment.Variable.TYPE_PARAM.getName());
 
                         return text;
 
                     default :
                         throw new IllegalArgumentException("invalid node type to add @param tag -- " +
                                                            node);
                 }
 
             case JavadocTokenTypes.TAG_THROWS :
             case JavadocTokenTypes.TAG_EXCEPTION :
 
                 switch (node.getType())
                 {
                     case JavaTokenTypes.METHOD_DEF :
 
                         String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION,
                                                      Defaults.COMMENT_JAVADOC_TEMPLATE_METHOD_EXCEPTION);
 
                     // fall through
                     case JavaTokenTypes.CTOR_DEF :
                         text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION,
                                               Defaults.COMMENT_JAVADOC_TEMPLATE_CTOR_EXCEPTION);
 
                         int offset = text.indexOf('*');
                         environment.set(Environment.Variable.TYPE_EXCEPTION.getName(),
                                         typeName);
 
                         if (offset > -1)
                         {
                             text = text.substring(offset + 1).trim();
                         }
 
                         text = environment.interpolate(text);
                         environment.unset(Environment.Variable.TYPE_EXCEPTION.getName());
 
                         return text;
 
                     default :
                         throw new IllegalArgumentException("invalid node type to add @throws tag -- " +
                                                            node);
                 }
 
             case JavadocTokenTypes.TAG_RETURN :
 
                 switch (node.getType())
                 {
                     case JavaTokenTypes.METHOD_DEF :
 
                         String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN,
                                                      Defaults.COMMENT_JAVADOC_TEMPLATE_METHOD_RETURN);
                         int offset = text.indexOf('*');
 
                         if (offset > -1)
                         {
                             text = text.substring(offset + 1).trim();
                         }
 
                         return text;
 
                     default :
                         throw new IllegalArgumentException("invalid node type to add @return tag -- " +
                                                            node);
                 }
 
             default :
                 return EMPTY_STRING;
         }
     }
 
 
     /**
      * Returns the string to start a Javadoc comment with.
      *
      * @param type type of the node to get the starting comment string for.
      *
      * @return the string to start a Javadoc comment with (usually <code>
      *         /&#42;&#42;</code>).
      *
      * @since 1.0b8
      */
     private String getTopString(int type)
     {
         switch (type)
         {
             case JavaTokenTypes.METHOD_DEF :
             {
                 String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP,
                                              Defaults.COMMENT_JAVADOC_TEMPLATE_METHOD_TOP);
                 int offset = text.indexOf(DELIMETER);
 
                 if (offset > -1)
                 {
                     return text.substring(0, offset);
                 }
                 else
                 {
                     return text;
                 }
             }
 
             case JavaTokenTypes.CTOR_DEF :
             {
                 String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP,
                                              Defaults.COMMENT_JAVADOC_TEMPLATE_CTOR_TOP);
                 int offset = text.indexOf(DELIMETER);
 
                 if (offset > -1)
                 {
                     return text.substring(0, offset);
                 }
                 else
                 {
                     return text;
                 }
             }
 
             case JavaTokenTypes.VARIABLE_DEF :
 
                 /**
                  * @todo parse user specified string
                  */
                 return "/**";
 
             case JavaTokenTypes.CLASS_DEF :
             {
                 String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_CLASS,
                                              Defaults.COMMENT_JAVADOC_TEMPLATE_CLASS)
                                         .trim();
                 int offset = text.indexOf(DELIMETER);
 
                 if (offset > -1)
                 {
                     return text.substring(0, offset);
                 }
                 else
                 {
                     return "/**";
                 }
             }
 
             case JavaTokenTypes.INTERFACE_DEF :
             {
                 String text = this.prefs.get(Keys.COMMENT_JAVADOC_TEMPLATE_INTERFACE,
                                              Defaults.COMMENT_JAVADOC_TEMPLATE_INTERFACE)
                                         .trim();
                 int offset = text.indexOf(DELIMETER);
 
                 if (offset > -1)
                 {
                     return text.substring(0, offset);
                 }
                 else
                 {
                     return "/**";
                 }
             }
 
             default :
                 return "/**";
         }
     }
 
 
     /**
      * Determines whether the given node is a valid Javadoc node.
      *
      * @param node a node.
      *
      * @return <code>true</code> if the given node can have a Javadoc comment
      *         as per the Javadoc specification. These are CLASS_DEF,
      *         INTERFACE_DEF, CTOR_DEF, METHOD_DEF and VARIABLE_DEF nodes.
      *
      * @since 1.0b8
      */
     private boolean isValidNode(AST node)
     {
         switch (node.getType())
         {
             case JavaTokenTypes.METHOD_DEF :
             case JavaTokenTypes.CTOR_DEF :
             case JavaTokenTypes.CLASS_DEF :
             case JavaTokenTypes.INTERFACE_DEF :
                 return true;
 
             case JavaTokenTypes.VARIABLE_DEF :
                 return !NodeHelper.isLocalVariable(node);
 
             default :
                 return false;
         }
     }
 
 
     /**
      * Checks whether the given METHOD_DEF node contains a return tag or not
      * and adds or removes one if necessary.
      *
      * @param node a METHOD_DEF node.
      * @param returnNode the found returnNode, may be <code>null</code>.
      * @param out current writer.
      *
      * @return the return tag, returns <code>null</code> if the method does
      *         not need a return tag.
      */
     private AST checkReturnTag(AST        node,
                                AST        returnNode,
                                NodeWriter out)
     {
         boolean needTag = false; // need @return tag?
 LOOP:
         for (AST child = node.getFirstChild();
              child != null;
              child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavaTokenTypes.TYPE :
 
                     if (child.getFirstChild().getType() != JavaTokenTypes.LITERAL_void)
                     {
                         needTag = true;
 
                         break LOOP;
                     }
 
                     break;
             }
         }
 
         if (returnNode != null)
         {
             if (!needTag)
             {
                 out.state.args[0] = out.getFilename();
                 out.state.args[1] = new Integer(out.line);
                 out.state.args[2] = new Integer(out.column);
                 out.state.args[3] = "@return";
                 out.state.args[4] = new Integer(((Node)returnNode).getStartLine());
                 returnNode = null;
                 Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN,
                                                KEY_TAG_REMOVE_OBSOLETE,
                                                out.state.args, null);
             }
         }
         else
         {
             if (needTag)
             {
                 returnNode = createTag(node, JavadocTokenTypes.TAG_RETURN, null,
                                        out.environment);
             }
         }
 
         return returnNode;
     }
 
 
     /**
      * Makes sure that the tag names matches the parameter names of the node.
      * Updates the given list as it adds missing or removes obsolete tags.
      *
      * @param node node the comment belongs to.
      * @param tags tags to print.
      * @param type tag type.
      * @param asterix string to use as leading asterix.
      * @param last type of the tag that was printed last.
      * @param out stream to write to.
      */
     private void checkTags(AST        node,
                            List       tags,
                            int        type,
                            String     asterix,
                            int        last,
                            NodeWriter out)
     {
         // get the actual names of the parameters or exceptions
         List validNames = getValidTypeNames(node, type);
         List validNamesCopy = new ArrayList(validNames);
 
         int capacity = (int)(tags.size() * 1.3);
 
         // will contain the correct tags
         Map correct = new HashMap(capacity);
 
         // will contain misspelled, obsolete or mispositioned tags
         List wrongOrObsolete = new ArrayList(capacity);
 
         // split the tag list in correct tags and wrong/obsolete ones
         for (int i = 0, size = tags.size(); i < size; i++)
         {
             AST tag = (AST)tags.get(i);
 
             if (tag.getFirstChild() != null)
             {
                 String description = tag.getFirstChild().getText().trim();
                 String name = null;
                 int offset = -1;
 
                 // determine the first word of the description: the parameter name
                 if ((offset = description.indexOf(' ')) > -1)
                 {
                     name = description.substring(0, offset);
                 }
                 else
                 {
                     name = description;
                 }
 
                 if (validNamesCopy.contains(name))
                 {
                     correct.put(name, tag);
                     validNamesCopy.remove(name);
                 }
                 else
                 {
                     wrongOrObsolete.add(tag);
                 }
             }
             else
             {
                 switch (tag.getType())
                 {
                     case JavadocTokenTypes.TAG_PARAM :
                     case JavadocTokenTypes.TAG_SEE :
                     case JavadocTokenTypes.TAG_THROWS :
                     case JavadocTokenTypes.TAG_EXCEPTION :
                         wrongOrObsolete.add(tag);
 
                         break;
                 }
             }
         }
 
         // create an empty list with as many empty slots as needed
         List result = new ArrayList(validNames);
         Collections.fill(result, null);
 
         // add all correct tags at the correct position
         for (Iterator i = correct.entrySet().iterator(); i.hasNext();)
         {
             Map.Entry entry = (Map.Entry)i.next();
             result.set(validNames.indexOf(entry.getKey()), entry.getValue());
         }
 
         // either we have too many or too less tags
         if (validNames.size() != tags.size())
         {
             /**
              * @todo the situation here can be ambigious if we have an
              *       obsolete tag AND a misspelled or missing name in which
              *       case we could end up renaming an obsolete tag name but
              *       deleting the misspelled or missing name tag Maybe we
              *       should change this routine to only add missing params
              *       and spit out warnings if we possibly found obsolete tags
              *       or add a switch to disable the removal
              */
 
             // fill gaps with wrong or obsolete tags until no more slots left
             for (int i = 0, size = wrongOrObsolete.size(); i < size; i++)
             {
                 int next = getNextEmptySlot(result);
 
                 // no more empty slots, spit out warnings for all tags which
                 // are skipped, e.g. essentially removed
                 if (next == -1)
                 {
                     for (int j = i, s = wrongOrObsolete.size(); j < s; j++)
                     {
                         AST tag = (AST)wrongOrObsolete.get(j);
                         out.state.args[0] = out.getFilename();
                         out.state.args[1] = new Integer(out.line);
                         out.state.args[2] = new Integer(out.column);
                         out.state.args[3] = tag.getText();
                         out.state.args[4] = new Integer(((Node)tag).getStartLine());
                         out.state.args[5] = tag;
 
                         Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN,
                                                        KEY_TAG_REMOVE_OBSOLETE,
                                                        out.state.args, null);
                     }
 
                     break;
                 }
 
                 AST tag = (AST)wrongOrObsolete.get(i);
 
                 // if the tag name was mispelled, it has been corrected so add
                 // it to the list
                 correctTagName(tag, validNames, next, asterix, last, out);
                 result.set(next, tag);
             }
 
             int emptySlots = validNames.size() - getEmptySlotCount(result);
 
             if (emptySlots < validNames.size())
             {
                 // create missing tags
                 for (int i = emptySlots, size = validNames.size();
                      i < size;
                      i++)
                 {
                     int next = getNextEmptySlot(result);
                     String name = (String)validNames.get(next);
                     AST tag = null;
                     String tagName = null;
 
                     switch (type)
                     {
                         case JavaTokenTypes.PARAMETERS :
                             tag = createTag(node, JavadocTokenTypes.TAG_PARAM,
                                             name, out.environment);
                             result.set(next, tag);
                             tagName = "@param";
 
                             break;
 
                         case JavaTokenTypes.LITERAL_throws :
                             result.set(next,
                                        tag = createTag(node,
                                                        JavadocTokenTypes.TAG_EXCEPTION,
                                                        name, out.environment));
                             tagName = "@throws";
 
                             break;
                     }
 
                     out.state.args[0] = out.getFilename();
                     out.state.args[1] = new Integer(out.line + next +
                                                     (shouldHaveNewlineBefore(
                                                                              tag,
                                                                              last)
                                                          ? 1
                                                          : 0));
                     out.state.args[2] = new Integer(out.getIndentLength() +
                                                     asterix.length() + 1);
                     out.state.args[3] = tagName;
                     out.state.args[4] = name;
                     Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN,
                                                    KEY_TAG_ADD_MISSING,
                                                    out.state.args, null);
                 }
             }
 
             // update the tag list
             tags.clear();
             tags.addAll(result);
         }
 
         // we have the right number of tags, but do they have the correct names
         // and positions?
         else
         {
             List c = new ArrayList(correct.values());
 
             /*for (int i = 0, size = tags.size(); i < size; i++)
 {
     AST tag = (AST)tags.get(i);
     // we're only interested in the missing/wrong tags
     if (c.contains(tag))
     {
         System.err.println("correct " + tag);
         //result.set(i, tag);
         System.err.println(i + " " + c.indexOf(tag));
         continue;
     }
     int next = getNextEmptySlot(result);
     result.set(next, tags.get(i));
 }*/
             for (int i = 0, size = result.size(); i < size; i++)
             {
                 AST tag = (AST)result.get(i);
 
                 // missing or mispelled tag
                 if ((tag == null) || (tag.getFirstChild() == null))
                 {
                     AST wrongTag = (AST)wrongOrObsolete.remove(0);
                     correctTagName(wrongTag, validNames, i, asterix, last, out);
                     tag = wrongTag;
                 }
 
                 // make sure the tag is at the correct position
                 tags.set(i, tag);
 
                 AST child = tag.getFirstChild();
 
                 /*if (child != null)
 {
     String text = child.getText().trim();
     String name = null;
     int offset = -1;
     // determine the first word of the description: the
     // parameter name
     if ((offset = text.indexOf(' ')) > -1)
     {
         name = text.substring(0, offset);
     }
     else
     {
         name = text;
         offset = text.length();
     }
     int pos = validNames.indexOf(name);
     // if we can't find the name in our list or if it does not
     // appear at the correct position, we rename it
     if ((pos == -1) || (pos != i))
     {
         String validName = (String)validNames.get(i);
         out.state.args[0] = out.getFilename();
         out.state.args[1] = new Integer(out.line + i);
         out.state.args[2] = new Integer(out.getIndentLength()
                                + asterix.length() + 1);
         out.state.args[3] = name;
         out.state.args[4] = validName;
         Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN,
                                        KEY_TAG_MISSPELLED_NAME,
                                        out.state.args, null);
         child.setText(SPACE + validName + text.substring(offset));
     }
 }*/
             }
         }
     }
 
 
     /**
      * Corrects the tag name of the given Javadoc standard tag.
      *
      * @param wrongTag the tag node to correct.
      * @param validNames list with all valid tag names for the method/ctor.
      * @param index current index in the list of valid names.
      * @param asterix string to use as leading asterix.
      * @param last type of tag that was printed last.
      * @param out stream to write to.
      *
      * @return index of the corrected tag in the list with valid names.
      */
     private int correctTagName(AST        wrongTag,
                                List       validNames,
                                int        index,
                                String     asterix,
                                int        last,
                                NodeWriter out)
     {
         AST child = wrongTag.getFirstChild();
 
         if (child != null)
         {
             // get the whole description text
             String text = child.getText().trim();
             String oldName = null;
             int offset = -1;
 
             // determine the first word of the text: the parameter name
             if ((offset = text.indexOf(' ')) > -1)
             {
                 oldName = text.substring(0, offset);
             }
             else
             {
                 oldName = text;
                 offset = text.length();
             }
 
             String match = getMatch(oldName, validNames);
             String newName = null;
 
             if (match != null)
             {
                 newName = match;
                 index = validNames.indexOf(match);
             }
             else
             {
                 newName = (String)validNames.get(index);
             }
 
             out.state.args[0] = out.getFilename();
 
             out.state.args[1] = new Integer(out.line + index +
                                             (shouldHaveNewlineBefore(wrongTag,
                                                                      last)
                                                  ? 1
                                                  : 0));
             out.state.args[2] = new Integer(out.getIndentLength() +
                                             asterix.length() + 1);
             out.state.args[3] = oldName;
             out.state.args[4] = newName;
 
             Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN, KEY_TAG_MISSPELLED_NAME,
                                            out.state.args, null);
 
             text = SPACE + newName + text.substring(offset);
             child.setText(text);
         }
         else
         {
             String newName = (String)validNames.get(index);
             String text = SPACE + newName;
             Node c = new Node(JavadocTokenTypes.PCDATA, text);
 
             wrongTag.setFirstChild(c);
         }
 
         return index;
     }
 
 
     /**
      * Creates a standard tag of the given type.
      *
      * @param node node to create the tag for.
      * @param type type of the tag.
      * @param typeName name of the type.
      * @param environment the environment.
      *
      * @return the created standard tag.
      */
     private AST createTag(AST         node,
                           int         type,
                           String      typeName,
                           Environment environment)
     {
         AST tag = new Node(type, EMPTY_STRING);
 
         if (typeName != null)
         {
             AST para = new Node(JavadocTokenTypes.PCDATA,
                                 getTagTemplateText(node, typeName, type,
                                                    environment));
             tag.setFirstChild(para);
         }
         else
         {
             AST description = new Node(JavadocTokenTypes.PCDATA,
                                        getTagTemplateText(node, null, type,
                                                           environment));
             tag.setFirstChild(description);
         }
 
         return tag;
     }
 
 
     /**
      * Determines whether the description for the given Javadoc comment starts
      * with the inheritDoc in-line tag.
      *
      * @param comment a Javadoc comment.
      *
      * @return <code>true</code> if the inheritDoc tag could be found.
      */
     private boolean hasInheritDoc(AST comment)
     {
         for (AST child = comment.getFirstChild();
              child != null;
              child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavadocTokenTypes.TAG_INLINE_INHERITDOC :
                     return true;
 
                 case JavadocTokenTypes.TAG_CUSTOM :
                 case JavadocTokenTypes.TAG_AUTHOR :
                 case JavadocTokenTypes.TAG_DEPRECATED :
                 case JavadocTokenTypes.TAG_EXCEPTION :
                 case JavadocTokenTypes.TAG_THROWS :
                 case JavadocTokenTypes.TAG_PARAM :
                 case JavadocTokenTypes.TAG_RETURN :
                 case JavadocTokenTypes.TAG_SEE :
                 case JavadocTokenTypes.TAG_SINCE :
                 case JavadocTokenTypes.TAG_SERIAL :
                 case JavadocTokenTypes.TAG_SERIAL_DATA :
                 case JavadocTokenTypes.TAG_SERIAL_FIELD :
                 case JavadocTokenTypes.TAG_VERSION :
                     return false;
             }
         }
 
         return false;
     }
 
 
     /**
      * Returns the text of the node and all siblings as one string.
      *
      * @param node node to merge its text for.
      *
      * @return string with the textual content of the node and all siblings.
      */
     private String mergeChildren(AST node)
     {
         StringBuffer buf = new StringBuffer(150);
 
         for (AST child = node; child != null; child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavadocTokenTypes.OCODE :
                 case JavadocTokenTypes.OTTYPE :
                 case JavadocTokenTypes.OANCHOR :
                 case JavadocTokenTypes.OEM :
                 case JavadocTokenTypes.OSTRONG :
                 case JavadocTokenTypes.OITALIC :
                 case JavadocTokenTypes.OBOLD :
                 case JavadocTokenTypes.OUNDER :
                 case JavadocTokenTypes.OSTRIKE :
                 case JavadocTokenTypes.OBIG :
                 case JavadocTokenTypes.OSMALL :
                 case JavadocTokenTypes.OSUB :
                 case JavadocTokenTypes.OSUP :
                 case JavadocTokenTypes.ODFN :
                 case JavadocTokenTypes.OSAMP :
                 case JavadocTokenTypes.OKBD :
                 case JavadocTokenTypes.OVAR :
                 case JavadocTokenTypes.OCITE :
                 case JavadocTokenTypes.OACRO :
                 case JavadocTokenTypes.OFONT :
                 case JavadocTokenTypes.OBQUOTE :
                 case JavadocTokenTypes.OULIST :
                 case JavadocTokenTypes.OOLIST :
                     buf.append(child.getText());
                     buf.append(mergeChildren(child.getFirstChild()));
 
                     break;
 
                 case JavadocTokenTypes.OLITEM :
                     buf.append(child.getText());
                     buf.append(mergeChildren(child.getFirstChild()));
                     buf.append(SPACE);
 
                     break;
 
                 case JavadocTokenTypes.TAG_INLINE_LINK :
                 case JavadocTokenTypes.TAG_INLINE_LINKPLAIN :
                 case JavadocTokenTypes.TAG_INLINE_INHERITDOC :
                 case JavadocTokenTypes.TAG_INLINE_DOCROOT :
                 case JavadocTokenTypes.TAG_INLINE_VALUE :
                 case JavadocTokenTypes.TAG_INLINE_CUSTOM :
                     buf.append(LCURLY);
                     buf.append(child.getText());
                     buf.append(mergeChildren(child.getFirstChild()));
                     buf.append(RCURLY);
 
                     break;
 
                 default :
                     buf.append(child.getText());
             }
         }
 
         return buf.toString();
     }
 
 
     /**
      * Prints the given blockquote.
      *
      * @param node root node of the blockquote.
      * @param asterix leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printBlockquote(AST        node,
                                  String     asterix,
                                  NodeWriter out)
         throws IOException
     {
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.print(node.getText(), JavadocTokenTypes.OBQUOTE);
         out.printNewline();
         printContent(node.getFirstChild(), asterix, out);
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.print("</blockquote>", JavadocTokenTypes.CBQUOTE);
         out.printNewline();
     }
 
 
     /**
      * Prints the given comment.
      *
      * @param node root node of the comment.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printComment(AST        node,
                               String     asterix,
                               NodeWriter out)
         throws IOException
     {
         String[] lines = split(node.getText(), Integer.MAX_VALUE, true);
         printCommentLines(lines, asterix, out, true);
     }
 
 
     /**
      * Prints the given lines. Prepends a leading asterix in front of each
      * line.
      *
      * @param lines lines to print.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printCommentLines(String[]   lines,
                                    String     asterix,
                                    NodeWriter out)
         throws IOException
     {
         printCommentLines(lines, asterix, out, false);
     }
 
 
     /**
      * Prints the individual comment lines.
      *
      * @param lines the comment lines.
      * @param asterix asterix to prepend before each line.
      * @param out stream to write to.
      * @param trim if <code>true</code> each line will be trimmed.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printCommentLines(String[]   lines,
                                    String     asterix,
                                    NodeWriter out,
                                    boolean    trim)
         throws IOException
     {
         if (trim)
         {
             for (int i = 0; i < lines.length; i++)
             {
                 if (asterix != null)
                 {
                     out.print(asterix, JavadocTokenTypes.PCDATA);
                 }
 
                 out.print(lines[i].trim(), JavadocTokenTypes.PCDATA);
                 out.printNewline();
             }
         }
         else
         {
             for (int i = 0; i < lines.length; i++)
             {
                 if (asterix != null)
                 {
                     out.print(asterix, JavadocTokenTypes.PCDATA);
                 }
 
                 out.print(lines[i], JavadocTokenTypes.PCDATA);
                 out.printNewline();
             }
         }
     }
 
 
     /**
      * Prints the content of the description section.
      *
      * @param node the first node of the description section.
      * @param asterix leading asterix.
      * @param out stream to write to.
      *
      * @return the next node to print. Either a node representing a standard
      *         Javadoc tag or the {@link #EMPTY_NODE} to indicate that no
      *         standard tags are available.
      *
      * @throws IOException if an I/O error occured.
      */
     private AST printContent(AST        node,
                              String     asterix,
                              NodeWriter out)
         throws IOException
     {
         AST next = EMPTY_NODE;
 ITERATION:
         for (AST child = node; child != null; child = child.getNextSibling())
         {
 SELECTION:
             for (;;)
             {
                 switch (child.getType())
                 {
                     case JavadocTokenTypes.COMMENT :
                         printComment(child, asterix, out);
 
                         break SELECTION;
 
                     case JavadocTokenTypes.PCDATA :
                     case JavadocTokenTypes.RCURLY :
                     case JavadocTokenTypes.LCURLY :
                     case JavadocTokenTypes.AT :
                     case JavadocTokenTypes.OTTYPE :
                     case JavadocTokenTypes.OITALIC :
                     case JavadocTokenTypes.OBOLD :
                     case JavadocTokenTypes.OUNDER :
                     case JavadocTokenTypes.OSTRIKE :
                     case JavadocTokenTypes.OBIG :
                     case JavadocTokenTypes.OSMALL :
                     case JavadocTokenTypes.OSUB :
                     case JavadocTokenTypes.OSUP :
                     case JavadocTokenTypes.OEM :
                     case JavadocTokenTypes.OSTRONG :
                     case JavadocTokenTypes.ODFN :
                     case JavadocTokenTypes.OCODE :
                     case JavadocTokenTypes.OSAMP :
                     case JavadocTokenTypes.OKBD :
                     case JavadocTokenTypes.OVAR :
                     case JavadocTokenTypes.OCITE :
                     case JavadocTokenTypes.OACRO :
                     case JavadocTokenTypes.OANCHOR :
                     case JavadocTokenTypes.IMG :
                     case JavadocTokenTypes.OFONT :
                     case JavadocTokenTypes.BR :
                         child = printText(child, asterix, out);
 
                         continue SELECTION;
 
                     case JavadocTokenTypes.TAG_INLINE_DOCROOT :
                     case JavadocTokenTypes.TAG_INLINE_LINK :
                     case JavadocTokenTypes.TAG_INLINE_LINKPLAIN :
                     case JavadocTokenTypes.TAG_INLINE_INHERITDOC :
                     case JavadocTokenTypes.TAG_INLINE_VALUE :
                     case JavadocTokenTypes.TAG_INLINE_CUSTOM :
                         child = printText(child, asterix, out);
 
                         continue SELECTION;
 
                     case JavadocTokenTypes.OPARA :
                         printParagraph(child, asterix, out);
 
                         break SELECTION;
 
                     case JavadocTokenTypes.OBQUOTE :
                         printBlockquote(child, asterix, out);
 
                         break SELECTION;
 
                     case JavadocTokenTypes.HR :
                         out.print(asterix, JavadocTokenTypes.PCDATA);
                         out.print(child.getText(), JavadocTokenTypes.HR);
                         out.printNewline();
 
                         break SELECTION;
 
                     case JavadocTokenTypes.OH1 :
                     case JavadocTokenTypes.OH2 :
                     case JavadocTokenTypes.OH3 :
                     case JavadocTokenTypes.OH4 :
                     case JavadocTokenTypes.OH5 :
                     case JavadocTokenTypes.OH6 :
                         printHeading(child, asterix, out);
 
                         break SELECTION;
 
                     case JavadocTokenTypes.OTABLE :
                         printTable(child, asterix, out);
 
                         break SELECTION;
 
                     case JavadocTokenTypes.PRE :
                         printPreformatted(child, asterix, out);
 
                         break SELECTION;
 
                     case JavadocTokenTypes.OULIST :
                     case JavadocTokenTypes.OOLIST :
                     case JavadocTokenTypes.ODLIST :
                         printList(child, asterix, out);
 
                         break SELECTION;
 
                     /**
                      * @todo center and div
                      */
                     default :
                         next = child;
 
                         break ITERATION;
                 }
             }
         }
 
         return next;
     }
 
 
     /**
      * Print the comment's leading description.
      *
      * @param node owner of the comment.
      * @param comment the first node of the description section.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @return the first node of the tag section or the {@link #EMPTY_NODE} if
      *         no tag nodes exist.
      *
      * @throws IOException if an I/O error occured.
      */
     private AST printDescriptionSection(AST        node,
                                         AST        comment,
                                         String     asterix,
                                         NodeWriter out)
         throws IOException
     {
         // check if we only have a description that fits in one line
         switch (node.getType())
         {
             case JavaTokenTypes.VARIABLE_DEF :
 
                 if (this.prefs.getBoolean(Keys.COMMENT_JAVADOC_FIELDS_SHORT,
                                           Defaults.COMMENT_JAVADOC_FIELDS_SHORT))
                 {
                     if (printSingleLineDescription(node, comment, out))
                     {
                         // no standard tags found if we print in one line,
                         // return the EMPTY_NODE to indicate this
                         return EMPTY_NODE;
                     }
                 }
 
                 break;
         }
 
         out.printNewline();
 
         return printContent(comment.getFirstChild(), asterix, out);
     }
 
 
     /**
      * Prints the given heading.
      *
      * @param node root node of the heading.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printHeading(AST        node,
                               String     asterix,
                               NodeWriter out)
         throws IOException
     {
         String[] lines = split(node.getText(), Integer.MAX_VALUE, false);
         printCommentLines(lines, asterix, out);
         out.last = JavadocTokenTypes.CH1;
     }
 
 
     /**
      * Prints out the given list (either a definition, ordered or unordered
      * list).
      *
      * @param node node to print
      * @param asterix leading asterix.
      * @param out stream to print to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printList(AST        node,
                            String     asterix,
                            NodeWriter out)
         throws IOException
     {
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.printNewline();
         out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
         out.print(node.getText(), node.getType());
         out.printNewline();
 
         for (AST child = node.getFirstChild();
              child != null;
              child = child.getNextSibling())
         {
             printListItem(child, asterix, out);
 
             if (child.getNextSibling() != null)
             {
                 out.printNewline();
             }
         }
 
         out.print(asterix, JavadocTokenTypes.PCDATA);
 
         switch (node.getType())
         {
             case JavadocTokenTypes.OULIST :
                 out.print("</ul>", JavadocTokenTypes.CULIST);
 
                 break;
 
             case JavadocTokenTypes.OOLIST :
                 out.print("</ol>", JavadocTokenTypes.COLIST);
 
                 break;
 
             case JavadocTokenTypes.ODLIST :
                 out.print("</dl>", JavadocTokenTypes.CDLIST);
 
                 break;
         }
 
         out.printNewline();
 
         if (node.getNextSibling() != null)
         {
             out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
             out.printNewline();
         }
     }
 
 
     /**
      * Prints out the given list item (either a list item, definition term or
      * definition).
      *
      * @param node node to print
      * @param asterix leading asterix.
      * @param out stream to print to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printListItem(AST        node,
                                String     asterix,
                                NodeWriter out)
         throws IOException
     {
         switch (node.getType())
         {
             case JavadocTokenTypes.OLITEM :
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("<li>", JavadocTokenTypes.OLITEM);
                 out.printNewline();
                 printContent(node.getFirstChild(), asterix, out);
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("</li>", JavadocTokenTypes.CLITEM);
 
                 break;
 
             case JavadocTokenTypes.ODTERM :
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("<dt>", JavadocTokenTypes.ODTERM);
                 out.printNewline();
                 printContent(node.getFirstChild(), asterix, out);
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("</dt>", JavadocTokenTypes.CDTERM);
 
                 break;
 
             case JavadocTokenTypes.ODDEF :
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("<dd>", JavadocTokenTypes.ODDEF);
                 out.printNewline();
                 printContent(node.getFirstChild(), asterix, out);
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("</dd>", JavadocTokenTypes.CDDEF);
 
                 break;
         }
     }
 
 
     /**
      * Prints an empty line before the given tag, if necessary.
      *
      * @param tag a Javadoc Standard tag.
      * @param last the type of the last tag printed.
      * @param asterix string to print as leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      *
      * @since 1.0b9
      */
     private void printNewlineBefore(AST        tag,
                                     int        last,
                                     String     asterix,
                                     NodeWriter out)
         throws IOException
     {
         if (shouldHaveNewlineBefore(tag, last))
         {
             out.print(trimTrailing(asterix), JavadocTokenTypes.PCDATA);
             out.printNewline();
         }
 
         /*switch (last)
 {
     case DESCRIPTION:
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.printNewline();
         break;
     case NONE:
         break;
     default:
         switch (tag.getType())
         {
             case JavadocTokenTypes.TAG_EXCEPTION:
             case JavadocTokenTypes.TAG_THROWS:
                 switch (last)
                 {
                     case JavadocTokenTypes.TAG_EXCEPTION:
                     case JavadocTokenTypes.TAG_THROWS:
                         break;
                     default:
                         out.print(asterix, JavadocTokenTypes.PCDATA);
                         out.printNewline();
                         break;
                 }
                 break;
             case JavadocTokenTypes.TAG_VERSION:
             break;
             case JavadocTokenTypes.TAG_PARAM:
             case JavadocTokenTypes.TAG_CUSTOM:
             case JavadocTokenTypes.TAG_AUTHOR:
                 if (last != tag.getType())
                 {
                     out.print(asterix, JavadocTokenTypes.PCDATA);
                     out.printNewline();
                 }
                 break;
             case JavadocTokenTypes.TAG_RETURN:
                     out.print(asterix, JavadocTokenTypes.PCDATA);
                     out.printNewline();
                 break;
             case JavadocTokenTypes.TAG_SEE:
                 if (last != tag.getType())
                 {
                     out.print(asterix, JavadocTokenTypes.PCDATA);
                     out.printNewline();
                 }
                 break;
             default:
             switch (last)
             {
                 case JavadocTokenTypes.TAG_PARAM:
                 case JavadocTokenTypes.TAG_RETURN:
                 case JavadocTokenTypes.TAG_THROWS:
                 case JavadocTokenTypes.TAG_EXCEPTION:
                 case JavadocTokenTypes.TAG_AUTHOR:
                 case JavadocTokenTypes.TAG_VERSION:
                     out.print(asterix, JavadocTokenTypes.PCDATA);
                     out.printNewline();
                     break;
             }
                 break;
         }
         break;
 }*/
     }
 
 
     /**
      * Prints the given paragraph.
      *
      * @param node root node of the paragraph.
      * @param asterix leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printParagraph(AST        node,
                                 String     asterix,
                                 NodeWriter out)
         throws IOException
     {
         switch (out.last)
         {
             case JavadocTokenTypes.CPARA :
             case JavadocTokenTypes.CBQUOTE :
             case JavadocTokenTypes.HR :
             case JavadocTokenTypes.CH1 :
             case JavadocTokenTypes.CTABLE :
             case JavadocTokenTypes.PRE :
             case JavadocTokenTypes.CULIST :
             case JavadocTokenTypes.COLIST :
             case JavadocTokenTypes.CDLIST :
             case JavadocTokenTypes.PCDATA :
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.printNewline();
         }
 
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.print(node.getText(), JavadocTokenTypes.OPARA);
 
         if (node.getFirstChild() != null)
         {
             out.printNewline();
 ITERATION:
             for (AST child = node.getFirstChild();
                  child != null;
                  child = child.getNextSibling())
             {
 SELECTION:
                 for (;;)
                 {
                     switch (child.getType())
                     {
                         case JavadocTokenTypes.PCDATA :
                         case JavadocTokenTypes.AT :
                         case JavadocTokenTypes.RCURLY :
                         case JavadocTokenTypes.LCURLY :
                         case JavadocTokenTypes.IMG :
                         case JavadocTokenTypes.BR :
                         case JavadocTokenTypes.OTTYPE :
                         case JavadocTokenTypes.OITALIC :
                         case JavadocTokenTypes.OBOLD :
                         case JavadocTokenTypes.OANCHOR :
                         case JavadocTokenTypes.OUNDER :
                         case JavadocTokenTypes.OSTRIKE :
                         case JavadocTokenTypes.OBIG :
                         case JavadocTokenTypes.OSMALL :
                         case JavadocTokenTypes.OSUB :
                         case JavadocTokenTypes.OSUP :
                         case JavadocTokenTypes.OEM :
                         case JavadocTokenTypes.OSTRONG :
                         case JavadocTokenTypes.ODFN :
                         case JavadocTokenTypes.OCODE :
                         case JavadocTokenTypes.OSAMP :
                         case JavadocTokenTypes.OKBD :
                         case JavadocTokenTypes.OVAR :
                         case JavadocTokenTypes.OCITE :
                         case JavadocTokenTypes.OACRO :
                         case JavadocTokenTypes.OFONT :
                             child = printText(child, asterix, out);
 
                             continue SELECTION;
 
                         case JavadocTokenTypes.TAG_INLINE_DOCROOT :
                         case JavadocTokenTypes.TAG_INLINE_LINK :
                         case JavadocTokenTypes.TAG_INLINE_LINKPLAIN :
                         case JavadocTokenTypes.TAG_INLINE_INHERITDOC :
                         case JavadocTokenTypes.TAG_INLINE_VALUE :
                         case JavadocTokenTypes.TAG_INLINE_CUSTOM :
                             out.print(LCURLY, JavadocTokenTypes.LCURLY);
                             child = printText(child, asterix, out);
                             out.print(RCURLY, JavadocTokenTypes.RCURLY);
 
                             continue SELECTION;
 
                         case JavadocTokenTypes.OBQUOTE :
                             printBlockquote(child, asterix, out);
 
                             break SELECTION;
 
                         case JavadocTokenTypes.PRE :
                             printPreformatted(child, asterix, out);
 
                             break SELECTION;
 
                         case JavadocTokenTypes.OULIST :
                         case JavadocTokenTypes.OOLIST :
                         case JavadocTokenTypes.ODLIST :
                             printList(child, asterix, out);
 
                             break SELECTION;
 
                         default :
                             break ITERATION;
                     }
                 }
             }
 
             out.print(asterix, JavadocTokenTypes.PCDATA);
             out.print(TAG_CPARA, JavadocTokenTypes.CPARA);
         }
         else
         {
             out.print(TAG_CPARA, JavadocTokenTypes.CPARA);
         }
 
         out.printNewline();
     }
 
 
     /**
      * Prints the given &lt;pre&gt; tag.
      *
      * @param node a PRE node.
      * @param asterix asterix to prepend before each line of the tag text.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printPreformatted(AST        node,
                                    String     asterix,
                                    NodeWriter out)
         throws IOException
     {
         String[] lines = split(node.getText(), out.originalLineSeparator, '*');
 
         /* int offset = asterix.indexOf('*');
 
 
 if (offset > -1)
 {
     if (lines.length > 0)
     {
         // add the whitespace that should be added after the asterix
         // before the first line (containing the <pre> tag)
         String whitespaceAfterAsterix = asterix.substring(offset + 1);
         lines[0] = whitespaceAfterAsterix + lines[0];
         // and strip whitespace after asterix to avoid unwanted
         // increasing indentation for all other lines
         //asterix = asterix.substring(0, offset + 1);
         String lastLine = lines[lines.length - 1];
         // add (or remove) the whitespace after the asterix
         // before the last line (containing the </pre> tag)
         if (new String(lines[lines.length - 1]).trim()
                                                .startsWith("</pre>"))
         {
             if ((whitespaceAfterAsterix.length() > 0) &&
                 (lastLine.charAt(0) == '<'))
             {
                 lines[lines.length - 1] = whitespaceAfterAsterix +
                                           lastLine;
             }
             else if ((whitespaceAfterAsterix.length() == 0) &&
                      lastLine.startsWith(" "))
             {
                 lines[lines.length - 1] = lastLine.trim();
             }
         }
     }
 }*/
         printCommentLines(lines, asterix, out);
         out.last = JavadocTokenTypes.PRE;
     }
 
 
     /**
      * Prints the given return tag.
      *
      * @param tag tag to print.
      * @param asterix the leading asterix.
      * @param maxWidth maximal width one line can consume.
      * @param added if <code>true</code> a warning message will be added that
      *        the tag was added.
      * @param last the type of the last printed tag.
      * @param out stream to write to.
      *
      * @return the type of the type printed.
      *
      * @throws IOException if an I/O error occured.
      *
      * @since 1.0b8
      */
     private int printReturnTag(AST        tag,
                                String     asterix,
                                int        maxWidth,
                                boolean    added,
                                int        last,
                                NodeWriter out)
         throws IOException
     {
         if (tag != null)
         {
             if (added)
             {
                 out.state.args[0] = out.getFilename();
                 out.state.args[1] = new Integer(out.line +
                                                 (shouldHaveNewlineBefore(tag,
                                                                          last)
                                                      ? 1
                                                      : 0));
                 out.state.args[2] = new Integer(out.getIndentLength() +
                                                 asterix.length() + 1);
                 out.state.args[3] = "@return";
                 out.state.args[4] = EMPTY_STRING;
                 Loggers.PRINTER_JAVADOC.l7dlog(Level.WARN, KEY_TAG_ADD_MISSING,
                                                out.state.args, null);
             }
 
             return printTag(tag, asterix, maxWidth, last, out);
         }
         else
         {
             return last;
         }
     }
 
 
     /**
      * Attempts to print the given node in one line.
      *
      * @param node owner of the comment.
      * @param comment comment to print.
      * @param out stream to write to.
      *
      * @return <code>true</code> if the node could be printed in one line.
      *
      * @throws IOException if an I/O error occured.
      */
     private boolean printSingleLineDescription(AST        node,
                                                AST        comment,
                                                NodeWriter out)
         throws IOException
     {
         StringBuffer buf = new StringBuffer();
         int maxwidth = this.prefs.getInt(Keys.LINE_LENGTH, Defaults.LINE_LENGTH) -
                        3 - out.getIndentLength();
 
         for (AST child = comment.getFirstChild();
              child != null;
              child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 // never print these in one line
                 case JavadocTokenTypes.OPARA :
                 case JavadocTokenTypes.TAG_SERIAL :
                 case JavadocTokenTypes.TAG_SERIAL_DATA :
                 case JavadocTokenTypes.TAG_SERIAL_FIELD :
                 case JavadocTokenTypes.TAG_PARAM :
                 case JavadocTokenTypes.TAG_RETURN :
                 case JavadocTokenTypes.TAG_THROWS :
                 case JavadocTokenTypes.TAG_EXCEPTION :
                 case JavadocTokenTypes.TAG_CUSTOM :
                 case JavadocTokenTypes.TAG_AUTHOR :
                 case JavadocTokenTypes.TAG_SEE :
                 case JavadocTokenTypes.TAG_VERSION :
                 case JavadocTokenTypes.TAG_SINCE :
                 case JavadocTokenTypes.TAG_DEPRECATED :
                     return false;
 
                 case JavadocTokenTypes.PCDATA :
                 case JavadocTokenTypes.AT :
                 case JavadocTokenTypes.RCURLY :
                 case JavadocTokenTypes.LCURLY :
                 case JavadocTokenTypes.IMG :
                 case JavadocTokenTypes.BR :
                     buf.append(child.getText());
 
                     break;
 
                 // physical text elements, shouldn't be used anymore
                 case JavadocTokenTypes.OTTYPE :
                 case JavadocTokenTypes.OITALIC :
                 case JavadocTokenTypes.OBOLD :
                 case JavadocTokenTypes.OANCHOR :
                 case JavadocTokenTypes.OUNDER :
                 case JavadocTokenTypes.OSTRIKE :
                 case JavadocTokenTypes.OBIG :
                 case JavadocTokenTypes.OSMALL :
                 case JavadocTokenTypes.OSUB :
                 case JavadocTokenTypes.OSUP :
 
                 // logical text elements
                 case JavadocTokenTypes.OEM :
                 case JavadocTokenTypes.OSTRONG :
                 case JavadocTokenTypes.ODFN :
                 case JavadocTokenTypes.OCODE :
                 case JavadocTokenTypes.OSAMP :
                 case JavadocTokenTypes.OKBD :
                 case JavadocTokenTypes.OVAR :
                 case JavadocTokenTypes.OCITE :
                 case JavadocTokenTypes.OACRO :
 
                 // special text elements
                 case JavadocTokenTypes.OFONT :
                     buf.append(child.getText());
                     buf.append(mergeChildren(child.getFirstChild()));
 
                     break;
 
                 // inline tags
                 case JavadocTokenTypes.TAG_INLINE_DOCROOT :
                 case JavadocTokenTypes.TAG_INLINE_LINK :
                 case JavadocTokenTypes.TAG_INLINE_LINKPLAIN :
                 case JavadocTokenTypes.TAG_INLINE_INHERITDOC :
                 case JavadocTokenTypes.TAG_INLINE_VALUE :
                 case JavadocTokenTypes.TAG_INLINE_CUSTOM :
                     buf.append(LCURLY);
                     buf.append(child.getText());
 
                     for (AST part = child.getFirstChild();
                          part != null;
                          part = part.getNextSibling())
                     {
                         buf.append(part.getText());
                     }
 
                     buf.append(RCURLY);
 
                     break;
             }
 
             if (buf.length() > maxwidth)
             {
                 return false;
             }
         }
 
         if (buf.length() < maxwidth)
         {
             out.print(SPACE, JavadocTokenTypes.JAVADOC_COMMENT);
             out.print(buf.toString().trim(), JavadocTokenTypes.JAVADOC_COMMENT);
 
             return true;
         }
 
         return false;
     }
 
 
     /**
      * Prints the given table.
      *
      * @param node root node of the table.
      * @param asterix leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printTable(AST        node,
                             String     asterix,
                             NodeWriter out)
         throws IOException
     {
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.printNewline();
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.print(node.getText(), JavadocTokenTypes.OTABLE);
         out.printNewline();
 
         for (AST row = node.getFirstChild();
              row != null;
              row = row.getNextSibling())
         {
             printTableRow(row, asterix, out);
         }
 
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.print("</table>", JavadocTokenTypes.CTABLE);
         out.printNewline();
     }
 
 
     /**
      * Prints the given table data.
      *
      * @param node root node of the table data.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printTableData(AST        node,
                                 String     asterix,
                                 NodeWriter out)
         throws IOException
     {
         out.print(node.getText(), JavadocTokenTypes.OTD);
         out.printNewline();
 
         if (node.getFirstChild() != null)
         {
             printContent(node.getFirstChild(), asterix, out);
         }
 
         switch (node.getType())
         {
             case JavadocTokenTypes.OTH :
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("</th>", JavadocTokenTypes.CTH);
 
                 break;
 
             case JavadocTokenTypes.OTD :
                 out.print(asterix, JavadocTokenTypes.PCDATA);
                 out.print("</td>", JavadocTokenTypes.CTD);
 
                 break;
         }
     }
 
 
     /**
      * Prints the given table row.
      *
      * @param node root node of the table row.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printTableRow(AST        node,
                                String     asterix,
                                NodeWriter out)
         throws IOException
     {
         out.print(asterix, JavadocTokenTypes.PCDATA);
         out.print(node.getText(), JavadocTokenTypes.O_TR);
 
         // out.printNewline();
         for (AST cell = node.getFirstChild();
              cell != null;
              cell = cell.getNextSibling())
         {
             printTableData(cell, asterix, out);
         }
 
         out.print("</tr>", JavadocTokenTypes.C_TR);
         out.printNewline();
     }
 
 
     /**
      * Prints the given tag.
      *
      * @param tag tag to print.
      * @param asterix the leading asterix.
      * @param maxwidth maximal width one line can consume.
      * @param last the type of the last printed tag.
      * @param out stream to write to.
      *
      * @return the type of the tag printed.
      *
      * @throws IOException if an I/O error occured.
      */
     private int printTag(AST        tag,
                          String     asterix,
                          int        maxwidth,
                          int        last,
                          NodeWriter out)
         throws IOException
     {
         if (tag != null)
         {
             printNewlineBefore(tag, last, asterix, out);
 
             out.print(asterix, JavaTokenTypes.JAVADOC_COMMENT);
 
             String ident = tag.getText();
             out.print(ident, JavaTokenTypes.JAVADOC_COMMENT);
 
             switch (tag.getType())
             {
                 // special case for @version tag as these may contain CVS
                 // version info strings that should never be wrapped
                 case JavadocTokenTypes.TAG_VERSION :
                 {
                     AST child = tag.getFirstChild();
 
                     // we trim the text so we have to take care to print a
                     // blank between tag name and description
                     if (child.getText().startsWith(SPACE) ||
                         child.getText().startsWith("<"))
                     {
                         out.print(SPACE, JavadocTokenTypes.JAVADOC_COMMENT);
                     }
 
                     String description = mergeChildren(child);
                     out.print(description.trim(),
                               JavadocTokenTypes.JAVADOC_COMMENT);
 
                     break;
                 }
 
                 // @tag name type description
                 case JavadocTokenTypes.TAG_SERIAL_FIELD :
 
                 // @tag name description
                 case JavadocTokenTypes.TAG_PARAM :
                 case JavadocTokenTypes.TAG_THROWS :
                 case JavadocTokenTypes.TAG_EXCEPTION :
                     printTagDescription(tag.getFirstChild(), ident, asterix,
                                         maxwidth, true, out);
 
                     break;
 
                 // @tag description
                 case JavadocTokenTypes.TAG_CUSTOM :
                 default :
                     printTagDescription(tag.getFirstChild(), ident, asterix,
                                         maxwidth, false, out);
 
                     break;
             }
 
             out.printNewline();
 
             return tag.getType();
         }
 
         return last;
     }
 
 
     /**
      * Prints the description of the given tag, if any.
      *
      * @param child the first child of the tag, starting the description.
      * @param name the tag name.
      * @param asterix string to print as leading asterix.
      * @param maxwidth maximal width one line can consume.
      * @param normalize if <code>true</code> the method tries to strip any
      *        whitespace between the first word and second word of the
      *        description.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      *
      * @since 1.0b9
      */
     private void printTagDescription(AST        child,
                                      String     name,
                                      String     asterix,
                                      int        maxwidth,
                                      boolean    normalize,
                                      NodeWriter out)
         throws IOException
     {
         if (child != null)
         {
             // we trim the text so we have to take care to print a
             // blank between tag name and description
             if (child.getText().startsWith(SPACE) ||
                 child.getText().startsWith("<"))
             {
                 out.print(SPACE, JavadocTokenTypes.JAVADOC_COMMENT);
             }
 
             String description = mergeChildren(child);
 
             // normalize the description if this is not an auto-generated tag
             if (normalize && (!description.startsWith("@")))
             {
                 if (_matcher.matches(description, _pattern))
                 {
                     MatchResult result = _matcher.getMatch();
 
                     if (result.group(1) != null)
                     {
                         StringBuffer buf = new StringBuffer(description.length());
                         buf.append(result.group(1));
                         buf.append(SPACE);
                         buf.append(result.group(2));
 
                         description = buf.toString();
                     }
                 }
             }
 
             int length = name.length();
             String[] lines = split(description, maxwidth - length - 1, true);
 
             for (int i = 0, size = lines.length - 1; i < size; i++)
             {
                 out.print(lines[i], JavadocTokenTypes.JAVADOC_COMMENT);
                 out.printNewline();
                 out.print(asterix, JavadocTokenTypes.JAVADOC_COMMENT);
                 out.print(out.getString(length + 1), JavaTokenTypes.WS);
             }
 
             out.print(lines[lines.length - 1],
                       JavadocTokenTypes.JAVADOC_COMMENT);
         }
 
         /**
          * @todo add semantic check
          */
     }
 
 
     /**
      * Prints all tags of the given comment.
      *
      * @param node owner of the comment.
      * @param comment the comment.
      * @param firstTag first tag to print.
      * @param asterix the leading asterix.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printTagSection(AST        node,
                                  AST        comment,
                                  AST        firstTag,
                                  String     asterix,
                                  NodeWriter out)
         throws IOException
     {
         List parameterTags = Collections.EMPTY_LIST;
         AST serialTag = null;
         AST serialDataTag = null;
         List serialFieldsTags = Collections.EMPTY_LIST;
         AST sinceTag = null;
         List seesTags = Collections.EMPTY_LIST;
         AST versionTag = null;
         List customTags = Collections.EMPTY_LIST;
         List authorTags = Collections.EMPTY_LIST;
         AST deprecatedTag = null;
         AST returnTag = null;
         List exceptionTags = Collections.EMPTY_LIST;
 
         boolean checkTags = this.prefs.getBoolean(Keys.COMMENT_JAVADOC_CHECK_TAG,
                                                   Defaults.COMMENT_JAVADOC_CHECK_TAG);
 
         if (checkTags)
         {
             checkTags = shouldCheckTags(node, comment);
         }
 
         for (AST tag = firstTag; tag != null; tag = tag.getNextSibling())
         {
             switch (tag.getType())
             {
                 case JavadocTokenTypes.TAG_PARAM :
 
                     if (parameterTags.isEmpty())
                     {
                         parameterTags = new ArrayList(4);
                     }
 
                     parameterTags.add(tag);
 
                     break;
 
                 case JavadocTokenTypes.TAG_SERIAL :
                     serialTag = tag;
 
                     break;
 
                 case JavadocTokenTypes.TAG_SERIAL_DATA :
                     serialDataTag = tag;
 
                     break;
 
                 case JavadocTokenTypes.TAG_SERIAL_FIELD :
 
                     if (serialFieldsTags.isEmpty())
                     {
                         serialFieldsTags = new ArrayList(4);
                     }
 
                     serialFieldsTags.add(tag);
 
                     break;
 
                 case JavadocTokenTypes.TAG_SINCE :
                     sinceTag = tag;
 
                     break;
 
                 case JavadocTokenTypes.TAG_SEE :
 
                     if (seesTags.isEmpty())
                     {
                         seesTags = new ArrayList(4);
                     }
 
                     seesTags.add(tag);
 
                     if (checkTags && (tag.getNextSibling() == null) &&
                         (tag == firstTag))
                     {
                         // checking only needed if no single @see tag found
                         // in description
                         checkTags = false;
                     }
 
                     break;
 
                 case JavadocTokenTypes.TAG_VERSION :
                     versionTag = tag;
 
                     break;
 
                 case JavadocTokenTypes.TAG_DEPRECATED :
                     deprecatedTag = tag;
 
                     break;
 
                 case JavadocTokenTypes.TAG_AUTHOR :
 
                     if (authorTags.isEmpty())
                     {
                         authorTags = new ArrayList(4);
                     }
 
                     authorTags.add(tag);
 
                     break;
 
                 case JavadocTokenTypes.TAG_RETURN :
                     returnTag = tag;
 
                     break;
 
                 case JavadocTokenTypes.TAG_THROWS :
                 case JavadocTokenTypes.TAG_EXCEPTION :
 
                     if (exceptionTags.isEmpty())
                     {
                         exceptionTags = new ArrayList(4);
                     }
 
                     exceptionTags.add(tag);
 
                     break;
 
                 case JavadocTokenTypes.TAG_CUSTOM :
 
                     if (customTags.isEmpty())
                     {
                         customTags = new ArrayList(5);
                     }
 
                     customTags.add(tag);
 
                     break;
             }
         }
 
         int maxwidth = this.prefs.getInt(Keys.LINE_LENGTH, Defaults.LINE_LENGTH) -
                        out.getIndentLength() - 3;
 
         int last = NONE;
 
         if (comment.getFirstChild() != firstTag)
         {
             last = DESCRIPTION;
         }
 
         // insert description if missing
         else if (checkTags)
         {
             /**
              * @todo log info/warning
              */
 
             // no description found, add missing
             out.print(asterix, JavadocTokenTypes.PCDATA);
 
             /**
              * @todo parse user specified Javadoc template
              */
             out.print("DOCUMENT ME!", JavadocTokenTypes.PCDATA);
             out.printNewline();
 
             last = DESCRIPTION;
         }
 
         boolean returnTagAdded = false;
         boolean checkParameterTags = false;
         boolean checkThrowsTags = false;
 
         // not all tags are valid for every node, so we only print the tags
         // that are valid
         switch (node.getType())
         {
             /**
              * @todo spit out warnings if we find invalid tags
              */
             case JavaTokenTypes.CLASS_DEF :
             case JavaTokenTypes.INTERFACE_DEF :
                 last = printTags(authorTags, asterix, maxwidth, last, out);
                 last = printTag(versionTag, asterix, maxwidth, last, out);
                 last = printTag(serialTag, asterix, maxwidth, last, out);
 
                 break;
 
             case JavaTokenTypes.VARIABLE_DEF :
                 last = printTag(serialTag, asterix, maxwidth, last, out);
                 last = printTags(serialFieldsTags, asterix, maxwidth, last, out);
 
                 break;
 
             case JavaTokenTypes.METHOD_DEF :
 
                 if (checkTags)
                 {
                     boolean tagPresent = returnTag != null;
                     returnTag = checkReturnTag(node, returnTag, out);
                     returnTagAdded = (!tagPresent) && (returnTag != null);
                 }
 
             // fall through
             case JavaTokenTypes.CTOR_DEF :
             {
                 last = printTag(serialDataTag, asterix, maxwidth, last, out);
 
                 if (getParamCount(node) > 0)
                 {
                     if (checkTags)
                     {
                         if (parameterTags.isEmpty())
                         {
                             parameterTags = new ArrayList(5);
                         }
 
                         checkParameterTags = true;
                     }
 
                     if (checkParameterTags)
                     {
                         last = printTags(parameterTags, asterix, maxwidth, node,
                                          JavaTokenTypes.PARAMETERS, last, out);
                     }
                     else
                     {
                         last = printTags(parameterTags, asterix, maxwidth, last,
                                          out);
                     }
                 }
 
                 switch (node.getType())
                 {
                     case JavaTokenTypes.METHOD_DEF :
                         last = printReturnTag(returnTag, asterix, maxwidth,
                                               returnTagAdded, last, out);
 
                         break;
                 }
 
                 if (checkTags)
                 {
                     if (exceptionTags.isEmpty())
                     {
                         exceptionTags = new ArrayList();
                     }
 
                     checkThrowsTags = true;
                 }
 
                 if (checkThrowsTags)
                 {
                     last = printTags(exceptionTags, asterix, maxwidth, node,
                                      JavaTokenTypes.LITERAL_throws, last, out);
                 }
                 else
                 {
                     last = printTags(exceptionTags, asterix, maxwidth, last,
                                      out);
                 }
 
                 break;
             }
         }
 
         last = printTags(customTags, asterix, maxwidth, last, out);
 
         // print the tags that can be used everywhere
         last = printTags(seesTags, asterix, maxwidth, last, out);
         last = printTag(sinceTag, asterix, maxwidth, last, out);
         last = printTag(deprecatedTag, asterix, maxwidth, last, out);
     }
 
 
     /**
      * Prints the given tags and checks for missing/obsolete ones.
      *
      * @param tags tags to print.
      * @param asterix string to print as leading asterix.
      * @param maxwidth maximal width one line can consume.
      * @param node associated node.
      * @param tagType the token type of the tags.
      * @param last if<code>true</code> an empty line will be printed before
      *        the tags.
      * @param out stream to write to.
      *
      * @return the type of the last tag printed.
      *
      * @throws IOException if an I/O error occured.
      *
      * @since 1.0b8
      */
     private int printTags(List       tags,
                           String     asterix,
                           int        maxwidth,
                           AST        node,
                           int        tagType,
                           int        last,
                           NodeWriter out)
         throws IOException
     {
         if ((tagType != -1) && (!tags.isEmpty()))
         {
             checkTags(node, tags, tagType, asterix, last, out);
         }
 
         return printTags(tags, asterix, maxwidth, last, out);
     }
 
 
     /**
      * Prints the given tags.
      *
      * @param tags tags to print.
      * @param asterix leading asterix.
      * @param maxwidth maximal width one line can consume.
      * @param last the type of the tag printed last.
      * @param out stream to write to.
      *
      * @return the type of the last printed tag.
      *
      * @throws IOException if an I/O error occured.
      */
     private int printTags(List       tags,
                           String     asterix,
                           int        maxwidth,
                           int        last,
                           NodeWriter out)
         throws IOException
     {
         for (int i = 0, size = tags.size(); i < size; i++)
         {
             last = printTag((AST)tags.get(i), asterix, maxwidth, last, out);
         }
 
         return last;
     }
 
 
     /**
      * Prints out all text and text level tags (&lt;tt&gt;, &lt;strong&gt;
      * etc.)
      *
      * @param node first node to print
      * @param asterix the leading asterix.
      * @param out stream to print to.
      *
      * @return the first non-text level node found (i.e the first node not
      *         printed) or the {@link #EMPTY_NODE} if all or none nodes have
      *         been printed.
      *
      * @throws IOException if an I/O error occured.
      */
     private AST printText(AST        node,
                           String     asterix,
                           NodeWriter out)
         throws IOException
     {
         StringBuffer buf = new StringBuffer(200);
         AST next = EMPTY_NODE;
 LOOP:
         for (AST child = node; child != null; child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavadocTokenTypes.COMMENT :
                     printComment(child, asterix, out);
 
                     break;
 
                 // text
                 case JavadocTokenTypes.PCDATA :
 
                 // special characters
                 case JavadocTokenTypes.AT :
                 case JavadocTokenTypes.RCURLY :
                 case JavadocTokenTypes.LCURLY :
                 case JavadocTokenTypes.IMG :
                 case JavadocTokenTypes.BR :
                     buf.append(child.getText());
 
                     break;
 
                 // physical text elements, shouldn't be used anymore
                 case JavadocTokenTypes.OTTYPE :
                 case JavadocTokenTypes.OITALIC :
                 case JavadocTokenTypes.OBOLD :
                 case JavadocTokenTypes.OANCHOR :
                 case JavadocTokenTypes.OUNDER :
                 case JavadocTokenTypes.OSTRIKE :
                 case JavadocTokenTypes.OBIG :
                 case JavadocTokenTypes.OSMALL :
                 case JavadocTokenTypes.OSUB :
                 case JavadocTokenTypes.OSUP :
 
                 // logical text elements
                 case JavadocTokenTypes.OEM :
                 case JavadocTokenTypes.OSTRONG :
                 case JavadocTokenTypes.ODFN :
                 case JavadocTokenTypes.OCODE :
                 case JavadocTokenTypes.OSAMP :
                 case JavadocTokenTypes.OKBD :
                 case JavadocTokenTypes.OVAR :
                 case JavadocTokenTypes.OCITE :
                 case JavadocTokenTypes.OACRO :
 
                 // special text elements
                 case JavadocTokenTypes.OFONT :
                     buf.append(child.getText());
                     buf.append(mergeChildren(child.getFirstChild()));
 
                     break;
 
                 // inline tags
                 case JavadocTokenTypes.TAG_INLINE_DOCROOT :
                 case JavadocTokenTypes.TAG_INLINE_LINK :
                 case JavadocTokenTypes.TAG_INLINE_LINKPLAIN :
                 case JavadocTokenTypes.TAG_INLINE_INHERITDOC :
                 case JavadocTokenTypes.TAG_INLINE_VALUE :
                 case JavadocTokenTypes.TAG_INLINE_CUSTOM :
                     buf.append(LCURLY);
                     buf.append(child.getText());
 
                     for (AST part = child.getFirstChild();
                          part != null;
                          part = part.getNextSibling())
                     {
                         // BUGFIX #581299
                         switch (part.getType())
                         {
                             case JavadocTokenTypes.OANCHOR :
                                 buf.append(' ');
 
                                 break;
                         }
 
                         buf.append(part.getText());
                     }
 
                     buf.append(RCURLY);
 
                     break;
 
                 // non text level element
                 default :
                     next = child;
 
                     // we only want text elements, so we quit here
                     break LOOP;
             }
         }
 
         if (buf.length() > 0)
         {
             int maxwidth = this.prefs.getInt(Keys.LINE_LENGTH,
                                              Defaults.LINE_LENGTH);
             printCommentLines(split(buf.toString().trim(),
                                     maxwidth - out.getIndentLength() - 3, true),
                               asterix, out);
         }
 
         return next;
     }
 
 
     /**
      * Indicates whether Javadoc tags should be checked.
      *
      * @param node the node that the Javadoc comment belongs to.
      * @param comment the Javadoc comment.
      *
      * @return <code>true</code> if <em>node</em> is a valid Javadoc node and
      *         the comment does start with the inheritDoc in-line tag.
      *
      * @since 1.0b8
      */
     private boolean shouldCheckTags(AST node,
                                     AST comment)
     {
         return isValidNode(node) && (!hasInheritDoc(comment));
     }
 
 
     /**
      * Determines whether the given tag should have an empty line before.
      *
      * @param tag a Javadoc tag.
      * @param last the type of the tag last printed.
      *
      * @return <code>true</code> if an empty should be printed.
      *
      * @since 1.0b9
      */
     private boolean shouldHaveNewlineBefore(AST tag,
                                             int last)
     {
         boolean result = false;
 
         switch (last)
         {
             case DESCRIPTION :
                 result = true;
 
                 break;
 
             case NONE :
                 break;
 
             default :
 
                 switch (tag.getType())
                 {
                     case JavadocTokenTypes.TAG_EXCEPTION :
                     case JavadocTokenTypes.TAG_THROWS :
 
                         switch (last)
                         {
                             case JavadocTokenTypes.TAG_EXCEPTION :
                             case JavadocTokenTypes.TAG_THROWS :
                                 break;
 
                             default :
                                 result = true;
 
                                 break;
                         }
 
                         break;
 
                     case JavadocTokenTypes.TAG_VERSION :
                         break;
 
                     case JavadocTokenTypes.TAG_PARAM :
                     case JavadocTokenTypes.TAG_CUSTOM :
                     case JavadocTokenTypes.TAG_AUTHOR :
 
                         if (last != tag.getType())
                         {
                             result = true;
                         }
 
                         break;
 
                     case JavadocTokenTypes.TAG_RETURN :
                         result = true;
 
                         break;
 
                     case JavadocTokenTypes.TAG_SEE :
 
                         if (last != tag.getType())
                         {
                             result = true;
                         }
 
                         break;
 
                     default :
 
                         switch (last)
                         {
                             case JavadocTokenTypes.TAG_PARAM :
                             case JavadocTokenTypes.TAG_RETURN :
                             case JavadocTokenTypes.TAG_THROWS :
                             case JavadocTokenTypes.TAG_EXCEPTION :
                             case JavadocTokenTypes.TAG_AUTHOR :
                             case JavadocTokenTypes.TAG_VERSION :
                                 result = true;
 
                                 break;
                         }
 
                         break;
                 }
 
                 break;
         }
 
         return result;
     }
 
 
     /**
      * Splits the given string into tokens.
      *
      * @param str string to split into tokens.
      * @param delim the delimeter to use for splitting.
      * @param character character > -1 all leading whitespace before the
      *        character will be removed.
      *
      * @return 1.0b8
      */
     private String[] split(String str,
                            String delim,
                            char   character)
     {
         if (character > -1)
         {
             int startOffset = 0;
             int endOffset = -1;
             int sepLength = delim.length();
             List lines = new ArrayList(15);
 
             while ((endOffset = str.indexOf(delim, startOffset)) > -1)
             {
                 String line = str.substring(startOffset, endOffset);
                 lines.add(trimLeadingWhitespace(line, character));
                 startOffset = endOffset + sepLength;
             }
 
             if (startOffset > 0)
             {
                 String line = trimLeadingWhitespace(str.substring(startOffset),
                                                     character);
                 lines.add(line);
             }
             else
             {
                 lines.add(trimLeadingWhitespace(str, character));
             }
 
             return (String[])lines.toArray(EMPTY_STRING_ARRAY);
         }
         else
         {
             return split(str, delim);
         }
     }
 
 
     /**
      * Splits the given string into tokens.
      *
      * @param str string to split into tokens.
      * @param delim the delimeter to use for splitting.
      *
      * @return array with the tokens.
      *
      * @since 1.0b8
      */
     private String[] split(String str,
                            String delim)
     {
         int startOffset = 0;
         int endOffset = -1;
         int sepLength = delim.length();
         List lines = new ArrayList(15);
 
         while ((endOffset = str.indexOf(delim, startOffset)) > -1)
         {
             lines.add(str.substring(startOffset, endOffset));
             startOffset = endOffset + sepLength;
         }
 
         if (startOffset > 0)
         {
             lines.add(str.substring(startOffset));
         }
         else
         {
             lines.add(str);
         }
 
         return (String[])lines.toArray(EMPTY_STRING_ARRAY);
     }
 
 
     /**
      * Splits the given string into multiple lines.
      *
      * @param str the string to split.
      * @param width the maximum width of each line.
      * @param trim if <code>true</code> the individual lines will be trimmed.
      *
      * @return array with the splitted lines.
      *
      * @since 1.0b8
      */
     private String[] split(String  str,
                            int     width,
                            boolean trim)
     {
         List lines = new ArrayList();
 
         if (trim)
         {
             str = str.trim();
         }
 
         if (str.length() < width)
         {
             lines.add(str);
         }
         else
         {
             BreakIterator iterator = (BreakIterator)_stringBreaker.get();
 
             try
             {
                 iterator.setText(str);
 
                 int lineStart = 0;
                 int nextStart = iterator.next();
                 int prevStart = 0;
 
                 do
                 {
 MOVE_FORWARD:
                     while (((nextStart - lineStart) < width) &&
                            (nextStart != BreakIterator.DONE))
                     {
                         prevStart = nextStart;
                         nextStart = iterator.next();
 
                         switch (iterator._type)
                         {
                             case BreakIterator.BREAK :
                                 prevStart = nextStart + 4;
 
                                 break MOVE_FORWARD;
                         }
                     }
 
                     if (prevStart == 0)
                     {
                         prevStart = nextStart;
                     }
 
                     if (nextStart == BreakIterator.DONE)
                     {
                         // if the text before and after the last space fits
                         // into the max width, just print it on one line
                         if (((prevStart - lineStart) + (str.length() - prevStart)) < width)
                         {
                             lines.add(str.substring(lineStart, str.length())
                                          .trim());
                         }
                         else
                         {
                             if ((prevStart > 0) &&
                                 (prevStart != BreakIterator.DONE))
                             {
                                 if (trim)
                                 {
                                     lines.add(str.substring(lineStart,
                                                             prevStart).trim());
                                     lines.add(str.substring(prevStart).trim());
                                 }
                                 else
                                 {
                                     lines.add(str.substring(lineStart,
                                                             prevStart));
                                     lines.add(str.substring(prevStart));
                                 }
                             }
                             else
                             {
                                 if (trim)
                                 {
                                     lines.add(str.substring(lineStart).trim());
                                 }
                                 else
                                 {
                                     lines.add(str.substring(lineStart));
                                 }
                             }
                         }
 
                         prevStart = str.length();
                     }
                     else
                     {
                         if (trim)
                         {
                             lines.add(str.substring(lineStart, prevStart).trim());
                         }
                         else
                         {
                             lines.add(str.substring(lineStart, prevStart));
                         }
                     }
 
                     lineStart = prevStart;
                     prevStart = 0;
                 }
                 while (lineStart < str.length());
             }
             finally
             {
                 iterator.reset();
             }
         }
 
         return (String[])lines.toArray(new String[0]);
     }
 
 
     private String trimLeadingWhitespace(String str,
                                          char   character)
     {
         int off = StringHelper.indexOfNonWhitespace(str);
 
         if ((off > -1) && (str.charAt(off) == character))
         {
             return str.substring(off + 1);
         }
 
         return str;
     }
 
 
     /**
      * Removes trailing whitespace from the given str.
      *
      * @param str the string to trim.
      *
      * @return a copy of the string with trailing whitespace removed, or the
      *         string if it has no trailing whitespace.
      */
     private String trimTrailing(String str)
     {
         int index = str.length();
 
         for (;
              (index > 0) && Character.isWhitespace(str.charAt(index - 1));
              index--)
         {
             ;
         }
 
         if (index != str.length())
         {
             return str.substring(0, index);
         }
         else
         {
             return str;
         }
     }
 
     //~ Inner Classes 
 
     private static class BreakIterator
     {
         private static final int WHITESPACE = 1;
         private static final int BREAK = 2;
         public static final int DONE = -10;
         private static final String TAG_BREAK = "<br>";
         private static final String TAG_BREAK_WELL = "<br/>";
         private String _text;
         private int _end = -1;
         private int _pos = -1;
         private int _type;
 
         public BreakIterator()
         {
         }
 
         /**
          * Returns the boundary following the current boundary.
          *
          * @return the character index of the next text boundary or {@link
          *         #DONE} if all boundaries have been returned.
          */
         public int getBreakType()
         {
             return _type;
         }
 
 
         public void setText(String text)
         {
             _text = text;
         }
 
 
         public int next()
         {
             _type = WHITESPACE;
             _pos = _text.indexOf(' ', _end + 1);
 
             if (_pos > -1)
             {
                 int tab = _text.indexOf('\t', _end + 1);
 
                 if ((tab > -1) && (tab < _pos))
                 {
                     _pos = tab;
                 }
 
                 int br = _text.indexOf(TAG_BREAK, _end + 1);
 
                 if (br == -1)
                 {
                     br = _text.indexOf(TAG_BREAK_WELL, _end + 1);
                 }
 
                 if ((br > -1) && (br < _pos))
                 {
                     _pos = br;
                     _type = BREAK;
                 }
             }
 
             if (_pos == -1)
             {
                 return DONE;
             }
 
             _end = _pos;
 
             return _pos;
         }
 
 
         public void reset()
         {
             _text = null;
             _end = -1;
             _pos = -1;
         }
     }
 }

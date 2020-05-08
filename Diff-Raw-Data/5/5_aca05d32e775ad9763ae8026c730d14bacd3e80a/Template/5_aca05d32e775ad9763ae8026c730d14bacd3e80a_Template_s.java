 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import java.util.*;
 
 /**
  * The Template class substitutes values from a Dataset into a template
  * string to produce a new string through a process called "expansion".
  * The goal for templates is to provide a simple mechanism for handling
  * the most common kinds of substitution; more complex forms, such as
  * iteration or recursion, are left to Java code.  Each character from
  * the template is copied to the output string except for the following
  * patterns, which cause substitutions:
  * @<i>name</i>             Copy the contents of the dataset element named
  *                          <i>name</i> to the output.  <i>name</i>
  *                          consists of all the standard Unicode identifier
  *                          characters following the {@code @}. It is an error
  *                          if the name doesn't exist in the dataset.
  * @(<i>name</i>)           Copy the contents of the data value named
  *                          <i>name</i> to the output.  <i>name</i>
  *                          consists of all the characters following the "("
  *                          up to the next ")".  {@code @} can be used
  *                          between the parentheses to perform substitutions
  *                          on the name; for example, {@code @(@foo)} finds
  *                          dataset element {@code foo}, uses its value as the
  *                          name of another dataset element, and substitutes
  *                          the value of that element into the output.  It is
  *                          an error if the name doesn't exist in the dataset.
  * @<i>name</i>?{...}       Default: if <i>name</i> exists in the dataset
  *                          and has a nonzero length, copy its contents
  *                          to the output and skip over {@code ?{...}}.
  *                          Otherwise, perform template expansion on the
  *                          text between the braces.
  * @<i>name</i>?{<i>t1</i>|<i>t2</i>}    Choice: if <i>name</i> exists in the
  *                          dataset perform template expansion on <i>t1</i>.
  *                          Otherwise perform template expansion on <i>t2</i>.
  * {{...}}                  Conditional substitution: normally the information
  *                          between the braces is processed just like the rest
  *                          of the template, except that the braces are not
  *                          copied to the output.  However, if there is a
  *                          data reference for which the name doesn't exist
  *                          then the information between the braces skipped:
  *                          nothing is copied to the output.
  * @@                       Append "@" to the output.
  * @{                       Append "{" to the output.
  * @}                       Append "}" to the output.
  * @*                       Any other occurrence of "@" besides those
  *                          described above is illegal and results in an error.
  * When a dataset element is substituted into a template, special characters
  * in the value will be escaped according to the method name. For example, if
  * HTML encoding has been specified, such as by calling expandHtml,
  * {@code <} characters will be translated to the entity reference
  * {@code &lt;}.  Translation occurs only for values coming from the
  * dataset, not for characters in the template itself;  you should ensure
  * that template characters already obey the output encoding rules.
  * Translation can be disabled for dataset values by invoking one of the the
  * methods ending in "Raw".
  *
  * When invoking template expansion, you can also provide data values using
  * one or more Objects instead of (or in addition to) a Dataset.  In this
  * case, you use numerical specifiers such as {@code @3} to refer to the
  * values: {@code @1} refers to the string value of the first object,
  * {@code @2} refers to the string value of the second object, and so on.
  * If the @ starts with a number, the name ends at the first non-numeric
  * character. E.g, "@2b" refers to the second object followed by a "b".
  * Use @(2b) to refer to a dataset value with key "2b"
  */
 
 public class Template {
     /**
      * MissingValueError is thrown when a dataset element required by a
      * template does not exist.
      */
     public static class MissingValueError extends Error {
         String name;
         CharSequence template;
 
         /**
          * Constructs a MissingValueError
          * @param name        Value which could not be found
          * @param template    Template which was being expanded
          */
         public MissingValueError(String name, CharSequence template) {
             this.name = name;
             this.template = template;
         }
 
         public String getMessage() {
             return "missing value \"" + name + "\" " + "in template \"" + template + "\"";
         }
     }
 
     /**
      * SyntaxError is thrown when there is an incorrect construct in a
      * template, such as an {@code @} followed by an unrecognized character.
      */
     public static class SyntaxError extends Error {
         /**
          * Constructs a SyntaxError with a given message.
          * @param message          Detailed information about the problem
          */
         public SyntaxError(String message) {
             super(message);
         }
     }
 
     /**
      * Instances of this enum indicate how to escape special characters
      * in data values incorporated into template output.
      */
     protected enum SpecialChars {
         /**
          * The output will be used in HTML, so replace special HTML
          * characters such as {@code <} with HTML entities such as
          * {@code &lt;}.
          */
         HTML,
 
         /**
          * The output will be used as Javascript code; assume that all
          * substituted data values will be used in Javascript strings,
          * so use backslashes to quote special characters.
          */
         JS,
 
         /**
          * The output will be used as part of a URL, so use {@code %xx}
          * encoding for any characters that aren't permitted in URLs.
          */
         URL,
 
         /** Don't perform any transformations on the data values. */
         NONE}
 
     /**
      * A cache of all parsed templates. Keys are template strings.
      */
     protected static Map<CharSequence, ParsedTemplate> parsedTemplates =
         Collections.synchronizedMap(new HashMap<CharSequence, ParsedTemplate>());
 
     /**
      * A ParsedTemplate is an efficient representation of a template after it has
      * been parsed, so that we do not need to reparse a template every time it is
      * used.
      */
     protected static class ParsedTemplate {
         // List of fragments that make up the template
         protected ArrayList<Fragment> fragments;
 
         /**
          * Creates a new ParsedTemplate with no fragments.
          */
         protected ParsedTemplate() {
             this.fragments = new ArrayList<Fragment>();
         }
 
         /**
          * Creates a new ParsedTemplate with the given fragments.
          * @param fragments   List of fragments describing the template
          */
         protected ParsedTemplate(ArrayList<Fragment> fragments) {
             this.fragments = fragments;
         }
 
         /**
          * Expands a cached template, substituting values and quoting data.
          * @param info         Information describing the current expansion
          */
         public void expand(ExpandInfo info) {
             for (Fragment fragment : fragments) {
                 fragment.expand(info);
             }
         }
 
         /**
          * Appends the fragment to this cache's list of fragments.
          * @param fragment    Describes part of this template
          */
         protected void addFragment(Fragment fragment) {
             fragments.add(fragment);
         }
     }
 
     /**
      * A template is parsed into a list of {@code Fragment} objects. Expanding
      * each of these in a row is equivalent to expanding the template.
      */
     protected static interface Fragment {
         /**
          * Evaluate the fragment and add its value to the output
          * @param info         Information describing the current expansion
          */
         public abstract void expand(ExpandInfo info);
     }
 
     /**
      * Represents plain text in a template. No substition is done on this
      * text.
      */
     protected static class TextFragment implements Fragment {
         String text;
 
         /**
          * Creates a new TextFragment
          * @param text       String this fragment is representing
          */
         public TextFragment(String text) {
             this.text = text;
         }
 
         /**
          * Evaluate the fragment and add its value to the output
          * @param info         Information describing the current expansion
          */
         public void expand(ExpandInfo info) {
             info.out.append(text);
         }
     }
 
     /**
      * Represents one @ variable, such as @foo, @1, or @(@foo)
      */
     protected static class IdFragment implements Fragment {
         String name;
         // If the name is a number, such as "5", index represents the integer
         // version of the number. Otherwise the index is -1.
         int index;
         // If the @ is the form of @(...), this object represents the parsed
         // version of the text inside the parens. Otherwise this is null
         ParsedTemplate parens;
 
         /**
          * Creates a new IdFragment
          * @param name   Name of the @ variable. When the template is evaluated
          *               it'll get expanded with a value from a dataset
          */
         public IdFragment(String name) {
             this.name = name;
             try {
                 index = Integer.parseInt(this.name);
             } catch (NumberFormatException e) {
                 index = -1;
             }
             this.parens = null;
         }
 
         /**
          * Creates a new IdFragment corresponding to text inside parens: @(...)
          * @param parens  Parsed representation of the text in the parens. When
          *                the template is evaluated, this will be expanded and
          *                used as the name of a value to substitute
          */
         public IdFragment(ParsedTemplate parens) {
             this.parens = parens;
         }
 
         /**
          * Evaluate the fragment and add its value to the output
          * @param info         Information describing the current expansion
          */
         public void expand(ExpandInfo info) {
             addValue(info, findValue(info, true));
         }
 
         /**
         * Finds and quotes a value from a dataset or indexed data
          * @param info         Information describing the current expansion
          * @param required     If true, throws a MissingValueError if the
          *                     request value cannot be found
         * @return             The quoted valued associated with this fragment
          */
         public String findValue(ExpandInfo info, boolean required) {
             String tmpName = this.name;
             int tmpIndex = this.index;
 
             if (parens != null) {
                 int outLen = info.out.length();
                 SpecialChars oldQuoting = info.quoting;
                 info.quoting = SpecialChars.NONE;
 
                 parens.expand(info);
 
                 tmpName = info.out.substring(outLen);
                 info.quoting = oldQuoting;
                 info.out.setLength(outLen);
                 try {
                     tmpIndex = Integer.parseInt(tmpName);
                 } catch (NumberFormatException e) {
                     tmpIndex = -1;
                 }
             }
 
             String value = null;
             // If there is indexed data in this expansion and the name is an
             // integer, use an indexed value if there is one corresponding to
             // the name.
             if (tmpIndex > 0 && info.indexedData != null) {
                 if (tmpIndex <= info.indexedData.length) {
                     Object tmp = info.indexedData[tmpIndex-1];
                     if (tmp != null) {
                         value = tmp.toString();
                     }
                 }
             } else {
                 if (info.data != null) {
                     value = info.data.checkString(tmpName);
                 }
             }
 
             if (value != null || !required) {
                 return value;
             }
 
             throw new MissingValueError(tmpName, info.template);
         }
     }
 
     /**
      * Represents part of a template in the form of @id?{...}
      */
     protected static class DefaultFragment implements Fragment {
         // id in @id?(frag)
         IdFragment id;
         // frag in @id?(frag)
         ParsedTemplate defaultOption;
 
         /**
          * Creates a DefaultFragment
          * @param id              The @ variable. If it exists, it is added to
          *                        the output. Otherwise, {@code fragment} is.
          * @param defaultOption   Added to the output if {@code id} does not exist
          */
         public DefaultFragment(IdFragment id, ParsedTemplate defaultOption) {
             this.id = id;
             this.defaultOption = defaultOption;
         }
 
         /**
          * Evaluate the fragment and add its value to the output
          * @param info         Information describing the current expansion
          */
         public void expand(ExpandInfo info) {
             String value = id.findValue(info, false);
             if (value == null || value.length() == 0) {
                 defaultOption.expand(info);
             } else {
                 addValue(info, value);
             }
         }
     }
 
     /**
      * Represents part of a template in the form @id{t1|t2}.
      */
     protected static class ChoiceFragment implements Fragment {
         // id in @id{t1, t2}
         IdFragment id;
         // t1 in @id{t1, t2}
         ParsedTemplate first;
         // t2 in @id{t1, t2}
         ParsedTemplate second;
         /**
          * Creates a new ChoiceFragment
          * @param id      If id exists, first is expanded to the output,
          *                otherwise second is
          * @param first   Added to the output if {@code id} exists
          * @param second  Added to the output if {@code id} does not exist
          */
         public ChoiceFragment(IdFragment id, ParsedTemplate first, ParsedTemplate second) {
             this.id = id;
             this.first = first;
             this.second = second;
         }
 
         /**
          * Evaluate the fragment and add its value to the output
          * @param info         Information describing the current expansion
          */
         public void expand(ExpandInfo info) {
             String value = id.findValue(info, false);
             if (value == null || value.length() == 0) {
                 second.expand(info);
             } else {
                 first.expand(info);
             }
         }
     }
 
     /**
      * Represents part of a template in the form {{...}}
      */
     protected static class ConditionalFragment implements Fragment {
         // Parsed representation of text inside brackets
         ParsedTemplate contents;
 
         /**
          * Creates a new ConditionalFragment
          * @param contents    Added to the output if all @ variables in it exist.
          *                    Otherwise, nothing is added to the output.
          */
         public ConditionalFragment(ParsedTemplate contents) {
             this.contents = contents;
         }
 
         /**
          * Evaluate the fragment and add its value to the output
          * @param info         Information describing the current expansion
          */
         public void expand(ExpandInfo info) {
             int outLen = info.out.length();
             int sqlLen = 0;
             if (info.sqlParameters != null) {
                 sqlLen = info.sqlParameters.size();
             }
 
             try {
                 contents.expand(info);
             } catch (MissingValueError e) {
                 info.out.setLength(outLen);
                 if (info.sqlParameters != null) {
                     for (int size = info.sqlParameters.size(); size > sqlLen; ) {
                         size--;
                         info.sqlParameters.remove(size);
                     }
                 }
             }
         }
     }
 
     // Holds information used during expansion
     protected static class ExpandInfo {
         // Output is appended here
         protected StringBuilder out;
         // String representation of the template we are expanding
         // (Used in error messages)
         protected CharSequence template;
         // Style of quoting to use
         protected SpecialChars quoting;
         // If we are expanding for a SQL query, values are added here instead
         // of being substituted in the template
         protected ArrayList sqlParameters;
         // Dataset of values to substitute
         protected Dataset data;
         // Indexed data to substitute
         protected Object[] indexedData;
 
         protected ExpandInfo(StringBuilder out, CharSequence template,
                              SpecialChars quoting, ArrayList<String> sqlParameters,
                              Dataset data, Object ... indexedData) {
             if (out == null) {
                 out = new StringBuilder();
             }
             this.out = out;
             this.template = template;
             this.quoting = quoting;
             this.sqlParameters = sqlParameters;
             this.data = data;
             this.indexedData = indexedData;
         }
     }
 
 
     // The following class is used internally to pass information between
     // methods.  Among other things, it provides a vehicle for returning
     // additional results from a method.
     protected static class ParseInfo {
         CharSequence template;     // The template being processed.
         int end;                   // Modified by methods such as
                                    // parseName to hold the index of the
                                    // character just after the last one
                                    // processed by the method.  For example,
                                    // if the method processed "@(name)"
                                    // then this value will give the index
                                    // of the next character after the ")".
         StringBuilder text;        // When parsing plain text (e.g., not a @),
                                    // text is appended here before being turned
                                    // into a fragment
         ParsedTemplate parse;      // Fragments are appended to this cache
         int lastDeletedSpace;      // Used in collapsing space characters
                                    // around braces to handle cumulative
                                    // situations like "<{{@a}} {{@b}} {{@c}}>":
                                    // if all three sets in braces drop out,
                                    // we want to eliminate exactly 2 spaces.
                                    // This field holds the index into the
                                    // template of the last space character
                                    // we collapsed out, or -1 if none.
         /**
          * Construct a ParseInfo object.
          * @param template         Template to be expanded.
          */
         public ParseInfo(CharSequence template) {
             this.template = new StringBuilder(template);
             this.text = new StringBuilder();
             this.parse = new ParsedTemplate();
         }
     }
 
     // No constructor: this class only has a static methods.
     private Template() {}
 
     /**
      * Adds a value to the ouput, escaping it if necessary
      * @param info         Information describing the current expansion.
      *                     info.quoting determines what sort of quoting to do
      * @param value        String which is quoted and added to output
      */
     protected static void addValue(ExpandInfo info, String value) {
         if (info.quoting == SpecialChars.HTML) {
             Html.escapeHtmlChars(value, info.out);
         } else if (info.quoting == SpecialChars.JS) {
             Html.escapeStringChars(value, info.out);
         } else if (info.quoting == SpecialChars.URL) {
             Html.escapeUrlChars(value, info.out);
         } else if (info.quoting == SpecialChars.NONE) {
             info.out.append(value);
         } else if (info.sqlParameters != null) {
             info.sqlParameters.add(value);
             info.out.append("?");
         } else {
             throw new InternalError("unknown quoting value in " +
                                     "Template.quoteString");
         }
     }
 
     /**
      * Expands a template
      * @param out             Output is appended here. If null, a new StringBuilder
      *                        is created
      * @param template        Template to expand
      * @param quoting         Style of quoting used on substituted values
      * @param data            Provides data to be substituted into the template.
      * @param indexedData     One or more objects, whose values can be
      *                        referred to in the template with
      *                        numerical specifiers such as {@code @1}.
      *                        Null values may be supplied to indicate
      *                        "no object with this index".
      * @return                The expanded template
      */
     protected static StringBuilder expand(StringBuilder out, CharSequence template,
                      SpecialChars quoting, Dataset data, Object ... indexedData) {
         return expand(out, template, quoting, null, data, indexedData);
     }
 
     /**
      * Expands a template
      * Returns a parsed version of the template, creating it if necessary.
      * @param out             Output is appended here. If null, a new StringBuilder
      *                        is created
      * @param template        Template to expand
      * @param quoting         Style of quoting used on substituted values
      * @param sqlParameters   If not null, substituted values are appended here
      * @param data            Provides data to be substituted into the template.
      * @param indexedData     One or more objects, whose values can be
      *                        referred to in the template with
      *                        numerical specifiers such as {@code @1}.
      *                        Null values may be supplied to indicate
      *                        "no object with this index".
      * @return                The expanded template
      */
     protected static StringBuilder expand(StringBuilder out, CharSequence template,
                      SpecialChars quoting, ArrayList<String> sqlParameters,
                      Dataset data, Object ... indexedData) {
         ParsedTemplate parsed = parsedTemplates.get(template);
         if (parsed == null) {
             ParseInfo info = new ParseInfo(template);
             parseTo(info, 0);
             parsed = info.parse;
             parsedTemplates.put(template, parsed);
         }
 
         ExpandInfo info = new ExpandInfo(out, template, quoting, sqlParameters, data, indexedData);
         parsed.expand(info);
         return info.out;
     }
 
     /**
      * Substitute data into a template string, using HTML conventions
      * for escaping special characters in substituted values.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the result of the
      *                             expansion.
      */
     public static String expandHtml(CharSequence template, Dataset data,
             Object ... indexedData) throws SyntaxError {
         return expand(null, template, SpecialChars.HTML, data,
                       indexedData).toString();
     }
 
     /**
      * Substitute data into a template string, using HTML conventions
      * for escaping special characters in substituted values.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the results of the
      *                             expansion.
      */
     public static String expandHtml(CharSequence template,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.HTML, null,
                       indexedData).toString();
     }
 
     /**
      * Substitute data into a template string, using HTML conventions
      * for escaping special characters in substituted values.
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendHtml(StringBuilder out, CharSequence template,
                                   Dataset data, Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.HTML, data, indexedData);
     }
 
     /**
      * Substitute data into a template string, using HTML conventions
      * for escaping special characters in substituted values.
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendHtml(StringBuilder out, CharSequence template,
                                   Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.HTML, null, indexedData);
     }
 
     /**
      * Substitute data into a template string, using Javascript string
      * conventions for escaping special characters in substituted values.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the result of the
      *                             expansion.
      */
     public static String expandJs(CharSequence template, Dataset data,
             Object ... indexedData)
             throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.JS, data,
                       indexedData).toString();
     }
 
     /**
      * Substitute data into a template string, using Javascript string
      * conventions for escaping special characters in substituted values.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the results of the
      *                             expansion.
      */
     public static String expandJs(CharSequence template,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.JS, null,
                                           indexedData).toString();
     }
 
     /**
      * Substitute data into a template string, using Javascript string
      * conventions for escaping special characters in substituted values.
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendJs(StringBuilder out,
             CharSequence template, Dataset data, Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.JS, data, indexedData);
     }
 
     /**
      * Substitute data into a template string, using Javascript string
      * conventions for escaping special characters in substituted values.
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendJs(StringBuilder out,
             CharSequence template, Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.JS, null, indexedData);
     }
 
     /**
      * Substitute data into a template string, using URL encoding for
      * escaping special characters in substituted values.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the result of the
      *                             expansion.
      */
     public static String expandUrl(CharSequence template, Dataset data,
             Object ... indexedData)
             throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.URL, data,
                       indexedData).toString();
     }
 
     /**
      * Substitute data into a template string, using URL encoding for
      * escaping special characters in substituted values.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the results of the
      *                             expansion.
      */
     public static String expandUrl(CharSequence template,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.URL, null,
                                           indexedData).toString();
     }
 
     /**
      * Substitute data into a template string, using URL encoding for
      * escaping special characters in substituted values.
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendUrl(StringBuilder out, CharSequence template,
             Dataset data, Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.URL, data, indexedData);
     }
 
     /**
      * Substitute data into a template string, using URL encoding for
      * escaping special characters in substituted values.
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendUrl(StringBuilder out, CharSequence template,
             Object... indexedData) throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.URL, null, indexedData);
     }
 
     /**
      * Substitute data into a template string (no escaping of special
      * characters in substituted values).
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the result of the
      *                             expansion.
      */
     public static String expandRaw(CharSequence template, Dataset data,
             Object ... indexedData)
             throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.NONE, data,
                       indexedData).toString();
     }
 
     /**
      * Substitute data into a template string (no escaping of special
      * characters in substituted values).
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      * @return                     A String containing the results of the
      *                             expansion.
      */
     public static String expandRaw(CharSequence template,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         return expand(null, template, SpecialChars.NONE, null,
                       indexedData).toString();
     }
 
     /**
      * Substitute data into a template string (no escaping of special
      * characters in substituted values).
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendRaw(StringBuilder out, CharSequence template,
             Dataset data, Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.NONE, data, indexedData);
     }
 
     /**
      * Substitute data into a template string (no escaping of special
      * characters in substituted values).
      * @param out                  The results of expansion are appended here.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void appendRaw(StringBuilder out, CharSequence template,
             Object... indexedData)
             throws MissingValueError, SyntaxError {
         expand(out, template, SpecialChars.NONE, null, indexedData);
     }
 
     /**
      * Substitute data into a template SQL query and return the expanded
      * result.  Because of the way variables are handled in SQL queries,
      * variable values are not substituted directly into the query.  Instead,
      * each substitution causes a "?" character to appear in the output query.
      * The variable values are collected in a separate ArrayList for the
      * caller, which will then invoke a JDBC method to attach them to the
      * SQL statement for the query.
      * @param template             Contains an SQL query that may contain
      *                             substitution specifiers such as
      *                             {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param sqlParameters        For each substitution in {@code template},
      *                             the value for the substitution is appended
      *                             to this ArrayList and "?" is appended to
      *                             the result string.  The caller will pass
      *                             {@code sqlParameters} to JDBC when it
      *                             invokes the SQL statement.
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expandSql(CharSequence template, Dataset data,
             ArrayList<String> sqlParameters)
             throws MissingValueError, SyntaxError {
         return expand(null, template, null, sqlParameters, data).toString();
     }
 
     /**
      * Parses a template until one of the delimiters is matched
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends
      *                             framents to info.parse. Info.end is set
      *                             to the index of the first character following
      *                             the @-specifier (e.g. for {@code @foo+bar}
      *                             info.end will refer to the {@code +} and for
      *                             {@code @abc d} info.end will refer to the
      *                             space.
      * @param start                Begin parsing at this position
      * @param delimiters           List of delimiters that marks the end of the
      *                             template
      */
     protected static void parseTo(ParseInfo info, int start, String ... delimiters) {
         boolean foundEnd = false;
         int len = info.template.length();
 
         int i;
         for (i = start; !foundEnd && i != len; ) {
             char c = info.template.charAt(i);
             char next = 0;
             if (i + 1 != len) {
                 next = info.template.charAt(i+1);
             }
 
             if (c == '@') {
                 parseAtSign(info, i+1);
                 i = info.end;
             } else if (c == '{' && next == '{') {
                 parseBraces(info, i+2);
                 i = info.end;
             } else if (foundDelimiter(info.template, i, delimiters)) {
                 foundEnd = true;
             } else {
                 info.text.append(c);
                 i++;
             }
         }
 
         flushText(info);
         info.end = i;
     }
 
     /**
      * This method is invoked to process the part of a template immediately
      * following an "@".
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends
      *                             framents to info.parse. Info.end is set
      *                             to the index of the first character following
      *                             the @-specifier (e.g. for {@code @foo+bar}
      *                             info.end will refer to the {@code +} and for
      *                             {@code @abc d} info.end will refer to the "d".
      * @param start                Index of the character immediately after
      *                             the {@code @}.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     protected static void parseAtSign(ParseInfo info, int start) {
         if (start >= info.template.length()) {
             throw new SyntaxError("dangling \"@\" in template \"" +
                     info.template + "\"");
         }
         char c = info.template.charAt(start);
         if (Character.isUnicodeIdentifierStart(c) || Character.isDigit(c)) {
             flushText(info);
             if (Character.isDigit(c)) {
                 info.end = StringUtil.numberEnd(info.template, start);
             } else {
                 info.end = StringUtil.identifierEnd(info.template, start);
             }
             String name = info.template.subSequence(start,
                     info.end).toString();
             if ((info.end < info.template.length())
                     && (info.template.charAt(info.end) == '?'))  {
                 parseChoice(info, name, info.end+1);
             } else {
                 info.parse.addFragment(new IdFragment(name));
             }
         } else if (c == '(') {
             flushText(info);
             parseParenName(info, start+1);
         } else if (c == '@' || c == '{' || c == '}') {
             info.text.append(c);
             info.end = start + 1;
         } else {
             throw new SyntaxError("invalid sequence \"@" + c
                     + "\" in template \"" + info.template + "\"");
         }
     }
 
     /**
      * This method is invoked to parse substitutions that start with
      * {@code @name?}.
      * @param info                 Overall information about the template
      *                             being parsed. This method appends fragments
      *                             to info.parse and sets info.end to the index
      *                             of the first character after the end of
      *                             this substitution.
      * @param name                 The name of the variable (everything
      *                             between the "@" and the "?").
      * @param start                Index in info.template of the character
      *                             just after the "?".
      * @throws SyntaxError         The template contains an illegal construct.
      */
     protected static void parseChoice(ParseInfo info, String name,
             int start) throws SyntaxError {
 
         flushText(info);
         CharSequence template = info.template;
         IdFragment nameFragment = new IdFragment(name);
 
         if ((start >= info.template.length()) || (template.charAt(start) != '{')) {
             throw new SyntaxError("missing \"{\" after \"?\" " +
                     "in template \"" + template + "\"");
         }
 
         ParsedTemplate oldCache = info.parse;
         ParsedTemplate cache1 = new ParsedTemplate();
         info.parse = cache1;
         parseTo(info, start + 1, "|", "}");
         if (info.end >= info.template.length()) {
             throw new SyntaxError("incomplete @...?{...} substitution " +
                     "in template \"" + template + "\"");
         }
 
         if (info.template.charAt(info.end) == '|') {
             ParsedTemplate cache2 = new ParsedTemplate();
             info.parse = cache2;
 
             parseTo(info, info.end + 1, "}");
 
             if (info.end == info.template.length()) {
                 throw new SyntaxError("incomplete @...?{...} substitution " +
                         "in template \"" + template + "\"");
             }
             oldCache.addFragment(new ChoiceFragment(nameFragment, cache1, cache2));
             info.end++;
         } else {
             oldCache.addFragment(new DefaultFragment(nameFragment, cache1));
             info.end++;
         }
 
 
         info.parse = oldCache;
     }
 
     /**
      * This method is invoked to parse parenthesized names, such as
      * {@code @(@first+@second)}.
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends a
      *                             fragment to info.parse. Info.end will
      *                             be set to the index of the character just
      *                             after the closing parenthesis.
      * @param start                Index of the character immediately after
      *                             the "@(".
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     protected static void parseParenName(ParseInfo info, int start)
             throws SyntaxError {
 
 
         ParsedTemplate oldCache = info.parse;
         info.parse = new ParsedTemplate();
 
         parseTo(info, start, ")");
 
         if (info.end >= info.template.length()) {
             throw new SyntaxError("missing \")\" for \"@(\" in template \""
                                   + info.template + "\"");
         }
         oldCache.addFragment(new IdFragment(info.parse));
         info.end++;
         info.parse = oldCache;
     }
 
     /**
      * This method is invoked to parse a portion of a template that
      * lies between double curly braces.
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends a
      *                             fragment to info.parse. Info.end will be set
      *                             to the index of the character just after the
      *                             closing braces.
      * @param start                Index of the character immediately after
      *                             the "{{".
      * @throws SyntaxError         The template is illegal, e.g. it doesn't
      *                             contain closing braces.
      */
     protected static void parseBraces(ParseInfo info, int start)
             throws SyntaxError {
         CharSequence template = info.template;
 
         flushText(info);
         ParsedTemplate oldCache = info.parse;
         info.parse = new ParsedTemplate();
 
         parseTo(info, start, "}}");
 
         if (info.end >= info.template.length()) {
             throw new SyntaxError("unmatched \"{{\" in template \""
                                   + info.template + "\"");
         }
 
         oldCache.addFragment(new ConditionalFragment(info.parse));
         info.end = info.end + 2;
         info.parse = oldCache;
         collapseSpaces(info, start);
     }
 
 
     /**
      * Moves spaces and stuff
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends a
      *                             fragment to info.parse. Info.end will be set
      *                             to the index of the character just after the
      *                             closing braces.
      *
      * @param start                Position of character after last }
      */
     protected static void collapseSpaces(ParseInfo info, int start) {
         char next;
         int end = info.end;
         if (end < info.template.length()) {
             next = info.template.charAt(end);
         } else {
             // pretend the next character is a '>'
             next = '>';
         }
 
         int indexBeforeBraces = start - 3;
         char lastChar = 0;
         if (indexBeforeBraces >= 0) {
             lastChar = info.template.charAt(indexBeforeBraces);
         } else {
             lastChar = '<';
         }
 
         if ((info.lastDeletedSpace < indexBeforeBraces)
             && (lastChar == ' ')
             && ((next == ' ') || (next == ']')
                 || (next == '>') || (next == '}')
                 || (next == ')') || (next == '\"')
                 || (next == '\''))) {
             info.lastDeletedSpace = indexBeforeBraces;
             movePreceedingSpace(info);
         } else {
             // If there is a space after the "}}" and the
             // character before the "{{" is an open-delimiter
             // such as "<" or the beginning of the string, then
             // skip over the trailing space.
             if ((next == ' ') && ((lastChar == '[')
                     || (lastChar == '<') || (lastChar == '{')
                     || (lastChar == '(') || (lastChar == '\"')
                     || (lastChar == '\''))) {
                 info.lastDeletedSpace = info.end;
                 moveTrailingSpace(info);
             }
         }
     }
 
     /**
      * Moves a space preceeding a ConditionalFragment into the Fragment
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends a
      *                             fragment to info.parse. Info.end will be set
      *                             to the index of the character just after the
      *                             closing braces.
      */
     protected static void movePreceedingSpace(ParseInfo info) {
         ArrayList<Fragment> frags = info.parse.fragments;
         ConditionalFragment last = ((ConditionalFragment) frags.get(frags.size()-1));
         Fragment firstInBraces = null;
         if (last.contents.fragments.size() > 0) {
             firstInBraces = last.contents.fragments.get(0);
         }
 
         if (firstInBraces instanceof TextFragment) {
             ((TextFragment) firstInBraces).text = " " + ((TextFragment) firstInBraces).text;
         } else {
             last.contents.fragments.add(0, new TextFragment(" "));
         }
 
         TextFragment withSpace = ((TextFragment) frags.get(frags.size()-2));
         withSpace.text = withSpace.text.substring(0, withSpace.text.length() - 1);
     }
 
     /**
      * Moves a space after a ConditionalFragment into the Fragment
      * @param info                 Contains information about the template
      *                             being parsed.  This method appends a
      *                             fragment to info.parse. Info.end will be set
      *                             to the index of the character just after the
      *                             closing braces.
      */
     protected static void moveTrailingSpace(ParseInfo info) {
         ArrayList<Fragment> frags = info.parse.fragments;
         ConditionalFragment last = ((ConditionalFragment) frags.get(frags.size()-1));
         Fragment lastInBraces = null;
         if (last.contents.fragments.size() > 0) {
             last.contents.fragments.get(last.contents.fragments.size()-1);
         }
 
         if (lastInBraces instanceof TextFragment) {
             ((TextFragment) lastInBraces).text += " ";
         } else {
             last.contents.fragments.add(new TextFragment(" "));
         }
 
         info.end++;
     }
 
     /**
      * Determines whether the current position in a string marks the beginning
      * one of the delimiters.
      *
      * @param template        This string is searched for delimiters
      * @param start           Current position in the string
      * @param delimiters      List of delimiters to check for in {@code str}
      */
     protected static boolean foundDelimiter(CharSequence template, int start,
                                             String ... delimiters) {
         int charsLeft = template.length() - start;
         int i;
         for (String delimiter : delimiters) {
             int len = delimiter.length();
             for (i = 0; i < len && i < charsLeft; i++) {
                 if (template.charAt(start + i) != delimiter.charAt(i)) {
                         break;
                 }
             }
             if (i == len) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Creates and appends a TextFragment to the current list of fragments.
      * @param info            Contains information about the template being
      *                        parsed.
      */
     protected static void flushText(ParseInfo info) {
         if (info.text.length() > 0) {
             info.parse.addFragment(new TextFragment(info.text.toString()));
         }
         info.text.setLength(0);
     }
 
 }

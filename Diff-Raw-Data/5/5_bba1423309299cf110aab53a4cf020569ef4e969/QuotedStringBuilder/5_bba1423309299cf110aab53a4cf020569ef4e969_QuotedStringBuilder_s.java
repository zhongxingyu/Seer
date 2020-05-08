 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.engine;
 import java.util.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 
 
 final class QuotedStringBuilder extends Vector implements Builder
 {
 
    final public Object build(BuildContext bc) throws BuildException 
    {
       StringBuffer str = new StringBuffer(100);
       QuotedString qs = new QuotedString();
 
       Enumeration enum = elements();
 
       while (enum.hasMoreElements()) {
          Object txt = enum.nextElement();
      
          if (txt instanceof Builder) {
             txt = ((Builder) txt).build(bc);
          }
 
          if (txt instanceof String) {
             str.append(txt);
          } else {
             qs.addElement(str.toString());
             qs.addElement(txt);
             str.setLength(0);
          }
       }
       if (str.length() > 0) {
          qs.addElement(str.toString());
       }
 
       if (qs.size() == 1) {
          return qs.elementAt(0);
       } else {
          return qs;
       }
    }
 }
 
 
 /**
   * A quoted string is a vector of strings and macros. When parsing,
   * it begins with a quotation mark and extends until a matching 
   * close quotation mark (single or double quotes accepted, the 
   * close quote must match the open quote). 
   * <p>
   * When parsing, you can use the escape character to protect values
   * that might otherwise be interepreted. The escape character is \
   * <p>
   * It usually contains a series of strings and variables. If you put
   * a Macro into it, the Macro.evaluate()/Macro.write() method will 
   * be used to include its contents. If you include a non-Macro, 
   * its Object.toString() method will be used instead.
   * <p>
   * Unix users should note that there is no difference between double 
   * and single quotes with quoted string. It is more like the quoting 
   * used for HTML attributes. Internal variables are alway evaluated.
   * <p>
   * Examples:<pre>
   *
   *    #include 'this is a quoted string with a $variable in it'
   *    #include "use double quotes and you can put don't in it"
   *    #include "use the escape char to write \$10 in a QuotedString"
   *
   * </pre>Here the text inside the quotes is the QuotedString.
   */
 final class QuotedString extends Vector implements Macro 
 {
 
    /**
      * conditionally include debugging statements in the compiled code
      */
    private static final boolean debug = false;
 
    /**
      * Create a new quoted string
      */
    QuotedString() { }
 
    /**
      * Return the value of the quoted string, after substituting all 
      * contained variables and removing the quotation marks.
      * @exception ContextException is required data is missing
      */
    public Object evaluate(Context data)
       throws ContextException
    {
       Object o;
       StringBuffer str = new StringBuffer(96); 
       for (int i=0; i<elementCount; i++) {
 	 o = elementData[i];
 	 if (! (o instanceof Macro)) {
 	    str.append(o.toString());
 	 } else {    // should only contain Variables and Strings 
             try {
	       str.append(((Variable) o).evaluate(data));
             } catch (ClassCastException e) {
                throw new ContextException(
                     "QuotedString: Expected variable or string, got: " + o);
             }
 	 }
       }
       return str.toString(); // never null, we created it above
    }
 
 
    /**
      * Write the quoted string out. Performs the same operation as 
      * evaluate(context) but writes it to the stream. Although this is 
      * required by the Macro superclass, we don't expect it to be used much
      * since a quoted string does not really appear in a Block (it appears
      * as the argument to a function or directive.)
      * @exception ContextException is required data is missing
      * @exception IOException if could not write to output stream
      */
    final public void write(FastWriter out, Context data) 
 	throws ContextException, IOException
    {
       out.write(evaluate(data).toString()); // evaluate never returns null
    }
 }

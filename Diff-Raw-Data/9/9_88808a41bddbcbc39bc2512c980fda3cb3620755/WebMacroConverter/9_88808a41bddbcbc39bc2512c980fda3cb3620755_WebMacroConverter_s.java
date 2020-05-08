 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2006 Tim Pizey
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Tim Pizey <timp At paneris.org>
  */
 package org.melati.template.velocity;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.oro.text.perl.Perl5Util;
 import org.melati.util.MelatiBugMelatiException;
 
 /**
  * Converter from WebMacro templates to Velocity templates.
  * 
  * Note that this does not succeed for modern WebMacro syntax 
  * which uses optional #begin in #foreach.
  * 
  * @author Tim Pizey based on work by Jason vanZyl and Tim Joyce.
  *
  */
 public final class WebMacroConverter {
 
   private WebMacroConverter() {}
   /**
    * The regexes to use for substitution. The regexes come
    * in pairs. The first is the string to match, the
    * second is the substitution to make.
    */
   public static final String[] regExps = {
         // Make #if directive match the Velocity directive style.
         "#if\\s*[(]\\s*(.*\\S)\\s*[)]\\s*(#begin|{)[ \\t]?",
         "#if( $1 )",
 
         // Remove the WM #end #else #begin usage.
         "[ \\t]?(#end|})\\s*#else\\s*(#begin|{)[ \\t]?(\\w)",
         "#else#**#$3", // avoid touching a followup word with embedded comment
         "[ \\t]?(#end|})\\s*#else\\s*(#begin|{)[ \\t]?",
         "#else",
 
         // Convert nulls.
         // This converts
         // $var != null
         // to
         // $var
         "\\s*(\\$[^\\s\\!]+)\\s*!=\\s*null\\s*",
         " $1 ",
         // This converts
         // $var == null
         // to
         // !$var
         "\\s*(\\$[^\\s\\!]+)\\s*==\\s*null\\s*",
         " !$1 ",
 
         // Convert WM style #foreach to Velocity directive style.
         "#foreach\\s+(\\$\\w+)\\s+in\\s+(\\$[^\\s#]+)\\s*(#begin|{)[ \\t]?",
         "#foreach( $1 in $2 )",
 
         // Change the "}" to #end. Have to get more
         // sophisticated here. Will assume either {}
         // and no javascript, or #begin/#end with the
         // possibility of javascript.
         "\n}", // assumes that javascript is indented, WMs not!!!
         "\n#end",
 
         // Convert WM style #set to Velocity directive style.
         //"#set\\s+(\\$[^\\s=]+)\\s*=\\s*(.*\\S)[ \\t]*",
         "#set\\s+(\\$[^\\s=]+)\\s*=\\s*([\\S \\t]+)",
         "#set( $1 = $2 )",
         "(##[# \\t\\w]*)\\)", // fix comments included at end of line
         ")$1",
 
         // Convert WM style #parse to Velocity directive style.
         "#parse\\s+([^\\s#]+)[ \\t]?",
         "#parse( $1 )",
 
        // parse is now depricated for web macro
         // include as template is recommended.
         // Velocity supports only parse
         // added for melati conversion
         "#include\\s+as\\s+template\\s+([^\\s#]+)[ \\t]?",
         "#parse( $1 )",
 
         // Convert WM style #include to Velocity directive style.
         "#include\\s+([^\\s#]+)[ \\t]?",
         "#include( $1 )",
 
         // Convert WM formal reference to VL syntax.
         "\\$\\(([^\\)]+)\\)",
        "${$1}",
        "\\${([^}\\(]+)\\(([^}]+)}\\)", // fix encapsulated brakets: {(})
        "${$1($2)}",
 
         // Velocity currently does not permit leading underscore.
         "\\$_",
         "$l_",
         "\\${(_[^}]+)}", // within a formal reference
         "${l$1}",
 
   };
 
   /**
    * Do the conversion.
    * 
    * @param in
    * @return the InputStream with substitutions applied
    */
   public static InputStream convert(InputStream in) {
     byte[] ca;
     try {
       ca = new byte[in.available()];
       in.read(ca);
       String contents = new String(ca);
       /* Regular expression tool */
       final Perl5Util perl = new Perl5Util();
       for (int i = 0; i < regExps.length; i += 2) {
         while (perl.match("/" + regExps[i] + "/", contents)) {
           contents = perl.substitute(
               "s/" + regExps[i] + "/" + regExps[i+1] + "/", contents);
         }
       }
       //System.err.println(contents);
       return new ByteArrayInputStream(contents.getBytes());
     } catch (IOException e) {
       throw new MelatiBugMelatiException(
               "Problem loading WebMacro template as a VelocityTemplate", e);
     }
   }
 }

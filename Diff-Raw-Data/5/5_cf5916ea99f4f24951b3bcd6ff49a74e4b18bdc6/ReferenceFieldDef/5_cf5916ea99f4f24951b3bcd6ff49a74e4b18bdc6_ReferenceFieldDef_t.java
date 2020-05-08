 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
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
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem.prepro;
 
 import java.util.*;
 import java.io.*;
 import org.melati.poem.StandardIntegrityFix;
 
 public class ReferenceFieldDef extends FieldDef {
 
   StandardIntegrityFix integrityfix;
 
   public ReferenceFieldDef(TableDef table, String name, int displayOrder,
                            String type, Vector qualifiers)
       throws IllegalityException {
     super(table, name, type, "Integer", displayOrder, qualifiers);
   }
 
   protected void generateColRawAccessors(Writer w) throws IOException {
     super.generateColRawAccessors(w);
 
     w.write(
       "\n" +
       "          public Object getRaw(Persistent g)\n" +
       "              throws AccessPoemException {\n" +
       "            return ((" + mainClass + ")g).get" + suffix + "Troid();\n" +
       "          }\n" +
       "\n" +
       "          public void setRaw(Persistent g, Object raw)\n" +
       "              throws AccessPoemException {\n" +
       "            ((" + mainClass + ")g).set" + suffix + "Troid((" +
                        rawType + ")raw);\n" +
       "          }\n");
 
     if (integrityfix != null)
       w.write(
         "\n" +
         "          public StandardIntegrityFix defaultIntegrityFix() {\n" +
         "            return StandardIntegrityFix." + integrityfix.name + ";\n" +
         "          }\n");
   }
 
   private String targetCast() {
     TableDef targetTable = (TableDef)table.dsd.tableOfClass.get(type);
     return targetTable == null || targetTable.superclass == null ?
              "" : "(" + type + ")";
   }
 
   public void generateBaseMethods(Writer w) throws IOException {
     super.generateBaseMethods(w);
 
     // FIXME the definition of these is duplicated from TableDef
     String targetTableAccessorMethod = "get" + type + "Table";
     String targetSuffix = type;
 
     String db = "get" + table.dsd.databaseClass + "()";
 
     w.write("\n" +
 	    "  public Integer get" + suffix + "Troid()\n" +
             "      throws AccessPoemException {\n" +
 	    "    readLock();\n" +
             "    return get" + suffix + "_unsafe();\n" +
             "  }\n" +
             "\n" +
             "  public void set" + suffix + "Troid(Integer raw)\n" +
             "      throws AccessPoemException {\n" +
             "    set" + suffix + "(" +
                     "raw == null ? null : \n" +
	                 // This cast is necessary when the target table is
	                 // an "extends"
	    "        " + targetCast() +
                        db + "." + targetTableAccessorMethod + "()." +
                        "get" + targetSuffix + "Object(raw));\n" +
             "  }\n" +
             "\n" +
             "  public " + type + " get" + suffix + "()\n" +
             "      throws AccessPoemException, NoSuchRowPoemException {\n" +
             "    Integer troid = get" + suffix + "Troid();\n" +
             "    return troid == null ? null :\n" +
 	                 // This cast is necessary when the target table is
 	                 // an "extends"
 	    "        " + targetCast() +
                          db + "." +
                          targetTableAccessorMethod + "()." +
                          "get" + targetSuffix + "Object(troid);\n" +
             "  }\n" +
             "\n" +
             "  public void set" + suffix + "(" + type + " cooked)\n" +
             "      throws AccessPoemException {\n" +
             "    _" + tableAccessorMethod + "().get" + suffix + "Column()." +
                     "getType().assertValidCooked(cooked);\n" +
 	    "    writeLock();\n" +
             "    if (cooked == null)\n" +
             "      set" + suffix + "_unsafe(null);\n" +
             "    else {\n" +
             "      cooked.existenceLock();\n" +
             "      set" + suffix + "_unsafe(cooked.troid());\n" +
             "    }\n" +
             "  }\n");
   }
 
   public void generateJavaDeclaration(Writer w) throws IOException {
     w.write("Integer " + name);
   }
 
   public String poemTypeJava() {
     // FIXME the definition of these is duplicated from TableDef
     String targetTableAccessorMethod = "get" + type + "Table";
     return
         "new ReferencePoemType(((" + table.dsd.databaseClass + ")getDatabase())." +
         targetTableAccessorMethod + "(), " + isNullable + ")";
   }
 }

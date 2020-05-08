 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense. 
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.poem.prepro;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 
 public class TableDef {
 
   DSD dsd;
   final String name;
   String superclass = null;
   final String suffix;
   String displayName;
   String description;
   String category;
   int displayOrder;
   boolean seqCached;
   int cacheSize = CacheSizeTableQualifier.DEFAULT;
   final String baseClass;
   final String mainClass;
   final String tableBaseClass;
   final String tableMainClass;
   final String tableAccessorMethod;
   private Vector data = new Vector();
   boolean isAbstract;
   boolean hidesSuperclass = false;
   boolean overridesPoemTable = false;
 
   int nextFieldDisplayOrder = 0;
 
   public TableDef(DSD dsd, StreamTokenizer tokens, int displayOrder,
 		  boolean isAbstract)
       throws ParsingDSDException, IOException, IllegalityException {
     this.dsd = dsd;
     this.displayOrder = displayOrder;
     this.isAbstract = isAbstract;
     if (tokens.ttype != StreamTokenizer.TT_WORD)
       throw new ParsingDSDException("<table name>", tokens);
     suffix = tokens.sval;
     name = suffix.toLowerCase();
     baseClass = suffix + "Base";
     mainClass = suffix;
     tableBaseClass = suffix + "TableBase";
     tableMainClass = suffix + "Table";
     tableAccessorMethod = "get" + tableMainClass;
 
     if (tokens.nextToken() == StreamTokenizer.TT_WORD) {
       if (!tokens.sval.equals("extends"))
         throw new ParsingDSDException("{", tokens);
       tokens.wordChars('.', '.');
       try {
         if (tokens.nextToken() != StreamTokenizer.TT_WORD)
           throw new ParsingDSDException("<class name>", tokens);
       }
       finally {
         tokens.ordinaryChar('.');
       }
       superclass = tokens.sval;
 
       hidesSuperclass =
 	  superclass.substring(superclass.lastIndexOf('.') + 1).
               equals(mainClass);
 
       overridesPoemTable = superclass.startsWith("org.melati.poem.");
     }
     else
       tokens.pushBack();
 
     while (tokens.nextToken() == '(') {
       tokens.nextToken();
       TableQualifier.from(tokens).apply(this);
       DSD.expect(tokens, ')');
     }
 
     DSD.expect(tokens, '{');
     while (tokens.nextToken() != '}')
       data.addElement(FieldDef.from(this, tokens, nextFieldDisplayOrder++));
     tokens.nextToken();
   }
 
   public boolean isPoemOverride() {
     return overridesPoemTable;
   }
 
   private final TableDef this_ = this;
 
   public void generateTableDeclJava(Writer w) throws IOException {
     if (!isAbstract)
       w.write("  private " + tableMainClass + " tab_" + name + " = null;\n");
   }
 
   public void generateTableDefnJava(Writer w) throws IOException {
     if (!isAbstract)
       w.write("    redefineTable(tab_" + name + " = " +
 	               "new " + tableMainClass + "(this, \"" + name + "\", " +
                                                    "DefinitionSource.dsd));\n");
   }
 
   public void generateTableAccessorJava(Writer w) throws IOException {
     // FIXME hack
     if (!isAbstract)
       w.write("  public " + (hidesSuperclass ? superclass + "Table" :
 			                       tableMainClass) +
 		  " get" + tableMainClass + "() {\n" +
 	      "    return tab_" + name + ";\n" +
 	      "  }\n");
   }
 
   public void generateBaseJava(Writer w) throws IOException {
     w.write("public abstract class " + baseClass + " extends " +
                 (superclass == null ? "Persistent" : superclass) + " {\n" +
             "\n" +
 	    "  public " + dsd.databaseClass + " get" + dsd.databaseClass +
                    "() {\n" +
 	    "    return (" + dsd.databaseClass + ")getDatabase();\n" +
             "  }\n" +
 	    "\n");
 
     // FIXME hack
 
     String tableRetClass =
         hidesSuperclass ? superclass + "Table" : tableMainClass;
 
     w.write("  public " + tableRetClass + " " + tableAccessorMethod +
                    "() {\n" +
             "    return (" + tableRetClass + ")getTable();\n" +
             "  }\n\n");
 
     w.write("  private " + tableMainClass + " _" + tableAccessorMethod +
                    "() {\n" +
             "    return (" + tableMainClass + ")getTable();\n" +
             "  }\n\n");
 
     for (Enumeration f = data.elements(); f.hasMoreElements();) {
       w.write("  protected ");
       ((FieldDef)f.nextElement()).generateJavaDeclaration(w);
       w.write(";\n");
     }
 
     for (Enumeration f = data.elements(); f.hasMoreElements();) {
       FieldDef field = (FieldDef)f.nextElement();
       w.write('\n');
       field.generateBaseMethods(w);
       w.write('\n');
       field.generateFieldCreator(w);
     }
 
     w.write("}\n");
   }
 
   public void generateMainJava(Writer w) throws IOException { 
     w.write("public class " + mainClass + " extends " + baseClass + " {\n" +
 	    "  public " + mainClass + "() {}\n" +
 	    "\n" +
             "  // programmer's domain-specific code here\n" +
             "}\n");
   }
 
   public void generateTableBaseJava(Writer w) throws IOException {
     w.write("public class " + tableBaseClass + " extends " +
                 (superclass == null ? "" : superclass) + "Table {\n" +
             "\n");
 
     for (Enumeration f = data.elements(); f.hasMoreElements();) {
       w.write("  private ");
       ((FieldDef)f.nextElement()).generateColDecl(w);
       w.write(" = null;\n");
     }
 
     w.write("\n" +
             "  public " + tableBaseClass + "(\n" +
 	    "      Database database, String name,\n" +
 	    "      DefinitionSource definitionSource)" +
                    " throws PoemException {\n" +
             "    super(database, name, definitionSource);\n" +
             "  }\n" +
             "\n" +
             "  public " + tableBaseClass + "(\n" +
 	    "      Database database, String name)" +
                    " throws PoemException {\n" +
             "    this(database, name, DefinitionSource.dsd);\n" +
             "  }\n" +
             "\n" +
 	    "  public " + dsd.databaseClass + " get" + dsd.databaseClass +
                    "() {\n" +
 	    "    return (" + dsd.databaseClass + ")getDatabase();" +
 	    "  }\n" +
 	    "\n" +
             "  protected void init() throws PoemException {\n" +
 	    "    super.init();\n");
 
     for (Enumeration f = data.elements(); f.hasMoreElements();) {
       ((FieldDef)f.nextElement()).generateColDefinition(w);
       if (f.hasMoreElements()) w.write('\n');
     }
 
     w.write("  }\n" +
             "\n");
 
     for (Enumeration f = data.elements(); f.hasMoreElements();) {
       ((FieldDef)f.nextElement()).generateColAccessor(w);
       w.write('\n');
     }
 
     String retMainClass = hidesSuperclass ? superclass : mainClass;
 
     w.write("  public " + retMainClass + " get" + mainClass + "Object(" +
                   "Integer troid) {\n" +
             "    return (" + retMainClass + ")getObject(troid);\n" +
             "  }\n" +
             "\n" +
             "  public " + retMainClass + " get" + mainClass + "Object(" +
                   "int troid) {\n" +
             "    return (" + retMainClass + ")getObject(troid);\n" +
             "  }\n");
 
     if (!isAbstract)
       w.write("\n" +
               "  protected Persistent _newPersistent() {\n" +
               "    return new " + mainClass + "();\n" +
               "  }" +
               "\n");
 
     if (displayName != null)
       w.write("  protected String defaultDisplayName() {\n" +
               "    return " + StringUtils.quoted(displayName, '"') + ";\n" +
               "  }\n" +
               "\n");
 
     if (description != null)
       w.write("  protected String defaultDescription() {\n" +
               "    return " + StringUtils.quoted(description, '"') + ";\n" +
               "  }\n" +
               "\n");
 
     if (seqCached)
       w.write("  protected boolean defaultRememberAllTroids() {\n" +
               "    return true;\n" +
               "  }\n" +
               "\n");
 
     if (cacheSize != CacheSizeTableQualifier.DEFAULT)
       w.write("  protected Integer defaultCacheLimit() {\n" +
              "    return new Integer(" +
                        (cacheSize == CacheSizeTableQualifier.UNLIMITED ?
                            "999999999" :  "" + cacheSize) +
                       ");\n" +
               "  }\n" +
               "\n");
 
     if (category != null)
       w.write("  protected String defaultCategory() {\n" +
               "    return " + StringUtils.quoted(category, '"') + ";\n" +
               "  }\n" +
               "\n");
 
     w.write("  protected int defaultDisplayOrder() {\n" +
             "    return " + displayOrder + ";\n" +
             "  }\n");
 
     w.write("}\n");
   }
 
   public void generateTableMainJava(Writer w) throws IOException { 
     w.write("public class " + tableMainClass +
                " extends " + tableBaseClass + " {\n" +
             "\n" +
             "  public " + tableMainClass + "(\n" +
 	    "      Database database, String name,\n" +
 	    "      DefinitionSource definitionSource)" +
                      " throws PoemException {\n" +
             "    super(database, name, definitionSource);\n" +
             "  }\n" +
             "\n" +
             "  // programmer's domain-specific code here\n" +
             "}\n");
   }
 
   public void generateJava() throws IOException {
 
     dsd.createJava(baseClass,
                    new Generator() {
                      public void process(Writer w) throws IOException {
                        this_.generateBaseJava(w);
                      }
                    });
 
     dsd.createJava(mainClass,
                    new Generator() {
                      public void process(Writer w) throws IOException {
                        this_.generateMainJava(w);
                      }
                    },
                    false);
 
     dsd.createJava(tableBaseClass,
                    new Generator() {
                      public void process(Writer w) throws IOException {
                        this_.generateTableBaseJava(w);
                      }
                    });
 
     dsd.createJava(tableMainClass,
                    new Generator() {
                      public void process(Writer w) throws IOException {
                        this_.generateTableMainJava(w);
                      }
                    },
                    false);
   }
 }

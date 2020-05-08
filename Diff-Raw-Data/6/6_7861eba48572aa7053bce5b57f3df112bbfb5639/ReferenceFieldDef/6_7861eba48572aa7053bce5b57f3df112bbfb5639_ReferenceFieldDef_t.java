 package org.melati.poem.prepro;
 
 import java.util.*;
 import java.io.*;
 
 public class ReferenceFieldDef extends FieldDef {
 
   public ReferenceFieldDef(TableDef table, String name, int displayOrder,
                            String type, Vector qualifiers)
       throws IllegalityException {
     super(table, name, type, "Integer", displayOrder, qualifiers);
   }
 
   protected void generateColIdentAccessors(Writer w) throws IOException {
     w.write(
       "          public Object getIdent(Persistent g)\n" +
       "              throws AccessPoemException {\n" +
       "            return ((" + mainClass + ")g).get" + suffix + "Troid();\n" +
       "          }\n" +
       "\n" +
       "          public void setIdent(Persistent g, Object ident)\n" +
       "              throws AccessPoemException {\n" +
       "            ((" + mainClass + ")g).set" + suffix + "Troid((" +
                        identType + ")ident);\n" +
       "          }\n");
   }
 
   public void generateBaseMethods(Writer w) throws IOException {
     // FIXME the definition of these is duplicated from TableDef
     String targetTableAccessorMethod = "get" + type + "Table";
     String targetSuffix = type;
 
     w.write("  public Integer get" + suffix + "Troid()\n" +
             "      throws AccessPoemException {\n" +
             "    return dataForReading()." + name + ";\n" +
             "  }\n" +
             "\n" +
             "  public void set" + suffix + "Troid(Integer ident)\n" +
             "      throws AccessPoemException {\n" +
             "    dataForWriting()." + name + " = ident;\n" +
             "  }\n" +
             "\n" +
             "  public " + type + " get" + suffix + "()\n" +
             "      throws AccessPoemException, NoSuchRowPoemException {\n" +
             "    Integer troid = get" + suffix + "Troid();\n" +
             "    return troid == null ? null :\n" +
            "        ((" + table.dsd.databaseClass + ")getDatabase())." +
                         targetTableAccessorMethod + "()." +
                          "get" + targetSuffix + "Object(troid);\n" +
             "  }\n" +
             "\n" +
             "  public void set" + suffix + "(" + type + " value)\n" +
             "      throws AccessPoemException {\n" +
             "    set" + suffix + "Troid(value == null ? null : value.troid());\n" +
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

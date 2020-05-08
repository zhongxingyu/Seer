 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 public class Main extends MouseAdapter {
     private static final JTextField txtClassname = new JTextField("Class name", 41);
     private static final JTextArea txtVariables = new JTextArea("Fields", 10, 40);
     private static final JCheckBox chkInherit = new JCheckBox("Inherit from Base class", true);
     private static final JCheckBox chkXml = new JCheckBox("Support XML creation/saving", true);
     private static final JCheckBox chkGetters = new JCheckBox("Create getters for fields", true);
     private static final JCheckBox chkSetters = new JCheckBox("Create setters for fields", true);
     
     
     public static void main(String... asdf) {
         JPanel panel;
         JFrame frame = new JFrame("Android EasyParcelable Boilerplate Code writer");
         frame.setSize(500, 500);
         frame.getContentPane().add(panel = new JPanel());
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         Main main = new Main();
         
         panel.add(txtClassname);
         
         
         JScrollPane scroll = new JScrollPane(txtVariables);
         scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         panel.add(scroll);
         
         JPanel checkPanel = new JPanel();
         checkPanel.setSize(250, 250);
         checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.PAGE_AXIS));
         chkInherit.setEnabled(false);
         checkPanel.add(chkInherit);
         checkPanel.add(Box.createVerticalGlue());
         checkPanel.add(chkXml);
         checkPanel.add(Box.createVerticalGlue());
         checkPanel.add(chkGetters);
         checkPanel.add(Box.createVerticalGlue());
         checkPanel.add(chkSetters);
         
         panel.add(checkPanel);
         panel.add(Box.createVerticalGlue());
         
         JButton but;
         panel.add(but = new JButton("Done"));
         but.addMouseListener(main);
         
         
         frame.setVisible(true);
     }
     
     private static final String INIT_SERIALIZE = "public void writeToSerializer(Serializer serializer) throws IOException {\nsuper.writeToSerializer(serializer);\n";
     private static final String INIT_DESERIALIZE = "public =1=(Deserializer deserializer) throws IOException {\nsuper(deserializer); \n";
     
     private static final String INIT_CREATOR = "public static final EasySerializableBase.CreatorBase<=1=> CREATOR = new EasySerializableBase.CreatorBase<=1=>() {\n"
                                                + "public =1=[] newArray(int size) {\n"
                                                + "    return new =1=[size];\n"
                                                + "}\npublic =1= createFromDeserializer(Deserializer deserializer) throws IOException {\n"
                                                + "    return new =1=(deserializer);\n}\n};\n";
     
     private static final String INIT_CREATOR_XML = "public static final XmlRecreatableBase.CreatorBase<=1=> CREATOR = new XmlRecreatableBase.CreatorBase<=1=>() {\n"
                                                    + "public =1=[] newArray(int size) {\n"
                                                    + "    return new =1=[size];\n"
                                                    + "}\npublic =1= createFromDeserializer(Deserializer deserializer) throws IOException {\n"
                                                    + "    return new =1=(deserializer);\n"
                                                    + "}\npublic =1= createFromParser(XmlPullParser parser) throws XmlPullParserException, IOException {\n"
                                                    + "    return new =1=(parser);\n" + "}\n};\n";
     
     private static final String CONSTRUCTOR_XML = "protected =1=(XmlPullParser parser) throws XmlPullParserException, IOException {\nsuper(parser);\n}\n";
     
     private static final String XML_ATTR_PRE = "protected void setAttribute(int hash, String value) {\nswitch(hash) {\n";
     private static final String XML_ATTR_POST = "default:\nsuper.setAttribute(hash, value);\nbreak;\n}\n}\n";
     
     private static final String XML_OBJS_PRE = "protected void setObject(int hash, XmlPullParser parser) {\nswitch(hash) {\n";
     private static final String XML_OBJS_POST = "default:\nsuper.setObject(hash, parser);\nbreak;\n}\n}\n";
     
     private static final String XML_GET_ATTR = "protected void getAttributes(HashMap<String, String> map) {\nsuper.getAttributes(map);\nString temp;\n";
     private static final String XML_GET_OBJS = "protected void getObjects(HashMap<String, XmlRecreatable> map) {\nsuper.getObjects(map);\n";
     private static final String XML_GET_OBJARRS = "protected void getObjectArrays(HashMap<String, XmlRecreatable[]> map) {\nsuper.getObjectArrays(map);\n";
     
     
     @Override
     public void mouseClicked(MouseEvent e) {
         String classname = txtClassname.getText();
         HashMap<Integer, Variable> vars = parseVars(txtVariables.getText().split("\n"), classname);
         
         boolean doGetters = chkGetters.isSelected();
         boolean doSetters = chkSetters.isSelected();
         boolean doXml = chkXml.isSelected();
         
         StringBuilder getters = new StringBuilder();
         StringBuilder setters = new StringBuilder();
         StringBuilder serialize = new StringBuilder(INIT_SERIALIZE);
         StringBuilder deserialize = new StringBuilder(INIT_DESERIALIZE.replace("=1=", classname));
         StringBuilder xmlHashes = new StringBuilder();
         StringBuilder xmlAttr = new StringBuilder(XML_ATTR_PRE);
         StringBuilder xmlObjs = new StringBuilder(XML_OBJS_PRE);
         StringBuilder xmlGetAttr = new StringBuilder(XML_GET_ATTR);
         StringBuilder xmlGetObjs = new StringBuilder(XML_GET_OBJS);
         StringBuilder xmlGetObjArrs = new StringBuilder(XML_GET_OBJARRS);
         
         
         for (Entry<Integer, Variable> ent : vars.entrySet()) {
             Variable var = ent.getValue();
             
             
             writeSerialize(serialize, var);
             writeDeserialize(deserialize, var);
             
             if (doGetters) {
                 writeGetter(getters, var);
             }
             if (doSetters) {
                 writeSetter(setters, var);
             }
             
             if (doXml) {
                 xmlHashes.append("public static final int HASH_").append(var.getIdentifier())
                         .append('=').append(ent.getKey()).append(";\n");
                 writeXmlSetter(xmlAttr, xmlObjs, var);
                 writeXmlGetter(xmlGetAttr, xmlGetObjs, xmlGetObjArrs, var, classname);
             }
         }
         
         serialize.append('}');
         deserialize.append('}');
         xmlGetAttr.append('}');
         xmlGetObjs.append('}');
         xmlGetObjArrs.append('}');
         xmlAttr.append(XML_ATTR_POST);
         xmlObjs.append(XML_OBJS_POST);
         
         JFileChooser save = new JFileChooser();
         if (JFileChooser.APPROVE_OPTION != save.showSaveDialog(null)) {
             return;
         }
         
         try (BufferedWriter str = new BufferedWriter(new FileWriter(save.getSelectedFile()))) {
             str.write("//----------------------------------------\n"
                       + "//Begin automatically generated code\n"
                       + "//----------------------------------------\n");
             
             str.write(serialize.toString());
             str.newLine();
             str.write(deserialize.toString());
             str.newLine();
             
             if (doGetters) {
                 str.write(getters.toString());
                 str.newLine();
             }
             if (doSetters) {
                 str.write(setters.toString());
                 str.newLine();
             }
             
             if (doXml) {
                 str.write(xmlGetAttr.toString());
                 str.newLine();
                 str.write(xmlGetObjs.toString());
                 str.newLine();
                 str.write(xmlGetObjArrs.toString());
                 str.newLine();
                 str.write(xmlHashes.toString());
                 str.newLine();
                 str.write(xmlAttr.toString());
                 str.newLine();
                 str.write(xmlObjs.toString());
                 str.newLine();
                 
                 str.write(CONSTRUCTOR_XML.replace("=1=", classname));
                 str.write(INIT_CREATOR_XML.replace("=1=", classname));
             } else {
                 str.write(INIT_CREATOR.replace("=1=", classname));
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
     
     private static void writeXmlGetter(StringBuilder ifAttr, StringBuilder ifObj,
             StringBuilder ifObjArr, Variable var, String classname) {
         String identifier = var.getIdentifier();
         if (var.isPrimitive() || var.isString()) {
             ifAttr.append("map.put(\"").append(var.getIdentifier());
             
             if (var.isArray()) {
                 ifAttr.append("\", (temp=Arrays.toString(").append(identifier)
                         .append(")).substring(1, temp.length()-1));\n");
             } else {
                 ifAttr.append("\", new StringBuilder().append(").append(identifier)
                         .append(").toString());\n");
             }
         } else {
             (var.isArray() ? ifObjArr : ifObj).append("map.put(\"=1=.=2=\", =2=);\n".replace("=2=",
                     var.getIdentifier()).replace("=1=", classname));
         }
     }
     
     private static void writeXmlSetter(StringBuilder ifAttr, StringBuilder ifObj, Variable var) {
         String identifier = var.getIdentifier();
         if (var.isPrimitive() || var.isString()) {
             String type = var.getCapitalStrippedType();
             ifAttr.append("case HASH_").append(identifier).append(":\n").append(identifier);
             
             if (var.isArray()) {
                 ifAttr.append("=StaticDataReader.read").append(type)
                         .append("Array(value);\nbreak;\n");
             } else {
                 if (type.equals("Int")) {
                     ifAttr.append("=Integer.parseInt(value);\n");
                 } else if (type.equals("Char")) {
                     ifAttr.append("=value.charAt(0);\n");
                 } else if (type.equals("String")) {
                     ifAttr.append("=value;\n");
                 } else {
                     ifAttr.append('=').append(type).append(".parse").append(type)
                             .append("(value);\n");
                 }
                 ifAttr.append("break;\n");
             }
         } else {
             ifObj.append("case HASH_").append(identifier).append(":\n");
             
             String thingy;
             if (var.isArray()) {
                 thingy = "{ArrayList<=1=> l = StaticDataReader.readXmlList(parser, \"=1=\", =1=.CREATOR);\n"
                          + "=2= = l == null ? null : l.toArray(new =1=[l.size()]);\nbreak;\n}\n";
             } else {
                 thingy = "=2= = StaticDataReader.readElementFromXml(parser, \"=1=\", =1=.CREATOR);\nbreak;\n\n";
             }
             
             ifObj.append(thingy.replace("=1=", var.getActualTypeNoArray()).replace("=2=",
                     identifier));
         }
     }
     private static void writeSerialize(StringBuilder b, Variable var) {
         b.append("serializer.write").append(var.getCapitalStrippedType());
         
         if (var.isArray()) {
             b.append("Array");
         }
         
         b.append('(').append(var.getIdentifier()).append(");\n");
     }
     
     private static void writeDeserialize(StringBuilder b, Variable var) {
         b.append(var.getIdentifier()).append(" = deserializer.read")
                 .append(var.getCapitalStrippedType());
         
         if (var.isArray()) {
             b.append("Array");
         }
         
         if (var.isPrimitive() || var.isString()) {
             b.append("();\n");
         } else {
             b.append('(').append(var.getActualTypeNoArray()).append(".CREATOR);\n");
         }
     }
     
     private static void writeGetter(StringBuilder b, Variable var) {
         b.append("public ").append(var.getActualType()).append(" get")
                 .append(var.getCapitalIdentifier()).append("() {\nreturn ")
                 .append(var.getIdentifier()).append(";\n}\n");
     }
     
     private static void writeSetter(StringBuilder b, Variable var) {
         b.append("public void set").append(var.getCapitalIdentifier()).append('(')
                 .append(var.getActualType()).append(" val) {\n").append(var.getIdentifier())
                 .append(" = val;\n}\n");
     }
     
     private static HashMap<Integer, Variable> parseVars(String[] in, String classname) {
         HashMap<Integer, Variable> ret = new HashMap<Integer, Variable>();
         
         int len = in.length;
         for (int i = 0; i < len; i++) {
             String[] split = in[i].split(" ");
             
             int cur = getNextImportant(split, -1);
             if (cur == -1) {
                 continue;
             }
             String type = split[cur];
             cur = getNextImportant(split, cur);
             if (cur == -1) {
                 continue;
             }
             String identifier = split[cur];
             
             if (identifier.endsWith(";")) {
                 identifier = identifier.substring(0, identifier.length() - 1);
             }
             
             Variable var = new Variable(classname, type, identifier);
             
             Variable old = ret.put(var.getIdentifierHash(), var);
             if (old != null) {
                 JOptionPane
                         .showMessageDialog(
                                 null,
                                 "The variables \""
                                         + var.i
                                         + "\" and \""
                                         + old.i
                                         + "\" conflict in their name hashes.\nIf you plan on doing XML recreation of objects you will need to change the name of\neither of these variables.");
                 while ((old = ret.put(old.getIdentifierHash() + 1, old)) != null) {
                     ;
                 }
             }
         }
         
         return ret;
     }
     private static int getNextImportant(String[] arr, int prev) {
         do {
             prev++;
             if (prev >= arr.length) {
                 return -1;
             }
        } while (equalsAny(arr[prev].trim(), "public", "private", "protected", "final", ""));
         return prev;
     }
     
     static boolean equalsAny(String test, String... things) {
         for (String s : things) {
             if (test.equals(s)) {
                 return true;
             }
         }
         return false;
     }
     
     final static class Variable {
         private final String t, i;
         private final String c;
         
         private final String stripped;
         
         public Variable(String classname, String type, String identifier) {
             t = type;
             i = identifier;
             c = classname;
             stripped = getStrippedType(type);
         }
         
         public String getCapitalIdentifier() {
            int start = i.charAt(0) == '_' && i.length() > 1 ? 1 : 0;
            return i.substring(start, start+1).toUpperCase().concat(i.substring(start+1));
         }
         
         public String getIdentifier() {
             return i;
         }
         
         /**
          * @return the actual type of this variable
          */
         public String getActualType() {
             return t;
         }
         
         /**
          * 
          * @return the actual type of this variable, less the "[]" if it is an array.
          */
         public String getActualTypeNoArray() {
             return isArray() ? t.substring(0, t.length() - 2) : t;
         }
         
         public boolean isArray() {
             return t.endsWith("[]");
         }
         
         public boolean isPrimitive() {
             return isPrimitive(getStrippedType());
         }
         
         private static boolean isPrimitive(String val) {
             return equalsAny(val, "int", "boolean", "char", "short", "long", "float", "double",
                     "byte");
         }
         
         private static boolean isString(String val) {
             return val.equals("String");
         }
         
         public boolean isString() {
             return isString(getStrippedType());
         }
         
         /**
          * 
          * @return If the variable is a primitive or String the identifier is returned, else the
          *         value is returned in the following format: <code>[Classname].[Identifier]</code>
          */
         public String getXmlName() {
             if (isPrimitive() || isString()) {
                 return i;
             } else {
                 return (c + "." + i);
             }
         }
         
         public int getIdentifierHash() {
             return getXmlName().hashCode();
         }
         
         /**
          * @return the stripped type of the variable, doing the following:
          *         <ul>
          *         <li>If the variable is an array: removes the '[]' from the type</li>
          *         <li>If the variable is a primitive: the primitive name</li>
          *         <li>If the variable is a string: returns String</li>
          *         <li>If the variable is any other object: returns Object</li>
          *         </ul>
          */
         public String getStrippedType() {
             return stripped;
         }
         
         private static String getStrippedType(String ret) {
             if (ret.endsWith("[]")) {
                 ret = ret.substring(0, ret.length() - 2);
             }
             if (isString(ret) || isPrimitive(ret)) {
                 return ret;
             }
             
             return "Object";
         }
         
         /**
          * 
          * @return {@link #getStrippedTyped()} with the first letter of the type capitalized.
          */
         public String getCapitalStrippedType() {
             String s = getStrippedType();
             return s.substring(0, 1).toUpperCase().concat(s.substring(1));
         }
     }
 }

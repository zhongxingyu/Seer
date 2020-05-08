 /*
  *  Java HTML Tidy - JTidy
  *  HTML parser and pretty printer
  *
  *  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
  *  Institute of Technology, Institut National de Recherche en
  *  Informatique et en Automatique, Keio University). All Rights
  *  Reserved.
  *
  *  Contributing Author(s):
  *
  *     Dave Raggett <dsr@w3.org>
  *     Andy Quick <ac.quick@sympatico.ca> (translation to Java)
  *     Gary L Peskin <garyp@firstech.com> (Java development)
  *     Sami Lempinen <sami@lempinen.net> (release management)
  *     Fabrizio Giustina <fgiust at users.sourceforge.net>
  *
  *  The contributing author(s) would like to thank all those who
  *  helped with testing, bug fixes, and patience.  This wouldn't
  *  have been possible without all of you.
  *
  *  COPYRIGHT NOTICE:
  *
  *  This software and documentation is provided "as is," and
  *  the copyright holders and contributing author(s) make no
  *  representations or warranties, express or implied, including
  *  but not limited to, warranties of merchantability or fitness
  *  for any particular purpose or that the use of the software or
  *  documentation will not infringe any third party patents,
  *  copyrights, trademarks or other rights.
  *
  *  The copyright holders and contributing author(s) will not be
  *  liable for any direct, indirect, special or consequential damages
  *  arising out of any use of the software or documentation, even if
  *  advised of the possibility of such damage.
  *
  *  Permission is hereby granted to use, copy, modify, and distribute
  *  this source code, or portions hereof, documentation and executables,
  *  for any purpose, without fee, subject to the following restrictions:
  *
  *  1. The origin of this source code must not be misrepresented.
  *  2. Altered versions must be plainly marked as such and must
  *     not be misrepresented as being the original source.
  *  3. This Copyright notice may not be removed or altered from any
  *     source or altered source distribution.
  *
  *  The copyright holders and contributing author(s) specifically
  *  permit, without fee, and encourage the use of this source code
  *  as a component for supporting the Hypertext Markup Language in
  *  commercial products. If you use this source code in a product,
  *  acknowledgment is not required but would be appreciated.
  *
  */
 package org.w3c.tidy;
 
 import org.w3c.tidy.Node.NodeType;
 import org.w3c.tidy.Options.TriState;
 
 /**
  * Clean up misuse of presentation markup. Filters from other formats such as Microsoft Word often make excessive use of
  * presentation markup such as font tags, B, I, and the align attribute. By applying a set of production rules, it is
  * straight forward to transform this to use CSS. Some rules replace some of the children of an element by style
  * properties on the element, e.g.
  * <p>
  * <b>... </b>
  * </p>.
  * <p style="font-weight: bold">
  * ...
  * </p>
  * Such rules are applied to the element's content and then to the element itself until none of the rules more apply.
  * Having applied all the rules to an element, it will have a style attribute with one or more properties. Other rules
  * strip the element they apply to, replacing it by style properties on the contents, e.g. <dir>
  * <li>
  * <p>
  * ...</li>
  * </dir>.
  * <p style="margin-left 1em">
  * ... These rules are applied to an element before processing its content and replace the current element by the first
  * element in the exposed content. After applying both sets of rules, you can replace the style attribute by a class
  * value and style rule in the document head. To support this, an association of styles and class names is built. A
  * naive approach is to rely on string matching to test when two property lists are the same. A better approach would be
  * to first sort the properties before matching.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public class Clean
 {
 	/**
      * xhtml namespace.
      */
     private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
 
     /**
      * Tag table.
      */
     private TagTable tt;
 
     /**
      * Instantiates a new Clean.
      * @param tagTable tag table instance
      */
     public Clean(TagTable tagTable)
     {
         this.tt = tagTable;
     }
     
     private void renameElem(final Node node, final TagId tid) {
         final Dict dict = tt.lookup(tid);
         node.element = dict.name;
         node.tag = dict;
     }
 
     /**
      * Insert a css style property.
      * @param props StyleProp instance
      * @param name property name
      * @param value property value
      * @return StyleProp containin the given property
      */
     private static StyleProp insertProperty(StyleProp props, String name, String value)
     {
         StyleProp first, prev, prop;
         int cmp;
 
         prev = null;
         first = props;
 
         while (props != null)
         {
             cmp = props.name.compareTo(name);
 
             if (cmp == 0)
             {
                 // this property is already defined, ignore new value
                 return first;
             }
 
             if (cmp > 0) // props.name > name
             {
                 // insert before this
 
                 prop = new StyleProp(name, value, props);
 
                 if (prev != null)
                 {
                     prev.next = prop;
                 }
                 else
                 {
                     first = prop;
                 }
 
                 return first;
             }
 
             prev = props;
             props = props.next;
         }
 
         prop = new StyleProp(name, value, null);
 
         if (prev != null)
         {
             prev.next = prop;
         }
         else
         {
             first = prop;
         }
 
         return first;
     }
 
     /**
      * Create sorted linked list of properties from style string.
      * @param prop StyleProp
      * @param style style string
      * @return StyleProp with given style
      */
     private static StyleProp createProps(StyleProp prop, String style)
     {
         int nameEnd;
         int valueEnd;
         int valueStart = 0;
         int nameStart = 0;
         boolean more;
 
         nameStart = 0;
         while (nameStart < style.length())
         {
             while (nameStart < style.length() && style.charAt(nameStart) == ' ')
             {
                 ++nameStart;
             }
 
             nameEnd = nameStart;
 
             while (nameEnd < style.length())
             {
                 if (style.charAt(nameEnd) == ':')
                 {
                     valueStart = nameEnd + 1;
                     break;
                 }
 
                 ++nameEnd;
             }
 
             if (nameEnd >= style.length() || style.charAt(nameEnd) != ':')
             {
                 break;
             }
 
             while (valueStart < style.length() && style.charAt(valueStart) == ' ')
             {
                 ++valueStart;
             }
 
             valueEnd = valueStart;
             more = false;
 
             while (valueEnd < style.length())
             {
                 if (style.charAt(valueEnd) == ';')
                 {
                     more = true;
                     break;
                 }
 
                 ++valueEnd;
             }
 
             prop = insertProperty(prop, style.substring(nameStart, nameEnd), style.substring(valueStart, valueEnd));
 
             if (more)
             {
                 nameStart = valueEnd + 1;
                 continue;
             }
 
             break;
         }
 
         return prop;
     }
 
     /**
      * Create a css property.
      * @param props StyleProp
      * @return css property as String
      */
     private static String createPropString(final StyleProp props) {
         final StringBuilder style = new StringBuilder();
         for (StyleProp prop = props; prop != null; prop = prop.next) {
             style.append(prop.name);
             if (prop.value != null) {
             	style.append(": ").append(prop.value);
             }
             if (prop.next == null) {
                 break;
             }
             style.append("; ");
         }
         return style.toString();
     }
 
     /**
      * Creates a string with merged properties.
      * @param style css style
      * @param property css properties
      * @return merged string
      */
     private static String addProperty(String style, String property)
     {
         StyleProp prop;
 
         prop = createProps(null, style);
         prop = createProps(prop, property);
         style = createPropString(prop);
         return style;
     }
 
     /**
      * Finds a css style.
      * @param lexer Lexer
      * @param tag tag name
      * @param properties css properties
      * @return style string
      */
     private static String findStyle(Lexer lexer, String tag, String properties)
     {
         Style style;
 
         for (style = lexer.styles; style != null; style = style.next)
         {
             if (style.tag.equals(tag) && style.properties.equals(properties))
             {
                 return style.tagClass;
             }
         }
 
         style = new Style(tag, lexer.gensymClass(), properties, lexer.styles);
         lexer.styles = style;
         return style.tagClass;
     }
     
     protected static void addStyleAsClass(final Lexer lexer, final Node node, final String stylevalue) {
     	final String classname = findStyle(lexer, node.element, stylevalue);
     	node.addClass(classname);
     }
 
     /**
      * Find style attribute in node, and replace it by corresponding class attribute. Search for class in style
      * dictionary otherwise gensym new class and add to dictionary. Assumes that node doesn't have a class attribute.
      * @param lexer Lexer
      * @param node node with a style attribute
      */
     private void style2Rule(Lexer lexer, Node node)
     {
         AttVal styleattr, classattr;
         String classname;
 
         styleattr = node.getAttrByName("style");
 
         if (styleattr != null)
         {
             classname = findStyle(lexer, node.element, styleattr.value);
             classattr = node.getAttrByName("class");
 
             // if there already is a class attribute then append class name after a space
 
             if (classattr != null)
             {
                 classattr.value = classattr.value + " " + classname;
                 node.removeAttribute(styleattr);
             }
             else
             {
                 // reuse style attribute for class attribute
                 styleattr.attribute = "class";
                 styleattr.value = classname;
             }
         }
     }
 
     /**
      * Adds a css rule for color.
      * @param lexer Lexer
      * @param selector css selector
      * @param color color value
      */
     private void addColorRule(Lexer lexer, String selector, String color)
     {
         if (color != null)
         {
             lexer.addStringLiteral(selector);
             lexer.addStringLiteral(" { color: ");
             lexer.addStringLiteral(color);
             lexer.addStringLiteral(" }\n");
         }
     }
 
     /**
      * Move presentation attribs from body to style element.
      * 
      * <pre>
      * background="foo" . body { background-image: url(foo) }
      * bgcolor="foo" . body { background-color: foo }
      * text="foo" . body { color: foo }
      * link="foo" . :link { color: foo }
      * vlink="foo" . :visited { color: foo }
      * alink="foo" . :active { color: foo }
      * </pre>
      * 
      * @param lexer Lexer
      * @param body body node
      */
     private void cleanBodyAttrs(Lexer lexer, Node body)
     {
         AttVal attr;
         String bgurl = null;
         String bgcolor = null;
         String color = null;
 
         attr = body.getAttrByName("background");
 
         if (attr != null)
         {
             bgurl = attr.value;
             attr.value = null;
             body.removeAttribute(attr);
         }
 
         attr = body.getAttrByName("bgcolor");
 
         if (attr != null)
         {
             bgcolor = attr.value;
             attr.value = null;
             body.removeAttribute(attr);
         }
 
         attr = body.getAttrByName("text");
 
         if (attr != null)
         {
             color = attr.value;
             attr.value = null;
             body.removeAttribute(attr);
         }
 
         if (bgurl != null || bgcolor != null || color != null)
         {
             lexer.addStringLiteral(" body {\n");
 
             if (bgurl != null)
             {
                 lexer.addStringLiteral("  background-image: url(");
                 lexer.addStringLiteral(bgurl);
                 lexer.addStringLiteral(");\n");
             }
 
             if (bgcolor != null)
             {
                 lexer.addStringLiteral("  background-color: ");
                 lexer.addStringLiteral(bgcolor);
                 lexer.addStringLiteral(";\n");
             }
 
             if (color != null)
             {
                 lexer.addStringLiteral("  color: ");
                 lexer.addStringLiteral(color);
                 lexer.addStringLiteral(";\n");
             }
 
             lexer.addStringLiteral(" }\n");
         }
 
         attr = body.getAttrByName("link");
 
         if (attr != null)
         {
             addColorRule(lexer, " :link", attr.value);
             body.removeAttribute(attr);
         }
 
         attr = body.getAttrByName("vlink");
 
         if (attr != null)
         {
             addColorRule(lexer, " :visited", attr.value);
             body.removeAttribute(attr);
         }
 
         attr = body.getAttrByName("alink");
 
         if (attr != null)
         {
             addColorRule(lexer, " :active", attr.value);
             body.removeAttribute(attr);
         }
     }
 
     /**
      * Check deprecated attributes in body tag.
      * @param lexer Lexer
      * @param doc document root node
      * @return <code>true</code> is the body doesn't contain deprecated attributes, false otherwise.
      */
     private boolean niceBody(Lexer lexer, Node doc)
     {
         Node body = doc.findBody();
 
         if (body != null)
         {
             if (body.getAttrByName("background") != null
                 || body.getAttrByName("bgcolor") != null
                 || body.getAttrByName("text") != null
                 || body.getAttrByName("link") != null
                 || body.getAttrByName("vlink") != null
                 || body.getAttrByName("alink") != null)
             {
                 lexer.badLayout |= Report.USING_BODY;
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Create style element using rules from dictionary.
      * @param lexer Lexer
      * @param doc root node
      */
     private void createStyleElement(Lexer lexer, Node doc)
     {
         Node node, head, body;
         Style style;
         AttVal av;
 
         if (lexer.styles == null && niceBody(lexer, doc))
         {
             return;
         }
 
         node = lexer.newNode(NodeType.StartTag, null, 0, 0, "style");
         node.implicit = true;
 
         // insert type attribute
         av = new AttVal(null, null, '"', "type", "text/css");
         av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
         node.attributes = av;
 
         body = doc.findBody();
 
         lexer.txtstart = lexer.lexsize;
 
         if (body != null)
         {
             cleanBodyAttrs(lexer, body);
         }
 
         for (style = lexer.styles; style != null; style = style.next)
         {
             lexer.addCharToLexer(' ');
             lexer.addStringLiteral(style.tag);
             lexer.addCharToLexer('.');
             lexer.addStringLiteral(style.tagClass);
             lexer.addCharToLexer(' ');
             lexer.addCharToLexer('{');
             lexer.addStringLiteral(style.properties);
             lexer.addCharToLexer('}');
             lexer.addCharToLexer('\n');
         }
 
         lexer.txtend = lexer.lexsize;
 
         node.insertNodeAtEnd(lexer.newNode(NodeType.TextNode, lexer.lexbuf, lexer.txtstart, lexer.txtend));
 
         // now insert style element into document head doc is root node. search its children for html node the head
         // node should be first child of html node
 
         head = doc.findHEAD();
 
         if (head != null)
         {
             head.insertNodeAtEnd(node);
         }
     }
 
     /**
      * Ensure bidirectional links are consistent.
      * @param node root node
      */
     private void fixNodeLinks(Node node)
     {
         Node child;
 
         if (node.prev != null)
         {
             node.prev.next = node;
         }
         else
         {
             node.parent.content = node;
         }
 
         if (node.next != null)
         {
             node.next.prev = node;
         }
         else
         {
             node.parent.last = node;
         }
 
         for (child = node.content; child != null; child = child.next)
         {
             child.parent = node;
         }
     }
 
     /**
      * Used to strip child of node when the node has one and only one child.
      * @param node parent node
      */
     private void stripOnlyChild(Node node)
     {
         Node child;
 
         child = node.content;
         node.content = child.content;
         node.last = child.last;
         child.content = null;
 
         for (child = node.content; child != null; child = child.next)
         {
             child.parent = node;
         }
     }
 
     /**
      * Used to strip font start and end tags.
      * @param element original node
      * @param pnode passed in as array to allow modification. pnode[0] will contain the final node
      * @todo remove the pnode parameter and make it a return value
      */
     private void discardContainer(Node element, Node[] pnode)
     {
         Node node;
         Node parent = element.parent;
 
         if (element.content != null)
         {
             element.last.next = element.next;
 
             if (element.next != null)
             {
                 element.next.prev = element.last;
                 element.last.next = element.next;
             }
             else
             {
                 parent.last = element.last;
             }
 
             if (element.prev != null)
             {
                 element.content.prev = element.prev;
                 element.prev.next = element.content;
             }
             else
             {
                 parent.content = element.content;
             }
 
             for (node = element.content; node != null; node = node.next)
             {
                 node.parent = parent;
             }
 
             pnode[0] = element.content;
         }
         else
         {
             if (element.next != null)
             {
                 element.next.prev = element.prev;
             }
             else
             {
                 parent.last = element.prev;
             }
 
             if (element.prev != null)
             {
                 element.prev.next = element.next;
             }
             else
             {
                 parent.content = element.next;
             }
 
             pnode[0] = element.next;
         }
 
         element.next = null;
         element.content = null;
     }
 
     /**
      * Add style property to element, creating style attribute as needed and adding ; delimiter.
      * @param node node
      * @param property property added to node
      */
     protected static void addStyleProperty(Node node, String property)
     {
         AttVal av;
 
         for (av = node.attributes; av != null; av = av.next)
         {
             if (av.attribute.equals("style"))
             {
                 break;
             }
         }
 
         // if style attribute already exists then insert property
 
         if (av != null)
         {
             String s;
 
             s = addProperty(av.value, property);
             av.value = s;
         }
         else
         {
             // else create new style attribute
             av = new AttVal(node.attributes, null, '"', "style", property);
             av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
             node.attributes = av;
         }
     }
 
     /**
      * Create new string that consists of the combined style properties in s1 and s2. To merge property lists, we build
      * a linked list of property/values and insert properties into the list in order, merging values for the same
      * property name.
      * @param s1 first property
      * @param s2 second property
      * @return merged properties
      */
     private String mergeProperties(String s1, String s2)
     {
         String s;
         StyleProp prop;
 
         prop = createProps(null, s1);
         prop = createProps(prop, s2);
         s = createPropString(prop);
         return s;
     }
 
     /**
      * Merge class attributes from 2 nodes.
      * @param node Node
      * @param child Child node
      */
     private void mergeClasses(Node node, Node child)
     {
         AttVal av;
         String s1, s2, names;
 
         for (s2 = null, av = child.attributes; av != null; av = av.next)
         {
             if ("class".equals(av.attribute))
             {
                 s2 = av.value;
                 break;
             }
         }
 
         for (s1 = null, av = node.attributes; av != null; av = av.next)
         {
             if ("class".equals(av.attribute))
             {
                 s1 = av.value;
                 break;
             }
         }
 
         if (s1 != null)
         {
             if (s2 != null) // merge class names from both
             {
                 names = s1 + ' ' + s2;
                 av.value = names;
             }
         }
         else if (s2 != null) // copy class names from child
         {
             av = new AttVal(node.attributes, null, '"', "class", s2);
             av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
             node.attributes = av;
         }
     }
 
     /**
      * Merge style from 2 nodes.
      * @param node Node
      * @param child Child node
      */
     private void mergeStyles(Node node, Node child)
     {
         AttVal av;
         String s1, s2, style;
 
         // the child may have a class attribute used for attaching styles, if so the class name needs to be copied to
         // node's class
         mergeClasses(node, child);
 
         for (s2 = null, av = child.attributes; av != null; av = av.next)
         {
             if (av.attribute.equals("style"))
             {
                 s2 = av.value;
                 break;
             }
         }
 
         for (s1 = null, av = node.attributes; av != null; av = av.next)
         {
             if (av.attribute.equals("style"))
             {
                 s1 = av.value;
                 break;
             }
         }
 
         if (s1 != null)
         {
             if (s2 != null) // merge styles from both
             {
                 style = mergeProperties(s1, s2);
                 av.value = style;
             }
         }
         else if (s2 != null) // copy style of child
         {
             av = new AttVal(node.attributes, null, '"', "style", s2);
             av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(av);
             node.attributes = av;
         }
     }
 
     /**
      * Map a % font size to a named font size.
      * @param size size in %
      * @return font size name
      */
     private String fontSize2Name(String size)
     {
         String[] sizes = {"60%", "70%", "80%", null, "120%", "150%", "200%"};
         String buf;
 
         if (size.length() > 0 && '0' <= size.charAt(0) && size.charAt(0) <= '6')
         {
             int n = size.charAt(0) - '0';
             return sizes[n];
         }
 
         if (size.length() > 0 && size.charAt(0) == '-')
         {
             if (size.length() > 1 && '0' <= size.charAt(1) && size.charAt(1) <= '6')
             {
                 int n = size.charAt(1) - '0';
                 double x;
 
                 for (x = 1.0; n > 0; --n)
                 {
                     x *= 0.8;
                 }
 
                 x *= 100.0;
                 buf = "" + (int) x + "%";
 
                 return buf;
             }
 
             return "smaller"; /* "70%"; */
         }
 
         if (size.length() > 1 && '0' <= size.charAt(1) && size.charAt(1) <= '6')
         {
             int n = size.charAt(1) - '0';
             double x;
 
             for (x = 1.0; n > 0; --n)
             {
                 x *= 1.2;
             }
 
             x *= 100.0;
             buf = "" + (int) x + "%";
 
             return buf;
         }
 
         return "larger"; /* "140%" */
     }
 
     /**
      * Adds a font-family style.
      * @param node Node
      * @param face font face
      */
     private void addFontFace(Node node, String face)
     {
         addStyleProperty(node, "font-family: " + face);
     }
 
     /**
      * Adds a font size style.
      * @param node Node
      * @param size font size
      */
     private void addFontSize(Node node, String size)
     {
         if (size == null)
         {
             return;
         }
 
         if ("6".equals(size) && node.is(TagId.P))
         {
             node.element = "h1";
             this.tt.findTag(node);
             return;
         }
 
         if ("5".equals(size) && node.is(TagId.P))
         {
             node.element = "h2";
             this.tt.findTag(node);
             return;
         }
 
         if ("4".equals(size) && node.is(TagId.P))
         {
             node.element = "h3";
             this.tt.findTag(node);
             return;
         }
 
         String value = fontSize2Name(size);
 
         if (value != null)
         {
             addStyleProperty(node, "font-size: " + value);
         }
     }
 
     /**
      * Adds a font color style.
      * @param node Node
      * @param color color value
      */
     private void addFontColor(Node node, String color)
     {
         addStyleProperty(node, "color: " + color);
     }
 
     /**
      * Adds an align style.
      * @param node Node
      * @param align align value
      */
     private void addAlign(Node node, String align)
     {
         // force alignment value to lower case
         addStyleProperty(node, "text-align: " + align.toLowerCase());
     }
 
     /**
      * Add style properties to node corresponding to the font face, size and color attributes.
      * @param node font tag
      * @param av attribute list for node
      */
     private void addFontStyles(Node node, AttVal av)
     {
         while (av != null)
         {
             if (av.attribute.equals("face"))
             {
                 addFontFace(node, av.value);
             }
             else if (av.attribute.equals("size"))
             {
                 addFontSize(node, av.value);
             }
             else if (av.attribute.equals("color"))
             {
                 addFontColor(node, av.value);
             }
 
             av = av.next;
         }
     }
 
     /**
      * Symptom: <code>&lt;p align=center></code>. Action: <code>&lt;p style="text-align: center"></code>.
      * @param lexer Lexer
      * @param node node with center attribute. Will be modified to use css style.
      */
     private void textAlign(Lexer lexer, Node node)
     {
         AttVal av, prev;
 
         prev = null;
 
         for (av = node.attributes; av != null; av = av.next)
         {
             if (av.attribute.equals("align"))
             {
                 if (prev != null)
                 {
                     prev.next = av.next;
                 }
                 else
                 {
                     node.attributes = av.next;
                 }
 
                 if (av.value != null)
                 {
                     addAlign(node, av.value);
                 }
 
                 break;
             }
 
             prev = av;
         }
     }
     
     /*
 	    Symptom: <table bgcolor="red">
 	    Action: <table style="background-color: red">
 	*/
 	private static void tableBgColor(final Node node) {
 	    final AttVal attr = node.getAttrById(AttrId.BGCOLOR);
 	    if (null != attr) {
 	        node.removeAttribute(attr);
 	        addStyleProperty(node, "background-color: " + attr.value);
 	    }
 	}
 
     /**
      * Symptom: <code>&lt;dir>&lt;li></code> where <code>&lt;li></code> is only child. Action: coerce
      * <code>&lt;dir> &lt;li></code> to <code>&lt;div></code> with indent. The clean up rules use the pnode argument
      * to return the next node when the original node has been deleted.
      * @param lexer Lexer
      * @param node dir tag
      * @return <code>true</code> if a dir tag has been coerced to a div
      */
     private boolean dir2Div(Lexer lexer, Node node)
     {
         Node child;
 
         if (node.is(TagId.DIR) || node.is(TagId.UL) || node.is(TagId.OL))
         {
             child = node.content;
 
             if (child == null)
             {
                 return false;
             }
 
             // check child has no peers
             if (child.next != null)
             {
                 return false;
             }
 
             if (!child.is(TagId.LI))
             {
                 return false;
             }
 
             if (!child.implicit)
             {
                 return false;
             }
 
             // coerce dir to div
             renameElem(node, TagId.DIV);
             addStyleProperty(node, "margin-left: 2em");
             stripOnlyChild(node);
             return true;
         }
 
         return false;
     }
 
     /**
      * Symptom:
      * 
      * <pre>
      * &lt;center>
      * </pre>.
      * <p>
      * Action: replace <code>&lt;center></code> by <code>&lt;div style="text-align: center"></code>
      * </p>
      * @param lexer Lexer
      * @param node center tag
      * @param pnode pnode[0] is the same as node, passed in as an array to allow modification
      * @return <code>true</code> if a center tag has been replaced by a div
      */
     private boolean center2Div(Lexer lexer, Node node, Node[] pnode)
     {
         if (node.is(TagId.CENTER))
         {
             if (lexer.configuration.isDropFontTags())
             {
                 if (node.content != null)
                 {
                     Node last = node.last;
                     Node parent = node.parent;
 
                     discardContainer(node, pnode);
 
                     node = lexer.inferredTag(TagId.BR);
 
                     if (last.next != null)
                     {
                         last.next.prev = node;
                     }
 
                     node.next = last.next;
                     last.next = node;
                     node.prev = last;
 
                     if (parent.last == last)
                     {
                         parent.last = node;
                     }
 
                     node.parent = parent;
                 }
                 else
                 {
                     Node prev = node.prev;
                     Node next = node.next;
                     Node parent = node.parent;
                     discardContainer(node, pnode);
 
                     node = lexer.inferredTag(TagId.BR);
                     node.next = next;
                     node.prev = prev;
                     node.parent = parent;
 
                     if (next != null)
                     {
                         next.prev = node;
                     }
                     else
                     {
                         parent.last = node;
                     }
 
                     if (prev != null)
                     {
                         prev.next = node;
                     }
                     else
                     {
                         parent.content = node;
                     }
                 }
 
                 return true;
             }
             renameElem(node, TagId.DIV);
             addStyleProperty(node, "text-align: center");
             return true;
         }
 
         return false;
     }
 
 	/* Copy child attributes to node. Duplicate attributes are overwritten.
 	   Unique attributes (such as ID) disable the action.
 	   Attributes style and class are not dealt with. A call to MergeStyles
 	   will do that.
 	*/
 	private boolean copyAttrs(final Node node, final Node child) {
 		/* Detect attributes that cannot be merged or overwritten. */
 		if (child.getAttrById(AttrId.ID) != null && node.getAttrById(AttrId.ID) != null) {
 			return false;
 		}
 
 		/* Move child attributes to node. Attributes in node
 		 can be overwritten or merged. */
 		for (AttVal av2 = child.attributes; av2 != null; ) {
 			/* Dealt by MergeStyles. */
 			if (av2.is(AttrId.STYLE) || av2.is(AttrId.CLASS)) {
 				av2 = av2.next;
 				continue;
 			}
 			/* Avoid duplicates in node */
 			final AttrId id = av2.getId();
 			AttVal av1;
 			if (id != AttrId.UNKNOWN && (av1 = node.getAttrById(id)) != null) {
 				node.removeAttribute(av1);
 			}
 
 			/* Move attribute from child to node */
 			child.removeAttribute(av2);
 			av1 = av2;
 			av2 = av2.next;
 			av1.next = null;
 			node.insertAttributeAtEnd(av1);
 		}
 		return true;
 	}
 
 	/*
 	    Symptom <XX><XX>...</XX></XX>
 	    Action: merge the two XXs
 	
 	  For instance, this is useful after nested <dir>s used by Word
 	  for indenting have been converted to <div>s
 	
 	  If state is "no", no merging.
 	  If state is "yes", inner element is discarded. Only Style and Class
 	  attributes are merged using MergeStyles().
 	  If state is "auto", atttibutes are merged as described in CopyAttrs().
 	  Style and Class attributes are merged using MergeStyles().
 	*/
     private boolean mergeNestedElements(final TagId id, final TriState state, final Node node) {
         if (state == TriState.No || !node.is(id)) {
             return false;
         }
         
         final Node child = node.content;
 
         if (child == null || child.next != null || !child.is(id)) {
             return false;
         }
 
         if (state == TriState.Auto && !copyAttrs(node, child)) {
         	return false;
         }
         mergeStyles(node, child);
         stripOnlyChild(node);
         return true;
     }
 
     /**
      * Symptom:
      * <ul>
      * <li>
      * <ul>
      * ...
      * </ul>
      * </li>
      * </ul>
      * Action: discard outer list.
      * @param lexer Lexer
      * @param node Node
      * @param pnode passed in as array to allow modifications.
      * @return <code>true</code> if nested lists have been found and replaced
      */
     private boolean nestedList(Lexer lexer, Node node, Node[] pnode)
     {
         Node child, list;
 
         if (node.is(TagId.UL) || node.is(TagId.OL))
         {
             child = node.content;
 
             if (child == null)
             {
                 return false;
             }
 
             // check child has no peers
 
             if (child.next != null)
             {
                 return false;
             }
 
             list = child.content;
 
             if (list == null)
             {
                 return false;
             }
 
             if (list.tag != node.tag)
             {
                 return false;
             }
 
             pnode[0] = list; // Set node to resume iteration
 
             // move inner list node into position of outer node
             list.prev = node.prev;
             list.next = node.next;
             list.parent = node.parent;
             fixNodeLinks(list);
 
             // get rid of outer ul and its li
             // XXX: Are we leaking the child node? -creitzel 7 Jun, 01
             child.content = null;
             node.content = null;
             node.next = null;
             node = null;
 
             // If prev node was a list the chances are this node should be appended to that list. Word has no way of
             // recognizing nested lists and just uses indents
             if (list.prev != null)
             {
                 if ((list.prev.is(TagId.UL) || list.prev.is(TagId.OL)) && list.prev.last != null) {
                     node = list;
                     list = node.prev;
 
                     list.next = node.next;
 
                     if (list.next != null)
                     {
                         list.next.prev = list;
                     }
 
                     child = list.last; /* <li> */
 
                     node.parent = child;
                     node.next = null;
                     node.prev = child.last;
                     fixNodeLinks(node);
                     cleanNode(lexer, node);
                 }
             }
 
             return true;
         }
 
         return false;
     }
     
     private static class CSSSpanEq {
     	public final TagId id;
     	public final String cssEq;
     	public final boolean deprecated;
     	
 		private CSSSpanEq(final TagId id, final String cssEq, final boolean deprecated) {
 			this.id = id;
 			this.cssEq = cssEq;
 			this.deprecated = deprecated;
 		}
     }
     
     private static final CSSSpanEq CSS_SPAN_EQ[] = {
     	new CSSSpanEq(TagId.B, "font-weight: bold", false),
         new CSSSpanEq(TagId.I, "font-style: italic", false),
         new CSSSpanEq(TagId.S, "text-decoration: line-through", true),
         new CSSSpanEq(TagId.STRIKE, "text-decoration: line-through", true),
         new CSSSpanEq(TagId.U, "text-decoration: underline", true)
     };
     
     /* Find CSS equivalent in a SPAN element */
     private static String findCSSSpanEq(final Node node, final boolean deprecatedOnly) {
         for (CSSSpanEq e : CSS_SPAN_EQ) {
             if ((!deprecatedOnly || e.deprecated) && node.is(e.id)) {
             	return e.cssEq;
             }
         }
         return null; 
     }
 
     /* Necessary conditions to apply BlockStyle(). */
     private static boolean canApplyBlockStyle(final Node node) {
         return node.hasCM(Dict.CM_BLOCK | Dict.CM_LIST | Dict.CM_DEFLIST | Dict.CM_TABLE)
             && !node.is(TagId.TABLE) && !node.is(TagId.TR) && !node.is(TagId.LI);
     }
 
     /**
      * Symptom: the only child of a block-level element is a presentation element such as B, I or FONT. Action: add
      * style "font-weight: bold" to the block and strip the &lt;b>element, leaving its children. example:
      * 
      * <pre>
      * &lt;p>
      * &lt;b>&lt;font face="Arial" size="6">Draft Recommended Practice&lt;/font>&lt;/b>
      * &lt;/p>
      * </pre>
      * 
      * becomes:
      * 
      * <pre>
      * &lt;p style="font-weight: bold; font-family: Arial; font-size: 6">
      * Draft Recommended Practice
      * &lt;/p>
      * </pre>
      * 
      * <p>
      * This code also replaces the align attribute by a style attribute. However, to avoid CSS problems with Navigator
      * 4, this isn't done for the elements: caption, tr and table
      * </p>
      * @param lexer Lexer
      * @param node parent node
      * @return <code>true</code> if the child node has been removed
      */
     private boolean blockStyle(final Lexer lexer, final Node node) {
         /* check for bgcolor */
         if (node.is(TagId.TABLE) || node.is(TagId.TD) || node.is(TagId.TH) || node.is(TagId.TR)) {
             tableBgColor(node);
         }
         if (canApplyBlockStyle(node)) {
             // check for align attribute
             if (!node.is(TagId.CAPTION)) {
                 textAlign(lexer, node);
             }
             final Node child = node.content;
             if (child == null) {
                 return false;
             }
             // check child has no peers
             if (child.next != null) {
                 return false;
             }
             final String cssEq = findCSSSpanEq(child, false);
             if (cssEq != null) {
                 mergeStyles(node, child);
                 addStyleProperty(node, cssEq);
                 stripOnlyChild(node);
                 return true;
             }
             else if (child.is(TagId.FONT)) {
                 mergeStyles(node, child);
                 addFontStyles(node, child.attributes);
                 stripOnlyChild(node);
                 return true;
             }
         }
         return false;
     }
     
     /* Necessary conditions to apply InlineStyle(). */
     private static boolean canApplyInlineStyle(final Node node) {
         return !node.is(TagId.FONT) && node.hasCM(Dict.CM_INLINE | Dict.CM_ROW);
     }
 
     /**
      * If the node has only one b, i, or font child remove the child node and add the appropriate style attributes to
      * parent.
      * @param lexer Lexer
      * @param node parent node
      * @param pnode passed as an array to allow modifications
      * @return <code>true</code> if child node has been stripped, replaced by style attributes.
      */
     private boolean inlineStyle(final Lexer lexer, final Node node, final Node[] pnode) {
         if (canApplyInlineStyle(node)) {
             final Node child = node.content;
             if (child == null) {
                 return false;
             }
             // check child has no peers
             if (child.next != null) {
                 return false;
             }
             final String cssEq = findCSSSpanEq(child, false);
             if (cssEq != null) {
                 mergeStyles(node, child);
                 addStyleProperty(node, cssEq);
                 stripOnlyChild(node);
                 return true;
             }
             else if (child.is(TagId.FONT)) {
                 mergeStyles(node, child);
                 addFontStyles(node, child.attributes);
                 stripOnlyChild(node);
                 return true;
             }
         }
         return false;
     }
     
     /*
 	    Transform element to equivalent CSS
 	*/
     private boolean inlineElementToCSS(final Node node) {
 		/* if node is the only child of parent element then leave alone
 		Do so only if BlockStyle may be succesful. */
 		if (node.parent.content == node && node.next == null &&
 				(canApplyBlockStyle(node.parent) || canApplyInlineStyle(node.parent))) {
 			return false;
 		}
 		final String cssEq = findCSSSpanEq(node, true);
 		if (cssEq != null) {
 			renameElem(node, TagId.SPAN);
 			addStyleProperty(node, cssEq);
 			return true;
 		}
 		return false;
 	} 
 
     /**
      * Replace font elements by span elements, deleting the font element's attributes and replacing them by a single
      * style attribute.
      * @param lexer Lexer
      * @param node font tag
      * @param pnode passed as an array to allow modifications
      * @return <code>true</code> if a font tag has been dropped and replaced by style attributes
      */
     private boolean font2Span(Lexer lexer, Node node, Node[] pnode)
     {
         AttVal av, style, next;
 
         if (node.is(TagId.FONT))
         {
             if (lexer.configuration.isDropFontTags())
             {
                 discardContainer(node, pnode);
                 return false;
             }
 
             // if FONT is only child of parent element then leave alone
            // Do so only if blockStyle may be succesful.
            if (node.parent.content == node && node.next == null && canApplyBlockStyle(node.parent)) {
                 return false;
             }
 
             addFontStyles(node, node.attributes);
 
             // extract style attribute and free the rest
             av = node.attributes;
             style = null;
 
             while (av != null)
             {
                 next = av.next;
 
                 if (av.attribute.equals("style"))
                 {
                     av.next = null;
                     style = av;
                 }
 
                 av = next;
             }
 
             node.attributes = style;
 
             renameElem(node, TagId.SPAN);
             return true;
         }
 
         return false;
     }
 
     /**
      * Applies all matching rules to a node.
      * @param lexer Lexer
      * @param node original node
      * @return cleaned up node
      */
     private Node cleanNode(Lexer lexer, Node node)
     {
         Node next = null;
         Node[] o = new Node[1];
         boolean b = false;
         final TriState mergeDivs = lexer.configuration.getMergeDivs();
         final TriState mergeSpans = lexer.configuration.getMergeSpans();
 
         for (next = node; node != null && node.isElement(); node = next)
         {
             o[0] = next;
 
             b = dir2Div(lexer, node);
             next = o[0];
             if (b)
             {
                 continue;
             }
 
             // Special case: true result means that arg node and its parent no longer exist.
             // So we must jump back up the CreateStyleProperties() call stack until we have a valid node reference.
             b = nestedList(lexer, node, o);
             next = o[0];
             if (b)
             {
                 return next;
             }
 
             b = center2Div(lexer, node, o);
             next = o[0];
             if (b)
             {
                 continue;
             }
 
             if (mergeNestedElements(TagId.DIV, mergeDivs, node)) {
                 continue;
             }
 
             if (mergeNestedElements(TagId.SPAN, mergeSpans, node)) {
                 continue;
             }
 
             b = blockStyle(lexer, node);
             next = o[0];
             if (b)
             {
                 continue;
             }
 
             b = inlineStyle(lexer, node, o);
             next = o[0];
             if (b)
             {
                 continue;
             }
             if (inlineElementToCSS(node)) {
             	continue;
             }
             b = font2Span(lexer, node, o);
             next = o[0];
             if (b)
             {
                 continue;
             }
 
             break;
         }
 
         return next;
     }
 
     /**
      * Special case: if the current node is destroyed by CleanNode() lower in the tree, this node and its parent no
      * longer exist. So we must jump back up the CreateStyleProperties() call stack until we have a valid node
      * reference.
      * @param lexer Lexer
      * @param node Node
      * @param prepl passed in as array to allow modifications
      * @return cleaned Node
      */
     private Node cleanTree(Lexer lexer, Node node, Node[] prepl)
     {
         Node child = node.content;
 
         if (child != null)
         {
             Node[] repl = new Node[1];
             repl[0] = node;
             while (child != null)
             {
                 child = cleanTree(lexer, child, repl);
                 if (repl[0] != node)
                 {
                     return repl[0];
                 }
                 if (child != null)
                 {
                     child = child.next;
                 }
             }
         }
 
         return cleanNode(lexer, node);
     }
 
     /**
      * Find style attribute in node content, and replace it by corresponding class attribute.
      * @param lexer Lexer
      * @param node parent node
      */
     private void defineStyleRules(Lexer lexer, Node node)
     {
         Node child;
 
         if (node.content != null)
         {
             child = node.content;
             while (child != null)
             {
                 defineStyleRules(lexer, child);
                 child = child.next;
             }
         }
 
         style2Rule(lexer, node);
     }
 
     /**
      * Clean an html tree.
      * @param lexer Lexer
      * @param doc root node
      */
     public void cleanDocument(Lexer lexer, Node doc)
     {
         Node[] repl = new Node[1];
         repl[0] = doc;
         doc = cleanTree(lexer, doc, repl);
 
         if (lexer.configuration.isMakeClean())
         {
             defineStyleRules(lexer, doc);
             createStyleElement(lexer, doc);
         }
     }
 
     /**
      * simplifies <b><b>... </b> ... </b> etc.
      * @param node root Node
      */
     public void nestedEmphasis(Node node)
     {
         Node[] o = new Node[1];
         Node next;
 
         while (node != null)
         {
             next = node.next;
 
             if ((node.is(TagId.B) || node.is(TagId.I))
                 && node.parent != null
                 && node.parent.tag == node.tag)
             {
                 // strip redundant inner element
                 o[0] = next;
                 discardContainer(node, o);
                 next = o[0];
                 node = next;
                 continue;
             }
 
             if (node.content != null)
             {
                 nestedEmphasis(node.content);
             }
 
             node = next;
         }
     }
 
     /**
      * Replace i by em and b by strong.
      * @param node root Node
      */
     public void emFromI(Node node)
     {
         while (node != null)
         {
             if (node.is(TagId.I))
             {
             	renameElem(node, TagId.EM);
             }
             else if (node.is(TagId.B))
             {
             	renameElem(node, TagId.STRONG);
             }
 
             if (node.content != null)
             {
                 emFromI(node.content);
             }
 
             node = node.next;
         }
     }
 
     /**
      * Some people use dir or ul without an li to indent the content. The pattern to look for is a list with a single
      * implicit li. This is recursively replaced by an implicit blockquote.
      * @param node root Node
      */
     public void list2BQ(Node node)
     {
         while (node != null)
         {
             if (node.content != null)
             {
                 list2BQ(node.content);
             }
 
             if (node.tag != null
                 && node.tag.getParser() == ParserImpl.LIST
                 && node.hasOneChild()
                 && node.content.implicit)
             {
                 stripOnlyChild(node);
                 renameElem(node, TagId.BLOCKQUOTE);
                 node.implicit = true;
             }
 
             node = node.next;
         }
     }
 
     /**
      * Replace implicit blockquote by div with an indent taking care to reduce nested blockquotes to a single div with
      * the indent set to match the nesting depth.
      * @param node root Node
      */
     public void bQ2Div(Node node)
     {
         while (node != null)
         {
             if (node.is(TagId.BLOCKQUOTE) && node.implicit)
             {
                 int indent = 1;
 
                 while (node.hasOneChild() && node.content.is(TagId.BLOCKQUOTE) && node.implicit)
                 {
                     ++indent;
                     stripOnlyChild(node);
                 }
 
                 if (node.content != null)
                 {
                     bQ2Div(node.content);
                 }
 
                 String indentBuf = "margin-left: " + 2 * indent + "em";
 
                 renameElem(node, TagId.DIV);
                 addStyleProperty(node, indentBuf);
             }
             else if (node.content != null)
             {
                 bQ2Div(node.content);
             }
 
             node = node.next;
         }
     }
 
     /**
      * Find the enclosing table cell for the given node.
      * @param node Node
      * @return enclosing cell node
      */
     Node findEnclosingCell(Node node)
     {
         Node check;
 
         for (check = node; check != null; check = check.parent)
         {
             if (check.is(TagId.TD))
             {
                 return check;
             }
         }
         return null;
     }
 
     /**
      * node is <code>&lt;![if ...]&gt;</code> prune up to <code>&lt;![endif]&gt;</code>.
      * @param lexer Lexer
      * @param node Node
      * @return cleaned up Node
      */
     public Node pruneSection(Lexer lexer, Node node)
     {
         for (;;)
         {
             if ((TidyUtils.getString(node.textarray, node.start, 21)).equals("if !supportEmptyParas")) {
 	            Node cell = findEnclosingCell(node);
 	            if (cell != null) {
 		            // Need to put &nbsp; into cell so it doesn't look weird
 		            Node nbsp = lexer.newLiteralTextNode("\u00a0");
 		            Node.insertNodeBeforeElement(node, nbsp);
 	            }
             }
 
             // discard node and returns next, unless it is a text node
             if (node.type == NodeType.TextNode) {
             	node = node.next;
             } else {
             	node = Node.discardElement(node);
             }
 
             if (node == null)
             {
                 return null;
             }
 
             if (node.type == NodeType.SectionTag)
             {
                 if ((TidyUtils.getString(node.textarray, node.start, 2)).equals("if"))
                 {
                     node = pruneSection(lexer, node);
                     continue;
                 }
 
                 if ((TidyUtils.getString(node.textarray, node.start, 5)).equals("endif"))
                 {
                     node = Node.discardElement(node);
                     break;
                 }
             }
         }
 
         return node;
     }
 
     /**
      * Drop if/endif sections inserted by word2000.
      * @param lexer Lexer
      * @param node Node root node
      */
     public void dropSections(Lexer lexer, Node node)
     {
         while (node != null)
         {
             if (node.type == NodeType.SectionTag)
             {
                 // prune up to matching endif
                 if ((TidyUtils.getString(node.textarray, node.start, 2)).equals("if")
                     && (!(TidyUtils.getString(node.textarray, node.start, 7)).equals("if !vml"))) // #444394 - fix 13
                 // Sep 01
                 {
                     node = pruneSection(lexer, node);
                     continue;
                 }
 
                 // discard others as well
                 node = Node.discardElement(node);
                 continue;
             }
 
             if (node.content != null)
             {
                 dropSections(lexer, node.content);
             }
 
             node = node.next;
         }
     }
 
     /**
      * Remove word2000 attributes from node.
      * @param node node to cleanup
      */
     public void purgeWord2000Attributes(Node node)
     {
         AttVal attr = null;
         AttVal next = null;
         AttVal prev = null;
 
         for (attr = node.attributes; attr != null; attr = next)
         {
             next = attr.next;
 
             // special check for class="Code" denoting pre text
             // Pass thru user defined styles as HTML class names
             if (attr.attribute != null && attr.value != null && attr.attribute.equals("class"))
             {
                 if (attr.value.equals("Code") || !attr.value.startsWith("Mso"))
                 {
                     prev = attr;
                     continue;
                 }
             }
 
             if (attr.attribute != null
                 && (attr.attribute.equals("class")
                     || attr.attribute.equals("style")
                     || attr.attribute.equals("lang")
                     || attr.attribute.startsWith("x:") || ((attr.attribute.equals("height") || attr.attribute
                     .equals("width")) && //
                 (node.is(TagId.TD) || node.is(TagId.TR) || node.is(TagId.TH)))))
             {
                 if (prev != null)
                 {
                     prev.next = next;
                 }
                 else
                 {
                     node.attributes = next;
                 }
 
             }
             else
             {
                 prev = attr;
             }
         }
     }
 
     /**
      * Word2000 uses span excessively, so we strip span out.
      * @param lexer Lexer
      * @param span Node span
      * @return cleaned node
      */
     public Node stripSpan(Lexer lexer, Node span)
     {
         Node node;
         Node prev = null;
         Node content;
 
         // deal with span elements that have content by splicing the content in place of the span after having
         // processed it
 
         cleanWord2000(lexer, span.content);
         content = span.content;
 
         if (span.prev != null)
         {
             prev = span.prev;
         }
         else if (content != null)
         {
             node = content;
             content = content.next;
             node.removeNode();
             Node.insertNodeBeforeElement(span, node);
             prev = node;
         }
 
         while (content != null)
         {
             node = content;
             content = content.next;
             node.removeNode();
             prev.insertNodeAfterElement(node);
             prev = node;
         }
 
         if (span.next == null)
         {
             span.parent.last = prev;
         }
 
         node = span.next;
         span.content = null;
         Node.discardElement(span);
         return node;
     }
 
     /**
      * Map non-breaking spaces to regular spaces.
      * @param lexer Lexer
      * @param node Node
      */
     private void normalizeSpaces(Lexer lexer, Node node)
     {
         while (node != null)
         {
             if (node.content != null)
             {
                 normalizeSpaces(lexer, node.content);
             }
 
             if (node.type == NodeType.TextNode)
             {
                 int i;
                 int[] c = new int[1];
                 int p = node.start;
 
                 for (i = node.start; i < node.end; ++i)
                 {
                     c[0] = node.textarray[i];
 
                     // look for UTF-8 multibyte character
                     if (c[0] > 0x7F)
                     {
                         i += PPrint.getUTF8(node.textarray, i, c);
                     }
 
                     if (c[0] == 160)
                     {
                         c[0] = ' ';
                     }
 
                     p = PPrint.putUTF8(node.textarray, p, c[0]);
                 }
             }
 
             node = node.next;
         }
     }
 
     /**
      * Used to hunt for hidden preformatted sections.
      * @param node checked node
      * @return <code>true</code> if the node has a "margin-top: 0" or "margin-bottom: 0" style
      */
     boolean noMargins(Node node)
     {
         AttVal attval = node.getAttrByName("style");
 
         if (attval == null || attval.value == null)
         {
             return false;
         }
 
         // search for substring "margin-top: 0"
         if (attval.value.indexOf("margin-top: 0") == -1)
         {
             return false;
         }
 
         // search for substring "margin-top: 0"
         if (attval.value.indexOf("margin-bottom: 0") == -1)
         {
             return false;
         }
 
         return true;
     }
 
     /**
      * Does element have a single space as its content?
      * @param lexer Lexer
      * @param node checked node
      * @return <code>true</code> if the element has a single space as its content
      */
     boolean singleSpace(Lexer lexer, Node node)
     {
         if (node.content != null)
         {
             node = node.content;
 
             if (node.next != null)
             {
                 return false;
             }
 
             if (node.type != NodeType.TextNode)
             {
                 return false;
             }
 
             if (((node.end - node.start) == 1) && lexer.lexbuf[node.start] == ' ')
             {
                 return true;
             }
 
             if ((node.end - node.start) == 2)
             {
                 int[] c = new int[1];
 
                 PPrint.getUTF8(lexer.lexbuf, node.start, c);
 
                 if (c[0] == 160)
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * This is a major clean up to strip out all the extra stuff you get when you save as web page from Word 2000. It
      * doesn't yet know what to do with VML tags, but these will appear as errors unless you declare them as new tags,
      * such as o:p which needs to be declared as inline.
      * @param lexer Lexer
      * @param node node to clean up
      */
     public void cleanWord2000(Lexer lexer, Node node)
     {
         // used to a list from a sequence of bulletted p's
         Node list = null;
 
         while (node != null)
         {
 
             // get rid of Word's xmlns attributes
             if (node.is(TagId.HTML))
             {
                 // check that it's a Word 2000 document
                 if ((node.getAttrByName("xmlns:o") == null))
                 {
                     return;
                 }
                 lexer.configuration.tt.freeAttrs(node);
             }
 
             // fix up preformatted sections by looking for a sequence of paragraphs with zero top/bottom margin
             if (node.is(TagId.P))
             {
                 if (noMargins(node))
                 {
                     Node pre;
                     Node next;
                     Node.coerceNode(lexer, node, TagId.PRE, false, true);
 
                     purgeWord2000Attributes(node);
 
                     if (node.content != null)
                     {
                         cleanWord2000(lexer, node.content);
                     }
 
                     pre = node;
                     node = node.next;
 
                     // continue to strip p's
                     while (node != null && node.is(TagId.P) && noMargins(node)) {
                         next = node.next;
                         node.removeNode();
                         pre.insertNodeAtEnd(lexer.newLineNode());
                         pre.insertNodeAtEnd(node);
                         stripSpan(lexer, node);
                         node = next;
                     }
 
                     if (node == null) {
                         break;
                     }
                 }
             }
 
             if (node.tag != null && TidyUtils.toBoolean(node.tag.model & Dict.CM_BLOCK) && singleSpace(lexer, node))
             {
                 node = stripSpan(lexer, node);
                 continue;
             }
 
             // discard Word's style verbiage
             if (node.is(TagId.STYLE) || node.is(TagId.META) || node.type == NodeType.CommentTag)
             {
                 node = Node.discardElement(node);
                 continue;
             }
 
             // strip out all span and font tags Word scatters so liberally!
             if (node.is(TagId.SPAN) || node.is(TagId.FONT))
             {
                 node = stripSpan(lexer, node);
                 continue;
             }
 
             if (node.is(TagId.LINK))
             {
                 AttVal attr = node.getAttrByName("rel");
 
                 if (attr != null && attr.value != null && attr.value.equals("File-List"))
                 {
                     node = Node.discardElement(node);
                     continue;
                 }
             }
             
             /* discards <o:p> which encodes the paragraph mark */
             if (node.tag != null && "o:p".equals(node.tag.name)) {
                 Node[] next = new Node[1];
                 discardContainer(node, next);
                 node = next[0];
                 continue;
             }
 
             // discard empty paragraphs
             if (node.content == null && node.is(TagId.P))
             {
                 node = Node.discardElement(node);
                 continue;
             }
 
             if (node.is(TagId.P))
             {
                 AttVal attr = node.getAttrByName("class");
                 AttVal atrStyle = node.getAttrByName("style");
 
                 // (JES) Sometimes Word marks a list item with the following hokie syntax
                 // <p class="MsoNormal" style="...;mso-list:l1 level1 lfo1;
                 // translate these into <li>
 
                 // map sequence of <p class="MsoListBullet"> to <ul> ... </ul>
                 // map <p class="MsoListNumber"> to <ol>...</ol>
                 if (attr != null
                     && attr.value != null
                     && ((attr.value.equals("MsoListBullet") || attr.value.equals("MsoListNumber")) //
                     || (atrStyle != null && (atrStyle.value.indexOf("mso-list:") != -1)))) // 463066 - fix by Joel
                 // Shafer 19 Sep 01
                 {
                     TagId listType = TagId.UL;
 
                     if (attr.value.equals("MsoListNumber"))
                     {
                         listType = TagId.OL;
                     }
 
                     Node.coerceNode(lexer, node, TagId.LI, false, true);
 
                     if (list == null || !list.is(listType))
                     {
                         list = lexer.inferredTag(listType);
                         Node.insertNodeBeforeElement(node, list);
                     }
 
                     purgeWord2000Attributes(node);
 
                     if (node.content != null)
                     {
                         cleanWord2000(lexer, node.content);
                     }
 
                     // remove node and append to contents of list
                     node.removeNode();
                     list.insertNodeAtEnd(node);
                     node = list;
                 }
                 // map sequence of <p class="Code"> to <pre> ... </pre>
                 else if (attr != null && attr.value != null && attr.value.equals("Code"))
                 {
                     Node br = lexer.newLineNode();
                     normalizeSpaces(lexer, node);
 
                     if (list == null || !list.is(TagId.PRE))
                     {
                         list = lexer.inferredTag(TagId.PRE);
                         Node.insertNodeBeforeElement(node, list);
                     }
 
                     // remove node and append to contents of list
                     node.removeNode();
                     list.insertNodeAtEnd(node);
                     stripSpan(lexer, node);
                     list.insertNodeAtEnd(br);
                     node = list.next;
                 }
                 else
                 {
                     list = null;
                 }
             }
             else
             {
                 list = null;
             }
 
             // strip out style and class attributes
             if (node.type == NodeType.StartTag || node.type == NodeType.StartEndTag)
             {
                 purgeWord2000Attributes(node);
             }
 
             if (node.content != null)
             {
                 cleanWord2000(lexer, node.content);
             }
 
             node = node.next;
         }
     }
 
     /**
      * Check if the current document is a converted Word document.
      * @param root root Node
      * @return <code>true</code> if the document has been geenrated by Microsoft Word.
      */
     public boolean isWord2000(Node root)
     {
         AttVal attval;
         Node node;
         Node head;
         Node html = root.findHTML();
 
         if (html != null && html.getAttrByName("xmlns:o") != null)
         {
             return true;
         }
 
         // search for <meta name="GENERATOR" content="Microsoft ...">
         head = root.findHEAD();
 
         if (head != null)
         {
             for (node = head.content; node != null; node = node.next)
             {
                 if (!node.is(TagId.META))
                 {
                     continue;
                 }
 
                 attval = node.getAttrByName("name");
 
                 if (attval == null || attval.value == null)
                 {
                     continue;
                 }
 
                 if (!"generator".equals(attval.value))
                 {
                     continue;
                 }
 
                 attval = node.getAttrByName("content");
 
                 if (attval == null || attval.value == null)
                 {
                     continue;
                 }
 
                 if (attval.value.indexOf("Microsoft") != -1)
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Where appropriate move object elements from head to body.
      * @param lexer Lexer
      * @param html html node
      */
     static void bumpObject(Lexer lexer, Node html)
     {
         if (html == null)
         {
             return;
         }
 
         Node node, next, head = null, body = null;
         for (node = html.content; node != null; node = node.next)
         {
             if (node.is(TagId.HEAD))
             {
                 head = node;
             }
 
             if (node.is(TagId.BODY))
             {
                 body = node;
             }
         }
 
         if (head != null && body != null)
         {
             for (node = head.content; node != null; node = next)
             {
                 next = node.next;
 
                 if (node.is(TagId.OBJECT))
                 {
                     Node child;
                     boolean bump = false;
 
                     for (child = node.content; child != null; child = child.next)
                     {
                         // bump to body unless content is param
                         if ((child.type == NodeType.TextNode && !node.isBlank(lexer)) || !child.is(TagId.PARAM))
                         {
                             bump = true;
                             break;
                         }
                     }
 
                     if (bump)
                     {
                         node.removeNode();
                         body.insertNodeAtStart(node);
                     }
                 }
             }
         }
     }
     
     /*
       FixLanguageInformation ensures that the document contains (only)
       the attributes for language information desired by the output
       document type. For example, for XHTML 1.0 documents both
       'xml:lang' and 'lang' are desired, for XHTML 1.1 only 'xml:lang'
       is desired and for HTML 4.01 only 'lang' is desired.
     */
     protected static void fixLanguageInformation(final Lexer lexer, Node node,
     		final boolean wantXmlLang, final boolean wantLang) {
         while (node != null) {
             Node next = node.next;
             /* todo: report modifications made here to the report system */
             if (node.isElement()) {
                 AttVal lang = node.getAttrById(AttrId.LANG);
                 AttVal xmlLang = node.getAttrById(AttrId.XML_LANG);
 
                 if (lang != null && xmlLang != null) {
                     /*
                       todo: check whether both attributes are in sync,
                       here or elsewhere, where elsewhere is probably
                       preferable.
                       AD - March 2005: not mandatory according the standards.
                     */
                 }
                 else if (lang != null && wantXmlLang) {
                 	if ((node.getAttributeVersions(AttrId.XML_LANG) & lexer.versionEmitted) != 0) {
                         node.repairAttrValue("xml:lang", lang.value);
                     }
                 }
                 else if (xmlLang != null && wantLang) {
                 	if ((node.getAttributeVersions(AttrId.LANG) & lexer.versionEmitted) != 0) {
                 		node.repairAttrValue("lang", xmlLang.value);
                     }
                 }
                 if (lang != null && !wantLang) {
                 	node.removeAttribute(lang);
                 }
                 if (xmlLang != null && !wantXmlLang) {
                 	node.removeAttribute(xmlLang);
                 }
             }
 
             if (node.content != null) {
                 fixLanguageInformation(lexer, node.content, wantXmlLang, wantLang);
             }
             node = next;
         }
     }
     
     /*
       Set/fix/remove <html xmlns='...'>
     */
     protected static void fixXhtmlNamespace(final Node root, final boolean wantXmlns) {
         Node html = root.findHTML();
         if (html == null) {
             return;
         }
         AttVal xmlns = html.getAttrById(AttrId.XMLNS);
         if (wantXmlns) {
             if (xmlns == null || !xmlns.valueIs(XHTML_NAMESPACE)) {
             	html.repairAttrValue("xmlns", XHTML_NAMESPACE);
             }
         }
         else if (xmlns != null) {
             html.removeAttribute(xmlns);
         }
     }
     
     protected void fixAnchors(final Lexer lexer, Node node, final boolean wantName, final boolean wantId) {
         Node next;
 
         while (node != null) {
             next = node.next;
 
             if (node.isAnchorElement()) {
                 AttVal name = node.getAttrById(AttrId.NAME);
                 AttVal id = node.getAttrById(AttrId.ID);
                 boolean hadName = name != null;
                 boolean hadId = id != null;
                 boolean IdEmitted = false;
                 boolean NameEmitted = false;
 
                 /* todo: how are empty name/id attributes handled? */
 
                 if (name != null && id != null) {
                 	boolean NameHasValue = name.hasValue();
                 	boolean IdHasValue = id.hasValue();
                     if ((NameHasValue != IdHasValue) || (NameHasValue && IdHasValue &&
                         !name.value.equals(id.value))) {
                         lexer.report.attrError(lexer, node, name, ErrorCode.ID_NAME_MISMATCH);
                     }
                 } else if (name != null && wantId) {
                     if ((node.getAttributeVersions(AttrId.ID) & lexer.versionEmitted) != 0) {
                         if (TidyUtils.isValidHTMLID(name.value)) {
                             node.repairAttrValue("id", name.value);
                             IdEmitted = true;
                         } else {
                             lexer.report.attrError(lexer, node, name, ErrorCode.INVALID_XML_ID);
                         }
                      }
                 } else if (id != null && wantName) {
                     if ((node.getAttributeVersions(AttrId.NAME) & lexer.versionEmitted) != 0) {
                         /* todo: do not assume id is valid */
                         node.repairAttrValue("name", id.value);
                         NameEmitted = true;
                     }
                 }
 
                 if (id != null && !wantId
                     /* make sure that Name has been emitted if requested */
                     && (hadName || !wantName || NameEmitted)) {
                     node.removeAttribute(id);
                 }
                 if (name != null && !wantName
                     /* make sure that Id has been emitted if requested */
                     && (hadId || !wantId || IdEmitted)) {
                 	node.removeAttribute(name);
                 }
                 if (node.getAttrById(AttrId.NAME) == null &&
                     node.getAttrById(AttrId.ID) == null) {
                     tt.removeAnchorByNode(node);
                 }
             }
             if (node.content != null) {
                 fixAnchors(lexer, node.content, wantName, wantId);
             }
             node = next;
         }
     }
     
     protected static void wbrToSpace(final Lexer lexer, Node node) {
         Node next;
 
         while (node != null) {
             next = node.next;
 
             if (node.is(TagId.WBR)) {
                 Node text = lexer.newLiteralTextNode(" ");
                 node.insertNodeAfterElement(text);
                 node.removeNode();
                 node = next;
                 continue;
             }
 
             if (node.content != null) {
                 wbrToSpace(lexer, node.content);
             }
             node = next;
         }
     }
 
 	public static void verifyHTTPEquiv(final Lexer lexer, final Node head) {
 	    StyleProp firstProp = null, lastProp = null, prop = null;
 	    final String enc = EncodingNameMapper.toIana(lexer.configuration.getOutCharEncodingName()).toLowerCase();
 
 	    if (enc == null) {
 	        return;
 	    }
 	    if (head == null) {
 	        return;
 	    }
 
 	    /* Find any <meta http-equiv='Content-Type' content='...' /> */
 	    for (Node node = head.content; null != node; node = node.next) {
 	        final AttVal httpEquiv = node.getAttrById(AttrId.HTTP_EQUIV);
 	        final AttVal metaContent = node.getAttrById(AttrId.CONTENT);
 
 	        if (!node.is(TagId.META) || metaContent == null || httpEquiv == null || !httpEquiv.valueIs("Content-Type")) {
 	            continue;
 	        }
 	        if (metaContent.value != null) {
 		        for (String t : metaContent.value.split(";")) {
 		        	prop = new StyleProp(t.trim(), null, null);
 	                if (null != lastProp) {
 	                    lastProp.next = prop;
 	                } else {
 	                    firstProp = prop;
 	                }
 	                lastProp = prop;
 		        }
 	        }
 	        /*  find the charset property */
 	        for (prop = firstProp; null != prop; prop = prop.next) {
 	            if (prop.name.length() < 7 || !"charset".equalsIgnoreCase(prop.name.substring(0, 7))) {
 	                continue;
 	            }
 	            prop.name = "charset=" + enc;
 	            metaContent.value = createPropString(firstProp);
 	            break;
 	        }
 	        firstProp = null;
 	        lastProp = null;
 	    }
 	}
 }

 /*
  * DOMContext.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  *
  * Created on Apr 14, 2003 by marion@users.sourceforge.net
  */
 package net.sourceforge.fraglets.zeig.jndi;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 
 import javax.naming.Binding;
 import javax.naming.CompositeName;
 import javax.naming.CompoundName;
 import javax.naming.Context;
 import javax.naming.InvalidNameException;
 import javax.naming.Name;
 import javax.naming.NameAlreadyBoundException;
 import javax.naming.NameClassPair;
 import javax.naming.NameNotFoundException;
 import javax.naming.NameParser;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.NotContextException;
 import javax.naming.spi.NamingManager;
 
 import org.apache.log4j.Category;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import net.sourceforge.fraglets.zeig.dom.DocumentImpl;
 import net.sourceforge.fraglets.zeig.dom.NamedNodeMapImpl;
 import net.sourceforge.fraglets.zeig.jdbc.ConnectionFactory;
 import net.sourceforge.fraglets.zeig.model.NodeFactory;
 import net.sourceforge.fraglets.zeig.model.PlainTextFactory;
 import net.sourceforge.fraglets.zeig.model.SAXFactory;
 import net.sourceforge.fraglets.zeig.model.VersionFactory;
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.8 $
  */
 public class DOMContext implements Context {
     /** Context option. */
     public static final String VERSION_COMMENT =
         "net.sourceforge.fraglets.zeig.jndi.versionComment";
     
     public static final String CONTEXT_NAMESPACE =
         "http://fraglets.sourceforge.net/zeig/DOMContext";
     
     public static final String CONTEXT_TAGNAME = "context";
     
     public static final String BINDING_TAGNAME = "binding";
     
     private Properties environment;
     private NameParser nameParser;
     private DOMContext parent;
     private Document binding;
     private HashMap context;
     private String atom;
     private int ve;
     
     public DOMContext(Hashtable defaults) throws NamingException {
         init(defaults);
         this.nameParser = new SimpleNameParser(environment);
         this.ve = getRoot();
         try {
             this.binding = new DocumentImpl(VersionFactory.getInstance().getValue(ve));
         } catch (SQLException ex) {
             throw namingException("root not found", ex);
         }
         this.atom = "";
     }
     
     protected DOMContext(DOMContext blueprint) {
         this.environment = new Properties(blueprint.environment);
         this.nameParser = blueprint.nameParser;
         this.binding = blueprint.binding;
         this.parent = blueprint.parent;
         this.atom = blueprint.atom;
         this.ve = blueprint.ve;
     }
     
     protected DOMContext(DOMContext parent, String atom, Document binding, int ve) {
         this.environment = new Properties(parent.environment);
         this.nameParser = parent.nameParser;
         this.binding = binding;
         this.parent = parent;
         this.atom = atom;
         this.ve = ve;
     }
     
     protected void init(Hashtable defaults) {
         environment = new Properties();
         environment.putAll(defaults);
     }
     
     /**
      * @see javax.naming.Context#lookup(javax.naming.Name)
      */
     public Object lookup(Name name) throws NamingException {
         if (name.isEmpty()) {
             // request for copy
             return new DOMContext(this);
         }
 
         Name nm = getComponents(name);
         String atom = nm.get(0);
         Element in = lookupElement(atom);
         
         if (nm.size() == 1) {
             if (in == null) {
                 throw new NameNotFoundException(name.toString());
             }
 
             try {
                 return NamingManager.getObjectInstance
                     (in, new CompositeName().add(atom), this, environment);
             } catch (Exception ex) {
                 throw namingException("getObjectInstance", ex);
             }
         } else {
             return getSubContext(atom).lookup(nm.getSuffix(1));
         }
     }
     
     protected Element lookupElement(String atom) {
         return binding.getElementById(atom);
     }
 
     /**
      * @see javax.naming.Context#lookup(java.lang.String)
      */
     public Object lookup(String name) throws NamingException {
         return lookup(toName(name));
     }
 
     /**
      * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
      */
     public void bind(Name name, Object obj) throws NamingException {
         if (name.isEmpty()) {
             throw new InvalidNameException("empty name");
         }
 
         // Extract components that belong to this namespace
         Name nm = getComponents(name);
         String atom = nm.get(0);
         Element in = lookupElement(atom);
 
         if (nm.size() == 1) {
             // Atomic name: Find object in internal data structure
             if (in != null) {
                 throw new NameAlreadyBoundException(name.toString());
             }
 
             rebind(obj, in, atom);
         } else {
             if (in == null) {
                 throw new NamingException("no subcontext: "+atom);
             }
             getSubContext(atom).bind(nm.getSuffix(1), obj);
         }
     }
     
     /**
      * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
      */
     public void bind(String name, Object obj) throws NamingException {
         bind(toName(name), obj);
     }
 
     /**
      * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
      */
     public void rebind(Name name, Object obj) throws NamingException {
         if (name.isEmpty()) {
             throw new InvalidNameException("empty name");
         }
 
         // Extract components that belong to this namespace
         Name nm = getComponents(name);
         String atom = nm.get(0);
         Element in = lookupElement(atom);
 
         if (nm.size() == 1) {
             rebind(obj, in, atom);
         } else {
             getSubContext(atom).rebind(nm.getSuffix(1), obj);
         }
     }
 
     /**
      * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
      */
     public void rebind(String name, Object obj) throws NamingException {
         rebind(toName(name), obj);
     }
 
     /**
      * @see javax.naming.Context#unbind(javax.naming.Name)
      */
     public void unbind(Name name) throws NamingException {
         if (name.isEmpty()) {
             throw new InvalidNameException("empty name");
         }
 
         // Extract components that belong to this namespace
         Name nm = getComponents(name);
         String atom = nm.get(0);
         Element in = lookupElement(atom);
 
         if (nm.size() == 1) {
             binding.getDocumentElement().removeChild(in);
             // just in case it's a context
             if (context != null) {
                 context.remove(atom);
             }
         } else {
             getSubContext(atom).unbind(nm.getSuffix(1));
         }
     }
 
     /**
      * @see javax.naming.Context#unbind(java.lang.String)
      */
     public void unbind(String name) throws NamingException {
         unbind(toName(name));
     }
 
     /**
      * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
      */
     public void rename(Name oldName, Name newName) throws NamingException {
         if (oldName.isEmpty() && newName.isEmpty()) {
             throw new InvalidNameException("empty name");
         }
 
         // Just bind and unbind, in that order. Note that this creates a new
         // version element, just like in CVS.
         Object obj = lookup(oldName);
         bind(newName, obj);
         unbind(oldName);
     }
 
     /**
      * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
      */
     public void rename(String oldName, String newName) throws NamingException {
         rename(toName(oldName), toName(newName));
     }
 
     /**
      * @see javax.naming.Context#list(javax.naming.Name)
      */
     public NamingEnumeration list(Name name) throws NamingException {
         if (name.isEmpty()) {
             // listing this context
             return new DOMNames(binding.getDocumentElement());
         } 
 
         // Perhaps 'name' names a context
         Object target = lookup(name);
         if (target instanceof Context) {
             try {
                 return ((Context)target).list("");
             } finally {
                 ((Context)target).close();
             }
         } else {
             throw new NotContextException(name.toString());
         }
     }
 
     /**
      * @see javax.naming.Context#list(java.lang.String)
      */
     public NamingEnumeration list(String name) throws NamingException {
         return list(toName(name));
     }
 
     /**
      * @see javax.naming.Context#listBindings(javax.naming.Name)
      */
     public NamingEnumeration listBindings(Name name) throws NamingException {
         if (name.isEmpty()) {
             // listing this context
             return new DOMBindings(binding.getDocumentElement(), this);
         } 
 
         // Perhaps 'name' names a context
         Object target = lookup(name);
         if (target instanceof Context) {
             try {
                 return ((Context)target).listBindings("");
             } finally {
                 ((Context)target).close();
             }
         } else {
             throw new NotContextException(name.toString());
         }
     }
 
     /**
      * @see javax.naming.Context#listBindings(java.lang.String)
      */
     public NamingEnumeration listBindings(String name) throws NamingException {
         return listBindings(toName(name));
     }
 
     /**
      * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
      */
     public void destroySubcontext(Name name) throws NamingException {
         unbind(name);
     }
 
     /**
      * @see javax.naming.Context#destroySubcontext(java.lang.String)
      */
     public void destroySubcontext(String name) throws NamingException {
         destroySubcontext(toName(name));
     }
 
     /**
      * @see javax.naming.Context#createSubcontext(javax.naming.Name)
      */
     public Context createSubcontext(Name name) throws NamingException {
         if (name.isEmpty()) {
             throw new InvalidNameException("empty name");
         }
 
         // Extract components that belong to this namespace
         Name nm = getComponents(name);
         String atom = nm.get(0);
         Element in = lookupElement(atom);
 
         if (nm.size() == 1) {
             rebind(new DocumentImpl(getEmpty()), in, atom);
             return getSubContext(atom);
         } else {
             return getSubContext(atom).createSubcontext(nm.getSuffix(1));
         }
     }
 
     /**
      * @see javax.naming.Context#createSubcontext(java.lang.String)
      */
     public Context createSubcontext(String name) throws NamingException {
         return createSubcontext(toName(name));
     }
 
     /**
      * @see javax.naming.Context#lookupLink(javax.naming.Name)
      */
     public Object lookupLink(Name name) throws NamingException {
         return lookup(name);
     }
 
     /**
      * @see javax.naming.Context#lookupLink(java.lang.String)
      */
     public Object lookupLink(String name) throws NamingException {
         return lookupLink(toName(name));
     }
 
     /**
      * @see javax.naming.Context#getNameParser(javax.naming.Name)
      */
     public NameParser getNameParser(Name name) throws NamingException {
         return nameParser;
     }
 
     /**
      * @see javax.naming.Context#getNameParser(java.lang.String)
      */
     public NameParser getNameParser(String name) throws NamingException {
         return getNameParser(toName(name));
     }
 
     /**
      * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
      */
     public Name composeName(Name name, Name prefix) throws NamingException {
         Name result = (Name)prefix.clone();
         return result.addAll(name);
     }
 
     /**
      * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
      */
     public String composeName(String name, String prefix) throws NamingException {
             return composeName(toName(name), toName(prefix)).toString();
     }
 
     /**
      * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
      */
     public Object addToEnvironment(String propName, Object propVal) throws NamingException {
         return environment.put(propName, propVal);
     }
 
     /**
      * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
      */
     public Object removeFromEnvironment(String propName) throws NamingException {
         return environment.remove(propName);
     }
 
     /**
      * @see javax.naming.Context#getEnvironment()
      */
     public Hashtable getEnvironment() throws NamingException {
         return new Properties(environment);
     }
 
     /**
      * @see javax.naming.Context#close()
      */
     public void close() throws NamingException {
         // easy
     }
 
     /**
      * @see javax.naming.Context#getNameInNamespace()
      */
     public String getNameInNamespace() throws NamingException {
         if (parent == null) {
             return "";
         }
         
         DOMContext up = this;
         Name name = nameParser.parse("");
         while (up != null && up.atom != null) {
             name.add(0, up.atom);
         }
         
         return name.toString();
     }
     
     protected void rebind(Object obj, Element old, String atom) throws NamingException {
         // Call getStateToBind for using any state factories
         obj = NamingManager.getStateToBind(obj,
             new CompositeName().add(atom), this, environment);
         if (obj != old) {
             try {
                binding.getDocumentElement().appendChild((Node)obj);
                 VersionFactory.getInstance()
                     .addVersion(ve, ((DocumentImpl)binding).getId(), 0);
             } catch (SQLException ex) {
                 throw namingException(ex);
             }
         }
     }
 
     protected DOMContext getSubContext(String atom) throws NumberFormatException, DOMException, NamingException {
         if (context == null) {
             context = new HashMap();
         }
         DOMContext sub = (DOMContext)context.get(atom);
         if (sub == null) {
             sub = (DOMContext)lookup(atom);
             context.put(atom, sub);
         }
         return sub;
     }
 
     protected Name getComponents(Name name) throws NamingException {
         if (name instanceof CompositeName) {
             // All components are eligible, we're terminal for the moment
             return nameParser.parse(name.toString());
         } else {
             // Already parsed
             return name;
         }
     }
     
     protected Name toName(String name) throws InvalidNameException {
         return new CompositeName(name);
     }
     
     protected Document getBinding() {
         return binding;
     }
     
     protected int getRoot() throws NamingException {
         try {
             try {
                 // lookup root
                 return VersionFactory.getInstance().getVersions(getEmpty())[0];
             } catch (ArrayIndexOutOfBoundsException ex) {
                 int root = getEmpty();
                 int comment = PlainTextFactory.getInstance()
                     .getPlainText("naming root");
                 return VersionFactory.getInstance().createVersion(root, comment);
             }
         } catch (NamingException ex) {
             throw ex;
         } catch (Exception ex) {
             throw namingException(ex);
         }
     }
     
     private static int emptyId;
     
     public static int getEmpty() throws NamingException {
         if (emptyId != 0) {
             return emptyId;
         }
         
         try  {
             SAXFactory sf = new SAXFactory(ConnectionFactory.getInstance());
             return emptyId = sf.parse("<ctx:"+CONTEXT_TAGNAME+" xmlns:ctx=\""+CONTEXT_NAMESPACE+"\"/>");
         } catch (Exception ex) {
             throw namingException(ex);
         }
     }
     
     public static int getLatest(int ve) throws NamingException {
         try {
             return VersionFactory.getInstance().getValue(ve);
         } catch (SQLException ex) {
             throw namingException(ex);
         }
     }
     
     private static int veTag;
     
     public static int getVe(Element el) throws NamingException {
         if (veTag == 0) {
             try {
                 veTag = NodeFactory.getInstance().getName(CONTEXT_NAMESPACE, "ve");
             } catch (SQLException ex) {
                 throw namingException(ex);
             }
         }
         
         return Integer.parseInt
             (((NamedNodeMapImpl)el.getAttributes())
                 .getNamedItem(veTag).getNodeValue());
     }
     
     private static int idTag;
     
     public static int getId() throws NamingException {
         if (idTag == 0) {
             try {
                 idTag = NodeFactory.getInstance().getName("", "id");
             } catch (SQLException ex) {
                 throw namingException(ex);
             }
         }
         return idTag;
     }
     
     public static NamingException namingException(String message, Throwable t) {
         NamingException result = new NamingException(message);
         result.setRootCause(t);
         return result;
     }
     
     public static NamingException namingException(Throwable t) {
         NamingException result = new NamingException();
         result.setRootCause(t);
         return result;
     }
     
     public static class DOMNames implements NamingEnumeration {
         private int idTag;
         private int index;
         private NodeList nl;
         
         public DOMNames(Node node) throws NamingException {
             this.nl = node.getChildNodes();
             this.index = 0;
             this.idTag = getId();
         }
 
         /**
          * @see javax.naming.NamingEnumeration#next()
          */
         public Object next() throws NamingException {
             if (index < nl.getLength()) {
                 Element e = (Element)nl.item(index++);
                 NamedNodeMapImpl nn = (NamedNodeMapImpl)e.getAttributes();
                 String name = nn.getNamedItem(idTag).getNodeValue();
                 String type = DOMObjectFactory.isDOMContext(e)
                     ? DOMContext.class.getName()
                     : Document.class.getName();
                 return new NameClassPair(name, type);
             } else {
                 throw new NoSuchElementException();
             }
         }
 
         /**
          * @see javax.naming.NamingEnumeration#hasMore()
          */
         public boolean hasMore() {
             return index < nl.getLength();
         }
 
         /**
          * @see javax.naming.NamingEnumeration#close()
          */
         public void close() {
             // easy
         }
 
         /**
          * @see java.util.Enumeration#hasMoreElements()
          */
         public boolean hasMoreElements() {
             return hasMore();
         }
 
         /**
          * @see java.util.Enumeration#nextElement()
          */
         public Object nextElement() {
             try {
                 return next();
             } catch (NamingException ex) {
                 throw new NoSuchElementException(ex.toString());
             }
         }
     }
     
     public static class DOMBindings extends DOMNames {
         private DOMContext ctx;
         
         /**
          * @param node
          * @throws NamingException
          */
         public DOMBindings(Node node, DOMContext ctx) throws NamingException {
             super(node);
             this.ctx = ctx;
         }
         
         /**
          * @see javax.naming.NamingEnumeration#next()
          */
         public Object next() throws NamingException {
             String name = ((NameClassPair)super.next()).getName();
             return new Binding(name, ctx.lookup(name));
         }
 
     }
     
     public static class SimpleNameParser implements NameParser {
         
         public static final String SYNTAX_PREFIX =
             "net.sourceforge.fraglets.zeig.jndi.syntax.";
         
         public static final int LENGTH_PREFIX =
             SYNTAX_PREFIX.length() - "jndi.syntax.".length();
         
         private Properties syntax;
         
         public SimpleNameParser(Properties defaults) {
             init(defaults);
         }
         
         protected void init(Properties defaults) {
             Category.getInstance(SimpleNameParser.class)
                 .debug("loading syntax");
             InputStream in = SimpleNameParser.class
                 .getResourceAsStream("jndiprovider.properties");
             try {
                 defaults = new Properties(defaults);
                 defaults.load(in);
             } catch (IOException ex) {
                 Category.getInstance(SimpleNameParser.class)
                     .error("loading syntax", ex);
             } finally {
                 try { in.close(); } catch (IOException ex) {}
             }
             syntax = new Properties();
             for (Enumeration e = defaults.propertyNames(); e.hasMoreElements();) {
                 String key = e.nextElement().toString();
                 if (key.startsWith(SYNTAX_PREFIX)) {
                     syntax.setProperty(key.substring(LENGTH_PREFIX),
                         defaults.getProperty(key));
                 } else {
                     Category.getInstance(SimpleNameParser.class)
                         .debug("ignore property: "+key+"="+defaults.getProperty(key));
                 }
             }
             Category.getInstance(SimpleNameParser.class)
                 .debug("syntax: "+syntax);
         }
         
         /**
          * @see javax.naming.NameParser#parse(java.lang.String)
          */
         public Name parse(String name) throws NamingException {
             Category.getInstance(SimpleNameParser.class)
                 .debug("parsing name '"+name+"'");
             return new CompoundName(name, syntax);
         }
     }
 }

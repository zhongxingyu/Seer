 /*
  * zeigURLContext.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  *
  * Created on May 18, 2003 by unknown
  */
 package net.sourceforge.fraglets.zeig;
 
 import java.util.Hashtable;
 
 import javax.naming.CannotProceedException;
 import javax.naming.CompositeName;
 import javax.naming.Context;
 import javax.naming.InvalidNameException;
 import javax.naming.Name;
 import javax.naming.NameParser;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.spi.NamingManager;
 
 import net.sourceforge.fraglets.zeig.jdbc.ConnectionFactory;
 import net.sourceforge.fraglets.zeig.jndi.DOMContext;
 
 import org.apache.xml.utils.URI;
 
 /**
  * @author unknown
  *
  * To change this generated comment go to 
  * Window>Preferences>Java>Code Generation>Code Template
  */
 public class zeigURLContext implements Context {
     private Hashtable env;
 
     /**
      * @param env
      */
     public zeigURLContext(Hashtable env) {
         this.env = new Hashtable();
         this.env.putAll(env);
     }
 
     /**
      * @see javax.naming.Context#lookup(javax.naming.Name)
      */
     public Object lookup(Name name) throws NamingException {
         Object first = lookup(name.get(0));
         if (name.size() > 1) {
             Context ctx = getContinuationContext(first);
             try {
                 return ctx.lookup(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         } else {
             return first;
         }
     }
 
     /**
      * @see javax.naming.Context#lookup(java.lang.String)
      */
     public Object lookup(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             return ctx.lookup(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
      */
     public void bind(Name name, Object obj) throws NamingException {
         if (name.size() == 1) {
             bind(name.get(0), obj);
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 ctx.bind(name.getSuffix(1), obj);
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
      */
     public void bind(String name, Object obj) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             ctx.bind(getRemainingName(uri), obj);
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
      */
     public void rebind(Name name, Object obj) throws NamingException {
         if (name.size() == 1) {
             rebind(name.get(0), obj);
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 ctx.rebind(name.getSuffix(1), obj);
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
      */
     public void rebind(String name, Object obj) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             ctx.rebind(getRemainingName(uri), obj);
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#unbind(javax.naming.Name)
      */
     public void unbind(Name name) throws NamingException {
         if (name.size() == 1) {
             unbind(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 ctx.unbind(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#unbind(java.lang.String)
      */
     public void unbind(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             ctx.unbind(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
      */
     public void rename(Name oldName, Name newName) throws NamingException {
         if (oldName.size() == 1 && newName.size() == 1) {
             rename(oldName.get(0), newName.get(0));
         } else {
             String oldAtom = oldName.get(0);
             Context oldCtx = getContinuationContext(lookup(oldAtom));
             try {
                 if (oldAtom.equals(newName.get(0))) {
                     oldCtx.rename(oldName.getSuffix(1), newName.getSuffix(1));
                 } else {
                     Context newCtx = getContinuationContext(lookup(newName.get(0)));
                     try {
                         newCtx.bind(newName.getSuffix(1), oldCtx.lookup(oldName.getSuffix(1)));
                         oldCtx.unbind(oldName.getSuffix(1));
                     } finally {
                         newCtx.close();
                     }
                 }
             } finally {
                 oldCtx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
      */
     public void rename(String oldName, String newName) throws NamingException {
         URI oldUri = toUri(oldName);
         URI newUri = toUri(newName);
         if (equalsRoots(oldUri, newUri)) {
             Context ctx = getRootContext(oldUri);
             try {
                 ctx.rename(getRemainingName(oldUri), getRemainingName(newUri));
             } finally {
                 ctx.close();
             }
         } else {
             bind(newName, lookup(oldName));
             unbind(oldName);
         }
     }
 
     /**
      * @see javax.naming.Context#list(javax.naming.Name)
      */
     public NamingEnumeration list(Name name) throws NamingException {
         if (name.size() == 1) {
             return list(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 return ctx.list(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#list(java.lang.String)
      */
     public NamingEnumeration list(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             return ctx.list(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#listBindings(javax.naming.Name)
      */
     public NamingEnumeration listBindings(Name name) throws NamingException {
         if (name.size() == 1) {
             return listBindings(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 return ctx.listBindings(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#listBindings(java.lang.String)
      */
     public NamingEnumeration listBindings(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             return ctx.listBindings(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
      */
     public void destroySubcontext(Name name) throws NamingException {
         if (name.size() == 1) {
             destroySubcontext(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 ctx.destroySubcontext(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#destroySubcontext(java.lang.String)
      */
     public void destroySubcontext(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             ctx.destroySubcontext(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#createSubcontext(javax.naming.Name)
      */
     public Context createSubcontext(Name name) throws NamingException {
         if (name.size() == 1) {
             return createSubcontext(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 return ctx.createSubcontext(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#createSubcontext(java.lang.String)
      */
     public Context createSubcontext(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             return ctx.createSubcontext(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#lookupLink(javax.naming.Name)
      */
     public Object lookupLink(Name name) throws NamingException {
         if (name.size() == 1) {
             return lookupLink(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 return ctx.lookupLink(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /**
      * @see javax.naming.Context#lookupLink(java.lang.String)
      */
     public Object lookupLink(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             return ctx.lookupLink(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#getNameParser(javax.naming.Name)
      */
     public NameParser getNameParser(Name name) throws NamingException {
         if (name.size() == 1) {
             return getNameParser(name.get(0));
         } else {
             Context ctx = getContinuationContext(lookup(name.get(0)));
             try {
                 return ctx.getNameParser(name.getSuffix(1));
             } finally {
                 ctx.close();
             }
         }
     }
 
     /* (non-Javadoc)
      * @see javax.naming.Context#getNameParser(java.lang.String)
      */
     public NameParser getNameParser(String name) throws NamingException {
         URI uri = toUri(name);
         Context ctx = getRootContext(uri);
         try {
             return ctx.getNameParser(getRemainingName(uri));
         } finally {
             ctx.close();
         }
     }
 
     /**
      * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
      */
     public Name composeName(Name name, Name prefix) throws NamingException {
         return ((Name)prefix.clone()).addAll(name);
     }
 
     /**
      * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
      */
     public String composeName(String name, String prefix) throws NamingException {
         if (prefix.length() == 0) {
             return name;
         } else if (name.length() == 0) {
             return prefix;
         } else {
             return prefix + '/' + name;
         }
     }
 
     /**
      * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
      */
     public Object addToEnvironment(String propName, Object propVal) throws NamingException {
         return env.put(propName, propVal);
     }
 
     /**
      * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
      */
     public Object removeFromEnvironment(String propName) throws NamingException {
         return env.remove(propName);
     }
 
     /**
      * @see javax.naming.Context#getEnvironment()
      */
     public Hashtable getEnvironment() throws NamingException {
         return (Hashtable)env.clone();
     }
 
     /**
      * @see javax.naming.Context#close()
      */
     public void close() throws NamingException {
         // no resources to free
     }
 
     /**
      * @see javax.naming.Context#getNameInNamespace()
      */
     public String getNameInNamespace() throws NamingException {
         return "";
     }
     
     public boolean isValidScheme(String scheme) {
         return scheme.equals("zeig");
     }
     
     public boolean equalsRoots(URI oldUri, URI newUri) {
         return oldUri.getScheme().equals(newUri.getScheme())
             && oldUri.getHost().equals(newUri.getHost())
             && oldUri.getUserinfo().equals(newUri.getUserinfo())
             && oldUri.getPort() == newUri.getPort();
     }
     
     /**
      * Get the remaining name for the path of the given <var>uri</var>.
      * @param uri the uri for which to get the remaining name
      * @return the name
      * @throws InvalidNameException
      */
     protected Name getRemainingName(URI uri) throws InvalidNameException {
         String remainingPath = uri.getPath(true, true);
         CompositeName result = new CompositeName();
        if (remainingPath.startsWith("/")) {
            result.add(remainingPath.substring(1));
        } else {
            result.add(remainingPath); // unlikely
         }
         return result;
     }
     
     /**
      * Get the root context for a given url.
      * @param url the url for which to resolve the root context
      * @param remaining value-result for the remaining name part
      * @return the root context
      * @throws NamingException
      */
     protected Context getRootContext(URI uri) throws NamingException {
         String connectionURL = ConnectionFactory.getConnectionURL
             (uri.getHost(), uri.getPort(), uri.getUserinfo());
         Hashtable subEnv = new Hashtable(env);
         subEnv.put(ConnectionFactory.RESOURCE_CONNECTION_URL, connectionURL);
         return new DOMContext(subEnv);
     }
     
     /**
      * Convert a string specification to a URI valid in this context.
      * @param spec the URI specification as a string
      * @return the URI in this context
      * @throws NamingException
      */
     protected URI toUri(String spec) throws NamingException {
         try {
             URI uri = new URI(spec);
             if (!uri.isGenericURI()) {
                 throw new NamingException("invalid URI: "+spec);
             }
             if (!isValidScheme(uri.getScheme())) {
                 throw new NamingException("invalid URI scheme: "+spec);
             }
             return uri;
         } catch (URI.MalformedURIException ex) {
             throw new NamingException(ex.toString());
         }
     }
 
     /**
      * Get the continuation context for the junction at <var>where</var>.
      * @param where
      * @return
      * @throws NamingException
      */
     protected Context getContinuationContext(Object where) throws NamingException {
         return NamingManager
             .getContinuationContext(cannotProceedException(where));
     }
 
     /**
      * Create a CannotProceedException for a resolved object at where.
      * @param where the resolved object where to continue
      * @return the created exception
      */
     protected CannotProceedException cannotProceedException(Object where) {
         CannotProceedException result = new CannotProceedException();
         result.setResolvedObj(where);
         result.setEnvironment(env);
         return result;
     }
 }

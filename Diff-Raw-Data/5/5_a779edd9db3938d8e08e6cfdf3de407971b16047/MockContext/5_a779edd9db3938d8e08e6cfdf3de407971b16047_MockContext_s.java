 /*
  * Copyright 2013 Brian Matthews
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.btmatthews.mockjndi;
 
 import javax.naming.*;
 import javax.naming.spi.ObjectFactory;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
  * @since 1.0.0
  */
 public final class MockContext implements Context {
 
     private final String fullName;
     private final String name;
     private final NameParser nameParser;
     private final Map<String, MockContext> subContexts = new HashMap<String, MockContext>();
     private final Map<String, MockBinding> bindings = new HashMap<String, MockBinding>();
     private final Hashtable<String, Object> environment = new Hashtable<String, Object>();
 
     MockContext(final String fullName,
                 final String name,
                 final NameParser nameParser) {
         this.fullName = fullName;
         this.name = name;
         this.nameParser = nameParser;
     }
 
     boolean addMockContext(final String name, final MockContext context) {
         if (subContexts.containsKey(name) || bindings.containsKey(name)) {
             return false;
         } else {
             subContexts.put(name, context);
             return true;
         }
     }
 
     boolean addBinding(final MockBinding binding) {
         if (subContexts.containsKey(name) || bindings.containsKey(name)) {
             return false;
         } else {
             bindings.put(binding.getName(), binding);
             return true;
         }
     }
 
     @Override
     public Object lookup(final Name name)
             throws NamingException {
         if (name != null) {
             if (name.isEmpty()) {
                 try {
                     return clone();
                 } catch (final CloneNotSupportedException e) {
                     throw new NamingException();
                 }
             } else {
                 return visitContext(name.getPrefix(name.size() - 1), new MockContextVisitor<Object>() {
                     @Override
                     public Object visit(final MockContext context) throws NamingException {
                         final String key = name.get(name.size() - 1);
                         if (context.bindings.containsKey(key)) {
                             return context.bindings.get(key).getBoundObject();
                         } else if (context.subContexts.containsKey(key)) {
                             return context.subContexts.get(key);
                         } else {
                             throw new NameNotFoundException();
                         }
                     }
                 });
             }
         } else {
             throw new InvalidNameException();
         }
     }
 
     @Override
     public Object lookup(final String name)
             throws NamingException {
         return lookup(nameParser.parse(name));
     }
 
     /**
      * Bind the object {@code obj} to the name {@code name}.
      *
      * @param name The name to bind, may not be empty.
      * @param obj  The object to be bound, may be {@code null}.
      * @throws NamingException
      */
     @Override
     public void bind(final Name name,
                      final Object obj) throws NamingException {
         if (name != null && name.size() > 0) {
             visitContext(name.getPrefix(name.size() - 1), new MockContextVisitor<Object>() {
                 @Override
                 public Object visit(final MockContext context) throws NamingException {
                     final String key = name.get(name.size() - 1);
                     if (context.subContexts.containsKey(key) || context.bindings.containsKey(key)) {
                         throw new NameAlreadyBoundException();
                     }
                     if (obj instanceof ObjectFactory) {
                         bindings.put(key, new ObjectFactoryBinding(key, (ObjectFactory) obj));
                     } else {
                         bindings.put(key, new ObjectBinding(key, obj));
                     }
                     return null;
                 }
             });
         } else {
             throw new InvalidNameException();
         }
     }
 
     @Override
     public void bind(final String name,
                      final Object obj)
             throws NamingException {
         bind(nameParser.parse(name), obj);
     }
 
     @Override
     public void rebind(final Name name,
                        final Object obj)
             throws NamingException {
         visitContext(name.getPrefix(name.size() - 1), new MockContextVisitor<Object>() {
             @Override
             public Object visit(final MockContext context) throws NamingException {
                 final String key = name.get(name.size() - 1);
                 if (context.subContexts.containsKey(key)) {
                     context.subContexts.remove(key).close();
                 } else if (!context.bindings.containsKey(key)) {
                     throw new NameNotFoundException();
                 }
                 if (obj instanceof ObjectFactory) {
                     context.bindings.put(key, new ObjectFactoryBinding(key, (ObjectFactory) obj));
                 } else {
                     context.bindings.put(key, new ObjectBinding(key, obj));
                 }
                 return null;
             }
         });
     }
 
     @Override
     public void rebind(final String name,
                        final Object obj)
             throws NamingException {
         rebind(nameParser.parse(name), obj);
     }
 
     @Override
     public void unbind(final Name name)
             throws NamingException {
         if (name != null && !name.isEmpty()) {
             visitContext(name.getPrefix(name.size() - 1), new MockContextVisitor<Object>() {
                 @Override
                 public Object visit(final MockContext context) throws NamingException {
                     final String key = name.get(name.size() - 1);
                     if (context.bindings.containsKey(key)) {
                         context.bindings.remove(key);
                     } else {
                         throw new NameNotFoundException();
                     }
                     return null;
                 }
             });
         } else {
             throw new InvalidNameException();
         }
     }
 
     @Override
     public void unbind(String name)
             throws NamingException {
         unbind(nameParser.parse(name));
     }
 
     @Override
     public void rename(final Name oldName,
                        final Name newName)
             throws NamingException {
         if (oldName != null && oldName != null && newName != null && !newName.isEmpty()) {
             if (!oldName.equals(newName)) {
                 final Name oldRoot = oldName.getPrefix(oldName.size() - 1);
                 final String oldKey = oldName.get(oldName.size() - 1);
                 final Name newRoot = newName.getPrefix(newName.size() - 1);
                 final String newKey = newName.get(newName.size() - 1);
                 visitContext(newRoot, new MockContextVisitor<Object>() {
                     @Override
                     public Object visit(final MockContext newContext) throws NamingException {
                         if (!newContext.bindings.containsKey(newKey) && !newContext.subContexts.containsKey(newKey)) {
                             visitContext(oldRoot, new MockContextVisitor<Object>() {
                                 @Override
                                 public Object visit(final MockContext oldContext) throws NamingException {
                                     if (oldContext.bindings.containsKey(oldKey)) {
                                         final MockBinding obj = oldContext.bindings.remove(oldKey);
                                         newContext.bindings.put(newKey, obj);
                                     } else if (oldContext.subContexts.containsKey(oldKey)) {
                                         final MockContext obj = oldContext.subContexts.remove(oldKey);
                                         newContext.subContexts.put(newKey, obj);
                                     } else {
                                         throw new NameNotFoundException();
                                     }
                                     return null;
                                 }
                             });
                         } else {
                             throw new NameAlreadyBoundException();
                         }
                         return null;
                     }
                 });
             }
         } else {
             throw new InvalidNameException();
         }
     }
 
     @Override
     public void rename(final String oldName,
                        final String newName)
             throws NamingException {
         rename(nameParser.parse(oldName), nameParser.parse(newName));
     }
 
     @Override
     public NamingEnumeration<NameClassPair> list(final Name name)
             throws NamingException {
         throw new UnsupportedOperationException("list(Name) is not currently supported by MockJNDI");
     }
 
     @Override
     public NamingEnumeration<NameClassPair> list(final String name)
             throws NamingException {
         throw new UnsupportedOperationException("list(String) is not currently supported by MockJNDI");
     }
 
     @Override
     public NamingEnumeration<Binding> listBindings(final Name name)
             throws NamingException {
         throw new UnsupportedOperationException("listBindings(Name) is not currently supported by MockJNDI");
     }
 
     @Override
     public NamingEnumeration<Binding> listBindings(final String name)
             throws NamingException {
         throw new UnsupportedOperationException("listBindings(String) is not currently supported by MockJNDI");
     }
 
     @Override
     public void destroySubcontext(final Name name)
             throws NamingException {
         if (name != null && !name.isEmpty()) {
             visitContext(name.getPrefix(name.size() - 1), new MockContextVisitor<Object>() {
                 @Override
                 public Object visit(final MockContext context) throws NamingException {
                     final String key = name.get(name.size() - 1);
                     if (context.subContexts.containsKey(key)) {
                         context.subContexts.remove(key).close();
                         return null;
                     } else {
                         throw new NameNotFoundException();
                     }
                 }
             });
         } else {
             throw new InvalidNameException();
         }
     }
 
     @Override
     public void destroySubcontext(final String name)
             throws NamingException {
         destroySubcontext(nameParser.parse(name));
     }
 
     @Override
     public Context createSubcontext(final Name name)
             throws NamingException {
         if (name != null && name.size() > 0) {
             return visitContext(name.getPrefix(name.size() - 1), new MockContextVisitor<Context>() {
                 @Override
                 public Context visit(MockContext context) throws NamingException {
                     final String key = name.get(name.size() - 1);
                     if (context.bindings.containsValue(key) || context.subContexts.containsValue(key)) {
                         throw new NameAlreadyBoundException();
                     }
                     final MockContext newContext = new MockContext(name.toString(), key, nameParser);
                     context.subContexts.put(key, newContext);
                     return newContext;
                 }
             });
         } else {
             throw new InvalidNameException();
         }
     }
 
     @Override
     public Context createSubcontext(final String name)
             throws NamingException {
         return createSubcontext(nameParser.parse(name));
     }
 
     @Override
     public Object lookupLink(final Name name)
             throws NamingException {
         return lookup(name);
     }
 
     @Override
     public Object lookupLink(final String name)
             throws NamingException {
         return lookupLink(nameParser.parse(name));
     }
 
     @Override
     public NameParser getNameParser(final Name name)
             throws NamingException {
         return visitContext(name, new MockContextVisitor<NameParser>() {
             @Override
             public NameParser visit(final MockContext context) throws NamingException {
                return context.getNameParser(name.getSuffix(1));
             }
         });
     }
 
     /**
      *
      * @param name
      * @return
      * @throws NamingException
      */
     @Override
     public NameParser getNameParser(final String name)
             throws NamingException {
         return getNameParser(nameParser.parse(name));
     }
 
     /**
      * Concatenate two names.
      *
      * @param name   The value to be appended to the prefix.
      * @param prefix The value of the name root.
      * @return The result of the concatenation.
      * @throws NamingException If there was a problem concatenating the names.
      */
     @Override
     public Name composeName(final Name name,
                             final Name prefix)
             throws NamingException {
         return ((Name) prefix.clone()).addAll(name);
     }
 
     /**
      * Concatenate two names.
      *
      * @param name   The value to be appended to the prefix.
      * @param prefix The value of the name root.
      * @return The result of the concatenation.
      * @throws NamingException If there was a problem concatenating the names.
      */
     @Override
     public String composeName(final String name,
                               final String prefix)
             throws NamingException {
         return composeName(nameParser.parse(name), nameParser.parse(prefix)).toString();
     }
 
     /**
      * Add an environment setting.
      *
      * @param propName The environment setting name.
      * @param propVal  The environment setting value.
      * @return The previous value of environment setting or {@code null} if the environment setting is new.
      * @throws NamingException If there was a problem adding the environment setting.
      */
     @Override
     public Object addToEnvironment(final String propName,
                                    final Object propVal)
             throws NamingException {
         return environment.put(propName, propVal);
     }
 
     /**
      * Remove an environment setting.
      *
      * @param propName The environment setting value.
      * @return The previous value of the environment setting or {@code null} if the environment setting did not exist.
      * @throws NamingException If there was a problem removing the environment setting.
      */
     @Override
     public Object removeFromEnvironment(final String propName)
             throws NamingException {
         return environment.remove(propName);
     }
 
     /**
      * Get the environment settings associated with the naming context.
      *
      * @return A {@link Hashtable} containing the environment settings.
      * @throws NamingException If there was a problem getting the environment settings.
      */
     @Override
     public Hashtable<?, ?> getEnvironment() throws
             NamingException {
         return environment;
     }
 
     /**
      * Close the naming context.
      *
      * @throws NamingException If there was problem closing the naming context.
      */
     @Override
     public void close() throws NamingException {
     }
 
     /**
      * Get the full name of the naming context.
      *
      * @return The full name of the naming context.
      * @throws NamingException If there was a problem getting the full name of the naming context.
      */
     @Override
     public String getNameInNamespace() throws NamingException {
         return fullName;
     }
 
     /**
      * Recursively descend down through the context namespace to match the context named {@code name} and
      * then invoke a callback.
      *
      * @param name    The name to be matched.
      * @param visitor The callback invoked by the visitor when the context is matched..
      * @param <T>     The return type of the visitor callback.
      * @return The response from the visitor callback.
      * @throws NamingException If there was an exception matching the context or executing the callback.
      */
     private <T> T visitContext(final Name name, final MockContextVisitor<T> visitor) throws NamingException {
         if (name == null || name.isEmpty()) {
             return visitor.visit(this);
         }
         final String key = name.get(0);
         final MockContext subContext = subContexts.get(key);
         if (subContext == null) {
             if (bindings.containsKey(key)) {
                 throw new NotContextException();
             } else {
                 throw new NameNotFoundException();
             }
         } else {
            return subContext.visitContext(name.getSuffix(name.size() - 1), visitor);
         }
     }
 }

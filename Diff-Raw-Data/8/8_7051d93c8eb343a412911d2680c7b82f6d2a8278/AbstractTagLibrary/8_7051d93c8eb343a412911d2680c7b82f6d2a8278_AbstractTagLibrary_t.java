 /**
  * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets.tag;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.el.ELException;
 import javax.faces.FacesException;
 import javax.faces.convert.Converter;
 import javax.faces.validator.Validator;
 
 import com.sun.facelets.FaceletContext;
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.FaceletHandler;
 
 /**
  * Base class for defining TagLibraries in Java
  * 
  * @author Jacob Hookom
 * @version $Id: AbstractTagLibrary.java,v 1.4 2005/07/21 02:09:14 jhook Exp $
  */
 public abstract class AbstractTagLibrary implements TagLibrary {
 
     private static class HandlerFactory implements TagHandlerFactory {
         private final static Class[] CONSTRUCTOR_SIG = new Class[] { TagConfig.class };
 
         protected final Class handlerType;
 
         public HandlerFactory(Class handlerType) {
             this.handlerType = handlerType;
         }
 
         public TagHandler createHandler(TagConfig cfg) throws FacesException,
                 ELException {
             try {
                 return (TagHandler) this.handlerType.getConstructor(
                         CONSTRUCTOR_SIG).newInstance(new Object[] { cfg });
             } catch (InvocationTargetException ite) {
                 Throwable t = ite.getCause();
                 if (t instanceof FacesException) {
                     throw (FacesException) t;
                 } else if (t instanceof ELException) {
                     throw (ELException) t;
                 } else {
                     throw new FacesException("Error Instantiating: "
                             + this.handlerType.getName(), t);
                 }
             } catch (Exception e) {
                 throw new FacesException("Error Instantiating: "
                         + this.handlerType.getName(), e);
             }
         }
     }
 
     private static class ComponentConfigWrapper implements ComponentConfig {
 
         protected final TagConfig parent;
 
         protected final String componentType;
 
         protected final String rendererType;
 
         public ComponentConfigWrapper(TagConfig parent, String componentType,
                 String rendererType) {
             this.parent = parent;
             this.componentType = componentType;
             this.rendererType = rendererType;
         }
 
         public String getComponentType() {
             return this.componentType;
         }
 
         public String getRendererType() {
             return this.rendererType;
         }
 
         public FaceletHandler getNextHandler() {
             return this.parent.getNextHandler();
         }
 
         public Tag getTag() {
             return this.parent.getTag();
         }
 
         public String getTagId() {
             return this.parent.getTagId();
         }
     }
 
     private static class UserTagFactory implements TagHandlerFactory {
         protected final URL location;
 
         public UserTagFactory(URL location) {
             this.location = location;
         }
 
         public TagHandler createHandler(TagConfig cfg) throws FacesException,
                 ELException {
             return new UserTagHandler(cfg, this.location);
         }
     }
 
     private static class ComponentHandlerFactory implements TagHandlerFactory {
 
         protected final String componentType;
 
         protected final String renderType;
 
         /**
          * @param handlerType
          */
         public ComponentHandlerFactory(String componentType, String renderType) {
             this.componentType = componentType;
             this.renderType = renderType;
         }
 
         public TagHandler createHandler(TagConfig cfg) throws FacesException,
                 ELException {
             ComponentConfig ccfg = new ComponentConfigWrapper(cfg,
                     this.componentType, this.renderType);
             return new ComponentHandler(ccfg);
         }
     }
 
     private static class UserComponentHandlerFactory implements
             TagHandlerFactory {
 
         private final static Class[] CONS_SIG = new Class[] { ComponentConfig.class };
 
         protected final String componentType;
 
         protected final String renderType;
 
         protected final Class type;
 
         protected final Constructor constructor;
 
         /**
          * @param handlerType
          */
         public UserComponentHandlerFactory(String componentType,
                 String renderType, Class type) {
             this.componentType = componentType;
             this.renderType = renderType;
             this.type = type;
             try {
                 this.constructor = this.type.getConstructor(CONS_SIG);
             } catch (Exception e) {
                 throw new FaceletException(
                         "Must have a Constructor that takes in a ComponentConfig",
                         e);
             }
         }
 
         public TagHandler createHandler(TagConfig cfg) throws FacesException,
                 ELException {
             try {
                 ComponentConfig ccfg = new ComponentConfigWrapper(cfg,
                         this.componentType, this.renderType);
                 return (TagHandler) this.constructor
                         .newInstance(new Object[] { ccfg });
            } catch (InvocationTargetException e) {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
             } catch (Exception e) {
                throw new FaceletException("Error Instantiating TagHandler: "+this.type.getName(), e);
             }
         }
     }
 
     private static class ValidatorHandlerFactory implements TagHandlerFactory {
 
         protected final String validatorId;
 
         public ValidatorHandlerFactory(String validatorId) {
             this.validatorId = validatorId;
         }
 
         public TagHandler createHandler(TagConfig cfg) throws FacesException,
                 ELException {
             return new ValidateHandler(cfg) {
                 protected Validator createValidator(FaceletContext ctx) {
                     return ctx.getFacesContext().getApplication()
                             .createValidator(validatorId);
                 }
             };
         }
     }
 
     private static class ConverterHandlerFactory implements TagHandlerFactory {
 
         protected final String converterId;
 
         public ConverterHandlerFactory(String converterId) {
             this.converterId = converterId;
         }
 
         public TagHandler createHandler(TagConfig cfg) throws FacesException,
                 ELException {
             return new ConvertHandler(cfg) {
                 protected Converter createConverter(FaceletContext ctx) {
                     return ctx.getFacesContext().getApplication()
                             .createConverter(converterId);
                 }
             };
         }
     }
 
     private final Map factories;
 
     private final String namespace;
 
     private final Map functions;
 
     public AbstractTagLibrary(String namespace) {
         this.namespace = namespace;
         this.factories = new HashMap();
         this.functions = new HashMap();
     }
 
     /**
      * Add a ComponentHandler with the specified componentType and rendererType,
      * aliased by the tag name.
      * 
      * @see ComponentHandler
      * @see javax.faces.application.Application#createComponent(java.lang.String)
      * @param name
      *            name to use, "foo" would be &lt;my:foo />
      * @param componentType
      *            componentType to use
      * @param rendererType
      *            rendererType to use
      */
     protected final void addComponent(String name, String componentType,
             String rendererType) {
         this.factories.put(name, new ComponentHandlerFactory(componentType,
                 rendererType));
     }
 
     /**
      * Add a ComponentHandler with the specified componentType and rendererType,
      * aliased by the tag name. The Facelet will be compiled with the specified
      * HandlerType (which must extend AbstractComponentHandler).
      * 
      * @see AbstractComponentHandler
      * @param name
      *            name to use, "foo" would be &lt;my:foo />
      * @param componentType
      *            componentType to use
      * @param rendererType
      *            rendererType to use
      * @param handlerType
      *            a Class that extends AbstractComponentHandler
      */
     protected final void addComponent(String name, String componentType,
             String rendererType, Class handlerType) {
         this.factories.put(name, new UserComponentHandlerFactory(componentType,
                 rendererType, handlerType));
     }
 
     /**
      * Add a ConvertHandler for the specified converterId
      * 
      * @see ConvertHandler
      * @see javax.faces.application.Application#createComponent(java.lang.String)
      * @param name
      *            name to use, "foo" would be &lt;my:foo />
      * @param converterId
      *            id to pass to Application instance
      */
     protected final void addConverter(String name, String converterId) {
         this.factories.put(name, new ConverterHandlerFactory(converterId));
     }
 
     /**
      * Add a ValidateHandler for the specified validatorId
      * 
      * @see ValidateHandler
      * @see javax.faces.application.Application#createValidator(java.lang.String)
      * @param name
      *            name to use, "foo" would be &lt;my:foo />
      * @param validatorId
      *            id to pass to Application instance
      */
     protected final void addValidator(String name, String validatorId) {
         this.factories.put(name, new ValidatorHandlerFactory(validatorId));
     }
 
     /**
      * Use the specified HandlerType in compiling Facelets. HandlerType must
      * extend TagHandler.
      * 
      * @see TagHandler
      * @param name
      *            name to use, "foo" would be &lt;my:foo />
      * @param handlerType
      *            must extend TagHandler
      */
     protected final void addTagHandler(String name, Class handlerType) {
         this.factories.put(name, new HandlerFactory(handlerType));
     }
 
     /**
      * Add a UserTagHandler specified a the URL source.
      * 
      * @see UserTagHandler
      * @param name
      *            name to use, "foo" would be &lt;my:foo />
      * @param source source where the Facelet (Tag) source is
      */
     protected final void addUserTag(String name, URL source) {
         this.factories.put(name, new UserTagFactory(source));
     }
     
     
     /**
      * Add a Method to be used as a Function at Compilation.
      * 
      * @see javax.el.FunctionMapper
      * 
      * @param name (suffix) of function name
      * @param method method instance 
      */
     protected final void addFunction(String name, Method method) {
         this.functions.put(name, method);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.tag.TagLibrary#containsNamespace(java.lang.String)
      */
     public boolean containsNamespace(String ns) {
         return this.namespace.equals(ns);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.tag.TagLibrary#containsTagHandler(java.lang.String,
      *      java.lang.String)
      */
     public boolean containsTagHandler(String ns, String localName) {
         if (this.namespace.equals(ns)) {
             if (this.factories.containsKey(localName)) {
                 return true;
             }
         }
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.tag.TagLibrary#createTagHandler(java.lang.String,
      *      java.lang.String, com.sun.facelets.tag.TagConfig)
      */
     public TagHandler createTagHandler(String ns, String localName,
             TagConfig tag) throws FacesException {
         if (this.namespace.equals(ns)) {
             TagHandlerFactory f = (TagHandlerFactory) this.factories
                     .get(localName);
             if (f != null) {
                 return f.createHandler(tag);
             }
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.tag.TagLibrary#containsFunction(java.lang.String,
      *      java.lang.String)
      */
     public boolean containsFunction(String ns, String name) {
         if (this.namespace.equals(ns)) {
             return this.functions.containsKey(name);
         }
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.tag.TagLibrary#createFunction(java.lang.String,
      *      java.lang.String)
      */
     public Method createFunction(String ns, String name) {
         if (this.namespace.equals(ns)) {
             return (Method) this.functions.get(name);
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object obj) {
         return (obj instanceof TagLibrary && obj.hashCode() == this.hashCode());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         return this.namespace.hashCode();
     }
 
     public String getNamespace() {
         return namespace;
     }
 }

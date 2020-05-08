 /*******************************************************************************
  * Copyright (c) 2012 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   SAP AG - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.enterprise.jsf.support;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceUnit;
 
 import com.sun.faces.spi.InjectionProvider;
 import com.sun.faces.spi.InjectionProviderException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class VirgoJsfInjectionProvider implements InjectionProvider {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 	private static final String COMP_ENV = "java:comp/env/";
 	private Context namingContext = null;
 
 	public VirgoJsfInjectionProvider() {
 		try {
 			//this.namingContext = (Context) ContextBindings.getClassLoader().lookup("");
 			this.namingContext = new InitialContext();
 		} catch (NamingException e) {
 		    if (logger.isErrorEnabled()) {
 		        logger.error("Injection of naming resources into JSF managed beans disabled.", e);
 		    }
 		}
 	}
 
 	@Override
 	public void inject(Object managedBean) throws InjectionProviderException {
 	    // try injecting everything with OWB's injector instance
 	    try {
             ClassLoader tccl = Thread.currentThread().getContextClassLoader();
             if (tccl != null) {
                 Class<?> injector = tccl.loadClass("org.apache.webbeans.inject.OWBInjector");
                 Object injectorInstance = injector.newInstance();
                 Method method = injector.getDeclaredMethod("inject", new Class<?>[] { Object.class });
                 injectorInstance = method.invoke(injectorInstance, new Object[] { managedBean });
                 return;
             }
         } catch (Exception e) {
            e.printStackTrace();
         }
	    // if TCCL is not available fall back to the manual processing
 		if (this.namingContext != null) {
 			try {
 				// Initialize fields annotations
 				Class<?> currentBeanClass = managedBean.getClass();
 				while (currentBeanClass != null) {
 					// Initialize the annotations
 					processFields(managedBean, currentBeanClass);
 					processMethods(managedBean, currentBeanClass);
 					currentBeanClass = currentBeanClass.getSuperclass();
 				}
 			} catch (Exception e) {
 			    if (logger.isErrorEnabled()) {
 			        logger.error("Failed to inject managed bean in FacesServlet:", e);
 			    }
 			}
         }
 	}
 
 	private void processMethods(Object managedBean, Class<?> currentBeanClass) throws NamingException, IllegalAccessException, InvocationTargetException {
 		Method[] methods = currentBeanClass.getDeclaredMethods();
 		for (Method method : methods) {
 			if (method.isAnnotationPresent(Resource.class)) {
 				Resource annotation = (Resource) method.getAnnotation(Resource.class);
 				String lookupName = COMP_ENV + annotation.name();
 				if (annotation.lookup() != null && !annotation.lookup().equals("")) {
 					lookupName = annotation.lookup();
 				}
 				lookupMethodResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), method, lookupName);
 			}
 			if (method.isAnnotationPresent(EJB.class)) {
 				EJB annotation = (EJB) method.getAnnotation(EJB.class);
 				String lookupName = COMP_ENV + annotation.name();
 				if (annotation.lookup() != null && !annotation.lookup().equals("")) {
 					lookupName = annotation.lookup();
 				}
 				lookupMethodResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), method, lookupName);
 			}
 			if (method.isAnnotationPresent(PersistenceContext.class)) {
 				PersistenceContext annotation = (PersistenceContext) method.getAnnotation(PersistenceContext.class);
 				lookupMethodResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), method, annotation.name());
 			}
 			if (method.isAnnotationPresent(PersistenceUnit.class)) {
 				PersistenceUnit annotation = (PersistenceUnit) method.getAnnotation(PersistenceUnit.class);
 				lookupMethodResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), method, annotation.name());
 			}
 		}
 	}
 
 	private void processFields(Object managedBean, Class<?> currentBeanClass) throws NamingException, IllegalAccessException {
 		Field[] fields = currentBeanClass.getDeclaredFields();
 		if (fields != null) {
 			for (Field field : fields) {
 				if (field.isAnnotationPresent(Resource.class)) {
 					Resource annotation = (Resource) field.getAnnotation(Resource.class);
 					String lookupName = COMP_ENV + annotation.name();
 					if (annotation.lookup() != null && !annotation.lookup().equals("")) {
 						lookupName = annotation.lookup();
 					}
 					lookupFieldResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), field, lookupName);
 				}
 				if (field.isAnnotationPresent(EJB.class)) {
 					EJB annotation = (EJB) field.getAnnotation(EJB.class);
 					String lookupName = COMP_ENV + annotation.name();
 					if (annotation.lookup() != null && !annotation.lookup().equals("")) {
 						lookupName = annotation.lookup();
 					}
 					lookupFieldResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), field, lookupName);
 				}
 				if (field.isAnnotationPresent(PersistenceContext.class)) {
 					PersistenceContext annotation = (PersistenceContext) field.getAnnotation(PersistenceContext.class);
 					lookupFieldResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), field, annotation.name());
 				}
 				if (field.isAnnotationPresent(PersistenceUnit.class)) {
 					PersistenceUnit annotation = (PersistenceUnit) field.getAnnotation(PersistenceUnit.class);
 					lookupFieldResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), field, annotation.name());
 				}
 				//FIXME a bit dumb: the non-annotated field could be a constant -> ignore the NameNotFoundException
 				if (field.getAnnotations().length == 0) {
 				    try {
 				        //try to lookup the field's naming data - it might be used as injection target
 				        lookupFieldResource(namingContext, managedBean, currentBeanClass.getCanonicalName(), field, null);
 				    } catch (NameNotFoundException nnfe) {
 				        //don't fail - the field could be a constant or not intended to be used as injection target
 				    }
 				}
 			}
 		}
 	}
 
     @Override
     public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
         Method postConstruct = findAnnotatedMethod(managedBean, PostConstruct.class);
 
         // At the end the PostConstruct annotated method is invoked
         if (postConstruct != null) {
             boolean accessibility = postConstruct.isAccessible();
             postConstruct.setAccessible(true);
             try {
                 postConstruct.invoke(managedBean);
             } catch (Exception e) {
                 throw new InjectionProviderException(e.getMessage(), e);
             } finally {
                 postConstruct.setAccessible(accessibility);
             }
         }
     }
 
     @Override
     public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
         Method preDestroy = findAnnotatedMethod(managedBean, PreDestroy.class);
 
         // At the end the PreDestroy annotated method is invoked
         if (preDestroy != null) {
             boolean accessibility = preDestroy.isAccessible();
             preDestroy.setAccessible(true);
             try {
                 preDestroy.invoke(managedBean);
             } catch (Exception e) {
                 throw new InjectionProviderException(e.getMessage(), e);
             } finally {
                 preDestroy.setAccessible(accessibility);
             }
         }
     }
 
     private Method findAnnotatedMethod(Object managedBean, Class<? extends Annotation> annotation) {
         Method result = null;
         Class<?> currentClass = managedBean.getClass();
         while (currentClass != null) {
             Method[] methods = currentClass.getDeclaredMethods();
             for (Method method : methods) {
                 if (method.isAnnotationPresent(annotation)) {
                     if ((result != null) || (method.getParameterTypes().length != 0) || (Modifier.isStatic(method.getModifiers()))
                         || (method.getExceptionTypes().length > 0) || (!method.getReturnType().getName().equals("void"))) {
                         throw new IllegalArgumentException("Invalid annotation " + annotation.getName());
                     }
                     result = method;
                     break;
                 }
             }
             if (result != null) {
                 break;
             }
             currentClass = currentClass.getSuperclass();
         }
         return result;
     }
 
 	protected static void lookupFieldResource(javax.naming.Context context, Object instance, String beanClassName, Field field, String name) throws NamingException, IllegalAccessException {
 		Object lookedupResource = null;
 		boolean accessibility = false;
 
 		if ((name != null) && (name.length() > 0) && (name.length() > COMP_ENV.length())) {
 			lookedupResource = context.lookup(name);
 		} else {
 			lookedupResource = context.lookup(getFullyQualifiedName(beanClassName + "/" + field.getName()));
 		}
 
 		accessibility = field.isAccessible();
 		field.setAccessible(true);
 		field.set(instance, lookedupResource);
 		field.setAccessible(accessibility);
 	}
 
 	protected static void lookupMethodResource(javax.naming.Context context, Object instance, String beanClassName, Method method, String name) throws NamingException, IllegalAccessException,
 			InvocationTargetException {
 		if (!method.getName().startsWith("set") || method.getParameterTypes().length != 1 || !method.getReturnType().getName().equals("void")) {
 			throw new IllegalArgumentException("Invalid method resource injection annotation");
 		}
 
 		Object lookedupResource = null;
 		boolean accessibility = false;
 
 		if ((name != null) && (name.length() > 0) && (name.length() > COMP_ENV.length())) {
 			lookedupResource = context.lookup(name);
 		} else {
 			String fieldName = method.getName().substring(3);
 			String firstChar = String.valueOf(fieldName.charAt(0));
 			fieldName = fieldName.replaceFirst(firstChar, firstChar.toLowerCase());
 
 			lookedupResource = context.lookup(getFullyQualifiedName(beanClassName + "/" + fieldName));
 		}
 
 		accessibility = method.isAccessible();
 		method.setAccessible(true);
 		method.invoke(instance, lookedupResource);
 		method.setAccessible(accessibility);
 	}
 
 	private static String getFullyQualifiedName(String name) {
 		return COMP_ENV + name;
 	}
 
 }

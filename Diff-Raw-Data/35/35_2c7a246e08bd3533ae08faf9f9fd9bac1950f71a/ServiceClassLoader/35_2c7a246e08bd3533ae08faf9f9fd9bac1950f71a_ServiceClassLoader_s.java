 /*
  * File: ServiceClassLoader.java
  * 
  * Copyright 2012 OSFramework Project.
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
 package org.osframework.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.ServiceConfigurationError;
 
 /**
  * A simple service-provider loading facility. Replicates the behavior of
  * {@linkplain java.util.ServiceLoader} in every way, except this class does not
  * instantiate the located provider classes. Deferred instantiation is of value
  * in situations where the client needs to perform additional inspection or
  * reflection of the loaded provider class prior to instantiation, or where the
  * provider class has no default constructor.
  *
  * @author <a href="mailto:dave@osframework.org">Dave Joyce</a>
  */
 public final class ServiceClassLoader<S> implements Iterable<Class<? extends S>> {
 
 	private static final String PREFIX = "META-INF/services/";
 
 	private final transient Class<S> serviceClass;
 	private final transient ClassLoader loader;
 	private final transient Map<String, Class<? extends S>> providerClasses = new LinkedHashMap<String, Class<? extends S>>();
 
 	// The current lazy-lookup iterator
     private transient LazyIterator lookupIterator;
 
 	public static <S> ServiceClassLoader<S> load(final Class<S> serviceClass, final ClassLoader loader) {
 		return new ServiceClassLoader<S>(serviceClass, loader);
 	}
 
 	public static <S> ServiceClassLoader<S> load(final Class<S> serviceClass) {
 		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		return ServiceClassLoader.load(serviceClass, loader);
 	}
 
 	public static <S> ServiceClassLoader<S> loadInstalled(final Class<S> serviceClass) {
 		ClassLoader loader = ClassLoader.getSystemClassLoader();
 		ClassLoader prev = null;
 		while (null != loader) {
 			prev = loader;
 			loader = loader.getParent();
 		}
 		return ServiceClassLoader.load(serviceClass, prev);
 	}
 
 	public void reload() {
 		providerClasses.clear();
 		lookupIterator = new LazyIterator(serviceClass, loader);
 	}
 
 	public Iterator<Class<? extends S>> iterator() {
 		// TODO Auto-generated method stub
 		return new Iterator<Class<? extends S>>() {
 			Iterator<Map.Entry<String, Class<? extends S>>> knownIt = providerClasses.entrySet().iterator();
 		
 			public boolean hasNext() {
 				if (knownIt.hasNext()) {
 					return true;
 				}
 				return lookupIterator.hasNext();
 			}
 		
 			public Class<? extends S> next() {
 				if (knownIt.hasNext()) {
 					return knownIt.next().getValue();
 				}
 				return lookupIterator.next();
 			}
 		
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder buf = new StringBuilder("org.osframework.util.ServiceClassLoader[")
 		                              .append(serviceClass.getName())
 		                              .append("]");
 		return buf.toString();
 	}
 
     private static void fail(final Class<?> service, final String msg, final Throwable cause)
     	throws ServiceConfigurationError {
     	throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
     }
 
     private static void fail(final Class<?> service, final String msg)
     	throws ServiceConfigurationError {
     	throw new ServiceConfigurationError(service.getName() + ": " + msg);
     }
 
     private static void fail(final Class<?> service, final URL url, final int line, final String msg)
     	throws ServiceConfigurationError {
     	fail(service, url + ":" + line + ": " + msg);
     }
 
 	private ServiceClassLoader(final Class<S> serviceClass, final ClassLoader loader) {
 		this.serviceClass = serviceClass;
 		this.loader = loader;
 		this.reload();
 	}
 
 	private int parseLine(final Class<S> serviceClass, final URL url, final BufferedReader reader, final int lineNum, final List<String> names)
 		throws IOException, ServiceConfigurationError {
 		String line = reader.readLine();
 		int nextLineNum;
 		if (null == line) {
 		    nextLineNum = -1;
 		} else {
 			// Read everything on line prior to start of comment
 			final int commentIdx = line.indexOf('#');
 			if (0 <= commentIdx) {
 				line = line.substring(0, commentIdx);
 			}
 			line = line.trim();
 			final int lineLength = line.length();
 			if (0 != lineLength) {
 				if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) {
 					fail(serviceClass, url, lineNum, "Illegal configuration-file syntax");
 				}
 				int codePt = line.codePointAt(0);
 				if (!Character.isJavaIdentifierStart(codePt)) {
 					fail(serviceClass, url, lineNum, "Illegal provider-class name: " + line);
 				}
 				for (int i = Character.charCount(codePt); i < lineLength; i += Character.charCount(codePt)) {
 					codePt = line.codePointAt(i);
 					if (!Character.isJavaIdentifierPart(codePt) && ('.' != codePt)) {
 						fail(serviceClass, url, lineNum, "Illegal provider-class name: " + line);
 					}
 				}
 				if (!providerClasses.containsKey(line) && !names.contains(line)) {
 					names.add(line);
 				}
 			}
 			nextLineNum = lineNum + 1;
 		}
 		return nextLineNum;
 	}
 
 	private Iterator<String> parse(final Class<S> serviceClass, final URL url)
 		throws ServiceConfigurationError {
 		InputStream inStream = null;
 		BufferedReader reader = null;
 		final ArrayList<String> names = new ArrayList<String>();
 		try {
 			inStream = url.openStream();
 			reader = new BufferedReader(new InputStreamReader(inStream, Charset.forName("UTF-8")));
 			int lineNum = 1;
 			do {
 				lineNum = parseLine(serviceClass, url, reader, lineNum, names);
 			} while (0 <= lineNum);
 		} catch (IOException ioe) {
 			fail(serviceClass, "Error reading configuration file", ioe);
 		} finally {
 			try {
 				if (null != reader) {
 					reader.close();
 				}
 				if (null != inStream) {
 					inStream.close();
 				}
 			} catch (IOException ioe) {
 				fail(serviceClass, "Error closing configuration file", ioe);
 			}
 		}
 		return names.iterator();
 	}
 
 	private class LazyIterator implements Iterator<Class<? extends S>> {
 	
 		private final transient Class<S> serviceClass;
 		private final transient ClassLoader loader;
 		private transient Enumeration<URL> configs = null;
 		private transient Iterator<String> pending = null;
 		private transient String nextName = null;
 	
 		LazyIterator(final Class<S> serviceClass, final ClassLoader loader) {
 			this.serviceClass = serviceClass;
 			this.loader = loader;
 		}
 	
 		public boolean hasNext() {
 			boolean hasNext = true;
 			if (null == nextName) {
 				hasNext = false;
			} else if (null == configs) {
				try {
					final String fullName = PREFIX + serviceClass.getName();
					configs = (null == loader)
							   ? ClassLoader.getSystemResources(fullName)
							   : loader.getResources(fullName);
				} catch (IOException ioe) {
					fail(serviceClass, "Error locating configuration files", ioe);
 				}
			}
			while ((null == pending) || !pending.hasNext()) {
				if (!configs.hasMoreElements()) {
					hasNext = false;
 				}
				pending = parse(serviceClass, configs.nextElement());
 			}
			nextName = pending.next();
 			return hasNext;
 		}
 	
 		public Class<? extends S> next() {
 			if (!hasNext()) {
 				throw new NoSuchElementException();
 			}
 			final String className = nextName;
 		    nextName = null;
 		    Class<?> cls = null;
 		    try {
 		    	cls = Class.forName(className, false, loader);
 		    } catch (ClassNotFoundException cnfe) {
 		    	fail(serviceClass, "Provider " + className + " not found");
 		    }
 		    if (!serviceClass.isAssignableFrom(cls)) {
 		    	fail(serviceClass, "Provider " + className  + " not a subtype");
 		    }
 		    try {
 			    final Class<? extends S> providerClass = cls.asSubclass(serviceClass);
 			    providerClasses.put(className, providerClass);
 			    return providerClass;
 		    } catch (Exception e) {
 		    	fail(serviceClass, "Provider " + className + " could not be cast to subtype: " + e, e);
 		    }
 		    // This cannot happen
 		    throw new Error();
 		}
 	
 		public void remove() {
 			throw new UnsupportedOperationException();	
 		}
 	}
 }

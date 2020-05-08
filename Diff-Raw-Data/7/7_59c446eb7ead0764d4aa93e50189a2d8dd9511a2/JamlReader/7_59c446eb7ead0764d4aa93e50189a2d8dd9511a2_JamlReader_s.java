 /*******************************************************************************
  * Copyright (C) 2011 by Harry Blauberg
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.jaml.core;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.jaml.api.Defaults;
 import org.jaml.api.IMarkupExtension;
 import org.jaml.api.IProportionHandler;
 import org.jaml.api.ITypeConverter;
 import org.jaml.api.ParsingInstructions;
 import org.jaml.cache.ClassCacheLibrary;
 import org.jaml.cache.Env;
 import org.jaml.container.ClassCache;
 import org.jaml.container.ParameterContainer;
 import org.jaml.objects.Element;
 import org.jaml.objects.ParentObject;
 import org.jaml.structs.Pair;
 import org.jaml.util.IOUtils;
 import org.jaml.util.ReflectionUtils;
 import org.jaml.util.ThreadUtils;
 
 public class JamlReader {
 	private static final XMLInputFactory factory = XMLInputFactory
 			.newInstance();
 
 	private JamlReader() {
 	}
 
 	public static Element load(Reader inputReader) {
 		Element rootElement = null;
 		ParentObject current = null;
 		Element temp = null;
 		Pair<Object, Map<ParsingInstructions, String>> pair = null;
 		try {
 			XMLStreamReader reader = factory.createXMLStreamReader(inputReader);
 			while (reader.hasNext()) {
 				reader.next();
 				if (reader.isStartElement()) {
 					System.out.println("isStartElement: " + reader.getName());
 					// Parse the element
 					pair = handleStartElement(reader);
 					if (current != null) {
 						// Get new element and add this new element as a child
 						// to the current element
 						temp = current.add(pair.getFirst());
 						// Handle proportions
 						handleProportions(current, temp);
 					} else {
 						// Very first element
 						temp = new Element(null, pair.getFirst());
 						// Save it as root element
 						rootElement = temp;
 					}
 					// Apply parsing instructions to the element
 					applyInstructions(temp, pair.getSecond());
 					// Handle object map
 					handleObjects(rootElement, temp);
 					// Set it as new current element
 					current = temp;
 				} else if (reader.isCharacters()) {
 					System.out.println("isCharacters: "
 							+ reader.getText().trim());
 				} else if (reader.isStandalone()) {
 					System.out.println("isStandalone: "
 							+ reader.getText().trim());
 				} else if (reader.isWhiteSpace()) {
 					System.out.println("isWhiteSpace: "
 							+ reader.getText().trim());
 				} else if (reader.isEndElement()) {
 					System.out.println("isEndElement: "
 							+ reader.getName().toString().trim());
 					// Get the parent element as new current element
 					current = current.getParent();
 				}
 			}
 			reader.close();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 		return rootElement;
 	}
 
 	private static void handleObjects(Element root, Element child) {
 		// Put the object into the global scope if 'Name'
 		// parsing instruction is given
 		if (child.hasInstruction(ParsingInstructions.Name)) {
 			root.storeObject(child.getInstruction(ParsingInstructions.Name),
 					child.getContent());
 		}
 	}
 
 	private static void handleProportions(ParentObject parent, Element child) {
 		Unit<?> unit = null;
 		Element element = null;
 		if (Element.class.isInstance(parent)) {
 			element = Element.class.cast(parent);
 		} else if (Unit.class.isInstance(parent)) {
 			unit = Unit.class.cast(parent);
 		}
 		Object parentObj = element != null ? element.getContent()
 				: unit != null ? unit.getRoot() : null;
 		Object childObj = child.isValid() ? child.getContent() : null;
 		if (parentObj != null && childObj != null) {
 			Pair<Class<?>, Class<?>> key = new Pair<Class<?>, Class<?>>(
 					parentObj.getClass(), childObj.getClass());
 			IProportionHandler handler = Env.get().getProportions().get(key);
 			if (handler != null) {
 				System.out.println("Invoking: " + handler);
 				handler.process(parentObj, childObj);
 			}
 		}
 	}
 
 	private static void applyInstructions(Element element,
 			Map<ParsingInstructions, String> parsingInstructions) {
 		for (ParsingInstructions instruction : parsingInstructions.keySet()) {
 			element.setInstruction(instruction,
 					parsingInstructions.get(instruction));
 		}
 	}
 
 	private static Pair<Object, Map<ParsingInstructions, String>> handleStartElement(
 			XMLStreamReader reader) {
 		Object obj = null;
 		Map<ParsingInstructions, String> parsingInstructions = new HashMap<ParsingInstructions, String>();
 		ParsingInstructions instruction = null;
 		String className = ReflectionUtils
 				.getClassByNamespace(reader.getName());
 		try {
 			ClassCacheLibrary.getInstance().addToCacheIfRequired(className);
 			ClassCache cache = ClassCacheLibrary.getInstance().getCache(
 					className);
 			obj = cache.getType().newInstance();
 			String attrName = null;
 			String attrValue = null;
 			String attrNameSpace = null;
 			String attrPrefix = null;
 			ParameterContainer paramContainer = null;
 			Method setter = null;
 			List<Class<?>[]> filtered = null;
 			Class<?> clazz = null;
 			ITypeConverter converter = null;
 			Object tmp = null;
 			for (int i = 0; i < reader.getAttributeCount(); i++) {
 				attrName = reader.getAttributeName(i).getLocalPart();
 				attrValue = reader.getAttributeValue(i);
 				attrNameSpace = reader.getAttributeName(i).getNamespaceURI();
 				attrPrefix = reader.getAttributeName(i).getPrefix();
 				// Check if attribute is in another name space than the
 				if (!(attrNameSpace.isEmpty() && attrPrefix.isEmpty())) {
 					System.out.println(attrPrefix + ":" + attrName
 							+ " is in another namespace: " + attrNameSpace);
 					if (attrNameSpace.equals(Defaults.parserNamespace)) {
 						instruction = ParsingInstructions.valueOf(attrName);
 						parsingInstructions.put(instruction, attrValue);
 					}
 				} else {
 					// Check if attribute is writable
 					if (cache.isWritable(attrName)) {
 						IMarkupExtension markup = Env.get().getMarkups()
 								.get(attrValue);
 						if (markup != null) {
 							handleMarkup(markup, attrValue);
 						} else {
 							paramContainer = cache
 									.getWritableArguments(attrName);
 							filtered = paramContainer.filterByNumber(1);
 							for (Class<?>[] classes : filtered) {
 								clazz = classes[0];
 								converter = Env.get().getConverters()
 										.get(clazz);
 								if (converter != null) {
 									tmp = converter.convertString(attrValue);
 									System.out.println(converter + " " + tmp);
 									setter = ReflectionUtils
 											.searchSetterMethod(cache,
 													attrName, tmp.getClass());
 									if (setter == null) {
 										System.err
 												.println(String
 														.format("Could not set '%s' on type '%s'!",
 																tmp.getClass(),
 																obj.getClass()));
 									} else {
 										try {
 											setter.invoke(obj, tmp);
 										} catch (Exception e) {
 											String stackTrace = IOUtils
 													.getStackTraceAsStr(e);
 											String fqnClassName = e.getClass()
 													.getName();
 											String text = String.format(
 													"(%s)[%s] %s: %s => %s",
 													attrValue, attrName,
 													fqnClassName,
 													e.getMessage(), stackTrace);
 											System.err.println(text);
 										}
 									}
 								} else {
 									System.err
 											.println("Missing converter for: "
 													+ clazz);
 								}
 							}
 						}
 					} else {
 						System.err.println(String.format(
 								"Field '%s' is not accessable to be set!",
 								attrName));
 					}
 				}
 			}
 		} catch (Exception e) {
 			System.err.println(e.getClass().getSimpleName() + ": "
 					+ e.getMessage());
 		}
 		return new Pair<Object, Map<ParsingInstructions, String>>(obj,
 				parsingInstructions);
 	}
 
 	private static void handleMarkup(IMarkupExtension extension, String value) {
 		String markup = value.substring(1, value.length() - 1);
 		System.out.println("Markup: '" + markup + "'");
 		String[] splitted = markup.split(" ");
 		String symbol = splitted[0];
 		String arg = splitted[1];
 		System.out.println(symbol + ", " + extension);
		extension.handleMarkup(arg);
 	}
 
 	public static Element load(InputStream inputStream) {
 		return load(new InputStreamReader(inputStream));
 	}
 
 	public static Element load(String input) {
 		return load(new StringReader(input));
 	}
 
 	public static Future<Element> loadAsync(final Reader reader) {
 		return ThreadUtils.exec(new Callable<Element>() {
 			@Override
 			public Element call() throws Exception {
 				return load(reader);
 			}
 		});
 	}
 
 	public static Future<Element> loadAsync(final InputStream stream) {
 		return ThreadUtils.exec(new Callable<Element>() {
 			@Override
 			public Element call() throws Exception {
 				return load(stream);
 			}
 		});
 	}
 
 	public static Future<Element> loadAsync(final String input) {
 		return ThreadUtils.exec(new Callable<Element>() {
 			@Override
 			public Element call() throws Exception {
 				return load(input);
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T loadAndUnwrap(Reader reader) {
 		return (T) (load(reader).getContent());
 	}
 
 	public static <T> T loadAndUnwrap(InputStream inputStream) {
 		return loadAndUnwrap(new InputStreamReader(inputStream));
 	}
 
 	public static <T> T loadAndUnwrap(String input) {
 		return loadAndUnwrap(new StringReader(input));
 	}
 
 	public static <T> Future<T> loadAndUnwrapAsync(final Reader reader) {
 		return ThreadUtils.exec(new Callable<T>() {
 			@Override
 			public T call() throws Exception {
 				return loadAndUnwrap(reader);
 			}
 		});
 	}
 
 	public static <T> Future<T> loadAndUnwrapAsync(final InputStream stream) {
 		return ThreadUtils.exec(new Callable<T>() {
 			@Override
 			public T call() throws Exception {
 				return loadAndUnwrap(stream);
 			}
 		});
 	}
 
 	public static <T> Future<T> loadAndUnwrapAsync(final String input) {
 		return ThreadUtils.exec(new Callable<T>() {
 			@Override
 			public T call() throws Exception {
 				return loadAndUnwrap(input);
 			}
 		});
 	}
 }

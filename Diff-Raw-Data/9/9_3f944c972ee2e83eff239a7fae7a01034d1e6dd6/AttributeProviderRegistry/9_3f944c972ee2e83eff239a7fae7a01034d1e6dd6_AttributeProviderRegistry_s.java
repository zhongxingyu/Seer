 package org.jackie.jclassfile.attribute;
 
 import org.jackie.utils.Assert;
 import org.jackie.utils.Log;
 import org.jackie.context.ContextObject;
 import static org.jackie.context.ContextManager.context;
 
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author Patrik Beno
  */
 public class AttributeProviderRegistry implements ContextObject {
 
 	static public final String RESOURCE = "META-INF/org.jackie.jclassfile/attributeproviders.properties";
 
 
 	static public AttributeProviderRegistry instance() {
 		return attributeProviderRegistry();
 	}
 
 	static public AttributeProviderRegistry attributeProviderRegistry() {
 		AttributeProviderRegistry registry = context().get(AttributeProviderRegistry.class);
 		if (registry == null) {
 			registry = new AttributeProviderRegistry();
 			context().set(AttributeProviderRegistry.class, registry);
 			registry.initDefaults();
 		}
 		return registry;
 	}
 
 	Map<String, AttributeProvider> providers;
 
 	{
 		providers = new HashMap<String, AttributeProvider>();
 	}
 
 	public void clear() {
 		providers.clear();
 	}
 
 	public void addProvider(AttributeProvider provider) {
 		providers.put(provider.name(), provider);
 	}
 
 	public AttributeProvider getProvider(String name) {
 		return providers.get(name);
 	}
 
 	public void removeProvider(String name) {
 		providers.remove(name);
 	}
 
 	public void initDefaults() {
 		Log.enter();
 		try {
 			Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(RESOURCE);
 			while (e.hasMoreElements()) {
 				URL url = e.nextElement();
 				Log.debug("Loading attribute providers from %s", url);
 				Properties props = new Properties();
 				props.load(url.openStream());
 				populate(props);
 			}
 		} catch (Throwable t) {
			Assert.unexpected(t);
 		}
 		Log.leave();
 	}
 
 	private void populate(Properties props) {
 		Enumeration<?> names = props.propertyNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String clsname = props.getProperty(name);
 			createProvider(name, clsname);
 		}
 	}
 
 	private void createProvider(String name, String clsname) {
 		try {
 			Log.debug("Creating AttributeProvider %s -> %s", name, clsname);
 			Class<?> cls = Class.forName(clsname, false, Thread.currentThread().getContextClassLoader());
 			AttributeProvider provider = (AttributeProvider) cls.newInstance();
 			providers.put(name, provider);
 
 		} catch (ClassNotFoundException e) {
			throw Assert.notYetHandled(e);
 		} catch (IllegalAccessException e) {
 			throw Assert.notYetHandled(e);
 		} catch (InstantiationException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 }

 package net.northfuse.resources.config;
 
 import net.northfuse.resources.ResourceHandler;
 import net.northfuse.resources.ResourceHandlerAdapter;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.parsing.BeanComponentDefinition;
 import org.springframework.beans.factory.support.ManagedMap;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 import org.springframework.beans.factory.xml.BeanDefinitionParser;
 import org.springframework.beans.factory.xml.ParserContext;
 import org.springframework.core.Ordered;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * @author tylers2
  */
 abstract class ResourceDefinitionParser<T extends ResourceHandler> implements BeanDefinitionParser {
 	private final AtomicBoolean registeredAdapter = new AtomicBoolean();
	private final Object lock = new Object();
 
 	public final BeanDefinition parse(Element element, ParserContext parserContext) {
 		doParse(parserContext, element, parserContext.extractSource(element));
 		return null;
 	}
 
 	private void doParse(ParserContext parserContext, Element element, Object source) {
 		Data data = registerResourceHandler(parserContext, element, source);
 
 		if (data == null) {
 			return;
 		}
 
 		String resourceMapping = data.resourceMapping;
 		String handlerBeanName = data.handlerBeanName;
 
 		registerAdapterIfNeeded(parserContext);
 
 		RootBeanDefinition handlerMappingDefinition = new RootBeanDefinition(SimpleUrlHandlerMapping.class);
 		handlerMappingDefinition.setSource(source);
 		String order = element.getAttribute("order");
 		handlerMappingDefinition.getPropertyValues().add("order", StringUtils.hasText(order) ? order : Ordered.LOWEST_PRECEDENCE - 1);
 
 		Map<String, String> urlMap = new ManagedMap<String, String>();
 		urlMap.put(resourceMapping, handlerBeanName);
 		handlerMappingDefinition.getPropertyValues().add("urlMap", urlMap);
 
 		String handlerMappingBeanName = parserContext.getReaderContext().generateBeanName(handlerMappingDefinition);
 		parserContext.getRegistry().registerBeanDefinition(handlerMappingBeanName, handlerMappingDefinition);
 		parserContext.registerBeanComponent(new BeanComponentDefinition(handlerMappingDefinition, handlerMappingBeanName));
 	}
 
 	private Data registerResourceHandler(ParserContext parserContext, Element element, Object source) {
 		RootBeanDefinition handlerDefinition = new RootBeanDefinition(getImplementation());
 
 		handlerDefinition.getPropertyValues().add("debug", element.getAttribute("debug"));
 		handlerDefinition.setSource(source);
 
 		String resourceMapping = element.getAttribute("mapping");
 		if (!StringUtils.hasText(resourceMapping)) {
 			parserContext.getReaderContext().error("The 'mapping' attribute is required.", parserContext.extractSource(element));
 			return null;
 		}
 		handlerDefinition.getPropertyValues().add("mapping", resourceMapping);
 
 		//find resource locations
 		handlerDefinition.getPropertyValues().add("resources", findLocations(element));
 
 		String handlerBeanName = parserContext.getReaderContext().generateBeanName(handlerDefinition);
 		parserContext.getRegistry().registerBeanDefinition(handlerBeanName, handlerDefinition);
 		parserContext.registerBeanComponent(new BeanComponentDefinition(handlerDefinition, handlerBeanName));
 
 		Data data = new Data();
 		data.handlerBeanName = handlerBeanName;
 		data.resourceMapping = resourceMapping;
 		return data;
 	}
 
 	private List<String> findLocations(Element element) {
 		List<String> resources = new LinkedList<String>();
 
 		final NodeList nodeList = element.getElementsByTagNameNS("http://northfuse.net/schema/resources-ext", "resource");
 		final Iterator<Node> i = new Iterator<Node>() {
 			private int count = 0;
 			public boolean hasNext() {
 				return count < nodeList.getLength();
 			}
 
 			public Node next() {
 				return nodeList.item(count++);
 			}
 
 			public void remove() {
 			}
 		};
 		for (Node node : new Iterable<Node>() {
 			public Iterator<Node> iterator() {
 				return i;
 			}
 		}) {
 			Element e = (Element) node;
 			resources.add(e.getAttribute("location"));
 		}
 		return resources;
 	}
 
 	private void registerAdapterIfNeeded(ParserContext parserContext) {
		synchronized (lock) {
 			if (!registeredAdapter.get()) {
 				registeredAdapter.set(true);
 
 				parserContext.getRegistry().registerBeanDefinition(ResourceHandlerAdapter.class.getName(), new RootBeanDefinition(ResourceHandlerAdapter.class));
 			}
 		}
 
 	}
 
 	protected abstract Class<T> getImplementation();
 
 	private class Data {
 		private String handlerBeanName;
 		private String resourceMapping;
 	}
 }

 package com.xmlmachines.providers.jersey;
 
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
 
 import org.apache.log4j.Logger;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.api.json.JSONJAXBContext;
 import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
 import com.sun.jersey.spi.scanning.AnnotationScannerListener;
 
 @Provider
 public final class JAXBContextResolver implements ContextResolver<JAXBContext> {
 
 	private static final Logger LOG = Logger
 			.getLogger(JAXBContextResolver.class);
 
 	private final JAXBContext context;
 	private final Set<Class<?>> types;
 	private final Class<?>[] cTypes;
 
 	public JAXBContextResolver() throws Exception {
 		LOG.info("Setting up JAXB Context resolver..");
 
 		PackageNamesScanner pns = new PackageNamesScanner(
 				new String[] { "com.xmlmachines.beans" });

 		@SuppressWarnings("unchecked")
 		AnnotationScannerListener asl = new AnnotationScannerListener(
				XmlRootElement.class, XmlType.class);
 		pns.scan(asl);
 		this.types = new HashSet<Class<?>>(asl.getAnnotatedClasses());
 
 		cTypes = new Class[this.types.size()];
 		this.context = new JSONJAXBContext(JSONConfiguration.natural().build(),
 				this.types.toArray(cTypes));
 	}
 
 	@Override
 	public JAXBContext getContext(Class<?> objectType) {
 		LOG.info(MessageFormat.format("Do we have a context? {0}",
 				types.contains(objectType)));
 		return (types.contains(objectType)) ? context : null;
 	}

 }

 package br.com.caelum.support;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 
 import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
 import br.com.caelum.vraptor.ioc.Component;
 import br.com.caelum.vraptor.serialization.ProxyInitializer;
 import br.com.caelum.vraptor.serialization.Serializer;
 import br.com.caelum.vraptor.serialization.xstream.XStreamJSONSerialization;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 
 @Component
public class CustomJsonSerializer extends XStreamJSONSerialization {
 	private Class<?>[] types;
 
	public CustomJsonSerializer(HttpServletResponse response,
 			TypeNameExtractor extractor, ProxyInitializer initializer) {
 		
 		super(response, extractor, initializer);
 	}
 	
 	@Override
 	protected XStream getXStream() {
 		XStream xStream = new XStream(new JettisonMappedXmlDriver());
 		xStream.processAnnotations(types);
 		return xStream;
 	}
 	
 	public <T extends Object> Serializer from(T object) {
 		Set<Class<?>> types = new HashSet<Class<?>>();
 		types.add(object.getClass());
 		
 		if (object instanceof Collection) {
 			Collection<?> collection = (Collection<?>) object;
 			for (Object o : collection) {
 				types.add(o.getClass());
 			}
 			this.types = types.toArray(new Class[]{});
 		}
 		return super.from(object);
 	}
 }

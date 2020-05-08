 package pl.radical.open.gg.packet.handlers;
 
 import pl.radical.open.gg.GGException;
 import pl.radical.open.gg.packet.IncomingPacket;
 import pl.radical.open.gg.utils.GGUtils;
 
 import java.util.HashMap;
 import java.util.Set;
 
 import org.reflections.Configuration;
 import org.reflections.Reflections;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.FilterBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Predicate;
 
 /**
  * Created on 2004-11-27
  * 
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
  * @author <a href="mailto:lukasz.rzanek@radical.com.pl>Łukasz Rżanek</a>
  */
 public class PacketChain {
 	private final static Logger log = LoggerFactory.getLogger(PacketChain.class);
 
 	private final HashMap<Integer, PacketHandler> m_packetHandlers = new HashMap<Integer, PacketHandler>();
 
 	public PacketChain() throws GGException {
 		registerDefaultHandlers();
 	}
 
 	public void registerGGPackageHandler(final int packetType, final PacketHandler packetHandler) {
 		if (packetHandler == null) {
 			throw new IllegalArgumentException("packetHandler cannot be null");
 		}
 		m_packetHandlers.put(Integer.valueOf(packetType), packetHandler);
 	}
 
 	public void registerGGPackageHandler(final int packetType, final Class<?> packetHandler) throws GGException {
 		if (packetHandler == null) {
 			throw new IllegalArgumentException("packetHandler cannot be null");
 		}
 
 		try {
 			m_packetHandlers.put(Integer.valueOf(packetType), (PacketHandler) packetHandler.newInstance());
 		} catch (final InstantiationException e) {
 			log.error("Unable to create an object of type {}", packetHandler.getClass().getName(), e);
 			throw new GGException("Unable to create an object of type " + packetHandler.getClass().getName(), e);
 		} catch (final IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void sendToChain(final PacketContext packageContent) throws GGException {
 		final PacketHandler packetHandler = m_packetHandlers.get(Integer.valueOf(packageContent.getHeader().getType()));
 		if (packetHandler == null) {
 			log.warn("Unknown package.");
 			log.warn("PacketHeader: " + packageContent.getHeader());
 			log.warn("PacketBody: " + GGUtils.prettyBytesToString(packageContent.getPackageContent()));
 			return;
 		}
 
 		packetHandler.handle(packageContent);
 	}
 
 	private void registerDefaultHandlers() throws GGException {
		final Predicate<String> filter = new FilterBuilder().include("pl\\.radical\\.open\\.gg\\.packet\\.in.*");
 		final Configuration configuration = new ConfigurationBuilder()
 		.filterInputsBy(filter)
 		.setScanners(new TypeAnnotationsScanner())
 		.setUrls(ClasspathHelper.getUrlForClass(IncomingPacket.class));
 		final Reflections reflections = new Reflections(configuration);
 
 		final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(IncomingPacket.class);
 
 		if (classes.size() == 0) {
			log.error("Nie znalazłem żadnych klas do rejestracji!");
 		}
 
 		for (final Class<?> c : classes) {
 			final String incomingClass = c.getName();
 			final IncomingPacket annotation = c.getAnnotation(IncomingPacket.class);
 			final Class<?> handler = c.getAnnotation(IncomingPacket.class).handler();
 
 			if (log.isDebugEnabled()) {
 				log.debug("Registering class {} with handler {}", incomingClass, handler.getName());
 			}
 
 			registerGGPackageHandler(annotation.type(), handler);
 		}
 	}
 }

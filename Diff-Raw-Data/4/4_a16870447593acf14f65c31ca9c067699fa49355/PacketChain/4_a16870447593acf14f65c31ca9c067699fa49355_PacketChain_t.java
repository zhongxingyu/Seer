 package pl.radical.open.gg.packet.handlers;
 
 import pl.radical.open.gg.GGException;
 import pl.radical.open.gg.packet.IncomingPacket;
 import pl.radical.open.gg.utils.GGUtils;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.ClassUtils;
 import org.reflections.Configuration;
 import org.reflections.Reflections;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 
 import de.huxhorn.lilith.slf4j.Logger;
 import de.huxhorn.lilith.slf4j.LoggerFactory;
 
 /**
  * Created on 2004-11-27
  * 
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
  * @author <a href="mailto:lukasz.rzanek@radical.com.pl>�?ukasz Rżanek</a>
  */
 public class PacketChain {
 	private static final Logger LOG = LoggerFactory.getLogger(PacketChain.class);
 
 	private final Map<Integer, PacketHandler> packetHandlers = new HashMap<Integer, PacketHandler>();
 
 	public PacketChain() throws GGException {
 		registerDefaultHandlers();
 	}
 
 	public void registerGGPackageHandler(final int packetType, final PacketHandler packetHandler) {
 		if (packetHandler == null) {
 			throw new IllegalArgumentException("packetHandler cannot be null");
 		}
 		packetHandlers.put(Integer.valueOf(packetType), packetHandler);
 	}
 
 	public void registerGGPackageHandler(final int packetType, final Class<?> packetHandler) throws GGException {
 		if (packetHandler == null) {
 			throw new IllegalArgumentException("packetHandler cannot be null");
 		}
 
 		try {
 			packetHandlers.put(Integer.valueOf(packetType), (PacketHandler) packetHandler.newInstance());
 		} catch (final InstantiationException e) {
 			LOG.error("Unable to create an object of type {}", packetHandler.getClass().getName(), e);
 			throw new GGException("Unable to create an object of type " + packetHandler.getClass().getName(), e);
 		} catch (final IllegalAccessException e) {
			LOG.error("Inproper use of object.newInstance()", e);
			throw new GGException("Inproper use of object.newInstance()", e);
 		}
 	}
 
 	public void sendToChain(final PacketContext packageContent) throws GGException {
 		final PacketHandler packetHandler = packetHandlers.get(Integer.valueOf(packageContent.getHeader().getType()));
 		if (packetHandler == null) {
 			LOG.warn("Unknown package.");
 			LOG.warn("PacketHeader: " + packageContent.getHeader());
 			LOG.warn("PacketBody: " + GGUtils.prettyBytesToString(packageContent.getPackageContent()));
 			return;
 		}
 
 		packetHandler.handle(packageContent);
 	}
 
 	protected void registerDefaultHandlers() throws GGException {
 		final Configuration configuration = new ConfigurationBuilder().setScanners(new TypeAnnotationsScanner()).setUrls(ClasspathHelper
 				.getUrlsForPackagePrefix("pl.radical.open.gg.packet.in"));
 		final Reflections reflections = new Reflections(configuration);
 
 		final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(IncomingPacket.class);
 
 		if (classes.size() == 0) {
 			throw new GGException("No classes found to register as packet handlers!");
 		}
 
 		for (final Class<?> c : classes) {
 			final IncomingPacket annotation = c.getAnnotation(IncomingPacket.class);
 
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("Registering class {} with handler {} for packet [{}]", ClassUtils.getShortClassName(c.getName()), ClassUtils
 						.getShortClassName(annotation.handler().getName()), Integer.toString(c.getAnnotation(IncomingPacket.class).type()));
 			}
 
 			registerGGPackageHandler(annotation.type(), annotation.handler());
 		}
 	}
 }

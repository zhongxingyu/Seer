 package pl.radical.open.gg.packet.handlers;
 
 import pl.radical.open.gg.GGException;
 import pl.radical.open.gg.packet.in.GGLogin80OK;
 import pl.radical.open.gg.utils.GGUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Created on 2004-11-27
  * 
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
  */
 public class GGLogin80OKPacketHandler implements PacketHandler {
 	private static final Logger log = LoggerFactory.getLogger(GGLogin80OKPacketHandler.class);
 
 	/**
 	 * @see pl.radical.open.gg.packet.handlers.PacketHandler#handle(pl.radical.open.gg.packet.handlers.Context)
 	 */
 	public void handle(final PacketContext context) throws GGException {
 		if (log.isDebugEnabled()) {
			log.debug("LoginOK80 packet received.");
 			log.debug("PacketHeader: " + context.getHeader());
 			log.debug("PacketBody: " + GGUtils.prettyBytesToString(context.getPackageContent()));
 		}
 
 		final GGLogin80OK loginOk = GGLogin80OK.getInstance();
 		context.getSessionAccessor().notifyGGPacketReceived(loginOk);
 		context.getSessionAccessor().notifyLoginOK();
 	}
 
 }

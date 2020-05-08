 package pt.com.broker.types.channels;
 
 import java.net.InetSocketAddress;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 
 public class ChannelAttributes
 {
 	private static final ConcurrentMap<String, Map<String, Object>> PROPS = new ConcurrentHashMap<String, Map<String, Object>>();
 
 	public static void set(String channelId, String name, Object value)
 	{
 		if (channelId == null)
 		{
 			return;
 		}
 		Map<String, Object> attribs = PROPS.get(channelId);
 
 		if (attribs == null)
 		{
			attribs = new ConcurrentHashMap<String, Object>();
 			PROPS.put(channelId, attribs);
 		}
 		attribs.put(name, value);
 	}
 
 	public static Object get(String channelId, String name)
 	{
 		if (channelId == null)
 		{
 			return null;
 		}
 		Map<String, Object> attribs = PROPS.get(channelId);
 		if (attribs == null)
 		{
 			return null;
 		}
 		return attribs.get(name);
 	}
 
 	public static void remove(String channelId)
 	{
 		PROPS.remove(channelId);
 	}
 
 	public static Set<String> getAttributeKeys(String channelId)
 	{
 		Map<String, Object> attribs = PROPS.get(channelId);
 		if (attribs != null)
 		{
 			return attribs.keySet();
 		}
 		else
 		{
 			return Collections.emptySet();
 		}
 	}
 
 	public static String getChannelId(ChannelHandlerContext ctx)
 	{
 		return getChannelId(ctx.getChannel());
 	}
 
 	public static String getChannelId(Channel channel)
 	{
 		InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
 		if (remoteAddress == null)
 		{
 			return null;
 		}
 		String channelId = remoteAddress.toString();
 		return channelId;
 	}
 }

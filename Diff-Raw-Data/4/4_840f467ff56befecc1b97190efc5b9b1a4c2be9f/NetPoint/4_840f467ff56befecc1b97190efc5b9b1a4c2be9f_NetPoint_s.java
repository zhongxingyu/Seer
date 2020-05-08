 package com.barchart.netty.util.point;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.barchart.netty.util.entry.Entry;
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigFactory;
 import com.typesafe.config.ConfigValueFactory;
 
 public class NetPoint extends Entry implements NetKey {
 
 	public static List<NetPoint> form(final List<? extends Config> confList) {
 
 		final List<NetPoint> pointList =
 				new ArrayList<NetPoint>(confList.size());
 
 		for (final Config config : confList) {
 			final NetPoint point = from(config);
 			pointList.add(point);
 		}
 
 		return pointList;
 
 	}
 
 	public static NetPoint from(final Config config) {
 		return new NetPoint(config.withFallback(reference()));
 	}
 
 	public static NetPoint from(final String hocon) {
 		return from(ConfigFactory.parseString(hocon));
 	}
 
 	public static Config reference() {
 		return ConfigFactory.defaultReference(NetPoint.class.getClassLoader())
 				.getConfig("net-point");
 	}
 
 	protected NetPoint(final Config config) {
 		super(config);
 	}
 
 	public String getId() {
 		return config().getString(KEY_ID);
 	}
 
 	public NetAddress getLocalAddress() {
 		return NetAddress.formTuple(config().getString(KEY_LOCAL_ADDRESS));
 	}
 
 	public int getPacketTTL() {
 		return config().getInt(KEY_PACKET_TTL);
 	}
 
 	public String getPipeline() {
 		return config().getString(KEY_PIPELINE);
 	}
 
	public String getPipelineTimeout() {
		return config().getString(KEY_PIPELINE_TIMEOUT);
 	}
 
 	public int getReceiveBufferSize() {
 		return config().getInt(KEY_RECV_BUF_SIZE);
 	}
 
 	public NetAddress getRemoteAddress() {
 		return NetAddress.formTuple(config().getString(KEY_REMOTE_ADDRESS));
 	}
 
 	public int getSendBufferSize() {
 		return config().getInt(KEY_SEND_BUF_SIZE);
 	}
 
 	public String getType() {
 		return config().getString(KEY_TYPE);
 	}
 
 	public NetPoint with(final String path, final Object value) {
 		return from(config().withValue(path,
 				ConfigValueFactory.fromAnyRef(value)));
 	}
 
 }

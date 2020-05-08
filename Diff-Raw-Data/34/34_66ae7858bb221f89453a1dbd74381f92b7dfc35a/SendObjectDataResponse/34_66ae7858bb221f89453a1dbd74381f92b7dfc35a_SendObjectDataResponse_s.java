 package com.fathomdb.proxy.http.handlers;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import com.fathomdb.proxy.http.client.TaskWithFuture;
 import com.fathomdb.proxy.objectdata.ObjectDataSink;
 
 public class SendObjectDataResponse extends TaskWithFuture implements
 		ObjectDataSink {
 
 	public SendObjectDataResponse(Channel channel) {
 		super(channel);
 	}
 
 	@Override
 	public void gotData(ChannelBuffer content) {
		HttpChunk chunk = new DefaultHttpChunk(content);
		getChannel().write(chunk);
 	}
 
 	@Override
 	public void beginResponse(HttpResponse response) {
		getChannel().write(response);
 	}
 
 	@Override
 	public void endData() {

 	}
 
 }

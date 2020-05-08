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
 
	// There's a pain-point / catch here...
	// we don't do GZIP compression unless the initial response has content
	// So we need to hold back the response until the first bit of data comes
	int dataCount = 0;
	HttpResponse response;

 	@Override
 	public void gotData(ChannelBuffer content) {
		if (dataCount == 0) {
			sendResponse(response, content);
		} else {
			HttpChunk chunk = new DefaultHttpChunk(content);
			getChannel().write(chunk);
		}

		dataCount++;
 	}
 
 	@Override
 	public void beginResponse(HttpResponse response) {
		this.response = response;
 	}
 
 	@Override
 	public void endData() {
		if (dataCount == 0) {
			sendResponse(response, null);
		}
 	}
 
	public void sendResponse(HttpResponse response, ChannelBuffer content) {
		if (content != null) {
			if (response.getContent() != null && response.getContent().readable())
				throw new IllegalStateException();
			response.setContent(content);
		}
		getChannel().write(response);
	}
 }
